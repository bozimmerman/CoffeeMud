package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqClasses extends Property
{
	public String ID() { return "Prop_ReqClasses"; }
	public String name(){ return "Class Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqClasses newOne=new Prop_ReqClasses();	newOne.setMiscText(text());	return newOne;}
	
	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		
		int x=text().toUpperCase().indexOf("ALL");
		int y=text().toUpperCase().indexOf(mob.charStats().getCurrentClass().name().toUpperCase());
		if(y<0) y=text().toUpperCase().indexOf(mob.charStats().getCurrentClass().baseClass().toUpperCase());
		if(((x>0)
			&&(text().charAt(x-1)=='-')
			&&((y<=0)
			   ||((y>0)&&(text().charAt(y-1)!='+'))))
		 ||((y>0)&&(text().charAt(y-1)=='-')))
			return false;
		return true;
	}

	public boolean okAffect(Affect affect)
	{
		if((affected!=null)
		   &&(affect.target()!=null)
		   &&(affect.target() instanceof Room)
		   &&(affect.targetMinor()==Affect.TYP_ENTER)
		   &&((affect.amITarget(affected))||(affect.tool()==affected)||(affected instanceof Area)))
		{
			Hashtable H=new Hashtable();
			if(text().toUpperCase().indexOf("NOFOL")>=0)
				H.put(affect.source(),affect.source());
			else
			{
				affect.source().getGroupMembers(H);
				for(Enumeration e=H.elements();e.hasMoreElements();)
					((MOB)e.nextElement()).getRideBuddies(H);
			}
			for(Enumeration e=H.elements();e.hasMoreElements();)
				if(passesMuster((MOB)e.nextElement()))
					return super.okAffect(affect);
			affect.source().tell("You are not allowed to go that way.");
			return false;
		}
		return super.okAffect(affect);
	}
}
