package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class PlayerKill extends StdCommand
{
	public PlayerKill(){}

	private String[] access={"PLAYERKILL","PKILL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ALWAYS")
			||CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("NEVER"))
		{
			mob.tell("This option has been disabled.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("YOU CANNOT TOGGLE THIS FLAG WHILE IN COMBAT!");
			return false;
		}
		if(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL))
		{
			if(CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ONEWAY"))
			{
				mob.tell("Once turned on, this flag may not be turned off again.");
				return false;
			}
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_PLAYERKILL));
			mob.tell("Your playerkill flag has been turned off.");
		}
		else
		if(!mob.isMonster())
		{
			mob.tell("Turning on this flag will allow you to kill and be killed by other players.");
			if(mob.session().confirm("Are you absolutely sure (y/N)?","N"))
			{
				mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_PLAYERKILL));
				mob.tell("Your playerkill flag has been turned on.");
			}
			else
				mob.tell("Your playerkill flag remains OFF.");
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
