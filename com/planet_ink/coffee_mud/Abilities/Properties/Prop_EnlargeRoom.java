package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public String ID() { return "Prop_EnlargeRoom"; }
	public String name(){ return "Change a rooms movement requirements";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public String accountForYourself()
	{ return "Enlarged";	}

	private double dval(String s)
	{
		if(s.indexOf(".")>=0)
			return Util.s_double(s);
		return new Integer(Util.s_int(s)).doubleValue();
	}

	private int ival(String s)
	{
		return (int)Math.round(dval(s));
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(text().length()>0)
		{
			int weight=affectableStats.weight();
			switch(text().charAt(0))
			{
			case '+':
				affectableStats.setWeight(weight+ival(text().substring(1).trim()));
				break;
			case '-':
				affectableStats.setWeight(weight-ival(text().substring(1).trim()));
				break;
			case '*':
				affectableStats.setWeight((int)Math.round(Util.mul(weight,dval(text().substring(1).trim()))));
				break;
			case '/':
				affectableStats.setWeight((int)Math.round(Util.div(weight,dval(text().substring(1).trim()))));
				break;
			default:
				affectableStats.setWeight(ival(text()));
				break;
			}
		}
	}
}
