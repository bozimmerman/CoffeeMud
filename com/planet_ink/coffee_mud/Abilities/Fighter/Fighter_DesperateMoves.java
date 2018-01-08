package com.planet_ink.coffee_mud.Abilities.Fighter;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Fighter_DesperateMoves extends FighterSkill
{
	@Override
	public String ID()
	{
		return "Fighter_DesperateMoves";
	}

	private final static String localizedName = CMLib.lang().L("Desperate Moves");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override public String displayText()
	{
		if((affected==invoker)&&(invoker!=null)&&(invoker.curState().getHitPoints()<(int)Math.round(CMath.div(invoker.maxState().getHitPoints(),10.0))))
			return L("(Desperate Moves)");
		else
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
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_EVASIVE;
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
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_DAMAGE)
		&&(affected==invoker)
		&&(msg.value()>0)
		&&(invoker.curState().getHitPoints()<=(int)Math.round(CMath.div(invoker.maxState().getHitPoints(),10.0))))
		{
			msg.setValue((int)Math.round(Math.ceil(CMath.div(msg.value(),1.0 + (1.0 * CMath.div(proficiency()+(10.0*getXLEVELLevel(invoker)),100.0))))));
			super.helpProficiency(invoker, 0);
		}
		return true;
	}
}
