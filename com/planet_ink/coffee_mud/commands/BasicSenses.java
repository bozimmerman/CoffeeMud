package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import java.util.*;

public class BasicSenses
{
	public static void look(MOB mob, Vector commands, boolean quiet)
	{
		String textMsg="<S-NAME> look(s) ";
		if((commands!=null)&&(commands.size()>1))
		{
			String ID=CommandProcessor.combine(commands,1);
			if(ID.equalsIgnoreCase("SELF"))
				ID=mob.name();
			Environmental thisThang=null;
			int dirCode=Directions.getGoodDirectionCode(ID);
			if(dirCode>=0)
			{
				Room room=mob.location().doors()[dirCode];
				Exit exit=mob.location().exits()[dirCode];
				if((room!=null)&&(exit!=null))
				{
					if(!exit.isOpen())
						thisThang=exit;
					else
					{
						if((room.domainType()&128)==Room.INDOORS)
							thisThang=room;
						else
						{
							mob.tell("You can't see that from here.");
							return;
						}
					}
				}
				else
					dirCode=-1;
			}
			if(dirCode<0)
				thisThang=mob.location().fetchFromMOBRoom(mob,null,ID);
			if(thisThang!=null)
			{
				String name="at "+thisThang.name();
				if(thisThang instanceof Room)
				{
					if(thisThang==mob.location())
						name="around";
					else
					if(dirCode>=0)
						name=Directions.getDirectionName(dirCode);
				}
				FullMsg msg=new FullMsg(mob,thisThang,null,Affect.VISUAL_LOOK,textMsg+"at "+thisThang.name(),Affect.VISUAL_LOOK,textMsg+"at you.",Affect.VISUAL_WNOISE,textMsg+"at "+thisThang.name());
				if(!mob.location().okAffect(msg))
					return;
				thisThang.affect(msg);
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

			FullMsg msg=new FullMsg(mob,mob.location(),null,Affect.VISUAL_LOOK,(quiet?null:textMsg+"around"),Affect.VISUAL_LOOK,(quiet?null:textMsg+"at you."),Affect.VISUAL_WNOISE,(quiet?null:textMsg+"around"));

			if(mob.location().okAffect(msg))
				mob.location().send(mob,msg);
		}
	}

	public static void wimpy(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your wimp level to what?");
			return;
		}
		mob.setWimpHitPoint(Util.s_int(CommandProcessor.combine(commands,1)));
		mob.tell("Your wimp level has been changed to "+mob.getWimpHitPoint()+" hit points.");
	}

	public static void description(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your description to what?");
			return;
		}
		mob.setDescription(CommandProcessor.combine(commands,1));
		mob.tell("Your description has been changed.");
	}

	public static void password(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Change your password to what?");
			return;
		}
		mob.setDescription(CommandProcessor.combine(commands,1));
		mob.tell("Your password has been changed.");
	}

	public static void emote(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("emote what?");
			return;
		}
		String emote="<S-NAME> "+CommandProcessor.combine(commands,1);
		FullMsg msg=new FullMsg(mob,null,null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,emote);
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

	public static void train(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Train what, with whom?");
			return;
		}
		commands.removeElementAt(0);

		if(mob.getTrains()==0)
		{
			mob.tell("You don't seem to have enough training points to do that.");
			return;
		}

		String abilityName=((String)commands.elementAt(0)).toUpperCase();
		int abilityCode=mob.charStats().getAbilityCode(abilityName);

		if(abilityCode<0)
		{
			mob.tell("You don't seem to have "+abilityName+".");
			return;
		}
		commands.removeElementAt(0);


		MOB teacher=mob.location().fetchInhabitant(CommandProcessor.combine(commands,0));
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

		int curStat=mob.charStats().getCurStat(abilityCode);

		if(!mob.charStats().getMyClass().canAdvance(mob,abilityCode))
		{
			mob.tell("You cannot train that any further.");
			return;
		}

		int teachStat=mob.charStats().getCurStat(abilityCode);
		if(curStat>teachStat)
		{
			mob.tell("You can only train with someone whose score is higher than yours.");
			return;
		}

		FullMsg msg=new FullMsg(teacher,mob,null,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,Affect.VISUAL_WNOISE,"<S-NAME> train(s) with <T-NAME>.");
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
		switch(abilityCode)
		{
		case 0:
			mob.tell("You feel stronger!");
			mob.baseCharStats().setStrength(curStat+1);
			break;
		case 1:
			mob.tell("You feel smarter!");
			mob.baseCharStats().setIntelligence(curStat+1);
			break;
		case 2:
			mob.tell("You feel more dextrous!");
			mob.baseCharStats().setDexterity(curStat+1);
			break;
		case 3:
			mob.tell("You feel healthier!");
			mob.baseCharStats().setConstitution(curStat+1);
			break;
		case 4:
			mob.tell("You feel more charismatic!");
			mob.baseCharStats().setCharisma(curStat+1);
			break;
		case 5:
			mob.tell("You feel wiser!");
			mob.baseCharStats().setWisdom(curStat+1);
			break;
		}
		mob.recoverCharStats();
		mob.setTrains(mob.getTrains()-1);
	}
}
