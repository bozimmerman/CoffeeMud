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
   Copyright 2004-2018 Bo Zimmerman

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
public class Tabaxi extends GreatCat
{
	@Override
	public String ID()
	{
		return "Tabaxi";
	}

	private final static String localizedStaticName = CMLib.lang().L("Tabaxi");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 69;
	}

	@Override
	public int shortestFemale()
	{
		return 69;
	}

	@Override
	public int heightVariance()
	{
		return 8;
	}

	@Override
	public int lightestWeight()
	{
		return 120;
	}

	@Override
	public int weightVariance()
	{
		return 80;
	}

	@Override
	public long forbiddenWornBits()
	{
		return 0;
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Feline");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "Skill_Hide", "Skill_Sneak" };
	private final int[]		racialAbilityLevels			= { 4, 4 };
	private final int[]		racialAbilityProficiencies	= { 50, 50 };
	private final boolean[]	racialAbilityQuals			= { false, false };
	private final String[]	racialAbilityParms			= { "", "" };

	@Override
	public String[] racialAbilityNames()
	{
		return racialAbilityNames;
	}

	@Override
	public int[] racialAbilityLevels()
	{
		return racialAbilityLevels;
	}

	@Override
	public int[] racialAbilityProficiencies()
	{
		return racialAbilityProficiencies;
	}

	@Override
	public boolean[] racialAbilityQuals()
	{
		return racialAbilityQuals;
	}

	@Override
	public String[] racialAbilityParms()
	{
		return racialAbilityParms;
	}

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
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
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_INFRARED); //would like to turn this darkvison if it is okay with Zac.
		affectableStats.setSpeed(affectableStats.speed() * 2.0);
	}

	@Override
	public void affectCharStats(MOB affectedMOB, CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectableStats.getStat(CharStats.STAT_DEXTERITY)+3);
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectableStats.getStat(CharStats.STAT_MAX_DEXTERITY_ADJ)+3);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectableStats.getStat(CharStats.STAT_STRENGTH)+3);
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectableStats.getStat(CharStats.STAT_MAX_STRENGTH_ADJ)+3);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectableStats.getStat(CharStats.STAT_INTELLIGENCE)-4);
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectableStats.getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ)-4);
		affectableStats.setStat(CharStats.STAT_WISDOM,affectableStats.getStat(CharStats.STAT_WISDOM)-2);
		affectableStats.setStat(CharStats.STAT_MAX_WISDOM_ADJ,affectableStats.getStat(CharStats.STAT_MAX_WISDOM_ADJ)-2);
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a set of @x1 fangs",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
				for(int i=0;i<5;i++)
				{
					resources.addElement(makeResource
					(L("a strip of @x1 leather",name().toLowerCase()),RawMaterial.RESOURCE_LEATHER));
				}
				for(int i=0;i<2;i++)
				{
					resources.addElement(makeResource
					(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_BEEF));
				}
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
