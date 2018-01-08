package com.planet_ink.coffee_mud.Items.BasicTech;
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
import com.planet_ink.coffee_mud.Items.interfaces.Technical.TechType;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;

/*
   Copyright 2013-2018 Bo Zimmerman

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
public class GenStealthShield extends GenTickerShield
{

	@Override
	public String ID()
	{
		return "GenStealthShield";
	}

	public GenStealthShield()
	{
		super();
		setName("a personal stealth generator");
		setDisplayText("a personal stealth generator sits here.");
		setDescription("");
	}

	@Override
	protected String fieldOnStr(MOB viewerM)
	{
		return L((owner() instanceof MOB)?
			"A stealth field surrounds <O-NAME>.":
			"A stealth field surrounds <T-NAME>.");
	}

	@Override
	protected String fieldDeadStr(MOB viewerM)
	{
		return L((owner() instanceof MOB)?
			"The stealth field around <O-NAME> flickers and dies out as <O-HE-SHE> fade(s) back into view.":
			"The stealth field around <T-NAME> flickers and dies out as <T-HE-SHE> fade(s) back into view.");
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		if(activated() && (affected==owner()) && (owner() instanceof MOB) && (!amWearingAt(Wearable.IN_INVENTORY)) && (powerRemaining() > 0))
			affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_INVISIBLE);
		super.affectPhyStats(affected, affectableStats);
	}

	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(msg.amITarget(owner()) && (owner() instanceof MOB) && (!amWearingAt(Wearable.IN_INVENTORY)))
		{
			if((msg.targetMinor()==CMMsg.TYP_LOOK)&&(msg.source()!=owner()))
			{
				if((msg.tool() instanceof Technical)&&(CMath.bset(msg.targetMajor(), CMMsg.MASK_CNTRLMSG)))
				{
					if(((Technical)msg.tool()).techLevel()>techLevel())
						return true;
					return false;
				}
			}
		}
		return true;
	}

}
