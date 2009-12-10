package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Tell extends StdCommand
{
	public Tell(){}

	private String[] access={"TELL","T"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((!mob.isMonster())&&CMath.bset(mob.getBitmap(),MOB.ATT_QUIET))
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
		   &&(CMath.isNumber(CMParms.combine(commands,1)))
		   &&(mob.playerStats()!=null))
		{
			Vector V=mob.playerStats().getTellStack();
			if((V.size()==0)
			||(CMath.bset(metaFlags,Command.METAFLAG_AS))
	        ||(CMath.bset(metaFlags,Command.METAFLAG_POSSESSED)))
				mob.tell("No telling.");
			else
			{
				int num=CMath.s_int(CMParms.combine(commands,1));
				if(num>V.size()) num=V.size();
		        Session S=mob.session();
		        try {
    		        if(S!=null) S.snoopSuspension(1);
    				for(int i=V.size()-num;i<V.size();i++)
    					mob.tell((String)V.elementAt(i));
		        } finally {
		            if(S!=null) S.snoopSuspension(-1);
		        }
			}
			return false;
		}
		
		MOB target=null;
		String targetName=((String)commands.elementAt(0)).toUpperCase();
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session thisSession=CMLib.sessions().elementAt(s);
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
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session thisSession=CMLib.sessions().elementAt(s);
			if((thisSession.mob()!=null)
			   &&(!thisSession.killFlag())
			   &&((CMLib.english().containsString(thisSession.mob().name(),targetName))
				  ||(CMLib.english().containsString(thisSession.mob().Name(),targetName))))
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
		String combinedCommands=CMParms.combine(commands,1);
		if(combinedCommands.equals(""))
		{
			mob.tell("Tell them what?");
			return false;
		}
		combinedCommands=CMProps.applyINIFilter(combinedCommands,CMProps.SYSTEM_SAYFILTER);
		if(target==null)
		{
			if(targetName.indexOf("@")>=0)
			{
				String mudName=targetName.substring(targetName.indexOf("@")+1);
				targetName=targetName.substring(0,targetName.indexOf("@"));
				if(CMLib.intermud().i3online()||CMLib.intermud().imc2online())
					CMLib.intermud().i3tell(mob,targetName,mudName,combinedCommands);
				else
					mob.tell("Intermud is unavailable.");
				return false;
			}
			mob.tell("That person doesn't appear to be online.");
			return false;
		}
		
		if(CMath.bset(target.getBitmap(),MOB.ATT_QUIET))
		{
			mob.tell("That person can not hear you.");
			return false;
		}
		
		
		Session ts=target.session();
		try{
            if(ts!=null) ts.snoopSuspension(1);
            CMLib.commands().postSay(mob,target,combinedCommands,true,true);
		} finally {
		    if(ts!=null) ts.snoopSuspension(-1);
		}
        
		if((target.session()!=null)&&(target.session().afkFlag()))
			mob.tell(target.session().afkMessage());
		return false;
	}
	// the reason this is not 0ed is because of combat -- we want the players to use SAY, and pay for it when coordinating.
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
	public boolean canBeOrdered(){return false;}

	
}
