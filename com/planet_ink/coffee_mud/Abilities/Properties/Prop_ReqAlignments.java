package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqAlignments extends Property
{
	public Prop_ReqAlignments()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Alignment Limitations";
		canAffectCode=Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;
	}

	public Environmental newInstance()
	{
		Prop_ReqAlignments newOne=new Prop_ReqAlignments();
		newOne.setMiscText(text());
		return newOne;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affect.target()!=null)
		   &&(affect.target() instanceof Room)
		   &&(affect.targetMinor()==Affect.TYP_ENTER)
		   &&((affect.amITarget(affected))||(affect.tool()==affected)||(affected instanceof Area)))
		{
			int x=text().toUpperCase().indexOf("ALL");
			int y=text().toUpperCase().indexOf(CommonStrings.shortAlignmentStr(affect.source().getAlignment()).toUpperCase());
			if(((x>0)
				&&(text().charAt(x-1)=='-')
				&&((y<=0)
				   ||((y>0)&&(text().charAt(y-1)=='-'))))
			 ||((y>0)&&(text().charAt(y-1)=='-')))
			{
				affect.source().tell("You may not go that way.");
				return false;
			}
		}
		return super.okAffect(affect);
	}
}
