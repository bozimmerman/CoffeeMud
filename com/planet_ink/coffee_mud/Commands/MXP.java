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
@SuppressWarnings("rawtypes")
public class MXP extends StdCommand
{
	public MXP(){}

	private final String[] access={"MXP"};
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			if((!CMath.bset(mob.getBitmap(),MOB.ATT_MXP))
			||(!mob.session().getClientTelnetMode(Session.TELNET_MXP)))
			{
				mob.session().changeTelnetMode(Session.TELNET_MXP,true);
				if(mob.session().getTerminalType().toLowerCase().startsWith("mushclient"))
					mob.session().negotiateTelnetMode(Session.TELNET_MXP);
				for(int i=0;((i<5)&&(!mob.session().getClientTelnetMode(Session.TELNET_MXP)));i++)
				{
					try{mob.session().prompt("",250);}catch(final Exception e){}
				}
				if(mob.session().getClientTelnetMode(Session.TELNET_MXP))
				{
					mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_MXP));
					final StringBuffer mxpText=Resources.getFileResource("text/mxp.txt",true);
					if(mxpText!=null)
						mob.session().rawOut("\033[6z\n\r"+mxpText.toString()+"\n\r");
					mob.tell(_("MXP codes enabled.\n\r"));
				}
				else
					mob.tell(_("Your client does not appear to support MXP."));
			}
			else
				mob.tell(_("MXP codes are already enabled.\n\r"));
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return super.securityCheck(mob)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP));}
}

