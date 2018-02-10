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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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
public class Dragon extends StdRace
{
	@Override
	public String ID()
	{
		return "Dragon";
	}

	private final static String localizedStaticName = CMLib.lang().L("Dragon");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 84;
	}

	@Override
	public int shortestFemale()
	{
		return 78;
	}

	@Override
	public int heightVariance()
	{
		return 80;
	}

	@Override
	public int lightestWeight()
	{
		return 2000;
	}

	@Override
	public int weightVariance()
	{
		return 500;
	}

	@Override
	public long forbiddenWornBits()
	{
		return Wearable.WORN_WIELD | Wearable.WORN_WAIST | Wearable.WORN_BACK | Wearable.WORN_ABOUT_BODY | Wearable.WORN_FEET | Wearable.WORN_HANDS;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Dragon");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "Dragonbreath", "WingFlying", "Skill_Buck" };
	private final int[]		racialAbilityLevels			= { 5, 1, 1 };
	private final int[]		racialAbilityProficiencies	= { 100, 100, 100 };
	private final boolean[]	racialAbilityQuals			= { false, false, false };
	private final String[]	racialAbilityParms			= { "", "", "" };

	@Override
	protected String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	protected int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	protected int[] racialAbilityProficiencies()
	{
		return racialAbilityProficiencies;
	}

	@Override
	protected boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	private final String[]	culturalAbilityNames			= { "Draconic" };
	private final int[]		culturalAbilityProficiencies	= { 100 };

	@Override
	public String[] culturalAbilityNames()
	{
		return culturalAbilityNames;
	}

	@Override
	public int[] culturalAbilityProficiencies()
	{
		return culturalAbilityProficiencies;
	}

	@Override
	public boolean useRideClass()
	{
		return true;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,2 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 5, 20, 110, 325, 500, 850, 950, 1050 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public int availabilityCode()
	{
		return Area.THEME_FANTASY | Area.THEME_SKILLONLYMASK;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED);
		if((affectableStats.disposition() & (PhyStats.IS_SITTING|PhyStats.IS_SLEEPING))==0)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		final int bump = affectedMOB.isMonster()?15:5;
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+bump);
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+bump);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)+bump);
	}

	@Override
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
		{
			naturalWeapon=CMClass.getWeapon("StdWeapon");
			naturalWeapon.setName(L("huge talons"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
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
				return name().toLowerCase()+" hatchling";
			case Race.AGE_CHILD:
			default:
				return super.makeMobName(gender, age);
		}
	}

	@Override
	public String healthText(MOB viewer, MOB mob)
	{
		final double pct=(CMath.div(mob.curState().getHitPoints(),mob.maxState().getHitPoints()));

		if(pct<.10)
			return L("^r@x1^r is raging in bloody pain!^N",mob.name(viewer));
		else
		if(pct<.20)
			return L("^r@x1^r is covered in blood.^N",mob.name(viewer));
		else
		if(pct<.30)
			return L("^r@x1^r is bleeding badly from lots of wounds.^N",mob.name(viewer));
		else
		if(pct<.50)
			return L("^y@x1^y has some bloody wounds and gashed scales.^N",mob.name(viewer));
		else
		if(pct<.60)
			return L("^p@x1^p has a few bloody wounds.^N",mob.name(viewer));
		else
		if(pct<.70)
			return L("^p@x1^p is cut and bruised heavily.^N",mob.name(viewer));
		else
		if(pct<.90)
			return L("^g@x1^g has a few bruises and scratched scales.^N",mob.name(viewer));
		else
		if(pct<.99)
			return L("^g@x1^g has a few small bruises.^N",mob.name(viewer));
		else
			return L("^c@x1^c is in perfect health.^N",mob.name(viewer));
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a @x1 claw",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				for(int i=0;i<100;i++)
				{
					resources.addElement(makeResource
					(L("a strip of @x1 scales",name().toLowerCase()),RawMaterial.RESOURCE_DRAGONSCALES));
				}
				for(int i=0;i<50;i++)
				{
					resources.addElement(makeResource
					(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_DRAGONMEAT));
				}
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_DRAGONBLOOD));
			}
		}
		return resources;
	}
}
