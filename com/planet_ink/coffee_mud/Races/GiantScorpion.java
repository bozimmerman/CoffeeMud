package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GiantScorpion extends StdRace
{
	public String ID(){	return "GiantScorpion"; }
	public String name(){ return "Giant Scorpion"; }
	protected int shortestMale(){return 32;}
	protected int shortestFemale(){return 34;}
	protected int heightVariance(){return 2;}
	protected int lightestWeight(){return 87;}
	protected int weightVariance(){return 97;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE;}
	public String racialCatagory(){return "Insect";}
	
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