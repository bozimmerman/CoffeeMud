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
 * Accepts any spelling of a character stat that the parm parser itself will
 * resolve: everything CHAR_STAT accepts (the 3 character abbreviations such as
 * str, wis, chr, savefir, and their max forms), the full stat names such as
 * strength, wisdom, and charisma, and the full saving throw names with their
 * save prefix, such as savemagic and saveelec.<p>
 *
 * The reason is the same as for EditorCharStateAny: because
 * CMParms.parseLooseParms resolves a written parm name against the parm list by
 * prefix, "wisdom+2" and "savemagic+2" are applied to the right fields by the
 * code that reads them, so a checker must not call them invalid.
 */
public class EditorCharStatAny extends AbilityParmEditorImpl
{
	private static Set<String>	KEYS	= null;

	public EditorCharStatAny()
	{
		super("CHAR_STAT_ANY",CMLib.lang().L("Char Stat Name"),ParmType.CHOICES);
	}

	protected static Set<String> getKeys()
	{
		if(KEYS == null)
		{
			final Set<String> set = new TreeSet<String>(EditorCharStat.getKeys());
			for(final int i : CharStats.CODES.BASECODES())
			{
				final String name = CharStats.CODES.NAME(i).toLowerCase();
				set.add(name);
				set.add("max"+name);
			}
			for(final int c : CharStats.CODES.SAVING_THROWS())
				set.add("save"+CharStats.CODES.NAME(c).toLowerCase());
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
