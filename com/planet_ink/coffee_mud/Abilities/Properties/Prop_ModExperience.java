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
public class Prop_ModExperience extends Property
{
	boolean disabled=false;
	public String ID() { return "Prop_ModExperience"; }
	public String name(){ return "Modifying Experience Gained";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}

	public String accountForYourself()
	{ return "";	}


	public int translateAmount(int amount, String val)
	{
		if(val.endsWith("%"))
			return (int)Math.round(Util.mul(amount,Util.div(Util.s_int(val.substring(0,val.length()-1)),100)));
		return Util.s_int(val);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(((msg.target()==affected)&&(affected instanceof MOB))
		   ||((affected instanceof Item)&&(msg.source()==((Item)affected).owner()))&&(!((Item)affected).amWearingAt(Item.INVENTORY)))
		   ||(affected instanceof Room)
		   ||(affected instanceof Area))
		{
			String s=text().trim();
			int x=s.indexOf(";");
			if(x>=0)
			{
				String mask=s.substring(x+1).trim();
				s=s.substring(0,x).trim();
				if(affected instanceof Item)
				{
					if((mask.length()>0)
					&&((msg.target()==null)||(!(msg.target() instanceof MOB))||(!MUDZapper.zapperCheck(mask,msg.target()))))
						return super.okMessage(myHost,msg);
				}
				else
				if((mask.length()>0)
				&&(!MUDZapper.zapperCheck(mask,msg.source())))
					return super.okMessage(myHost,msg);
			}

			if(s.length()==0)
				msg.setValue(0);
			else
			if(s.startsWith("="))
				msg.setValue(translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("+"))
				msg.setValue(msg.value()+translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("-"))
				msg.setValue(msg.value()-translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("*"))
				msg.setValue(msg.value()*translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("/"))
				msg.setValue((int)Math.round(Util.div(msg.value(),translateAmount(msg.value(),s.substring(1)))));
			else
				msg.setValue(translateAmount(msg.value(),s));
		}
		return super.okMessage(myHost,msg);
	}
}
