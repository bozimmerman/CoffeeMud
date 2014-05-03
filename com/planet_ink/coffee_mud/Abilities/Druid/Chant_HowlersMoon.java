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
public class Chant_HowlersMoon extends Chant
{
	@Override public String ID() { return "Chant_HowlersMoon"; }
	@Override public String name(){ return "Howlers Moon";}
	@Override public String displayText(){return "(Howlers Moon)";}
	@Override public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	@Override public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_MOBS|CAN_ROOMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_MOONALTERING;}
	protected int ticksTicked=0;
	protected int fromDir=-1;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
		{
			if(affected instanceof Room)
				((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,_("The howlers moon sets."));
			super.unInvoke();
			return;
		}

		final MOB mob=(MOB)affected;
		if(mob.amFollowing()==null)
			CMLib.tracking().wanderAway(mob,true,false);
		super.unInvoke();
		if((canBeUninvoked())&&(mob.amFollowing()==null))
		{
			mob.tell(_("You are no longer under the howlers moon."));
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(affected instanceof Room)
		{
			final Room room=(Room)affected;
			if(!room.getArea().getClimateObj().canSeeTheMoon(room,this))
				unInvoke();

			if((++ticksTicked)<20) return true;
			int numWolfs=0;
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)
				&&(M.isMonster())
				&&(M.fetchEffect(ID())!=null))
					numWolfs++;
			}
			if((numWolfs>5)||((invoker()!=null)&&(numWolfs>(invoker().phyStats().level()+(2*super.getXLEVELLevel(invoker())))/10)))
				 return true;
			if(fromDir<0)
			{
				final Vector choices=fillChoices(room);
				if(choices.size()==0)
					return true;
				fromDir=((Integer)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).intValue();
			}
			if(fromDir>=0)
			{
				ticksTicked=0;
				int level=CMLib.ableMapper().lowestQualifyingLevel(ID())+5;
				if(invoker()!=null) level=invoker().phyStats().level()+5+(2*super.getXLEVELLevel(invoker()));
				final MOB target = determineMonster(invoker(),level);
				final Room newRoom=room.getRoomInDir(fromDir);
				final int opDir=Directions.getOpDirectionCode(fromDir);
				target.bringToLife(newRoom,true);
				CMLib.beanCounter().clearZeroMoney(target,null);
				target.location().showOthers(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
				newRoom.recoverRoomStats();
				target.setStartRoom(null);
				CMLib.tracking().walk(target,opDir,false,false);
				if(target.location()==room)
				{
					final int d=CMLib.dice().rollPercentage();
					if((d<33)&&(invoker()!=null)&&(invoker().location()==room))
					{
						CMLib.commands().postFollow(target,invoker(),true);
						beneficialAffect(invoker(),target,0,0);
						if(target.amFollowing()!=invoker())
							target.setVictim(invoker());
					}
					else
					if((d>66)&&(invoker()!=null)&&(invoker().location()==room))
						target.setVictim(invoker());
					beneficialAffect(target,target,0,Ability.TICKS_ALMOST_FOREVER);
				}
				else
				{
					if(target.amDead()) target.setLocation(null);
					target.destroy();
				}
			}
		}
		return true;
	}

	protected Vector fillChoices(Room R)
	{
		final Vector choices=new Vector();
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			final Room room=R.getRoomInDir(d);
			final Exit exit=R.getExitInDir(d);
			final Exit opExit=R.getReverseExit(d);
			if((room!=null)
			&&((room.domainType()&Room.INDOORS)==0)
			&&(room.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
			&&((exit!=null)&&(exit.isOpen()))
			&&(opExit!=null)&&(opExit.isOpen()))
				choices.addElement(Integer.valueOf(d));
		}
		return choices;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			final Room R=mob.location();
			if(R!=null)
			{
				if(!R.getArea().getClimateObj().canSeeTheMoon(R,null))
					return Ability.QUALITY_INDIFFERENT;
				for(final Enumeration<Ability> a=R.effects();a.hasMoreElements();)
				{
					final Ability A=a.nextElement();
					if((A!=null)
					&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONALTERING))
						return Ability.QUALITY_INDIFFERENT;
				}
				final Vector choices=fillChoices(R);
				if(choices.size()==0)
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}


	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room target=mob.location();
		if(target==null) return false;
		if(!target.getArea().getClimateObj().canSeeTheMoon(target,null))
		{
			mob.tell(_("You must be able to see the moon for this magic to work."));
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(_("This place is already under the howler's moon."));
			return false;
		}
		for(final Enumeration<Ability> a=target.effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if((A!=null)
			&&((A.classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_MOONALTERING))
			{
				mob.tell("The moon is already under "+A.name()+", and can not be changed until this magic is gone.");
				return false;
			}
		}


		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell(_("You must be outdoors for this chant to work."));
			return false;
		}
		final Vector choices=fillChoices(mob.location());
		fromDir=-1;
		if(choices.size()==0)
		{
			mob.tell(_("You must be further outdoors to summon an animal."));
			return false;
		}
		fromDir=((Integer)choices.elementAt(CMLib.dice().roll(1,choices.size(),-1))).intValue();

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to the sky.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,_("The Howler's Moon Rises!"));
					ticksTicked=0;
					beneficialAffect(mob,target,asLevel,0);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}

	public MOB determineMonster(MOB caster, int level)
	{
		final MOB newMOB=CMClass.getMOB("GenMob");
		newMOB.basePhyStats().setAbility(0);
		newMOB.basePhyStats().setLevel(level);
		CMLib.factions().setAlignment(newMOB,Faction.Align.NEUTRAL);
		newMOB.basePhyStats().setWeight(350);
		newMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
		newMOB.baseCharStats().setMyRace(CMClass.getRace("Wolf"));
		newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
		newMOB.baseCharStats().setStat(CharStats.STAT_GENDER,'M');
		newMOB.recoverPhyStats();
		newMOB.recoverCharStats();
		newMOB.basePhyStats().setArmor(CMLib.leveler().getLevelMOBArmor(newMOB));
		newMOB.basePhyStats().setAttackAdjustment(CMLib.leveler().getLevelAttack(newMOB));
		newMOB.basePhyStats().setDamage(CMLib.leveler().getLevelMOBDamage(newMOB));
		newMOB.basePhyStats().setSpeed(CMLib.leveler().getLevelMOBSpeed(newMOB));
		newMOB.setName("a ferocious wolf");
		newMOB.setDisplayText("a huge, ferocious wolf is here");
		newMOB.setDescription("Dark black fur, always standing on end surrounds its muscular body.  The eyes are deep red, and his teeth are bared, snarling at you.");
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		Behavior B=CMClass.getBehavior("CorpseEater");
		if(B!=null) newMOB.addBehavior(B);
		B=CMClass.getBehavior("Emoter");
		if(B!=null)
		{
			B.setParms("broadcast sound min=3 max=10 chance=80;howls at the moon.");
			newMOB.addBehavior(B);
		}
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.setStartRoom(null);
		newMOB.text();
		return(newMOB);
	}
}
