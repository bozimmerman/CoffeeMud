package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_NoPurge extends Property
{
	public Prop_NoPurge()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Prevents automatic purging";
		canAffectCode=Ability.CAN_ROOMS;
	}

	public Environmental newInstance()
	{
		return new Prop_NoPurge();
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((affected!=null)&&(affected instanceof Room))
		{
			Room R=(Room)affected;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if(I!=null) I.setDispossessionTime(null);
			}
		}
	}
}