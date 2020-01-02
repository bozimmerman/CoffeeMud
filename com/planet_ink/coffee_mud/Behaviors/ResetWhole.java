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
   Copyright 2003-2020 Bo Zimmerman

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

	protected long		lastAccess	= -1;
	protected long		time		= 1800000;
	protected int		rlHour		= -1;
	protected int		mudHour		= -1;
	protected boolean	resetDone	= false;

	@Override
	public String accountForYourself()
	{
		return "periodic resetting";
	}

	@Override
	public void setParms(final String parameters)
	{
		super.setParms(parameters);
		try
		{
			time = 30000;
			rlHour = -1;
			mudHour= -1;
			resetDone = false;
			if(CMath.isNumber(parameters))
			{
				time=Long.parseLong(parameters);
				time=time*CMProps.getTickMillis();
			}
			else
			{
				String s = CMParms.getParmStr(parameters, "TICKS", "");
				if(CMath.isNumber(s))
				{
					time=CMath.s_long(s);
					time=time*CMProps.getTickMillis();
				}
				else
				{
					s = CMParms.getParmStr(parameters, "RLHOUR", "");
					if(CMath.isNumber(s))
						this.rlHour = CMath.s_int(s);
					else
					{
						s = CMParms.getParmStr(parameters, "MUDHOUR", "");
						if(CMath.isNumber(s))
							this.mudHour=CMath.s_int(s);
					}
				}

			}
		}
		catch(final Exception e)
		{
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(!msg.source().isMonster())
		{
			final Room R=msg.source().location();
			if(R!=null)
			{
				if((host instanceof Area)
				&&(((Area)host).inMyMetroArea(R.getArea())))
					lastAccess=System.currentTimeMillis();
				else
				if((host instanceof Room) &&(R==host))
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
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);
		if(lastAccess<0)
			return true;

		if((lastAccess+time)<System.currentTimeMillis())
		{
			if(this.rlHour>=0)
			{
				lastAccess = System.currentTimeMillis() + TimeManager.MILI_HOUR;
				final Calendar C=Calendar.getInstance();
				if(C.get(Calendar.HOUR_OF_DAY) != rlHour)
				{
					resetDone = false;
					lastAccess = System.currentTimeMillis() + (TimeManager.MILI_HOUR/5);
					return true;
				}
				else
				if(resetDone)
					return true;
			}
			if(this.mudHour>=0)
			{
				Physical E;
				if(ticking instanceof Physical)
					E=(Physical)ticking;
				else
					E=super.getBehaversRoom(ticking);
				lastAccess = System.currentTimeMillis() + CMProps.getMillisPerMudHour();
				if(E==null)
					return true;
				final TimeClock C=CMLib.time().localClock(E);
				if(C==null)
					return true;
				if(C.getHourOfDay() != mudHour)
				{
					resetDone = false;
					lastAccess = System.currentTimeMillis() + (CMProps.getMillisPerMudHour()/5);
					return true;
				}
				else
				if(resetDone)
					return true;
			}

			if(ticking instanceof Area)
			{
				for(final Enumeration<Room> r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					Room R=r.nextElement();
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
						resetDone = true;
						CMLib.map().resetRoom(R, true);
					}
				}
			}
			else
			if(ticking instanceof Room)
			{
				if(!this.isRoomBeingCamped((Room)ticking))
				{
					resetDone = true;
					CMLib.map().resetRoom((Room)ticking, true);
				}
			}
			else
			{
				final Room room=super.getBehaversRoom(ticking);
				if((room!=null) && (!this.isRoomBeingCamped(room)))
				{
					resetDone = true;
					CMLib.map().resetRoom(room, true);
				}
			}
			if(time > 0)
				lastAccess=System.currentTimeMillis();
		}
		return true;
	}
}
