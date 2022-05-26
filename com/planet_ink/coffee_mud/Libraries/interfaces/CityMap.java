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
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
/*
   Copyright 2005-2022 Bo Zimmerman

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
public interface CityMap extends CMLibrary
{
	public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch);
	public Enumeration<Auctioneer> auctionHouses();
	public void addAuctionHouse(final Auctioneer newOne);
	public void delAuctionHouse(final Auctioneer oneToDel);

	public PostOffice getPostOffice(String chain, String areaNameOrBranch);
	public Enumeration<PostOffice> postOffices();
	public void addPostOffice(final PostOffice newOne);
	public void delPostOffice(final PostOffice oneToDel);

	public Banker getBank(String chain, String areaNameOrBranch);
	public Enumeration<Banker> banks();
	public Iterator<String> bankChains(Area AreaOrNull);
	public void addBank(final Banker newOne);
	public void delBank(final Banker oneToDel);

	public int numLibraries();
	public Librarian getLibrary(String chain, String areaNameOrBranch);
	public Enumeration<Librarian> libraries();
	public Iterator<String> libraryChains(Area AreaOrNull);
	public void addLibrary(final Librarian newOne);
	public void delLibrary(final Librarian oneToDel);

	/**
	 * Register a particular room as infused to the given deity.
	 * @see WorldMap#deregisterHolyPlace(String, Places)
	 * @see WorldMap#holyPlaces(String)
	 * @param deityName the deity to register
	 * @param newOne the new holy place
	 */
	public void registerHolyPlace(final String deityName, final Places newOne);
	/**
	 * De-register a particular room that was probably infused
	 * to the given deity.
	 * @see WorldMap#registerHolyPlace(String, Places)
	 * @param deityName the deity to de-register
	 * @param oldOne the old holy place for this deity
	 */
	public void deregisterHolyPlace(final String deityName, final Places oldOne);

	/**
	 * Enumerate the holy places for the given deity.
	 * @see WorldMap#registerHolyPlace(String, Places)
	 * @see WorldMap#deregisterHolyPlace(String, Places)
	 * @param deityName the deity to list for
	 * @return the holy places for the deity.
	 */
	public Enumeration<Places> holyPlaces(final String deityName);
}
