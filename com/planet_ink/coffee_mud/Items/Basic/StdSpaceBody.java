package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMSecurity.DbgFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.SpaceShip.ShipFlag;
import com.planet_ink.coffee_mud.Items.interfaces.ShipDirectional.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2024 Bo Zimmerman

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
public class StdSpaceBody extends StdItem implements SpaceObject
{
	@Override
	public String ID()
	{
		return "StdSpaceBody";
	}

	protected Coord3D		coordinates	= new Coord3D();
	protected long			radius;
	protected Dir3D			direction	= new Dir3D();
	protected double		speed		= 0;
	protected SpaceObject	spaceSource = null;
	protected SpaceObject	spaceTarget = null;

	public StdSpaceBody()
	{
		super();
		setName("a thing in space");
		setDisplayText("a thing is floating in space");
		final Random random=new Random(System.currentTimeMillis());
		radius=SpaceObject.Distance.Kilometer.dm + (random.nextLong() % (SpaceObject.Distance.Kilometer.dm / 2));
		basePhyStats().setWeight(100);
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STONE);
	}

	@Override
	public String genericName()
	{
		if(radius >= SpaceObject.Distance.StarBRadius.dm/2)
			return L("a B-type star");
		else
		if(radius >= SpaceObject.Distance.StarGRadius.dm/2)
			return L("a G-type star");
		else
		if(radius >= SpaceObject.Distance.StarDRadius.dm)
			return L("a D-type star");
		else
		if(radius >= SpaceObject.Distance.SaturnRadius.dm)
			return L("an enormous planet");
		else
		if(radius >= SpaceObject.Distance.SaturnRadius.dm/2)
			return L("a huge planet");
		else
		if(radius >= SpaceObject.Distance.PlanetRadius.dm*2)
			return L("an large planet");
		else
		if(radius >= SpaceObject.Distance.PlanetRadius.dm/2)
			return L("a planet");
		else
		if(radius >= SpaceObject.Distance.MoonRadius.dm/2)
			return L("a moon");
		else
		if(radius >= SpaceObject.Distance.AsteroidRadius.dm)
			return L("a moonlet");
		else
			return L("an asteroid");
	}

	@Override
	public void destroy()
	{
		CMLib.space().delObjectInSpace(this);
		super.destroy();
	}

	@Override
	public CMObject copyOf()
	{
		final StdSpaceBody E=(StdSpaceBody)super.copyOf();
		E.coordinates = coordinates.copyOf();
		E.direction = direction.copyOf();
		return E;
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

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DAMAGE:
			{
				final double myMass=getMass();
				final double hardness = RawMaterial.CODES.HARDNESS(material()) * SpaceObject.Distance.Kilometer.dm;
				msg.setValue((int)Math.round((usesRemaining() * (msg.value() / myMass)) / hardness));
				return true; // avoid the stditem damage to item code
			}
			}
		}
		return super.okMessage(myHost, msg);
	}

	protected boolean isTechWeapon(final Environmental E)
	{
		if((E instanceof SpaceObject) && (E instanceof Weapon))
			return true;
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);

		if(msg.amITarget(this) && (!amDestroyed()))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DAMAGE: // kinetic damage taken to the body by a weapon
			case CMMsg.TYP_COLLISION:
			{
				final long myMass=getMass();
				if((msg.value() > 0)&&(myMass>0))
				{
					if(!isTechWeapon(msg.tool()))
					{
						final SpaceObject srcP;
						if((msg.tool() == this) && (msg.target() instanceof SpaceObject))
						{
							srcP=(SpaceObject)msg.target();
							CMLib.space().sendSpaceEmissionEvent(srcP, this, CMMsg.TYP_COLLISION|CMMsg.MASK_MOVE|CMMsg.MASK_EYES,
																L("<S-NAME> is hit by <O-NAME>"));
						}
					}
				}
				break;
			}
			default:
				break;
			}
		}
	}
}
