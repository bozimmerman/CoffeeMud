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
public class OutFit extends StdCommand
{
	public OutFit(){}

	private String[] access={"OUTFIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob==null) return false;
		if(mob.charStats()==null) return false;
		CharClass C=mob.charStats().getCurrentClass();
		Race R=mob.charStats().getMyRace();
		if(C!=null)
			CoffeeUtensils.outfit(mob,C.outfit());
		if(R!=null)
			CoffeeUtensils.outfit(mob,R.outfit());
		Command C2=CMClass.getCommand("Equipment");
		if(C2!=null) C2.execute(mob,Util.parse("EQUIPMENT"));
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
