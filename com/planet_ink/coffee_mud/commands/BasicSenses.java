package com.planet_ink.coffee_mud.commands;

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
			String ID=Util.combine(commands,1);
			if(ID.equalsIgnoreCase("SELF"))
				ID=mob.name();
			Environmental thisThang=null;
			int dirCode=Directions.getGoodDirectionCode(ID);
			if(dirCode>=0)
			{
				Room room=mob.location().doors()[dirCode];
				Exit exit=mob.location().exits()[dirCode];
				if((room!=null)&&(exit!=null))
					thisThang=exit;
				else
				{
					mob.tell("You don't see anything that way.");
					return;
				}
			}
			if(dirCode<0)
				thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID);
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
					thisThang.affect(msg);
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
		mob.setUserInfo(mob.name(),Util.combine(commands,1),mob.lastDateTime());
		mob.tell("Your password has been changed.");
	}

	public void emote(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("emote what?");
			return;
		}
		String emote="<S-NAME> "+Util.combine(commands,1);
		FullMsg msg=new FullMsg(mob,null,null,Affect.MSG_NOISYMOVEMENT,emote);
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
			if(possTeach!=mob)
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

		int curStat=mob.baseCharStats().getCurStat(abilityCode);

		if(!mob.baseCharStats().getMyClass().canAdvance(mob,abilityCode))
		{
			mob.tell("You cannot train that any further.");
			return;
		}

		int teachStat=mob.baseCharStats().getCurStat(abilityCode);
		if(curStat>teachStat)
		{
			mob.tell("You can only train with someone whose score is higher than yours.");
			return;
		}

		FullMsg msg=new FullMsg(teacher,mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> train(s) with <T-NAMESELF>.");
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

	public void outfit(MOB mob)
	{
		if(mob==null) return;
		if(mob.charStats()==null) return;
		CharClass C=mob.charStats().getMyClass();
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
}
