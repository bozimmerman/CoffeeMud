package com.planet_ink.coffee_mud.Abilities.Thief;

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
public class Thief_Autosneak extends ThiefSkill
{
	public String ID() { return "Thief_Autosneak"; }
	public String displayText() {return "(AutoSneak)";}
	public String name(){ return "AutoSneak";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"AUTOSNEAK"};
	public String[] triggerStrings(){return triggerStrings;}
	private boolean noRepeat=false;

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if((student.fetchAbility("Thief_Sneak")==null)
		   &&(student.fetchAbility("Ranger_Sneak")==null))
		{
			teacher.tell(student.name()+" has not yet learned to sneak.");
			student.tell("You need to learn to sneak first.");
			return false;
		}

		return true;
	}


	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected instanceof MOB)
		&&(!noRepeat)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(((MOB)affected).location()!=null))
		{
			int dir=-1;
			MOB mob=(MOB)affected;
			for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				if((mob.location().getRoomInDir(d)==msg.target())
				||(mob.location().getReverseExit(d)==msg.tool())
				||(mob.location().getExitInDir(d)==msg.tool()))
				{ dir=d; break;}
			if(dir>=0)
			{
				Ability A=mob.fetchAbility("Thief_Sneak");
				if(A==null) A=mob.fetchAbility("Ranger_Sneak");
				if(A!=null)
				{
					noRepeat=true;
					if(A.invoke(mob,Util.parse(Directions.getDirectionName(dir)),null,false,0))
					{
						int[] usage=A.usageCost(mob);
						if(Util.bset(A.usageType(),Ability.USAGE_HITPOINTS)&&(usage[USAGE_HITPOINTSINDEX]>0))
							mob.curState().adjHitPoints(usage[USAGE_HITPOINTSINDEX]/2,mob.maxState());
						if(Util.bset(A.usageType(),Ability.USAGE_MANA)&&(usage[USAGE_MANAINDEX]>0))
							mob.curState().adjMana(usage[USAGE_MANAINDEX]/2,mob.maxState());
						if(Util.bset(A.usageType(),Ability.USAGE_MOVEMENT)&&(usage[USAGE_MOVEMENTINDEX]>0))
							mob.curState().adjMovement(usage[USAGE_MOVEMENTINDEX]/2,mob.maxState());
					}
					if(Dice.rollPercentage()<10)
						helpProfficiency(mob);
					noRepeat=false;
				}
				return false;
			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.fetchEffect(ID())!=null))
		{
			mob.tell("You are no longer automatically sneaking around.");
			mob.delEffect(mob.fetchEffect(ID()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			mob.tell("You will now automatically sneak around while you move.");
			beneficialAffect(mob,mob,asLevel,0);
			Ability A=mob.fetchEffect(ID());
			if(A!=null) A.makeLongLasting();
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to get into <S-HIS-HER> sneaking stance, but fail(s).");
		return success;
	}

}
