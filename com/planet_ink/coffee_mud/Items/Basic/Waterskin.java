package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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
public class Waterskin extends StdDrink
{
	public String ID(){	return "Waterskin";}
	public Waterskin()
	{
		super();
		setName("a waterskin");
		amountOfThirstQuenched=200;
		amountOfLiquidHeld=1000;
		amountOfLiquidRemaining=1000;
		baseEnvStats.setWeight(10);
		capacity=5;
		setDisplayText("a tough little waterskin sits here.");
		setDescription("Looks like it could hold quite a bit of drink.");
		baseGoldValue=10;
		material=EnvResource.RESOURCE_LEATHER;
		recoverEnvStats();
	}



}
