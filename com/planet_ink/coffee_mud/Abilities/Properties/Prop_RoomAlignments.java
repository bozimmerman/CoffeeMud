package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomAlignments extends Property
{
	public Prop_RoomAlignments()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Room Alignment Limitations";
	}

	public Environmental newInstance()
	{
		Prop_RoomAlignments newOne=new Prop_RoomAlignments();
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
			int x=text().toUpperCase().indexOf("ALL");
			int y=text().toUpperCase().indexOf(ExternalPlay.shortAlignmentStr(affect.source().getAlignment()).toUpperCase());
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
