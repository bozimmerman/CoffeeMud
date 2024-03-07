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
   Copyright 2023-2024 Bo Zimmerman

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
public class Wolverine extends Mustelid
{
	@Override
	public String ID()
	{
		return "Wolverine";
	}

	private final static String localizedStaticName = CMLib.lang().L("Wolverine");

	@Override
	public String name()
	{
		return localizedStaticName;
	}

	private final String[]	racialAbilityNames			= { "Skill_Swim", "Fighter_Berzerk", "WildTagTurf", "Skill_Burrow" };
	private final int[]		racialAbilityLevels			= { 1, 2, 3, 1 };
	private final int[]		racialAbilityProficiencies	= { 100, 75, 100, 75 };
	private final boolean[]	racialAbilityQuals			= { false, false, false, false };
	private final String[]	racialAbilityParms			= { "", "", "", "" };

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

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_SEE_DARK);
	}

	protected static Vector<RawMaterial>	resources	= new Vector<RawMaterial>();
	protected List<RawMaterial> privateResources() { return resources; }

}

