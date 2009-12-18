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
public class Chants extends Skills
{
	public Chants(){}

	private String[] access={"CHANTS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("");
        String qual=CMParms.combine(commands,1).toUpperCase();
		if(parsedOutIndividualSkill(mob,qual,Ability.ACODE_CHANT))
			return true;
        int[] level=new int[1];
        int[] domain=new int[1];
        String[] domainName=new String[1];
        domainName[0]="";
        level[0]=-1;
        parseDomainInfo(mob,commands,CMParms.makeVector(Integer.valueOf(Ability.ACODE_CHANT)),level,domain,domainName);
        msg.append("\n\r^HYour "+domainName[0].replace('_',' ')+"chants:^? "+getAbilities(mob,Ability.ACODE_CHANT,domain[0],true,level[0]));
		if(!mob.isMonster())
			mob.session().wraplessPrintln(msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}

	
}
