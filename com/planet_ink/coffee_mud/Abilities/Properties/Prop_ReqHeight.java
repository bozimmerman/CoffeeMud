package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqHeight extends Property
{
	public String ID() { return "Prop_ReqHeight"; }
	public String name(){ return "Height Restrictions";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqHeight newOne=new Prop_ReqHeight();	newOne.setMiscText(text());	return newOne;}

	public String accountForYourself()
	{ return "Height limit: "+Util.s_int(text());	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		   &&(affect.target()!=null)
		   &&(affect.target() instanceof Room)
		   &&(affect.targetMinor()==Affect.TYP_ENTER)
		   &&((affect.amITarget(affected))||(affect.tool()==affected)||(affected instanceof Area)))
		{
			int height=100;
			if(Util.s_int(text())>0)
				height=Util.s_int(text());
			if(affect.source().envStats().height()>height)
			{
				affect.source().tell("You are too tall to fit in there.");
				return false;
			}
		}
		return super.okAffect(myHost,affect);
	}
}
