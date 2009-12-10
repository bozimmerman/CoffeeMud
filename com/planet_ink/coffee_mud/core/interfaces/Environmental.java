package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * something that is affected by, or affects
 * the environment around them.
 */
/**
 * The core of most object types in CoffeeMud. Much of the most common fields and
 * data are derived from this.
 * @author Bo Zimmerman
 *
 */
public interface Environmental extends Tickable, Affectable, StatsAffecting, MsgListener, CMObject, CMModifiable, Behavable
{
    /**
     * The displayable name of this object.  May be modified by envStats() object. Is
     * derived from the Name().
     * @see  Environmental#Name()
     * @return the modified final name of this object on the map.
     */
	public String name();
    /**
     * The raw unmodified name of this object as stored in the database.
     * This is the name set by builders and used as a basis for the name() method.
     * @see  Environmental#name()
     * @return the raw base name of this object on the map.
     */
	public String Name();
    /**
     * Sets the raw unmodified name of this object as stored in the database.
     * This is the name set by builders and used as a basis for the name() method.
     * @see  Environmental#Name()
     * @param newName the raw base name of this object on the map.
     */
	public void setName(String newName);

    /**
     * Gets the raw string used to show what this object looks like in the room.
     * May be used as a basis for other methods, such as the title of rooms, and
     * what an exit looks like when open.  The value for Items may be null if the item
     * is not displayed when the room is seen.
     * @return the string describing how this object looks in the room
     */
    public String displayText();
    /**
     * Sets the raw string used to show what this object looks like in the room.
     * May be used as a basis for other methods, such as the title of rooms, and
     * what an exit looks like when open.  The value for Items may be null if the item
     * is not displayed when the room is seen.
     * @param newDisplayText the string describing how this object looks in the room
     */
	public void setDisplayText(String newDisplayText);

	/**
     * The basic description of this object, as shown when the item is directly LOOKed at.
     * @return the basic detail description of this object
	 */
	public String description();
    /**
     * Sets the basic description of this object, as shown when the item is directly LOOKed at.
     * @param newDescription the basic detail description of this object
     */
	public void setDescription(String newDescription);

    /**
     * Utterly and permanently destroy this object, not only removing it from the map, but
     * causing this object to be collected as garbage by Java.  Containers, rooms. and mobs who have
     * their destroy() method called will also call the destroy() methods on all items and other
     * objects listed as content, recursively.
     */
    public void destroy();
    /**
     * Whether, if this object is in a room, whether it is appropriate to save this object to
     * the database as a permanent feature of its container.  It always returns true except
     * under unique circumstances.
     * @return true, usually.
     */
    public boolean savable();
    /**
     * Whether the destroy() method has been previousy called on this object.
     * @return whether the object is destroy()ed.
     */
    public boolean amDestroyed();

    /**
     * Returns the fully qualified and determined name of the image file displayed for this
     * object when MXP is used.  If rawImage() is non-empty, it will return rawImage, and
     * otherwise use the mxp default data file.
     * @return the name of the mxp image to display for this object.
     */
	public String image();
    /**
     * Returns the raw name of the image file to display for this object when MXP is used.  This
     * is the value set by the builder, and may be returned by image() if it is non-empty.
     * @return the raw name of the mxp image file
     */
    public String rawImage();
    /**
     * Sets the raw name of the image file to display for this object when MXP is used.  This
     * is the value set by the builder, and may be returned by image() if it is non-empty.
     * @param newImage the raw name of the mxp image file
     */
	public void setImage(String newImage);

    /**
     * Whether the fields of this item are set in code, or set by builders.  Generic means that
     * they are set by builders, in which case XML is returned by the text() method containing
     * all of the values for all the fields.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#text()
     * @return whether this item is modifiable by builders
     */
	public boolean isGeneric();

	/**
     * For objects which have false for their isGeneric method, this is used to set any internally
     * coded strings to change the nature or behavior of the object.  For objects which have true
     * for their isGeneric method, this is used to set and parse the XML which will be used to
     * fill out the fields in this object.  Since Ability objects are never Generic, this will always
     * be where parameters are read from an Ability instance.
     * @param newMiscText either an open internal text string, or XML
	 */
	public void setMiscText(String newMiscText);
    /**
     * For objects which have false for their isGeneric method, this is used to set any internally
     * coded strings to change the nature or behavior of the object.  For objects which have true
     * for their isGeneric method, this is used to set and parse the XML which will be used to
     * fill out the fields in this object. Since Ability objects are never Generic, this will always
     * be where parameters are read from an Ability instance.
     * @return either an open internal text string, or XML
     */
	public String text();

	/**
	 * Unimplemented as of yet, but will hold a string telling the system what
	 * the proper format of any miscText data.  Will use the CMParms.MTFORMAT_*
	 * constants for definition.
	 * @see com.planet_ink.coffee_mud.core.CMParms
	 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#text()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#setMiscText(String)
	 * @return the format expected for the miscText field
	 */
	public String miscTextFormat();
	
    /**
     * Whether this object instance is functionally identical to the object passed in.  Works by repeatedly
     * calling getStat on both objects and comparing the values.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#getStatCodes()
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#getStat(String)
     * @param E the object to compare this one to
     * @return whether this object is the same as the one passed in
     */
	public boolean sameAs(Environmental E);
	
    /**
     * If this object expires, it should have a timestamp saying when it expires, in real time.
     * When it expires, a MSG_EXPIRE message will be sent to it.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#setExpirationDate(long)
     * @return the time stamp when this thing expires
     */
	public long expirationDate();
    /**
     * If this object expires, it should have a timestamp saying when it expires, in real time.
     * When it expires, a MSG_EXPIRE message will be sent to it.
     * @see com.planet_ink.coffee_mud.core.interfaces.Environmental#expirationDate()
     * @param dateTime the time stamp when this thing expires
     */
	public void setExpirationDate(long dateTime);
	/**
	 * the maximum range of this object, if applicable.  Can refer to the size of a room,
     * the range of a weapon, or the calculated range of a mob in combat.
     * @return the maximum range
	 */
	public int maxRange();
    /**
     * the minimum range of this object, if applicable.  Can refer to the size of a room,
     * the range of a weapon, or the calculated range of a mob in combat.  Usually 0.
     * @return the minimum range
     */
	public int minRange();
}
