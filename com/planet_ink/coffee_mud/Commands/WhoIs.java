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
public class WhoIs extends Who
{
	public WhoIs(){}

	private String[] access={"WHOIS"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		String mobName=Util.combine(commands,1);
		if((mobName==null)||(mobName.length()==0))
		{
			mob.tell("whois whom?");
			return false;
		}

		if(mobName.startsWith("@"))
		{
			if((!(CMClass.I3Interface().i3online()))
			&&(!CMClass.I3Interface().imc2online()))
				mob.tell("Intermud is unavailable.");
			else
				CMClass.I3Interface().i3who(mob,mobName.substring(1));
			return false;
		}

		StringBuffer msg=new StringBuffer("");
		for(int s=0;s<Sessions.size();s++)
		{
			Session thisSession=Sessions.elementAt(s);
			MOB mob2=thisSession.mob();
			if((mob2!=null)
			&&(!thisSession.killFlag())
			&&((((mob2.envStats().disposition()&EnvStats.IS_CLOAKED)==0)
				||((CMSecurity.isAllowedAnywhere(mob,"CLOAK")||CMSecurity.isAllowedAnywhere(mob,"WIZINV"))&&(mob.envStats().level()>=mob2.envStats().level()))))
			&&(mob2.envStats().level()>0)
			&&(mob2.name().toUpperCase().startsWith(mobName.toUpperCase())))
				msg.append(showWhoShort(mob2));
		}
		if((mobName!=null)&&(msg.length()==0))
			mob.tell("That person doesn't appear to be online.\n\r");
		else
		{
			StringBuffer head=new StringBuffer("");
			head.append("^x[");
			head.append(Util.padRight("Race",12)+" ");
			head.append(Util.padRight("Class",12)+" ");
			head.append(Util.padRight("Level",7));
			head.append("] Character name^.^N\n\r");
			mob.tell(head.toString()+msg.toString());
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
