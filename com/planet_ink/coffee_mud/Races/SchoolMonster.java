package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class SchoolMonster extends StdRace
{
	public SchoolMonster()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="School Monster";
	}
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStrength(1);
		affectableStats.setDexterity(1);
		affectableStats.setIntelligence(3);
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		int weightModifier = Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + Math.abs(randomizer.nextInt() % 10) + 4;
		mob.baseEnvStats().setWeight(30+weightModifier);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("sharp claws");
			naturalWeapon.setWeaponType(Weapon.TYPE_SLASHING);
		}
		return naturalWeapon;
	}
}
