package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Prop_ReqHeight extends Property
{
	public String ID() { return "Prop_ReqHeight"; }
	public String name(){ return "Height Restrictions";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}

	public String accountForYourself()
	{ return "Height limit: "+Util.s_int(text());	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(msg.target()!=null)
		   &&(msg.target() instanceof Room)
		   &&(msg.targetMinor()==CMMsg.TYP_ENTER)
		   &&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			int height=100;
			if(Util.s_int(text())>0)
				height=Util.s_int(text());
			if(msg.source().envStats().height()>height)
			{
				msg.source().tell("You are too tall to fit in there.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
