package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Sheep extends StdRace
{
	public String ID(){	return "Sheep"; }
	public String name(){ return "Sheep"; }
	protected int shortestMale(){return 36;}
	protected int shortestFemale(){return 36;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 50;}
	protected int weightVariance(){return 60;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET-Item.ON_NECK-Item.ON_EARS-Item.ON_EYES;}
	public String racialCatagory(){return "Ovine";}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,5);
		affectableStats.setStat(CharStats.DEXTERITY,1);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a pair of hooves");
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
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
			return "^r" + mob.name() + "^r is covered in blood and matted wool.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has large patches of bloody matted wool.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody matted wool.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a lot of cuts and gashes.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p has a few cut patches.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has a cut patch of wool.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has some disheveled wool.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has some misplaced wool.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<10;i++)
					resources.addElement(makeResource
					("some "+name().toLowerCase()+" wool",EnvResource.RESOURCE_WOOL));
				for(int i=0;i<3;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MUTTON));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}