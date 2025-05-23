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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/*
   Copyright 2015-2025 Bo Zimmerman

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
public class DefaultAuction implements AuctionData
{
	@Override
	public String ID()
	{
		return "DefaultAuction";
	}

	@Override
	public String name()
	{
		return ID();
	}

	public Item		auctioningI		= null;
	public MOB		auctioningM		= null;
	public String	auctioningMName	= null;
	public MOB		highBidderM		= null;
	public String	highBidderMName	= null;
	public String	currency		= "";
	public double	highBid			= 0.0;
	public double	bid				= 0.0;
	public double	buyOutPrice		= 0.0;
	public int		state			= -1;
	public long		tickDown		= 0;
	public long		start			= 0;
	public String	auctionDBKey	= "";

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().getDeclaredConstructor().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultAuction();
		}
	}

	@Override
	public int compareTo(final CMObject o)
	{
		return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public CMObject copyOf()
	{
		try
		{
			return (DefaultAuction)this.clone();
		}
		catch(final CloneNotSupportedException e)
		{
			return newInstance();
		}
	}

	@Override
	public int daysRemaining(final MOB mob, final MOB mob2)
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

	@Override
	public int daysEllapsed(final MOB mob, final MOB mob2)
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

	@Override
	public Item getAuctionedItem()
	{
		return auctioningI;
	}

	@Override
	public void setAuctionedItem(final Item auctioningI)
	{
		this.auctioningI = auctioningI;
	}

	@Override
	public MOB getAuctioningMob()
	{
		if(auctioningM==null)
		{
			if(auctioningMName != null)
			{
				auctioningM = CMLib.players().getLoadPlayerAllHosts(auctioningMName);
				auctioningMName = null;
			}
		}
		return auctioningM;
	}

	@Override
	public void setAuctioningMob(final MOB auctioningM)
	{
		this.auctioningM = auctioningM;
	}

	@Override
	public void setAuctioningMobName(final String auctioningMName)
	{
		this.auctioningMName = auctioningMName;
	}

	@Override
	public MOB getHighBidderMob()
	{
		if(highBidderM==null)
		{
			if(highBidderMName != null)
			{
				highBidderM = CMLib.players().getLoadPlayerAllHosts(highBidderMName);
				highBidderMName = null;
			}
		}
		return highBidderM;
	}

	@Override
	public void setHighBidderMob(final MOB highBidderM)
	{
		this.highBidderM = highBidderM;
	}

	@Override
	public void setHighBidderMobName(final String highBidderMName)
	{
		this.highBidderMName = highBidderMName;
	}

	@Override
	public String getCurrency()
	{
		return currency;
	}

	@Override
	public void setCurrency(final String currency)
	{
		this.currency = currency;
	}

	@Override
	public double getHighBid()
	{
		return highBid;
	}

	@Override
	public void setHighBid(final double highBid)
	{
		this.highBid = highBid;
	}

	@Override
	public double getBid()
	{
		return bid;
	}

	@Override
	public void setBid(final double bid)
	{
		this.bid = bid;
	}

	@Override
	public double getBuyOutPrice()
	{
		return buyOutPrice;
	}

	@Override
	public void setBuyOutPrice(final double buyOutPrice)
	{
		this.buyOutPrice = buyOutPrice;
	}

	@Override
	public int getAuctionState()
	{
		return state;
	}

	@Override
	public void setAuctionState(final int state)
	{
		this.state = state;
	}

	@Override
	public long getAuctionTickDown()
	{
		return tickDown;
	}

	@Override
	public void setAuctionTickDown(final long tickDown)
	{
		this.tickDown = tickDown;
	}

	@Override
	public long getStartTime()
	{
		return start;
	}

	@Override
	public void setStartTime(final long start)
	{
		this.start = start;
	}

	@Override
	public String getAuctionDBKey()
	{
		return auctionDBKey;
	}

	@Override
	public void setAuctionDBKey(final String auctionDBKey)
	{
		this.auctionDBKey = auctionDBKey;
	}
}
