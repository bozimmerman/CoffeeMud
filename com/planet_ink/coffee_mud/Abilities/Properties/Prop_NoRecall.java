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
public class Prop_NoRecall extends Property
{
	public String ID() { return "Prop_NoRecall"; }
	public String name(){ return "Recall Neuralizing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS|Ability.CAN_ITEMS;}

	public String accountForYourself()
	{ return "No Recall Field";	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.sourceMinor()==CMMsg.TYP_RECALL)
		{
            if(((!(myHost instanceof Item))
                ||(msg.source()==((Item)myHost).owner()))
            &&(msg.source()!=null)
			&&(msg.source().location()!=null))
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the magic fizzles.");
			return false;
		}
		return super.okMessage(myHost,msg);
	}
}
