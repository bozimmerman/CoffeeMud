package com.planet_ink.coffee_mud.Protocols.gmcp;

import com.planet_ink.coffee_mud.Abilities.Misc.ExtraData;
import com.planet_ink.coffee_mud.Areas.interfaces.GridZones;
import com.planet_ink.coffee_mud.Common.interfaces.Climate;
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

	public String build() {
		if (mob == null || room == null || mob.isMonster() || !CMLib.flags().canSee(mob)) {
			return "room.info {}";
		}

		addBasicInfo();
		addExtraData();
		addExitsNum();
		addExitsId();
		addCoordinates();
		return "room.info " + json;
	}

	private void addBasicInfo() {
		final String roomID = CMLib.map().getExtendedRoomID(room);
		final String domType = getDomainType();
		final String moveType = getMoveType();

		json.put("num", CMath.abs(roomID.hashCode()));
		json.put("id", roomID);
		json.put("name", MiniJSON.toJSONString(room.displayText(mob)));
		json.put("zone", MiniJSON.toJSONString(room.getArea().name()));
		json.put("desc", MiniJSON.toJSONString(room.description(mob)));
		json.put("terrain", domType.toLowerCase());
		json.put("details", "");
		json.put("move", moveType);
	}

	private String getDomainType() {
		if ((room.domainType() & Room.INDOORS) == 0)
			return Room.DOMAIN_OUTDOOR_DESCS[room.domainType()];
		else
			return Room.DOMAIN_INDOORS_DESCS[CMath.unsetb(room.domainType(), Room.INDOORS)];
	}

	private String getMoveType() {
		String type = "normal";
		if (CMLib.flags().isCrawlable(room))
			type = "crawl";
		else if (CMLib.flags().isWateryRoom(room))
			type = "swim";
		else if (CMLib.flags().isAiryRoom(room))
			type = "fly";
		return type;
	}

	private void addExitsNum() {
		JSONObject exits = new JSONObject();

		for (int d : Directions.DISPLAY_CODES()) {
			if (room.getExitInDir(d) != null) {
				Room room2 = room.getRoomInDir(d);
				if (room2 != null) {
					String room2ID = CMLib.map().getExtendedRoomID(room2);
					if (!room2ID.isEmpty())
						exits.put(CMLib.directions().getDirectionChar(d), CMath.abs(room2ID.hashCode()));
				}
			}
		}
		json.put("exits", exits);
	}

	private void addExitsId() {
		JSONObject exits = new JSONObject();

		for (int d : Directions.DISPLAY_CODES()) {
			if (room.getExitInDir(d) != null) {
				Room room2 = room.getRoomInDir(d);
				if (room2 != null) {
					String room2ID = CMLib.map().getExtendedRoomID(room2);
					if (!room2ID.isEmpty())
						exits.put(CMLib.directions().getDirectionChar(d), room2ID);
				}
			}
		}
		json.put("exitsid", exits);
	}

	private void addExtraData() {
		ExtraData extraData = (ExtraData) room.fetchEffect("ExtraData");
		if (extraData != null) {
			JSONObject extraDataJson = new JSONObject();
			boolean hasExtraData = false;
			for (String key : extraData.getStatCodes()) {
				if (!key.equals("CLASS") && !key.equals("TEXT")) {
					String value = extraData.getStat(key);
					if (value != null && !value.isEmpty()) {
						extraDataJson.put(key.toLowerCase(), MiniJSON.toJSONString(value));
						hasExtraData = true;
					}
				}
			}
			if (hasExtraData) {
				json.put("extraData", extraDataJson);
			}
		}
	}

	private void addCoordinates() {
		JSONObject coord = new MiniJSON.JSONObject();

		if (room.getArea().getClimateObj().weatherType(room) == Climate.WEATHER_FOG) {
			json.put("coord", coord);
			return;
		}

		if (room.getGridParent() != null) {
			String parentID = room.getGridParent().roomID();
			GridZones.XYVector vec = room.getGridParent().getRoomXY(room);
			coord.put("id", Math.abs(parentID.hashCode()));
			coord.put("x", (vec == null) ? -1 : vec.x);
			coord.put("y", (vec == null) ? -1 : vec.y);
		} else if (room.getArea() instanceof GridZones) {
			GridZones.XYVector vec = ((GridZones)room.getArea()).getRoomXY(room);
			coord.put("id", 0);
			coord.put("x", (vec == null) ? -1 : vec.x);
			coord.put("y", (vec == null) ? -1 : vec.y);
		} else {
			coord.put("id", 0);
			coord.put("x", -1);
			coord.put("y", -1);
		}

		coord.put("cont", 0);  // continent?
		json.put("coord", coord);
	}
}
