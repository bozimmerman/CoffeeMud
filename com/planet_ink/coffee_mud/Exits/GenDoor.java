package com.planet_ink.coffee_mud.Exits;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class GenDoor extends GenExit
{
	public String ID(){	return "GenDoor";}
	public GenDoor()
	{
		super();
		name="a door";
		displayText="";
		description="An ordinary wooden door with hinges and a latch.";
		hasADoor=true;
		hasALock=false;
		doorDefaultsClosed=true;
		doorDefaultsLocked=false;
		closedText="a closed door";
		doorName="door";
		closeName="close";
		openName="open";
	}
}
