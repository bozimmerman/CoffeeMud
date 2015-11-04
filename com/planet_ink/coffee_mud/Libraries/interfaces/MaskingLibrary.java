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
   Copyright 2005-2015 Bo Zimmerman

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
	public Map<String,Integer> getMaskCodes();
	public String rawMaskHelp();
	public String maskHelp(final String CR, final String word);
	public List<String> getAbilityEduReqs(final String text);
	public String maskDesc(final String text);
	public String maskDesc(final String text, final boolean skipFirstWord);
	public CompiledZapperMask maskCompile(final String text);
	public CompiledZapperMask getPreCompiledMask(final String str);
	public boolean maskCheck(final CompiledZapperMask cset, final Environmental E, final boolean actual);
	public boolean maskCheck(final String text, final Environmental E, final boolean actual);
	public boolean maskCheck(final CompiledZapperMask cset, final PlayerLibrary.ThinPlayer E);
	public boolean maskCheck(final String text, final PlayerLibrary.ThinPlayer E);
	public boolean syntaxCheck(final String text, final List<String> errorSink);
	public int minMaskLevel(final String text, final int minMinLevel);
	public String[] separateMaskStrs(final String newText);

	public static class CompiledZapperMaskEntry
	{
		public final int maskType;
		public final Object[] parms;
		public CompiledZapperMaskEntry(final int type, final Object[] parms)
		{	maskType=type;this.parms=parms;}
	}

	public static class CompiledZapperMask
	{
		public final boolean[] flags;
		public final boolean empty;
		public final CompiledZapperMaskEntry[] entries;
		public CompiledZapperMask(final boolean[] flags, final CompiledZapperMaskEntry[] entries)
		{	this.flags=flags; this.entries=entries; this.empty=false;}
		public CompiledZapperMask(final boolean[] flags, final CompiledZapperMaskEntry[] entries, final boolean empty)
		{	this.flags=flags; this.entries=entries; this.empty=empty;}
		public static final CompiledZapperMask EMPTY()
		{	return new CompiledZapperMask(new boolean[2],new CompiledZapperMaskEntry[0],true); }
	}

	public static class SavedClass
	{
		public final CharClass C;
		public final String name;
		public final String upperName;
		public final String baseClass;
		public final String upperBaseClass;
		public final String nameStart;
		public final String plusNameStart;
		public final String minusNameStart;
		public final String baseClassStart;
		public final String plusBaseClassStart;
		public final String minusBaseClassStart;
		public SavedClass(final CharClass charClass, final int startChars)
		{
			C=charClass;
			name=charClass.name();
			upperName=name.toUpperCase();
			nameStart=CMStrings.safeLeft(name.toUpperCase(),startChars);
			plusNameStart="+"+nameStart;
			minusNameStart="-"+nameStart;
			baseClass=charClass.baseClass();
			upperBaseClass=baseClass.toUpperCase();
			baseClassStart=CMStrings.safeLeft(baseClass.toUpperCase(),startChars);
			plusBaseClassStart="+"+baseClassStart;
			minusBaseClassStart="-"+baseClassStart;
		}
	}

	public static class SavedRace
	{
		public final Race R;
		public final String name;
		public final String upperName;
		public final String racialCategory;
		public final String upperCatName;
		public final String nameStart;
		public final String plusNameStart;
		public final String minusNameStart;
		public final String catNameStart;
		public final String plusCatNameStart;
		public final String minusCatNameStart;
		public SavedRace(final Race race, final int startChars)
		{
			R=race;
			name=race.name();
			upperName=name.toUpperCase();
			nameStart=CMStrings.safeLeft(name.toUpperCase(),startChars);
			plusNameStart="+"+nameStart;
			minusNameStart="-"+nameStart;
			racialCategory=race.racialCategory();
			upperCatName=racialCategory.toUpperCase();
			catNameStart=CMStrings.safeLeft(racialCategory.toUpperCase(),startChars);
			plusCatNameStart="+"+catNameStart;
			minusCatNameStart="-"+catNameStart;
		}
	}
}
