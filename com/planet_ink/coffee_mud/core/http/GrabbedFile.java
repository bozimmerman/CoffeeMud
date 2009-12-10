package com.planet_ink.coffee_mud.core.http;
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


// simple wrapper for returned file from FileGrabber
//  contains a File and a state integer
// yes yes should be using wrapper functions, but it's
//  so simple I don't see the point

// if state != OK, file is not guaranteed to be non-null

/* 
   Copyright 2000-2010 Bo Zimmerman

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
public class GrabbedFile
{
	public CMFile file;
	public int state;
	
	public static final int STATE_OK = 0;
	public static final int STATE_IS_DIRECTORY = 1;	// file will be valid - not an error
	public static final int STATE_BAD_FILENAME = 2;
	public static final int STATE_NOT_FOUND = 3;
	public static final int STATE_INTERNAL_ERROR = 4;
	public static final int STATE_SECURITY_VIOLATION = 5;
	
}
