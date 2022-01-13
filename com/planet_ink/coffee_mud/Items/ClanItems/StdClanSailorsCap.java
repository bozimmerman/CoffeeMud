package com.planet_ink.coffee_mud.Items.ClanItems;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Items.Armor.StdArmor;
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
   Copyright 2021-2022 Bo Zimmerman

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
public class StdClanSailorsCap extends StdArmor implements ClanItem
{
	@Override
	public String ID()
	{
		return "StdClanSailorsCap";
	}

	private Environmental	riteOwner		= null;
	protected String		myClan			= "";
	protected ClanItemType	ciType			= ClanItemType.SAILORSCAP;
	private long			lastClanCheck	= 0;
	protected volatile MOB	approvedGetterM	= null;

	public StdClanSailorsCap()
	{
		super();

		setName("a sailors cap");
		basePhyStats.setWeight(1);
		setDisplayText("an captains sailors cap belonging to a clan is here.");
		setDescription("");
		secretIdentity = "";
		baseGoldValue = 1;
		setClanItemType(ClanItem.ClanItemType.SAILORSCAP);
		super.setRawProperLocationBitmap(Wearable.WORN_HEAD);
		material = RawMaterial.RESOURCE_COTTON;
		recoverPhyStats();
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
	public long expirationDate()
	{
		return 0;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if ((System.currentTimeMillis() - lastClanCheck) > TimeManager.MILI_HOUR)
		{
			lastClanCheck = System.currentTimeMillis();
			if ((clanID().length() > 0) && (CMLib.clans().getClanAnyHost(clanID()) == null))
			{
				destroy();
				return;
			}
		}
		if (StdClanItem.stdExecuteMsg(this, msg))
		{
			super.executeMsg(myHost, msg);
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if (StdClanItem.stdOkMessage(this, msg))
		{
			if (clanID().length() > 0)
			{
				if(msg.targetMinor() == CMMsg.TYP_GET)
				{
					final MOB approvedM;
					synchronized(this)
					{
						approvedM=this.approvedGetterM;
					}
					if((msg.source()!=approvedM)&& (msg.amITarget(this)))
					{
						final Clan C=CMLib.clans().getClan(clanID());
						if((C != null)
						&&(msg.source().getClanRole(clanID())!=null))
						{
							if(C.getAuthority(msg.source().getClanRole(clanID()).second.intValue(), Clan.Function.PROPERTY_OWNER)!=Clan.Authority.CAN_DO)
							{
								msg.source().tell(L("You have not earned @x1.",name()));
								return false;
							}
						}
					}
					return super.okMessage(myHost, msg);
				}
				else
				if((msg.targetMinor() == CMMsg.TYP_GIVE)
				&&(msg.tool()==this)
				&&(msg.target() instanceof MOB))
				{
					final Clan C=CMLib.clans().getClan(clanID());
					if((C != null)
					&&(msg.source().getClanRole(clanID())!=null))
					{
						if((C.getAuthority(msg.source().getClanRole(clanID()).second.intValue(), Clan.Function.PROPERTY_OWNER)==Clan.Authority.CAN_DO)
						&&(((MOB)msg.target()).getClanRole(clanID())!=null))
						{
							synchronized(this)
							{
								this.approvedGetterM=(MOB)msg.target();
							}
						}
					}
					return super.okMessage(myHost, msg);
				}
				else
					return super.okMessage(myHost, msg);
			}
			else
				return super.okMessage(myHost, msg);
		}
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
