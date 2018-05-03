package com.planet_ink.coffee_mud.Behaviors;
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

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class ResetWhole extends StdBehavior
{
	@Override
	public String ID()
	{
		return "ResetWhole";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS|Behavior.CAN_AREAS;
	}

	protected long lastAccess=-1;
	long time=1800000;

	@Override
	public String accountForYourself()
	{
		return "periodic resetting";
	}

	@Override
	public void setParms(String parameters)
	{
		super.setParms(parameters);
		try
		{
			time=Long.parseLong(parameters);
			time=time*CMProps.getTickMillis();
		}
		catch(final Exception e)
		{
		}
	}
	
	@Override
	public void executeMsg(Environmental E, CMMsg msg)
	{
		super.executeMsg(E,msg);
		if(!msg.source().isMonster())
		{
			final Room R=msg.source().location();
			if(R!=null)
			{
				if((E instanceof Area)
				&&(((Area)E).inMyMetroArea(R.getArea())))
					lastAccess=System.currentTimeMillis();
				else
				if((E instanceof Room) &&(R==E))
					lastAccess=System.currentTimeMillis();
			}
		}
	}

	private boolean isRoomBeingCamped(final Room R)
	{
		if(CMLib.flags().canNotBeCamped(R)
		&& (R.numPCInhabitants() > 0) 
		&& (!CMLib.tracking().isAnAdminHere(R,false)))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(lastAccess<0)
			return true;

		if((lastAccess+time)<System.currentTimeMillis())
		{
			if(ticking instanceof Area)
			{
				for(final Enumeration r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					for(final Enumeration<Behavior> e=R.behaviors();e.hasMoreElements();)
					{
						final Behavior B=e.nextElement();
						if((B!=null)&&(B.ID().equals(ID())))
						{
							R=null;
							break;
						}
					}
					if((R!=null)&&(!this.isRoomBeingCamped(R)))
					{
						CMLib.map().resetRoom(R, true);
					}
				}
			}
			else
			if(ticking instanceof Room)
			{
				if(!this.isRoomBeingCamped((Room)ticking))
					CMLib.map().resetRoom((Room)ticking, true);
			}
			else
			{
				final Room room=super.getBehaversRoom(ticking);
				if((room!=null) && (!this.isRoomBeingCamped(room)))
					CMLib.map().resetRoom(room, true);
			}
			lastAccess=System.currentTimeMillis();
		}
		return true;
	}
}
