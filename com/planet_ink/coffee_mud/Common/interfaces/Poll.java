package com.planet_ink.coffee_mud.Common.interfaces;
import java.util.List;

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
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
 * A Poll object manages the data and processes for all of the public
 * and private voting polls available through the CoffeeMud POLL
 * CREATE POLL, LIST POLLS, etc.. commands.
 *
 * These objects are managed by the PollManager Library
 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager
 * @see com.planet_ink.coffee_mud.core.CMLib#polls()
 */
public interface Poll extends CMCommon
{
	/**
	 * A name for this poll, used for internal purposes only.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setName(String)
	 * @return a name for this poll
	 */
	public String getName();

	/**
	 * Sets a name for this poll, used for internal purposes only.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getName()
	 * @param newname a name for this poll
	 */
	public void setName(String newname);

	/**
	 * Returns the title used when showing poll results.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setSubject(String)
	 * @return the title used when showing poll results.
	 */
	public String getSubject();

	/**
	 * Sets the title used when showing poll results.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getSubject()
	 * @param newsubject the title used when showing poll results.
	 */
	public void setSubject(String newsubject);

	/**
	 * Returns the description shown when participating in the poll.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setDescription(String)
	 * @return the description shown when participating in the poll.
	 */
	public String getDescription();

	/**
	 * Sets the description shown when participating in the poll.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getDescription()
	 * @param newdescription the description shown when participating in the poll.
	 */
	public void setDescription(String newdescription);

	/**
	 * Returns the Name of the creator of this poll.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setAuthor(String)
	 * @return the Name of the creator of this poll.
	 */
	public String getAuthor();

	/**
	 * Sets the Name of the creator of this poll.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getAuthor()
	 * @param newname the Name of the creator of this poll.
	 */
	public void setAuthor(String newname);

	/**
	 * Returns a bitmap of flags related to this poll.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setFlags(long)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ACTIVE
	 * @return a bitmap of flags related to this poll.
	 */
	public long getFlags();

	/**
	 * Sets a bitmap of flags related to this poll.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getFlags()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ABSTAIN
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ACTIVE
	 * @param flag a bitmap of flags related to this poll.
	 */
	public void setFlags(long flag);

	/**
	 * Returns a zapper mask string to mask out valid participants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setQualZapper(String)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary#maskCheck(String, Environmental, boolean)
	 * @return a zapper mask string to mask out valid participants.
	 */
	public String getQualZapper();

	/**
	 * Sets a zapper mask string to mask out valid participants.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getQualZapper()
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary#maskCheck(String, Environmental, boolean)
	 * @param newZap a zapper mask string to mask out valid participants.
	 */
	public void setQualZapper(String newZap);

	/**
	 * Returns the time, in millis since 1970, that this poll expires.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setExpiration(long)
	 * @return the time, in millis since 1970, that this poll expires.
	 */
	public long getExpiration();

	/**
	 * Sets the time, in millis since 1970, that this poll expires.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getExpiration()
	 * @param time the time, in millis since 1970, that this poll expires.
	 */
	public void setExpiration(long time);

	/**
	 * Returns a list of PollOption objects denoting the poll choices.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setOptions(List)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollOption
	 * @return a list of PollOption objects denoting the poll choices.
	 */
	public List<PollOption> getOptions();

	/**
	 * Sets a list of PollOption objects denoting the poll choices.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getOptions()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollOption
	 * @param V a list of PollOption objects denoting the poll choices.
	 */
	public void setOptions(List<PollOption> V);

	/**
	 * Returns a list of PollResult objects renoting all poll votes.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getResults()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult
	 * @return a list of PollResult objects renoting all poll votes.
	 */
	public List<PollResult> getResults();

	/**
	 * Sets a list of PollResult objects renoting all poll votes.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getResults()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult
	 * @param V a list of PollResult objects renoting all poll votes.
	 */
	public void setResults(List<PollResult> V);

	/**
	 * Returns a String of PollOption objects rendered as an XML document.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getOptions()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollOption
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager#loadPollIfNecessary(Poll)
	 * @return a String of PollOption objects rendered as an XML document.
	 */
	public String getOptionsXML();

