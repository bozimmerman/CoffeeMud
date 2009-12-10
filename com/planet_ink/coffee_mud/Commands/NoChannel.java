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
public class NoChannel extends StdCommand
{
	public NoChannel(){}

	private String[] access=null;
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		String channelName=((String)commands.elementAt(0)).toUpperCase().trim().substring(2);
		commands.removeElementAt(0);
		int channelNum=-1;
		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).equalsIgnoreCase(channelName))
			{
				channelNum=c;
				channelName=CMLib.channels().getChannelName(c);
			}
		}
        if(channelNum<0)
        for(int c=0;c<CMLib.channels().getNumChannels();c++)
        {
            if(CMLib.channels().getChannelName(c).toUpperCase().startsWith(channelName))
            {
                channelNum=c;
                channelName=CMLib.channels().getChannelName(c);
            }
        }
		if((channelNum<0)
		||(!CMLib.masking().maskCheck(CMLib.channels().getChannelMask(channelNum),mob,true)))
		{
			mob.tell("This channel is not available to you.");
			return false;
		}
		if(!CMath.isSet(pstats.getChannelMask(),channelNum))
		{
			pstats.setChannelMask(pstats.getChannelMask()|(1<<channelNum));
			mob.tell("The "+channelName+" channel has been turned off.  Use `"+channelName.toUpperCase()+"` to turn it back on.");
		}
		else
			mob.tell("The "+channelName+" channel is already off.");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
