package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Worm extends StdRace
{
	public String ID(){	return "Worm"; }
	public String name(){ return "Worm"; }
	protected int shortestMale(){return 2;}
	protected int shortestFemale(){return 2;}
	protected int heightVariance(){return 0;}
	protected int lightestWeight(){return 1;}
	protected int weightVariance(){return 0;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Worm";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,0 ,1 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}
	
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