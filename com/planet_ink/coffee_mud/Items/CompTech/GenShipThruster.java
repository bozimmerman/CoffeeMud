package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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
public class GenShipThruster extends StdShipThruster
{
	@Override
	public String ID()
	{
		return "GenShipThruster";
	}
	
	protected String readableText="";

	public GenShipThruster()
	{
		super();
		setName("a generic thruster");
		basePhyStats.setWeight(5000);
		setDisplayText("a generic thruster sits here.");
		setDescription("");
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(String text)
	{
		readableText = text;
	}
	
	@Override
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES","RESETTIME",
										   "POWERCAP","POWERREM","CONSUMEDTYPES","MAXTHRUST","ACTIVATED",
										   "MANUFACTURER", "INSTFACT","DEFCLOSED","DEFLOCKED",
										   "SPECIMPL","FUELEFF","MINTHRUST","ISCONST","AVAILPORTS",
										   "RECHRATE"};
	
	@Override
	public String getStat(String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getCodeNum(code))
		{
		case 0:
			return "" + hasALock();
		case 1:
			return "" + hasADoor();
		case 2:
			return "" + capacity();
		case 3:
			return "" + containTypes();
		case 4:
			return "" + openDelayTicks();
		case 5:
			return "" + powerCapacity();
		case 6:
			return "" + powerRemaining();
		case 7:
		{
			final StringBuilder str=new StringBuilder("");
			for(int i=0;i<getConsumedFuelTypes().length;i++)
			{
				if(i>0)
					str.append(", ");
				str.append(RawMaterial.CODES.NAME(getConsumedFuelTypes()[i]));
			}
			return str.toString();
		}
		case 8:
			return "" + getMaxThrust();
		case 9:
			return "" + activated();
		case 10:
			return "" + getManufacturerName();
		case 11:
			return "" + getInstalledFactor();
		case 12:
			return "" + defaultsClosed();
		case 13:
			return "" + defaultsLocked();
		case 14:
			return "" + getSpecificImpulse();
		case 15:
			return "" + Math.round(getFuelEfficiency() * 100);
		case 16:
			return "" + getMinThrust();
		case 17:
			return "" + isConstantThruster();
		case 18:
			return CMParms.toListString(getAvailPorts());
		case 19:
			return "" + getRechargeRate();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}
	
	@Override
	public void setStat(String code, String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), CMath.s_bool(val), false, CMath.s_bool(val) && defaultsLocked());
			break;
		case 1:
			setDoorsNLocks(CMath.s_bool(val), isOpen(), CMath.s_bool(val) && defaultsClosed(), hasALock(), isLocked(), defaultsLocked());
			break;
		case 2:
			setCapacity(CMath.s_parseIntExpression(val));
			break;
		case 3:
			setContainTypes(CMath.s_parseBitLongExpression(Container.CONTAIN_DESCS, val));
			break;
		case 4:
			setOpenDelayTicks(CMath.s_parseIntExpression(val));
			break;
		case 5:
			setPowerCapacity(CMath.s_parseLongExpression(val));
			break;
		case 6:
			setPowerRemaining(CMath.s_parseLongExpression(val));
			break;
		case 7:
		{
			final List<String> mats = CMParms.parseCommas(val,true);
			final int[] newMats = new int[mats.size()];
			for(int x=0;x<mats.size();x++)
			{
				final int rsccode = RawMaterial.CODES.FIND_CaseSensitive(mats.get(x).trim());
				if(rsccode > 0)
					newMats[x] = rsccode;
			}
			super.setConsumedFuelType(newMats);
			break;
		}
		case 8:
			setMaxThrust(CMath.s_parseIntExpression(val));
			break;
		case 9:
			activate(CMath.s_bool(val));
			break;
		case 10:
			setManufacturerName(val);
			break;
		case 11:
			setInstalledFactor((float)CMath.s_parseMathExpression(val));
			break;
		case 12:
			setDoorsNLocks(hasADoor(), isOpen(), CMath.s_bool(val), hasALock(), isLocked(), defaultsLocked());
			break;
		case 13:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), hasALock(), isLocked(), CMath.s_bool(val));
			break;
		case 14:
			setSpecificImpulse(CMath.s_parseLongExpression(val));
			break;
		case 15:
			setFuelEfficiency(CMath.s_parseMathExpression(val) / 100.0);
			break;
		case 16:
			setMinThrust(CMath.s_parseIntExpression(val));
			break;
		case 17:
			this.setConstantThruster(CMath.s_bool(val));
			break;
		case 18:
			this.setAvailPorts(CMParms.parseEnumList(TechComponent.ShipDir.class, val, ',').toArray(new TechComponent.ShipDir[0]));
			break;
		case 19:
			setRechargeRate(CMath.s_parseLongExpression(val));
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

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenShipThruster.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
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
		if(!(E instanceof GenShipThruster))
			return false;
		final String[] theCodes=getStatCodes();
		for(int i=0;i<theCodes.length;i++)
		{
			if(!E.getStat(theCodes[i]).equals(getStat(theCodes[i])))
				return false;
		}
		return true;
	}
}
