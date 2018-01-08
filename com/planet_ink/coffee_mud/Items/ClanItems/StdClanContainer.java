package com.planet_ink.coffee_mud.Items.ClanItems;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
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
public class StdClanContainer extends StdContainer implements ClanItem
{
	@Override
	public String ID()
	{
		return "StdClanContainer";
	}

	private Environmental	riteOwner		= null;
	protected ClanItemType	ciType			= ClanItemType.SPECIALOTHER;
	private long			lastClanCheck	= 0;

	public StdClanContainer()
	{
		super();

		setName("a clan container");
		basePhyStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity = "";
		baseGoldValue = 1;
		capacity = 100;
		material = RawMaterial.RESOURCE_OAK;
		recoverPhyStats();
	}

	@Override
	public Environmental rightfulOwner()
	{
		return riteOwner;
	}

	@Override
	public void setRightfulOwner(Environmental E)
	{
		riteOwner = E;
	}

	protected String	myClan	= "";

	@Override
	public ClanItemType getClanItemType()
	{
		return ciType;
	}

	@Override
	public void setClanItemType(ClanItemType type)
	{
		ciType = type;
	}

	@Override
	public String clanID()
	{
		return myClan;
	}

	@Override
	public void setClanID(String ID)
	{
		myClan = ID;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if ((System.currentTimeMillis() - lastClanCheck) > TimeManager.MILI_HOUR)
		{
			if ((clanID().length() > 0) 
			&& (owner() instanceof MOB) 
			&& (!amDestroyed()))
			{
				if ((CMLib.clans().getClan(clanID()) == null) 
				|| ((((MOB) owner()).getClanRole(clanID()) == null) 
					&& (getClanItemType() != ClanItem.ClanItemType.PROPAGANDA)))
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
			if ((clanID().length() > 0) 
			&& (CMLib.clans().getClan(clanID()) == null))
			{
				destroy();
				return;
			}
		}
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
	public boolean tick(Tickable ticking, int tickID)
	{
		if (!StdClanItem.standardTick(this, tickID))
			return false;
		return super.tick(ticking, tickID);
	}
}
