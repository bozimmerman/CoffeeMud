package com.planet_ink.coffee_mud.Commands.base;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class AbilityEvoker extends Scriptable
{
	private AbilityEvoker(){}

	public static void gain(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()==1)
		{
			mob.tell(getScr("Movement","gainerr"));
			return;
		}
		commands.insertElementAt(getScr("CommandSet","say"),0);
		ExternalPlay.doCommand(mob,commands);
	}
	private static boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(((String)thisAbility.triggerStrings()[i]).equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	private static boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(((String)thisAbility.triggerStrings()[i]).equalsIgnoreCase(thisWord))
			{
				if((thisAbility.name().toUpperCase().startsWith(secondWord)))
					return true;
			}
		}
		return false;
	}

	public static Ability getToEvoke(MOB mob, Vector commands)
	{
		String evokeWord=((String)commands.elementAt(0)).toUpperCase();

		boolean foundMoreThanOne=false;
		Ability evokableAbility=null;
		for(int a=0;a<mob.numAbilities();a++)
		{
			Ability thisAbility=mob.fetchAbility(a);
			if((thisAbility!=null)
			&&(evokedBy(thisAbility,evokeWord)))
				if(evokableAbility!=null)
				{
					foundMoreThanOne=true;
					evokableAbility=null;
					break;
				}
				else
					evokableAbility=thisAbility;
		}

		if((evokableAbility!=null)&&(commands.size()>1))
		{
			int classCode=evokableAbility.classificationCode()&Ability.ALL_CODES;
			switch(classCode)
			{
			case Ability.SPELL:
			case Ability.SONG:
			case Ability.PRAYER:
			case Ability.CHANT:
				evokableAbility=null;
				foundMoreThanOne=true;
				break;
			default:
				break;
			}
		}

		if(evokableAbility!=null)
			commands.removeElementAt(0);
		else
		if((foundMoreThanOne)&&(commands.size()>1))
		{
			commands.removeElementAt(0);
			foundMoreThanOne=false;
			String secondWord=((String)commands.elementAt(0)).toUpperCase();
			for(int a=0;a<mob.numAbilities();a++)
			{
				Ability thisAbility=mob.fetchAbility(a);
				if((thisAbility!=null)
				&&(evokedBy(thisAbility,evokeWord,secondWord.toUpperCase())))
				{
					if(thisAbility.name().equalsIgnoreCase(secondWord))
					{
						evokableAbility=thisAbility;
						foundMoreThanOne=false;
						break;
					}
					else
					if(evokableAbility!=null)
						foundMoreThanOne=true;
					else
						evokableAbility=thisAbility;
				}
			}
			if((evokableAbility!=null)&&(!foundMoreThanOne))
				commands.removeElementAt(0);
			else
			if((foundMoreThanOne)&&(commands.size()>1))
			{
				String secondAndThirdWord=secondWord+" "+((String)commands.elementAt(1)).toUpperCase();

				for(int a=0;a<mob.numAbilities();a++)
				{
					Ability thisAbility=mob.fetchAbility(a);
					if((thisAbility!=null)
					   &&(evokedBy(thisAbility,evokeWord,secondAndThirdWord.toUpperCase())))
					{
						evokableAbility=thisAbility;
						break;
					}
				}
				if(evokableAbility!=null)
				{
					commands.removeElementAt(0);
					commands.removeElementAt(0);
				}
			}
			else
			{
				for(int a=0;a<mob.numAbilities();a++)
				{
					Ability thisAbility=mob.fetchAbility(a);
					if((thisAbility!=null)
					&&(evokedBy(thisAbility,evokeWord))
					&&(thisAbility.name().toUpperCase().indexOf(" "+secondWord.toUpperCase())>0))
					{
						evokableAbility=thisAbility;
						commands.removeElementAt(0);
						break;
					}
				}
			}
		}
		return evokableAbility;
	}

	public static void evoke(MOB mob, Vector commands)
	{
		Ability evokableAbility=getToEvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","evokeerr1"));
			return;
		}
		if((CMAble.qualifyingLevel(mob,evokableAbility)>=0)
		&&(!CMAble.qualifiesByLevel(mob,evokableAbility)))
		{
			mob.tell(getScr("AbilityEvoker","evokeerr2"));
			return;
		}
		evokableAbility.invoke(mob,commands,null,false);
	}

	public static void teach(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell(getScr("AbilityEvoker","teacherr1"));
			return;
		}
		commands.removeElementAt(0);


		MOB student=mob.location().fetchInhabitant((String)commands.elementAt(0));
		if((student==null)||((student!=null)&&(!Sense.canBeSeenBy(student,mob))))
		{
			mob.tell(getScr("AbilityEvoker","teacherr2"));
			return;
		}
		commands.removeElementAt(0);


		String abilityName=Util.combine(commands,0);
		Ability realAbility=CMClass.findAbility(abilityName,student.charStats());
		Ability myAbility=null;
		if(realAbility!=null)
			myAbility=mob.fetchAbility(realAbility.ID());
		else
			myAbility=mob.fetchAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","teacherr3",abilityName));
			return;
		}
		if(!myAbility.canBeTaughtBy(mob,student))
			return;
		if(!myAbility.canBeLearnedBy(mob,student))
			return;
		if(student.fetchAbility(myAbility.ID())!=null)
		{
			mob.tell(getScr("AbilityEvoker","teacherr4",student.name()));
			return;
		}
		FullMsg msg=new FullMsg(mob,student,null,Affect.MSG_SPEAK,null);
		if(!mob.location().okAffect(mob,msg))
			return;
		msg=new FullMsg(mob,student,null,Affect.MSG_NOISYMOVEMENT,getScr("AbilityEvoker","teaches",myAbility.name()));
		if(!mob.location().okAffect(mob,msg))
			return;
		myAbility.teach(mob,student);
		mob.location().send(mob,msg);
	}

	public static void practice(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell(getScr("AbilityEvoker","pracerr1"));
			return;
		}
		commands.removeElementAt(0);

		MOB teacher=null;
		if(commands.size()>1)
		{
			teacher=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if(teacher!=null) commands.removeElementAt(commands.size()-1);
		}

		String abilityName=Util.combine(commands,0);

		if(teacher==null)
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			MOB possTeach=mob.location().fetchInhabitant(i);
			if((possTeach!=null)&&(possTeach.fetchAbility(abilityName)!=null)&&(possTeach!=mob))
			{
				teacher=possTeach;
				break;
			}
		}

		if((teacher==null)||((teacher!=null)&&(!Sense.canBeSeenBy(teacher,mob))))
		{
			mob.tell(getScr("AbilityEvoker","pracerr2"));
			return;
		}

		Ability myAbility=mob.fetchAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","pracerr3",abilityName));
			return;
		}

		Ability teacherAbility=mob.fetchAbility(abilityName);
		if(teacherAbility==null)
		{
			mob.tell(getScr("AbilityEvoker","pracerr4",teacher.name(),abilityName));
			return;
		}

		if(!teacherAbility.canBeTaughtBy(teacher,mob))
			return;
		if(!teacherAbility.canBePracticedBy(teacher,mob))
			return;
		FullMsg msg=new FullMsg(teacher,mob,null,Affect.MSG_SPEAK,null);
		if(!mob.location().okAffect(mob,msg))
			return;
		msg=new FullMsg(teacher,mob,null,Affect.MSG_NOISYMOVEMENT,getScr("AbilityEvoker","practices",myAbility.name()));
		if(!mob.location().okAffect(mob,msg))
			return;
		teacherAbility.practice(teacher,mob);
		mob.location().send(mob,msg);
	}

}
