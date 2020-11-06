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
   Copyright 2020-2020 Bo Zimmerman

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
public class Thief_BorrowBoon extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_BorrowBoon";
	}

	private final static String localizedName = CMLib.lang().L("Borrow Boon");

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

	private static final String[] triggerStrings =I(new String[] {"BORROWBOON","BSTEAL"});
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

	protected Ability findBoon(final MOB mob)
	{
		if(mob==null)
			return null;
		boolean tryAgain=false;
		for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
			{
				if((A.castingQuality(mob, mob)==Ability.QUALITY_BENEFICIAL_SELF)
				||(A.castingQuality(mob, mob)==Ability.QUALITY_BENEFICIAL_OTHERS))
					return A;
				tryAgain=true;
			}
		}
		if(tryAgain)
		{
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)
				{
					if((A.abstractQuality()==Ability.QUALITY_BENEFICIAL_SELF)
					||(A.abstractQuality()==Ability.QUALITY_BENEFICIAL_OTHERS))
						return A;
				}
			}
		}
		return null;
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
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Item catalystI;
		if(!auto)
		{
			catalystI = mob.fetchHeldItem();
			if(catalystI==null)
			{
				mob.tell(L("You must be holding a suitable holy item to attempt this."));
				return false;
			}
			if(catalystI instanceof Wand)
			{
				if((((Wand)catalystI).getSpell()==null)
				||(((Wand)catalystI).usesRemaining()<=0)
				||((((Wand)catalystI).getSpell().classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_PRAYER))
				{
					mob.tell(L("@x1 does not appear to be a suitable holy item to drawn from for boon borrowing.",catalystI.name(mob)));
					return false;
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
					mob.tell(L("@x1 does not appear to be a suitable holy item to drawn from for boon borrowing.",catalystI.name(mob)));
					return false;
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
					mob.tell(L("@x1 does not appear to be a suitable holy item to drawn from for boon borrowing.",catalystI.name(mob)));
					return false;
				}
			}
		}
		else
		{
			catalystI=CMClass.getBasicItem("GenItem");
			catalystI.setName("the universe");
		}

		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		else
			target=mob.location().fetchInhabitant(CMParms.combine(commands));
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
			mob.tell(L("You cannot borrow from @x1.",target.charStats().himher()));
			return false;
		}
		if(target==mob)
		{
			mob.tell(L("You cannot borrow from yourself."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Ability stolen=findBoon(target);

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
						CMMsg.MSG_NOISYMOVEMENT,auto?"":L("You fumble the attempt to 'borrow'; <T-NAME> spots you!"),
						CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to 'borrow' from you and fails!"),
						CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> tries to 'borrow' from <T-NAME> and fails!"));
				if(mob.location().okMessage(mob,msg))
					mob.location().send(mob,msg);
			}
			else
				mob.tell(auto?"":L("You fumble the attempt to 'borrow' a boon."));
		}
		else
		{
			String str=null;
			int code=CMMsg.MSG_THIEF_ACT;
			if(!auto)
			{
				if(stolen!=null)
					str=L("<S-NAME> 'borrow(s)' @x1 from <T-NAMESELF> using the power of @x2.",stolen.name(),catalystI.name());
				else
				{
					code=CMMsg.MSG_QUIETMOVEMENT;
					str=L("<S-NAME> attempt(s) to 'borrow' from <T-HIM-HER>, but comes up empty!",target.charStats().heshe());
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
				final long expires=stolen.expirationDate();
				final Ability newEffA = (Ability)stolen.copyOf();
				newEffA.startTickDown(mob, mob, super.getBeneficialTickdownTime(mob, mob, 0, asLevel));
				if((newEffA.canBeUninvoked())
				&&(expires < (CMProps.getTickMillis() * 1000)))
					newEffA.setExpirationDate(expires);
				stolen.unInvoke();
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
		return success;
	}
}
