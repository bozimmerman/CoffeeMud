package com.planet_ink.coffee_mud.Races;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Doll extends StdRace
{
	public String ID(){	return "Doll"; }
	public String name(){ return "Doll"; }
	public int shortestMale(){return 6;}
	public int shortestFemale(){return 6;}
	public int heightVariance(){return 3;}
	public int lightestWeight(){return 10;}
	public int weightVariance(){return 20;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Wood Golem";}
	public boolean fertile(){return false;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,0,0,0,0,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setPermaStat(CharStats.STRENGTH,5);
		affectableStats.setPermaStat(CharStats.DEXTERITY,5);
		affectableStats.setPermaStat(CharStats.INTELLIGENCE,13);
	}
	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOLEM);
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}

	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is nearly disassembled!^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in tears and cracks.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is broken badly with lots of tears.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous tears and gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some tears and gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has a few cracks.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is scratched heavily.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some minor scratches.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g is a bit disheveled.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g is no longer in perfect condition.^N";
		else
			return "^c" + mob.name() + "^c is in perfect condition^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" clothes",EnvResource.RESOURCE_COTTON));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" parts",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
