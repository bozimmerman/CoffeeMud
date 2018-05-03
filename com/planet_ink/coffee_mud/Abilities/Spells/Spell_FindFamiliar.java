package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Spell_FindFamiliar extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_FindFamiliar";
	}

	private final static String localizedName = CMLib.lang().L("Find Familiar");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Find Familiar)");

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
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
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
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.numFollowers()>0)||(mob.isMonster()))
		{
			mob.tell(L("You cannot have any followers when casting this spell."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int experienceToLose=getXPCOSTAdjustment(mob,100);
		CMLib.leveler().postExperience(mob,null,null,-experienceToLose,false);
		mob.tell(L("The effort causes you to lose @x1 experience.",""+experienceToLose));

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> call(s) for a familiar.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.phyStats().level());
				if(target.isInCombat())
					target.makePeace(true);
				CMLib.commands().postFollow(target,mob,true);
				invoker=mob;
				if(target.amFollowing()!=mob)
					mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> call(s), but choke(s) on the words."));

		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, int level)
	{

		final MOB newMOB=CMClass.getMOB("GenMOB");
		newMOB.basePhyStats().setAbility(7);
		newMOB.basePhyStats().setLevel(level);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		final int choice=CMLib.dice().roll(1,9,-1);
		switch(choice)
		{
		case 0:
			newMOB.setName(L("a dog"));
			newMOB.setDisplayText(L("a dog is sniffing around here"));
			newMOB.setDescription(L("She looks like a nice loyal companion."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Dog"));
			break;
		case 1:
			newMOB.setName(L("a turtle"));
			newMOB.setDisplayText(L("a turtle is crawling around here"));
			newMOB.setDescription(L("Not very fast, but pretty cute."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Turtle"));
			break;
		case 2:
			newMOB.setName(L("a cat"));
			newMOB.setDisplayText(L("a cat is prowling around here"));
			newMOB.setDescription(L("She looks busy ignoring you."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Cat"));
			break;
		case 3:
			newMOB.setName(L("a bat"));
			newMOB.setDisplayText(L("a bat is flying around here"));
			newMOB.setDescription(L("The darn thing just won`t stay still!"));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Bat"));
			break;
		case 4:
			newMOB.setName(L("a rat"));
			newMOB.setDisplayText(L("a rat scurries nearby"));
			newMOB.setDescription(L("Such a cute, furry little guy!"));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rodent"));
			break;
		case 5:
			newMOB.setName(L("a snake"));
			newMOB.setDisplayText(L("a snake is slithering around"));
			newMOB.setDescription(L("..red on yellow..., how did that go again?"));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Snake"));
			break;
		case 6:
			newMOB.setName(L("an owl"));
			newMOB.setDisplayText(L("an owl is flying around here"));
			newMOB.setDescription(L("He looks wise beyond his years."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Owl"));
			break;
		case 7:
			newMOB.setName(L("a rabbit"));
			newMOB.setDisplayText(L("a cute little rabbit is watching you"));
			newMOB.setDescription(L("Don`t blink, or she may twitch her nose."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Rabbit"));
			break;
		case 8:
			newMOB.setName(L("a raven"));
			newMOB.setDisplayText(L("a raven is pearched nearby"));
			newMOB.setDescription(L("You think he`s watching you."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
			newMOB.baseCharStats().setMyRace(CMClass.getRace("Raven"));
			break;
		}
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		CMLib.factions().setAlignment(newMOB,Faction.Align.GOOD);
		newMOB.setStartRoom(null);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		final Ability A=CMClass.getAbility("Prop_Familiar");
		A.setMiscText(""+choice);
		newMOB.addPriorityEffect(A);
		A.makeNonUninvokable();
		A.makeLongLasting();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
		caster.location().recoverRoomStats();
		return(newMOB);

	}
}
