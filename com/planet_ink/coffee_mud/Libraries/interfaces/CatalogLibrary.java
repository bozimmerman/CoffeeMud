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
 * The Catalog Library maintains the prototypes of various Items and Mobs
 * who are keyed by their Name.  They can belong to categories to help
 * organize them, but that doesn't change the key requirements.  Individual
 * mobs and items are flagged as actually BEING an instance of a cataloged
 * prototype, and thus will change to reflect prototype changes when they
 * are able.  In addition to all this, certain statistics and information 
 * about catalog usage is maintained by the system.
 * @author Bo Zimmerman
 */
public interface CatalogLibrary extends CMLibrary
{
	/**
	 * Creates a list of all the item keys (names) of all the items
	 * in the catalog, regardless of catagory.
	 * @see CatalogLibrary#getCatalogItemNames(String)
	 * @return a list of all the item keys
	 */
	public String[] getCatalogItemNames();

	/**
	 * Creates a list of all the item keys (names) of all the items
	 * in the catalog in the given catagory.  Send null to get all
	 * items regardless of catagory
	 * @see CatalogLibrary#getCatalogItemNames()
	 * @param catagory the item catagory to filter by
	 * @return a list of all the item keys
	 */
	public String[] getCatalogItemNames(String catagory);

	/**
	 * Creates a list of all the mob keys (names) of all the mobs
	 * in the catalog, regardless of catagory.
	 * @see CatalogLibrary#getCatalogItemNames()
	 * @see CatalogLibrary#getCatalogMobNames(String)
	 * @return a list of all the mob keys
	 */
	public String[] getCatalogMobNames();

	/**
	 * Creates a list of all the mob keys (names) of all the mobs
	 * in the catalog in the given catagory.  Send null to get all
	 * mobs regardless of catagory
	 * @see CatalogLibrary#getCatalogMobNames()
	 * @param catagory the mob catagory to filter by
	 * @return a list of all the mob keys
	 */
	public String[] getCatalogMobNames(String catagory);

	/**
	 * Creates a list of all the catagories that mobs have been
	 * placed in.
	 * @see CatalogLibrary#getItemCatalogCatagories()
	 * @return a list of all the catagories
	 */
	public String[] getMobCatalogCatagories();

	/**
	 * Creates a list of all the catagories that items have been
	 * placed in.
	 * @see CatalogLibrary#getMobCatalogCatagories()
	 * @return a list of all the catagories
	 */
	public String[] getItemCatalogCatagories();

	/**
	 * Creates a list of all the prototype catalog items.
	 * @see CatalogLibrary#getCatalogMobs()
	 * @return a list of all the prototype catalog items.
	 */
	public Item[] getCatalogItems();

	/**
	 * Creates a list of all the prototype catalog mobs.
	 * @see CatalogLibrary#getCatalogItems()
	 * @return a list of all the prototype catalog mobs.
	 */
	public MOB[] getCatalogMobs();

	/**
	 * Returns whether there exists an item/mob in the catalog
	 * of the same type and name/key as the given item or
	 * mob.
	 * @see CatalogLibrary#isCatalogObj(String)
	 * @param E the item or mob
	 * @return true if it's in the catalog, false otherwise
	 */
	public boolean isCatalogObj(Environmental E);

	/**
	 * Returns whether there exists an item or mob in the catalog
	 * of the given name/key. Since the name could be item or mob,
	 * preference is given to mobs.
	 * @see CatalogLibrary#isCatalogObj(Environmental)
	 * @param name the item or mob name
	 * @return true if it's in the catalog, false otherwise
	 */
	public boolean isCatalogObj(String name);

	/**
	 * Returns the cataloged prototype item of the given name.
	 * @see CatalogLibrary#getCatalogItem(String)
	 * @see CatalogLibrary#getCatalogObj(Physical)
	 * @param name the name to look for.
	 * @return the cataloged prototype item
	 */
	public Item getCatalogItem(String name);

	/**
	 * Returns the cataloged prototype mob of the given name.
	 * @see CatalogLibrary#getCatalogMob(String)
	 * @see CatalogLibrary#getCatalogObj(Physical)
	 * @param name the name to look for.
	 * @return the cataloged prototype mob
	 */
	public MOB getCatalogMob(String name);

