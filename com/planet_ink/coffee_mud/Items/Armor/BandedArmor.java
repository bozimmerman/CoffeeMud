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
public class BandedArmor extends StdArmor
{
	public String ID(){	return "BandedArmor";}
	public BandedArmor()
	{
		super();
		setName("a suit of banded armor");
		setDisplayText("a suit of armor made from metal bands fastened to leather");
		setDescription("This suit of armor is made from metal bands fastened to leather and will provide protection for the torso, arms, and legs.");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseEnvStats().setArmor(44);
		baseEnvStats().setWeight(55);
		baseEnvStats().setAbility(0);
		baseGoldValue=400;
		material=EnvResource.RESOURCE_IRON;
		recoverEnvStats();
	}

}
