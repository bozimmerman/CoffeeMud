package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.StdItem;

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
public class StdBoffWeapon extends StdWeapon
{
	public String ID(){	return "StdBoffWeapon";}

	public StdBoffWeapon()
	{
		super();

		setName("boff weapon");
		setDisplayText(" sits here.");
		setDescription("This is a not so deadly looking weapon.");
		wornLogicalAnd=false;
		properWornBitmap=Item.HELD|Item.WIELD;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(0);
		baseEnvStats().setAbility(0);
		baseGoldValue=15;
		weaponType=Weapon.TYPE_BASHING;
		weaponClassification=Weapon.CLASS_BLUNT;
		material=EnvResource.RESOURCE_STEEL;
		setUsesRemaining(100);
		recoverEnvStats();
	}

	public String hitString(int damageAmount)
	{
		String word="boff(s)";
		switch(Dice.roll(1,7,-1))
		{
		case 0: word= "puff(s)"; break;
		case 1: word= "boff(s)"; break;
		case 2: word= "poof(s)"; break;
		case 3: word= "bambam(s)"; break;
		case 4: word= "whack(s)"; break;
		case 5: word= "smoosh(es)"; break;
		case 6: word= "kabloom(s)"; break;
		}
		boolean showDamn=CommonStrings.getVar(CommonStrings.SYSTEM_SHOWDAMAGE).equalsIgnoreCase("YES");
		switch(weaponClassification())
		{
		case Weapon.CLASS_RANGED:
			return "<S-NAME> fire(s) "+name()+" at <T-NAMESELF> and "+word+((showDamn)?" ("+damageAmount+")":"")+" <T-HIM-HER>."+CommonStrings.msp("arrow.wav",20);
		case Weapon.CLASS_THROWN:
			return "<S-NAME> throw(s) "+name()+" at <T-NAMESELF> and "+word+((showDamn)?" ("+damageAmount+")":"")+" <T-HIM-HER>."+CommonStrings.msp("arrow.wav",20);
		default:
			return "<S-NAME> "+word+((showDamn)?" ("+damageAmount+")":"")+" <T-NAMESELF> with "+name()+"."+CommonStrings.msp("punch"+Dice.roll(1,4,0)+".wav",20);
		}
	}
}
