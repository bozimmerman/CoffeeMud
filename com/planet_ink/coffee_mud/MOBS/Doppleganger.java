package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class Doppleganger extends StdMOB
{

	private MOB mimicing=null;
	private long ticksSinceMimicing=0;

	public Doppleganger()
	{
		super();
		revert();
	}

	private void revert()
	{
		Random randomizer = new Random(System.currentTimeMillis());
		Username="a doppleganger";
		setDescription("A formless biped creature, with wicked black eyes.");
		setDisplayText("A formless biped stands here.");
		setBaseEnvStats(new DefaultEnvStats());
		setBaseCharStats(new DefaultCharStats());
		setBaseState(new DefaultCharState());
		setAlignment(0);
		setMoney(250);
		baseEnvStats.setWeight(100 + Math.abs(randomizer.nextInt() % 101));

		baseCharStats().setStat(CharStats.INTELLIGENCE,10 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.STRENGTH,12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStat(CharStats.DEXTERITY,9 + Math.abs(randomizer.nextInt() % 6));

		baseEnvStats().setDamage(7);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(20);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,5));

		addBehavior(CMClass.getBehavior("Mobile"));
		addBehavior(CMClass.getBehavior("MudChat"));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public Environmental newInstance()
	{
		return new Doppleganger();
	}

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==Host.MOB_TICK))
		{
			if(mimicing!=null)
			{
				ticksSinceMimicing++;
				if(ticksSinceMimicing>500)
				{
					revert();
				}
			}
		}
		return super.tick(tickID);
	}

	public DeadBody killMeDead()
	{
		revert();
		return super.killMeDead();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if((affect.amITarget(this))&&(Util.bset(affect.targetCode(),Affect.MASK_MALICIOUS)))
		{
			if(mimicing!=null)
			{
				if((mimicing.getVictim()!=null)&&(mimicing.getVictim()!=this))
					mimicing=null;
				if((mimicing.location()!=null)&&(mimicing.location()!=location()))
					mimicing=null;
			}
			if((mimicing==null)&&(location()!=null)&&(affect.source()!=null))
			{
				location().show(this,null,Affect.MSG_OK_VISUAL,"<S-NAME> take(s) on a new form!");
				mimicing=affect.source();
				Username=mimicing.name();
				setDisplayText(mimicing.rawDisplayText());
				setDescription(mimicing.description());
				setBaseEnvStats(mimicing.baseEnvStats().cloneStats());
				setBaseCharStats(mimicing.baseCharStats().cloneCharStats());
				setBaseState(mimicing.baseState().cloneCharState());
				recoverEnvStats();
				recoverCharStats();
				recoverMaxState();
				resetToMaxState();
				ticksSinceMimicing=0;
			}
		}
		return true;
	}
}