package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/*
* <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
* <p>Portions Copyright (c) 2004 Bo Zimmerman</p>

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
public class Announce extends StdCommand
{
	public Announce(){}

	private String[] access={getScr("Announce","cmd"),getScr("Announce","cmdt"),getScr("Announce","cmdm")};
	public String[] getAccessWords(){return access;}

	public void sendAnnounce(String announcement, Session S)
	{
	  	StringBuffer Message=new StringBuffer("");
        if((S.mob()!=null)&&(S.mob().playerStats()!=null)&&(S.mob().playerStats().announceMessage().length()>0))
            Message.append(S.mob().playerStats().announceMessage()+" '"+announcement+"'.^.^N");
        else
        {
    	  	int alignType=2;
            if (Sense.isEvil(S.mob()))
                alignType = 0;
            else
            if (Sense.isGood(S.mob()))
                alignType = 1;
    	  	switch(alignType)
    	  	{
    	  	  case 0:
    	  	    Message.append(getScr("Announce","evil",announcement));
    	  	    break;
    	  	  case 1:
    	  	    Message.append(getScr("Announce","good",announcement));
    	  	    break;
    	  	  case 2:
    	  	    Message.append(getScr("Announce","neutral",announcement));
    	  	    break;
    	  	}
        }
	  	S.stdPrintln(Message.toString());
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
        
        String cmd=((String)commands.firstElement()).toUpperCase();
        if((!cmd.equalsIgnoreCase(getScr("Announce","cmdm")))
        &&(!cmd.equalsIgnoreCase(getScr("Announce","cmdt")))
        &&(!cmd.equalsIgnoreCase(getScr("Announce","cmd"))))
        {
            boolean cmdm=getScr("Announce","cmdm").toUpperCase().startsWith(cmd);
            boolean cmdt=getScr("Announce","cmdt").toUpperCase().startsWith(cmd);
            boolean cmd1=getScr("Announce","cmd").toUpperCase().startsWith(cmd);
            if(cmdm&&(!cmdt)&&(!cmd1))
                cmd=getScr("Announce","cmdm");
            else
            if(cmdt&&(!cmdm)&&(!cmd1))
                cmd=getScr("Announce","cmdt");
            else
            if(cmd1&&(!cmdm)&&(!cmdt))
                cmd=getScr("Announce","cmd");
        }
		if(commands.size()>1)
		{
            if(cmd.equalsIgnoreCase(getScr("Announce","cmdm")))
            {
                String s=Util.combine(commands,1);
                if(mob.playerStats()!=null)
                    mob.playerStats().setAnnounceMessage(s);
                mob.tell("Your announce message has been changed.");
            }
            else
            if((!cmd.equalsIgnoreCase(getScr("Announce","cmdt")))
            ||(((String)commands.elementAt(1)).toUpperCase().equals(getScr("Announce","all"))))
			{
                String text=null;
                if(cmd.equalsIgnoreCase(getScr("Announce","cmdt")))
                    text=Util.combine(commands,2);
                else
                    text=Util.combine(commands,1);
                    
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"ANNOUNCE")))
						sendAnnounce(text,S);
				}
			}
			else
			{
				boolean found=false;
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(CMSecurity.isAllowed(mob,S.mob().location(),"ANNOUNCE"))
					&&(EnglishParser.containsString(S.mob().name(),(String)commands.elementAt(1))))
					{
						sendAnnounce(Util.combine(commands,2),S);
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell(getScr("Announce","notfound"));
			}
		}
		else
			mob.tell(getScr("Announce","exp"));
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"ANNOUNCE");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
