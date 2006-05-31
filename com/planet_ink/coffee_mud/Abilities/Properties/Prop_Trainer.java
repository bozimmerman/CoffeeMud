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
			MOB mob=(MOB)affected;
			CharClass C=null;
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				C=(CharClass)c.nextElement();
				addCharClassIfNotFound(mob,C);
			}
			for(Enumeration e=CMLib.edu().definitions();e.hasMoreElements();)
			{
				EducationLibrary.EducationDefinition def=(EducationLibrary.EducationDefinition)e.nextElement();
				if(mob.fetchEducation(def.ID)==null) mob.addEducation(def.ID);
			}
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
		}
		return super.tick(ticking,tickID);
	}
}
