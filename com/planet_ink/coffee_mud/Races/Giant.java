package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Giant extends StdRace
{
	public String ID(){	return "Giant"; }
	public String name(){ return "Giant"; }
	public int shortestMale(){return 84;}
	public int shortestFemale(){return 80;}
	public int heightVariance(){return 24;}
	public int lightestWeight(){return 300;}
	public int weightVariance(){return 200;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Giant-kin";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,5,40,125,188,250,270,290};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setPermaStat(CharStats.STRENGTH,18);
		affectableStats.setPermaStat(CharStats.DEXTERITY,7);
		affectableStats.setPermaStat(CharStats.INTELLIGENCE,7);
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
