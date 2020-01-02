package com.planet_ink.coffee_mud.Abilities.Diseases;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Disease_Anemia extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_Anemia";
	}

	private final static String localizedName = CMLib.lang().L("Anemia");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Anemia)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int spreadBitmap()
	{
		return DiseaseAffect.SPREAD_CONSUMPTION;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 2;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return getTicksPerDay() * CMLib.dice().roll(1, 4, 3);
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 20;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("You feel healthier.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> feel(s) a little weak and slow.^?");
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		return true;
	}

	@Override
	public void affectPhyStats(final Physical E, final PhyStats stats)
	{
		super.affectPhyStats(E,stats);
	}

	@Override
	public void affectCharStats(final MOB E, final CharStats stats)
	{
		super.affectCharStats(E,stats);
		stats.setStat(CharStats.STAT_DEXTERITY,stats.getStat(CharStats.STAT_DEXTERITY)-3);
		if(stats.getStat(CharStats.STAT_DEXTERITY) < 1)
			stats.setStat(CharStats.STAT_DEXTERITY,1);
		stats.setStat(CharStats.STAT_STRENGTH,stats.getStat(CharStats.STAT_STRENGTH)-3);
		if(stats.getStat(CharStats.STAT_STRENGTH) < 1)
			stats.setStat(CharStats.STAT_STRENGTH,1);
	}
}

