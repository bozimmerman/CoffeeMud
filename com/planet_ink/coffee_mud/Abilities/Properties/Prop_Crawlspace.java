package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Crawlspace extends Property
{
	public String ID() { return "Prop_Crawlspace"; }
	public String name(){ return "Room navigation limitation";}
	protected int canAffectCode(){return Ability.CAN_EXITS|Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_Crawlspace();}
	public String accountForYourself()
	{ return "Must be crawled through.";	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)&&((affected instanceof Room)||(affected instanceof Exit)))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_ENTER:
			case Affect.TYP_LEAVE:
			case Affect.TYP_FLEE:
				if(((affect.amITarget(affected))||(affect.tool()==affected))
				&&(affect.source().envStats().height()>12)
				&&(!Sense.isSitting(affect.source())))
				{
					if(affect.source().envStats().height()>120)
					{
						affect.source().tell("You cannot fit in there.");
						return false;
					}
					affect.source().tell("You must crawl that way.");
					return false;
				}
				break;
			case Affect.TYP_STAND:
				if((affected instanceof Room)
				&&(affect.source().envStats().height()>12))
				{
			        if(Sense.isSleeping(affect.source()))
					{
			            MOB mob=affect.source();
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
			            affect.source().tell("You cannot stand up here, try crawling.");
			            return false;
			        }
				}
				break;
			}
		}
		return super.okAffect(myHost,affect);
	}
}
