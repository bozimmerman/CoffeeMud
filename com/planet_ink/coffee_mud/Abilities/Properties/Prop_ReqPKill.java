package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ReqPKill extends Property
{
	public String ID() { return "Prop_ReqPKill"; }
	public String name(){ return "Playerkill ONLY Zone";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(msg.target()!=null)
		   &&(msg.target() instanceof Room)
		   &&(msg.targetMinor()==CMMsg.TYP_ENTER)
		   &&(!Sense.isFalling(msg.source()))
		   &&((msg.amITarget(affected))||(msg.tool()==affected)||(affected instanceof Area)))
		{
			if((!msg.source().isMonster())
			   &&(!Util.bset(msg.source().getBitmap(),MOB.ATT_PLAYERKILL)))
			{
				msg.source().tell("You must have your playerkill flag set to enter here.");
				return false;
			}
		}
		if((!msg.source().isMonster())
		&&(!Util.bset(msg.source().getBitmap(),MOB.ATT_PLAYERKILL)))
			msg.source().setBitmap(Util.setb(msg.source().getBitmap(),MOB.ATT_PLAYERKILL));
		return super.okMessage(myHost,msg);
	}
}
