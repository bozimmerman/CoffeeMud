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
public class EternityBarkArmor extends StdArmor
{
	public String ID(){	return "EternityBarkArmor";}
	public EternityBarkArmor()
	{
		super();

		setName("a suit of Eternity Tree Bark Armor");
		setDisplayText("a suit of Eternity tree bark armor sits here.");
		setDescription("This suit of armor is made from the bark of the Fox god\\`s Eternity Tree(armor:  100 and as light as leather armor--wearable by theives)");
		properWornBitmap=Item.ON_TORSO | Item.ON_ARMS | Item.ON_LEGS;
		wornLogicalAnd=true;
		baseGoldValue+=25000;
		baseEnvStats().setArmor(100);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
		material=EnvResource.RESOURCE_WOOD;
	}


}
