package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_Adjuster extends Prop_HaveAdjuster
{
	@Override
	public String ID()
	{
		return "Prop_Adjuster";
	}

	@Override
	public String name()
	{
		return "Adjustments to stats";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ITEMS | Ability.CAN_EXITS | Ability.CAN_ROOMS | Ability.CAN_AREAS;
	}

	@Override
	public boolean bubbleAffect()
	{
		return (affected instanceof Area);
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	@Override
	public boolean canApply(MOB mob)
	{
		if((affected!=null)
		&&((mask==null)||(CMLib.masking().maskCheck(mask,mob,true))))
			return true;
		return false;
	}

	@Override
	public boolean canApply(Environmental E)
	{
		if((affected!=null)
		&&((mask==null)||(CMLib.masking().maskCheck(mask,E,true))))
			return true;
		return false;
	}

	@Override
	public int triggerMask()
	{
		return TriggeredAffect.TRIGGER_ALWAYS;
	}

	@Override
	public String accountForYourself()
	{
		return fixAccoutingsWithMask("Effects: "+parameters[0],parameters[1]);
	}
}
