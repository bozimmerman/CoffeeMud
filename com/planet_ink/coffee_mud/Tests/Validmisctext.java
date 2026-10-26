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
		if (!CMLib.ableParms().isValidMiscText("abc", "abc"))
		{
			return ("Error#1: literal 'abc' should match 'abc'");
		}

		// Test 2: Literal non-match
		if (CMLib.ableParms().isValidMiscText("abc", "abd"))
		{
			return ("Error#2: literal 'abc' should not match 'abd'");
		}

		// Test 3: Placeholder matches a token
		if (!CMLib.ableParms().isValidMiscText("[NAME]", "Bob "))
		{
			return ("Error#3: placeholder [NAME] should match 'Bob '");
		}

		// Test 4: Placeholder matches token without trailing space
		if (!CMLib.ableParms().isValidMiscText("[NAME]", "Bob"))
		{
			return ("Error#4: placeholder [NAME] should match 'Bob'");
		}

		// Test 5: Two placeholders
		if (!CMLib.ableParms().isValidMiscText("[A] [B]", "foo bar "))
		{
			return ("Error#5: '[A] [B]' should match 'foo bar '");
		}

		// Test 6: Optional group - present
		if (!CMLib.ableParms().isValidMiscText("(abc)", "abc"))
		{
			return ("Error#6: optional '(abc)' should match 'abc'");
		}

		// Test 7: Optional group - absent
		if (!CMLib.ableParms().isValidMiscText("(abc)", ""))
		{
			return ("Error#7: optional '(abc)' should match empty string");
		}

		// Test 8: Optional group with required suffix
		if (!CMLib.ableParms().isValidMiscText("(abc)def", "def"))
		{
			return ("Error#8: '(abc)def' should match 'def'");
		}

		// Test 9: Optional group with required suffix - both present
		if (!CMLib.ableParms().isValidMiscText("(abc)def", "abcdef"))
		{
			return ("Error#9: '(abc)def' should match 'abcdef'");
		}

		// Test 10: Alternation - first choice
		if (!CMLib.ableParms().isValidMiscText("(a/b)", "a"))
		{
			return ("Error#10: alternation '(a/b)' should match 'a'");
		}

		// Test 11: Alternation - second choice
		if (!CMLib.ableParms().isValidMiscText("(a/b)", "b"))
		{
			return ("Error#11: alternation '(a/b)' should match 'b'");
		}

		// Test 12: Alternation - no match
		if (CMLib.ableParms().isValidMiscText("(a/b)", "c"))
		{
			return ("Error#12: alternation '(a/b)' should not match 'c'");
		}

		// Test 13: Repetition with literal
		if (!CMLib.ableParms().isValidMiscText("a...", "aaa"))
		{
			return ("Error#13: repetition 'a...' should match 'aaa'");
		}

		// Test 14: Repetition with placeholder and delimiter
		if (!CMLib.ableParms().isValidMiscText("[NAME];...", "foo;bar;baz;"))
		{
			return ("Error#14: '[NAME];...' should match 'foo;bar;baz;'");
		}

		// Test 15: Escape - literal bracket
		if (!CMLib.ableParms().isValidMiscText("\\[test\\]", "[test]"))
		{
			return ("Error#15: escaped '\\[test\\]' should match '[test]'");
		}

		// Test 16: Escape - literal parenthesis
		if (!CMLib.ableParms().isValidMiscText("\\(abc\\)", "(abc)"))
		{
			return ("Error#16: escaped '\\(abc\\)' should match '(abc)'");
		}

		// Test 17: Complex format - KEY=[VALUE]
		if (!CMLib.ableParms().isValidMiscText("KEY=[VALUE]", "KEY=something "))
		{
			return ("Error#17: 'KEY=[VALUE]' should match 'KEY=something '");
		}

		// Test 18: Complex format with optional
		if (!CMLib.ableParms().isValidMiscText("[NAME](;[NAME]...)", "foo"))
		{
			return ("Error#18: '[NAME](;[NAME]...)' should match 'foo'");
		}

		// Test 19: Complex format with optional - multiple values
		if (!CMLib.ableParms().isValidMiscText("[NAME](;[NAME]...)", "foo;bar;baz"))
		{
			return ("Error#19: '[NAME](;[NAME]...)' should match 'foo;bar;baz'");
		}

		// Test 20: Multiple optional groups
		if (!CMLib.ableParms().isValidMiscText("(A)(B)(C)", "AC"))
		{
			return ("Error#20: '(A)(B)(C)' should match 'AC'");
		}

		// Test 21: Empty proposition against empty format
		if (!CMLib.ableParms().isValidMiscText("", ""))
		{
			return ("Error#21: empty format should match empty proposition");
		}

		// Test 22: Non-empty format against empty proposition should fail
		if (CMLib.ableParms().isValidMiscText("abc", ""))
		{
			return ("Error#22: 'abc' should not match empty proposition");
		}

		// Test 23: Alternation with multi-char options
		if (!CMLib.ableParms().isValidMiscText("(TRUE/FALSE)", "FALSE"))
		{
			return ("Error#23: '(TRUE/FALSE)' should match 'FALSE'");
		}

		// Test 24: Nested optional not consumed
		if (!CMLib.ableParms().isValidMiscText("([A])", ""))
		{
			return ("Error#24: '([A])' should match empty (optional placeholder)");
		}
		// Test 25: Alternative - first option
		if (!CMLib.ableParms().isValidMiscText("a b/c d", "a b d"))
		{
			return ("Error#25: 'a b/c d' should match 'a b d' (first alternative)");
		}
		// Test 26: Alternative - second option
		if (!CMLib.ableParms().isValidMiscText("a b/c d", "a c d"))
		{
			return ("Error#26: 'a b/c d' should match 'a c d' (second alternative)");
		}
		// Test 27: Alternative - both options invalid
		if (CMLib.ableParms().isValidMiscText("a b/c d", "a b c d"))
		{
			return ("Error#27: 'a b/c d' should not match 'a b c d' (can't use both alternatives)");
		}
		// Test 28: Repeating placeholder - single match
		if (!CMLib.ableParms().isValidMiscText("[file]...", "foo"))
		{
			return ("Error#28: '[file]...' should match 'foo' (one repetition)");
		}
		// Test 29: Repeating placeholder - multiple matches
		if (!CMLib.ableParms().isValidMiscText("[file]...", "foo bar"))
		{
			return ("Error#29: '[file]...' should match 'foo bar' (multiple repetitions)");
		}
		// Test 30: Repeating placeholder - requires at least one
		if (CMLib.ableParms().isValidMiscText("[file]...", ""))
		{
			return ("Error#30: '[file]...' should not match empty (requires at least one)");
		}
		// Test 31: Optional repeating placeholder - empty allowed
		if (!CMLib.ableParms().isValidMiscText("([file]...)", ""))
		{
			return ("Error#31: '([file]...)' should match empty (optional repetition)");
		}
		// Test 32: Optional repeating placeholder - multiple matches
		if (!CMLib.ableParms().isValidMiscText("([file]...)", "foo bar"))
		{
			return ("Error#32: '([file]...)' should match 'foo bar' (optional with repetitions)");
		}
		// Test 33: Flag alternative - short form
		if (!CMLib.ableParms().isValidMiscText("-v/--verbose", "-v"))
		{
			return ("Error#33: '-v/--verbose' should match '-v' (short flag alternative)");
		}
		// Test 34: Flag alternative - long form
		if (!CMLib.ableParms().isValidMiscText("-v/--verbose", "--verbose"))
		{
			return ("Error#34: '-v/--verbose' should match '--verbose' (long flag alternative)");
		}
		// Test 35: Flag alternative - requires one option
		if (CMLib.ableParms().isValidMiscText("-v/--verbose", ""))
		{
			return ("Error#35: '-v/--verbose' should not match empty (requires a flag)");
		}
		// Test 36: Typed placeholder - editor accepts valid value
		if (!CMLib.ableParms().isValidMiscText("[FREQUENCY]", "15"))
		{
			return ("Error#36: '[FREQUENCY]' should match '15' (typed placeholder)");
		}
		// Test 37: Typed placeholder - editor rejects invalid value
		if (CMLib.ableParms().isValidMiscText("[FREQUENCY]", "notanumber"))
		{
			return ("Error#37: '[FREQUENCY]' should not match 'notanumber' (typed placeholder)");
		}
		// Test 38: Greedy placeholder at end of format captures the rest
		if (!CMLib.ableParms().isValidMiscText("[NAME...]", "foo bar"))
		{
			return ("Error#38: '[NAME...]' should match 'foo bar'");
		}
		// Test 39: Greedy placeholder at end of format requires input
		if (CMLib.ableParms().isValidMiscText("[NAME...]", ""))
		{
			return ("Error#39: '[NAME...]' should not match an empty input");
		}
		// Test 40: Greedy placeholder with an immediately following literal
		if (!CMLib.ableParms().isValidMiscText("[NAME...]tail", "foo bartail"))
		{
			return ("Error#40: '[NAME...]tail' should match 'foo bartail'");
		}
		// Test 41: Greedy value may include spaces before the following literal
		if (!CMLib.ableParms().isValidMiscText("[NAME...]tail", "foo bar tail"))
		{
			return ("Error#41: '[NAME...]tail' should match 'foo bar tail'");
		}
		// Test 42: Greedy placeholder requires at least one character
		if (CMLib.ableParms().isValidMiscText("[NAME...]tail", "tail"))
		{
			return ("Error#42: '[NAME...]tail' should not match an empty value");
		}
		// Test 43: A space after the ] terminates the greedy part
		if (CMLib.ableParms().isValidMiscText("[NAME...] tail", "foo bar tail"))
		{
			return ("Error#43: '[NAME...] tail' should not be greedy across the space");
		}
		// Test 44: A space after the ] makes it behave like [NAME]
		if (!CMLib.ableParms().isValidMiscText("[NAME...] tail", "foo tail"))
		{
			return ("Error#44: '[NAME...] tail' should behave like '[NAME] tail'");
		}
		// Test 45: Escaped literal after a space
		if (!CMLib.ableParms().isValidMiscText("[NAME...] \\[x\\]", "foo [x]"))
		{
			return ("Error#45: '[NAME...] \\[x\\]' should match 'foo [x]'");
		}
		// Test 46: Escaped literal immediately following the greedy placeholder
		if (!CMLib.ableParms().isValidMiscText("[NAME...]\\[x\\]", "foo [x]"))
		{
			return ("Error#46: '[NAME...]\\[x\\]' should match 'foo [x]'");
		}
		// Test 47: Greedy values may contain non-ASCII characters
		if (!CMLib.ableParms().isValidMiscText("[NAME...]tail", "caf\u00e9tail"))
		{
			return ("Error#47: '[NAME...]tail' should match 'caf\u00e9tail'");
		}
		// Test 48: Greedy values may contain backslashes
		if (!CMLib.ableParms().isValidMiscText("[NAME...]b", "a\\b"))
		{
			return ("Error#48: '[NAME...]b' should match 'a\\b'");
		}
		// Test 49: Greedy typed placeholder - editor accepts valid value
		if (!CMLib.ableParms().isValidMiscText("[FREQUENCY...]x", "15x"))
		{
			return ("Error#49: '[FREQUENCY...]x' should match '15x'");
		}
		// Test 50: Greedy typed placeholder - editor rejects invalid value
		if (CMLib.ableParms().isValidMiscText("[FREQUENCY...]x", "notanumberx"))
		{
			return ("Error#50: '[FREQUENCY...]x' should not match 'notanumberx'");
		}
		// Test 51: Repeating a greedy placeholder is rejected (not a hang)
		if (CMLib.ableParms().isValidMiscText("[NAME...]...", "foo"))
		{
			return ("Error#51: '[NAME...]...' should be rejected");
		}
		// Test 52: A greedy placeholder at the end of a group must end the format
		if (CMLib.ableParms().isValidMiscText("([NAME...]) tail", "foo tail"))
		{
			return ("Error#52: '([NAME...]) tail' should be rejected");
		}
		// Test 53: ParmType placeholder validates a number
		if (CMLib.ableParms().isValidMiscText("[NUMBER]", "notanumber"))
		{
			return ("Error#53: '[NUMBER]' should reject 'notanumber'");
		}
		// Test 54: ParmType placeholder accepts an integer
		if (!CMLib.ableParms().isValidMiscText("[NUMBER]", "15"))
		{
			return ("Error#54: '[NUMBER]' should accept '15'");
		}
		// Test 55: Nullable numeric ParmType still rejects malformed input
		if (CMLib.ableParms().isValidMiscText("[NUMBERORNULL]", "abc"))
		{
			return ("Error#55: '[NUMBERORNULL]' should reject 'abc'");
		}
		// Test 56: A greedy ONEWORD placeholder rejects multiple words
		if (CMLib.ableParms().isValidMiscText("[ONEWORD...]", "hello world"))
		{
			return ("Error#56: '[ONEWORD...]' should reject 'hello world'");
		}
		// Test 57: A greedy STRING placeholder accepts spaces
		if (!CMLib.ableParms().isValidMiscText("[STRING...]", "hello world"))
		{
			return ("Error#57: '[STRING...]' should accept 'hello world'");
		}
		// Test 58: A NUMBER_PAIR ParmType accepts a comma-separated pair
		if (!CMLib.ableParms().isValidMiscText("[NUMBER_PAIR...]", "5,7"))
		{
			return ("Error#58: '[NUMBER_PAIR...]' should accept '5,7'");
		}
		// Test 59: A NUMBER_PAIR ParmType rejects a malformed pair
		if (CMLib.ableParms().isValidMiscText("[NUMBER_PAIR...]", "5,x"))
		{
			return ("Error#59: '[NUMBER_PAIR...]' should reject '5,x'");
		}
		// Test 60: Bare CHOICES ParmType accepts a configured choice
		if(!CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE,ICE)]", "FIRE"))
		{
			return ("Error#60: configured CHOICES should accept FIRE");
		}
		// Test 61: Bare CHOICES ParmType rejects a value outside the configured choices
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE,ICE)]", "WATER"))
		{
			return ("Error#61: configured CHOICES should reject WATER");
		}
		// Test 62: Bare CHOICES ParmType rejects a missing choices argument
		if(CMLib.ableParms().isValidMiscText("[CHOICES]", "FIRE"))
		{
			return ("Error#62: CHOICES without configured choices should be invalid");
		}
		// Test 63: NUMBER_PAIR preserves the base editor's single-integer acceptance
		if(!CMLib.ableParms().isValidMiscText("[NUMBER_PAIR]", "5"))
		{
			return ("Error#63: '[NUMBER_PAIR]' should accept a single integer");
		}
		// Test 64: A format-local prompt override is returned without changing the editor
		final AbilityParmEditor editor = CMLib.ableParms().getEditors().get("FREQUENCY");
		final String originalPrompt = editor.prompt();
		final String originalHeader = editor.colHeader();
		final AbilityParmEditor promptCopy = editor.copyOf();
		promptCopy.setPrompt("Recharge time");
		if(!promptCopy.prompt().equals("Recharge time"))
			return ("Error#64: copied editor did not return the overridden prompt");
		final AbilityParmEditor secondPromptCopy = editor.copyOf();
		secondPromptCopy.setPrompt("Cooldown");
		if(!secondPromptCopy.prompt().equals("Cooldown"))
			return ("Error#64: second copied editor did not retain its own prompt");
		if(!promptCopy.prompt().equals("Recharge time"))
			return ("Error#64: prompt override leaked between editor copies");
		if(!editor.prompt().equals(originalPrompt))
			return ("Error#64: copied prompt override changed the registered editor");
		// Test 65: A format-local column-header override is returned independently
		final AbilityParmEditor headerCopy = editor.copyOf();
		headerCopy.setColHeader("Recharge");
		if(!headerCopy.colHeader().equals("Recharge"))
			return ("Error#65: copied editor did not return the overridden column header");
		if(!editor.colHeader().equals(originalHeader))
			return ("Error#65: copied column-header override changed the registered editor");
		// Test 66: Local label resolution does not mutate the registered editor
		if((!editor.prompt().equals(originalPrompt))||(!editor.colHeader().equals(originalHeader)))
			return ("Error#66: local label lookup changed the registered editor defaults");
		// Test 67: Parsed placeholder arguments retain both independent labels
		try
		{
			final Method parseMethod = CMLib.ableParms().getClass().getDeclaredMethod("parseMiscTextFormat", String.class);
			parseMethod.setAccessible(true);
			final Object token = parseMethod.invoke(null, "[FREQUENCY(prompt=Recharge time/colh=Recharge)]");
			final Field argsField = token.getClass().getDeclaredField("parameterArgs");
			argsField.setAccessible(true);
			final Map<?,?> args = (Map<?,?>)argsField.get(token);
			if((!"Recharge time".equals(args.get("PROMPT")))||(!"Recharge".equals(args.get("COLH"))))
				return ("Error#67: format-local prompt and column-header arguments were not retained");
		}
		catch(final Exception e)
		{
			return ("Error#67: format-local label arguments could not be inspected");
		}
		// Test 68: Local labels replace existing prompts without adding headings to composite editors
		try
		{
			final Map<String,AbilityParmEditor> editors = CMLib.ableParms().getEditors();
			final String codedSpellOutput = capturePrompt(editors.get("CODED_SPELL_LIST"),"Local spell label");
			if(!codedSpellOutput.contains("Local spell label"))
				return ("Error#68: coded spell list ignored its local prompt label ["+codedSpellOutput+"]");
			final String legacyCodedSpellOutput = capturePrompt(editors.get("CODED_SPELL_LIST"),null);
			if(!legacyCodedSpellOutput.contains("Effects:"))
				return ("Error#68: coded spell list changed its legacy summary label");
			final String pcodedSpellOutput = capturePrompt(editors.get("PCODED_SPELL_LIST"),"Local packed spell label");
			if(!pcodedSpellOutput.contains("Local packed spell label"))
				return ("Error#68: packed coded spell list ignored its local prompt label");
			final String legacyPcodedSpellOutput = capturePrompt(editors.get("PCODED_SPELL_LIST"),null);
			if(!legacyPcodedSpellOutput.contains("Effects/Behavs:"))
				return ("Error#68: packed coded spell list changed its legacy summary label");
			final String wearLocationOutput = capturePrompt(editors.get("CODED_WEAR_LOCATION"),"Local wear label");
			if(wearLocationOutput.contains("Local wear label")
			||(!wearLocationOutput.contains("Layer:"))
			||(!wearLocationOutput.contains("Wear on any one of:")))
				return ("Error#68: coded wear location added a local heading or lost a subfield label");
			final String legacyWearLocOutput = capturePrompt(editors.get("WEAR_LOC"),null);
			if(!legacyWearLocOutput.contains("Wear Location:"))
				return ("Error#68: WEAR_LOC changed its legacy prompt wording");
		}
		catch(final Exception e)
		{
			return ("Error#68: specialized prompt labels could not be verified");
		}
		// Test 69: A missing argument-closing parenthesis cannot bypass typed validation
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE,ICE]", "FIRE"))
			return ("Error#69: malformed typed placeholder arguments should be rejected");
		// Test 70: Malformed argument fields are rejected
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices)]", "FIRE"))
			return ("Error#70: malformed placeholder argument fields should be rejected");
		// Test 71: Bare MULTICHOICES accepts a configured choice
		if(!CMLib.ableParms().isValidMiscText("[MULTICHOICES(choices=FIRE,ICE)]", "FIRE"))
			return ("Error#71: configured MULTICHOICES should accept FIRE");
		// Test 72: Bare MULTICHOICES retains integer bitmask acceptance
		if(!CMLib.ableParms().isValidMiscText("[MULTICHOICES(choices=FIRE,ICE)]", "3"))
			return ("Error#72: MULTICHOICES should accept an integer bitmask");
		// Test 73: Bare MULTICHOICES rejects values outside the configured choices
		if(CMLib.ableParms().isValidMiscText("[MULTICHOICES(choices=FIRE,ICE)]", "WATER"))
			return ("Error#73: configured MULTICHOICES should reject WATER");
		// Test 74: Duplicate placeholder argument keys are rejected
		if(CMLib.ableParms().isValidMiscText("[CHOICES(choices=FIRE/choices=ICE)]", "FIRE"))
			return ("Error#74: duplicate placeholder argument keys should be rejected");
		// Test 75: Composite editors retain their subfield labels without adding headings
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
				return ("Error#75: EXIT_NAMES added a heading or lost a subfield label");
			final String legacyExitNamesOutput = capturePrompt(editors.get("EXIT_NAMES"),null);
			if(!legacyExitNamesOutput.contains("Exit Noun:")||legacyExitNamesOutput.contains("Exit Words"))
				return ("Error#75: EXIT_NAMES legacy four-argument output changed");
			final String pagesCharsOutput = capturePrompt(editors.get("PAGES_CHARS"),"Local page limits");
			if(pagesCharsOutput.contains("Local page limits")
			||(!pagesCharsOutput.contains("Max Pages:"))
			||(!pagesCharsOutput.contains("Max Chars/Page:")))
				return ("Error#75: PAGES_CHARS added a heading or lost a subfield label");
			final String legacyPagesCharsOutput = capturePrompt(editors.get("PAGES_CHARS"),null);
			if(!legacyPagesCharsOutput.contains("Max Pages:")
			||(!legacyPagesCharsOutput.contains("Max Chars/Page:"))
			||legacyPagesCharsOutput.contains("Max Pgs/Chrs."))
				return ("Error#75: PAGES_CHARS legacy four-argument output changed");
			final String rideOverrideOutput = capturePrompt(editors.get("RIDE_OVERRIDE_STRS"),"Local ride strings");
			if(rideOverrideOutput.contains("Local ride strings")
			||(!rideOverrideOutput.contains("State Str:"))
			||(!rideOverrideOutput.contains("State Subject:"))
			||(!rideOverrideOutput.contains("Ride Str:"))
			||(!rideOverrideOutput.contains("Mount Str:"))
			||(!rideOverrideOutput.contains("Dismount Str:"))
			||(!rideOverrideOutput.contains("Put Str:")))
				return ("Error#75: RIDE_OVERRIDE_STRS added a heading or lost a subfield label");
			final String legacyRideOverrideOutput = capturePrompt(editors.get("RIDE_OVERRIDE_STRS"),null);
			if(!legacyRideOverrideOutput.contains("State Str:")||legacyRideOverrideOutput.contains("Ride Strings"))
				return ("Error#75: RIDE_OVERRIDE_STRS legacy four-argument output changed");
		}
		catch(final Exception e)
		{
			return ("Error#75: composite prompt labels could not be verified");
		}
		return null;
	}
}
