package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.database.DBConnections;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2017-2017 Bo Zimmerman

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

public class StdPlayerBook extends StdBook
{
	@Override
	public String ID()
	{
		return "StdPlayerBook";
	}

	public StdPlayerBook()
	{
		super();
		setName("a book");
		setDisplayText("a book sits here.");
		setDescription("Enter `READ [NUMBER] [BOOK]` to read a chapter.%0D%0AUse your WRITE skill to add new chapters. ");
		material=RawMaterial.RESOURCE_PAPER;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		recoverPhyStats();
	}

	@Override
	protected int getChapterCount(final String to)
	{
		final String key=super.getParm("KEY");
		if((key == null)||(key.length()==0))
			return 0;
		final String cat="BOOK_"+key;
		return CMLib.database().DBCountPlayerData(cat);
	}

	@Override
	protected String getTOCHeader()
	{
		final String key=super.getParm("KEY");
		if((key == null)||(key.length()==0))
			return L("\n\rAn unfinished work.\n\r");
		final String cat="BOOK_"+key;
		List<String> authors = CMLib.database().DBReadPlayerDataPlayersBySection(cat);
		if(authors.size()==0)
			return L("\n\rAn unfinished work.\n\r");
		if(authors.size()==1)
			return L("\n\r@x1\n\rBy @x2\n\r\n\rTable of Contents\n\r",Name(),authors.get(0));
		return L("\n\r@x1\n\rBy Various Authors\n\r\n\rTable of Contents\n\r",Name());
	}
	
	@Override
	protected List<JournalEntry> readChaptersByCreateDate()
	{
		final String key=super.getParm("KEY");
		if((key == null)||(key.length()==0))
			return new ArrayList<JournalEntry>();
		final List<JournalEntry> entries = new ArrayList<JournalEntry>();
		final String cat="BOOK_"+key;
		final List<PlayerData> jentries = CMLib.database().DBReadPlayerSectionData(cat);
		Collections.sort(jentries, new Comparator<PlayerData>()
		{
			@Override
			public int compare(PlayerData o1, PlayerData o2)
			{
				final String key1=o1.key();
				final String key2=o2.key();
				final int x1=key1.lastIndexOf('_');
				final int x2=key2.lastIndexOf('_');
				if(x1<0 && x2<0)
					return 0;
				if(x1<0)
					return -1;
				if(x2<0)
					return 1;
				final int ch1=CMath.s_int(key1.substring(x1+1));
				final int ch2=CMath.s_int(key2.substring(x2+1));
				return ch1==ch2 ? 0 : ch1 > ch2 ? 1 : -1;
			}
		});
		
		final Set<String> authors=new TreeSet<String>();
		for(final PlayerData data : jentries)
		{
			if(!authors.contains(data.who()))
				authors.add(data.who());
		}
		final boolean addAuthors = authors.size() > 1;
		int chapter=1;
		for(final PlayerData data : jentries)
		{
			final JournalEntry entry=(JournalEntry)CMClass.getCommon("DefaultJournalEntry");
			entry.key		(data.key());
			entry.from		(data.who());
			entry.to		("ALL");
			
			if(addAuthors)
				entry.subj		(L("Chapter @x1 by @x2",""+chapter,data.who()));
			else
				entry.subj		(L("Chapter @x1 ",""+chapter));
			entry.parent	("");
			entry.attributes();
			entry.data		("");
			entry.update	(0);
			entry.views		(0);
			entry.replies	(0);
			entry.msg		(data.xml());
			entries.add(entry);
			chapter++;
		}
		return entries;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(this) && (msg.targetMinor() == CMMsg.TYP_WRITE))
		{
			if(msg.targetMessage().length()==0)
			{
				msg.source().tell(L("Write what on @x1?",name()));
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void  addNewChapter(final String from, final String to, final String subject, final String message)
	{
		String subkey=super.getParm("KEY");
		if((subkey == null)||(subkey.length()==0))
		{
			subkey=Math.random()+"_"+Math.random();
			setReadableText("KEY=\""+subkey+"\" "+super.readableText());
		}
		String cat="BOOK_"+subkey;
		String key=from.toUpperCase()+"_"+cat+"_"+this.getChapterCount(to);
		CMLib.database().DBCreatePlayerData(from, cat, key, message);
	}
}
