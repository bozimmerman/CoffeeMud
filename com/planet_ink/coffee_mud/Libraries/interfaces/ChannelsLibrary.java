package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.PreviousCmd;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
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
 * The main chat channels management library.  This is one of the 
 * older libraries in the system, and as such, still reflects the
 * use of indexed vectors that was common.  It still makes sense
 * given the way those numbers are used in CMMsg message codes to
 * identify the channels.  
 * 
 * Over the years, the ability to filter the channels by user
 * was added, along with flags to automatically insert information
 * or enable other parts of the system to generate channel
 * messages.
 * 
 * @author Bo Zimmerman
 */
public interface ChannelsLibrary extends CMLibrary
{
	/**
	 * Returns the number of registered channels
	 * @see ChannelsLibrary#getChannelIndex(String)
	 * @return the number of registered channels
	 */
	public int getNumChannels();

	/**
	 * Returns the CMChannel object for the given
	 * registered channel from 0 - getNumChannels();
	 * Basically this is almost all you need, but
	 * there's more....
	 * @see ChannelsLibrary#getNumChannels()
	 * @param channelNumber the index of the channel
	 * @return the CMChannel object
	 */
	public CMChannel getChannel(int channelNumber);

	/**
	 * Generates a list of previous channel messages, in ChannelMsg
	 * object format.  This may potentially hit the database.
	 * @see ChannelsLibrary#getChannelIndex(String)
	 * @see ChannelsLibrary.ChannelMsg
	 * @param channelNumber the channel id number/index
	 * @param numNewToSkip starting message number (0 based)
	 * @param numToReturn total number of messages to return
	 * @return the list of messages
	 */
	public List<ChannelMsg> getChannelQue(int channelNumber, int numNewToSkip, int numToReturn);

