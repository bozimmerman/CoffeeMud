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
   Copyright 2020-2025 Bo Zimmerman

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
public class Chant_ReadRunes extends Chant implements Scroll.ScrollUsage
{

	@Override
	public String ID()
	{
		return "Chant_ReadRunes";
	}

	private final static String	localizedName	= CMLib.lang().L("Read Runes");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Ability to read chants)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ITEMS;
	}

	@Override
	public int getReadMagicType()
	{
		return Ability.ACODE_CHANT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_NATURELORE;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_DIVINING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		// first, using the commands vector, determine
		// the target of the spell.  If no target is specified,
		// the system will assume your combat target.
		final Physical target=getTarget(mob,null,givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&(mob.fetchEffect(this.ID())==null))
		{
			final Ability A=(Ability)this.copyOf();
			mob.addEffect(A);
			try
			{
				CMLib.commands().postRead(mob, target, "", false);
			}
			finally
			{
				mob.delEffect(A);
			}
		}
		else
			return beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) and gaze(s) over <T-NAMESELF>, but nothing more happens."));

		// return whether it worked
		return success;
	}
}
