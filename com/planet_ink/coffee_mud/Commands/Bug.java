package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Bug extends StdCommand
{
	public Bug(){}

	private String[] access={getScr("Bug","cmd")};
	public String[] getAccessWords(){return access;}
	
	public boolean review(MOB mob,
						  String journalID, 
						  String journalWord,
						  Vector commands,
						  String security)
	{
		String first=(String)commands.elementAt(1);
		String second=(commands.size()>2)?Util.combine(commands,2):"";
		if(!("REVIEW".startsWith(first.toUpperCase().trim())))
		   return false;
		if(!CMSecurity.isAllowed(mob,mob.location(),security))
			return false;
		if((second.length()>0)&&(!Util.isNumber(second)))
			return false;
		int count=Util.s_int(second);
			
		Item journalItem=CMClass.getItem("StdJournal");
		if(journalItem==null)
			mob.tell(getScr("Bug","featdis"));
		else
		{
			Vector journal=CMClass.DBEngine().DBReadJournal(journalID);
			int size=0;
			if(journal!=null) size=journal.size();
			if(size<=0)
				mob.tell(getScr("Bug","nolisted",journalWord));
			else
			{
				journalItem.setName(journalID);						
				if(count>size)
					mob.tell(getScr("Bug","maxcount",journalWord,""+size));
				else
				while(count<=size)
				{
					FullMsg msg=new FullMsg(mob,journalItem,null,CMMsg.MSG_READ,null,CMMsg.MSG_READ,""+count,CMMsg.MSG_READ,null);
					msg.setValue(1);
					journalItem.executeMsg(mob,msg);
					if(msg.value()==0)
						break;
					else
					if(msg.value()<0)
						size--;
					else
						count++;
				}
			}
		}
		return true;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(Util.combine(commands,1).length()>0)
		{
			if(!review(mob,"SYSTEM_BUGS","bugs",commands,"KILLBUGS"))
			{
				CMClass.DBEngine().DBWriteJournal("SYSTEM_BUGS",mob.Name(),"ALL","BUG: "+Util.padRight(Util.combine(commands,1),10),Util.combine(commands,1),-1);
				mob.tell(getScr("Bug","thankyou"));
			}
		}
		else
			mob.tell(getScr("Bug","whats"));
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
