package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Bull extends Buffalo
{
	public String ID(){	return "Bull"; }
	public String name(){ return "Bull"; }
	protected int shortestMale(){return 48;}
	protected int shortestFemale(){return 48;}
	protected int heightVariance(){return 6;}
	protected int lightestWeight(){return 350;}
	protected int weightVariance(){return 100;}
	protected static Vector resources=new Vector();
	public String racialCatagory(){return "Bovine";}
	
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a pair of deadly horns");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,19);
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a pair of "+name().toLowerCase()+" horns",EnvResource.RESOURCE_BONE));
				for(int i=0;i<10;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" leather",EnvResource.RESOURCE_LEATHER));
				for(int i=0;i<7;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}