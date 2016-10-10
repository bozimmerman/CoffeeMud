package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.MUDZapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
/*
   Copyright 2005-2016 Bo Zimmerman

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
public interface MaskingLibrary extends CMLibrary
{
	public Map<String,ZapperKey> getMaskCodes();
	public String rawMaskHelp();
	public String maskHelp(final String CR, final String word);
	public List<String> getAbilityEduReqs(final String text);
	public String maskDesc(final String text);
	public String maskDesc(final String text, final boolean skipFirstWord);
	public CompiledZMask maskCompile(final String text);
	public CompiledZMask getPreCompiledMask(final String str);
	public boolean maskCheck(final CompiledZMask cset, final Environmental E, final boolean actual);
	public boolean maskCheck(final String text, final Environmental E, final boolean actual);
	public boolean maskCheck(final CompiledZMask cset, final PlayerLibrary.ThinPlayer E);
	public boolean maskCheck(final String text, final PlayerLibrary.ThinPlayer E);
	public boolean syntaxCheck(final String text, final List<String> errorSink);
	public int minMaskLevel(final String text, final int minMinLevel);
	public String[] separateMaskStrs(final String newText);
	public CompiledZMask createEmptyMask();
	
	public enum ZapperKey
	{
		_CLASS("-CLASSES"),
		_BASECLASS("-BASECLASSES"),
		_RACE("-RACES"),
		_ALIGNMENT("-ALIGNMENTS","-ALIGN"),
		_GENDER("-GENDERS"),
		_LEVEL("-LEVELS"),
		_CLASSLEVEL("-CLASSLEVELS"),
		_TATTOO("-TATTOOS"),
		TATTOO("+TATTOOS"),
		_NAME("-NAMES"),
		_PLAYER("-PLAYER"),
		_NPC("-MOB"),
		_RACECAT("-RACECATS"),
		RACECAT("+RACECATS"),
		_CLAN("-CLANS"),
		CLAN("+CLANS"),
		NAME("+NAMES"),
		_ANYCLASS("-ANYCLASSES"),
		ANYCLASS("+ANYCLASSES"),
		_ANYCLASSLEVEL("-ANYCLASSLEVELS"),
		ANYCLASSLEVEL("+ANYCLASSLEVELS"),
		ADJSTRENGTH("+ADJSTR"),
		ADJINTELLIGENCE("+ADJINT"),
		ADJWISDOM("+ADJWIS"),
		ADJDEXTERITY("+ADJDEX"),
		ADJCONSTITUTION("+ADJCON"),
		ADJCHARISMA("+ADJCHA"),
		_ADJSTRENGTH("-ADJSTR"),
		_ADJINTELLIGENCE("-ADJINT"),
		_ADJWISDOM("-ADJWIS"),
		_ADJDEXTERITY("-ADJDEX"),
		_ADJCONSTITUTION("-ADJCON"),
		_ADJCHARISMA("-ADJCHA"),
		_AREA("-AREAS"),
		AREA("+AREAS"),
		ITEM,
		_ITEM,
		CLASS("CLASSES"),
		ALIGNMENT,
		GENDER,
		LVLGR,
		LVLGE,
		LVLLT,
		LVLLE,
		LVLEQ,
		EFFECT("+EFFECTS"),
		_EFFECT("-EFFECTS"),
		_DEITY,
		DEITY,
		_FACTION,
		FACTION,
		WORN,
		_WORN,
		MATERIAL,
		_MATERIAL,
		RESOURCE,
		_RESOURCE,
		_JAVACLASS,
		JAVACLASS,
		ABILITY("+ABILITIES","+ABLE","+ABLES"),
		_ABILITY("-ABILITIES","-ABLE","-ABLES"),
		WORNON,
		_WORNON,
		VALUE,
		_VALUE,
		WEIGHT,
		_WEIGHT,
		ARMOR,
		_ARMOR,
		DAMAGE,
		_DAMAGE,
		ATTACK,
		_ATTACK,
		DISPOSITION,
		_DISPOSITION,
		SENSES,
		_SENSES,
		HOUR,
		_HOUR,
		SEASON,
		_SEASON,
		MONTH,
		_MONTH,
		_SECURITY("-SECURITIES","-SEC"),
		SECURITY("+SECURITIES","+SEC"),
		_EXPERTISE("-EXPERTISES"),
		EXPERTISE("+EXPERTISES"),
		_SKILL("-SKILLS"),
		SKILL("+SKILLS"),
		QUALLVL,
		_QUALLVL,
		STRENGTH("+STR"),
		INTELLIGENCE("+INT"),
		WISDOM("+WIS"),
		DEXTERITY("+DEX"),
		CONSTITUTION("+CON"),
		CHARISMA("+CHA"),
		_STRENGTH("-STR"),
		_INTELLIGENCE("-INT"),
		_WISDOM("-WIS"),
		_DEXTERITY("-DEX"),
		_CONSTITUTION("-CON"),
		_CHARISMA("-CHA"),
		HOME,
		_HOME,
		_SKILLFLAG("-SKILLFLAGS"),
		SKILLFLAG("+SKILLFLAGS"),
		_MAXCLASSLEVEL("-MAXCLASSLEVELS"),
		WEATHER,
		_WEATHER,
		DAY,
		_DAY,
		SYSOP,
		_SYSOP,
		SUBOP,
		_SUBOP,
		RACE("RACES"),
		_QUESTWIN,
		QUESTWIN,
		_GROUPSIZE,
		GROUPSIZE,
		BASECLASS("+BASECLASSES"),
		_IF,
		IF,
		_MOOD("-MOODS"),
		MOOD("+MOODS"),
		_CHANCE,
		_ACCCHIEVE("-ACCCHIEVES"),
		ACCCHIEVE("+ACCCHIEVES"),
		ISHOME("+ISHOME"),
		_ISHOME("-ISHOME")
		;
		private final String[] keys;
		private ZapperKey(String... exts)
		{
			final List<String> k = new ArrayList<String>();
			k.add(name().startsWith("_") ? ("-"+name().substring(1)) : ("+"+name()));
			for(String x : exts)
				k.add(x);
			keys = k.toArray(new String[0]);
		}
		public String[] keys()
		{
			return keys;
		}
	}
	
	public static interface CompiledZMaskEntry
	{
		public ZapperKey maskType();
		public Object[] parms();
	}
	
	public static interface CompiledZMask
	{
		public boolean[] flags();
		public boolean empty();
		public CompiledZMaskEntry[] entries();
	}
}
