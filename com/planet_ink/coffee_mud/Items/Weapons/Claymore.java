package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class Claymore extends Sword
{
	public String ID(){	return "Claymore";}
	public final static int PLAIN					= 0;
	public final static int QUALITY_WEAPON			= 1;
	public final static int EXCEPTIONAL	  			= 2;

	public Claymore()
	{
		super();


		Random randomizer = new Random(System.currentTimeMillis());
		int claymoreType = Math.abs(randomizer.nextInt() % 3);

		this.envStats.setAbility(claymoreType);
		setItemDescription(this.envStats.ability());

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(10);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		baseGoldValue=25;
		recoverEnvStats();
		wornLogicalAnd=true;
		properWornBitmap=Item.HELD|Item.WIELD;
		material=EnvResource.RESOURCE_STEEL;
		weaponType=TYPE_SLASHING;
	}



	public void setItemDescription(int level)
	{
		switch(level)
		{
			case Claymore.PLAIN:
				setName("a simple claymore");
				setDisplayText("a simple claymore is on the ground.");
				setDescription("It\\`s an oversized two-handed sword.  Someone made it just to make it.");
				break;

			case Claymore.QUALITY_WEAPON:
				setName("a very nice claymore");
				setDisplayText("a very nice claymore leans against the wall.");
				setDescription("It\\`s ornate with an etched hilt.  Someone took their time making it.");
				break;

			case Claymore.EXCEPTIONAL:
				setName("an exceptional claymore");
				setDisplayText("an exceptional claymore is found nearby.");
				setDescription("It\\`s a huge two-handed sword, with a etchings in the blade and a tassel hanging from the hilt.");
				break;

			default:
				setName("a simple claymore");
				setDisplayText("a simple claymore is on the ground.");
				setDescription("It\\`s an oversized two-handed sword.  Someone made it just to make it.");
				break;
		}
	}


}
