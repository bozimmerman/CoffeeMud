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
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_PirateFamiliar extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_PirateFamiliar";
	}

	private final static String	localizedName	= CMLib.lang().L("Call Pirate Familiar");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Pirate Familiar)");

	private static final String[]	triggerStrings	= I(new String[] { "CALLFAMILIAR", "PIRATEFAMILIAR", "CALLPIRATEFAMILIAR" });
	
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NOORDERING;
	}

	@Override
	protected int overrideMana()
	{
		return Ability.COST_ALL;
	}

	public int castingQuality(MOB mob, MOB target)
	{
		if((target!=null)&&(mob!=target))
			return Ability.QUALITY_INDIFFERENT;
		if(mob.numFollowers()>0)
			return Ability.QUALITY_INDIFFERENT;
		if(mob.isMonster())
			return Ability.QUALITY_INDIFFERENT;
		return abstractQuality();
	}

	protected enum Familiar
	{
		RAT("rat"),
		PARROT("parrot"),
		SPIDERMONKEY("spidermonkey"),
		BOA_CONSTRICTOR("boa constrictor"),
		IGUANA("iguana"),
		SEATURTLE("sea turtle")
		;
		public String name;
		private Familiar(String name)
		{
			this.name=name;
		}
	}
	
	protected Familiar		familiarType			= Familiar.PARROT;
	
	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(target instanceof MOB)
			{
				if((((MOB)target).numFollowers()>0)||(((MOB)target).isMonster()))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			if(invoker==null)
			{
				if(M.amFollowing()!=null)
					invoker=M.amFollowing();
			}
			MOB invoker=this.invoker;
			if(invoker!=null)
			{
				if(affectableStats.level() < invoker.phyStats().level()-3)
				{
					int level = invoker.phyStats().level()-3;
					if(level < 1)
						level = 1;
					M.basePhyStats().setLevel(level);
					affectableStats.setLevel(level);
					M.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(M)-(2*super.getXLEVELLevel(invoker)));
					affectableStats.setArmor(M.basePhyStats().armor());
					M.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(M));
					affectableStats.setAttackAdjustment(M.basePhyStats().attackAdjustment());
					M.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(M));
					affectableStats.setDamage(M.basePhyStats().damage());
					M.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(M));
					affectableStats.setSpeed(M.basePhyStats().speed());
					M.setExperience(CMLib.leveler().getLevelExperience(M.basePhyStats().level()));
				}
			}
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.numFollowers()>0)||(mob.isMonster()))
		{
			mob.tell(L("You cannot have any followers when calling a familiar."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_THIEF_ACT),auto?"":L("^S<S-NAME> call(s) for <S-HIS-HER> pirate familiar.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.phyStats().level()-3);
				if(target.isInCombat())
					target.makePeace(true);
				CMLib.commands().postFollow(target,mob,true);
				invoker=mob;
				if(target.amFollowing()!=mob)
					mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> call(s) for a familiar, but choke(s) on the words."));

		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, int level)
	{

		final MOB newMOB=CMClass.getMOB("GenMOB");
		newMOB.basePhyStats().setAbility(super.getXLEVELLevel(caster));
		if(level < 1)
			level=1;
		newMOB.basePhyStats().setLevel(level);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		int choice=CMLib.dice().roll(1,6,-1);
		String choiceStr;
		switch(choice)
		{
		case 0:
		{
			choiceStr="parrot";
			newMOB.setName(L("a parrot"));
			newMOB.setDisplayText(L("a parrot is here"));
			newMOB.setDescription(L("He doesn't like you, and has said as much."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("SongBird"));
			familiarType = Familiar.PARROT;
			break;
		}
		case 1:
		{
			choiceStr="rat";
			newMOB.setName(L("a rat"));
			newMOB.setDisplayText(L("a rat scurries nearby"));
			newMOB.setDescription(L("Such a cute, furry little guy!"));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rodent"));
			familiarType = Familiar.RAT;
			break;
		}
		case 2:
		{
			choiceStr="spidermonkey";
			newMOB.setName(L("a spidermoney"));
			newMOB.setDisplayText(L("a mischievous monkey is watching you"));
			newMOB.setDescription(L("Don`t blink, or she may swipe your stuff."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rabbit"));
			familiarType = Familiar.SPIDERMONKEY;
			break;
		}
		case 3:
		{
			choiceStr="boa constrictor";
			newMOB.setName(L("a boa constrictor"));
			newMOB.setDisplayText(L("a huge boa constrictor is slithering around"));
			newMOB.setDescription(L("He looks cuddly, but I wouldn`t risk it."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Snake"));
			familiarType = Familiar.BOA_CONSTRICTOR;
			break;
		}
		case 4:
		{
			choiceStr="iguana";
			newMOB.setName(L("an iguana"));
			newMOB.setDisplayText(L("an iguana is standing here"));
			newMOB.setDescription(L("She looks like a nice loyal companion."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Lizard"));
			familiarType = Familiar.IGUANA;
			break;
		}
		case 5:
		{
			choiceStr="sea turtle";
			newMOB.setName(L("a sea turtle"));
			newMOB.setDisplayText(L("a sea turtle is crawling around here"));
			newMOB.setDescription(L("Not very fast, but pretty cute."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Turtle"));
			familiarType = Familiar.SEATURTLE;
			break;
		}
		default:
			choiceStr="dog";
			break;
		}
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB)-(2*super.getXLEVELLevel(invoker)));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.setExperience(CMLib.leveler().getLevelExperience(newMOB.basePhyStats().level()));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		CMLib.factions().setAlignment(newMOB,Faction.Align.GOOD);
		newMOB.setStartRoom(null);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		this.invoker=caster;
		newMOB.addNonUninvokableEffect((Ability)this.copyOf());
		Ability A=CMClass.getAbility("Prop_Familiar");
		if(A!=null)
		{
			A.setMiscText(choiceStr);
			newMOB.addNonUninvokableEffect(A);
		}
		A=CMClass.getAbility("Thief_Sneak");
		if(A!=null)
		{
			A.setProficiency(100);
			newMOB.addAbility(A);
		}
		A=CMClass.getAbility("Thief_Autosneak");
		if(A!=null)
		{
			A.setProficiency(100);
			newMOB.addAbility(A);
		}
		final Behavior B=CMClass.getBehavior("CombatAbilities");
		if(B!=null)
		{
			newMOB.addBehavior(B);
		}
		newMOB.text();
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> arrives!"));
		caster.location().recoverRoomStats();
		return(newMOB);
	}
}
