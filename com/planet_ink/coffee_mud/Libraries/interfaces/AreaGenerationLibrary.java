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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Libraries.layouts.AbstractLayout;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
/**
 * The AreaGenerationLibrary is the random generator, which takes a special
 * flavor of xml as input to randomly generate anything from strings, to
 * mobs and items, whole populated rooms, or entire areas.  Most of the methods
 * in this library provide a finer control to the generation process. 
 * @author Bo Zimmerman
 */
public interface AreaGenerationLibrary extends CMLibrary
{
	/**
	 * Given an area-generation xml file, this method will return all the defined tag ids. This
	 * allows the caller to specifically select one of them for generation.
	 * @param xmlRoot the root of the area-generation xml file
	 * @param defined a map of ids to objects, whether strings or tags.
	 */
	public void buildDefinedIDSet(List<XMLTag> xmlRoot, Map<String,Object> defined);

	/**
	 * Given a specific ITEM generation tag, this method will return the items selected
	 * by that tag piece.
	 * @see AreaGenerationLibrary#buildDefinedIDSet(List, Map)
	 * @param piece the identified tag that can return items
	 * @param defined the defined id set from the entire xml document
	 * @return the list of items generated from the tag.
	 * @throws CMException any parsing or generation errors
	 */
	public List<Item> findItems(XMLTag piece, Map<String,Object> defined) throws CMException;

	/**
	 * Given a specific MOB generation tag, this method will return the MOBs selected
	 * by that tag piece.
	 * @see AreaGenerationLibrary#buildDefinedIDSet(List, Map)
	 * @param piece the identified tag that can return MOBs
	 * @param defined the defined id set from the entire xml document
	 * @return the list of MOBs generated from the tag.
	 * @throws CMException any parsing or generation errors
	 */
	public List<MOB> findMobs(XMLTag piece, Map<String,Object> defined) throws CMException;

	/**
	 * Returns a string of the given tag name type, from the given top-level xml tag piece that
	 * resolves to a string, and with the given pre-defined id set.
	 * @see AreaGenerationLibrary#buildDefinedIDSet(List, Map)
	 * @param tagName the name of the string tag
	 * @param piece the top level piece, probably of type tagname
	 * @param defined the pre-defined id set from the entire xml document
	 * @return the string this resolves to
	 * @throws CMException any parsing or generation errors
	 */
	public String findString(String tagName, XMLTag piece, Map<String,Object> defined) throws CMException;

	/**
	 * Given a specific ROOM generation tag, this method will return the room selected
	 * by that tag piece, with the entrace to it being in the given direction
	 * @see AreaGenerationLibrary#buildDefinedIDSet(List, Map)
	 * @param piece the identified tag that can return a room
	 * @param defined the defined id set from the entire xml document
	 * @param exits pre-defined exits from this room, if any
	 * @param direction the direction of entrance to this room
	 * @return the room generated from the tag.
	 * @throws CMException any parsing or generation errors
	 */
	public Room buildRoom(XMLTag piece, Map<String,Object> defined, Exit[] exits, int direction) throws CMException;

	/**
	 * Does nothing but check the requirements to build the given xml tag piece, and compares it with
	 * the variables in the given id definition map, to see if all requirements are met.  If not,
	 * it throws an exception.
	 * @param piece the xml tag piece you want to build
	 * @param defined the defined id set from the entire xml document, and the user
	 * @throws CMException any parsing or generation errors
	 */
	public void checkRequirements(XMLTag piece, Map<String,Object> defined) throws CMException;

	/**
	 * Check the requirements to build the given xml tag piece, and compares it with
	 * the variables in the given id definition map, to see if all requirements are met.  If not,
	 * it returns a map of the undefined or mis-defined ids as keys, mapped to the data type name.
	 * @param defined the defined id set from the entire xml document, and the user
	 * @param piece the xml tag piece you want to build
	 * @return a map of the undefined or mis-defined ids as keys, mapped to the data type name
	 */
	public Map<String,String> getUnfilledRequirements(Map<String,Object> defined, XMLTag piece);
	
	/**
	 * Given a specific AREA generation tag, this method will return the area selected
	 * by that tag piece, with the entrance to it being in the given direction
	 * @see AreaGenerationLibrary#buildDefinedIDSet(List, Map)
	 * @param piece the identified tag that can return a room
	 * @param defined the defined id set from the entire xml document
	 * @param directions the direction of entrance to this room
	 * @return the area generated from the tag.
	 * @throws CMException any parsing or generation errors
	 */
	public Area findArea(XMLTag piece, Map<String,Object> defined, int directions) throws CMException;
	
	/**
	 * Given a specific AREA generation tag, and an empty area, this method will 
	 * populate the area with rooms from the given tag piece, with the entrance 
	 * to it being in the given direction.
	 * @see AreaGenerationLibrary#buildDefinedIDSet(List, Map)
	 * @param piece the identified tag that can return a room
	 * @param defined the defined id set from the entire xml document
	 * @param A the area to put the rooms into
	 * @param direction the direction of entrance to this room
	 * @return true if everything went well, an exception otherwise
	 * @throws CMException any parsing or generation errors
	 */
	public boolean fillInArea(XMLTag piece, Map<String,Object> defined, Area A, int direction) throws CMException;
	
