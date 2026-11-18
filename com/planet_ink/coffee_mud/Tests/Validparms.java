package com.planet_ink.coffee_mud.Tests;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMParms;
import com.planet_ink.coffee_mud.core.CMClass;
import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.core.interfaces.CMObject;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB;
import com.planet_ink.coffee_mud.MOBS.interfaces.Deity;
import com.planet_ink.coffee_mud.Common.interfaces.Clan;
import com.planet_ink.coffee_mud.core.Log;
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
	 * Fixtures this test creates for itself, so that the JOURNAL, CLAN and
	 * DEITY fields can be confirmed against real world data. A journal, a
	 * clan and two deities (one single word, one multiword) are written
	 * before the rows run and removed afterwards.
	 */
	private static final String TEST_JOURNAL = "VALIDPARMS_TEST_JOURNAL";
	private static final String TEST_CLAN = "ValidparmsTestClan";
	private static final String[] TEST_DEITIES = { "ValidparmsTestDeity", "Validparms Test Deity" };

	/**
	 * A room id from the world the test runs in, substituted for *ROOMID* in
	 * the rows, so that a room field can be confirmed without hard coding a
	 * room that exists only in one world.
	 */
	private static String TEST_ROOM_ID = null;

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
			TEST_CLAN,
			TEST_CLAN+" 5",
			TEST_CLAN+" 5 msg=\"OMG ATTACK!\"",
			"5",
			"",
			"!McElvy 5.5",
			"!NoSuchClan"},
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
			"journal=\""+TEST_JOURNAL+"\"",
			"JOURNAL=\""+TEST_JOURNAL+"\" LAW=FALSE OWNERSHIP=FALSE",
			"!journal=\"NoSuchJournal\"",
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
		// Emoter: a first field of ticker keys, expires, a quoted in-room list,
		// a zappermask and the emote type words, then ';' and one message per
		// field (Behaviors/Emoter.java:61-125). The six documented examples are
		// here verbatim except that the room list uses *ROOMID*; inroom is
		// captured as a quoted list rather than room by room, because a
		// repeated group holding a greedy-to placeholder can loop forever in
		// the matcher.
		{"Emoter",
			"",
			"broadcast sound chance=25;grumbles!;eeeks!",
			"chance=100 inroom=\"*ROOMID*\";looks horrible!;is bad!",
			"chance=100 inroom=\"*ROOMID*,*ROOMID*\";looks horrible!",
			";grumbles!;eeeks!",
			"min=10 max=20 chance=25;wiggles his bottom.;smiles evilly.;burps!",
			"min=1 max=2 chance=25;sound grumbles!;visual leers broadly.;social smile",
			"smell chance=25 expires=10;smells horrible!;is really stinky!",
			"noise private PUFF chance=25;x!",
			"min=3 max=3 chance=100 mask=\"-race +elf\";grumbles!",
			"chance=25/grumbles!",
			"inroom=\"0\";x!",
			"inroom=\"0,0\";x!",
			"!foo",
			"!chance=abc",
			"!expires=x",
			"!mask=\"+\";x!",
			"!chance=25;;",
			"!inroom=\"NoSuchRoom#99\";x!"},
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
		// FaithHelper: deity name words, max combatants, optional message (Behaviors/FaithHelper.java:115). Example arc_behaviors.ini:70. The deity branch repeats, so any single word is acceptable and only a format that fails to parse can be caught here.
		{"FaithHelper",
			TEST_DEITIES[0]+" 5 msg=\"OMG ATTACK!\"",
			TEST_DEITIES[0],
			TEST_DEITIES[0]+" 5",
			"\""+TEST_DEITIES[1]+"\" 5",
			"5",
			"!NoSuchDeity",
			"!NoSuchDeity 5 msg=\"hi\""},
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
		// Guard: one free form mob name (Behaviors/Guard.java:56, compared with
		// the target name at Behaviors/Guard.java:75). The placeholder sits in
		// an optional group, so empty parms matches, as for every unconfigured
		// behavior; any word is a name, so there is no rejectable value.
		{"Guard",
			"",
			"Bob",
			"a city guard",
			"a guard named Bob"},
		// Hireling: price;minutes;zappermask (Behaviors/Hireling.java:114, split on ; at Behaviors/Hireling.java:87-96). Example arc_behaviors.ini:10000;30.
		{"Hireling",
			"10000;30",
			"10000",
			"10000;30 -race +elf",
			"10000;30 \"-race +elf\"",
			"!abc",
			"!10000;abc",
			"!10000;30;x +elf"},
		{"InstantDeath",
			"mask=\"-race +orc\"",
			"mask=\"-RACE +ORC\"",
			"min=10 max=20 chance=75 mask=\"-RACE +ORC\"",
			"min=10 max=20 chance=75",
			"!-RACE +ORC",
			"!mask=\"-foo +bar\""},
		// ItemIdentifier: a single COST= formula (Behaviors/ItemIdentifier.java, read at CMParms.getParmStr(parms,\"COST\",...) ). Example arc_behaviors.ini: COST=\"100+@x1\". CMATH_FORMULA requires @x1 (EditorCMathFormula.java:47-52).
		{"ItemIdentifier",
			"COST=\"100+@x1\"",
			"COST=100+@x1",
			"COST=\"(100-@x3)+@x1\"",
			"!COST=100",
			"!COST=junk"},
		// ItemMender: a single COST= formula (Behaviors/ItemMender.java, read at CMParms.getParmStr(parms,\"COST\",...) ). Example arc_behaviors.ini: COST=\"100+@x1\". CMATH_FORMULA requires @x1 (EditorCMathFormula.java:47-52).
		{"ItemMender",
			"COST=\"100+@x1\"",
			"COST=100+@x1",
			"COST=\"(100-@x3)+@x1\"",
			"!COST=100",
			"!COST=junk"},
		// ItemRefitter: a single COST= formula (Behaviors/ItemRefitter.java, read at CMParms.getParmStr(parms,\"COST\",...) ). Example arc_behaviors.ini: COST=\"100+@x1\". CMATH_FORMULA requires @x1 (EditorCMathFormula.java:47-52).
		{"ItemRefitter",
			"COST=\"100+@x1\"",
			"COST=100+@x1",
			"COST=\"(100-@x3)+@x1\"",
			"!COST=100",
			"!COST=junk"},
		// MOBHelper: max combatants, optional MSG= (Behaviors/MOBHelper.java:58-66). Examples arc_behaviors.ini: 5 MSG=\"OMG ATTACK!\" / 5 msg=\"OMG ATTACK!\".
		{"MOBHelper",
			"5",
			"5 MSG=\"OMG ATTACK!\"",
			"5 msg=\"OMG ATTACK!\"",
			"msg=\"OMG ATTACK!\"",
			"!5.5",
			"!MSG=\"x\" junk"},
		// PlayerHelper: max combatants, optional MSG= (Behaviors/PlayerHelper.java:58-66). Examples arc_behaviors.ini: 5 MSG=\"OMG ATTACK!\" / 5 msg=\"OMG ATTACK!\".
		// MOBTeacher: percentages, char classes, abilities, expertises and
		// flags in any order (Behaviors/MOBTeacher.java:246-301). All four
		// documented examples are included, plus negatives for tokens the
		// class silently ignores.
		{"MOBTeacher",
			"",
			"mage",
			"Spell_BurningHands",
			"evoker illusionist transmuter",
			"mage evoker illusionist transmuter",
			"100% mage 10% Thief_Steal NOCOMMON",
			"Spell_Sleep 5% Spell_Wish",
			"NOTEACH NOEXPS NOHLEXP",
			"50%",
			"EXPCRAFT1",
			"100% EXPCRAFT1",
			"!Spell_NoSuchSpell",
			"!50",
			"!100% %",
			"!NOCOMMONX",
			"!garbage",
			"!EXPCRAFT0"},
		{"PlayerHelper",
			"5",
			"5 MSG=\"OMG ATTACK!\"",
			"5 msg=\"OMG ATTACK!\"",
			"msg=\"OMG ATTACK!\"",
			"!5.5",
			"!MSG=\"x\" junk"},
		// PuddleMaker: one percent chance (Behaviors/PuddleMaker.java:121, read with CMath.s_int(getParms()) at Behaviors/PuddleMaker.java:115-116). Example arc_behaviors.ini: 34.
		{"PuddleMaker",
			"34",
			"0",
			"100",
			"!x",
			"!5.5"},
		// RaceHelper: max combatants, optional MSG= (Behaviors/RaceHelper.java:58-66). Examples arc_behaviors.ini: 5 MSG=\"OMG ATTACK!\" / 5 msg=\"OMG ATTACK!\".
		{"RaceHelper",
			"5",
			"5 MSG=\"OMG ATTACK!\"",
			"5 msg=\"OMG ATTACK!\"",
			"msg=\"OMG ATTACK!\"",
			"!5.5",
			"!MSG=\"x\" junk"},
		// ResetWhole: either a bare tick count or one of TICKS=/RLHOUR=/MUDHOUR= (Behaviors/ResetWhole.java:105, read at Behaviors/ResetWhole.java:72-95). Example arc_behaviors.ini: 4000.
		{"ResetWhole",
			"4000",
			"TICKS=4000",
			"RLHOUR=\"5\"",
			"MUDHOUR=\"5\"",
			"!RLHOUR=abc",
			"!MUDHOUR=x"},
		// ROMGangMember: gang name, then messages, all semicolon separated (Behaviors/ROMGangMember.java:69, parsed at Behaviors/ROMGangMember.java:57). Examples arc_behaviors.ini: Troll / Troll;I`m gonna kill you!;DIE!
		{"ROMGangMember",
			"Troll",
			"Troll;I`m gonna kill you!;I`ve got you now!;DIE!",
			"Troll;DIE!",
			"!;"},
		// VeryAggressive: inherits the Aggressive format, which owns delay= and the flags (Behaviors/VeryAggressive.java:75). Examples arc_behaviors.ini: -race +elf, mobkill misbehave -race +elf.
		{"VeryAggressive",
			"-race +elf",
			"-race +elf message=\"die! You!\"",
			"mobkill misbehave -race +elf",
			"delay=5",
			"!delay=abc",
			"!-foo +bar"},
		// WeatherAffects: key=value settings for weather effects (Behaviors/WeatherAffects.java:180, read at Behaviors/WeatherAffects.java:137-210). Note the consumer reads iceoverchance, while arc_behaviors.ini:2537 documents freezeOverChance; the format follows the consumer.
		{"WeatherAffects",
			"windsheer=10 puddlepct=20",
			"iceoverchance=50",
			"botherticks=100",
			"dmgname=\"a big ship\"",
			"shipdmgpct=\"2*@x1\"",
			"weather=RAIN",
			"!bogus=1",
			"!freezeoverchance=50",
			"!puddlepct=abc"},
		// Wimpy: delay=, very=, then a zappermask (Behaviors/Wimpy.java:79, read at Behaviors/Wimpy.java:69-72). Example arc_behaviors.ini: -race +elf.
		{"Wimpy",
			"-race +elf",
			"delay=5",
			"very=1",
			"delay=5 -race +elf",
			"!delay=abc",
			"!very=x"},
		// WimpyAggressive: inherits the Aggressive format (Behaviors/WimpyAggressive.java:83). Examples arc_behaviors.ini: -race +elf, -race +elf misbehave mobkill.
		{"WimpyAggressive",
			"-race +elf",
			"-race +elf misbehave mobkill",
			"delay=5",
			"!delay=abc"},
		// CombatAbilities: order free word list. bsize read at Behaviors/CombatAbilities.java:99,
		// combat mode at :159-175, char class names at :122-157, flags and the +-ability lists at
		// :469-503. Covers the derived Clericness/Fighterness/Mageness/Thiefness/Druidness/Bardness.
		{"CombatAbilities",
			"",
			"Cleric",
			"Fighter",
			"Cleric OFFENSIVE",
			"Cleric MIXDEFENSIVE",
			"Cleric PROFICIENT NOSTATS NOCOMBATSTAT",
			"Cleric -Spell_Sleep +Prayer_Bless",
			"Cleric MINTICKS=10 MAXTICKS=20 CHANCE=75",
			"Cleric min=10 max=20 chance=75 mingrp=2",
			"Cleric BSIZE=3",
			"!Cleric NOCOMBATSTATX",
			"!Cleric min=abc",
			"!Cleric -NoSuchAbility",
			"!Cleric BSIZE=x",
			"!Cleric fee fie foe"},
		// Clericness declares no format of its own: Behaviors/Clericness.java:35 extends
		// CombatAbilities and its makeClass() call at :59 pins the class to Cleric, so these
		// rows assert the inherited format is really reachable through a derived behavior.
		// The values are the published examples in resources/help/arc_behaviors.ini
		// (BEHAVIOR_CLERICNESS) wherever this distribution can satisfy them.
		{"Clericness",
			"",
			"Templar",
			"Templar OFFENSIVE",
			"Templar MIXEDDEFENSIVE",
			"Templar PROFICIENT NOSTATS NOCOMBATSTAT",
			"Templar -Spell_Sleep +Prayer_Bless",
			"Templar MINTICKS=10 MAXTICKS=20 CHANCE=75",
			"Templar min=10 max=20 chance=75 mingrp=2",
			"Templar BSIZE=3",
			"!Doomsayer OFFENSIVE",
			"!Templar NOCOMBATSTATX",
			"!Templar min=abc",
			"!Templar fee fie foe"},
		// Mobile: ActiveTicker ticker keys (Behaviors/ActiveTicker.java:74-82), then LEASH= at
		// Behaviors/Mobile.java:186, the WANDER/OPENDOORS flags at :191-195, and +- locale words
		// at :197-241, where the domain name may be abbreviated because the reader matches with
		// DOMAIN_*_DESCS[i].startsWith(s) -- hence LOCALE_DOMAIN_PREFIX, not LOCALE_DOMAIN.
		{"Mobile",
			"",
			"min=10 max=20 chance=75 wander -air -underwater",
			"min=10 max=20 chance=75 opendoors",
			"min=10 max=20 chance=75 leash=3 opendoors",
			"wander",
			"leash=5",
			"+all",
			"-all +city",
			"-under",
			"!leash=x",
			"!-nosuchlocale",
			"!wandle",
			"!+",
			"!min=10 max=20 chance=75 -air fee"},
		// MobileAggressive: one string read by four classes -- ActiveTicker via Mobile,
		// Mobile.java, MobileAggressive.java:83-94, and Aggressive through the veryA
		// delegate. The locale words are stripped before the mask run is taken
		// (MobileAggressive.java:92-93), so -underwater/-air are locales, while
		// -thief +elf and -elf stay in the mask.
		{"MobileAggressive",
			"",
			"min=10 max=20 chance=75 -underwater -thief +elf",
			"min=10 max=20 chance=75 wander -air -elf",
			"min=10 max=20 chance=75 message=\"aha!\" -air -elf",
			"min=10 max=20 chance=75 mobkill misbehave",
			"leash=3 opendoors",
			"min=10 max=20 chance=75 +city -under",
			"BRIBE=\"5,100,2000\" BRIBEPAYMSG=\"take this\" BRIBEPAIDMSG=\"now go\"",
			"min=10 max=20 chance=75 delay=5 message=\"die! You!\" mobkill misbehave -elf +fighter",
			"!delay=abc",
			"!min=10 max=20 chance=75 -nosuchlocale",
			"!min=10 max=20 chance=75 mobkills",
			"!BRIBE=abc",
			"!+",
			"!min=10 max=20 chance=75 -air fee"},
		// Sounder: Sounder.java:81-120. Every ;-separated field is one trigger; a field
		// is either the min/max ticker keys (first field only, since the consumer
		// drops field 0 once either key is found), a SOUND chance plus emote, or a
		// trigger code plus emote. The emote is freeform up to the next ;, so an
		// emote may not contain one, and an empty emote is rejected.
		{"Sounder",
			"",
			"sound 80 wiggles his bottom.;sound 60 smiles evilly.;sound 20 burps!",
			"min=20 max=20;wear $p looks good on you.;remove Now you don't look so good.;remove_room $n doesn't look so good.",
			"sound 50 burps",
			"get A ghostly hand takes it.",
			"portal_enter_room $n arrives in a swirl of mist.",
			"min=5",
			"max=5",
			"!sound eighty burps",
			"!wibble wibble",
			"!get",
			"!sound 50",
			"!min=abc max=20"},
		// StockMarket: setParms:1474-1479 -> configure():1437-1468. Space-delimited KEY=value
		// parms through CMParms.parseEQParms, so the order is free; empty parms means the
		// STOCK preset.  CONFIGS= holds comma-separated property-file paths, and those files
		// are a separate Properties surface which this format does not cover.
		{"StockMarket",
			"",
			"JOURNAL=\""+TEST_JOURNAL+"\" MARKETTYPE=STOCK",
			"CONFIGS=\"/resources/config1.txt,/resources/config2.txt\"",
			"MARKETTYPE=BOND",
			"NAME=\"Stock in @x1 of @x2\"",
			"AREAEVENTS=\"+revolt,-celebration\"",
			"GROUPBY=ALL AREAMASK=\"-EFFECTS +Arrest +EFFECTS -Conquerable\"",
			"ALLOWCLANS=TRUE SHOPMASK=\"-NPC\" GROUPBY=NOTHING NAME=\"Clan Stock: @x1 of @x2\" PLAYINFLU=FALSE",
			"UPDATEDAYS=24 WAITDAYAB=10 MAXSTOCKS=10 MAXSHARES=25",
			"groupby=race",
			"!MARKETTYPE=BOGUS",
			"!GROUPBY=NOPE",
			"!UPDATEDAYS=abc",
			"!ALLOWCLANS=perhaps",
			"!MAXSTOCKS=2.5",
			"!NAME=Stock in @x1 of @x2"},
		// MudChat: setParms:327-380. A leading + means the whole string is bonus chat data;
		// otherwise the first = splits data file from the rest, and the first + in that rest
		// splits the chat group from bonus data.  A bare string with no = is a chat group
		// name (MudChat.java:352-353), which is why the final branch accepts anything and
		// this row cannot carry a negative: every non-empty value is a legal group name.
		// MovingRoom: an optional ticker group, ';' or '/', the section name in
		// resources/movingroom.xml, then the two tick numbers the class writes
		// but never reads (Behaviors/MovingRoom.java:96-122). The documented
		// example is usable as written because movingroom.xml has a SUBWAY block.
		{"MovingRoom",
			"min=3 max=3 chance=100;SUBWAY;3;10",
			"SUBWAY",
			"SUBWAY;3",
			"SUBWAY;;10",
			"SUBWAY;3;10",
			"min=10 max=20 chance=75 mingrp=2;SUBWAY;5;5",
			"min=3/SUBWAY",
			"!",
			"!min=x;SUBWAY",
			"!SUBWAY;x",
			"!SUBWAY;1;2;3"},
		{"MudChat",
			"",
			"smurf",
			"chat.dat=smurf",
			"chat.dat=smurf+(smurfy);9Smurfy!",
			"mychat.dat=cityguard",
			"mychat.dat=",
			"+(here there);9neither;5well, where?;1nowhere",
			"dog fido beastly fido_dog"},
		// Nanny: setParms:810-845 reads RATE as a double, NAME as a string, and WATCHES as a
		// comma list of the eight types it writes back itself at getParms:762-807 (Babies,
		// Children, Mounts, Wagons, Cars, Boats, AirCars, Followers), matching each element
		// by its first three letters (:826-841).  Everything after |~| is machine-written
		// drop-off XML (:813, :860-863), so the tail is only checked as well-formed XML.
		{"Nanny",
			"",
			"rate=2.5 name=\"The Stables\" watches=\"mounts,wagons\"",
			"RATE=2 NAME=\"Nursery\" WATCHES=\"Babies,Children\"",
			"WATCHES=\"Babies,Children,Mounts,Wagons,Cars,Boats,AirCars,Followers\"",
			"WATCHES=\"Mounts,Wagons,\"",
			"WATCHES=\"bab\"",
			"NAME=\"The Stables\"",
			"rate=2.5 name=\"The Stables\" watches=\"mounts,wagons,\" |~| <DROP><ENAM>Bob</ENAM><ONAM>Mary</ONAM><TIME>5</TIME></DROP>",
			"!WATCHES=\"babies,chickens\"",
			"!RATE=abc",
			"!NAME=The Stables"},
	};

	/**
	 * Every behavior that implements parmsFormat(), as of this test. Each
	 * one must return a format which parses, and which is not the undefined
	 * default. This list must be updated in step with Behaviors/, so that a
	 * new format owner cannot arrive without tests. Behaviors which merely
	 * inherit a format are listed too, so that the inheritance itself is
	 * asserted rather than assumed (Clericness inherits CombatAbilities).
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
		"Clericness",
		"CombatAbilities",
		"CommonSpeaker",
		"Conquerable",
		"CorpseEater",
		"Decay",
		"EvilExecutioner",
		"Emoter",
		"FieryRoom",
		"Follower",
		"GateGuard",
		"GoodExecutioner",
		"InstantDeath",
		"FaithHelper",
		"Guard",
		"Hireling",
		"ItemIdentifier",
		"ItemMender",
		"ItemRefitter",
		"MOBTeacher",
		"MOBHelper",
		"Mobile",
		"MobileAggressive",
		"MovingRoom",
		"MudChat",
		"Nanny",
		"PlayerHelper",
		"PuddleMaker",
		"RaceHelper",
		"ResetWhole",
		"ROMGangMember",
		"Sounder",
		"StockMarket",
		"VeryAggressive",
		"WeatherAffects",
		"Wimpy",
		"WimpyAggressive"
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

	private static Clan makeTestClan()
	{
		final Clan C=(Clan)CMClass.getCommon("DefaultClan");
		C.setName(TEST_CLAN);
		C.setGovernmentID(CMLib.clans().getDefaultGovernment().getID());
		C.setStatus(Clan.CLANSTATUS_PENDING);
		return C;
	}

	private static String createFixtures(final List<Object> fixtures)
	{
		// an interrupted earlier run can leave its fixtures behind, so clear
		// them first; otherwise the next run dies on a duplicate key
		try
		{
			CMLib.database().DBDeleteJournal(TEST_JOURNAL,null);
		}
		catch(final Exception e)
		{
			Log.errOut("Validparms",e);
		}
		try
		{
			CMLib.database().DBWriteJournal(TEST_JOURNAL,"Validparms","Validparms","Fixture","Created by the Validparms test.");
			fixtures.add(TEST_JOURNAL);
		}
		catch(final Exception e)
		{
			return "Error: could not write the test journal: " + e.getMessage();
		}
		final List<String> journals=CMLib.database().DBReadJournals();
		if((journals==null)||(!journals.contains(TEST_JOURNAL)))
			return "Error: the test journal '" + TEST_JOURNAL + "' was not readable after it was written";
		// destroyClan does not always clear the stored row, so a leftover from
		// an interrupted run has to be removed explicitly before recreating
		try
		{
			Clan stale=CMLib.clans().getClan(TEST_CLAN);
			if(stale==null)
				stale=CMLib.clans().findClan(TEST_CLAN);
			if(stale!=null)
			{
				stale.destroyClan();
				CMLib.database().DBDeleteClan(stale);
			}
		}
		catch(final Exception e)
		{
			Log.errOut("Validparms",e);
		}
		try
		{
			Clan C=makeTestClan();
			try
			{
				C.create();
			}
			catch(final Exception e)
			{
				final Clan stale=CMLib.clans().findClan(TEST_CLAN);
				if(stale==null)
					throw e;
				CMLib.clans().removeClan(stale);
				C=makeTestClan();
				C.create();
			}
			fixtures.add(C);
		}
		catch(final Exception e)
		{
			return "Error: could not create the test clan: " + e.getMessage();
		}
		if(CMLib.clans().getClan(TEST_CLAN)==null)
			return "Error: the test clan '" + TEST_CLAN + "' was not readable after it was created";
		for(final String deityName : TEST_DEITIES)
		{
			try
			{
				final Deity stale=CMLib.map().getDeity(deityName);
				if(stale!=null)
					CMLib.map().registerWorldObjectDestroyed(null,null,stale);
				final Deity D=(Deity)CMClass.getMOB("StdDeity");
				if(D==null)
					return "Error: StdDeity could not be instantiated";
				D.setName(deityName);
				CMLib.map().registerWorldObjectLoaded(null,null,D);
				fixtures.add(D);
			}
			catch(final Exception e)
			{
				return "Error: could not create the test deity '" + deityName + "': " + e.getMessage();
			}
			if(CMLib.map().getDeity(deityName)==null)
				return "Error: the test deity '" + deityName + "' was not readable after it was created";
		}
		return null;
	}

	private static void destroyFixtures(final List<Object> fixtures)
	{
		for(int i=fixtures.size()-1;i>=0;i--)
		{
			final Object o=fixtures.get(i);
			try
			{
				if(o instanceof Clan)
				{
					((Clan)o).destroyClan();
					CMLib.database().DBDeleteClan((Clan)o);
				}
				else
				if(o instanceof Deity)
					CMLib.map().registerWorldObjectDestroyed(null,null,(Deity)o);
				else
					CMLib.database().DBDeleteJournal(String.valueOf(o),null);
			}
			catch(final Exception e)
			{
				Log.errOut("Validparms","Could not remove the test fixture " + o + ": " + e.getMessage());
			}
		}
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
		if((mob != null) && (mob.location() != null))
			TEST_ROOM_ID = CMLib.map().getExtendedRoomID(mob.location());
		final List<Object> fixtures = new ArrayList<Object>();
		try
		{
			final String fixtureError = createFixtures(fixtures);
			if(fixtureError != null)
				return fixtureError;
			return runTests();
		}
		finally
		{
			destroyFixtures(fixtures);
		}
	}

	private String runTests()
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
				if(TEST_ROOM_ID != null)
					val = CMStrings.replaceAll(val, "*ROOMID*", TEST_ROOM_ID);
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
