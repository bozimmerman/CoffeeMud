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
public class Prop_ReqPKill extends Property
{
	public String ID() { return "Prop_ReqPKill"; }
	public String name(){ return "Playerkill ONLY Zone";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_EXITS;}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(msg.target()!=null)
		   &&(((msg.target() instanceof Room)&&(msg.targetMinor()==CMMsg.TYP_ENTER))
			  ||((msg.target() instanceof Rideable)&&(msg.targetMinor()==CMMsg.TYP_SIT)))
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
