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
   Copyright 2005-2023 Bo Zimmerman

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
 * Providing data in a readable format is an important part of
 * the user interface, especially for Archons who need access
 * to charts and data for their work.  Making such data and lists
 * readable is the purpose of this library.
 *
 * This library does Not manage english syntax, or parsable
 * data protocol strings, but charts and graph type tables.
 *
 * @author Bo Zimmerman
 *
 */
public interface ListingLibrary extends CMLibrary
{
	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * a list of items, mobs, or exits (usually), optional xml/html tags, and various
	 * flags to affect what property of the objects is shown, this will generate
	 * a single column list of the given things.  Enforces maximum sized list.
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param items the items, exits, mobs, whatever
	 * @param useName true to use names, false for display text
	 * @param tag null, or an optional tag to include
	 * @param tagParm null, * for item name, or thing to append to tag
	 * @param longLook true to bypass max, and shows container contents
	 * @param compress true to return paragraph, false is the normal list
	 * @return a list of things
	 */
	public String lister(final MOB viewerM, final List<? extends Environmental> items, final boolean useName,
						 final String tag, final String tagParm, final boolean longLook, final boolean compress);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and an map of Ability values whose IDs/class IDs will be the values, this will
	 * create a three-column table.  An optional domain/ability code filter is provided.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration, int)
	 * @see ListingLibrary#build3ColTable(MOB, Vector, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the objects whose IDs to show
	 * @param ofType -1, or a domain/ability type filter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Map<String, Ability> these, int ofType);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and a vector of objects whose IDs/class IDs will be the values, this will
	 * create a three-column table.  An optional domain/ability code filter is provided.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration, int)
	 * @see ListingLibrary#build3ColTable(MOB, Map, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the objects whose IDs to show
	 * @param ofType -1, or a domain/ability type filter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Vector<Ability> these, int ofType);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and an enumeration of objects whose IDs/class IDs will be the values, this will
	 * create a three-column table.  An optional domain/ability code filter is provided.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Vector, int)
	 * @see ListingLibrary#build3ColTable(MOB, Map, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the objects whose IDs to show
	 * @param ofType -1, or a domain/ability type filter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Enumeration<Ability> these, int ofType);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and an map of object values whose IDs/class IDs will be the values, this will
	 * create a three-column table.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration)
	 * @see ListingLibrary#build3ColTable(MOB, Vector)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the objects whose IDs to show
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Map<String,? extends Object> these);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and a vector of objects whose IDs/class IDs will be the values, this will
	 * create a three-column table.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration)
	 * @see ListingLibrary#build3ColTable(MOB, Map)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the objects whose IDs to show
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Vector<? extends Object> these);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and an enumeration of objects whose IDs/class IDs will be the values, this will
	 * create a three-column table.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Vector)
	 * @see ListingLibrary#build3ColTable(MOB, Map)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the objects whose IDs to show
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Enumeration<? extends Object> these);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * a map of mapped room values whose ID()s show in a three-column table, and a room to
	 * use as a filter, showing only the objects whose area has the same name as the filter
	 * room, this will create a three-column table
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration, Room)
	 * @see ListingLibrary#build3ColTable(MOB, Vector, Room)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the map of room objects whose IDs to show
	 * @param likeRoom the room whose area serves as a filter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Map<String, Room> these, Room likeRoom);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * a vector of Room ID()s to show in a three-column table, and a room to
	 * use as a filter, showing only the objects whose area has the same name as the filter
	 * room, this will create a three-column table
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration, Room)
	 * @see ListingLibrary#build3ColTable(MOB, Map, Room)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the list of room IDs to show
	 * @param likeRoom the room whose area serves as a filter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Vector<Room> these, Room likeRoom);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * an enumeration of Room ID()s to show in a three-column table, and a room to
	 * use as a filter, showing only the objects whose area has the same name as the filter
	 * room, this will create a three-column table
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Vector, Room)
	 * @see ListingLibrary#build3ColTable(MOB, Map, Room)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the list of room IDs to show
	 * @param likeRoom the room whose area serves as a filter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Enumeration<Room> these, Room likeRoom);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * a map of Values to show in a three-column table, some optional filters
	 * to choose which objects and shown, and a ListStringer to convert the objects
	 * to strings, this will build the three-column table of strings taken only
	 * from the values of the map.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration, Filterer[], ListStringer)
	 * @see ListingLibrary#build3ColTable(MOB, Vector, Filterer[], ListStringer)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the map of objects to show -- values only
	 * @param filters null, or filters to choose which objects to show
	 * @param stringer required object to string converter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Map<String,? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * a vector of objects to show in a three-column table, some optional filters
	 * to choose which objects and shown, and a ListStringer to convert the objects
	 * to strings, this will build the three-column table of strings.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Enumeration, Filterer[], ListStringer)
	 * @see ListingLibrary#build3ColTable(MOB, Map, Filterer[], ListStringer)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the list of objects to show
	 * @param filters null, or filters to choose which objects to show
	 * @param stringer required object to string converter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Vector<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * an enumeration of objects to show in a three-column table, some optional filters
	 * to choose which objects and shown, and a ListStringer to convert the objects
	 * to strings, this will build the three-column table of strings.
	 *
	 * @see ListingLibrary#build3ColTable(MOB, Map, Filterer[], ListStringer)
	 * @see ListingLibrary#build3ColTable(MOB, Vector, Filterer[], ListStringer)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the list of objects to show
	 * @param filters null, or filters to choose which objects to show
	 * @param stringer required object to string converter
	 * @return a three column table
	 */
	public String build3ColTable(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and an enumeration of objects to show in a two-column table, this will convert
	 * the objects to ID (or class IDs) in a two column table.
	 *
	 * @see ListingLibrary#build2ColTable(MOB, Enumeration, Filterer[], ListStringer)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the list of objects to show
	 * @return a two column table
	 */
	public String build2ColTable(MOB viewerM, Enumeration<? extends Object> these);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * an enumeration of objects to show in a two-column table, some optional filters
	 * to choose which objects and shown, and a ListStringer to convert the objects
	 * to strings, this will build the two-column table of strings.
	 *
	 * @see ListingLibrary#build2ColTable(MOB, Enumeration)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param these the list of objects to show
	 * @param filters null, or filters to choose which objects to show
	 * @param stringer required object to string converter
	 * @return a two column table
	 */
	public String build2ColTable(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);

	/**
	 * To support publishing CoffeeMud engine data to wiki's, this method exists to produce tables
	 * that conform to open-wiki type formats.  One or more filters can be sent to choose which
	 * objects are shown.  The ID of each object is shown, unless the name is requested.
	 *
	 * @see ListingLibrary#buildWikiList(Enumeration, int)
	 *
	 * @param these an enumeration of objects to make a table from
	 * @param filters filters to choose which objects can be shown
	 * @param includeName true to show the name, false to use the ID
	 * @return the wiki table
	 */
	public String buildWikiList(Enumeration<? extends CMObject> these, Filterer<Object>[] filters, boolean includeName);

	/**
	 * To support publishing CoffeeMud engine data to wiki's, this method exists to produce tables
	 * that conform to open-wiki type formats.  The name() of each object is shown.  For Abilities,
	 * a filter for the domain and ability type can be sent.
	 *
	 * @see ListingLibrary#buildWikiList(Enumeration, int)
	 *
	 * @param these an enumeration of objects to make a table from
	 * @param ofType -1, or a domain/ability type filter
	 * @return the wiki table
	 */
	public String buildWikiList(Enumeration<? extends CMObject> these, int ofType);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and a list of identically classed strings, this will produce a table with the
	 * four columns.
	 *
	 * @see ListingLibrary#build4ColTable(MOB, List)
	 * @see ListingLibrary#build4ColTable(MOB, List, String)
	 * @see ListingLibrary#build3ColTable(MOB, List)
	 * @see ListingLibrary#build3ColTable(MOB, List, String)
	 * @see ListingLibrary#buildNColTable(MOB, List, String, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param reverseList the list of strings to make a columned list from
	 * @return the full table
	 */
	public String build4ColTable(MOB viewerM, List<String> reverseList);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and a list of identically classed strings, this will produce a table with the
	 * four columns.  An optional xml/html tag can wrap the values.
	 *
	 * @see ListingLibrary#build4ColTable(MOB, List)
	 * @see ListingLibrary#build4ColTable(MOB, List, String)
	 * @see ListingLibrary#build3ColTable(MOB, List)
	 * @see ListingLibrary#build3ColTable(MOB, List, String)
	 * @see ListingLibrary#buildNColTable(MOB, List, String, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param reverseList the list of strings to make a columned list from
	 * @param tag null, or a tag to wrap the string in
	 * @return the full table
	 */
	public String build4ColTable(MOB viewerM, List<String> reverseList, String tag);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and a list of identically classed strings, this will produce a table with the
	 * three columns.
	 *
	 * @see ListingLibrary#build4ColTable(MOB, List)
	 * @see ListingLibrary#build4ColTable(MOB, List, String)
	 * @see ListingLibrary#build3ColTable(MOB, List)
	 * @see ListingLibrary#build3ColTable(MOB, List, String)
	 * @see ListingLibrary#buildNColTable(MOB, List, String, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param reverseList the list of strings to make a columned list from
	 * @return the full table
	 */
	public String build3ColTable(MOB viewerM, List<String> reverseList);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and a list of identically classed strings, this will produce a table with the
	 * three columns.  An optional xml/html tag can wrap the values.
	 *
	 * @see ListingLibrary#build4ColTable(MOB, List)
	 * @see ListingLibrary#build4ColTable(MOB, List, String)
	 * @see ListingLibrary#build3ColTable(MOB, List)
	 * @see ListingLibrary#build3ColTable(MOB, List, String)
	 * @see ListingLibrary#buildNColTable(MOB, List, String, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param reverseList the list of strings to make a columned list from
	 * @param tag null, or a tag to wrap the string in
	 * @return the full table
	 */
	public String build3ColTable(MOB viewerM, List<String> reverseList, String tag);

	/**
	 * Given a viewer player mob from whose sessions the line wrap column will be used,
	 * and a list of identically classed strings, this will produce a table with the
	 * given number of columns.  An optional xml/html tag can wrap the values.
	 *
	 * @see ListingLibrary#build4ColTable(MOB, List)
	 * @see ListingLibrary#build4ColTable(MOB, List, String)
	 * @see ListingLibrary#build3ColTable(MOB, List)
	 * @see ListingLibrary#build3ColTable(MOB, List, String)
	 * @see ListingLibrary#buildNColTable(MOB, List, String, int)
	 *
	 * @param viewerM the player mob to use for session settings
	 * @param reverseList the list of strings to make a columned list from
	 * @param tag null, or a tag to wrap the string in
	 * @param numCols the number of columns to put the data into
	 * @return the full table
	 */
	public String buildNColTable(MOB viewerM, List<String> reverseList, String tag, int numCols);

	/**
	 * Returns a default ListStringer object-to-string converter that
	 * will return the class ID of the given object, unless it is
	 * an Ability, Class, or Race, in which case it returns the ID
	 * with an asterisk if it is generic.
	 *
	 * @see ListingLibrary.ListStringer
	 *
	 * @return default ListStringer
	 */
	public ListStringer getListStringer();

	/**
	 * Given a column width, which is expect to be a percent of 78,
	 * this method will scale the value according to the given
	 * mob's session's user-preferred total wrap width.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getWrap()
	 * @see ListingLibrary#fixColWidth(double, double)
	 * @see ListingLibrary#fixColWidth(double, Session)
	 * @see ListingLibrary#fixColWidths(int[], Session)
	 *
	 * @param colWidth the column width of 78
	 * @param mob the mob to take the session wrap from
	 * @return the new column width
	 */
	public int fixColWidth(final double colWidth, final MOB mob);

	/**
	 * Given a column width, which is expect to be a percent of 78,
	 * this method will scale the value according to the given
	 * session's user-preferred total wrap width.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getWrap()
	 * @see ListingLibrary#fixColWidth(double, double)
	 * @see ListingLibrary#fixColWidth(double, MOB)
	 * @see ListingLibrary#fixColWidths(int[], Session)
	 *
	 * @param colWidth the column width of 78
	 * @param session the session to get wrap length from
	 * @return the new column width
	 */
	public int fixColWidth(final double colWidth, final Session session);

	/**
	 * Given a column width, which is expect to be a percent of 78,
	 * this method will scale the value according to the given
	 * user-preferred total wrap width.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getWrap()
	 * @see ListingLibrary#fixColWidth(double, MOB)
	 * @see ListingLibrary#fixColWidth(double, Session)
	 * @see ListingLibrary#fixColWidths(int[], Session)
	 *
	 * @param colWidth the column width of 78
	 * @param totalWidth the new total width (not 78)
	 * @return the new column width
	 */
	public int fixColWidth(final double colWidth, final double totalWidth);

	/**
	 * Given a set of column widths, which are expected to add up to 78,
	 * this method will scale the values of those widths according to the
	 * given sessions line width preferences.
	 *
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Session#getWrap()
	 * @see ListingLibrary#fixColWidth(double, double)
	 * @see ListingLibrary#fixColWidth(double, MOB)
	 * @see ListingLibrary#fixColWidth(double, Session)
	 *
	 * @param colWidths the column widths
	 * @param session the session to get wrap length from
	 */
	public void fixColWidths(final int[] colWidths, final Session session);

	/**
	 * An interface to provide a custom conversion from an arbitrary
	 * object type to a string, which might include String to String
	 * conversions.  These interfaces are supported by various
	 * methods in the listing library.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface ListStringer
	{
		/**
		 * Convert the given object into a string,
		 * usually for a chart or graph.
		 *
		 * @param o the object to convert
		 * @return the string to display
		 */
		public String stringify(Object o);
	}
}
