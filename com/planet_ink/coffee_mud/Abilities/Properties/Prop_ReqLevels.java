package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqLevels extends Property
{
	public String ID() { return "Prop_ReqLevels"; }
	public String name(){ return "Level Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqLevels newOne=new Prop_ReqLevels();	newOne.setMiscText(text());	return newOne;}

	public boolean passesMuster(MOB mob, Room R)
	{
		if(mob==null) return false;
		if(Sense.isATrackingMonster(mob))
			return true;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;

		if((text().toUpperCase().indexOf("ALL")>=0)||(text().length()==0)||(mob.isASysOp(R)))
			return true;

		if((text().toUpperCase().indexOf("SYSOP")>=0)&&(!mob.isASysOp(R)))
			return false;

		int lvl=mob.envStats().level();

		int lastPlace=0;
		int x=0;
		while(x>=0)
		{
			x=text().indexOf(">",lastPlace);
			if(x<0)	x=text().indexOf("<",lastPlace);
			if(x<0)	x=text().indexOf("=",lastPlace);
			if(x>=0)
			{
				char primaryChar=text().charAt(x);
				x++;
				boolean ok=false;
				boolean andEqual=false;
				if(text().charAt(x)=='=')
				{
					andEqual=true;
					x++;
				}
				lastPlace=x;

				String cmpString="";
				while((x<text().length())&&
					  (((text().charAt(x)==' ')&&(cmpString.length()==0))
					   ||(Character.isDigit(text().charAt(x)))))
				{
					if(Character.isDigit(text().charAt(x)))
						cmpString+=text().charAt(x);
					x++;
				}
				if(cmpString.length()>0)
				{
					int cmpLevel=Util.s_int(cmpString);
					if((cmpLevel==lvl)&&(andEqual))
						ok=true;
					else
					switch(primaryChar)
					{
					case '>': ok=(lvl>cmpLevel); break;
					case '<': ok=(lvl<cmpLevel); break;
					case '=': ok=(lvl==cmpLevel); break;
					}
				}
				return ok;
			}
		}
		return true;
	}
	public boolean okAffect(Environmental myHost, Affect affect)
	{
		if((affected!=null)
		   &&(affect.target()!=null)
		   &&(affect.target() instanceof Room)
		   &&(affect.targetMinor()==Affect.TYP_ENTER)
		   &&(!Sense.isFalling(affect.source()))
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
				if(passesMuster((MOB)e.nextElement(),(Room)affect.target()))
					return super.okAffect(myHost,affect);
			affect.source().tell("You are not allowed to go that way.");
			return false;
		}
		return super.okAffect(myHost,affect);
	}
}