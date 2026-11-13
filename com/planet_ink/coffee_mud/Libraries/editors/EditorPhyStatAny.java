package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_web.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.AbilityComponent.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.RawMaterial.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2026-2026 Bo Zimmerman

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

/**
 * Accepts any spelling of a physical stat field that the parm parser itself
 * will resolve: the 3 character abbreviations (arm, att, dam, spe, ...) and the
 * full field names (armor, attack, damage, speed, ...).<p>
 *
 * This is the counterpart of PHY_STAT_PREFIX, which only accepts the
 * abbreviations, and so called valid text invalid even though
 * CMParms.parseLooseParms resolves the written name against the parm list by
 * prefix and applies "armor+110" to armor quite happily.
 */
public class EditorPhyStatAny extends AbilityParmEditorImpl
{
	// speed is tracked apart from the phystats array, so it has no STAT_DESCS
	// entry of its own and is named here instead.
	protected static final String	SPEED_PREFIX	= "spe";
	protected static final String	SPEED_FULL		= "speed";
	private static Set<String>		KEYS			= null;

	public EditorPhyStatAny()
	{
		super("PHY_STAT_ANY",CMLib.lang().L("Phys Stat Name"),ParmType.CHOICES);
	}

	protected static Set<String> getKeys()
	{
		if(KEYS == null)
		{
			final Set<String> set = new TreeSet<String>();
			for(int i = 0; i < PhyStats.NUM_STATS; i++)
			{
				set.add(CMStrings.limit(PhyStats.STAT_DESCS[i],3).toLowerCase());
				set.add(PhyStats.STAT_DESCS[i].toLowerCase());
			}
			set.add(SPEED_PREFIX);
			set.add(SPEED_FULL);
			KEYS = set;
		}
		return KEYS;
	}

	@Override
	public void createChoices()
	{
		createChoices(getKeys().toArray(new String[0]));
	}

	@Override
	public boolean confirmValue(final String oldVal, final Map<String,String> args)
	{
		if((oldVal == null) || (oldVal.trim().length() == 0))
			return true;
		return isLooseKeyMatch(getKeys(),oldVal);
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}
}
