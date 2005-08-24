package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
		&&(msg.targetMinor()==CMMsg.TYP_LOOK))
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
