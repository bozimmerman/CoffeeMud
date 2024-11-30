package com.planet_ink.coffee_mud.Abilities.Properties;

import java.util.ArrayList;
import java.util.List;

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

public abstract class LoggableProperty extends ConfigurableProperty {

	@Override
	protected void handleParsedConfiguration() {
		parseLogSettings();
	}

	private void parseLogSettings() {
		if (getConfigStore().keyExists("LOG")) {
			boolean loggingEnabled = getConfigStore().getBooleanValue("LOG");
			logger.setLoggingEnabled(loggingEnabled);
			logger.logInfo("Logging is now " + (loggingEnabled ? "enabled" : "disabled"));
		}

		if (getConfigStore().keyExists("LOG_TO_PLAYERS")) {
			List<String> playerNames = getConfigStore().getListValue("LOG_TO_PLAYERS");
			List<String> playerNames2 = new ArrayList<String>();
			for (String playerName : playerNames) {
				playerName = playerName.trim();
				playerName = configStore.removeQuotes(playerName);
				playerNames2.add(playerName);
				logger.addPlayer(playerName);
			}
			logger.logInfo("Configured logging to players: " + String.join(", ", playerNames2));
		}

		if (getConfigStore().keyExists("LOG_TO_PLAYER_LEVEL")) {
			String levelString = getConfigStore().getStringValue("LOG_TO_PLAYER_LEVEL");
			try {
				levelString = levelString.trim();
				levelString = configStore.removeQuotes(levelString);
				levelString = levelString.toUpperCase();
				logger.setPlayerLogLevel(levelString);
				logger.logInfo("Set player logging level to: " + levelString);
			} catch (IllegalArgumentException e) {
				logger.logError("Invalid LOG_TO_PLAYER_LEVEL: " + levelString + ". Using default level.");
			}
		}
	}
}
