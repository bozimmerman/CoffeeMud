package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_RoomView extends Property
{
	public String ID() { return "Prop_RoomView"; }
	public String name(){ return "Different Room View";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	private Room newRoom=null;

	public String accountForYourself()
	{ return "Different View of "+text();	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((newRoom==null)||(!CMMap.getExtendedRoomID(newRoom).equalsIgnoreCase(text().trim())))
			newRoom=CMMap.getRoom(text());
		if(newRoom==null) return super.okMessage(myHost,msg);

		if((affected!=null)
		&&((affected instanceof Room)||(affected instanceof Exit)||(affected instanceof Item))
		&&(msg.amITarget(affected))
		&&(newRoom.fetchEffect(ID())==null)
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING))
		{
			FullMsg msg2=new FullMsg(msg.source(),newRoom,msg.tool(),
						  msg.sourceCode(),msg.sourceMessage(),
						  msg.targetCode(),msg.targetMessage(),
						  msg.othersCode(),msg.othersMessage());
			if(newRoom.okMessage(msg.source(),msg2))
			{
				newRoom.executeMsg(msg.source(),msg2);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

}
