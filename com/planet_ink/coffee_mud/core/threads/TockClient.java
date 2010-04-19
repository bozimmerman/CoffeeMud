package com.planet_ink.coffee_mud.core.threads;
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
public class TockClient implements Comparable<TockClient>
{
	public final Tickable clientObject;
	public final int tickID;
	public final int reTickDown;
	public int tickDown=0;
	public boolean suspended=false;
	public long lastStart=0;
	public long lastStop=0;
	public long milliTotal=0;
	public long tickTotal=0;

	public TockClient(Tickable newClientObject,
					  int newTickDown,
					  int newTickID)
	{
		reTickDown=newTickDown;
		tickDown=newTickDown;
		clientObject=newClientObject;
		tickID=newTickID;
	}

	public boolean equals(Object obj)
	{
		if(obj instanceof TockClient)
			return compareTo((TockClient)obj)==0;
		return false;
	}
	
	public int compareTo(TockClient arg0) 
	{
		long hc0=(clientObject==null)?0:clientObject.hashCode();
		long hc1=(arg0.clientObject==null)?0:arg0.clientObject.hashCode();
		if(hc0>hc1) return 1;
		if(hc0<hc1) return -1;
		if(tickID>arg0.tickID) return 1;
		if(tickID<arg0.tickID) return -1;
		return 0;
	}
}
