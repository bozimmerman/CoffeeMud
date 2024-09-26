package com.planet_ink.coffee_mud.Common.interfaces;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.DefaultFaction;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FRange;
import com.planet_ink.coffee_mud.Common.interfaces.Faction.FactionChangeEvent;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ColorLibrary.ColorState;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/*
   Copyright 2024-2024 Bo Zimmerman

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
 * A Formatting object stores guide information for how to format
 * output text from the mud to the user.
 *
 * This includes information like word wrap, color tables, and
 * feature lists.
 */

public interface Formatting extends CMCommon
{

	interface FormatFilter
	{
		/**
		 * Applies this filter to the text about to be sent to a session.
		 * Returns either the modified text, or null to cancel the filter.
		 * @param mob the mob whose session this is
		 * @param source the source of the text message
		 * @param target the target of the text message
		 * @param tool the tool being used by the message generator
		 * @param msg the message itself
		 *
		 * @return the modified message, or null to remove the filter
		 */
		public String applyFilter(MOB mob, final Physical source, final Environmental target, final Environmental tool, final String msg);
	}

	/**
	 * The FormatCode are the list of telnet-related codes that can affect how a string is formatted
	 *
	 * @author Bo Zimmerman
	 *
	 */
	public enum FCode
	{
		MXP,
		MSP,
		ANSI,
		ANSI16
	}

	/**
	 * Returns a list of standard/foreground telnet coded strings
	 * indexed by coffeemud color code.  May be from the standard list,
	 * or read from player records for a customized list.
	 * @return telnet coded color strings.
	 */
	public String[] getColorCodes();

	/**
	 * Returns the current color code.
	 * @return the current color code.
	 */
	public ColorState getCurrentColor();

	/**
	 * Sets the current color code.
	 * @param newcolor the color to change it to
	 */
	public void setCurrentColor(final ColorState newcolor);

	/**
	 * Returns the marked color code.
	 * @return the marked color code.
	 */
	public ColorState popMarkedColor();

	/**
	 * Sets the marked color code.
	 * @param newcolor the color to change it to
	 */
	public void pushMarkedColor(final ColorState newcolor);

	/**
	 * Returns the previous current color code.
	 * @return the previous current color code.
	 */
	public ColorState getLastColor();

	/**
	 * Returns the previous current color code.
	 * @param newColor the color to change it to
	 */
	public void setLastColor(final ColorState newColor);

	/**
	 * Gets the column number for engine word-wrapping.
	 * 0 Means disabled.
	 *
	 * @return the wrap column
	 */
	public int getWrap();

	/**
	 * Returns true if the given tag of the given category will be accepted by the client.
	 * Send a null tag to check general support
	 * @see Formatting#setAllowed(FCode, String, boolean)
	 *
	 * @param code the formatting code the given tag belongs to
	 * @param tag the tag to check, or null to check general support
	 * @return true if allowed, false otherwise
	 */
	public boolean isAllowed(FCode code, String tag);


	/**
	 * Returns true if the given max tag will be accepted by the client.
	 * @see Formatting#isAllowed(FCode, String)
	 *
	 * @param code the formatting code the given tag belongs to
	 * @param tag the tag to check, or null to check general support
	 * @param tf true if allowed/added, false if not allowed/removed
	 */
	public void setAllowed(FCode code, String tag, boolean tf);
}
