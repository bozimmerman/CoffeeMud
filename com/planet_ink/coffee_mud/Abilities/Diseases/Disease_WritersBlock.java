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
   Copyright 2003-2018 Bo Zimmerman

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

public class Disease_WritersBlock extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_WritersBlock";
	}

	private final static String localizedName = CMLib.lang().L("Writers Block");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Writers Block)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
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
	protected int DISEASE_TICKS()
	{
		final long max=CMProps.getTicksPerMudHour()
					* CMLib.time().globalClock().getHoursInDay()
					* CMLib.time().globalClock().getDaysInMonth();
		return CMLib.dice().roll(1, (int)max, 0);
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 100;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("You feel less uninspired.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> come(s) down with writers block.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public int difficultyLevel()
	{
		return 7;
	}

	protected boolean ringing=false;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		return true;
	}

	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.source()==affected)
		&&((msg.sourceMinor()==CMMsg.TYP_WRITE)
			||(msg.sourceMinor()==CMMsg.TYP_REWRITE)))
		{
			msg.source().tell(L("You are too uninspired to write anything."));
			return false;
		}
		return true;
	}
}
