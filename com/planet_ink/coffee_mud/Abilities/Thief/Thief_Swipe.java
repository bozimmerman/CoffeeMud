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
public class Thief_Swipe extends ThiefSkill
{
	public String ID() { return "Thief_Swipe"; }
	public String name(){ return "Swipe gold";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;}
	private static final String[] triggerStrings = {"SWIPE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	private DVector lastOnes=new DVector(2);
	protected int timesPicked(MOB target)
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
		lastOnes.addElement(target,Integer.valueOf(times+1));
		return times+1;
	}


    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(!(target instanceof MOB))
                return Ability.QUALITY_INDIFFERENT;
            if((target==null)||((MOB)target).amDead()||(!CMLib.flags().canBeSeenBy(target,mob)))
                return Ability.QUALITY_INDIFFERENT;
            if((mob.isInCombat())&&(CMLib.flags().aliveAwakeMobile((MOB)target,true)||(mob.getVictim()!=target)))
                return Ability.QUALITY_INDIFFERENT;
            if(!((MOB)target).mayIFight(mob))
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Swipe from whom?");
			return false;
		}
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if((mob.isInCombat())&&(CMLib.flags().aliveAwakeMobile(target,true)||(mob.getVictim()!=target)))
		{
			mob.tell(mob,mob.getVictim(),null,"Not while you are fighting <T-NAME>!");
			return false;
		}

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));
		if((!target.mayIFight(mob))||(levelDiff>15))
		{
			mob.tell("You cannot swipe from "+target.charStats().himher()+".");
			return false;
		}
		if(target==mob)
		{
			mob.tell("You cannot swipe from yourself.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		String currency=CMLib.beanCounter().getCurrency(target);
		int discoverChance=(target.charStats().getStat(CharStats.STAT_WISDOM)*5)
							-(levelDiff*3)
							+(getX1Level(mob)*5);
		int times=timesPicked(target);
		if(times>5) discoverChance-=(20*(times-5));
		if(!CMLib.flags().canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95) discoverChance=95;
		if(discoverChance<5) discoverChance=5;

		if(levelDiff>0)
			levelDiff=-(levelDiff*((!CMLib.flags().canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((!CMLib.flags().canBeSeenBy(mob,target))?1:2));
		if(!CMLib.flags().aliveAwakeMobile(target,true)){levelDiff=100;discoverChance=0;}
		boolean success=proficiencyCheck(mob,levelDiff,auto);

		if(!success)
		{
			if(CMLib.dice().rollPercentage()>discoverChance)
			{
				if((target.isMonster())&&(mob.getVictim()==null)) mob.setVictim(target);
				CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":"You fumble the swipe; <T-NAME> spots you!",CMMsg.MSG_NOISYMOVEMENT,auto?"":"<S-NAME> tries to pick your pocket and fails!",CMMsg.MSG_OK_VISUAL,auto?"":"<S-NAME> tries to pick <T-NAME>'s pocket and fails!");
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":"You fumble the swipe.");
		}
		else
		{
			double pct=0.25;
			if(levelDiff>0) pct=0.15;
			if(levelDiff>5) pct=0.10;
			if(levelDiff>10) pct=0.05;
			double goldTaken=CMLib.beanCounter().getTotalAbsoluteNativeValue(target)*pct*Math.random();
			if(goldTaken<((double)CMLib.ableMapper().qualifyingClassLevel(mob,this)))
				goldTaken=(double)CMLib.ableMapper().qualifyingClassLevel(mob,this);
			if(goldTaken>CMLib.beanCounter().getTotalAbsoluteNativeValue(target)) goldTaken=CMLib.beanCounter().getTotalAbsoluteNativeValue(target);
			String goldTakenStr=CMLib.beanCounter().nameCurrencyShort(target,goldTaken);

			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
				if(goldTaken > 0)
					str="<S-NAME> pick(s) <T-HIS-HER> pocket for "+goldTakenStr+".";
				else
				{
					str="<S-NAME> attempt(s) to pick <T-HIS-HER> pocket, but nothing was found to steal!";
					code=CMMsg.MSG_QUIETMOVEMENT;
				}

			boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(CMLib.dice().rollPercentage()<discoverChance)
				hisStr=null;
			else
			{
				str+=" <T-NAME> spots you!";
				hisCode=hisCode|((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);
			}

			CMMsg msg=CMClass.getMsg(mob,target,this,code,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
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
				&&(CMLib.dice().rollPercentage()>goldTaken))
				{
					if(target.getVictim()==mob)
						target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
				}
				CMLib.beanCounter().addMoney(mob,currency,goldTaken);
				mob.recoverEnvStats();
				CMLib.beanCounter().subtractMoney(target,currency,goldTaken);
				target.recoverEnvStats();
			}
		}
		return success;
	}

}
