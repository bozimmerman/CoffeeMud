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
/**
 * For the sake of efficient look-ups, the engine keeps certain kinds of objects
 * cached together, regardless of where they are in the world.  The CityMap does
 * this for objects normally found in cities, such as banks, post offices,
 * auction houses, libraries, etc.
 *
 * @author Bo Zimmerman
 */
public interface CityMap extends CMLibrary
{
	/**
	 * Returns a cached Auctioneer of the given chain, from the given
	 * area.  If the area name is also the same as the chain, it just
	 * returns the first Auctioneer in that chain, otherwise, it only
	 * returns the first Auctioneer whose home is that area name.
	 *
	 * @see CityMap#getAuctionHouse(String, String)
	 * @see CityMap#auctionHouses()
	 * @see CityMap#addAuctionHouse(Auctioneer)
	 * @see CityMap#delAuctionHouse(Auctioneer)
	 *
	 * @param chain name of the chain to return a Auctioneer of
	 * @param areaNameOrBranch name of the area to return the chain Auctioneer in
	 * @return the cached Auctioneer, or null
	 */
	public Auctioneer getAuctionHouse(String chain, String areaNameOrBranch);

	/**
	 * Returns an enumeration of all Auctioneer objects in the
	 * city cache
	 *
	 * @see CityMap#getAuctionHouse(String, String)
	 * @see CityMap#auctionHouses()
	 * @see CityMap#addAuctionHouse(Auctioneer)
	 * @see CityMap#delAuctionHouse(Auctioneer)
	 *
	 * @return an enumeration of all Auctioneer objects
	 */
	public Enumeration<Auctioneer> auctionHouses();

	/**
	 * Adds the given Auctioneer to the city cache
	 *
	 * @see CityMap#getAuctionHouse(String, String)
	 * @see CityMap#auctionHouses()
	 * @see CityMap#addAuctionHouse(Auctioneer)
	 * @see CityMap#delAuctionHouse(Auctioneer)
	 *
	 * @param newOne Auctioneer to add
	 */
	public void addAuctionHouse(final Auctioneer newOne);

	/**
	 * Deletes the given Auctioneer from the city cache
	 *
	 * @see CityMap#getAuctionHouse(String, String)
	 * @see CityMap#auctionHouses()
	 * @see CityMap#addAuctionHouse(Auctioneer)
	 * @see CityMap#delAuctionHouse(Auctioneer)
	 *
	 * @param oneToDel the Auctioneer to delete
	 */
	public void delAuctionHouse(final Auctioneer oneToDel);

	/**
	 * Returns a cached PostOffice of the given chain, from the given
	 * area.  If the area name is also the same as the chain, it just
	 * returns the first PostOffice in that chain, otherwise, it only
	 * returns the first PostOffice whose home is that area name.
	 *
	 * @see CityMap#getPostOffice(String, String)
	 * @see CityMap#postOffices()
	 * @see CityMap#addPostOffice(PostOffice)
	 * @see CityMap#delPostOffice(PostOffice)
	 *
	 * @param chain name of the postal chain to return a Banker of
	 * @param areaNameOrBranch name of the area to return the chain PostOffice in
	 * @return the cached PostOffice, or null
	 */
	public PostOffice getPostOffice(String chain, String areaNameOrBranch);

	/**
	 * An enumeration of all PostOffice objects in the cache
	 *
	 * @see CityMap#getPostOffice(String, String)
	 * @see CityMap#postOffices()
	 * @see CityMap#addPostOffice(PostOffice)
	 * @see CityMap#delPostOffice(PostOffice)
	 *
	 * @return an enumeration of all PostOffice objects in the cache
	 */
	public Enumeration<PostOffice> postOffices();

	/**
	 * Adds the given PostOffice object to the city cache
	 *
	 * @see CityMap#getPostOffice(String, String)
	 * @see CityMap#postOffices()
	 * @see CityMap#addPostOffice(PostOffice)
	 * @see CityMap#delPostOffice(PostOffice)
	 *
	 * @param newOne the PostOffice object to cache
	 */
	public void addPostOffice(final PostOffice newOne);

	/**
	 * Removes the given PostOffice object from the city cache
	 *
	 * @see CityMap#getPostOffice(String, String)
	 * @see CityMap#postOffices()
	 * @see CityMap#addPostOffice(PostOffice)
	 * @see CityMap#delPostOffice(PostOffice)
	 *
	 * @param oneToDel the PostOffice object to remove from the city cache
	 */
	public void delPostOffice(final PostOffice oneToDel);

	/**
	 * Returns a cached Banker of the given chain, from the given
	 * area.  If the area name is also the same as the chain, it just
	 * returns the first Banker in that chain, otherwise, it only
	 * returns the first Banker whose home is that area name.
	 *
	 * @see CityMap#banks()
	 * @see CityMap#bankChains(Area)
	 * @see CityMap#addBank(Banker)
	 * @see CityMap#delBank(Banker)
	 *
	 * @param chain name of the bank chain to return a Banker of
	 * @param areaNameOrBranch name of the area to return the chain Banker in
	 * @return the cached Banker, or null
	 */
	public Banker getBank(String chain, String areaNameOrBranch);

