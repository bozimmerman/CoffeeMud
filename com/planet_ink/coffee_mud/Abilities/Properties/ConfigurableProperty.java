package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.core.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public abstract class ConfigurableProperty extends BaseLoggableProperty {
	private Map<String, Object> parsedValues;
	protected List<String> errors;
	protected PropertyConfigStore configStore = new PropertyConfigStore(";", '=', ",");

	protected abstract void handleParsedConfiguration();

	protected void logConfigurationErrors() {
		for (String error : errors) {
			logger.logError("Configuration error: " + error);
		}
	}

	protected void parseConfiguration(String config) {
		errors = new ArrayList<>();
		parsedValues = configStore.parse(config, errors, new ArrayList<>());
		if (!errors.isEmpty()) {
			logConfigurationErrors();
		}
	}

	@Override
	public void setMiscText(String newText) {
		super.setMiscText(newText);
		try {
			parseConfiguration(newText);
			handleParsedConfiguration();
		} catch (IllegalArgumentException e) {
			logger.logError("Error in configuration handling: " + e.getMessage());
			Log.sysOut("Error in configuration handling: " + e.getMessage());
		}
	}

	public PropertyConfigStore getConfigStore() {
		return configStore;
	}
}