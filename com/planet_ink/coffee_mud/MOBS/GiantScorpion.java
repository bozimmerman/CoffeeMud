package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

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


		baseCharStats().setStat(CharStats.INTELLIGENCE,5 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STRENGTH,13);
		baseCharStats().setStat(CharStats.DEXTERITY,9);

		baseEnvStats().setDamage(10);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(5);
		baseEnvStats().setArmor(30);

		baseState.setHitPoints((Math.abs(randomizer.nextInt() % 18)*baseEnvStats().level()) + 9);

        addBehavior(CMClass.getBehavior("Mobile"));

		recoverMaxState();
		resetToMaxState();
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
		if((!amDead())&&(tickID==Host.MOB_TICK))
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
		if (Sense.aliveAwakeMobile(this,true)&&
			(Sense.canHear(this)||Sense.canSee(this)||Sense.canSmell(this)))
		{
			MOB target = getVictim();
			// ===== if it is less than three so roll for it
			int roll = (int)Math.round(Math.random()*99);

			// ===== check the result
			if (roll<20)
			{
                // Sting was successful
 				FullMsg msg=new FullMsg(this, target, null, Affect.MSK_MALICIOUS_MOVE|Affect.TYP_POISON, "<S-NAME> sting(s) <T-NAMESELF>");
				if(location().okAffect(msg))
				{
					this.location().send(target,msg);
					if(!msg.wasModified())
					{
						Ability poison = CMClass.getAbility("Poison");
						poison.baseEnvStats().setLevel(baseEnvStats().level());
						poison.invoke(this, target, true);
					}
				}
			}
		}
		return true;
	}


}
