package com.planet_ink.coffee_mud.Libraries.editors;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;

import java.util.*;

public class EditorCharStatAbbr extends AbilityParmEditorImpl
{

	public EditorCharStatAbbr()
	{
		super("CHAR_STAT_ABBR",CMLib.lang().L("Char Stat Abbr"),ParmType.CHOICES);
	}

	protected static Set<String> getKeys()
	{
		// Rebuilt on each call: CharStats.CODES is not populated yet when the
		// parm editors are first registered, so a cached list can come back empty.
		final Set<String> set = new TreeSet<String>();
		try
		{
			for(final int i : CharStats.CODES.ALLCODES())
			{
				final String abbr = CharStats.CODES.ABBR(i);
				if(abbr.length() > 0)
					set.add(abbr.toLowerCase());
				final String name = CharStats.CODES.NAME(i);
				if(name.length() > 0)
					set.add(name.toLowerCase());
			}
		}
		catch(final Exception e)
		{
			set.clear();
		}
		return set;
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
		return getKeys().contains(oldVal.trim().toLowerCase());
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
