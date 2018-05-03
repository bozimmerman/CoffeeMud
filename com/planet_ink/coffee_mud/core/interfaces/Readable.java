package com.planet_ink.coffee_mud.core.interfaces;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Behaviors.interfaces.Behavior;
import com.planet_ink.coffee_mud.Common.interfaces.PhyStats;

/*
   Copyright 2010-2018 Bo Zimmerman

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
*
* Something that can potentially be read, because it may or may not
* have writing on it.
*
* @see com.planet_ink.coffee_mud.core.interfaces.Physical
* @author Bo Zimmerman
*
*/
public interface Readable extends Physical
{

	/**
	 * For things that are readable, this returns the readable string
	 * for this thing.  That is to say, what the player sees when they
	 * read the door.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#isReadable()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#setReadable(boolean)
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#setReadableText(String)
	 * @return the readable string
	 */
	public String readableText();

	/**
	 * Returns whether this thing is readable when the player uses the READ command
	 * and targets it.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#readableText()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#setReadable(boolean)
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#setReadableText(String)
	 * @return true if the thing is readable.
	 */
	public boolean isReadable();

	/**
	 * Returns whether this thing is readable when the player uses the READ command
	 * and targets it.  Readable text should also be set or unset.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#readableText()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#isReadable()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#setReadableText(String)
	 * @param isTrue true if the thing is readable, and false otherwise
	 */
	public void setReadable(boolean isTrue);

	/**
	 * For things that are readable, this set the readable string
	 * for this thing.  That is to say, what the player sees when they
	 * read the door.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#isReadable()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#setReadable(boolean)
	 * @see com.planet_ink.coffee_mud.core.interfaces.Readable#readableText()
	 * @param text the readable text
	 */
	public void setReadableText(String text);

}
