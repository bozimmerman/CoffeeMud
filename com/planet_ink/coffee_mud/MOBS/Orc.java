package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
/* 
   Copyright 2000-2004 Lee H. Fox

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
public class Orc extends StdMOB
{
	public String ID(){return "Orc";}
	public Orc()
	{
		super();
		Username="an Orc";
		setDescription("He\\`s dirty, cranky, and very mean.");
		setDisplayText("An angry Orc marches around.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		Weapon d=CMClass.getWeapon("Dagger");
		if(d!=null)
		{
			d.wearAt(Item.WIELD);
			addInventory(d);
		}

		baseCharStats().setStat(CharStats.INTELLIGENCE,6);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setMyRace(CMClass.getRace("Orc"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}




}
