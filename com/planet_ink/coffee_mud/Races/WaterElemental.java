package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WaterElemental extends StdRace
{
	public String ID(){	return "WaterElemental"; }
	public String name(){ return "Water Elemental"; }
	protected int shortestMale(){return 64;}
	protected int shortestFemale(){return 60;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 400;}
	protected int weightVariance(){return 100;}
	protected long forbiddenWornBits(){return 0;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
		affectableStats.setStat(CharStats.SAVE_WATER,affectableStats.getStat(CharStats.SAVE_WATER)+100);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("an arm of ice");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
	
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is almost dry!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is dripping alot and is almost dried out.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is dripping alot and steaming massively.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y is dripping alot and steaming a lot.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y is dripping and steaming.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p is dripping and starting to steam.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is dripping more.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g is showing some dripping.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is showing small drips.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
					("a puddle of water",EnvResource.RESOURCE_FRESHWATER));
			}
		}
		return resources;
	}
}
