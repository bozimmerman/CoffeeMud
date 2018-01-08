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
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Libraries.Socials;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.util.*;
/*
   Copyright 2006-2018 Bo Zimmerman

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
public interface MaterialLibrary extends CMLibrary
{
	public Environmental unbundle(Item I, int number, Container C);
	public Environmental splitBundle(Item I, int size, Container C);
	public int getMaterialRelativeInt(String s);
	public int getMaterialCode(String s, boolean exact);
	public int getResourceCode(String s, boolean exact);
	public String getResourceDesc(int MASK);
	public String getMaterialDesc(int MASK);
	public Item makeItemResource(int type);
	public PhysicalAgent makeResource(int myResource, String localeCode, boolean noAnimals, String fullName);
	public void addEffectsToResource(Item I);
	public int getRandomResourceOfMaterial(int material);
	public boolean rebundle(Item I);
	public boolean quickDestroy(Item I);
	public int destroyResourcesValue(MOB E, int howMuch, int finalMaterial, int otherMaterial, Item never);
	public int destroyResourcesValue(Room E, int howMuch, int finalMaterial, int otherMaterial, Item never);
	public int destroyResourcesValue(List<Item> V, int howMuch, int finalMaterial, int otherMaterial, Item never, Container C);
	public int destroyResourcesAmt(MOB E, int howMuch, int finalMaterial,Container C);
	public int destroyResourcesAmt(Room E, int howMuch, int finalMaterial,Container C);
	public int destroyResourcesAmt(List<Item> V, int howMuch, int finalMaterial,Container C);
	public Item fetchFoundOtherEncoded(Room E, String otherRequired);
	public Item fetchFoundOtherEncoded(MOB E, String otherRequired);
	public Item findMostOfMaterial(Room E, int material);
	public Item findMostOfMaterial(MOB E, int material);
	public int findNumberOfResource(Room E, int resource);
	public int findNumberOfResource(MOB E, int resource);
	public Item findMostOfMaterial(Room E, String other);
	public Item findMostOfMaterial(MOB E, String other);
	public Item findFirstResource(Room E, int resource);
	public Item findFirstResource(MOB E, int resource);
	public Item findFirstResource(Room E, String other);
	public Item findFirstResource(MOB E, String other);
	public void adjustResourceName(Item I);
	public String genericType(Item I);

	/**
	 * Returns the number of ticks that the given item, whatever
	 * will burn, or 0 if it won't burn.
	 * @param E the item to check
	 * @return the number of ticks to burn, or 0
	 */
	public int getBurnDuration(Environmental E);
}
