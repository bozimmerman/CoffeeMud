package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Troll extends StdMOB
{
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

		int hitPoints = 0;
		for(int i = 0; i < 7; i++)
			hitPoints += Math.abs(randomizer.nextInt()) % 10 + 1;
		hitPoints+=6;

		baseState.setHitPoints(hitPoints);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Troll();
	}

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==Host.MOB_TICK))
		{
			if((--regDown)<=0)
			{
				regDown=3;
				regenerate();
			}
		}
		return super.tick(tickID);
	}

	protected boolean regenerate()
	{
		Room target = null;
		target = location();

		if(curState.getHitPoints() < maxState.getHitPoints())
		{
			String msgText = "The troll regenerates wounds";

			FullMsg message = new FullMsg(this, target, null, Affect.MSG_OK_ACTION, msgText);

			target.send(this, message);

			curState.adjHitPoints(3, maxState);
		}
		return(true);

	}


}
