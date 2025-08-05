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
public class EditorBuildingFlags extends AbilityParmEditorImpl
{
	public EditorBuildingFlags()
	{
		super("BUILDING_FLAGS",CMLib.lang().L("Flags"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		if(oldVal.trim().length()==0)
			return true;
		final Pair<String[],String[]> codesFlags = getBuildingCodesNFlags();
		final String[] names = CMParms.parseSpaces(oldVal, true).toArray(new String[0]);
		for(final String name : names)
		{
			if(!CMParms.containsIgnoreCase(codesFlags.second, name))
				return false;
		}
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
		return "";
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		return CMParms.parseSpaces(oldVal, true).toArray(new String[0]);
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String webValue = httpReq.getUrlParameter(fieldName);
		if(webValue == null)
			return oldVal;
		final StringBuilder s=new StringBuilder("");
		String id="";
		int index=0;
		final Pair<String[],String[]> codesFlags = getBuildingCodesNFlags();
		for(;httpReq.isUrlParameter(fieldName+id);id=""+(++index))
		{
			final String newVal = httpReq.getUrlParameter(fieldName+id);
			if(CMParms.containsIgnoreCase(codesFlags.second, newVal.toUpperCase().trim()))
				s.append(" ").append(newVal.toUpperCase().trim());
		}
		return s.toString().trim();
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final StringBuffer str = new StringBuffer("");
		final String webValue = webValue(httpReq,parms,oldVal,fieldName);
		String onChange = null;
		onChange = " MULTIPLE ";
		if(!parms.containsKey("NOSELECT"))
			onChange+= "ONCHANGE=\"MultiSelect(this);\"";
		final Pair<String[],String[]> codesFlags = getBuildingCodesNFlags();
		final String[] fakeValues = this.fakeUserInput(webValue);
		str.append("\n\r<SELECT NAME="+fieldName+onChange+">");
		for(int i=0;i<codesFlags.second.length;i++)
		{
			final String option = (codesFlags.second[i]);
			str.append("<OPTION VALUE=\""+option+"\" ");
			if(CMParms.containsIgnoreCase(fakeValues, option))
				str.append("SELECTED");
			str.append(">"+option);
		}
		return str.toString()+"</SELECT>";
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		final Pair<String[],String[]> codesFlags = getBuildingCodesNFlags();
		final String help=CMParms.combineWith(Arrays.asList(codesFlags.second), ',');
		final String newVal = CMLib.genEd().prompt(mob, oldVal, ++showNumber[0], showFlag, L("Flags"), true, help);
		String[] newVals;
		if(newVal.indexOf(',')>0)
			newVals = CMParms.parseCommas(newVal.toUpperCase().trim(), true).toArray(new String[0]);
		else
		if(newVal.indexOf(';')>0)
			newVals = CMParms.parseSemicolons(newVal.toUpperCase().trim(), true).toArray(new String[0]);
		else
			newVals = CMParms.parse(newVal.toUpperCase().trim()).toArray(new String[0]);
		final StringBuilder finalVal = new StringBuilder("");
		for(int i=0;i<newVals.length;i++)
		{
			if(CMParms.containsIgnoreCase(codesFlags.second, newVals[i]))
				finalVal.append(" ").append(newVals[i]);
		}
		return finalVal.toString().toUpperCase().trim();
	}
}
