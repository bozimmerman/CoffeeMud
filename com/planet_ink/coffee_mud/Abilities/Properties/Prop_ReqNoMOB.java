package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqNoMOB extends Property
{
	public String ID() { return "Prop_ReqNoMOB"; }
	public String name(){ return "Monster Limitations";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public Environmental newInstance(){	Prop_ReqNoMOB newOne=new Prop_ReqNoMOB();	newOne.setMiscText(text());	return newOne;}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(Sense.isATrackingMonster(mob))
			return true;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		return !mob.isMonster();
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
			msg.source().tell("You are not allowed in there.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}