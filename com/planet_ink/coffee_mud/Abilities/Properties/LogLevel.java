package com.planet_ink.coffee_mud.Abilities.Properties;

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

public enum LogLevel {
	DEBUG,
	INFO,
	WARNING,
	ERROR;

	// Method to parse a string and return the corresponding LogLevel
	public static LogLevel parse(String levelString) {
		for (LogLevel level : values()) {
			if (level.name().equalsIgnoreCase(levelString)) {
				return level;
			}
		}
		throw new IllegalArgumentException("Invalid log level: " + levelString);
	}
	public static boolean isValid(String level) {
		for (LogLevel logLevel : values()) {
			if (logLevel.name().equalsIgnoreCase(level)) {
				return true;
			}
		}
		return false;
	}
}