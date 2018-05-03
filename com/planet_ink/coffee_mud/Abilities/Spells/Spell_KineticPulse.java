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
	Copyright 2017-2018 Bo Zimmerman

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

public class Spell_KineticPulse extends Spell
{
	@Override
	public String ID()
	{
		return "Spell_KineticPulse";
	}

	@Override
	public String name()
	{
		return "Kinetic Pulse";
	}

	@Override
	public int minRange()
	{
		return 2;
	}

	@Override
	public int maxRange()
	{
		return 3;
	}

	@Override
	public int abstractQuality()
	{
		return QUALITY_MALICIOUS;
	}

	@Override
	public Environmental newInstance()
	{
		return new Spell_KineticPulse();
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) 
			return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=super.proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,super.somanticCastCode(mob,target,auto),
					L("<S-NAME> points at <T-NAMESELF> and sends an invisible wave of force towards <S_HIMHER>."));
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				invoker=mob;

				int damage = 0;
				final int maxDie =  (adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob)))/2;
				damage += CMLib.dice().roll(maxDie,6,15);
				mob.location().send(mob,msg2);
				if((msg.value()>0)||(msg2.value()>0))
					damage = (int)Math.round(CMath.div(damage,2.0));

				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_JUSTICE,Weapon.TYPE_BASHING,L("The kinetic force wave <DAMAGES> <T-NAME>!"));
				int percentage = CMLib.dice().roll(1, 100, 0);
				if(percentage < 10)
				{
					CMMsg msg3=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),L("<T-NAME> get(s) shoved backwards by an unseen force."));
					if(mob.location().okMessage(mob,msg3))
					{
						mob.location().send(mob, msg3);
						if(msg3.value()<=0)
						{
							final MOB shoveWhom;
							if(target.getVictim()==mob)
								shoveWhom=target;
							else
							if(mob.getVictim()==target)
								shoveWhom=mob;
							else
								shoveWhom=target;
							int dist=2+getXLEVELLevel(mob)+shoveWhom.rangeToTarget();
							if(mob.location().maxRange()<2)
								dist=mob.location().maxRange();
							shoveWhom.setRangeToTarget(dist);
							if(shoveWhom.getVictim()!=mob)
								shoveWhom.getVictim().setRangeToTarget(shoveWhom.rangeToTarget());
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> incant(s) and point(s) at <T-NAMESELF>, but flub(s) the spell."));


		// return whether it worked
		return success;
	}
}