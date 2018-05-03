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
   Copyright 2008-2018 Bo Zimmerman

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
public interface ItemBalanceLibrary extends CMLibrary
{
	public int timsLevelCalculator(Item I);
	public int timsLevelCalculator(Item I, Ability ADJ, Ability RES, Ability CAST, int castMul);
	public boolean fixRejuvItem(Item I);
	public void toneDownWeapon(Weapon W, Ability ADJ);
	public void toneDownArmor(Armor A, Ability ADJ);
	public boolean toneDownValue(Item I);
	public int timsBaseLevel(Item I);
	public void balanceItemByLevel(Item I);
	public int levelsFromCaster(Item savedI, Ability CAST);
	public int levelsFromAdjuster(Item savedI, Ability ADJ);
	public boolean itemFix(Item I, int lvlOr0, StringBuffer changes);
	public Ability[] getTimsAdjResCast(Item I, int[] castMul);
	public Item enchant(Item I, int pct);
	public int levelsFromAbility(Item savedI);
	public Map<String, String> timsItemAdjustments(Item I,
										 int level,
										 int material,
										 int hands,
										 int wclass,
										 int reach,
										 long worndata);
}
