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
	 * The name of the area the test runs in, substituted for *AREA* in the rows.
	 * LinkedWeather confirms its area name against the live map, so its rows
	 * need an area that actually exists in the world being tested.
	 */
	private static String TEST_AREA = null;

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
		// CombatAssister: the whole parms string is one zappermask naming the
		// mobs this mob will defend (Behaviors/CombatAssister.java:47 describes
		// it, :76 tests it). Empty parms means "defend anyone". Examples
		// arc_behaviors.ini: -RACE +Elf / -NAME "+My Buddies" / -CLANS "+My Clan".
		{"CombatAssister",
			"",
			"-RACE +Elf",
			"+Elf -RACE",
			"-NAME \"+My Buddies\"",
			"-CLANS \"+My Clan\"",
			"-race +elf -class +mage",
			"!nosuchmaskcode"},
		{"CommonSpeaker",
			"Common",
			"Orcish",
			"Elvish",
			"",
			"!NoSuchLanguage"},
		// Conquerable: JOURNAL=, LAW=, OWNERSHIP=, LOYALTY=, LEVELDIFF=
		// (Behaviors/Conquerable.java:103)
		{"Concierge",
			"1.5",
			"*ROOMID*; =100 ; AREAONLY=false; MAXRANGE=100",
			"",
			"=70",
			"70",
			"*ROOMID*",
			"AREAONLY=true",
			"MAXRANGE=100",
			"PERROOM=1.5",
			"!AREAONLY=perhaps",
			"!MAXRANGE=x",
			"!PERROOM=x"},
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
			"-race +dwarf",
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
		{"DelayedTransporter",
			"",
			"min=10 max=20 chance=25;*ROOMID*;*ROOMID*;*ROOMID*",
			"min=10 max=20 chance=25;*ROOMID*",
			";*ROOMID*",
			"!*ROOMID*",
			"!min=10 max=20 chance=25;NoSuchRoom#9999",
			"!min=abc;*ROOMID*"},
		// DoorwayGuardian: the NOSNEAK/ALWAYS flags and the doored direction
		// words in any order, then an optional zappermask, then ';' and the
		// refusal message (Behaviors/DoorwayGuardian.java:63). The three
		// documented examples at arc_behaviors.ini:381-383 are here in full.
		// A mask written before the flags swallows them, exactly as the setter
		// does, so the reversed spelling is a positive too.
		{"DoorwayGuardian",
			"always north",
			"north always",
			"n",
			"nosneak east -class +mage -race +elf -evil",
			"-class +mage -race +elf -evil nosneak east",
			"\"-race +elf\"",
			"east;",
			"nosneak up;Go away!",
			"east north;<S-NAME> smack(s) <T-NAME> and push(es) <T-HIM-HER> back!",
			"",
			// the mask slot is only checked when a ';' ends it, so a bad mask
			// has to carry one to be caught
			"!bogus;",
			"!bogus direction;",
			"!north=1;",
			"!nosneak east=2;"},
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
			"inroom=\"0,0\";x!"},
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
		// FasterRecovery: TYPE=amount[/skip ticks] pairs in any order, the type
		// being the RecType enum names (Behaviors/FasterRecovery.java:68), the
		// amount optionally signed and optionally a percent, the ticks
		// optional. All six documented examples at arc_behaviors.ini:51-56 are
		// here in full. Fractional amounts and a trailing '/' are spellings the
		// setter tolerates (CMath.s_int truncates, ticks become 0), so they are
		// negatives with that reason.
		{"FasterRecovery",
			"burst=2",
			"health=5",
			"health=50%",
			"hits=2/5 mana=50% move=50%/1",
			"mana=+5 hits=+10",
			"mana=-5",
			"burst=2 health=5",
			"!bogus=2",
			"!burst",
			"!burst=abc",
			"!burst=2/",
			"!health=5.5"},
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
		// ItemIdentifier: a single COST= formula (Behaviors/ItemIdentifier.java, read at CMParms.getParmStr(parms,\"COST\",...) ). Example arc_behaviors.ini: COST=\"100+@x1\".
		{"ItemGenerator",
			"",
			"min=10 max=20 chance=75 minitems=1 maxitems=10 maxdups=0;-LEVELS +>10",
			"minitems=10 maxitems=20 MOBS -ALL +CITY;-MATERIAL +IRON +BRONZE",
			"minitems=10 maxitems=20 enchanted=20;-NAME \"+Short Sword\"",
			"mobs",
			"minitems=5",
			"minitems=2 maxitems=3 maxdups=1 enchanted=50",
			"+all",
			"-all +city",
			"-under",
			"!minitems=abc",
			"!enchanted=x",
			"!-nosuchlocale",
			"!mobsz",
			"!minitems=1;-nosuchmaskcode"},
		{"ItemIdentifier",
			"COST=\"100+@x1\"",
			"COST=100+@x1",
			"COST=\"(100-@x3)+@x1\"",
			"COST=100",
			"!COST=junk"},
		// ItemMender: a single COST= formula (Behaviors/ItemMender.java, read at CMParms.getParmStr(parms,\"COST\",...) ). Example arc_behaviors.ini: COST=\"100+@x1\".
		{"ItemMender",
			"COST=\"100+@x1\"",
			"COST=100+@x1",
			"COST=\"(100-@x3)+@x1\"",
			"COST=100",
			"!COST=junk"},
		// ItemRefitter: a single COST= formula (Behaviors/ItemRefitter.java, read at CMParms.getParmStr(parms,\"COST\",...) ). Example arc_behaviors.ini: COST=\"100+@x1\".
		{"ItemRefitter",
			"COST=\"100+@x1\"",
			"COST=100+@x1",
			"COST=\"(100-@x3)+@x1\"",
			"COST=100",
			"!COST=junk"},
		// LinkedWeather: an area name confirmed against the live map, then an
		// optional ;ROLLING tail (Behaviors/LinkedWeather.java:75-91; the area
		// lookup is EditorAreaName.java:69-74). The area always comes first, so
		// the pair has only one order. *AREA* is the area of the room the test
		// runs in. Examples arc_behaviors.ini: My Area / My Area;ROLLING.
		{"LinkedWeather",
			"",
			"*AREA*",
			"*AREA*;ROLLING",
			"*AREA*;rolling",
			"!*AREA*;NOSUCHFLAG",
			"!NoSuchAreaForValidparms",
			"!ROLLING"},
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
		{"MOBEater",
			"",
			"min=4 max=8 chance=5 acidpct=50",
			"acidpct=10",
			"min=4 max=8 chance=5",
			"!acidpct=x",
			"!min=abc"},
		{"MOBHunter",
			"",
			"min=600 max=1200 chance=99 radius=50 -RACE +Human",
			"min=50 max=1200 chance=75 -NAME \"+Evil Wizards\"",
			"radius=5",
			"-RACE +Human",
			"min=600 max=1200 chance=99 radius=50",
			"mingrp=2 radius=10 -RACE +HUMAN",
			"!radius=x",
			"!min=abc",
			"!radius=50 -nosuchmaskcode"},
		{"MOBReSave",
			"",
			"min=140 max=140 chance=100",
			"STRENGTH=25",
			"min=140 max=140 chance=100 STRENGTH=25 DEXTERITY=10",
			"MAXSTRENGTH=25",
			"!nosuchstat=5",
			"!maxstrength=abc",
			"!min=abc"},
		{"NastyAbilities",
			"",
			"min=10 max=20 chance=75",
			"min=10 max=20 chance=75 fightok",
			"min=1 fightok ; -RACE +ELF",
			"checklevel",
			"min=10 fightok; -RACE +ELF",
			"; -RACE +ELF",
			"!bogus",
			"!min=abc",
			"!min=1 fightok; -nosuchmaskcode"},
		{"NeutralAbilities",
			"",
			"min=10 max=20 chance=75",
			"min=1 ; -RACE +ELF",
			"skills=\"Prayer_Bless,Spell_Sleep\"",
			"skills=Prayer_Bless",
			"min=10 skills=\"Prayer_Bless,Spell_Sleep\"; -RACE +ELF",
			"!skills=\"Prayer_Bless,NoSuchAbility\"",
			"!fightok",
			"!min=abc"},
		{"NiceAbilities",
			"",
			"min=10 max=20 chance=75",
			"min=10 ; -RACE +ELF",
			"; -RACE +ELF",
			"min=10 max=20 chance=75; -RACE +ELF",
			"!bogus",
			"!min=abc",
			"!min=10; -nosuchmaskcode"},
		{"ObjectGuardian",
			"",
			"SENTINAL",
			"SENTINEL",
			"sentinal dropok",
			"itemmask=\"-NAME +sword\" mobmask=\"-RACE +ELF\" sentinal",
			"itemmask=\"-NAME +sword\"",
			"!sentinalx",
			"!dropokx",
			"!itemmask=-nosuchmaskcode",
			"!bogus"},
		// Patroller: the documented examples at arc_behaviors.ini:1215-1218, in
		// full. Every word before the first ';' is a ticker parm, RIDEOK flag or
		// DIAMETER, and the rest is the route, so route steps are single words.
		{"Patroller",
			"min=10 max=20 chance=75 rideok=true;Midgaard#3001;#3002;#3003;RESTART",
			"min=10 max=20 chance=99 rideok=only;Midgaard#3001;#3002;#3003;RESTART",
			"min=10 max=20 chance=15;east;.;north;north;east",
			"min=10 max=20 chance=15 diameter=100;Midgaard#1;New Thalos#10;.;.;restart",
			// An empty route is accepted and simply never moves the mob
			"",
			// No negative is possible: the class treats every word as a parm and
			// ignores the ones it does not recognize, so its format accepts any text
			// (even a trailing ';', since ';' is just another word character)
			""},
		// ResourceOverride: the documented examples at arc_behaviors.ini:1433-1435.
		// setParms (ResourceOverride.java:113-215) accepts every whitespace-delimited
		// word: a resource code, a material name, a room type, a quoted
		// "name(RESOURCE SUBTYPE)" custom name, MIN/MAX/CHANCE, or MOBMASK= and a
		// mask.  Unknown words are logged, never rejected, so there are no
		// negatives for this behavior.
		{"ResourceOverride",
			"MIN=5 MAX=5 CHANCE=100 CORN WHEAT RICE",
			"CORN WHEAT RICE ROCKY MOUNTAINS PLAINS",
			"CORN \"some weird corn(CORN WIERDCORN)\" \"CORN WIERDCORN\"",
			"WOOD METAL COR CORNS CORN none 5",
			"MOBMASK=-RACE +ELF",
			"MOBMASK=\"-RACE +ELF\"",
			"",
			// setParms logs and drops an unknown resource word (ResourceOverride.java:205)
			"!BOGUSRESOURCE",
			"!NOTARESOURCE"},
		{"PokerDealer",
			"",
			"game=\"Draw Poker\" minplayers=4 anti=5.0",
			"game=\"Straight Poker\"",
			"game=\"Draw Poker\" anti=5.5",
			"game=0",
			"minplayers=3",
			"anti=2.5",
			"!game=99",
			"!game=\"No Such Game\"",
			"!anti=abc",
			"!minplayers=x"},
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
		{"ProtectedCitizen",
			"",
			"min=1 max=5 chance=25 radius=7 maxassists=1;-NAME +cityguard;HELP ME!!!;Save me!",
			"WANDER radius=20;-NAME +cityguard;HELP ME!!!;,screams for his life!",
			";cityguard",
			"radius=7",
			"wander",
			"min=1 max=5 chance=25 radius=7 maxassists=1 ; -NAME +cityguard ; Help me!",
			"!radius=x",
			"!min=abc",
			"!maxassists=x"},
		{"ProtectedCitizens",
			"",
			"radius=5 maxassists=7;-NAME +citizen;-NAME +cityguard;HELP ME!!!;Save me!",
			"; -NAME +citizen",
			"radius=5 maxassists=7;-NAME +citizen",
			"min=10 max=20 chance=75 ; -RACE +ELF ; -RACE +HUMAN ; Help me! ; Save me!",
			"!radius=10 maxassists=2;-NAME +citizen;HELP ME!!!;,screams for his life!",
			"!radius=x",
			"!min=abc",
			"!; -nosuchmaskcode"},
		{"RaceHelper",
			"5",
			"5 MSG=\"OMG ATTACK!\"",
			"5 msg=\"OMG ATTACK!\"",
			"msg=\"OMG ATTACK!\"",
			"!5.5",
			"!MSG=\"x\" junk"},
		// RandomItems: setParms reads the MOBS and locale flags only from inside
		// the if(x>=0) at Behaviors/RandomItems.java:111, so those words do nothing
		// unless a semicolon is present, and everything after that semicolon is the
		// item file or the pasted <ITEMS> xml (Behaviors/RandomItems.java:95-101).
		// The format is therefore two branches, and the flag words are negatives
		// without a semicolon. Examples arc_behaviors.ini: myitems.cmare after a
		// semicolon, MOBS -ALL +CITY before one, and a bare <ITEMS> xml after one.
		{"RandomItems",
			"",
			"min=10 max=20 chance=75 minitems=1 maxitems=10;myitems.cmare",
			"minitems=10 maxitems=20 MOBS -ALL +CITY;stuff.cmare",
			"minitems=10 maxitems=20 ;<ITEMS><ITEM>..etc..",
			"minitems=10 maxitems=20 ;<ITEMS><ITEM NAME=\"gem slot\"></ITEM></ITEMS>",
			";myitems.cmare",
			"mobs;myitems.cmare",
			"minitems=5",
			"minitems=5;",
			"+all -under;myitems.cmare",
			";myitems.cmare;-ALL +CITY",
			"chance=50 mingrp=2 minitems=3 maxitems=4;",
			"!minitems=abc",
			"!maxitems=x",
			"!-nosuchlocale;myitems.cmare",
			// without a semicolon the code reads none of these flags at all
			"!mobs",
			"!+all -under",
			"!minitems=10 maxitems=20 MOBS -ALL +CITY"},
		// RandomMonsters: the same gate (Behaviors/RandomMonsters.java:109) and the
		// same <MOBS> xml tail, with the locale flags as its only flag words.
		{"RandomMonsters",
			"",
			"min=10 max=20 chance=75 minmonsters=1 maxmonsters=10;monsters.cmare",
			"minmonsters=10 maxmonsters=20 -ALL +CITY;criminals.cmare",
			"minmonsters=10 maxmonsters=20 ;<MOBS><MOB>..etc..",
			";monsters.cmare",
			"minmonsters=5",
			"minmonsters=5;",
			"+all -under;monsters.cmare",
			";monsters.cmare;-ALL +CITY",
			"!minmonsters=abc",
			"!maxmonsters=x",
			"!-nosuchlocale;monsters.cmare",
			// without a semicolon the code reads none of these flags at all
			"!+all -under",
			"!minmonsters=10 maxmonsters=20 -ALL +CITY"},
		{"RandomQuests",
			"",
			"minquests=3 maxquests=5 template=normal_capture1",
			"min=10 max=20 chance=75",
			"path=/randareas/example.xml tagid=all_quests",
			"expire=\"3 hours\"",
			"inline=true",
			"maxattempts=5",
			"template=normal_capture1 num=5",
			// the generic VAR=value branch accepts any non-empty word, so an
			// empty value is what this format can actually reject
			"!minquests=",
			"!inline=",
			"!foo"},
		{"RandomTeleporter",
			"",
			"min=10 max=20 chance=75 -air -underwater",
			"min=10 max=20 chance=75 nowander POOFIN=\"<S-NAME> Arrives!\"",
			"nowander",
			"poofin=\"Arrives!\" poofout=\"Leaves!\"",
			"+all",
			"-all +city",
			"poofin=\"\"",
			"!min=abc",
			"!poofin",
			"!-nosuchlocale"},
		// RandomTraps: the ticker keys, mintraps and maxtraps are read from the
		// parms as a whole, but ROOMS=, ITEMS=, EXITS= and the +/- locale words are
		// read only from the text BEFORE the first semicolon, because the split at
		// RandomTraps.java:109 gates all of them (setParms :109-243). So the format
		// has two branches: the keys up to a semicolon, which may be followed by
		// anything at all because nothing after it is read, or just the ticker keys
		// and the trap counts. Both arc_behaviors.ini examples end with the
		// semicolon, as the help now says, and the same text without one is kept as
		// a negative so that the silent-ignore trap cannot come back. The second
		// example used to end +CityStreet, which is not a Room.DOMAIN_* desc prefix
		// (Locales/interfaces/Room.java:110-167) and so was ignored by the code; it
		// is now +CITY in the help, and the bogus word is a negative here.
		{"RandomTraps",
			"",
			"min=10 max=20 chance=25 mintraps=1 maxtraps=10",
			"min=10 max=20 chance=25 mintraps=1 maxtraps=10 EXITS=\"YES\" ITEMS=\"YES\" ROOMS=\"YES\";",
			"min=10 max=20 chance=25 ROOMS=\"YES\" ITEMS=\"NO\" -ALL +CITY;",
			"EXITS=NO ITEMS=CONT ROOMS=YES mintraps=2 maxtraps=5 min=10 max=20 chance=50;",
			"maxtraps=3 mintraps=3",
			"maxtraps=3 mintraps=3;",
			"+JUNGLE +AIR;",
			"+AIR +JUNGLE;",
			"chance=50 mintraps=2 +AIR EXITS=DOORS maxtraps=4 -SWAMP min=5;",
			"ITEMS=LOCK EXITS=LOCKED;",
			"min=10;",
			"min=10;anything at all here is ignored",
			"ROOMS=Y ITEMS=NO EXITS=NO;",
			"ITEMS=LID EXITS=DOORS -ALL +CIT;",
			"+ALL;",
			"-ALL;",
			"MINTRAPS=2;",
			"min=10;;",
			"!ITEMS=NOSUCH;",
			"!ROOMS=MAYBE;",
			"!EXITS=NOSUCH;",
			"!mintraps=x;",
			"!min=abc",
			"!-nosuchlocale;",
			"!min=10 max=20 chance=25 ROOMS=\"YES\" ITEMS=\"NO\" -ALL +CityStreet;",
			// without a semicolon the code reads none of these keys at all
			"!min=10 max=20 ROOMS=YES",
			"!ROOMS=\"YES\"",
			"!+JUNGLE +AIR",
			"!min=10 max=20 chance=25 mintraps=1 maxtraps=10 EXITS=\"YES\" ITEMS=\"YES\" ROOMS=\"YES\""},
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
			"Troll;DIE!"},
		// VeryAggressive: inherits the Aggressive format, which owns delay= and the flags (Behaviors/VeryAggressive.java:75). Examples arc_behaviors.ini: -race +elf, mobkill misbehave -race +elf.
		{"TaxCollector",
			"",
			"WAIT=60000 GRACE=600000",
			"wait=60000",
			"grace=600000",
			"WAIT=1 GRACE=2",
			"!WAIT=abc",
			"!GRACE=1.5",
			"!bogus=1"},
		{"TaxiBehavior",
			"1.5",
			"*ROOMID*=1.5; 70 ; AREAONLY=false; MAXRANGE=100",
			"",
			"*ROOMID*",
			"!MAXRANGE=x"},
		{"VeryAggressive",
			"-race +elf",
			"-race +elf message=\"die! You!\"",
			"mobkill misbehave -race +elf",
			"delay=5",
			"!delay=abc",
			"!-foo +bar"},
		// WeatherAffects: key=value settings for weather effects (Behaviors/WeatherAffects.java:180, read at Behaviors/WeatherAffects.java:137-210). Note the consumer reads iceoverchance, while arc_behaviors.ini:2537 documents freezeOverChance; the format follows the consumer.
		{"WaterCurrents",
			"",
			"min=1 max=3 chance=99 go=east south",
			"east=true south=true inmsg=\"<S-NAME> enter(s) from @x1.\"",
			"boats",
			"go=east",
			"go=\"east south\"",
			"inmsg=\"swept in\" outmsg=\"swept away\"",
			"min=1 max=3 chance=99 go=\"e south\" boats",
			"!go=nosuchdir",
			"!nosuchdir=true",
			"!min=abc",
			"!eastnorth=true"},
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
			"Cleric MIXEDDEFENSIVE",
			"Cleric PROFICIENT NOSTATS NOCOMBATSTAT",
			"Cleric -Spell_Sleep +Prayer_Bless",
			"+Skill_CrowsNest",
			"!+Skill_NoSuchSkill",
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
			"!NoSuchClass OFFENSIVE",
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
		// MoneyChanger: CUT=, DEFAULT=, SPACEMAXCUT=, SPACEMAXDISTANCE= plus one
		// currency=rate pair per currency, in any order
		// (Behaviors/MoneyChanger.java:150); every value is a percent. The
		// documented examples at arc_behaviors.ini:1023 and :1025 are here in
		// full. arc_behaviors.ini:1024 is deliberately left out: its INGITS key
		// is not a currency in a stock install (CMLib.beanCounter resolves
		// currency names case-insensitively and returns null for an unknown
		// code), and the example is recorded nowhere else in the tree.
		{"MoneyChanger",
			"",
			"cut=5",
			"default=0 GOLD=5",
			"cut=5 spacemaxcut=90 spacemaxdistance=35",
			"spacemaxdistance=35 cut=5 spacemaxcut=90",
			"default=2.5",
			"gold=3",
			"!cut",
			"!cut=abc",
			"!GLOD=5",
			"!delete=5"},
		// Sounder: Sounder.java:81-120. Every ;-separated field is one trigger; a field
		// is either the min/max ticker keys (first field only, since the consumer
		// drops field 0 once either key is found), a SOUND chance plus emote, or a
		// trigger code plus emote. The emote is freeform up to the next ;, so an
		// emote may not contain one, and an empty emote is rejected.
		{"Sailor",
			"",
			"tickdelay=10 peacemover=true areaonly=false",
			"TICKDELAY=10",
			"tickbonus=5",
			"defender=true boarder=true fightmover=true fighttech=true",
			"aggro=true aggromobs=true aggrolevelcheck=true aggromask=\"-race +elf\"",
			"aggromask=\"-elf +fighter\" wimpy=false",
			"!tickdelay=abc",
			"!tickdelay=1.5",
			"!peacemover=perhaps",
			"!bogus=true"},
		// Scriptable and ScriptableEverymob take script text or a LOAD=file
		// reference (arc_behaviors.ini:1499-1500 and :1912-1913), so their format
		// is a single all-consuming string and every value is legal, including
		// none.
		{"Scriptable",
			"LOAD=progs/mrinfo.script~",
			"say Hello from a script~",
			""},
		{"ScriptableEverymob",
			"LOAD=progs/mrinfo.script~",
			"if(name().length()>0) say Hi~",
			""},
		{"Scavenger",
			"",
			"min=10 max=20 chance=75",
			"min=10 max=20 chance=75 trash=\"no\"",
			"min=10 max=20 chance=75 trash=\"*ROOMID*\"",
			"min=10 max=20 chance=75 ; -JAVACLASS +GENWEAPON",
			"trash=\"\"",
			"atnight=true",
			"min=10 max=20 chance=75 trash=\"*ROOMID*\" atnight=true; -JAVACLASS +GENWEAPON",
			"!trash=\"NoSuchRoom#9999\"",
			"!atnight=perhaps",
			"!min=abc",
			"!trash=\"no\" ; -nosuchmaskcode"},
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
		// MovingRoom's setter takes the text before the first ';' (or '/') as the
		// ticker parms and the rest as room info, ignoring unparsable parms, so
		// even nonsense keys and an empty value are accepted input.
		{"MovingRoom",
			"min=3;SUBWAY;3;10",
			"min=3 max=3;SUBWAY;3;10",
			"min=3 max=3 chance=100;SUBWAY;3;10",
			"SUBWAY",
			"SUBWAY;3",
			"SUBWAY;;10",
			"SUBWAY;3;10",
			"min=10 max=20 chance=75 mingrp=2;SUBWAY;5;5",
			"min=3/SUBWAY",
			"",
			"min=x;SUBWAY",
			"SUBWAY;x",
			"SUBWAY;1;2;3;4"},
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
			"WATCHES=\"mounts,wagons,\"",
			"WATCHES=\"bab\"",
			"NAME=\"The Stables\"",
			"rate=2.5 name=\"The Stables\" watches=\"mounts,wagons,\" |~| <DROP><ENAM>Bob</ENAM><ONAM>Mary</ONAM><TIME>5</TIME></DROP>",
			"!WATCH",
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
		"CombatAssister",
		"CommonSpeaker",
		"Concierge",
		"Conquerable",
		"CorpseEater",
		"Decay",
		"EvilExecutioner",
		"DelayedTransporter",
		"DoorwayGuardian",
		"Emoter",
		"FieryRoom",
		"Follower",
		"GateGuard",
		"GoodExecutioner",
		"InstantDeath",
		"FaithHelper",
		"FasterRecovery",
		"Guard",
		"Hireling",
		"ItemGenerator",
		"ItemIdentifier",
		"ItemMender",
		"ItemRefitter",
		"LinkedWeather",
		"MOBTeacher",
		"MOBHelper",
		"Mobile",
		"MobileAggressive",
		"MoneyChanger",
		"MovingRoom",
		"MudChat",
		"Nanny",
		"MOBEater",
		"MOBHunter",
		"MOBReSave",
		"NastyAbilities",
		"NeutralAbilities",
		"NiceAbilities",
		"ObjectGuardian",
		"Patroller",
		"PokerDealer",
		"PlayerHelper",
		"PuddleMaker",
		"ProtectedCitizen",
		"ProtectedCitizens",
		"RaceHelper",
		"ResourceOverride",
		"RandomItems",
		"RandomMonsters",
		"RandomQuests",
		"RandomTeleporter",
		"RandomTraps",
		"ResetWhole",
		"ROMGangMember",
		"Sailor",
		"Scavenger",
		"Scriptable",
		"ScriptableEverymob",
		"Sounder",
		"StockMarket",
		"TaxCollector",
		"TaxiBehavior",
		"VeryAggressive",
		"WaterCurrents",
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
		final String err = CMLib.ableParms().testParmsFormat(format, "");
		if((err != null) && (err.startsWith("malformed format: ")))
			return err;
		return null;
	}

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		if((mob != null) && (mob.location() != null))
		{
			TEST_ROOM_ID = CMLib.map().getExtendedRoomID(mob.location());
			if(mob.location().getArea() != null)
				TEST_AREA = mob.location().getArea().Name();
		}
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
				if(TEST_AREA != null)
					val = CMStrings.replaceAll(val, "*AREA*", TEST_AREA);
				final boolean shouldPass = !val.startsWith("!");
				if(!shouldPass)
					val = val.substring(1);
				final String isInvalid = CMLib.ableParms().testParmsFormat(format, val);
				if((isInvalid == null) != shouldPass)
				{
					int lastGood = 0;
					for(int pfx = 1; pfx <= val.length(); pfx++)
					{
						if(CMLib.ableParms().testParmsFormat(format, val.substring(0, pfx)) == null)
							lastGood = pfx;
					}
					fails.append("Error#" + testNum + ": behavior '" + row[0] + "' with value '" + val
						+ "' should " + (shouldPass ? "match" : "not match")
						+ ((isInvalid == null) ? "" : " (" + isInvalid + ")")
						+ " (matches up to [" + val.substring(0, lastGood) + "])").append("\n");
				}
			}
		}
		if(fails.length() > 0)
			return fails.toString();
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
		if(CMLib.ableParms().testParmsFormat(CMParms.FORMAT_UNDEFINED, "min=10") != null)
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
