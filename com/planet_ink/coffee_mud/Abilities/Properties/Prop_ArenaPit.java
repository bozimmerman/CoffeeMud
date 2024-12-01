package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/*
   Copyrigh3t 2024 github.com/toasted323

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

/**
 * Prop_ArenaPit: Broadcasts pit actions to spectator rooms.
 *
 * <p>
 * Configuration:
 * <ul>
 *     <li><b>PREFIX</b>=<code>&lt;message prefix&gt;</code>: Adds a prefix to all broadcast messages.</li>
 *     <li><b>SPECTATORS</b>=<code>&lt;room1&gt;, &lt;room2&gt;, ...</code>: Comma-separated list of spectator room IDs.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Configuration Example:
 * <pre>
 * PREFIX="[Arena] "; SPECTATORS="ROOM#1", ROOM2;
 * </pre>
 * </p>
 */
public class Prop_ArenaPit extends LoggableProperty {
	private static final ThreadLocal<Integer> recursionDepth = ThreadLocal.withInitial(() -> 0);
	private static final int MAX_RECURSION_DEPTH = 10;

	private List<String> spectatorRoomIDs = new ArrayList<>();
	protected List<Room> spectatorRooms = new ArrayList<>();
	protected String prefix = "";

	@Override
	public String ID() {
		return "Prop_ArenaPit";
	}

	@Override
	public String name() {
		return "Arena Pit Broadcast";
	}

	@Override
	protected int canAffectCode() {
		return Ability.CAN_ROOMS;
	}

	@Override
	public String accountForYourself() {
		return "Broadcasts pit actions to spectator rooms.";
	}

	@Override
	protected void handleParsedConfiguration() {
		super.handleParsedConfiguration();

		if (configStore.keyExists("PREFIX")) {
			prefix = configStore.getStringValue("PREFIX");
			if (!prefix.isEmpty()) {
				logger.logInfo("Set PREFIX: '" + prefix + "'");
			}
		}

		spectatorRoomIDs.clear();
		if (configStore.keyExists("SPECTATORS")) {
			spectatorRoomIDs = configStore.getListValue("SPECTATORS");
			recreateSpectatorRooms();
		}
		else {
			logger.logError("SPECTATORS is not set by configuration.");
		}
	}

	protected List<Room> getSpectatorRooms() {
		for (Room spectatorRoom : spectatorRooms) {
			if (spectatorRoom == null || spectatorRoom.amDestroyed()) {
				logger.logError("Invalid spectator room, recreate all spectator rooms.");
				recreateSpectatorRooms();
				break;
			}
		}
		return spectatorRooms;
	}

	private void recreateSpectatorRooms() {
		spectatorRooms.clear();

		for (String roomID : spectatorRoomIDs) {
			Room room = CMLib.map().getRoom(roomID);
			if (room != null) {
				logger.logInfo("Recreated spectator room: " + roomID);
				spectatorRooms.add(room);
			}
			else {
				logger.logWarn("Failed to recreated speactator room: " + roomID);
			}
		}

		if (spectatorRooms.isEmpty()) {
			logger.logWarn("No valid spectator rooms found.");
		}
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg) {
		if (recursionDepth.get() >= MAX_RECURSION_DEPTH) {
			logger.logError("Maximum recursion depth reached.");
			Log.sysOut("Maximum recursion depth reached.");
			return false;
		}
		recursionDepth.set(recursionDepth.get() + 1);

		try {
			if (!super.okMessage(myHost, msg)) {
				logger.logWarn("Superclass okMessage denied the message: " + msg.toString());
				return false;
			}

			return true;
		} finally {
			recursionDepth.set(recursionDepth.get() - 1);
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg) {
		super.executeMsg(myHost, msg);

		if ((affected instanceof Room) && (msg.othersCode() != CMMsg.NO_EFFECT) && (msg.othersMessage() != null) && !msg.othersMessage().isEmpty()) {
			final Room pitRoom = (Room) affected;
			broadcastToSpectators(pitRoom, msg);
		}
		else {
			logger.logInfo("Message not processed for broadcasting; either affected is not a room or there is no relevant message for others.");
		}
	}

	private void broadcastToSpectators(Room pitRoom, CMMsg msg) {
		List<Room> currentSpectatorRooms = getSpectatorRooms();
		for (Room spectatorRoom : currentSpectatorRooms) {
			if (spectatorRoom == null || spectatorRoom.amDestroyed()) {
				logger.logError("Invalid spectator room.");
				continue;
			}

			CMMsg msg2 = CMClass.getMsg(
					msg.source(),
					msg.target(),
					msg.tool(),
					CMMsg.NO_EFFECT, null,
					CMMsg.NO_EFFECT, null,
					CMMsg.MSG_OK_VISUAL,
					(prefix != null && !prefix.isEmpty()) ? (prefix + msg.othersMessage()) : msg.othersMessage()
			);

			for (Enumeration<MOB> e = spectatorRoom.inhabitants(); e.hasMoreElements(); ) {
				MOB M = e.nextElement();
				if (M != null && M.session() != null && CMLib.flags().canBeSeenBy(msg.source(), M)) {
					M.executeMsg(M, msg2);
					logger.logInfo(M.name() + " received broadcast: " + msg2.othersMessage());
				}
			}

			logger.logInfo("Broadcasted message to spectators in room: " + spectatorRoom.roomID());
		}
	}
}