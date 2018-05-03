package com.planet_ink.coffee_mud.Common;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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
public class DefaultAuctionPolicy implements AuctionPolicy, Cloneable
{
	private double	liveListPrice	= 0.0;
	private double	timeListPrice	= 0.0;
	private double	timeListPct		= 0.0;
	private double	liveCutPct		= 0.0;
	private double	timeCutPct		= 0.0;
	private int		maxDays			= Integer.MAX_VALUE;
	private int		minDays			= 0;
	
	@Override
	public String ID()
	{
		return "DefaultAuctionPolicy";
	}

	public DefaultAuctionPolicy()
	{
		final List<String> ratesV = CMParms.parseCommas(CMProps.getVar(CMProps.Str.AUCTIONRATES), true);
		while (ratesV.size() < 7)
			ratesV.add("0");
		liveListPrice = CMath.s_double(ratesV.get(0));
		timeListPrice = CMath.s_double(ratesV.get(1));
		timeListPct = CMath.s_pct(ratesV.get(2));
		liveCutPct = CMath.s_pct(ratesV.get(3));
		timeCutPct = CMath.s_pct(ratesV.get(4));
		minDays = CMath.s_int(ratesV.get(5));
		maxDays = CMath.s_int(ratesV.get(6));
		if (minDays > maxDays)
			minDays = maxDays;
	}
	
	@Override
	public String name()
	{
		return ID();
	}

	@Override
	public int compareTo(CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public CMObject newInstance()
	{
		return new DefaultAuctionPolicy();
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (CMObject)this.clone();
		}
		catch(Exception e)
		{
			return newInstance();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public void mergeAuctioneerPolicy(Auctioneer A)
	{
		if (A == null)
			return;
		final DefaultAuctionPolicy base = new DefaultAuctionPolicy();
		liveListPrice = base.liveListPrice;
		timeListPrice = A.timedListingPrice() < 0.0 ? base.timeListPrice : A.timedListingPrice();
		timeListPct = A.timedListingPct() < 0.0 ? base.timeListPct : A.timedListingPct();
		liveCutPct = base.liveCutPct;
		timeCutPct = A.timedFinalCutPct() < 0.0 ? base.timeCutPct : A.timedFinalCutPct();
		maxDays = A.maxTimedAuctionDays() < 0 ? base.maxDays : A.maxTimedAuctionDays();
		minDays = A.minTimedAuctionDays() < 0 ? base.minDays : A.minTimedAuctionDays();
		if (minDays > maxDays)
			minDays = maxDays;
	}
	
	@Override
	public double timedListingPrice()
	{
		return this.timeListPrice;
	}

	@Override
	public void setTimedListingPrice(double d)
	{
		this.timeListPrice = d;
	}

	@Override
	public double timedListingPct()
	{
		return this.timeListPct;
	}

	@Override
	public void setTimedListingPct(double d)
	{
		this.timeListPct = d;
	}

	@Override
	public double timedFinalCutPct()
	{
		return this.timeCutPct;
	}

	@Override
	public void setTimedFinalCutPct(double d)
	{
		this.timeCutPct = d;
	}

	@Override
	public int maxTimedAuctionDays()
	{
		return this.maxDays;
	}

	@Override
	public void setMaxTimedAuctionDays(int d)
	{
		this.maxDays = d;
	}

	@Override
	public int minTimedAuctionDays()
	{
		return this.minDays;
	}

	@Override
	public void setMinTimedAuctionDays(int d)
	{
		this.minDays = d;
	}

	@Override
	public double liveListingPrice()
	{
		return this.liveListPrice;
	}

	@Override
	public void setLiveListingPrice(double d)
	{
		this.liveListPrice = d;
	}

	@Override
	public double liveFinalCutPct()
	{
		return this.liveCutPct;
	}

	@Override
	public void setLiveFinalCutPct(double d)
	{
		this.liveCutPct = d;
	}
}
