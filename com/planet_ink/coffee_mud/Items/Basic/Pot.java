package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Pot extends StdDrink
{
	public String ID(){	return "Pot";}
	public Pot()
	{
		super();
		setName("a pot");
		setDisplayText("a cooking pot sits here.");
		setDescription("A sturdy iron pot for cooking in.");
		capacity=25;
		baseGoldValue=5;
		setLiquidHeld(20);
		setThirstQuenched(1);
		setLiquidRemaining(0);
		setLiquidType(EnvResource.RESOURCE_FRESHWATER);
		setMaterial(EnvResource.RESOURCE_IRON);
		baseEnvStats().setWeight(5);
		recoverEnvStats();
	}



}
