package com.planet_ink.coffee_mud.Items.BasicTech;

import com.planet_ink.coffee_mud.Items.interfaces.Weapon;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.interfaces.SpaceObject;
import com.planet_ink.coffee_mud.core.interfaces.Tickable;

/*
Copyright 2016-2022 Bo Zimmerman

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
public class StdSmartTorpedo extends StdTorpedo
{
	@Override
	public String ID()
	{
		return "StdSmartTorpedo";
	}

	public StdSmartTorpedo()
	{
		super();
		setName("a smart torpedo");
		setDisplayText("a smart torpedo is sitting here");
	}

	protected boolean isInSpace()
	{
		final SpaceObject O=CMLib.space().getSpaceObject(this, true);
		if(O != null)//&&(this.powerRemaining() > this.powerNeeds()))
			return CMLib.space().isObjectInSpace(O);
		return false;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((ticking == this)
		&& (tickID == Tickable.TICKID_BALLISTICK))
		{
			if((knownTarget() != null)
			&& (isInSpace()))
			{
				final double[] dirTo = CMLib.space().getDirection(this, knownTarget());
				final double[] diffDelta = CMLib.space().getFacingAngleDiff(direction(), dirTo); // starboard is -, port is +
				if((Math.abs(diffDelta[0])+Math.abs(diffDelta[1]))>.02)
				{
					double speed=this.speed()/2.0;
					if(speed < 1)
						speed = 1;
					speed = CMLib.space().accelSpaceObject(dirTo, this.speed(), dirTo, speed);
				}
			}
			return true;
		}
		return true;
	}
}
