package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqEntry extends Property
{
	public String ID() { return "Prop_ReqEntry"; }
	public String name(){ return "All Room/Exit Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqEntry newOne=new Prop_ReqEntry(); newOne.setMiscText(text());return newOne;}

	public String accountForYourself()
	{
		return "Entry restricted as follows: "+SaucerSupport.zapperDesc(miscText);
	}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(Sense.isATrackingMonster(mob))
			return true;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		return SaucerSupport.zapperCheck(text(),mob);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(msg.target()!=null)
		   &&(msg.target() instanceof Room)
		   &&(msg.targetMinor()==CMMsg.TYP_ENTER)
		   &&(!Sense.isFalling(msg.source()))
		   &&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			Hashtable H=new Hashtable();
			if(text().toUpperCase().indexOf("NOFOL")>=0)
				H.put(msg.source(),msg.source());
			else
			{
				msg.source().getGroupMembers(H);
				for(Enumeration e=H.elements();e.hasMoreElements();)
					((MOB)e.nextElement()).getRideBuddies(H);
			}
			for(Enumeration e=H.elements();e.hasMoreElements();)
				if(passesMuster((MOB)e.nextElement()))
					return super.okMessage(myHost,msg);
			msg.source().tell("You can not go that way.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}