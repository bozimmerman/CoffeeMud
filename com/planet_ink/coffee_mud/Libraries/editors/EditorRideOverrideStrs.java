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
public class EditorRideOverrideStrs extends AbilityParmEditorImpl
{
	public EditorRideOverrideStrs()
	{
		super("RIDE_OVERRIDE_STRS",CMLib.lang().L("Ride Strings"),ParmType.SPECIAL);
	}

	@Override
	public int appliesToClass(final Object o)
	{
		return (o instanceof Rideable) ? 1 : -1;
	}

	@Override
	public void createChoices()
	{
	}

	@Override
	public String defaultValue()
	{
		return "";
	}

	@Override
	public String convertFromItem(final ItemCraftor A, final Item I)
	{
		if(I instanceof Rideable)
		{
			final Rideable R=(Rideable)I;
			final StringBuilder str=new StringBuilder("");
			//STATESTR,STATESUBJSTR,RIDERSTR,MOUNTSTR,DISMOUNTSTR,PUTSTR
			str.append(R.getStateString().replace(';', ',')).append(';');
			str.append(R.getStateStringSubject().replace(';', ',')).append(';');
			str.append(R.getRideString().replace(';', ',')).append(';');
			str.append(R.getMountString().replace(';', ',')).append(';');
			str.append(R.getDismountString().replace(';', ',')).append(';');
			str.append(R.getPutString().replace(';', ','));
			if(str.length()==5)
				return "";
			return str.toString();
		}
		return "";
	}

	@Override
	public boolean confirmValue(final String oldVal)
	{
		return true;
	}

	@Override
	public String webValue(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String[] finput = this.fakeUserInput(oldVal);
		String stateStr=finput[0];
		String stateSubjectStr=finput[1];
		String riderStr=finput[2];
		String mountStr=finput[3];
		String dismountStr=finput[4];
		String putStr=finput[5];
		if(httpReq.isUrlParameter(fieldName+"_RSTATESTR"))
			stateStr = httpReq.getUrlParameter(fieldName+"_RSTATESTR");
		if(httpReq.isUrlParameter(fieldName+"_RSTATESUBJSTR"))
			stateSubjectStr = httpReq.getUrlParameter(fieldName+"_RSTATESUBJSTR");
		if(httpReq.isUrlParameter(fieldName+"_RRIDERSTR"))
			riderStr = httpReq.getUrlParameter(fieldName+"_RRIDERSTR");
		if(httpReq.isUrlParameter(fieldName+"_RMOUNTSTR"))
			mountStr = httpReq.getUrlParameter(fieldName+"_RMOUNTSTR");
		if(httpReq.isUrlParameter(fieldName+"_RDISMOUNTSTR"))
			dismountStr = httpReq.getUrlParameter(fieldName+"_RDISMOUNTSTR");
		if(httpReq.isUrlParameter(fieldName+"_RPUTSTR"))
			putStr = httpReq.getUrlParameter(fieldName+"_RPUTSTR");
		final StringBuilder str=new StringBuilder("");
		str.append(stateStr.replace(';', ',')).append(';');
		str.append(stateSubjectStr.replace(';', ',')).append(';');
		str.append(riderStr.replace(';', ',')).append(';');
		str.append(mountStr.replace(';', ',')).append(';');
		str.append(dismountStr.replace(';', ',')).append(';');
		str.append(putStr.replace(';', ','));
		if(str.length()==5)
			return "";
		return str.toString();
	}

