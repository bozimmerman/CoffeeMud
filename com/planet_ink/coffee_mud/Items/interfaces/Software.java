package com.planet_ink.coffee_mud.Items.interfaces;
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
   Copyright 2005-2018 Bo Zimmerman

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
 * Software is a plain technical item that monitors and manipulates
 * other space ship components and systems, while also managing
 * a user interface for players/mobs monitoring the software.
 * These user interfaces are divided into menus.  Each software
 * can specify a "parent menu", referring to another software
 * or system that gives access to this software.
 * @author Bo Zimmerman
 *
 */
public interface Software extends Item, Technical
{
	/**
	 * The parent menu that this software gets access from.
	 * When Software is available from root, "" is returned.
	 * @return parent menu that this software gets access from
	 */
	public String getParentMenu();
	
	/**
	 * Returns the internal name of this software.
	 * @return the internal name of this software.
	 */
	public String getInternalName();
	
	/**
	 * Returns whether the given computer-entry command
	 * should be responded to by THIS software object
	 * on an activation command.
	 * @param word the computer-entry command entered
	 * @return true if this software should respond.
	 */
	public boolean isActivationString(String word);
	
	/**
	 * Returns whether the given computer-entry command
	 * should be responded to by THIS software object
	 * on a deactivation command.
	 * @param word the computer-entry command entered
	 * @return true if this software should respond.
	 */
	public boolean isDeActivationString(String word);
	
	/**
	 * Returns whether the given computer-entry command
	 * should be responded to by THIS software object
	 * on a WRITE/ENTER command.
	 * @param word the computer-entry command
	 * @param isActive true if the software is already activated
	 * @return true if this software can respond
	 */
	public boolean isCommandString(String word, boolean isActive);
	
	/**
	 * Returns the menu name of this software, so that it can
	 * be identified on its parent screen.
	 * @return the menu name of this software
	 */
	public String getActivationMenu();
	
	/**
	 * Adds a new message to the screen from this program, which
	 * will be received by those monitoring the computer
	 * @see Software#getScreenMessage()
	 * @see Software#getCurrentScreenDisplay()
	 * @param msg the new message for the screen
	 */
	public void addScreenMessage(String msg);
	
	/**
	 * Returns any new messages from this program when
	 * it is activated and on the screen.  Seen by those
	 * monitoring the computer.
	 * @see Software#addScreenMessage(String)
	 * @see Software#getCurrentScreenDisplay()
	 * @return the new screen messages
	 */
	public String getScreenMessage();
	
	/**
	 * Returns the full screen appearance of this program when
	 * it is activated and on the screen.  Only those intentially
	 * looking at the screen again, or forced by the program, will
	 * see this larger message.
	 * @see Software#addScreenMessage(String)
	 * @see Software#getScreenMessage()
	 * @return the entire screen message
	 */
	public String getCurrentScreenDisplay();
	
	/**
	 * Software runs on computers, and computers run on power systems.
	 * This method tells the software what the power system "circuit" key
	 * is that the computer host is running on, allowing the software to
	 * find other equipment on the same circuit and control it.
	 * @param key the circuit key
	 */
	public void setCircuitKey(String key);
}

