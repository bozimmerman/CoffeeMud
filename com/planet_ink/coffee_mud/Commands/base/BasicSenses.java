package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BasicSenses
{
	public void look(MOB mob, Vector commands, boolean quiet)
	{
		String textMsg="<S-NAME> look(s) ";
		if(mob.location()==null) return;
		if((commands!=null)&&(commands.size()>1))
		{
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("at")))
			   commands.removeElementAt(1);
			else
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("to")))
			   commands.removeElementAt(1);
			String ID=Util.combine(commands,1);
			
			if((ID.toUpperCase().startsWith("EXIT")&&(commands.size()==2)))
			{
				mob.location().listExits(mob);
				return;
			}
			if(ID.equalsIgnoreCase("SELF"))
				ID=mob.name();
			Environmental thisThang=null;
			int dirCode=Directions.getGoodDirectionCode(ID);
			if(dirCode>=0)
			{
				Room room=mob.location().getRoomInDir(dirCode);
				Exit exit=mob.location().getExitInDir(dirCode);
				if((room!=null)&&(exit!=null))
					thisThang=exit;
				else
				{
					mob.tell("You don't see anything that way.");
					return;
				}
			}
			if(dirCode<0)
			{
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID,Item.WORN_REQ_ANY);
				if((thisThang==null)
				&&(commands.size()>2)
				&&(((String)commands.elementAt(1)).equalsIgnoreCase("in")))
				{
					commands.removeElementAt(1);
					String ID2=Util.combine(commands,1);
					thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID2,Item.WORN_REQ_ANY);
					if((thisThang!=null)&&((!(thisThang instanceof Container))||(((Container)thisThang).capacity()==0)))
					{
						mob.tell("That's not a container.");
						return;
					}
				}
			}
			if(thisThang!=null)
			{
				String name="at <T-NAMESELF>.";
 				if((thisThang instanceof Room)||(thisThang instanceof Exit))
				{
					if(thisThang==mob.location())
						name="around";
					else
					if(dirCode>=0)
						name=Directions.getDirectionName(dirCode);
				}
				FullMsg msg=new FullMsg(mob,thisThang,null,Affect.MSG_EXAMINESOMETHING,textMsg+name);
				
				if(mob.location().okAffect(msg))
					mob.location().send(mob,msg);
				if((thisThang instanceof Room)&&((mob.getBitmap()&MOB.ATT_AUTOEXITS)>0))
					((Room)thisThang).listExits(mob);

			}
			else
				mob.tell("You don't see that here!");
		}
		else
		{
			if((commands!=null)&&(commands.size()>0))
				if(((String)commands.elementAt(0)).toUpperCase().startsWith("E"))
				{
					mob.tell("Examine what?");
					return;
				}

			FullMsg msg=new FullMsg(mob,mob.location(),null,Affect.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"around"),Affect.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"at you."),Affect.MSG_EXAMINESOMETHING,(quiet?null:textMsg+"around"));
			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
			if(((mob.getBitmap()&MOB.ATT_AUTOEXITS)>0)
			&&(Sense.canBeSeenBy(mob.location(),mob)))
				mob.location().listExits(mob);
		}
	}

	public void wimpy(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your wimp level to what?");
			return;
		}
		mob.setWimpHitPoint(Util.s_int(Util.combine(commands,1)));
		mob.tell("Your wimp level has been changed to "+mob.getWimpHitPoint()+" hit points.");
	}

	public void description(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your description to what?");
			return;
		}
		mob.setDescription(Util.combine(commands,1));
		mob.tell("Your description has been changed.");
	}

	public void password(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your password to what?");
			return;
		}
		mob.setUserInfo(mob.name(),Util.combine(commands,1));
		mob.tell("Your password has been changed.");
	}

	public void emote(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("emote what?");
			return;
		}
		String emote="^E<S-NAME> "+Util.combine(commands,1)+"^?";
		FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_EMOTE,emote);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public void mundaneTake(MOB mob, Vector commands)
	{
		if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("off"))
		{
			commands.removeElementAt(commands.size()-1);
			new ItemUsage().remove(mob,commands);
		}
		else
		if((commands.size()>1)&&(((String)commands.elementAt(1)).equalsIgnoreCase("off")))
		{
			commands.removeElementAt(1);
			new ItemUsage().remove(mob,commands);
		}
		else
			new ItemUsage().get(mob,commands);
	}

	public void train(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Train what?");
			return;
		}
		commands.removeElementAt(0);

		String abilityName=((String)commands.elementAt(0)).toUpperCase();
		int abilityCode=mob.charStats().getCode(abilityName);
		CharClass theClass=null;
		if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
		&&(abilityCode<0))
		{
			for(int c=0;c<CMClass.charClasses.size();c++)
			{
				CharClass C=(CharClass)CMClass.charClasses.elementAt(c);
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
				return;
			}
		}
		commands.removeElementAt(0);


		if(abilityCode==104)
		{
			if(mob.getPractices()<7)
			{
				mob.tell("You don't seem to have enough practices to do that.");
				return;
			}
		}
		else
		if(mob.getTrains()==0)
		{
			mob.tell("You don't seem to have enough training sessions to do that.");
			return;
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
			return;
		}
		if(teacher==mob)
		{
			mob.tell("You cannot train with yourself!");
			return;
		}

		if((abilityCode==106)
		&&(!teacher.charStats().getCurrentClass().baseClass().equals(mob.charStats().getCurrentClass().baseClass())))
	    {
			mob.tell("You can only learn that from another "+mob.charStats().getCurrentClass().baseClass()+".");
			return;
		}
																 
		int curStat=-1;
		if(abilityCode<100)
		{
			curStat=mob.baseCharStats().getStat(abilityCode);
			if(!mob.baseCharStats().getCurrentClass().canAdvance(mob,abilityCode))
			{
				mob.tell("You cannot train that any further.");
				return;
			}

			int teachStat=mob.baseCharStats().getStat(abilityCode);
			if(curStat>teachStat)
			{
				mob.tell("You can only train with someone whose score is higher than yours.");
				return;
			}
		}

		FullMsg msg=new FullMsg(teacher,mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> train(s) with <T-NAMESELF>.");
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
		switch(abilityCode)
		{
		case 0:
			mob.tell("You feel stronger!");
			mob.baseCharStats().setStat(CharStats.STRENGTH,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 1:
			mob.tell("You feel smarter!");
			mob.baseCharStats().setStat(CharStats.INTELLIGENCE,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 2:
			mob.tell("You feel more dextrous!");
			mob.baseCharStats().setStat(CharStats.DEXTERITY,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 3:
			mob.tell("You feel healthier!");
			mob.baseCharStats().setStat(CharStats.CONSTITUTION,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 4:
			mob.tell("You feel more charismatic!");
			mob.baseCharStats().setStat(CharStats.CHARISMA,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
			break;
		case 5:
			mob.tell("You feel wiser!");
			mob.baseCharStats().setStat(CharStats.WISDOM,curStat+1);
			mob.recoverCharStats();
			mob.setTrains(mob.getTrains()-1);
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
	}

	public void outfit(MOB mob)
	{
		if(mob==null) return;
		if(mob.charStats()==null) return;
		CharClass C=mob.charStats().getCurrentClass();
		Race R=mob.charStats().getMyRace();
		if(C!=null) C.outfit(mob);
		if(R!=null) R.outfit(mob);
		new Scoring().equipment(mob);
	}

	public void autoExits(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_AUTOEXITS)>0)
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_AUTOEXITS);
			mob.tell("Autoexits has been turned off.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_AUTOEXITS);
			mob.tell("Autoexits has been turned on.");
		}
	}

	public void brief(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_BRIEF)>0)
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_BRIEF);
			mob.tell("Brief room descriptions are now off.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_BRIEF);
			mob.tell("Brief room descriptions are now on.");
		}
	}

	public void autoweather(MOB mob)
	{
		if((mob.getBitmap()&MOB.ATT_AUTOWEATHER)>0)
		{
			mob.setBitmap(mob.getBitmap()-MOB.ATT_AUTOWEATHER);
			mob.tell("Weather descriptions are now off.");
		}
		else
		{
			mob.setBitmap(mob.getBitmap()|MOB.ATT_AUTOWEATHER);
			mob.tell("Weather descriptions are now on.");
		}
	}

	public void ansi(MOB mob, int WhatToDo)
	{
		if(!mob.isMonster())
		{
			switch(WhatToDo)
			{
			case -1:
			{
				if((mob.getBitmap()&MOB.ATT_ANSI)>0)
				{
					mob.setBitmap(mob.getBitmap()-MOB.ATT_ANSI);
					mob.tell("ANSI colour disabled.\n\r");
				}
				else
				{
					mob.setBitmap(mob.getBitmap()|MOB.ATT_ANSI);
					mob.tell("^!ANSI^N ^Hcolour^N enabled.\n\r");
				}
			}
			break;
			case 0:
			{
				if((mob.getBitmap()&MOB.ATT_ANSI)>0)
				{
					mob.setBitmap(mob.getBitmap()-MOB.ATT_ANSI);
					mob.tell("ANSI colour disabled.\n\r");
				}
				else
				{
					mob.tell("ANSI is already disabled.\n\r");
				}
			}
			break;
			case 1:
			{
				if((mob.getBitmap()&MOB.ATT_ANSI)==0)
				{
					mob.setBitmap(mob.getBitmap()|MOB.ATT_ANSI);
					mob.tell("^!ANSI^N ^Hcolour^N enabled.\n\r");
				}
				else
				{
					mob.tell("^!ANSI^N is ^Halready^N enabled.\n\r");
				}
			}
			break;
			}
		}
	}
	
	public void weather(MOB mob, Vector commands)
	{
		Room room=mob.location();
		if(room==null) return;
		if((commands.size()>1)&&((room.domainType()&Room.INDOORS)==0)&&(((String)commands.elementAt(1)).equalsIgnoreCase("WORLD")))
		{
			StringBuffer tellMe=new StringBuffer("");
			for(int a=0;a<CMMap.numAreas();a++)
			{
				Area A=(Area)CMMap.getArea(a);
				if(!Sense.isHidden(A))
					tellMe.append(Util.padRight(A.name(),20)+": "+A.weatherDescription(room)+"\n\r");
			}
			mob.tell(tellMe.toString());
			return;
		}
		mob.tell(room.getArea().weatherDescription(room));
	}
	public void time(MOB mob, Vector commands)
	{
		Room room=mob.location();
		if(room==null) return;
		mob.tell(room.getArea().timeDescription(mob,room));
	}
}
