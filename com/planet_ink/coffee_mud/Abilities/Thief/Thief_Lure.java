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
public class Thief_Lure extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_Lure"; }
	public String name(){ return "Lure";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"LURE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public boolean disabled(){return false;}
	public boolean sprung(){return false;}
	public void disable(){ unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public void spring(MOB M){}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{return null;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("Lure whom which direction?");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are fighting!");
			return false;
		}
		String str=(String)commands.lastElement();
		commands.removeElementAt(commands.size()-1);
		int dirCode=Directions.getGoodDirectionCode(str);
		if((dirCode<0)||(mob.location()==null)||(mob.location().getRoomInDir(dirCode)==null)||(mob.location().getExitInDir(dirCode)==null))
		{
			mob.tell("'"+str+"' is not a valid direction.");
			return false;
		}
		String direction=Directions.getInDirectionName(dirCode);

		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());

		boolean success=profficiencyCheck(mob,-(levelDiff*(!Sense.canBeSeenBy(mob,target)?5:10)),auto);
		success=success&&(Dice.rollPercentage()>target.charStats().getSave(CharStats.SAVE_TRAPS));
		success=success&&(Dice.rollPercentage()>target.charStats().getSave(CharStats.SAVE_MIND));

		str="<S-NAME> lure(s) <T-NAME> "+direction+".";
		FullMsg msg=new FullMsg(mob,target,this,(auto?CMMsg.MASK_GENERAL:0)|CMMsg.MSG_SPEAK,str);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			if((success)&&(MUDTracker.move(mob,dirCode,false,false))&&(Sense.canBeHeardBy(target,mob)))
				MUDTracker.move(target,dirCode,false,false);
		}
		return success;
	}

}
