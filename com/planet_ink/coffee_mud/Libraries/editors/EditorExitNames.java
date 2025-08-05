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
public class EditorExitNames extends AbilityParmEditorImpl
{
	public EditorExitNames()
	{
		super("EXIT_NAMES",CMLib.lang().L("Exit Words"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public int appliesToClass(final Object o)
	{
		if(o instanceof String)
		{
			final String chk=((String)o).toUpperCase();
			if(chk.equalsIgnoreCase("DOOR"))
				return 1;
		}
		return -1;
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		if(oldVal.trim().length()==0)
			return true;
		final String[] names = CMParms.parseAny(oldVal.trim(), '|', true).toArray(new String[0]);
		if(names.length > 6)
			return false;
		return true;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}

	@Override
	public String defaultValue()
	{
		return "door|open|close|A closed door.|An open doorway.|";
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{

		final Vector<String> V = new Vector<String>();
		V.addAll(CMParms.parseAny(oldVal.trim(), '|', true));
		while(V.size()<6)
			V.add("");
		return CMParms.toStringArray(V);
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		if(httpReq.isUrlParameter(fieldName+"_W1"))
		{
			final StringBuilder str=new StringBuilder("");
			str.append(httpReq.getUrlParameter(fieldName+"_W1")).append("|");
			str.append(httpReq.getUrlParameter(fieldName+"_W2")).append("|");
			str.append(httpReq.getUrlParameter(fieldName+"_W3")).append("|");
			str.append(httpReq.getUrlParameter(fieldName+"_W4")).append("|");
			str.append(httpReq.getUrlParameter(fieldName+"_W5")).append("|");
			str.append(httpReq.getUrlParameter(fieldName+"_W6"));
			String s=str.toString();
			while(s.endsWith("|"))
				s=s.substring(0,s.length()-1);
			return s;
		}
		else
		{
			return oldVal;
		}
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String[] fieldNames = new String[]{
			CMLib.lang().L("Noun"),
			CMLib.lang().L("Open"),
			CMLib.lang().L("Close"),
			CMLib.lang().L("Closed Display"),
			CMLib.lang().L("Open Display"),
			CMLib.lang().L("Description")
		};
		final StringBuffer str = new StringBuffer("");
		str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
		final String[] vals = this.fakeUserInput(oldVal);
		for(int i=0;i<fieldNames.length;i++)
		{
			str.append("<TR><TD WIDTH=30%><FONT COLOR=WHITE>"+fieldNames[i]+"</FONT></TD>");
			str.append("<TD><INPUT TYPE=TEXT SIZE=30 NAME="+fieldName+"_W"+(i+1)+" VALUE=\""+vals[i]+"\">");
			str.append("</TD></TR>");
		}
		str.append("</TABLE>");
		return str.toString();
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		final String[] vals = this.fakeUserInput(oldVal);
		final StringBuilder newVal = new StringBuilder("");
		newVal.append(CMLib.genEd().prompt(mob, vals[0], ++showNumber[0], showFlag, L("Exit Noun"), true)).append("|");
		newVal.append(CMLib.genEd().prompt(mob, vals[1], ++showNumber[0], showFlag, L("Open Verb"), true)).append("|");
		newVal.append(CMLib.genEd().prompt(mob, vals[2], ++showNumber[0], showFlag, L("Close Verb"), true)).append("|");
		newVal.append(CMLib.genEd().prompt(mob, vals[3], ++showNumber[0], showFlag, L("Opened Text"), true)).append("|");
		newVal.append(CMLib.genEd().prompt(mob, vals[4], ++showNumber[0], showFlag, L("Closed Text"), true)).append("|");
		newVal.append(CMLib.genEd().prompt(mob, vals[5], ++showNumber[0], showFlag, L("Description"), true));
		String s=newVal.toString();
		while(s.endsWith("|"))
			s=s.substring(0,s.length()-1);
		return s;
	}
}
