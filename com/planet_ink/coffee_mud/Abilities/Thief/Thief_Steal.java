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
public class Thief_Steal extends ThiefSkill
{
	public String ID() { return "Thief_Steal"; }
	public String name(){ return "Steal";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	private static final String[] triggerStrings = {"STEAL"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	private DVector lastOnes=new DVector(2);
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("Steal what from whom?");
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
		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());

		if((!target.mayIFight(mob))&&(levelDiff<10))
		{
			mob.tell("You cannot steal from "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Item stolen=target.fetchCarried(null,itemToSteal);

		int discoverChance=(target.charStats().getStat(CharStats.WISDOM)*5)-(levelDiff*5);
		int times=timesPicked(target);
		if(times>5) discoverChance-=(20*(times-5));
		if(!Sense.canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;

		if(levelDiff>0)
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((!Sense.canBeSeenBy(mob,target))?1:2));
		boolean success=profficiencyCheck(mob,levelDiff,auto);

		if(!success)
		{
			if(Dice.rollPercentage()>discoverChance)
			{
				FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to steal; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to steal from you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to steal from <T-NAME> and fails!");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":"You fumble the attempt to steal.");
		}
		else
		{
			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
				if((stolen!=null)&&(stolen.amWearingAt(Item.INVENTORY)))
					str="<S-NAME> steal(s) "+stolen.name()+" from <T-NAMESELF>.";
				else
				{
					code=CMMsg.MSG_QUIETMOVEMENT;
					str="<S-NAME> attempt(s) to steal from <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";
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
					if(mob.getVictim()==target)
						mob.makePeace();
				}
				msg=new FullMsg(target,stolen,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(mob,msg);
					msg=new FullMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		return success;
	}

}
