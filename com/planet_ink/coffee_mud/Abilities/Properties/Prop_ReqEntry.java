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
		return "Entry restricted as follows: "+MUDZapper.zapperDesc(miscText);
	}

	public boolean passesMuster(MOB mob)
	{
		if(mob==null) return false;
		if(Sense.isATrackingMonster(mob))
			return true;
		if(Sense.isSneaking(mob)&&(text().toUpperCase().indexOf("NOSNEAK")<0))
			return true;
		return MUDZapper.zapperCheck(text(),mob);
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)&&(msg.target()!=null))
		{
			if((msg.target() instanceof Room)
			&&(msg.targetMinor()==CMMsg.TYP_ENTER)
			&&(!Sense.isFalling(msg.source()))
			&&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
			{
				HashSet H=new HashSet();
				if(text().toUpperCase().indexOf("NOFOL")>=0)
					H.add(msg.source());
				else
				{
					msg.source().getGroupMembers(H);
					for(Iterator e=H.iterator();e.hasNext();)
						((MOB)e.next()).getRideBuddies(H);
				}
				for(Iterator e=H.iterator();e.hasNext();)
					if(passesMuster((MOB)e.next()))
						return super.okMessage(myHost,msg);
				msg.source().tell("You can not go that way.");
				return false;
			}
			else
			if((msg.target() instanceof Rideable)
			&&(msg.amITarget(affected)))
			{
				switch(msg.targetMinor())
				{
				case CMMsg.TYP_SIT:
				case CMMsg.TYP_ENTER:
				case CMMsg.TYP_SLEEP:
				case CMMsg.TYP_MOUNT:
					{
						HashSet H=new HashSet();
						if(text().toUpperCase().indexOf("NOFOL")>=0)
							H.add(msg.source());
						else
						{
							msg.source().getGroupMembers(H);
							for(Iterator e=H.iterator();e.hasNext();)
								((MOB)e.next()).getRideBuddies(H);
						}
						for(Iterator e=H.iterator();e.hasNext();)
							if(passesMuster((MOB)e.next()))
								return super.okMessage(myHost,msg);
						msg.source().tell("You are not permitted in there.");
						return false;
					}
				default:
					break;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}
}