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
public class Prop_Crawlspace extends Property
{
	public String ID() { return "Prop_Crawlspace"; }
	public String name(){ return "Room navigation limitation";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public String accountForYourself()
	{ return "Must be crawled through.";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&((affected instanceof Room)||(affected instanceof Exit)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_FLEE:
				if(((msg.amITarget(affected))||(msg.tool()==affected))
				&&(msg.source().envStats().height()>12)
				&&(!Sense.isSitting(msg.source())))
				{
					if(msg.source().envStats().height()>120)
					{
						msg.source().tell("You cannot fit in there.");
						return false;
					}
					msg.source().tell("You must crawl that way.");
					return false;
				}
				break;
			case CMMsg.TYP_STAND:
				if((affected instanceof Room)
				&&(msg.source().envStats().height()>12))
				{
			        if(Sense.isSleeping(msg.source()))
					{
			            MOB mob=msg.source();
			            int oldDisposition = mob.baseEnvStats().disposition();
			            oldDisposition=oldDisposition&(Integer.MAX_VALUE-EnvStats.IS_SLEEPING-EnvStats.IS_SNEAKING-EnvStats.IS_SITTING);
			            mob.baseEnvStats().setDisposition(oldDisposition|EnvStats.IS_SITTING);
			            mob.recoverEnvStats();
			            mob.recoverCharStats();
			            mob.recoverMaxState();
			            mob.tell("You wake up, but you are still crawling.");
			            return false;
			        }
			        else
			        {
			            msg.source().tell("You cannot stand up here, try crawling.");
			            return false;
			        }
				}
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
