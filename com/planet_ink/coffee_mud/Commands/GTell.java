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
public class GTell extends StdCommand
{
	public GTell(){}

	private String[] access={"GTELL","GT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String text=Util.combine(commands,1);
		if(text.length()==0)
		{
			mob.tell("Tell the group what?");
			return false;
		}
		text=CommonStrings.applyFilter(text,CommonStrings.SYSTEM_SAYFILTER);
		
		if((commands.size()>2)
		&&((((String)commands.elementAt(1)).equalsIgnoreCase("last"))
		&&(Util.isNumber(Util.combine(commands,2))))
		&&(mob.playerStats()!=null))
		{
			Vector V=mob.playerStats().getGTellStack();
			if(V.size()==0)
				mob.tell("No telling.");
			else
			{
				int num=Util.s_int(Util.combine(commands,2));
				if(num>V.size()) num=V.size();
				for(int i=V.size()-num;i<V.size();i++)
					mob.tell((String)V.elementAt(i));
			}
			return false;
		}
								 

		HashSet group=mob.getGroupMembers(new HashSet());
		for(Iterator e=group.iterator();e.hasNext();)
		{
			MOB target=(MOB)e.next();
			FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_TELL,"^t<S-NAME> tell(s) the group '"+text+"'^?^.",CMMsg.MSG_TELL,"^t<S-NAME> tell(s) the group '"+text+"'^?^.",CMMsg.NO_EFFECT,null);
			if((mob.location().okMessage(mob,msg))
			&&(target.okMessage(target,msg)))
			{
				if(target.playerStats()!=null)
					target.playerStats().addGTellStack(CoffeeFilter.fullOutFilter(target.session(),target,mob,target,null,msg.sourceMessage(),false));
				target.executeMsg(target,msg);
				if(msg.trailerMsgs()!=null)
				{
					for(int i=0;i<msg.trailerMsgs().size();i++)
					{
						CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
						if((msg!=msg)&&(target.okMessage(target,msg2)))
							target.executeMsg(target,msg2);
					}
					msg.trailerMsgs().clear();
				}
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
