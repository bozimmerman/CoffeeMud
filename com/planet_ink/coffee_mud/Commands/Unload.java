package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Unload extends StdCommand
{
	public Unload(){}

	private String[] access={"UNLOAD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String str=Util.combine(commands,1);
		if(str.length()==0)
		{
			mob.tell("UNLOAD what?");
			return false;
		}
		if(((String)commands.elementAt(1)).equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<3)
			{
				mob.tell("Unload which class?");
				return false;
			}
			commands.removeElementAt(0);
			commands.removeElementAt(0);
			for(int i=0;i<commands.size();i++)
			{
				String name=(String)commands.elementAt(0);
				Object O=CMClass.getClass(name);
				if((O==null)||(!CMClass.delClass(O)))
					mob.tell("Class '"+name+"' was not found in the library.");
				else
					mob.tell("Class '"+name+"' was unloaded.");
			}
			return false;
		}
		if(str.equalsIgnoreCase("help"))
		{
			MUDHelp.unloadHelpFile(mob);
			return false;
		}
		if(str.equalsIgnoreCase("all"))
		{
			mob.tell("All resources unloaded.");
			Resources.clearResources();
			return false;
		}
		Vector V=Resources.findResourceKeys(str);
		if(V.size()==0)
		{
			mob.tell("Unknown resource '"+str+"'.  Use LIST RESOURCES.");
			return false;
		}
		for(int v=0;v<V.size();v++)
		{
			String key=(String)V.elementAt(v);
			Resources.removeResource(key);
			mob.tell("Resource '"+key+"' unloaded.");
		}

		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"LOADUNLOAD");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
