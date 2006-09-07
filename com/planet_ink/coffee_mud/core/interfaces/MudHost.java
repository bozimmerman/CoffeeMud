package com.planet_ink.coffee_mud.core.interfaces;
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
import java.util.Properties;

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
/** 
 * The interface implemented by the main mud application.  Includes several timing constants.
 * @author Bo Zimmerman
 *
 */
public interface MudHost
{
    /** the number of milliseconds between each savethread execution */
	public final static long TIME_SAVETHREAD_SLEEP=60*60000; // 60 minutes, right now.
    /** the number of milliseconds between each utilithread execution */
	public final static long TIME_UTILTHREAD_SLEEP=Tickable.TIME_MILIS_PER_MUDHOUR;
    /**
     * the hostname of the mud server
     * @return hostname or ip address 
     */
	public String getHost();
    /**
     * the port a given MUD server instance is listening on
     * @return the port numbered listened on by this mud instance
     */
	public int getPort();
    /**
     * An order to permanently shutdown the entire mud system
     * @param S a player session to send status messages to.  May be null.
     * @param keepItDown true to shutdown, false to restart
     * @param externalCommand if keepItDown is false, an external command to execute
     */
	public void shutdown(Session S, boolean keepItDown, String externalCommand);
    /**
     * Retreive a string telling the status of mud startup or shutdown
     * @return status of mud startup or shutdown
     */
    public String getStatus();
}
