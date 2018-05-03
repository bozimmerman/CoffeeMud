package com.planet_ink.coffee_mud.Items.CompTech;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.GenericBuilder;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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
public class GenSolarGenerator extends GenFuellessGenerator
{
	@Override
	public String ID()
	{
		return "GenSolarGenerator";
	}

	public GenSolarGenerator()
	{
		super();
		setName("a solar generator");
		setDisplayText("a solar generator sits here.");
		setDescription("");
	}

	@Override
	protected boolean canGenerateRightNow()
	{
		if(activated())
		{
			final Area A=CMLib.map().areaLocation(this);
			if(A instanceof SpaceShip)
			{
				final Room dockRoom=((SpaceShip)A).getIsDocked();
				if(dockRoom!=null) 
					return (dockRoom.getArea()!=null) && (dockRoom.getArea().getClimateObj().canSeeTheSun(dockRoom));
				final SpaceObject obj = ((SpaceShip)A).getShipSpaceObject();
				final List<SpaceObject> objs = CMLib.map().getSpaceObjectsWithin(obj, obj.radius(), SpaceObject.Distance.SolarSystemDiameter.dm);
				for(final SpaceObject o : objs)
				{
					if((o instanceof Physical)
					&&(!CMLib.flags().isLightSource((Physical)o)))
						continue;
					if(o.radius() >= (SpaceObject.Distance.StarDRadius.dm / 2)
					&&(o.getMass() >= (o.radius() * SpaceObject.MULTIPLIER_STAR_MASS)))
						return true;
				}
			}
			else
			if((A!=null) && (A.getClimateObj().canSeeTheSun(CMLib.map().roomLocation(this))))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenSolarGenerator))
			return false;
		return super.sameAs(E);
	}
}
