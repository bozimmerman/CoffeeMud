package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqCapacity extends Property
{
	public String ID() { return "Prop_ReqCapacity"; }
	public String name(){ return "Capacity Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqCapacity newOne=new Prop_ReqCapacity();	newOne.setMiscText(text());	return newOne;}

	public String accountForYourself()
	{ return "Capacity limit: "+Util.s_int(text());	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affect.target()!=null)
		   &&(affect.target() instanceof Room)
		   &&(affect.targetMinor()==Affect.TYP_ENTER)
		   &&((affect.amITarget(affected))||(affect.tool()==affected)||(affected instanceof Area)))
		{
			int capacity=2;
			if(Util.s_int(text())>0)
				capacity=Util.s_int(text());
			if(((Room)affect.target()).numInhabitants()>=capacity)
			{
				affect.source().tell("No more people can fit in there.");
				return false;
			}
		}
		return super.okAffect(affect);
	}
}
