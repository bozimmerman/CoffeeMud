package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Poll.PollOption;
import com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class DefaultManufacturer implements Manufacturer
{
	public String ID(){return "DefaultManufacturer";}
	protected String 	name			= "ACME";
	protected byte 		maxTechLevelDiff= 10;
	protected double	efficiency		= 1.0;
	protected double	reliability		= 1.0;
	
	public String name() { return name;}
	
	public CMObject newInstance(){try{return getClass().newInstance();}catch(Exception e){return new DefaultPoll();}}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public CMObject copyOf()
	{
		try
		{
			return (Manufacturer)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return newInstance();
		}
	}
	
	public byte getMaxTechLevelDiff()
	{
		return maxTechLevelDiff;
	}

	public void setMaxTechLevelDiff(byte max)
	{
		this.maxTechLevelDiff = max;
	}
	
	public void setName(String name)
	{
		this.name=name;
	}

	public double getEfficiencyPct()
	{
		return efficiency;
	}

	public void setEfficiencyPct(double pct)
	{
		efficiency=CMath.div(Math.round(pct*100),100.0);
	}
	
	public double getReliabilityPct()
	{
		return reliability;
	}
	
	
	public void setReliabilityPct(double pct)
	{
		reliability=CMath.div(Math.round(pct*100),100.0);
	}

	public String getXml()
	{
		StringBuilder xml=new StringBuilder("");
		xml.append("<NAME>"+CMLib.xml().parseOutAngleBrackets(name)+"</NAME>");
		xml.append("<TECHDIFF>"+maxTechLevelDiff+"</TECHDIFF>");
		xml.append("<EFFICIENCY>"+efficiency+"</EFFICIENCY>");
		xml.append("<RELIABILITY>"+reliability+"</RELIABILITY>");
		return xml.toString();
	}
	
	public void setXml(String xml)
	{
		List<XMLpiece> xpc = CMLib.xml().parseAllXML(xml);
		setName(CMLib.xml().restoreAngleBrackets(CMLib.xml().getValFromPieces(xpc,"NAME")));
		setMaxTechLevelDiff((byte)CMath.s_short(CMLib.xml().getValFromPieces(xpc,"TECHDIFF")));
		setEfficiencyPct(CMLib.xml().getDoubleFromPieces(xpc,"EFFICIENCY"));
		setReliabilityPct(CMLib.xml().getDoubleFromPieces(xpc,"RELIABILITY"));
	}
}
