package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GreatFish extends StdRace
{
	public String ID(){	return "GreatFish"; }
	public String name(){ return "Great Fish"; }
	protected int shortestMale(){return 30;}
	protected int shortestFemale(){return 35;}
	protected int heightVariance(){return 10;}
	protected int lightestWeight(){return 55;}
	protected int weightVariance(){return 15;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_EYES;}
	public String racialCategory(){return "Amphibian";}
	protected static Vector resources=new Vector();
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,2 ,1 ,0 ,1 ,0 };
	public int[] bodyMask(){return parts;}
	
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
		affectableStats.setStat(CharStats.DEXTERITY,13);
	}
	public String arriveStr()
	{
		return "swims in";
	}
	public String leaveStr()
	{
		return "swims";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a heat butt");
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
				for(int i=0;i<8;i++)
				resources.addElement(makeResource
				("some "+name().toLowerCase(),EnvResource.RESOURCE_FISH));
				for(int i=0;i<5;i++)
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",EnvResource.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
