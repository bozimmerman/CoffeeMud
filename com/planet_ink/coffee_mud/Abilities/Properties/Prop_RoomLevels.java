package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomLevels extends Property
{
	public Prop_RoomLevels()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Room Level Limitations";
	}

	public Environmental newInstance()
	{
		Prop_RoomLevels newOne=new Prop_RoomLevels();
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
			if((text().toUpperCase().indexOf("ALL")>=0)||(text().length()==0)||(affect.source().isASysOp((Room)affected)))
				return super.okAffect(affect);

			if((text().toUpperCase().indexOf("SYSOP")>=0)&&(!affect.source().isASysOp((Room)affected)))
			{
				affect.source().tell("That way is restricted.  You are not allowed.");
				return false;
			}

			int lvl=affect.source().envStats().level();
			int x=text().indexOf(">=");
			if(x<0)	x=text().indexOf(">");
			if(x<0)	x=text().indexOf("<=");
			if(x<0)	x=text().indexOf("<");
			if(x<0)	x=text().indexOf("=");
			if(x>=0)
			{
				char c=text().charAt(x);
				int y=Util.s_int(text().substring(x+1).trim());
				if(text().length()>(x+1))
					if(text().charAt(x+1)=='=')
					{
						y=Util.s_int(text().substring(x+2).trim());
						if(lvl==y) return super.okAffect(affect);
					}

				switch(c)
				{
				case '>':
					if(lvl>y) return super.okAffect(affect);
					break;
				case '<':
					if(lvl<y) return super.okAffect(affect);
					break;
				case '=':
					if(lvl==y) return super.okAffect(affect);
					break;
				}
				affect.source().tell("You can not go that way.");
				return false;
			}
		}
		return super.okAffect(affect);
	}
}