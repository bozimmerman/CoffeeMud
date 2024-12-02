package com.planet_ink.coffee_mud.Abilities.Properties;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
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
	public void testParseQuotedStringsWithSeparators() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=\"value;with;semicolons\"; KEY2='value,with,commas'; KEY3=\"value=with=equals\"";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value;with;semicolons", parser.getStringValue("KEY1"));

		assertTrue(parser.keyExists("KEY2"));
		assertEquals("value,with,commas", parser.getStringValue("KEY2"));

		assertTrue(parser.keyExists("KEY3"));
		assertEquals("value=with=equals", parser.getStringValue("KEY3"));

		assertTrue(errors.isEmpty());
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testParseQuotedListWithSeparators() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "LIST=\"item1,with,commas\",\"item2;with;semicolons\",\"item3=with=equals\"";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("LIST"));
		List<String> list = parser.getListValue("LIST");
		assertEquals(3, list.size());
		assertEquals("item1,with,commas", list.get(0));
		assertEquals("item2;with;semicolons", list.get(1));
		assertEquals("item3=with=equals", list.get(2));

		assertTrue(errors.isEmpty());
		assertTrue(warnings.isEmpty());
	}

	/*
	 * whitespace
	 */

	@Test
	public void testWhitespaceHandling() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1 = value1 ; KEY2=  value2  ; KEY3 =value3";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value1", parser.getStringValue("KEY1"));
		assertTrue(parser.keyExists("KEY2"));
		assertEquals("value2", parser.getStringValue("KEY2"));
		assertTrue(parser.keyExists("KEY3"));
		assertEquals("value3", parser.getStringValue("KEY3"));
		assertTrue(errors.isEmpty());
	}

	@Test
	public void testWhitespacePreservationInQuotedStrings() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=\"  value with  spaces  \"; KEY2='  another  spaced  value  '";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("  value with  spaces  ", parser.getStringValue("KEY1"));
		assertTrue(parser.keyExists("KEY2"));
		assertEquals("  another  spaced  value  ", parser.getStringValue("KEY2"));
		assertTrue(errors.isEmpty());
	}

	@Test
	public void testWhitespacePreservationInUnquotedStrings() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=  value with spaces  ; KEY2=another  spaced  value";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value with spaces", parser.getStringValue("KEY1"));
		assertTrue(parser.keyExists("KEY2"));
		assertEquals("another  spaced  value", parser.getStringValue("KEY2"));
		assertTrue(errors.isEmpty());
	}

	@Test
	public void testWhitespacePreservationInLists() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "LIST1=\"item1 with spaces\", item2 ,  \"  item3  \"  ; LIST2=  a  ,b,  c ";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("LIST1"));
		List<String> list1 = parser.getListValue("LIST1");
		assertEquals(3, list1.size());
		assertEquals("item1 with spaces", list1.get(0));
		assertEquals("item2", list1.get(1));
		assertEquals("  item3  ", list1.get(2));

		assertTrue(parser.keyExists("LIST2"));
		List<String> list2 = parser.getListValue("LIST2");
		assertEquals(3, list2.size());
		assertEquals("a", list2.get(0));
		assertEquals("b", list2.get(1));
		assertEquals("c", list2.get(2));

		assertTrue(errors.isEmpty());
	}

	@Test
	public void testListHandlingWithWhitespaceAndQuotes() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "LIST1=\"item 1\", item2 ,  \"  item 3  \", 'item 4', '  item 5  '; " +
				"LIST2=a  b  c, \"d  e  f\", '  g  h  i  '";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("LIST1"));
		List<String> list1 = parser.getListValue("LIST1");
		assertEquals(5, list1.size());
		assertEquals("item 1", list1.get(0));
		assertEquals("item2", list1.get(1));
		assertEquals("  item 3  ", list1.get(2));
		assertEquals("item 4", list1.get(3));
		assertEquals("  item 5  ", list1.get(4));

		assertTrue(parser.keyExists("LIST2"));
		List<String> list2 = parser.getListValue("LIST2");
		assertEquals(3, list2.size());
		assertEquals("a  b  c", list2.get(0));
		assertEquals("d  e  f", list2.get(1));
		assertEquals("  g  h  i  ", list2.get(2));

		assertTrue(errors.isEmpty());
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testListHandlingWithEscapedQuotes() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "LIST=\"item \\\"1\\\"\", \"item \\\"2\\\"\", 'item \\'3\\''";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("LIST"));
		List<String> list = parser.getListValue("LIST");
		assertEquals(3, list.size());
		assertEquals("item \"1\"", list.get(0));
		assertEquals("item \"2\"", list.get(1));
		assertEquals("item '3'", list.get(2));

		assertTrue(errors.isEmpty());
		assertTrue(warnings.isEmpty());
	}

	/*
	 * malformed
	 */

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
	public void testParseMalformedInput() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=value1; KEY2=\"unclosed quote; KEY3=value3";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value1", parser.getStringValue("KEY1"));
		assertFalse(parser.keyExists("KEY2"));
		assertFalse(parser.keyExists("KEY3"));
		assertFalse(errors.isEmpty());
		assertTrue(errors.stream().anyMatch(e -> e.contains("Unclosed quotes")));
	}

	@Test
	public void testUnclosedQuotesInValue() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=\"value1\"; KEY2=\"unclosed quote; KEY3=value3";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value1", parser.getStringValue("KEY1"));
		assertFalse(parser.keyExists("KEY2"));
		assertFalse(parser.keyExists("KEY3"));
		assertFalse(errors.isEmpty());
		assertTrue(errors.stream().anyMatch(e -> e.contains("Unclosed quotes")));
	}

	@Test
	public void testUnclosedQuotesInList() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "LIST1=\"item1\", \"item2, item3; KEY2=value2";
		parser.parse(config, errors, warnings);

		assertFalse(parser.keyExists("LIST1"));
		assertFalse(parser.keyExists("KEY2"));
		assertFalse(errors.isEmpty());
		assertTrue(errors.stream().anyMatch(e -> e.contains("Unclosed quotes")));
	}

	@Test
	public void testMultipleUnclosedQuotes() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=\"value1\"; KEY2='unclosed; KEY3=\"also unclosed; KEY4=value4";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value1", parser.getStringValue("KEY1"));
		assertFalse(parser.keyExists("KEY2"));
		assertFalse(parser.keyExists("KEY3"));
		assertFalse(parser.keyExists("KEY4"));
		assertEquals(1, errors.size());
		assertTrue(errors.stream().allMatch(e -> e.contains("Unclosed quotes")));
	}

	@Test
	public void testParseInvalidAssignment() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=value1; KEY2==value2; KEY3=value3";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value1", parser.getStringValue("KEY1"));
		assertTrue(parser.keyExists("KEY2"));
		assertEquals("=value2", parser.getStringValue("KEY2"));
		assertTrue(parser.keyExists("KEY3"));
		assertEquals("value3", parser.getStringValue("KEY3"));
	}

	@Test
	public void testParseInvalidListFormat() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "LIST1=\"item1,item2\",\"item3\"; LIST2=item1,,item2";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("LIST1"));
		List<String> list1 = parser.getListValue("LIST1");
		assertEquals(2, list1.size());
		assertEquals("item1,item2", list1.get(0));
		assertEquals("item3", list1.get(1));

		assertTrue(parser.keyExists("LIST2"));
		List<String> list2 = parser.getListValue("LIST2");
		assertEquals(2, list2.size());
		assertEquals("item1", list2.get(0));
		assertEquals("item2", list2.get(1));
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
	public void testParseEmptyKey() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

		String config = "KEY1=value1; =value2; KEY3=value3";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value1", parser.getStringValue("KEY1"));
		assertFalse(parser.keyExists(""));
		assertTrue(parser.keyExists("KEY3"));
		assertEquals("value3", parser.getStringValue("KEY3"));
		assertFalse(errors.isEmpty());
		assertTrue(errors.stream().anyMatch(e -> e.contains("Empty key")));
	}

	@Test
	public void testParseConfigurationWithOnlyDelimiters() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();

		String config = ";;;";
		Map<String, Object> parsedValues = parser.parse(config, errors, new ArrayList<>());

		assertTrue(parsedValues.isEmpty());
		assertTrue(errors.stream().anyMatch(e -> e.contains("Empty key")));
	}

	/*
	 * warnings
	 */
	@Test
	public void testDuplicateKeys() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		String config = "KEY1=value1; KEY1=value2; KEY2=value3";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("value2", parser.getStringValue("KEY1"));
		assertTrue(parser.keyExists("KEY2"));
		assertEquals("value3", parser.getStringValue("KEY2"));
		assertTrue(warnings.stream().anyMatch(w -> w.contains("Duplicate key encountered: KEY1")));
	}

	@Test
	public void testLongKeyAndValue() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		String longKey = String.join("", Collections.nCopies(101, "A"));
		String longValue = String.join("", Collections.nCopies(1001, "B"));
		String config = longKey + "=value1; KEY2=" + longValue;
		parser.parse(config, errors, warnings);

		assertTrue(warnings.stream().anyMatch(w -> w.contains("Key length exceeds 100 characters")));
		assertTrue(warnings.stream().anyMatch(w -> w.contains("Value length exceeds 1000 characters for key: KEY2")));
	}

	@Test
	public void testEmptyValue() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		String config = "KEY1=; KEY2=\"\"";
		parser.parse(config, errors, warnings);

		assertTrue(parser.keyExists("KEY1"));
		assertEquals("", parser.getStringValue("KEY1"));
		assertTrue(parser.keyExists("KEY2"));
		assertEquals("", parser.getStringValue("KEY2"));
		assertEquals(2, warnings.size());
		assertTrue(warnings.stream().allMatch(w -> w.contains("Empty value for key")));
	}

	@Test
	public void testUnescapedBackslash() {
		PropertyConfigStore parser = new PropertyConfigStore(";", '=', ",");
		List<String> errors = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		String config = "KEY1=value\\with\\backslash; KEY2=\"value\\\"with\\escaped\\\"quotes\"";
		parser.parse(config, errors, warnings);

		assertTrue(warnings.stream().anyMatch(w -> w.contains("Value contains unescaped backslash for key: KEY1")));
		assertFalse(warnings.stream().anyMatch(w -> w.contains("Value contains unescaped backslash for key: KEY2")));
	}

	/*
	 * utility
	 */

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