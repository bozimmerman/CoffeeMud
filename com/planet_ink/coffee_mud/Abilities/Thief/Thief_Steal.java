package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2001-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "Thief_Steal";
	}

	private final static String localizedName = CMLib.lang().L("Steal");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;
	}

	private static final String[] triggerStrings =I(new String[] {"STEAL"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT|USAGE_MANA;
	}

	public int code=0;

	@Override
	public int abilityCode()
	{
		return code;
	}

	@Override
	public void setAbilityCode(int newCode)
	{
		code=newCode;
	}

	protected PairVector<MOB,Integer> lastOnes=new PairVector<MOB,Integer>();
	protected int timesPicked(MOB target)
	{
		int times=0;
		for(int x=0;x<lastOnes.size();x++)
		{
			final MOB M=lastOnes.getFirst(x);
			final Integer I=lastOnes.getSecond(x);
			if(M==target)
			{
				times=I.intValue();
				lastOnes.removeElementFirst(M);
				break;
			}
		}
		if(lastOnes.size()>=50)
			lastOnes.removeElementAt(0);
		lastOnes.addElement(target,Integer.valueOf(times+1));
		return times+1;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(!(target instanceof MOB))
				return Ability.QUALITY_INDIFFERENT;
			if(((MOB)target).amDead()||(!CMLib.flags().canBeSeenBy(target,mob)))
				return Ability.QUALITY_INDIFFERENT;
			if((mob.isInCombat())&&(CMLib.flags().isAliveAwakeMobile((MOB)target,true)||(mob.getVictim()!=target)))
				return Ability.QUALITY_INDIFFERENT;
			if(!((MOB)target).mayIFight(mob))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String itemToSteal="all";
		if(!auto)
		{
			if(commands.size()<2)
			{
				mob.tell(L("Steal what from whom?"));
				return false;
			}
			itemToSteal=commands.get(0);
		}

		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
			target=mob.location().fetchInhabitant(CMParms.combine(commands,1));
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell(L("You don't see '@x1' here.",CMParms.combine(commands,1)));
			return false;
		}
		if((mob.isInCombat())&&(CMLib.flags().isAliveAwakeMobile(target,true)||(mob.getVictim()!=target)))
		{
			mob.tell(mob,mob.getVictim(),null,L("Not while you are fighting <T-NAME>!"));
			return false;
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+abilityCode()+(getXLEVELLevel(mob)*2));

		if((!target.mayIFight(mob))||(levelDiff>15))
		{
			mob.tell(L("You cannot steal from @x1.",target.charStats().himher()));
			return false;
		}
		if(target==mob)
		{
			mob.tell(L("You cannot steal from yourself."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Item stolen=target.fetchItem(null,Wearable.FILTER_UNWORNONLY,itemToSteal);
		if(stolen instanceof Coins)
		{
			mob.tell(L("You'll need to try and SWIPE that."));
			return false;
		}

		int discoverChance=(target.charStats().getStat(CharStats.STAT_WISDOM)*5)
							-(levelDiff*5)
							+(getX1Level(mob)*5);
		final int times=timesPicked(target);
		if(times>5)
			discoverChance-=(20*(times-5));
		if(!CMLib.flags().canBeSeenBy(mob,target))
			discoverChance+=50;
		if(discoverChance>95)
			discoverChance=95;
		if(discoverChance<5)
			discoverChance=5;

		if(levelDiff>0)
			levelDiff=-(levelDiff*((!CMLib.flags().canBeSeenBy(mob,target))?5:15));
		else
			levelDiff=-(levelDiff*((!CMLib.flags().canBeSeenBy(mob,target))?1:2));
		if(!CMLib.flags().isAliveAwakeMobile(target,true))
		{
			levelDiff=100;
			discoverChance=0;
		}

		final boolean success=proficiencyCheck(mob,levelDiff,auto);

		if(!success)
		{
			if(CMLib.dice().rollPercentage()>discoverChance)
			{
				if((target.isMonster())&&(mob.getVictim()==null))
					mob.setVictim(target);
				final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":L("You fumble the attempt to steal; <T-NAME> spots you!"),CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to steal from you and fails!"),CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to steal from <T-NAME> and fails!"));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":L("You fumble the attempt to steal."));
		}
		else
		{
			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
			{
				if((stolen!=null)&&(stolen.amWearingAt(Wearable.IN_INVENTORY)))
					str=L("<S-NAME> steal(s) @x1 from <T-NAMESELF>.",stolen.name());
				else
				{
					code=CMMsg.MSG_QUIETMOVEMENT;
					str=L("<S-NAME> attempt(s) to steal from <T-HIM-HER>, but it doesn't appear @x1 has that in <T-HIS-HER> inventory!",target.charStats().heshe());
				}
			}

			final boolean alreadyFighting=(mob.getVictim()==target)||(target.getVictim()==mob);
			String hisStr=str;
			int hisCode=CMMsg.MSG_THIEF_ACT;
			if(CMLib.dice().rollPercentage()<discoverChance)
				hisStr=null;
			else
			{
				str+=" ^Z<T-NAME> spots you!^.^N";
				hisCode=hisCode|((target.mayIFight(mob))?CMMsg.MASK_MALICIOUS:0);
			}

			CMMsg msg=CMClass.getMsg(mob,target,this,code,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				if((!target.isMonster())&&(mob.isMonster())&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace(true);
					if(mob.getVictim()==target)
						mob.makePeace(true);
				}
				else
				if(((hisStr==null)||mob.isMonster())
				&&(!alreadyFighting)
				&&((stolen==null)||(CMLib.dice().rollPercentage()>stolen.phyStats().level())))
				{
					if(target.getVictim()==mob)
						target.makePeace(true);
					if(mob.getVictim()==target)
						mob.makePeace(true);
				}
				msg=CMClass.getMsg(target,stolen,null,CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_NOISE,null);
				if(target.location().okMessage(target,msg))
				{
					target.location().send(mob,msg);
					msg=CMClass.getMsg(mob,stolen,null,CMMsg.MSG_GET,CMMsg.MSG_GET,CMMsg.MSG_NOISE,null);
					if(mob.location().okMessage(mob,msg))
					{
						mob.location().send(mob,msg);
						if(stolen!=null)
						{
							Ability propertyProp=stolen.fetchEffect("Prop_PrivateProperty");
							if(propertyProp==null)
							{
								propertyProp=CMClass.getAbility("Prop_PrivateProperty");
								propertyProp.setMiscText("owner=\""+mob.Name()+"\" expiresec=60");
								stolen.addNonUninvokableEffect(propertyProp);
							}
						}
					}
				}
			}
		}
		return success;
	}

}
