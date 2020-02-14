package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2012-2020 Bo Zimmerman

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
public class Prop_RoomLit extends Property
{
	@Override
	public String ID()
	{
		return "Prop_RoomLit";
	}

	@Override
	public String name()
	{
		return "Lighting Property";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return true;
	}

	@Override
	public String accountForYourself()
	{
		return "Always Lit";
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	protected boolean setup = false;
	protected int[] hoursOfOperation = null;

	protected boolean isItLit()
	{
		final Physical affected = this.affected;
		if(affected == null)
			return false;
		if(!setup)
		{
			hoursOfOperation = null;
			setup = true;
			if(text().length()>0)
			{
				final String hrStr = CMParms.getParmStr(text(), "HOURS", "").trim();
				if(hrStr.length()>0)
				{
					final Set<Integer> finalV=new TreeSet<Integer>();
					for(final String hr : CMParms.parseCommas(hrStr, true))
					{
						if(CMath.isInteger(hr))
							finalV.add(Integer.valueOf(CMath.s_int(hr)));
					}
					if(finalV.size()>0)
					{
						final int[] finalArray = new int[finalV.size()];
						int dex=0;
						for(final Integer I : finalV)
							finalArray[dex++] = I.intValue();
						Arrays.sort(finalArray);
						hoursOfOperation = finalArray;
					}
				}
			}
		}
		if(hoursOfOperation != null)
		{
			final TimeClock C=CMLib.time().localClock(affected);
			return CMParms.contains(hoursOfOperation, C.getHourOfDay());
		}
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(CMLib.flags().isInDark(affected) && isItLit())
			affectableStats.setDisposition(affectableStats.disposition()-PhyStats.IS_DARK);
	}
}
