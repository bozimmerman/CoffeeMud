package com.planet_ink.coffee_mud.Items.ClanItems;

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
   Copyright 2004-2018 Bo Zimmerman

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
public class GenClanCommonContainer extends StdClanCommonContainer
{
	@Override
	public String ID()
	{
		return "GenClanCommonContainer";
	}

	protected String	readableText	= "";

	public GenClanCommonContainer()
	{
		super();
		setName("a generic clan worker container");
		basePhyStats.setWeight(2);
		setDisplayText("a generic clan worker container sits here.");
		setDescription("");
		baseGoldValue = 5;
		capacity = 100;
		recoverPhyStats();
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
	public String keyName()
	{
		return readableText;
	}

	@Override
	public void setKeyName(String newKeyName)
	{
		readableText = newKeyName;
	}

	@Override
	public boolean isGeneric()
	{
		return true;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this, false);
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText = "";
		CMLib.coffeeMaker().setPropertiesStr(this, newText, false);
		recoverPhyStats();
	}

	private final static String[]	MYCODES	= { "HASLOCK", "HASLID", "CAPACITY", "CONTAINTYPES", "RESETTIME", "CLANID", "CITYPE", "DEFCLOSED", "DEFLOCKED" };

	@Override
	public String getStat(String code)
	{
		if (CMLib.coffeeMaker().getGenItemCodeNum(code) >= 0)
			return CMLib.coffeeMaker().getGenItemStat(this, code);
		switch (getCodeNum(code))
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
			return clanID();
		case 6:
			return "" + getClanItemType().ordinal();
		case 7:
			return "" + defaultsClosed();
		case 8:
			return "" + defaultsLocked();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(String code, String val)
	{
		if (CMLib.coffeeMaker().getGenItemCodeNum(code) >= 0)
			CMLib.coffeeMaker().setGenItemStat(this, code, val);
		else
		{
			switch (getCodeNum(code))
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
				setClanID(val);
				break;
			case 6:
				setClanItemType(ClanItem.ClanItemType.getValueOf(val));
				break;
			case 7:
				setDoorsNLocks(hasADoor(), isOpen(), CMath.s_bool(val), hasALock(), isLocked(), defaultsLocked());
				break;
			case 8:
				setDoorsNLocks(hasADoor(), isOpen(), defaultsClosed(), hasALock(), isLocked(), CMath.s_bool(val));
				break;
			default:
				CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
				break;
			}
		}
	}

	@Override
	protected int getCodeNum(String code)
	{
		for (int i = 0; i < MYCODES.length; i++)
		{
			if (code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[]	codes	= null;

	@Override
	public String[] getStatCodes()
	{
		if (codes != null)
			return codes;
		final String[] MYCODES = CMProps.getStatCodesList(GenClanCommonContainer.MYCODES, this);
		final String[] superCodes = CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes = new String[superCodes.length + MYCODES.length];
		int i = 0;
		for (; i < superCodes.length; i++)
			codes[i] = superCodes[i];
		for (int x = 0; x < MYCODES.length; i++, x++)
			codes[i] = MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(Environmental E)
	{
		if (!(E instanceof GenClanCommonContainer))
			return false;
		final String[] codes = getStatCodes();
		for (int i = 0; i < codes.length; i++)
		{
			if (!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
