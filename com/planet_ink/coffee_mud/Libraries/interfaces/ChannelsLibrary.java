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
   Copyright 2005-2015 Bo Zimmerman

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
	 * @return the number of registered channels
	 */
	public int getNumChannels();
	
	/**
	 * Returns the CMChannel object for the given
	 * registered channel from 0 - getNumChannels();
	 * Basically this is almost all you need, but
	 * there's more....
	 * @see ChannelsLibrary#getNumChannels()
	 * @param i the index of the channel
	 * @return the CMChannel object
	 */
	public CMChannel getChannel(int i);
	
	
	public List<ChannelMsg> getChannelQue(int i, int numNewToSkip, int numToReturn);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i, boolean offlineOK);
	public boolean mayReadThisChannel(MOB sender, boolean areaReq, Session ses, int i);
	public boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly);
	public void channelQueUp(int i, CMMsg msg);
	public int getChannelIndex(String channelName);
	public int getChannelCodeNumber(String channelName);
	public List<String> getFlaggedChannelNames(ChannelFlag flag);
	public String getExtraChannelDesc(String channelName);
	public List<CMChannel> getIMC2ChannelsList();
	public List<CMChannel> getI3ChannelsList();
	public String[] getChannelNames();
	public String findChannelName(String channelName);
	public List<Session> clearInvalidSnoopers(Session mySession, int channelCode);
	public void restoreInvalidSnoopers(Session mySession, List<Session> invalid);
	public int loadChannels(String list, String ilist, String imc2list);
	public boolean channelTo(Session ses, boolean areareq, int channelInt, CMMsg msg, MOB sender);
	public void reallyChannel(MOB mob, String channelName, String message, boolean systemMsg);
	public CMChannel createNewChannel(final String name, final String i3Name, final String imc2Name, 
									  final String mask, final String colorOverride, final String colorOverrideStr, 
									  final Set<ChannelFlag> flags);

	/**
	 * Basic Channel definition
	 * @author Bo Zimmerman
	 */
	public static interface CMChannel
	{
		public String name();
		public String i3name();
		public String imc2Name();
		public String mask();
		public String colorOverride();
		public String colorOverrideStr();
		public Set<ChannelFlag> flags();
		public SLinkedList<ChannelMsg> queue();
	}

	public interface ChannelMsg
	{
		public CMMsg msg();
		public long ts();
	}

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
		CLANALLYONLY,
		ACCOUNTOOC,
		NOBACKLOG,
		ACHIEVEMENTS
	}
}
