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
   Copyright 2002-2025 Bo Zimmerman

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
public class WereBat extends Bat
{
	@Override
	public String ID()
	{
		return "WereBat";
	}

	private final static String localizedStaticName = CMLib.lang().L("WereBat");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 59;
	}

	@Override
	public int shortestFemale()
	{
		return 59;
	}

	@Override
	public int heightVariance()
	{
		return 12;
	}

	@Override
	public int lightestWeight()
	{
		return 80;
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

	private final static String localizedStaticRacialCat = CMLib.lang().L("Pteropine");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "RodentSpeak", "WingFlying", "Thief_AvoidTraps" };
	private final int[]		racialAbilityLevels			= { 1, 1, 1 };
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

	//  							  an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,2 ,2 ,1 ,2 ,2 ,1 ,0 ,1 ,1 ,1 ,2 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private final int[]	agingChart	= { 0, 4, 8, 12, 16, 20, 24, 28, 32 };

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
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+100);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)-100);
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(!CMLib.flags().isSleeping(affected))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_FLYING);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				for(int i=0;i<4;i++)
				{
					resources.addElement(makeResource
					(L("a strip of @x1 hair",name().toLowerCase()),RawMaterial.RESOURCE_FUR));
				}
				for(int i=0;i<2;i++)
				{
					resources.addElement(makeResource
					(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				}
				resources.addElement(makeResource
				(L("a pair of @x1 wings",name().toLowerCase()),RawMaterial.RESOURCE_HIDE));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
			}
		}
		return resources;
	}
}
