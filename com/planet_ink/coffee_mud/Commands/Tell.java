package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Tell extends StdCommand
{
	public Tell(){}

	private String[] access={"TELL","T"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((!mob.isMonster())&&Util.bset(mob.getBitmap(),MOB.ATT_QUIET))
		{
			mob.tell("You have QUIET mode on.  You must turn it off first.");
			return false;
		}
		
		if(commands.size()<3)
		{
			mob.tell("Tell whom what?");
			return false;
		}
		commands.removeElementAt(0);
		
		if(((String)commands.firstElement()).equalsIgnoreCase("last")
		   &&(Util.isNumber(Util.combine(commands,1)))
		   &&(mob.playerStats()!=null))
		{
			Vector V=mob.playerStats().getTellStack();
			if(V.size()==0)
				mob.tell("No telling.");
			else
			{
				int num=Util.s_int(Util.combine(commands,1));
				if(num>V.size()) num=V.size();
				for(int i=V.size()-num;i<V.size();i++)
					mob.tell((String)V.elementAt(i));
			}
			return false;
		}
		
		MOB target=null;
		String targetName=((String)commands.elementAt(0)).toUpperCase();
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=Sessions.elementAt(s);
			if((thisSession.mob()!=null)
			   &&(!thisSession.killFlag())
			   &&((thisSession.mob().name().equalsIgnoreCase(targetName))
				  ||(thisSession.mob().Name().equalsIgnoreCase(targetName))))
			{
				target=thisSession.mob();
				break;
			}
		}
		if(target==null)
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=Sessions.elementAt(s);
			if((thisSession.mob()!=null)
			   &&(!thisSession.killFlag())
			   &&((EnglishParser.containsString(thisSession.mob().name(),targetName))
				  ||(EnglishParser.containsString(thisSession.mob().Name(),targetName))))
			{
				target=thisSession.mob();
				break;
			}
		}
		for(int i=1;i<commands.size();i++)
		{
			String s=(String)commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
		String combinedCommands=Util.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell("Tell them what?");
			return false;
		}
		combinedCommands=CommonStrings.applyFilter(combinedCommands,CommonStrings.SYSTEM_SAYFILTER);
		if(target==null)
		{
			if(targetName.indexOf("@")>=0)
			{
				String mudName=targetName.substring(targetName.indexOf("@")+1);
				targetName=targetName.substring(0,targetName.indexOf("@"));
				if(CMClass.I3Interface().i3online()||CMClass.I3Interface().imc2online())
					CMClass.I3Interface().i3tell(mob,targetName,mudName,combinedCommands);
				else
					mob.tell("Intermud is unavailable.");
				return false;
			}
			else
			{
				mob.tell("That person doesn't appear to be online.");
				return false;
			}
		}
		
		if(Util.bset(target.getBitmap(),MOB.ATT_QUIET))
		{
			mob.tell("That person can not hear you.");
			return false;
		}
		
		CommonMsgs.say(mob,target,combinedCommands,true,true);
		if((target.session()!=null)
		&&(target.session().afkFlag()))
			mob.tell(target.name()+" is AFK at the moment.");
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
