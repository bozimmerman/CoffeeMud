package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
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
public class FireGiant extends StdMOB
{
	public String ID(){return "FireGiant";}
	public FireGiant()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a Fire Giant";
		setDescription("A tall humanoid standing about 18 feet tall, 12 foot chest, coal black skin and fire red-orange hair.");
		setDisplayText("A Fire Giant ponders killing you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(6500 + Math.abs(randomizer.nextInt() % 1001));


		baseCharStats().setStat(CharStats.INTELLIGENCE,8 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STRENGTH,20);
		baseCharStats().setStat(CharStats.DEXTERITY,13);
		baseCharStats().setMyRace(CMClass.getRace("Giant"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(20);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(15);
		baseEnvStats().setArmor(-10);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public void recoverCharStats()
	{
		super.recoverCharStats();
		charStats().setStat(CharStats.SAVE_FIRE,charStats().getStat(CharStats.SAVE_FIRE)+100);
	}


}
