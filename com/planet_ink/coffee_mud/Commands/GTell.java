package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

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

		HashSet group=mob.getGroupMembers(new HashSet());
		for(Iterator e=group.iterator();e.hasNext();)
		{
			MOB target=(MOB)e.next();
			FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_TELL,"^T<S-NAME> tell(s) the group '"+text+"'^?^.",CMMsg.MSG_TELL,"^T<S-NAME> tell(s) the group '"+text+"'^?^.",CMMsg.NO_EFFECT,null);
			if((mob.location().okMessage(mob,msg))
			&&(target.okMessage(target,msg)))
			{
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
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
