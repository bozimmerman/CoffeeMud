package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Behaviors.StdBehavior;
import com.planet_ink.coffee_mud.core.collections.*;
import java.util.*;

/**
 * Tests that every behavior which consumes parms declares a parmsFormat()
 * which describes what the behavior actually reads.
 *
 * Each row is a behavior id followed by parms values. A leading ! means the
 * value must NOT match the behavior's parmsFormat(). The values come from
 * two places: the parsing in the behavior itself (mostly setParms), and the
 * Example lines for that behavior in resources/help/arc_behaviors.ini.
 *
 * A documented example that the shipped format rejects is recorded as a
 * negative, with the reason, so that repairing the format fails this test
 * and forces the expectation to be reconsidered.
 */
public class Validparms extends StdTest
{
	/**
	 * Behavior id, then parms values. A leading ! means the value must NOT
	 * match the behavior's parmsFormat().
	 */
	private static final String[][] BEHAVIOR_TESTS = {
		// ActiveTicker: MIN/MAX/CHANCE/MINGRP (Behaviors/ActiveTicker.java:84)
		// no help entry documents its parms.
		{"ActiveTicker",
			"min=10",
			"chance=75",
			"min=10 max=20 chance=75",
			"min=10 max=20 chance=75 mingrp=2",
			"!KICK",
			"!min=abc",
			"!chance=many"},
		// Aggressive: delay=, MESSAGE=, BRIBE=, then the aggression flags
		// from CHOICES and a zappermask (Behaviors/Aggressive.java:186).
		// The examples at arc_behaviors.ini:78 write the mask as bare words;
		// each whitespace separated word is confirmed on its own, so a
		// multiword mask has to be quoted, as every writer stores it.
		{"Aggressive",
			"delay=5",
			"message=\"All must die!!\"",
			"MOBKILL",
			"MOBKILL MISBEHAVE",
			"wander",
			"\"-elf +fighter\"",
			"+elf -fighter",
			"message=\"All must die!!\" -elf +fighter",
			"MOBKILL MISBEHAVE +elf -fighter",
			"delay=5 -elf +fighter",
			"!delay=abc",
			"!-foo +bar"},
		// AlignHelper: NUM, then MSG= (Behaviors/AlignHelper.java:75), which
		// is the key the class reads at Behaviors/AlignHelper.java:60 and the
		// key the example at arc_behaviors.ini:118 uses.
		{"AlignHelper",
			"5",
			"-5",
			"",
			"5 msg=\"OMG I'M ATTACKING!\"",
			"msg=\"OMG\"",
			"!message=\"OMG\"",
			"!5.5"},
		// AntiVagrant: ActiveTicker keys, then KICK/KICKOUT/ANYWHERE
		// (Behaviors/AntiVagrant.java:76)
		{"AntiVagrant",
			"min=2 max=3 chance=99",
			"min=2 max=3 chance=99 KICKOUT",
			"min=2 max=3 chance=75 ANYWHERE",
			"min=2 max=3 chance=75 ANYWHERE KICKOUT",
			"kickout",
			"!min=2 max=abc",
			"!JUMPOUT"},
		// Arrest: a law file name, then key=value law settings
		// (Behaviors/Arrest.java:80, parsed at Behaviors/Arrest.java:88)
		{"Arrest",
			"",
			"custom",
			"laws_mycity.ini",
			"custom;JUDGE=\"justice judge\" OFFICERS=\"cityguard police\"",
			"!McElvy 5 msg=\"OMG ATTACK!\""},
		// BribeGateGuard: PRICE=, GATES=, MASK= (Behaviors/BribeGateGuard.java:88)
		{"BribeGateGuard",
			"price=5",
			"gates=Avalon",
			"mask=\"-race +elf\"",
			"price=5 gates=Avalon",
			"mask=\"-race +elf\" price=5",
			"price=5 mask=\"-race +elf\"",
			"!price=five",
			"!mask=-race +elf price=5"},
		// BrotherHelper: NAMEONLY, NUM, MSG= (Behaviors/BrotherHelper.java:100)
		{"BrotherHelper",
			"",
			"5",
			"5 nameonly",
			"5 nameonly msg=\"omg! Attack!\"",
			"!guard"},
		// ClanHelper: CLAN NAME, NUM, MSG=
		// (Behaviors/ClanHelper.java:128; the clan name is read from the head
		// of the parms at Behaviors/ClanHelper.java:95)
		{"ClanHelper",
			"McElvy",
			"McElvy 5",
			"McElvy 5 msg=\"OMG ATTACK!\"",
			"5",
			"",
			"!McElvy 5.5"},
		// CommonSpeaker: LANGUAGE (Behaviors/CommonSpeaker.java:84)
		{"CommonSpeaker",
			"Common",
			"Orcish",
			"Elvish",
			"",
			"!NoSuchLanguage"},
		// Conquerable: JOURNAL=, LAW=, OWNERSHIP=, LOYALTY=, LEVELDIFF=
		// (Behaviors/Conquerable.java:103)
		{"Conquerable",
			"law=FALSE ownership=TRUE loyalty=5 leveldiff=3",
			"journal=\"The Conquest Times\"",
			"JOURNAL=\"The Conquest Times\" LAW=FALSE OWNERSHIP=FALSE",
			"!law=sometimes"},
		// CorpseEater: ActiveTicker keys, EATITEMS/+PLAYER/-NPC/-MOB,
		// then a zappermask (Behaviors/CorpseEater.java:100). The mask in the
		// example at arc_behaviors.ini:337 is bare, so it is quoted here.
		{"CorpseEater",
			"min=10 max=20 chance=75",
			"min=10 max=20 chance=75 EATITEMS",
			"min=10 max=20 chance=75 +PLAYER",
			"min=10 max=20 chance=75 -LEVEL +<17",
			"min=10 max=20 chance=75 \"-RACE +Elf\"",
			"min=10 max=20 chance=75 -RACE +Elf",
			"+DWARF",
			"!min=10 max=20 chance=75 -foo +bar"},
		// Decay: ActiveTicker keys, REMAIN=, USE2=, NOTRIGGER, ANSWER=,
		// then a zappermask (Behaviors/Decay.java:236).
		{"Decay",
			"answer=\" goes poof!\"",
			"use2=true",
			"notrigger",
			"min=10 max=20 chance=75 remain=5",
			"min=10 max=20 chance=75 -LEVEL +<17",
			"min=10 max=20 chance=75 answer=\" goes poof!\"",
			"min=10 max=20 chance=75 \"-race +orc\"",
			"notrigger answer=\" vanishes slowly\" use2=true",
			"min=10 max=20 chance=75 -CLASS +Paladin",
			"!use2=perhaps",
			"!min=10 max=20 chance=75 -foo +bar"},
		// EvilExecutioner: PLAYERS/PLAYER (Behaviors/EvilExecutioner.java:63)
		{"EvilExecutioner",
			"PLAYERS",
			"PLAYER",
			"",
			"!MOBS"},
		// FieryRoom: ActiveTicker keys, TITLE=, DESCRIPTION=, DAMAGE=,
		// EQCHANCE=, BURNTICKS=, then NOSTOP/NONPC/NOFIRETEXT
		// (Behaviors/FieryRoom.java:416). The documented example at
		// arc_behaviors.ini:537 is here in full; it used to separate damage
		// with a semicolon, which the format cannot accept (fields are space
		// separated, though CMParms.getParmStr would have found the damage=
		// after it), so that spelling stays covered as a negative.
		{"FieryRoom",
			"damage=77",
			"title=\"A Flaming Room\"",
			"damage=77 title=\"A Flaming Room\"",
			"min=5 max=10 chance=100 burnticks=5 eqchance=50",
			"NOSTOP NONPC NOFIRETEXT",
			"min=5 max=10 chance=100 damage=77",
			"min=5 max=10 chance=100 damage=77 Title=\"A Horribly Blackened Spot\" "
				+"Description=\"This place was once just like any part of the road.  "
				+"Now it is nothing more than a sooty pit.\" NOSTOP eqchance=50",
			"!min=5 max=10 chance=100;damage=77 Title=\"A Horribly"},
		// Follower: NAME=, a zappermask, and the GROUP/WANDER/NOFOLLOWERS/
		// INVENTORY flags (Behaviors/Follower.java:91). The examples at
		// arc_behaviors.ini:578 write the mask as bare words, which is the
		// stored form, so both the bare and the quoted spellings are legal.
		{"Follower",
			"group",
			"name=\"a guard\"",
			"inventory nofollowers",
			"\"-race +elf\"",
			"group \"-race +elf\"",
			"\"-race +elf\" group",
			"min=1 max=1 chance=50 name=\"a guard\"",
			"min=1 max=1 chance=50 -race +elf",
			"group nofollowers -race +elf +gnome",
			"min=4 max=20 chance=100 inv -race +elf +gnome",
			"wander -class +mage",
			"!-foo +bar"},
		// GateGuard: a zappermask, then KEEPLOCKED/ALLNIGHT
		// (Behaviors/GateGuard.java:87). The example at
		// arc_behaviors.ini:610 is a bare mask, which is the stored form.
		{"GateGuard",
			"keeplocked",
			"allnight",
			"\"-class +mage -race +elf -evil\"",
			"\"-class +mage -race +elf\" keeplocked",
			"-class +mage -race +elf -evil",
			"!-foo +bar"},
		// GoodExecutioner: PLAYERS/PLAYER (Behaviors/GoodExecutioner.java:63)
		{"GoodExecutioner",
			"PLAYERS",
			"PLAYER",
			"!MOBS"},
		// InstantDeath: the ActiveTicker keys it inherits (Behaviors/
		// InstantDeath.java:37 extends ActiveTicker, and its setParms calls
		// super.setParms at :63, which reads MIN/MAX/CHANCE/MINGRP), then
		// MASK=.
		{"InstantDeath",
			"mask=\"-race +orc\"",
			"mask=\"-RACE +ORC\"",
			"min=10 max=20 chance=75 mask=\"-RACE +ORC\"",
			"min=10 max=20 chance=75",
			"!-RACE +ORC",
			"!mask=\"-foo +bar\""},
	};

