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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_SummonSteed extends Spell
{
	@Override public String ID() { return "Spell_SummonSteed"; }
	@Override public String name(){return "Summon Steed";}
	@Override public String displayText(){return "(Summon Steed)";}
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	@Override public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	@Override public long flags(){return Ability.FLAG_SUMMONING;}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				final MOB mob=(MOB)affected;
				if(!mob.isInCombat())
				if(((mob.amFollowing()==null)
				||(mob.location()==null)
				||(mob.amDead())
				||(invoker==null)
				||(invoker.location()==null)
				||((invoker!=null)&&(mob.location()!=invoker.location())&&(invoker.riding()!=affected))))
				{
					mob.delEffect(this);
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
				}
				else
				if((mob.amFollowing()==null)
				&&(mob.location()!=null)
				&&(mob.curState().getHitPoints()<((mob.maxState().getHitPoints()/10)*3)))
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,_("<S-NAME> flees."));
					mob.delEffect(this);
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
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
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
		}
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> call(s) for a loyal steed.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.phyStats().level()+((getXLEVELLevel(mob)+getX1Level(mob))/2));
				final MOB squabble = checkPack(target, mob);
				target.addNonUninvokableEffect( (Ability) copyOf());
				if(squabble==null)
				{
					if (target.isInCombat()) target.makePeace();
					CMLib.commands().postFollow(target,mob,true);
					invoker=mob;
					if (target.amFollowing() != mob)
						mob.tell(target.name(mob) + " seems unwilling to follow you.");
				}
				else
				if(squabble.location()!=null)
				{
					squabble.location().showOthers(squabble,target,CMMsg.MSG_OK_ACTION,"^F^<FIGHT^><S-NAME> bares its teeth at <T-NAME> and begins to attack!^</FIGHT^>^?");
					target.setVictim(squabble);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for a loyal steed, but choke(s) on the words.");

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{

		final MOB newMOB=CMClass.getMOB("GenRideable");
		final Rideable ride=(Rideable)newMOB;
		newMOB.basePhyStats().setAbility(11);
		newMOB.basePhyStats().setLevel(level);
		newMOB.basePhyStats().setWeight(500);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		if(level<4)
		{
			newMOB.setName("a pony");
			newMOB.setDisplayText("a very pretty pony stands here");
			newMOB.setDescription("She looks loyal, and oh so pretty.");
			newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'F');
			ride.setRiderCapacity(1);
		}
		else
		if(level<10)
		{
			newMOB.setName("a pack horse");
			newMOB.setDisplayText("a sturdy pack horse stands here");
			newMOB.setDescription("A strong and loyal beast, who looks like he`s seen his share of work.");
			ride.setRiderCapacity(2);
		}
		else
		if(level<18)
		{
			newMOB.setName("a riding horse");
			newMOB.setDisplayText("a loyal riding horse stands here");
			newMOB.setDescription("A proud and noble companion; brown hair with a long black mane.");
			ride.setRiderCapacity(2);
		}
		else
		{
			newMOB.setName("a warhorse");
			newMOB.setDisplayText("a mighty warhorse stands here");
			newMOB.setDescription("Ferocious, fleet of foot, and strong, a best of breed!");
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
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
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
