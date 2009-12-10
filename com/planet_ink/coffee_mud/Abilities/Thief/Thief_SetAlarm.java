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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
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
public class Thief_SetAlarm extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_SetAlarm"; }
	public String name(){ return "Set Alarm";}
	protected int canAffectCode(){return Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_EXITS;}
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_TRAPPING;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"SETALARM"};
	public String[] triggerStrings(){return triggerStrings;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	protected boolean sprung=false;
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
    public Vector getTrapComponents() { return new Vector(); }
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{beneficialAffect(mob,E,qualifyingClassLevel+trapBonus,0);return (Trap)E.fetchEffect(ID());}

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
			&&(CMLib.dice().rollPercentage()>msg.source().charStats().getSave(CharStats.STAT_SAVE_TRAPS))))
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
			TrackingLibrary.TrackingFlags flags;
			flags = new TrackingLibrary.TrackingFlags()
					.add(TrackingLibrary.TrackingFlag.OPENONLY)
					.add(TrackingLibrary.TrackingFlag.AREAONLY);
			CMLib.tracking().getRadiantRooms(room1,rooms,flags,null,10+(getXLEVELLevel(invoker())*2),null);
			CMLib.tracking().getRadiantRooms(room2,rooms,flags,null,10+(getXLEVELLevel(invoker())*2),null);
			Vector mobsDone=new Vector();
			room1.showHappens(CMMsg.MSG_NOISE,"A horrible alarm is going off here.");
			room2.showHappens(CMMsg.MSG_NOISE,"A horrible alarm is going off here.");
			for(int r=0;r<rooms.size();r++)
			{
				Room R=(Room)rooms.elementAt(r);
				if((R!=room1)&&(R!=room2))
				{
					int dir=CMLib.tracking().radiatesFromDir(R,rooms);
					if(dir>=0)
					{
						R.showHappens(CMMsg.MSG_NOISE,"You hear a loud alarm "+Directions.getInDirectionName(dir)+".");
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M=R.fetchInhabitant(i);
							if((M!=null)
							&&(M.isMonster())
							&&(!M.isInCombat())
							&&(CMLib.flags().isMobile(M))
							&&(!mobsDone.contains(M))
							&&(CMLib.flags().canHear(M))
							&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_MIND))
							&&(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
							{
								mobsDone.addElement(M);
								CMLib.tracking().move(M,dir,false,false);
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
		String whatToalarm=CMParms.combine(commands,0);
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

		boolean success=proficiencyCheck(mob,0,auto);

		CMMsg msg=CMClass.getMsg(mob,alarmThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_ALWAYS|CMMsg.MSG_THIEF_ACT,CMMsg.MSG_OK_ACTION,(auto?alarmThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap on "+alarmThis.name()+"."));
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
				if(CMLib.dice().rollPercentage()>50)
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
