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
   Copyright 2000-2013 Bo Zimmerman

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
public class GreatAmphibian extends StdRace
{
	public String ID(){	return "GreatAmphibian"; }
	public String name(){ return "Great Amphibian"; }
	public int shortestMale(){return 20;}
	public int shortestFemale(){return 25;}
	public int heightVariance(){return 5;}
	public int lightestWeight(){return 155;}
	public int weightVariance(){return 40;}
	public long forbiddenWornBits(){return ~(Wearable.WORN_EYES);}
	public String racialCategory(){return "Amphibian";}
	protected static Vector<RawMaterial> resources=new Vector<RawMaterial>();
	private String[]racialAbilityNames={"Skill_Swim"};
	private int[]racialAbilityLevels={1};
	private int[]racialAbilityProficiencies={100};
	private boolean[]racialAbilityQuals={false};
	protected String[] racialAbilityNames(){return racialAbilityNames;}
	protected int[] racialAbilityLevels(){return racialAbilityLevels;}
	protected int[] racialAbilityProficiencies(){return racialAbilityProficiencies;}
	protected boolean[] racialAbilityQuals(){return racialAbilityQuals;}
	public int[] getBreathables() { return breatheAirWaterArray; }
	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,0 ,1 ,0 ,0 ,0 ,1 ,0 ,0 ,0 ,2 ,1 ,0 ,1 ,0 };
	public int[] bodyMask(){return parts;}

	private int[] agingChart={0,2,4,6,8,10,12,14,16};
	public int[] getAgingChart(){return agingChart;}
	
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,13);
	}
	public String arriveStr()
	{
		return "shuffles in";
	}
	public String leaveStr()
	{
		return "shuffles";
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName("some sharp teeth");
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
		}
		return naturalWeapon;
	}
	
	public String makeMobName(char gender, int age)
	{
		switch(age)
		{
			case Race.AGE_INFANT:
			case Race.AGE_TODDLER:
				return name().toLowerCase()+" tadpole";
			case Race.AGE_CHILD:
				return name().toLowerCase()+" polliwog";
			default : 
				return super.makeMobName('N', age);
		}
	}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		final MOB mob=(MOB)affected;
		final Room R=mob.location();
		if((R!=null)
		&&((R.domainType()==Room.DOMAIN_INDOORS_WATERSURFACE)
			||(R.domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE)
			||(R.domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
			||(R.domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			||((RawMaterial.CODES.GET(R.getAtmosphere())&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LIQUID)))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SWIMMING);
	}
	
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<15;i++)
				resources.addElement(makeResource
				("some "+name().toLowerCase(),RawMaterial.RESOURCE_FISH));
				for(int i=0;i<5;i++)
				resources.addElement(makeResource
				("a "+name().toLowerCase()+" hide",RawMaterial.RESOURCE_HIDE));
				resources.addElement(makeResource
				("some "+name().toLowerCase()+" blood",RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
