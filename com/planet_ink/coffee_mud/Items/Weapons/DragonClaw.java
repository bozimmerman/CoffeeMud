package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

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
public class DragonClaw extends Natural
{
	public String ID(){	return "DragonClaw";}
	public DragonClaw()
	{
		super();

		setName("a vicious dragons claw.");
		setDisplayText("a Dragons Claw");
		setDescription("the claw of a dragon");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(7);
		baseEnvStats().setAttackAdjustment(2);
		baseEnvStats().setDamage(8);
		recoverEnvStats();
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
		weaponClassification=Weapon.CLASS_NATURAL;
	}


}
