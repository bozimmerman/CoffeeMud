package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomRaces extends Property
{
	public Prop_RoomRaces()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Room Race Limitations";
	}

	public Environmental newInstance()
	{
		Prop_RoomRaces newOne=new Prop_RoomRaces();
		newOne.setMiscText(text());
		return newOne;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affected instanceof Room)
		   &&((affect.amITarget(affected))
			||((affect.target() instanceof GridLocaleChild)&&(affected==((GridLocaleChild)affect.target()).parent())))
		   &&(affect.targetMinor()==Affect.TYP_ENTER))
		{
			int x=text().toUpperCase().indexOf("ALL");
			int y=text().toUpperCase().indexOf(affect.source().charStats().getMyRace().name().toUpperCase());
			if(((x>0)
				&&(text().charAt(x-1)=='-')
				&&((y<=0)
				   ||((y>0)&&(text().charAt(y-1)!='+'))))
			 ||((y>0)&&(text().charAt(y-1)=='-')))
			{
				affect.source().tell("You can not go that way.");
				return false;
			}
		}
		return super.okAffect(affect);
	}
}