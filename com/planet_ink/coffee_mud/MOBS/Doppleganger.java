package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Behaviors.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.service.*;
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
		setBaseEnvStats(new Stats());
		setBaseCharStats(new CharStats());
		setMaxState(new CharState());
		setAlignment(0);
		setMoney(250);
		baseEnvStats.setWeight(100 + Math.abs(randomizer.nextInt() % 101));

		baseCharStats().setIntelligence(10 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setStrength(12 + Math.abs(randomizer.nextInt() % 6));
		baseCharStats().setDexterity(9 + Math.abs(randomizer.nextInt() % 6));

		baseEnvStats().setDamage(7);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(6);
		baseEnvStats().setArmor(20);

		maxState.setHitPoints(Dice.roll(baseEnvStats().level(),20,5));

		addBehavior(new Mobile());
		addBehavior(new MudChat());

		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public Environmental newInstance()
	{
		return new Doppleganger();
	}

	public boolean tick(int tickID)
	{
		if((!amDead())&&(tickID==ServiceEngine.MOB_TICK))
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

	public void kill()
	{
		super.kill();
		revert();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if((affect.amITarget(this))&&(affect.targetType()==Affect.STRIKE))
		{
			if(mimicing!=null)
			{
				if(mimicing.getVictim()!=this)
					mimicing=null;
				if(mimicing.location()!=location())
					mimicing=null;
			}
			if(mimicing==null)
			{
				location().show(this,null,Affect.VISUAL_WNOISE,"<S-NAME> take(s) on a new form!");
				mimicing=affect.source();
				Username=mimicing.name();
				setDisplayText(mimicing.rawDisplayText());
				setDescription(mimicing.description());
				setBaseEnvStats(mimicing.baseEnvStats().cloneStats());
				setBaseCharStats(mimicing.baseCharStats().cloneCharStats());
				setMaxState(mimicing.maxState().cloneCharState());
				recoverEnvStats();
				recoverCharStats();
				recoverMaxState();
				ticksSinceMimicing=0;
			}
		}
		return true;
	}
}