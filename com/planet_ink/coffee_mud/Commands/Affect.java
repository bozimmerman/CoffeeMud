package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Affect extends StdCommand
{
	private String[] access={"AFFECT","AFF","AF"};
	public String[] getAccessWords(){return access;}
	
	public String getAffects(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^!You are affected by:^? ");
		int colnum=2;
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability thisAffect=mob.fetchEffect(a);
			String disp=thisAffect.displayText();
			if((thisAffect!=null)&&(disp.length()>0))
			{
				if(((++colnum)>2)||(disp.length()>25)){ msg.append("\n\r"); colnum=0;}
				msg.append("^S"+Util.padRightPreserve(thisAffect.displayText(),25));
				if(disp.length()>25) colnum=99;
			}
		}
		if(msg.length()==0)
			msg.append("Nothing!");
		else
			msg.append("^?");
		msg.append("\n\r");
		return msg.toString();
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		Session S=mob.session();
		if(S!=null)
			S.colorOnlyPrintln(getAffects(mob));
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
