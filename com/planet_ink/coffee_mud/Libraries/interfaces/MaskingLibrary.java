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
	public CompiledZapperMask createEmptyMask();
	
	public static interface CompiledZapperMaskEntry
	{
		public int maskType();
		public Object[] parms();
	}
	
	public static interface CompiledZapperMask
	{
		public boolean[] flags();
		public boolean empty();
		public CompiledZapperMaskEntry[] entries();
	}
}
