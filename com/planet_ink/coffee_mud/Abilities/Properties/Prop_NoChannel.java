package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoChannel extends Property
{
	public String ID() { return "Prop_NoChannel"; }
	public String name(){ return "Channel Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}
	public Environmental newInstance(){	return new Prop_NoChannel();}

	public String accountForYourself()
	{ return "No Channeling Field";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;


		if((msg.othersMajor()&CMMsg.MASK_CHANNEL)>0)
		{
			msg.source().tell("This is a no-channel area.");
			return false;
		}
		return true;
	}
}
