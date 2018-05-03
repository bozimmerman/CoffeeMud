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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
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
public interface DiceLibrary extends CMLibrary
{
	public boolean normalizeAndRollLess(int score);
	public int normalizeBy5(int score);
	public int rollHP(int level, int code);
	public int getHPCode(String str);
	public int getHPCode(int roll, int dice, int plus);
	public int[] getHPBreakup(int level, int code);
	public int roll(int number, int die, int modifier);
	public Object pick(Object[] set, Object not);
	public Object pick(Object[] set);
	public int pick(int[] set, int not);
	public int pick(int[] set);
	public Object doublePick(Object[][] set);
	public Object pick(List<? extends Object> set);
	public int rollPercentage();
	public int rollNormalDistribution(int number, int die, int modifier);
	public int rollLow(int number, int die, int modifier);
	public Random getRandomizer();
	public long plusOrMinus(final long range);
	public int plusOrMinus(final int range);
	public int inRange(final int min, final int max);
	public long inRange(final long min, final long max);
	public void scramble(List<?> objs);
	public void scramble(int[] objs);
}
