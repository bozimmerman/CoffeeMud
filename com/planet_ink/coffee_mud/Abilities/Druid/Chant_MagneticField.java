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
public class Chant_MagneticField extends Chant
{
	@Override public String ID() { return "Chant_MagneticField"; }
	@Override public String name(){return "Magnetic Field";}
	@Override public String displayText(){return "(Magnetic Field chant)";}
	@Override public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_DEEPMAGIC;}
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override protected int canAffectCode(){return CAN_MOBS;}
	@Override public long flags(){return Ability.FLAG_PARALYZING;}

	public boolean wearingHeldMetal(Environmental affected)
	{
		if(affected instanceof MOB)
		{
			final MOB M=(MOB)affected;
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
				if((I!=null)
				&&(I.container()==null)
				&&(CMLib.flags().isMetal(I))
				&&(!I.amWearingAt(Wearable.IN_INVENTORY))
				&&(!I.amWearingAt(Wearable.WORN_HELD))
				&&(!I.amWearingAt(Wearable.WORN_WIELD)))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.source()==affected)
		&&(wearingHeldMetal(affected))
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&(!(msg.tool() instanceof Ability))
		&&((msg.sourceMinor()==CMMsg.TYP_LEAVE)
		||(msg.sourceMinor()==CMMsg.TYP_ENTER)
		||(msg.sourceMinor()==CMMsg.TYP_ADVANCE)
		||(msg.sourceMinor()==CMMsg.TYP_RETREAT)))
		{
			msg.source().tell(_("Your metal armor is holding you in place!"));
			return false;
		}
		else
		if(((CMath.bset(msg.targetMajor(),CMMsg.MASK_DELICATE)
		   ||CMath.bset(msg.targetMajor(),CMMsg.MASK_HANDS)))
		&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
		&&(affected instanceof MOB))
		{
			if((msg.target() instanceof Item)
			&&(CMLib.flags().isMetal(msg.target()))
			&&(((MOB)affected).isMine(msg.target())))
			{
				msg.source().tell(_("The magnetic field around @x1 prevents you from doing that.",((Item)msg.target()).name(msg.source())));
				return false;
			}
			if((msg.tool() instanceof Item)
			&&(CMLib.flags().isMetal(msg.tool()))
			&&(((MOB)affected).isMine(msg.tool())))
			{
				msg.source().tell(_("The magnetic field around @x1 prevents you from doing that.",((Item)msg.tool()).name(msg.source())));
				return false;
			}
		}
		return true;
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
		{
			mob.tell(_("The magnetic field fades!"));
			CMLib.commands().postStand(mob,true);
		}
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;


		boolean success=proficiencyCheck(mob,-(levelDiff*2),auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) at <T-NAMESELF>.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,-levelDiff,-1);
					if(success)
						if(target.location()==mob.location())
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> become(s) surrounded by a powerful magnetic field!!"));
				}
			}
		}
		else
			return maliciousFizzle(mob,target,_("<S-NAME> chant(s) to <T-NAMESELF>, but the spell fades."));

		// return whether it worked
		return success;
	}
}
