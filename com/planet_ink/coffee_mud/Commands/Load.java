package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Load extends StdCommand
{
	public Load(){}

	private String[] access={"LOAD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(null))
		{
			mob.tell("Only the Archons may use that command.");
			return false;
		}
		else
		{
			if(commands.size()<3)
			{
				mob.tell("LOAD what? Use LOAD RESOURCE/ABILITY/ITEM/WEAPON/ETC.. [CLASS NAME]");
				return false;
			}
			String what=(String)commands.elementAt(1);
			String name=Util.combine(commands,2);
			if(what.equalsIgnoreCase("RESOURCE"))
			{
				StringBuffer buf=Resources.getFileResource(name);
				if((buf==null)||(buf.length()==0))
					mob.tell("Resource '"+name+"' was not found.");
				else
					mob.tell("Resource '"+name+"' was loaded.");
			}
			else
			if(CMClass.classCode(what)<0)
				mob.tell("'"+what+"' is not a valid class type.");
			else
			if(CMClass.loadClass(what,name))
				mob.tell(Util.capitalize(what)+" "+name+" was loaded.");
			else
				mob.tell(Util.capitalize(what)+" "+name+" was not loaded.");
		}

		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
