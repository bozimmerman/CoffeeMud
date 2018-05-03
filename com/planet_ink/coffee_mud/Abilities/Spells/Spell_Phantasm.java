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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_Phantasm extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Phantasm";
	}

	private final static String localizedName = CMLib.lang().L("Phantasm");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Phantasm)");

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
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}
	MOB myTarget=null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if(myTarget==null)
					myTarget=mob.getVictim();

				if((myTarget!=mob.getVictim())
				   ||(myTarget==null))
				{
					if(mob.amDead())
						mob.setLocation(null);
					else
					if(mob.location()!=null)
						mob.location().show(mob,null,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> look(s) around for someone to fight..."));
					((MOB)affected).destroy();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			if(msg.amITarget(mob)&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL))
			{
				msg.source().tell(L("@x1 seems strangely unaffected by your magic.",mob.name(msg.source())));
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB))
		{
			final MOB mob=(MOB)affected;
			if((msg.amISource(mob)||msg.amISource(mob.amFollowing()))
			&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
			{
				unInvoke();
				if(msg.source().playerStats()!=null)
					msg.source().playerStats().setLastUpdated(0);
			}
			else
			if(msg.amITarget(mob)&&(msg.targetMinor()==CMMsg.TYP_DAMAGE))
				msg.addTrailerMsg(CMClass.getMsg(mob,null,CMMsg.MSG_QUIT,L("@x1's attack somehow went THROUGH <T-NAMESELF>.",msg.source().name())));
		}
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead())
				mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		String type=null;
		if(mob.isMonster())
		{
			Race R=CMClass.randomRace();
			for(int i=0;i<10;i++)
			{
				if((R!=null)
				&&(CMProps.isTheme(R.availabilityCode()))
				&&(R!=mob.charStats().getMyRace()))
					break;
				R=CMClass.randomRace();
			}
			if(R!=null)
			{
				type=R.name();
			}
			else
				return false;
		}
		else
		{
			if(commands.size()==0)
			{
				mob.tell(L("You must specify the type of creature to create a phantasm of!"));
				return false;
			}
			type=CMStrings.capitalizeAndLower(CMParms.combine(commands,0));
		}
		final Race R=CMClass.getRace(type);
		if((R==null)
		||(!CMProps.isTheme(R.availabilityCode())))
		{
			mob.tell(L("You don't know how to create a phantasm of a '@x1'.",type));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> incant(s), calling on the name of @x1.^?",type));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB myMonster = determineMonster(mob, R, mob.phyStats().level()+(2*getXLEVELLevel(mob)));
				myMonster.setVictim(mob.getVictim());
				CMLib.commands().postFollow(myMonster,mob,true);
				if(myMonster.getVictim()!=null)
					myMonster.getVictim().setVictim(myMonster);
				invoker=mob;
				beneficialAffect(mob,myMonster,asLevel,0);
				if(myMonster.amFollowing()!=mob)
					mob.tell(L("@x1 seems unwilling to follow you.",myMonster.name()));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> incant(s) to summon a @x1, but fails.",type));

		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, Race R, int level)
	{
		final MOB newMOB=CMClass.getMOB("GenMob");
		newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());
		final CharClass C=CMClass.getCharClass("Fighter");
		newMOB.baseCharStats().setCurrentClass(C);
		newMOB.basePhyStats().setLevel(level);
		CMLib.factions().setAlignment(newMOB,Faction.Align.EVIL);
		newMOB.basePhyStats().setWeight(850);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setStat(CharStats.STAT_STRENGTH,25);
		newMOB.baseCharStats().setStat(CharStats.STAT_DEXTERITY,25);
		newMOB.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,25);
		newMOB.baseCharStats().setMyRace(R);
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.setName(L("a ferocious @x1",R.name().toLowerCase()));
		newMOB.setDisplayText(L("a ferocious @x1 is stalking around here",R.name().toLowerCase()));
		newMOB.setDescription("");
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
		caster.location().recoverRoomStats();
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
