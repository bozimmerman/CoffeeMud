package com.planet_ink.coffee_mud.Abilities.Fighter;
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

public class Fighter_CalledShot extends Fighter_CalledStrike
{
	public String ID() { return "Fighter_CalledShot"; }
	public String name(){ return "Called Shot";}
	private static final String[] triggerStrings = {"CALLEDSHOT"};
	public String[] triggerStrings(){return triggerStrings;}

	protected boolean prereqs(MOB mob)
	{
		if(mob.isInCombat()&&(mob.rangeToTarget()==0))
		{
			mob.tell("You are too close to perform a called shot!");
			return false;
		}

		Item w=mob.fetchWieldedItem();
		if((w==null)||(!(w instanceof Weapon)))
		{
			mob.tell("You need a weapon to perform a called shot!");
			return false;
		}
		Weapon wp=(Weapon)w;
		if((wp.weaponClassification()!=Weapon.CLASS_RANGED)&&(wp.weaponClassification()!=Weapon.CLASS_THROWN))
		{
			mob.tell("You cannot shoot with "+wp.name()+"!");
			return false;
		}
		return true;
	}
}