	/**
	 * Returns an enumeration of all Bankers in the cache
	 *
	 * @see CityMap#getBank(String, String)
	 * @see CityMap#bankChains(Area)
	 * @see CityMap#addBank(Banker)
	 * @see CityMap#delBank(Banker)
	 *
	 * @return an enumeration of all Bankers in the cache
	 */
	public Enumeration<Banker> banks();

	/**
	 * Returns an iterator of all bank chains, with a given
	 * optional Area to act as a filter.
	 *
	 * @see CityMap#getBank(String, String)
	 * @see CityMap#banks()
	 * @see CityMap#addBank(Banker)
	 * @see CityMap#delBank(Banker)
	 *
	 * @param areaOrNull null or an Area to act as a filter
	 * @return an iterator of all bank chains
	 */
	public Enumeration<String> bankChains(Area areaOrNull);

	/**
	 * Adds the given Banker to this cache
	 *
	 * @see CityMap#getBank(String, String)
	 * @see CityMap#banks()
	 * @see CityMap#bankChains(Area)
	 * @see CityMap#delBank(Banker)
	 *
	 * @param newOne the Banker to add
	 */
	public void addBank(final Banker newOne);
	/**
	 * Deletes the given Banker from the cache.
	 *
	 * @see CityMap#getBank(String, String)
	 * @see CityMap#banks()
	 * @see CityMap#bankChains(Area)
	 * @see CityMap#addBank(Banker)
	 *
	 * @param oneToDel the Banker to delete
	 */
	public void delBank(final Banker oneToDel);

	/**
	 * Returns the number of cached Librarian objects
	 *
	 * @see CityMap#getLibrary(String, String)
	 * @see CityMap#libraries()
	 * @see CityMap#libraryChains(Area)
	 * @see CityMap#addLibrary(Librarian)
	 * @see CityMap#delLibrary(Librarian)
	 *
	 * @return the number of cached Librarian objects
	 */
	public int numLibraries();

	/**
	 * Returns a cached Librarian of the given chain, from the given
	 * area.  If the area name is also the same as the chain, it just
	 * returns the first Librarian in that chain, otherwise, it only
	 * returns the first Librarian whose home is that area name.
	 *
	 * @see CityMap#numLibraries()
	 * @see CityMap#libraries()
	 * @see CityMap#libraryChains(Area)
	 * @see CityMap#addLibrary(Librarian)
	 * @see CityMap#delLibrary(Librarian)
	 *
	 * @param chain name of the library chain to return a librarian of
	 * @param areaNameOrBranch name of the area to return the chain librarian in
	 * @return null, or the found Librarian
	 */
	public Librarian getLibrary(String chain, String areaNameOrBranch);

	/**
	 * Returns an enumeration of all Librarians in the cache.
	 *
	 * @see CityMap#numLibraries()
	 * @see CityMap#getLibrary(String, String)
	 * @see CityMap#libraryChains(Area)
	 * @see CityMap#addLibrary(Librarian)
	 * @see CityMap#delLibrary(Librarian)
	 *
	 * @return an enumeration of all cached Librarians
	 */
	public Enumeration<Librarian> libraries();

	/**
	 * Given an Area to act as filter, or null for no filter,
	 * this will return an iterator of library chain names.
	 * The Area filter also respects child areas.
	 *
	 * @see CityMap#numLibraries()
	 * @see CityMap#getLibrary(String, String)
	 * @see CityMap#libraries()
	 * @see CityMap#addLibrary(Librarian)
	 * @see CityMap#delLibrary(Librarian)
	 *
	 * @param areaOrNull null, or an Area filter
	 * @return an iterator of library chains
	 */
	public Enumeration<String> libraryChains(Area areaOrNull);

	/**
	 * Add a Librarian to the city cache
	 *
	 * @see CityMap#numLibraries()
	 * @see CityMap#getLibrary(String, String)
	 * @see CityMap#libraries()
	 * @see CityMap#libraryChains(Area)
	 * @see CityMap#delLibrary(Librarian)
	 *
	 * @param newOne the Librarian to add
	 */
	public void addLibrary(final Librarian newOne);

	/**
	 * Delete a Librarian from the city cache.
	 *
	 * @see CityMap#numLibraries()
	 * @see CityMap#getLibrary(String, String)
	 * @see CityMap#libraries()
	 * @see CityMap#libraryChains(Area)
	 * @see CityMap#addLibrary(Librarian)
	 *
	 * @param oneToDel the Librarian to add
	 */
	public void delLibrary(final Librarian oneToDel);

	/**
	 * Register a particular room as infused to the given deity.
	 * @see CityMap#deregisterHolyPlace(String, Places)
	 * @see CityMap#holyPlaces(String)
	 * @param deityName the deity to register
	 * @param newOne the new holy place
	 */
	public void registerHolyPlace(final String deityName, final Places newOne);
	/**
	 * De-register a particular room that was probably infused
	 * to the given deity.
	 * @see CityMap#registerHolyPlace(String, Places)
	 * @see CityMap#holyPlaces(String)
	 * @param deityName the deity to de-register
	 * @param oldOne the old holy place for this deity
	 */
	public void deregisterHolyPlace(final String deityName, final Places oldOne);

	/**
	 * Enumerate the holy places for the given deity.
	 * @see CityMap#registerHolyPlace(String, Places)
	 * @see CityMap#deregisterHolyPlace(String, Places)
	 * @param deityName the deity to list for
	 * @return the holy places for the deity.
	 */
	public Enumeration<Places> holyPlaces(final String deityName);
}
