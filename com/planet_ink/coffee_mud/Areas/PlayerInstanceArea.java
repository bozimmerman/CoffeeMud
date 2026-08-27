package com.planet_ink.coffee_mud.Areas;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PAData;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
Copyright 2026-2026 Bo Zimmerman

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
public class PlayerInstanceArea extends StdThinInstance implements PlayerOwned
{
	protected String playerOwner = "";

	public PlayerInstanceArea()
	{
		super();
		this.flags |= Area.FLAG_PLAYER_INSTANCE | Area.FLAG_INSTANCE_CHILD;
	}

	@Override
	public String ID()
	{
		return "PlayerInstanceArea";
	}

	@Override
	public String getOwnerName()
	{
		return playerOwner;
	}
	
	@Override
	public void setOwnerName(String owner)
	{
		if(owner != null)
			playerOwner = owner;
	}

	@Override
	public boolean isProperlyOwned()
	{
		return (playerOwner.length() > 0) &&
				CMLib.players().playerExists(playerOwner);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((msg.targetMinor()==CMMsg.TYP_EXPIRE)
		&&(msg.target() instanceof Room)
		&&CMath.bset(flags(), Area.FLAG_INSTANCE_CHILD))
		{
			final Room R = (Room)msg.target();
			if((R.roomID().length() > 0)
			&& (CMLib.flags().isSavable(R))
			&&(this.isRoom(R)))
				CMLib.database().DBUpdateRoom(R);
		}
		else
		if(msg.sourceMinor()==CMMsg.TYP_SHUTDOWN)
		{
			for(final Enumeration<Room> r=super.getProperMap();r.hasMoreElements();)
			{
				final Room R =r.nextElement();
				if((R.roomID().length() > 0) && (CMLib.flags().isSavable(R)))
					CMLib.database().DBUpdateRoom(R);
			}
		}
	}
	@Override
	public Room getRoom(String roomID)
	{
		if(!CMath.bset(flags(), Area.FLAG_INSTANCE_CHILD))
			return super.getRoom(roomID);
		if((roomID == null) || (!isRoom(roomID)))
			return null;
		final Room existingR = super.getRoomBase(roomID);
		if((existingR != null) && (!existingR.amDestroyed()))
			return existingR;
		if(roomID.toUpperCase().startsWith(Name().toUpperCase() + "#"))
			roomID = Name() + roomID.substring(Name().length());
		final String ownerName = getOwnerName();
		if(ownerName.length() == 0)
			return null;
		final List<PAData> pDataList = CMLib.database().DBReadPlayerData(ownerName, "PLAYERINSTANCE", roomID);
		if(pDataList.size()==0)
			return null;
		final String xml = pDataList.get(0).xml();
		if((xml == null) || (xml.length() == 0))
			return null;
		final XMLLibrary xmlLib = CMLib.xml();
		final List<XMLTag> xmlTags = xmlLib.parseAllXML(xml);
		if(xmlTags == null)
			return null;
		final List<XMLTag> roomData = xmlLib.getContentsFromPieces(xmlTags, "AROOM");
		if(roomData == null)
			return null;
		final XMLTag roomIdTag = xmlLib.getPieceFromPieces(roomData, "ROOMID");
		if(roomIdTag != null)
			roomIdTag.setValue(roomID);
		final XMLTag areaTag = xmlLib.getPieceFromPieces(roomData, "RAREA");
		if(areaTag != null)
			areaTag.setValue(Name());
		final String err = CMLib.coffeeMaker().unpackRoomFromXML(this, roomData, true, false, true);
		if((err != null) && (err.length() > 0))
			return null;
		final Room newR = getRoomBase(roomID);
		newR.startItemRejuv();
		newR.setExpirationDate(System.currentTimeMillis() + WorldMap.ROOM_EXPIRATION_MILLIS);
		final MOB mob = CMClass.getFactoryMOB("the wind", 1, newR);
		try
		{
			newR.executeMsg(mob, CMClass.getMsg(mob, newR, CMMsg.MSG_NEWROOM, null));
		}
		finally
		{
			mob.destroy();
		}
		return newR;
	}

	@Override
	protected AreaIStats getAreaIStats()
	{
		return super.getAreaIStats();
	}
}
