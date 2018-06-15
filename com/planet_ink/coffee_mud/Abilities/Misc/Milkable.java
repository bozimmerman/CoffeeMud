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
   Copyright 2018-2018 Bo Zimmerman

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

public class Milkable extends StdAbility
{
	@Override
	public String ID()
	{
		return "Milkable";
	}

	private final static String	localizedName	= CMLib.lang().L("Milkable");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Milkable)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
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
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_COMMANDFAIL:
			System.out.println("HO!!!!!");
			break;
		case CMMsg.TYP_COMMANDREJECT:
			System.out.println("HE!!!!!"); // both came this way
			break;
		case CMMsg.TYP_HUH:
			System.out.println("HY!!!!!");
			break;
		case CMMsg.TYP_DRINK:
		case CMMsg.TYP_FILL:
			System.out.println("HI!!!!!"); // drink came this way
			break;
		}
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_COMMANDFAIL:
			System.out.println("!!HO!!!!!");
			break;
		case CMMsg.TYP_COMMANDREJECT:
			System.out.println("!!HE!!!!!");
			break;
		case CMMsg.TYP_HUH:
			System.out.println("!!HY!!!!!");
			break;
		case CMMsg.TYP_DRINK:
		case CMMsg.TYP_FILL:
			System.out.println("!!HI!!!!!"); // drink came this way
			break;
		}
		System.out.println("---");
		return super.okMessage(myHost,msg);
	}
}
