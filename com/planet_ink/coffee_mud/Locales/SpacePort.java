package com.planet_ink.coffee_mud.Locales;
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
   Copyright 2004-2024 Bo Zimmerman

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
public class SpacePort extends StdRoom implements LocationRoom
{
	@Override
	public String ID()
	{
		return "SpacePort";
	}

	protected Dir3D dirFromCore = new Dir3D();

	public SpacePort()
	{
		super();
		name="the space port";
		setMovementCost(1);
		recoverPhyStats();
	}

	@Override
	public int domainType()
	{
		return Room.DOMAIN_OUTDOORS_SPACEPORT;
	}

	@Override
	public Coord3D coordinates()
	{
		final SpaceObject planet=CMLib.space().getSpaceObject(this,true);
		if(planet!=null)
			return CMLib.space().getLocation(planet.coordinates(),dirFromCore,planet.radius());
		return new Coord3D();
	}

	@Override
	public Dir3D getDirectionFromCore()
	{
		return dirFromCore;
	}

	@Override
	public void setDirectionFromCore(final Dir3D dir)
	{
		if((dir!=null)&&(dir.length()==2))
			dirFromCore=dir;
	}

	private final static String[] MYCODES={"COREDIR"};
	@Override
	public String getStat(final String code)
	{
		switch(getLocCodeNum(code))
		{
		case 0:
			return CMParms.toListString(this.getDirectionFromCore().toDoubles());
		default:
			return super.getStat(code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		switch(getLocCodeNum(code))
		{
		case 0:
			this.setDirectionFromCore(new Dir3D(CMParms.toDoubleArray(CMParms.parseCommas(val, true))));
			break;
		default:
			super.setStat(code, val);
			break;
		}
	}

	protected int getLocCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		return (codes != null) ? codes : (codes =  CMProps.getStatCodesList(CMParms.appendToArray(super.getStatCodes(), MYCODES),this));
	}
}
