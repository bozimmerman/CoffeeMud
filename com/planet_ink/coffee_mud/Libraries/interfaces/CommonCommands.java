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

import java.io.IOException;
import java.util.*;
/* 
   Copyright 2000-2005 Bo Zimmerman

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
public interface CommonCommands extends CMObject
{
    public boolean doStandardCommand(MOB mob, String command, Vector parms);
    public StringBuffer getScore(MOB mob);
    public StringBuffer getEquipment(MOB viewer, MOB mob);
    public StringBuffer getInventory(MOB viewer, MOB mob);
    public void channel(MOB mob, String channelName, String message, boolean systemMsg);
    public void channel(String channelName, String clanID, String message, boolean systemMsg);
    public boolean drop(MOB mob, Environmental dropThis, boolean quiet, boolean optimized);
    public boolean get(MOB mob, Item container, Item getThis, boolean quiet);
    public boolean remove(MOB mob, Item item, boolean quiet);
    public void look(MOB mob, boolean quiet);
    public void flee(MOB mob, String whereTo);
    public void sheath(MOB mob, boolean ifPossible);
    public void draw(MOB mob, boolean doHold, boolean ifNecessary);
    public void stand(MOB mob, boolean ifNecessary);
    public void follow(MOB follower, MOB leader, boolean quiet);
    public void say(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag);
}
