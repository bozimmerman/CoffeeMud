package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.service.*;
public class Wyvern extends StdMOB
{
	private int stingDown=5;

	public Wyvern()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a wyvern";
		setDescription("A distant cousin to the dragon, a wyvern is 35-foot-long dark brown to gray body of the wyvern is half tail. Its leathery batlike wings are over 50 feet from tip to tip..");
		setDisplayText("A mean looking wyvern is here.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(2);

		baseEnvStats().setWeight(2000 + Math.abs(randomizer.nextInt() % 550));


		baseCharStats().setIntelligence(5 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStrength(18);
		baseCharStats().setDexterity(13);

		baseEnvStats().setDamage(16);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(7);
		baseEnvStats().setArmor(30);
        baseEnvStats().setDisposition(Sense.IS_FLYING);

		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 9);

        addBehavior(new Aggressive());

		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Wyvern();
	}

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==ServiceEngine.MOB_TICK))
		{
			if((--stingDown)<=0)
			{
				stingDown=5;
				if (isInCombat())
					sting();
			}
		}
        return super.tick(tickID);
	}

	protected boolean sting()
	{
		if (Sense.canPerformAction(this)&&
			(Sense.canHear(this)||Sense.canSee(this)||Sense.canSmell(this)))
		{
			MOB target = getVictim();
			// ===== if it is less than three so roll for it
			int roll = (int)Math.round(Math.random()*99);

			// ===== check the result
			if (roll<20)
			{
                // Sting was successful
 				FullMsg msg=new FullMsg(this, target, null, Affect.STRIKE, Affect.STRIKE, Affect.STRIKE, "<S-NAME> stings <T-NAME>");
				this.location().send(target,msg);
                Poison poison = new Poison();
                poison.baseEnvStats().setLevel(baseEnvStats().level());
                poison.invoke(this, target, true);
			}
		}
		return true;
	}


}
