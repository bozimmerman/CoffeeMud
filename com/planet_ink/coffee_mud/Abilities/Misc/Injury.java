package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Injury extends StdAbility
{
	public String ID() { return "Injury"; }
	public String name(){ return "Injury";}
	public String displayText()
	{
		return "";
	}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"INJURE"};
	public String[] triggerStrings(){return triggerStrings;}
	public boolean canBeUninvoked(){return false;}
	public boolean isAutoInvoked(){return true;}
	public int classificationCode(){return Ability.PROPERTY;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		if((msg.target()==affected)
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&(Sense.canBeSeenBy(affected,msg.source()))
		&&(affected instanceof MOB))
		{
			// *TODO Show Injuries
		}
		super.executeMsg(host,msg);
	}
	
	public void setMiscText(String text)
	{
		super.setMiscText(text);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
        return true;
	}
}
