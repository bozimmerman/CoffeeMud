package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Scorpion extends StdRace
{
	public String ID(){	return "Scorpion"; }
	public String name(){ return "Scorpion"; }
	public int shortestMale(){return 4;}
	public int shortestFemale(){return 4;}
	public int heightVariance(){return 2;}
	public int lightestWeight(){return 5;}
	public int weightVariance(){return 5;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCategory(){return "Arachnid";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,0 ,2 ,2 ,1 ,8 ,8 ,0 ,0 ,1 ,0 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,5);
		affectableStats.setStat(CharStats.DEXTERITY,5);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
		affectableStats.setStat(CharStats.SAVE_POISON,affectableStats.getStat(CharStats.SAVE_POISON)+100);
	}
	public String arriveStr()
	{
		return "creeps in";
	}
	public String leaveStr()
	{
		return "creeps";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a nasty stinger");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
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
				("some "+name().toLowerCase()+" pincers",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}