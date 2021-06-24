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
	protected volatile int	holesInWalls	= 0;
	protected volatile int  lastPctHealth	= 100;
	protected volatile Room targetRoom		= null;
	protected volatile long	targetExpire	= 0;

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
		if (!StdClanItem.stdOkMessage(this, msg))
			return false;

		if((msg.target()==this)
		&&(msg.targetMinor()==CMMsg.TYP_SIT)
		&&hasADoor()
		&&(!isOpen())
		&&(holesInWalls>0)
		&&(msg.sourceMessage().indexOf(mountString(CMMsg.TYP_SIT,msg.source()))>0)
		&&(clanID().length()>0))
		{
			final MOB srcM=(msg.source().amUltimatelyFollowing()==null)?msg.source():msg.source().amUltimatelyFollowing();
			final Clan C=CMLib.clans().getClan(clanID());
			if((C!=null)
			&&(!CMLib.clans().isClanFriendly(srcM, C))
			&&(!CMLib.flags().isSneaking(msg.source())))
			{
				this.isOpen=true;
				final boolean success=super.okMessage(myHost, msg);
				this.isOpen=false;
				if(!success)
					return false;
			}
			else
			if(!super.okMessage(myHost, msg))
				return false;
		}
		else
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==this)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(clanID().length()>0))
		{
			final MOB srcM=(msg.source().amUltimatelyFollowing()==null)?msg.source():msg.source().amUltimatelyFollowing();
			final Clan C=CMLib.clans().getClan(clanID());
			if(C!=null)
			{
				if((!CMLib.clans().isClanFriendly(srcM, C))
				&&(!CMLib.flags().isSneaking(msg.source())))
				{
					if(holesInWalls>0)
					{
						holesInWalls--;
						this.targetExpire=System.currentTimeMillis()+2000;
					}
					else
					{
						msg.source().tell(L("You do not have leave from @x1 to enter there.",C.name()));
						return false;
					}
				}
			}
		}
		return true;
	}

	protected Room getDestinationRoom(final Room fromRoom)
	{
		if(System.currentTimeMillis()>targetExpire)
			return super.getDestinationRoom(fromRoom);
		final Room tr=targetRoom;
		if(tr == null)
			return super.getDestinationRoom(fromRoom);
		return tr;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if (!StdClanItem.standardTick(this, tickID))
			return false;
		if(tickID == Tickable.TICKID_SPECIALCOMBAT)
		{
			if(this.amInTacticalMode())
			{
				if(this.usesRemaining()<lastPctHealth)
				{
					final double pct=CMath.div(lastPctHealth-usesRemaining(), 100.0);
					final int hullPtsLost = (int)Math.round(CMath.mul(pct, this.getMaxHullPoints()));
					if(hullPtsLost >= 50)
					{
						final int newHoles = hullPtsLost/50;
						final Room R=CMLib.map().roomLocation(this);
						if(newHoles == 1)
							R.showHappens(CMMsg.MSG_OK_VISUAL, L("A new temporary breach opens up in @x1.",name()));
						else
							R.showHappens(CMMsg.MSG_OK_VISUAL, L("@x1 temporary breaches open up in @x2.",""+newHoles,name()));
						this.holesInWalls += newHoles;
						final Area A=this.getArea();
						Room tR=null;
						for(int i=0;i<A.numberOfProperIDedRooms()*10;i++)
						{
							final Room rR=A.getRandomProperRoom();
							if((rR!=null)
							&&(rR.domainType()!=Room.DOMAIN_INDOORS_AIR)
							&&(rR.domainType()!=Room.DOMAIN_OUTDOORS_AIR)
							&&(rR.domainType()!=Room.DOMAIN_INDOORS_UNDERWATER)
							&&(rR.domainType()!=Room.DOMAIN_OUTDOORS_UNDERWATER))
							{
								tR=rR;
								break;
							}
						}
						if(tR!=null)
							targetRoom=tR;
					}
				}
				lastPctHealth=this.usesRemaining();
			}
		}
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
