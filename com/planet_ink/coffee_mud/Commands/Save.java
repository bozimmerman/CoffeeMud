package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Save extends StdCommand
{
	public Save(){}

	private String[] access={"SAVE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("Your game is automatically being saved while you play.\n\r");
			return false;
		}
		String commandType="";
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("USERS"))
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session session=(Session)Sessions.elementAt(s);
				MOB M=session.mob();
				if(M!=null)
				{
					CMClass.DBEngine().DBUpdateMOB(M);
					CMClass.DBEngine().DBUpdateFollowers(M);
				}
			}
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes everyone.\n\r");
		}
		else
		if(commandType.equals("ITEMS"))
		{
			CoffeeUtensils.clearDebriAndRestart(mob.location(),1);
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("ROOM"))
		{
			CoffeeUtensils.clearDebriAndRestart(mob.location(),0);
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("MOBS"))
		{
			CoffeeUtensils.clearDebriAndRestart(mob.location(),2);
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("QUESTS"))
		{
			Quests.save();
			mob.tell("Quest list saved.");
		}
		else
		{
			mob.tell(
				"\n\rYou cannot save '"+commandType+"'. "
				+"However, you might try "
				+"ITEMS, USERS, MOBS, or ROOM.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean arcCommand(){return true;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
