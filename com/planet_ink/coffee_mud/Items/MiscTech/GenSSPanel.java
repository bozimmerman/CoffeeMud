package com.planet_ink.coffee_mud.Items.MiscTech;
import com.planet_ink.coffee_mud.Items.Basic.StdContainer;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class GenSSPanel extends GenShipContainer
{
	public String ID(){	return "GenSSPanel";}
	public GenSSPanel()
	{
		super();
		setName("a generic space ship panel");
		baseEnvStats.setWeight(2);
		setDescription("");
		baseGoldValue=5;
		containType=Container.CONTAIN_SSCOMPONENTS;
		setLidsNLocks(true,true,false,false);
		capacity=500;
		setMaterial(EnvResource.RESOURCE_STEEL);
		recoverEnvStats();
	}

	public String displayText(){
		if(isOpen())
			return name()+" is opened here.";
		else
			return "";
	}
	public boolean canContain(Environmental E)
	{
		if(!super.canContain(E)) return false;
		if(E instanceof ShipComponent)
		{
			int myType=((ShipComponent)E).componentType();
			switch(componentType())
			{
			case ShipComponent.COMPONENT_PANEL_ANY:
				return true;
			case ShipComponent.COMPONENT_PANEL_ENGINE:
				return myType==ShipComponent.COMPONENT_ENGINE;
			case ShipComponent.COMPONENT_PANEL_POWER:
				return myType==ShipComponent.COMPONENT_POWER;
			case ShipComponent.COMPONENT_PANEL_SENSOR:
				return myType==ShipComponent.COMPONENT_SENSOR;
			case ShipComponent.COMPONENT_PANEL_WEAPON:
				return myType==ShipComponent.COMPONENT_WEAPON;
			case ShipComponent.COMPONENT_PANEL_COMPUTER:
				return myType==ShipComponent.COMPONENT_COMPUTER;
			}
		}
		return true;
	}

	public boolean isGeneric(){return true;}
}
