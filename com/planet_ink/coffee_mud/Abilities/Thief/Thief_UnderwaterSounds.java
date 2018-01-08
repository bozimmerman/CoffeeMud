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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback.Type;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2016-2018 Bo Zimmerman

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

public class Thief_UnderwaterSounds extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_UnderwaterSounds";
	}

	private final static String	localizedName	= CMLib.lang().L("Underwater Sounds");

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
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return affected != invoker;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_WATERLORE;
	}
	
	protected Thief_Listen myListen = null;
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((myListen == null)&&(ticking instanceof MOB))
		{
			myListen = (Thief_Listen)((MOB)ticking).fetchAbility("Thief_Listen");
			if(myListen != null)
			{
				myListen.flags.add(Thief_Listen.ListenFlag.UNDERWATER);
			}
		}
		return true;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.source()==affected)
		&&(myListen != null)
		&&(msg.tool() == myListen)
		&&(msg.target() instanceof Room)
		&&(msg.target() != msg.source().location())
		&&(CMLib.flags().isUnderWateryRoom((Room)msg.target())))
		{
			if(msg.source().isMine(msg.tool()))
			{
				if(!super.proficiencyCheck(msg.source(), super.getXLEVELLevel(msg.source())*5, false))
				{
					msg.source().tell(L("You don't hear anything."));
					return false;
				}
				else
				{
					super.helpProficiency(msg.source(), super.getXLEVELLevel(msg.source()));
				}
			}
			else
			{
				myListen = null;
			}
		}
		return true;
	}
}
