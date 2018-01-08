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

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class PollCmd extends StdCommand
{
	public PollCmd(){}

	private final String[] access=I(new String[]{"POLL"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if((mob==null)||mob.isMonster())
			return false;
		final java.util.List<Poll>[] mypolls=CMLib.polls().getMyPollTypes(mob,(commands==null));

		if((mypolls[0].size()==0)&&(mypolls[2].size()==0))
		{
			if((commands!=null)&&(mypolls[1].size()==0))
			{
				mob.tell(L("No polls are available at this time."));
				return false;
			}
			else
			if(commands==null)
			{
				if(mypolls[1].size()>0)
					mob.tell(L("@x1 poll(s) <-IS-ARE> awaiting your participation.",""+mypolls[1].size()));
				return false;
			}
		}

		for(final Poll P : mypolls[0])
		{
			CMLib.polls().processVote(P, mob);
			if(P.mayISeeResults(mob))
			{
				CMLib.polls().processResults(P, mob);
				mob.session().prompt(L("Press ENTER to continue:\n\r"));
			}
		}
		if(commands==null)
		{
			if(mypolls[1].size()==1)
				mob.tell(L("\n\r^H@x1 other poll(s) <-IS-ARE> awaiting your participation.^N\n\r",""+mypolls[1].size()));
			if(mypolls[2].size()>0)
				mob.tell(L("\n\r^HResults from @x1 poll(s) <-IS-ARE> still available.^N\n\r",""+mypolls[2].size()));
			return true;
		}
		for(final Poll P : mypolls[1])
		{
			CMLib.polls().processVote(P, mob);
			if(P.mayISeeResults(mob))
			{
				CMLib.polls().processResults(P, mob);
				mob.session().prompt(L("Press ENTER to continue:"));
			}
		}

		if(mypolls[2].size()>0)
			mob.tell(L("\n\r^HPrevious polling results:^N\n\r"));
		int i=0;
		for(final Poll P : mypolls[2])
		{
			if(P.mayISeeResults(mob))
			{
				CMLib.polls().processResults(P, mob);
				if(i<mypolls[2].size()-1)
					mob.session().prompt(L("Press ENTER to continue:\n\r"));
			}
			i++;
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
