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

import java.util.List;
import java.util.Vector;
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
public class Puppy extends Dog
{
	@Override
	public String ID()
	{
		return "Puppy";
	}

	private final static String localizedStaticName = CMLib.lang().L("Puppy");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	@Override
	public int shortestMale()
	{
		return 6;
	}

	@Override
	public int shortestFemale()
	{
		return 6;
	}

	@Override
	public int heightVariance()
	{
		return 3;
	}

	@Override
	public int lightestWeight()
	{
		return 7;
	}

	@Override
	public int weightVariance()
	{
		return 20;
	}

	@Override
	public long forbiddenWornBits()
	{
		return ~(Wearable.WORN_HEAD | Wearable.WORN_FEET | Wearable.WORN_NECK | Wearable.WORN_EARS | Wearable.WORN_EYES);
	}

	private final static String localizedStaticRacialCat = CMLib.lang().L("Canine");

	@Override
	public String racialCategory()
	{
		return localizedStaticRacialCat;
	}

	private final String[]	racialAbilityNames			= { "DogSpeak", "Adorable", "Skill_TrackFriend" };
	private final int[]		racialAbilityLevels			= { 1, 3, 1 };
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
	private static final int[] parts={0 ,2 ,2 ,1 ,1 ,0 ,0 ,1 ,4 ,4 ,1 ,0 ,1 ,1 ,1 ,0 };

	@Override
	public int[] bodyMask()
	{
		return parts;
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,6);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,11);
		affectableStats.setRacialStat(CharStats.STAT_INTELLIGENCE,1);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectedMOB.baseCharStats().getStat(CharStats.STAT_STRENGTH));
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_STRENGTH_ADJ));
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectedMOB.baseCharStats().getStat(CharStats.STAT_DEXTERITY));
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
		affectableStats.setStat(CharStats.STAT_INTELLIGENCE,affectedMOB.baseCharStats().getStat(CharStats.STAT_INTELLIGENCE));
		affectableStats.setStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_INTELLIGENCE_ADJ));
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		affectableMaxState.setMovement(affectableMaxState.getMovement()+50);
	}

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a @x1 nose",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("some @x1 hair",name().toLowerCase()),RawMaterial.RESOURCE_FUR));
				resources.addElement(makeResource
				(L("a pound of @x1 meat",name().toLowerCase()),RawMaterial.RESOURCE_MEAT));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
