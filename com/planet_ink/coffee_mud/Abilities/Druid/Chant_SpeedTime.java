package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_SpeedTime extends Chant
{
	public String ID() { return "Chant_SpeedTime"; }
	public String name(){ return "Speed Time";}
	public String displayText(){return "(Speed Time)";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 100;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"Something is happening!":"^S<S-NAME> begin(s) to chant...^?");
			if(mob.location().okMessage(mob,msg))
			{
				int mana=mob.curState().getMana();
				mob.location().send(mob,msg);
				for(int i=0;i<(adjustedLevel(mob)/2);i++)
					CMClass.ThreadEngine().tickAllTickers(mob.location());
				if(mob.curState().getMana()>mana)
					mob.curState().setMana(mana);
				mob.location().show(mob,null,this,affectType(auto),auto?"It stops.":"^S<S-NAME> stop(s) chanting.^?");
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> chant(s), but nothing happens.");

		return success;
	}
}