	/**
	 * Returns the cataloged prototype mob or item of the same type
	 * and with the same name as the given object.
	 * @see CatalogLibrary#getCatalogMob(String)
	 * @see CatalogLibrary#getCatalogItem(String)
	 * @param P the object type and name to look for
	 * @return the cataloged prototype mob or item
	 */
	public Physical getCatalogObj(Physical P);

	/**
	 * Returns the cataloged metadata for the item of the given name
	 * @see CataData
	 * @see CatalogLibrary#getCatalogMobData(String)
	 * @see CatalogLibrary#getCatalogData(Physical)
	 * @param name the name of the cataloged item.
	 * @return the cataloged metadata for the item
	 */
	public CataData getCatalogItemData(String name);

	/**
	 * Returns the cataloged metadata for the mob of the given name
	 * @see CataData
	 * @see CatalogLibrary#getCatalogItemData(String)
	 * @see CatalogLibrary#getCatalogData(Physical)
	 * @param name the name of the cataloged mob.
	 * @return the cataloged metadata for the mob
	 */
	public CataData getCatalogMobData(String name);

	/**
	 * Returns the cataloged metadata for the mob or item of the same type
	 * and with the same name as the given object.
	 * @see CataData
	 * @see CatalogLibrary#getCatalogItemData(String)
	 * @see CatalogLibrary#getCatalogMobData(String)
	 * @param P the object type and name to look for
	 * @return the cataloged metadata for the mob
	 */
	public CataData getCatalogData(Physical P);

	/**
	 * Creates a new catalog item or mob from the given item or mob
	 * in the given catagory. The given object is marked as cataloged
	 * and a copy if submitted to the database and memory.
	 * @see CatalogLibrary#addCatalog(Physical)
	 * @see CatalogLibrary#delCatalog(Physical)
	 * @see CatalogLibrary#submitToCatalog(Physical)
	 * @param catagory the new catagory, such as null
	 * @param PA the item or mob to create in the catalog
	 */
	public void addCatalog(String catagory, Physical PA);

	/**
	 * Creates a new catalog item or mob from the given item or mob
	 * in no catagory. The given object is marked as cataloged
	 * and a copy if submitted to the database and memory.
	 * @see CatalogLibrary#addCatalog(String, Physical)
	 * @see CatalogLibrary#delCatalog(Physical)
	 * @see CatalogLibrary#submitToCatalog(Physical)
	 * @see CatalogLibrary#updateCatalog(Physical)
	 * @param PA the item or mob to create in the catalog
	 */
	public void addCatalog(Physical PA);
	
	/**
	 * If the given item or mob is not yet in the catalog prototype
	 * library, this method will mark it as uncataloged (as is
	 * correct for a prototype), and add it to the items or mobs
	 * list, depending on which is appropriate.  This method, unlike
	 * addCatalog, does not copy the item or add to the database.
	 * @see CatalogLibrary#addCatalog(Physical)
	 * @see CatalogLibrary#delCatalog(Physical)
	 * @see CatalogLibrary#addCatalog(String, Physical)
	 * @param P the item or mob to add to the catalog
	 */
	public void submitToCatalog(Physical P);
	
	/**
	 * Updates the catagory assigned to the given prototype mob or
	 * item to the given catagory.  Catagories do not affect the
	 * requirement for a unique name/key for each mob or item. Unlike
	 * setCatagory, this method is totally synchronized and also 
	 * clears the VFSDir cache.
	 * @see CatalogLibrary#setCategory(Physical, String)
	 * @param modelP the prototype mob or item
	 * @param newCat the catagory to change it into, or null
	 */
	public void updateCatalogCategory(Physical modelP, String newCat);
	
	/**
	 * Changes the catagory of the catalog item with the given
	 * physical objects name to the given catagory. Unlike
	 * updateCatalogCatagory, this method is not synchronized
	 * and does not clear the VFSDir cache
	 * @see CatalogLibrary#updateCatalogCategory(Physical, String)
	 * @param P the catalog item to get a key/name from
	 * @param catagory the new catagory, such as null
	 */
	public void setCategory(Physical P, String catagory);
	
