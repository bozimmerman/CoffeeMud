package com.planet_ink.coffee_mud.interfaces;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public interface PlayerStats
{
	public static final String defaultPrompt="^N<^c%hhp %mm %vmv^N>";
	public String getEmail();
	public void setEmail(String newAdd);
	/** When the USER last logged off */
	public long lastDateTime();
	public long lastUpdated();
	public void setLastDateTime(long C);
	public void setUpdated(long time);
	/** User PASSWORD */
	public String password();
	public void setPassword(String newPassword);
	public void setChannelMask(int newMask);
	public int getChannelMask();
	public String getColorStr();
	public void setColorStr(String color);
	public String getPrompt();
	public void setPrompt(String prompt);
	public HashSet getFriends();
	public HashSet getIgnored();
	public void addTellStack(String msg);
	public Vector getTellStack();
	public void addGTellStack(String msg);
	public Vector getGTellStack();
	public String poofIn();
	public String poofOut();
	public String tranPoofIn();
	public String tranPoofOut();
	public void setPoofs(String poofIn, String poofOut, String tranPoofIn, String tranPoofOut);
	public String getXML();
	public void setXML(String str);
	public String lastIP();
	public void setLastIP(String ip);
	public void setReplyTo(MOB mob);
	public MOB replyTo();
	public Vector getSecurityGroups();
}
