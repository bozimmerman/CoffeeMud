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
public class GreatCat extends StdRace
{
	public String ID(){	return "GreatCat"; }
	public String name(){ return "Great Cat"; }
	public int shortestMale(){return 18;}
	public int shortestFemale(){return 18;}
	public int heightVariance(){return 10;}
	public int lightestWeight(){return 100;}
	public int weightVariance(){return 60;}
	public long forbiddenWornBits(){return Integer.MAX_VALUE-Item.ON_HEAD-Item.ON_FEET-Item.ON_NECK-Item.ON_EARS-Item.ON_EYES;}
	public String racialCategory(){return "Feline";}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,2,4,7,15,20,21,22};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availability(){return Race.AVAILABLE_MAGICONLY;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setPermaStat(CharStats.STRENGTH,16);
		affectableStats.setPermaStat(CharStats.DEXTERITY,16);
		affectableStats.setPermaStat(CharStats.INTELLIGENCE,1);
	}
	public String arriveStr()
	{
		return "struts in";
	}
	public String leaveStr()
	{
		return "struts";
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("huge sharp claws");
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is down to one life!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in blood and matted hair.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has large patches of bloody matted hair.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some bloody matted hair.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a lot of cuts and gashes.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p has a few cut patches.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has a cut patch of fur.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has some disheveled hair.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has some misplaced hairs.^N";
		else
			return "^c" + mob.name() + "^c is in perfect health.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" claws",EnvResource.RESOURCE_BONE));
				for(int i=0;i<4;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" fur",EnvResource.RESOURCE_FUR));
				resources.addElement(makeResource
				("a pound of "+name().toLowerCase()+" meat",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
