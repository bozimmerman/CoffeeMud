package com.planet_ink.coffee_mud.Abilities.Properties;

import java.util.ArrayList;
import java.util.HashMap;
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

public class PropertyConfigStore {

	private final String kvDelimiter;
	private final char assignmentChar;
	private final String listDelimiter;

	private Map<String, Object> parsedValues = new HashMap<>();

	/**
	 * Constructor for PropertyConfigKeyValueParser.
	 *
	 * @param kvDelimiter    The delimiter used to separate key-value pairs. Default is ";".
	 * @param assignmentChar The character used to assign values to keys. Default is '='.
	 * @param listDelimiter  The delimiter used to separate items in a list. Default is ",".
	 */
	public PropertyConfigStore(String kvDelimiter, char assignmentChar, String listDelimiter) {
		this.kvDelimiter = kvDelimiter != null ? kvDelimiter : ";";
		this.assignmentChar = assignmentChar;
		this.listDelimiter = listDelimiter != null ? listDelimiter : ",";
	}

	/**
	 * Parses a configuration string into a map of key-value pairs.
	 * <p>
	 * The configuration string is expected to contain key-value pairs separated by the specified
	 * key-value delimiter. Each key-value pair should use the specified assignment character to
	 * separate the key from its corresponding value. If a key does not have an associated value,
	 * it will be treated as a flag and stored with a Boolean true value. Whitespace around delimiters
	 * is ignored, and quoted values are preserved. Escaped quotes are handled correctly.
	 * </p>
	 *
	 * <p>
	 * <config> ::= <pair> ( ';' <pair> )*
	 * <pair> ::= <key> '=' <value>
	 * <key> ::= <identifier>
	 * <value> ::= <string> | <flag>
	 * <string> ::= '"' <string_content> '"' | "'" <string_content> "'"
	 * <string_content> ::= (<character> | <escaped_quote>)*
	 * <escaped_quote> ::= '\' '"' | '\' "'"
	 * <flag> ::= <identifier>
	 * <identifier> ::= [a-zA-Z0-9_-]+
	 * </p>
	 *
	 * @param config   The configuration string to parse, formatted as "key1=value1;key2=value2;..."
	 * @param errors   A list to collect any error messages encountered during parsing.
	 * @param warnings A list to collect any warning messages encountered during parsing.
	 * @return A map containing parsed key-value pairs, where flags are represented as Boolean true values.
	 */
	public Map<String, Object> parse(String config, List<String> errors, List<String> warnings) {
		parsedValues.clear();

		StringBuilder currentKey = new StringBuilder();
		StringBuilder currentValue = new StringBuilder();
		boolean insideQuotes = false;

		for (int i = 0; i < config.length(); i++) {
			char currentChar = config.charAt(i);

			// Toggle quote state
			if ((currentChar == '"' || currentChar == '\'') && (i == 0 || config.charAt(i - 1) != '\\')) {
				insideQuotes = !insideQuotes;
				continue;
			}

			// Handle assignment
			if (!insideQuotes && currentChar == assignmentChar) {
				i++;
				while (i < config.length() && Character.isWhitespace(config.charAt(i))) i++;

				while (i < config.length() && !(config.charAt(i) == ';' || config.charAt(i) == kvDelimiter.charAt(0))) {
					currentValue.append(config.charAt(i));
					i++;
				}

				String keyTrimmed = currentKey.toString().trim();
				if (!isValidKey(keyTrimmed)) {
					errors.add("Invalid key: " + keyTrimmed + ". Only alphanumeric characters, dashes, and underscores are allowed.");
				}
				else {
					parsedValues.put(keyTrimmed, currentValue.toString().trim());
				}

				currentKey.setLength(0);
				currentValue.setLength(0);
				continue;
			}
			// Handle flags
			else if (!insideQuotes && currentChar == kvDelimiter.charAt(0)) {
				if (currentKey.length() > 0) {
					String keyTrimmed = currentKey.toString().trim();
					if (!isValidKey(keyTrimmed)) {
						errors.add("Invalid key: " + keyTrimmed + ". Only alphanumeric characters, dashes, and underscores are allowed.");
					}
					else {
						parsedValues.put(keyTrimmed, true);
					}
					currentKey.setLength(0);
				}
			}
			else {
				// Building key or value
				if (currentKey.length() == 0 && Character.isWhitespace(currentChar)) {
					continue;
				}

				if (currentValue.length() == 0) {
					currentKey.append(currentChar);
				}
				else {
					currentValue.append(currentChar);
				}
			}
		}

		// Check for unmatched quotes
		if (insideQuotes) {
			errors.add("Unmatched quotes in configuration string.");
		}

		return parsedValues;
	}

