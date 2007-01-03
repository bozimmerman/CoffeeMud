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
   Copyright 2000-2006 Bo Zimmerman

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
public class Prop_ReqEntry extends Property
{
	public String ID() { return "Prop_ReqEntry"; }
	public String name(){ return "All Room/Exit Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}

	public String accountForYourself()
	{
		return "Entry restricted as follows: "+CMLib.masking().maskDesc(miscText);
	}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(CMLib.flags().isATrackingMonster(mob))
			return true;
		if(CMLib.flags().isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		return CMLib.masking().maskCheck(text(),mob,false);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(msg.target()!=null))
		{
			if((msg.target() instanceof Room)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(!CMLib.flags().isFalling(msg.source()))
			&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
			{
				HashSet H=new HashSet();
				if(text().toUpperCase().indexOf("NOFOL")>=0)
					H.add(msg.source());
				else
				{
					msg.source().getGroupMembers(H);
					HashSet H2=(HashSet)H.clone();
					for(Iterator e=H2.iterator();e.hasNext();)
						((MOB)e.next()).getRideBuddies(H);
				}
				for(Iterator e=H.iterator();e.hasNext();)
					if(passesMuster((MOB)e.next()))
						return super.okMessage(myHost,msg);
				msg.source().tell("You can not go that way.");
				return false;
			}
			else
			if((msg.target() instanceof Rideable)
			&&(msg.amITarget(affected)))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_MOUNT:
					{
						HashSet H=new HashSet();
						if(text().toUpperCase().indexOf("NOFOL")>=0)
							H.add(msg.source());
						else
						{
							msg.source().getGroupMembers(H);
							HashSet H2=(HashSet)H.clone();
							for(Iterator e=H.iterator();e.hasNext();)
								((MOB)e.next()).getRideBuddies(H2);
							H=H2;
						}
						for(Iterator e=H.iterator();e.hasNext();)
						{
						    Environmental E=(Environmental)e.next();
						    if((E instanceof MOB)
							&&(passesMuster((MOB)E)))
								return super.okMessage(myHost,msg);
						}
						msg.source().tell("You are not permitted in there.");
						return false;
					}
				default:
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}
