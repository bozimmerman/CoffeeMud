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
   Copyright 2004-2020 Bo Zimmerman

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
public class Prop_RideEnabler extends Prop_HaveEnabler
{
	@Override
	public String ID()
	{
		return "Prop_RideEnabler";
	}

	@Override
	public String name()
	{
		return "Granting skills when ridden";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_MOBS;
	}

	protected Vector<Rider> lastRiders=new Vector<Rider>();

	@Override
	public String accountForYourself()
	{
		return spellAccountingsWithMask("Grants "," to those mounted.");
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_MOUNT;
	}

	@Override
	public void setMiscText(final String newText)
	{
		super.setMiscText(newText);
		lastRiders=new Vector<Rider>();
	}

	@Override
	public void affectPhyStats(final Physical host, final PhyStats affectableStats)
	{
		if(processing)
			return;
		try
		{
			processing=true;
			if(affected instanceof Rideable)
			{
				final Rideable RI=(Rideable)affected;
				for(int r=0;r<RI.numRiders();r++)
				{
					final Rider R=RI.fetchRider(r);
					if(R instanceof MOB)
					{
						final MOB M=(MOB)R;
						if((!lastRiders.contains(M))&&(RI.amRiding(M)))
						{
							if(addMeIfNeccessary(M,M,maxTicks))
								lastRiders.add(M);
						}
					}
				}
				for(int i=lastRiders.size()-1;i>=0;i--)
				{
					final MOB M=(MOB)lastRiders.elementAt(i);
					if(!RI.amRiding(M))
					{
						removeMyAffectsFrom(M);
						while(lastRiders.contains(M))
							lastRiders.removeElement(M);
					}
				}
			}
		}
		finally
		{
			processing=false;
		}
	}

	@Override
	public String getStat(final String code)
	{
		if(code == null)
			return "";
		if(code.equalsIgnoreCase("STAT-LEVEL"))
		{
			int level = 0;
			for(final Pair<Ability,Integer> p : this.getMySpellsV())
			{
				final Ability A=p.first;
				if(A!=null)
				{
					final int mul=1;
					level += (mul*CMLib.ableMapper().lowestQualifyingLevel(A.ID()));
				}
			}
			return ""+level;
		}
		else
		if(code.toUpperCase().startsWith("STAT-"))
			return "";
		return super.getStat(code);
	}

}
