package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.exceptions.HTTPRedirectException;

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
public class MOTD extends StdCommand
{
	public MOTD(){}

	private String[] access={"MOTD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands!=null)
		&&(commands.size()>1)
		&&(mob.playerStats()!=null)
		&&(Util.combine(commands,1).equalsIgnoreCase("AGAIN")
		   ||Util.combine(commands,1).equalsIgnoreCase("NEW")))
		{
			StringBuffer buf=new StringBuffer("");
			try
			{
				String msg = Resources.getFileResource("text"+File.separatorChar+"motd.txt",false).toString();
				if(msg.length()>0)
				{
					if(msg.startsWith("<cmvp>"))
						msg=new String(CMClass.httpUtils().doVirtualPage(msg.substring(6).getBytes()));
				    buf.append(msg+"\n\r--------------------------------------\n\r");
				}
		
				Vector journal=CMClass.DBEngine().DBReadJournal("CoffeeMud News");
				for(int which=0;which<journal.size();which++)
				{
					Vector entry=(Vector)journal.elementAt(which);
					String from=(String)entry.elementAt(1);
					long last=Util.s_long((String)entry.elementAt(2));
					String to=(String)entry.elementAt(3);
					String subject=(String)entry.elementAt(4);
					String message=(String)entry.elementAt(5);
					long compdate=Util.s_long((String)entry.elementAt(6));
					boolean mineAble=to.equalsIgnoreCase(mob.Name())||from.equalsIgnoreCase(mob.Name());
					if((compdate>mob.playerStats().lastDateTime())
					&&(to.equals("ALL")||mineAble))
					{
						if(message.startsWith("<cmvp>"))
							message=new String(CMClass.httpUtils().doVirtualPage(message.substring(6).getBytes()));
						buf.append("\n\rNews: "+IQCalendar.d2String(last)+"\n\r"+"FROM: "+Util.padRight(from,15)+"\n\rTO  : "+Util.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
						buf.append("\n\r--------------------------------------\n\r");
					}
				}
				if((mob.session()!=null)&&(buf.length()>0))
					mob.session().wraplessPrintln("\n\r--------------------------------------\n\r"+buf.toString());
			}
			catch(HTTPRedirectException e){}
			return false;
		}
		
		if(Util.bset(mob.getBitmap(),MOB.ATT_DAILYMESSAGE))
		{
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_DAILYMESSAGE));
			mob.tell("The daily message has been turned on.");
		}
		else
		{
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_DAILYMESSAGE));
			mob.tell("The daily message has been turned off.");
		}
		mob.tell("Enter MOTD AGAIN to see the message over again.");
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
