package com.planet_ink.coffee_mud.Common;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
import com.planet_ink.coffee_mud.Common.interfaces.PrideStats.PrideStat;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;

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
public class DefaultPrideStats implements PrideStats
{
	protected long[]			prideExpireTime		= new long[TimeClock.TimePeriod.values().length];
	protected int[][]			prideStats			= new int[TimeClock.TimePeriod.values().length][PrideStats.PrideStat.values().length];

	@Override
	public String ID()
	{
		return "DefaultPrideStats";
	}

	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultPoll();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (PrideStats) this.clone();
		}
		catch (final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	@Override
	public void bumpPrideStat(final PrideStat stat, final int amt)
	{
		final long now=System.currentTimeMillis();
		if(stat==null)
			return;
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			if(period==TimeClock.TimePeriod.ALLTIME)
				prideStats[period.ordinal()][stat.ordinal()]+=amt;
			else
			{
				if(now>prideExpireTime[period.ordinal()])
				{
					for(final PrideStats.PrideStat stat2 : PrideStats.PrideStat.values())
						prideStats[period.ordinal()][stat2.ordinal()]=0;
					prideExpireTime[period.ordinal()]=period.nextPeriod();
				}
				prideStats[period.ordinal()][stat.ordinal()]+=amt;
			}
		}
	}

	@Override
	public int getPrideStat(final TimePeriod period, final PrideStat stat)
	{
		if((period==null)||(stat==null))
			return 0;
		return prideStats[period.ordinal()][stat.ordinal()];
	}

	@Override
	public String getXML()
	{
		final StringBuilder rest=new StringBuilder("");
		rest.append("<NEXTPRIDEPERIODS>").append(CMParms.toTightListString(prideExpireTime)).append("</NEXTPRIDEPERIODS>");
		rest.append("<PRIDESTATS>");
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
			rest.append(CMParms.toTightListString(prideStats[period.ordinal()])).append(";");
		rest.append("</PRIDESTATS>");
		return rest.toString();
	}

	protected void setXML(final XMLLibrary xmlLib, final List<XMLLibrary.XMLTag> xml)
	{
		final String[] nextPeriods=xmlLib.getValFromPieces(xml, "NEXTPRIDEPERIODS").split(",");
		final String[] prideStats=xmlLib.getValFromPieces(xml, "PRIDESTATS").split(";");
		final Pair<Long,int[]>[] finalPrideStats = CMLib.players().parsePrideStats(nextPeriods, prideStats);
		for(final TimeClock.TimePeriod period : TimeClock.TimePeriod.values())
		{
			if(period.ordinal()<finalPrideStats.length)
			{
				this.prideExpireTime[period.ordinal()]=finalPrideStats[period.ordinal()].first.longValue();
				this.prideStats[period.ordinal()]=finalPrideStats[period.ordinal()].second;
			}
		}
	}

	@Override
	public void setXML(final String str)
	{
		final XMLLibrary xmlLib = CMLib.xml();
		final List<XMLLibrary.XMLTag> xml = xmlLib.parseAllXML(str);
		this.setXML(xmlLib, xml);
	}
}
