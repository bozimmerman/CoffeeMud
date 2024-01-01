package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;
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
import com.planet_ink.coffee_web.interfaces.HTTPOutputConverter;
import com.planet_ink.coffee_web.interfaces.HTTPRequest;

import java.util.*;
/*
   Copyright 2013-2024 Bo Zimmerman

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
 * A helper library for the web server that implements the CMVP pages
 * by interfacing with the web macro system, whereby the web server
 * gains access to the rest of the mud runtime.
 *
 * Most of the methods are helpers also for the macros themselves,
 * providing object caching both short and longer term.
 *
 * @author Bo Zimmerman
 *
 */
public interface WebMacroLibrary extends CMLibrary, HTTPOutputConverter
{
	/**
	 * Does macro filtering on a fake web page by constructing a fake request
	 * and calling the full page filter method.
	 *
	 * @see WebMacroLibrary#virtualPageFilter(String)
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer)
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer, Map, Map)
	 * @see WebMacroLibrary#virtualPageFilter(HTTPRequest, Map, long[], String[], StringBuffer)
	 *
	 * @param data the fake page to filter
	 * @return the filtered page data
	 * @throws HTTPRedirectException any redirect exceptions encountered
	 */
	public byte [] virtualPageFilter(byte [] data) throws HTTPRedirectException;

	/**
	 * Does macro filtering on a fake web page by constructing a fake request
	 * and calling the full page filter method.
	 *
	 * @see WebMacroLibrary#virtualPageFilter(byte[])
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer)
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer, Map, Map)
	 * @see WebMacroLibrary#virtualPageFilter(HTTPRequest, Map, long[], String[], StringBuffer)
	 *
	 * @param data the fake page to filter
	 * @return the filtered page data
	 * @throws HTTPRedirectException any redirect exceptions encountered
	 */
	public String virtualPageFilter(String data) throws HTTPRedirectException;

	/**
	 * Does macro filtering on a fake web page by constructing a fake request
	 * and calling the full page filter method.
	 *
	 * @see WebMacroLibrary#virtualPageFilter(byte[])
	 * @see WebMacroLibrary#virtualPageFilter(String)
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer, Map, Map)
	 * @see WebMacroLibrary#virtualPageFilter(HTTPRequest, Map, long[], String[], StringBuffer)
	 *
	 * @param data the fake page to filter
	 * @return the filtered page data
	 * @throws HTTPRedirectException any redirect exceptions encountered
	 */
	public StringBuffer virtualPageFilter(StringBuffer data) throws HTTPRedirectException;

	/**
	 * Does macro filtering on a fake web page by constructing a fake request
	 * and calling the full page filter method.
	 *
	 * @see WebMacroLibrary#virtualPageFilter(byte[])
	 * @see WebMacroLibrary#virtualPageFilter(String)
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer)
	 * @see WebMacroLibrary#virtualPageFilter(HTTPRequest, Map, long[], String[], StringBuffer)
	 *
	 * @param data the fake page to filter
	 * @param parms fake request parameters, which normally come from the request object
	 * @param objs the object cache for use by macros
	 * @return the filtered page data
	 * @throws HTTPRedirectException any redirect exceptions encountered
	 */
	public StringBuffer virtualPageFilter(final StringBuffer data, Map<String,String> parms, Map<String,Object> objs) throws HTTPRedirectException;

	/**
	 * The official web page filtering method for this output converter.
	 * Does all the macro conversions and so forth.
	 *
	 * @see WebMacroLibrary#virtualPageFilter(byte[])
	 * @see WebMacroLibrary#virtualPageFilter(String)
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer)
	 * @see WebMacroLibrary#virtualPageFilter(StringBuffer, Map, Map)
	 *
	 * @param request the web request that was made
	 * @param objects an empty 'sessions' object map for use by macros as a cache
	 * @param processStartTime a 1-dimensional array with start time-ms, for timeouts
	 * @param lastFoundMacro a 1-dimensional array for storing the last found macro
	 * @param data the page data, which will be modified and returned by the filter
	 * @return the modified and filtered page data
	 * @throws HTTPRedirectException an http redirect exception found
	 */
	public StringBuffer virtualPageFilter(HTTPRequest request, Map<String, Object> objects, long[] processStartTime, String[] lastFoundMacro, StringBuffer data) throws HTTPRedirectException;

	/**
	 * Searches for web-macro commands in the given string and, if found,
	 * disables them and returns the modified string.
	 *
	 * @param s the string to clear or web
	 * @return the modified and cleared string
	 */
	public String clearWebMacros(StringBuffer s);

	/**
	 * Given a stringbuffer and an index into it where an
	 * '@' sign was found, this method will identify a potential
	 * web macro string, and return it, optionally
	 * deleting the '@' characters on internal macros.
	 *
	 * @param s the stringbuffer to match a webmacro in
	 * @param i the index to start looking at
	 * @param lookOnly FALSE to clear '@' on internal macros
	 * @return the found web macro
	 */
	public String parseFoundMacro(StringBuffer s, int i, boolean lookOnly);

