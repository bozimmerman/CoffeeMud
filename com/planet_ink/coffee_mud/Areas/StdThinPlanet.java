package com.planet_ink.coffee_mud.Areas;
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
   Copyright 2013-2024 Bo Zimmerman

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

	protected Coord3D	coordinates	= new Coord3D();
	protected Dir3D		direction	= new Dir3D();
	protected long		radius;

	public StdThinPlanet()
	{
		super();

		myClock = (TimeClock)CMClass.getCommon("DefaultTimeClock");
		coordinates=new Coord3D(new long[]{Math.round(Long.MAX_VALUE*Math.random()),Math.round(Long.MAX_VALUE*Math.random()),Math.round(Long.MAX_VALUE*Math.random())});
		final Random random=new Random(System.currentTimeMillis());
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
	public void addChild(final Area area)
	{
		super.addChild(area);
		area.setTimeObj(getTimeObj());
		for(final Enumeration<Area> cA=area.getChildren();cA.hasMoreElements();)
			cA.nextElement().setTimeObj(getTimeObj());
	}

	@Override
	public Coord3D coordinates()
	{
		return coordinates;
	}

	@Override
	public void setCoords(final Coord3D coords)
	{
		if((coords!=null)&&(coords.length()==3))
			CMLib.space().moveSpaceObject(this,coords);
	}

	@Override
	public Dir3D direction()
	{
		return direction;
	}

	@Override
	public void setDirection(final Dir3D dir)
	{
		direction = dir;
	}

	@Override
	public String genericName()
	{
		if(radius >= SpaceObject.Distance.SaturnRadius.dm)
			return L("a large planet");
		else
		if(radius <= SpaceObject.Distance.MoonRadius.dm)
			return L("a tiny planet");
		else
		if(radius < SpaceObject.Distance.PlanetRadius.dm/2)
			return L("a small planet");
		else
			return L("a planet");
	}

	@Override
	public double speed()
	{
		return 0;
	}

	@Override
	public void setSpeed(final double v)
	{
	}

	@Override
	public long radius()
	{
		return radius;
	}

	@Override
	public Coord3D center()
	{
		return coordinates();
	}

	@Override
	public void setRadius(final long radius)
	{
		this.radius = radius;
	}

	@Override
	public void setName(final String newName)
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
	public void setKnownTarget(final SpaceObject O)
	{
	}

	@Override
	public SpaceObject knownSource()
	{
		return null;
	}

	@Override
	public void setKnownSource(final SpaceObject O)
	{
	}

	@Override
	public BoundedCube getCube()
	{
		return new BoundedCube(coordinates(),radius());
	}

	@Override
	public BoundedSphere getSphere()
	{
		return new BoundedSphere(coordinates(),radius());
	}
}
