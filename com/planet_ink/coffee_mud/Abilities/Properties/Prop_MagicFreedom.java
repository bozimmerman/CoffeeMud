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
   Copyright 2001-2018 Bo Zimmerman

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
public class Prop_MagicFreedom extends Property
{
	@Override
	public String ID()
	{
		return "Prop_MagicFreedom";
	}

	@Override
	public String name()
	{
		return "Magic Neutralizing";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	@Override
	public String accountForYourself()
	{
		return "Anti-Magic Field";
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_IMMUNER;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((CMath.bset(msg.sourceMajor(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.targetMajor(),CMMsg.MASK_MAGIC))
		||(CMath.bset(msg.othersMajor(),CMMsg.MASK_MAGIC)))
		{
			Room room=null;
			if(affected instanceof Room)
				room=(Room)affected;
			else
			if(msg.source().location()!=null)
				room=msg.source().location();
			else
			if((msg.target() instanceof MOB)
			&&(((MOB)msg.target()).location()!=null))
				room=((MOB)msg.target()).location();
			if(room!=null)
				room.showHappens(CMMsg.MSG_OK_VISUAL,L("Magic energy fizzles and is absorbed into the air."));
			return false;
		}
		return true;
	}
}
