package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Owl extends StdRace
{
	protected static Vector resources=new Vector();
	public Owl()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Owl";
		shortestMale=8;
		shortestFemale=8;
		heightVariance=5;
		lightestWeight=5;
		weightVariance=5;
		forbiddenWornBits=Integer.MAX_VALUE-Item.ON_HEAD;
	}
	public boolean playerSelectable(){return false;}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,3);
		affectableStats.setStat(CharStats.DEXTERITY,12);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
		affectableStats.setStat(CharStats.WISDOM,18);
	}
	public String arriveStr()
	{
		return "soars in";
	}
	public String leaveStr()
	{
		return "soars";
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
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name.toLowerCase()+" eyes",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" feathers",EnvResource.RESOURCE_FEATHERS));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" meat",EnvResource.RESOURCE_POULTRY));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name.toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
