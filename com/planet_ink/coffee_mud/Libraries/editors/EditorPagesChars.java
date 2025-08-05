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
public class EditorPagesChars extends AbilityParmEditorImpl
{
	public EditorPagesChars()
	{
		super("PAGES_CHARS",CMLib.lang().L("Max Pgs/Chrs."),ParmType.SPECIAL);
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return (o instanceof Book) ? 1 : -1;
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public String defaultValue()
	{
		return "1/0";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		if(I instanceof Book)
			return ""+((Book)I).getMaxPages()+"/"+((Book)I).getMaxCharsPerPage();
		return "1/0";
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		return oldVal.trim().length() > 0;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		int maxPages=1;
		int maxCharsPage=0;
		if(oldVal.length()>0)
		{
			final int x=oldVal.indexOf('/');
			if(x>0)
			{
				maxPages=CMath.s_int(oldVal.substring(0,x));
				maxCharsPage=CMath.s_int(oldVal.substring(x+1));
			}
		}
		if(httpReq.isUrlParameter(fieldName+"_MAXPAGES"))
			maxPages = CMath.s_int(httpReq.getUrlParameter(fieldName+"_MAXPAGES"));
		if(httpReq.isUrlParameter(fieldName+"_MAXCHARSPAGE"))
			maxCharsPage = CMath.s_int(httpReq.getUrlParameter(fieldName+"_MAXCHARSPAGE"));
		return ""+maxPages+"/"+maxCharsPage;
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String value = webValue(httpReq, parms, oldVal, fieldName);
		final StringBuffer str = new StringBuffer("");
		str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0><TR>");
		final String[] vals = this.fakeUserInput(value);
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Max Pages")+"</FONT></TD>");
		str.append("<TD WIDTH=25%><INPUT TYPE=TEXT SIZE=5 NAME="+fieldName+"_MAXPAGES VALUE=\""+vals[0]+"\">");
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Max Chars Page")+"</FONT></TD>");
		str.append("<TD WIDTH=25%><INPUT TYPE=TEXT SIZE=5 NAME="+fieldName+"_MAXCHARSPAGE VALUE=\""+vals[1]+"\">");
		str.append("</TD>");
		str.append("</TR></TABLE>");
		return str.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		final ArrayList<String> V = new ArrayList<String>();
		int maxPages=1;
		int maxCharsPage=0;
		if(oldVal.length()>0)
		{
			final int x=oldVal.indexOf('/');
			if(x>0)
			{
				maxPages=CMath.s_int(oldVal.substring(0,x));
				maxCharsPage=CMath.s_int(oldVal.substring(x+1));
			}
		}
		V.add(""+maxPages);
		V.add(""+maxCharsPage);
		return CMParms.toStringArray(V);
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		final String[] input=this.fakeUserInput(oldVal);
		int maxPages=CMath.s_int(input[0]);
		int maxCharsPage=CMath.s_int(input[1]);
		maxPages = CMLib.genEd().prompt(mob, maxPages, ++showNumber[0], showFlag, L("Max Pages"), null);
		maxCharsPage = CMLib.genEd().prompt(mob, maxCharsPage, ++showNumber[0], showFlag, L("Max Chars/Page"), null);
		return maxPages+"/"+maxCharsPage;
	}
}
