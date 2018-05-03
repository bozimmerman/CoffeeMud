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
   Copyright 2004-2018 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Deities extends StdCommand
{
	public Deities(){}

	private final String[] access=I(new String[]{"DEITIES","GODS","DEITY"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class[][] internalParameters=new Class[][]{{Deity.class}};

	public String getDeityInformation(MOB mob, Deity D)
	{
		final StringBuffer msg = new StringBuffer("");
		msg.append("\n\r^x"+D.name()+"^.^?");
		if(D.hasFaction(CMLib.factions().AlignID()))
		{
			int faction=D.fetchFaction(CMLib.factions().AlignID());
			msg.append("^N ("+CMLib.factions().getRange(CMLib.factions().AlignID(), faction)+")");
		}
		msg.append("\n\r");
		msg.append(D.description()+"\n\r\n\r");
		if((mob==null)||(CMSecurity.isASysOp(mob)))
		{
			msg.append(D.getClericRequirementsDesc()+"\n\r\n\r");
			msg.append(D.getWorshipRequirementsDesc()+"\n\r");
		}
		else
		if(mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
			msg.append(D.getClericRequirementsDesc()+"\n\r");
		else
			msg.append(D.getWorshipRequirementsDesc()+"\n\r");
		if(D.numBlessings()>0)
		{
			msg.append(L("\n\r^HBlessings: ^N"));
			for(int b=0;b<D.numBlessings();b++)
			{
				msg.append(D.fetchBlessing(b).name());
				if(D.fetchBlessingCleric(b))
					msg.append(L(" (Clerics only)"));
				if(b<D.numBlessings()-1)
					msg.append(", ");
			}
			msg.append(L("\n\r^HBlessing Instructions: ^N"));
			if((mob==null)||(CMSecurity.isASysOp(mob)))
			{
				msg.append(D.getClericTriggerDesc()+"\n\r");
				msg.append(D.getWorshipTriggerDesc()+"\n\r");
			}
			else
			if(mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
				msg.append(D.getClericTriggerDesc()+"\n\r");
			else
				msg.append(D.getWorshipTriggerDesc()+"\n\r");
		}
		if((mob==null)||CMSecurity.isASysOp(mob)||mob.charStats().getCurrentClass().baseClass().equals("Cleric"))
		{
			if(D.numPowers()>0)
			{
				msg.append(L("\n\r^HGranted Powers: ^N"));
				for(int b=0;b<D.numPowers();b++)
				{
					msg.append(D.fetchPower(b).name());
					if(b<D.numPowers()-1)
						msg.append(", ");
				}
				msg.append(L("\n\r^HPowers Instructions: ^N"));
				msg.append(D.getClericPowerupDesc()+"\n\r");
			}
			msg.append(L("\n\r^HService Instructions: ^N"));
			msg.append(D.getServiceTriggerDesc()+"\n\r");
		}
		return msg.toString();
	}

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE.toString();
		return this.getDeityInformation(mob, (Deity)args[0]);
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final String str=CMParms.combine(commands,1).toUpperCase();
		final StringBuffer msg=new StringBuffer("");
		if(str.length()==0)
			msg.append(L("\n\r^xThe known deities:^.^? \n\r\n\r"));
		else
			msg.append(L("\n\r^HThe known deities named '@x1':^? \n\r",str));
		int col=0;
		final int colWidth=CMLib.lister().fixColWidth(18,mob.session());
		for(final Enumeration d=CMLib.map().deities();d.hasMoreElements();)
		{
			final Deity D=(Deity)d.nextElement();
			if((str.length()>0)&&(CMLib.english().containsString(D.name(),str)))
				msg.append(this.getDeityInformation(mob, D));
			else
			if(str.length()==0)
			{
				col++;
				if(col>4)
				{
					msg.append("\n\r");
					col=0;
				}
				msg.append(CMStrings.padRight("^H"+D.name()+"^?",colWidth));
			}
		}
		if(str.length()==0)
			msg.append(L("\n\r\n\r^xUse DEITIES <NAME> to see important details on each deity!^.^N\n\r"));
		mob.tell(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
