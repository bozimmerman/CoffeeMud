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

public class Spell_SummonSteed extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_SummonSteed";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon Steed");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Summon Steed)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
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
			else
			if(mob.location()!=null)
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> gallop(s) away!"));
			mob.destroy();
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if(!mob.isInCombat())
				{
					if((mob.amFollowing()==null)
					||(mob.location()==null)
					||(mob.amDead())
					||((invoker!=null)
						&&((mob.location()!=invoker.location())
							||(!CMLib.flags().isInTheGame(invoker, true))
							||((invoker.riding() instanceof MOB)&&(invoker.riding()!=affected)))))
					{
						mob.delEffect(this);
						if(mob.amDead())
							mob.setLocation(null);
						mob.destroy();
					}
					else
					if((mob.amFollowing()==null)
					&&(mob.location()!=null)
					&&(mob.curState().getHitPoints()<((mob.maxState().getHitPoints()/10)*3)))
					{
						mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> flees."));
						mob.delEffect(this);
						if(mob.amDead())
							mob.setLocation(null);
						mob.destroy();
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing())||(msg.source()==invoker()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null)
				msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> call(s) for a loyal steed.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.phyStats().level()+((getXLEVELLevel(mob)+getX1Level(mob))/2));
				final MOB squabble = checkPack(target, mob);
				beneficialAffect(mob,target,asLevel,0);
				if(squabble==null)
				{
					if (target.isInCombat()) 
						target.makePeace(true);
					CMLib.commands().postFollow(target,mob,true);
					invoker=mob;
					if (target.amFollowing() != mob)
						mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
				}
				else
				if(squabble.location()!=null)
				{
					squabble.location().showOthers(squabble,target,CMMsg.MSG_OK_ACTION,L("^F^<FIGHT^><S-NAME> bares its teeth at <T-NAME> and begins to attack!^</FIGHT^>^?"));
					target.setVictim(squabble);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> call(s) for a loyal steed, but choke(s) on the words."));

		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, int level)
	{

		final MOB newMOB=CMClass.getMOB("GenRideable");
		final Rideable ride=(Rideable)newMOB;
		newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());
		newMOB.basePhyStats().setLevel(level);
		newMOB.basePhyStats().setWeight(500);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		if(level<4)
		{
			newMOB.setName(L("a pony"));
			newMOB.setDisplayText(L("a very pretty pony stands here"));
			newMOB.setDescription(L("She looks loyal, and oh so pretty."));
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			ride.setRiderCapacity(1);
		}
		else
		if(level<10)
		{
			newMOB.setName(L("a pack horse"));
			newMOB.setDisplayText(L("a sturdy pack horse stands here"));
			newMOB.setDescription(L("A strong and loyal beast, who looks like he`s seen his share of work."));
			ride.setRiderCapacity(2);
		}
		else
		if(level<18)
		{
			newMOB.setName(L("a riding horse"));
			newMOB.setDisplayText(L("a loyal riding horse stands here"));
			newMOB.setDescription(L("A proud and noble companion; brown hair with a long black mane."));
			ride.setRiderCapacity(2);
		}
		else
		{
			newMOB.setName(L("a warhorse"));
			newMOB.setDisplayText(L("a mighty warhorse stands here"));
			newMOB.setDescription(L("Ferocious, fleet of foot, and strong, a best of breed!"));
			ride.setRiderCapacity(3);
		}
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
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

	public MOB checkPack(MOB newPackmate, MOB mob)
	{
		for(int i=0;i<mob.numFollowers();i++)
		{
			final MOB possibleBitch = mob.fetchFollower(i);
			if(newPackmate.Name().equalsIgnoreCase(possibleBitch.Name())
			&&(possibleBitch.location()==newPackmate.location())
			&& (CMLib.dice().rollPercentage()-mob.charStats().getStat(CharStats.STAT_CHARISMA)+newPackmate.phyStats().level() > 75))
				return possibleBitch;
		}
		return null;
	}
}
