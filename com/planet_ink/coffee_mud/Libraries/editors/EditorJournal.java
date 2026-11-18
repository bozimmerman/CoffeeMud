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
 * A journal name, confirmed against the journals the world actually has,
 * since Behaviors/Conquerable.java and Behaviors/StockMarket.java both
 * store a journal name that must exist for the report to land anywhere.
 */
public class EditorJournal extends AbilityParmEditorImpl
{
	public EditorJournal()
	{
		super("JOURNAL",CMLib.lang().L("Journal"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
		final TreeSet<String> set = new TreeSet<String>();
		final List<String> journals=CMLib.database().DBReadJournals();
		if(journals!=null)
		{
			for(final String journal : journals)
			{
				if((journal!=null)&&(journal.trim().length()>0))
					set.add(journal.trim());
			}
		}
		createChoices(set.toArray(new String[0]));
	}

	@Override
	public boolean confirmValue(final String oldVal, Map<String, String> args)
	{
		if(oldVal.trim().length()==0)
			return true;
		final List<String> journals=CMLib.database().DBReadJournals();
		if(journals==null)
			return true;
		final String value=oldVal.trim();
		for(final String journal : journals)
		{
			if((journal!=null)&&(journal.trim().equalsIgnoreCase(value)))
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
