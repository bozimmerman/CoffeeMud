package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Giant extends StdRace
{
	protected static Vector resources=new Vector();
	public Giant()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name=myID;
		// inches
		shortestMale=84;
		shortestFemale=80;
		heightVariance=24;
		// pounds
		lightestWeight=300;
		weightVariance=200;
		forbiddenWornBits=0;
	}
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
				("some "+name.toLowerCase()+" hairs",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("a strip of "+name.toLowerCase()+" hide",EnvResource.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name.toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
