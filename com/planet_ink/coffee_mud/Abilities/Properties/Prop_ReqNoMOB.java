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
public class Prop_ReqNoMOB extends Property
{
	public String ID() { return "Prop_ReqNoMOB"; }
	public String name(){ return "Monster Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(Sense.isATrackingMonster(mob))
			return true;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		return !mob.isMonster();
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		&&(msg.target()!=null)
		&&(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
		   ||((msg.target() instanceof Rideable)&&(msg.targetMinor()==CMMsg.TYP_SIT)))
		&&(!Sense.isFalling(msg.source()))
		&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			HashSet H=new HashSet();
			if(text().toUpperCase().indexOf("NOFOL")>=0)
				H.add(msg.source());
			else
			{
				msg.source().getGroupMembers(H);
				int hsize=0;
				while(hsize!=H.size())
				{
					hsize=H.size();
					HashSet H2=(HashSet)H.clone();
					for(Iterator e=H2.iterator();e.hasNext();)
					{
						Object O=e.next();
						if(O instanceof MOB)
							((MOB)O).getRideBuddies(H);
					}
				}
			}
			for(Iterator e=H.iterator();e.hasNext();)
			{
				Object O=e.next();
				if((!(O instanceof MOB))||(passesMuster((MOB)O)))
					return super.okMessage(myHost,msg);
			}
			msg.source().tell("You are not allowed in there.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
