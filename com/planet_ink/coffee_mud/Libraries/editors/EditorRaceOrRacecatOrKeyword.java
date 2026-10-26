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
public class EditorRaceOrRacecatOrKeyword extends AbilityParmEditorImpl
{
	public EditorRaceOrRacecatOrKeyword()
	{
		super("RACE_OR_RACECAT_OR_KEYWORD",CMLib.lang().L("Race"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
		final TreeSet<String> set = new TreeSet<String>();
		for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
		{
			final Race R=r.nextElement();
			set.add(R.ID().toUpperCase());
			if((R.racialCategory()!=null)&&(R.racialCategory().trim().length()>0))
				set.add(R.racialCategory().toUpperCase().trim());
		}
		createChoices(set.toArray(new String[0]));
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		return true;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		if(httpReq.isUrlParameter(fieldName+"_WHICH"))
		{
			final String which=httpReq.getUrlParameter(fieldName+"_WHICH");
			if(which.trim().length()>0)
				return httpReq.getUrlParameter(fieldName+"_RACE");
			return httpReq.getUrlParameter(fieldName+"_WORD");
		}
		return oldVal;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		String value=webValue(httpReq,parms,oldVal,fieldName);
		if(value.endsWith("$"))
			value = value.substring(0,oldVal.length()-1);
		value = value.trim();
		final String[] raceCatList = choices().toArrayFirst(new String[0]);
		final StringBuffer str = new StringBuffer("");
		str.append("\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH ");
		final boolean isRaceCat=(value.trim().length()==0)||(CMParms.containsIgnoreCase(raceCatList,value));
		if(isRaceCat)
			str.append("CHECKED ");
		str.append("VALUE=\"RACE\">");
		str.append("\n\r<SELECT NAME="+fieldName+"_RACE>");
		for(final String S : raceCatList)
		{
			str.append("<OPTION VALUE=\""+S+"\"");
			if(isRaceCat&&(value.equalsIgnoreCase(S)))
				str.append(" SELECTED");
			str.append(">"+CMStrings.capitalizeAndLower(S));
		}
		str.append("</SELECT>");
		str.append("<BR>");
		str.append("\n\r<INPUT TYPE=RADIO NAME="+fieldName+"_WHICH ");
		if(!isRaceCat)
			str.append("CHECKED ");
		str.append("VALUE=\"\">");
		str.append("\n\r<INPUT TYPE=TEXT NAME="+fieldName+"_WORD VALUE=\""+(isRaceCat?"":value)+"\">");
		return str.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		return new String[] { oldVal };
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		++showNumber[0];
		boolean proceed = true;
		String str = oldVal;
		final String[] raceCatList = choices().toArrayFirst(new String[0]);
		while(proceed&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			proceed = false;
			str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,CMParms.toListString(raceCatList)).trim();
			if(str.equals(oldVal))
				return oldVal;
			if(str.length()==0)
				return "";
			final boolean isRaceCat = CMParms.containsIgnoreCase(raceCatList,str);
			if((!isRaceCat)&&(mob.session()!=null)&&(!mob.session().isStopped()))
				if(!mob.session().confirm(L("You`ve entered a non-race keyword '@x1', ok (Y/n)?",str),"Y"))
					proceed = true;
		}
		return str;
	}

	@Override
	public String defaultValue()
	{
		return "";
	}
}
