package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomNoMOB extends Property
{
	public Prop_RoomNoMOB()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Room Monster Limitations";
	}

	public Environmental newInstance()
	{
		Prop_RoomNoMOB newOne=new Prop_RoomNoMOB();
		newOne.setMiscText(text());
		return newOne;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affected instanceof Room)
		   &&(affect.amITarget(affected))
		   &&(affect.targetMinor()==Affect.TYP_ENTER))
		{
			if((affect.source().isMonster())&&(affect.source().amFollowing()==null))
				return false;
		}
		return super.okAffect(affect);
	}
}