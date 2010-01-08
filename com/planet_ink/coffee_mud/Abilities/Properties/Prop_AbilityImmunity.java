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
@SuppressWarnings("unchecked")
public class Prop_AbilityImmunity extends Property
{
	public String ID() { return "Prop_AbilityImmunity"; }
	public String name(){ return "Ability Immunity";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public String accountForYourself() { return "Immunity";	}
	protected Vector diseases=new Vector();
	protected Vector messages=new Vector();
	protected boolean owner = false;
	protected boolean wearer = false;

	public void setMiscText(String newText)
	{
        messages=new Vector();
		diseases=CMParms.parseSemicolons(newText.toUpperCase(),true);
		owner = false;
		wearer = false;
		for(int d=0;d<diseases.size();d++)
		{
			String s=(String)diseases.elementAt(d);
			if(s.equalsIgnoreCase("owner"))
				owner=true;
			else
			if(s.equalsIgnoreCase("wearer"))
				wearer=true;
			else
			{
				int x=s.indexOf("=");
				if(x<0)
					messages.addElement("");
				else
				{
					diseases.setElementAt(s.substring(0,x).trim(),d);
					messages.addElement(s.substring(x+1).trim());
				}
			}
		}
		super.setMiscText(newText);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if ( (msg.source() != null)
	    && (msg.target() != null)
	    && (msg.tool() != null)
	    && ((msg.amITarget(affected))
	    		||(owner && (affected instanceof Item)&&(msg.target()==((Item)affected).owner()))
	    		||(owner && (affected instanceof Item)&&(msg.target()==((Item)affected).owner())&&(!((Item)affected).amWearingAt(Wearable.IN_INVENTORY))))
	    && (msg.tool() instanceof Ability ))
		{
			Ability d = (Ability)msg.tool();
			for(int i = 0; i < diseases.size(); i++)
			{
				if((CMLib.english().containsString(d.ID(),((String)diseases.elementAt(i))))
				||(CMLib.english().containsString(d.name(),((String)diseases.elementAt(i)))))
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
