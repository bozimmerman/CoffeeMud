package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
public class GenUndead extends GenMob
{
	public GenUndead()
	{
		super();
		Username="a generic undead being";
		setDescription("He looks dead to me.");
		setDisplayText("A generic undead mob stands here.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setMyRace(CMClass.getRace("Undead"));
		baseCharStats().getMyRace().setHeightWeight(baseEnvStats(),(char)baseCharStats().getStat(CharStats.GENDER));
		baseCharStats().setStat(CharStats.CHARISMA,2);

		baseEnvStats().setAbility(10);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(50);

		baseState.setHitPoints((10*baseEnvStats().level())+(int)Math.round(Math.random()*baseEnvStats().level()*baseEnvStats().ability()));

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new GenUndead();
	}
	public boolean isGeneric(){return true;}
}
