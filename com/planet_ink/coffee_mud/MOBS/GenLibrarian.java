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
   Copyright 2017-2018 Bo Zimmerman

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
public class GenLibrarian extends StdLibrarian
{
	@Override
	public String ID()
	{
		return "GenLibrarian";
	}

	private String PrejudiceFactors="";
	private String libraryChain="main";
	private String IgnoreMask="";

	public GenLibrarian()
	{
		super();
		username="a generic librarian";
		setDescription("She dares you to make a sound.");
		setDisplayText("A generic librarian stands here.");
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
	public String libraryChain()
	{
		return libraryChain;
	}

	@Override
	public void setLibraryChain(String name)
	{
		libraryChain=name;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		CMLib.coffeeMaker().resetGenMOB(this,newText);
	}

	private final static String[] MYCODES={"WHATISELL",
									 "PREJUDICE",
									 "LIBRCHAIN","LIBROVERCHG","LIBRDAYCHG",
									 "LIBROVERPCT","LIBDAYPCT","LIBMINDAYS",
									 "LIBMAXDAYS","LIBMAXBORROW","LIBCMASK",
									 "IGNOREMASK","PRICEMASKS","ITEMMASK"};

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
			return libraryChain();
		case 3:
			return "" + this.getOverdueCharge();
		case 4:
			return "" + this.getDailyOverdueCharge();
		case 5:
			return "" + CMath.toPct(100.0*this.getOverdueChargePct());
		case 6:
			return "" + CMath.toPct(100.0*this.getDailyOverdueChargePct());
		case 7:
			return "" + this.getMinOverdueDays();
		case 8:
			return "" + this.getMaxOverdueDays();
		case 9:
			return "" + this.getMaxBorrowed();
		case 10:
			return this.contributorMask();
		case 11:
			return ignoreMask();
		case 12:
			return CMParms.toListString(itemPricingAdjustments());
		case 13:
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
		case 0:
			setWhatIsSoldMask(CMath.s_long(val));
			break;
		case 1:
			setPrejudiceFactors(val);
			break;
		case 2:
			setLibraryChain(val);
			break;
		case 3:
			this.setOverdueCharge(CMath.s_parseMathExpression(val));
			break;
		case 4:
			this.setDailyOverdueCharge(CMath.s_parseMathExpression(val));
			break;
		case 5:
			this.setOverdueChargePct(CMath.s_pct(val));
			break;
		case 6:
			this.setDailyOverdueChargePct(CMath.s_pct(val));
			break;
		case 7:
			this.setMinOverdueDays(CMath.s_parseIntExpression(val));
			break;
		case 8:
			this.setMaxOverdueDays(CMath.s_parseIntExpression(val));
			break;
		case 9:
			this.setMaxBorrowed(CMath.s_parseIntExpression(val));
			break;
		case 10:
			this.setContributorMask(val);
			break;
		case 11:
			setIgnoreMask(val);
			break;
		case 12:
			setItemPricingAdjustments((val.trim().length() == 0) ? new String[0] : CMParms.toStringArray(CMParms.parseCommas(val, true)));
			break;
		case 13:
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
		final String[] MYCODES=CMProps.getStatCodesList(GenLibrarian.MYCODES,this);
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
		if(!(E instanceof GenLibrarian))
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
