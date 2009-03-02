package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2009 Bo Zimmerman

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
public interface JournalsLibrary extends CMLibrary, Runnable
{
    public int loadCommandJournals(String list);
    public Enumeration<CommandJournal> journals();
    public CommandJournal getCommandJournal(String named);
    public int getNumCommandJournals();
    public String getScriptValue(MOB mob, String journal, String oldValue);
    
	public static final String JOURNAL_BOUNDARY="%0D^w---------------------------------------------^N%0D";
	
	public static class JournalEntry
	{
		public String key;
		public String from;
		public String date;
		public String to;
		public String subj;
		public String msg;
		public String update;
	}
	
    public static class CommandJournal
    {
    	protected String name="";
    	protected String mask="";
    	protected Hashtable<JournalFlag,String> flags=new Hashtable<JournalFlag,String>(1);
    	public CommandJournal(String name, String mask, Hashtable<JournalFlag,String> flags)
    	{
    		this.name=name;
    		this.mask=mask;
    		this.flags=flags;
    	}
    	public String NAME(){return name;}
    	public String mask(){return mask;}
    	public String getFlag(JournalFlag flag){return flags.get(flag);} 
    	public String getScriptFilename(){return flags.get(JournalFlag.SCRIPT);}
    }
    
    public static enum JournalFlag {
        CHANNEL,ADDROOM,EXPIRE,ADMINECHO,CONFIRM,SCRIPT;
    };
}
