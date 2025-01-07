package com.planet_ink.coffee_mud.Items.BasicTech;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
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
   Copyright 2016-2024 Bo Zimmerman

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
public class StdSpaceTech extends StdTechItem implements SpaceObject
{
	@Override
	public String ID()
	{
		return "StdSpaceTech";
	}

	protected Coord3D		coordinates	= new Coord3D();
	protected long			radius;
	protected Dir3D			direction	= new Dir3D();
	protected double		speed		= 0;
	protected SpaceObject	spaceSource = null;
	protected SpaceObject	spaceTarget = null;

	public StdSpaceTech()
	{
		super();
		setName("a techy thing in space");
		setDisplayText("a techy thing is floating in space");
		final Random random=new Random(System.currentTimeMillis());
		radius=SpaceObject.Distance.Kilometer.dm + (random.nextLong() % (SpaceObject.Distance.Kilometer.dm / 2));
		basePhyStats().setWeight(1);
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
	}

	@Override
	public void destroy()
	{
		CMLib.space().delObjectInSpace(this);
		super.destroy();
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
		this.radius=radius;
	}

	@Override
	public Dir3D direction()
	{
		return direction;
	}

	@Override
	public void setDirection(final Dir3D dir)
	{
		if((dir!=null)&&(dir.length()==2))
			direction=dir;
	}

	@Override
	public double speed()
	{
		return speed;
	}

	@Override
	public void setSpeed(final double v)
	{
		speed=v;
	}

	@Override
	public SpaceObject knownTarget()
	{
		return spaceTarget;
	}

	@Override
	public void setKnownTarget(final SpaceObject O)
	{
		spaceTarget=O;
	}

	@Override
	public SpaceObject knownSource()
	{
		return spaceSource;
	}

	@Override
	public void setKnownSource(final SpaceObject O)
	{
		spaceSource=O;
	}

	@Override
	public long getMass()
	{
		return basePhyStats().weight() * radius();
	}
}
