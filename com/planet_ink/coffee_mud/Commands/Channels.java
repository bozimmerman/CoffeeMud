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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class Channels extends StdCommand
{
	public Channels(){}

	private final String[] access=I(new String[]{"CHANNELS"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final PlayerStats pstats=mob.playerStats();
		if(pstats==null)
			return false;
		final StringBuffer buf=new StringBuffer(L("Available channels: \n\r"));
		int col=0;
		final String[] names=CMLib.channels().getChannelNames();
		final int COL_LEN=CMLib.lister().fixColWidth(24.0,mob);
		for(int x=0;x<names.length;x++)
		{
			if(CMLib.masking().maskCheck(CMLib.channels().getChannel(x).mask(),mob,true))
			{
				if((++col)>3)
				{
					buf.append("\n\r");
					col=1;
				}
				final String channelName=names[x];
				final boolean onoff=CMath.isSet(pstats.getChannelMask(),x);
				buf.append(CMStrings.padRight("^<CHANNELS '"+(onoff?"":"NO")+"'^>"+channelName+"^</CHANNELS^>"+(onoff?" (OFF)":""),COL_LEN));
			}
		}
		if(names.length==0)
			buf.append("None!");
		else
			buf.append("\n\rUse NOCHANNELNAME (ex: NOGOSSIP) to turn a channel off.");
		mob.tell(buf.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