	/**
	 * Completely removes the given item or mob from the catalog, deleting
	 * it from the database, and unsetting the cataloged flag from every
	 * instance in the world, though it does not save the items..
	 * @see CatalogLibrary#addCatalog(Physical)
	 * @see CatalogLibrary#updateCatalog(Physical)
	 * @param P the item or mob whose name and prototype are removed
	 */
	public void delCatalog(Physical P);
	
	/**
	 * When the properties of a cataloged item or mob change, this method
	 * is called to propagate those changes into the catalog prototype,
	 * and then to all the known instances of the cataloged item.  Therefore
	 * this is a potentially expensive call timewise.
	 * @param modelP the updated changed item or mob
	 */
	public void updateCatalog(Physical modelP);
	
	/**
	 * Given an item or mob that might be in the catalog, this method
	 * confirms that fact.  It returns null if all is well, and if there
	 * are differences between this object and the cataloged one, it
	 * returns a string list of the stats that differ.
	 * @param P the item or mob to check
	 * @return null or the list of bad stats
	 */
	public StringBuffer checkCatalogIntegrity(Physical P);
	
	/**
	 * Confirms this mob or item instance against the catalog prototype.
	 * If it matches, it does nothing.  If it doesn't, the given item
	 * or mob loses its catalog flagging.
	 * @param P the item or mob to confirm against the catalog prototype
	 */
	public void updateCatalogIntegrity(Physical P);
	
	/**
	 * Adds or clears the catalog flag on the item or mob instance given
	 * to this method, and adds (or removes) it as an instance from the
	 * catadata for its prototype.
	 * @param P the item or mob to alter
	 * @param add true to flag it as cataloged, false to clear its flag.
	 */
	public void changeCatalogUsage(Physical P, boolean add);
	
	/**
	 * The catalog has the ability to provide random items from the catalog
	 * as either random equipment on a live mob, or a random drop from a
	 * dead one.  This method is called to generate just such an item 
	 * from the available options.
	 * @param M the mob to equip
	 * @param live true if its for a live mob, false if its for a corpse
	 * @return null or a random item
	 */
	public Item getDropItem(MOB M, boolean live);
	
	/**
	 * Builds catalog metadata, optionally building it from an xml
	 * doc of saved metadata.
	 * @see CataData
	 * @param xml an optional xml doc of metadata data
	 * @return a catadata object
	 */
	public CataData sampleCataData(String xml);

	/**
	 * Returns the coded contents of a room, as it presently exists.
	 * That's all the mobs and items, including all the shopkeeper
	 * shop items.  This is used typically for catalogging.
	 * @see CatalogLibrary#updateRoomContent(String, List)
	 * @see CatalogLibrary.RoomContent
	 * @param R the room to retrieve content from
	 * @return the list of items and mobs in the room
	 */
	public List<RoomContent> roomContent(Room R);
	
	/**
	 * Updates the database entries of the given room id and the
	 * adjusted content objects.  
	 * @see CatalogLibrary#roomContent(Room)
	 * @see CatalogLibrary.RoomContent
	 * @param roomID the roomID of the room being updated
	 * @param content the adjusted content of the room
	 */
	public void updateRoomContent(String roomID, List<RoomContent> content);
	
	/**
	 * Registers the given cataloged item or mob as being an instance,
	 * for metadata collection purposes only.
	 * @param P the item or mob to register as being in the world
	 */
	public void newInstance(Physical P);
	
	/**
	 * When a cataloged mob dies, or a cataloged item is picked up,
	 * this method is called to bump the metadata stats on the object.  
	 * @param P the item or mob to bump the stats on
	 */
	public void bumpDeathPickup(Physical P);
	
	/**
	 * In order to make the catalog appear in the vfs directory paths,
	 * this method is called to get the root directory of the catalog,
	 * which automatically turns all the catalog entries into 
	 * directories and files.
	 * @param resourcesRoot the vfs dir of the resources directory
	 * @return the vfs dir of the catalog directory
	 */
	public CMFile.CMVFSDir getCatalogRoot(CMFile.CMVFSDir resourcesRoot);

