package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Doll extends StdRace
{
	public Doll()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
	}
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStrength(5);
		affectableStats.setDexterity(5);
		affectableStats.setIntelligence(13);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		int weightModifier = Math.abs(randomizer.nextInt() % 5);
		mob.baseEnvStats().setWeight(3+weightModifier);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
}
