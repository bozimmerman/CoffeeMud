package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Prop_NoPKill extends Property
{
	public String ID() { return "Prop_NoPKill"; }
	public String name(){ return "No Player Killing";}
	protected int canAffectCode(){return Ability.CAN_ROOMS|Ability.CAN_AREAS;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
			&&(msg.target()!=null)
			&&(msg.target() instanceof MOB)
		    &&(!((MOB)msg.target()).isMonster())
		    &&(!msg.source().isMonster()))
		{
			if(Util.s_int(text())==0)
			{
				msg.source().tell("Player killing is forbidden here.");
				msg.source().setVictim(null);
				return false;
			}
			int levelDiff=msg.source().envStats().level()-((MOB)msg.target()).envStats().level();
			if(levelDiff<0) levelDiff=levelDiff*-1;
			if(levelDiff>Util.s_int(text()))
			{
				msg.source().tell("Player killing is forbidden for characters whose level difference is greater than "+Util.s_int(text())+".");
				msg.source().setVictim(null);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
