package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2013 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings({"unchecked","rawtypes"})
public class StdCommand implements Command
{
	public StdCommand(){}
	protected String ID=null;
	public String ID()
	{
		if(ID==null){
			ID=this.getClass().getName();
			int x=ID.lastIndexOf('.');
			if(x>=0) ID=ID.substring(x+1);
		}
		return ID;
	}
	public String name() { return ID();}
	
	private String[] access=null;
	public String[] getAccessWords(){return access;}
	public void initializeClass(){}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		// accepts the mob executing, and a Vector of Strings as a parm.
		// the return value is arbitrary, though false is conventional.
		return false;
	}
	public boolean preExecute(MOB mob, Vector commands, int metaFlags, int secondsElapsed, double actionsRemaining)
		throws java.io.IOException
	{
		return true;
	}

	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		// fake it!
		Vector commands = new Vector();
		commands.add(getAccessWords()[0]);
		for(Object o : args)
			commands.add(o.toString());
		return Boolean.valueOf(execute(mob,commands,metaFlags));
	}
	
	public boolean checkArguments(Class[][] fmt, Object... args)
	{
		for(int f=0;f<fmt.length;f++)
		{
			Class[] ff=fmt[f];
			if(ff.length==args.length)
			{
				boolean check=true; 
				for(int i=0;i<ff.length;i++)
				{
					if((args[i]!=null)
					&&(!ff[i].isAssignableFrom(args[i].getClass())))
					{
						check=false;
						break;
					}
				}
				if(check)
					return true;
			}
		}
		StringBuilder str=new StringBuilder("");
		str.append("Illegal arguments. Sent: ");
		for(Object o : args)
			if(o==null)
				str.append("null ");
			else
				str.append(o.getClass().getSimpleName()).append(" ");
		str.append(". Correct: ");
		for(int f=0;f<fmt.length;f++)
			for(Class c : fmt[f])
				str.append(c.getSimpleName()).append(" ");
		Log.errOut(ID(),str.toString());
		return false;
	}
	
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getActionCost(ID(), 0.0);
	}
	public double combatActionsCost(MOB mob, List<String> cmds)
	{
		return CMProps.getCombatActionCost(ID(), 0.0);
	}
	public double checkedActionsCost(final MOB mob, final List<String> cmds)
	{
		if(mob!=null)
			return mob.isInCombat() ? combatActionsCost(mob,cmds) : actionsCost(mob,cmds);
		return actionsCost(mob,cmds);
	}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return true;}
	public boolean staffCommand(){return false;}
	public CMObject newInstance(){return this;}
	public CMObject copyOf()
	{
		try
		{
			Object O=this.clone();
			return (CMObject)O;
		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}
	
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
