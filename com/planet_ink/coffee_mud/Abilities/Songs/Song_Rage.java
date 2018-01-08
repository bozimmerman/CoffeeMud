
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
public class Song_Rage extends Song
{
	@Override
	public String ID()
	{
		return "Song_Rage";
	}

	private final static String localizedName = CMLib.lang().L("Rage");

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

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(invoker==null)
			return;
		if(affected==invoker)
			return;
		affectableStats.setDamage(affectableStats.damage()+(int)Math.round(CMath.div(affectableStats.damage(),2.0+CMath.mul(0.25,super.getXLEVELLevel(invoker())))));
		affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-(int)Math.round(CMath.div(affectableStats.attackAdjustment(),6.0+CMath.mul(0.5,super.getXLEVELLevel(invoker())))));
		affectableStats.setArmor(affectableStats.armor()+super.adjustedLevel(invoker(),0));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if(msg.amISource(invoker))
			return true;
		if(msg.sourceMinor()!=CMMsg.TYP_FLEE)
			return true;
		if(msg.source().fetchEffect(this.ID())==null)
			return true;

		msg.source().tell(L("You are too enraged to flee."));
		return false;
	}

}
