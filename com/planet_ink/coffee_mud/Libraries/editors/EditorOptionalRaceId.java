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
public class EditorOptionalRaceId extends AbilityParmEditorImpl
{
	public EditorOptionalRaceId()
	{
		super("OPTIONAL_RACE_ID",CMLib.lang().L("Race"),ParmType.SPECIAL);
	}

	@Override
	public void createChoices()
	{
		createChoices(CMClass.races());
		choices().add("","");
		for(int x=0;x<choices().size();x++)
			choices().get(x).first = choices().get(x).first.toUpperCase();
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		return ""; // absolutely no way to determine
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		if(oldVal.trim().length()==0)
			return true;
		final Vector<String> parsedVals = CMParms.parse(oldVal.toUpperCase());
		for(int v=0;v<parsedVals.size();v++)
		{
			if(CMClass.getRace(parsedVals.elementAt(v))==null)
				return false;
		}
		return true;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		Vector<String> raceIDs=null;
		if(httpReq.isUrlParameter(fieldName+"_RACE"))
		{
			String id="";
			raceIDs=new Vector<String>();
			for(int i=0;httpReq.isUrlParameter(fieldName+"_RACE"+id);id=""+(++i))
				raceIDs.addElement(httpReq.getUrlParameter(fieldName+"_RACE"+id).toUpperCase().trim());
		}
		else
			raceIDs = CMParms.parse(oldVal.toUpperCase().trim());
		return CMParms.combine(raceIDs,0);
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final Vector<String> raceIDs=CMParms.parse(webValue(httpReq,parms,oldVal,fieldName).toUpperCase());
		final StringBuffer str = new StringBuffer("");
		str.append("\n\r<SELECT NAME="+fieldName+"_RACE MULTIPLE>");
		str.append("<OPTION VALUE=\"\" "+((raceIDs.size()==0)?"SELECTED":"")+">");
		for(final Enumeration<Race> e=CMClass.races();e.hasMoreElements();)
		{
			final Race R=e.nextElement();
			str.append("<OPTION VALUE=\""+R.ID()+"\" "+((raceIDs.contains(R.ID().toUpperCase()))?"SELECTED":"")+">"+R.name());
		}
		str.append("</SELECT>");
		return str.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		final Vector<String> parsedVals = CMParms.parse(oldVal.toUpperCase());
		if(parsedVals.size()==0)
			return new String[]{""};
		final Vector<String> races = new Vector<String>();
		for(int p=0;p<parsedVals.size();p++)
		{
			final Race R=CMClass.getRace(parsedVals.elementAt(p));
			races.addElement(R.name());
		}
		for(int p=0;p<parsedVals.size();p++)
		{
			final Race R=CMClass.getRace(parsedVals.elementAt(p));
			races.addElement(R.name());
		}
		races.addElement("");
		return CMParms.toStringArray(races);
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		if((showFlag>0)&&(showFlag!=showNumber[0]))
			return oldVal;
		String behave="NO";
		String newVal = oldVal;
		while((mob.session()!=null)&&(!mob.session().isStopped())&&(behave.length()>0))
		{
			mob.tell(showNumber+". "+prompt()+": '"+newVal+"'.");
			if((showFlag!=showNumber[0])&&(showFlag>-999))
				return newVal;
			final Vector<String> parsedVals = CMParms.parse(newVal.toUpperCase());
			behave=mob.session().prompt(L("Enter a race to add/remove (?)\n\r:"),"");
			if(behave.length()>0)
			{
				if(behave.equalsIgnoreCase("?"))
					mob.tell(CMLib.lister().build3ColTable(mob,CMClass.races()).toString());
				else
				{
					final Race R=CMClass.getRace(behave);
					if(R!=null)
					{
						if(parsedVals.contains(R.ID().toUpperCase()))
						{
							mob.tell(L("'@x1' removed.",behave));
							parsedVals.remove(R.ID().toUpperCase().trim());
							newVal = CMParms.combine(parsedVals,0);
						}
						else
						{
							mob.tell(L("@x1 added.",R.ID()));
							parsedVals.addElement(R.ID().toUpperCase());
							newVal = CMParms.combine(parsedVals,0);
						}
					}
					else
					{
						mob.tell(L("'@x1' is not a recognized race.  Try '?'.",behave));
					}
				}
			}
			else
			{
				if(oldVal.equalsIgnoreCase(newVal))
					mob.tell(L("(no change)"));
			}
		}
		return newVal;
	}
}
