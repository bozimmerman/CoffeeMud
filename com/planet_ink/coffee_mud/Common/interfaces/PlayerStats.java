package com.planet_ink.coffee_mud.Common.interfaces;
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
   Copyright 2000-2006 Bo Zimmerman

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
public interface PlayerStats extends CMObject
{
	public static final String defaultPrompt="^N%E<^c%hhp %mm %vmv^N>";
	public String getEmail();
	public void setEmail(String newAdd);
	/** When the USER last logged off */
	public long lastDateTime();
	public long lastUpdated();
    public long leveledDateTime(int level);
	public void setLastDateTime(long C);
	public void setUpdated(long time);
    public void setLeveledDateTime(int level);
	/** User PASSWORD */
	public String password();
	public void setPassword(String newPassword);
	public void setChannelMask(int newMask);
	public int getChannelMask();
	public String getColorStr();
	public void setColorStr(String color);
	public String getPrompt();
	public void setPrompt(String prompt);
	public String notes();
	public void setNotes(String newnotes);
	public HashSet getFriends();
	public HashSet getIgnored();
	public Vector getTitles();
	public void addTellStack(String msg);
	public Vector getTellStack();
	public void addGTellStack(String msg);
	public Vector getGTellStack();
	public String poofIn();
	public String poofOut();
	public String tranPoofIn();
	public String tranPoofOut();
	public void setPoofs(String poofIn, String poofOut, String tranPoofIn, String tranPoofOut);
    public String announceMessage();
    public void setAnnounceMessage(String msg);
	public String getXML();
	public void setXML(String str);
	public String lastIP();
	public void setLastIP(String ip);
	public void setReplyTo(MOB mob);
	public MOB replyTo();
	public Vector getSecurityGroups();
	public int[] getBirthday();
	public int initializeBirthday(int ageHours, Race R);
	public int getWrap();
	public void setWrap(int newWrap);
	
	public final static long HYGIENE_DELIMIT=5000;
	public final static long HYGIENE_WATERCLEAN=-1000;
	public final static long HYGIENE_COMMONDIRTY=2;
	public final static long HYGIENE_FIGHTDIRTY=1;
	public long getHygiene();
	public void setHygiene(long newVal);
	public boolean adjHygiene(long byThisMuch);
    
    public void addRoomVisit(Room R);
    public boolean hasVisited(Room R);
    public int percentVisited(MOB mob, Area A);

    public String[] getAliasNames();
    public String getAlias(String named);
    public void addAliasName(String named);
    public void delAliasName(String named);
    public void setAlias(String named, String value);
    
    // Acct Exp
    public long getAccountExpiration();
    public void setAccountExpiration(long newVal);
}
