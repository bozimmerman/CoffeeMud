package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Train extends StdCommand
{
	public Train(){}

	private String[] access={"TRAIN","TR","TRA"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("You have "+mob.getTrains()+" training sessions. Enter HELP TRAIN for more information.");
			return false;
		}
		commands.removeElementAt(0);

		String abilityName=((String)commands.elementAt(0)).toUpperCase();

		int trainsRequired=1;
		int abilityCode=mob.baseCharStats().getCode(abilityName);
		if(abilityCode>=0)
		{
			if(mob.charStats().getStat(abilityCode)<18)
				trainsRequired=1;
			else
			if(mob.charStats().getStat(abilityCode)<20)
				trainsRequired=2;
			else
			if(mob.charStats().getStat(abilityCode)<22)
				trainsRequired=3;
			else
			if(mob.charStats().getStat(abilityCode)<24)
				trainsRequired=4;
			else
				trainsRequired=5;
		}
		CharClass theClass=null;
		if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
		&&(abilityCode<0))
		{
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if(C.name().toUpperCase().startsWith(abilityName.toUpperCase()))
				{
					if(C.qualifiesForThisClass(mob,false))
					{
						abilityCode=106;
						theClass=C;
					}
					break;
				}
			}
		}

		if(abilityCode<0)
		{
			if("HIT POINTS".startsWith(abilityName.toUpperCase()))
				abilityCode=101;
			else
			if("MANA".startsWith(abilityName.toUpperCase()))
				abilityCode=102;
			else
			if("MOVE".startsWith(abilityName.toUpperCase()))
				abilityCode=103;
			else
			if("GAIN".startsWith(abilityName.toUpperCase()))
				abilityCode=104;
			else
			if("PRACTICES".startsWith(abilityName.toUpperCase()))
				abilityCode=105;
			else
			{
				mob.tell("You don't seem to have "+abilityName+".");
				return false;
			}
		}
		commands.removeElementAt(0);


		if(abilityCode==104)
		{
			if(mob.getPractices()<7)
			{
				mob.tell("You don't seem to have enough practices to do that.");
				return false;
			}
		}
		else
		if(mob.getTrains()==0)
		{
			mob.tell("You don't seem to have enough training sessions to do that.");
			return false;
		}
		else
		if(mob.getTrains()<trainsRequired)
		{
			mob.tell("Training that ability further will require "+trainsRequired+" training points.");
			return false;
		}

		MOB teacher=null;
		if(commands.size()>0)
		{
			teacher=mob.location().fetchInhabitant((String)commands.elementAt(0));
			if(teacher!=null) commands.removeElementAt(0);
		}
		if(teacher==null)
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB possTeach=mob.location().fetchInhabitant(i);
			if((possTeach!=null)&&(possTeach!=mob))
			{
				teacher=possTeach;
				break;
			}
		}
		if((teacher==null)||((teacher!=null)&&(!Sense.canBeSeenBy(teacher,mob))))
		{
			mob.tell("That person doesn't seem to be here.");
			return false;
		}
		if(teacher==mob)
		{
			mob.tell("You cannot train with yourself!");
			return false;
		}
		if(Util.bset(teacher.getBitmap(),MOB.ATT_NOTEACH))
		{
			mob.tell(teacher.name()+" is refusing to teach right now.");
			return false;
		}
		if(Util.bset(mob.getBitmap(),MOB.ATT_NOTEACH))
		{
			mob.tell("You are refusing training at this time.");
			return false;
		}

		if((abilityCode==106)
		&&(!teacher.charStats().getCurrentClass().baseClass().equals(mob.charStats().getCurrentClass().baseClass()))
		&&(teacher.charStats().getClassLevel(theClass)<1))
	    {
			if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("MULTI")))
				mob.tell("You can only learn that from another "+mob.charStats().getCurrentClass().baseClass()+".");
			else
				mob.tell("You can only learn that from another "+theClass.name()+".");
			return false;
		}

		int curStat=-1;
		if(abilityCode<100)
		{
			curStat=mob.baseCharStats().getStat(abilityCode);
			if(curStat>=(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)
						 +mob.charStats().getStat(CharStats.MAX_STRENGTH_ADJ+abilityCode)))
			{
				mob.tell("You cannot train that any further.");
				return false;
			}

			int teachStat=teacher.charStats().getStat(abilityCode);
			if(curStat>teachStat)
			{
				mob.tell("You can only train with someone whose score is higher than yours.");
				return false;
			}
		}

		FullMsg msg=new FullMsg(teacher,mob,null,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> train(s) with <T-NAMESELF>.");
		if(!mob.location().okMessage(mob,msg))
			return false;
		mob.location().send(mob,msg);
		switch(abilityCode)
		{
		case 0:
			mob.tell("You feel stronger!");
			mob.baseCharStats().setStat(CharStats.STRENGTH,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 1:
			mob.tell("You feel smarter!");
			mob.baseCharStats().setStat(CharStats.INTELLIGENCE,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 2:
			mob.tell("You feel more dextrous!");
			mob.baseCharStats().setStat(CharStats.DEXTERITY,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 3:
			mob.tell("You feel healthier!");
			mob.baseCharStats().setStat(CharStats.CONSTITUTION,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 4:
			mob.tell("You feel more charismatic!");
			mob.baseCharStats().setStat(CharStats.CHARISMA,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 5:
			mob.tell("You feel wiser!");
			mob.baseCharStats().setStat(CharStats.WISDOM,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-trainsRequired);
			break;
		case 101:
			mob.tell("You feel even healthier!");
			mob.baseState().setHitPoints(mob.baseState().getHitPoints()+10);
			mob.maxState().setHitPoints(mob.maxState().getHitPoints()+10);
			mob.curState().setHitPoints(mob.curState().getHitPoints()+10);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 102:
			mob.tell("You feel more powerful!");
			mob.baseState().setMana(mob.baseState().getMana()+20);
			mob.maxState().setMana(mob.maxState().getMana()+20);
			mob.curState().setMana(mob.curState().getMana()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 103:
			mob.tell("You feel more rested!");
			mob.baseState().setMovement(mob.baseState().getMovement()+20);
			mob.maxState().setMovement(mob.maxState().getMovement()+20);
			mob.curState().setMovement(mob.curState().getMovement()+20);
			mob.setTrains(mob.getTrains()-1);
			break;
		case 104:
			mob.tell("You feel more trainable!");
			mob.setTrains(mob.getTrains()+1);
			mob.setPractices(mob.getPractices()-7);
			break;
		case 105:
			mob.tell("You feel more educatable!");
			mob.setTrains(mob.getTrains()-1);
			mob.setPractices(mob.getPractices()+5);
			break;
		case 106:
			mob.tell("You have undergone "+theClass.name()+" training!");
			mob.setTrains(mob.getTrains()-1);
			mob.baseCharStats().setCurrentClass(theClass);
			mob.recoverCharStats();
			mob.charStats().getCurrentClass().startCharacter(mob,false,true);
			break;
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
