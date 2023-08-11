package com.planet_ink.coffee_mud.Items.BasicTech;

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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.math.BigDecimal;
import java.util.*;

/*
Copyright 2016-2023 Bo Zimmerman

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

	public volatile double[] targetDir = null;
	public volatile int rescanCtr = 0;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((ticking == this)
		&& (tickID == Tickable.TICKID_BALLISTICK))
		{
			final SpaceObject targetO=knownTarget();
			if((targetO != null)
			&& (isInSpace())
			&&(super.timeTicking != null))
			{
				final GalacticMap space=CMLib.space();
				final int maxTicks = (int)((maxChaseTimeMs-(System.currentTimeMillis()-super.timeTicking.longValue()))/CMProps.getTickMillis());
				if((targetDir==null)
				||(speed()==0)
				||((++rescanCtr>2) && (space.getMinDistanceFrom(this.coordinates,
						space.moveSpaceObject(this.coordinates, direction(), space.getDistanceFrom(this, targetO)+Math.round(speed())), targetO.coordinates())>radius()+targetO.radius())))
				{
					rescanCtr=0;
					final double maxSpeed = CMath.mul((phyStats().speed()/100.0), SpaceObject.VELOCITY_LIGHT);
					final Pair<double[], Long> intercept = space.calculateIntercept(this, targetO, Math.round(maxSpeed), maxTicks);
					if(intercept == null)
					{
						targetDir = this.direction;
						if(speed()<maxSpeed)
							this.setSpeed(space.accelSpaceObject(direction, this.speed(), targetDir, maxSpeed/8.0)); //TODO: acceleration!
					}
					else
					{
						this.targetDir = intercept.first;
						if(speed()<intercept.second.longValue())
							this.setSpeed(space.accelSpaceObject(direction, this.speed(), targetDir, maxSpeed/8.0)); //TODO: acceleration!
						else
							this.setSpeed(intercept.second.longValue());
					}
				}
				final double diffDelta = space.getAngleDelta(direction(), targetDir);
				if(diffDelta>.0001)
				{
					double accel=this.speed()/8.0; // try to turn by accel 1/8 current speed
					if(accel < 1)
						accel = 1;
					this.setSpeed(space.accelSpaceObject(direction, speed(), targetDir, accel));
				}
			}
			return true;
		}
		return true;
	}
}
