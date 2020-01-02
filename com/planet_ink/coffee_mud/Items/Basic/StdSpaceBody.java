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
import com.planet_ink.coffee_mud.Items.interfaces.TechComponent.ShipDir;
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechCommand;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2020 Bo Zimmerman

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

	protected long[]		coordinates	= new long[3];
	protected long			radius;
	protected double[]		direction	= new double[2];
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

	public void destroy()
	{
		CMLib.map().delObjectInSpace(this);
		super.destroy();
	}

	@Override
	public BoundedCube getBounds()
	{
		return new BoundedObject.BoundedCube(coordinates(),radius());
	}

	@Override
	public long[] coordinates()
	{
		return coordinates;
	}

	@Override
	public void setCoords(final long[] coords)
	{
		if((coords!=null)&&(coords.length==3))
			CMLib.map().moveSpaceObject(this,coords);
	}

	@Override
	public long radius()
	{
		return radius;
	}

	@Override
	public void setRadius(final long radius)
	{
		this.radius=radius;
	}

	@Override
	public double[] direction()
	{
		return direction;
	}

	@Override
	public void setDirection(final double[] dir)
	{
		if((dir!=null)&&(dir.length==2))
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
					// not sure
				}
				break;
			}
			default:
				break;
			}
		}
	}
}
