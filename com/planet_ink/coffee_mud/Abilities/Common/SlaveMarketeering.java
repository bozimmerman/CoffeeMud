package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2022-2023 Bo Zimmerman

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
public class SlaveMarketeering extends Merchant
{
	@Override
	public String ID()
	{
		return "SlaveMarketeering";
	}

	public SlaveMarketeering()
	{
		super();
		super.whatIsSoldMask = ShopKeeper.DEAL_SLAVES;
	}

	private final static String	localizedName	= CMLib.lang().L("Slave Marketeering");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SLAVEMARKETEERING", "SMARKET" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected boolean canSell(final MOB mob, final Environmental E)
	{
		if(E instanceof MOB)
		{
			if((!CMLib.flags().isASlave((MOB)E, mob))
			||(((MOB)E).isPlayer()))
			{
				commonTelL(mob,"@x1 is not your slave.",((MOB)E).name(mob));
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		makeActive(mob);
		if(commands.size()==0)
		{
			commonTelL(mob,"Slave Market what? Enter \"smarket list\" for a list or \"smarket mob value\" to sell someone.");
			return false;
		}
		return super.invoke(mob, commands, givenTarget, auto, asLevel);
	}
}
