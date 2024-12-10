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
   Copyright 2024 github.com/toasted323
   Copyright 2004-2024 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   CHANGES:
   2024-12 toasted323: enable hiding class and level information by configuration
   2024-12 toasted323: hide level and class from group command
*/
public class Group extends StdCommand
{
	public Group()
	{
	}

	private final String[] access=I(new String[]{"GROUP","GR"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public static StringBuffer showWhoLong(final MOB seer, final MOB who, final int[] cols, final int statShortLevel)
	{
		final StringBuffer msg=new StringBuffer("^N");
		msg.append("[^w");
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		{
			if(who.charStats().getCurrentClass().raceless())
				msg.append(CMStrings.padRight(" ",cols[0])+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().raceName(),cols[0])+" ");
		}

		String levelStr = who.charStats().displayClassLevel(who, true).trim();
		final int x = levelStr.lastIndexOf(' ');
		if (x >= 0) levelStr = levelStr.substring(x).trim();

		if ((CMSecurity.isASysOp(seer) || !CMProps.isCharacterInfoPrivate(CMProps.PrivateCharacterInfo.CLASS) || !who.isPlayer())
				&& !CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)) {
			if (who.charStats().getMyRace().classless())
				msg.append(CMStrings.padRight(" ", cols[1]) + " ");
			else
				msg.append(CMStrings.padRight(who.charStats().displayClassName(), cols[1]) + " ");
		}
		if ((CMSecurity.isASysOp(seer) || !CMProps.isCharacterInfoPrivate(CMProps.PrivateCharacterInfo.LEVEL)) && !CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)) {
			if (who.charStats().getCurrentClass().leveless() || who.charStats().getMyRace().leveless())
				msg.append(CMStrings.padRight(" ", cols[2]));
			else
				msg.append(CMStrings.padRight(levelStr, cols[2]));
		}

		final double hpPct = CMath.div(who.curState().getHitPoints(), who.maxState().getHitPoints());
		final double mnPct = CMath.div(who.curState().getMana(), who.maxState().getMana());
		final double mvPct = CMath.div(who.curState().getMovement(), who.maxState().getMovement());
		final String hpColor = (hpPct < .25) ? "^r" : (hpPct < .5) ? "^y" : "^w";
		final String mnColor = (mnPct < .25) ? "^r" : (mnPct < .5) ? "^y" : "^w";
		final String mvColor = (mvPct < .25) ? "^r" : (mvPct < .5) ? "^y" : "^w";
		msg.append("^N] ^H" + CMStrings.padRight(who.name(),cols[3])+"^N ");
		final String hpCur = CMStrings.padRightPreserve(""+who.curState().getHitPoints(),cols[4]);
		final String hpMax = CMStrings.padRightPreserve(""+who.maxState().getHitPoints(),cols[4]);
		final String mnCur = CMStrings.padRightPreserve(""+who.curState().getMana(),cols[4]);
		final String mnMax = CMStrings.padRightPreserve(""+who.maxState().getMana(),cols[4]);
		final String mvCur = CMStrings.padRightPreserve(""+who.curState().getMovement(),cols[4]);
		final String mvMax = CMStrings.padRightPreserve(""+who.maxState().getMovement(),cols[4]);
		if(statShortLevel == 0)
		{
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("hp(@x1@x2^N/^w@x3^N)",hpColor,hpCur,hpMax),cols[5]));
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("mn(@x1@x2^N/^w@x3^N)",mnColor,mnCur,mnMax),cols[5]));
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("mv(@x1@x2^N/^w@x3^N)",mvColor,mvCur,mvMax),cols[5]));
		}
		else
		if(statShortLevel == 1)
		{
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("@x1@x2hp^N ",hpColor,hpCur),cols[5]));
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("@x1@x2mn^N ",mnColor,mnCur),cols[5]));
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("@x1@x2mv^N",mvColor,mvCur),cols[5]));
		}
		else
		{
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("@x1@x2^N/",hpColor,hpCur),cols[5]));
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("@x1@x2^N/",mnColor,mnCur),cols[5]));
			msg.append(CMStrings.padRightPreserve(CMLib.lang().L("@x1@x2^N",mvColor,mvCur),cols[5]));
		}
		msg.append("\n\r");
		return msg;
	}

	public int fixCols(final MOB mob, final int[] cols, final List<MOB> orderedGroup)
	{
		int longestName = 0;
		for (final MOB follower : orderedGroup)
		{
			if(follower.name().length()>=longestName)
				longestName=follower.name().length()+1;
		}

		int statLen=12;
		int shortCode = 0;
		if(longestName < 13)
			longestName = 13;
		else
		{
			if(longestName < 19)
			{
				longestName=18;
				shortCode=1;
				statLen=7;
			}
			else
			if(longestName < 21)
			{
				longestName=20;
				shortCode=2;
				statLen=5;
			}
			else
			if(longestName < 35)
			{
				shortCode=2;
				statLen=5;
			}
			else
			{
				longestName=35;
				shortCode=2;
				statLen=5;
			}
		}
		cols[0]=CMLib.lister().fixColWidth(7,mob.session()); // race

		if ((CMSecurity.isASysOp(mob) || !CMProps.isCharacterInfoPrivate(CMProps.PrivateCharacterInfo.CLASS)))
			cols[1] = CMLib.lister().fixColWidth(7, mob.session()); // class
		else
			cols[1] = CMLib.lister().fixColWidth(0, mob.session()); // class

		boolean hasNPC = false;
		for (MOB m : orderedGroup) {
			if (!m.isPlayer()) {
				hasNPC = true;
				break;
			}
		}

		if ((CMSecurity.isASysOp(mob) || !CMProps.isCharacterInfoPrivate(CMProps.PrivateCharacterInfo.LEVEL) || hasNPC))
			cols[2] = CMLib.lister().fixColWidth(5, mob.session()); // level
		else
			cols[2] = CMLib.lister().fixColWidth(0, mob.session()); // level

		cols[3]=CMLib.lister().fixColWidth(longestName,mob.session());//name
		cols[4]=CMLib.lister().fixColWidth(3,mob.session()); // one digit
		cols[5]=CMLib.lister().fixColWidth(statLen,mob.session()); // one stat
		return shortCode;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		mob.tell(L("@x1's group:\n\r",mob.name()));
		final Set<MOB> group=mob.getGroupMembers(new HashSet<MOB>());
		final List<MOB> orderedGroup = new LinkedList<MOB>();
		final Room R=mob.location();
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
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
		final int[] cols=new int[6];
		final int statShortLevel = fixCols(mob, cols, orderedGroup);

		for (final MOB follower : orderedGroup)
		{
			if((follower!=null)
			&& (follower!=mob))
			{
				if(follower.amFollowing()==null)
				{
					for (final MOB leader : orderedGroup)
					{
						if((leader!=null)
						&&(leader != follower)
						&&(leader.isFollowedBy(follower)))
						{
							Log.errOut(follower.Name()+" should be in "+mob.Name()+"'s group, but is in no ones.  Fixing.");
							follower.setFollowing(leader);
						}
					}
				}
			}
			msg.append(showWhoLong(mob,follower,cols,statShortLevel));
		}
		mob.tell(msg.toString());
		mob.tell(L("You have @x1/@x2 followers.",""+mob.totalFollowers(),""+mob.maxFollowers()));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
