package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Config extends StdCommand
{
	public Config(){}

	private String[] access={"CONFIG"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("^HYour configuration flags:^?\n\r");
		for(int i=0;i<MOB.AUTODESC.length;i++)
		{
			if((i!=18)||(Resources.getFileResource("text"+java.io.File.separatorChar+"motd.txt")!=null))
			{
				msg.append(Util.padRight(MOB.AUTODESC[i],15)+": ");
				boolean set=Util.isSet(mob.getBitmap(),i);
				if(MOB.AUTOREV[i]) set=!set;
				msg.append(set?"ON":"OFF");
				msg.append("\n\r");
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
