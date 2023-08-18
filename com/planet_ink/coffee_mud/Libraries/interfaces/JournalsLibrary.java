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

import java.io.IOException;
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
 * Journals are any set of ordered messages, threaded or unthreaded,
 * which are stored in a database and divided into categories, called
 * Journals or Forums.  Journals are the common name when talking about
 * readable message items and certain system journals (Command Journals),
 * such as BUGS. Forums the term used when a journal is available
 * from a web page.
 *
 * @author Bo Zimmerman
 */
public interface JournalsLibrary extends CMLibrary
{
	/**
	 * Returns the cached set of basic items that implement
	 * the ArchonOnly interface, thus making them ArchonOnly
	 * journals.
	 *
	 * @see JournalsLibrary#isArchonJournalName(String)
	 * @see com.planet_ink.coffee_mud.Items.interfaces.ArchonOnly
	 *
	 * @return the set of journal names
	 */
	public Set<String> getArchonJournalNames();

	/**
	 * Returns whether the given journal appears on the archon
	 * journal names list.
	 *
	 * @see JournalsLibrary#getArchonJournalNames()
	 * @see com.planet_ink.coffee_mud.Items.interfaces.ArchonOnly
	 *
	 * @param journal the journal name
	 * @return true if its an archon only item name, false otherwise
	 */
	public boolean isArchonJournalName(String journal);

	/**
	 * Given an encoded journals list string, this function will
	 * register all the CommandJournals described by the encoded
	 * list string.  See coffeemud.ini file for the format.
	 *
	 * @see JournalsLibrary.CommandJournal
	 * @see JournalsLibrary#commandJournals()
	 * @see JournalsLibrary#getCommandJournal(String)
	 * @see JournalsLibrary#getNumCommandJournals()
	 *
	 * @param list the coded string
	 * @return the number of command journals loaded
	 */
	public int loadCommandJournals(String list);

	/**
	 * Returns an enumeration of all registered command
	 * journals
	 *
	 * @see JournalsLibrary.CommandJournal
	 * @see JournalsLibrary#loadCommandJournals(String)
	 * @see JournalsLibrary#getCommandJournal(String)
	 * @see JournalsLibrary#getNumCommandJournals()
	 *
	 * @return the command journals
	 */
	public Enumeration<CommandJournal> commandJournals();

	/**
	 * Returns the information about the Command Journal
	 * of the given name.
	 *
	 * @see JournalsLibrary.CommandJournal
	 * @see JournalsLibrary#loadCommandJournals(String)
	 * @see JournalsLibrary#commandJournals()
	 * @see JournalsLibrary#getNumCommandJournals()
	 *
	 * @param named the command journal name
	 * @return null, or the journal info object
	 */
	public CommandJournal getCommandJournal(String named);

	/**
	 * Returns the number of command journals, or journals
	 * written to using standard game commands.
	 *
	 * @see JournalsLibrary.CommandJournal
	 * @see JournalsLibrary#loadCommandJournals(String)
	 * @see JournalsLibrary#commandJournals()
	 * @see JournalsLibrary#getCommandJournal(String)
	 *
	 * @return the number of command journals loaded
	 */
	public int getNumCommandJournals();

	/**
	 * If the given journal requires running a MOBPROG script
	 * in order to produce a final message, this function will
	 * run that script.
	 *
	 * @param mob the player trying to post to the journal
	 * @param journal the journal that might be scripted
	 * @param oldValue the value entered before the scripting
	 * @return the final value, typically just the oldValue
	 */
	public String getScriptValue(MOB mob, String journal, String oldValue);

	/**
	 * A security checker for whether the given user can read the
	 * given message.  Applies a myriad of rules that can be controlled
	 * from the arguments.
	 *
	 * @param entry the journal entry
	 * @param srchMatch null, or a string to match almost anything in the entry
	 * @param readerM null, or the reader of the message
	 * @param ignorePrivileges true to NOT require JOURNAL secflag, false to require it
	 * @return true if the message can be read
	 */
	public boolean canReadMessage(JournalEntry entry, String srchMatch, MOB readerM, boolean ignorePrivileges);

