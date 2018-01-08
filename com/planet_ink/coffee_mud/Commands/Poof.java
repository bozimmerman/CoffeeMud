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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Poof extends StdCommand
{
	public Poof(){}

	private final String[] access=I(new String[]{"POOF"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell(L("You are not allowed to do that here."));
		return false;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		int showFlag=-1;
		if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
			showFlag=-999;
		boolean ok=false;
		while((!ok)&&(mob.playerStats()!=null))
		{
			int showNumber=0;
			final String poofIn=CMLib.genEd().prompt(mob,mob.playerStats().getPoofIn(),++showNumber,showFlag,L("Poof-in"),true,true);
			final String poofOut=CMLib.genEd().prompt(mob,mob.playerStats().getPoofOut(),++showNumber,showFlag,L("Poof-out"),true,true);
			final String tranPoofIn=CMLib.genEd().prompt(mob,mob.playerStats().getTranPoofIn(),++showNumber,showFlag,L("Transfer-in"),true,true);
			final String tranPoofOut=CMLib.genEd().prompt(mob,mob.playerStats().getTranPoofOut(),++showNumber,showFlag,L("Transfer-out"),true,true);
			mob.playerStats().setPoofs(poofIn,poofOut,tranPoofIn,tranPoofOut);
			if(showFlag<-900)
			{
				ok=true;
				break;
			}
			if(showFlag>0)
			{
				showFlag=-1;
				continue;
			}
			showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
			if(showFlag<=0)
			{
				showFlag=-1;
				ok=true;
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedContainsAny(mob,mob.location(),CMSecurity.SECURITY_GOTO_GROUP);
	}

}
