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
public class Thief_RemoveTraps extends ThiefSkill
{
	public String ID() { return "Thief_RemoveTraps"; }
	public String name(){ return "Remove Traps";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"DETRAP","UNTRAP","REMOVETRAPS"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental lastChecked=null;
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_DETRAP;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
    public Vector lastDone=new Vector();

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
        boolean saveTheTrap=false;
        if((commands.size()>0)&&(commands.lastElement() instanceof Boolean))
        {
            saveTheTrap=((Boolean)commands.lastElement()).booleanValue();
            commands.removeElementAt(commands.size()-1);
        }
		String whatTounlock=CMParms.combine(commands,0);
		Environmental unlockThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatTounlock);
		Room R=mob.location();
		Room nextRoom=null;
		if(dirCode>=0)
		{
			nextRoom=R.getRoomInDir(dirCode);
			unlockThis=R.getExitInDir(dirCode);
		}
		if((unlockThis==null)&&(whatTounlock.equalsIgnoreCase("room")||whatTounlock.equalsIgnoreCase("here")))
			unlockThis=R;
		if(unlockThis==null)
			unlockThis=getAnyTarget(mob,commands,givenTarget,Wearable.FILTER_UNWORNONLY);
		if(unlockThis==null) return false;
		int oldProficiency=proficiency();

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,+(((mob.envStats().level()+(getXLEVELLevel(mob)*2))
											 -unlockThis.envStats().level())*3),auto);
		Vector permSetV=new Vector();
		Trap theTrap=CMLib.utensils().fetchMyTrap(unlockThis);
		if(theTrap!=null) permSetV.addElement(unlockThis);
		Trap opTrap=null;
		boolean permanent=false;
		if((unlockThis instanceof Room)
		&&(CMLib.law().doesOwnThisProperty(mob,((Room)unlockThis))))
			permanent=true;
		else
		if(unlockThis instanceof Exit)
		{
			Room R2=null;
			if(dirCode<0)
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				if(R.getExitInDir(d)==unlockThis){ dirCode=d; R2=R.getRoomInDir(d); break;}
			if((CMLib.law().doesOwnThisProperty(mob,R))
			||((R2!=null)&&(CMLib.law().doesOwnThisProperty(mob,R2))))
				permanent=true;
			if(dirCode>=0)
			{
				Exit exit=R.getReverseExit(dirCode);
				if(exit!=null)
					opTrap=CMLib.utensils().fetchMyTrap(exit);
				if(opTrap!=null) permSetV.addElement(exit);
				Trap roomTrap=null;
				if(nextRoom!=null) roomTrap=CMLib.utensils().fetchMyTrap(nextRoom);
				if(roomTrap!=null) permSetV.addElement(nextRoom);
				if((theTrap!=null)&&(theTrap.disabled())&&(roomTrap!=null))
				{
					opTrap=null;
					unlockThis=nextRoom;
					theTrap=roomTrap;
				}
			}
		}
		CMMsg msg=CMClass.getMsg(mob,unlockThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_ACTION,auto?unlockThis.name()+" begins to glow.":"<S-NAME> attempt(s) to safely deactivate a trap on "+unlockThis.name()+".");
        if((success)&&(!lastDone.contains(""+unlockThis)))
        {
            while(lastDone.size()>40) lastDone.removeElementAt(0);
            lastDone.addElement(""+unlockThis);
            msg.setValue(1);
        }
        else
            msg.setValue(0);
		if(R.okMessage(mob,msg))
		{
			R.send(mob,msg);
			if((unlockThis==lastChecked)&&((theTrap==null)||(theTrap.disabled())))
				setProficiency(oldProficiency);
			if(success)
			{
				if(theTrap!=null)
                {
					theTrap.disable();
                    if(saveTheTrap)
                        commands.addElement(theTrap);
                }
				if(opTrap!=null)
                {
					opTrap.disable();
                    if(saveTheTrap)
                        commands.addElement(opTrap);
                }
				if(permanent)
				{
					for(int i=0;i<permSetV.size();i++)
					{
						if(theTrap!=null) { 
                            theTrap.unInvoke(); 
                            ((Environmental)permSetV.elementAt(i)).delEffect(theTrap);
                        }
						if(opTrap!=null) { 
                            opTrap.unInvoke(); 
                            ((Environmental)permSetV.elementAt(i)).delEffect(opTrap);
                        }
					}
					CMLib.database().DBUpdateRoom(R);
					CMLib.database().DBUpdateExits(R);
				}
			}
			if((!auto)&&(!saveTheTrap))
				mob.tell("You have completed your attempt.");
			lastChecked=unlockThis;
		}

		return success;
	}
}
