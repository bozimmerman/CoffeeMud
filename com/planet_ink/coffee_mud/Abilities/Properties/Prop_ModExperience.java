package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ModExperience extends Property
{
	boolean disabled=false;
	public String ID() { return "Prop_ModExperience"; }
	public String name(){ return "Modifying Experience Gained";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS;}

	public String accountForYourself()
	{ return "";	}


	public int translateAmount(int amount, String val)
	{
		if(val.endsWith("%"))
			return (int)Math.round(Util.mul(amount,Util.div(Util.s_int(val.substring(0,val.length()-1)),100)));
		else
			return Util.s_int(val);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_EXPCHANGE)
		&&(((msg.target()==affected)&&(affected instanceof MOB))
		   ||((affected instanceof Item)&&(msg.source()==((Item)affected).owner()))&&(!((Item)affected).amWearingAt(Item.INVENTORY)))
		   ||(affected instanceof Room)
		   ||(affected instanceof Area))
		{
			String s=text().trim();
			int x=s.indexOf(";");
			if(x>=0)
			{
				String mask=s.substring(x+1).trim();
				s=s.substring(0,x).trim();
				if(affected instanceof Item)
				{
					if((mask.length()>0)
					&&((msg.target()==null)||(!(msg.target() instanceof MOB))||(!MUDZapper.zapperCheck(mask,(MOB)msg.target()))))
						return super.okMessage(myHost,msg);
				}
				else
				if((mask.length()>0)
				&&(!MUDZapper.zapperCheck(mask,msg.source())))
					return super.okMessage(myHost,msg);
			}

			if(s.length()==0)
				msg.setValue(0);
			else
			if(s.startsWith("="))
				msg.setValue(translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("+"))
				msg.setValue(msg.value()+translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("-"))
				msg.setValue(msg.value()-translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("*"))
				msg.setValue(msg.value()*translateAmount(msg.value(),s.substring(1)));
			else
			if(s.startsWith("/"))
				msg.setValue((int)Math.round(Util.div(msg.value(),translateAmount(msg.value(),s.substring(1)))));
			else
				msg.setValue(translateAmount(msg.value(),s));
		}
		return super.okMessage(myHost,msg);
	}
}
