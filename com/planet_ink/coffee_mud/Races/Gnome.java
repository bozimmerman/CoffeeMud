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
public class Gnome extends StdRace
{
	public String ID(){	return "Gnome"; }
	public String name(){ return "Gnome"; }
	public int shortestMale(){return 40;}
	public int shortestFemale(){return 36;}
	public int heightVariance(){return 6;}
	public int lightestWeight(){return 60;}
	public int weightVariance(){return 50;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Gnome";}
	private String[]culturalAbilityNames={"Gnomish","Digging"};
	private int[]culturalAbilityProfficiencies={100,50};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProfficiencies(){return culturalAbilityProfficiencies;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,5,40,100,150,200,230,260};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availability(){return Race.AVAILABLE_ALL;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.INTELLIGENCE,affectableStats.getStat(CharStats.INTELLIGENCE)+1);
		affectableStats.setStat(CharStats.MAX_INTELLIGENCE_ADJ,affectableStats.getStat(CharStats.MAX_INTELLIGENCE_ADJ)+1);
		affectableStats.setStat(CharStats.WISDOM,affectableStats.getStat(CharStats.WISDOM)-1);
		affectableStats.setStat(CharStats.MAX_WISDOM_ADJ,affectableStats.getStat(CharStats.MAX_WISDOM_ADJ)-1);
		affectableStats.setStat(CharStats.SAVE_MIND,affectableStats.getStat(CharStats.SAVE_MIND)+10);
	}
	public Vector outfit()
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			// Have to, since it requires use of special constructor
			Armor s1=CMClass.getArmor("GenShirt");
			s1.setName("a small patchy tunic");
			s1.setDisplayText("a small patchy tunic has been left here.");
			s1.setDescription("This small tunic is made of bits and pieces of many other shirts, it seems.  There are lots of tiny hidden compartments on it, and loops for hanging tools.");
			s1.text();
			outfitChoices.addElement(s1);

			Armor s2=CMClass.getArmor("GenShoes");
			s2.setName("a pair of small shoes");
			s2.setDisplayText("a pair of small shoes lie here.");
			s2.setDescription("This pair of small shoes appears to be a hodgepodge of materials and workmanship.");
			s2.text();
			outfitChoices.addElement(s2);

			Armor p1=CMClass.getArmor("GenPants");
			p1.setName("a pair of small patchy pants");
			p1.setDisplayText("a pair of small patchy pants lie here.");
			p1.setDescription("This pair of small pants is made of bits and pieces of many other pants, it seems.  There are lots of tiny hidden compartments on it, and loops for hanging tools.");
			p1.text();
			outfitChoices.addElement(p1);
		}
		return outfitChoices;
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}
	public String healthText(MOB mob)
	{
		double pct=(Util.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name() + "^r is curiously close to death.^N";
		else
		if(pct<.20)
			return "^r" + mob.name() + "^r is covered in excessive bloody wounds.^N";
		else
		if(pct<.30)
			return "^r" + mob.name() + "^r is bleeding badly from a plethora of small wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name() + "^y has numerous bloody wounds and unexpected gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.name() + "^y has some alarming wounds and small gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.name() + "^p has some small unwanted bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.name() + "^p is cut and bruised in strange places.^N";
		else
		if(pct<.80)
			return "^g" + mob.name() + "^g has some small cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.name() + "^g has a few bruises and interesting scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.name() + "^g has a few small curious bruises.^N";
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
				("a pair of "+name().toLowerCase()+" eyes",EnvResource.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",EnvResource.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",EnvResource.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
