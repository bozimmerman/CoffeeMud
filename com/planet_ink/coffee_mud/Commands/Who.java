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
@SuppressWarnings({"unchecked","rawtypes"})
public class Who extends StdCommand
{
	public Who()
	{
	}

	private final String[]	access	= I(new String[] { "WHO", "WH" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class[][]	filterParameters	= new Class[][] { { Boolean.class, Filterer.class } };

	public int[] getShortColWidths(MOB seer)
	{
		return new int[]{
			CMLib.lister().fixColWidth(12,seer.session()),
			CMLib.lister().fixColWidth(12,seer.session()),
			CMLib.lister().fixColWidth(7,seer.session()),
			CMLib.lister().fixColWidth(40,seer.session())
		};
	}

	public String getHead(int[] colWidths)
	{
		final StringBuilder head=new StringBuilder("");
		head.append("^x[");
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
			head.append(CMStrings.padRight(L("Race"),colWidths[0])+" ");
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
			head.append(CMStrings.padRight(L("Class"),colWidths[1])+" ");
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
			head.append(CMStrings.padRight(L("Level"),colWidths[2]));
		head.append("] Character name^.^N\n\r");
		return head.toString();
	}

	public StringBuffer showWhoShort(MOB who, int[] colWidths)
	{
		final StringBuffer msg=new StringBuffer("");
		msg.append("[");
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		{
			if(who.charStats().getCurrentClass().raceless())
				msg.append(CMStrings.padRight(" ",colWidths[0])+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().raceName(),colWidths[0])+" ");
		}
		String levelStr=who.charStats().displayClassLevel(who,true).trim();
		final int x=levelStr.lastIndexOf(' ');
		if(x>=0)
			levelStr=levelStr.substring(x).trim();
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		{
			if(who.charStats().getMyRace().classless())
				msg.append(CMStrings.padRight(" ",colWidths[1])+" ");
			else
				msg.append(CMStrings.padRight(who.charStats().displayClassName(),colWidths[1])+" ");
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
		{
			if(who.charStats().getMyRace().leveless()
			||who.charStats().getCurrentClass().leveless())
				msg.append(CMStrings.padRight(" ",colWidths[2]));
			else
				msg.append(CMStrings.padRight(levelStr,colWidths[2]));
		}
		String name=getWhoName(who);
		msg.append("] "+CMStrings.padRight(name,colWidths[3]));
		msg.append("\n\r");
		return msg;
	}

	public String getWhoName(MOB seenM)
	{
		String name=null;
		if(CMLib.flags().isCloaked(seenM))
			name="("+(seenM.Name().equals(seenM.name())?seenM.titledName():seenM.name())+")";
		else
			name=(seenM.Name().equals(seenM.name())?seenM.titledName():seenM.name());
		if((seenM.session()!=null)&&(seenM.session().isAfk()))
			name=name+(" (idle: "+CMLib.time().date2BestShortEllapsedTime(seenM.session().getIdleMillis())+")");
		return name;
	}

	public String getPlainWhoName(MOB seenM)
	{
		String name=null;
		if(CMLib.flags().isCloaked(seenM))
			name="("+seenM.Name()+")";
		else
			name=seenM.Name();
		if((seenM.session()!=null)&&(seenM.session().isAfk()))
			name=name+(" (idle: "+CMLib.time().date2BestShortEllapsedTime(seenM.session().getIdleMillis())+")");
		return name;
	}

	public boolean checkWho(MOB seerM, MOB seenM, Set<String> friends, Filterer<MOB> mobFilter)
	{
		if((seenM!=null)
		&&(((!CMLib.flags().isCloaked(seenM))
			||((CMSecurity.isAllowedAnywhere(seerM,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(seerM,CMSecurity.SecFlag.WIZINV))&&(seerM.phyStats().level()>=seenM.phyStats().level()))))
		&&((friends==null)||(friends.contains(seenM.Name())||(friends.contains("All"))))
		&&((mobFilter==null)||(mobFilter.passesFilter(seenM)))
		&&(seenM.basePhyStats().level()>0))
			return true;
		return false;
	}

	public String getWho(MOB mob, Set<String> friends, boolean emptyOnNone, Filterer<MOB> mobFilter)
	{
		final StringBuffer msg=new StringBuffer("");
		final int[] colWidths=getShortColWidths(mob);
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			MOB mob2=S.mob();
			if((mob2!=null)&&(mob2.soulMate()!=null))
				mob2=mob2.soulMate();

			if(checkWho(mob,mob2,friends,mobFilter))
				msg.append(showWhoShort(mob2,colWidths));
		}
		if((emptyOnNone)&&(msg.length()==0))
			return "";
		else
		{
			final StringBuffer head=new StringBuffer(getHead(colWidths));
			head.append(msg.toString());
			return head.toString();
		}
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String mobName=CMParms.combine(commands,1);
		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.startsWith("@")))
		{
			if((!(CMLib.intermud().i3online()))
			&&(!CMLib.intermud().imc2online()))
				mob.tell(L("Intermud is unavailable."));
			else
				CMLib.intermud().i3who(mob,mobName.substring(1));
			return false;
		}
		Set<String> friends=null;
		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.equalsIgnoreCase("friends"))
		&&(mob.playerStats()!=null))
		{
			friends=mob.playerStats().getFriends();
			mobName=null;
		}

		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.equalsIgnoreCase("pk")
		||mobName.equalsIgnoreCase("pkill")
		||mobName.equalsIgnoreCase("playerkill")))
		{
			friends=new HashSet<String>();
			for(final Session S : CMLib.sessions().allIterable())
			{
				final MOB mob2=S.mob();
				if((mob2!=null)&&(mob2.isAttributeSet(MOB.Attrib.PLAYERKILL)))
					friends.add(mob2.Name());
			}
		}
		
		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.equalsIgnoreCase("acct")
			||mobName.equalsIgnoreCase("accounts")
			||mobName.equalsIgnoreCase("account"))
		&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDPLAYERS))
		&&(CMProps.isUsingAccountSystem()))
		{
			int[] colWidths = new int[]{
				CMLib.lister().fixColWidth(20,mob.session()),
				CMLib.lister().fixColWidth(40,mob.session())
			};
			final StringBuilder msg=new StringBuilder("");
			msg.append("^x[");
			msg.append(CMStrings.padRight(L("Account"),colWidths[0]));
			msg.append(L("] Character name^.^N\n\r"));
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				MOB mob2=S.mob();
				if((mob2!=null)&&(mob2.soulMate()!=null))
					mob2=mob2.soulMate();

				if(checkWho(mob,mob2,friends,null) && (mob2!=null))
				{
					final PlayerStats pStats2=mob2.playerStats();
					final String accountName = (pStats2 != null) && (pStats2.getAccount() != null) ? pStats2.getAccount().getAccountName() : "?!?";
					msg.append("["+CMStrings.padRight(accountName,colWidths[0]));
					final String name=getPlainWhoName(mob2);
					msg.append("] "+CMStrings.padRight(name,colWidths[1]));
					msg.append("\n\r");
				}
			}
			mob.tell(msg.toString());
			return false;
		}

		final String msg = getWho(mob,friends,mobName!=null,null);
		if((mobName!=null)&&(msg.length()==0))
			mob.tell(L("That person doesn't appear to be online.\n\r"));
		else
			mob.tell(msg);
		return false;
	}

	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(args.length==0)
			return getWho(mob,null,false,null);
		else
		if(super.checkArguments(filterParameters, args))
			return getWho(mob,null,((Boolean)args[0]).booleanValue(),(Filterer)args[1]);
		return Boolean.FALSE;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
