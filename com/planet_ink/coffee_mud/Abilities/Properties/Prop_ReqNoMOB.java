package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqNoMOB extends Property
{
	public Prop_ReqNoMOB()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Monster Limitations";
	}

	public Environmental newInstance()
	{
		Prop_ReqNoMOB newOne=new Prop_ReqNoMOB();
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
			if((affect.source().isMonster())&&(affect.source().amFollowing()==null))
				return false;
		}
		return super.okAffect(affect);
	}
}