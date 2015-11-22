package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLpiece;
import com.planet_ink.coffee_mud.Libraries.layouts.AbstractLayout;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
/*
   Copyright 2008-2015 Bo Zimmerman

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
 * The AreaGenerationLibrary is the random generator, which takes a special
 * flavor of xml as input to randomly generate anything from strings, to
 * mobs and items, whole populated rooms, or entire areas.  Most of the methods
 * in this library provide a finer control to the generation process. 
 * @author Bo Zimmerman
 */
public interface AreaGenerationLibrary extends CMLibrary
{
	public void buildDefinedIDSet(List<XMLpiece> xmlRoot, Map<String,Object> defined);
	public List<Item> findItems(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public List<MOB> findMobs(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public String findString(String tagName, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public Room buildRoom(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Exit[] exits, int direction) throws CMException;
	public void checkRequirements(XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public Map<String,String> getUnfilledRequirements(Map<String,Object> defined, XMLLibrary.XMLpiece piece);
	public Area findArea(XMLLibrary.XMLpiece piece, Map<String,Object> defined, int directions) throws CMException;
	public boolean fillInArea(XMLLibrary.XMLpiece piece, Map<String,Object> defined, Area A, int direction) throws CMException;
	public LayoutManager getLayoutManager(String named);
	public void postProcess(final Map<String,Object> defined) throws CMException;
	public void defineReward(Modifiable E, List<String> ignoreStats, String defPrefix, XMLLibrary.XMLpiece piece, String value, Map<String,Object> defined) throws CMException;
	public void preDefineReward(Modifiable E, List<String> ignoreStats, String defPrefix, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;
	public List<XMLpiece> getAllChoices(String tagName, XMLLibrary.XMLpiece piece, Map<String,Object> defined) throws CMException;

	/**
	 * Area generators work by first laying out a set of rooms into a
	 * configuration called a Layout. Layouts are scalable configurations
	 * that can handle any size, or originating direction of entry. 
	 * A Layout Manager then, is an algorithm for generating the room set
	 * given a size and initial direction.  Each Layout Manager then 
	 * generates a layout of rooms according to its kind.
	 * @author Bo Zimmerman
	 *
	 */
	public static interface LayoutManager
	{
		/**
		 * The name of the layout manager
		 * @return name of the layout manager
		 */
		public String name();
		
		/**
		 * Generates a list of layout nodes, each
		 * of which reprents a single room in the area.
		 * The first node is always the entry room, which
		 * is entered from the direction given to this method.
		 * @see AreaGenerationLibrary.LayoutNode
		 * @param num the number of nodes to generate
		 * @param dir the direction of entry into this group of nodes.
		 * @return a list of layout nodes
		 */
		public List<LayoutNode> generate(int num, int dir);
	}

	/**
	 * A layout node represents a single room in a layout
	 * manager.  It contains numerous methods for linking
	 * nodes together the way rooms are, and for flagging
	 * nodes to give some idea of where the node fits in
	 * various layouts.
	 * @author Bo Zimmerman
	 *
	 */
	public static interface LayoutNode
	{
		public void crossLink(LayoutNode to);
		public void delLink(LayoutNode linkNode);
		public LayoutNode getLink(int d);
		public Map<Integer, LayoutNode> links();
		public Map<LayoutTags, String> tags();
		public long[] coord();
		public boolean isStreetLike();
		public void deLink();
		public void flag(LayoutFlags flag);
		public void flagRun(LayoutRuns dirs);
		public boolean isFlagged(LayoutFlags flag);
		public LayoutRuns getFlagRuns();
		public LayoutTypes type();
		public void setExits(int[] dirs);
		public void reType(LayoutTypes type);
		public String getColorRepresentation(char roomChar, int line);
		public Room room();
		public void setRoom(Room room);
	}

	public enum LayoutTags
	{
		NODERUN,
		NODEFLAGS,
		NODETYPE,
		NODEEXITS
	}

	public enum LayoutTypes
	{
		surround,
		leaf,
		street,
		square,
		interior
	}

	public enum LayoutFlags
	{
		corner,
		gate,
		intersection,
		tee,
		offleaf
	}

	public enum LayoutRuns
	{
		ew,
		ns,
		ud,
		nesw,
		nwse
	}
}
