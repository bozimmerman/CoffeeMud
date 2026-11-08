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

	private static final String[][] MISC_TESTS = {
		{"abc", "abc", "!abd", "ABC", "  abc  ", "!"},
		{"[NAME]", "Bob ", "foo;bar", "!"},
		{"[A] [B]", "foo bar ", "foo  bar"},
		{"(abc)", "abc", ""},
		{"(abc)def", "def", "abcdef"},
		{"(a/b)", "a", "b", "!c"},
		{"a...", "aaa"},
		{"[NAME];...", "foo;;;"},
		{"\\[test\\]", "[test]"},
		{"KEY=[VALUE]", "KEY=something "},
		{"[NAME](;[NAME])...", "foo", "foo;bar;baz"},
		{"(A)(B)(C)", "AC"},
		{"", ""},
		{"[file]...", "!"},
		{"[FREQUENCY]", "15", "!notanumber"},
		{"[NAME...]", "foo bar", "!"},
		{"[NAME...]tail", "foo bartail", "!tail"},
		{"[NAME...] tail", "!foo bar tail", "foo tail"},
		{"[NAME...]\\[x\\]", "foo [x]"},
		{"[NAME...]b", "a\\b"},
		{"[NAME...]...", "!foo"},
		{"([NAME...]) tail", "!foo tail"},
		{"[NUMBER]", "!notanumber", "15", "-5", "!-x", "!+5", "!5.5", "!"},
		{"[ONEWORD...]", "!hello world"},
		{"[STRING...]", "hello world"},
		{"[NUMBER_PAIR...]", "5,7", "!5,x"},
		{"[CHOICES(choices=FIRE,ICE)]", "FIRE", "!WATER"},
		{"[CHOICES]", "!FIRE"},
		{"[NUMBER_PAIR]", "5"},
		{"[CHOICES(choices=FIRE,ICE]", "!FIRE"},
		{"[CHOICES(choices)]", "!FIRE"},
		{"[MULTICHOICES(choices=FIRE,ICE)]", "FIRE", "3", "!WATER"},
		{"[CHOICES(choices=FIRE/choices=ICE)]", "!FIRE"},
		{"NM=(\")[STRING(prompt=Name/colh=Player)...](\")", "NM=Bob", "NM=\"Bob The Builder\"",
			"!NM=\"Bob The Builder", "NM=\"Bob \\\"The\\\" Builder\""},
		{"NM=(\")[STRING(prompt=Name/colh=Player)...](\")z", "NM=\"Bob The\"z", "NM=Bob z"},
		{"N=(\")[NUMBER(prompt=N/colh=N)...](\")", "!N=\"notanumber\"", "N=\"15\""},
		{"NM=(\")[STRING(prompt=Name/colh=Player)...](\") ;TELEPORT=[CONFIRM(prompt=Teleport/colh="
			+ "Teleport)]", "NM=\"Bob The Builder\" ;TELEPORT=true"},
		{"([STRING(prompt=Hated group/colh=Group)]=(\")[ZAPPERMASK(prompt=Mask/colh=Mask)...](\"))...",
			"KEY1=\"mask one\" KEY2=mask2"},
		{"FILENAME=(\")[STRING(prompt=Filename/colh=File)...](\") ISRECORDER=[CONFIRM(prompt=Is recorder/"
			+ "colh=Recorder)] TRIGGERTIME=[NUMBER(prompt=Trigger time/colh=Trigger)] MINLEVEL="
			+ "[NUMBER(prompt=Min level/colh=Min level)] (BY=[STRING(prompt=By/colh=By)])/(PLAYERS=(\")"
			+ "[STRING(prompt=Players/colh=Players)...](\"))", "FILENAME=\"::/resources/crecordings/\" "
			+ "ISRECORDER=true TRIGGERTIME=0 MINLEVEL=10 PLAYERS=\"bob smith\""},
		{"([CHECK(name=UNINVOKABLE/prompt=Uninvokable/colh=Uninvokable)])", "UNINVOKABLE", ""},
		{"[CHECK(name=UNINVOKABLE/prompt=Uninvokable/colh=Uninvokable)]", "!true", "!false",
			"!SOMETHINGELSE"},
		{"[NAME]..,", "foo", "foo,bar,baz", "!foo,", "!,foo", "!", "!foo,bar,"},
		{"[NAME]..;", "foo;bar"},
		{"[NAME]..|", "foo|bar"},
		{"[NAME]../", "!foo/bar"},
		{"[STRING(prompt=Hated group/colh=Group)]..,", "elf,dwarf,orc"},
		{"([NAME])..;", "!foo;", "foo;bar", ""},
		{"Druid_ShapeShift", "3", "[FILE=skills/shapeshift.txt]3", "[Cat];NAME=cat;3", "ATTADJ=0.7;3",
			"SHAPES=Rat,Beaver;3", "RACES=Rat,Beaver;3", "[RODENT FORM];NAME=Rodent form;ATTADJ=0.7;DMGADJ="
			+ "0.4;ARMADJ=1.0;SPEEDADJ=0.2;SHAPES=Rat,Beaver,Dire Rat;RACES=Rat,Beaver;3",
			"[Cat];NAME=cat;[Wolf];NAME=wolf;3"},
		{"[NAME...]/tail", "!foo/tail"},
		{"(NAME=[STRING(prompt=X/colh=X)...])..;", "!NAME=foo bar;"},
		{"[STRING..;]", "foo bar", "!foo bar;"},
		{"(NAME=[STRING..;])..;", "NAME=foo bar;NAME=baz"},
		{"[DECIMAL..;]", "0.7"},
		{"([STRING..,])..,", "foo,bar"},
		{"[DECIMAL]", "0.7", "-2.5", "!1.2.3"},
		{"[NUMBER]-[NUMBER]", "5-10"},
		{"(LEVEL=(+/-)[NUMBER])", "LEVEL=-5"},
		{"[STRING]", "a.b"},
		{"([STRING]/[NUMBER])%", "50%"},
		{"(A [STRING]);", "A foo;"},
		{"X=[STRINGORNULL]y", "!X=y"},
		{"[STRING](X);", "ab;"},
		{"[STRING](\");", "ab;"},
		{"(a/b/c)...", "a", "b", "a b"},
		{"(a/b/c...)", "c c"},
		{"((a/b/c))...", "a b"},
		{"[NAME]?", "foo?", "!foo"},
		{"aa bb/cc dd", "aa bb dd", "aa cc dd", "!cc dd"},
		{"aa/bb cc/dd", "bb cc", "aa", "!aa dd"},
		{"(ab)...", "abab", "", "!aba"},
		{"((a)bc)", "abc", "bc", ""},
		{"   ", "anything"},
		{"[NUMBER..;];", "123;"},
		{"[NUMBERORNULL]", "5", "!", "!x"},
		{"[NUMBER..;]", "!abc;def"},
		{"[NAME", "!foo"},
		{"(abc", "!abc"},
		{"[]", "!foo"},
		{"()", "!"},
		{"...", "!foo"},
		{"(a//b)", "!a"},
		{"(a/)", "!a"},
		{"((TIME=[NUMBER])/(LEVEL=[NUMBER]))...", "TIME=1 LEVEL=2", "LEVEL=2 TIME=1", "TIME=1 TIME=2"},
		{"(([NUMBER])/([ONEWORD]))...", "5 hello 6"},
		{"(((a/b)/c)/(d))...", "a d b"},
		{"((TIME=[NUMBER] LEVEL=[NUMBER])/(INTERVAL=[NUMBER]))...",
			"TIME=1 LEVEL=2 INTERVAL=3 TIME=4 LEVEL=5"},
		{"((TIME=[NUMBER])/(LEVEL=[NUMBER])...)", "LEVEL=2 LEVEL=3", "!TIME=1 LEVEL=2"},
		{"Immunities", "FIRE ALL COLD", "", "!FIRE BOGUSXYZ", "COLD Spell_Fireball", "FIRE Aggressive"},
		{"Gait", "ARRIVE=\"waddles in\" LEAVE=\"waddles\" STATE=\"waddling\"",
			"ARRIVE=waddling LEAVE=waddles STATE=waddling", "!ARRIVE=\"waddles in\" FOO=1"},
		{"Injury", "7:torso:10;5:left arm:20;", "3:left arm:10", "", "!99:torso:10", "!3:torso",
			"!3:klsjdfkasjhdf:10"},
		{"InstanceArea", "ID=MyArea DURATION=10", "DESCRIPTION=\"a dark cave\" DURATION=10",
			"ALIGNMENT=5", "DEFAULT_NEW", "", "!BOGUSKEY=x", "!DURATION", "!NOSUCHTYPE"},
		{"Milkable", "DRINK=true ON=true PERDAY=500 TYPE=MILK", "TYPE=MILK DRINK=true", "PERDAY=500", "",
			"!BOGUSKEY=x", "!DRINK=yes", "!TYPE=BOGUSXYZ", "!PERDAY"},
		{"PresenceReaction", "Spell_Fireball=damage", "Aggressive=zone~~Spell_Fireball=damage",
			"Spell_Fireball=damage~~Aggressive=zone", "Mood=aggressive posture", "+Aggressive=zone", "",
			"!BOGUSNAME=x", "!NOEQUALS", "!Aggressive"},
		{"QuestBound", "*", "*;*", "*;", "", "BOGUSXYZ", "!;", "!Q1;;Q2"},
		{"Regeneration", "HITS=5/3", "MANA=4", "MANA=4 HITS=5/3", "HITS=5/3 MANA=4", "HITS=+5",
			"HITS=-5", "+MAGIC HITS=5", "HITS=5 +MAGIC", "+LEVEL10 MANA=4 HITS=5", "+LEVEL10 MANA=4",
			"+SLASHING HITS=5/3", "", "!BOGUS=5", "!HITS", "!HITS=x", "!+BOGUS", "!+LEVEL"},
		{"Branding", "PRICE=5 OWNER=\"Bob\"", "OWNER=\"Bob\" PRICE=5", "!PRICE"},
		{"Loyalty", "NAME=\"Bob\" TELEPORT=true", "TELEPORT=true NAME=\"Bob\"", "!BOGUS=1"},
		{"Encrypto", "LROT=1 WROT=2 LVL=3", "LVL=3 WROT=2 LROT=1", "!BOGUS=1"},
		{"SelfInvesting", "LASTUSE=100 BANKED=1.5", "BANKED=1.5 LASTUSE=100", "!BOGUS=1"},
		{"Archon_CRecord", "FILENAME=rec ISRECORDER=true", "ISRECORDER=true FILENAME=rec", "!BOGUS=1"},
		{"Burning", "PERMANENT NOSPREAD", "NOSPREAD PERMANENT", "!BOGUS"},
		{"Dragonbreath", "f LESSER", "f", "!x"},
		{"Prop_Hidden", "UNLOCATABLE BUBBLES", "BUBBLES UNLOCATABLE", "!BOGUS"},
		{"Prop_ReqNoMOB", "NOFOLLOW NOSNEAK", "NOSNEAK NOFOLLOW", "!BOGUS"},
		{"Prop_RoomUnmappable", "MAPOK NOEXPLORE", "NOEXPLORE MAPOK", "!BOGUS"},
		{"Prop_NoSummon", "ALLOWNONAGGR MAGICONLY", "MAGICONLY ALLOWNONAGGR", "!BOGUS"},
		{"Prop_NoChannel", "GOSSIP;SENDOK", "SENDOK;GOSSIP", "!BOGUSXYZ;SENDOK"},
		{"Prop_LimitedItems", "5", "5;ID=grp", "!x"},
		{"Scarring", "TORSO;ARM", "ARM;TORSO", "left arm;RIGHT LEG;HEAD", "HEAD;RIGHT LEG;left arm",
			"LEFT EYEBROW;HEAD;face", "face;HEAD;LEFT EYEBROW", "HEAD;TORSO;", "", "!BOGUS", "!;",
			"!TORSO;;ARM", "FINGER", "!+TORSO"},
		{"ScriptLater", "<SCRIPTS><SCRIPT INVOKER=\"Zac\" CLOCK=\"2026/10/31/14 Example Area\">wave</"
			+ "SCRIPT><SCRIPT TICKS=\"5\">say hi</SCRIPT></SCRIPTS>", "<SCRIPTS><SCRIPT TICKS=\"5\">say hi</"
			+ "SCRIPT><SCRIPT INVOKER=\"Zac\" CLOCK=\"2026/10/31/14 Example Area\">wave</SCRIPT></SCRIPTS>",
			"<SCRIPTS><SCRIPT TIME=\"1234567890123\">bow</SCRIPT></SCRIPTS>",
			"<SCRIPTS><SCRIPT CLOCK=\"10/31/2026 14h\">nod</SCRIPT></SCRIPTS>", "<SCRIPTS></SCRIPTS>",
			"!BOGUS", "!<SCRIPTS><SCRIPT>wave</SCRIPT></SCRIPTS>",
			"!<SCRIPTS><SCRIPT CLOCK=\"DAY 1\">wave</SCRIPT></SCRIPTS>",
			"!<SCRIPTS><SCRIPT TICKS=\"abc\">wave</SCRIPT></SCRIPTS>",
			"!<SCRIPTS><SCRIPT TIME=\"abc\">wave</SCRIPT></SCRIPTS>"},
		{"SoundEcho", "10", "RANGE=10 ACCURACY=50", "ACCURACY=50 RANGE=10", "RANGE=7", "", "!BOGUS",
			"!RANGE=abc", "!10 20"},
		{"TemporaryAffects", "", "-Spell_Fly", "Spell_Fly 10", "Spell_Fireball 20 glow bright",
			"+Spell_Fly 10", "++Spell_Fly 10", "+#Spell_Fly 10", "#Spell_Fly 10", "AMBIANCE 10 glows",
			"+BINDTO fred the fighter", "!Spell_Fly", "!BOGUS", "!+BINDTO", "!BINDTO fred",
			"!-Spell_Fly 10"},
		{"TemporaryImmunity", "+Spell_Fireball", "Spell_Fireball/1000;Spell_Shield/2000",
			"Spell_Shield/2000;Spell_Fireball/1000", "Spell_Fireball/1000;", "", "!BOGUS",
			"!Spell_Fireball/abc", "!Spell_Fireball", "!+", "!+Spell_Fireball/1000"},
		{"TriggeredAffects", "", "-Spell_Fly", "ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD",
			"ONTRIGGER=ADD TRIGGER=SITTING TICKS=10 ID=Spell_Fly", "ID=Spell_Fly TICKS=10 TRIGGER=SITTING "
			+ "ONTRIGGER=ADD PARMS=\"glow bright\" MESSAGE=\"shimmers\"",
			"+ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD", "(ID=Spell_Fly TICKS=10 TRIGGER=SITTING "
			+ "ONTRIGGER=ADD)(ID=Spell_Shield TICKS=5 TRIGGER=SITTING ONTRIGGER=REMOVE)",
			"ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD IMEANIT=true INVOKE=false",
			"ID=AMBIANCE TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD PARMS=glows", "!BOGUS",
			"!ID=Spell_Fly TICKS=abc TRIGGER=SITTING ONTRIGGER=ADD",
			"!ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=MAYBE",
			"!++ID=Spell_Fly TICKS=10 TRIGGER=SITTING ONTRIGGER=ADD"},
		{"WanderHomeLater", "", "areaok=true once=true minticks=10", "minticks=10 once=true areaok=true",
			"destroy=false", "!BOGUS", "!areaok", "!minticks=abc"},
		{"Poison", "RANK=1.5 ITEMAMBIANCE=\"foul stench\" MOBAMBIANCE=\"sickly glow\"",
			"MOBAMBIANCE=\"sickly glow\" RANK=1.5 ITEMAMBIANCE=\"foul stench\"",
			"ITEMAMBIANCE=stench RANK=2", "RANK=2 ITEMAMBIANCE=stench", "RANK=1.5", "", "!BOGUS=1",
			"!RANK=abc", "!RANK="},
		{"UniPoison", "RANK=1.5 NAME=\"Deadly Venom\" DAMAGE=5",
			"DAMAGE=5 RANK=1.5 NAME=\"Deadly Venom\"", "AFFECTTARG MOOD=angry", "MOOD=angry AFFECTTARG",
			"RANK", "", "!BOGUS=1", "!RANK=abc", "!123=abc"},
		{"Prop_AbsorbDamage", "50% +ALL -SILVER -CLOTH -LEATHER", "5 +ALL -MAGIC -SLASHING",
			"25% +ALL -LEVEL15", "15% +BASHING 1 +IRON 3 +STEEL 15% +MAGIC", "ENHANCED", "!BOGUS",
			"!+BOGUS", "!ALL", "!+", "!+50", "!50.5", "!50 %"},
		{"Prop_CombatAdjuster", "", "ATTACK+10", "ATTACK-20", "ATTACK=5", "DAMAGE=5 ATTACK+10",
			"SPEED-3 DAMAGE=2 ATTACK=1", "HP+100 MANA-50 MOVE+10 ARMOR=25", "ATTACK=-5", "!ATTACK",
			"!ATTACK 10", "!ATTACK+abc", "!BOGUS+5", "!ATTACK=5.5", "!ATTACK=+5", "!ATTACK+"},
		{"Prop_CommonTwister", "", "SKILL=* MASK=iron", "MASK=iron SKILL=*",
			"SKILL=Spell_Fireball MASK=steel", "MASK=steel SKILL=Spell_Fireball",
			"SKILL=* MASK=iron;MASK=steel SKILL=Spell_Fireball", "!BOGUS",
			"!SKILL=NotARealAbility MASK=iron", "!MASK=", "!SKILL=* MASK=iron BOGUS"},
		{"Prop_AffectReTick", "", "SCAN=MOBS,ITEMS Spell_Fireball=@x1*2",
			"Spell_MagicMissile=@x1/3 SCAN=MOBS", "SCAN=MOB,ITEM,OWNER,OWNERS Spell_Fireball=@x1+1",
			"SCANMASK=\"-LEVEL +>=10\" Spell_Fireball=\"@x1 * 2\"", "SCANMASK=GUEST Spell_MagicMissile=@x1",
			"!SCAN=BOGUS Spell_Fireball=@x1*2", "!Spell_NotARealAbility=@x1*2", "!Spell_Fireball=2",
			"!Spell_Fireball=@x1)", "!Fireball=@x1*2"},
		{"Prop_AddDamage", "50%", "-3", "70% 5", "\"SLASHING\"", "\"CAST_SPELL\"",
			"50% \"SLASHING\" \"CAST_SPELL\"", "-3 \"PIERCING\" \"CAST_SPELL\";SMASK=\"-LEVEL +5\"",
			"!BOGUS", "!\"WRONGTYPE\"", "SLASHING", "CAST_SPELL", "!WRONGTYPE"},
		{"Prop_Artifact", "", "AUTODROP=true NOCAST=false", "NOCAST=false AUTODROP=true NODB=true",
			"AUTODROP=true;Sword@1", ";Sword@1"},
		{"Prop_ClanEquipment", "FIRE 5 Clan Type", "FIRE 5 \"Clan Name\" \"Some Type\"",
			"\"COLD\" 3 \"Big Clan\" \"Other Type\"", "ACID 2 \"Clan\" Type",
			"!FIRE nope \"Clan\" \"Type\"", "!FIRE 5 \"Clan\""},
		{"Prop_CloseCommand", "", "noclose", "close door", "message=You can't close that",
			"noclose;close door", "message=You can't;close door;noclose", "close door;"},
		{"Prop_ClosedDayNight", "", "DAY HOURS=8-17", "HOURS=8-17 DAY",
			"SLEEP \"WARNMSG5=Closing soon\"", "\"HOME=The Tavern\" DAY", "\"SHOPMSG=We are closed\" DAY",
			"LOCKUP SIT HOURS=20-6 MASK=\"-RACE +ELF\"", "DAY MASK=-RACE +ELF"},
		{"Prop_ClosedSeason", "", "WINTER NIGHT", "NIGHT WINTER", "SUMMER RAIN MASK=-RACE +ELF",
			"WINTER MASK=GUEST", "WINTER MASK = GUEST"},
		{"Prop_Doppleganger", "", "MAX=27 MIN=27", "MIN=27 MAX=27", "ASMATERIAL=\"IRON\" MIN=27 MAX=27",
			"LEVELPCT=75 LEVELADD=5 DIFFGRP=true", "SAVEPCT=\"30\" DIFFPCT=80 MIN=20 MAX=40",
			"LEVELPCT=25 SAVEPCT=0 DIFFPCT=100", "LEVELPCT=\"75\" SAVEPCT=\"0\" DIFFPCT=\"100\"",
			"LEVELPCT=75", "!DIFFPCT=\"abc\"", "!LEVELPCT=bad", "10", "50%", "!BOGUSKEY=x",
			"!MATERIALS_METALLER"},
		{"Prop_ImproveGather", "AMT=7", "SKILLS=\"Spell_Fireball,Spell_MagicMissile\"", "SKILLS=\"ALL\"",
			"MASK=\"ROCK\"", "AMT=3 SKILLS=\"Spell_Fireball,Spell_MagicMissile\"",
			"MASK=\"ROCK\" AMT=5 SKILLS=\"ALL\"",
			"AMT=4 SKILLS=\"Spell_Fireball,Spell_MagicMissile\" MASK=\"ROCK\"", "!BOGUS=x",
			"!SKILLS=\"Fireball\" XTRA=5", ""},
		{"Prop_ItemBinder", "", "TO=CHARACTER", "TO=CLAN", "TO=PLAYER", "TO=NPC", "TO=NOONE", "ON=EQUIP",
			"ON=PICKUP", "BOUND=\"Bob The Builder\"", "DEATHKEEP=true",
			"MESSAGE=\"the item binds to you!\"", "TO=PLAYER ON=PICKUP BOUND=\"Bob The Builder\" DEATHKEEP="
			+ "true MESSAGE=\"the item binds to you!\"",
			"TO=NOONE ON=PICKUP BOUND=\"the graveyard\" DEATHKEEP=false",
			"TO=CLAN ON=PICKUP BOUND=\"the clan vault\""},
		{"Prop_LangTranslator", "", "Elvish", "50 Elvish", "50% Draconic", "Elvish 'hello",
			"'greetings Draconic", "`bonjour` Draconic", "#Zac Elvish", "NOTRANSLATE",
			"Elvish NOTRANSLATE Goblinese 50", "25% Draconic 'hail 50% Goblinese #Zac NOTRANSLATE",
			"Elvish 50% 25 Draconic", "!BOGUSLANG", "#Zac"},
		{"Prop_LimitedContents", "", "5", "5 -RACE +ELF", "5;10", "5 -RACE +ELF;10 -CLASS +Fighter",
			"MSG=\"only five\"", "ACTUAL=false", "ACTUAL=true MSG=\"do not add more\"",
			"5 -RACE +ELF;10;MSG=\"only five\" ACTUAL=false"},
		{"Prop_ItemTransReceiver", "", "any string at all", "KEY=value junk;&^%$ 42"},
		{"Prop_ItemTransporter", "", "any string at all", "KEY=value junk;&^%$ 42"},
		{"Prop_LanguageSpeaker", "", "PLAYERS=true", "NOMOBS=true", "HOMEONLY=true",
			"PLAYERS=true NOMOBS=true HOMEONLY=true LANGSTR=\"Common\"", "LANGSTR=\"Common\"",
			"PLAYERS=true LANGSTR=\"Draconic\";RACE -ELF",
			"!PLAYERS=yes NOMOBS=true HOMEONLY=true LANGSTR=\"Common\"", "!LANGSTR=\"NotARealAbility\""},
		{"Prop_RoomForSale", "Bob/100000", "Bob/RENTAL 1000", "Bob/GRID 1500", "Bob/ALLOWTHEFT 1200",
			"Bob/TAX500X 13000", "Bob/RENTAL GRID ALLOWTHEFT TAX720X 2500",
			"Bob/GRID RENTAL TAX720X ALLOWTHEFT 2500", "Bob/GRID ALLOWTHEFT RENTAL 1750", "!Bob/BOGUS 1000",
			"!Bob/TAXBADX 1000", "!Bob/RENTAL=1 1000", "/RENTAL GRID 1800", "!Bob//RENTAL GRID 1800",
			"!//RENTAL GRID 1800"},
		{"Prop_AreaForSale", "Bob/100000", "Bob/RENTAL 1000", "Bob/ALLOWTHEFT 1200", "Bob/TAX500X 13000",
			"Bob/RENTAL ALLOWTHEFT TAX720X 2500", "Bob/TAX720X ALLOWTHEFT RENTAL 2500",
			"Bob/ALLOWTHEFT RENTAL 1750", "/RENTAL 5000", "!Bob/BOGUS 1000", "!Bob/TAXBADX 1000",
			"!Bob//RENTAL ALLOWTHEFT 5000", "!//RENTAL 5000"},
		{"Apothecary", "ANTIDOTE", "", "!BOGUS"},
		{"Skill_CaravanTravel", "FULL", "", "!BOGUS"},
		{"Skill_SeaNavigation", "FULL", "", "!BOGUS"},
		{"Spell_ColorSpray", "UNCONSCIOUS", "STUNNED", "", "!BOGUS"},
		{"Spell_Fear", "WEAK", "", "!BOGUS"},
		{"Spell_LimbRack", "ARMSONLY", "", "!BOGUS"},
		{"Prop_EnlargeRoom", "", "10", "+10", "/10"},
		{"AnimalBonding", "", "Bob", "Bob the Ranger"},
		{"Antidote", "", "Spell_Fireball", "Poison", "Magic Missile", "!NotARealAbility"},
		{"DiligentStudying", "", "2", "-3", "!abc", "!5.5"},
		{"Chant_AlterTime", "", "1", "-1", "!2", "!0"},
		{"Chant_BestowName", "", "Bold", "the Bold", "Bob The Builder"},
		{"Chant_CharmAnimal", "", "Bob", "Bob the Charmer"},
		{"Chant_FindPlant", "", "rose", "big red rose"},
		{"Chant_Flood", "", "ATMOSPHERE=2", "!ATMOSPHERE=abc"},
		{"Chant_StoneFriend", "", "Bob", "Bob the Charmer"},
		{"Chant_SummonAnimal", "", "Human", "Elf", "!NotARealRace"},
		{"DiseaseCure", "", "Spell_Fireball", "Fireball", "Magic Missile", "!NotARealAbility"},
		{"Skill_ControlUndead", "", "Bob", "Bob the Necromancer"},
		{"Spell_Charm", "", "Bob", "Bob the Charmer"},
		{"Spell_Enthrall", "", "Bob", "Bob the Charmer"},
		{"SmokeRings", "", "STOP", "thick blue rings"},
		{"Spell_Torture", "", "HITONLY", "please stop hurting me"},
		{"Spell_Brainwash", "", "you love the archon", "obey"},
		{"Prop_NewDeathMsg", "", "you have died a painful death", "slain"},
		{"([STRING(prompt=Name/colh=Name).. ])", "", "Bold", "the Bold", "Bob The Builder"},
		{"KV=(\")[STRING(prompt=Value/colh=Value).. ](\")", "KV=\"a b c\"", "KV=abc", "KV=a b c",
			"!KV=\"a b c"},
		{"Bomb_Noxious", "", "Spell_Fireball", "!NotARealAbility"},
		{"Bomb_Pepper", "", "Spell_Fireball", "!NotARealAbility"},
		{"Bomb_Poison", "", "Spell_Fireball", "!NotARealAbility"},
		{"Bomb_Smoke", "", "Spell_Fireball", "!NotARealAbility"},
		{"Bomb_Sonic", "", "Spell_Fireball", "!NotARealAbility"},
		{"Bomb_SpellBlast", "", "Spell_Fireball", "!NotARealAbility"},
		{"Bomb_Spores", "", "Spell_Fireball", "!NotARealAbility"},
		{"Fighter_FavoredMount", "", "Equine,Horse", "Humanoid,Human", "!Horse,Equine", "!Equine",
			"!Equine,NotARace"},
		{"Fighter_Runover", "", "goblin", "Bob the Fighter"},
		{"GenTrap", "", "Spell_Fireball", "Poison", "!NotARealAbility"},
		{"PoisonousBite", "", "Spell_Fireball", "Poison", "!NotARealAbility"},
		{"Prayer_BlessedHearth", "", "Bob", "Bob the Builder", "The Red Hand Clan"},
		{"Prayer_BloodHearth", "", "Bob", "The Red Hand Clan"},
		{"Prayer_Designation", "", "the Chosen", "Bob the Bold"},
		{"Prayer_FindSacredItem", "", "relic", "big red relic"},
		{"Prayer_GreaterWardingGlyph", "", "Bob", "Bob the Fighter"},
		{"Prayer_GuardianHearth", "", "Bob", "The Red Hand Clan"},
		{"Prayer_ImprovedWardingGlyph", "", "Bob", "Bob the Fighter"},
		{"Prayer_InfuseBalance", "", "!NotARealDeity"},
		{"Prayer_InfuseDiscipline", "", "!NotARealDeity"},
		{"Prayer_InfuseHoliness", "", "!NotARealDeity"},
		{"Prayer_InfuseImpunity", "", "!NotARealDeity"},
		{"Prayer_InfuseModeration", "", "!NotARealDeity"},
		{"Prayer_InfuseUnholiness", "", "!NotARealDeity"},
		{"Prayer_LesserWardingGlyph", "", "Bob", "Bob the Fighter"},
		{"Prayer_Monolith", "", "STONE", "FIRE", "ice", "!WOOD", "!STONE FIRE"},
		{"Prayer_Sanctum", "", "Bob", "The Red Hand Clan", "Bob;msg"},
		{"Prayer_SummonElemental", "", "FIRE", "fire", "Earth", "AIR", "WATER", "!ICE", "!WOOD", "!FIRE AIR"},
		{"Prop_AstralSpirit", "", "SELF-RES", "self-res", "SELF-RES SELF-RES", "!SELF", "!BOGUS"},
		{"Prop_HaveEnabler", "", "NOUNINVOKE", "NOUNINVOKE;", "ONCLOSED;", "LEVEL=5;", "TICKS=5;", "Spell_Fireball;", "50% Spell_Fireball;", "Spell_Fireball;MASK=Bob", "NOUNINVOKE;MASK=Bob", "!Spell_Fireball(param);"},
		{"Prop_MOBEmoter", "", "Emoter say hello", "say hello", "BOGUS foo", "Aggressive"},
		{"Prop_NoPKill", "", "10", "0", "-3", "!abc", "!1.5"},
		{"Prop_ReqHeight", "", "60", "0", "-5", "!abc"},
		{"Prop_RoomDark", "", "HOURS=18", "HOURS=18,6", "HOURS=\"18,6\"", "hours=18", "Hours=\"18\"", "!HOURS=18-6", "!HOURS=18,", "!HOURS=,18", "!BOGUS", "!18,6"},
		{"Prop_RoomLit", "", "HOURS=6", "HOURS=6,18", "HOURS=\"6,18\"", "hours=6", "!HOURS=6-18", "!BOGUS"},
		{"Prop_Tattoo", "", "tattoo", "tattoo;another", "tattoo;respect", "account 5 tattoo;another", "!account 5 tattoo name;another", "!5;", "!account 5 tattoo;other 3 ink", "!;"},
		{"Prop_TicketTaker", "", "1000", "a ticket", "a ticket please", "abc", "10.5"},
		{"Ranger_Enemy1", "", "Humanoid", "Equine", "Unique", "!Human", "!NotARace"},
		{"Skill_Enslave", "", "Bob", "BOGUS", "MASTER=Bob", "MASTER=Bob PRICE=100", "MASTER=\"Bob the Bold\" PRICE=100", "!Bob the Bold"},
		{"Skill_Mimicry", "", "Bob", "Bob the Bold"},
		{"Skill_NavalTactics", "", "FOLLOW", "follow", "APPROACH", "RETREAT", "FLEE", "!BOGUS", "!FOLLOW APPROACH"},
	};

	@Override
	public String doTest(final MOB mob, final int metaFlags, final String what, final List<String> commands)
	{
		int testNum = 0;
		for(final String[] row : MISC_TESTS)
		{
			testNum++;
			final Ability miscAbility = CMClass.getAbility(row[0]);
			final String format;
			if(miscAbility != null)
			{
				format = miscAbility.miscTextFormat();
				if(format == null)
					return ("Error#" + testNum + ": ability '" + row[0] + "' could not produce a format");
			}
			else
				format = row[0];
			for(int v = 1; v < row.length; v++)
			{
				String val = row[v];
				final boolean shouldPass = !val.startsWith("!");
				if(!shouldPass)
					val = val.substring(1);
				final boolean isValid = CMLib.ableParms().isValidMiscText(format, val);
				if(isValid != shouldPass)
					return ("Error#" + testNum + ": " + (miscAbility != null ? "ability '" + row[0] + "' " : "")
						+ "format '" + format + "' with value '" + val + "' should " + (shouldPass ? "match" : "not match"));
			}
		}
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
		if(!CMLib.ableParms().isValidMiscText(raptureFmt, "HAPPY"))
			return ("Error#205d: the Prayer_JoyousRapture format should accept a generated mood choice");
		if(CMLib.ableParms().isValidMiscText(raptureFmt, "NOTAMOOD"))
			return ("Error#205e: the Prayer_JoyousRapture format should reject an unknown mood");
		// Test 206: the Infuse prayers accept an empty or real deity name and reject unknown ones.
		final String deityFormat = "([DEITY(prompt=Deity Name/colh=Deity).. ])";
		final String[] infusePrayers = {
			"Prayer_InfuseBalance", "Prayer_InfuseDiscipline", "Prayer_InfuseHoliness",
			"Prayer_InfuseImpunity", "Prayer_InfuseModeration", "Prayer_InfuseUnholiness"
		};
		String realDeityName = null;
		for(final Enumeration<Deity> d=CMLib.map().deities();d.hasMoreElements();)
		{
			final Deity D=d.nextElement();
			if((D!=null)&&(D.Name().trim().length()>0))
			{
				realDeityName=D.Name();
				break;
			}
		}
		for(final String infuseID : infusePrayers)
		{
			final Ability infuseA = CMClass.getAbility(infuseID);
			if(infuseA == null)
				return ("Error#206a: ability '"+infuseID+"' is not loaded");
			final String infuseFmt = infuseA.miscTextFormat();
			if(!deityFormat.equals(infuseFmt))
				return ("Error#206b: ability '"+infuseID+"' should use '"+deityFormat+"', got '"+infuseFmt+"'");
			if(!CMLib.ableParms().isValidMiscText(infuseFmt, ""))
				return ("Error#206c: ability '"+infuseID+"' should accept an empty deity");
			if(CMLib.ableParms().isValidMiscText(infuseFmt, "NotARealDeity"))
				return ("Error#206d: ability '"+infuseID+"' should reject an unknown deity");
			if((realDeityName != null)&&(!CMLib.ableParms().isValidMiscText(infuseFmt, realDeityName)))
				return ("Error#206e: ability '"+infuseID+"' should accept the deity '"+realDeityName+"'");
		}
		// Test 207: room-ID properties accept a real room, empty, and reject unknown IDs.
		final String realRoomID = CMLib.map().getExtendedRoomID(CMLib.map().getRandomRoom());
		final String[] roomIDAbilities = {"Prayer_Refuge", "Prop_SparringRoom"};
		for(final String roomID : roomIDAbilities)
		{
			final Ability ridA = CMClass.getAbility(roomID);
			if(ridA == null)
				return ("Error#207a: ability '"+roomID+"' is not loaded");
			final String ridFmt = ridA.miscTextFormat();
			if(!CMLib.ableParms().isValidMiscText(ridFmt, ""))
				return ("Error#207b: ability '"+roomID+"' should accept an empty room");
			if(!CMLib.ableParms().isValidMiscText(ridFmt, realRoomID))
				return ("Error#207c: ability '"+roomID+"' should accept the room '"+realRoomID+"'");
			if(CMLib.ableParms().isValidMiscText(ridFmt, "No Such Area#9999"))
				return ("Error#207d: ability '"+roomID+"' should reject an unknown room");
		}
		// Test 208: Ranger_PlanarEnemy accepts an empty or known plane and rejects unknown ones.
		final Ability planarEnemyA = CMClass.getAbility("Ranger_PlanarEnemy");
		if(planarEnemyA == null)
			return ("Error#208a: Ranger_PlanarEnemy ability not loaded");
		final String planarEnemyFmt = planarEnemyA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(planarEnemyFmt, ""))
			return ("Error#208b: the Ranger_PlanarEnemy format should accept a blank plane");
		if(CMLib.ableParms().isValidMiscText(planarEnemyFmt, "NOTAPLANE"))
			return ("Error#208c: the Ranger_PlanarEnemy format should reject an unknown plane");
		final Ability planarProto2 = CMClass.getAbilityPrototype("StdPlanarAbility");
		if(planarProto2 instanceof PlanarAbility)
		{
			final List<String> planeKeys = ((PlanarAbility)planarProto2).getAllPlaneKeys();
			if(planeKeys.size() > 0)
			{
				if(!CMLib.ableParms().isValidMiscText(planarEnemyFmt, planeKeys.get(0)))
					return ("Error#208d: the Ranger_PlanarEnemy format should accept a known plane");
			}
		}
		// Test 209: Skill_Convert accepts an empty or real deity name and rejects unknown ones.
		final Ability convertA = CMClass.getAbility("Skill_Convert");
		if(convertA == null)
			return ("Error#209a: Skill_Convert ability not loaded");
		final String convertFmt = convertA.miscTextFormat();
		if(!CMLib.ableParms().isValidMiscText(convertFmt, ""))
			return ("Error#209b: the Skill_Convert format should accept an empty deity");
		if(CMLib.ableParms().isValidMiscText(convertFmt, "NotARealDeity"))
			return ("Error#209c: the Skill_Convert format should reject an unknown deity");
		if((realDeityName != null)&&(!CMLib.ableParms().isValidMiscText(convertFmt, realDeityName)))
			return ("Error#209d: the Skill_Convert format should accept the deity '"+realDeityName+"'");
		return null;
	}
}
