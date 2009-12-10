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
public class Halfling extends StdRace
{
	public String ID(){	return "Halfling"; }
	public String name(){ return "Halfling"; }
	public int shortestMale(){return 40;}
	public int shortestFemale(){return 36;}
	public int heightVariance(){return 6;}
	public int lightestWeight(){return 80;}
	public int weightVariance(){return 50;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Halfling";}
	private String[]culturalAbilityNames={"Elvish","Cooking"};
	private int[]culturalAbilityProficiencies={25,75};
	public String[] culturalAbilityNames(){return culturalAbilityNames;}
	public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

	//                                an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,0 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,1,4,20,50,75,100,110,120};
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
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+1);
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectableStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)+1);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)-1);
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectableStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)-1);
		affectableStats.setStat(CharStats.STAT_SAVE_PARALYSIS,affectableStats.getStat(CharStats.STAT_SAVE_PARALYSIS)+10);
        affectableStats.setStat(CharStats.STAT_SAVE_DETECTION,affectableStats.getStat(CharStats.STAT_SAVE_DETECTION)+10);
	}
	
	public Vector outfit(MOB myChar)
	{
		if(outfitChoices==null)
		{
			outfitChoices=new Vector();
			// Have to, since it requires use of special constructor
			Armor s1=CMClass.getArmor("GenShirt");
			s1.setName("a small tunic");
			s1.setDisplayText("a small tunic is folded neatly here.");
			s1.setDescription("It is a small but nicely made button-up tunic.");
			s1.text();
			outfitChoices.addElement(s1);
			Armor p1=CMClass.getArmor("GenPants");
			p1.setName("some small pants");
			p1.setDisplayText("some small pants lie here.");
			p1.setDescription("They appear to be for a dimunitive person, and extend barely past the knee at that.");
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
			return "^r" + mob.displayName(viewer) + "^r has very little life left.^N";
		else
		if(pct<.20)
			return "^r" + mob.displayName(viewer) + "^r is covered in small streams of blood.^N";
		else
		if(pct<.30)
			return "^r" + mob.displayName(viewer) + "^r is bleeding badly from lots of small wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.displayName(viewer) + "^y has numerous bloody wounds and small gashes.^N";
		else
		if(pct<.50)
			return "^y" + mob.displayName(viewer) + "^y has some bloody wounds and small gashes.^N";
		else
		if(pct<.60)
			return "^p" + mob.displayName(viewer) + "^p has a few small bloody wounds.^N";
		else
		if(pct<.70)
			return "^p" + mob.displayName(viewer) + "^p is cut and bruised in small places.^N";
		else
		if(pct<.80)
			return "^g" + mob.displayName(viewer) + "^g has some small cuts and bruises.^N";
		else
		if(pct<.90)
			return "^g" + mob.displayName(viewer) + "^g has a few bruises and small scratches.^N";
		else
		if(pct<.99)
			return "^g" + mob.displayName(viewer) + "^g has a few small bruises.^N";
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
				("a pair of hairy "+name().toLowerCase()+" feet",RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
