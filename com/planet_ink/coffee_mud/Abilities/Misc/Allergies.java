package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

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

public class Allergies extends StdAbility
{
	public String ID() { return "Allergies"; }
	public String name(){ return "Allergies";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	public int classificationCode(){return Ability.PROPERTY;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return true;
		return true;
	}
}
