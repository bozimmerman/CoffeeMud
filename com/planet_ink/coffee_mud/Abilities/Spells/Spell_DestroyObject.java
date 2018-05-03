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
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_DestroyObject extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_DestroyObject";
	}

	private final static String localizedName = CMLib.lang().L("Destroy Object");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		final List<DeadBody> DBs=CMLib.utensils().getDeadBodies(target);
		for(int v=0;v<DBs.size();v++)
		{
			final DeadBody DB=DBs.get(v);
			if(DB.isPlayerCorpse()
			&&(!DB.getMobName().equals(mob.Name())))
			{
				mob.tell(L("You are not allowed to destroy a player corpse."));
				return false;
			}
		}

		if(!CMLib.utensils().canBePlayerDestroyed(mob,target,true))
		{
			mob.tell(L("You are not powerful enough to destroy @x1.",target.name(mob)));
			return false;
		}
		
		if(!super.invoke(mob,commands, givenTarget, auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,(((mob.phyStats().level()+(2*getXLEVELLevel(mob)))-target.phyStats().level())*25),auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
									(auto?"<T-NAME> begins to glow!"
										 :"^S<S-NAME> incant(s) at <T-NAMESELF>!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,L("<T-NAME> vanish(es) into thin air!"));
				target.destroy();
				mob.location().recoverRoomStats();
			}

		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> incant(s) at <T-NAMESELF>, but nothing happens."));

		// return whether it worked
		return success;
	}
}
