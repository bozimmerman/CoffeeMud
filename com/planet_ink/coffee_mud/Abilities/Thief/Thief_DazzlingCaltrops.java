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
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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
public class Thief_DazzlingCaltrops extends Thief_Caltrops
{
	@Override
	public String ID()
	{
		return "Thief_DazzlingCaltrops";
	}

	private final static String localizedName = CMLib.lang().L("Dazzling Caltrops");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[] triggerStrings =I(new String[] {"DAZZLINGCALTROPS"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public String caltropTypeName()
	{
		return CMLib.lang().L("dazzling ");
	}

	@Override
	public void spring(MOB mob)
	{
		if((!invoker().mayIFight(mob))
		||(invoker().getGroupMembers(new HashSet<MOB>()).contains(mob))
		||(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,L("<S-NAME> avoid(s) looking at some @x1caltrops on the floor.",caltropTypeName()));
		else
		if(mob.curState().getMana()>6)
		{
			mob.curState().adjMana(-CMLib.dice().roll(3+getX1Level(mob),8,20),mob.maxState());
			mob.location().show(invoker(),mob,this,CMMsg.MSG_OK_ACTION,L("The @x1caltrops on the ground sparkle and confuse <T-NAME>",caltropTypeName()));
		}
		// does not set sprung flag -- as this trap never goes out of use
	}
}
