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

	public String accountForYourself()
	{ return "Height limit: "+Util.s_int(text());	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(msg.target()!=null)
		   &&(msg.target() instanceof Room)
		   &&(msg.targetMinor()==CMMsg.TYP_ENTER)
		   &&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			int height=100;
			if(Util.s_int(text())>0)
				height=Util.s_int(text());
			if(msg.source().envStats().height()>height)
			{
				msg.source().tell("You are too tall to fit in there.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
