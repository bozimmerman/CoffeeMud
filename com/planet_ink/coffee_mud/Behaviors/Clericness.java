package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Clericness extends CombatAbilities
{
	public String ID(){return "Clericness";}
	public Behavior newInstance()
	{
		return new Clericness();
	}
	
	boolean confirmedSetup=false;

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		if(!mob.baseCharStats().getCurrentClass().ID().equals("Cleric"))
		{
			mob.baseCharStats().setCurrentClass("Cleric");
			mob.baseCharStats().setClassLevel("Cleric",mob.envStats().level());
		}
		// now equip character...
		newCharacter(mob);
	}
}