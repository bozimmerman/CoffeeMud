package com.planet_ink.coffee_mud.Tests;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMath.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.WebMacros.interfaces.*;
import com.planet_ink.coffee_web.http.HTTPMethod;
import com.planet_ink.coffee_web.http.MultiPartData;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.text.*;
import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;

/*
Copyright 2026-2026 Bo Zimmerman

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
public class Validmisctext extends StdTest
{
	private static class PromptCaptureSession extends com.planet_ink.coffee_mud.Common.FakeSession
	{
		private final StringBuilder output = new StringBuilder();

		@Override
		public boolean isFake()
		{
			return false;
		}

		@Override
		public void onlyPrint(final String msg, final boolean noCache)
		{
			output.append(msg);
		}

		public String output()
		{
			return output.toString();
		}
	}

	private String capturePrompt(final AbilityParmEditor cachedEditor, final String promptOverride)
		throws java.io.IOException
	{
		final AbilityParmEditor editor;
		if(promptOverride == null)
			editor = cachedEditor;
		else
		{
			editor = cachedEditor.copyOf();
			editor.setPrompt(promptOverride);
		}
		final MOB promptMob = CMClass.getFactoryMOB();
		final PromptCaptureSession session = new PromptCaptureSession();
		session.setMob(promptMob);
		promptMob.setSession(session);
		try
		{
			editor.commandLinePrompt(promptMob,"",new int[]{0},0);
			return session.output();
		}
		finally
		{
			promptMob.setSession(null);
		}
	}

	@Override
	public String ID()
	{
		return "Validmisctext";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] { "all" };
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		// Test 1: Simple literal matching
		if(!CMLib.ableParms().isValidMiscText("abc", "abc"))
			return ("Error#1: literal 'abc' should match 'abc'");
		// Test 2: Literal non-match
		if(CMLib.ableParms().isValidMiscText("abc", "abd"))
			return ("Error#2: literal 'abc' should not match 'abd'");
		// Test 3: Placeholder matches a token
		if(!CMLib.ableParms().isValidMiscText("[NAME]", "Bob "))
			return ("Error#3: placeholder [NAME] should match 'Bob '");
		// Test 4: Two placeholders
		if(!CMLib.ableParms().isValidMiscText("[A] [B]", "foo bar "))
			return ("Error#4: '[A] [B]' should match 'foo bar '");
		// Test 5: Optional group - present
		if(!CMLib.ableParms().isValidMiscText("(abc)", "abc"))
			return ("Error#5: optional '(abc)' should match 'abc'");
		// Test 6: Optional group - absent
		if(!CMLib.ableParms().isValidMiscText("(abc)", ""))
			return ("Error#6: optional '(abc)' should match empty string");
		// Test 7: Optional group with required suffix - absent group
		if(!CMLib.ableParms().isValidMiscText("(abc)def", "def"))
			return ("Error#7: '(abc)def' should match 'def'");
		// Test 8: Optional group with required suffix - both present
		if(!CMLib.ableParms().isValidMiscText("(abc)def", "abcdef"))
			return ("Error#8: '(abc)def' should match 'abcdef'");
		// Test 9: Alternation - first choice
		if(!CMLib.ableParms().isValidMiscText("(a/b)", "a"))
			return ("Error#9: alternation '(a/b)' should match 'a'");
		// Test 10: Alternation - second choice
		if(!CMLib.ableParms().isValidMiscText("(a/b)", "b"))
			return ("Error#10: alternation '(a/b)' should match 'b'");
		// Test 11: Alternation - no match
		if(CMLib.ableParms().isValidMiscText("(a/b)", "c"))
			return ("Error#11: alternation '(a/b)' should not match 'c'");
		// Test 12: Repetition with literal
		if(!CMLib.ableParms().isValidMiscText("a...", "aaa"))
			return ("Error#12: repetition 'a...' should match 'aaa'");
		// Test 13: Repetition of a literal delimiter after a placeholder
		if(!CMLib.ableParms().isValidMiscText("[NAME];...", "foo;;;"))
			return ("Error#13: '[NAME];...' should match 'foo;;;'");
		// Test 14: Escape - literal bracket
		if(!CMLib.ableParms().isValidMiscText("\\[test\\]", "[test]"))
			return ("Error#14: escaped '\\[test\\]' should match '[test]'");
		// Test 15: Complex format - KEY=[VALUE]
		if(!CMLib.ableParms().isValidMiscText("KEY=[VALUE]", "KEY=something "))
			return ("Error#15: 'KEY=[VALUE]' should match 'KEY=something '");
		// Test 16: Complex format with optional - single value
		if(!CMLib.ableParms().isValidMiscText("[NAME](;[NAME])...", "foo"))
			return ("Error#16: '[NAME](;[NAME])...' should match 'foo'");
		// Test 17: Complex format with optional - multiple values
		if(!CMLib.ableParms().isValidMiscText("[NAME](;[NAME])...", "foo;bar;baz"))
			return ("Error#17: '[NAME](;[NAME])...' should match 'foo;bar;baz'");
		// Test 18: Multiple optional groups
		if(!CMLib.ableParms().isValidMiscText("(A)(B)(C)", "AC"))
			return ("Error#18: '(A)(B)(C)' should match 'AC'");
		// Test 19: Empty proposition against empty format
		if(!CMLib.ableParms().isValidMiscText("", ""))
			return ("Error#19: empty format should match empty proposition");
		// Test 20: Non-empty format against empty proposition should fail
		if(CMLib.ableParms().isValidMiscText("abc", ""))
			return ("Error#20: 'abc' should not match empty proposition");
		// Test 21: Repetition requires at least one
		if(CMLib.ableParms().isValidMiscText("[file]...", ""))
			return ("Error#21: '[file]...' should not match empty (requires at least one)");
		// Test 22: Typed placeholder - editor accepts valid value
		if(!CMLib.ableParms().isValidMiscText("[FREQUENCY]", "15"))
			return ("Error#22: '[FREQUENCY]' should match '15' (typed placeholder)");
		// Test 23: Typed placeholder - editor rejects invalid value
		if(CMLib.ableParms().isValidMiscText("[FREQUENCY]", "notanumber"))
			return ("Error#23: '[FREQUENCY]' should not match 'notanumber' (typed placeholder)");
		// Test 24: Greedy placeholder at end of format captures the rest
		if(!CMLib.ableParms().isValidMiscText("[NAME...]", "foo bar"))
			return ("Error#24: '[NAME...]' should match 'foo bar'");
		// Test 25: Greedy placeholder at end of format requires input
		if(CMLib.ableParms().isValidMiscText("[NAME...]", ""))
			return ("Error#25: '[NAME...]' should not match an empty input");
		// Test 26: Greedy placeholder with an immediately following literal
		if(!CMLib.ableParms().isValidMiscText("[NAME...]tail", "foo bartail"))
			return ("Error#26: '[NAME...]tail' should match 'foo bartail'");
		// Test 27: Greedy placeholder requires at least one character
		if(CMLib.ableParms().isValidMiscText("[NAME...]tail", "tail"))
			return ("Error#27: '[NAME...]tail' should not match an empty value");
		// Test 28: A space after the ] terminates the greedy part
		if(CMLib.ableParms().isValidMiscText("[NAME...] tail", "foo bar tail"))
			return ("Error#28: '[NAME...] tail' should not be greedy across the space");
		// Test 29: A space after the ] makes it behave like [NAME]
		if(!CMLib.ableParms().isValidMiscText("[NAME...] tail", "foo tail"))
			return ("Error#29: '[NAME...] tail' should behave like '[NAME] tail'");
		// Test 30: Escaped literal immediately following the greedy placeholder
		if(!CMLib.ableParms().isValidMiscText("[NAME...]\\[x\\]", "foo [x]"))
			return ("Error#30: '[NAME...]\\[x\\]' should match 'foo [x]'");
		// Test 31: Greedy values may contain backslashes
		if(!CMLib.ableParms().isValidMiscText("[NAME...]b", "a\\b"))
			return ("Error#31: '[NAME...]b' should match 'a\\b'");
		// Test 32: Repeating a greedy placeholder is rejected (not a hang)
		if(CMLib.ableParms().isValidMiscText("[NAME...]...", "foo"))
			return ("Error#32: '[NAME...]...' should be rejected");
		// Test 33: A greedy placeholder at the end of a group must end the format
		if(CMLib.ableParms().isValidMiscText("([NAME...]) tail", "foo tail"))
			return ("Error#33: '([NAME...]) tail' should be rejected");
		// Test 34: ParmType placeholder validates a number
		if(CMLib.ableParms().isValidMiscText("[NUMBER]", "notanumber"))
			return ("Error#34: '[NUMBER]' should reject 'notanumber'");
		// Test 35: ParmType placeholder accepts an integer
		if(!CMLib.ableParms().isValidMiscText("[NUMBER]", "15"))
			return ("Error#35: '[NUMBER]' should accept '15'");
		// Test 36: A greedy ONEWORD placeholder rejects multiple words
		if(CMLib.ableParms().isValidMiscText("[ONEWORD...]", "hello world"))
			return ("Error#36: '[ONEWORD...]' should reject 'hello world'");
		// Test 37: A greedy STRING placeholder accepts spaces
		if(!CMLib.ableParms().isValidMiscText("[STRING...]", "hello world"))
			return ("Error#37: '[STRING...]' should accept 'hello world'");
		// Test 38: A NUMBER_PAIR ParmType accepts a comma-separated pair
		if(!CMLib.ableParms().isValidMiscText("[NUMBER_PAIR...]", "5,7"))
			return ("Error#38: '[NUMBER_PAIR...]' should accept '5,7'");
		// Test 39: A NUMBER_PAIR ParmType rejects a malformed pair
		if(CMLib.ableParms().isValidMiscText("[NUMBER_PAIR...]", "5,x"))
			return ("Error#39: '[NUMBER_PAIR...]' should reject '5,x'");
		// Test 40: Bare CHOICES ParmType accepts a configured choice
		if(!CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE,ICE)]", "FIRE"))
			return ("Error#40: configured CHOICES should accept FIRE");
		// Test 41: Bare CHOICES ParmType rejects a value outside the configured choices
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE,ICE)]", "WATER"))
			return ("Error#41: configured CHOICES should reject WATER");
		// Test 42: Bare CHOICES ParmType rejects a missing choices argument
		if(CMLib.ableParms().isValidMiscText("[CHOICES]", "FIRE"))
			return ("Error#42: CHOICES without configured choices should be invalid");
		// Test 43: NUMBER_PAIR preserves the base editor's single-integer acceptance
		if(!CMLib.ableParms().isValidMiscText("[NUMBER_PAIR]", "5"))
			return ("Error#43: '[NUMBER_PAIR]' should accept a single integer");
		// Test 44: A format-local prompt override is returned without changing the editor
		final AbilityParmEditor editor = CMLib.ableParms().getEditors().get("FREQUENCY");
		final String originalPrompt = editor.prompt();
		final String originalHeader = editor.colHeader();
		final AbilityParmEditor promptCopy = editor.copyOf();
		promptCopy.setPrompt("Recharge time");
		if(!promptCopy.prompt().equals("Recharge time"))
			return ("Error#44: copied editor did not return the overridden prompt");
		final AbilityParmEditor secondPromptCopy = editor.copyOf();
		secondPromptCopy.setPrompt("Cooldown");
		if(!secondPromptCopy.prompt().equals("Cooldown"))
			return ("Error#44: second copied editor did not retain its own prompt");
		if(!promptCopy.prompt().equals("Recharge time"))
			return ("Error#44: prompt override leaked between editor copies");
		if(!editor.prompt().equals(originalPrompt))
			return ("Error#44: copied prompt override changed the registered editor");
		// Test 45: A format-local column-header override is returned independently
		final AbilityParmEditor headerCopy = editor.copyOf();
		headerCopy.setColHeader("Recharge");
		if(!headerCopy.colHeader().equals("Recharge"))
			return ("Error#45: copied editor did not return the overridden column header");
		if(!editor.colHeader().equals(originalHeader))
			return ("Error#45: copied column-header override changed the registered editor");
		// Test 46: Local label resolution does not mutate the registered editor
		if((!editor.prompt().equals(originalPrompt))||(!editor.colHeader().equals(originalHeader)))
			return ("Error#46: local label lookup changed the registered editor defaults");
		// Test 47: Parsed placeholder arguments retain both independent labels
		try
		{
			final Method parseMethod = CMLib.ableParms().getClass().getDeclaredMethod("parseMiscTextFormat", String.class);
			parseMethod.setAccessible(true);
			final Object token = parseMethod.invoke(null, "[FREQUENCY(prompt=Recharge time/colh=Recharge)]");
			final Field argsField = token.getClass().getDeclaredField("parameterArgs");
			argsField.setAccessible(true);
			final Map<?,?> args = (Map<?,?>)argsField.get(token);
			if((!"Recharge time".equals(args.get("PROMPT")))||(!"Recharge".equals(args.get("COLH"))))
				return ("Error#47: format-local prompt and column-header arguments were not retained");
		}
		catch(final Exception e)
		{
			return ("Error#47: format-local label arguments could not be inspected");
		}
		// Test 48: Local labels replace existing prompts without adding headings to composite editors
		try
		{
			final Map<String,AbilityParmEditor> editors = CMLib.ableParms().getEditors();
			final String codedSpellOutput = capturePrompt(editors.get("CODED_SPELL_LIST"),"Local spell label");
			if(!codedSpellOutput.contains("Local spell label"))
				return ("Error#48: coded spell list ignored its local prompt label ["+codedSpellOutput+"]");
			final String legacyCodedSpellOutput = capturePrompt(editors.get("CODED_SPELL_LIST"),null);
			if(!legacyCodedSpellOutput.contains("Effects:"))
				return ("Error#48: coded spell list changed its legacy summary label");
			final String pcodedSpellOutput = capturePrompt(editors.get("PCODED_SPELL_LIST"),"Local packed spell label");
			if(!pcodedSpellOutput.contains("Local packed spell label"))
				return ("Error#48: packed coded spell list ignored its local prompt label");
			final String legacyPcodedSpellOutput = capturePrompt(editors.get("PCODED_SPELL_LIST"),null);
			if(!legacyPcodedSpellOutput.contains("Effects/Behavs:"))
				return ("Error#48: packed coded spell list changed its legacy summary label");
			final String wearLocationOutput = capturePrompt(editors.get("CODED_WEAR_LOCATION"),"Local wear label");
			if(wearLocationOutput.contains("Local wear label")
			||(!wearLocationOutput.contains("Layer:"))
			||(!wearLocationOutput.contains("Wear on any one of:")))
				return ("Error#48: coded wear location added a local heading or lost a subfield label");
			final String legacyWearLocOutput = capturePrompt(editors.get("WEAR_LOC"),null);
			if(!legacyWearLocOutput.contains("Wear Location:"))
				return ("Error#48: WEAR_LOC changed its legacy prompt wording");
		}
		catch(final Exception e)
		{
			return ("Error#48: specialized prompt labels could not be verified");
		}
		// Test 49: A missing argument-closing parenthesis cannot bypass typed validation
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE,ICE]", "FIRE"))
			return ("Error#49: malformed typed placeholder arguments should be rejected");
		// Test 50: Malformed argument fields are rejected
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices)]", "FIRE"))
			return ("Error#50: malformed placeholder argument fields should be rejected");
		// Test 51: Bare MULTICHOICES accepts a configured choice
		if(!CMLib.ableParms().isValidMiscText("[MULTICHOICES(choices=FIRE,ICE)]", "FIRE"))
			return ("Error#51: configured MULTICHOICES should accept FIRE");
		// Test 52: Bare MULTICHOICES retains integer bitmask acceptance
		if(!CMLib.ableParms().isValidMiscText("[MULTICHOICES(choices=FIRE,ICE)]", "3"))
			return ("Error#52: MULTICHOICES should accept an integer bitmask");
		// Test 53: Bare MULTICHOICES rejects values outside the configured choices
		if(CMLib.ableParms().isValidMiscText("[MULTICHOICES(choices=FIRE,ICE)]", "WATER"))
			return ("Error#53: configured MULTICHOICES should reject WATER");
		// Test 54: Duplicate placeholder argument keys are rejected
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE/choices=ICE)]", "FIRE"))
			return ("Error#54: duplicate placeholder argument keys should be rejected");
		// Test 55: Composite editors retain their subfield labels without adding headings
		try
		{
			final Map<String,AbilityParmEditor> editors = CMLib.ableParms().getEditors();
			final String exitNamesOutput = capturePrompt(editors.get("EXIT_NAMES"),"Local exit labels");
			if(exitNamesOutput.contains("Local exit labels")
			||(!exitNamesOutput.contains("Exit Noun:"))
			||(!exitNamesOutput.contains("Open Verb:"))
			||(!exitNamesOutput.contains("Close Verb:"))
			||(!exitNamesOutput.contains("Opened Text:"))
			||(!exitNamesOutput.contains("Closed Text:"))
			||(!exitNamesOutput.contains("Description:")))
				return ("Error#55: EXIT_NAMES added a heading or lost a subfield label");
			final String legacyExitNamesOutput = capturePrompt(editors.get("EXIT_NAMES"),null);
			if(!legacyExitNamesOutput.contains("Exit Noun:")||legacyExitNamesOutput.contains("Exit Words"))
				return ("Error#55: EXIT_NAMES legacy four-argument output changed");
			final String pagesCharsOutput = capturePrompt(editors.get("PAGES_CHARS"),"Local page limits");
			if(pagesCharsOutput.contains("Local page limits")
			||(!pagesCharsOutput.contains("Max Pages:"))
			||(!pagesCharsOutput.contains("Max Chars/Page:")))
				return ("Error#55: PAGES_CHARS added a heading or lost a subfield label");
			final String legacyPagesCharsOutput = capturePrompt(editors.get("PAGES_CHARS"),null);
			if(!legacyPagesCharsOutput.contains("Max Pages:")
			||(!legacyPagesCharsOutput.contains("Max Chars/Page:"))
			||legacyPagesCharsOutput.contains("Max Pgs/Chrs."))
				return ("Error#55: PAGES_CHARS legacy four-argument output changed");
			final String rideOverrideOutput = capturePrompt(editors.get("RIDE_OVERRIDE_STRS"),"Local ride strings");
			if(rideOverrideOutput.contains("Local ride strings")
			||(!rideOverrideOutput.contains("State Str:"))
			||(!rideOverrideOutput.contains("State Subject:"))
			||(!rideOverrideOutput.contains("Ride Str:"))
			||(!rideOverrideOutput.contains("Mount Str:"))
			||(!rideOverrideOutput.contains("Dismount Str:"))
			||(!rideOverrideOutput.contains("Put Str:")))
				return ("Error#55: RIDE_OVERRIDE_STRS added a heading or lost a subfield label");
			final String legacyRideOverrideOutput = capturePrompt(editors.get("RIDE_OVERRIDE_STRS"),null);
			if(!legacyRideOverrideOutput.contains("State Str:")||legacyRideOverrideOutput.contains("Ride Strings"))
				return ("Error#55: RIDE_OVERRIDE_STRS legacy four-argument output changed");
		}
		catch(final Exception e)
		{
			return ("Error#55: composite prompt labels could not be verified");
		}
		// Test 56: The (") marker keeps normal space-delimited rules for unquoted values
		if(!CMLib.ableParms().isValidMiscText("NM=(\")[STRING(prompt=Name/colh=Player)...](\")", "NM=Bob"))
			return ("Error#56: an unquoted value should follow normal space-delimited rules");
		// Test 57: A quoted value is greedy up to the closing quotation mark
		if(!CMLib.ableParms().isValidMiscText("NM=(\")[STRING(prompt=Name/colh=Player)...](\")", "NM=\"Bob The Builder\""))
			return ("Error#57: a quoted multi-word value should match greedily to the closing quote");
		// Test 58: An unterminated quoted value is invalid
		if(CMLib.ableParms().isValidMiscText("NM=(\")[STRING(prompt=Name/colh=Player)...](\")", "NM=\"Bob The Builder"))
			return ("Error#58: an unterminated quoted value should not match");
		// Test 59: Escape sequences inside a quoted value are de-escaped
		if(!CMLib.ableParms().isValidMiscText("NM=(\")[STRING(prompt=Name/colh=Player)...](\")", "NM=\"Bob \\\"The\\\" Builder\""))
			return ("Error#59: escaped quotes inside a quoted value should be de-escaped");
		// Test 60: The quote-delimited value stops at the closing quote
		if(!CMLib.ableParms().isValidMiscText("NM=(\")[STRING(prompt=Name/colh=Player)...](\")z", "NM=\"Bob The\"z"))
			return ("Error#60: a quoted value should stop at the closing quote and allow a trailing literal");
		// Test 61: A quoted typed value that fails the editor is invalid
		if(CMLib.ableParms().isValidMiscText("N=(\")[NUMBER(prompt=N/colh=N)...](\")", "N=\"notanumber\""))
			return ("Error#61: a quoted typed value should validate through its editor");
		// Test 62: A valid quoted typed value matches
		if(!CMLib.ableParms().isValidMiscText("N=(\")[NUMBER(prompt=N/colh=N)...](\")", "N=\"15\""))
			return ("Error#62: a valid quoted typed value should match");
		// Test 63: Quoted values with spaces do not break following fields
		if(!CMLib.ableParms().isValidMiscText("NM=(\")[STRING(prompt=Name/colh=Player)...](\") ;TELEPORT=[CONFIRM(prompt=Teleport/colh=Teleport)]", "NM=\"Bob The Builder\" ;TELEPORT=true"))
			return ("Error#63: a quoted value with spaces should not break following fields");
		// Test 64: Without a matched quotation mark, the greedy is disregarded
		if(!CMLib.ableParms().isValidMiscText("NM=(\")[STRING(prompt=Name/colh=Player)...](\")z", "NM=Bob z"))
			return ("Error#64: without a matched quotation mark, the greedy should be disregarded");
		// Test 65: Quote mode is restored between repeated groups
		if(!CMLib.ableParms().isValidMiscText("([STRING(prompt=Hated group/colh=Group)]=(\")[ZAPPERMASK(prompt=Mask/colh=Mask)...](\"))...", "KEY1=\"mask one\" KEY2=mask2"))
			return ("Error#65: repeated groups should mix quoted and unquoted masks");
		// Test 66: Quote mode is restored across alternation branches
		if(!CMLib.ableParms().isValidMiscText("FILENAME=(\")[STRING(prompt=Filename/colh=File)...](\") ISRECORDER=[CONFIRM(prompt=Is recorder/colh=Recorder)] TRIGGERTIME=[NUMBER(prompt=Trigger time/colh=Trigger)] MINLEVEL=[NUMBER(prompt=Min level/colh=Min level)] (BY=[STRING(prompt=By/colh=By)])/(PLAYERS=(\")[STRING(prompt=Players/colh=Players)...](\"))", "FILENAME=\"::/resources/crecordings/\" ISRECORDER=true TRIGGERTIME=0 MINLEVEL=10 PLAYERS=\"bob smith\""))
			return ("Error#66: quoted filename and players should match the CRecord format");
		// Test 67: A CHECK flag accepts its configured name token
		if(!CMLib.ableParms().isValidMiscText("([CHECK(name=UNINVOKABLE/prompt=Uninvokable/colh=Uninvokable)])", "UNINVOKABLE"))
			return ("Error#67: a CHECK flag should accept its configured name token");
		// Test 68: An optional CHECK flag accepts an empty value
		if(!CMLib.ableParms().isValidMiscText("([CHECK(name=UNINVOKABLE/prompt=Uninvokable/colh=Uninvokable)])", ""))
			return ("Error#68: an optional CHECK flag should accept an empty value");
		// Test 69: A required CHECK flag rejects values the ability does not read
		if(CMLib.ableParms().isValidMiscText("[CHECK(name=UNINVOKABLE/prompt=Uninvokable/colh=Uninvokable)]", "true")
		||CMLib.ableParms().isValidMiscText("[CHECK(name=UNINVOKABLE/prompt=Uninvokable/colh=Uninvokable)]", "false")
		||CMLib.ableParms().isValidMiscText("[CHECK(name=UNINVOKABLE/prompt=Uninvokable/colh=Uninvokable)]", "SOMETHINGELSE"))
			return ("Error#69: a CHECK flag should reject values other than its name token");
		// Test 70: Delimiter repetition - comma-delimited single value
		if(!CMLib.ableParms().isValidMiscText("[NAME]..,", "foo"))
			return ("Error#70: '[NAME]..,' should match 'foo'");
		// Test 71: Delimiter repetition - comma-delimited multiple values
		if(!CMLib.ableParms().isValidMiscText("[NAME]..,", "foo,bar,baz"))
			return ("Error#71: '[NAME]..,' should match 'foo,bar,baz'");
		// Test 72: Delimiter repetition - rejects trailing delimiter
		if(CMLib.ableParms().isValidMiscText("[NAME]..,", "foo,"))
			return ("Error#72: '[NAME]..,' should reject a trailing comma");
		// Test 73: Delimiter repetition - rejects leading delimiter
		if(CMLib.ableParms().isValidMiscText("[NAME]..,", ",foo"))
			return ("Error#73: '[NAME]..,' should reject a leading comma");
		// Test 74: Delimiter repetition - rejects empty input
		if(CMLib.ableParms().isValidMiscText("[NAME]..,", ""))
			return ("Error#74: '[NAME]..,' should reject empty input");
		// Test 75: Delimiter repetition - semicolon-delimited
		if(!CMLib.ableParms().isValidMiscText("[NAME]..;", "foo;bar"))
			return ("Error#75: '[NAME]..;' should match 'foo;bar'");
		// Test 76: Delimiter repetition - other punctuation delimiter
		if(!CMLib.ableParms().isValidMiscText("[NAME]..|", "foo|bar"))
			return ("Error#76: '[NAME]..|' should match 'foo|bar'");
		// Test 77: Delimiter repetition - grammar character is an invalid delimiter
		if(CMLib.ableParms().isValidMiscText("[NAME]../", "foo/bar"))
			return ("Error#77: '[NAME]../' should be rejected as an invalid delimiter");
		// Test 78: Delimiter repetition - long typed placeholder written once
		if(!CMLib.ableParms().isValidMiscText("[STRING(prompt=Hated group/colh=Group)]..,", "elf,dwarf,orc"))
			return ("Error#78: long placeholder '..,' should match 'elf,dwarf,orc'");
		// Test 79: Delimiter repetition - repeated element is required even when the leading element is optional
		if(CMLib.ableParms().isValidMiscText("([NAME])..;", "foo;"))
			return ("Error#79: '([NAME])..;' should reject a trailing semicolon with no element");
		// Test 80: Delimiter repetition - optional leading element still accepts a value list
		if(!CMLib.ableParms().isValidMiscText("([NAME])..;", "foo;bar"))
			return ("Error#80: '([NAME])..;' should match 'foo;bar'");
		// Test 81: Delimiter repetition - optional leading element accepts empty input
		if(!CMLib.ableParms().isValidMiscText("([NAME])..;", ""))
			return ("Error#81: '([NAME])..;' should match empty");
		return null;
	}
}
