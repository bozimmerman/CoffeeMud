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
import java.net.*;
import java.text.*;
import java.util.*;

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
		return null;
	}
}
