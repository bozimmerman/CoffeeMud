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
public class Sword extends StdWeapon
{
	public String ID(){	return "Sword";}
	public Sword()
	{
		super();

		setName("a sword");
		setDisplayText("a rather plain looking sword leans against the wall.");
		setDescription("An plain sword.");
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(10);
		recoverEnvStats();
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(8);
		recoverEnvStats();
		weaponType=TYPE_SLASHING;
		material=EnvResource.RESOURCE_STEEL;
		weaponClassification=Weapon.CLASS_SWORD;
	}

	public Environmental newInstance()
	{
		if(!ID().equals("Sword"))
		{
			try{
				return (Environmental)this.getClass().newInstance();
			}
			catch(Exception e){}
			return new Sword();
		}
		Random randomizer = new Random(System.currentTimeMillis());
		int swordType = Math.abs(randomizer.nextInt() % 6);
		switch (swordType)
		{
			case 0:  return new Rapier();
			case 1:	 return new Katana();
			case 2:	 return new Longsword();
			case 3:	 return new Scimitar();
			case 4:	 return new Claymore();
			case 5:	 return new Shortsword();
			default: 
				try{
					return (Environmental)this.getClass().newInstance();
				}
				catch(Exception e){}
				return new Sword();
		}

	}
}
