package com.planet_ink.coffee_mud.core.intermud.i3.net;
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
/**
 * com.planet_ink.coffee_mud.core.intermud.i3.net.InvalidNameException
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * An exception thrown for invalid user names.
 */

/**
 * This class is thrown whenever an attempt to create
 * a bad user name is made.
 * Created: 28 September 1996
 * Last modified: 28 September 1996
 * @author George Reese (borg@imaginary.com)
 * @version 1.0
 */
public class InvalidNameException extends Exception
{
	public static final long serialVersionUID=0;
	/**
	 * Constructs a new invalid name exception with
	 * the specified reason.
	 * @param reason the reason for the exception
	 */
	public InvalidNameException(String reason)
	{
		super(reason);
	}
}
