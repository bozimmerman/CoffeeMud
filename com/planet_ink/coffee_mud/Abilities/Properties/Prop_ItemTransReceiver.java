package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ItemTransReceiver extends Property
{
	public Prop_ItemTransReceiver()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Item Transporter Receiver";
		canAffectCode=Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;
	}

	public Environmental newInstance()
	{
		return new Prop_ItemTransReceiver();
	}

	public String accountForYourself()
	{ return "Item Transporter Receiver";	}
}