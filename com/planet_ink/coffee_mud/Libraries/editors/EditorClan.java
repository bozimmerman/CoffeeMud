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

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/**
 * A clan name, confirmed against the clans the world actually has, since
 * Behaviors/ClanHelper.java stores one. Lookup is by the same rules the
 * class uses: exact id, then any-host id, then the loose findClan match.
 */
public class EditorClan extends AbilityParmEditorImpl
{
	public EditorClan()
	{
		super("CLAN",CMLib.lang().L("Clan"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
		final TreeSet<String> set = new TreeSet<String>();
		for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			final Clan C=c.nextElement();
			if((C!=null)&&(C.name()!=null)&&(C.name().trim().length()>0))
				set.add(C.name().trim());
		}
		createChoices(set.toArray(new String[0]));
	}

	@Override
	public boolean confirmValue(final String oldVal, Map<String, String> args)
	{
		if(oldVal.trim().length()==0)
			return true;
		final String value=oldVal.trim();
		if(CMLib.clans().getClan(value)!=null)
			return true;
		if(CMLib.clans().findClan(value)!=null)
			return true;
		for(final Enumeration<Clan> c=CMLib.clans().clans();c.hasMoreElements();)
		{
			final Clan C=c.nextElement();
			if((C!=null)&&(C.name()!=null)&&(C.name().trim().equalsIgnoreCase(value)))
				return true;
		}
		return false;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}

	@Override
	public String defaultValue()
	{
		return "";
	}
}
