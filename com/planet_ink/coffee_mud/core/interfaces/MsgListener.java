package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
 * An object which is permitted to handle game events in CoffeeMud.  Almost all objects implement this interface.
 * @author Bo Zimmerman
 *
 */
public interface MsgListener
{
	/**
	 * The general message event handler for the object.  Messages passed herein
	 * may not necessarily be FOR this object, or from it, but will almost
	 * always represent events happening in the same room.  The messages
	 * have already been through an approval process, so this method is
	 * called only to affect the final execution of the meaning of the
	 * message.  Every game event goes through these methods.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg
	 * @param myHost either the initiator of the event, or the host of this object
	 * @param msg the CMMsg that needs to be executed
	 */
	public void executeMsg(final Environmental myHost, final CMMsg msg);

	/**
	 * The general message event previewer for the object.  Messages passed herein
	 * are in a pending state, and may be safely modified or rejected without fear
	 * that they might be in the middle of being executed.  Messages passed herein
	 * may not necessarily be FOR or FROM this object, but will almost always
	 * represent events which want to happen in the same rom.  This method should
	 * always always return true UNLESS this message needs to be canceled, in which
	 * case it is necessary to tell the mob initiating the event (CMMsg.source())
	 * why it is being cancelled.  Every game event goes through these methods.
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg
	 * @see com.planet_ink.coffee_mud.Common.interfaces.CMMsg#source()
	 * @param myHost either the initiator of the event, or the host of this object
	 * @param msg the CMMsg that wants to be executed
	 * @return whether this message is allowed to execute
	 */
	public boolean okMessage(final Environmental myHost, final CMMsg msg);

}
