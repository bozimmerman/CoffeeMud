package com.planet_ink.coffee_mud.Behaviors;

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
public class GetsAllEquipped extends ActiveTicker
{
	public String ID(){return "GetsAllEquipped";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	public GetsAllEquipped()
	{
		maxTicks=5;minTicks=10;chance=100;
		tickReset();
	}

	private boolean DoneEquipping=false;



	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if((canAct(ticking,tickID))&&(ticking instanceof MOB))
		{
			if(DoneEquipping)
				return true;

			MOB mob=(MOB)ticking;
			Room thisRoom=mob.location();
			if(thisRoom.numItems()==0) return true;

			DoneEquipping=true;
			Vector V=new Vector();
			V.addElement("GET");
			V.addElement("ALL");
			Vector V1=new Vector();
			V1.addElement("WEAR");
			V1.addElement("ALL");
			mob.doCommand(V);
			mob.doCommand(V1);
		}
		return true;
	}
}
