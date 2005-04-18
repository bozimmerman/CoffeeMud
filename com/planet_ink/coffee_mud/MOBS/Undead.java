package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class Undead extends StdMOB
{
	public String ID(){return "Undead";}
	public Undead()
	{
		super();
		Username="an undead being";
		setDescription("decayed and rotting, a dead body has been brought back to life...");
		setDisplayText("an undead thing slowly moves about.");
		Factions.setAlignment(this,Faction.ALIGN_EVIL);
		setMoney(10);
		baseEnvStats.setWeight(30);
		setWimpHitPoint(0);

		baseCharStats().setMyRace(CMClass.getRace("Undead"));
		baseCharStats().getMyRace().startRacing(this,false);
		baseEnvStats().setDamage(8);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(80);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setDisposition(0); // disable infrared stuff
		baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		addAbility(CMClass.getAbility("Skill_AllBreathing"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}


}
