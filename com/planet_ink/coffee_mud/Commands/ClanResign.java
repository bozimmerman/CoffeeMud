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
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2012 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class ClanResign extends StdCommand
{
	public ClanResign(){}

	private final String[] access={"CLANRESIGN"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String clanName=(commands.size()>1)?CMParms.combine(commands,1,commands.size()):"";
		
		Clan C=null;
		for(Pair<Clan,Integer> c : mob.clans())
			if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName)))
			{	C=c.first; break; }
		
		StringBuffer msg=new StringBuffer("");
		if(C==null)
		{
			msg.append("You can't resign from "+((clanName.length()==0)?"anything":clanName)+".");
		}
		else
		if(!mob.isMonster())
		{
			try
			{
				String check=mob.session().prompt("Resign from "+C.getName()+".  Are you absolutely SURE (y/N)?","N");
				if(check.equalsIgnoreCase("Y"))
				{
					if(C.getGovernment().getExitScript().trim().length()>0)
					{
						Pair<Clan,Integer> curClanRole=mob.getClanRole(C.clanID());
						if(curClanRole!=null)
							mob.setClan(C.clanID(), curClanRole.second.intValue());
						ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
						S.setSavable(false);
						S.setVarScope("*");
						S.setScript(C.getGovernment().getExitScript());
						CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,"CLANEXIT");
						S.executeMsg(mob, msg2);
						S.dequeResponses();
						S.tick(mob,Tickable.TICKID_MOB);
					}
					CMLib.clans().clanAnnounce(mob,"Member resigned from "+C.getGovernmentName()+" "+C.name()+": "+mob.Name());
					C.delMember(mob);
				}
				else
				{
					return false;
				}
			}
			catch(java.io.IOException e)
			{
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return false;}

	
}
