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
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2014 Bo Zimmerman

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
public class ClanPremise extends StdCommand
{
	public ClanPremise(){}

	private final String[] access={"CLANPREMISE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(final MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String clanName=(commands.size()>1)?CMParms.combine(commands,1,commands.size()):"";
		
		Clan chkC=null;
		final boolean skipChecks=mob.getClanRole(mob.Name())!=null;
		if(skipChecks) chkC=mob.getClanRole(mob.Name()).first;
		
		if(chkC==null)
		for(Pair<Clan,Integer> c : mob.clans())
			if((clanName.length()==0)||(CMLib.english().containsString(c.first.getName(), clanName))
			&&(c.first.getAuthority(c.second.intValue(), Clan.Function.PREMISE)!=Authority.CAN_NOT_DO))
			{	chkC=c.first; break; }
		
		commands.setElementAt(getAccessWords()[0],0);

		final Clan C=chkC;
		if(C==null)
		{
			mob.tell("You aren't allowed to set a premise for "+((clanName.length()==0)?"anything":clanName)+".");
			return false;
		}

		if((!skipChecks)&&(!CMLib.clans().goForward(mob,C,commands,Clan.Function.PREMISE,false)))
		{
			mob.tell("You aren't in the right position to set the premise to your "+C.getGovernmentName()+".");
			return false;
		}
		if((skipChecks)&&(commands.size()>1))
		{
			setClanPremise(mob,C,CMParms.combine(commands,1));
			return false;
		}
		final Session session=mob.session();
		if(session==null)
		{
			return false;
		}
		session.prompt(new InputCallback(InputCallback.Type.PROMPT,"",0)
		{
			@Override public void showPrompt() { session.promptPrint("Describe your "+C.getGovernmentName()+"'s Premise\n\r: ");}
			@Override public void timedOut() { }
			@Override public void callBack() 
			{
				final String premise=this.input;
				if(premise.length()==0)
				{
					return;
				}
				Vector cmds=new Vector();
				cmds.addElement(getAccessWords()[0]);
				cmds.addElement(premise);
				if(skipChecks||CMLib.clans().goForward(mob,C,cmds,Clan.Function.PREMISE,true))
				{
					setClanPremise(mob,C,premise);
				}
			}
		});
		return false;
	}
	
	public void setClanPremise(MOB mob, Clan C, String premise)
	{
		C.setPremise(premise);
		C.update();
		CMLib.clans().clanAnnounce(mob,"The premise of "+C.getGovernmentName()+" "+C.clanID()+" has been changed.");
	}
	
	
	public boolean canBeOrdered(){return false;}

	
}
