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
public interface JournalsLibrary extends CMLibrary
{
	public Set<String> getArchonJournalNames();
	public boolean isArchonJournalName(String journal);

	public int loadCommandJournals(String list);
	public Enumeration<CommandJournal> commandJournals();
	public CommandJournal getCommandJournal(String named);
	public int getNumCommandJournals();
	public String getScriptValue(MOB mob, String journal, String oldValue);

	public boolean canReadMessage(JournalEntry entry, String srchMatch, MOB readerM, boolean ignorePrivileges);
	public int loadForumJournals(String list);
	public Enumeration<ForumJournal> forumJournals();
	public ForumJournal getForumJournal(String named);
	public ForumJournal getForumJournal(String named, Clan clan);
	public int getNumForumJournals();

	public void registerClanForum(Clan clan, String allClanForumMappings);
	public List<ForumJournal> getClanForums(Clan clan);

	public boolean subscribeToJournal(String journalName, String userName, boolean saveMailingList);
	public boolean unsubscribeFromJournal(String journalName, String userName, boolean saveMailingList);

	public JournalMetaData getJournalStats(ForumJournal journal);
	public void clearJournalSummaryStats(ForumJournal journal);

	public enum MsgMkrResolution { SAVEFILE, CANCELFILE }

	public MsgMkrResolution makeMessage(final MOB mob, final String messageTitle, final List<String> vbuf, boolean autoAdd) throws IOException;
	public void makeMessageASync(final MOB mob, final String messageTitle, final List<String> vbuf, final boolean autoAdd, final MsgMkrCallback back);

	public static final String JOURNAL_BOUNDARY="%0D^w---------------------------------------------^N%0D";

	public interface MsgMkrCallback
	{
		public void callBack(final MOB mob, final Session sess, final MsgMkrResolution res);
	}
	
	public interface JournalMetaData
	{
		public String name();
		public JournalMetaData name(String intro);
		public int threads();
		public JournalMetaData threads(int num);
		public int posts();
		public JournalMetaData posts(int num);
		public String imagePath();
		public JournalMetaData imagePath(String intro);
		public String shortIntro();
		public JournalMetaData shortIntro(String intro);
		public String longIntro();
		public JournalMetaData longIntro(String intro);
		public String introKey();
		public JournalMetaData introKey(String key);
		public String latestKey();
		public JournalMetaData latestKey(String key);
		public List<String> stuckyKeys();
		public JournalMetaData stuckyKeys(List<String> keys);
	}

	public static interface CommandJournal
	{
		public String NAME();
		public String mask();
		public String JOURNAL_NAME();
		public String getFlag(CommandJournalFlags flag);
		public String getScriptFilename();
	}

	public static enum CommandJournalFlags
	{
		CHANNEL,
		ADDROOM,
		EXPIRE,
		ADMINECHO,
		CONFIRM,
		SCRIPT,
		REPLYSELF,
		REPLYALL,
		MOTD,
		ASSIGN;
	}

	public static interface SMTPJournal
	{
		public String  name();
		public boolean forward();
		public boolean subscribeOnly();
		public boolean keepAll();
		public String  criteriaStr();
		public MaskingLibrary.CompiledZMask criteria();
	}

	public static interface ForumJournal
	{
		public String NAME();
		public String readMask();
		public String postMask();
		public String replyMask();
		public String adminMask();
		public String getFlag(CommandJournalFlags flag);
		public boolean maskCheck(MOB M, String mask);
		public boolean authorizationCheck(MOB M, ForumJournalFlags fl);
	}

	public static enum ForumJournalFlags
	{
		EXPIRE,
		READ,
		POST,
		REPLY,
		ADMIN,
		SORTBY;
	}
}
