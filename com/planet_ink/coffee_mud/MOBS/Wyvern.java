package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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


		baseCharStats().setStat(CharStats.INTELLIGENCE,5 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStat(CharStats.STRENGTH,18);
		baseCharStats().setStat(CharStats.DEXTERITY,13);
		baseCharStats().setMyRace(CMClass.getRace("Wyvern"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setDamage(16);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(7);
		baseEnvStats().setArmor(30);
        baseEnvStats().setDisposition(baseEnvStats().disposition()|EnvStats.IS_FLYING);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

        addBehavior(CMClass.getBehavior("Aggressive"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new Wyvern();
	}

	public boolean tick(int tickID)
	{
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

	public void recoverCharStats()
	{
		super.recoverCharStats();
		charStats().setStat(CharStats.SAVE_POISON,charStats().getStat(CharStats.SAVE_POISON)+100);
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
 				FullMsg msg=new FullMsg(this, target, null, Affect.MSK_MALICIOUS_MOVE|Affect.TYP_POISON, "^F<S-NAME> sting(s) <T-NAMESELF>!^?");
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
