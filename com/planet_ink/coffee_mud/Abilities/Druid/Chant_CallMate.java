package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.Follower;
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

public class Chant_CallMate extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_CallMate";
	}

	private final static String	localizedName	= CMLib.lang().L("Call Mate");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Call Mate)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
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
		return Ability.FLAG_SUMMONING|Ability.FLAG_CHARMING;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(invoker!=null))
			{
				final MOB mob=(MOB)affected;
				if(((mob.amFollowing()!=invoker)
				||(mob.amDead())
				||(mob.amFollowing().getGroupMembers(new HashSet<MOB>()).size()>2)
				||(mob.charStats().getMyRace() != invoker.charStats().getMyRace())
				||(mob.location()!=invoker.location())))
					unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)))
		{
			if(msg.sourceMinor()==CMMsg.TYP_DEATH)
			{
				unInvoke();
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.location()!=null)
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> go(es) away."));
			if(mob.amDead())
				mob.setLocation(null);
			mob.destroy();
		}
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
		if(mob==null)
			return false;
		final Room R=mob.location();
		if(R==null)
			return false;
		if(!Druid_ShapeShift.isShapeShifted(mob))
		{
			mob.tell(L("You must be in your animal form to call a mate."));
			return false;
		}
		
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
		{
			mob.tell(L("You must be in the wild to call a mate."));
			return false;
		}
		
		/*
		String raceCat = mob.charStats().getMyRace().racialCategory();
		if((raceCat.equalsIgnoreCase("Fish"))||(raceCat.equalsIgnoreCase("Sea Mammal")))
		{
			if(!CMLib.flags().isWateryRoom(R))
			{
				mob.tell(L("This magic will not work here."));
				return false;
			}
		}
		else
		{
			mob.tell(L("You are not in the proper form to call on a mate here."));
			return false;
		}
		*/
		
		if(mob.getGroupMembers(new HashSet<MOB>()).size()>1)
		{
			mob.tell(L("You already have companions, so a mate would not come to you."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,somanticCastCode(mob,null,auto),auto?"":L("^S<S-NAME> chant(s), calling for <S-HIS-HER> mate.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, mob.charStats().getMyRace());
				beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE / 2);
				mob.location().show(target, null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> answers the call."));
				final Chant_Crossbreed A=(Chant_Crossbreed)CMClass.getAbility("Chant_Crossbreed");
				A.setInvoker(mob);
				A.beneficialAffect(mob,target,asLevel,Ability.TICKS_ALMOST_FOREVER);
				CMLib.commands().postFollow(target,mob,false);
				if(target.amFollowing()!=mob)
					mob.tell(L("@x1 seems unwilling to follow you.",target.name(mob)));
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> chant(s), but nothing happens."));

		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, Race R)
	{
		final MOB victim=caster.getVictim();
		final MOB newMOB=CMClass.getMOB("GenMOB");
		int level=adjustedLevel(caster,0);
		if(level<1)
			level=1;
		newMOB.basePhyStats().setLevel(level);
		newMOB.baseCharStats().setMyRace(R);
		char casterGender = (char)caster.charStats().getStat(CharStats.STAT_GENDER);
		char mobGender = casterGender == 'M' ? 'F' : 'M';
		final int oldCat=caster.baseCharStats().ageCategory();
		String name=R.makeMobName(mobGender, R.getAgingChart()[oldCat]);
		name=CMLib.english().startWithAorAn(name).toLowerCase();
		newMOB.setName(name);
		newMOB.setDisplayText(L("@x1 is here.",name));
		newMOB.setDescription("");
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		final Ability A=CMClass.getAbility("Fighter_Rescue");
		A.setProficiency(100);
		newMOB.addAbility(A);
		newMOB.setVictim(victim);
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setAbility(newMOB.basePhyStats().ability()*2);
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment((int)CMath.mul(CMLib.leveler().getLevelAttack(newMOB),0.8));
		newMOB.basePhyStats().setDamage((int)CMath.mul(CMLib.leveler().getLevelMOBDamage(newMOB),0.9));
		newMOB.basePhyStats().setSpeed((int)CMath.mul(CMLib.leveler().getLevelMOBSpeed(newMOB),0.9));
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'N');
		newMOB.basePhyStats().setSensesMask(newMOB.basePhyStats().sensesMask()|PhyStats.CAN_SEE_DARK);
		newMOB.setLocation(caster.location());
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.setMiscText(newMOB.text());
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(caster.location(),true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		newMOB.setMoneyVariation(0);
		if(victim != null)
		{
			if(victim.getVictim()!=newMOB)
				victim.setVictim(newMOB);
			if(newMOB.getVictim()!=victim)
				newMOB.setVictim(victim);
			newMOB.location().showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,L("<S-NAME> start(s) attacking <T-NAMESELF>!"));
		}
		newMOB.setStartRoom(null);
		return(newMOB);
	}
}
