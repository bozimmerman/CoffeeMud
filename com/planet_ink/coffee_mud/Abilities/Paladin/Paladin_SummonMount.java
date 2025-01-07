package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2002-2024 Bo Zimmerman

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
public class Paladin_SummonMount extends StdAbility implements PrivateProperty
{
	@Override
	public String ID()
	{
		return "Paladin_SummonMount";
	}

	private final static String	localizedName	= CMLib.lang().L("Call Mount");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Mount)");
	}

	private static final String[]	triggerStrings	= I(new String[] { "CALLMOUNT" });

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
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_SUMMONING;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ANIMALAFFINITY;
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
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected instanceof MOB)&&(invoker!=null))
			{
				final MOB mob=(MOB)affected;
				if((mob.amFollowing()==null)
				||(mob.amDead())
				||(mob.location()==null)
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(L("You must be outdoors to call your mount."));
			return false;
		}
		if(!PaladinSkill.paladinAlignmentCheck(this, mob, auto))
			return false;

		if(lastHorse != null)
		{
			final MOB M = lastHorse.get();
			if((M != null)
			&&(CMLib.flags().isInTheGame(M, true))
			&&(!M.amDead()))
			{
				mob.tell(L("Your holy mount is already in the world."));
				return false;
			}
		}

		final List<Integer> choices=new ArrayList<Integer>();
		int fromDir=-1;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room room=mob.location().getRoomInDir(d);
			final Exit exit=mob.location().getExitInDir(d);
			final Exit opExit=mob.location().getReverseExit(d);
			if((room!=null)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
			&&((exit!=null)&&(exit.isOpen()))
			&&(opExit!=null)&&(opExit.isOpen()))
				choices.add(Integer.valueOf(d));
		}
		if(choices.size()==0)
		{
			mob.tell(L("You must be further outdoors to call your mount."));
			return false;
		}
		fromDir=choices.get(CMLib.dice().roll(1,choices.size(),-1)).intValue();
		final Room newRoom=mob.location().getRoomInDir(fromDir);
		final int opDir=mob.location().getReverseDir(fromDir);
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(newRoom!=null))
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?"":L("<S-NAME> call(s) for <S-HIS-HER> loyal steed."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, adjustedLevel(mob,asLevel));
				target.bringToLife(newRoom,true);
				CMLib.beanCounter().clearZeroMoney(target,null);
				target.setMoneyVariation(0);
				target.location().showOthers(target,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				if(target.isInCombat())
					target.makePeace(true);
				CMLib.tracking().walk(target,opDir,false,false);
				if(target.location()==mob.location())
				{
					if(target.isInCombat())
						target.makePeace(true);
					CMLib.commands().postFollow(target,mob,true);
					if(target.amFollowing()!=mob)
						mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
				}
				invoker=mob;
				final Paladin_SummonMount pmA = (Paladin_SummonMount)copyOf();
				pmA.setOwnerName(mob.Name());
				target.addNonUninvokableEffect(pmA);
				this.lastHorse = new WeakReference<MOB>(target);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> call(s) for <S-HIS-HER> loyal steed, but <S-IS-ARE> not answered."));

		// return whether it worked
		return success;
	}

	protected WeakReference<MOB> lastHorse = null;

	public MOB determineMonster(final MOB caster, final int level)
	{
		final MOB newMOB=CMClass.getMOB("GenRideable");
		final Rideable ride=(Rideable)newMOB;
		newMOB.basePhyStats().setAbility(CMProps.getMobHPBase());//normal
		newMOB.basePhyStats().setLevel(level);
		newMOB.basePhyStats().setWeight(500);
		CMLib.factions().setAlignment(newMOB,Faction.Align.GOOD);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		final Deity deity = caster.charStats().getMyDeity();
		final PairList<String, Race> steeds = CMLib.utensils().getFavoredMounts(deity);
		final Pair<String,Race> steed = steeds.get(CMLib.dice().roll(1, steeds.size(), -1));
		final Race steedR = steed.second;
		newMOB.baseCharStats().setMyRace(steedR);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		if(!CMLib.flags().isEvil(caster))
		{
			newMOB.setName(L("a white "+steedR.name().toLowerCase()));
			newMOB.setDisplayText(L("a proud white "+steedR.name().toLowerCase()+" stands here"));
			if(steedR.name().equalsIgnoreCase("Horse"))
				newMOB.setDescription(L("A proud and noble steed; albino white and immaculate."));
		}
		else
		{
			newMOB.setName(L("a black "+steedR.name().toLowerCase()));
			newMOB.setDisplayText(L("a treacherous black "+steedR.name().toLowerCase()+" stands here"));
			if(steedR.name().equalsIgnoreCase("Horse"))
				newMOB.setDescription(L("A cold and ignoble steed; jet black and untrustworthy."));
		}
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience","0 RIDEOK"));
		newMOB.addTattoo("SYSTEM_SUMMONED");
		newMOB.addTattoo("SUMMONED_BY:"+caster.name());
		ride.setRiderCapacity(4);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		return(newMOB);
	}

	@Override
	public int getPrice()
	{
		return 0;
	}

	@Override
	public void setPrice(final int price)
	{
	}

	@Override
	public String getOwnerName()
	{
		return text();
	}

	@Override
	public void setOwnerName(final String owner)
	{
		setMiscText(owner);
	}

	@Override
	public boolean isProperlyOwned()
	{
		return getOwnerName().length()>0;
	}

	@Override
	public String getTitleID()
	{
		return "PALADIN_MOUNT_FOR_"+text();
	}
}
