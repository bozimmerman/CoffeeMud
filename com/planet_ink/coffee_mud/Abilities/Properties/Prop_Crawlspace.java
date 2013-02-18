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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

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

	public long flags(){return Ability.FLAG_ADJUSTER;}

	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_ROOMCRUNCHEDIN);
	}
	
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)&&((affected instanceof Room)||(affected instanceof Exit)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_FLEE:
				if(((msg.amITarget(affected))||(msg.tool()==affected))
				&&(msg.sourceMinor()!=CMMsg.TYP_RECALL)
				&&(msg.source().phyStats().height()>12)
				&&(!CMLib.flags().isSitting(msg.source())))
				{
					if(msg.source().phyStats().height()>120)
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
				&&(msg.source().phyStats().height()>12))
				{
					if(CMLib.flags().isSleeping(msg.source()))
					{
						MOB mob=msg.source();
						int oldDisposition = mob.basePhyStats().disposition();
						oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING));
						mob.basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SITTING);
						mob.recoverPhyStats();
						mob.recoverCharStats();
						mob.recoverMaxState();
						mob.tell("You wake up, but you are still crawling.");
						return false;
					}
					msg.source().tell("You cannot stand up here, try crawling.");
					return false;
				}
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
