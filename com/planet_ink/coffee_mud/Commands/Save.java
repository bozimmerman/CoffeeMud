package com.planet_ink.coffee_mud.Commands;
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
			commandType=((String)commands.lastElement()).toUpperCase();
		
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
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("AREA")))
			{
				if((mob.session()!=null)&&(mob.session().confirm("Doing this assumes every item in every room in this area is correctly placed.  Are you sure (N/y)?","N")))
				{
					Area A=mob.location().getArea();
					for(Enumeration e=A.getMap();e.hasMoreElements();)
						CoffeeUtensils.clearDebriAndRestart((Room)e.nextElement(),1);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
				}
				else
					return false;
			}
			else
			{
				CoffeeUtensils.clearDebriAndRestart(mob.location(),1);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the room.\n\r");
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(commandType.equals("ROOM"))
		{
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("AREA")))
			{
				if((mob.session()!=null)&&(mob.session().confirm("Doing this assumes every mob and item in every room in this area is correctly placed.  Are you sure (N/y)?","N")))
				{
					Area A=mob.location().getArea();
					for(Enumeration e=A.getMap();e.hasMoreElements();)
						CoffeeUtensils.clearDebriAndRestart((Room)e.nextElement(),0);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
				}
				else
					return false;
			}
			else
			{
				CoffeeUtensils.clearDebriAndRestart(mob.location(),0);
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the room.\n\r");
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(commandType.equals("MOBS"))
		{
			if((commands.size()>2)&&(((String)commands.elementAt(1)).equalsIgnoreCase("AREA")))
			{
				if((mob.session()!=null)&&(mob.session().confirm("Doing this assumes every mob in every room in this area is correctly placed.  Are you sure (N/y)?","N")))
				{
					Area A=mob.location().getArea();
					for(Enumeration e=A.getMap();e.hasMoreElements();)
						CoffeeUtensils.clearDebriAndRestart((Room)e.nextElement(),2);
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
				}
				else 
					return false;
				
			}
			else
			{
				CoffeeUtensils.clearDebriAndRestart(mob.location(),2);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A feeling of permanency envelopes the room.\n\r");
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
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