	/**
	 * Returns the layout manager of the given name.
	 * @param named the name of the layout manager
	 * @return the layout manager of the given name.
	 */
	public LayoutManager getLayoutManager(String named);
	
	/**
	 * Sometimes an object cannot be generated at a given time because some tag which
	 * will be defined later has not yet been defined, and cannot be resolved at
	 * generation time.  In those cases, this method is called with the defined id
	 * list as it was returned during primary area, room, item, mob, or string generation.
	 * The post-processes will be extracted from the defined ids, and then processed
	 * on those objects.
	 * @param defined the defined ids after they've gone through generating an object
	 * @throws CMException any parsing or generation errors
	 */
	public void postProcess(final Map<String,Object> defined) throws CMException;
	
	/**
	 * If an xml tag is selected manually, by something outside the library, then 
	 * certain post-selection processes are not properly done, making generation
	 * based on the tag potentially impossible.  In those cases, before generation
	 * of the object from the selected xml tag piece, this method is called to handle
	 * post-selection processing (called rewarding, as in a reward for being picked).
	 * @param piece the selected xml tag piece
	 * @param defined the defined id set from the entire xml document
	 * @throws CMException any parsing or generation errors
	 */
	public void defineReward(XMLTag piece, Map<String,Object> defined) throws CMException;
	
	/**
	 * If an xml tag is selected manually, by something outside the library, then 
	 * certain pre-selection processes are not properly done, making generation
	 * based on the tag potentially impossible.  In those cases, before generation
	 * of the object from the selected xml tag piece, this method is called to handle
	 * pre-selection processing (called rewarding, as in a reward for being picked).
	 * @param piece the selected xml tag piece
	 * @param defined the defined id set from the entire xml document
	 * @throws CMException any parsing or generation errors
	 */
	public void preDefineReward(XMLTag piece, Map<String,Object> defined) throws CMException;
	
	/**
	 * Given a root xml tag and a tag name, this method will return all matching xml tag pieces.
	 * @param tagName the name of the tag to search for
	 * @param piece the root xml tag piece
	 * @param defined the defined id set from the entire xml document
	 * @return the list of found xml tags that match the tag name
	 * @throws CMException any parsing or generation errors
	 */
	public List<XMLTag> getAllChoices(String tagName, XMLTag piece, Map<String,Object> defined) throws CMException;

	/**
	 * Adjusts the levels of all mobs, items, and mob-items in the room by adjusting 
	 * them from their place in an existing range to a new range.Does not save -- that's up to you.
	 * @param room the room to adjust
	 * @param oldMin current minimum level range for the rooms area
	 * @param oldMax current maximum level range for the rooms area
	 * @param newMin new minimum level range for the rooms area
	 * @param newMax new maximum level range for the rooms area 
	 * @return true if the room was modified, false otherwise.
	 */
	public boolean relevelRoom(Room room, int oldMin, int oldMax, int newMin, int newMax);
	
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
	 * @see AreaGenerationLibrary.LayoutTags
	 * @see AreaGenerationLibrary.LayoutTypes
	 * @see AreaGenerationLibrary.LayoutFlags
	 * @see AreaGenerationLibrary.LayoutRuns
	 * @author Bo Zimmerman
	 *
	 */
	public static interface LayoutNode
	{
		/**
		 * Uses this nodes coordinates, as well as the coordinates
		 * of the given connected node, this method will create a 
		 * link between them of the appropriate direction
		 * see delLink(LayoutNode)
		 * @see AreaGenerationLibrary.LayoutNode#getLink(int)
		 * @see AreaGenerationLibrary.LayoutNode#links()
		 * @see AreaGenerationLibrary.LayoutNode#deLink()
		 * @param to the node to connect this one to.
		 */
		public void crossLink(LayoutNode to);
		
		/**
		 * Removes all directional links between this node and the
		 * given node.
		 * see crossLink(LayoutNode)
		 * @see AreaGenerationLibrary.LayoutNode#getLink(int)
		 * @see AreaGenerationLibrary.LayoutNode#links()
		 * @see AreaGenerationLibrary.LayoutNode#deLink()
		 * @param linkNode the node to de-link from.
		 */
		public void delLink(LayoutNode linkNode);
		
		/**
		 * Returns a link from this node in the given direction.  
		 * crossLink must have already been called to establish the 
		 * link.
		 * see crossLink(LayoutNode)
		 * see delLink(LayoutNode)
		 * @see AreaGenerationLibrary.LayoutNode#links()
		 * @see AreaGenerationLibrary.LayoutNode#deLink()
		 * @param d the direction code
		 * @return the node in the given direction
		 */
		public LayoutNode getLink(int d);
		
		/**
		 * Removes all links from other nodes to this one, and removes
		 * all links from this node to others.  crossLink must have been
		 * called for there to be any links.
		 * see crossLink(LayoutNode)
		 * see delLink(LayoutNode)
		 * @see AreaGenerationLibrary.LayoutNode#links()
		 * @see AreaGenerationLibrary.LayoutNode#getLink(int)
		 */
		public void deLink();
		
