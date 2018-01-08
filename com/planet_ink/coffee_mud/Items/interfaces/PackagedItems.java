package com.planet_ink.coffee_mud.Items.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
/**
 * An interface for a large number of Items packaged into 
 * a single Item object.   There are methods unPackaging one or more, 
 * and so forth.  It's similar to a container, but without some of the
 * deep engine features at the expense of less CPU usage.
 * @author Bo Zimmerman
 *
 */
public interface PackagedItems extends Item
{
	public static final int PACKAGE_FLAG_TO_ITEMS_PROGRAMMATICALLY=1;

	/**
	 * Puts the given item into the package, and records the
	 * number of times it is repeated in the package.
	 * @see PackagedItems#unPackage(int)
	 * @see PackagedItems#isPackagable(List)
	 * @param I the item to put in the package 
	 * @param number the number of times the item repeats
	 * @return true if the packaging went well, false otherwise
	 */
	public boolean packageMe(Item I, int number);
	
	/**
	 * Returns whether the given list of items can be held
	 * by this package.  Some require all items to be identical
	 * while others are more flexible.  This method should be
	 * called before trying to package anything.
	 * @param V the list of items to test
	 * @return true if the entire list can be packaged, and false otherwise
	 */
	public boolean isPackagable(List<Item> V);
	
	/**
	 * Unpackages the top number of items in the package and returns
	 * them in an item list.  If this results in 0 items in the 
	 * package, the package is destroyed!
	 * @param number the number of items to unpackage
	 * @return the list of items unpackaged
	 */
	public List<Item> unPackage(int number);
	
	/**
	 * Returns the total number of items in the package.
	 * @return the total number of items in the package.
	 */
	public int numberOfItemsInPackage();
	
	/**
	 * Returns the first item in the package, as a peek.
	 * It does not affect the contents of the package.
	 * The item should be destroyed after inspection.
	 * @return the first item in the package, or null
	 */
	public Item peekFirstItem();
	
	/**
	 * For packages that hold only one type of item, this
	 * is a quick method to alter the number in the package.
	 * It is otherwise harmful.
	 * @param number the new number of items in the package
	 */
	public void setNumberOfItemsInPackage(int number);
	
	/**
	 * Returns whether this package contains identical items.
	 * @return true if this package contains identical items
	 */
	public boolean areAllItemsTheSame();
	
	/**
	 * Returns the contents of the package as an XML doc.
	 * @see PackagedItems#setPackageText(String)
	 * @return  the contents of the package as an XML doc
	 */
	public String packageText();
	
	/**
	 * Sets the contents of the package from an XML doc.
	 * @see PackagedItems#packageText()
	 * @param text the contents of the package as an XML doc
	 */
	public void setPackageText(String text);
	
	/**
	 * Returns the flag bitmap describing how this item behaves.
	 * @see PackagedItems#PACKAGE_FLAG_TO_ITEMS_PROGRAMMATICALLY
	 * @see PackagedItems#setPackageFlagsBitmap(int)
	 * @return  the flag bitmap describing how this item behaves.
	 */
	public int getPackageFlagsBitmap();

	/**
	 * Sets the flag bitmap describing how this item behaves.
	 * @see PackagedItems#PACKAGE_FLAG_TO_ITEMS_PROGRAMMATICALLY
	 * @see PackagedItems#getPackageFlagsBitmap()
	 * @param bitmap the flag bitmap describing how this item behaves.
	 */
	public void setPackageFlagsBitmap(int bitmap);
}
