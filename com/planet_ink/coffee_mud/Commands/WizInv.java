package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class WizInv extends StdCommand
{
	public WizInv(){}

	private String[] access={"WIZINVISIBLE","WIZINV","NOWIZINV","VISIBLE","VIS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(null)) return false;
		String str=(String)commands.firstElement();
		if(Character.toUpperCase(str.charAt(0))!='W')
			commands.insertElementAt("OFF",1);
		commands.removeElementAt(0);
		str="Prop_WizInvis";
		Ability A=mob.fetchEffect(str);
		if(Util.combine(commands,0).trim().equalsIgnoreCase("OFF"))
		{
		   if(A!=null)
			   A.unInvoke();
		   else
			   mob.tell("You are not wizinvisible!");
		   return false;
		}
		else
		if(A!=null)
		{
			mob.tell("You have already faded from view!");
			return false;
		}

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		A=CMClass.getAbility(str);
		if(A!=null)
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> fade(s) from view!");
			mob.addEffect((Ability)A.copyOf());
			mob.recoverEnvStats();
			mob.location().recoverRoomStats();
			mob.tell("You may uninvoke WIZINV with 'WIZINV OFF'.");
			return false;
		}
		else
		{
			mob.tell("Wizard invisibility is not available!");
			return false;
		}
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
