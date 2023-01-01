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
import com.planet_ink.coffee_mud.Libraries.interfaces.ListingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2023 Bo Zimmerman

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
public class Abilities extends Skills
{
	public Abilities()
	{
	}

	private final String[]	access	= I(new String[] { "ABILITIES", "ABILITYS", "ABLES" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}
	
	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final String qual=CMParms.combine(commands,1).toUpperCase();
		if(parsedOutIndividualSkill(mob,qual,null))
			return true;
		final int[] level=new int[]{-1};
		final int[] domain=new int[]{-1};
		final int[] type=new int[]{Ability.ALL_ACODES};
		final String[] typeName=new String[] {""};
		final String[] domainName=new String[] {""};
		parseTypeInfo(mob,commands,level,type,typeName);
		if(typeName[0].length()==0)
			parseDomainInfo(mob,commands,null,level,domain,domainName);
		if(qual.equalsIgnoreCase("?"))
			return false;
		if(type[0]<0)
			type[0]=Ability.ALL_ACODES;
		if(typeName[0].length()>0)
		{
			final StringBuilder msg=new StringBuilder("");
			msg.append(L("\n\r^HYour @x1@x2:^? ",domainName[0].replace('_',' '),CMLib.english().makePlural(Ability.ACODE_DESCS[type[0]].toLowerCase())));
			StringBuilder ableListStr = getAbilities(mob,mob,type[0],domain[0],true,level[0]);
			if(!mob.isMonster())
				mob.session().wraplessPrintln(msg.toString()+ableListStr.toString()+"\n\r");
		}
		else
		{
			boolean anythingShown=false;
			for(final int acode : playerAcodes)
			{
				final StringBuilder msg=new StringBuilder("");
				msg.append(L("\n\r^HYour @x1@x2:^? ",domainName[0].replace('_',' '),CMLib.english().makePlural(Ability.ACODE_DESCS[acode].toLowerCase())));
				StringBuilder ableListStr = getAbilities(mob,mob,acode,domain[0],acode==playerAcodes[playerAcodes.length-1],level[0]);
				if(ableListStr.length()<10)
					continue;
				anythingShown=true;
				if(!mob.isMonster())
					mob.session().wraplessPrintln(msg.toString()+ableListStr.toString()+"\n\r");
			}
			if((!mob.isMonster())&&(!anythingShown))
				mob.tell(L("You have no abilities at all?!"));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
