package com.planet_ink.coffee_mud.Items.Armor;
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
public class LeatherArmor extends StdArmor
{
	public String ID(){	return "LeatherArmor";}
	public LeatherArmor()
	{
		super();

		setName("a suit of leather armor");
		setDisplayText("a suit of leather armor including a breastplate, arms, and legs.");
		setDescription("This is a fairly decent looking suit of leather armor.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(12);
		baseEnvStats().setWeight(15);
		baseEnvStats().setAbility(0);
		baseGoldValue=10;
		recoverEnvStats();
		material=EnvResource.RESOURCE_LEATHER;
	}

}
