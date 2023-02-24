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
   Copyright 2020-2023 Bo Zimmerman

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
public class Thief_StealBoon extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_StealBoon";
	}

	private final static String localizedName = CMLib.lang().L("Steal Boon");

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
	public long flags()
	{
		return super.flags() | Ability.FLAG_STEALING;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_STEALING;
	}

	private static final String[] triggerStrings =I(new String[] {"STEALBOON","BSTEAL"});
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
	public void setAbilityCode(final int newCode)
	{
		code=newCode;
	}

	protected PairVector<MOB,Integer> lastOnes=new PairVector<MOB,Integer>();
	protected int timesPicked(final MOB target)
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

	protected List<Ability> findBoons(final MOB mob)
	{
		final List<Ability> choices = new ArrayList<Ability>();
		for(final Enumeration<Ability> m=mob.effects();m.hasMoreElements();)
		{
			final Ability A=m.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			&&(A.canBeUninvoked())
			&&(!A.isAutoInvoked())
			&&(A.triggerStrings()!=null)
			&&(A.triggerStrings().length>0)
			&&((A.castingQuality(mob, mob)==Ability.QUALITY_BENEFICIAL_SELF)
				||(A.castingQuality(mob, mob)==Ability.QUALITY_BENEFICIAL_OTHERS)
				||(A.castingQuality(mob, mob)!=Ability.QUALITY_MALICIOUS))
			&&(!A.isSavable())
			&&(A.displayText().length()>0)
			&&(A.invoker()!=null))
				choices.add(A);
		}
		if(choices.size()==0)
		{
			for(final Enumeration<Ability> m=mob.effects();m.hasMoreElements();)
			{
				final Ability A=m.nextElement();
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				&&(A.canBeUninvoked())
				&&(!A.isAutoInvoked())
				&&(A.triggerStrings()!=null)
				&&(A.triggerStrings().length>0)
				&&((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF)
					||(A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS)
					||(A.abstractQuality()!=Ability.QUALITY_MALICIOUS))
				&&(!A.isSavable())
				&&(A.displayText().length()>0)
				&&(A.invoker()!=null))
					choices.add(A);
			}
		}
		return choices;
	}

	protected Ability findBoon(final MOB mob)
	{
		if(mob==null)
			return null;
		final List<Ability> choices = findBoons(mob);
		if(choices.size()==0)
			return null;
		return choices.get(CMLib.dice().roll(1, choices.size(), -1));
	}

	protected Ability findBoon(final MOB mob, final String boonName)
	{
		if(mob==null)
			return null;
		final List<Ability> choices = findBoons(mob);
		if(choices.size()==0)
			return null;
		Ability A=(Ability)CMLib.english().fetchEnvironmental(choices, boonName, false);
		if(A==null)
			A=(Ability)CMLib.english().fetchEnvironmental(choices, boonName, false);
		return A;
	}

	protected Item fetchCatalyst(final MOB mob, final boolean quiet)
	{
		final Item catalystI = mob.fetchHeldItem();
		if(catalystI==null)
		{
			if(!quiet)
				mob.tell(L("You must be holding a suitable holy item to attempt this."));
			return null;
		}
		if(catalystI instanceof Wand)
		{
			if((((Wand)catalystI).getSpell()==null)
			||(((Wand)catalystI).usesRemaining()<=0)
			||((((Wand)catalystI).getSpell().classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PRAYER))
			{
				if(!quiet)
					mob.tell(L("@x1 does not appear to be a suitable holy item to drawn from for boon stealing.",catalystI.name(mob)));
				return null;
			}
		}
		else
		if(catalystI instanceof SpellHolder)
		{
			boolean hasPrayer=false;
			for(final Ability A : ((SpellHolder)catalystI).getSpells())
			{
				if((A!=null)
				&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
					hasPrayer=true;
			}
			if((!hasPrayer)
			||(((catalystI instanceof Scroll)&&(catalystI.usesRemaining()<=0))))
			{
				if(!quiet)
					mob.tell(L("@x1 does not appear to be a suitable holy item to drawn from for boon stealing.",catalystI.name(mob)));
				return null;
			}
		}
		else
		{
			boolean hasPrayer=false;
			for(final Enumeration<Ability> a=catalystI.effects();a.hasMoreElements();)
			{
				final Ability eA=a.nextElement();
				if(eA instanceof AbilityContainer)
				{
					for(final Enumeration<Ability> aa = ((AbilityContainer)eA).abilities(); aa.hasMoreElements();)
					{
						final Ability A=aa.nextElement();
						if((A!=null)
						&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER))
							hasPrayer=true;
					}
				}
			}
			if(!hasPrayer)
			{
				if(!quiet)
					mob.tell(L("@x1 does not appear to be a suitable holy item to drawn from for boon stealing.",catalystI.name(mob)));
				return null;
			}
		}
		return catalystI;
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
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
			if(CMLib.flags().canBeSeenBy(mob, (MOB)target))
				return Ability.QUALITY_INDIFFERENT;
			if(this.fetchCatalyst(mob, true)==null)
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Item catalystI;
		if(!auto)
		{
			catalystI = fetchCatalyst(mob,false);
			if(catalystI==null)
				return false;
		}
		else
		{
			catalystI=CMClass.getBasicItem("GenItem");
			catalystI.setName("the universe");
		}

		if(commands.size()<2)
		{
			if(mob.isMonster())
			{
				final MOB target=(givenTarget instanceof MOB)?((MOB)givenTarget):null;
				if(target == null)
					return false;
				final Ability A=findBoon(target);
				if(A==null)
					return false;
				if(commands.size()==1)
					commands.add(0,A.Name());
				else
				{
					commands.add(A.Name());
					commands.add(target.Name());
				}
			}
			else
			{
				mob.tell(L("Steal which boon from whom?"));
				return false;
			}
		}
		final String boonName=commands.get(0);
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
			target=getVisibleRoomTarget(mob,CMParms.combine(commands,1));
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
			mob.tell(L("You cannot stealing from yourself."));
			return false;
		}
		final Ability stolenA=findBoon(target,boonName);
		/*
		if(stolenA==null)
		{
			mob.tell(L("@x1 has no boon you can steal called '@x2'",target.name(mob),boonName));
			return false;
		}
		*/

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		// higher is good for the player, lower is good for the npc
		// leveldiff is + when npc has advantage, and - when player does.
		int discoverChance=(mob.charStats().getStat(CharStats.STAT_DEXTERITY)*3)
							-(target.charStats().getStat(CharStats.STAT_WISDOM)*5)
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
			levelDiff=-(levelDiff*((CMLib.flags().canBeSeenBy(mob,target))?1:2));
		if(!CMLib.flags().isAliveAwakeMobile(target,true))
		{
			levelDiff=100;
			discoverChance=100;
		}

		final boolean success=proficiencyCheck(mob,levelDiff,auto);

		if(!success)
		{
			if(CMLib.dice().rollPercentage()>discoverChance)
			{
				if((target.isMonster())&&(mob.getVictim()==null))
					mob.setVictim(target);
				final CMMsg msg=CMClass.getMsg(mob,target,this,
						CMMsg.MSG_NOISYMOVEMENT,auto?"":L("You fumble the attempt to 'steal'; <T-NAME> spots you!"),
						CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to 'steal' from you and fails!"),
						CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to 'steal' from <T-NAME> and fails!"));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":L("You fumble the attempt to 'steal' a boon."));
		}
		else
		{
			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
			{
				if(stolenA!=null)
					str=L("<S-NAME> 'steal(s)' @x1 from <T-NAMESELF> using the power of @x2.",stolenA.name(),catalystI.name());
				else
				{
					code=CMMsg.MSG_QUIETMOVEMENT;
					str=L("<S-NAME> attempt(s) to 'steal' from <T-HIM-HER>, but comes up empty!",target.charStats().heshe());
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

			final CMMsg msg=CMClass.getMsg(mob,target,this,code,str,hisCode,hisStr,CMMsg.NO_EFFECT,null);
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
				&&(!alreadyFighting))
				{
					if(target.getVictim()==mob)
						target.makePeace(true);
					if(mob.getVictim()==target)
						mob.makePeace(true);
				}
				if(stolenA != null)
				{
					final long expires=stolenA.expirationDate();

					final Ability newEffA = (Ability)stolenA.copyOf();
					newEffA.startTickDown(mob, mob, super.getBeneficialTickdownTime(mob, mob, 0, asLevel));
					if((newEffA.canBeUninvoked())
					&&(expires < (CMProps.getTickMillis() * 1000)))
						newEffA.setExpirationDate(expires);
					stolenA.unInvoke();
					if(CMLib.dice().rollPercentage() < 12-super.getXLEVELLevel(mob))
					{
						if((catalystI instanceof Wand)
						&&(((Wand)catalystI).usesRemaining()>0))
							((Wand)catalystI).setUsesRemaining(((Wand)catalystI).usesRemaining()-1);
						else
						if((catalystI instanceof Scroll)
						&&(((Scroll)catalystI).usesRemaining()>0))
							((Scroll)catalystI).setUsesRemaining(((Scroll)catalystI).usesRemaining()-1);
					}
				}
			}
		}
		return success;
	}
}
