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
public class Mount extends StdCommand
{
	public Mount(){}

	private String[] access={"MOUNT","BOARD","RIDE","M"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell(getScr("Movement","mounterr1",((String)commands.elementAt(0))));
			return false;
		}
		commands.removeElementAt(0);
		Environmental recipient=null;
		Vector possRecipients=new Vector();
		for(int m=0;m<mob.location().numInhabitants();m++)
		{
			MOB M=mob.location().fetchInhabitant(m);
			if((M!=null)&&(M instanceof Rideable))
				possRecipients.addElement(M);
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I instanceof Rideable))
				possRecipients.addElement(I);
		}
		Rider RI=null;
		if(commands.size()>1)
		{
			Item I=mob.location().fetchItem(null,(String)commands.firstElement());
			if(I!=null)
			{
				commands.removeElementAt(0);
				I.setRiding(null);
				RI=I;
			}
			if(RI==null)
			{
			    MOB M=mob.location().fetchInhabitant((String)commands.firstElement());
			    if(M!=null)
			    {
			        if(!Sense.canBeSeenBy(M,mob))
			        {
			            mob.tell("You don't see "+((String)commands.firstElement())+" here.");
			            return false;
			        }
			        if((!Sense.isBoundOrHeld(M))&&(!M.willFollowOrdersOf(mob)))
			        {
			            mob.tell("Only the bound or servants can be mounted unwillingly.");
			            return false;
			        }
			        RI=M;
			        RI.setRiding(null);
			        commands.removeElementAt(0);
			    }
			}
		}
		recipient=EnglishParser.fetchEnvironmental(possRecipients,Util.combine(commands,0),true);
		if(recipient==null)
			recipient=EnglishParser.fetchEnvironmental(possRecipients,Util.combine(commands,0),false);
		if(recipient==null)
			recipient=mob.location().fetchFromRoomFavorMOBs(null,Util.combine(commands,0),Item.WORN_REQ_UNWORNONLY);
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell(getScr("Movement","youdontsee",Util.combine(commands,0)));
			return false;
		}
		String mountStr=null;
		if(recipient instanceof Rideable)
		{
			if(RI!=null)
				mountStr=getScr("Movement","mountonto");
			else
				mountStr=getScr("Movement","mounton",((Rideable)recipient).mountString(CMMsg.TYP_MOUNT,mob));
		}
		else
		{
			if(RI!=null)
				mountStr=getScr("Movement","mountsto");
			else
				mountStr=getScr("Movement","mounts");
		}
		FullMsg msg=new FullMsg(mob,recipient,RI,CMMsg.MSG_MOUNT,mountStr);
		if(mob.location().okMessage(mob,msg))
			mob.location().send(mob,msg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
