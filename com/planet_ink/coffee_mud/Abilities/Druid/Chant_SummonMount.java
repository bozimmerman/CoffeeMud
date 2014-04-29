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

@SuppressWarnings({"unchecked","rawtypes"})
public class Chant_SummonMount extends Chant
{
	@Override public String ID() { return "Chant_SummonMount"; }
	@Override public String name(){ return "Summon Mount";}
	@Override public String displayText(){return "(Mount)";}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;}
	@Override public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public long flags(){return Ability.FLAG_SUMMONING;}

	@Override
	public void unInvoke()
	{
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			mob.setFollowing(null);
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
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
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((affected!=null)&&(affected instanceof MOB)&&(invoker!=null))
			{
				final MOB mob=(MOB)affected;
				if(((mob.amFollowing()==null)
				||(mob.amDead())
				||((invoker!=null)&&(mob.location()!=invoker.location())&&(invoker.riding()!=affected))))
				{
					mob.delEffect(this);
					mob.setFollowing(null);
					if(mob.amDead()) mob.setLocation(null);
					mob.destroy();
					return false;
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		final Vector choices=new Vector();
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
				choices.addElement(Integer.valueOf(d));
		}
		if(choices.size()==0)
		{
			mob.tell("You must be further outdoors to summon a mount.");
			return false;
		}
		fromDir=((Integer)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).intValue();
		final Room newRoom=mob.location().getRoomInDir(fromDir);
		final int opDir=Directions.getOpDirectionCode(fromDir);
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if((success)&&(newRoom!=null))
		{
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) humbly for a mount.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final MOB target = determineMonster(mob, adjustedLevel(mob,asLevel));
				target.bringToLife(newRoom,true);
				CMLib.beanCounter().clearZeroMoney(target,null);
				target.location().showOthers(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				if(target.isInCombat()) target.makePeace();
				CMLib.tracking().walk(target,opDir,false,false);
				if(target.location()==mob.location())
				{
					if(target.isInCombat()) target.makePeace();
					CMLib.commands().postFollow(target,mob,true);
					if(target.amFollowing()!=mob)
						mob.tell(target.name(mob)+" seems unwilling to follow you.");
				}
				invoker=mob;
				target.addNonUninvokableEffect((Ability)copyOf());
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) humbly for a mount, but <S-IS-ARE> not answered.");

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
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Horse"));
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.setName("a wild horse");
		newMOB.setDisplayText("a wild horse stands here");
		newMOB.setDescription("An untamed beast of the fields, tame only by magical means.");
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		ride.setRiderCapacity(1);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.text();
		return(newMOB);


	}
}
