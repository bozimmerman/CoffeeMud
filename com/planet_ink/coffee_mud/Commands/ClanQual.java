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
public class ClanQual extends StdCommand
{
	public ClanQual(){}

	private final String[] access={"CLANQUAL"};
	@Override public String[] getAccessWords(){return access;}
	@Override
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
			mob.tell("You aren't allowed to set qualifications for "+((clanName.length()==0)?"anything":clanName)+".");
			return false;
		}

		if((!skipChecks)&&(!CMLib.clans().goForward(mob,C,commands,Clan.Function.PREMISE,false)))
		{
			mob.tell("You aren't in the right position to set the qualifications to your "+C.getGovernmentName()+".");
			return false;
		}

		if((skipChecks)&&(commands.size()>1))
		{
			setClanQualMask(mob,C,CMParms.combine(commands,1));
			return false;
		}
		final Session session=mob.session();
		if(session==null)
		{
			return false;
		}
		final InputCallback[] IC=new InputCallback[1];
		IC[0]=new InputCallback(InputCallback.Type.PROMPT,"",0)
		{
			@Override public void showPrompt() { session.promptPrint("Describe your "+C.getGovernmentName()+"'s Qualification Code (?)\n\r: ");}
			@Override public void timedOut() { }
			@Override public void callBack()
			{
				final String qualMask=this.input;
				if(qualMask.length()==0)
				{
					return;
				}
				if(qualMask.equals("?"))
				{
					mob.tell(CMLib.masking().maskHelp("\n\r","disallow"));
					session.prompt(IC[0].reset());
					return;
				}
				session.prompt(new InputCallback(InputCallback.Type.CHOOSE,"Y","YN\n",0)
				{
					@Override public void showPrompt()
					{
						session.println("Your qualifications will be as follows: "+CMLib.masking().maskDesc(qualMask)+"\n\r");
						session.promptPrint("Is this correct (Y/n)?");
					}
					@Override public void timedOut() { }
					@Override public void callBack()
					{
						if(!this.input.equalsIgnoreCase("Y"))
						{
							session.prompt(IC[0].reset());
							return;
						}
						Vector cmds=new Vector();
						cmds.addElement(getAccessWords()[0]);
						cmds.addElement(qualMask);
						if(skipChecks||CMLib.clans().goForward(mob,C,cmds,Clan.Function.PREMISE,true))
						{
							setClanQualMask(mob,C,qualMask);
						}
					}
				});
			}
		};
		session.prompt(IC[0]);
		return false;
	}

	public void setClanQualMask(MOB mob, Clan C, String qualMask)
	{
		C.setAcceptanceSettings(qualMask);
		C.update();
		CMLib.clans().clanAnnounce(mob,"The qualifications of "+C.getGovernmentName()+" "+C.clanID()+" have been changed.");
	}

	@Override public boolean canBeOrdered(){return false;}


}
