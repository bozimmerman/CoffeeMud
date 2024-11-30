package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.Log;

import java.util.HashSet;
import java.util.Set;

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

public class PropertyLogger {
	private boolean loggingEnabled;
	private final String scope;
	private LogLevel playerLogLevel;
	private final Set<String> playerNames;

	public PropertyLogger(String scope) {
		this.scope = scope;
		this.loggingEnabled = false;
		this.playerLogLevel = LogLevel.WARNING;
		this.playerNames = new HashSet<>();
	}

	public void addPlayer(String playerName) {
		playerNames.add(playerName);
	}

	public void removePlayer(String playerName) {
		playerNames.remove(playerName);
	}

	public void clearPlayers() {
		playerNames.clear();
	}

	public boolean isLoggingEnabled() {
		return loggingEnabled;
	}

	public void setLoggingEnabled(boolean enabled) {
		this.loggingEnabled = enabled;
	}

	public LogLevel getPlayerLogLevel() {
		return playerLogLevel;
	}

	public void setPlayerLogLevel(String levelString) {
		if (LogLevel.isValid(levelString)) {
			this.playerLogLevel = LogLevel.valueOf(levelString);
			logInfo("Set player logging level to: " + playerLogLevel);
		}
		else {
			logError("Invalid log level provided: " + levelString);
		}
	}

	public void logDebug(String message) {
		Log.debugOut(scope, message);
		sendToPlayers(message, LogLevel.DEBUG);
	}

	public void logInfo(String message) {
		Log.infoOut(scope, message);
		sendToPlayers(message, LogLevel.INFO);
	}

	public void logWarn(String message) {
		Log.warnOut(scope, message);
		sendToPlayers(message, LogLevel.WARNING);
	}

	public void logError(String message) {
		Log.errOut(scope, message);
		sendToPlayers(message, LogLevel.ERROR);
	}

	private void sendToPlayers(String message, LogLevel level) {
		if (loggingEnabled && level.ordinal() >= playerLogLevel.ordinal()) {
			for (String playerName : playerNames) {
				MOB player = CMLib.players().getPlayer(playerName);
				if (player != null && player.session() != null) {
					player.tell("[" + scope + "] " + level.toString() + ": " + message);
				}
			}
		}
	}
}