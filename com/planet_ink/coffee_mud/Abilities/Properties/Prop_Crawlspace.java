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
		if((affected!=null)
		   &&((affected instanceof Room)||(affected instanceof Exit))
		   &&(affect.amITarget(affected))
		   &&((affect.targetMinor()==Affect.TYP_ENTER)
			  ||(affect.targetMinor()==Affect.TYP_FLEE)
			  ||(affect.targetMinor()==Affect.TYP_LEAVE))
		   &&(!Sense.isSitting(affect.source())))
		{
			affect.source().tell("You must crawl that way");
			return false;
		}
		return super.okAffect(affect);
	}
}
