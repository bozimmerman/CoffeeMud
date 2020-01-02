package com.planet_ink.coffee_mud.Items.ClanItems;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Items.Armor.StdArmor;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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

/*
   Copyright 2004-2020 Bo Zimmerman

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

public class StdClanArmor extends StdArmor implements ClanItem
{
	@Override
	public String ID()
	{
		return "StdClanArmor";
	}

	protected String		myClan			= "";
	protected ClanItemType	ciType			= ClanItemType.TABBARD;
	private long			lastClanCheck	= System.currentTimeMillis();
	private Environmental	riteOwner		= null;

	public StdClanArmor()
	{
		super();

		setName("a clan armor piece");
		basePhyStats.setWeight(1);
		setDisplayText("a piece of armor belonging to a clan is here.");
		setDescription("");
		secretIdentity = "";
		baseGoldValue = 1;
		basePhyStats.setArmor(1);
		phyStats.setArmor(1);
		capacity=0;
		material = RawMaterial.RESOURCE_COTTON;
		recoverPhyStats();
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
		super.executeMsg(myHost, msg);
		if ((System.currentTimeMillis() - lastClanCheck) > TimeManager.MILI_HOUR / 2)
		{
			if ((clanID().length() > 0)
			&& (owner() instanceof MOB)
			&& (!amDestroyed()))
			{
				if ((CMLib.clans().getClanAnyHost(clanID()) == null)
				|| ((getClanItemType() != ClanItem.ClanItemType.PROPAGANDA)
					&& (((MOB) owner()).getClanRole(clanID()) == null)))
				{
					final Room R = CMLib.map().roomLocation(this);
					setRightfulOwner(null);
					unWear();
					removeFromOwnerContainer();
					if (owner() != R)
						R.moveItemTo(this, ItemPossessor.Expire.Player_Drop);
					if (R != null)
						R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 is dropped!", name()));
				}
			}
			lastClanCheck = System.currentTimeMillis();
		}
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

}
