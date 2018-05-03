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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_WatchfulHound extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_WatchfulHound";
	}

	private final static String localizedName = CMLib.lang().L("Watchful Hound");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Watchful Hound)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
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
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
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
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL, L("The watchful hound runs away."));
			mob.destroy();
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			final MOB invoker=invoker();
			if(invoker != null)
			{
				if((msg.amISource(mob)||msg.amISource(invoker)||msg.amISource(mob.amFollowing())))
				{
					if(msg.sourceMinor()==CMMsg.TYP_QUIT)
					{
						unInvoke();
						if(msg.source().playerStats()!=null)
							msg.source().playerStats().setLastUpdated(0);
					}
				}
				else
				if((msg.targetMinor()==CMMsg.TYP_ENTER) && (msg.target() == mob.location()))
				{
					if((invoker.location()!=mob.location())||(!CMLib.flags().isInTheGame(invoker, true)))
						unInvoke();
					else
					if(CMLib.flags().isPossiblyAggressive(msg.source()))
					{
						final Room R = mob.location();
						CMLib.threads().scheduleRunnable(new Runnable(){

							@Override
							public void run() 
							{
								if(R != null)
									R.show(mob, msg.source(), CMMsg.MSG_NOISE, L("<S-NAME> start(s) barking angrily at <T-NAME>."));
								if(CMLib.flags().isSleeping(invoker))
									CMLib.commands().forceStandardCommand(mob, "Wake", new XVector<String>("Wake","$" + invoker.Name() + "$"));
							}
						}, 1000);
					}
				}
				else
				if(((!invoker.isInCombat()) || (!mob.isInCombat()) || (!msg.source().isInCombat())) 
				&& msg.isTarget(CMMsg.MASK_MALICIOUS) && (msg.target() == invoker))
				{
					if((msg.source().getVictim() == invoker) || (msg.source().getVictim() == null))
						msg.source().setVictim(mob);
					if(mob.getVictim() == null)
						mob.setVictim(msg.source());
				}
			}
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
				if((mob.location()==null)
				||(mob.amDead())
				||((invoker!=null)
					&&((mob.location()!=invoker.location())||(!CMLib.flags().isInTheGame(invoker, true)))))
				{
					mob.delEffect(this);
					if(mob.amDead())
						mob.setLocation(null);
					mob.destroy();
				}
				mob.setFollowing(null);
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(mob.isInCombat())
		{
			mob.tell(L("Not while you are fighting!"));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> conjure(s) up a watchful hound.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.phyStats().level()+((getX1Level(mob)+getXLEVELLevel(mob))/2));
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to conjure a watchful hound, but choke(s) it."));

		// return whether it worked
		return success;
	}
	
	public MOB determineMonster(MOB caster, int level)
	{

		final MOB newMOB=CMClass.getMOB("GenMob");
		newMOB.basePhyStats().setAbility(5);
		newMOB.basePhyStats().setLevel(level);
		newMOB.basePhyStats().setWeight(500);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Dog"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(1);
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.setName(L("a watchful hound"));
		newMOB.setDisplayText(L("a hound dog is here watching you carefully"));
		newMOB.setDescription(L("Those sad eyes never leave you, and those teeth look sharp."));
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.basePhyStats().setArmor(newMOB.basePhyStats().armor()-50);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
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