	/**
	 * Loads the forumjournals from the given encoded forum
	 * definition string.  See the coffeemud.ini file for the
	 * format of this string.
	 *
	 * @see JournalsLibrary#getNumForumJournals()
	 * @see JournalsLibrary#forumJournals()
	 * @see JournalsLibrary#forumJournalsSorted()
	 * @see JournalsLibrary#getForumJournal(String)
	 * @see JournalsLibrary#getForumJournal(String, Clan)
	 *
	 * @param list the coded string
	 * @return the number of forum journals loaded
	 */
	public int loadForumJournals(String list);

	/**
	 * Returns an enumeration of all normal forumJournals (not
	 * clan ones).  Unsorted for speed.
	 *
	 * @see JournalsLibrary#getNumForumJournals()
	 * @see JournalsLibrary#forumJournalsSorted()
	 * @see JournalsLibrary#getForumJournal(String)
	 * @see JournalsLibrary#getForumJournal(String, Clan)
	 * @see JournalsLibrary#loadForumJournals(String)
	 *
	 * @return the forumjournals
	 */
	public Enumeration<ForumJournal> forumJournals();
	/**
	 * Returns an enumeration of all normal ForumJournals (not
	 * clan ones), sorted by somehow.
	 *
	 * @see JournalsLibrary#getNumForumJournals()
	 * @see JournalsLibrary#forumJournals()
	 * @see JournalsLibrary#getForumJournal(String)
	 * @see JournalsLibrary#getForumJournal(String, Clan)
	 * @see JournalsLibrary#loadForumJournals(String)
	 *
	 * @return the sorted forums
	 */
	public Enumeration<ForumJournal> forumJournalsSorted();

	/**
	 * Returns a ForumJournal of the given name.
	 *
	 * @see JournalsLibrary#getNumForumJournals()
	 * @see JournalsLibrary#forumJournals()
	 * @see JournalsLibrary#forumJournalsSorted()
	 * @see JournalsLibrary#getForumJournal(String, Clan)
	 * @see JournalsLibrary#loadForumJournals(String)
	 *
	 * @param named the name of the forumjournal
	 * @return the ForumJournal object
	 */
	public ForumJournal getForumJournal(String named);

	/**
	 * Retreives a specific forum journal given to a particular clan.
	 *
	 * @see JournalsLibrary#getNumForumJournals()
	 * @see JournalsLibrary#forumJournals()
	 * @see JournalsLibrary#forumJournalsSorted()
	 * @see JournalsLibrary#getForumJournal(String)
	 * @see JournalsLibrary#loadForumJournals(String)
	 *
	 * @param named the name of the journal
	 * @param clan the clan to whom the journal belongs
	 * @return null, or the forumjournal
	 */
	public ForumJournal getForumJournal(String named, Clan clan);

	/**
	 * Returns the number of forum journals.
	 *
	 * @see JournalsLibrary#forumJournals()
	 * @see JournalsLibrary#forumJournalsSorted()
	 * @see JournalsLibrary#getForumJournal(String)
	 * @see JournalsLibrary#getForumJournal(String, Clan)
	 * @see JournalsLibrary#loadForumJournals(String)
	 *
	 * @return the number of forum journals.
	 */
	public int getNumForumJournals();

	/**
	 * Registers a set of ForumJournals for the given clan from the
	 * coded string.  See the coffeemud.ini file for the format
	 * of this string.
	 *
	 * @see JournalsLibrary#getClanForums(Clan)
	 *
	 * @param clan the clan
	 * @param allClanForumMappings the coded string
	 */
	public void registerClanForum(Clan clan, String allClanForumMappings);

	/**
	 * Returns the list of ForumJournals that exist for the given clan.
	 *
	 * @see JournalsLibrary#registerClanForum(Clan, String)
	 *
	 * @param clan the clan
	 * @return the list of ForumJournals for this clan
	 */
	public List<ForumJournal> getClanForums(Clan clan);

	/**
	 * Called to add a user to the subscription list for a journal.
	 *
	 * @param journalName the name of the journal
	 * @param userName the user who subscribed
	 * @param saveMailingList true to save afterwards, false if you'll do it later
	 * @return true if someone was added, false otherwise
	 */
	public boolean subscribeToJournal(String journalName, String userName, boolean saveMailingList);

	/**
	 * Unsubscribes the given user/account from every mailing list.
	 *
	 * @param username the user/account
	 * @return true if an unsubscribe happened
	 */
	public boolean unsubscribeFromAll(final String username);