	/**
	 * Resolves a delimiter-separated list into a List of strings while preserving outer quotes.
	 *
	 * <p>
	 * <list> ::= <element> ( ',' <element> )*
	 * <element> ::= <quoted_string> | <identifier>
	 * <quoted_string> ::= '"' <string_content> '"' | "'" <string_content> "'"
	 * <string_content> ::= (<character> | <escaped_quote>)*
	 * <escaped_quote> ::= '\' '"' | '\' "'"
	 * <identifier> ::= [a-zA-Z0-9_-]+
	 * </p>
	 *
	 * @param listString The string containing values separated by the specified list delimiter.
	 * @return A List of strings with outer quotes preserved.
	 */
	public List<String> resolveList(String listString) {
		List<String> resolvedList = new ArrayList<>();
		StringBuilder currentElement = new StringBuilder();
		boolean insideQuotes = false;

		for (int i = 0; i < listString.length(); i++) {
			char currentChar = listString.charAt(i);

			// Toggle quote state
			if ((currentChar == '"' || currentChar == '\'') && (i == 0 || listString.charAt(i - 1) != '\\')) {
				insideQuotes = !insideQuotes;
			}

			// Handle list delimiter
			if (!insideQuotes && currentChar == listDelimiter.charAt(0)) {
				resolvedList.add(currentElement.toString());
				currentElement.setLength(0);
			}
			else if (Character.isWhitespace(currentChar) && !insideQuotes) {
				continue; // Ignore whitespace outside quotes
			}
			else {
				currentElement.append(currentChar);
			}
		}

		// Add last element if present
		if (currentElement.length() > 0) {
			resolvedList.add(currentElement.toString());
		}

		return resolvedList;
	}


	/**
	 * Validates whether a given key contains only allowed characters: alphanumeric, dashes, and underscores.
	 *
	 * @param key The key to validate.
	 * @return True if the key is valid; false otherwise.
	 */
	private boolean isValidKey(String key) {
		return key.matches("[a-zA-Z0-9_-]+");
	}

	/**
	 * Checks if a specified key exists in the parsed values.
	 *
	 * @param key The key to check for existence.
	 * @return True if the key exists; false otherwise.
	 */
	public boolean keyExists(String key) {
		return parsedValues.containsKey(key.toUpperCase());
	}

	/**
	 * Retrieves the string value associated with the specified key.
	 *
	 * @param key The key whose associated value is to be returned.
	 * @return The string value associated with the specified key.
	 * @throws IllegalArgumentException If no string value is found for the specified key.
	 */
	public String getStringValue(String key) {
		Object value = parsedValues.get(key.toUpperCase());
		if (value instanceof String) {
			String stringValue = (String) value;
			stringValue = stringValue.trim();
			stringValue = removeQuotes(stringValue);
			return stringValue;
		}
		throw new IllegalArgumentException("No string value found for key: " + key);
	}

	/**
	 * Retrieves a list of strings associated with the specified key.
	 *
	 * @param key The key whose associated list is to be returned.
	 * @return A List of strings associated with the specified key.
	 * @throws IllegalArgumentException If no list value is found for the specified key.
	 */
	public List<String> getListValue(String key) {
		Object value = parsedValues.get(key.toUpperCase());
		if (value instanceof String) {
			List<String> resolvedList = resolveList((String) value);
			List<String> resultList = new ArrayList<>();
			for (String item : resolvedList) {
				resultList.add(removeQuotes(item));
			}
			return resultList;
		}
		throw new IllegalArgumentException("No list value found for key: " + key);
	}

	/**
	 * Validates whether the specified key corresponds to a valid boolean value.
	 *
	 * @param key The key to validate.
	 * @return True if the key exists and its value is a valid boolean; false otherwise.
	 */
	public boolean isBooleanValue(String key) {
		try {
			getBooleanValue(key);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * Retrieves a boolean value associated with the specified key.
	 *
	 * @param key The key whose associated boolean value is to be returned.
	 * @return True if the boolean value associated with the specified key is true; otherwise false.
	 * @throws IllegalArgumentException If no boolean value is found for the specified key or
	 *                                  if an invalid boolean representation is encountered.
	 */
	public boolean getBooleanValue(String key) {
		Object value = parsedValues.get(key.toUpperCase());
		if (value instanceof Boolean) {
			return (Boolean) value;
		}
		else if (value instanceof String) {
			String strValue = removeQuotes(((String) value).trim()).toLowerCase();
			switch (strValue) {
				case "on":
				case "true":
				case "1":
					return true;
				case "off":
				case "false":
				case "0":
					return false;
				default:
					throw new IllegalArgumentException("Invalid boolean representation for key: " + key);
			}
		}
		throw new IllegalArgumentException("No boolean value found for key: " + key);
	}

	/**
	 * Retrieves all keys that have been parsed from the configuration.
	 *
	 * @return A list of all keys present in the parsed values.
	 */
	public List<String> getAllKeys() {
		return new ArrayList<>(parsedValues.keySet());
	}

	/**
	 * Removes surrounding quotes from a given string and handles escaped quotes within it.
	 *
	 * @param str The string potentially containing surrounding quotes.
	 * @return The string without surrounding quotes and with escaped quotes handled.
	 */
	public String removeQuotes(String str) {
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			str = str.substring(1, str.length() - 1);
		}

		return str.replace("\\\"", "\"").replace("\\'", "'");
	}
}