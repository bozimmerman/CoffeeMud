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
import com.planet_ink.coffee_mud.Libraries.interfaces.LanguageLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

/*
   Copyright 2025-2026 Bo Zimmerman

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
 * Marks an object as holding tags.
 * @author Bo Zimmerman
 *
 */
public interface Taggable extends CMObject
{
	/**
	 * Adds a tag to this object.
	 *
	 * @param tag the tag to add
	 */
	public void addTag(String tag);

	/**
	 * Deletes a tag from this object.
	 *
	 * @param tag the tag to delete
	 */
	public void delTag(String tag);

	/**
	 * Gets an enumeration of all tags on this object.
	 *
	 * @return the enumeration of all tags
	 */
	public Enumeration<String> tags();

	/**
	 * Checks to see if this object has the given tag.
	 *
	 * @param tag the tag to look for
	 * @return true if the object has the tag, false otherwise
	 */
	public boolean hasTag(String tag);
}