	@Override
	public String webField(final HTTPRequest httpReq, final java.util.Map<String,String> parms, final String oldVal, final String fieldName)
	{
		final String value = webValue(httpReq, parms, oldVal, fieldName);
		final StringBuffer str = new StringBuffer("");
		str.append("<TABLE WIDTH=100% BORDER=\"1\" CELLSPACING=0 CELLPADDING=0>");
		final String[] vals = this.fakeUserInput(value);
		str.append("<TR>");
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("State")+"</FONT></TD>");
		str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RSTATESTR VALUE=\""+vals[0]+"\">");
		str.append("</TR>");
		str.append("<TR>");
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("State Subj.")+"</FONT></TD>");
		str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RSTATESUBJSTR VALUE=\""+vals[1]+"\">");
		str.append("</TR>");
		str.append("<TR>");
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Rider")+"</FONT></TD>");
		str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RRIDERSTR VALUE=\""+vals[2]+"\">");
		str.append("</TR>");
		str.append("<TR>");
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Mount")+"</FONT></TD>");
		str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RMOUNTSTR VALUE=\""+vals[3]+"\">");
		str.append("</TR>");
		str.append("<TR>");
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Dismount")+"</FONT></TD>");
		str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RDISMOUNTSTR VALUE=\""+vals[4]+"\">");
		str.append("</TR>");
		str.append("<TR>");
		str.append("<TD WIDTH=25%><FONT COLOR=WHITE>"+L("Put")+"</FONT></TD>");
		str.append("<TD><INPUT TYPE=TEXT SIZE=50 NAME="+fieldName+"_RPUTSTR VALUE=\""+vals[5]+"\">");
		str.append("</TR>");
		str.append("</TABLE>");
		return str.toString();
	}

	@Override
	public String[] fakeUserInput(final String oldVal)
	{
		final ArrayList<String> V = new ArrayList<String>();
		String stateStr="";
		String stateSubjectStr="";
		String riderStr="";
		String mountStr="";
		String dismountStr="";
		String putStr="";
		if(oldVal.length()>0)
		{
			final List<String> lst=CMParms.parseSemicolons(oldVal.trim(),false);
			if(lst.size()>0)
				stateStr=lst.get(0).replace(';',',');
			if(lst.size()>1)
				stateSubjectStr=lst.get(1).replace(';',',');
			if(lst.size()>2)
				riderStr=lst.get(2).replace(';',',');
			if(lst.size()>3)
				mountStr=lst.get(3).replace(';',',');
			if(lst.size()>4)
				dismountStr=lst.get(4).replace(';',',');
			if(lst.size()>5)
				putStr=lst.get(5).replace(';',',');
		}
		V.add(stateStr);
		V.add(stateSubjectStr);
		V.add(riderStr);
		V.add(mountStr);
		V.add(dismountStr);
		V.add(putStr);
		return CMParms.toStringArray(V);
	}

	@Override
	public String commandLinePrompt(final MOB mob, final String oldVal, final int[] showNumber, final int showFlag) throws java.io.IOException
	{
		final String[] finput = this.fakeUserInput(oldVal);
		String stateStr=finput[0];
		String stateSubjectStr=finput[1];
		String riderStr=finput[2];
		String mountStr=finput[3];
		String dismountStr=finput[4];
		String putStr=finput[5];
		stateStr = CMLib.genEd().prompt(mob, stateStr, ++showNumber[0], showFlag, L("State Str"), true);
		stateSubjectStr = CMLib.genEd().prompt(mob, stateSubjectStr, ++showNumber[0], showFlag, L("State Subject"), true);
		riderStr = CMLib.genEd().prompt(mob, riderStr, ++showNumber[0], showFlag, L("Ride Str"), true);
		mountStr = CMLib.genEd().prompt(mob, mountStr, ++showNumber[0], showFlag, L("Mount Str"), true);
		dismountStr = CMLib.genEd().prompt(mob, dismountStr, ++showNumber[0], showFlag, L("Dismount Str"), true);
		putStr = CMLib.genEd().prompt(mob, putStr, ++showNumber[0], showFlag, L("Put Str"), true);
		final StringBuilder str=new StringBuilder("");
		str.append(stateStr.replace(';', ',')).append(';');
		str.append(stateSubjectStr.replace(';', ',')).append(';');
		str.append(riderStr.replace(';', ',')).append(';');
		str.append(mountStr.replace(';', ',')).append(';');
		str.append(dismountStr.replace(';', ',')).append(';');
		str.append(putStr.replace(';', ','));
		if(str.length()==5)
			return "";
		return str.toString();
	}
}
