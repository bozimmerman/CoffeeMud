package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.File;
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
public class MXP extends StdCommand
{
	public MXP(){}

	private String[] access={"MXP"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isMonster())
		{
			if((!Util.bset(mob.getBitmap(),MOB.ATT_MXP))||(!Util.bset(mob.session().getTermID(),Session.TERM_MXP)))
			{
			    if(mob.session().supports(Session.TERM_MXP))
			    {
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_MXP));
					mob.session().setTermID(mob.session().getTermID()|Session.TERM_MXP);
					StringBuffer mxpText=Resources.getFileResource("text"+File.separatorChar+"mxp.txt");
			        if(mxpText!=null)
			            mob.session().rawPrintln("\033[6z\n\r"+mxpText.toString()+"\n\r");
					mob.tell("MXP codes enabled.\n\r");
			    }
			    else
			        mob.tell("Your client does not appear to support MXP.");
			}
			else
			{
				mob.tell("MXP codes are already enabled.\n\r");
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}

