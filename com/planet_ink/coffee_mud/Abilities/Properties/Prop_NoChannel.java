package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoChannel extends Property
{
	public Prop_NoChannel()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Channel Neutralizing";
		canAffectCode=Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	public Environmental newInstance()
	{
		return new Prop_NoChannel();
	}

	public String accountForYourself()
	{ return "No Channeling Field";	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;


		if((affect.othersMajor()&affect.MASK_CHANNEL)>0)
		{
			affect.source().tell("This is a no-channel area.");
			return false;
		}
		return true;
	}
}
