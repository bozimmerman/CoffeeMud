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
public class ListSessions extends StdCommand
{
	public ListSessions(){}

	private String[] access={"SESSIONS"};
	public String[] getAccessWords(){return access;}


	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String sort="";
		if((commands!=null)&&(commands.size()>1))
			sort=Util.combine(commands,1).trim().toUpperCase();
		StringBuffer lines=new StringBuffer("^x");
		lines.append(Util.padRight("Status",9)+"| ");
		lines.append(Util.padRight("Valid",5)+"| ");
		lines.append(Util.padRight("Name",17)+"| ");
		lines.append(Util.padRight("IP",17)+"| ");
		lines.append(Util.padRight("Idle",17)+"^.^N\n\r");
		Vector broken=new Vector();
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=Sessions.elementAt(s);
			String[] set=new String[5];
			set[0]=(thisSession.killFlag()?"^H":"")+Util.padRight(Session.statusStr[thisSession.getStatus()],9)+(thisSession.killFlag()?"^?":"")+"| ";
			if (thisSession.mob() != null)
			{
				set[1]=Util.padRight(((thisSession.mob().session()==thisSession)?"Yes":"^HNO!^?"),5)+"| ";
				set[2]="^!"+Util.padRight("^<LSTUSER^>"+thisSession.mob().Name()+"^</LSTUSER^>",17)+"^?| ";
			}
			else
			{
				set[1]=Util.padRight("N/A",5)+"| ";
				set[2]=Util.padRight("NAMELESS",17)+"| ";
			}
			set[3]=Util.padRight(thisSession.getAddress(),17)+"| ";
			set[4]=Util.padRight((thisSession.getIdleMillis()+""),17);
			broken.addElement(set);
		}
		Vector sorted=null;
		int sortNum=-1;
		if(sort.length()>0)
		{
			if("STATUS".startsWith(sort))
				sortNum=0;
			else
			if("VALID".startsWith(sort))
				sortNum=1;
			else
			if(("NAME".startsWith(sort))||("PLAYER".startsWith(sort)))
				sortNum=2;
			else
			if(("IP".startsWith(sort))||("ADDRESS".startsWith(sort)))
				sortNum=3;
			else
			if(("IDLE".startsWith(sort))||("MILLISECONDS".startsWith(sort)))
				sortNum=4;
		}
		if(sortNum<0)
			sorted=broken;
		else
		{
			sorted=new Vector();
			while(broken.size()>0)
			{
				int selected=0;
				for(int s=1;s<broken.size();s++)
				{
					String[] S=(String[])broken.elementAt(s);
					if(S[sortNum].compareToIgnoreCase(((String[])broken.elementAt(selected))[sortNum])<0)
					   selected=s;
				}
				sorted.addElement(broken.elementAt(selected));
				broken.removeElementAt(selected);
			}
		}
		for(int s=0;s<sorted.size();s++)
		{
			String[] S=(String[])sorted.elementAt(s);
			for(int i=0;i<S.length;i++)
				lines.append(S[i]);
			lines.append("\n\r");
		}
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(lines.toString());
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"SESSIONS");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
