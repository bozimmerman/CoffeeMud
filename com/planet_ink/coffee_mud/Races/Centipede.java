package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Centipede extends StdRace
{
	public String ID(){	return "Centipede"; }
	public String name(){ return "Centipede"; }
	protected int shortestMale(){return 1;}
	protected int shortestFemale(){return 1;}
	protected int heightVariance(){return 0;}
	protected int lightestWeight(){return 1;}
	protected int weightVariance(){return 0;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_TORSO;}
	public String racialCategory(){return "Insect";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={2 ,2 ,0 ,1 ,0 ,0 ,0 ,1 ,99,99,0 ,0 ,1 ,0 ,0 ,0 };
	public int[] bodyMask(){return parts;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,3);
		affectableStats.setStat(CharStats.DEXTERITY,3);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public String arriveStr()
	{
		return "crawls in";
	}
	public String leaveStr()
	{
		return "crawls";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a deadly maw");
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
				("some "+name().toLowerCase()+" legs",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
