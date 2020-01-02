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
   Copyright 2005-2020 Bo Zimmerman

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
public interface ListingLibrary extends CMLibrary
{
	public String itemSeenString(final MOB viewerM, final Environmental item, final boolean useName, final boolean longLook, final boolean sysMsgs, final boolean compress);
	public int getReps(final MOB viewerM, final Environmental item, final List<? extends Environmental> theRest, final boolean useName, final boolean longLook);
	public void appendReps(final int reps, final StringBuilder say, final boolean compress);
	public StringBuilder lister(final MOB viewerM, final List<? extends Environmental> items, final boolean useName, final String tag, final String tagParm, final boolean longLook, final boolean compress);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, int ofType);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, Room likeRoom);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, int ofType);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, int ofType);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, Room likeRoom);
	public StringBuilder reallyList(MOB viewerM, Map<String,? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder reallyList(MOB viewerM, Vector<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, Room likeRoom);
	public StringBuilder reallyList(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder reallyList2Cols(MOB viewerM, Enumeration<? extends Object> these);
	public StringBuilder reallyList2Cols(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, ListStringer stringer);
	public StringBuilder reallyWikiList(MOB viewerM, Enumeration<? extends Object> these, Filterer<Object>[] filters, boolean includeName);
	public StringBuilder reallyWikiList(MOB viewerM, Enumeration<? extends Object> these, int ofType);
	public StringBuilder fourColumns(MOB viewerM, List<String> reverseList);
	public StringBuilder fourColumns(MOB viewerM, List<String> reverseList, String tag);
	public StringBuilder threeColumns(MOB viewerM, List<String> reverseList);
	public StringBuilder threeColumns(MOB viewerM, List<String> reverseList, String tag);
	public StringBuilder makeColumns(MOB viewerM, List<String> reverseList, String tag, int numCols);
	public ListStringer getListStringer();
	public int fixColWidth(final double colWidth, final MOB mob);
	public int fixColWidth(final double colWidth, final Session session);
	public int fixColWidth(final double colWidth, final double totalWidth);
	public void fixColWidths(final int[] colWidths, final Session session);

	public static interface ListStringer
	{
		public String stringify(Object o);
	}
}
