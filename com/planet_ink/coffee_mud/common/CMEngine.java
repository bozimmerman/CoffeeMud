package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CMEngine extends Scriptable
{
	private CMEngine(){}
	/*
	public static Integer getCommand(String firstWord, Vector commands, boolean exactOnly)
	{
		Integer commandCodeObj=(Integer)commandSet.get(firstWord);
		if((commandCodeObj==null)&&(firstWord.length()>0))
		{
			if(!Character.isLetterOrDigit(firstWord.charAt(0)))
				commandCodeObj=(Integer)commandSet.get(""+firstWord.charAt(0));
			if(commandCodeObj!=null)
			{
				commands.insertElementAt(((String)commands.elementAt(0)).substring(1),1);
				commands.setElementAt(""+firstWord.charAt(0),0);
			}
		}
		if((exactOnly)||(commandCodeObj!=null))
			return commandCodeObj;

		for(Enumeration e=commandSet.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key.toUpperCase().startsWith(firstWord))
			{
				commandCodeObj=(Integer)commandSet.get(key);
				commands.setElementAt(key.toLowerCase(),0);
				break;
			}
		}
		return commandCodeObj;
	}
	*/

	public static int tryCommand(MOB mob, Vector commands, boolean untimed)
		throws Exception
	{
		if(commands.size()==0) return -1;
		if(mob.location()==null) return -1;

		String firstWord=((String)commands.elementAt(0)).toUpperCase();
		if(!Character.isLetterOrDigit(firstWord.charAt(0)))
		{
			commands.insertElementAt(((String)commands.elementAt(0)).substring(1),1);
			commands.setElementAt(""+firstWord.charAt(0),0);
			firstWord=""+firstWord.charAt(0);
		}

		// first, exacting pass
		Command C=CMClass.findCommandByTrigger(firstWord,true);
		if(C!=null)
		{
			if((C.ticksToExecute()>0)&&(!untimed))
				return C.ticksToExecute();
			if(!C.execute(mob,commands))
				return -1;
		}
		

		/*Integer commandCodeObj=getCommand(firstWord,commands,true);
		if((commandCodeObj!=null)
		&&((commandCodeObj.intValue()!=CommandSet.EVOKE)
		   ||(mob.hasAbilityEvoker(firstWord))))
		{
			doCommandCode(mob,commands,commandCodeObj.intValue());
			return;
		}

		/*Social social=Socials.FetchSocial(commands,true);
		if(social!=null)
		{
			social.invoke(mob,commands,null,false);
			return;
		}*/

		mob.tell("Huh?\n\r");
		return -1;
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

	private static Ability getToEvoke(MOB mob, Vector commands)
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
		FullMsg msg=new FullMsg(mob,student,null,CMMsg.MSG_SPEAK,null);
		if(!mob.location().okMessage(mob,msg))
			return;
		msg=new FullMsg(mob,student,null,CMMsg.MSG_TEACH,getScr("AbilityEvoker","teaches",myAbility.name()));
		if(!mob.location().okMessage(mob,msg))
			return;
		myAbility.teach(mob,student);
		mob.location().send(mob,msg);
	}
}
