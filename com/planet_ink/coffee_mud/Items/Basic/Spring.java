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
public class Spring extends StdDrink
{
	public String ID(){	return "Spring";}
	public Spring()
	{
		super();
		setName("a spring");
		amountOfThirstQuenched=250;
		amountOfLiquidHeld=999999;
		amountOfLiquidRemaining=999999;
		baseEnvStats().setWeight(5);
		capacity=0;
		setDisplayText("a little magical spring flows here.");
		setDescription("The spring is coming magically from the ground.  The water looks pure and clean.");
		baseGoldValue=10;
		baseEnvStats().setSensesMask(EnvStats.SENSE_ITEMNOTGET);
		material=EnvResource.RESOURCE_FRESHWATER;
		recoverEnvStats();
	}


}
