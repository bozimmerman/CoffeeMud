package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Goto extends At
{
	public Goto(){}

	private String[] access={"GOTO"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Room room=null;
		if(commands.size()<2)
		{
			mob.tell("Go where? Try a Room ID, player name, area name, or room text!");
			return false;
		}
		commands.removeElementAt(0);
		boolean chariot=false;
		if(((String)commands.lastElement()).equalsIgnoreCase("!"))
		{
		   chariot=true;
		   commands.removeElement(commands.lastElement());
		}
		StringBuffer cmd = new StringBuffer(Util.combine(commands,0));
		Room curRoom=mob.location();
		room=findRoomLiberally(mob,cmd);

		if(room==null)
		{
			mob.tell("Goto where? Try a Room ID, player name, area name, or room text!");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,"GOTO"))
		{
			mob.tell("You aren't powerful enough to do that. Try 'GO'.");
			return false;
		}
		if(curRoom==room)
		{
			mob.tell("Done.");
			return false;
		}
		else
		{
			room.bringMobHere(mob,true);
			if(chariot)
			{
				room.show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> ride(s) in on a flaming chariot.");
				CommonMsgs.look(mob,true);
			}
			else
				mob.tell("Done.");
			return false;
		}
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"GOTO");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
