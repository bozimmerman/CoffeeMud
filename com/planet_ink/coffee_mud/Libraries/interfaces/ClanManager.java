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
public interface ClanManager extends CMObject
{
    public Enumeration allClans();
    public int numClans();
    public void shutdownClans();
    public boolean isCommonClanRelations(String id1, String id2, int relation);
    public int getClanRelations(String id1, String id2);
    public Clan getClan(String id);
    public Clan findClan(String id);
    public Clan getClanType(int type);
    public int getRoleOrder(int role);
    public String getRoleName(int government, int role, boolean titleCase, boolean plural);
    public Enumeration clans();
    public int size();
    public void addClan(Clan C);
    public void removeClan(Clan C);
    public void tickAllClans();
    public void clanAnnounceAll(String msg);
    public String translatePrize(int trophy);
    public boolean trophySystemActive();
}
