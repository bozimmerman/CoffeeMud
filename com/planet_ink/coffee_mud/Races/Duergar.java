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
   Copyright 2010-2024 Bo Zimmerman

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
public class Duergar extends Dwarf
{
	@Override
	public String ID()
	{
		return "Duergar";
	}

	public Duergar()
	{
		super();
		super.naturalAbilImmunities.add("Disease_Syphilis");
	}

	private final static String localizedStaticName = CMLib.lang().L("Duergar");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	private final String[]	racialEffectNames			= {  };
	private final int[]		racialEffectLevels			= {  };
	private final String[]	racialEffectParms			= {  };

	@Override
	protected String[] racialEffectNames()
	{
		return racialEffectNames;
	}

	@Override
	protected int[] racialEffectLevels()
	{
		return racialEffectLevels;
	}

	@Override
	protected String[] racialEffectParms()
	{
		return racialEffectParms;
	}

	private final String[]	culturalAbilityNames			= { "Dwarven", "Mining", "Undercommon", "Spell_Invisibility", "Spell_Grow" };
	private final int[]		culturalAbilityProficiencies	= { 100, 50, 25, 25, 25 };

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
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		final int senses=affectableStats.sensesMask();
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(senses|PhyStats.CAN_SEE_DARK);
	}

	@Override
	public int getXPAdjustment()
	{
		return -10;
	}

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,-3);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)+15);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)+5);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)-15);
		affectableStats.setStat(CharStats.STAT_SAVE_WATER,affectableStats.getStat(CharStats.STAT_SAVE_WATER)-10);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)-10);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.adjStat(CharStats.STAT_CHARISMA,+3);
		affectableStats.setStat(CharStats.STAT_SAVE_FIRE,affectableStats.getStat(CharStats.STAT_SAVE_FIRE)-15);
		affectableStats.setStat(CharStats.STAT_SAVE_MIND,affectableStats.getStat(CharStats.STAT_SAVE_MIND)-5);
		affectableStats.setStat(CharStats.STAT_SAVE_COLD,affectableStats.getStat(CharStats.STAT_SAVE_COLD)+15);
		affectableStats.setStat(CharStats.STAT_SAVE_WATER,affectableStats.getStat(CharStats.STAT_SAVE_WATER)+10);
		affectableStats.setStat(CharStats.STAT_SAVE_DISEASE,affectableStats.getStat(CharStats.STAT_SAVE_DISEASE)+10);
	}

	private static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();

	@Override
	public List<RawMaterial> myResources()
	{
		synchronized(resources)
		{
			if(resources.size()==0)
			{
				resources.addElement(makeResource
				(L("a @x1 beard",name().toLowerCase()),RawMaterial.RESOURCE_FUR,L("@x1 beard",name().toLowerCase())));
				resources.addElement(makeResource
				(L("some @x1 blood",name().toLowerCase()),RawMaterial.RESOURCE_BLOOD));
				resources.addElement(makeResource
				(L("a pile of @x1 bones",name().toLowerCase()),RawMaterial.RESOURCE_BONE));
			}
		}
		return resources;
	}
}
