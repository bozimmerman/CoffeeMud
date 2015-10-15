package com.planet_ink.coffee_mud.MOBS.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/*
   Copyright 2008-2015 Bo Zimmerman

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
public interface Auctioneer extends ShopKeeper
{
	public String auctionHouse();
	public void setAuctionHouse(String named);

	public double timedListingPrice();
	public void setTimedListingPrice(double d);

	public double timedListingPct();
	public void setTimedListingPct(double d);

	public double timedFinalCutPct();
	public void setTimedFinalCutPct(double d);

	public int maxTimedAuctionDays();
	public void setMaxTimedAuctionDays(int d);

	public int minTimedAuctionDays();
	public void setMinTimedAuctionDays(int d);

	/* //I forgot what these are about
	public double liveListingPrice();
	public void setLiveListingPrice(double d);

	public double liveFinalCutPct();
	public void setLiveFinalCutPct(double d);
	*/

	public static class AuctionData
	{
		public Item		auctioningI		= null;
		public MOB		auctioningM		= null;
		public MOB		highBidderM		= null;
		public String	currency		= "";
		public double	highBid			= 0.0;
		public double	bid				= 0.0;
		public double	buyOutPrice		= 0.0;
		public int		state			= -1;
		public long		tickDown		= 0;
		public long		start			= 0;
		public String	auctionDBKey	= "";

		public int daysRemaining(MOB mob, MOB mob2)
		{
			if (System.currentTimeMillis() >= tickDown)
				return 0;
			Area A = CMLib.map().getStartArea(mob);
			if (A == null)
				A = CMLib.map().getStartArea(mob2);
			long daysRemain = tickDown - System.currentTimeMillis();
			daysRemain = Math.round(Math.floor(CMath.div(CMath.div(daysRemain, CMProps.getMillisPerMudHour()), A.getTimeObj().getHoursInDay())));
			return (int) daysRemain;
		}

		public int daysEllapsed(MOB mob, MOB mob2)
		{
			if (System.currentTimeMillis() < start)
				return 0;
			Area A = CMLib.map().getStartArea(mob);
			if (A == null)
				A = CMLib.map().getStartArea(mob2);
			long daysRemain = System.currentTimeMillis() - start;
			daysRemain = Math.round(Math.floor(CMath.div(CMath.div(daysRemain, CMProps.getMillisPerMudHour()), A.getTimeObj().getHoursInDay())));
			return (int) daysRemain;
		}
	}

	public static class AuctionRates
	{
		public double	liveListPrice	= 0.0;
		public double	timeListPrice	= 0.0;
		public double	timeListPct		= 0.0;
		public double	liveCutPct		= 0.0;
		public double	timeCutPct		= 0.0;
		public int		maxDays			= Integer.MAX_VALUE;
		public int		minDays			= 0;

		public AuctionRates()
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

		public AuctionRates(Auctioneer A)
		{
			if (A == null)
				return;
			final AuctionRates base = new AuctionRates();
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
	}
}
