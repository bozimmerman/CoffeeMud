package com.planet_ink.coffee_mud.Abilities.Specializations;
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
public class Specialization_Natural extends Specialization_Weapon
{
	@Override
	public String ID()
	{
		return "Specialization_Natural";
	}

	private final static String	localizedName	= CMLib.lang().L("Hand to hand combat");

	@Override
	public String name()
	{
		return localizedName;
	}

	public Specialization_Natural()
	{
		super();
		weaponClass=Weapon.CLASS_NATURAL;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((activated)
		&&(msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
		&&(CMLib.dice().rollPercentage()<10)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected))
		&&((!(msg.tool() instanceof Weapon))||(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_NATURAL)))
			helpProficiency((MOB)affected, 0);
		super.executeMsg(myHost, msg);
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		activated=false;
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)&&(((MOB)affected).fetchWieldedItem()==null))
		{
			activated=true;
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
					+(int)Math.round(15.0*(CMath.div(proficiency(),100.0)))
					+(10*(getXLEVELLevel((MOB)affected))));
		}
	}

	@Override
	protected boolean canDamage(MOB mob, Weapon W)
	{
		return (W.weaponClassification()==Weapon.CLASS_NATURAL) || (!W.amWearingAt(Wearable.IN_INVENTORY));
	}
}
