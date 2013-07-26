package com.planet_ink.coffee_mud.Items.MiscTech;
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
public class StdShipThruster extends StdFuelConsumer implements ShipComponent.ShipEngine
{
	public String ID(){	return "StdShipThruster";}
	public StdShipThruster()
	{
		super();
		setName("a thruster engine");
		basePhyStats.setWeight(5000);
		setDisplayText("a thruster engine sits here.");
		setDescription("");
		baseGoldValue=500000;
		basePhyStats().setLevel(1);
		recoverPhyStats();
		setMaterial(RawMaterial.RESOURCE_STEEL);
		setCapacity(basePhyStats.weight()+10000);
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof StdShipThruster)) return false;
		return super.sameAs(E);
	}
	protected int maxThrust=1000;
	public int getMaxThrust(){return maxThrust;}
	public void setMaxThrust(int max){maxThrust=max;}
	protected int thrust=0;
	public int getThrust(){return thrust;}
	public void setThrust(int current){thrust=current;}
	protected long specificImpulse=SpaceObject.VELOCITY_SUBLIGHT;
	public long getSpecificImpulse() { return specificImpulse; }
	public void setSpecificImpulse(long amt) { specificImpulse = amt; }
	
	@Override protected boolean willConsumeFuelIdle() { return getThrust()>0; }
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		executeThrusterMsg(this, myHost, msg);
	}
	
	public static void executeThrusterMsg(ShipEngine me, Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(me))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ACTIVATE:
				me.activate(true);
				//TODO:what does the ship need to know?
				break;
			case CMMsg.TYP_DEACTIVATE:
				me.setThrust(0);
				me.activate(false);
				//TODO:what does the ship need to know?
				break;
			case CMMsg.TYP_POWERCURRENT:
				//TODO:what does the ship need to know?
				break;
			}
		}
	}
}
