package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Thief_Robbery extends ThiefSkill
{
	public String ID() { return "Thief_Robbery"; }
	public String name(){ return "Robbery";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"ROBBERY","ROB"};
	public String[] triggerStrings(){return triggerStrings;}
	public Vector mobs=new Vector();
	private DVector lastOnes=new DVector(2);
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	private int timesPicked(MOB target)
	{
		int times=0;
		for(int x=0;x<lastOnes.size();x++)
		{
			MOB M=(MOB)lastOnes.elementAt(x,1);
			Integer I=(Integer)lastOnes.elementAt(x,2);
			if(M==target)
			{
				times=I.intValue();
				lastOnes.removeElement(M);
				break;
			}
		}
		if(lastOnes.size()>=50)
			lastOnes.removeElementAt(0);
		lastOnes.addElement(target,new Integer(times+1));
		return times+1;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.amITarget(affected))
		   &&(mobs.contains(msg.source())))
		{
			if((msg.targetMinor()==CMMsg.TYP_BUY)
			   ||(msg.targetMinor()==CMMsg.TYP_SELL)
			   ||(msg.targetMinor()==CMMsg.TYP_LIST)
			   ||(msg.targetMinor()==CMMsg.TYP_VALUE)
			   ||(msg.targetMinor()==CMMsg.TYP_VIEW))
			{
				msg.source().tell(affected.name()+" looks unwilling to do business with you.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			mob.tell("Rob what from whom?");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}

		String itemToSteal=(String)commands.elementAt(0);

		MOB target=mob.location().fetchInhabitant(Util.combine(commands,1));
		if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) target=(MOB)givenTarget;
		if((target==null)||(target.amDead())||(!Sense.canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+Util.combine(commands,1)+"' here.");
			return false;
		}
		int levelDiff=target.envStats().level()-mob.envStats().level();

		if(((!target.mayIFight(mob))&&(levelDiff<10))||(CoffeeUtensils.getShopKeeper(target)==null))
		{
			mob.tell("You cannot rob from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		ShopKeeper shop=CoffeeUtensils.getShopKeeper(target);
		Environmental stolen=shop.getStock(itemToSteal,mob);
		if(stolen!=null)
		{
			if((stolen instanceof Ability)
			||(stolen instanceof MOB)
			||(stolen instanceof Room)
			||(stolen instanceof LandTitle))
			{
				mob.tell(mob,target,stolen,"You cannot rob '<O-NAME>' from <T-NAME>.");
				return false;
			}
			if(!shop.doIHaveThisInStock(stolen.Name(),mob))
				stolen=null;
		}

		int discoverChance=(mob.charStats().getStat(CharStats.CHARISMA)-target.charStats().getStat(CharStats.WISDOM))*5;
		int times=timesPicked(target);
		if(times>5) discoverChance-=(20*(times-5));
		if(!Sense.canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;
		boolean success=profficiencyCheck(mob,-(levelDiff),auto);

		if(!success)
		{
			if(Dice.rollPercentage()>discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to rob <T-NAMESELF>; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to rob you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to rob <T-NAME> and fails!");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
				Thief_Robbery A=(Thief_Robbery)target.fetchEffect(ID());
				if(A==null)
				{
					mobs.clear();
					mobs.addElement(mob);
					beneficialAffect(mob,target,0);
				}
				else
					A.mobs.addElement(mob);
			}
			else
				mob.tell(mob,target,null,auto?"":"You fumble the attempt to rob <T-NAME>.");
		}
		else
		{
			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
				if(stolen!=null)
					str="<S-NAME> rob(s) "+stolen.name()+" from <T-NAMESELF>.";
				else
				{
					str="<S-NAME> attempt(s) to rob <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";
					code=CMMsg.MSG_QUIETMOVEMENT;
				}

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(Dice.rollPercentage()<discoverChance)
				hisStr=null;
			else
			{
				str+=" <T-NAME> spots you!";
				hisCode=hisCode|((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);
			}

			FullMsg msg=new FullMsg(mob,target,this,code,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Thief_Robbery A=(Thief_Robbery)target.fetchEffect(ID());
				if(A==null)	beneficialAffect(mob,target,0);
				A=(Thief_Robbery)target.fetchEffect(ID());
				if(A!=null)
					A.mobs.addElement(mob);
				
				if((!target.isMonster())&&(mob.isMonster())&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
				}
				else
				if(((hisStr==null)||mob.isMonster())
				&&(!alreadyFighting)
				&&((stolen==null)||(Dice.rollPercentage()>stolen.envStats().level())))
				{
					if(target.getVictim()==mob)
						target.makePeace();
				}
				if(stolen!=null)
				{
					Vector products=shop.removeSellableProduct(stolen.Name(),mob);
					stolen=(Environmental)products.firstElement();
					if(stolen instanceof Item)
					{
						mob.location().addItemRefuse((Item)stolen,Item.REFUSE_PLAYER_DROP);
						msg=new FullMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
						if(mob.location().okMessage(mob,msg))
							mob.location().send(mob,msg);
					}
				}
			}
		}
		return success;
	}

}
