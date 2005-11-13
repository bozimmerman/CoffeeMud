package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.File;
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
public class Sounds extends StdCommand
{
	public Sounds(){}

	private String[] access={"SOUNDS","MSP"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
        if(!mob.isMonster())
        {
            if((!Util.bset(mob.getBitmap(),MOB.ATT_SOUND))
            ||(!Util.bset(mob.session().getTermID(),Session.TERM_MSP)))
            {
                if(mob.session().supports(Session.TERM_MSP))
                {
                    mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_SOUND));
                    mob.session().setTermID(mob.session().getTermID()|Session.TERM_MSP);
                    mob.session().requestServerChangeOption(Session.TELNET_MSP,true);
                    mob.tell("MSP Sound/Music enabled.\n\r");
                }
                else
                    mob.tell("Your client does not appear to support MSP.");
            }
            else
            {
                mob.tell("MSP Sound/Music is already enabled.\n\r");
            }
        }
        return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
