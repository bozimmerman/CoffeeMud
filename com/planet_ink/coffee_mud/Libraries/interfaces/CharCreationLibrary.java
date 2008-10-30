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
@SuppressWarnings("unchecked")
public interface CharCreationLibrary extends CMLibrary
{
    /**
     * @param mob
     * @param C
     */
    public void reRollStats(MOB mob, CharStats C);
    /**
     * @param mob
     * @param thisClass
     * @param theme
     * @return
     */
    public boolean classOkForMe(MOB mob, CharClass thisClass, int theme);
    /**
     * @param mob
     * @param theme
     * @return
     */
    public Vector classQualifies(MOB mob, int theme);
    /**
     * @param login
     * @return
     */
    public boolean isOkName(String login);
    /**
     * @param mob
     */
    public void reloadTerminal(MOB mob);
    /**
     * @param mob
     */
    public void showTheNews(MOB mob);
    /**
     * @param mob
     * @return
     */
    public boolean checkExpiration(MOB mob);
    /**
     * @param mob
     * @param login
     * @param session
     * @return
     * @throws java.io.IOException
     */
    public boolean createCharacter(MOB mob, String login, Session session)
        throws java.io.IOException;
    
    /**
     * 0=no login, 1=login, 2=swap login
     * @param mob
     * @param attempt
     * @return
     * @throws java.io.IOException
     */
    public int login(MOB mob, int attempt)
        throws java.io.IOException;
    
    public void pageRooms(CMProps page, Hashtable table, String start);
    
    /**
     * 
     * @param page
     */
    public void initStartRooms(CMProps page);
    
    /**
     * 
     * @param page
     */
    public void initDeathRooms(CMProps page);
    
    /**
     * 
     * @param page
     */
    public void initBodyRooms(CMProps page);
    
    /**
     * 
     * @param mob
     * @return
     */
    public Room getDefaultStartRoom(MOB mob);
    
    /**
     * 
     * @param mob
     * @return
     */
    public Room getDefaultDeathRoom(MOB mob);
    
    /**
     * 
     * @param mob
     * @return
     */
    public Room getDefaultBodyRoom(MOB mob);
}
