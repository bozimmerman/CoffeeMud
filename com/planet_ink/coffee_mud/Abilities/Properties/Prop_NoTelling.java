package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoTelling extends Property
{
	public String ID() { return "Prop_NoTelling"; }
	public String name(){ return "Tel Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_NoTelling();}

	public String accountForYourself()
	{ return "No Telling Field";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;


		if((msg.sourceMinor()==CMMsg.TYP_TELL)
		&&((!(affected instanceof MOB))||(msg.source()==affected)))
		{
			if(affected instanceof MOB)
				msg.source().tell("Your message drifts into oblivion.");
			else
				msg.source().tell("This is a no-tell area.");
			return false;
		}
		return true;
	}
}