	/**
	 * An enumeration for the two general catalog object kinds.
	 * @author Bo Zimmerman
	 */
	public enum CatalogKind
	{
		OBJECT,
		MOB,
		ITEM
	}

	/**
	 * An interface for getting a manipulable list of the content
	 * of a room.  This interface would represent one mob, item,
	 * or shop object in a room.  It can then be altered, and flagged
	 * as dirty for re-saving to the database.
	 * @see CatalogLibrary#roomContent(Room)
	 * @see CatalogLibrary#updateRoomContent(String, List)
	 * @author Bo Zimmerman
	 */
	public static interface RoomContent
	{
		/**
		 * The item or mob in the room
		 * @return item or mob in the room
		 */
		public Physical P();
		
		/**
		 * The shopkeeper, or mob that owns the
		 * item.
		 * @return  mob, or shopkeeper
		 */
		public Environmental holder();
		
		/**
		 * Returns whether the object needs re-saving.
		 * @return whether the object needs re-saving.
		 */
		public boolean isDirty();
		
		/**
		 * Sets the object as needing re-saving.
		 */
		public void flagDirty();
		
		/**
		 * Returns whether the object was deleted.
		 * @return whether the object was deleted.
		 */
		public boolean deleted();
	}

	/**
	 * CataData is the metadata about each entry in the
	 * catalog.  It stores information for features like
	 * the random drop, for instances of the cataloged items
	 * in the world, and how many times the mob has been
	 * killed or item picked up.
	 * @author Bo Zimmerman
	 *
	 */
	public static interface CataData
	{
		/**
		 * A compiled zapper mask that is applied to mobs to
		 * determine if this particular item is potentially
		 * a random drop.  The mask is only applied if it is
		 * non-null, so null means it is NOT a random drop.
		 * @see CataData#getMaskStr()
		 * @see CataData#getWhenLive()
		 * @see CataData#getRate()
		 * @return a compiled zapper mask for dead mobs
		 */
		public MaskingLibrary.CompiledZMask getMaskV();
		
		/**
		 * A zapper mask string that is applied to mobs to
		 * determine if this particular item is potentially
		 * a random drop.  The mask is only applied if it is
		 * non-empty, so empty means it is NOT a random drop.
		 * @see CataData#getMaskV()
		 * @see CataData#getRate()
		 * @see CataData#setMaskStr(String)
		 * @return a  zapper mask string for dead mobs
		 */
		public String getMaskStr();
		
		/**
		 * If this item is a random drop, this flag will 
		 * return true if it is random equipment for a live
		 * mob, and false if it is random drop for a corpse.
		 * @see CataData#getMaskV()
		 * @see CataData#getRate()
		 * @see CataData#setWhenLive(boolean)
		 * @return true for equipment, false for live mob
		 */
		public boolean getWhenLive();
		
		/**
		 * If this item is a random drop, then this is the pct
		 * chance that this item is a potential selection for
		 * a random drop.  It is a number from 0.0 - 1, where 0.0
		 * means it is not a random drop at all.
		 * @see CataData#getMaskV()
		 * @see CataData#setRate(double)
		 * @see CataData#getWhenLive()
		 * @return pct chance that the item is a potential selection
		 */
		public double getRate();
		
		/**
		 * A zapper mask string that is applied to mobs to
		 * determine if this particular item is potentially
		 * a random drop.  The mask is only applied if it is
		 * non-empty, so empty means it is NOT a random drop.
		 * @see CataData#getMaskV()
		 * @see CataData#getRate()
		 * @see CataData#getMaskStr()
		 * @param s a  zapper mask string for dead mobs
		 */
		public void setMaskStr(String s);
		
		/**
		 * If this item is a random drop, this flag will 
		 * be true if it is random equipment for a live
		 * mob, and false if it is random drop for a corpse.
		 * @see CataData#getMaskV()
		 * @see CataData#getRate()
		 * @see CataData#getWhenLive()
		 * @param l true for equipment, false for live mob
		 */
		public void setWhenLive(boolean l);
		
