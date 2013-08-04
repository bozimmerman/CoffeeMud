package com.planet_ink.coffee_mud.Items.ShipTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
public class StdShipEngine extends StdCompGenerator implements ShipComponent.ShipEngine
{
	public String ID(){	return "StdShipEngine";}
	
	protected float 	installedFactor	= 1.0F;
	protected int		maxThrust		= 1000;
	protected int		thrust			= 0;
	protected long		specificImpulse	= SpaceObject.VELOCITY_SUBLIGHT;
	protected double	fuelEfficiency	= 0.33;

	public StdShipEngine()
	{
		super();
		setName("a ships engine");
		basePhyStats.setWeight(50000);
		setDisplayText("a ships engine sits here.");
		setDescription("");
		baseGoldValue=500000;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdShipEngine)) return false;
		return super.sameAs(E);
	}
	@Override public double getFuelEfficiency() { return fuelEfficiency; }
	@Override public void setFuelEfficiency(double amt) { fuelEfficiency=amt; }
	@Override public float getInstalledFactor() { return installedFactor; }
	@Override public void setInstalledFactor(float pct) { if((pct>=0.0)&&(pct<=2.0)) installedFactor=pct; }
	@Override public int getMaxThrust(){return maxThrust;}
	@Override public void setMaxThrust(int max){maxThrust=max;}
	@Override public int getThrust(){return thrust;}
	@Override public void setThrust(int current){thrust=current;}
	@Override public long getSpecificImpulse() { return specificImpulse; }
	@Override public void setSpecificImpulse(long amt) { specificImpulse = amt; }
	@Override public TechType getTechType() { return TechType.SHIP_ENGINE; }
	
	@Override protected boolean willConsumeFuelIdle() { return getThrust()>0; }
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		StdShipThruster.executeThrusterMsg(this, myHost, circuitKey, msg);
	}
}
