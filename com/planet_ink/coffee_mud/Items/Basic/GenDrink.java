package com.planet_ink.coffee_mud.Items.Basic;
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
   Copyright 2004-2025 Bo Zimmerman

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
public class GenDrink extends StdDrink
{
	@Override
	public String ID()
	{
		return "GenDrink";
	}

	protected String	readableText	= "";

	public GenDrink()
	{
		super();
		setName("a generic drinking container");
		basePhyStats.setWeight(1);
		setDisplayText("a generic drinking container sits here.");
		setDescription("");
		baseGoldValue=1;
		capacity=5;
		amountOfThirstQuenched=100;
		amountOfLiquidHeld=700;
		amountOfLiquidRemaining=700;
		liquidType=RawMaterial.RESOURCE_FRESHWATER;
		setMaterial(RawMaterial.RESOURCE_LEATHER);
		recoverPhyStats();
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this,false);
	}

	@Override
	public int liquidType()
	{
		if((material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)
			return material();
		if(!CMath.isInteger(readableText))
			return liquidType;
		return CMath.s_int(readableText);
	}

	@Override
	public void setLiquidType(final int newLiquidType)
	{
		liquidType = newLiquidType;
		readableText = "" + newLiquidType;
	}

	@Override
	public String keyName()
	{
		return readableText;
	}

	@Override
	public void setKeyName(final String newKeyName)
	{
		readableText=newKeyName;
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(final String text)
	{
		readableText = text;
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(this,newText,false);
		recoverPhyStats();
	}

	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES","RESETTIME","QUENCHED","LIQUIDHELD","LIQUIDTYPE","DEFCLOSED","DEFLOCKED"};

	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getInternalCodeNum(code))
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
			return "" + thirstQuenched();
		case 6:
			return "" + liquidHeld();
		case 7:
			return RawMaterial.CODES.NAME(liquidType());
		case 8:
			return "" + defaultsClosed();
		case 9:
			return "" + defaultsLocked();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getInternalCodeNum(code))
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
			setThirstQuenched(CMath.s_parseIntExpression(val));
			break;
		case 6:
			setLiquidHeld(CMath.s_parseIntExpression(val));
			break;
		case 7:
		{
			int x = CMath.s_parseListIntExpression(RawMaterial.CODES.NAMES(), val);
			x = ((x >= 0) && (x < RawMaterial.RESOURCE_MASK)) ? RawMaterial.CODES.GET(x) : x;
			setLiquidType(x);
			break;
		}
		case 8:
			setDoorsNLocks(hasADoor(), isOpen(), CMath.s_bool(val), hasALock(), isLocked(), defaultsLocked());
			break;
		case 9:
			setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), hasALock(), isLocked(), CMath.s_bool(val));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	private int getInternalCodeNum(final String code)
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
		final String[] MYCODES=CMProps.getStatCodesList(GenDrink.MYCODES,this);
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
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenDrink))
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
