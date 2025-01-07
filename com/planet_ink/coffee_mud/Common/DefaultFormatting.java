package com.planet_ink.coffee_mud.Common;
import java.util.*;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Formatting.FCode;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
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
public class DefaultFormatting implements Formatting
{
	protected String[]  	clookup			= null;
	protected ColorState	currentColor	= null;
	protected ColorState	lastColor		= null;
	protected int			terminalWidth	 = -1;
	protected final Stack<ColorState>
							markedColors	= new Stack<ColorState>();
	protected boolean[]		formatFlags		= new boolean[FCode.values().length];

	@Override
	public String ID()
	{
		return "DefaultFormatting";
	}

	@Override
	public String name()
	{
		return "DefaultFormatting";
	}

	@Override
	public CMObject newInstance()
	{
		return new DefaultFormatting();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (DefaultFormatting) super.clone();
		}
		catch(final Exception e)
		{
			return new DefaultFormatting();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public String[] getColorCodes()
	{
		if(clookup==null)
			clookup=CMLib.color().standardColorLookups();
		return clookup;
	}

	@Override
	public ColorState getCurrentColor()
	{
		return currentColor;
	}

	@Override
	public void setCurrentColor(final ColorState newColor)
	{
		if(newColor!=null)
			currentColor=newColor;
	}

	@Override
	public ColorState getLastColor()
	{
		return lastColor;
	}

	@Override
	public void setLastColor(final ColorState newColor)
	{
		if(newColor!=null)
			lastColor=newColor;
	}

	@Override
	public ColorState popMarkedColor()
	{
		if(markedColors.size()==0)
			return CMLib.color().getNormalColor();
		return markedColors.pop();
	}

	@Override
	public void pushMarkedColor(final ColorState newColor)
	{
		if(newColor!=null)
			markedColors.push(newColor);
	}

	@Override
	public int getWrap()
	{
		if(terminalWidth>5)
			return terminalWidth;
		return 78;
	}

	@Override
	public boolean isAllowed(final FCode code, final String tag)
	{
		if(code == null)
			return false;
		return formatFlags[code.ordinal()];
	}

	@Override
	public void setAllowed(final FCode code, final String tag, final boolean tf)
	{
		if(code == null)
			return;
		formatFlags[code.ordinal()] = tf;
	}

	@Override
	public int compareTo(final CMObject o)
	{
		if(o == this)
			return 0;
		if(o == null)
			return 1;
		if(o.hashCode()==hashCode())
			return 0;
		if(o.hashCode()>hashCode())
			return -1;
		return 1;
	}

}
