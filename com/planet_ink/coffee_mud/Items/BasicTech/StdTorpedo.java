package com.planet_ink.coffee_mud.Items.BasicTech;

import com.planet_ink.coffee_mud.Items.interfaces.Ammunition;
import com.planet_ink.coffee_mud.Items.interfaces.Weapon;
import com.planet_ink.coffee_mud.core.CMLib;
import com.planet_ink.coffee_mud.core.CMProps;
import com.planet_ink.coffee_mud.core.CMath;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.core.collections.Pair;
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
public class StdTorpedo extends StdSpaceTechWeapon implements Ammunition
{
	@Override
	public String ID()
	{
		return "StdTorpedo";
	}

	protected final static long maxChaseTimeMs = 300000;

	public StdTorpedo()
	{
		super();
		setName("a torpedo");
		setDisplayText("a torpedo is sitting here");
		super.properWornBitmap=0;
		super.weaponClass=Weapon.CLASS_BLUNT;
		super.weaponType=Weapon.TYPE_BURNING;
		super.basePhyStats.setDamage(100);
		super.phyStats.setDamage(100);
		super.basePhyStats.setWeight(1000);
		super.phyStats.setWeight(1000);
		super.basePhyStats.setSpeed(0.3);
		super.phyStats.setSpeed(0.3);
	}

	protected boolean isInSpace()
	{
		final SpaceObject O=CMLib.space().getSpaceObject(this, true);
		if(O != null)//&&(this.powerRemaining() > this.powerNeeds()))
			return CMLib.space().isObjectInSpace(O);
		return false;
	}

	protected volatile Long timeTicking = null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((ticking == this)
		&& (tickID == Tickable.TICKID_BALLISTICK))
		{
			if(!isInSpace())
				return true;
			if((timeTicking == null)||(speed()==0))
			{
				timeTicking = Long.valueOf(System.currentTimeMillis());
				// first out of the toob is well directioned
				final SpaceObject targetO=knownTarget();
				if(targetO != null)
				{
					final int maxTicks = (int)(maxChaseTimeMs/CMProps.getTickMillis());
					final double maxSpeed = CMath.mul((phyStats().speed()/100.0), SpaceObject.VELOCITY_LIGHT);
					final Pair<double[], Long> intercept = CMLib.space().calculateIntercept(this, targetO, Math.round(maxSpeed), maxTicks);
					if(intercept != null)
					{
						//TODO:delme
						Log.debugOut("Setting "+name()+" to speed "+intercept.second.longValue()+" in dir " +CMLib.english().directionDescShort(intercept.first)+" to target "+CMLib.english().directionDescShort(CMLib.space().getDirection(this, targetO)));
						Log.debugOut(name()+" is "+CMLib.space().getDistanceFrom(this, targetO)+" away now.");
						this.setSpeed(intercept.second.longValue());
						this.setDirection(intercept.first);
					}
				}
			}
			final long diff = System.currentTimeMillis()-timeTicking.longValue();
			if(diff > maxChaseTimeMs) // 5 minutes of live sounds good
			{
				this.destroy();
				return false;
			}
			return true;
		}
		// do not let tickid_spaceweapon get here, ever
		if(!super.tick(ticking, tickID))
			return false;
		return true;
	}

	@Override
	public String ammunitionType()
	{
		return "Torpedo";
	}

	@Override
	public void setAmmunitionType(final String type)
	{
	}

	@Override
	public int ammunitionRemaining()
	{
		return 1;
	}

	@Override
	public void setAmmoRemaining(final int amount)
	{
	}
}
