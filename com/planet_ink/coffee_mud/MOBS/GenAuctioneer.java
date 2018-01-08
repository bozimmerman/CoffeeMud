package com.planet_ink.coffee_mud.MOBS;
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

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2007-2018 Bo Zimmerman

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
public class GenAuctioneer extends StdAuctioneer
{
	@Override
	public String ID()
	{
		return "GenAuctioneer";
	}

	private String PrejudiceFactors="";
	private String auctionChain="";
	private String IgnoreMask="";

	public GenAuctioneer()
	{
		super();
		username="a generic auctioneer";
		setDescription("He talks so fast, you have no idea what he`s saying.");
		setDisplayText("A generic auctioneer stands here.");
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		if(CMProps.getBoolVar(CMProps.Bool.MOBCOMPRESS))
			miscText=CMLib.encoder().compressString(CMLib.coffeeMaker().getPropertiesStr(this,false));
		else
			miscText=CMLib.coffeeMaker().getPropertiesStr(this,false);
		return super.text();
	}

	@Override
	public String prejudiceFactors()
	{
		return PrejudiceFactors;
	}

	@Override
	public void setPrejudiceFactors(String factors)
	{
		PrejudiceFactors=factors;
	}

	@Override
	public String ignoreMask()
	{
		return IgnoreMask;
	}

	@Override
	public void setIgnoreMask(String factors)
	{
		IgnoreMask=factors;
	}

	@Override
	public String auctionHouse()
	{
		return auctionChain;
	}

	@Override
	public void setAuctionHouse(String named)
	{
		auctionChain=named;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		CMLib.coffeeMaker().resetGenMOB(this,newText);
	}

	private final static String[] MYCODES={"WHATISELL",
										   "PREJUDICE",
										   "AUCHOUSE","LIVEPRICE","TIMEPRICE",
										   "TIMEPCT","LIVECUT","TIMECUT",
										   "MAXADAYS","MINADAYS",
										   "IGNOREMASK","PRICEMASKS",
										   "ITEMMASK"};

	@Override
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenMobCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenMobStat(this,code);
		switch(getCodeNum(code))
		{
		case 0:
			return "" + getWhatIsSoldMask();
		case 1:
			return prejudiceFactors();
		case 2:
			return auctionHouse();
		case 3:
			return "" + timedListingPrice();
		case 4:
			return "" + timedListingPct();
		case 5:
			return "" + timedFinalCutPct();
		case 6:
			return "" + maxTimedAuctionDays();
		case 7:
			return "" + minTimedAuctionDays();
		case 8:
			return ignoreMask();
		case 9:
			return CMParms.toListString(itemPricingAdjustments());
		case 10:
			return this.getWhatIsSoldZappermask();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenMobCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenMobStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:{
			if((val.length()==0)||(CMath.isLong(val)))
				setWhatIsSoldMask(CMath.s_long(val));
			else
			if(CMParms.containsIgnoreCase(ShopKeeper.DEAL_DESCS,val))
				setWhatIsSoldMask(CMParms.indexOfIgnoreCase(ShopKeeper.DEAL_DESCS,val));
			break;
		}
		case 1:
			setPrejudiceFactors(val);
			break;
		case 2:
			setAuctionHouse(val);
			break;
		case 3:
			setTimedListingPrice(CMath.s_parseMathExpression(val));
			break;
		case 4:
			setTimedListingPct(CMath.s_parseMathExpression(val));
			break;
		case 5:
			setTimedFinalCutPct(CMath.s_parseMathExpression(val));
			break;
		case 6:
			setMaxTimedAuctionDays(CMath.s_parseIntExpression(val));
			break;
		case 7:
			setMinTimedAuctionDays(CMath.s_parseIntExpression(val));
			break;
		case 8:
			setIgnoreMask(val);
			break;
		case 9:
			setItemPricingAdjustments((val.trim().length() == 0) ? new String[0] : CMParms.toStringArray(CMParms.parseCommas(val, true)));
			break;
		case 10:
			this.setWhatIsSoldZappermask(val.trim());
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	protected int getCodeNum(String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenAuctioneer.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenMOBCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenAuctioneer))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
