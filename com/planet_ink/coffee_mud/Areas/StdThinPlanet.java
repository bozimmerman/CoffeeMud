package com.planet_ink.coffee_mud.Areas;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject;
import com.planet_ink.coffee_mud.core.interfaces.BoundedObject.BoundedCube;
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
   Copyright 2013-2018 Bo Zimmerman

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
public class StdThinPlanet extends StdThinArea implements SpaceObject
{
	@Override
	public String ID()
	{
		return "StdThinPlanet";
	}

	protected long[]	coordinates	= new long[3];
	protected double[]	direction	= new double[2];
	protected long		radius;

	public StdThinPlanet()
	{
		super();

		myClock = (TimeClock)CMClass.getCommon("DefaultTimeClock");
		coordinates=new long[]{Math.round(Long.MAX_VALUE*Math.random()),Math.round(Long.MAX_VALUE*Math.random()),Math.round(Long.MAX_VALUE*Math.random())};
		Random random=new Random(System.currentTimeMillis());
		radius=SpaceObject.Distance.PlanetRadius.dm + (random.nextLong() % (SpaceObject.Distance.PlanetRadius.dm / 20));
	}

	@Override
	public CMObject copyOf()
	{
		final CMObject O=super.copyOf();
		if(O instanceof Area)
			((Area)O).setTimeObj((TimeClock)CMClass.getCommon("DefaultTimeClock"));
		return O;
	}

	@Override
	public TimeClock getTimeObj()
	{
		return myClock;
	}

	@Override
	public long getMass()
	{
		return radius * MULTIPLIER_PLANET_MASS;
	}

	@Override
	public void addChild(Area area)
	{
		super.addChild(area);
		area.setTimeObj(getTimeObj());
		for(final Enumeration<Area> cA=area.getChildren();cA.hasMoreElements();)
			cA.nextElement().setTimeObj(getTimeObj());
	}

	@Override
	public long[] coordinates()
	{
		return coordinates;
	}

	@Override 
	public void setCoords(long[] coords)
	{
		if((coords!=null)&&(coords.length==3))
			CMLib.map().moveSpaceObject(this,coords);
	}

	@Override
	public double[] direction()
	{
		return direction;
	}

	@Override
	public void setDirection(double[] dir)
	{
		direction = dir;
	}

	@Override
	public double speed()
	{
		return 0;
	}

	@Override
	public void setSpeed(double v)
	{
	}

	@Override
	public long radius()
	{
		return radius;
	}

	@Override
	public void setRadius(long radius)
	{
		this.radius = radius;
	}

	@Override 
	public void setName(String newName)
	{
		super.setName(newName);
		myClock.setLoadName(newName);
	}

	@Override
	public SpaceObject knownTarget()
	{
		return null;
	}

	@Override
	public void setKnownTarget(SpaceObject O)
	{
	}

	@Override
	public SpaceObject knownSource()
	{
		return null;
	}

	@Override
	public void setKnownSource(SpaceObject O)
	{
	}

	@Override
	public BoundedCube getBounds()
	{
		return new BoundedObject.BoundedCube(coordinates(),radius());
	}
}
