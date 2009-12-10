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
public class Title extends StdCommand
{
	private String[] access={"TITLE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
	    if((mob.playerStats()==null)||(mob.playerStats().getTitles().size()==0))
	    {
	        mob.tell("You don't have any titles to select from.");
	        return false;
	    }
        String currTitle=(String)mob.playerStats().getTitles().elementAt(0);
        if(currTitle.startsWith("{")&&currTitle.endsWith("}"))
        {
            mob.tell("You can not change your current title.");
            return false;
        }
	    PlayerStats ps=mob.playerStats();
	    StringBuffer menu=new StringBuffer("^xTitles:^.^?\n\r");
        CMLib.titles().evaluateAutoTitles(mob);
	    if(!ps.getTitles().contains("*")) ps.getTitles().addElement("*");
	    for(int i=0;i<ps.getTitles().size();i++)
	    {
	        String title=(String)ps.getTitles().elementAt(i);
            if(title.startsWith("{")&&title.endsWith("}")) title=title.substring(1,title.length()-1);
	        if(title.equalsIgnoreCase("*"))
	    	    menu.append(CMStrings.padRight(""+(i+1),2)+": Do not use a title.\n\r");
	        else
		        menu.append(CMStrings.padRight(""+(i+1),2)+": "+CMStrings.replaceAll(title,"*",mob.Name())+"\n\r");
	    }
	    int selection=1;
	    while((mob.session()!=null)&&(!mob.session().killFlag()))
	    {
		    mob.tell(menu.toString());
	        String which=mob.session().prompt("Enter a selection: ",""+selection);
	        if(which.length()==0)
	            break;
	        int num=CMath.s_int(which);
	        if((num>0)&&(num<=ps.getTitles().size()))
	        {
	            selection=num;
	            break;
	        }
	    }
	    if(selection==1)
	        mob.tell("No change");
	    else
	    {
	        String which=(String)ps.getTitles().elementAt(selection-1);
	        ps.getTitles().removeElementAt(selection-1);
	        ps.getTitles().insertElementAt(which,0);
	        mob.tell("Title changed accepted.");
	    }
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}

