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
public class Halberd extends StdWeapon
{
	public String ID(){	return "Halberd";}
	public final static int PLAIN					= 0;
	public final static int QUALITY_WEAPON			= 1;
	public final static int EXCEPTIONAL	  			= 2;

	public Halberd()
	{
		super();


		Random randomizer = new Random(System.currentTimeMillis());
		int HalberdType = Math.abs(randomizer.nextInt() % 3);

		this.envStats.setAbility(HalberdType);
		setItemDescription(this.envStats.ability());

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(10);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(10);
		baseGoldValue=10;
		recoverEnvStats();
		wornLogicalAnd=true;
		properWornBitmap=Item.HELD|Item.WIELD;
		weaponType=TYPE_SLASHING;
		material=EnvResource.RESOURCE_STEEL;
		weaponClassification=Weapon.CLASS_POLEARM;
	}

	public void setItemDescription(int level)
	{
		switch(level)
		{
			case Claymore.PLAIN:
				setName("a simple halberd");
				setDisplayText("a simple halberd is on the ground.");
				setDescription("It`s a polearm with a large bladed axe on the end.");
				break;
			case Claymore.QUALITY_WEAPON:
				setName("a very nice halberd");
				setDisplayText("a very nice halberd leans against the wall.");
				setDescription("It`s an ornate polearm with a large bladed axe on the end.");
				break;
			case Claymore.EXCEPTIONAL:
				setName("an exceptional halberd");
				setDisplayText("an exceptional halberd is found nearby.");
				setDescription("It`s an ornate polearm with a large bladed axe on the end.  It is well balanced and decorated with fine etchings.");
				break;
			default:
				setName("a simple halberd");
				setDisplayText("a simple halberd is on the ground.");
				setDescription("It`s a polearm with a large bladed axe on the end.");
				break;
		}
	}



}
