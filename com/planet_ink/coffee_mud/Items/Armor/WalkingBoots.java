package com.planet_ink.coffee_mud.Items.Armor;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

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
public class WalkingBoots extends StdArmor
{
	public String ID(){	return "WalkingBoots";}
	public WalkingBoots()
	{
		super();

		setName("a pair of nice hide walking boots");
		setDisplayText("a pair of hide walking boots sits here.");
		setDescription("They look like a rather nice pair of footwear.");
		properWornBitmap=Item.ON_FEET;
		wornLogicalAnd=false;
		baseEnvStats().setArmor(1);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(5);
		baseGoldValue=5;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}


}