	/**
	 * Returns whether the given Mob can read a channel message from the given sender, on 
	 * a particular channel.
	 * @see ChannelsLibrary#getNumChannels()
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, int, boolean)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, Session, int)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, MOB, int, boolean)
	 * @param sender the sender of the channel message
	 * @param areaReq true if the message can only be read by someone in the senders Area
	 * @param M the potential receiver of the message to confirm
	 * @param channelNumber the channel index number
	 * @return true if the mob can read the senders message, false otherwise
	 */
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int channelNumber);

	/**
	 * Returns whether the given Mob can read a channel message from the given sender, on 
	 * a particular channel.
	 * @see ChannelsLibrary#getChannelIndex(String)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, int, boolean)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, Session, int)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, MOB, int)
	 * @param sender the sender of the channel message
	 * @param areaReq true if the message can only be read by someone in the senders Area
	 * @param M the potential receiver of the message to confirm
	 * @param channelNumber the channel index number
	 * @param offlineOK true if the channel reader can be read, or offline, false if they must be online
	 * @return true if the mob can read the senders message, false otherwise
	 */
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int channelNumber, boolean offlineOK);

	/**
	 * Returns whether the given Mob can read a channel message from the given sender, on 
	 * a particular channel.
	 * @see ChannelsLibrary#getNumChannels()
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, int, boolean)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, MOB, int)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, MOB, int, boolean)
	 * @param sender the sender of the channel message
	 * @param areaReq true if the message can only be read by someone in the senders Area
	 * @param ses the potential receiver session of the message to confirm
	 * @param channelNumber the channel index number
	 * @return true if the mob can read the senders message, false otherwise
	 */
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, Session ses, int channelNumber);

	/**
	 * Returns whether the given Mob can read a channel message from the given sender, on 
	 * a particular channel.
	 * @see ChannelsLibrary#getChannelIndex(String)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, MOB, int)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, Session, int)
	 * @see ChannelsLibrary#mayReadThisChannel(MOB, boolean, MOB, int, boolean)
	 * @param M the potential receiver of the message to confirm
	 * @param channelNumber the channel index number
	 * @param zapCheckOnly the channel index number
	 * @return true if the mob can read the senders message, false otherwise
	 */
	public boolean mayReadThisChannel(MOB M, int channelNumber, boolean zapCheckOnly);

	/**
	 * Adds the given channel message CMMsg event message to the que for the given
	 * channel number.  Will cause a database write.
	 * @see ChannelsLibrary#getNumChannels()
	 * @see ChannelsLibrary#getChannelIndex(String)
	 * @param channelNumber the channel index number
	 * @param msg the channel message msg that was sent around
	 */
	public void channelQueUp(int channelNumber, CMMsg msg);

	/**
	 * Returns the official index number of the channel with the
	 * given name or which starts with the given name.  It is
	 * case insensitive search. 
	 * @see ChannelsLibrary#getNumChannels()
	 * @param channelName the channel string to search for
	 * @return the official index number of the channel
	 */
	public int getChannelIndex(String channelName);

	/**
	 * Returns the bitmask appropriate to the channel of the given
	 * name, or which starts with the given string.  The bitmask
	 * is used for the individual player toggle switches.
	 * @param channelName the name of the channel
	 * @return the bitmask appropriate to the channel
	 */
	public int getChannelCodeNumber(String channelName);

	/**
	 * Returns the list of channel names that have the given
	 * ChannelFlag set.
	 * @see ChannelsLibrary.ChannelFlag
	 * @param flag the flag to find channels for
	 * @return the list of channel names with the flag set.
	 */
	public List<String> getFlaggedChannelNames(ChannelFlag flag);
	
	/**
	 * Returns the friendly readable description of the channel
	 * with the given name, or which starts with the given string.
	 * It's mostly requirements to read or write to the channel.
	 * @param channelName the name of the channel
	 * @return the friendly readable description of the channel
	 */
	public String getExtraChannelDesc(String channelName);

	/**
	 * Returns all the CMChannel objects for any channels flagged
	 * as being mapped to IMC2.
	 * @return all the CMChannel objects for IMC2
	 */
	public List<CMChannel> getIMC2ChannelsList();

	/**
	 * Returns all the CMChannel objects for any channels flagged
	 * as being mapped to I3.
	 * @return all the CMChannel objects for I3
	 */
	public List<CMChannel> getI3ChannelsList();

	/**
	 * Returns an array of all the names of all the channels.
	 * @see ChannelsLibrary#getNumChannels()
	 * @return an array of all the names of all the channels.
	 */
	public String[] getChannelNames();

	/**
	 * Returns the official channel name of the channel with the
	 * given name or which starts with the given name.  It is
	 * case insensitive search. 
	 * @see ChannelsLibrary#getNumChannels()
	 * @param channelName the channel string to search for
	 * @return the official channel name of the channel
	 */
	public String findChannelName(String channelName);

	/**
	 * Searches all users online for any sessions that are snooping on the
	 * given session, and also are not permitted to read the given channel
	 * number.  It then returns all the offending sessions, after forcing
	 * them to stop snooping.
	 * @see ChannelsLibrary#getNumChannels()
	 * @see ChannelsLibrary#restoreInvalidSnoopers(Session, List)
	 * @param mySession the session to search for snoopers of
	 * @param channelNumber the channel number to cross reference snoopers by
	 * @return all the offending sessions
	 */
	public List<Session> clearInvalidSnoopers(Session mySession, int channelNumber);
	
	/**
	 * Iterates through the given list of sessions and forces them all to snoop on
	 * the given session.  This makes sense if you check the clearInvalidSnoopers
	 * method.
	 * @see ChannelsLibrary#clearInvalidSnoopers(Session, int)
	 * @param mySession the session to search restoring snooping on
	 * @param invalid the list of sessions to force resnooping
	 */
	public void restoreInvalidSnoopers(Session mySession, List<Session> invalid);

	/**
	 * Clears the channels list and then reloads it from the three given comma-delimited
	 * list of coded strings.
	 * @param list the main game channel list
	 * @param ilist the list of i3 channels
	 * @param imc2list the list of imc2 channels
	 * @return the total number of channels loaded
	 */
	public int loadChannels(String list, String ilist, String imc2list);

	/**
	 * Sends the given channel message from the given sender to the given session on the
	 * given channelNumbered channel.  
	 * @see ChannelsLibrary#getNumChannels()
	 * @see ChannelsLibrary#createAndSendChannelMessage(MOB, String, String, boolean)
	 * @param ses the session to send the channel message to
	 * @param areareq true if the sender and session must be in the same area, false otherwise
	 * @param channelNumber the channel index number of the message
	 * @param msg the constructed CMMsg channel message
	 * @param sender the sender of the channel message
	 * @return true if the message was sent, and false otherwise
	 */
	public boolean sendChannelCMMsgTo(Session ses, boolean areareq, int channelNumber, CMMsg msg, MOB sender);

	/**
	 * Creates and sends the given string message on the channel with the given name to all sessions who are
	 * allowed to receive the message from the given sender.
	 * @see ChannelsLibrary#sendChannelCMMsgTo(Session, boolean, int, CMMsg, MOB)
	 * @param mob the sender of the message
	 * @param channelName the name of the channel to send the message on
	 * @param message the string message to send on the channel
	 * @param systemMsg true to format as a system message, false for a normal chat message
	 */
	public void createAndSendChannelMessage(MOB mob, String channelName, String message, boolean systemMsg);
	
	/**
	 * Creates a new channel object.
	 * @see ChannelsLibrary.CMChannel
	 * @see ChannelsLibrary.ChannelFlag
	 * @param name the channel name
	 * @param i3Name empty string, or the mapped name of the i3 channel
	 * @param imc2Name empty string, or the mapped name of the imc2 channel
	 * @param mask the zapper mask for who may read the channel
	 * @param flags the channel flags to set for the given channel
	 * @param colorOverrideANSI empty string for default, or the color code for this channel
	 * @param colorOverrideWords empty string for default, or the color code for this channel
	 * @return the newly created channel object
	 */
	public CMChannel createNewChannel(final String name, final String i3Name, final String imc2Name, 
									  final String mask, final Set<ChannelFlag> flags, 
									  final String colorOverrideANSI, final String colorOverrideWords);

	/**
	 * Basic Channel definition
	 * @author Bo Zimmerman
	 */
	public static interface CMChannel
	{
		/**
		 * The name of the channel, always uppercased
		 * @return name of the channel
		 */
		public String name();
		
		/**
		 * An empty string, or the name of the I3 channel that
		 * this channel is mapped to.
		 * @return the name of the I3 channel or ""
		 */
		public String i3name();
		
		/**
		 * An empty string, or the name of the IMC2 channel that
		 * this channel is mapped to.
		 * @return the name of the IMC2 channel or ""
		 */
		public String imc2Name();
		
		/**
		 * The zapper mask to filter in those who may read this
		 * channel.
		 * @see MaskingLibrary
		 * @return the zapper mask for who may read
		 */
		public String mask();
		
		/**
		 * Empty string, or a color ansi codes to override the
		 * default channel color
		 * @see ColorLibrary
		 * @return Empty string, or a color ansi codes to override
		 */
		public String colorOverrideANSICodes();
		
		/**
		 * Empty string, or a color code words to override the
		 * default channel color
		 * @see ColorLibrary
		 * @return Empty string, or a color code words to override
		 */
		public String colorOverrideWords();
		
		/**
		 * The channel flags for this channel.
		 * @see ChannelsLibrary.ChannelFlag
		 * @return channel flags for this channel.
		 */
		public Set<ChannelFlag> flags();
		
		/**
		 * The internal cached queue of previous 
		 * channel messages.
		 * @see ChannelsLibrary.ChannelMsg
		 * Always trimmed when it gets too large/
		 * @return internal cached queue of messages
		 */
		public SLinkedList<ChannelMsg> queue();
	}

	/**
	 * The entry for the channel que, containing the CMMsg
	 * object that was sent as the original message as well
	 * as the timestamp when it was sent.
	 * @author Bo Zimmerman
	 *
	 */
	public interface ChannelMsg
	{
		/**
		 * The CMMsg object that was sent as the original message
		 * @return CMMsg object that was sent as the original message
		 */
		public CMMsg msg();
		
		/**
		 * The timestamp of when the message was sent
		 * @return timestamp of when the message was sent
		 */
		public long sentTimeMillis();
	}

	/**
	 * The channel flags that define extra stuff that appears in or
	 * gets automatically sent on a given channel.
	 * @author Bo Zimmerman
	 *
	 */
	public static enum ChannelFlag
	{
		DEFAULT,
		SAMEAREA,
		CLANONLY,
		READONLY,
		EXECUTIONS,
		LOGINS,
		LOGOFFS,
		BIRTHS,
		MARRIAGES,
		DIVORCES,
		CHRISTENINGS,
		LEVELS,
		DETAILEDLEVELS,
		DEATHS,
		DETAILEDDEATHS,
		CONQUESTS,
		CONCEPTIONS,
		NEWPLAYERS,
		LOSTLEVELS,
		PLAYERPURGES,
		CLANINFO,
		WARRANTS,
		PLAYERREADONLY,
		ARCHONREADONLY,
		CLANALLYONLY,
		ACCOUNTOOC,
		NOBACKLOG,
		ACHIEVEMENTS,
		ACCOUNTOOCNOADMIN,
		NOLANGUAGE,
		TWITTER,
		ADDROOM,
		ADDAREA,
		ADDACCOUNT
	}
}
