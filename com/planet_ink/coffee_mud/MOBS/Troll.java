package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.db.*;

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

		baseCharStats().setIntelligence(8 + Math.abs(randomizer.nextInt()) % 3);
		baseCharStats().setCharisma(2);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(8);
		baseEnvStats().setAttackAdjustment(baseEnvStats().attackAdjustment()+20);
		baseEnvStats().setDamage(baseEnvStats().damage()+5);
		baseEnvStats().setArmor(30);
		baseEnvStats().setSpeed(3.0);
		baseEnvStats().setSensesMask(Sense.CAN_SEE_DARK | Sense.CAN_SEE_INFRARED);

		int hitPoints = 0;
		for(int i = 0; i < 7; i++)
			hitPoints += Math.abs(randomizer.nextInt()) % 10 + 1;
		hitPoints+=6;

		maxState.setHitPoints(hitPoints);

		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Troll();
	}

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==ServiceEngine.MOB_TICK))
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
		int AffectCode = Affect.VISUAL_WNOISE;

		target = location();

		if(curState.getHitPoints() < maxState.getHitPoints())
		{
			String msgText = "The troll regenerates wounds";

			FullMsg message = new FullMsg(this, target, null, AffectCode, Affect.VISUAL_WNOISE, Affect.VISUAL_WNOISE, msgText);

			target.send(this, message);

			curState.adjHitPoints(3, maxState);
		}
		return(true);

	}


}
