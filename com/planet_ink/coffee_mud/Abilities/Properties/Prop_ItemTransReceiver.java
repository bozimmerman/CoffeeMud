package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ItemTransReceiver extends Property
{
	public String ID() { return "Prop_ItemTransReceiver"; }
	public String name(){ return "Item Transporter Receiver";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	public String accountForYourself()
	{ return "Item Transporter Receiver";	}
}