	/**
	 * Searches for web-macro commands in the given string and, if found,
	 * disables them and returns the modified string.
	 *
	 * @param s the string to clear or web
	 * @return the modified and cleared string
	 */
	public String clearWebMacros(String s);

	/**
	 * Returns whether the given string resembles a web cache
	 * code, which consists of digits and dashes.
	 *
	 * @param str the string to check
	 * @return true if it resembles a web cache code
	 */
	public boolean isAllNum(final String str);

	/**
	 * Checks the expiration date of the given object, which
	 * must be something from one of the web caches, as those are
	 * the types that have their expiration date used FOR the cache.
	 * It then returns a friendly description of that expiration.
	 *
	 * @param E the mob or item to get an expiration description for
	 * @return an expiration description
	 */
	public String getWebCacheSuffix(final Environmental E);

	/**
	 * Given an item or mob, this will attempt to find its like or match
	 * in the catalog, web caches, or the given item owner, or given
	 * web cache.
	 *
	 * @param E the object to get a code for
	 * @param RorM an item owner
	 * @param classes a web cache
	 * @return the appropriate code for the object
	 */
	public String getAppropriateCode(final PhysicalAgent E, final Physical RorM, final Collection<? extends Physical> classes);

	/**
	 * Adds the given items to the web cache, if each one qualifies to
	 * be so added.
	 *
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param items the collection of items to submit
	 * @return the item web cache again
	 */
	public Collection<Item> contributeItemsToWebCache(final Collection<Item> items);

	/**
	 * Searches the official item web cache for an item just
	 * like the one given and, if found, returns the cache
	 * object.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param I the item to look for like
	 * @return the item from the cache, or null
	 */
	public Item findItemMatchInWebCache(final Item I);

	/**
	 * Given some sort of item-finding code, including catalog codes,
	 * item class ids, catalog codes, web cache codes, and general
	 * canonical java class names, this will return the best item
	 * match it can find.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param MATCHING the item code to look for
	 * @return the item, if found
	 */
	public Item findItemInWebCache(final String MATCHING);

	/**
	 * Given some sort of item-finding code, including catalog codes,
	 * item class ids, catalog codes, web cache codes, and general
	 * canonical java class names, this will return the best item
	 * match it can find.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param allitems a mob, a room, or an item cache, or null
	 * @param MATCHING the item code to look for
	 * @return the item, if found
	 */
	public Item findItemInAnything(final Object allitems, final String MATCHING);

	/**
	 * Searches the catalog for the item with the matching
	 * catalog code and, if found, returns it.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param MATCHING the catalog item code
	 * @return null, or the item
	 */
	public Item getItemFromCatalog(final String MATCHING);

	/**
	 * Searches the catalog and given item web cache for the
	 * item with the given code and, if found, returns it.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param code the item web cache code, or catalog code
	 * @param allitems the item web cache to use
	 * @return null, or the item
	 */
	public Item getItemFromWebCache(final Collection<Item> allitems, String code);

	/**
	 * Searches the catalog and official item web cache for the
	 * item with the given code and, if found, returns it.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param code the item web cache code, or catalog code
	 * @return null, or the item
	 */
	public Item getItemFromWebCache(final String code);

	/**
	 * Give a room item owner, and the item web cache code, this
	 * will attempt to return the matching item.  Also supports
	 * catalog items.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param R the room item owner
	 * @param code the item code
	 * @return null, or the item
	 */
	public Item getItemFromWebCache(final Room R, String code);

	/**
	 * Give a mob item owner, and the item web cache code, this
	 * will attempt to return the matching item.  Also supports
	 * shopkeepers, and catalog items.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param M the mob item owner
	 * @param code the item code
	 * @return null, or the item
	 */
	public Item getItemFromWebCache(final MOB M, String code);

	/**
	 * Given an item and its mob owner, this will return the item web
	 * cache code for the item.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param M the mob to find the item in
	 * @param I the item to get a code for
	 * @return the item web cache code
	 */
	public String findItemWebCacheCode(final MOB M, final Item I);

	/**
	 * Searches the given item web cache for this item, or
	 * one just like it, and returns the code for it.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param I the item to find like
	 * @param allitems the web item cache to use
	 * @return "", or the item web cache code
	 */
	public String findItemWebCacheCode(final Collection<Item> allitems, final Item I);

	/**
	 * Searches the official item web cache for this item, or
	 * one just like it, and returns the code for it.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param I the item to find like
	 * @return "", or the item web cache code
	 */
	public String findItemWebCacheCode(final Item I);

	/**
	 * Given an item and its room owner, this will return the item web
	 * cache code for the item.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param R the room to find the item in
	 * @param I the item to get a code for
	 * @return the item web cache code
	 */
	public String findItemWebCacheCode(final Room R, final Item I);

	/**
	 * Returns whether the given item is exactly in the
	 * official item web cache.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#getItemWebCacheIterable()
	 *
	 * @param I the item to look for
	 * @return true if that item object is in the cache
	 */
	public boolean isWebCachedItem(final Object I);