	/**
	 * Called to remove a user from the subscription list for a journal.
	 *
	 * @param journalName the name of the journal
	 * @param userName the user who subscribed
	 * @param saveMailingList true to save afterwards, false if you'll do it later
	 * @return true if someone was removed, false otherwise
	 */
	public boolean unsubscribeFromJournal(String journalName, String userName, boolean saveMailingList);

	/**
	 * Get the forum journal summary stats, caching them after
	 * calculation from the database
	 *
	 * @see JournalsLibrary.JournalMetaData
	 *
	 * @param journal the journal
	 * @return the stats
	 */
	public JournalMetaData getJournalStats(ForumJournal journal);

	/**
	 * Clears the forum journal summary stats so that they can
	 * be rebuilt from the database.
	 *
	 * @see JournalsLibrary.JournalMetaData
	 *
	 * @param journal the journal that needs updating
	 */
	public void clearJournalSummaryStats(ForumJournal journal);

	/**
	 * Whenever a post to a journal is made, this function is called to send a notification to online
	 * users who have subscribed to such things.
	 *
	 * @param journal the journal posted at
	 * @param from the author of the message posted
	 * @param to who the post is too
	 * @param subject the subject of the top message
	 */
	public void notifyPosting(final String journal, final String from, final String to, final String subject);

	/**
	 * Whenever a reply to a journal is made, this function is called to send a notification to online
	 * users who have subscribed to such things.
	 *
	 * @param journal the journal replied at
	 * @param tpAuthor the author of the message replied to
	 * @param reAuthor the author of the reply
	 * @param subject the subject of the top message
	 */
	public void notifyReplying(final String journal, final String tpAuthor, final String reAuthor, final String subject);

	/**
	 * An asynchronous system for allowing a user to edit a text document with options for search and replace, line
	 * inserting and deleting, and line replacement.  A save option is also provided.
	 *
	 * @see JournalsLibrary.MsgMkrCallback
	 * @see JournalsLibrary.MsgMkrResolution
	 *
	 * @param mob the player doing the editing
	 * @param messageTitle the title of the message
	 * @param vbuf the text of the message
	 * @param autoAdd true to go directly into line adding mode, false to start in menu
	 * @param back the callBack when done
	 */
	public void makeMessageASync(final MOB mob, final String messageTitle, final List<String> vbuf, final boolean autoAdd, final MsgMkrCallback back);

	/**
	 * Since Calendar events are also journal entries, whenever the calendar is changed,
	 * this method must be called to make sure notifications for new events occurs.
	 */
	public void resetCalendarEvents();

	/**
	 * Non-Forum journal messages handle "replies" by tacking them to the end of the
	 * main message.  This is the official boundary.
	 */
	public static final String JOURNAL_BOUNDARY="%0D^w---------------------------------------------^N%0D";

