package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GiantWorm extends StdRace
{
	public String ID(){	return "GiantWorm"; }
	public String name(){ return "Giant Worm"; }
	protected int shortestMale(){return 22;}
	protected int shortestFemale(){return 22;}
	protected int heightVariance(){return 0;}
	protected int lightestWeight(){return 180;}
	protected int weightVariance(){return 20;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public String arriveStr()
	{
		return "shuffles in";
	}
	public String leaveStr()
	{
		return "shuffles";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a nasty maw");
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
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
				("some "+name().toLowerCase()+" guts",EnvResource.RESOURCE_MEAT));
			}
		}
		return resources;
	}
}