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
public class EternityLeafShield extends StdShield
{
	public String ID(){	return "EternityLeafShield";}
	public EternityLeafShield()
	{
		super();

		setName("a huge leaf");
		setDisplayText("a huge and very rigid leaf lays on the ground.");
		setDescription("a very huge and very rigid leaf");
		secretIdentity="A shield made from one of the leaves of the Fox god\\`s Eternity Trees.  (Armor:  30)";
		properWornBitmap=Item.HELD;
		wornLogicalAnd=true;
		baseGoldValue+=15000;
		baseEnvStats().setArmor(30);
		baseEnvStats().setAbility(0);
		baseEnvStats().setWeight(15);
		baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_BONUS);
		recoverEnvStats();
		material=EnvResource.RESOURCE_SEAWEED;
	}


}
