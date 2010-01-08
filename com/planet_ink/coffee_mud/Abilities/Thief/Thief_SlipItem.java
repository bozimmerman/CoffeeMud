package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Thief_SlipItem extends ThiefSkill
{
	public String ID() { return "Thief_SlipItem"; }
	public String name(){ return "Slip Item";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}
	private static final String[] triggerStrings = {"SLIPITEM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
                if((target==null)||(((MOB)target).amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
                    return Ability.QUALITY_INDIFFERENT;
                Item w=mob.fetchWieldedItem();
                if((w==null)||(w.minRange()>0)||(w.maxRange()>0))
                    return Ability.QUALITY_INDIFFERENT;
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        String itemToSteal="all";
        if(!auto)
        {
    		if(commands.size()<2)
    		{
    			mob.tell("Slip what off of whom?");
    			return false;
    		}
    		itemToSteal=(String)commands.elementAt(0);
        }

        MOB target=null;
        if((target==null)&&(givenTarget!=null)&&(givenTarget instanceof MOB)) 
            target=(MOB)givenTarget;
        else
            target=mob.location().fetchInhabitant(CMParms.combine(commands,1));
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+CMParms.combine(commands,1)+"' here.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));

		if((!target.mayIFight(mob))||(levelDiff>15))
		{
			mob.tell("You cannot slip anything off of "+target.charStats().himher()+".");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Item stolen=target.fetchWornItem(itemToSteal);
		if((stolen==null)||(!CMLib.flags().canBeSeenBy(stolen,mob)))
		{
			mob.tell(target.name()+" doesn't seem to be wearing '"+itemToSteal+"'.");
			return false;
		}
		if(stolen.amWearingAt(Wearable.WORN_WIELD))
		{
			mob.tell(target.name()+" is wielding "+stolen.name()+"! Try disarm!");
			return false;
		}

		int discoverChance=(target.charStats().getStat(CharStats.STAT_WISDOM)*5)
							-(levelDiff*5)
							+(getX1Level(mob)*5);
		if(!CMLib.flags().canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;

		if(levelDiff>0)
			levelDiff=-(levelDiff*((!CMLib.flags().canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((!CMLib.flags().canBeSeenBy(mob,target))?1:2));
		boolean success=proficiencyCheck(mob,levelDiff,auto);

		if(!success)
		{
			if(CMLib.dice().rollPercentage()>discoverChance)
			{
				CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the attempt to slip "+stolen.name()+" off <T-NAME>; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to slip "+stolen.name()+" off you and fails!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to slip "+stolen.name()+" off <T-NAME> and fails!");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":"You fumble the attempt to slip "+stolen.name()+" off "+target.name()+".");
		}
		else
		{
			String str=null;
			if(!auto)
				if(!stolen.amWearingAt(Wearable.IN_INVENTORY))
					str="<S-NAME> slip(s) "+stolen.name()+" off <T-NAMESELF>.";
				else
					str="<S-NAME> attempt(s) to slip "+stolen.name()+" off <T-HIM-HER>, but it doesn't appear "+target.charStats().heshe()+" has that in <T-HIS-HER> inventory!";

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(CMLib.dice().rollPercentage()<discoverChance)
				hisStr=null;
			else
				hisCode=hisCode|((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);

			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
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
				&&(CMLib.dice().rollPercentage()>stolen.envStats().level()))
				{
					if(target.getVictim()==mob)
						target.makePeace();
				}
				msg=CMClass.getMsg(target,stolen,null,CMMsg.MSG_REMOVE,CMMsg.MSG_REMOVE,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
					target.location().send(mob,msg);
			}
		}
		return success;
	}
}