	/**
	 * Returns a String of PollResult objects rendered as an XML document.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getResults()
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager#loadPollIfNecessary(Poll)
	 * @return a String of PollResult objects rendered as an XML document.
	 */
	public String getResultsXML();

	/**
	 * Returns whether the PollOption and PollResult objects have already been
	 * loaded and populated into this object from the database.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#setLoaded(boolean)
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager#loadPollIfNecessary(Poll)
	 * @return true returns true if this object is fully populated from the database
	 */
	public boolean loaded();

	/**
	 * Sets whether the PollOption and PollResult objects have already been
	 * loaded and populated into this object from the database.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#loaded()
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager#loadPollIfNecessary(Poll)
	 * @param truefalse set to true if this object is fully populated from the database
	 */
	public void setLoaded(boolean truefalse);

	/**
	 * Populates this Poll object if necessary, and returns the PollResult
	 * object that represents the given mobs vote in this Poll, or NULL otherwise.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager#loadPollIfNecessary(Poll)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#addVoteResult(com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult
	 * @param mob the mob to find a result object for
	 * @return the PollResult object denoting this mobs vote
	 */
	public PollResult getMyVote(MOB mob);

	/**
	 * Populates this Poll object if necessary, and adds the given PollResult
	 * object that represents a mobs vote in this Poll to the list of results.
	 * @see com.planet_ink.coffee_mud.Libraries.interfaces.PollManager#loadPollIfNecessary(Poll)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getMyVote(MOB)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll.PollResult
	 * @param R the particular vote by a particular mob to add
	 */
	public void addVoteResult(PollResult R);

	/**
	 * Returns true if the given mob is allowed to vote in this poll, false otherwise.
	 * Always returns false if getMyVote(mob)!=null.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#getMyVote(MOB)
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_VOTEBYIP
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_ACTIVE
	 * @param mob the mob to check for elligibility
	 * @return true if the given mob is allowed to vote in this poll, false otherwise.
	 */
	public boolean mayIVote(MOB mob);

	/**
	 * Returns true if the given mob may not see the poll results, false otherwise.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_HIDERESULTS
	 * @see com.planet_ink.coffee_mud.Common.interfaces.Poll#FLAG_PREVIEWRESULTS
	 * @param mob the mob to check for elligibility
	 * @return true if the given mob can not see the poll results, false otherwise
	 */
	public boolean mayISeeResults(MOB mob);

	/**
	 * A class to represent a single choosable option for this poll.
	 *
	 * @author Bo Zimmermanimmerman
	 *
	 */
	public static class PollOption
	{
		/** the option shown to the user for choosing */
		public String text="";
		/**
		 * Creates a single choosable option for this poll
		 * @param opt the text of the option, as shown to the user
		 */
		public PollOption(String opt) { text=opt;}
	}

	/**
	 * A class to represent a single vote by a single user in this poll.
	 *
	 * @author Bo Zimmermanimmerman
	 *
	 */
	public static class PollResult
	{
		/** The user name of the user who has voted */
		public String user="";
		/** the user name of the user who has voted */
		public String ip="";
		/** cardinal order number of the option chosen by this user in the poll */
		public String answer="";

		/**
		 * Create a poll result object denoting the vote of
		 * a particular user for a particular poll option.
		 * @param usr user name of the user who has voted
		 * @param ipaddr user name of the user who has voted
		 * @param ans cardinal order number of the option chosen by this user in the poll
		 */
		public PollResult(String usr, String ipaddr, String ans)
		{
			user=usr;
			ip=ipaddr;
			answer=ans;
		}
	}

	/** bitmask poll flag denoting that the poll is ready to receive votes */
	public static final int FLAG_ACTIVE=1;
	/** bitmask poll flag denoting that players may preview results before voting */
	public static final int FLAG_PREVIEWRESULTS=2;
	/** bitmask poll flag denoting that players may select to abstain from a vote */
	public static final int FLAG_ABSTAIN=4;
	/** bitmask poll flag denoting that a player is considered to have voted if ips match */
	public static final int FLAG_VOTEBYIP=8;
	/** bitmask poll flag denoting that players may never seen the results of the poll */
	public static final int FLAG_HIDERESULTS=16;
	/** bitmask poll flag denoting that players will not be given a chance to vote at logon */
	public static final int FLAG_NOTATLOGIN=32;
}
