package com.planet_ink.coffee_mud.Abilities.Properties;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

public class PropertyConfigStoreTest {

	@Test
	public void testParseValidKeyValuePairs() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "PREFIX=\"[Arena]\"; SPECTATORS=\"ROOM1, ROOM2\"; ATTACK; LOG=\"0\"; LOG_TO_PLAYERS=\"test1\",'test2',test3";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("PREFIX"));
		assertEquals("[Arena]", parser.getStringValue("PREFIX"));

		assertTrue(parser.keyExists("SPECTATORS"));
		assertEquals("ROOM1, ROOM2", parser.getStringValue("SPECTATORS"));
		assertEquals(1, parser.getListValue("SPECTATORS").size());
		assertEquals("ROOM1, ROOM2", parser.getListValue("SPECTATORS").get(0));

		assertTrue(parser.keyExists("ATTACK"));
		assertEquals(true, parser.getBooleanValue("ATTACK"));

		assertTrue(parser.keyExists("LOG"));
		assertEquals(false, parser.getBooleanValue("LOG"));

		assertTrue(parser.keyExists("LOG_TO_PLAYERS"));
		List<String> logPlayers = parser.getListValue("LOG_TO_PLAYERS");
		assertEquals(3, logPlayers.size());
		assertEquals("test1", logPlayers.get(0));
		assertEquals("test2", logPlayers.get(1));
		assertEquals("test3", logPlayers.get(2));

		assertTrue(errors.isEmpty());
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testParseInvalidKey() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "INVALID#KEY; ATTACK;";
		parser.parse(config, errors, warnings);

		assertFalse(parser.keyExists("INVALID#KEY"));
		assertFalse(errors.isEmpty());
	}

	@Test
	public void testParseQuotedStringsWithEscapedQuotes() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "PREFIX=\"This is a \\\"quoted\\\" prefix\"; SPECTATORS=\"ROOM1, \\\"ROOM2\\\"\";";
		parser.parse(config, errors, warnings);

		assertEquals("This is a \"quoted\" prefix", parser.getStringValue("PREFIX"));
		assertEquals("ROOM1, \"ROOM2\"", parser.getStringValue("SPECTATORS"));

		assertTrue(errors.isEmpty());
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testParseBooleanValuesWithQuotes() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();

		String config = "LONGLOOK=\"true\"; LOG=\"false\";";
		parser.parse(config, errors, new ArrayList<>());

		assertTrue(parser.keyExists("LONGLOOK"));
		assertTrue(parser.getBooleanValue("LONGLOOK"));

		assertTrue(parser.keyExists("LOG"));
		assertFalse(parser.getBooleanValue("LOG"));

		assertTrue(errors.isEmpty());
	}

	@Test
	public void testResolveListWithQuotedElements() {
		PropertyConfigStore parser = new PropertyConfigStore(",", '=', ",");

		String listString = "\"Element 1\", \"Element 2\", 'Element 3'";
		List<String> resolvedList = parser.resolveList(listString);

		assertEquals(3, resolvedList.size());
		assertEquals("\"Element 1\"", resolvedList.get(0));
		assertEquals("\"Element 2\"", resolvedList.get(1));
		assertEquals("'Element 3'", resolvedList.get(2));
	}

	@Test
	public void testResolveListWithEscapedQuotes() {
		PropertyConfigStore parser = new PropertyConfigStore(",", '=', ",");

		String listString = "\"This is a \\\"quoted\\\" element\", Element2";
		List<String> resolvedList = parser.resolveList(listString);

		assertEquals(2, resolvedList.size());
		assertEquals("\"This is a \\\"quoted\\\" element\"", resolvedList.get(0));
		assertEquals("Element2", resolvedList.get(1));
	}

	@Test
	public void testParseEmptyConfiguration() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");

		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "";
		Map<String, Object> parsedValues = parser.parse(config, errors, warnings);

		assertTrue(parsedValues.isEmpty());
		assertTrue(errors.isEmpty());
	}

	@Test
	public void testParseConfigurationWithOnlyDelimiters() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();

		String config = ";;;";
		Map<String, Object> parsedValues = parser.parse(config, errors, new ArrayList<>());

		assertTrue(parsedValues.isEmpty());
		assertTrue(errors.isEmpty());
	}

	@Test
	public void testGetAllKeys() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");

		List<String> errors = new ArrayList<>();

		String config = "KEY1=\"value1\"; KEY2=\"value2\";";
		parser.parse(config, errors, new ArrayList<>());

		List<String> keys = parser.getAllKeys();

		assertEquals(2, keys.size());
		assertTrue(keys.contains("KEY1"));
		assertTrue(keys.contains("KEY2"));
	}
}