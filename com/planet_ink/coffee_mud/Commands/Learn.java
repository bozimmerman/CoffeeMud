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
   Copyright 2000-2006 Bo Zimmerman

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
public class Learn extends StdCommand
{
	public Learn(){}

	private String[] access={"LEARN"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()==1)
		{
			mob.tell(getScr("AbilityEvoker","learnerr"));
			return false;
		}
		commands.removeElementAt(0);
        String teacherName=null;
        if(commands.size()>1)
        {
            teacherName=(String)commands.lastElement();
            if(teacherName.length()>1)
            {
                commands.removeElementAt(commands.size()-1);
                if((commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("FROM")))
                    commands.removeElementAt(commands.size()-1);
            }
            else
                teacherName=null;
        }
		
		String what=CMParms.combine(commands,0);
		Vector V=Train.getAllPossibleThingsToTrainFor();
		if(V.contains(what.toUpperCase().trim()))
		{
			Vector CC=CMParms.makeVector(getScr("CommandSet","say"),getScr("AbilityEvoker","trainme",what));
			mob.doCommand(CC);
			Command C=CMClass.getCommand("TRAIN");
			if(C!=null) C.execute(mob, commands);
			return true;
		}
		if(CMClass.findAbility(what, mob)!=null)
		{
			Vector CC=CMParms.makeVector(getScr("CommandSet","say"),getScr("AbilityEvoker","teachme",what));
			mob.doCommand(CC);
			return true;
		}
		for(int v=0;v<V.size();v++)
			if(((String)V.elementAt(v)).startsWith(what.toUpperCase().trim()))
			{
				Vector CC=CMParms.makeVector(getScr("CommandSet","say"),getScr("AbilityEvoker","trainme",what));
				mob.doCommand(CC);
				Command C=CMClass.getCommand("TRAIN");
				if(C!=null) C.execute(mob, commands);
				return true;
				
			}
		Vector CC=CMParms.makeVector(getScr("CommandSet","say"),getScr("AbilityEvoker","teachme",what));
		mob.doCommand(CC);
		return false;
	}
    public double combatActionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return false;}

	
}
