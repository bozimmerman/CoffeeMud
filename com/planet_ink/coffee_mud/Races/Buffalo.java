package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Buffalo extends Cow
{
	protected static Vector resources=new Vector();
	public Buffalo()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=48;
		shortestFemale=48;
		heightVariance=6;
		// pounds
		lightestWeight=350;
		weightVariance=100;
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a pair of deadly hoofs");
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<10;i++)
				resources.addElement(makeResource
				("a strip of "+name.toLowerCase()+" hide",EnvResource.RESOURCE_FUR));
				for(int i=0;i<5;i++)
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" meat",EnvResource.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name.toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
