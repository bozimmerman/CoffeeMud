package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Pig extends StdRace
{
	public String ID(){	return "Pig"; }
	public String name(){ return "Pig"; }
	protected int shortestMale(){return 12;}
	protected int shortestFemale(){return 12;}
	protected int heightVariance(){return 12;}
	protected int lightestWeight(){return 100;}
	protected int weightVariance(){return 60;}
	protected long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_EARS-Item.ON_EYES;}
	public String racialCategory(){return "Porcine";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,0 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,10);
		affectableStats.setStat(CharStats.DEXTERITY,4);
		affectableStats.setStat(CharStats.INTELLIGENCE,1);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a dangerous snout");
			naturalWeapon.setWeaponType(Weapon.TYPE_NATURAL);
		}
		return naturalWeapon;
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName() + "^r is squealing in bloody pain!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName() + "^y has numerous bloody wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName() + "^p has a bloody snout and some wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName() + "^p is cut and bruised heavily.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName() + "^g has some minor cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName() + "^g has a few bruises and scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName() + "^g has a few small pink bruises.^N";
		else
			return "^c" + mob.displayName() + "^c is in perfect health^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" snout",EnvResource.RESOURCE_MEAT));
				for(int i=0;i<5;i++)
					resources.addElement(makeResource
					("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_PORK));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
