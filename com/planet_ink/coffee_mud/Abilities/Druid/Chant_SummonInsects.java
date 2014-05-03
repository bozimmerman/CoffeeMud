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
public class Chant_SummonInsects extends Chant
{
	@Override public String ID() { return "Chant_SummonInsects"; }
	@Override public String name(){ return "Summon Insects";}
	@Override public String displayText(){return "(In a swarm of insects)";}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ANIMALAFFINITY;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public int maxRange(){return adjustedMaxInvokerRange(5);}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override protected int canTargetCode(){return 0;}
	Room castingLocation=null;
	@Override public long flags(){return Ability.FLAG_SUMMONING;}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(affected!=null)
		&&(affected instanceof MOB))
		{
			final MOB M=(MOB)affected;
			if(M.location()!=castingLocation)
				unInvoke();
			else
			if((!M.amDead())&&(M.location()!=null))
			{
				CMLib.combat().postDamage(invoker,M,this,CMLib.dice().roll(1,3+super.getXLEVELLevel(invoker),0),CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,-1,"<T-NAME> <T-IS-ARE> stung by the swarm!");
				if((!M.isInCombat())&&(M!=invoker)&&(M.location()!=null)&&(M.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,M)))
					CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
			}
		}
		return super.tick(ticking,tickID);
	}
	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			if((!mob.amDead())&&(mob.location()!=null))
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,_("<S-NAME> manage(s) to escape the insect swarm!"));
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		 if(mob!=null)
		 {
			 if(!mob.isInCombat())
				 return Ability.QUALITY_INDIFFERENT;
			 final Room R=mob.location();
			 if(R!=null)
			 {
				 if((R.domainType()&Room.INDOORS)>0)
					 return Ability.QUALITY_INDIFFERENT;
			 }
		 }
		 return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell(_("You must be outdoors for this chant to work."));
			return false;
		}

		final Set<MOB> h=properTargets(mob,givenTarget,auto);

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(h==null)
			{
				mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?"A swarm of stinging insects appear, then flutter away!":"^S<S-NAME> chant(s) into the sky.  A swarm of stinging insects appear.  Finding no one to sting, they flutter away.^?");
				return false;
			}
			if(mob.location().show(mob,null,this,verbalCastCode(mob,null,auto),auto?"A swarm of stinging insects appear, then flutter away!":"^S<S-NAME> chant(s) into the sky.  A swarm of stinging insects appears and attacks!^?"))
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),null);
					if((mob.location().okMessage(mob,msg))
					   &&(target.fetchEffect(this.ID())==null))
					{
						mob.location().send(mob,msg);
						if((msg.value()<=0)&&(target.location()==mob.location()))
						{
							castingLocation=mob.location();
							success=maliciousAffect(mob,target,asLevel,((mob.phyStats().level()+(2*super.getXLEVELLevel(mob)))*10),-1);
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> become(s) enveloped by the swarm of stinging insects!"));
						}
					}
				}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> chant(s), but the magic fizzles.");


		// return whether it worked
		return success;
	}
}
