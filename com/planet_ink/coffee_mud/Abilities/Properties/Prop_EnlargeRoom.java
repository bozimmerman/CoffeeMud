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
   Copyright 2003-2018 Bo Zimmerman

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
public class Prop_EnlargeRoom extends Property
{
	@Override
	public String ID()
	{
		return "Prop_EnlargeRoom";
	}

	@Override
	public String name()
	{
		return "Change a rooms movement requirements";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	@Override
	public String accountForYourself()
	{
		return "Enlarged";
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_ADJUSTER;
	}

	protected double dval(String s)
	{
		if(s.indexOf('.')>=0)
			return CMath.s_double(s);
		return CMath.s_int(s);
	}

	protected int ival(String s)
	{
		return (int)Math.round(dval(s));
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		if(text().length()>0)
		{
			final int weight=affectableStats.weight();
			switch(text().charAt(0))
			{
			case '+':
				affectableStats.setWeight(weight+ival(text().substring(1).trim()));
				break;
			case '-':
				affectableStats.setWeight(weight-ival(text().substring(1).trim()));
				break;
			case '*':
				affectableStats.setWeight((int)Math.round(CMath.mul(weight,dval(text().substring(1).trim()))));
				break;
			case '/':
				affectableStats.setWeight((int)Math.round(CMath.div(weight,dval(text().substring(1).trim()))));
				break;
			default:
				affectableStats.setWeight(ival(text()));
				break;
			}
		}
	}
}
