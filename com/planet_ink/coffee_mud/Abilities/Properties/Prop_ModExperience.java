package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_AREAS|Ability.CAN_ROOMS;}

	public String accountForYourself()
	{ return "";	}


	public int translateAmount(int amount, String val)
	{
	    if(amount<0) amount=-amount;
		if(val.endsWith("%"))
			return (int)Math.round(CMath.mul(amount,CMath.div(CMath.s_int(val.substring(0,val.length()-1)),100)));
		return CMath.s_int(val);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(((msg.target()==affected)&&(affected instanceof MOB))
		   ||((affected instanceof Item)
                   &&(msg.source()==((Item)affected).owner())
                   &&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY)))
		   ||(affected instanceof Room)
		   ||(affected instanceof Area)))
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
					&&((msg.target()==null)||(!(msg.target() instanceof MOB))||(!CMLib.masking().maskCheck(mask,msg.target(),true))))
						return super.okMessage(myHost,msg);
				}
				else
				if((mask.length()>0)
				&&(!CMLib.masking().maskCheck(mask,msg.source(),true)))
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
				msg.setValue((int)Math.round(CMath.div(msg.value(),translateAmount(msg.value(),s.substring(1)))));
			else
				msg.setValue(translateAmount(msg.value(),s));
		}
		return super.okMessage(myHost,msg);
	}
}
