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

import java.util.Enumeration;
import java.util.Vector;



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
public class Chant_DeathMoon extends Chant
{
	@Override public String ID() { return "Chant_DeathMoon"; }
	@Override public String name(){ return "Death Moon";}
	@Override public String displayText(){return "(Death Moon)";}
	@Override public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	@Override protected int canAffectCode(){return CAN_ROOMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_MOONALTERING;}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
			if(affected instanceof Room)
				((Room)affected).showHappens(CMMsg.MSG_OK_VISUAL,"The death moon sets.");

		super.unInvoke();

	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask() |  PhyStats.CAN_NOT_SPEAK);
		affectableStats.setSensesMask(affectableStats.sensesMask() |  PhyStats.CAN_NOT_HEAR);
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
			else
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(M!=invoker))
				{
					CMLib.combat().postDamage(invoker,M,this,CMLib.dice().roll(1,M.phyStats().level()+(2*super.getXLEVELLevel(invoker())),0),CMMsg.MASK_ALWAYS|CMMsg.TYP_UNDEAD,Weapon.TYPE_BURSTING,"The gaze of the death moon <DAMAGE> <T-NAME>!");
					if((!M.isInCombat())&&(M.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,M)))
						CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
				}
			}
		}
		return true;
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
			mob.tell("You must be able to see the moon for this magic to work.");
			return false;
		}
		if(target.fetchEffect(ID())!=null)
		{
			mob.tell("This place is already under the death moon.");
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
					for(int i=0;i<target.numInhabitants();i++)
					{
						final MOB M=target.fetchInhabitant(i);
						if((M!=null)&&(mob!=M))
							mob.location().show(mob,M,CMMsg.MASK_MALICIOUS|CMMsg.TYP_OK_VISUAL,null);
					}
					mob.location().showHappens(CMMsg.MSG_OK_VISUAL,"The Death Moon Rises!");
					beneficialAffect(mob,target,asLevel,10);
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> chant(s) to the sky, but the magic fades.");
		// return whether it worked
		return success;
	}
}
