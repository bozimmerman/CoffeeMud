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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_InspectShard extends Chant
{

	@Override
	public String ID()
	{
		return "Chant_InspectShard";
	}

	private final static String localizedName = CMLib.lang().L("Inspect Shard");

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
	public long flags()
	{
		return super.flags() | Ability.FLAG_DIVINING;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_NATURELORE;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!(target instanceof Wand))
		{
			mob.tell(L("@x1 is not a druidic shard.",target.name(mob)));
			return false;
		}

		final Wand wI = (Wand)target;

		if((wI.getEnchantType()!=-1)
		&&(wI.getEnchantType()!=Ability.ACODE_CHANT))
		{
			mob.tell(L("@x1 is not a druidic shard.",target.name(mob)));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> inspect(s) <T-NAMESELF> while softly chanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final StringBuilder info = new StringBuilder("");
				if(wI.getSpell()==null)
					info.append(L("@x1 appears to be mundane.",target.name(mob)));
				else
				{
					info.append(L("@x1 contains the '@x2' magic, and is invoked with the word '@x3'.",target.name(mob),wI.getSpell().name(),wI.magicWord()));
					if(super.getXLEVELLevel(mob)>2)
						info.append(L(" It has @x2 charges.",""+wI.getCharges()));
					if(super.getXLEVELLevel(mob)>5)
						info.append(L(" It can hold at most @x2 charges.",""+wI.getMaxCharges()));
					if(super.getXLEVELLevel(mob)>8)
						info.append(" "+target.secretIdentity());
				}
				if(mob.isMonster())
					CMLib.commands().postSay(mob,null,info.toString(),false,false);
				else
					mob.tell(info.toString());
			}

		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> inspect(s) <T-NAMESELF> while softly chanting, looking more frustrated every second."));

		// return whether it worked
		return success;
	}
}