		/**
		 * Returns a map of directions to other layoutnodes.  These
		 * being the established links between nodes. crossLink must have been
		 * called for there to be any links.
		 * see crossLink(LayoutNode)
		 * see delLink(LayoutNode)
		 * @see AreaGenerationLibrary.LayoutNode#deLink()
		 * @see AreaGenerationLibrary.LayoutNode#getLink(int)
		 * @return the map of links
		 */
		public Map<Integer, LayoutNode> links();
		
		/**
		 * Returns comma-delimited values of each LayoutTag type,
		 * through a may of layouttag to string mapping.
		 * @see AreaGenerationLibrary.LayoutTags
		 * @return the map of layouttags to strings
		 */
		public Map<LayoutTags, String> tags();
		
		/**
		 * Returns x,y coordinates of this node.
		 * These are generated by the layout manager.
		 * @return x,y coordinates of this node
		 */
		public long[] coord();
		
		/**
		 * Returns true if the first two linked nodes to this
		 * node are in the same general direction, this making
		 * this node look like a street node.
		 * @return true if this node is part of a street.
		 */
		public boolean isStreetLike();
		
		/**
		 * Flags this node with one of the visual layout flags,
		 * telling whether it is at an intersection, or on a
		 * corner, or something like that.
		 * @param flag the flag to set.
		 */
		public void flag(LayoutFlags flag);
		
		/**
		 * Returns whether this node is flagged with one of the
		 * visual layout flags, telling whether it is an intersectino,
		 * or on a corner, or something like that.
		 * see flag(LayoutFlags)
		 * @param flag the flag to check for
		 * @return true if this node is flagged, false otherwise
		 */
		public boolean isFlagged(LayoutFlags flag);
		
		/**
		 * Flags this node as being part of a street-like run.
		 * @see AreaGenerationLibrary.LayoutRuns
		 * @param dirs the direction of the run
		 */
		public void flagRun(LayoutRuns dirs);
		
		/**
		 * Return whether this node is flagged as being part of a 
		 * street-like run.
		 * @see AreaGenerationLibrary.LayoutRuns
		 * @return the direction of the run, or null
		 */
		public LayoutRuns getFlagRuns();
		
		/**
		 * Returns the layout type flag that this node represents,
		 * denoting whether it is a leaf, a street, or some other
		 * role.
		 * @see AreaGenerationLibrary.LayoutTypes
		 * @return the type of this node
		 */
		public LayoutTypes type();
		
		/**
		 * Sets the layout type flag that this node represents,
		 * denoting whether it is a leaf, a street, or some other
		 * role.
		 * @see LayoutTypes
		 * @param type the type of this node
		 */
		public void reType(LayoutTypes type);
		
		/**
		 * Sets the exit available link directions, without setting
		 * the nodes they are linked to -- what good is this? 
		 * Well, it sets the LayoutTags.NODEEXITS string, which
		 * makes some sense I guess.
		 * @param dirs the array of Direction codes to set.
		 */
		public void setExits(int[] dirs);
		
		/**
		 * Returns one line of a 3x3 character representation. Each call
		 * returns 3 characters, with the middle being the roomchar, and
		 * the rest depending on the links.  Call this three times with
		 * 0, 1, and 2, to get all three lines.
		 * @param roomChar the char to use for this room
		 * @param line which line, 0, 1, or 2
		 * @return the 3 character string for this line.
		 */
		public String getColorRepresentation(char roomChar, int line);
		
		/**
		 * Returns the room object assigned to this node.
		 * @see AreaGenerationLibrary.LayoutNode#setRoom(Room)
		 * @return the room object assigned to this node.
		 */
		public Room room();
		
		/**
		 * Sets the room object assigned to this node.
		 * @see AreaGenerationLibrary.LayoutNode#room()
		 * @param room the room object assigned to this node.
		 */
		public void setRoom(Room room);
	}

	/**
	 * Enum of the types of tags that room layoutnodes can
	 * be flagged with.
	 * @see AreaGenerationLibrary.LayoutNode
	 * @author Bo Zimmerman
	 *
	 */
	public enum LayoutTags
	{
		NODERUN,
		NODEFLAGS,
		NODETYPE,
		NODEEXITS
	}

	/**
	 * Enum of the type of room layoutnode.
	 * @see AreaGenerationLibrary.LayoutNode
	 * @author Bo Zimmerman
	 *
	 */
	public enum LayoutTypes
	{
		surround,
		leaf,
		street,
		square,
		interior
	}

	/**
	 * Enum of the type of room layoutnode
	 * @see AreaGenerationLibrary.LayoutNode
	 * @author Bo Zimmerman
	 *
	 */
	public enum LayoutFlags
	{
		corner,
		gate,
		intersection,
		tee,
		offleaf
	}

	/**
	 * If this room layoutnode is a street type, what
	 * direction does it run?
	 * @see AreaGenerationLibrary.LayoutNode
	 * @author Bo Zimmerman
	 *
	 */
	public enum LayoutRuns
	{
		ew,
		ns,
		ud,
		nesw,
		nwse
	}
	
}
