package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Law.TreasurySet;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/*
   Copyright 2015-2020 Bo Zimmerman

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
public class DefaultJournalEntry implements JournalEntry
{
	public String		key					= null;
	public String		from;
	public String		to;
	public String		subj;
	public String		msg;
	public long			date				= 0;
	public long			update				= 0;
	public String		parent				= "";
	public long			attributes			= 0;
	public String		data				= "";
	public int			cardinal			= 0;
	public String		msgIcon				= "";
	public int			replies				= 0;
	public int			views				= 0;
	public boolean		isLastEntry			= false;
	public StringBuffer	derivedBuildMessage	= null;

	@Override
	public String ID()
	{
		return "DefaultJournalEntry";
	}

	@Override
	public String name()
	{
		return key;
	}

	@Override
	public CMObject newInstance()
	{
		try
		{
			return getClass().newInstance();
		}
		catch (final Exception e)
		{
			return new DefaultJournalEntry();
		}
	}

	@Override
	public void initializeClass()
	{
	}

	@Override
	public int compareTo(final CMObject o)
	{
		if(o instanceof JournalEntry)
			return compareTo((JournalEntry)o);
		return 1;
	}

	@Override
	public String key()
	{
		return key;
	}

	@Override
	public JournalEntry key(final String key)
	{
		this.key = key;
		return this;
	}

	@Override
	public String from()
	{
		return from;
	}

	@Override
	public JournalEntry from(final String from)
	{
		this.from = from;
		return this;
	}

	@Override
	public String to()
	{
		return to;
	}

	@Override
	public JournalEntry to(final String to)
	{
		this.to = to;
		return this;
	}

	@Override
	public String subj()
	{
		return subj;
	}

	@Override
	public JournalEntry subj(final String subj)
	{
		this.subj = subj;
		return this;
	}

	@Override
	public String msg()
	{
		return msg;
	}

	@Override
	public JournalEntry msg(final String msg)
	{
		this.msg = msg;
		return this;
	}

	@Override
	public long date()
	{
		return date;
	}

	@Override
	public JournalEntry date(final long date)
	{
		this.date = date;
		return this;
	}

	@Override
	public long update()
	{
		return update;
	}

	@Override
	public JournalEntry update(final long update)
	{
		this.update = update;
		return this;
	}

	@Override
	public String parent()
	{
		return parent;
	}

	@Override
	public JournalEntry parent(final String parent)
	{
		this.parent = parent;
		return this;
	}

	@Override
	public long attributes()
	{
		return attributes;
	}

	@Override
	public JournalEntry attributes(final long attributes)
	{
		this.attributes = attributes;
		return this;
	}

	@Override
	public String data()
	{
		return data;
	}

	@Override
	public JournalEntry data(final String data)
	{
		this.data = data;
		return this;
	}

	@Override
	public int cardinal()
	{
		return cardinal;
	}

	@Override
	public JournalEntry cardinal(final int cardinal)
	{
		this.cardinal = cardinal;
		return this;
	}

	@Override
	public String msgIcon()
	{
		return msgIcon;
	}

	@Override
	public JournalEntry msgIcon(final String msgIcon)
	{
		this.msgIcon = msgIcon;
		return this;
	}

	@Override
	public int replies()
	{
		return replies;
	}

	@Override
	public JournalEntry replies(final int replies)
	{
		this.replies = replies;
		return this;
	}

	@Override
	public int views()
	{
		return views;
	}

	@Override
	public JournalEntry views(final int views)
	{
		this.views = views;
		return this;
	}

	@Override
	public boolean isLastEntry()
	{
		return isLastEntry;
	}

	@Override
	public JournalEntry lastEntry(final boolean lastEntry)
	{
		this.isLastEntry = lastEntry;
		return this;
	}

	@Override
	public StringBuffer derivedBuildMessage()
	{
		return derivedBuildMessage;
	}

	@Override
	public JournalEntry derivedBuildMessage(final StringBuffer msg)
	{
		this.derivedBuildMessage = msg;
		return this;
	}

	@Override
	public int compareTo(final JournalEntry o)
	{
		if(date < o.date())
			return -1;
		if(date > o.date())
			return 1;
		return 0;
	}

	@Override
	public JournalEntry copyOf()
	{
		try
		{
			return (JournalEntry) this.clone();
		}
		catch (final Exception e)
		{
			return new DefaultJournalEntry();
		}
	}

}
