package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2001-2018 Bo Zimmerman

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
public class Song_Lullibye extends Song
{
	@Override
	public String ID()
	{
		return "Song_Lullibye";
	}

	private final static String localizedName = CMLib.lang().L("Lullaby");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	boolean asleep=false;

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(invoker==null)
			return;
		if(affected==invoker)
			return;
		if(asleep)
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_SLEEPING);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final MOB mob=(MOB)affected;
		if(mob==null)
			return true;
		if(mob==invoker)
			return true;
		final boolean oldasleep=asleep;
		if(CMLib.dice().rollPercentage()>(50-(2*getXLEVELLevel(invoker()))))
			asleep=true;
		else
			asleep=false;
		if(asleep!=oldasleep)
		{
			if(oldasleep)
			{
				if(CMLib.flags().isSleeping(mob))
					mob.phyStats().setDisposition(mob.phyStats().disposition()-PhyStats.IS_SLEEPING);
				mob.location().show(mob,null,CMMsg.MSG_QUIETMOVEMENT,L("<S-NAME> wake(s) up."));
			}
			else
			{
				mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> fall(s) asleep."));
				mob.phyStats().setDisposition(mob.phyStats().disposition()|PhyStats.IS_SLEEPING);
			}
		}

		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.source()==invoker)
			return true;

		if(msg.source()!=affected)
			return true;

		if((!msg.sourceMajor(CMMsg.MASK_ALWAYS))
		&&((msg.targetMinor()==CMMsg.TYP_STAND)||(msg.sourceMinor()==CMMsg.TYP_SIT))&&(asleep))
			return false;
		return true;
	}
}
