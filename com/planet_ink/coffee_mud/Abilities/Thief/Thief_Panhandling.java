package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/*
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Thief_Panhandling extends ThiefSkill
{
	public String ID() { return "Thief_Panhandling"; }
	public String name(){ return "Panhandling";}
	public String displayText(){return "(Panhandling)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"PANHANDLE","PANHANDLING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	Vector mobsHitUp=new Vector();
	int tickTock=0;

	public void executeMsg(Environmental oking, CMMsg msg)
	{
		super.executeMsg(oking,msg);
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			if((msg.source()==mob)
			&&(msg.target()==mob.location())
			&&(msg.targetMinor()==CMMsg.TYP_LEAVE))
				unInvoke();
			else
			if((Sense.isStanding(mob))||(Sense.isSleeping(mob)))
				unInvoke();
			else
			if((msg.amITarget(mob))&&(msg.targetMinor()==CMMsg.TYP_GIVE))
				msg.addTrailerMsg(new FullMsg(mob,msg.source(),CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Thank you gov'ner!' to <T-NAME> ^?"));
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(affected instanceof MOB)
		{
			tickTock++;
			if(tickTock<2) return true;
			tickTock=0;
			MOB mob=(MOB)affected;
			for(int i=0;i<mob.location().numInhabitants();i++)
			{
				MOB mob2=mob.location().fetchInhabitant(i);
				if((mob2!=null)
				&&(Sense.canBeSeenBy(mob2,mob))
				&&(mob2!=mob)
				&&(!mobsHitUp.contains(mob2))
				&&(profficiencyCheck(mob,0,false)))
				{
					switch(Dice.roll(1,10,0))
					{
					case 1:
						CommonMsgs.say(mob,mob2,"A little something for a vet please?",false,false);
						break;
					case 2:
						CommonMsgs.say(mob,mob2,"Spare a gold piece "+((mob2.charStats().getStat(CharStats.GENDER)=='M')?"mister?":"madam?"),false,false);
						break;
					case 3:
						CommonMsgs.say(mob,mob2,"Spare some change?",false,false);
						break;
					case 4:
						CommonMsgs.say(mob,mob2,"Please "+((mob2.charStats().getStat(CharStats.GENDER)=='M')?"mister":"madam")+", a little something for an poor soul down on "+mob.charStats().hisher()+" luck?",false,false);
						break;
					case 5:
						CommonMsgs.say(mob,mob2,"Hey, I lost my 'Will Work For Food' sign.  Can you spare me the money to buy one?",false,false);
						break;
					case 6:
						CommonMsgs.say(mob,mob2,"Spread a little joy to an poor soul?",false,false);
						break;
					case 7:
						CommonMsgs.say(mob,mob2,"Change?",false,false);
						break;
					case 8:
						CommonMsgs.say(mob,mob2,"Can you spare a little change?",false,false);
						break;
					case 9:
						CommonMsgs.say(mob,mob2,"Can you spare a little gold?",false,false);
						break;
					case 10:
						CommonMsgs.say(mob,mob2,"Gold piece for a poor soul down on "+mob.charStats().hisher()+" luck?",false,false);
						break;
					}
                    // if align is enabled AND they're good AND they make a justice save
                    // OR
                    // align is disabled and they make a justice save
                    if( ( (Factions.isAlignEnabled())
                         &&((Dice.rollPercentage()*10)<(Factions.getPercent(Factions.AlignID(),mob.fetchFaction(Factions.AlignID()))))
                         &&(Dice.rollPercentage()>mob2.charStats().getSave(CharStats.SAVE_JUSTICE)))
                        ||(!(Factions.isAlignEnabled())
                         &&(Dice.rollPercentage()>mob2.charStats().getSave(CharStats.SAVE_JUSTICE))))
					{
					    double total=BeanCounter.getTotalAbsoluteNativeValue(mob2);
					    if(total>1.0)
					    {
						    total=total/20.0;
						    if(total<1.0) total=1.0;
							Coins C=BeanCounter.makeBestCurrency(mob2,total);
							if(C!=null)
							{
								BeanCounter.subtractMoney(mob2,total);
								mob2.addInventory(C);
								mob2.doCommand(Util.parse("GIVE \""+C.name()+"\" \""+mob.Name()+"\""));
								if(!C.amDestroyed()) C.putCoinsBack();
							}
					    }
					}

					mobsHitUp.addElement(mob2);
					break;
				}
			}
			if((mobsHitUp.size()>0)&&(Dice.rollPercentage()<10))
				mobsHitUp.removeElementAt(0);
		}
		return true;
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if((canBeUninvoked())&&(mob.location()!=null))
			mob.tell("You stop panhandling.");
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(target,null,null,"<S-NAME> <S-IS-ARE> already panhandling.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!Sense.isSitting(mob))
		{
			mob.tell("You must be sitting!");
			return false;
		}
		if(mob.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)
		{
			mob.tell("You must be on a city street to panhandle.");
			return false;
		}

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,null,this,auto?CMMsg.MASK_GENERAL:CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,auto?"":"<S-NAME> start(s) panhandling.");
		if(!success)
			return beneficialVisualFizzle(mob,null,auto?"":"<S-NAME> can't seem to get <S-HIS-HER> panhandling act started.");
		else
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,target,asLevel,0);
		}
		return success;
	}
}
