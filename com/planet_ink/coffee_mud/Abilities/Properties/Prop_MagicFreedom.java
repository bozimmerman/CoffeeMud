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
public class Prop_MagicFreedom extends Property
{
	public String ID() { return "Prop_MagicFreedom"; }
	public String name(){ return "Magic Neutralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public String accountForYourself()
	{ return "Anti-Magic Field";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((Util.bset(msg.sourceCode(),CMMsg.MASK_MAGIC))
		||(Util.bset(msg.targetCode(),CMMsg.MASK_MAGIC))
		||(Util.bset(msg.othersCode(),CMMsg.MASK_MAGIC)))
		{
			Room room=null;
			if(affected instanceof Room)
				room=(Room)affected;
			else
			if((msg.source()!=null)
			&&(msg.source().location()!=null))
				room=msg.source().location();
			else
			if((msg.target()!=null)
			&&(msg.target() instanceof MOB)
			&&(((MOB)msg.target()).location()!=null))
				room=((MOB)msg.target()).location();
			if(room!=null)
				room.showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
			return false;
		}
		return true;
	}
}
