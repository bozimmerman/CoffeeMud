package com.planet_ink.coffee_mud.commands;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class AbilityEvoker
{
	private boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().size();i++)
		{
			if(((String)thisAbility.triggerStrings().elementAt(i)).equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}

	private boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().size();i++)
		{
			if(((String)thisAbility.triggerStrings().elementAt(i)).equalsIgnoreCase(thisWord))
			{
				if((thisAbility.name().toUpperCase().startsWith(secondWord)))
					return true;
			}
		}
		return false;
	}

	public void evoke(MOB mob, Vector commands)
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
					{
						foundMoreThanOne=true;
						break;
					}
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

		if(evokableAbility==null)
		{
			mob.tell("You don't know how to do that.");
			return;
		}
		if(evokableAbility.envStats().level()>mob.envStats().level())
		{
			mob.tell("You are not high enough level to do that.");
			return;
		}
		evokableAbility.invoke(mob,commands,null,false);
	}

	public void teach(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Teach who what?");
			return;
		}
		commands.removeElementAt(0);


		MOB student=mob.location().fetchInhabitant((String)commands.elementAt(0));
		if((student==null)||((student!=null)&&(!Sense.canBeSeenBy(student,mob))))
		{
			mob.tell("That person doesn't seem to be here.");
			return;
		}
		commands.removeElementAt(0);


		String abilityName=Util.combine(commands,0);
		Ability myAbility=mob.fetchAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell("You don't seem to know "+abilityName+".");
			return;
		}
		if(!myAbility.canBeTaughtBy(mob,student))
			return;
		if(!myAbility.canBeLearnedBy(mob,student))
			return;
		if(student.fetchAbility(myAbility.ID())!=null)
		{
			mob.tell(student.name()+" already knows how to do that.");
			return;
		}
		FullMsg msg=new FullMsg(mob,student,null,Affect.MSG_SPEAK,null);
		if(!mob.location().okAffect(msg))
			return;
		msg=new FullMsg(mob,student,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> teach(es) <T-NAMESELF> '"+myAbility.name()+"'.");
		if(!mob.location().okAffect(msg))
			return;
		myAbility.teach(mob,student);
		mob.location().send(mob,msg);
	}

	public void practice(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Practice what?");
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
			mob.tell("That person doesn't seem to be here.");
			return;
		}

		Ability myAbility=mob.fetchAbility(abilityName);
		if(myAbility==null)
		{
			mob.tell("You don't seem to know "+abilityName+".");
			return;
		}

		Ability teacherAbility=mob.fetchAbility(abilityName);
		if(teacherAbility==null)
		{
			mob.tell(teacher.name()+" doesn't seem to know "+abilityName+".");
			return;
		}

		if(!teacherAbility.canBeTaughtBy(teacher,mob))
			return;
		if(!teacherAbility.canBePracticedBy(teacher,mob))
			return;
		FullMsg msg=new FullMsg(teacher,mob,null,Affect.MSG_SPEAK,null);
		if(!mob.location().okAffect(msg))
			return;
		msg=new FullMsg(teacher,mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> practice(s) '"+myAbility.name()+"' with <T-NAMESELF>.");
		if(!mob.location().okAffect(msg))
			return;
		teacherAbility.practice(teacher,mob);
		mob.location().send(mob,msg);
	}
}
