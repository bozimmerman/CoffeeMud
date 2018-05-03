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
public class ResourceOverride extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "ResourceOverride";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ROOMS | Behavior.CAN_AREAS;
	}

	private final List<Integer>	rscs		= new Vector<Integer>();
	private final Set<Integer>	roomTypes	= new TreeSet<Integer>();

	@Override
	public String accountForYourself()
	{
		return "resource overriding";
	}

	@Override
	public void setParms(String newStr)
	{
		super.setParms(newStr);
		super.tickDown=1;
		rscs.clear();
		roomTypes.clear();
		final Vector<String> V=CMParms.parse(getParms());
		if(V.size()==0)
			return;
		for(int v=0;v<V.size();v++)
		{
			// first try for a real one
			int code=-1;
			final String which=V.elementAt(v).toUpperCase().trim();
			if(CMath.isInteger(which))
				code=CMath.s_int(which);
			if(code<0)
				code = RawMaterial.CODES.FIND_IgnoreCase(which);
			if(code<0)
			{
				final RawMaterial.Material m=RawMaterial.Material.findIgnoreCase(which);
				if(m!=null)
					code=RawMaterial.CODES.COMPOSE_RESOURCES(m.mask()).get(0).intValue();
			}
			if(code<0)
				code = RawMaterial.CODES.FIND_StartsWith(which);
			if(code<0)
			{
				final RawMaterial.Material m=RawMaterial.Material.startsWith(which);
				if(m!=null)
					code=RawMaterial.CODES.COMPOSE_RESOURCES(m.mask()).get(0).intValue();
			}
			if(code>=0)
			{
				if(!rscs.contains(Integer.valueOf(code)))
					rscs.add(Integer.valueOf(code));
			}
			else
			{
				for(int i=0;i<Room.DOMAIN_OUTDOOR_DESCS.length;i++)
				{
					if(which.equalsIgnoreCase(Room.DOMAIN_OUTDOOR_DESCS[i]))
					{
						code = i;
						break;
					}
				}
				if(code<0)
				{
					for(int i=0;i<Room.DOMAIN_INDOORS_DESCS.length;i++)
					{
						if(which.equalsIgnoreCase(Room.DOMAIN_INDOORS_DESCS[i]))
						{
							code = Room.INDOORS | i;
							break;
						}
					}
				}
				if(code>=0)
					roomTypes.add(Integer.valueOf(code));
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(rscs.size()==0)
			return true;
		if(super.canAct(ticking, tickID))
		{
			switch(tickID)
			{
			case Tickable.TICKID_ROOM_BEHAVIOR:
				if(ticking instanceof Room)
				{
					final Room R=(Room)ticking;
					if(!rscs.contains(Integer.valueOf(R.myResource())))
						R.setResource(rscs.get(CMLib.dice().roll(1,rscs.size(),-1)).intValue());
				}
				break;
			case Tickable.TICKID_AREA:
				if(ticking instanceof Area)
				{
					final Area A=(Area)ticking;
					Room R=null;
					for(final Enumeration<Room> e=A.getMetroMap();e.hasMoreElements();)
					{
						R=e.nextElement();
						if((R!=null)
						&&((roomTypes.size()==0)||(roomTypes.contains(Integer.valueOf(R.domainType()))))
						&&(!rscs.contains(Integer.valueOf(R.myResource()))))
							R.setResource(rscs.get(CMLib.dice().roll(1,rscs.size(),-1)).intValue());
					}
				}
			}
		}
		return true;
	}
}
