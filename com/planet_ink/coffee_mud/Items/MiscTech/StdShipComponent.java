package com.planet_ink.coffee_mud.Items.MiscTech;
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
public class StdShipComponent extends StdShipItem implements ShipComponent
{
	public String ID(){	return "StdShipComponent";}
	public StdShipComponent()
	{
		super();
		setName("a ships component");
		setDisplayText("a small ships component sits here.");
		setDescription("You can't tell what it is by looking at it.");

		material=EnvResource.RESOURCE_STEEL;
		baseGoldValue=0;
		recoverEnvStats();
	}
	
	private int componentType=ShipComponent.COMPONENT_MISC;
	public int componentType(){return componentType;}
	public void setComponentType(int type){componentType=type;}
}
