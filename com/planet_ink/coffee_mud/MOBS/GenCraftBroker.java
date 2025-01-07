package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
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
public class GenCraftBroker extends StdCraftBroker
{
	@Override
	public String ID()
	{
		return "GenCraftBroker";
	}

	private String prejudiceFactors="";
	private String brokerChain="main";
	private String ignoreMask="";

	public GenCraftBroker()
	{
		super();
		_name="a generic broker";
		setDescription("He looks like he's ready to fascilitate a deal!");
		setDisplayText("A generic broker stands here.");
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
			miscText=CMLib.encoder().compressString(CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this,false));
		else
			miscText=CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this,false);
		return super.text();
	}

	@Override
	public String getRawPrejudiceFactors()
	{
		return prejudiceFactors;
	}

	@Override
	public void setPrejudiceFactors(final String factors)
	{
		prejudiceFactors=factors;
	}

	@Override
	public String getRawIgnoreMask()
	{
		return ignoreMask;
	}

	@Override
	public void setIgnoreMask(final String factors)
	{
		ignoreMask=factors;
	}

	@Override
	public String brokerChain()
	{
		return brokerChain;
	}

	@Override
	public void setBrokerChain(final String name)
	{
		brokerChain=name;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		CMLib.coffeeMaker().resetGenMOB(this,newText);
	}

	private final static String[] MYCODES={"WHATISELL","PREJUDICE",
									 "BROKERCHAIN","MAXLISTINGS","MAXDAYS",
									 "IGNOREMASK","PRICEMASKS","ITEMMASK",
									 "SIVIEWTYPES","BUDGET", "COMPCT"};

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenMobCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenMobStat(this,code);
		switch(getCodeNum(code))
		{
		case 0:
			return "" + getWhatIsSoldMask();
		case 1:
			return getRawPrejudiceFactors();
		case 2:
			return brokerChain();
		case 3:
			return "" + maxListings();
		case 4:
			return "" + maxTimedListingDays();
		case 5:
			return getRawIgnoreMask();
		case 6:
			return CMParms.toListString(getRawItemPricingAdjustments());
		case 7:
			return getWhatIsSoldZappermask();
		case 8:
			return CMParms.toListString(viewFlags());
		case 9:
			return ""+getRawBbudget();
		case 10:
			return ""+commissionPct();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenMobCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenMobStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:
			setWhatIsSoldMask(CMath.s_long(val));
			break;
		case 1:
			setPrejudiceFactors(val);
			break;
		case 2:
			setBrokerChain(val);
			break;
		case 3:
			setMaxListings(CMath.s_parseIntExpression(val));
			break;
		case 4:
			setMaxTimedListingDays(CMath.s_parseIntExpression(val));
			break;
		case 5:
			setIgnoreMask(val);
			break;
		case 6:
			setItemPricingAdjustments((val.trim().length() == 0) ? new String[0] : CMParms.toStringArray(CMParms.parseCommas(val, true)));
			break;
		case 7:
			this.setWhatIsSoldZappermask(val.trim());
			break;
		case 8:
			viewFlags().clear();
			for(final String s : CMParms.parseCommas(val.toUpperCase().trim(), true))
			{
				final ViewType V=(ViewType)CMath.s_valueOf(ViewType.class, s);
				if(V!=null)
					viewFlags().add(V);
			}
			break;
		case 9:
			setBudget(val);
			break;
		case 10:
			setCommissionPct(CMath.parseMathExpression(val));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	@Override
	protected int getCodeNum(final String code)
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
		final String[] MYCODES=CMProps.getStatCodesList(GenCraftBroker.MYCODES,this);
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
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenCraftBroker))
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
