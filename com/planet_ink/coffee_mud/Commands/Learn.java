package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary.ExpertiseDefinition;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Learn extends StdCommand
{
	public Learn(){}

	private final String[] access={"LEARN"};
	@Override public String[] getAccessWords(){return access;}
	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.location().numInhabitants()==1)
		{
			mob.tell("You will need to find someone to teach you first.");
			return false;
		}
		if(commands.size()==1)
		{
			mob.tell("Learn what?  Enter QUALIFY or TRAIN to see what you can learn.");
			return false;
		}
		commands.removeElementAt(0);
		String teacherName="";
		String sayTo="SAY";
		if(commands.size()>1)
		{
			teacherName="\""+((String)commands.lastElement())+"\" ";
			if((teacherName.length()>1)&&(mob.location().fetchFromRoomFavorMOBs(null, (String)commands.lastElement()) instanceof MOB))
			{
				sayTo="SAYTO";
				commands.removeElementAt(commands.size()-1);
				if((commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("FROM")))
					commands.removeElementAt(commands.size()-1);
			}
			else
				teacherName="";
		}

		String what=CMParms.combine(commands,0);
		Vector V=Train.getAllPossibleThingsToTrainFor();
		if(V.contains(what.toUpperCase().trim()))
		{
			Vector CC=CMParms.parse(sayTo+" "+teacherName+"I would like to be trained in "+what);
			mob.doCommand(CC,metaFlags);
			Command C=CMClass.getCommand("TRAIN");
			if(C!=null) C.execute(mob, commands,metaFlags);
			return true;
		}
		if(CMClass.findAbility(what, mob)!=null)
		{
			Vector CC=CMParms.parse(sayTo+" "+teacherName+"I would like you to teach me "+what);
			mob.doCommand(CC,metaFlags);
			return true;
		}
		ExpertiseLibrary.ExpertiseDefinition theExpertise=null;
		List<ExpertiseDefinition> V2=CMLib.expertises().myListableExpertises(mob);
		for(Iterator<ExpertiseDefinition> i=V2.iterator();i.hasNext();)
		{
			ExpertiseLibrary.ExpertiseDefinition def=i.next();
			if((def.name.equalsIgnoreCase(what)
			||def.name.equalsIgnoreCase(what))
			||(def.name.toLowerCase().startsWith((what).toLowerCase())
					&&(CMath.isRomanNumeral(def.name.substring((what).length()).trim())||CMath.isNumber(def.name.substring((what).length()).trim())))
			)
			{ theExpertise=def; break;}
		}
		if(theExpertise==null)
		for(Enumeration<ExpertiseDefinition> e=CMLib.expertises().definitions();e.hasMoreElements();)
		{
			ExpertiseLibrary.ExpertiseDefinition def=e.nextElement();
			if(def.name.equalsIgnoreCase(what))
			{ theExpertise=def; break;}
		}
		if(theExpertise!=null)
		{
			Vector CC=new XVector("SAY","I would like you to teach me "+theExpertise.name);
			mob.doCommand(CC,metaFlags);
			return true;
		}

		for(int v=0;v<V.size();v++)
			if(((String)V.elementAt(v)).startsWith(what.toUpperCase().trim()))
			{
				Vector CC=CMParms.parse(sayTo+" "+teacherName+"I would like to be trained in "+what);
				mob.doCommand(CC,metaFlags);
				Command C=CMClass.getCommand("TRAIN");
				if(C!=null) C.execute(mob, commands,metaFlags);
				return true;

			}
		Vector CC=CMParms.parse(sayTo+" "+teacherName+"I would like you to teach me "+what);
		mob.doCommand(CC,metaFlags);
		return false;
	}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	@Override public boolean canBeOrdered(){return false;}


}
