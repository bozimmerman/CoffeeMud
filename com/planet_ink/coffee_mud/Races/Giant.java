package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Giant extends StdRace
{
	public String ID(){	return "Giant"; }
	public String name(){ return "Giant"; }
	protected int shortestMale(){return 84;}
	protected int shortestFemale(){return 80;}
	protected int heightVariance(){return 24;}
	protected int lightestWeight(){return 300;}
	protected int weightVariance(){return 200;}
	protected long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Giant-kin";}
	
	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}
	
	protected static Vector resources=new Vector();
	public boolean playerSelectable(){return false;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STRENGTH,18);
		affectableStats.setStat(CharStats.DEXTERITY,7);
		affectableStats.setStat(CharStats.INTELLIGENCE,7);
	}
	public String arriveStr()
	{
		return "thunders in";
	}
	public String leaveStr()
	{
		return "storms";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("a pair of gigantic fists");
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}
	
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is almost fallen!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of large wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has enormous bloody wounds and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some huge wounds and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few huge bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p has huge cuts and is heavily bruised.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some large cuts and huge bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has large bruises and scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small(?) bruises.^N";
		else
			return "^c" + mob.name() + "^c is in towering health^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" hairs",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("a strip of "+name().toLowerCase()+" hide",EnvResource.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
