package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Fighterness extends CombatAbilities
{
	public String ID(){return "Fighterness";}
	public Behavior newInstance()
	{
		return new Fighterness();
	}

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		if(!mob.baseCharStats().getCurrentClass().ID().equals("Fighter"))
		{
			mob.baseCharStats().setCurrentClass("Fighter");
			mob.baseCharStats().setClassLevel("Fighter",mob.envStats().level());
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}
}