package com.planet_ink.coffee_mud.MOBS.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import java.util.Vector;


/*
   Copyright 2000-2007 Bo Zimmerman

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
	public static class AuctionData
	{
		public Item			 auctioningI=null;
		public MOB			 auctioningM=null;
		public MOB           highBidderM=null;
		public String        currency="";
		public double        highBid=Double.MIN_VALUE;
		public double        bid=Double.MIN_VALUE;
		public int           state=-1;
		public long          tickDown=0;
		public long          start=0;
		public String		 auctionKey="";
	}
	public static final int INIRATE_LIVELIST=0;
	public static final int INIRATE_TIMELIST=1;
	public static final int INIRATE_TIMEPCTD=2;
	public static final int INIRATE_LIVECUT=3;
	public static final int INIRATE_TIMECUT=4;
	public static final int INIRATE_MAXDAYS=5;
	public static final int INIRATE_MINDAYS=6;
	public static final int INIRATE_NUM=7;

	public static final int STATE_START=0;
	public static final int STATE_RUNOUT=1;
	public static final int STATE_ONCE=2;
	public static final int STATE_TWICE=3;
    public static final int STATE_THREE=4;
    public static final int STATE_CLOSED=5;

    public String auctionHouse();
    public void setAuctionHouse(String named);

    public double liveListingPrice();
    public void setLiveListingPrice(double d);

    public double timedListingPrice();
    public void setTimedListingPrice(double d);

    public double timedListingPct();
    public void setTimedListingPct(double d);

    public double liveFinalCutPct();
    public void setLiveFinalCutPct(double d);

    public double timedFinalCutPct();
    public void setTimedFinalCutPct(double d);

    public int maxTimedAuctionDays();
    public void setMaxTimedAuctionDays(int d);

    public int minTimedAuctionDays();
    public void setMinTimedAuctionDays(int d);
}