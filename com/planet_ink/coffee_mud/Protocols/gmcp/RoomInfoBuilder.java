package com.planet_ink.coffee_mud.Protocols.gmcp;

import com.planet_ink.coffee_mud.Common.interfaces.Climate;
import com.planet_ink.coffee_mud.Exits.interfaces.Exit;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.core.MiniJSON;
import com.planet_ink.coffee_mud.core.MiniJSON.JSONObject;

/*
   Copyright 2024 github.com/toasted323

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

public class RoomInfoBuilder {
	private final Room room;
	private final MOB mob;
	private final JSONObject json;

	public RoomInfoBuilder(Room room, MOB mob) {
		this.room = room;
		this.mob = mob;
		this.json = new JSONObject();
	}

	private void addBasicInfo() {
		final String roomID = CMLib.map().getExtendedRoomID(room);
		final String domType = getDomainType();

		json.put("num", CMath.abs(roomID.hashCode()));
		json.put("id", roomID);
		json.put("name", MiniJSON.toJSONString(room.displayText(mob)));
		json.put("zone", MiniJSON.toJSONString(room.getArea().name()));
		json.put("desc", MiniJSON.toJSONString(room.description(mob)));
		json.put("terrain", domType.toLowerCase());
		json.put("details", "");
	}

	public String build() {
		if (mob == null || room == null || mob.isMonster() || !CMLib.flags().canSee(mob)) {
			return "room.info {}";
		}

		addBasicInfo();
		addExits();
		addCoordinates();
		return "room.info " + json.toString();
	}

	private String getDomainType() {
		if ((room.domainType() & Room.INDOORS) == 0)
			return Room.DOMAIN_OUTDOOR_DESCS[room.domainType()];
		else
			return Room.DOMAIN_INDOORS_DESCS[CMath.unsetb(room.domainType(), Room.INDOORS)];
	}

	private void addExits() {
		JSONObject exits = new JSONObject();
		if (room.getArea().getClimateObj().weatherType(room) == Climate.WEATHER_FOG) {
			json.put("exits", exits);
			return;
		}

		for (int d : Directions.DISPLAY_CODES()) {
			Room room2 = room.getRoomInDir(d);
			if (room2 != null) {
				exits.put(CMLib.directions().getDirectionChar(d), CMath.abs(CMLib.map().getExtendedRoomID(room2).hashCode()));
			}
		}
		json.put("exits", exits);
	}

	private void addCoordinates() {
		JSONObject coord = new MiniJSON.JSONObject();
		if (room.getArea().getClimateObj().weatherType(room) == Climate.WEATHER_FOG) {
			json.put("coord", coord);
			return;
		}

		coord.put("id", 0);
		coord.put("x", -1);
		coord.put("y", -1);
		coord.put("cont", 0);
		json.put("coord", coord);
	}
}
