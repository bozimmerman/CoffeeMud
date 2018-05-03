package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class DiligentStudying extends StdAbility
{
	@Override
	public String ID()
	{
		return "DiligentStudying";
	}

	private final static String	localizedName	= CMLib.lang().L("Diligent Studying");

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
		return Ability.ACODE_COMMON_SKILL;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(msg.sourceMinor()==CMMsg.TYP_LEVEL)
		{
			if(msg.source() == affected)
			{
				int amt = (msg.value() - msg.source().basePhyStats().level());
				int multiplier = CMath.s_int(text());
				if(multiplier != 0)
					amt = amt * multiplier;
				if(amt == 1)
					msg.source().tell(L("^NYou gain ^H1^N practice point.\n\r^N"));
				else
				if(amt > 1)
					msg.source().tell(L("^NYou gain ^H@x1^N practice points.\n\r^N",""+amt));
				else
				if(amt == -1)
					msg.source().tell(L("^NYou lose ^H1^N practice point.\n\r^N"));
				else
				if(amt < -1)
					msg.source().tell(L("^NYou lose ^H@x1^N practice points.\n\r^N",""+(-amt)));
					
				msg.source().setPractices(msg.source().getPractices() + amt);
				if(msg.source().getPractices()<0)
					msg.source().setPractices(0);
			}
		}
		super.executeMsg(myHost,msg);
	}
}
