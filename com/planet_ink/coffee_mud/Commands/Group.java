package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

	private String[] access={"GROUP","GR"};
	public String[] getAccessWords(){return access;}
	
	public static StringBuffer showWhoLong(MOB who)
	{

		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		if(!CMSecurity.isDisabled("RACES"))
			msg.append(Util.padRight(who.charStats().raceName(),7)+" ");
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		int x=levelStr.lastIndexOf(" ");
		if(x>=0) levelStr=levelStr.substring(x).trim();
		if(!CMSecurity.isDisabled("CLASSES"))
			msg.append(Util.padRight(who.charStats().displayClassName(),7)+" ");
		if(!CMSecurity.isDisabled("LEVELS"))
			msg.append(Util.padRight(levelStr,5));
		msg.append("] "+Util.padRight(who.name(),13)+" ");
		msg.append(Util.padRightPreserve("hp("+Util.padRightPreserve(""+who.curState().getHitPoints(),3)+"/"+Util.padRightPreserve(""+who.maxState().getHitPoints(),3)+")",12));
		msg.append(Util.padRightPreserve("mn("+Util.padRightPreserve(""+who.curState().getMana(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMana(),3)+")",12));
		msg.append(Util.padRightPreserve("mv("+Util.padRightPreserve(""+who.curState().getMovement(),3)+"/"+Util.padRightPreserve(""+who.maxState().getMovement(),3)+")",12));
		msg.append("\n\r");
		return msg;
	}
	
	public boolean execute(MOB mob, Vector commands)
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
