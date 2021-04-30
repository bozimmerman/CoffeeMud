package com.planet_ink.coffee_mud.Common.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultFaction;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/*
Copyright 2015-2021 Bo Zimmerman

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
 * An entry in a journal or book
 *
 * @author Bo Zimmerman
 *
 */
public interface JournalEntry extends CMCommon, Cloneable
{
	/**
	 * The fully unique key for this entry, unique across all entries in all books and journals
	 * @return the key
	 */
	public String key();

	/**
	 * Sets the fully unique key for this entry, unique across all entries in all books and journals
	 * @param key the key
	 */
	public JournalEntry key(String key);

	/**
	 * The author of the entry
	 * @return author of the entry
	 */
	public String from();

	/**
	 * Sets the author of the entry
	 * @param from author of the entry
	 * @return the journal
	 */
	public JournalEntry from(String from);

	/**
	 * Who the entry is for, usually ALL
	 * @return who the entry is for, usually ALL
	 */
	public String to();

	/**
	 * Sets who the entry is for, usually ALL
	 * @param to who the entry is for, usually ALL
	 * @return the journal
	 */
	public JournalEntry to(String to);

	/**
	 * The subject of the entry
	 * @return the subject of the entry
	 */
	public String subj();

	/**
	 * Sets the subject of the entry
	 * @param subj the subject of the entry
	 * @return the journal
	 */
	public JournalEntry subj(String subj);

	/**
	 * The content of the entry.
	 * @return the content of the entry.
	 */
	public String msg();

	/**
	 * Sets the content of the entry.
	 * @param msg the content of the entry.
	 * @return the journal
	 */
	public JournalEntry msg(String msg);

	/**
	 * The date/time, in ms, that the message was posted
	 * @return the date/time, in ms, that the message was posted
	 */
	public long date();

	/**
	 * Sets the date/time, in ms, that the message was posted
	 * @param date the date/time, in ms, that the message was posted
	 * @return the journal
	 */
	public JournalEntry date(long date);

	/**
	 * The date/time, in ms, that the message was updated
	 * @return the date/time, in ms, that the message was updated
	 */
	public long update();

	/**
	 * Sets the date/time, in ms, that the message was updated
	 * @param update the date/time, in ms, that the message was updated
	 * @return the journal
	 */
	public JournalEntry update(long update);

	/**
	 * The entry key of the entry that this entry is a reply to
	 * @return the entry key of the entry that this entry is a reply to
	 */
	public String parent();

	/**
	 * Sets the entry key of the entry that this entry is a reply to
	 * @param parent the entry key of the entry that this entry is a reply to
	 * @return the journal
	 */
	public JournalEntry parent(String parent);

	/**
	 * The attribute flags for this entry
	 * @return the attribute flags for this entry
	 */
	public long attributes();

	/**
	 * Sets the attribute flags for this entry
	 * @param attributes the attribute flags for this entry
	 * @return the journal
	 */
	public JournalEntry attributes(long attributes);

	/**
	 * The "image path" or, for mailboxes, the Journal Name this mail served as a source for.
	 * @return the "image path" or, for mailboxes, the Journal Name this mail served as a source for.
	 */
	public String data();

	/**
	 * Sets the "image path" or, for mailboxes, the Journal Name this mail served as a source for.
	 * @param data the "image path" or, for mailboxes, the Journal Name this mail served as a source for.
	 * @return the journal
	 */
	public JournalEntry data(String data);

	/**
	 * The cardinal entry number for the journal entry.
	 * @return the cardinal entry number for the journal entry.
	 */
	public int cardinal();

	/**
	 * Sets the cardinal entry number for the journal entry.
	 * @param cardinal the cardinal entry number for the journal entry.
	 * @return the journal
	 */
	public JournalEntry cardinal(int cardinal);

	/**
	 * The image icon for forum entries
	 * @return the image icon for forum entries
	 */
	public String msgIcon();

	/**
	 * Sets the image icon for forum entries
	 * @param msgIcon the image icon for forum entries
	 * @return the journal
	 */
	public JournalEntry msgIcon(String msgIcon);

	/**
	 * The number of replies to this entry.
	 * @return the number of replies to this entry.
	 */
	public int replies();

	/**
	 * Sets he number of replies to this entry.
	 * @param replies the number of replies to this entry.
	 * @return the journal
	 */
	public JournalEntry replies(int replies);

	/***
	 * The number of views of this entry
	 * @return the number of views of this entry
	 */
	public int views();

	/**
	 * Sets the number of views of this entry
	 * @param views the number of views of this entry
	 * @return the journal
	 */
	public JournalEntry views(int views);

	/**
	 * Returns whether this is the last entry in the db.  This
	 * is not actually stored in the DB, but is derived
	 * from the reading process.
	 * @return whether this is the last entry in the db
	 */
	public boolean isLastEntry();

	/**
	 * Sets whether this is the last entry in the db
	 * @param lastEntry whether this is the last entry in the db
	 * @return the journal
	 */
	public JournalEntry lastEntry(boolean lastEntry);

	/**
	 * The friendly viewable form of this message in the command line.
	 * Is not included in any xml builds or in the DB.
	 * @return the friendly message
	 */
	public StringBuffer derivedBuildMessage();

	/**
	 * The friendly viewable form of this message in the command line.
	 * Is not included in any xml builds or in the DB.
	 * @param msg the friendly message
	 * @return the journal
	 */
	public JournalEntry derivedBuildMessage(StringBuffer msg);

	/**
	 * Compares two journal entry objects to each other
	 * @param o another journal entry obj
	 * @return true or false
	 */
	public int compareTo(JournalEntry o);

	/**
	 * Returns an xml representation of this entry.
	 * @return an xml representation of this entry
	 */
	public String getXML();

	/**
	 * Sets an entry based on the given xml.
	 * @param xml the xml to use.
	 */
	public void setXML(final String xml);

	@Override
	/**
	 * Returns a copy of the journal entry
	 * @return the new entry
	 */
	public JournalEntry copyOf();

	/**
	 * Flagging this entry as stuck to the top
	 */
	public final static long ATTRIBUTE_STUCKY=2;

	/**
	 * Flatting this entry as protected from auto-purging
	 */
	public final static long ATTRIBUTE_PROTECTED=1;
}
