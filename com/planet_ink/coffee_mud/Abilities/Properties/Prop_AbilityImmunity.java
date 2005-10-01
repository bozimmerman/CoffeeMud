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
public class Prop_AbilityImmunity extends Property
{
	public String ID() { return "Prop_AbilityImmunity"; }
	public String name(){ return "Ability Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public String accountForYourself() { return "Immunity";	}
	private Vector diseases=new Vector();
	private Vector messages=new Vector();

	public void setMiscText(String newText)
	{
        messages=new Vector();
		diseases=Util.parseSemicolons(newText.toUpperCase(),true);
		for(int d=0;d<diseases.size();d++)
		{
			String s=(String)diseases.elementAt(d);
			int x=s.indexOf("=");
			if(x<0)
				messages.addElement("");
			else
			{
				diseases.setElementAt(s.substring(0,x).trim(),d);
				messages.addElement(s.substring(x+1).trim());
			}
		}
		super.setMiscText(newText);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if ( (msg.source() != null)
	    && (msg.target() != null)
	    && (msg.tool() != null)
	    && (msg.amITarget(affected))
	    && (msg.tool() instanceof Ability ))
		{
			Ability d = (Ability)msg.tool();
			for(int i = 0; i < diseases.size(); i++)
			{
				if((EnglishParser.containsString(d.ID(),((String)diseases.elementAt(i))))
				||(EnglishParser.containsString(d.name(),((String)diseases.elementAt(i)))))
				{
					if(msg.target() instanceof MOB)
						((MOB)msg.target()).tell("You are immune to "+msg.tool().name()+".");
					if(msg.source()!=msg.target())
					{
						String s=(String)messages.elementAt(i);
						if(s.length()>0)
							msg.source().tell(msg.source(),msg.target(),msg.tool(),s);
						else
							msg.source().tell(msg.source(),msg.target(),msg.tool(),"<T-NAME> seem(s) immune to <O-NAME>.");
					}
					return false;
				}
			}
		}
		return super.okMessage(myHost, msg);
	}
}
