package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Cub extends Bear
{
	public String ID(){	return "Cub"; }
	public String name(){ return "Cub"; }
	protected int shortestMale(){return 24;}
	protected int shortestFemale(){return 24;}
	protected int heightVariance(){return 6;}
	protected int lightestWeight(){return 45;}
	protected int weightVariance(){return 10;}
	public String racialCatagory(){return "Ursine";}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,10);
		affectableStats.setStat(CharStats.DEXTERITY,10);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a pair of claws");
			naturalWeapon.setWeaponType(Weapon.TYPE_SLASHING);
		}
		return naturalWeapon;
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" paws",EnvResource.RESOURCE_HIDE));
				resources.addElement(makeResource
				("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}