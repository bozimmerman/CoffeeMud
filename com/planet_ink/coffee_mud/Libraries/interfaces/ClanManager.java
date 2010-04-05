package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public interface ClanManager extends CMLibrary
{
    /**
     * @return
     */
    public Enumeration allClans();
    /**
     * @return
     */
    public int numClans();
    /**
     * @param id1
     * @param id2
     * @param relation
     * @return
     */
    public boolean isCommonClanRelations(String id1, String id2, int relation);
    /**
     * @param id1
     * @param id2
     * @return
     */
    public int getClanRelations(String id1, String id2);
    /**
     * @param id
     * @return
     */
    public Clan getClan(String id);
    /**
     * @param id
     * @return
     */
    public Clan findClan(String id);
    /**
     * @param type
     * @return
     */
    public Clan getClanType(int type);
    /**
     * @param role
     * @return
     */
    public int getRoleOrder(int role);
    /**
     * @param government
     * @param role
     * @param titleCase
     * @param plural
     * @return
     */
    public String getRoleName(int government, int role, boolean titleCase, boolean plural);
    /**
     * @return
     */
    public Enumeration clans();
    /**
     * @return
     */
    public int size();
    /**
     * @param C
     */
    public void addClan(Clan C);
    /**
     * @param C
     */
    public void removeClan(Clan C);
    /**
     * 
     */
    public void tickAllClans();
    /**
     * @param msg
     */
    public void clanAnnounceAll(String msg);
    /**
     * @param trophy
     * @return
     */
    public String translatePrize(int trophy);
    /**
     * @return
     */
    public boolean trophySystemActive();
    /**
     * 
     * @param M
     * @param members
     * @return
     */
    public boolean isFamilyOfMembership(MOB M, List<MemberRecord> members);
    
    /**
     * 
     * @param mob
     * @param msg
     */
    public void clanAnnounce(MOB mob, String msg);
    
    /**
     * 
     * @param mob
     * @param C
     * @param commands
     * @param function
     * @param voteIfNecessary
     * @return
     */
    public boolean goForward(MOB mob, Clan C, Vector commands, int function, boolean voteIfNecessary);
    
    
    /**
     * 
     * @param government
     * @param position
     * @return
     */
    public int getRoleFromName(int government, String position);
    
    /**
     * 
     * @param roleType
     * @return
     */
    public int getIntFromRole(int roleType);
}
