package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class UmberHulk extends StdMOB
{
	public String ID(){return "UmberHulk";}
	Random randomizer = new Random();
	int confuseDown=3;

	public UmberHulk()
	{
		super();

		Username="an Umber Hulk";
		setDescription("An 8 foot tall, 5 foot wide mass of meanness just waiting to eat....");
		setDisplayText("A huge Umber Hulk eyes you.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(350);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,8);
		baseCharStats().setStat(CharStats.CHARISMA,2);
		baseCharStats().setMyRace(CMClass.getRace("UmberHulk"));
		baseCharStats().getMyRace().startRacing(this,false);

		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(8);
		baseEnvStats().setAttackAdjustment(baseEnvStats().attackAdjustment()+20);
		baseEnvStats().setDamage(baseEnvStats().damage()+12);
		baseEnvStats().setArmor(0);
		baseEnvStats().setSpeed(2.0);
		baseEnvStats().setSensesMask(EnvStats.CAN_SEE_DARK | EnvStats.CAN_SEE_INFRARED);

		baseState.setHitPoints(Dice.roll(baseEnvStats().level(),20,baseEnvStats().level()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new UmberHulk();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((!amDead())&&(tickID==Host.TICK_MOB))
		{
			if((--confuseDown)<=0)
			{
				confuseDown=3;
				confuse();
			}
		}
		return super.tick(ticking,tickID);
	}
    public void addNaturalAbilities()
    {
        Ability confuse=CMClass.getAbility("Spell_Confusion");
		if(confuse==null) return;

    }
	protected boolean confuse()
	{
		if(this.location()==null)
			return true;

      Ability confuse=CMClass.getAbility("Spell_Confusion");
		confuse.setProfficiency(75);
		if(this.fetchAbility(confuse.ID())==null)
		   this.addAbility(confuse);
		else
			confuse =this.fetchAbility(confuse.ID());

		if(confuse!=null) confuse.invoke(this,null,false);
		return true;
	}


}
