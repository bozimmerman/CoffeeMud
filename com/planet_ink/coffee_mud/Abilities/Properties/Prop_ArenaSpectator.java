package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;

import java.util.Enumeration;

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


/**
 * Prop_ArenaSpectator: Allows spectators to view actions in the pit.
 *
 * <p>
 * Configuration:
 * <ul>
 *     <li><b>PIT</b>=<code>&lt;room ID&gt;</code>: Specifies the pit room to view.</li>
 *     <li><b>VIEW_MESSAGE</b>=<code>&lt;message&gt;</code>: Custom message displayed when looking into the pit.</li>
 *     <li><b>LONGLOOK</b>: Show a detailed view of the pit when examining.</li>
 *     <li><b>DETAILED_VIEW_MESSAGE</b>=<code>&lt;message&gt;</code>: Custom message displayed during detailed examination of the pit.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Configuration Example:
 * <pre>
 * PIT="SPECTATOR ROOM #1"; VIEW_MESSAGE="In the arena, you see:"; LONGLOOK; DETAILED_VIEW_MESSAGE="Detailed view of the arena:"
 * </pre>
 * </p>
 */
public class Prop_ArenaSpectator extends LoggableProperty {
	private static final ThreadLocal<Integer> recursionDepth = ThreadLocal.withInitial(() -> 0);
	private static final int MAX_RECURSION_DEPTH = 10;

	protected Room pitRoom = null;
	protected String pitRoomID = "";
	protected String viewMessage = "In the arena, you see:";
	protected boolean longlook = false;
	protected String detailedViewMessage = "Detailed view of the arena:";

	@Override
	public String ID() {
		return "Prop_ArenaSpectator";
	}

	@Override
	public String name() {
		return "Arena Spectator View";
	}

	@Override
	protected int canAffectCode() {
		return Ability.CAN_ROOMS;
	}

	@Override
	public String accountForYourself() {
		return "Allows spectators to view actions in the pit.";
	}

	@Override
	protected void handleParsedConfiguration() {
		super.handleParsedConfiguration();

		if (configStore.keyExists("PIT")) {
			pitRoomID = configStore.getStringValue("PIT");
			recreatePitRoom();
		}
		else {
			logger.logError("PIT is not set by configuration.");
		}

		if (configStore.keyExists("LONGLOOK")) {
			longlook = configStore.getBooleanValue("LONGLOOK");
			if (longlook) {
				logger.logInfo("Longlook mode enabled.");
			}
		}

		if (configStore.keyExists("VIEW_MESSAGE")) {
			viewMessage = configStore.getStringValue("VIEW_MESSAGE");
			logger.logInfo("Set VIEW_MESSAGE: " + viewMessage);
		}

		if (configStore.keyExists("DETAILED_VIEW_MESSAGE")) {
			detailedViewMessage = configStore.getStringValue("DETAILED_VIEW_MESSAGE");
			logger.logInfo("Set DETAILED_VIEW_MESSAGE: " + detailedViewMessage);
		}

		if (pitRoom == null) {
			logger.logWarn("No valid pit room specified.");
		}
	}

	protected Room getPitRoom() {
		if (pitRoom == null || pitRoom.amDestroyed()) {
			recreatePitRoom();
		}
		return pitRoom;
	}

	private void recreatePitRoom() {
		pitRoom = CMLib.map().getRoom(pitRoomID);
		if (pitRoom != null) {
			logger.logInfo("Recreated PIT room: " + pitRoomID);
		}
		else {
			logger.logError("Failed to recreate PIT room with ID: " + pitRoomID);
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

			Room currentPitRoom = getPitRoom();
			if (currentPitRoom != null && msg.source().location() == affected) {
				if (msg.targetMinor() == CMMsg.TYP_LOOK || msg.targetMinor() == CMMsg.TYP_EXAMINE) {
					addPitViewTrailer(msg);
					return true;
				}
			}

			return true;
		} finally {
			recursionDepth.set(recursionDepth.get() - 1);
		}
	}

	private void addPitViewTrailer(final CMMsg msg) {
		msg.addTrailerRunnable(new Runnable() {
			final Room R = getPitRoom();
			final CMMsg mmsg = msg;

			@Override
			public void run() {
				if (CMLib.flags().canBeSeenBy(R, mmsg.source()) && mmsg.source().session() != null) {

					if (longlook && mmsg.targetMinor() == CMMsg.TYP_EXAMINE) {
						mmsg.source().tell("\n" + detailedViewMessage);
						final CMMsg msg2 = CMClass.getMsg(mmsg.source(), R, mmsg.tool(),
								mmsg.sourceCode(), null, mmsg.targetCode(), null, mmsg.othersCode(), null);
						if (R.okMessage(mmsg.source(), msg2)) R.send(mmsg.source(), msg2);
					}
					else {
						mmsg.source().tell("\n" + viewMessage);
						for (Enumeration<MOB> e = R.inhabitants(); e.hasMoreElements(); ) {
							MOB M = e.nextElement();
							if (M != null && CMLib.flags().canBeSeenBy(M, mmsg.source())) {
								mmsg.source().tell("  " + M.name(mmsg.source()));
							}
						}
					}
				}
			}
		});
	}
}