	/**
	 * The return value of the resolution of the message maker closing.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum MsgMkrResolution
	{
		SAVEFILE,
		CANCELFILE
	}

	/**
	 * Since The Message Maker advanced cl editor is async, this interface exists to provide
	 * a callBack to the message maker.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public interface MsgMkrCallback
	{
		/**
		 * The callBack once Message Maker ends.
		 *
		 * @see JournalsLibrary.MsgMkrResolution
		 *
		 * @param mob the player who used the message maker
		 * @param sess the players session
		 * @param res how the message maker was resolved
		 */
		public void callBack(final MOB mob, final Session sess, final MsgMkrResolution res);
	}

	/**
	 * ForumJournals are journals accessed via the web, and are typically
	 * threaded in their messages.  They have a special journal entry
	 * containing metadata and stats about the journal that changes
	 * periodically. This structure manages that metadata.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public interface JournalMetaData
	{
		/**
		 * Return the journal name
		 *
		 * @return the journal name
		 */
		public String name();
		/**
		 * Builder method for setting name
		 *
		 * @param intro the name
		 * @return this same object
		 */
		public JournalMetaData name(String intro);
		/**
		 * Returns the number of threads
		 *
		 * @return the number of threads
		 */
		public int threads();
		/**
		 * Builder method for setting number of thread
		 *
		 * @param num number of thread
		 * @return this same object
		 */
		public JournalMetaData threads(int num);
		/**
		 * Returns the number of messages
		 *
		 * @return the number of messages
		 */
		public int posts();
		/**
		 * Builder method for setting number of messages
		 *
		 * @param num number of messages
		 * @return this same object
		 */
		public JournalMetaData posts(int num);
		/**
		 * Returns path to the journal images
		 *
		 * @return path to the journal images
		 */
		public String imagePath();
		/**
		 * Builder method for setting images path
		 *
		 * @param intro images path
		 * @return this same object
		 */
		public JournalMetaData imagePath(String intro);
		/**
		 * Returns short description of the journal
		 *
		 * @return short description of the journal
		 */
		public String shortIntro();
		/**
		 * Builder method for setting  short desc
		 *
		 * @param intro short desc
		 * @return this same object
		 */
		public JournalMetaData shortIntro(String intro);
		/**
		 * Returns long description of the journal
		 *
		 * @return long description of the journal
		 */
		public String longIntro();
		/**
		 * Builder method for setting description
		 * @param intro description
		 * @return this same object
		 */
		public JournalMetaData longIntro(String intro);

		/**
		 * Returns message key to an intro or something?
		 *
		 * @return message key to an intro or something?
		 */
		public String introKey();
		/**
		 * Builder method for setting intro
		 *
		 * @param key intro
		 * @return this same object
		 */
		public JournalMetaData introKey(String key);
		/**
		 * Returns message key to latest message
		 *
		 * @return message key to latest message
		 */
		public String latestKey();
		/**
		 * Builder method for setting latest message
		 *
		 * @param key latest message
		 * @return this same object
		 */
		public JournalMetaData latestKey(String key);
		/**
		 * Returns message keys to stucky messages
		 *
		 * @return message keys to stucky messages
		 */
		public List<String> stuckyKeys();
		/**
		 * Builder method for setting stucky messages
		 *
		 * @param keys stucky messages
		 * @return this same object
		 */
		public JournalMetaData stuckyKeys(List<String> keys);
	}

	/**
	 * Command Journals are another category of Journal that is
	 * accessed from the command line (as oppposed to SMTP Journals
	 * accessed via email, Forums accessed via web page, and standard
	 * Journal accessed via item).  Examples of a command journal are
	 * things like the BUG or TYPO command.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface CommandJournal
	{
		/**
		 * Returns the name of the journal, as well as the command word
		 *
		 * @return the name of the journal, as well as the command word
		 */
		public String NAME();

		/**
		 * Returns the ZapperMask for posting to the journal
		 *
		 * @return the ZapperMask for posting to the journal
		 */
		public String mask();
		/**
		 * Returns the name of the journal in the database.  SYSTEM_*
		 *
		 * @return the name of the journal in the database.  SYSTEM_*
		 */
		public String JOURNAL_NAME();
		/**
		 * Returns the value of a give journal flag
		 *
		 * @see JournalsLibrary.CommandJournalFlags
		 *
		 * @param flag the flag
		 * @return the value of a give journal flag, or null
		 */
		public String getFlag(CommandJournalFlags flag);

		/**
		 * Returns the same as getFlag(SCRIPT);
		 *
		 * @see JournalsLibrary.CommandJournal#getFlag(CommandJournalFlags)
		 *
		 * @return the same as getFlag(SCRIPT);
		 */
		public String getScriptFilename();
	}

	/**
	 * This enum defines flags that apply to command journals.
	 *
	 * @see JournalsLibrary.CommandJournal
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static enum CommandJournalFlags
	{
		/**
		 * Echo messages to this journal to the given Channel
		 */
		CHANNEL,
		/**
		 * Include the journalers current room in the message
		 */
		ADDROOM,
		/**
		 * How old before a message is auto-purged
		 */
		EXPIRE,
		/**
		 * Echo messages to this journal to the privileged folk.
		 */
		ADMINECHO,
		/**
		 * Require user confirmation before completing a post.
		 */
		CONFIRM,
		/**
		 * Path to mobprog script to run when trying to post to this
		 */
		SCRIPT,
		/**
		 * Journal, ALL, or user to send message to when replying
		 * to message to/from the user.
		 */
		REPLYSELF,
		/**
		 * Journal, ALL, or user to send message to when replying
		 * to messages sent to ALL
		 */
		REPLYALL,
		/**
		 * Read the journal at logon
		 */
		MOTD,
		/**
		 * Arbitrary transfer targets for admins
		 */
		ASSIGN;
	}

	/**
	 * An accessor interface for a view of a journal that is
	 * exposed to an email system.  The built-in SMTP server
	 * in CoffeeMud is such a system.
	 *
	 * @see SMTPLibrary
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface SMTPJournal
	{
		/**
		 * Returns the name of the journal
		 *
		 * @return the name of the journal
		 */
		public String  name();

		/**
		 * Returns whether the journal allows forwarding to email addresses?
		 *
		 * @return whether the journal allows forwarding to email addresses?
		 */
		public boolean forward();

		/**
		 * Returns whether the journal allows non-subscribe posts via email
		 *
		 * @return whether the journal allows non-subscribe posts via email
		 */
		public boolean subscribeOnly();

		/**
		 * Returns whether the journal supports message expiration deletions
		 * @return whether the journal supports message expiration deletions
		 */
		public boolean keepAll();

		/**
		 * Returns the email posting zapper mask
		 *
		 * @return the email posting zapper mask
		 */
		public String  criteriaStr();

		/**
		 * Returns the email posting zapper mask
		 *
		 * @return the email posting zapper mask
		 */
		public MaskingLibrary.CompiledZMask criteria();
	}

	/**
	 * Journals with a meta-flags entry, typically Forums,
	 * are described by the accessors of this interface.
	 * ForumJournals are a type of Journal that is accessed
	 * via the web.
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public static interface ForumJournal
	{
		/**
		 * Returns the journal name
		 *
		 * @return the journal name
		 */
		public String NAME();

		/**
		 * Returns the category this journal is in
		 *
		 * @return the category this journal is in
		 */
		public String category();

		/**
		 * Returns the raw zapper mask for reading
		 *
		 * @return the raw zapper mask for reading
		 */
		public String readMask();

		/**
		 * Returns the raw zapper mask for posting
		 *
		 * @return the raw zapper mask for posting
		 */
		public String postMask();

		/**
		 * Returns the raw zapper mask for replying
		 *
		 * @return the raw zapper mask for replying
		 */
		public String replyMask();

		/**
		 * Returns the raw zapper mask for attaching
		 *
		 * @return the raw zapper mask for attaching
		 */
		public String attachMask();

		/**
		 * Returns the maximum attachments
		 *
		 * @return the maximum attachments
		 */
		public int maxAttach();

		/**
		 * Returns the raw zapper mask for administering
		 *
		 * @return the raw zapper mask for administering
		 */
		public String adminMask();

		/**
		 * Returns the value of a forum journal flag
		 *
		 * @see JournalsLibrary.ForumJournalFlags
		 *
		 * @param flag the flag to return
		 * @return the value of the flag or null
		 */
		public String getFlag(ForumJournalFlags flag);

		/**
		 * Returns whether the given mob passes the
		 * given zapper mask, presumably one of the above.
		 * It might seem wasteful, but remember that
		 * the masking library caches stuff like this.
		 *
		 * @param M the mob to check
		 * @param mask the mask to check them against
		 * @return true if the mob passes, false otherwise
		 */
		public boolean maskCheck(MOB M, String mask);

		/**
		 * Returns whether the given mob is authorized to
		 * do the task designated by the given flag,
		 * typically by calling maskCheck above.
		 *
		 * @see JournalsLibrary.ForumJournalFlags
		 *
		 * @param M the mob to check
		 * @param fl an appropriate flag
		 * @return true if the mob is authorized, false otherwise
		 */
		public boolean authorizationCheck(MOB M, ForumJournalFlags fl);
	}

	/**
	 * Journals with a meta-flags entry, typically Forums,
	 * have numerous flags that define them.  These are
	 * those flags
	 *
	 * @author Bo Zimmerman
	 */
	public static enum ForumJournalFlags
	{
		/**
		 * The expiration time of messages
		 */
		EXPIRE,
		/**
		 * The ZapperMask for readers
		 */
		READ,
		/**
		 * The ZapperMask for posters
		 */
		POST,
		/**
		 * The ZapperMask for repliers
		 */
		REPLY,
		/**
		 * The ZapperMask for administrators
		 */
		ADMIN,
		/**
		 * The field to sort messages by
		 */
		SORTBY,
		/**
		 * The category that the journal belongs to
		 */
		CATEGORY,
		/**
		 * The ZapperMask for those who can attach
		 */
		ATTACH,
		/**
		 * The maximum attachments per message
		 */
		MAXATTACH
		;
	}
}
