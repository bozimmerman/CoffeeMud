package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Quit extends StdCommand
{
	public Quit(){}

	private String[] access={"QUIT","QUI","Q"};
	public String[] getAccessWords(){return access;}

	public static void dispossess(MOB mob)
	{
		if(mob.soulMate()==null)
		{
			mob.tell("Huh?");
			return;
		}
		Session s=mob.session();
		s.setMob(mob.soulMate());
		mob.soulMate().setSession(s);
		mob.setSession(null);
		mob.soulMate().tell("^HYour spirit has returned to your body...\n\r\n\r^N");
		CommonMsgs.look(mob.soulMate(),true);
		mob.setSoulMate(null);
	}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob.soulMate()!=null)
			dispossess(mob);
		else
		if(!mob.isMonster())
		{
			try
			{
				mob.session().cmdExit(mob,commands);
			}
			catch(Exception e)
			{
				if(mob.session()!=null)
					mob.session().setKillFlag(true);
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
