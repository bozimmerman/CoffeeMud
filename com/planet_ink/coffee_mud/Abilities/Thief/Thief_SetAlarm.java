package com.planet_ink.coffee_mud.Abilities.Thief;

import com.planet_ink.coffee_mud.Abilities.Traps.Trap_Trap;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_SetAlarm extends ThiefSkill implements Trap
{
	public String ID() { return "Thief_SetAlarm"; }
	public String name(){ return "Set Alarm";}
	protected int canAffectCode(){return Ability.CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_EXITS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"SETALARM"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_SetAlarm();}
	private boolean sprung=false;
	public Room room1=null;
	public Room room2=null;

	public boolean disabled(){return false;}
	public void disable(){ unInvoke();}
	public void setReset(int Reset){}
	public int getReset(){return 0;}
	public boolean maySetTrap(MOB mob, int asLevel){return false;}
	public boolean canSetTrapOn(MOB mob, Environmental E){return false;}
	public String requiresToSet(){return "";}
	public Trap setTrap(MOB mob, Environmental E, int classLevel, int qualifyingClassLevel)
	{beneficialAffect(mob,E,0);return (Trap)E.fetchAffect(ID());}

	public void spring(MOB M)
	{
		sprung=true;
	}
	
	public void affect(Environmental myHost, Affect affect)
	{
		if(sprung){	return;	}
		super.affect(myHost,affect);

		if((affect.amITarget(affected))&&(affect.targetMinor()==Affect.TYP_OPEN))
		{
			if((!affect.amISource(invoker())
			&&(Dice.rollPercentage()>affect.source().charStats().getStat(CharStats.SAVE_TRAPS))))
				spring(affect.source());
		}
	}

	public void doRoom(int fromDir, Room room, Vector roomsDone, Vector mobsDone, int depth)
	{
		if(depth>=10) return;
		if(room==null) return;
		if(fromDir>=0)
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
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
					ExternalPlay.move(M,fromDir,false,false);
				}
			}
		for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
		{
			Room R=room.getRoomInDir(d);
			Exit E=room.getExitInDir(d);
			if((R!=null)&&(E!=null)&&(E.isOpen())&&(!roomsDone.contains(R)))
			{
				R.showHappens(Affect.MSG_NOISE,"You hear a loud alarm "+Directions.getInDirectionName(Directions.getOpDirectionCode(d))+".");
				roomsDone.addElement(R);
				doRoom(Directions.getOpDirectionCode(d),R,roomsDone,mobsDone,depth+1);
			}
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
			Vector roomsDone=new Vector();
			roomsDone.addElement(room1);
			roomsDone.addElement(room2);
			Vector mobsDone=new Vector();
			room1.showHappens(Affect.MSG_NOISE,"A horrible alarm is going off here.");
			doRoom(-1,room1,roomsDone,mobsDone,0);
			room2.showHappens(Affect.MSG_NOISE,"A horrible alarm is going off here.");
			doRoom(-1,room2,roomsDone,mobsDone,0);
		}
		return true;
	}
		
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
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

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);
		
		FullMsg msg=new FullMsg(mob,alarmThis,this,auto?Affect.MSG_OK_ACTION:Affect.MSG_THIEF_ACT,Affect.MASK_GENERAL|Affect.MSG_THIEF_ACT,Affect.MSG_OK_ACTION,(auto?alarmThis.name()+" begins to glow!":"<S-NAME> attempt(s) to lay a trap on "+alarmThis.name()+"."));
		if(mob.location().okAffect(mob,msg))
		{
			invoker=mob;
			mob.location().send(mob,msg);
			if(success)
			{
				sprung=false;
				room1=mob.location();
				room2=mob.location().getRoomInDir(dirCode);
				mob.tell("You have set the alarm.");
				beneficialAffect(mob,alarmThis,0);
			}
			else
			{
				if(Dice.rollPercentage()>50)
				{
					beneficialAffect(mob,alarmThis,0);
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> trigger(s) the alarm on accident!");
					Trap T=(Trap)alarmThis.fetchAffect(ID());
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