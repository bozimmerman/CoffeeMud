package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Skill_FalseArrest extends BardSkill
{
	public String ID() { return "Skill_FalseArrest"; }
	public String name(){ return "False Arrest";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"FALSEARREST"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int overrideMana(){return 50;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(mob==target)
		{
			mob.tell("Arrest whom?!");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}

		Behavior B=null;
		Area A2=null;
		if(mob.location()!=null)
		{
			B=CoffeeUtensils.getLegalBehavior(mob.location());
			if((B==null)||(!B.modifyBehavior(CoffeeUtensils.getLegalObject(mob.location()),target,new Integer(Law.MOD_HASWARRANT))))
				B=null;
			else
				A2=CoffeeUtensils.getLegalObject(mob.location());
		}

		if(B==null)
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
		{
			Area A=(Area)e.nextElement();
			if(Sense.canAccess(mob,A))
			{
				B=CoffeeUtensils.getLegalBehavior(A);
				if((B!=null)
				&&(B.modifyBehavior(CoffeeUtensils.getLegalObject(A),target,new Integer(Law.MOD_HASWARRANT))))
				{
					A2=CoffeeUtensils.getLegalObject(A);
					break;
				}
			}
			B=null;
			A2=null;
		}

		if(B==null)
		{
			mob.tell(target.name()+" is not wanted for anything, anywhere.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(!success)
		{
			beneficialWordsFizzle(mob,target,"<S-NAME> frown(s) at <T-NAMESELF>, but lose(s) the nerve.");
			return false;
		}
		FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_HANDS_ACT,"<S-NAME> frown(s) at <T-NAMESELF>.",CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			Vector V=new Vector();
			V.addElement(new Integer(Law.MOD_ARREST));
			V.addElement(mob);
			if(!B.modifyBehavior(A2,target,V))
			{
				mob.tell("You are not able to arrest "+target.name()+" at this time.");
				return false;
			}
		}
		return success;
	}

}
