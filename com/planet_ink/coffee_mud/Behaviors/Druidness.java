package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Druidness extends CombatAbilities
{
	boolean confirmedSetup=false;

	public Druidness()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}

	public Behavior newInstance()
	{
		return new Druidness();
	}

	public void startBehavior(Environmental forMe)
	{
		super.startBehavior(forMe);
		if(!(forMe instanceof MOB)) return;
		MOB mob=(MOB)forMe;
		if(!mob.baseCharStats().getMyClass().ID().equals("Druid"))
		{
			mob.baseCharStats().setMyClass(CMClass.getCharClass("Druid"));
			mob.recoverCharStats();
		}
		// now equip character...
		newCharacter(mob);
	}
}