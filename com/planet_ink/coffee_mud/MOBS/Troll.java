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
public class Troll extends StdMOB
{
	public String ID(){return "Troll";}
	Random randomizer = new Random();
	int regDown=3;

	public Troll()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a troll";
		setDescription("Nine foot tall and reeking of rotten meat..");
		setDisplayText("A mean looking troll glares at you.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(350);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,8 + Math.abs(randomizer.nextInt()) % 3);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setMyRace(CMClass.getRace("Troll"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(8);
		baseEnvStats().setAttackAdjustment(baseEnvStats().attackAdjustment()+20);
		baseEnvStats().setDamage(baseEnvStats().damage()+5);
		baseEnvStats().setArmor(30);
		baseEnvStats().setSpeed(3.0);
		baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK | EnvStats.CAN_SEE_INFRARED);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}


	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==MudHost.TICK_MOB))
		{
			if((--regDown)<=0)
			{
				regDown=3;
				regenerate();
			}
		}
		return super.tick(ticking,tickID);
	}

	protected boolean regenerate()
	{
		Room target = null;
		target = location();

		if(curState.getHitPoints() < maxState.getHitPoints())
		{
			String msgText = "The troll regenerates wounds";

			FullMsg message = new FullMsg(this, target, null, CMMsg.MSG_OK_ACTION, msgText);

			target.send(this, message);

			curState.adjHitPoints(3, maxState);
		}
		return(true);

	}


}
