package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_Retainable extends Property
{
	public String ID() { return "Prop_Retainable"; }
	public String name(){ return "Ability to set Price/Retainability of a pet.";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prop_Retainable();}

	public String accountForYourself()
	{ return "Retainable";	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((mob.location()!=null)&&(mob.amFollowing()!=null))
			{
				Room room=mob.location();
				mob.baseEnvStats().setRejuv(0);
				mob.setStartRoom(room);
				if((affect.sourceMinor()==Affect.TYP_SHUTDOWN)
				||((affect.sourceMinor()==Affect.TYP_QUIT)&&(affect.amISource(mob.amFollowing()))))
					mob.setFollowing(null);
			}
		}
	}
}
