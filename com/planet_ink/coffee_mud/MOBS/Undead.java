package com.planet_ink.coffee_mud.MOBS;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Races.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.db.*;
public class Undead extends StdMOB
{
	
	public Undead()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="an undead being";
		setDescription("decayed and rotting, a dead body has been brought back to life...");
		setDisplayText("an undead thing slowly moves about.");
		setAlignment(0);
		setMoney(10);
		baseEnvStats.setWeight(30);
		setWimpHitPoint(0);
		
		baseEnvStats().setDamage(8);
		
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(1);
		baseEnvStats().setArmor(80);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setSensesMask(Sense.CAN_SEE_DARK);
		
		int hitPoints = 0;
		hitPoints += Math.abs(randomizer.nextInt()) % 18 + 1;
		hitPoints += Math.abs(randomizer.nextInt()) % 18 + 1;

		maxState.setHitPoints(hitPoints);
		
		addAbility(new Skill_AllBreathing());
		
		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	
	public Environmental newInstance()
	{
		return new Undead();
	}
	public void affect(Affect affect)
	{
		if(affect.amITarget(this)&&(affect.targetCode()==Affect.STRIKE_MIND))
		{
			String tool=null;
			if(affect.tool()!=null)
			{
			    if(affect.tool() instanceof Ability)
					tool=((Ability)affect.tool()).name();
			}
			location().show(affect.source(),this,Affect.VISUAL_WNOISE,"<T-NAME> seems(s) completely unaffected by the "+((tool==null)?"mental attack":tool)+" from <S-NAME>.");
			affect.tagModified(true);
		}
		else
		if(affect.amITarget(this)&&(affect.targetCode()==Affect.STRIKE_GAS))
		{
			String tool=null;
			if(affect.tool()!=null)
			{
			    if(affect.tool() instanceof Ability)
					tool=((Ability)affect.tool()).name();
			}
			location().show(affect.source(),this,Affect.VISUAL_WNOISE,"<T-NAME> seems(s) completely unaffected by the "+((tool==null)?"gas attack":tool)+" from <S-NAME>.");
			affect.tagModified(true);
		}
	}
}
