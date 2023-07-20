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
   Copyright 2023-2023 Bo Zimmerman

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
public class Ghast extends Undead
{
	@Override
	public String ID()
	{
		return "Ghast";
	}

	private final static String localizedStaticName = CMLib.lang().L("Ghast");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	private final String[]	racialAbilityNames			= { "Paralysis" };
	private final int[]		racialAbilityLevels			= { 1 };
	private final int[]		racialAbilityProficiencies	= { 100 };
	private final boolean[]	racialAbilityQuals			= { false };
	private final String[]	racialAbilityParms			= { "" };

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

	private final String[]	racialEffectNames			= { "Spell_CauseStink" };
	private final int[]		racialEffectLevels			= { 1 };
	private final String[]	racialEffectParms			= { "" };

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

	@Override
	public void affectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMOB, affectableStats);
		affectableStats.setRacialStat(CharStats.STAT_STRENGTH,20);
		affectableStats.setRacialStat(CharStats.STAT_DEXTERITY,15);
		affectableStats.setRacialStat(CharStats.STAT_CHARISMA,2);
	}

	@Override
	public void unaffectCharStats(final MOB affectedMOB, final CharStats affectableStats)
	{
		super.unaffectCharStats(affectedMOB, affectableStats);
		affectableStats.setStat(CharStats.STAT_STRENGTH,affectedMOB.baseCharStats().getStat(CharStats.STAT_STRENGTH));
		affectableStats.setStat(CharStats.STAT_MAX_STRENGTH_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_STRENGTH_ADJ));
		affectableStats.setStat(CharStats.STAT_DEXTERITY,affectedMOB.baseCharStats().getStat(CharStats.STAT_DEXTERITY));
		affectableStats.setStat(CharStats.STAT_MAX_DEXTERITY_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_DEXTERITY_ADJ));
		affectableStats.setStat(CharStats.STAT_CHARISMA,affectedMOB.baseCharStats().getStat(CharStats.STAT_CHARISMA));
		affectableStats.setStat(CharStats.STAT_MAX_CHARISMA_ADJ,affectedMOB.baseCharStats().getStat(CharStats.STAT_MAX_CHARISMA_ADJ));
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}

}
