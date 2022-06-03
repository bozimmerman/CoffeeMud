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
import com.planet_ink.coffee_mud.Libraries.Sense;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/*
   Copyright 2005-2022 Bo Zimmerman

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
public interface PollManager extends CMLibrary
{
	public Poll getPoll(String named);
	public Poll getPoll(int x);
	public List<Poll>[] getMyPollTypes(MOB mob, boolean login);
	public Iterator<Poll> getPollList();

	/**
	 * Given a Poll object with as little as a name, and a
	 * player mob, this will allow the mob to vote in the poll,
	 * possibly loading the poll fully in the process.
	 *
	 * @see PollManager#modifyVote(Poll, MOB)
	 * @see PollManager#processResults(Poll, MOB)
	 *
	 * @param P the Poll object
	 * @param mob the player who can vote on it
	 */
	public void processVote(Poll P, MOB mob);

	/**
	 * Given a Poll object fully loaded, and a
	 * archon-ish mob, this will allow the settings of the
	 * poll to be edited.
	 *
	 * @see PollManager#processVote(Poll, MOB)
	 * @see PollManager#processResults(Poll, MOB)
	 *
	 * @param P the Poll object
	 * @param mob the player who can edit it
	 */
	public void modifyVote(Poll P, MOB mob) throws java.io.IOException;

	/**
	 * Given a Poll object with as little as a name, and a
	 * player mob, this will show the results of the poll
	 * to the given user, possibly loading the Poll in the
	 * process.
	 *
	 * @see PollManager#processVote(Poll, MOB)
	 * @see PollManager#modifyVote(Poll, MOB)
	 *
	 * @param P the Poll object, with maybe just a name
	 * @param mob the player to show results to
	 */
	public void processResults(Poll P, MOB mob);

	/**
	 * Given an existing Poll in the database that has
	 * new Poll Results added to it, this will update
	 * those results in the database.
	 *
	 * @see PollManager#updatePoll(String, Poll)
	 *
	 * @param P the poll with results
	 */
	public void updatePollResults(Poll P);

	/**
	 * Given the name of an existing Poll in the database,
	 * and a Poll object, this will update the database entry
	 * accordingly
	 *
	 * @see PollManager#updatePollResults(Poll)
	 *
	 * @param oldName the name of an existing poll
	 * @param P the poll to update in the database
	 */
	public void updatePoll(String oldName, Poll P);

	/**
	 * Given a Poll object, this will add it to the memory
	 * cache -- that is all.
	 *
	 * @see PollManager#createPoll(Poll)
	 *
	 * @param P the Poll object to cache
	 */
	public void addPoll(Poll P);

	/**
	 * Given a fully populated Poll object,
	 * this will create the Poll in the database,
	 * and add it to the cache.
	 *
	 * @see PollManager#addPoll(Poll)
	 * @see PollManager#loadPollByName(String)
	 * @see PollManager#loadPollIfNecessary(Poll)
	 *
	 * @param P the fully populated Poll object
	 */
	public void createPoll(Poll P);

	/**
	 * Given a Poll object that might be cached,
	 * this will remove the object from the cache.
	 *
	 * @see PollManager#deletePoll(Poll)
	 *
	 * @param P the Poll object to de-cache
	 */
	public void removePoll(Poll P);

	/**
	 * Given a Poll object with a name filled in,
	 * this will delete the Poll from the database
	 * and remove it from the cache.
	 *
	 * @see PollManager#removePoll(Poll)
	 *
	 * @param P a Poll object with a name
	 */
	public void deletePoll(Poll P);

	/**
	 * Given the name of a poll, this will attempt to load it
	 * from the database and return the fully populated
	 * Poll object.  Does *not* call addPoll.
	 *
	 * @see PollManager#loadPollIfNecessary(Poll)
	 * @see PollManager#addPoll(Poll)
	 *
	 * @param name the name of the poll
	 * @return the Poll object, or null
	 */
	public Poll loadPollByName(String name);

	/**
	 * Given a Poll object which might contain nothing more than a name,
	 * this will load the entirety of the Poll object, if necessary.
	 *
	 * @see PollManager#loadPollByName(String)
	 * @see PollManager#addPoll(Poll)
	 *
	 * @param P the Poll object, which might need filling out
	 * @return true if loaded, false if problems
	 */
	public boolean loadPollIfNecessary(Poll P);
}
