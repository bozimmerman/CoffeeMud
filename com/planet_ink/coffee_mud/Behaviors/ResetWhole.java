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
public class ResetWhole extends StdBehavior
{
	public String ID(){return "ResetWhole";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;}

	protected long lastAccess=-1;



	public void executeMsg(Environmental E, CMMsg msg)
	{
		super.executeMsg(E,msg);
		if(!msg.source().isMonster())
		{
			if((E instanceof Area)
			&&(((Area)E).inMetroArea(msg.source().location().getArea())))
				lastAccess=System.currentTimeMillis();
			else
			if((E instanceof Room)
			&&(msg.source().location()==E))
				lastAccess=System.currentTimeMillis();
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(lastAccess<0) return true;

		long time=(long)1800000;
		try
		{
			time=Long.parseLong(getParms());
			time=time*MudHost.TICK_TIME;
		}
		catch(Exception e){}
		if((lastAccess+time)<System.currentTimeMillis())
		{
			if(ticking instanceof Area)
			{
				for(Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					for(int b=0;b<R.numBehaviors();b++)
					{
						Behavior B=R.fetchBehavior(b);
						if((B!=null)&&(B.ID().equals(ID())))
						{ R=null; break;}
					}
					if(R!=null)	CoffeeUtensils.resetRoom(R);
				}
			}
			else
			if(ticking instanceof Room)
				CoffeeUtensils.resetRoom((Room)ticking);
			else
			{
				Room room=super.getBehaversRoom(ticking);
				if(room!=null)
					CoffeeUtensils.resetRoom(room);
			}
			lastAccess=System.currentTimeMillis();
		}
		return true;
	}
}
