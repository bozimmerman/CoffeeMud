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
   Copyright 2019-2020 Bo Zimmerman

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
public class Prayer_FaithfulHellhound extends Prayer
{

	@Override
	public String ID()
	{
		return "Prayer_FaithfulHellhound";
	}

	private final static String localizedName = CMLib.lang().L("Faithful HellHound");

	@Override
	public String name()
	{
		return localizedName;
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
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int enchantQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_COMMUNING;
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
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(!mob.isInCombat())
		{
			mob.tell(L("You must be in combat to pray for the faithful hellhound!"));
			return false;
		}
		for(final Enumeration<Pair<MOB,Short>> m=mob.followers();m.hasMoreElements();)
		{
			final MOB M=m.nextElement().first;
			if((M!=null)
			&&(M.fetchEffect(ID())!=null))
			{
				mob.tell(L("You already have a faithful hellhound!"));
				return false;
			}
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":L("^S<S-NAME> @x1 for a faithful hellhound!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB caster=mob;
				final MOB newMOB=CMClass.getMOB("GenMOB");
				newMOB.basePhyStats().setAbility(newMOB.basePhyStats().ability()+(4*super.getXLEVELLevel(caster)));
				newMOB.basePhyStats().setLevel(mob.phyStats().level()+super.getXLEVELLevel(caster));
				newMOB.basePhyStats().setWeight(500);
				newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				newMOB.baseCharStats().setMyRace(CMClass.getRace("Dog"));
				newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
				newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
				newMOB.baseCharStats().setStat(CharStats.STAT_SAVE_FIRE,50);
				newMOB.baseCharStats().setStat(CharStats.STAT_SAVE_ACID,50);
				newMOB.recoverPhyStats();
				newMOB.recoverCharStats();
				newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
				newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
				newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
				newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
				newMOB.basePhyStats().setLevel(mob.phyStats().level());
				newMOB.setName(L("a faithful hellhound"));
				newMOB.setDisplayText(L("an ferocious hellhound is here watching you carefully"));
				newMOB.setDescription(L("He looks like he likes his meat well done."));
				newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience","0"));
				newMOB.recoverCharStats();
				newMOB.recoverPhyStats();
				newMOB.recoverMaxState();
				CMLib.factions().setAlignment(newMOB,Faction.Align.EVIL);
				newMOB.resetToMaxState();
				final Ability loyalA=CMClass.getAbility("Loyalty");
				if(loyalA!=null)
				{
					loyalA.setMiscText("NAME=\""+caster.Name()+"\" TELEPORT=TRUE");
					newMOB.addNonUninvokableEffect(loyalA);
				}
				final Behavior combatB=CMClass.getBehavior("CombatAbilities");
				if(combatB != null)
				{
					combatB.setParms("MINTICKS=1 MAXTICKS=4 CHANCE=99");
					newMOB.addBehavior(combatB);
				}
				final Ability fireA=CMClass.findAbility("Firebreath");
				if(fireA!=null)
				{
					fireA.setProficiency(100);
					newMOB.addAbility(fireA);
				}
				newMOB.text();
				newMOB.bringToLife(caster.location(),true);
				CMLib.beanCounter().clearZeroMoney(newMOB,null);
				newMOB.setMoneyVariation(0);
				newMOB.location().showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> appears!"));
				caster.location().recoverRoomStats();
				newMOB.setStartRoom(null);
				CMLib.commands().postFollow(newMOB,caster,false);
				beneficialAffect(mob,newMOB,asLevel,0);
			}
		}
		else
			beneficialWordsFizzle(mob,null,L("<S-NAME> @x1 for a faithful hellhound, but <S-IS-ARE> not answered.",prayWord(mob)));

		// return whether it worked
		return success;
	}
}
