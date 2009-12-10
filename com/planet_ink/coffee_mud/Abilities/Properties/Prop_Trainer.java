package com.planet_ink.coffee_mud.Abilities.Properties;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Prop_Trainer extends Prop_StatTrainer
{
	public String ID() { return "Prop_Trainer"; }
	public String name(){ return "THE Training MOB";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	public String accountForYourself()
	{ return "Trainer";	}
	private boolean built=false;

	private void addCharClassIfNotFound(MOB mob, CharClass C)
	{
		boolean found=false;
		for(int n=0;n<mob.baseCharStats().numClasses();n++)
			if(mob.baseCharStats().getMyClass(n).ID().equals(C.ID()))
			{ found=true; break;}
		if(!found)
		{
			mob.baseCharStats().setCurrentClass(C);
			mob.baseCharStats().setClassLevel(C,0);
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if((!built)&&(affected instanceof MOB))
		{
			built=true;
			CharClass C=null;
			Vector allowedClasses=new Vector();
			Vector allowedExpertises=new Vector();
			Vector V=CMParms.parse(text());
			String s=null;
			for(int v=0;v<V.size();v++)
			{
				s=(String)V.elementAt(v);
				if(s.equalsIgnoreCase("all")) continue;
				C=CMClass.getCharClass(s);
				if(C!=null)
				{
					if((v>0)&&(((String)V.elementAt(v-1)).equalsIgnoreCase("ALL")))
					{
						String baseClass=C.baseClass();
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							C=(CharClass)c.nextElement();
							if((C.baseClass().equalsIgnoreCase(baseClass))
							&&(!allowedClasses.contains(C)))
								allowedClasses.addElement(C);
						}
					}
					else
						allowedClasses.addElement(C);
				}
				else
                {
                    ExpertiseLibrary.ExpertiseDefinition def=CMLib.expertises().getDefinition(s);
    				if(def!=null) allowedExpertises.addElement(def);
                }
			}
			if(allowedClasses.size()==0)
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
				allowedClasses.addElement(c.nextElement());
			if(allowedExpertises.size()==0)
			for(Enumeration e=CMLib.expertises().definitions();e.hasMoreElements();)
				allowedExpertises.addElement((ExpertiseLibrary.ExpertiseDefinition)e.nextElement());
			
			
			MOB mob=(MOB)affected;
			for(int c=0;c<allowedClasses.size();c++)
			{
				C=(CharClass)allowedClasses.elementAt(c);
				addCharClassIfNotFound(mob,C);
			}
			for(int e=0;e<allowedExpertises.size();e++)
			{
				ExpertiseLibrary.ExpertiseDefinition def=(ExpertiseLibrary.ExpertiseDefinition)allowedExpertises.elementAt(e);
				if(mob.fetchExpertise(def.ID)==null) mob.addExpertise(def.ID);
			}
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
		}
		return super.tick(ticking,tickID);
	}
}
