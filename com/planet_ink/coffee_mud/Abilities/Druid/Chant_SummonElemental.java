package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_SummonElemental extends Chant
{
	@Override public String ID() { return "Chant_SummonElemental"; }
	private final static String localizedName = CMLib.lang()._("Summon Elemental");
	@Override public String name() { return localizedName; }
	private final static String localizedStaticDisplay = CMLib.lang()._("(Summon Elemental)");
	@Override public String displayText() { return localizedStaticDisplay; }
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	@Override public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	@Override public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public long flags(){return Ability.FLAG_SUMMONING;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB)&&(invoker!=null))
			{
				final MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()!=invoker.location())))
				{
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
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if(CMLib.flags().hasAControlledFollower(mob,this))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(CMLib.flags().hasAControlledFollower(mob, this))
		{
			mob.tell(_("You can only control one elemental."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":_("^S<S-NAME> chant(s) and summon(s) help from another Plain.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
				target.addNonUninvokableEffect((Ability)this.copyOf());
				if(target.isInCombat()) target.makePeace();
				CMLib.commands().postFollow(target,mob,true);
				if(target.amFollowing()!=mob)
					mob.tell(_("@x1 seems unwilling to follow you.",target.name(mob)));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> chant(s), but nothing happens."));

		// return whether it worked
		return success;
	}
	public MOB determineMonster(MOB caster, int level)
	{
		final MOB newMOB=CMClass.getMOB("GenMOB");
		newMOB.basePhyStats().setLevel(adjustedLevel(caster,0));
		switch(CMLib.dice().roll(1,4,0))
		{
		case 1:
			newMOB.setName("a fire elemental");
			newMOB.setDisplayText("a fire elemental is flaming nearby.");
			newMOB.setDescription("A large beast, wreathed in flame, with sparkling eyes and a hot temper.");
			newMOB.basePhyStats().setDisposition(newMOB.basePhyStats().disposition()|PhyStats.IS_LIGHTSOURCE);
			CMLib.factions().setAlignment(newMOB,Faction.Align.EVIL);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("FireElemental"));
			newMOB.addAbility(CMClass.getAbility("Firebreath"));
			break;
		case 2:
			newMOB.setName("an ice elemental");
			newMOB.setDisplayText("an ice elemental is chilling out here.");
			newMOB.setDescription("A large beast, made of ice, with crytaline eyes and a cold disposition.");
			CMLib.factions().setAlignment(newMOB,Faction.Align.GOOD);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("WaterElemental"));
			newMOB.addAbility(CMClass.getAbility("Frostbreath"));
			break;
		case 3:
			newMOB.setName("an earth elemental");
			newMOB.setDisplayText("an earth elemental looks right at home.");
			newMOB.setDescription("A large beast, made of rock and dirt, with a hard stare.");
			CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("EarthElemental"));
			newMOB.addAbility(CMClass.getAbility("Gasbreath"));
			break;
		case 4:
			newMOB.setName("an air elemental");
			newMOB.setDisplayText("an air elemental blows right by.");
			newMOB.setDescription("A large beast, made of swirling clouds and air.");
			CMLib.factions().setAlignment(newMOB,Faction.Align.GOOD);
			newMOB.baseCharStats().setMyRace(CMClass.getRace("AirElemental"));
			newMOB.addAbility(CMClass.getAbility("Lighteningbreath"));
			break;
		}
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setAbility(newMOB.basePhyStats().ability()*2);
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.addBehavior(CMClass.getBehavior("CombatAbilities"));
		newMOB.setLocation(caster.location());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> appears!"));
		newMOB.setStartRoom(null);
		newMOB.addNonUninvokableEffect(this);
		return(newMOB);
	}
}
