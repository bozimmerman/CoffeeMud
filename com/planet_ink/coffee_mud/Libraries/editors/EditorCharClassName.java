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
public class EditorCharClassName extends AbilityParmEditorImpl
{
	public EditorCharClassName()
	{
		super("CHAR_CLASS_NAME",CMLib.lang().L("Char Class Name"),ParmType.CHOICES);
	}

	private Set<String> getChoices()
	{
		final Set<String> set = new TreeSet<String>();
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			final CharClass C=c.nextElement();
			if(C==null)
				continue;
			if(C.baseClass().trim().length()>0)
				set.add(C.baseClass().toUpperCase().trim());
			if(C.name().trim().length()>0)
				set.add(C.name().toUpperCase().trim());
		}
        return set;
	}

	@Override
	public void createChoices()
	{
        final Set<String> set = getChoices();
		createChoices(set.toArray(new String[0]));
	}

	@Override
	public boolean confirmValue(final String oldVal, final Map<String,String> args)
	{
		if((oldVal == null) || (oldVal.trim().length() == 0))
			return true;
        final Set<String> set = getChoices();
		return set.contains(oldVal.toUpperCase().trim());
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
