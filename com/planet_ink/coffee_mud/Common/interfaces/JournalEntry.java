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
Copyright 2015-2018 Bo Zimmerman

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

public interface JournalEntry extends CMCommon, Cloneable
{
	public String key();
	public JournalEntry key(String key);
	public String from();
	public JournalEntry from(String from);
	public String to();
	public JournalEntry to(String to);
	public String subj();
	public JournalEntry subj(String subj);
	public String msg();
	public JournalEntry msg(String msg);
	public long date();
	public JournalEntry date(long date);
	public long update();
	public JournalEntry update(long update);
	public String parent();
	public JournalEntry parent(String parent);
	public long attributes();
	public JournalEntry attributes(long attributes);
	public String data();
	public JournalEntry data(String data);
	public int cardinal();
	public JournalEntry cardinal(int cardinal);
	public String msgIcon();
	public JournalEntry msgIcon(String msgIcon);
	public int replies();
	public JournalEntry replies(int replies);
	public int views();
	public JournalEntry views(int views);
	public boolean isLastEntry();
	public JournalEntry lastEntry(boolean lastEntry);
	public StringBuffer derivedBuildMessage();
	public JournalEntry derivedBuildMessage(StringBuffer msg);
	public int compareTo(JournalEntry o);

	@Override
	public JournalEntry copyOf();

	public final static long ATTRIBUTE_STUCKY=2;
	public final static long ATTRIBUTE_PROTECTED=1;
}