		/**
		 * If this item is a random drop, then this is the pct
		 * chance that this item is a potential selection for
		 * a random drop.  It is a number from 0.0 - 1, where 0.0
		 * means it is not a random drop at all.
		 * @see CataData#getMaskV()
		 * @see CataData#setRate(double)
		 * @see CataData#getWhenLive()
		 * @param r pct chance that the item is a potential selection
		 */
		public void setRate(double r);

		/**
		 * Creates and returns an enumeration of all the instances of 
		 * this cataloged item in the world.
		 * @see CataData#addReference(Physical)
		 * @see CataData#isReference(Physical)
		 * @see CataData#delReference(Physical)
		 * @see CataData#numReferences()
		 * @return an enumeration of all the instances
		 */
		public Enumeration<Physical> enumeration();
		
		/**
		 * Adds the given object as a world instance of this
		 * cataloged object
		 * @see CataData#enumeration()
		 * @see CataData#isReference(Physical)
		 * @see CataData#delReference(Physical)
		 * @see CataData#numReferences()
		 * @param P the mob or item in the world
		 */
		public void addReference(Physical P);
		
		/**
		 * Returns whether the given item is a registered world
		 * instance of this cataloged object.
		 * @see CataData#enumeration()
		 * @see CataData#addReference(Physical)
		 * @see CataData#delReference(Physical)
		 * @see CataData#numReferences()
		 * @param P the item or mob in the world
		 * @return true if its registered, false otherwise
		 */
		public boolean isReference(Physical P);
		
		/**
		 * Removes the given item from the list of registered
		 * world instances of this cataloged object.
		 * @see CataData#enumeration()
		 * @see CataData#addReference(Physical)
		 * @see CataData#isReference(Physical)
		 * @see CataData#numReferences()
		 * @param P the item or mob in the world
		 */
		public void delReference(Physical P);
		
		/**
		 * Returns the number of world items that are instances
		 * of this cataloged object.
		 * @see CataData#enumeration()
		 * @see CataData#addReference(Physical)
		 * @see CataData#isReference(Physical)
		 * @see CataData#delReference(Physical)
		 * @return  the number of world items
		 */
		public int numReferences();
		
		/**
		 * Determines and returns the name of the most
		 * popular area in which instances of this catalog
		 * object can be found.
		 * @return the name of the area
		 */
		public String mostPopularArea();
		
		/**
		 * Returns one of the rooms in which an instance
		 * of this cataloged item was registered.
		 * @return a room with an instance
		 */
		public String randomRoom();
		
		/**
		 * Goes through all the world instances of this
		 * cataloged item and removes any that are destroyed.
		 * Hopefully this method never does anything.
		 */
		public void cleanHouse();
		
		/**
		 * Returns the first world instance of this
		 * cataloged item or mob.
		 * @return the first world instance
		 */
		public Physical getLiveReference();
		
		/**
		 * Returns the number of times this mob has died,
		 * if the cataloged object is a mob, or the number of
		 * times this item has been picked up, if it's an item.
		 * @see CataData#bumpDeathPickup()
		 * @return the number of times
		 */
		public int getDeathsPicksups();
		
		/**
		 * Bumps the number of times this mob has died,
		 * if the cataloged object is a mob, or the number of
		 * times this item has been picked up, if it's an item.
		 * @see CataData#bumpDeathPickup()
		 */
		public void bumpDeathPickup();
		
		/**
		 * Returns the user-defined catagory to which this
		 * cataloged object belongs.  null is uncatagorized  
		 * @see CataData#setCategory(String)
		 * @return the catagory name
		 */
		public String category();
		
		/**
		 * Sets the user-defined catagory to which this
		 * cataloged object belongs.  null is uncatagorized
		 * @see CataData#category()
		 * @param cat the catagory name
		 */
		public void setCategory(String cat);

		/**
		 * Returns this metadata as an xml doc
		 * @param name null, or an optional item/mob name
		 * @see CataData#build(String)
		 * @return this metadata as an xml doc
		 */
		public String data(String name);

		/**
		 * Builds this metadata from an xml doc
		 * @see CataData#data(String)
		 * @param catadata this metadata as an xml doc
		 */
		public void build(String catadata);
	}
}
