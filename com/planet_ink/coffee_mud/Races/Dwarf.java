package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Dwarf extends StdRace
{
	public String ID(){	return "Dwarf"; }
	public String name(){ return "Dwarf"; }
	public int shortestMale(){return 52;}
	public int shortestFemale(){return 48;}
	public int heightVariance(){return 8;}
	public int lightestWeight(){return 150;}
	public int weightVariance(){return 100;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Dwarf";}
	private String[]culturalAbilityNames={"Dwarven","Mining"};
	private int[]culturalAbilityProficiencies={100,50};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,5,40,125,188,250,270,290};
	public int[] getAgingChart(){return agingChart;}
	
	protected static Vector resources=new Vector();
	public int availabilityCode(){return Area.THEME_FANTASY;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INFRARED);
	}
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_CONSTITUTION,affectableStats.getStat(CharStats.STAT_CONSTITUTION)+1);
		affectableStats.setStat(CharStats.STAT_MAX_CONSTITUTION_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CONSTITUTION_ADJ)+1);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-1);
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-1);
		affectableStats.setStat(CharStats.STAT_SAVE_POISON,affectableStats.getStat(CharStats.STAT_SAVE_POISON)+10);
	}
	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			// Have to, since it requires use of special constructor
			Armor s1=CMClass.getArmor("GenShirt");
			s1.setName("a grey work tunic");
			s1.setDisplayText("a grey work tunic has been left here.");
			s1.setDescription("There are lots of little loops and folks for hanging tools about it.");
			s1.text();
			outfitChoices.addElement(s1);

			Armor s2=CMClass.getArmor("GenShoes");
			s2.setName("a pair of hefty work boots");
			s2.setDisplayText("some hefty work boots have been left here.");
			s2.setDescription("Thick and well worn boots with very tough souls.");
			s2.text();
			outfitChoices.addElement(s2);

			Armor p1=CMClass.getArmor("GenPants");
			p1.setName("some hefty work pants");
			p1.setDisplayText("some hefty work pants have been left here.");
			p1.setDescription("There are lots of little loops and folks for hanging tools about it.");
			p1.text();
			outfitChoices.addElement(p1);
			
			Armor s3=CMClass.getArmor("GenBelt");
			outfitChoices.addElement(s3);
		}
		return outfitChoices;
	}
	public Weapon myNaturalWeapon()
	{ return funHumanoidWeapon();	}

	public String healthText(MOB viewer, MOB mob)
	{
		double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.displayName(viewer) + "^r is nearly dead!^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is covered in blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is bleeding from cuts and gashes.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y has numerous wounds.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y has some wounds.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p has a few cuts.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p is cut.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g has some bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g is very winded.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g is slightly winded.^N";
		else
			return "^c" + mob.displayName(viewer) + "^c is in perfect health.^N";
	}
	public Vector myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" beard",RawMaterial.RESOURCE_FUR));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
