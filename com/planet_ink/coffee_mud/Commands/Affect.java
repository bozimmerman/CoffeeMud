package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Affect extends StdCommand
{
	private String[] access={"AFFECT","AFF"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Session S=mob.session();
		if(S!=null)
		{
			StringBuffer msg=new StringBuffer("");
			msg.append("\n\r^!You are affected by:^? ");
			for(int a=0;a<mob.numEffects();a++)
			{
				Ability thisAffect=mob.fetchEffect(a);
				if((thisAffect!=null)&&(thisAffect.displayText().length()>0))
					msg.append("\n\r^S"+thisAffect.displayText());
			}
			if(msg.length()==0)
				msg.append("Nothing!");
			else
				msg.append("^?");
			msg.append("\n\r");
			S.colorOnlyPrintln(msg.toString());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
