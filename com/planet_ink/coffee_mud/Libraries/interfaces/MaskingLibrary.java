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
   Copyright 2005-2018 Bo Zimmerman

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
		_PLAYER("-PLAYER"),
		_NPC("-MOB"),
		_CHANCE,
		_CLASS("-CLASSES"),
		CLASS("CLASSES"),
		_BASECLASS("-BASECLASSES"),
		BASECLASS("+BASECLASSES"),
		_CLASSTYPE("-CLASSTYPE"),
		CLASSTYPE("CLASSTYPE"),
		_RACE("-RACES"),
		RACE("RACES"),
		_ALIGNMENT("-ALIGNMENTS","-ALIGN"),
		ALIGNMENT,
		_GENDER("-GENDERS"),
		GENDER,
		_LEVEL("-LEVELS"),
		_CLASSLEVEL("-CLASSLEVELS"),
		_TATTOO("-TATTOOS"),
		TATTOO("+TATTOOS"),
		_NAME("-NAMES"),
		NAME("+NAMES"),
		_RACECAT("-RACECATS"),
		RACECAT("+RACECATS"),
		_CLAN("-CLANS"),
		CLAN("+CLANS"),
		_ANYCLASS("-ANYCLASSES"),
		ANYCLASS("+ANYCLASSES"),
		_ANYCLASSLEVEL("-ANYCLASSLEVELS"),
		ANYCLASSLEVEL("+ANYCLASSLEVELS"),
		_ADJSTRENGTH("-ADJSTR"),
		ADJSTRENGTH("+ADJSTR"),
		_ADJINTELLIGENCE("-ADJINT"),
		ADJINTELLIGENCE("+ADJINT"),
		_ADJWISDOM("-ADJWIS"),
		ADJWISDOM("+ADJWIS"),
		_ADJDEXTERITY("-ADJDEX"),
		ADJDEXTERITY("+ADJDEX"),
		_ADJCONSTITUTION("-ADJCON"),
		ADJCONSTITUTION("+ADJCON"),
		_ADJCHARISMA("-ADJCHA"),
		ADJCHARISMA("+ADJCHA"),
		_AREA("-AREAS"),
		AREA("+AREAS"),
		ITEM,
		_ITEM,
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
		_WORN,
		WORN,
		_MATERIAL,
		MATERIAL,
		_RESOURCE,
		RESOURCE,
		_JAVACLASS,
		JAVACLASS,
		_ABILITY("-ABILITIES","-ABLE","-ABLES"),
		ABILITY("+ABILITIES","+ABLE","+ABLES"),
		_WORNON,
		WORNON,
		_VALUE,
		VALUE,
		_WEIGHT,
		WEIGHT,
		_ARMOR,
		ARMOR,
		_DAMAGE,
		DAMAGE,
		_ATTACK,
		ATTACK,
		_DISPOSITION,
		DISPOSITION,
		_SENSES,
		SENSES,
		_HOUR,
		HOUR,
		_SEASON,
		SEASON,
		_MONTH,
		MONTH,
		_SECURITY("-SECURITIES","-SEC"),
		SECURITY("+SECURITIES","+SEC"),
		_EXPERTISE("-EXPERTISES"),
		EXPERTISE("+EXPERTISES"),
		_SKILL("-SKILLS"),
		SKILL("+SKILLS"),
		_QUALLVL,
		QUALLVL,
		_STRENGTH("-STR"),
		STRENGTH("+STR"),
		_INTELLIGENCE("-INT"),
		INTELLIGENCE("+INT"),
		_WISDOM("-WIS"),
		WISDOM("+WIS"),
		_DEXTERITY("-DEX"),
		DEXTERITY("+DEX"),
		_CONSTITUTION("-CON"),
		CONSTITUTION("+CON"),
		_CHARISMA("-CHA"),
		CHARISMA("+CHA"),
		_HOME,
		HOME,
		_SKILLFLAG("-SKILLFLAGS"),
		SKILLFLAG("+SKILLFLAGS"),
		_MAXCLASSLEVEL("-MAXCLASSLEVELS"),
		_WEATHER,
		WEATHER,
		_DAY,
		DAY,
		_SYSOP,
		SYSOP,
		_SUBOP,
		SUBOP,
		_QUESTWIN,
		QUESTWIN,
		_GROUPSIZE,
		GROUPSIZE,
		_IF,
		IF,
		_MOOD("-MOODS"),
		MOOD("+MOODS"),
		_ACCCHIEVE("-ACCCHIEVES"),
		ACCCHIEVE("+ACCCHIEVES"),
		_ISHOME("-ISHOME"),
		ISHOME("+ISHOME"),
		_IFSTAT("-IFSTAT"),
		IFSTAT("+IFSTAT"),
		_SUBNAME("-SUBNAME"),
		SUBNAME("+SUBNAME"),
		_WEAPONCLASS("-WEAPONCLASS"),
		WEAPONCLASS("+WEAPONCLASS"),
		_WEAPONTYPE("-WEAPONTYPE"),
		WEAPONTYPE("+WEAPONTYPE"),
		_WEAPONAMMO("-WEAPONAMMO"),
		WEAPONAMMO("+WEAPONAMMO"),
		_ACCOUNT("-ACCOUNTS"),
		ACCOUNT("+ACCOUNTS")
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
