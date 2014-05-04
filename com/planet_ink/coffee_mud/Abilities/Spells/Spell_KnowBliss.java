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
public class Spell_KnowBliss extends Spell
{
	@Override public String ID() { return "Spell_KnowBliss"; }
	@Override public String name(){return "Know Bliss";}
	@Override public String displayText(){return "(Know Bliss)";}
	@Override public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public int maxRange(){return adjustedMaxInvokerRange(5);}
	public int hpAdjustment=0;
	@Override public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_ENCHANTMENT;}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
		{
			mob.tell(_("You feel less blissful."));
			if((mob.isMonster())
			   &&(!mob.amDead())
			   &&(mob.location()!=null)
			   &&(mob.location()!=mob.getStartRoom()))
					CMLib.tracking().wanderAway(mob,true,true);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&((msg.value())>0))
			unInvoke();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			// undo the affects of this spell
			if(!(affected instanceof MOB))
				return super.tick(ticking,tickID);
			final MOB mob=(MOB)affected;
			CMLib.tracking().wanderAway(mob,false,true);
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);
		final MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		&&(msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		{
			mob.tell(_("Nah, you feel too happy to do that."));
			mob.setVictim(null);
			return false;
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

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
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> incant(s) happily at <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target.location()==mob.location())
				{
					target.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> smile(s) most peculiarly!"));
					maliciousAffect(mob,target,asLevel,0,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0));
					target.makePeace();
					if(mob.getVictim()==target)
						mob.makePeace();
					for(int m=0;m<target.location().numInhabitants();m++)
					{
						final MOB M=target.location().fetchInhabitant(m);
						if((M!=null)&&(M.getVictim()==target))
							M.makePeace();
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> incant(s) happily at <T-NAMESELF>, but nothing more happens."));

		// return whether it worked
		return success;
	}
}
