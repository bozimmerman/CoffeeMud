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
public class EditorResourceNameOrHerbName extends AbilityParmEditorImpl
{
	public EditorResourceNameOrHerbName()
	{
		super("RESOURCE_NAME_OR_HERB_NAME",CMLib.lang().L("Resrc/Herb"),ParmType.SPECIAL);
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
		if(!oldVal.endsWith("$"))
		{
			return CMParms.contains(RawMaterial.CODES.NAMES(),oldVal);
		}
		return true;
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return "";
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		if(oldVal.endsWith("$"))
			return new String[]{oldVal.substring(0,oldVal.length()-1)};
		return new String[]{oldVal};
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, String oldVal, final String fieldName)
	{
		final AbilityParmEditor A = CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
		if(oldVal.endsWith("$"))
			oldVal = oldVal.substring(0,oldVal.length()-1);
		final String value = A.webValue(httpReq,parms,oldVal,fieldName);
		final int r=RawMaterial.CODES.FIND_IgnoreCase(value);
		if(r>=0)
			return RawMaterial.CODES.NAME(r);
		return (value.trim().length()==0)?"":(value+"$");
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final AbilityParmEditor A = CMLib.ableParms().getEditors().get("RESOURCE_OR_KEYWORD");
		return A.webField(httpReq,parms,oldVal,fieldName);
	}

	@Override
	public String webTableField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal)
	{
		if(oldVal.endsWith("$"))
			return oldVal.substring(0,oldVal.length()-1);
		return oldVal;
	}

	@Override
	public String commandLinePrompt(final MOB mob, String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		++showNumber[0];
		boolean proceed = true;
		String str = oldVal;
		final String orig = oldVal;
		while(proceed&&(mob.session()!=null)&&(!mob.session().isStopped()))
		{
			proceed = false;
			if(oldVal.trim().endsWith("$"))
				oldVal=oldVal.trim().substring(0,oldVal.trim().length()-1);
			str=CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),true,CMParms.toListString(RawMaterial.CODES.NAMES())).trim();
			if(str.equals(orig))
				return orig;
			final int r=RawMaterial.CODES.FIND_IgnoreCase(str);
			if(r==0)
				str="";
			else
			if(r>0)
				str=RawMaterial.CODES.NAME(r);
			if(str.equals(orig))
				return orig;
			if(str.length()==0)
				return "";
			final boolean isResource = CMParms.contains(RawMaterial.CODES.NAMES(),str);
			if((!isResource)&&(mob.session()!=null)&&(!mob.session().isStopped()))
			{
				if(!mob.session().confirm(L("You`ve entered a non-resource item keyword '@x1', ok (Y/n)?",str),"Y"))
					proceed = true;
				else
					str=str+"$";
			}
		}
		return str;
	}

	@Override
	public String defaultValue()
	{
		return "";
	}
}
