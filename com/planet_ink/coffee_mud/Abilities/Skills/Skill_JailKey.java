package com.planet_ink.coffee_mud.Abilities.Skills;
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
@SuppressWarnings("unchecked")
public class Skill_JailKey extends StdSkill
{
	public String ID() { return "Skill_JailKey"; }
	public String name(){ return "Jail Key";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_EXITS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"JAILKEY","JKEY"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_LEGAL; }
	public int code=0;

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String whatTounlock=CMParms.combine(commands,0);
		Exit unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if((dirCode>=0)&&(mob.location()!=null))
		{
			unlockThis=mob.location().getExitInDir(dirCode);
			Room unlockThat=mob.location().getRoomInDir(dirCode);
			if(unlockThat==null) unlockThis=null;
			if(unlockThis!=null)
			{
                LegalBehavior B=null;
                
				Area legalA=CMLib.law().getLegalObject(mob.location());
				if(legalA!=null) B=CMLib.law().getLegalBehavior(legalA);
				if(B==null) 
				    unlockThis=null;
				else
				if(!B.isJailRoom(legalA,CMParms.makeVector(mob.location())))
				    unlockThis=null;
			}
		}
		
		if(unlockThis==null) 
	    {
		    if(dirCode<0)
    		    mob.tell("You should specify a direction.");
		    else
		    {
		        Exit E=mob.location().getExitInDir(dirCode);
		        if(E==null)
	                mob.tell("You must specify a jail door direction.");
		        else
		        if(!E.hasADoor())
                    mob.tell("You must specify a jail **DOOR** direction.");
		        else
		        if(!E.hasALock())
                    mob.tell("You must specify a **JAIL** door direction.");
		        else
		        if(E.isOpen())
                    mob.tell(E.name()+" is open already.");
		        else
                    mob.tell("That's not a jail door.");
		    }
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

		boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
			beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) <S-HIS-HER> jailkey on "+unlockThis.name()+" and fail(s).");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_VISUAL:(CMMsg.MSG_THIEF_ACT),CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
			if(mob.location().okMessage(mob,msg))
			{
				if(!unlockThis.isLocked())
					msg=CMClass.getMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> use(s) <S-HIS-HER> jailkey and relock(s) "+unlockThis.name()+".");
				else
					msg=CMClass.getMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,auto?unlockThis.name()+" vibrate(s) and click(s).":"<S-NAME> use(s) <S-HIS-HER> jailkey and unlock(s) "+unlockThis.name()+".");
				CMLib.utensils().roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}
