package com.planet_ink.coffee_mud.Items.MiscMagic;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class GenPotion extends StdPotion
{
	@Override
	public String ID()
	{
		return "GenPotion";
	}

	protected String	readableText	= "";

	public GenPotion()
	{
		super();

		setName("a potion");
		basePhyStats.setWeight(1);
		setDisplayText("A potion sits here.");
		setDescription("A strange potion with stranger markings.");
		secretIdentity="";
		baseGoldValue=200;
		recoverPhyStats();
		material=RawMaterial.RESOURCE_GLASS;
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String getSpellList()
	{
		return readableText;
	}

	@Override
	public void setSpellList(String list)
	{
		readableText = list;
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(String text)
	{
		readableText=text;
		setSpellList(readableText);
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}
	
	private final static String[] MYCODES={"HASLOCK","HASLID","CAPACITY","CONTAINTYPES","RESETTIME","QUENCHED","LIQUIDHELD","LIQUIDTYPE","DEFCLOSED","DEFLOCKED"};

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
			return "" + thirstQuenched();
		case 6:
			return "" + liquidHeld();
		case 7:
			return "" + liquidType();
		case 8:
			return "" + defaultsClosed();
		case 9:
			return "" + defaultsLocked();
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
		final String[] MYCODES=CMProps.getStatCodesList(GenPotion.MYCODES,this);
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
		if(!(E instanceof GenPotion))
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
