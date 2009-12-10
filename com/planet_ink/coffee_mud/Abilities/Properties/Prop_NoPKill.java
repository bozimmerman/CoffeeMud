package com.planet_ink.coffee_mud.Abilities.Properties;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
		if(((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
		||(CMath.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
			&&(msg.target()!=null)
			&&(msg.target() instanceof MOB)
		    &&(!((MOB)msg.target()).isMonster())
		    &&(!msg.source().isMonster()))
		{
			if(CMath.s_int(text())==0)
			{
				msg.source().tell("Player killing is forbidden here.");
				msg.source().setVictim(null);
				return false;
			}
			int levelDiff=msg.source().envStats().level()-((MOB)msg.target()).envStats().level();
			if(levelDiff<0) levelDiff=levelDiff*-1;
			if(levelDiff>CMath.s_int(text()))
			{
				msg.source().tell("Player killing is forbidden for characters whose level difference is greater than "+CMath.s_int(text())+".");
				msg.source().setVictim(null);
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
