package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GreatBird extends StdRace
{
	public GreatBird()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Great Bird";
	}
	public boolean playerSelectable(){return false;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|Sense.IS_FLYING);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStrength(8);
		affectableStats.setDexterity(10);
		affectableStats.setIntelligence(4);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());

		int weightModifier = Math.abs(randomizer.nextInt() % 3);
		mob.baseEnvStats().setWeight(2+weightModifier);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("some sharp talons");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
}
