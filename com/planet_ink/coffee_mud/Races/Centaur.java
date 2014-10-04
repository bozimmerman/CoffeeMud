package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
public class Centaur extends StdRace
{
	@Override public String ID(){	return "Centaur"; }
	@Override public String name(){ return "Centaur"; }
	@Override public int shortestMale(){return 60;}
	@Override public int shortestFemale(){return 60;}
	@Override public int heightVariance(){return 12;}
	@Override public int lightestWeight(){return 1150;}
	@Override public int weightVariance(){return 400;}
	@Override public long forbiddenWornBits(){return Wearable.WORN_WAIST|Wearable.WORN_LEGS|Wearable.WORN_FEET;}
	@Override public String racialCategory(){return "Equine";}
	@Override public boolean useRideClass() { return true; }
	private final String[]culturalAbilityNames={"Elvish","Foraging"};
	private final int[]culturalAbilityProficiencies={75,50};
	@Override public String[] culturalAbilityNames(){return culturalAbilityNames;}
	@Override public int[] culturalAbilityProficiencies(){return culturalAbilityProficiencies;}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };
	@Override public int[] bodyMask(){return parts;}

	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	@Override public int availabilityCode(){return Area.THEME_FANTASY;}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED);
	}
	
	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+1);
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectableStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)+1);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)+1);
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectableStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)+1);
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)-1);
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectableStats.getStat(CharStats.STAT_MAX_CHARISMA_ADJ)-1);
		affectableStats.setStat(CharStats.STAT_WISDOM,affectableStats.getStat(CharStats.STAT_WISDOM)-1);
		affectableStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,affectableStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)-1);
	}
	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("a pair of hooves"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
		}
		return naturalWeapon;
	}

	@Override
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
				return name().toLowerCase()+" foal";
			case Race.AGE_CHILD:
			case Race.AGE_YOUNGADULT:
				switch(gender)
				{
				case 'M': case 'm': return name().toLowerCase()+" colt";
				case 'F': case 'f': return name().toLowerCase()+" filly";
				default: return "young "+name().toLowerCase();
				}
			case Race.AGE_MATURE:
			case Race.AGE_MIDDLEAGED:
			default:
				switch(gender)
				{
				case 'M': case 'm': return "male "+name().toLowerCase();
				case 'F': case 'f': return "female "+name().toLowerCase();
				default: return name().toLowerCase();
				}
			case Race.AGE_OLD:
			case Race.AGE_VENERABLE:
			case Race.AGE_ANCIENT:
				switch(gender)
				{
				case 'M': case 'm': return "old male "+name().toLowerCase();
				case 'F': case 'f': return "old female "+name().toLowerCase();
				default: return "old "+name().toLowerCase();
				}
		}
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return "^r" + mob.name(viewer) + "^r is hovering on deaths door!^N";
		else
		if(pct<.20)
			return "^r" + mob.name(viewer) + "^r is covered in blood and matted hair.^N";
		else
		if(pct<.30)
			return "^r" + mob.name(viewer) + "^r is bleeding badly from lots of wounds.^N";
		else
		if(pct<.40)
			return "^y" + mob.name(viewer) + "^y has large patches of bloody matted fur.^N";
		else
		if(pct<.50)
			return "^y" + mob.name(viewer) + "^y has some bloody matted fur.^N";
		else
		if(pct<.60)
			return "^p" + mob.name(viewer) + "^p has a lot of cuts and gashes.^N";
		else
		if(pct<.70)
			return "^p" + mob.name(viewer) + "^p has a few cut patches.^N";
		else
		if(pct<.80)
			return "^g" + mob.name(viewer) + "^g has a cut patch of fur.^N";
		else
		if(pct<.90)
			return "^g" + mob.name(viewer) + "^g has some disheveled fur.^N";
		else
		if(pct<.99)
			return "^g" + mob.name(viewer) + "^g has some misplaced hairs.^N";
		else
			return "^c" + mob.name(viewer) + "^c is in perfect health.^N";
	}
	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(""+name().toLowerCase()+" mane",RawMaterial.RESOURCE_FUR));
				for(int i=0;i<2;i++)
					resources.addElement(makeResource
					("a strip of "+name().toLowerCase()+" leather",RawMaterial.RESOURCE_LEATHER));
				resources.addElement(makeResource
				("a pound of "+name().toLowerCase()+" meat",RawMaterial.RESOURCE_BEEF));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				("a pile of "+name().toLowerCase()+" bones",RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
