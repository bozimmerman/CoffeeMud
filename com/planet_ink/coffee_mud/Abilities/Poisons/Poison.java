package com.planet_ink.coffee_mud.Abilities.Poisons;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Poison extends StdAbility
{
	public String ID() { return "Poison"; }
	public String name(){ return "Poison";}
	public String displayText(){ return "(Poisoned)";}
	protected int canAffectCode(){return CAN_MOBS|CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"POISONSTING"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_POISON;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	protected int POISON_TICKS(){return 0;} // 0 means no adjustment!
	protected int POISON_DELAY(){return 3;}
	protected String POISON_DONE(){return "The poison runs its course.";}
	protected String POISON_START(){return "^G<S-NAME> turn(s) green.^?";}
	protected String POISON_START_TARGETONLY(){return "";}
	protected boolean POISON_AFFECTTARGET(){return true;}
	protected String POISON_AFFECT(){return "<S-NAME> cringe(s) as the poison courses through <S-HIS-HER> blood.";}
	protected String POISON_CAST(){return "^F^<FIGHT^><S-NAME> attempt(s) to poison <T-NAMESELF>!^</FIGHT^>^?";}
	protected String POISON_FAIL(){return "<S-NAME> attempt(s) to poison <T-NAMESELF>, but fail(s).";}
	protected int POISON_DAMAGE(){return (invoker!=null)?CMLib.dice().roll(1,invoker().envStats().level(),1):0;}
    protected boolean processing=false;

	protected int poisonTick=3;

	protected boolean catchIt(MOB mob, Environmental target)
	{
		MOB poisoner=invoker;
		if(poisoner==null) poisoner=mob;
		if((poisoner==null)&&(target instanceof MOB)) poisoner=(MOB)target;
		if((target!=null)&&(target instanceof MOB)&&(target.fetchEffect(ID())==null))
		{
			MOB targetMOB=(MOB)target;
			if(POISON_START_TARGETONLY().length()>0)
			    targetMOB.tell(POISON_START_TARGETONLY());
            String startMsg=CMStrings.replaceAll(POISON_START(),"<S-","<T-");
            CMMsg msg=CMClass.getMsg(poisoner,targetMOB,this,CMMsg.MASK_ALWAYS|CMMsg.MASK_MALICIOUS|CMMsg.TYP_POISON,startMsg);
			if((POISON_START_TARGETONLY().length()>0)
			||((targetMOB.location()!=null)
    			&&(targetMOB.location().okMessage(targetMOB,msg))))
			{
                targetMOB.location().send(targetMOB,msg);
			    if((POISON_AFFECTTARGET()&&(msg.value()<=0)))
			    {
					maliciousAffect(poisoner,target,
									(affected instanceof Item)?affected.envStats().level():0,
									POISON_TICKS(),-1);
					return true;
			    }
		        return false;
			}
		}
		return false;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if(mob==null) return false;
		if((--poisonTick)<=0)
		{
			poisonTick=POISON_DELAY();
			if(POISON_AFFECT().length()>0)
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,POISON_AFFECT()+CMProps.msp("poisoned.wav",10));
			if(invoker==null) invoker=mob;
			if(POISON_DAMAGE()!=0)
            {
				CMLib.combat().postDamage(invoker,mob,this,POISON_DAMAGE(),CMMsg.MASK_ALWAYS|CMMsg.TYP_POISON,-1,null);
                if((!mob.isInCombat())&&(mob!=invoker)&&(mob.location()!=null)&&(mob.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,mob)))
                    CMLib.combat().postAttack(mob,invoker,mob.fetchWieldedItem());
            }
		}
		return true;
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		if((affected==null)||(!ID().equals("Poison"))) return;
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)-5);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-5);
		if(affectableStats.getStat(CharStats.STAT_CONSTITUTION)<=0)
			affectableStats.setStat(CharStats.STAT_CONSTITUTION,1);
		if(affectableStats.getStat(CharStats.STAT_STRENGTH)<=0)
			affectableStats.setStat(CharStats.STAT_STRENGTH,1);
	}

	public void unInvoke()
	{
		if(affected!=null)
		{
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;

				super.unInvoke();
				if((canBeUninvoked())&&(POISON_DONE().length()>0))
					mob.tell(POISON_DONE());
			}
			else
			if(invoker!=null)
				super.unInvoke();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return;
		if(affected instanceof Item)
		{
			if(!processing)
			{
				Item myItem=(Item)affected;
				if(myItem.owner()==null) return;
				processing=true;
				if(msg.amITarget(myItem))
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_DRINK:
						if(myItem instanceof Drink)
                        {
							catchIt(msg.source(),msg.source());
                            if(((((Drink)myItem).liquidRemaining()<0)
                                ||((((Drink)myItem).liquidRemaining()-((Drink)myItem).thirstQuenched())<=0))
                            &&(!((Drink)myItem).disappearsAfterDrinking()))
                                affected.delEffect(this);
                        }
						break;
					case CMMsg.TYP_EAT:
						if(myItem instanceof Food)
							catchIt(msg.source(),msg.source());
						break;
					}
				else
				if(msg.tool()==affected)
					switch(msg.targetMinor())
					{
					case CMMsg.TYP_DAMAGE:
						if((msg.source()!=msg.target())
						&&(msg.target()!=null)
						&&(myItem instanceof Weapon)
						&&(msg.target() instanceof MOB))
						{
							tickDown--;
							catchIt((MOB)msg.source(),msg.target());
						}
						break;
                    case CMMsg.TYP_FILL:
                        if((msg.target() instanceof Drink)
                        &&(affected instanceof Drink))
                        {
                            msg.target().addEffect((Ability)this.copyOf());
                            if((((Drink)affected).liquidRemaining()-((Drink)msg.target()).amountTakenToFillMe((Drink)affected))<=0)
                                affected.delEffect(this);
                        }
                        break;
					}
			}
			processing=false;
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Environmental target=this.getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(target==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			String str=auto?"":POISON_CAST();
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_POISON|(auto?CMMsg.MASK_ALWAYS:0),str);
            CMLib.color().fixSourceFightColor(msg);
			Room R=mob.location();
			if((target instanceof MOB)&&(((MOB)target).location()!=null))
				R=((MOB)target).location();
			if(R.okMessage(mob,msg))
			{
			    R.send(mob,msg);
				if(msg.value()<=0)
				{
					if(target instanceof MOB)
					{
					    if(POISON_START_TARGETONLY().length()>0)
							((MOB)target).tell(POISON_START_TARGETONLY());
					    else
							R.show((MOB)target,null,CMMsg.MSG_OK_VISUAL,POISON_START());
						if(POISON_AFFECTTARGET())
						    success=maliciousAffect(mob,target,asLevel,POISON_TICKS(),-1);
						else
						    success=true;
					}
                    else
                        success=maliciousAffect(mob,target,asLevel,100,-1);
				}
				else
					success=false;
			}
			else
				success=false;
		}
		else
			return maliciousFizzle(mob,target,POISON_FAIL());

        return success;

	}

}
