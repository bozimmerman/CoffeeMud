package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_WeakBridge extends Property
{
	public String ID() { return "Prop_WeakBridge"; }
	public String name(){ return "Weak Rickity Bridge";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	return new Prop_WeakBridge();}

	public String accountForYourself()
	{ return "Weak and Rickity";	}

	
}
