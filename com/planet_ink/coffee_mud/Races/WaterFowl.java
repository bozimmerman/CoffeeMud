package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WaterFowl extends StdRace
{
	public WaterFowl()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Water Fowl";
	}
	public boolean playerSelectable(){return false;}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,3);
		affectableStats.setStat(CharStats.DEXTERITY,3);
		affectableStats.setStat(CharStats.INTELLIGENCE,3);
	}
	public String arriveStr()
	{
		return "waddles in";
	}
	public String leaveStr()
	{
		return "waddles";
	}
	public void setWeight(MOB mob)
	{
		Random randomizer = new Random(System.currentTimeMillis());
		int weightModifier = Math.abs(randomizer.nextInt() % 10);
		mob.baseEnvStats().setWeight(10+weightModifier);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a menacing beak");
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}
	
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is hovering on deaths door!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood and matted feathers.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody matted feathers.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody matted feathers.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a lot of missing feathers.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p has a few missing feathers.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has a missing feather.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few feathers out of place.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a some ruffled features.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
}
