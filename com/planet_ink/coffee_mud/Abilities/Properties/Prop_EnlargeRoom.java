package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_EnlargeRoom extends Property
{
	public Prop_EnlargeRoom()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Change a rooms movement requirements";
		canAffectCode=Ability.CAN_ROOMS;
	}

	public Environmental newInstance()
	{
		return new Prop_EnlargeRoom();
	}

	public String accountForYourself()
	{ return "Enlarged";	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		if(text().length()>0)
			affectableStats.setWeight(Util.s_int(text()));
	}
}
