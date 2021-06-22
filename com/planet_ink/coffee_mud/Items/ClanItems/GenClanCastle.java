package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.GenCastle;
import com.planet_ink.coffee_mud.Items.Basic.GenSiegableBoardable;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.ClanItem.ClanItemType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;

/*
   Copyright 2021-2021 Bo Zimmerman

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
public class GenClanCastle extends GenCastle implements ClanItem
{
	@Override
	public String ID()
	{
		return "GenCastle";
	}

	private Environmental	riteOwner		= null;
	protected ClanItemType	ciType			= ClanItemType.SPECIALOTHER;
	protected String		myClan			= "";

	public GenClanCastle()
	{
		super();
		setName("the castle [NEWNAME]");
		setDisplayText("the castle [NEWNAME] is here.");
		setMaterial(RawMaterial.RESOURCE_STONE);
		this.doorName="portcullis";
	}

	@Override
	public Environmental rightfulOwner()
	{
		return riteOwner;
	}

	@Override
	public void setRightfulOwner(final Environmental E)
	{
		riteOwner = E;
	}

	@Override
	public ClanItemType getClanItemType()
	{
		return ciType;
	}

	@Override
	public void setClanItemType(final ClanItemType type)
	{
		ciType = type;
	}

	@Override
	public String clanID()
	{
		return myClan;
	}

	@Override
	public void setClanID(final String ID)
	{
		myClan = ID;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if (StdClanItem.stdExecuteMsg(this, msg))
			super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if (StdClanItem.stdOkMessage(this, msg))
			return super.okMessage(myHost, msg);
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if (!StdClanItem.standardTick(this, tickID))
			return false;
		return super.tick(ticking, tickID);
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenClanCastle))
			return false;
		return super.sameAs(E);
	}

	private final static String[] MYCODES=
	{
		"CLANID", "CITYPE"
	};

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenClanCastle.MYCODES,this);
		final String[] superCodes=super.getStatCodes();
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public String getStat(final String code)
	{
		if(CMParms.contains(MYCODES, code))
		{
			switch(CMParms.indexOf(GenClanCastle.MYCODES, code.toUpperCase().trim()))
			{
			case 0:
				return clanID();
			case 1:
				return "" + getClanItemType().ordinal();
			default:
				return "";
			}
		}
		else
			return super.getStat(code);
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMParms.contains(GenClanCastle.MYCODES, code.toUpperCase().trim()))
		{
			switch(CMParms.indexOf(GenClanCastle.MYCODES, code.toUpperCase().trim()))
			{
			case 0:
				setClanID(val);
				break;
			case 1:
				setClanItemType(ClanItem.ClanItemType.getValueOf(val));
				break;
			}
		}
		else
			super.setStat(code, val);
	}
}