	/**
	 * Every behavior that implements parmsFormat(), as of this test. Each
	 * one must return a format which parses, and which is not the undefined
	 * default. This list must be updated in step with Behaviors/, so that a
	 * new format owner cannot arrive without tests.
	 */
	private static final String[] FORMATTED_BEHAVIORS = {
		"ActiveTicker",
		"Aggressive",
		"AlignHelper",
		"AntiVagrant",
		"Arrest",
		"BribeGateGuard",
		"BrotherHelper",
		"ClanHelper",
		"CommonSpeaker",
		"Conquerable",
		"CorpseEater",
		"Decay",
		"EvilExecutioner",
		"FieryRoom",
		"Follower",
		"GateGuard",
		"GoodExecutioner",
		"InstantDeath"
	};

	/** A behavior that neither stores nor reads parms. */
	private static final String UNFORMATTED_BEHAVIOR = "CorpseLooter";

	@Override
	public String ID()
	{
		return "Validparms";
	}

	@Override
	public String[] getTestGroups()
	{
		return new String[] { "all" };
	}

	private static String fmt(final Behavior B)
	{
		return (B == null) ? null : B.parmsFormat();
	}

	private static String checkParses(final String format)
	{
		try
		{
			CMLib.ableParms().isValidMiscText(format, "");
			return null;
		}
		catch(final Exception e)
		{
			return (e.getMessage() == null) ? e.getClass().getName() : e.getMessage();
		}
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		int testNum = 0;
		final StringBuilder fails = new StringBuilder("");
		for(final String[] row : BEHAVIOR_TESTS)
		{
			testNum++;
			final Behavior B = CMClass.getBehavior(row[0]);
			if(B == null)
				return "Error#" + testNum + ": behavior '" + row[0] + "' does not exist";
			final String format = fmt(B);
			if((format == null) || (format.equals(CMParms.FORMAT_UNDEFINED)))
				return "Error#" + testNum + ": behavior '" + row[0] + "' could not produce a format";
			for(int v = 1; v < row.length; v++)
			{
				String val = row[v];
				final boolean shouldPass = !val.startsWith("!");
				if(!shouldPass)
					val = val.substring(1);
				final boolean isValid = CMLib.ableParms().isValidMiscText(format, val);
				if(isValid != shouldPass)
					fails.append("Error#" + testNum + ": behavior '" + row[0] + "' with value '" + val
						+ "' should " + (shouldPass ? "match" : "not match")).append("\n");
			}
		}
		// every format owner must have a table row, a format, and a
		// well-formed format
		final Set<String> tested = new HashSet<String>();
		for(final String[] row : BEHAVIOR_TESTS)
			tested.add(row[0].toUpperCase());
		for(final String id : FORMATTED_BEHAVIORS)
		{
			testNum++;
			if(!tested.contains(id.toUpperCase()))
				return "Error#" + testNum + ": behavior '" + id + "' declares a format but has no test values";
			final Behavior B = CMClass.getBehavior(id);
			if(B == null)
				return "Error#" + testNum + ": behavior '" + id + "' does not exist";
			final String format = fmt(B);
			if((format == null) || (format.equals(CMParms.FORMAT_UNDEFINED)))
				return "Error#" + testNum + ": behavior '" + id + "' could not produce a format";
			final String parseError = checkParses(format);
			if(parseError != null)
				return "Error#" + testNum + ": behavior '" + id + "' has a malformed format: " + parseError;
		}
		// a behavior whose parms are never read has no format at all
		testNum++;
		final Behavior unformatted = CMClass.getBehavior(UNFORMATTED_BEHAVIOR);
		if(unformatted == null)
			return "Error#" + testNum + ": behavior '" + UNFORMATTED_BEHAVIOR + "' does not exist";
		final String unformattedFmt = fmt(unformatted);
		if((unformattedFmt != null) && (!unformattedFmt.equals(CMParms.FORMAT_UNDEFINED)))
			return "Error#" + testNum + ": behavior '" + UNFORMATTED_BEHAVIOR + "' now declares a format";
		if(CMLib.ableParms().isValidMiscText(CMParms.FORMAT_UNDEFINED, "min=10"))
			return "Error#" + testNum + ": the undefined format accepted a parms value";
		// the base behavior class must keep the undefined default
		testNum++;
		if(!new StdBehavior().parmsFormat().equals(CMParms.FORMAT_UNDEFINED))
			return "Error#" + testNum + ": StdBehavior no longer returns the undefined format";
		// every instance of a formatted behavior must agree on its format
		testNum++;
		final Behavior tickerA = CMClass.getBehavior("ActiveTicker");
		final Behavior tickerB = CMClass.getBehavior("ActiveTicker");
		if((tickerA == null) || (tickerB == null))
			return "Error#" + testNum + ": ActiveTicker does not exist";
		if(!fmt(tickerA).equals(fmt(tickerB)))
			return "Error#" + testNum + ": two ActiveTicker instances disagree about their format";
		// no format may use the col= spelling that the editor argument list
		// does not define; the argument is colh= (Libraries/CMGenEditor.java)
		testNum++;
		for(final String id : FORMATTED_BEHAVIORS)
		{
			final String format = fmt(CMClass.getBehavior(id));
			if((format != null) && (format.indexOf("/col=") >= 0))
				return "Error#" + testNum + ": behavior '" + id + "' spells its column header col= instead of colh=";
		}
		return null;
	}
}
