package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
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
public class Thief_SetAlarm extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_SetAlarm"; }
	public String name(){ return "Set Alarm";}
	protected int canAffectCode(){return Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SETALARM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	private boolean sprung=false;
	public Room room1=null;
	public Room room2=null;

	public boolean isABomb(){return false;}
	public void activateBomb(){}
	public boolean sprung(){return sprung;}
	public boolean disabled(){return false;}
	public void disable(){ unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{beneficialAffect(mob,E,classLevel,0);return (Trap)E.fetchEffect(ID());}

	public void spring(MOB M)
	{
		sprung=true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(sprung){	return;	}
		super.executeMsg(myHost,msg);

		if((msg.amITarget(affected))&&(msg.targetMinor()==CMMsg.TYP_OPEN))
		{
			if((!msg.amISource(invoker())
			&&(Dice.rollPercentage()>msg.source().charStats().getStat(CharStats.SAVE_TRAPS))))
				spring(msg.source());
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected==null)||(!(affected instanceof Exit))||(room1==null)||(room2==null))
			return false;
		if(sprung)
		{
			Vector rooms=new Vector();
			MUDTracker.getRadiantRooms(room1,rooms,true,true,false,null,10);
			MUDTracker.getRadiantRooms(room2,rooms,true,true,false,null,10);
			Vector mobsDone=new Vector();
			room1.showHappens(CMMsg.MSG_NOISE,"A horrible alarm is going off here.");
			room2.showHappens(CMMsg.MSG_NOISE,"A horrible alarm is going off here.");
			for(int r=0;r<rooms.size();r++)
			{
				Room R=(Room)rooms.elementAt(r);
				if((R!=room1)&&(R!=room2))
				{
					int dir=MUDTracker.radiatesFromDir(R,rooms);
					if(dir>=0)
					{
						R.showHappens(CMMsg.MSG_NOISE,"You hear a loud alarm "+Directions.getInDirectionName(dir)+".");
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.isMonster())
							&&(!M.isInCombat())
							&&(Sense.isMobile(M))
							&&(!mobsDone.contains(M))
							&&(Sense.canHear(M))
							&&(Dice.rollPercentage()>M.charStats().getSave(CharStats.SAVE_MIND))
							&&(Dice.rollPercentage()>M.charStats().getSave(CharStats.SAVE_TRAPS)))
							{
								mobsDone.addElement(M);
								MUDTracker.move(M,dir,false,false);
							}
						}
					}
				}

			}
		}
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		String whatToalarm=Util.combine(commands,0);
		Exit alarmThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToalarm);
		if(dirCode>=0)
			alarmThis=mob.location().getExitInDir(dirCode);
		if((alarmThis==null)||(!alarmThis.hasADoor()))
		{
			mob.tell("You can't set an alarm that way.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		FullMsg msg=new FullMsg(mob,alarmThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_GENERAL|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?alarmThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap on "+alarmThis.name()+"."));
		if(mob.location().okMessage(mob,msg))
		{
			invoker=mob;
			mob.location().send(mob,msg);
			if(success)
			{
				sprung=false;
				room1=mob.location();
				room2=mob.location().getRoomInDir(dirCode);
				mob.tell("You have set the alarm.");
				beneficialAffect(mob,alarmThis,asLevel,0);
			}
			else
			{
				if(Dice.rollPercentage()>50)
				{
					beneficialAffect(mob,alarmThis,asLevel,0);
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> trigger(s) the alarm on accident!");
					Trap T=(Trap)alarmThis.fetchEffect(ID());
					if(T!=null) T.spring(mob);
				}
				else
				{
					mob.tell("You fail in your attempt to set an alarm.");
				}
			}
		}
		return success;
	}
}
