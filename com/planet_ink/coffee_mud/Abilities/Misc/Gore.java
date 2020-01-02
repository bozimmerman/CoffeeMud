package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Gore extends StdAbility
{
	@Override
	public String ID()
	{
		return "Gore";
	}

	private final static String	localizedName	= CMLib.lang().L("Gore");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_SELF;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	protected boolean isWeaponMatch(final Weapon W)
	{
		if((W.weaponDamageType()==Weapon.TYPE_PIERCING)
		||(W.weaponClassification()==Weapon.CLASS_NATURAL))
			return true;
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(msg.tool() instanceof Weapon))
		{
			final Weapon w=(Weapon)msg.tool();
			if(isWeaponMatch(w))
			{
				if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)&&(CMLib.dice().rollPercentage()<10))
					helpProficiency((MOB)affected, 0);
				else
				if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
				&&(msg.value()>0))
				{
					final double dmgMultiplier = 1.5 + (0.1 * super.getXLEVELLevel(msg.source()));
					msg.setValue((int)Math.round(CMath.mul(msg.value(),dmgMultiplier)));
				}
			}
		}
	}
}
