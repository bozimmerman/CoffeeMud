package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Crawlspace extends Property
{
	public Prop_Crawlspace()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Room navigation limitation";
	}

	public Environmental newInstance()
	{
		return new Prop_Crawlspace();
	}

	public String accountForYourself()
	{ return "Must be crawled through.";	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)&&((affected instanceof Room)||(affected instanceof Exit)))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_ENTER:
			case Affect.TYP_LEAVE:
			case Affect.TYP_FLEE:
				if(((affect.amITarget(affected))||(affect.tool()==affected))
				&&(!Sense.isSitting(affect.source())))
				{
					affect.source().tell("You must crawl that way.");
					return false;
				}
				break;
			case Affect.TYP_STAND:
				if(affected instanceof Room)
				{
					affect.source().tell("You cannot stand up here.");
					return false;
				}
			}
		}
		return super.okAffect(affect);
	}
}
