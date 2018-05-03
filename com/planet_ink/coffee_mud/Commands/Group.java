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

public class Group extends StdCommand
{
	public Group(){}

	private final String[] access=I(new String[]{"GROUP","GR"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public static StringBuffer showWhoLong(MOB seer, MOB who)
	{

		final StringBuffer msg=new StringBuffer("^N");
		msg.append("[^w");
		final int[] cols={
				CMLib.lister().fixColWidth(7,seer.session()),
				CMLib.lister().fixColWidth(7,seer.session()),
				CMLib.lister().fixColWidth(5,seer.session()),
				CMLib.lister().fixColWidth(13,seer.session()),
				CMLib.lister().fixColWidth(3,seer.session()),
				CMLib.lister().fixColWidth(12,seer.session())
			};
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		{
			if(who.charStats().getCurrentClass().raceless())
				msg.append(CMStrings.padRight(" ",cols[0])+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().raceName(),cols[0])+" ");
		}

		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		final int x=levelStr.lastIndexOf(' ');
		if(x>=0)
			levelStr=levelStr.substring(x).trim();
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		{
			if(who.charStats().getMyRace().classless())
				msg.append(CMStrings.padRight(" ",cols[1])+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().displayClassName(),cols[1])+" ");
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		{
			if(who.charStats().getCurrentClass().leveless()
			||who.charStats().getMyRace().leveless())
				msg.append(CMStrings.padRight(" ",cols[2]));
			else
				msg.append(CMStrings.padRight(levelStr,cols[2]));
		}
		final double hpPct = CMath.div(who.curState().getHitPoints(), who.maxState().getHitPoints());
		final double mnPct = CMath.div(who.curState().getMana(), who.maxState().getMana());
		final double mvPct = CMath.div(who.curState().getMovement(), who.maxState().getMovement());
		final String hpColor = (hpPct < .25) ? "^r" : (hpPct < .5) ? "^y" : "^w";
		final String mnColor = (mnPct < .25) ? "^r" : (mnPct < .5) ? "^y" : "^w";
		final String mvColor = (mvPct < .25) ? "^r" : (mvPct < .5) ? "^y" : "^w";
		msg.append("^N] ^H" + CMStrings.padRight(who.name(),cols[3])+"^N ");
		msg.append(CMStrings.padRightPreserve(CMLib.lang().L("hp(@x1@x2^N/^w@x3^N)",hpColor,CMStrings.padRightPreserve(""+who.curState().getHitPoints(),cols[4]),CMStrings.padRightPreserve(""+who.maxState().getHitPoints(),cols[4])),cols[5]));
		msg.append(CMStrings.padRightPreserve(CMLib.lang().L("mn(@x1@x2^N/^w@x3^N)",mnColor,CMStrings.padRightPreserve(""+who.curState().getMana(),cols[4]),CMStrings.padRightPreserve(""+who.maxState().getMana(),cols[4])),cols[5]));
		msg.append(CMStrings.padRightPreserve(CMLib.lang().L("mv(@x1@x2^N/^w@x3^N)",mvColor,CMStrings.padRightPreserve(""+who.curState().getMovement(),cols[4]),CMStrings.padRightPreserve(""+who.maxState().getMovement(),cols[4])),cols[5]));
		msg.append("\n\r");
		return msg;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		mob.tell(L("@x1's group:\n\r",mob.name()));
		final Set<MOB> group=mob.getGroupMembers(new HashSet<MOB>());
		final List<MOB> orderedGroup = new LinkedList<MOB>();
		final Room R=mob.location();
		for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if(group.contains(M))
			{
				orderedGroup.add(M);
				group.remove(M);
			}
		}
		orderedGroup.addAll(group);
		group.clear();
		final StringBuffer msg=new StringBuffer("");
		for (final Object element : orderedGroup)
		{
			final MOB follower=(MOB)element;
			msg.append(showWhoLong(mob,follower));
		}
		mob.tell(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
