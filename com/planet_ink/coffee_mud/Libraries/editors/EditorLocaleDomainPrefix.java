package com.planet_ink.coffee_mud.Libraries.editors;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;

import java.util.*;

/*
   Copyright 2025-2026 Bo Zimmerman

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
 * The LOCALE_DOMAIN editor, except that a value matches any domain whose
 * name begins with it, the way Behaviors/Mobile.java matches its locale
 * parms. Use LOCALE_DOMAIN where the consumer wants an exact domain.
 */
public class EditorLocaleDomainPrefix extends AbilityParmEditorImpl
{
	public EditorLocaleDomainPrefix(final String ID, final String header, final ParmType type)
	{
		super(ID, header, type);
	}

	public EditorLocaleDomainPrefix()
	{
		this("LOCALE_DOMAIN_PREFIX",CMLib.lang().L("Locale Dom Prefix"),ParmType.CHOICES);
	}

	public List<String> getChoices()
	{
		final List<String> list=new ArrayList<String>();
		list.addAll(Arrays.asList(Room.DOMAIN_INDOORS_DESCS));
		list.addAll(Arrays.asList(Room.DOMAIN_OUTDOOR_DESCS));
		return list;
	}

	@Override
	public void createChoices()
	{
		createChoices(getChoices());
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

	@Override
	public int maxColWidth()
	{
		return 20;
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return 1;
	}

	@Override
	public boolean confirmValue(final String oldVal, final Map<String,String> args)
	{
		if((oldVal==null)||(oldVal.trim().length()==0))
			return true;
		final String val=oldVal.toUpperCase().trim();
		for(final String choice : getChoices())
			if(choice.toUpperCase().startsWith(val))
				return true;
		return false;
	}
}
