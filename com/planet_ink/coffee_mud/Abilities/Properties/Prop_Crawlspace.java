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
					affect.source().tell("You cannot stand up here, try crawling.");
					return false;
				}
			}
		}
		return super.okAffect(myHost,affect);
	}
}
