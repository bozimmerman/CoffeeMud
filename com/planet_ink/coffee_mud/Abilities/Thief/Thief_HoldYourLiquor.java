package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2016-2025 Bo Zimmerman

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
public class Thief_HoldYourLiquor extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_HoldYourLiquor";
	}

	private final static String localizedName = CMLib.lang().L("Hold Your Liquor");

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

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_FITNESS;
	}

	protected volatile int checkAgain = Integer.MAX_VALUE/2;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!(affected instanceof MOB))
			return super.okMessage(myHost,msg);

		final MOB mob=(MOB)affected;
		if((msg.amISource(mob))
		&&(msg.targetMinor()==CMMsg.TYP_DRINK)
		&&(msg.target() instanceof Drink)
		&&(msg.target() instanceof Item)
		&&(CMLib.flags().isAlcoholic((Item)msg.target())))
		{
			if(proficiencyCheck(mob,0,false))
				checkAgain=2;
			super.helpProficiency(mob, 0);
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if(((--checkAgain)<=0)&&(ticking instanceof Physical))
		{
			checkAgain=Integer.MAX_VALUE/2;
			final List<Ability> aList=CMLib.flags().flaggedAffects((Physical)ticking, Ability.FLAG_INTOXICATING);
			for(final Ability A : aList)
			{
				//int code=A.abilityCode();
				A.setAbilityCode(0);
			}
			if(aList.size()>0)
			{
				((Physical)ticking).recoverPhyStats();
				if(ticking instanceof MOB)
				{
					((MOB)ticking).recoverCharStats();
					((MOB)ticking).recoverMaxState();
				}
			}
		}
		return true;
	}
}
