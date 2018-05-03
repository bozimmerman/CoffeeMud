package com.planet_ink.coffee_mud.Abilities.Prayers;
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

public class Prayer_SummonElemental extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_SummonElemental";
	}

	private final static String localizedName = CMLib.lang().L("Elemental Aid");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Elemental Aid)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_CREATION;
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

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if(affected instanceof MOB)
			{
				final MOB mob=(MOB)affected;
				if(mob.amFollowing()!=invoker())
					unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing())||(msg.source()==invoker()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null)
				msg.source().playerStats().setLastUpdated(0);
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
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(CMLib.flags().hasAControlledFollower(mob, this))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(CMLib.flags().hasAControlledFollower(mob, this))
		{
			mob.tell(L("You can only control one elemental."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> @x1 for elemental assistance.^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB myMonster = determineMonster(mob, mob.phyStats().level()+(2*getXLEVELLevel(mob)));
				invoker=mob;
				beneficialAffect(mob,myMonster,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 for elemental assistance, but is not answered.",prayWord(mob)));

		// return whether it worked
		return success;
	}

	protected final static String types[]={"EARTH","FIRE","AIR","WATER"};

	public MOB determineMonster(MOB caster, int level)
	{
		final MOB newMOB=CMClass.getMOB("GenRideable");
		final Rideable ride=(Rideable)newMOB;
		newMOB.basePhyStats().setAbility(13);
		if(level>5)
			newMOB.basePhyStats().setLevel(level-5);
		else
			newMOB.basePhyStats().setLevel(level);
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.basePhyStats().setWeight(850);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setSpeed(1);
		newMOB.baseCharStats().setStat(CharStats.STAT_STRENGTH,25);
		newMOB.baseCharStats().setStat(CharStats.STAT_DEXTERITY,25);
		newMOB.baseCharStats().setStat(CharStats.STAT_CONSTITUTION,25);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		int type=-1;
		for(int i=0;i<types.length;i++)
		{
			if(text().toUpperCase().indexOf(types[i])>=0)
				type=i;
		}
		if(type<0)
			type=CMLib.dice().roll(1,types.length,-1);
		switch(type)
		{
		case 0:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("EarthElemental"));
			newMOB.setName(L("a hideous rock beast"));
			newMOB.setDisplayText(L("a hideous rock beast is stomping around here"));
			newMOB.setDescription(L("This enormous hunk of rock is roughly the shape of a humanoid."));
			ride.setRiderCapacity(2);
			break;
		case 1:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("FireElemental"));
			newMOB.setName(L("a creature of flame and smoke"));
			newMOB.setDisplayText(L("a creature of flame and smoke is here"));
			newMOB.setDescription(L("This enormous burning ember is roughly the shape of a humanoid."));
			ride.setRiderCapacity(0);
			break;
		case 2:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("AirElemental"));
			newMOB.setName(L("a swirling air elemental"));
			newMOB.setDisplayText(L("a swirling air elemental spins around here"));
			newMOB.setDescription(L("This enormous swirling code of air is roughly the shape of a humanoid."));
			ride.setRiderCapacity(0);
			break;
		case 3:
			newMOB.baseCharStats().setMyRace(CMClass.getRace("WaterElemental"));
			newMOB.setName(L("a hideous ice beast"));
			newMOB.setDisplayText(L("a hideous ice beast is stomping around here"));
			newMOB.setDescription(L("This enormous hunk of ice is roughly the shape of a humanoid."));
			ride.setRiderCapacity(2);
			break;
		}
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));

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
		final MOB victim=caster.getVictim();
		newMOB.setStartRoom(null); // this must be before postFollow due to the effects on conquest.
		CMLib.commands().postFollow(newMOB,caster,true);
		if(newMOB.amFollowing()!=caster)
			caster.tell(L("@x1 seems unwilling to follow you.",newMOB.name()));
		else
		if(victim!=null)
		{
			if(newMOB.getVictim()!=victim)
				newMOB.setVictim(victim);
			newMOB.location().showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,L("<S-NAME> start(s) attacking <T-NAMESELF>!"));
		}
		return(newMOB);
	}
}
