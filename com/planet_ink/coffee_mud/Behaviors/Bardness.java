package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bardness extends CombatAbilities
{
	public String ID(){return "Bardness";}
	private int tickDown=0;
	public Behavior newInstance()
	{
		return new Bardness();
	}

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		if(!mob.baseCharStats().getCurrentClass().ID().equals("Bard"))
		{
			mob.baseCharStats().setCurrentClass("Bard");
			mob.baseCharStats().setClassLevel("Bard",mob.envStats().level());
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}
}