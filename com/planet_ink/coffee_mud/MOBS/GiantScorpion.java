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
public class GiantScorpion extends StdMOB
{

	public int stingDown=5;

	public GiantScorpion()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a Giant Scorpion";
		setDescription("The giant scorpion has a green carapace and yellowish green legs and pincers. The segmented tail is black, with a vicious stinger on the end.");
		setDisplayText("A mean Giant Scorpion hunts.");
		setAlignment(500);
		setMoney(0);
		setWimpHitPoint(2);

		baseEnvStats().setWeight(1000 + Math.abs(randomizer.nextInt() % 550));


		baseCharStats().setIntelligence(5 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStrength(13);
		baseCharStats().setDexterity(9);

		baseEnvStats().setDamage(10);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(5);
		baseEnvStats().setArmor(30);

		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 9);

        addBehavior(new Mobile());

		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GiantScorpion();
	}

	public boolean tick(int tickID)
	{
		// ===== are we in combat?
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
