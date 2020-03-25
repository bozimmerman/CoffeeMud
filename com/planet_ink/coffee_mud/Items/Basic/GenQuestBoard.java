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
import com.planet_ink.coffee_mud.Items.ClanItems.GenClanFlag;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;

/*
   Copyright 2020-2020 Bo Zimmerman

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
public class GenQuestBoard extends StdQuestBoard
{
	@Override
	public String ID()
	{
		return "GenQuestBoard";
	}

	protected String	readableText="";
	protected String	sareaMaskStr	= "";
	protected String	tareaMaskStr	= "";
	protected String	aareaMaskStr	= "";
	protected String	giverMaskStr	= "";

	public GenQuestBoard()
	{
		super();
		setName("a generic quest board");
		setDisplayText("a generic quest board sits here.");
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
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(final String text)
	{
		readableText=text;
		for(final String stat : StdQuestBoard.MYCODES)
			setStat(stat,CMParms.getParmStr(text, stat, ""));
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	@Override
	public String getStat(final String code)
	{
		if (CMLib.coffeeMaker().getGenItemCodeNum(code) >= 0)
			return CMLib.coffeeMaker().getGenItemStat(this, code);
		switch (getCodeNum(code))
		{
		case 0:
			return sareaMaskStr;
		case 1:
			return tareaMaskStr;
		case 2:
			return aareaMaskStr;
		case 3:
			return giverMaskStr;
		case 4:
			return typFilter;
		case 5:
			return catFilter;
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if (CMLib.coffeeMaker().getGenItemCodeNum(code) >= 0)
			CMLib.coffeeMaker().setGenItemStat(this, code, val);
		else
		{
			switch (getCodeNum(code))
			{
			case 0:
				sareaMaskStr=val;
				sareaMask=(val.length()==0)?null:CMLib.masking().maskCompile(val);
				break;
			case 1:
				tareaMaskStr=val;
				tareaMask=(val.length()==0)?null:CMLib.masking().maskCompile(val);
				break;
			case 2:
				aareaMaskStr=val;
				aareaMask=(val.length()==0)?null:CMLib.masking().maskCompile(val);
				break;
			case 3:
				giverMaskStr=val;
				giverMask=(val.length()==0)?null:CMLib.masking().maskCompile(val);
				break;
			case 4:
				typFilter=val;
				break;
			case 5:
				catFilter=val;
				break;
			default:
				CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
				break;
			}
		}
	}

	@Override
	protected int getCodeNum(final String code)
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
		final String[] MYCODES = CMProps.getStatCodesList(GenQuestBoard.MYCODES, this);
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
	public boolean sameAs(final Environmental E)
	{
		if (!(E instanceof GenQuestBoard))
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
