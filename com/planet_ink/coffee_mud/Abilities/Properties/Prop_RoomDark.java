package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomDark extends Property
{
	public Prop_RoomDark()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Darkening Property";
	}

	public Environmental newInstance()
	{
		return new Prop_RoomDark();
	}

	public String accountForYourself()
	{ return "Darkened";	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_DARK);
	}
}
