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
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_IceLance extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_IceLance";
	}

	private final static String localizedName = CMLib.lang().L("Ice Lance");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int minRange()
	{
		return 1;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(5);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_WATERBASED;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),L((auto?"A ":"^S<S-NAME> incant(s) and point(s) at <T-NAMESELF>. A ")+"long shard of ice streaks through the air!^?")+CMLib.protocol().msp("spelldam2.wav",40));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_SOMANTIC|CMMsg.TYP_COLD|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				invoker=mob;

				int damage = 0;
				final int maxDie =  (adjustedLevel(mob,asLevel)+(2*super.getX1Level(mob)))/2;
				damage += CMLib.dice().roll(maxDie,6,15);
				mob.location().send(mob,msg2);
				if((msg2.value()>0)||(msg.value()>0))
					damage = (int)Math.round(CMath.div(damage,2.0));

				if(target.location()==mob.location())
					CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_COLD,Weapon.TYPE_FROSTING,L("The lance <DAMAGE> <T-NAME>!"));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> incant(s) and point(s) at <T-NAMESELF>, but flub(s) the spell."));

		// return whether it worked
		return success;
	}
}
