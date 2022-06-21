package com.planet_ink.coffee_mud.Items.BasicTech;

import com.planet_ink.coffee_mud.Items.interfaces.Weapon;
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
public class StdTorpedo extends StdSpaceTechWeapon
{
	@Override
	public String ID()
	{
		return "StdTorpedo";
	}

	public StdTorpedo()
	{
		super();
		setName("a torpedo");
		setDisplayText("a torpedo is sitting here");
		super.properWornBitmap=0;
		super.weaponClass=Weapon.CLASS_BLUNT;
		super.weaponType=Weapon.TYPE_BURNING;
		super.basePhyStats.setWeight(1000);
		super.phyStats.setWeight(1000);
	}

	protected volatile Long timeTicking = null;

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((ticking == this)
		&& (tickID == Tickable.TICKID_SPACEWEAPON))
		{
			if(timeTicking == null)
				timeTicking = Long.valueOf(System.currentTimeMillis());
			final long diff = System.currentTimeMillis()-timeTicking.longValue();
			if(diff > 300000) // 5 minutes of live sounds good
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
}
