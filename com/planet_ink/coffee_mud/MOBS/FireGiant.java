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
public class FireGiant extends StdMOB
{

	public FireGiant()
	{
		super();
		Random randomizer = new Random(System.currentTimeMillis());

		Username="a Fire Giant";
		setDescription("A tall humanoid standing about 18 feet tall, 12 foot chest, coal black skin and fire red-orange hair.");
		setDisplayText("A Fire Giant ponders killing you.");
		setAlignment(0);
		setMoney(0);
		baseEnvStats.setWeight(6500 + Math.abs(randomizer.nextInt() % 1001));


		baseCharStats().setIntelligence(8 + Math.abs(randomizer.nextInt() % 3));
		baseCharStats().setStrength(20);
		baseCharStats().setDexterity(13);

		baseEnvStats().setDamage(20);
		baseEnvStats().setSpeed(1.0);
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(15);
		baseEnvStats().setArmor(-10);

		maxState.setHitPoints((Math.abs(randomizer.nextInt() % 12)*baseEnvStats().level()) + 17);

		addBehavior(new Aggressive());

		recoverMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	public Environmental newInstance()
	{
		return new FireGiant();
	}
	public boolean okAffect(Affect affect)
	{
		if((affect.target()==null)||(!(affect.target() instanceof MOB)))
			return true;

		MOB mob=(MOB)affect.target();
		if((affect.targetCode()==affect.STRIKE_FIRE)
		&&(mob.isMine(this)))
		{
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> appear(s) to be unaffected.");
			return false;
		}
		return true;
	}


}
