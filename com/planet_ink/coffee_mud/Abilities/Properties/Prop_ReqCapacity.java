package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomCapacity extends Property
{
	public Prop_RoomCapacity()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Room Capacity Limitations";
	}

	public Environmental newInstance()
	{
		Prop_RoomCapacity newOne=new Prop_RoomCapacity();
		newOne.setMiscText(text());
		return newOne;
	}

	public String accountForYourself()
	{ return "Capacity limit: "+Util.s_int(text());	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affected instanceof Room)
		   &&(affect.amITarget(affected))
		   &&(affect.targetMinor()==Affect.TYP_ENTER))
		{
			int capacity=2;
			if(Util.s_int(text())>0)
				capacity=Util.s_int(text());
			if(((Room)affected).numInhabitants()>=capacity)
			{
				affect.source().tell("No more people can fit in there.");
				return false;
			}
		}
		return super.okAffect(affect);
	}
}
