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
public class AHelp extends StdCommand
{
	public AHelp(){}

	private String[] access={"ARCHELP","AHELP"};
	public String[] getAccessWords(){return access;}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String helpStr=CMParms.combine(commands,1);
		if(CMLib.help().getArcHelpFile().size()==0)
		{
			mob.tell("No archon help is available.");
			return false;
		}
		StringBuffer thisTag=null;
		if(helpStr.length()==0)
		{
			thisTag=Resources.getFileResource("help/arc_help.txt",true);
			if((thisTag!=null)&&(helpStr.equalsIgnoreCase("more")))
			{
				StringBuffer theRest=(StringBuffer)Resources.getResource("arc_help.therest");
				if(theRest==null)
				{
					Vector V=new Vector();
					theRest=new StringBuffer("");

					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PROPERTY))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append("\n\rProperties:\n\r");
						theRest.append(CMLib.lister().fourColumns(V));
					}

					V.clear();
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_DISEASE))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append("\n\rDiseases:\n\r");
						theRest.append(CMLib.lister().fourColumns(V));
					}

					V.clear();
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_POISON))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append("\n\rPoisons:\n\r");
						theRest.append(CMLib.lister().fourColumns(V));
					}

					V.clear();
					for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
					{
						Ability A=(Ability)a.nextElement();
						if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SUPERPOWER))
							V.addElement(A.ID());
					}
					if(V.size()>0)
					{
					    theRest.append("\n\rSuper Powers:\n\r");
						theRest.append(CMLib.lister().fourColumns(V));
					}

					V.clear();
					for(Enumeration b=CMClass.behaviors();b.hasMoreElements();)
					{
						Behavior B=(Behavior)b.nextElement();
						if(B!=null) V.addElement(B.ID());
					}
					if(V.size()>0)
					{
					    theRest.append("\n\r\n\rBehaviors:\n\r");
						theRest.append(CMLib.lister().fourColumns(V));
					}
					Resources.submitResource("arc_help.therest",theRest);
				}
				thisTag=new StringBuffer(thisTag.toString());
				thisTag.append(theRest);
			}
		}
		else
			thisTag=new StringBuffer(CMLib.help().getHelpText(helpStr,CMLib.help().getArcHelpFile(),mob).toString());
		if(thisTag==null)
		{
			mob.tell("No archon help is available on "+helpStr+" .\n\rEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			Log.errOut("Help: "+mob.name()+" wanted archon help on "+helpStr);
		}
		else
		if(!mob.isMonster())
			mob.session().wraplessPrintln(thisTag.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"AHELP");}

	
}
