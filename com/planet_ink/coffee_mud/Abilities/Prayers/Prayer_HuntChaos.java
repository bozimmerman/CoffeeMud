package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2019-2020 Bo Zimmerman

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
public class Prayer_HuntChaos extends Prayer_HuntEvil
{
	@Override
	public String ID()
	{
		return "Prayer_HuntChaos";
	}

	private final static String localizedName = CMLib.lang().L("Hunt Chaos");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_LAW|Ability.FLAG_TRACKING;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_NEUTRALIZATION;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Hunting Chaos)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected String word()
	{
		return "chaos";
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	protected MOB gameHere(final Room room)
	{
		if(room==null)
			return null;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB mob=room.fetchInhabitant(i);
			if(CMLib.flags().isChaotic(mob))
				return mob;
		}
		return null;
	}

}
