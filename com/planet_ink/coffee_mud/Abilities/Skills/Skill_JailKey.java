package com.planet_ink.coffee_mud.Abilities.Skills;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_JailKey extends StdAbility
{
	public String ID() { return "Skill_JailKey"; }
	public String name(){ return "Jail Key";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"JAILKEY","JKEY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String whatTounlock=Util.combine(commands,0);
		Exit unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if((dirCode>=0)&&(mob.location()!=null))
		{
			unlockThis=mob.location().getExitInDir(dirCode);
			Room unlockThat=mob.location().getRoomInDir(dirCode);
			if(unlockThat==null) unlockThis=null;
			if(unlockThis!=null)
			{
				Behavior B=null;
				Vector V=new Vector();
				V.addElement(new Integer(Law.MOD_ISJAILROOM));
				V.addElement(unlockThat);
				V.addElement(mob.location());
				Area legalA=CoffeeUtensils.getLegalObject(mob.location());
				if(legalA!=null) B=CoffeeUtensils.getLegalBehavior(legalA);
				if(B==null) 
				    unlockThis=null;
				else
				if(!B.modifyBehavior(legalA,mob,V))
				    unlockThis=null;
			}
		}
		
		if(unlockThis==null) 
	    {
		    mob.tell("You must specify a jail door direction.");
		    return false;
	    }

		if(!unlockThis.hasALock())
		{
			mob.tell("There is no lock on "+unlockThis.name()+"!");
			return false;
		}

		if(unlockThis.isOpen())
		{
			mob.tell(unlockThis.name()+" is open!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) <S-HIS-HER> jailkey on "+unlockThis.name()+" and fail(s).");
		else
		{
			FullMsg msg=new FullMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_VISUAL:(CMMsg.MSG_THIEF_ACT),CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
			if(mob.location().okMessage(mob,msg))
			{
				if(!unlockThis.isLocked())
					msg=new FullMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> use(s) <S-HIS-HER> jailkey and relock(s) "+unlockThis.name()+".");
				else
					msg=new FullMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> use(s) <S-HIS-HER> jailkey and unlock(s) "+unlockThis.name()+".");
				CoffeeUtensils.roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}
