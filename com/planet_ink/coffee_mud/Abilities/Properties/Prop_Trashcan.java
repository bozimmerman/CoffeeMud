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
public class Prop_Trashcan extends Property
{
	public String ID() { return "Prop_Trashcan"; }
	public String name(){ return "Auto purges items put into a container";}
	protected int canAffectCode(){return Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected instanceof Item)
		&&(msg.targetMinor()==CMMsg.TYP_PUT)
		&&(msg.amITarget(affected))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
			((Item)msg.tool()).destroy();
		else
		if((affected instanceof Room)
		&&(msg.targetMinor()==CMMsg.TYP_DROP)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Item))
			((Item)msg.target()).destroy();
	}
}
