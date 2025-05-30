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
   Copyright 2016-2025 Bo Zimmerman

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
public class Walrus extends Seal
{
	@Override
	public String ID()
	{
		return "Walrus";
	}

	private final static String localizedStaticName = CMLib.lang().L("Walrus");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 70;
	}

	@Override
	public int shortestFemale()
	{
		return 65;
	}

	@Override
	public int heightVariance()
	{
		return 10;
	}

	@Override
	public int lightestWeight()
	{
		return 800;
	}

	@Override
	public int weightVariance()
	{
		return 500;
	}

	@Override
	public int[] getBreathables()
	{
		return breatheAirWaterArray;
	}

	private final String[]					racialAbilityNames			= { "Aquan", "Skill_Swim", "Skill_AutoSwim", "Gore" };
	private final int[]						racialAbilityLevels			= { 1,1,1,9 };
	private final int[]						racialAbilityProficiencies	= { 100,100,100,100 };
	private final boolean[]					racialAbilityQuals			= { false,false,false,false };
	private final String[]					racialAbilityParms			= { "", "", "", "" };

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

	private final int[]	agingChart	= { 0, 2, 5, 8, 10, 15, 25, 28, 30 };

	@Override
	public int[] getAgingChart()
	{
		return agingChart;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		//super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,17);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectedMOB.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE));
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ));
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectedMOB.baseCharStats().getStat(CharStats.STAT_STRENGTH));
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_STRENGTH_ADJ));
	}

	@Override
	public String arriveStr()
	{
		return "flops in";
	}

	@Override
	public String leaveStr()
	{
		return "flops";
	}

	@Override
	public Weapon[] getNaturalWeapons()
	{
		if(this.naturalWeaponChoices.length==0)
		{
			final Weapon naturalWeapon=CMClass.getWeapon("GenWeapon");
			naturalWeapon.setName(L("a pair of tusks"));
			naturalWeapon.setMaterial(RawMaterial.RESOURCE_BONE);
			naturalWeapon.setUsesRemaining(1000);
			naturalWeapon.setWeaponDamageType(Weapon.TYPE_PIERCING);
			this.naturalWeaponChoices = new Weapon[] { naturalWeapon };
		}
		return super.getNaturalWeapons();
	}

	private static Vector<RawMaterial>	resources = new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<18;i++)
				{
					resources.addElement(makeResource
					(L("some @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_FISH));
				}
				for(int i=0;i<15;i++)
				{
					resources.addElement(makeResource
					(L("a thick @x1 hide",name().toLowerCase()),RawMaterial.RESOURCE_HIDE));
				}
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