	/**
	 * Returns an iterable of the item web cache.
	 *
	 * @see WebMacroLibrary#contributeItemsToWebCache(Collection)
	 * @see WebMacroLibrary#findItemMatchInWebCache(Item)
	 * @see WebMacroLibrary#findItemInWebCache(String)
	 * @see WebMacroLibrary#findItemInAnything(Object, String)
	 * @see WebMacroLibrary#getItemFromCatalog(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getItemFromWebCache(String)
	 * @see WebMacroLibrary#getItemFromWebCache(Room, String)
	 * @see WebMacroLibrary#getItemFromWebCache(MOB, String)
	 * @see WebMacroLibrary#findItemWebCacheCode(MOB, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Collection, Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Item)
	 * @see WebMacroLibrary#findItemWebCacheCode(Room, Item)
	 * @see WebMacroLibrary#isWebCachedItem(Object)
	 *
	 * @return an iterable of the item web cache.
	 */
	public Iterable<Item> getItemWebCacheIterable();

	/**
	 * Adds the given mobs to the web cache, if each one qualifies to
	 * be so added.
	 *
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param inhabs the mobs to add to the web cache
	 * @return the mob web cache
	 */
	public Collection<MOB> contributeMOBsToWebCache(final Collection<MOB> inhabs);

	/**
	 * Searches the official web cache for a mob like the
	 * given one, and returns it if found.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param M the mob to find a match for
	 * @return null, or the found cached mob
	 */
	public MOB findMOBMatchInWebCache(final MOB M);

	/**
	 * Given some sort of mob-finding code, including catalog codes,
	 * mob class ids, catalog codes, web cache codes, and general
	 * canonical java class names, this will return the best mob
	 * match it can find.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param MATCHING the mob finding code
	 * @return the mob found, or null
	 */
	public MOB getMOBFromAnywhere(final String MATCHING);

	/**
	 * If the given mob cache code is a catalog code, this will return
	 * the mob from the catalog, if found.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param MATCHING the catalog cache code
	 * @return null, or the found mob
	 */
	public MOB getMOBFromCatalog(final String MATCHING);

	/**
	 * Searches the given mob web cache for the mob of the given code,
	 * which might be a catalog code also.  If found, the mob is returned.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param allmobs your mob web cache
	 * @param code the mob cache code
	 * @return null, or the found mob
	 */
	public MOB getMOBFromWebCache(final Collection<MOB> allmobs, String code);

	/**
	 * Searches the official mob web cache for the mob of the given code,
	 * which might be a catalog code also.  If found, the mob is returned.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param code the mob code to get a mob for
	 * @return null, or the found mob
	 */
	public MOB getMOBFromWebCache(final String code);

	/**
	 * Finds the mob in the given room with the given web cache code and
	 * returns the mob object from the cache.  If the Room is null, the
	 * normal cache is searched.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param R the room the mob with the code must be in
	 * @param code the mob cache code
	 * @return null, or the mob matching the code
	 */
	public MOB getMOBFromWebCache(final Room R, String code);

	/**
	 * Given your own mob cache, this will find the mob just like the given
	 * one and return its code.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param mobs the mob cache
	 * @param M the mob to look for
	 * @return the mobs cache code, or ""
	 */
	public String findMOBWebCacheCode(final Collection<MOB> mobs, final MOB M);

	/**
	 * Searches for a mob in the web cache that is just like the given mob,
	 * and returns the cache code.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param M the mob to search for
	 * @return the mobs cache code, or ""
	 */
	public String findMOBWebCacheCode(final MOB M);

	/**
	 * Returns the room-context cache code for the given mob, if it
	 * is really in the room.  This does a pure object comparison,
	 * so mob copies wouldn't work.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param R the room to look in
	 * @param M the mob to get a code for
	 * @return the mob web cache code or ""
	 */
	public String findMOBWebCacheCode(final Room R, final MOB M);

	/**
	 * Returns whether the given mob is in the web cache.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#getMOBWebCacheIterable()
	 *
	 * @param M the mob to look for
	 * @return true if its in the cache, false otherwise
	 */
	public boolean isWebCachedMOB(final Object M);

	/**
	 * Returns an iterator through the mob web cache.
	 *
	 * @see WebMacroLibrary#contributeMOBsToWebCache(Collection)
	 * @see WebMacroLibrary#findMOBMatchInWebCache(MOB)
	 * @see WebMacroLibrary#getMOBFromAnywhere(String)
	 * @see WebMacroLibrary#getMOBFromCatalog(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Collection, String)
	 * @see WebMacroLibrary#getMOBFromWebCache(String)
	 * @see WebMacroLibrary#getMOBFromWebCache(Room, String)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Collection, MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(MOB)
	 * @see WebMacroLibrary#findMOBWebCacheCode(Room, MOB)
	 * @see WebMacroLibrary#isWebCachedMOB(Object)
	 *
	 * @return an iterator through the mob web cache.
	 */
	public Iterable<MOB> getMOBWebCacheIterable();
}
