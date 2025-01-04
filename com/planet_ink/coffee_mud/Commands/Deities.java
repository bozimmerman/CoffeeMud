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
   Copyright 2004-2025 Bo Zimmerman

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
public class Deities extends StdCommand
{
	public Deities()
	{
	}

	private final String[] access=I(new String[]{"DEITIES","GODS","DEITY"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]{{Deity.class},{Deity.class,Boolean.class}};

	protected void addToSet(final Set<String> set, final Item I)
	{
		if(I instanceof Weapon)
			set.add(I.name());
	}

	protected Set<String> getWeapons(final MOB mob, final Deity D)
	{
		final Set<String> set=new HashSet<String>();
		if(D != null)
		{
			addToSet(set, D.fetchWieldedItem());
			addToSet(set, D.fetchHeldItem());
			for(final Enumeration<Item> i=D.items();i.hasMoreElements();)
				addToSet(set,i.nextElement());
		}
		return set;
	}

	public String getDeityInformation(final MOB mob, final Deity D, final boolean nameOnly)
	{
		final StringBuffer msg = new StringBuffer("");
		msg.append("^x"+D.name()+"^.^?");
		if(D.hasFaction(CMLib.factions().getAlignmentID())||D.hasFaction(CMLib.factions().getInclinationID()))
		{
			msg.append("^N (");
			int faction=D.fetchFaction(CMLib.factions().getAlignmentID());
			final Faction.FRange range1=CMLib.factions().getRange(CMLib.factions().getAlignmentID(), faction);
			if(range1!=null)
				msg.append(range1.name());
			faction=D.fetchFaction(CMLib.factions().getInclinationID());
			final Faction.FRange range2=CMLib.factions().getRange(CMLib.factions().getInclinationID(), faction);
			if(range2!=null)
				msg.append((range1!=null)?"/":"").append(range2.name());
			msg.append(")");
		}
		msg.append("\n\r");
		if(nameOnly)
			return msg.toString();
		msg.append(D.description()+"\n\r\n\r");
		if((mob==null)||(CMSecurity.isASysOp(mob)))
		{
			msg.append(D.getClericRequirementsDesc()+"\n\r\n\r");
			msg.append(D.getWorshipRequirementsDesc()+"\n\r");
		}
		else
		if(mob.charStats().getStat(CharStats.STAT_FAITH)>=100)
			msg.append(D.getClericRequirementsDesc()+"\n\r");
		else
		if(mob.charStats().getStat(CharStats.STAT_FAITH)>=25)
		{
			msg.append(D.getClericRequirementsDesc()+"\n\r\n\r");
			msg.append(D.getWorshipRequirementsDesc()+"\n\r");
		}
		else
			msg.append(D.getWorshipRequirementsDesc()+"\n\r");
		final Set<String> items = getWeapons(mob,D);
		if(items.size()>0)
			msg.append(L("\n\r^HWields: ^N@x1\n\r",CMParms.toListString(items)));
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
			if(mob.charStats().getStat(CharStats.STAT_FAITH)>=100)
				msg.append(D.getClericTriggerDesc()+"\n\r");
			else
				msg.append(D.getWorshipTriggerDesc()+"\n\r");
			msg.append("See also help on BLESSINGS\n\r");
		}
		if((mob==null)||(mob.charStats().getStat(CharStats.STAT_FAITH)>=100))
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
				msg.append("See also help on POWERS\n\r");
			}
			msg.append(L("\n\r^HService Instructions: ^N"));
			msg.append(D.getServiceTriggerDesc()+"\n\r");
			msg.append("See also help on SERVICES\n\r");
		}
		return msg.toString();
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE.toString();
		if((args.length==2)
		&&(args[1] instanceof Boolean)
		&&(((Boolean)args[1]).booleanValue()))
			return this.getDeityInformation(mob, (Deity)args[0], true);
		return this.getDeityInformation(mob, (Deity)args[0], false);
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
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
		for(final Enumeration<Deity> d=CMLib.map().deities();d.hasMoreElements();)
		{
			final Deity D=d.nextElement();
			if((str.length()>0)&&(CMLib.english().containsString(D.name(),str)))
				msg.append("\n\r"+this.getDeityInformation(mob, D, false));
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
