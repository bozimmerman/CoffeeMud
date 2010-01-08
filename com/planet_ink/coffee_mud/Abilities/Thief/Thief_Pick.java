package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_Pick extends ThiefSkill
{
	public String ID() { return "Thief_Pick"; }
	public String name(){ return "Pick Locks";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"PICK"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;}
	public int code=0;
    public Vector lastDone=new Vector();

	public int abilityCode(){return code;}
	public void setAbilityCode(int newCode){code=newCode;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String whatTounlock=CMParms.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		if(dirCode>=0)
			unlockThis=mob.location().getExitInDir(dirCode);
		if(unlockThis==null)
			unlockThis=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(unlockThis==null) return false;

		if(((unlockThis instanceof Exit)&&(!((Exit)unlockThis).hasALock()))
		||((unlockThis instanceof Container)&&(!((Container)unlockThis).hasALock()))
		||((unlockThis instanceof Item)&&(!(unlockThis instanceof Container))))
		{
			mob.tell("There is no lock on "+unlockThis.name()+"!");
			return false;
		}

		if(((unlockThis instanceof Exit)&&(((Exit)unlockThis).isOpen()))
		||((unlockThis instanceof Container)&&(((Container)unlockThis).isOpen())))
		{
			mob.tell(unlockThis.name()+" is open!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int adjustment=((mob.envStats().level()+abilityCode()+(2*super.getXLEVELLevel(mob)))-unlockThis.envStats().level())*5;
		if(adjustment>0) adjustment=0;
		boolean success=proficiencyCheck(mob,adjustment,auto);

		if(!success)
			beneficialVisualFizzle(mob,unlockThis,"<S-NAME> attempt(s) to pick the lock on <T-NAME> and fail(s).");
		else
		{
			CMMsg msg=CMClass.getMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_VISUAL:(CMMsg.MSG_THIEF_ACT),CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
            msg.setValue(0);
			if(mob.location().okMessage(mob,msg))
			{
				if(((unlockThis instanceof Exit)&&(!((Exit)unlockThis).isLocked()))
				||((unlockThis instanceof Container)&&(!((Container)unlockThis).isLocked())))
					msg=CMClass.getMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_LOCK,CMMsg.MSG_OK_VISUAL,auto?"<T-NAME> vibrate(s) and click(s).":"<S-NAME> pick(s) and relock(s) <T-NAME>.");
				else
					msg=CMClass.getMsg(mob,unlockThis,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,auto?"<T-NAME> vibrate(s) and click(s).":"<S-NAME> pick(s) the lock on <T-NAME>.");
                if(!lastDone.contains(""+unlockThis))
                {
                    while(lastDone.size()>40) lastDone.removeElementAt(0);
                    lastDone.addElement(""+unlockThis);
                    msg.setValue(1);
                }
				CMLib.utensils().roomAffectFully(msg,mob.location(),dirCode);
			}
		}

		return success;
	}
}
