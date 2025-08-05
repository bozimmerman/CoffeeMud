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
   Copyright 2008-2025 Bo Zimmerman

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
public class EditorWearLoc extends AbilityParmEditorImpl
{
	public EditorWearLoc()
	{
		super("WEAR_LOC",CMLib.lang().L("Wear Loc"),ParmType.SPECIAL);
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return 0;
	}

	@Override
	public void createChoices()
	{
		final PairList<String,String> list = new PairArrayList<String,String>();
		for(final String w : Wearable.CODES.NAMES())
			list.add(new Pair<String,String>(w,w));
		choices = list;
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		return oldVal.trim().length() > 0;
	}

	@Override
	public String defaultValue()
	{
		return "NECK";
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		return httpReq.getUrlParameter(fieldName+"_WEARLOC");
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String value = webValue(httpReq,parms,oldVal,fieldName);
		final StringBuffer str = new StringBuffer("");
		str.append("\n\r<SELECT NAME="+fieldName+"_WORLOC>");
		final Wearable.CODES codes = Wearable.CODES.instance();
		for(int i=1;i<codes.total();i++)
		{
			final String locstr=codes.name(i);
			str.append("<OPTION VALUE=\""+locstr+"\"");
			if(locstr.equalsIgnoreCase(value))
				str.append(" SELECTED");
			str.append(">"+locstr);
		}
		str.append("</SELECT>");
		return str.toString();
	}

	@Override
	public String convertFromItem(final ItemCraftor C, final Item I)
	{
		return "HELD";
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		return new String[] { oldVal } ;
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		return CMLib.genEd().promptChoice(mob, oldVal, ++showNumber[0], showFlag, L("Wear Location"), choices);
	}
}
