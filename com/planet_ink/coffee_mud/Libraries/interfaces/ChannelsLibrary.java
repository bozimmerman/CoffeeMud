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
   Copyright 2000-2008 Bo Zimmerman

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
public interface ChannelsLibrary extends CMLibrary
{
    public final int QUEUE_SIZE=100;
    
    /**
     * @return
     */
    public int getNumChannels();
    /**
     * @param i
     * @return
     */
    public String getChannelMask(int i);
    /**
     * @param i
     * @return
     */
    public Vector getChannelFlags(int i);
    /**
     * @param i
     * @return
     */
    public String getChannelName(int i);
    /**
     * @param i
     * @return
     */
    public Vector getChannelQue(int i);
    /**
     * @param sender
     * @param areaReq
     * @param M
     * @param i
     * @return
     */
    public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i);
    /**
     * @param sender
     * @param areaReq
     * @param M
     * @param i
     * @param offlineOK
     * @return
     */
    public boolean mayReadThisChannel(MOB sender, boolean areaReq, MOB M, int i, boolean offlineOK);
    /**
     * @param sender
     * @param areaReq
     * @param ses
     * @param i
     * @return
     */
    public boolean mayReadThisChannel(MOB sender, boolean areaReq, Session ses, int i);
    /**
     * @param M
     * @param i
     * @param zapCheckOnly
     * @return
     */
    public boolean mayReadThisChannel(MOB M, int i, boolean zapCheckOnly);
    /**
     * @param i
     * @param msg
     */
    public void channelQueUp(int i, CMMsg msg);
    /**
     * @param channelName
     * @return
     */
    public int getChannelIndex(String channelName);
    /**
     * @param channelName
     * @return
     */
    public int getChannelCodeNumber(String channelName);
    /**
     * @param channelName
     * @return
     */
    public String getChannelName(String channelName);
    /**
     * @param flag
     * @return
     */
    public Vector getFlaggedChannelNames(String flag);
    /**
     * @return
     */
    public String[][] imc2ChannelsArray();
    /**
     * @return
     */
    public String[][] iChannelsArray();
    /**
     * @return
     */
    public String[] getChannelNames();
    /**
     * @param mySession
     * @param channelCode
     * @return
     */
    public Vector clearInvalidSnoopers(Session mySession, int channelCode);
    /**
     * @param mySession
     * @param invalid
     */
    public void restoreInvalidSnoopers(Session mySession, Vector invalid);
    /**
     * @param mask
     * @param flags
     * @return
     */
    public String parseOutFlags(String mask, Vector flags);
    /**
     * @param list
     * @param ilist
     * @param imc2list
     * @return
     */
    public int loadChannels(String list, String ilist, String imc2list);
    /**
     * 
     */
    public static final String[] ALLFLAGS={
            "DEFAULT","SAMEAREA","CLANONLY","READONLY",
            "EXECUTIONS","LOGINS","LOGOFFS","BIRTHS","MARRIAGES", 
            "DIVORCES","CHRISTENINGS","LEVELS","DETAILEDLEVELS","DEATHS","DETAILEDDEATHS",
            "CONQUESTS","CONCEPTIONS","NEWPLAYERS","LOSTLEVELS","PLAYERPURGES","CLANINFO",
            "WARRANTS", "PLAYERREADONLY"};
        
}
