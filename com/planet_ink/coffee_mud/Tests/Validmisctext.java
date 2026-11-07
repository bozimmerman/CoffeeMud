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
	private static class PromptCaptureHandler implements java.lang.reflect.InvocationHandler
	{
		private final StringBuilder output = new StringBuilder();
		private MOB mob = null;

		public String output()
		{
			return output.toString();
		}

		private void printArg(final Object[] args)
		{
			if(args != null)
			{
				for(int i = args.length - 1; i >= 0; i--)
				{
					if(args[i] instanceof String)
					{
						output.append((String)args[i]);
						break;
					}
					if(args[i] instanceof char[])
					{
						output.append(new String((char[])args[i]));
						break;
					}
				}
			}
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args)
		{
			final String name = method.getName();
			if(name.equals("toString"))
				return "";
			if(name.equals("hashCode"))
				return Integer.valueOf(System.identityHashCode(proxy));
			if(name.equals("equals"))
				return Boolean.valueOf(proxy == args[0]);
			if(name.equals("mob"))
				return mob;
			if(name.equals("setMob"))
			{
				if((args != null)&&(args.length > 0)&&(args[0] instanceof MOB))
					mob = (MOB)args[0];
				return null;
			}
			if(name.equals("isStopped")||name.equals("isFake"))
				return Boolean.FALSE;
			if(name.equals("blockingIn")||name.equals("readlineContinue"))
				return "";
			if(name.equals("prompt"))
			{
				if((args != null)&&(args.length == 1)&&(args[0] instanceof Session.InputCallback))
				{
					final Session.InputCallback cb = (Session.InputCallback)args[0];
					cb.showPrompt();
					cb.setInput("");
					cb.callBack();
					return null;
				}
				printArg(new Object[]{args[0]});
				if((args != null)&&(args.length >= 2)&&(args[1] instanceof String))
					return args[1];
				return "";
			}
			if(name.equals("choose"))
			{
				printArg(new Object[]{args[0]});
				if((args != null)&&(args.length >= 3)&&(args[2] instanceof String))
					return args[2];
				return "";
			}
			if(name.equals("confirm"))
			{
				if((args != null)&&(args.length >= 2)&&(args[1] instanceof String))
				{
					final String def = (String)args[1];
					final String mapped = def.toUpperCase().startsWith("T") ? "Y" : def;
					return Boolean.valueOf("Y".equals(mapped));
				}
				return Boolean.FALSE;
			}
			if(method.getReturnType() == void.class)
			{
				printArg(args);
				return null;
			}
			final Class<?> rt = method.getReturnType();
			if(rt == boolean.class)
				return Boolean.FALSE;
			if(rt == char.class)
				return Character.valueOf(' ');
			if((rt == byte.class)||(rt == short.class)||(rt == int.class))
				return Integer.valueOf(0);
			if(rt == long.class)
				return Long.valueOf(0L);
			if(rt == float.class)
				return Float.valueOf(0f);
			if(rt == double.class)
				return Double.valueOf(0.0);
			if(rt == String.class)
				return "";
			return null;
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
		final PromptCaptureHandler handler = new PromptCaptureHandler();
		final Session session = (Session)java.lang.reflect.Proxy.newProxyInstance(Session.class.getClassLoader(),
			new Class<?>[]{Session.class}, handler);
		session.setMob(promptMob);
		promptMob.setSession(session);
		try
		{
			editor.commandLinePrompt(promptMob,"",new int[]{0},0);
			return handler.output();
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
		// Test 82: A self-delimiting repeated sequence of greedy placeholders
		//          (Druid_ShapeShift.miscTextFormat) matches a complete writer output.
		final Ability druidA = CMClass.getAbility("Druid_ShapeShift");
		if(druidA == null)
			return ("Error#82: Druid_ShapeShift ability not loaded");
		final String druidFormat = druidA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "3"))
			return ("DBG1: bare '3' fails ["+druidFormat+"]");
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "[FILE=skills/shapeshift.txt]3"))
			return ("DBG2: file form fails");
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "[Cat];NAME=cat;3"))
			return ("DBG3: simple form fails");
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "ATTADJ=0.7;3"))
			return ("DBG4: ATTADJ fails");
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "SHAPES=Rat,Beaver;3"))
			return ("DBG5: SHAPES fails");
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "RACES=Rat,Beaver;3"))
			return ("DBG6: RACES fails");
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "[RODENT FORM];NAME=Rodent form;ATTADJ=0.7;DMGADJ=0.4;ARMADJ=1.0;SPEEDADJ=0.2;SHAPES=Rat,Beaver,Dire Rat;RACES=Rat,Beaver;3"))
			return ("Error#82: a complete self-delimiting form list should match");
		// Test 83: The same format matches several forms plus a trailing race code.
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "[Cat];NAME=cat;[Wolf];NAME=wolf;3"))
			return ("Error#83: multiple self-delimiting forms should match");
		// Test 84: The same format matches the file-reference form.
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "[FILE=skills/shapeshift.txt]3"))
			return ("Error#84: a file-reference form should match");
		// Test 85: The same format matches a bare race code.
		if(!CMLib.ableParms().isValidMiscText(druidFormat, "3"))
			return ("Error#85: a bare race code should match");
		// Test 86: A greedy placeholder followed by '/' is rejected.
		if(CMLib.ableParms().isValidMiscText("[NAME...]/tail", "foo/tail"))
			return ("Error#86: a greedy placeholder before '/' should be rejected");
		// Test 87: A group ending in an open greedy placeholder cannot be
		//          followed by more format (e.g. a delimiter repeat).
		if(CMLib.ableParms().isValidMiscText("(NAME=[STRING(prompt=X/colh=X)...])..;", "NAME=foo bar;"))
			return ("Error#87: an open greedy placeholder at the end of a group should reject trailing format");
		// Test 88: '..;' inside a placeholder is greedy to the ';' and captures spaces.
		if(!CMLib.ableParms().isValidMiscText("[STRING..;]", "foo bar"))
			return ("Error#88: '[STRING..;]' should match 'foo bar'");
		// Test 89: '..;' stops before the delimiter and does not consume it.
		if(CMLib.ableParms().isValidMiscText("[STRING..;]", "foo bar;"))
			return ("Error#89: '[STRING..;]' should not consume the ';'");
		// Test 90: greedy-to-';' placeholders work inside a '..;' list.
		if(!CMLib.ableParms().isValidMiscText("(NAME=[STRING..;])..;", "NAME=foo bar;NAME=baz"))
			return ("Error#90: greedy-to-';' placeholders should work inside a '..;' list");
		// Test 91: greedy-to-';' captures a decimal point.
		if(!CMLib.ableParms().isValidMiscText("[DECIMAL..;]", "0.7"))
			return ("Error#91: '[DECIMAL..;]' should match '0.7'");
		// Test 92: '..,' inside a placeholder is greedy to the ',' without consuming it.
		if(!CMLib.ableParms().isValidMiscText("([STRING..,])..,", "foo,bar"))
			return ("Error#92: '([STRING..,])..,' should match 'foo,bar'");
		// Test 93: bare [NUMBER] accepts a leading negative sign.
		if(!CMLib.ableParms().isValidMiscText("[NUMBER]", "-5"))
			return ("Error#93: '[NUMBER]' should match '-5'");
		// Test 94: a sign must be followed by digits.
		if(CMLib.ableParms().isValidMiscText("[NUMBER]", "-x"))
			return ("Error#94: '[NUMBER]' should reject '-x'");
		// Test 95: bare [DECIMAL] accepts a decimal point.
		if(!CMLib.ableParms().isValidMiscText("[DECIMAL]", "0.7"))
			return ("Error#95: '[DECIMAL]' should match '0.7'");
		// Test 96: bare [DECIMAL] accepts a negative decimal.
		if(!CMLib.ableParms().isValidMiscText("[DECIMAL]", "-2.5"))
			return ("Error#96: '[DECIMAL]' should match '-2.5'");
		// Test 97: malformed decimals are still rejected.
		if(CMLib.ableParms().isValidMiscText("[DECIMAL]", "1.2.3"))
			return ("Error#97: '[DECIMAL]' should reject '1.2.3'");
		// Test 98: an interior '-' still terminates (range).
		if(!CMLib.ableParms().isValidMiscText("[NUMBER]-[NUMBER]", "5-10"))
			return ("Error#98: '[NUMBER]-[NUMBER]' should match '5-10'");
		// Test 99: a sign-as-literal alternative still consumes the sign.
		if(!CMLib.ableParms().isValidMiscText("(LEVEL=(+/-)[NUMBER])", "LEVEL=-5"))
			return ("Error#99: '(LEVEL=(+/-)[NUMBER])' should match 'LEVEL=-5'");
		// Test 100: a bare placeholder captures punctuation up to whitespace.
		if(!CMLib.ableParms().isValidMiscText("[STRING]", "a.b"))
			return ("Error#100: '[STRING]' should match 'a.b'");
		// Test 101: terminators propagate into an alternative branch.
		if(!CMLib.ableParms().isValidMiscText("([STRING]/[NUMBER])%", "50%"))
			return ("Error#101: '([STRING]/[NUMBER])%' should match '50%'");
		// Test 102: terminators propagate to the last child of a group.
		if(!CMLib.ableParms().isValidMiscText("(A [STRING]);", "A foo;"))
			return ("Error#102: '(A [STRING]);' should match 'A foo;'");
		// Test 103: a nullable placeholder must not empty-match before a literal.
		if(CMLib.ableParms().isValidMiscText("X=[STRINGORNULL]y", "X=y"))
			return ("Error#103: 'X=[STRINGORNULL]y' should reject 'X=y'");
		// Test 104: nullable-sibling union stops at a following punctuation terminator.
		if(!CMLib.ableParms().isValidMiscText("[STRING](X);", "ab;"))
			return ("Error#104: '[STRING](X);' should match 'ab;'");
		// Test 105: a trailing delimiter after multiple elements is still rejected
		//           (repeating group's last child sees the next iteration's delimiter).
		if(CMLib.ableParms().isValidMiscText("[NAME]..,", "foo,bar,"))
			return ("Error#105: '[NAME]..,' should reject 'foo,bar,'");
		// Test 106: the (") quote marker is nullable in the suffix walk.
		if(!CMLib.ableParms().isValidMiscText("[STRING](\");", "ab;"))
			return ("Error#106: '[STRING](\");' should match 'ab;'");
		// Test 107: '...' outside an alternation still matches a single first branch.
		if(!CMLib.ableParms().isValidMiscText("(a/b/c)...", "a"))
			return ("Error#107: '(a/b/c)...' should match 'a'");
		// Test 108: '...' outside an alternation still matches a single middle branch.
		if(!CMLib.ableParms().isValidMiscText("(a/b/c)...", "b"))
			return ("Error#108: '(a/b/c)...' should match 'b'");
		// Test 109: '...' inside an alternation repeats only the last branch.
		if(!CMLib.ableParms().isValidMiscText("(a/b/c...)", "c c"))
			return ("Error#109: '(a/b/c...)' should match 'c c'");
		// Test 110: '...' outside an alternation repeats the alternation itself.
		if(!CMLib.ableParms().isValidMiscText("(a/b/c)...", "a b"))
			return ("Error#110: '(a/b/c)...' should match 'a b'");
		// Test 111: '...' on a group around an alternation repeats the alternation.
		if(!CMLib.ableParms().isValidMiscText("((a/b/c))...", "a b"))
			return ("Error#111: '((a/b/c))...' should match 'a b'");
		// Test 112: literals match case-insensitively.
		if(!CMLib.ableParms().isValidMiscText("abc", "ABC"))
			return ("Error#112: literal 'abc' should match 'ABC'");
		// Test 113: leading whitespace in the value is skipped.
		if(!CMLib.ableParms().isValidMiscText("abc", "  abc  "))
			return ("Error#113: literal 'abc' should match '  abc  '");
		// Test 114: multiple spaces separate placeholder words.
		if(!CMLib.ableParms().isValidMiscText("[A] [B]", "foo  bar"))
			return ("Error#114: '[A] [B]' should match 'foo  bar'");
		// Test 115: '?' is a literal character, not a quantifier.
		if(!CMLib.ableParms().isValidMiscText("[NAME]?", "foo?"))
			return ("Error#115: '[NAME]?' should match 'foo?'");
		// Test 116: '?' does not make the preceding token optional.
		if(CMLib.ableParms().isValidMiscText("[NAME]?", "foo"))
			return ("Error#116: '[NAME]?' should reject 'foo'");
		// Test 117: a middle alternation matches its first branch in context.
		if(!CMLib.ableParms().isValidMiscText("aa bb/cc dd", "aa bb dd"))
			return ("Error#117: 'aa bb/cc dd' should match 'aa bb dd'");
		// Test 118: a middle alternation matches its second branch in context.
		if(!CMLib.ableParms().isValidMiscText("aa bb/cc dd", "aa cc dd"))
			return ("Error#118: 'aa bb/cc dd' should match 'aa cc dd'");
		// Test 119: a full multi-token alternation matches a middle branch.
		if(!CMLib.ableParms().isValidMiscText("aa/bb cc/dd", "bb cc"))
			return ("Error#119: 'aa/bb cc/dd' should match 'bb cc'");
		// Test 120: a full multi-token alternation matches its first branch.
		if(!CMLib.ableParms().isValidMiscText("aa/bb cc/dd", "aa"))
			return ("Error#120: 'aa/bb cc/dd' should match 'aa'");
		// Test 121: a middle alternation still requires its leading literal.
		if(CMLib.ableParms().isValidMiscText("aa bb/cc dd", "cc dd"))
			return ("Error#121: 'aa bb/cc dd' should reject 'cc dd'");
		// Test 122: a full alternation rejects values mixed across branches.
		if(CMLib.ableParms().isValidMiscText("aa/bb cc/dd", "aa dd"))
			return ("Error#122: 'aa/bb cc/dd' should reject 'aa dd'");
		// Test 123: a repeating group matches repeated content.
		if(!CMLib.ableParms().isValidMiscText("(ab)...", "abab"))
			return ("Error#123: '(ab)...' should match 'abab'");
		// Test 124: an optional repeating group matches empty input.
		if(!CMLib.ableParms().isValidMiscText("(ab)...", ""))
			return ("Error#124: '(ab)...' should match empty input");
		// Test 125: nested optional groups match full, partial, and empty input.
		if(!CMLib.ableParms().isValidMiscText("((a)bc)", "abc")
		||!CMLib.ableParms().isValidMiscText("((a)bc)", "bc")
		||!CMLib.ableParms().isValidMiscText("((a)bc)", ""))
			return ("Error#125: '((a)bc)' should match 'abc', 'bc', and empty input");
		// Test 126: a plain placeholder captures punctuation up to whitespace.
		if(!CMLib.ableParms().isValidMiscText("[NAME]", "foo;bar"))
			return ("Error#126: '[NAME]' should match 'foo;bar'");
		// Test 127: a whitespace-only format accepts any value.
		if(!CMLib.ableParms().isValidMiscText("   ", "anything"))
			return ("Error#127: a whitespace-only format should accept any value");
		// Test 128: a greedy-to placeholder plus a consumed delimiter matches.
		if(!CMLib.ableParms().isValidMiscText("[NUMBER..;];", "123;"))
			return ("Error#128: '[NUMBER..;];' should match '123;'");
		// Test 129: NUMBERORNULL accepts an integer.
		if(!CMLib.ableParms().isValidMiscText("[NUMBERORNULL]", "5"))
			return ("Error#129: '[NUMBERORNULL]' should match '5'");
		// Test 130: a plain placeholder requires at least one character.
		if(CMLib.ableParms().isValidMiscText("[NAME]", ""))
			return ("Error#130: '[NAME]' should reject empty input");
		// Test 131: a repeating group rejects a partial repetition.
		if(CMLib.ableParms().isValidMiscText("(ab)...", "aba"))
			return ("Error#131: '(ab)...' should reject 'aba'");
		// Test 132: NUMBER accepts only a leading '-', never '+'.
		if(CMLib.ableParms().isValidMiscText("[NUMBER]", "+5"))
			return ("Error#132: '[NUMBER]' should reject '+5'");
		// Test 133: NUMBER rejects decimals.
		if(CMLib.ableParms().isValidMiscText("[NUMBER]", "5.5"))
			return ("Error#133: '[NUMBER]' should reject '5.5'");
		// Test 134: NUMBER requires at least one character.
		if(CMLib.ableParms().isValidMiscText("[NUMBER]", ""))
			return ("Error#134: '[NUMBER]' should reject empty input");
		// Test 135: ONEWORD rejects multi-word input.
		if(CMLib.ableParms().isValidMiscText("[ONEWORD]", "hello world"))
			return ("Error#135: '[ONEWORD]' should reject 'hello world'");
		// Test 136: CONFIRM rejects non-boolean input.
		if(CMLib.ableParms().isValidMiscText("[CONFIRM]", "frog"))
			return ("Error#136: '[CONFIRM]' should reject 'frog'");
		// Test 137: NUMBERORNULL still requires a captured character and a number.
		if(CMLib.ableParms().isValidMiscText("[NUMBERORNULL]", "")
		||CMLib.ableParms().isValidMiscText("[NUMBERORNULL]", "x"))
			return ("Error#137: '[NUMBERORNULL]' should reject empty and non-numeric input");
		// Test 138: CHECK without a configured name rejects everything.
		if(CMLib.ableParms().isValidMiscText("[CHECK]", "x")
		||CMLib.ableParms().isValidMiscText("[CHECK]", ""))
			return ("Error#138: '[CHECK]' without a name should reject any input");
		// Test 139: a greedy-to placeholder validates without backtracking.
		if(CMLib.ableParms().isValidMiscText("[NUMBER..;]", "abc;def"))
			return ("Error#139: '[NUMBER..;]' should reject 'abc;def'");
		// Test 140: malformed formats are rejected, not hung or matched.
		if(CMLib.ableParms().isValidMiscText("[NAME", "foo")
		||CMLib.ableParms().isValidMiscText("(abc", "abc")
		||CMLib.ableParms().isValidMiscText("[]", "foo")
		||CMLib.ableParms().isValidMiscText("()", "")
		||CMLib.ableParms().isValidMiscText("...", "foo")
		||CMLib.ableParms().isValidMiscText("(a//b)", "a")
		||CMLib.ableParms().isValidMiscText("(a/)", "a"))
			return ("Error#140: malformed formats should be rejected");
		// Test 141: dots outside an alternation of groups repeats the alternation.
		if(!CMLib.ableParms().isValidMiscText("((TIME=[NUMBER])/(LEVEL=[NUMBER]))...", "TIME=1 LEVEL=2")
		||!CMLib.ableParms().isValidMiscText("((TIME=[NUMBER])/(LEVEL=[NUMBER]))...", "LEVEL=2 TIME=1")
		||!CMLib.ableParms().isValidMiscText("((TIME=[NUMBER])/(LEVEL=[NUMBER]))...", "TIME=1 TIME=2"))
			return ("Error#141: '((TIME=[NUMBER])/(LEVEL=[NUMBER]))...' should match multi-key, reordered, and repeated input");
		// Test 142: dots outside an alternation of bare placeholders repeats the alternation.
		if(!CMLib.ableParms().isValidMiscText("(([NUMBER])/([ONEWORD]))...", "5 hello 6"))
			return ("Error#142: '(([NUMBER])/([ONEWORD]))...' should match '5 hello 6'");
		// Test 143: dots outside an alternation with a nested alternation branch repeats the alternation.
		if(!CMLib.ableParms().isValidMiscText("(((a/b)/c)/(d))...", "a d b"))
			return ("Error#143: '(((a/b)/c)/(d))...' should match 'a d b'");
		// Test 144: dots outside an alternation with a multi-token branch repeats the alternation.
		if(!CMLib.ableParms().isValidMiscText("((TIME=[NUMBER] LEVEL=[NUMBER])/(INTERVAL=[NUMBER]))...", "TIME=1 LEVEL=2 INTERVAL=3 TIME=4 LEVEL=5"))
			return ("Error#144: multi-token branches should repeat with dots outside the alternation");
		// Test 145: dots inside an alternation of groups repeats only the last branch.
		if(!CMLib.ableParms().isValidMiscText("((TIME=[NUMBER])/(LEVEL=[NUMBER])...)", "LEVEL=2 LEVEL=3")
		||CMLib.ableParms().isValidMiscText("((TIME=[NUMBER])/(LEVEL=[NUMBER])...)", "TIME=1 LEVEL=2"))
			return ("Error#145: dots inside the alternation should repeat only the last branch");
		// Test 146: a dynamically built literal alternation validates fixed keys and rejects garbage.
		final String immFmt = CMClass.getAbility("Immunities").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(immFmt, "FIRE ALL COLD")
		||!CMLib.ableParms().isValidMiscText(immFmt, "")
		||CMLib.ableParms().isValidMiscText(immFmt, "FIRE BOGUSXYZ"))
			return ("Error#146: the Immunities format should accept type keys, ALL, and empty, and reject garbage");
		// Test 148: the Immunities format accepts ability IDs.
		if(!CMLib.ableParms().isValidMiscText(immFmt, "COLD Spell_Fireball"))
			return ("Error#148: the Immunities format should accept ability IDs");
		// Test 149: the Immunities format accepts behavior IDs.
		if(!CMLib.ableParms().isValidMiscText(immFmt, "FIRE Aggressive"))
			return ("Error#149: the Immunities format should accept behavior IDs");
		// Test 147: quoted-or-bare greedy values validate their own class text.
		final String gaitFmt = CMClass.getAbility("Gait").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(gaitFmt, "ARRIVE=\"waddles in\" LEAVE=\"waddles\" STATE=\"waddling\"")
		||!CMLib.ableParms().isValidMiscText(gaitFmt, "ARRIVE=waddling LEAVE=waddles STATE=waddling")
		||CMLib.ableParms().isValidMiscText(gaitFmt, "ARRIVE=\"waddles in\" FOO=1"))
			return ("Error#147: the Gait format should accept quoted, bare, and mixed triples, and reject unknown keys");
		// Test 150: the Injury format accepts stored triple lists, including multiword messages and a trailing semicolon.
		final String injFmt = CMClass.getAbility("Injury").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(injFmt, "7:torso:10;5:left arm:20;")
		||!CMLib.ableParms().isValidMiscText(injFmt, "3:left arm:10")
		||!CMLib.ableParms().isValidMiscText(injFmt, "")
		||CMLib.ableParms().isValidMiscText(injFmt, "99:torso:10")
		||CMLib.ableParms().isValidMiscText(injFmt, "3:torso")
		||CMLib.ableParms().isValidMiscText(injFmt, "3:klsjdfkasjhdf:10"))
			return ("Error#150: the Injury format should accept part:limb:hurt lists and empty, and reject bad parts, short triples, and non-limb messages");
		// Test 151: the InstanceArea format accepts key=value pairs, faction keys, bare type IDs, and empty.
		final String instFmt = CMClass.getAbility("InstanceArea").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(instFmt, "ID=MyArea DURATION=10")
		||!CMLib.ableParms().isValidMiscText(instFmt, "DESCRIPTION=\"a dark cave\" DURATION=10")
		||!CMLib.ableParms().isValidMiscText(instFmt, "ALIGNMENT=5")
		||!CMLib.ableParms().isValidMiscText(instFmt, "DEFAULT_NEW")
		||!CMLib.ableParms().isValidMiscText(instFmt, "")
		||CMLib.ableParms().isValidMiscText(instFmt, "BOGUSKEY=x")
		||CMLib.ableParms().isValidMiscText(instFmt, "DURATION")
		||CMLib.ableParms().isValidMiscText(instFmt, "NOSUCHTYPE"))
			return ("Error#151: the InstanceArea format should accept pairs, faction keys, type IDs, and empty, and reject bad keys, keyless pairs, and unknown types");
		// Test 152: the Milkable format accepts any subset/order of DRINK/ON/PERDAY/TYPE pairs, and empty.
		final String milkFmt = CMClass.getAbility("Milkable").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(milkFmt, "DRINK=true ON=true PERDAY=500 TYPE=MILK")
		||!CMLib.ableParms().isValidMiscText(milkFmt, "TYPE=MILK DRINK=true")
		||!CMLib.ableParms().isValidMiscText(milkFmt, "PERDAY=500")
		||!CMLib.ableParms().isValidMiscText(milkFmt, "")
		||CMLib.ableParms().isValidMiscText(milkFmt, "BOGUSKEY=x")
		||CMLib.ableParms().isValidMiscText(milkFmt, "DRINK=yes")
		||CMLib.ableParms().isValidMiscText(milkFmt, "TYPE=BOGUSXYZ")
		||CMLib.ableParms().isValidMiscText(milkFmt, "PERDAY"))
			return ("Error#152: the Milkable format should accept DRINK/ON/PERDAY/TYPE pairs in any order or subset, and reject unknown keys, non-boolean booleans, unknown resources, and keyless pairs");
		// Test 153: the PresenceReaction format accepts single, ~~-separated, and +-prefixed name=parms entries, and empty.
		final String presFmt = CMClass.getAbility("PresenceReaction").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(presFmt, "Spell_Fireball=damage")
		||!CMLib.ableParms().isValidMiscText(presFmt, "Aggressive=zone~~Spell_Fireball=damage")
		||!CMLib.ableParms().isValidMiscText(presFmt, "Spell_Fireball=damage~~Aggressive=zone")
		||!CMLib.ableParms().isValidMiscText(presFmt, "Mood=aggressive posture")
		||!CMLib.ableParms().isValidMiscText(presFmt, "+Aggressive=zone")
		||!CMLib.ableParms().isValidMiscText(presFmt, "")
		||CMLib.ableParms().isValidMiscText(presFmt, "BOGUSNAME=x")
		||CMLib.ableParms().isValidMiscText(presFmt, "NOEQUALS")
		||CMLib.ableParms().isValidMiscText(presFmt, "Aggressive"))
			return ("Error#153: the PresenceReaction format should accept name=parms entries joined by ~~, the + form, and empty, and reject unknown names and =-less entries");
		// Test 154: the QuestBound format accepts real quest IDs and the * wildcard, ;-separated, and empty.
		final String questFmt = CMClass.getAbility("QuestBound").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(questFmt, "*")
		||!CMLib.ableParms().isValidMiscText(questFmt, "*;*")
		||!CMLib.ableParms().isValidMiscText(questFmt, "*;")
		||!CMLib.ableParms().isValidMiscText(questFmt, "")
		||CMLib.ableParms().isValidMiscText(questFmt, "BOGUSXYZ")
		||CMLib.ableParms().isValidMiscText(questFmt, ";")
		||CMLib.ableParms().isValidMiscText(questFmt, "Q1;;Q2"))
			return ("Error#154: the QuestBound format should accept quest IDs and *, ;-separated, trailing delimiters, and empty, and reject unknown quests and vacuous or consecutive delimiters");
		// Test 155: the Regeneration format accepts RecType rates and + exceptions in any order, and empty.
		final String regenFmt = CMClass.getAbility("Regeneration").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "HITS=5/3"))
			return ("Error#155b1");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "MANA=4"))
			return ("Error#155b2");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "MANA=4 HITS=5/3"))
			return ("Error#155b3");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "HITS=5/3 MANA=4"))
			return ("Error#155b4");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "HITS=+5"))
			return ("Error#155b5");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "HITS=-5"))
			return ("Error#155b6");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "+MAGIC HITS=5"))
			return ("Error#155b7");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "HITS=5 +MAGIC"))
			return ("Error#155b8");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "+LEVEL10 MANA=4 HITS=5"))
			return ("Error#155b9");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "+LEVEL10 MANA=4"))
			return ("Error#155b10");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, "+SLASHING HITS=5/3"))
			return ("Error#155b11");
		if(!CMLib.ableParms().isValidMiscText(regenFmt, ""))
			return ("Error#155b12");
		if(CMLib.ableParms().isValidMiscText(regenFmt, "BOGUS=5"))
			return ("Error#155b13");
		if(CMLib.ableParms().isValidMiscText(regenFmt, "HITS"))
			return ("Error#155b14");
		if(CMLib.ableParms().isValidMiscText(regenFmt, "HITS=x"))
			return ("Error#155b15");
		if(CMLib.ableParms().isValidMiscText(regenFmt, "+BOGUS"))
			return ("Error#155b16");
		if(CMLib.ableParms().isValidMiscText(regenFmt, "+LEVEL"))
			return ("Error#155b17");
		// Test 156: the Branding format accepts PRICE and OWNER in any order.
		final String brandFmt = CMClass.getAbility("Branding").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(brandFmt, "PRICE=5 OWNER=\"Bob\"")
		||!CMLib.ableParms().isValidMiscText(brandFmt, "OWNER=\"Bob\" PRICE=5")
		||CMLib.ableParms().isValidMiscText(brandFmt, "PRICE"))
			return ("Error#156: the Branding format should accept PRICE and OWNER in any order, and reject keyless pairs");
		// Test 157: the Loyalty format accepts NAME and TELEPORT in any order.
		final String loyalFmt = CMClass.getAbility("Loyalty").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(loyalFmt, "NAME=\"Bob\" TELEPORT=true")
		||!CMLib.ableParms().isValidMiscText(loyalFmt, "TELEPORT=true NAME=\"Bob\"")
		||CMLib.ableParms().isValidMiscText(loyalFmt, "BOGUS=1"))
			return ("Error#157: the Loyalty format should accept NAME and TELEPORT in any order, and reject unknown keys");
		// Test 158: the Encrypto format accepts its pairs in any order.
		final String encrFmt = CMClass.getAbility("Encrypto").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(encrFmt, "LROT=1 WROT=2 LVL=3")
		||!CMLib.ableParms().isValidMiscText(encrFmt, "LVL=3 WROT=2 LROT=1")
		||CMLib.ableParms().isValidMiscText(encrFmt, "BOGUS=1"))
			return ("Error#158: the Encrypto format should accept its pairs in any order, and reject unknown keys");
		// Test 159: the SelfInvesting format accepts LASTUSE and BANKED in any order.
		final String investFmt = CMClass.getAbility("SelfInvesting").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(investFmt, "LASTUSE=100 BANKED=1.5")
		||!CMLib.ableParms().isValidMiscText(investFmt, "BANKED=1.5 LASTUSE=100")
		||CMLib.ableParms().isValidMiscText(investFmt, "BOGUS=1"))
			return ("Error#159: the SelfInvesting format should accept LASTUSE and BANKED in any order, and reject unknown keys");
		// Test 160: the Archon_CRecord format accepts its pairs in any order.
		final String crecFmt = CMClass.getAbility("Archon_CRecord").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(crecFmt, "FILENAME=rec ISRECORDER=true")
		||!CMLib.ableParms().isValidMiscText(crecFmt, "ISRECORDER=true FILENAME=rec")
		||CMLib.ableParms().isValidMiscText(crecFmt, "BOGUS=1"))
			return ("Error#160: the Archon_CRecord format should accept its pairs in any order, and reject unknown keys");
		// Test 161: the Burning format accepts flags in any order.
		final String burnFmt = CMClass.getAbility("Burning").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(burnFmt, "PERMANENT NOSPREAD")
		||!CMLib.ableParms().isValidMiscText(burnFmt, "NOSPREAD PERMANENT")
		||CMLib.ableParms().isValidMiscText(burnFmt, "BOGUS"))
			return ("Error#161: the Burning format should accept flags in any order, and reject unknown flags");
		// Test 162: the Dragonbreath format accepts a colour code with optional LESSER after it.
		final String breathFmt = CMClass.getAbility("Dragonbreath").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(breathFmt, "f LESSER")
		||!CMLib.ableParms().isValidMiscText(breathFmt, "f")
		||CMLib.ableParms().isValidMiscText(breathFmt, "x"))
			return ("Error#162: the Dragonbreath format should accept a colour code with optional LESSER, and reject unknown codes");
		// Test 163: the Prop_Hidden format accepts flags in any order.
		final String hiddenFmt = CMClass.getAbility("Prop_Hidden").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(hiddenFmt, "UNLOCATABLE BUBBLES")
		||!CMLib.ableParms().isValidMiscText(hiddenFmt, "BUBBLES UNLOCATABLE")
		||CMLib.ableParms().isValidMiscText(hiddenFmt, "BOGUS"))
			return ("Error#163: the Prop_Hidden format should accept flags in any order, and reject unknown flags");
		// Test 164: the Prop_ReqNoMOB format accepts flags in any order.
		final String reqnoFmt = CMClass.getAbility("Prop_ReqNoMOB").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(reqnoFmt, "NOFOLLOW NOSNEAK")
		||!CMLib.ableParms().isValidMiscText(reqnoFmt, "NOSNEAK NOFOLLOW")
		||CMLib.ableParms().isValidMiscText(reqnoFmt, "BOGUS"))
			return ("Error#164: the Prop_ReqNoMOB format should accept flags in any order, and reject unknown flags");
		// Test 165: the Prop_RoomUnmappable format accepts flags in any order.
		final String unmapFmt = CMClass.getAbility("Prop_RoomUnmappable").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(unmapFmt, "MAPOK NOEXPLORE")
		||!CMLib.ableParms().isValidMiscText(unmapFmt, "NOEXPLORE MAPOK")
		||CMLib.ableParms().isValidMiscText(unmapFmt, "BOGUS"))
			return ("Error#165: the Prop_RoomUnmappable format should accept flags in any order, and reject unknown flags");
		// Test 166: the Prop_NoSummon format accepts flags in any order.
		final String nosumFmt = CMClass.getAbility("Prop_NoSummon").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(nosumFmt, "ALLOWNONAGGR MAGICONLY")
		||!CMLib.ableParms().isValidMiscText(nosumFmt, "MAGICONLY ALLOWNONAGGR")
		||CMLib.ableParms().isValidMiscText(nosumFmt, "BOGUS"))
			return ("Error#166: the Prop_NoSummon format should accept flags in any order, and reject unknown flags");
		// Test 167: the Prop_NoChannel format accepts channels and flags in any ;-separated order.
		final String nochanFmt = CMClass.getAbility("Prop_NoChannel").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(nochanFmt, "GOSSIP;SENDOK")
		||!CMLib.ableParms().isValidMiscText(nochanFmt, "SENDOK;GOSSIP")
		||CMLib.ableParms().isValidMiscText(nochanFmt, "BOGUSXYZ;SENDOK"))
			return ("Error#167: the Prop_NoChannel format should accept channels and flags in any order, and reject unknown channels");
		// Test 168: the Prop_LimitedItems format accepts a max with optional group.
		final String limFmt = CMClass.getAbility("Prop_LimitedItems").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(limFmt, "5")
		||!CMLib.ableParms().isValidMiscText(limFmt, "5;ID=grp")
		||CMLib.ableParms().isValidMiscText(limFmt, "x"))
			return ("Error#168: the Prop_LimitedItems format should accept a max with optional group, and reject non-numeric maxes");
		// Test 169: the Scarring format accepts ;-separated limb names with left/right prefixes and proper names, in any order, trailing delimiter, and empty.
		final String scarFmt = CMClass.getAbility("Scarring").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "TORSO;ARM"))
			return ("Error#169a");
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "ARM;TORSO"))
			return ("Error#169b");
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "left arm;RIGHT LEG;HEAD"))
			return ("Error#169c");
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "HEAD;RIGHT LEG;left arm"))
			return ("Error#169d");
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "LEFT EYEBROW;HEAD;face"))
			return ("Error#169e");
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "face;HEAD;LEFT EYEBROW"))
			return ("Error#169f");
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "HEAD;TORSO;"))
			return ("Error#169g");
		if(!CMLib.ableParms().isValidMiscText(scarFmt, ""))
			return ("Error#169h");
		if(CMLib.ableParms().isValidMiscText(scarFmt, "BOGUS"))
			return ("Error#169i: the Scarring format should reject unknown limb names");
		if(CMLib.ableParms().isValidMiscText(scarFmt, ";"))
			return ("Error#169j: the Scarring format should reject vacuous delimiters");
		if(CMLib.ableParms().isValidMiscText(scarFmt, "TORSO;;ARM"))
			return ("Error#169k: the Scarring format should reject consecutive delimiters");
		// BODY_PART includes FINGER though the setter ignores it; accepted as a known editor divergence.
		if(!CMLib.ableParms().isValidMiscText(scarFmt, "FINGER"))
			return ("Error#169l: the Scarring format should accept FINGER per the BODY_PART editor");
		if(CMLib.ableParms().isValidMiscText(scarFmt, "+TORSO"))
			return ("Error#169m: the Scarring format should reject the transient +-prefixed form");
		// Test 170: the ScriptLater format accepts <SCRIPTS> docs with CLOCK/TICKS/TIME entries in any order, and an empty wrapper.
		final String laterFmt = CMClass.getAbility("ScriptLater").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT INVOKER=\"Zac\" CLOCK=\"2026/10/31/14 Example Area\">wave</SCRIPT><SCRIPT TICKS=\"5\">say hi</SCRIPT></SCRIPTS>"))
			return ("Error#170a");
		if(!CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT TICKS=\"5\">say hi</SCRIPT><SCRIPT INVOKER=\"Zac\" CLOCK=\"2026/10/31/14 Example Area\">wave</SCRIPT></SCRIPTS>"))
			return ("Error#170b");
		if(!CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT TIME=\"1234567890123\">bow</SCRIPT></SCRIPTS>"))
			return ("Error#170c");
		if(!CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT CLOCK=\"10/31/2026 14h\">nod</SCRIPT></SCRIPTS>"))
			return ("Error#170d");
		if(!CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS></SCRIPTS>"))
			return ("Error#170e");
		if(CMLib.ableParms().isValidMiscText(laterFmt, "BOGUS"))
			return ("Error#170f: the ScriptLater format should reject non-XML text");
		if(CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT>wave</SCRIPT></SCRIPTS>"))
			return ("Error#170g: the ScriptLater format should reject entries without a time attribute");
		if(CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT CLOCK=\"DAY 1\">wave</SCRIPT></SCRIPTS>"))
			return ("Error#170h: the ScriptLater format should reject non-code CLOCK values");
		if(CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT TICKS=\"abc\">wave</SCRIPT></SCRIPTS>"))
			return ("Error#170i: the ScriptLater format should reject non-numeric TICKS values");
		if(CMLib.ableParms().isValidMiscText(laterFmt, "<SCRIPTS><SCRIPT TIME=\"abc\">wave</SCRIPT></SCRIPTS>"))
			return ("Error#170j: the ScriptLater format should reject non-numeric TIME values");
		// Test 171: the SoundEcho format accepts a bare range or order-free RANGE=/ACCURACY= pairs, and empty.
		final String echoFmt = CMClass.getAbility("SoundEcho").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(echoFmt, "10"))
			return ("Error#171a");
		if(!CMLib.ableParms().isValidMiscText(echoFmt, "RANGE=10 ACCURACY=50"))
			return ("Error#171b");
		if(!CMLib.ableParms().isValidMiscText(echoFmt, "ACCURACY=50 RANGE=10"))
			return ("Error#171c");
		if(!CMLib.ableParms().isValidMiscText(echoFmt, "RANGE=7"))
			return ("Error#171d");
		if(!CMLib.ableParms().isValidMiscText(echoFmt, ""))
			return ("Error#171e");
		if(CMLib.ableParms().isValidMiscText(echoFmt, "BOGUS"))
			return ("Error#171f: the SoundEcho format should reject non-numeric text");
		if(CMLib.ableParms().isValidMiscText(echoFmt, "RANGE=abc"))
			return ("Error#171g: the SoundEcho format should reject non-numeric values");
		if(CMLib.ableParms().isValidMiscText(echoFmt, "10 20"))
			return ("Error#171h: the SoundEcho format should reject doubled bare values");
		// Test 172: the TemporaryAffects format accepts the command shapes the setter parses.
		final String tempFmt = CMClass.getAbility("TemporaryAffects").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(tempFmt, ""))
			return ("Error#172a");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "-Spell_Fly"))
			return ("Error#172b");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "Spell_Fly 10"))
			return ("Error#172c");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "Spell_Fireball 20 glow bright"))
			return ("Error#172d");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "+Spell_Fly 10"))
			return ("Error#172e");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "++Spell_Fly 10"))
			return ("Error#172f");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "+#Spell_Fly 10"))
			return ("Error#172g");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "#Spell_Fly 10"))
			return ("Error#172h");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "AMBIANCE 10 glows"))
			return ("Error#172i");
		if(!CMLib.ableParms().isValidMiscText(tempFmt, "+BINDTO fred the fighter"))
			return ("Error#172j");
		if(CMLib.ableParms().isValidMiscText(tempFmt, "Spell_Fly"))
			return ("Error#172k: the TemporaryAffects format should reject IDs without ticks");
		if(CMLib.ableParms().isValidMiscText(tempFmt, "BOGUS"))
			return ("Error#172l: the TemporaryAffects format should reject unknown single words");
		if(CMLib.ableParms().isValidMiscText(tempFmt, "+BINDTO"))
			return ("Error#172m: the TemporaryAffects format should reject bare BINDTO");
		if(CMLib.ableParms().isValidMiscText(tempFmt, "BINDTO fred"))
			return ("Error#172n: the TemporaryAffects format should reject prefix-less BINDTO");
		if(CMLib.ableParms().isValidMiscText(tempFmt, "-Spell_Fly 10"))
			return ("Error#172o: the TemporaryAffects format should reject ticked removals");
		// Test 173: the TemporaryImmunity format accepts +adds and ;-separated name/timestamp entries.
		final String immunFmt = CMClass.getAbility("TemporaryImmunity").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(immunFmt, "+Spell_Fireball"))
			return ("Error#173a");
		if(!CMLib.ableParms().isValidMiscText(immunFmt, "Spell_Fireball/1000;Spell_Shield/2000"))
			return ("Error#173b");
		if(!CMLib.ableParms().isValidMiscText(immunFmt, "Spell_Shield/2000;Spell_Fireball/1000"))
			return ("Error#173c");
		if(!CMLib.ableParms().isValidMiscText(immunFmt, "Spell_Fireball/1000;"))
			return ("Error#173d");
		if(!CMLib.ableParms().isValidMiscText(immunFmt, ""))
			return ("Error#173e");
		if(CMLib.ableParms().isValidMiscText(immunFmt, "BOGUS"))
			return ("Error#173f: the TemporaryImmunity format should reject unknown names");
		if(CMLib.ableParms().isValidMiscText(immunFmt, "Spell_Fireball/abc"))
			return ("Error#173g: the TemporaryImmunity format should reject non-numeric timestamps");
		if(CMLib.ableParms().isValidMiscText(immunFmt, "Spell_Fireball"))
			return ("Error#173h: the TemporaryImmunity format should reject slash-less entries");
		if(CMLib.ableParms().isValidMiscText(immunFmt, "+"))
			return ("Error#173i: the TemporaryImmunity format should reject a bare plus");
		if(CMLib.ableParms().isValidMiscText(immunFmt, "+Spell_Fireball/1000"))
			return ("Error#173j: the TemporaryImmunity format should reject slashed plus-adds");
		// Test 174: the TriggeredAffects format accepts bare and parenthesized key=value entries.
		final String trigFmt = CMClass.getAbility("TriggeredAffects").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(trigFmt, ""))
			return ("Error#174a");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "-Spell_Fly"))
			return ("Error#174b");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD"))
			return ("Error#174c");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "ONTRIGGER=ADD TRIGGER=SITTING TICKS=10 ID=Spell_Fly"))
			return ("Error#174d");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD PARMS=\"glow bright\" MESSAGE=\"shimmers\""))
			return ("Error#174e");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "+ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD"))
			return ("Error#174f");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "(ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD)(ID=Spell_Shield TICKS=5 TRIGGER=SITTING ONTRIGGER=REMOVE)"))
			return ("Error#174g");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD IMEANIT=true INVOKE=false"))
			return ("Error#174h");
		if(!CMLib.ableParms().isValidMiscText(trigFmt, "ID=AMBIANCE TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD PARMS=glows"))
			return ("Error#174i");
		if(CMLib.ableParms().isValidMiscText(trigFmt, "BOGUS"))
			return ("Error#174j: the TriggeredAffects format should reject unknown words");
		if(CMLib.ableParms().isValidMiscText(trigFmt, "ID=Spell_Fly TICKS=abc TRIGGER=SITTING ONTRIGGER=ADD"))
			return ("Error#174k: the TriggeredAffects format should reject non-numeric ticks");
		if(CMLib.ableParms().isValidMiscText(trigFmt, "ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=MAYBE"))
			return ("Error#174l: the TriggeredAffects format should reject bad ONTRIGGER");
		if(CMLib.ableParms().isValidMiscText(trigFmt, "++ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD"))
			return ("Error#174m: the TriggeredAffects format should reject double-plus");
		// Test 175: the WanderHomeLater format accepts order-free boolean/integer option pairs.
		final String wanderFmt = CMClass.getAbility("WanderHomeLater").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(wanderFmt, ""))
			return ("Error#175a");
		if(!CMLib.ableParms().isValidMiscText(wanderFmt, "areaok=true once=true minticks=10"))
			return ("Error#175b");
		if(!CMLib.ableParms().isValidMiscText(wanderFmt, "minticks=10 once=true areaok=true"))
			return ("Error#175c");
		if(!CMLib.ableParms().isValidMiscText(wanderFmt, "destroy=false"))
			return ("Error#175d");
		if(CMLib.ableParms().isValidMiscText(wanderFmt, "BOGUS"))
			return ("Error#175e: the WanderHomeLater format should reject unknown words");
		if(CMLib.ableParms().isValidMiscText(wanderFmt, "areaok"))
			return ("Error#175f: the WanderHomeLater format should reject bare flags");
		if(CMLib.ableParms().isValidMiscText(wanderFmt, "minticks=abc"))
			return ("Error#175g: the WanderHomeLater format should reject non-numeric values");
		// Test 176: the Poison format accepts RANK/ITEMAMBIANCE/MOBAMBIANCE in any order, and empty.
		final String poisonFmt = CMClass.getAbility("Poison").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(poisonFmt, "RANK=1.5 ITEMAMBIANCE=\"foul stench\" MOBAMBIANCE=\"sickly glow\""))
			return ("Error#176a");
		if(!CMLib.ableParms().isValidMiscText(poisonFmt, "MOBAMBIANCE=\"sickly glow\" RANK=1.5 ITEMAMBIANCE=\"foul stench\""))
			return ("Error#176b");
		if(!CMLib.ableParms().isValidMiscText(poisonFmt, "ITEMAMBIANCE=stench RANK=2"))
			return ("Error#176c");
		if(!CMLib.ableParms().isValidMiscText(poisonFmt, "RANK=2 ITEMAMBIANCE=stench"))
			return ("Error#176d");
		if(!CMLib.ableParms().isValidMiscText(poisonFmt, "RANK=1.5"))
			return ("Error#176e");
		if(!CMLib.ableParms().isValidMiscText(poisonFmt, ""))
			return ("Error#176f");
		if(CMLib.ableParms().isValidMiscText(poisonFmt, "BOGUS=1"))
			return ("Error#176g: the Poison format should reject unknown keys");
		if(CMLib.ableParms().isValidMiscText(poisonFmt, "RANK=abc"))
			return ("Error#176h: the Poison format should reject non-numeric RANK values");
		if(CMLib.ableParms().isValidMiscText(poisonFmt, "RANK="))
			return ("Error#176i: the Poison format should reject empty RANK values");
		// Test 177: the UniPoison format accepts RANK plus GenPoison stat codes in any order, and empty.
		final String unipoisonFmt = CMClass.getAbility("UniPoison").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(unipoisonFmt, "RANK=1.5 NAME=\"Deadly Venom\" DAMAGE=5"))
			return ("Error#177a");
		if(!CMLib.ableParms().isValidMiscText(unipoisonFmt, "DAMAGE=5 RANK=1.5 NAME=\"Deadly Venom\""))
			return ("Error#177b");
		if(!CMLib.ableParms().isValidMiscText(unipoisonFmt, "AFFECTTARG MOOD=angry"))
			return ("Error#177c");
		if(!CMLib.ableParms().isValidMiscText(unipoisonFmt, "MOOD=angry AFFECTTARG"))
			return ("Error#177d");
		if(!CMLib.ableParms().isValidMiscText(unipoisonFmt, "RANK"))
			return ("Error#177e");
		if(!CMLib.ableParms().isValidMiscText(unipoisonFmt, ""))
			return ("Error#177f");
		if(CMLib.ableParms().isValidMiscText(unipoisonFmt, "BOGUS=1"))
			return ("Error#177g: the UniPoison format should reject unknown stat codes");
		if(CMLib.ableParms().isValidMiscText(unipoisonFmt, "RANK=abc"))
			return ("Error#177h: the UniPoison format should reject non-numeric RANK values");
		if(CMLib.ableParms().isValidMiscText(unipoisonFmt, "123=abc"))
			return ("Error#177i: the UniPoison format should reject digit-led keys the parser skips");
		// Test 178: the Prayer_MassGrave format accepts a room ID or empty.
		final String graveFmt = CMClass.getAbility("Prayer_MassGrave").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(graveFmt, CMLib.map().getExtendedRoomID(CMLib.map().getRandomRoom())))
			return ("Error#178a");
		if(!CMLib.ableParms().isValidMiscText(graveFmt, ""))
			return ("Error#178b");
		if(CMLib.ableParms().isValidMiscText(graveFmt, "No Such Area#9999"))
			return ("Error#178c: the Prayer_MassGrave format should reject unresolvable room IDs");
		// Test 179: a JSON editor validates JSON documents.
		final AbilityParmEditor jsonEditor = CMLib.ableParms().getEditors().get("JSON");
		if(jsonEditor == null)
			return ("Error#179a: the JSON editor is not registered");
		if(!jsonEditor.confirmValue("{\"a\":1,\"b\":[true,null,\"x\"]}", null))
			return ("Error#179b: the JSON editor rejected a valid object");
		if(!jsonEditor.confirmValue("[1,2,3]", null))
			return ("Error#179c: the JSON editor rejected a valid array");
		if(jsonEditor.confirmValue("{\"a\":", null))
			return ("Error#179d: the JSON editor accepted malformed JSON");
		if(!CMLib.ableParms().isValidMiscText("[JSON]", "{\"a\":1}"))
			return ("Error#179e: '[JSON]' should accept compact JSON");
		if(!CMLib.ableParms().isValidMiscText("[JSON...]", "{\"a\": 1, \"b\": [true, null]}"))
			return ("Error#179f: '[JSON...]' should accept spaced JSON");
		if(CMLib.ableParms().isValidMiscText("[JSON...]", "{\"a\": 1"))
			return ("Error#179g: '[JSON...]' should reject malformed JSON");
		// Test 180: an XML editor validates XML documents.
		final AbilityParmEditor xmlEditor = CMLib.ableParms().getEditors().get("XML");
		if(xmlEditor == null)
			return ("Error#180a: the XML editor is not registered");
		if(!xmlEditor.confirmValue("<FOO bar=\"1\">text</FOO>", null))
			return ("Error#180b: the XML editor rejected a valid document");
		if(!xmlEditor.confirmValue("<FOO/>", null))
			return ("Error#180c: the XML editor rejected a self-closed tag");
		if(xmlEditor.confirmValue("<FOO>", null))
			return ("Error#180d: the XML editor accepted malformed XML");
		if(!CMLib.ableParms().isValidMiscText("[XML]", "<FOO/>"))
			return ("Error#180e: '[XML]' should accept compact XML");
		if(!CMLib.ableParms().isValidMiscText("[XML...]", "<FOO bar=\"1\">some text</FOO>"))
			return ("Error#180f: '[XML...]' should accept spaced XML");
		if(CMLib.ableParms().isValidMiscText("[XML...]", "<FOO>"))
			return ("Error#180g: '[XML...]' should reject malformed XML");
		// Test 181: Prayer_SacredImbuingQuest stores its state as JSON.
		final Ability sacredA = CMClass.getAbility("Prayer_SacredImbuingQuest");
		if(sacredA == null)
			return ("Error#181a: Prayer_SacredImbuingQuest is not loaded");
		final String sacredFmt = sacredA.miscTextFormat();
		if(!sacredFmt.equals("[JSON(prompt=Quest data/colh=Quest data)...]"))
			return ("Error#181b: Prayer_SacredImbuingQuest should use '[JSON(prompt=Quest data/colh=Quest data)...]', got '"+sacredFmt+"'");
		if(!CMLib.ableParms().isValidMiscText(sacredFmt, "{\"ability\": \"Spell_Foo\", \"quest1\": \"Q1\", \"itemname\": \"sword\", \"itemid\": \"item1\"}"))
			return ("Error#181c: Prayer_SacredImbuingQuest should accept a spaced JSON state object");
		if(CMLib.ableParms().isValidMiscText(sacredFmt, "{\"ability\": \"Spell_Foo\""))
			return ("Error#181d: Prayer_SacredImbuingQuest should reject malformed JSON");
		// Test 182: the Prop_AbsorbDamage format accepts amounts and signed absorb types.
		final String absFmt = CMClass.getAbility("Prop_AbsorbDamage").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(absFmt, "50% +ALL -SILVER -CLOTH -LEATHER"))
			return ("Error#182a");
		if(!CMLib.ableParms().isValidMiscText(absFmt, "5 +ALL -MAGIC -SLASHING"))
			return ("Error#182b");
		if(!CMLib.ableParms().isValidMiscText(absFmt, "25% +ALL -LEVEL15"))
			return ("Error#182c");
		if(!CMLib.ableParms().isValidMiscText(absFmt, "15% +BASHING 1 +IRON 3 +STEEL 15% +MAGIC"))
			return ("Error#182d");
		if(!CMLib.ableParms().isValidMiscText(absFmt, "ENHANCED"))
			return ("Error#182e");
		if(CMLib.ableParms().isValidMiscText(absFmt, "BOGUS"))
			return ("Error#182f: the Prop_AbsorbDamage format should reject unknown words");
		if(CMLib.ableParms().isValidMiscText(absFmt, "+BOGUS"))
			return ("Error#182g: the Prop_AbsorbDamage format should reject unknown signed words");
		if(CMLib.ableParms().isValidMiscText(absFmt, "ALL"))
			return ("Error#182h: the Prop_AbsorbDamage format should reject an unsigned ALL");
		if(CMLib.ableParms().isValidMiscText(absFmt, "+"))
			return ("Error#182i: the Prop_AbsorbDamage format should reject a bare sign");
		if(CMLib.ableParms().isValidMiscText(absFmt, "+50"))
			return ("Error#182j: the Prop_AbsorbDamage format should reject signed amounts");
		if(CMLib.ableParms().isValidMiscText(absFmt, "50.5"))
			return ("Error#182k: the Prop_AbsorbDamage format should reject percent values without a percent sign");
		if(CMLib.ableParms().isValidMiscText(absFmt, "50 %"))
			return ("Error#182l: the Prop_AbsorbDamage format should reject spaced percent signs");
		// Test 183: the Prop_CombatAdjuster format accepts signed and equals numeric pairs in any order.
		final String combatFmt = CMClass.getAbility("Prop_CombatAdjuster").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(combatFmt, ""))
			return ("Error#183a");
		if(!CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK+10"))
			return ("Error#183b");
		if(!CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK-20"))
			return ("Error#183c");
		if(!CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK=5"))
			return ("Error#183d");
		if(!CMLib.ableParms().isValidMiscText(combatFmt, "DAMAGE=5 ATTACK+10"))
			return ("Error#183e");
		if(!CMLib.ableParms().isValidMiscText(combatFmt, "SPEED-3 DAMAGE=2 ATTACK=1"))
			return ("Error#183f");
		if(!CMLib.ableParms().isValidMiscText(combatFmt, "HP+100 MANA-50 MOVE+10 ARMOR=25"))
			return ("Error#183g");
		if(!CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK=-5"))
			return ("Error#183h");
		if(CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK"))
			return ("Error#183i: the Prop_CombatAdjuster format should reject bare keys");
		if(CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK 10"))
			return ("Error#183j: the Prop_CombatAdjuster format should reject spaced values");
		if(CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK+abc"))
			return ("Error#183k: the Prop_CombatAdjuster format should reject non-numeric values");
		if(CMLib.ableParms().isValidMiscText(combatFmt, "BOGUS+5"))
			return ("Error#183l: the Prop_CombatAdjuster format should reject unknown keys");
		if(CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK=5.5"))
			return ("Error#183m: the Prop_CombatAdjuster format should reject decimal values");
		if(CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK=+5"))
			return ("Error#183n: the Prop_CombatAdjuster format should reject signed equals values");
		if(CMLib.ableParms().isValidMiscText(combatFmt, "ATTACK+"))
			return ("Error#183o: the Prop_CombatAdjuster format should reject valueless operators");
		// Test 184: the Prop_CommonTwister format validates SKILL/MASK records in either key order.
		final String twisterFmt = CMClass.getAbility("Prop_CommonTwister").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(twisterFmt, ""))
			return ("Error#184a");
		if(!CMLib.ableParms().isValidMiscText(twisterFmt, "SKILL=* MASK=iron"))
			return ("Error#184b");
		if(!CMLib.ableParms().isValidMiscText(twisterFmt, "MASK=iron SKILL=*"))
			return ("Error#184c");
		if(!CMLib.ableParms().isValidMiscText(twisterFmt, "SKILL=Spell_Fireball MASK=steel"))
			return ("Error#184d");
		if(!CMLib.ableParms().isValidMiscText(twisterFmt, "MASK=steel SKILL=Spell_Fireball"))
			return ("Error#184e");
		if(!CMLib.ableParms().isValidMiscText(twisterFmt, "SKILL=* MASK=iron;MASK=steel SKILL=Spell_Fireball"))
			return ("Error#184f");
		if(CMLib.ableParms().isValidMiscText(twisterFmt, "BOGUS"))
			return ("Error#184g: the Prop_CommonTwister format should reject unknown keys");
		if(CMLib.ableParms().isValidMiscText(twisterFmt, "SKILL=NotARealAbility MASK=iron"))
			return ("Error#184h: the Prop_CommonTwister format should reject unknown abilities");
		if(CMLib.ableParms().isValidMiscText(twisterFmt, "MASK="))
			return ("Error#184i: the Prop_CommonTwister format should reject valueless masks");
		if(CMLib.ableParms().isValidMiscText(twisterFmt, "SKILL=* MASK=iron BOGUS"))
			return ("Error#184j: the Prop_CommonTwister format should reject trailing garbage");
		// Test 185: the CAN_DESCS editor validates comma lists of Ability.CAN_DESCS codes.
		final AbilityParmEditor canDescsEditor = CMLib.ableParms().getEditors().get("CAN_DESCS");
		if(canDescsEditor == null)
			return ("Error#185a: the CAN_DESCS editor is not registered");
		if(!canDescsEditor.confirmValue("MOBS", null))
			return ("Error#185b: CAN_DESCS should accept a single code");
		if(!canDescsEditor.confirmValue("MOBS,ITEMS", null))
			return ("Error#185c: CAN_DESCS should accept a comma list of codes");
		if(!canDescsEditor.confirmValue("mobs, items", null))
			return ("Error#185d: CAN_DESCS should accept mixed case and surrounding spaces");
		if(!canDescsEditor.confirmValue("", null))
			return ("Error#185e: CAN_DESCS should accept an empty list");
		if(canDescsEditor.confirmValue("BOGUS", null))
			return ("Error#185f: CAN_DESCS should reject unknown codes");
		if(canDescsEditor.confirmValue("MOBS,BOGUS", null))
			return ("Error#185g: CAN_DESCS should reject a list containing an unknown code");
		if(canDescsEditor.confirmValue("MOB", null))
			return ("Error#185h: CAN_DESCS without PLURAL should reject singular codes");
		if(canDescsEditor.confirmValue("OWNER", null))
			return ("Error#185i: CAN_DESCS should reject OWNER unless it is added");
		final String canDescsFmt = "[CAN_DESCS(prompt=Scan types/colh=Scan)]";
		if(!CMLib.ableParms().isValidMiscText(canDescsFmt, "MOBS,ITEMS"))
			return ("Error#185j: '[CAN_DESCS]' should accept MOBS,ITEMS");
		if(CMLib.ableParms().isValidMiscText(canDescsFmt, "MOB"))
			return ("Error#185k: '[CAN_DESCS]' should reject a singular code without PLURAL");
		final String canDescsPlusFmt = "[CAN_DESCS(plural=true/prompt=Scan types/colh=Scan)]";
		if(!CMLib.ableParms().isValidMiscText(canDescsPlusFmt, "MOB,ITEM"))
			return ("Error#185l: '[CAN_DESCS(plural=true)]' should accept singular codes");
		if(CMLib.ableParms().isValidMiscText(canDescsPlusFmt, "ROOMZ"))
			return ("Error#185m: '[CAN_DESCS(plural=true)]' should reject unknown codes");
		final String canDescsAddFmt = "[CAN_DESCS(add=OWNER,OWNERS/prompt=Scan types/colh=Scan)]";
		if(!CMLib.ableParms().isValidMiscText(canDescsAddFmt, "MOBS,OWNER,OWNERS"))
			return ("Error#185n: '[CAN_DESCS(add=OWNER,OWNERS)]' should accept the added codes");
		if(CMLib.ableParms().isValidMiscText(canDescsAddFmt, "OWNERZ"))
			return ("Error#185o: '[CAN_DESCS(add=OWNER,OWNERS)]' should reject unknown codes");
		// Test 186: the CMATH_FORMULA editor validates CMath formulas over @x1.
		final AbilityParmEditor formulaEditor = CMLib.ableParms().getEditors().get("CMATH_FORMULA");
		if(formulaEditor == null)
			return ("Error#186a: the CMATH_FORMULA editor is not registered");
		if(!formulaEditor.confirmValue("@x1", null))
			return ("Error#186b: CMATH_FORMULA should accept a bare @x1");
		if(!formulaEditor.confirmValue("@x1*2", null))
			return ("Error#186c: CMATH_FORMULA should accept @x1*2");
		if(!formulaEditor.confirmValue("@x1/3+1", null))
			return ("Error#186d: CMATH_FORMULA should accept @x1/3+1");
		if(formulaEditor.confirmValue("", null))
			return ("Error#186e: CMATH_FORMULA should reject an empty formula");
		if(formulaEditor.confirmValue("*2", null))
			return ("Error#186f: CMATH_FORMULA should reject formulas without @x1");
		if(formulaEditor.confirmValue("@x2*2", null))
			return ("Error#186g: CMATH_FORMULA should reject formulas that do not use @x1");
		if(formulaEditor.confirmValue("@x1)", null))
			return ("Error#186h: CMATH_FORMULA should reject a stray close parenthesis");
		if(formulaEditor.confirmValue("(@x1", null))
			return ("Error#186i: CMATH_FORMULA should reject a missing close parenthesis");
		if(formulaEditor.confirmValue("@x12", null))
			return ("Error#186j: CMATH_FORMULA should reject an illegal variable reference");
		final String formulaFmt = "[CMATH_FORMULA(prompt=Tick formula/colh=Formula)]";
		if(!CMLib.ableParms().isValidMiscText(formulaFmt, "@x1*2"))
			return ("Error#186k: '[CMATH_FORMULA]' should accept @x1*2");
		if(CMLib.ableParms().isValidMiscText(formulaFmt, "@x2*2"))
			return ("Error#186l: '[CMATH_FORMULA]' should reject formulas that do not use @x1");
		// Test 187: the Prop_AffectReTick format accepts scan rules and ability formulas.
		final String reTickFmt = CMClass.getAbility("Prop_AffectReTick").miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(reTickFmt, ""))
			return ("Error#187a");
		if(!CMLib.ableParms().isValidMiscText(reTickFmt, "SCAN=MOBS,ITEMS Spell_Fireball=@x1*2"))
			return ("Error#187b");
		if(!CMLib.ableParms().isValidMiscText(reTickFmt, "Spell_MagicMissile=@x1/3 SCAN=MOBS"))
			return ("Error#187c");
		if(!CMLib.ableParms().isValidMiscText(reTickFmt, "SCAN=MOB,ITEM,OWNER,OWNERS Spell_Fireball=@x1+1"))
			return ("Error#187d");
		if(!CMLib.ableParms().isValidMiscText(reTickFmt, "SCANMASK=\"-LEVEL +>=10\" Spell_Fireball=\"@x1 * 2\""))
			return ("Error#187e");
		if(!CMLib.ableParms().isValidMiscText(reTickFmt, "SCANMASK=GUEST Spell_MagicMissile=@x1"))
			return ("Error#187f");
		if(CMLib.ableParms().isValidMiscText(reTickFmt, "SCAN=BOGUS Spell_Fireball=@x1*2"))
			return ("Error#187g: the Prop_AffectReTick format should reject unknown scan codes");
		if(CMLib.ableParms().isValidMiscText(reTickFmt, "Spell_NotARealAbility=@x1*2"))
			return ("Error#187h: the Prop_AffectReTick format should reject unknown abilities");
		if(CMLib.ableParms().isValidMiscText(reTickFmt, "Spell_Fireball=2"))
			return ("Error#187i: the Prop_AffectReTick format should reject formulas without @x1");
		if(CMLib.ableParms().isValidMiscText(reTickFmt, "Spell_Fireball=@x1)"))
			return ("Error#187j: the Prop_AffectReTick format should reject malformed formulas");
		if(CMLib.ableParms().isValidMiscText(reTickFmt, "Fireball=@x1*2"))
			return ("Error#187k: the Prop_AffectReTick format should reject ability names in place of IDs");
		// Test 188: the Prop_AddDamage format accepts amounts, weapon types, attack styles, and masks.
		final Ability addA = CMClass.getAbility("Prop_AddDamage");
		if(addA == null)
			return ("Error#188a: Prop_AddDamage ability not loaded");
		final String addFmt = addA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(addFmt, "50%"))
			return ("Error#188b: a bare percent should match");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "-3"))
			return ("Error#188c: a negative flat bonus should match");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "70% 5"))
			return ("Error#188d: percent then flat bonus should match");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "\"SLASHING\""))
			return ("Error#188e: a quoted weapon type should match");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "\"CAST_SPELL\""))
			return ("Error#188f: a quoted attack style should match");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "50% \"SLASHING\" \"CAST_SPELL\""))
			return ("Error#188g: the full amount/weapon/style combo should match in writer order");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "-3 \"PIERCING\" \"CAST_SPELL\";SMASK=\"-LEVEL +5\""))
			return ("Error#188h: negative bonus plus weapon and style plus a quoted source mask with spaces should match");
		if(CMLib.ableParms().isValidMiscText(addFmt, "BOGUS"))
			return ("Error#188i: the Prop_AddDamage format should reject an unknown bare word");
		if(CMLib.ableParms().isValidMiscText(addFmt, "\"WRONGTYPE\""))
			return ("Error#188j: the Prop_AddDamage format should reject a quoted unknown weapon type");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "SLASHING"))
			return ("Error#188k: a bare (unquoted) weapon type should match alongside the quoted form");
		if(!CMLib.ableParms().isValidMiscText(addFmt, "CAST_SPELL"))
			return ("Error#188l: a bare (unquoted) attack style should match alongside the quoted form");
		if(CMLib.ableParms().isValidMiscText(addFmt, "WRONGTYPE"))
			return ("Error#188m: the Prop_AddDamage format should reject an unknown untyped word");
		// Test 189: the Prop_Artifact format accepts the flags and item id that setMiscText parses.
		final Ability artifactA = CMClass.getAbility("Prop_Artifact");
		if(artifactA == null)
			return ("Error#189a: Prop_Artifact ability not loaded");
		final String artifactFmt = artifactA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(artifactFmt, ""))
			return ("Error#189b: the Prop_Artifact format should accept empty text");
		if(!CMLib.ableParms().isValidMiscText(artifactFmt, "AUTODROP=true NOCAST=false"))
			return ("Error#189c: the Prop_Artifact format should accept its flags");
		if(!CMLib.ableParms().isValidMiscText(artifactFmt, "NOCAST=false AUTODROP=true NODB=true"))
			return ("Error#189d: the Prop_Artifact format should accept its flags in any order");
		if(!CMLib.ableParms().isValidMiscText(artifactFmt, "AUTODROP=true;Sword@1"))
			return ("Error#189e: the Prop_Artifact format should accept an item id after the flags");
		if(!CMLib.ableParms().isValidMiscText(artifactFmt, ";Sword@1"))
			return ("Error#189f: the Prop_Artifact format should accept an item id without flags");
		// Test 190: the Prop_ClanEquipment format accepts the four fields that setMiscText parses.
		final Ability clanEquipA = CMClass.getAbility("Prop_ClanEquipment");
		if(clanEquipA == null)
			return ("Error#190a: Prop_ClanEquipment ability not loaded");
		final String clanEquipFmt = clanEquipA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(clanEquipFmt, "FIRE 5 Clan Type"))
			return ("Error#190b: the Prop_ClanEquipment format should accept four bare fields");
		if(!CMLib.ableParms().isValidMiscText(clanEquipFmt, "FIRE 5 \"Clan Name\" \"Some Type\""))
			return ("Error#190c: the Prop_ClanEquipment format should accept quoted clan fields");
		if(!CMLib.ableParms().isValidMiscText(clanEquipFmt, "\"COLD\" 3 \"Big Clan\" \"Other Type\""))
			return ("Error#190d: the Prop_ClanEquipment format should accept a quoted type");
		if(!CMLib.ableParms().isValidMiscText(clanEquipFmt, "ACID 2 \"Clan\" Type"))
			return ("Error#190e: the Prop_ClanEquipment format should accept a bare final clan type");
		if(CMLib.ableParms().isValidMiscText(clanEquipFmt, "FIRE nope \"Clan\" \"Type\""))
			return ("Error#190f: the Prop_ClanEquipment format should reject a non-numeric power level");
		if(CMLib.ableParms().isValidMiscText(clanEquipFmt, "FIRE 5 \"Clan\""))
			return ("Error#190g: the Prop_ClanEquipment format should reject missing fields");
		// Test 191: the Prop_CloseCommand format accepts the semicolon-delimited values that setMiscText parses.
		final Ability closeCmdA = CMClass.getAbility("Prop_CloseCommand");
		if(closeCmdA == null)
			return ("Error#191a: Prop_CloseCommand ability not loaded");
		final String closeCmdFmt = closeCmdA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(closeCmdFmt, ""))
			return ("Error#191b: the Prop_CloseCommand format should accept empty text");
		if(!CMLib.ableParms().isValidMiscText(closeCmdFmt, "noclose"))
			return ("Error#191c: the Prop_CloseCommand format should accept noclose");
		if(!CMLib.ableParms().isValidMiscText(closeCmdFmt, "close door"))
			return ("Error#191d: the Prop_CloseCommand format should accept a multiword command");
		if(!CMLib.ableParms().isValidMiscText(closeCmdFmt, "message=You can't close that"))
			return ("Error#191e: the Prop_CloseCommand format should accept a multiword message");
		if(!CMLib.ableParms().isValidMiscText(closeCmdFmt, "noclose;close door"))
			return ("Error#191f: the Prop_CloseCommand format should accept semicolon-separated values");
		if(!CMLib.ableParms().isValidMiscText(closeCmdFmt, "message=You can't;close door;noclose"))
			return ("Error#191g: the Prop_CloseCommand format should accept values in any order");
		if(!CMLib.ableParms().isValidMiscText(closeCmdFmt, "close door;"))
			return ("Error#191h: the Prop_CloseCommand format should accept a trailing semicolon");
		// Test 192: the Prop_ClosedDayNight format accepts the flag, hour, message, and mask text setMiscText parses.
		final Ability dayNightA = CMClass.getAbility("Prop_ClosedDayNight");
		if(dayNightA == null)
			return ("Error#192a: Prop_ClosedDayNight ability not loaded");
		final String dayNightFmt = dayNightA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, ""))
			return ("Error#192b: the Prop_ClosedDayNight format should accept empty text");
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, "DAY HOURS=8-17"))
			return ("Error#192c: the Prop_ClosedDayNight format should accept flags and hours");
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, "HOURS=8-17 DAY"))
			return ("Error#192d: the Prop_ClosedDayNight format should accept flags and hours in any order");
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, "SLEEP \"WARNMSG5=Closing soon\""))
			return ("Error#192e: the Prop_ClosedDayNight format should accept warning messages");
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, "\"HOME=The Tavern\" DAY"))
			return ("Error#192f: the Prop_ClosedDayNight format should accept a quoted home");
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, "\"SHOPMSG=We are closed\" DAY"))
			return ("Error#192g: the Prop_ClosedDayNight format should accept a quoted shop message");
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, "LOCKUP SIT HOURS=20-6 MASK=\"-RACE +ELF\""))
			return ("Error#192h: the Prop_ClosedDayNight format should accept a trailing quoted mask");
		if(!CMLib.ableParms().isValidMiscText(dayNightFmt, "DAY MASK=-RACE +ELF"))
			return ("Error#192i: the Prop_ClosedDayNight format should accept a trailing bare mask");
		// Test 193: the Prop_ClosedSeason format accepts the variation codes and mask text setMiscText parses.
		final Ability seasonA = CMClass.getAbility("Prop_ClosedSeason");
		if(seasonA == null)
			return ("Error#193a: Prop_ClosedSeason ability not loaded");
		final String seasonFmt = seasonA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(seasonFmt, ""))
			return ("Error#193b: the Prop_ClosedSeason format should accept empty text");
		if(!CMLib.ableParms().isValidMiscText(seasonFmt, "WINTER NIGHT"))
			return ("Error#193c: the Prop_ClosedSeason format should accept variation codes");
		if(!CMLib.ableParms().isValidMiscText(seasonFmt, "NIGHT WINTER"))
			return ("Error#193d: the Prop_ClosedSeason format should accept variation codes in any order");
		if(!CMLib.ableParms().isValidMiscText(seasonFmt, "SUMMER RAIN MASK=-RACE +ELF"))
			return ("Error#193e: the Prop_ClosedSeason format should accept a trailing bare mask");
		if(!CMLib.ableParms().isValidMiscText(seasonFmt, "WINTER MASK=GUEST"))
			return ("Error#193f: the Prop_ClosedSeason format should accept a mask after codes");
		if(!CMLib.ableParms().isValidMiscText(seasonFmt, "WINTER MASK = GUEST"))
			return ("Error#193g: the Prop_ClosedSeason format should accept a spaced mask after codes");
		// Test 194: the Prop_Doppleganger format accepts its key=value fields in any order and empty.
		final Ability dopFmtA = CMClass.getAbility("Prop_Doppleganger");
		if(dopFmtA == null)
			return ("Error#194a1: Prop_Doppleganger ability not loaded");
		final String dopFmt = dopFmtA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(dopFmt, ""))
			return ("Error#194a2: the Prop_Doppleganger format should accept empty text");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "MAX=27 MIN=27"))
			return ("Error#194a3: the Prop_Doppleganger format should accept MAX and MIN together");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "MIN=27 MAX=27"))
			return ("Error#194a4: the Prop_Doppleganger format should accept MIN and MAX in any order");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "ASMATERIAL=\"METAL\" MIN=27 MAX=27"))
			return ("Error#194a5: the Prop_Doppleganger format should accept ASMATERIAL alongside MAX and MIN in any order");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "LEVELPCT=75 LEVELADD=5 DIFFGRP=true"))
			return ("Error#194a6: the Prop_Doppleganger format should accept unquoted pct and boolean key=value fields");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "SAVEPCT=\"30\" DIFFPCT=80 MIN=20 MAX=40"))
			return ("Error#194a7: the Prop_Doppleganger format should accept a quoted SAVEPCT alongside an unquoted DIFFPCT");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "LEVELPCT=25 SAVEPCT=0 DIFFPCT=100"))
			return ("Error#194a8: the Prop_Doppleganger format should accept fully unquoted pct fields");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "LEVELPCT=\"75\" SAVEPCT=\"0\" DIFFPCT=\"100\""))
			return ("Error#194a9: the Prop_Doppleganger format should accept fully quoted pct fields");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "LEVELPCT=75"))
			return ("Error#194ad: the Prop_Doppleganger format should accept a lone unquoted LEVELPCT");
		if(CMLib.ableParms().isValidMiscText(dopFmt, "DIFFPCT=\"abc\""))
			return ("Error#194ae: the Prop_Doppleganger format should reject an invalid quoted DIFFPCT value");
		if(CMLib.ableParms().isValidMiscText(dopFmt, "LEVELPCT=bad"))
			return ("Error#194af: the Prop_Doppleganger format should reject an invalid unquoted LEVELPCT value");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "10"))
			return ("Error#194b1: the Prop_Doppleganger format should accept a bare number level add");
		if(!CMLib.ableParms().isValidMiscText(dopFmt, "50%"))
			return ("Error#194b2: the Prop_Doppleganger format should accept a percentage level pct");
		if(CMLib.ableParms().isValidMiscText(dopFmt, "BOGUSKEY=x"))
			return ("Error#194c1: the Prop_Doppleganger format should reject an unknown key");
		if(CMLib.ableParms().isValidMiscText(dopFmt, "MATERIALS_METALLER"))
			return ("Error#194c2: the Prop_Doppleganger format should reject a bare flag without a value");
		// Test 195: the Prop_ImproveGather format accepts AMT/MASK/SKILLS in any order and rejects garbage.
		final Ability igA = CMClass.getAbility("Prop_ImproveGather");
		if(igA == null)
			return ("Error#195a1: Prop_ImproveGather ability not loaded");
		final String igFmt = igA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(igFmt, "AMT=7"))
			return ("Error#195a2: the Prop_ImproveGather format should accept a bare AMT number");
		if(!CMLib.ableParms().isValidMiscText(igFmt, "SKILLS=\"Spell_Fireball,Spell_MagicMissile\""))
			return ("Error#195a3: the Prop_ImproveGather format should accept quoted SKILLS with ability ids");
		if(!CMLib.ableParms().isValidMiscText(igFmt, "SKILLS=\"ALL\""))
			return ("Error#195a4: the Prop_ImproveGather format should accept a quoted SKILLS ALL literal");
		if(!CMLib.ableParms().isValidMiscText(igFmt, "MASK=\"ROCK\""))
			return ("Error#195a5: the Prop_ImproveGather format should accept a quoted MASK");
		if(!CMLib.ableParms().isValidMiscText(igFmt, "AMT=3 SKILLS=\"Spell_Fireball,Spell_MagicMissile\""))
			return ("Error#195a6: the Prop_ImproveGather format should accept AMT and SKILLS in any order");
		if(!CMLib.ableParms().isValidMiscText(igFmt, "MASK=\"ROCK\" AMT=5 SKILLS=\"ALL\""))
			return ("Error#195a7: the Prop_ImproveGather format should accept MASK, AMT and SKILLS in any order");
		if(!CMLib.ableParms().isValidMiscText(igFmt, "AMT=4 SKILLS=\"Spell_Fireball,Spell_MagicMissile\" MASK=\"ROCK\""))
			return ("Error#195a8: the Prop_ImproveGather format should accept all three fields in any order");
		if(CMLib.ableParms().isValidMiscText(igFmt, "BOGUS=x"))
			return ("Error#195b1: the Prop_ImproveGather format should reject an unknown key");
		if(CMLib.ableParms().isValidMiscText(igFmt, "SKILLS=\"Fireball\" XTRA=5"))
			return ("Error#195b2: the Prop_ImproveGather format should reject a trailing unknown key after valid fields");
		if(!CMLib.ableParms().isValidMiscText(igFmt, ""))
			return ("Error#195b3: the Prop_ImproveGather format should accept empty text (all fields have defaults)");
		// Test 196: the Prop_ItemBinder format accepts the TO/ON/BOUND/DEATHKEEP/MESSAGE fields setMiscText parses.
		final Ability itemBindA = CMClass.getAbility("Prop_ItemBinder");
		if(itemBindA == null)
			return ("Error#196a: Prop_ItemBinder ability not loaded");
		final String itemBindFmt = itemBindA.miscTextFormat();
		if(itemBindFmt == null)
			return ("Error#196b: Prop_ItemBinder could not produce a format");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, ""))
			return ("Error#196c: the Prop_ItemBinder format should accept empty text (all fields have defaults)");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=CHARACTER"))
			return ("Error#196d: the Prop_ItemBinder format should accept TO=CHARACTER");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=CLAN"))
			return ("Error#196e: the Prop_ItemBinder format should accept TO=CLAN");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=PLAYER"))
			return ("Error#196f: the Prop_ItemBinder format should accept TO=PLAYER");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=NPC"))
			return ("Error#196g: the Prop_ItemBinder format should accept TO=NPC");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=NOONE"))
			return ("Error#196h: the Prop_ItemBinder format should accept TO=NOONE");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "ON=EQUIP"))
			return ("Error#196i: the Prop_ItemBinder format should accept ON=EQUIP");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "ON=PICKUP"))
			return ("Error#196j: the Prop_ItemBinder format should accept ON=PICKUP");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "BOUND=\"Bob The Builder\""))
			return ("Error#196k: the Prop_ItemBinder format should accept a quoted multiword BOUND name");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "DEATHKEEP=true"))
			return ("Error#196l: the Prop_ItemBinder format should accept DEATHKEEP=true");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "MESSAGE=\"the item binds to you!\""))
			return ("Error#196m: the Prop_ItemBinder format should accept a quoted multiword MESSAGE");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=PLAYER ON=PICKUP BOUND=\"Bob The Builder\" DEATHKEEP=true MESSAGE=\"the item binds to you!\""))
			return ("Error#196n: the Prop_ItemBinder format should accept all of its fields together");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=NOONE ON=PICKUP BOUND=\"the graveyard\" DEATHKEEP=false"))
			return ("Error#196o: the Prop_ItemBinder format should accept a noone-bound pickup item");
		if(!CMLib.ableParms().isValidMiscText(itemBindFmt, "TO=CLAN ON=PICKUP BOUND=\"the clan vault\""))
			return ("Error#196p: the Prop_ItemBinder format should accept a clan-bound pickup item");
		// Test 197: the Prop_LangTranslator format accepts the percentage, word, trusted, and
		//           notranslate tokens that setMiscText parses.
		final Ability langTransA = CMClass.getAbility("Prop_LangTranslator");
		if(langTransA == null)
			return ("Error#197a: Prop_LangTranslator ability not loaded");
		final String langTransFmt = langTransA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, ""))
			return ("Error#197b: the Prop_LangTranslator format should accept empty text (common knowledge by default)");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "Elvish"))
			return ("Error#197c: the Prop_LangTranslator format should accept a bare language id");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "50 Elvish"))
			return ("Error#197d: the Prop_LangTranslator format should accept a percentage before a language id");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "50% Draconic"))
			return ("Error#197e: the Prop_LangTranslator format should accept a percent-suffixed percentage before a language id");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "Elvish 'hello"))
			return ("Error#197f: the Prop_LangTranslator format should accept a quoted word after a language id");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "'greetings Draconic"))
			return ("Error#197g: the Prop_LangTranslator format should accept a quoted word before a language id");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "`bonjour` Draconic"))
			return ("Error#197h: the Prop_LangTranslator format should accept back-tick-quoted word lists");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "#Zac Elvish"))
			return ("Error#197i: the Prop_LangTranslator format should accept a # trusted name before a language id");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "NOTRANSLATE"))
			return ("Error#197j: the Prop_LangTranslator format should accept the NOTRANSLATE flag");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "Elvish NOTRANSLATE Goblinese 50"))
			return ("Error#197k: the Prop_LangTranslator format should accept a language id followed by NOTRANSLATE and another language id");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "25% Draconic 'hail 50% Goblinese #Zac NOTRANSLATE"))
			return ("Error#197l: the Prop_LangTranslator format should accept percentages, quoted words, trusted names, and NOTRANSLATE together");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "Elvish 50% 25 Draconic"))
			return ("Error#197m: the Prop_LangTranslator format should accept back-to-back percentages before a language id");
		if(CMLib.ableParms().isValidMiscText(langTransFmt, "BOGUSLANG"))
			return ("Error#197n: the Prop_LangTranslator format should reject an unknown non-language token");
		if(!CMLib.ableParms().isValidMiscText(langTransFmt, "#Zac"))
			return ("Error#197o: the Prop_LangTranslator format should accept a trusted name without a language id");
		// Test 198: the Prop_LimitedContents format accepts the ;-separated integer limits
		//           with masks and MSG/ACTUAL parm parts that setMiscText parses.
		final Ability limitedA = CMClass.getAbility("Prop_LimitedContents");
		if(limitedA == null)
			return ("Error#198a: Prop_LimitedContents ability not loaded");
		final String limitedFmt = limitedA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, ""))
			return ("Error#198b: the Prop_LimitedContents format should accept empty text");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "5"))
			return ("Error#198c: the Prop_LimitedContents format should accept a bare integer limit");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "5 -RACE +ELF"))
			return ("Error#198d: the Prop_LimitedContents format should accept an integer limit with a mask");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "5;10"))
			return ("Error#198e: the Prop_LimitedContents format should accept multiple ;-separated integer limits");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "5 -RACE +ELF;10 -CLASS +Fighter"))
			return ("Error#198f: the Prop_LimitedContents format should accept multiple limits with masks");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "MSG=\"only five\""))
			return ("Error#198g: the Prop_LimitedContents format should accept a quoted MSG parm part");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "ACTUAL=false"))
			return ("Error#198h: the Prop_LimitedContents format should accept an ACTUAL parm part");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "ACTUAL=true MSG=\"do not add more\""))
			return ("Error#198i: the Prop_LimitedContents format should accept ACTUAL and MSG in any order");
		if(!CMLib.ableParms().isValidMiscText(limitedFmt, "5 -RACE +ELF;10;MSG=\"only five\" ACTUAL=false"))
			return ("Error#198j: the Prop_LimitedContents format should accept limits and parm parts together");
		// Test 199: the Prop_ItemTransReceiver and Prop_ItemTransporter formats accept any string,
		//          as they do not parse their misc text at all.
		final Ability transRecvA = CMClass.getAbility("Prop_ItemTransReceiver");
		if(transRecvA == null)
			return ("Error#199a: Prop_ItemTransReceiver ability not loaded");
		final String transRecvFmt = transRecvA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(transRecvFmt, ""))
			return ("Error#199b: the Prop_ItemTransReceiver format should accept an empty string");
		if(!CMLib.ableParms().isValidMiscText(transRecvFmt, "any string at all"))
			return ("Error#199c: the Prop_ItemTransReceiver format should accept any string");
		if(!CMLib.ableParms().isValidMiscText(transRecvFmt, "KEY=value junk;&^%$ 42"))
			return ("Error#199d: the Prop_ItemTransReceiver format should accept arbitrary text of any shape");
		final Ability transSendA = CMClass.getAbility("Prop_ItemTransporter");
		if(transSendA == null)
			return ("Error#199e: Prop_ItemTransporter ability not loaded");
		final String transSendFmt = transSendA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(transSendFmt, ""))
			return ("Error#199f: the Prop_ItemTransporter format should accept an empty string");
		if(!CMLib.ableParms().isValidMiscText(transSendFmt, "any string at all"))
			return ("Error#199g: the Prop_ItemTransporter format should accept any string");
		if(!CMLib.ableParms().isValidMiscText(transSendFmt, "KEY=value junk;&^%$ 42"))
			return ("Error#199h: the Prop_ItemTransporter format should accept arbitrary text of any shape");

		// Test 200: the Prop_LanguageSpeaker format accepts the flag, language, and mask fields that setMiscText parses.
		final Ability langSpkA = CMClass.getAbility("Prop_LanguageSpeaker");
		if(langSpkA == null)
			return ("Error#200a: Prop_LanguageSpeaker ability not loaded");
		final String langSpkFmt = langSpkA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(langSpkFmt, ""))
			return ("Error#200b: the Prop_LanguageSpeaker format should accept empty text");
		if(!CMLib.ableParms().isValidMiscText(langSpkFmt, "PLAYERS=true"))
			return ("Error#200c: the Prop_LanguageSpeaker format should accept a single player flag");
		if(!CMLib.ableParms().isValidMiscText(langSpkFmt, "NOMOBS=true"))
			return ("Error#200d: the Prop_LanguageSpeaker format should accept a single no-mobs flag");
		if(!CMLib.ableParms().isValidMiscText(langSpkFmt, "HOMEONLY=true"))
			return ("Error#200e: the Prop_LanguageSpeaker format should accept a single home-only flag");
		if(!CMLib.ableParms().isValidMiscText(langSpkFmt, "PLAYERS=true NOMOBS=true HOMEONLY=true LANGSTR=\"Common\""))
			return ("Error#200f: the Prop_LanguageSpeaker format should accept all flag fields with a quoted language id");
		if(!CMLib.ableParms().isValidMiscText(langSpkFmt, "LANGSTR=\"Common\""))
			return ("Error#200g: the Prop_LanguageSpeaker format should accept just a quoted language id");
		if(!CMLib.ableParms().isValidMiscText(langSpkFmt, "PLAYERS=true LANGSTR=\"Draconic\";RACE -ELF"))
			return ("Error#200h: the Prop_LanguageSpeaker format should accept flags plus a quoted language id with a zapper mask");
		if(CMLib.ableParms().isValidMiscText(langSpkFmt, "PLAYERS=yes NOMOBS=true HOMEONLY=true LANGSTR=\"Common\""))
			return ("Error#200i: the Prop_LanguageSpeaker format should reject a non-boolean flag value");
		if(CMLib.ableParms().isValidMiscText(langSpkFmt, "LANGSTR=\"NotARealAbility\""))
			return ("Error#200j: the Prop_LanguageSpeaker format should reject an unknown ability id for the language");

		// Test 201: the Prop_RoomForSale format accepts the owner/slash prefix, flag
		//           tokens, the back-tax TAX num X group, and the price that its
		//           saveData() writer produces, in writer and permuted orders.
		final Ability roomSaleA = CMClass.getAbility("Prop_RoomForSale");
		if(roomSaleA == null)
			return ("Error#201a: Prop_RoomForSale ability not loaded");
		final String roomSaleFmt = roomSaleA.miscTextFormat();
		if(roomSaleFmt == null)
			return ("Error#201b: Prop_RoomForSale could not produce a format");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/100000"))
			return ("Error#201d: the Prop_RoomForSale format should accept an owner and price");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/RENTAL 1000"))
			return ("Error#201e: the Prop_RoomForSale format should accept a rental token");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/GRID 1500"))
			return ("Error#201f: the Prop_RoomForSale format should accept a grid token");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/ALLOWTHEFT 1200"))
			return ("Error#201g: the Prop_RoomForSale format should accept an allow-theft token");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/TAX500X 13000"))
			return ("Error#201h: the Prop_RoomForSale format should accept a back-tax group");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/RENTAL GRID ALLOWTHEFT TAX720X 2500"))
			return ("Error#201i: the Prop_RoomForSale format should accept all fields in writer order");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/GRID RENTAL TAX720X ALLOWTHEFT 2500"))
			return ("Error#201j: the Prop_RoomForSale format should accept all fields in a permuted order");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/GRID ALLOWTHEFT RENTAL 1750"))
			return ("Error#201k: the Prop_RoomForSale format should accept a partial three-field permutation");
		if(CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/BOGUS 1000"))
			return ("Error#201m: the Prop_RoomForSale format should reject an unknown bare token");
		if(CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/TAXBADX 1000"))
			return ("Error#201n: the Prop_RoomForSale format should reject a non-numeric back-tax group");
		if(CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob/RENTAL=1 1000"))
			return ("Error#201o: the Prop_RoomForSale format should reject a token with a value suffix");
		if(!CMLib.ableParms().isValidMiscText(roomSaleFmt, "/RENTAL GRID 1800"))
			return ("Error#201l: the Prop_RoomForSale format should accept empty-owner text written by setOwnerName(\"\")");
		if(CMLib.ableParms().isValidMiscText(roomSaleFmt, "Bob//RENTAL GRID 1800"))
			return ("Error#201p: the Prop_RoomForSale format should reject a double slash after an owner");
		if(CMLib.ableParms().isValidMiscText(roomSaleFmt, "//RENTAL GRID 1800"))
			return ("Error#201q: the Prop_RoomForSale format should reject a double leading slash");
		// Test 202: the Prop_AreaForSale format accepts the owner/slash prefix, rental
		//           and theft flag tokens, the back-tax group, and price entries its
		//           writers produce, in writer and permuted orders.
		final Ability areaSaleA = CMClass.getAbility("Prop_AreaForSale");
		if(areaSaleA == null)
			return ("Error#202a: Prop_AreaForSale ability not loaded");
		final String areaSaleFmt = areaSaleA.miscTextFormat();
		if(areaSaleFmt == null)
			return ("Error#202b: Prop_AreaForSale could not produce a format");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/100000"))
			return ("Error#202d: the Prop_AreaForSale format should accept an owner and price");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/RENTAL 1000"))
			return ("Error#202e: the Prop_AreaForSale format should accept a rental token");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/ALLOWTHEFT 1200"))
			return ("Error#202f: the Prop_AreaForSale format should accept an allow-theft token");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/TAX500X 13000"))
			return ("Error#202g: the Prop_AreaForSale format should accept a back-tax group");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/RENTAL ALLOWTHEFT TAX720X 2500"))
			return ("Error#202h: the Prop_AreaForSale format should accept all fields in writer order");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/TAX720X ALLOWTHEFT RENTAL 2500"))
			return ("Error#202i: the Prop_AreaForSale format should accept all fields in a permuted order");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/ALLOWTHEFT RENTAL 1750"))
			return ("Error#202j: the Prop_AreaForSale format should accept a partial three-field permutation");
		if(!CMLib.ableParms().isValidMiscText(areaSaleFmt, "/RENTAL 5000"))
			return ("Error#202k: the Prop_AreaForSale format should accept empty-owner text written by setOwnerName(\"\")");
		if(CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/BOGUS 1000"))
			return ("Error#202l: the Prop_AreaForSale format should reject an unknown bare token");
		if(CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob/TAXBADX 1000"))
			return ("Error#202m: the Prop_AreaForSale format should reject a non-numeric back-tax group");
		if(CMLib.ableParms().isValidMiscText(areaSaleFmt, "Bob//RENTAL ALLOWTHEFT 5000"))
			return ("Error#202n: the Prop_AreaForSale format should reject a double slash after an owner");
		if(CMLib.ableParms().isValidMiscText(areaSaleFmt, "//RENTAL 5000"))
			return ("Error#202o: the Prop_AreaForSale format should reject a double leading slash");
		// Test 203: Apothecary accepts the optional ANTIDOTE mode flag.
		final Ability apoA = CMClass.getAbility("Apothecary");
		if(apoA == null)
			return ("Error#203a: Apothecary ability not loaded");
		final String apoFmt = apoA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(apoFmt, "ANTIDOTE"))
			return ("Error#203b: the Apothecary format should accept ANTIDOTE");
		if(!CMLib.ableParms().isValidMiscText(apoFmt, ""))
			return ("Error#203c: the Apothecary format should accept an empty mode");
		if(CMLib.ableParms().isValidMiscText(apoFmt, "BOGUS"))
			return ("Error#203d: the Apothecary format should reject an unknown mode");
		// Test 204: Paladin_PlanarDefiance accepts the optional plane name editor.
		final Ability planarA = CMClass.getAbility("Paladin_PlanarDefiance");
		if(planarA == null)
			return ("Error#204a: Paladin_PlanarDefiance ability not loaded");
		final String planarFmt = planarA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(planarFmt, ""))
			return ("Error#204b: the Paladin_PlanarDefiance format should accept a blank plane");
		if(CMLib.ableParms().isValidMiscText(planarFmt, "NOTAPLANE"))
			return ("Error#204c: the Paladin_PlanarDefiance format should reject an unknown plane");
		final Ability planeProto = CMClass.getAbilityPrototype("StdPlanarAbility");
		if(planeProto instanceof PlanarAbility)
		{
			final List<String> planeKeys = ((PlanarAbility)planeProto).getAllPlaneKeys();
			if(planeKeys.size() > 0)
			{
				if(!CMLib.ableParms().isValidMiscText(planarFmt, planeKeys.get(0)))
					return ("Error#204d: the Paladin_PlanarDefiance format should accept a known plane");
			}
		}
		// Test 205: Prayer_JoyousRapture accepts NORMAL and the runtime mood choices.
		final Ability raptureA = CMClass.getAbility("Prayer_JoyousRapture");
		if(raptureA == null)
			return ("Error#205a: Prayer_JoyousRapture ability not loaded");
		final String raptureFmt = raptureA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(raptureFmt, "NORMAL"))
			return ("Error#205b: the Prayer_JoyousRapture format should accept NORMAL");
		final String[] moodChoices = com.planet_ink.coffee_mud.Abilities.Misc.Mood.getMoodChoiceList().split(",");
		if(moodChoices.length == 0)
			return ("Error#205c: no mood choices were generated");
		if(!CMLib.ableParms().isValidMiscText(raptureFmt, moodChoices[0]))
			return ("Error#205d: the Prayer_JoyousRapture format should accept a generated mood choice");
		if(CMLib.ableParms().isValidMiscText(raptureFmt, "NOTAMOOD"))
			return ("Error#205e: the Prayer_JoyousRapture format should reject an unknown mood");
		// Test 206: Skill_CaravanTravel accepts the optional FULL navigation flag.
		final Ability caravanA = CMClass.getAbility("Skill_CaravanTravel");
		if(caravanA == null)
			return ("Error#206a: Skill_CaravanTravel ability not loaded");
		final String caravanFmt = caravanA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(caravanFmt, "FULL"))
			return ("Error#206b: the Skill_CaravanTravel format should accept FULL");
		if(!CMLib.ableParms().isValidMiscText(caravanFmt, ""))
			return ("Error#206c: the Skill_CaravanTravel format should accept an empty flag");
		if(CMLib.ableParms().isValidMiscText(caravanFmt, "BOGUS"))
			return ("Error#206d: the Skill_CaravanTravel format should reject an unknown flag");
		// Test 207: Skill_SeaNavigation accepts the optional FULL navigation flag.
		final Ability seaNavA = CMClass.getAbility("Skill_SeaNavigation");
		if(seaNavA == null)
			return ("Error#207a: Skill_SeaNavigation ability not loaded");
		final String seaNavFmt = seaNavA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(seaNavFmt, "FULL"))
			return ("Error#207b: the Skill_SeaNavigation format should accept FULL");
		if(!CMLib.ableParms().isValidMiscText(seaNavFmt, ""))
			return ("Error#207c: the Skill_SeaNavigation format should accept an empty flag");
		if(CMLib.ableParms().isValidMiscText(seaNavFmt, "BOGUS"))
			return ("Error#207d: the Skill_SeaNavigation format should reject an unknown flag");
		// Test 208: Spell_ColorSpray accepts its optional coded effect states.
		final Ability sprayA = CMClass.getAbility("Spell_ColorSpray");
		if(sprayA == null)
			return ("Error#208a: Spell_ColorSpray ability not loaded");
		final String sprayFmt = sprayA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(sprayFmt, "UNCONSCIOUS"))
			return ("Error#208b: the Spell_ColorSpray format should accept UNCONSCIOUS");
		if(!CMLib.ableParms().isValidMiscText(sprayFmt, "STUNNED"))
			return ("Error#208c: the Spell_ColorSpray format should accept STUNNED");
		if(!CMLib.ableParms().isValidMiscText(sprayFmt, ""))
			return ("Error#208d: the Spell_ColorSpray format should accept an empty state");
		if(CMLib.ableParms().isValidMiscText(sprayFmt, "BOGUS"))
			return ("Error#208e: the Spell_ColorSpray format should reject an unknown state");
		// Test 209: Spell_Fear accepts the optional WEAK target flag.
		final Ability fearA = CMClass.getAbility("Spell_Fear");
		if(fearA == null)
			return ("Error#209a: Spell_Fear ability not loaded");
		final String fearFmt = fearA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(fearFmt, "WEAK"))
			return ("Error#209b: the Spell_Fear format should accept WEAK");
		if(!CMLib.ableParms().isValidMiscText(fearFmt, ""))
			return ("Error#209c: the Spell_Fear format should accept an empty flag");
		if(CMLib.ableParms().isValidMiscText(fearFmt, "BOGUS"))
			return ("Error#209d: the Spell_Fear format should reject an unknown flag");
		// Test 210: Spell_LimbRack accepts the optional ARMSONLY target flag.
		final Ability limbA = CMClass.getAbility("Spell_LimbRack");
		if(limbA == null)
			return ("Error#210a: Spell_LimbRack ability not loaded");
		final String limbFmt = limbA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(limbFmt, "ARMSONLY"))
			return ("Error#210b: the Spell_LimbRack format should accept ARMSONLY");
		if(!CMLib.ableParms().isValidMiscText(limbFmt, ""))
			return ("Error#210c: the Spell_LimbRack format should accept an empty flag");
		if(CMLib.ableParms().isValidMiscText(limbFmt, "BOGUS"))
			return ("Error#210d: the Spell_LimbRack format should reject an unknown flag");
		final Ability enlA = CMClass.getAbility("Prop_EnlargeRoom");
		if(enlA == null)
			return ("Error#211a: Prop_AddDamage ability not loaded");
		final String enlFmt = enlA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(enlFmt, ""))
			return ("Error#211b: empty should match");
		if(!CMLib.ableParms().isValidMiscText(enlFmt, "10"))
			return ("Error#211c: number should match");
		if(!CMLib.ableParms().isValidMiscText(enlFmt, "+10"))
			return ("Error#211d: +formula should match");
		if(!CMLib.ableParms().isValidMiscText(enlFmt, "/10"))
			return ("Error#211e: /formula should match");

		return null;
	}
}
