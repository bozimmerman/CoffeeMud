package com.planet_ink.coffee_mud.Items.Weapons;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.Basic.StdItem;

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
public class StdSpear extends StdWeapon
{
	public String ID(){	return "StdSpear";}
	public StdSpear()
	{
		super();
		setName("a spear");
		setDisplayText("a spear has been left here.");
		setDescription("It looks like it might sail far!");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(8);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(5);
		baseGoldValue=10;
		recoverEnvStats();
		minRange=0;
		maxRange=3;
		weaponType=Weapon.TYPE_PIERCING;
		material=EnvResource.RESOURCE_WOOD;
		weaponClassification=Weapon.CLASS_THROWN;
		setRawLogicalAnd(false);
	}


}
