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
public class Group extends StdCommand
{
	public Group(){}

	private String[] access={"GROUP","GR"};
	public String[] getAccessWords(){return access;}
	
	public static StringBuffer showWhoLong(MOB who)
	{

		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		if(!CMSecurity.isDisabled("RACES"))
		{
			if(who.charStats().getCurrentClass().raceless())
				msg.append(CMStrings.padRight(" ",7)+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().raceName(),7)+" ");
		}
		    
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(" ");
		if(x>=0) levelStr=levelStr.substring(x).trim();
		if(!CMSecurity.isDisabled("CLASSES"))
		{
			if(who.charStats().getMyRace().classless())
				msg.append(CMStrings.padRight(" ",7)+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().displayClassName(),7)+" ");
		}
		if(!CMSecurity.isDisabled("LEVELS"))
		{
			if(who.charStats().getCurrentClass().leveless()
			||who.charStats().getMyRace().leveless())
				msg.append(CMStrings.padRight(" ",5));
			else
				msg.append(CMStrings.padRight(levelStr,5));
		}
		msg.append("] "+CMStrings.padRight(who.name(),13)+" ");
		msg.append(CMStrings.padRightPreserve("hp("+CMStrings.padRightPreserve(""+who.curState().getHitPoints(),3)+"/"+CMStrings.padRightPreserve(""+who.maxState().getHitPoints(),3)+")",12));
		msg.append(CMStrings.padRightPreserve("mn("+CMStrings.padRightPreserve(""+who.curState().getMana(),3)+"/"+CMStrings.padRightPreserve(""+who.maxState().getMana(),3)+")",12));
		msg.append(CMStrings.padRightPreserve("mv("+CMStrings.padRightPreserve(""+who.curState().getMovement(),3)+"/"+CMStrings.padRightPreserve(""+who.maxState().getMovement(),3)+")",12));
		msg.append("\n\r");
		return msg;
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		mob.tell(mob.name()+"'s group:\n\r");
		HashSet group=mob.getGroupMembers(new HashSet());
		StringBuffer msg=new StringBuffer("");
		for(Iterator e=group.iterator();e.hasNext();)
		{
			MOB follower=(MOB)e.next();
			msg.append(showWhoLong(follower));
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
