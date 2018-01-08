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
 * A Drinkable object containing its own liquid material type, and liquid capacity management.
 * @author Bo Zimmerman
 *
 */
public interface Modifiable
{
	/**
	 * Returns an array of the string names of those fields which are modifiable on this object at run-time by
	 * builders.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#getStat(String)
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#setStat(String, String)
	 * @return list of the fields which may be set.
	 */
	public String[] getStatCodes();

	/**
	 * Returns the index into the stat codes array where extra savable fields begins.
	 * This number is always the same as getStatCodes().length unless there are extra
	 * fields which need to be saved in xml for generic objects.  This method is used
	 * by editors for post-build user-defined fields.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#getStatCodes()
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#getStat(String)
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#setStat(String, String)
	 * @return the index into getStatCodes()
	 */
	public int getSaveStatIndex();

	/**
	 * An alternative means of retreiving the values of those fields on this object which are modifiable at
	 * run-time by builders.  See getStatCodes() for possible values for the code passed to this method.
	 * Values returned are always strings, even if the field itself is numeric or a list.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#getStatCodes()
	 * @param code the name of the field to read.
	 * @return the value of the field read
	 */
	public String getStat(String code);
	/**
	 * An alternative means of retreiving the values of those fields on this object which are modifiable at
	 * run-time by builders.  See getStatCodes() for possible values for the code passed to this method.
	 * Values returned are always strings, even if the field itself is numeric or a list.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#getStatCodes()
	 * @param code the name of the field to read.
	 * @return true if the code is a real value, false otherwise
	 */
	public boolean isStat(String code);
	/**
	 * An alternative means of setting the values of those fields on this object which are modifiable at
	 * run-time by builders.  See getStatCodes() for possible values for the code passed to this method.
	 * The value passed in is always a string, even if the field itself is numeric or a list.
	 * @see com.planet_ink.coffee_mud.core.interfaces.Modifiable#getStatCodes()
	 * @param code the name of the field to set
	 * @param val the value to set the field to
	 */
	public void setStat(String code, String val);
}
