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
   Copyright 2004-2020 Bo Zimmerman

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

	@SuppressWarnings("rawtypes")
	private final static Class[][]	filterParameters	= new Class[][]
	{
		{ Boolean.class, Filterer.class},
		{ Boolean.class, Filterer.class, String.class }
	};

	public int[] getShortColWidths(final MOB seer)
	{
		return new int[]{
			CMLib.lister().fixColWidth(12,seer.session()),
			CMLib.lister().fixColWidth(12,seer.session()),
			CMLib.lister().fixColWidth(7,seer.session()),
			CMLib.lister().fixColWidth(40,seer.session())
		};
	}

	public String getHead(final int[] colWidths)
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

	public String getTail(final int[] colWidths, final String word, final int amt)
	{
		int width=colWidths[0];
		for(int i=1;i<=2;i++)
			width+=colWidths[i]+1;
		final StringBuilder tail=new StringBuilder("");
		tail.append(L("^x["+CMStrings.padRight(L("Total "+word+" online"),width)+"]^.^N @x1\n\r",""+amt));
		return tail.toString();
	}

	public StringBuffer showWhoShort(final MOB who, final MOB viewerM, final int[] colWidths)
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
		final String name=getWhoName(who, viewerM);
		msg.append("] "+CMStrings.padRight(name,colWidths[3]));
		msg.append("\n\r");
		return msg;
	}

	public String getWhoName(final MOB seenM, final MOB viewerM)
	{
		String name;
		if(CMLib.flags().isCloaked(seenM))
			name="("+(seenM.Name().equals(seenM.name())?seenM.titledName():seenM.name())+")^N";
		else
			name=((seenM.Name().equals(seenM.name())?seenM.titledName():seenM.name()))+"^N";
		if((seenM.session()!=null)&&(seenM.session().isAfk()))
			name=name+(" (idle: "+CMLib.time().date2BestShortEllapsedTime(seenM.session().getIdleMillis())+")");
		return name;
	}

	public String getPlainWhoName(final MOB seenM)
	{
		String name;
		if(CMLib.flags().isCloaked(seenM))
			name="("+seenM.Name()+")^N";
		else
			name=seenM.Name()+"^N";
		if((seenM.session()!=null)&&(seenM.session().isAfk()))
			name+=(" (idle: "+CMLib.time().date2BestShortEllapsedTime(seenM.session().getIdleMillis())+")");
		return name;
	}

	public boolean checkWho(final MOB seerM, final MOB seenM, final Filterer<MOB> mobFilter)
	{
		if((seenM!=null)
		&&(((!CMLib.flags().isCloaked(seenM))
			||((CMSecurity.isAllowedAnywhere(seerM,CMSecurity.SecFlag.CLOAK)||CMSecurity.isAllowedAnywhere(seerM,CMSecurity.SecFlag.WIZINV))&&(seerM.phyStats().level()>=seenM.phyStats().level()))))
		&&((mobFilter==null)||(mobFilter.passesFilter(seenM)))
		&&(seenM.basePhyStats().level()>0))
			return true;
		return false;
	}

	public String getWho(final MOB mob, final boolean emptyOnNone, final Filterer<MOB> mobFilter, final Comparator<MOB> mobSort, final String tailStr)
	{
		final StringBuffer msg=new StringBuffer("");
		final int[] colWidths=getShortColWidths(mob);
		final List<MOB> mobs=new ArrayList<MOB>(CMLib.sessions().numLocalOnline());
		for(final Session S : CMLib.sessions().localOnlineIterable())
		{
			MOB mob2=S.mob();
			if((mob2!=null)&&(mob2.soulMate()!=null))
				mob2=mob2.soulMate();

			if(checkWho(mob,mob2,mobFilter))
				mobs.add(mob2);
		}
		if(mobSort != null)
			Collections.sort(mobs, mobSort);
		final int count=mobs.size();
		for(final MOB mob2 : mobs)
			msg.append(showWhoShort(mob2,mob,colWidths));
		mobs.clear();
		if((emptyOnNone)&&(msg.length()==0))
			return "";
		else
		{
			final StringBuffer head=new StringBuffer(getHead(colWidths));
			head.append(msg.toString());
			if(tailStr != null)
				head.append(getTail(colWidths, tailStr, count));
			return head.toString();
		}
	}

	protected static Integer nullObjectCompare(final Object o1, final Object o2)
	{
		if(o1 == null)
		{
			if(o2 == null)
				return Integer.valueOf(0);
			return Integer.valueOf(-1);
		}
		else
		if(o2 == null)
			return Integer.valueOf(1);
		return null;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
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

		Filterer<MOB> mobFilter = null;
		final Comparator<MOB> mobSort = new Comparator<MOB>()
		{
			@Override
			public int compare(final MOB o1, final MOB o2)
			{
				final Integer check=nullObjectCompare(o1,o2);
				if(check != null)
					return check.intValue();
				if(CMSecurity.isASysOp(o1))
				{
					if(CMSecurity.isASysOp(o2))
						return 0;
					return -1;
				}
				else
				if(CMSecurity.isASysOp(o2))
					return 1;
				final int lastPlayerLevel = CMProps.getIntVar(CMProps.Int.LASTPLAYERLEVEL);
				if(o1.phyStats().level()>=lastPlayerLevel)
				{
					if(o2.phyStats().level()>=lastPlayerLevel)
						return 0;
					return -1;
				}
				else
				if(o2.phyStats().level()>= lastPlayerLevel)
					return 1;
				return 0;
			}
		};

		@SuppressWarnings("unused")
		String summaryName = "Characters";
		if((mobName != null) && (mob != null))
		{
			if((mobName.equalsIgnoreCase("friends"))
			&&(mob.playerStats()!=null))
			{
				mobFilter = new Filterer<MOB>()
				{
					final Set<String> friends = mob.playerStats().getFriends();
					@Override
					public boolean passesFilter(final MOB obj)
					{
						return (obj != null) && (this.friends.contains(obj.Name()));
					}
				};
				mobName=null;
				summaryName="Friends";
			}
			else
			if((mobName.equalsIgnoreCase("pk")
			||mobName.equalsIgnoreCase("pkill")
			||mobName.equalsIgnoreCase("playerkill")))
			{
				final Set<String> pkErs = new TreeSet<String>();
				for(final Session S : CMLib.sessions().allIterable())
				{
					final MOB mob2=S.mob();
					if((mob2!=null)&&(mob2.isAttributeSet(MOB.Attrib.PLAYERKILL)))
						pkErs.add(mob2.Name());
				}
				mobFilter = new Filterer<MOB>()
				{
					final Set<String> pkNames = pkErs;
					@Override
					public boolean passesFilter(final MOB obj)
					{
						return (obj != null) && (this.pkNames.contains(obj.Name()));
					}
				};
				mobName = null;
				summaryName="Targets";
			}
			else
			if((mobName.equalsIgnoreCase("acct")
				||mobName.equalsIgnoreCase("accounts")
				||mobName.equalsIgnoreCase("account"))
			&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDPLAYERS))
			&&(CMProps.isUsingAccountSystem()))
			{
				final int[] colWidths = new int[]{
					CMLib.lister().fixColWidth(20,mob.session()),
					CMLib.lister().fixColWidth(40,mob.session())
				};
				final StringBuilder msg=new StringBuilder("");
				msg.append("^x[");
				msg.append(CMStrings.padRight(L("Account"),colWidths[0]));
				msg.append(L("] Character name^.^N\n\r"));
				final List<MOB> mobs=new ArrayList<MOB>(CMLib.sessions().numLocalOnline());
				for(final Session S : CMLib.sessions().localOnlineIterable())
				{
					MOB mob2=S.mob();
					if((mob2!=null)&&(mob2.soulMate()!=null))
						mob2=mob2.soulMate();

					if(checkWho(mob,mob2,null) && (mob2!=null))
						mobs.add(mob2);
				}
				Collections.sort(mobs, new Comparator<MOB>()
				{
					@Override
					public int compare(final MOB o1, final MOB o2)
					{
						Integer check = nullObjectCompare(o1,o2);
						if(check != null)
							return check.intValue();
						final PlayerStats p1 = o1.playerStats();
						final PlayerStats p2 = o2.playerStats();
						check = nullObjectCompare(p1, p2);
						if(check != null)
							return check.intValue();
						final PlayerAccount a1 = p1.getAccount();
						final PlayerAccount a2 = p2.getAccount();
						check = nullObjectCompare(a1, a2);
						if(check != null)
							return check.intValue();
						return a1.getAccountName().compareTo(a2.getAccountName());
					}

				});
				for(final MOB mob2 : mobs)
				{
					final PlayerStats pStats2=mob2.playerStats();
					final String accountName = (pStats2 != null) && (pStats2.getAccount() != null) ? pStats2.getAccount().getAccountName() : "?!?";
					msg.append("["+CMStrings.padRight(accountName,colWidths[0]));
					final String name=getPlainWhoName(mob2);
					msg.append("] "+CMStrings.padRight(name,colWidths[1]));
					msg.append("\n\r");
				}
				//msg.append(L("^x["+CMStrings.padRight("Total Characters",colWidths[0])+"]^.^N @x1\n\r",""+mobs.size()));
				mob.tell(msg.toString());
				return false;
			}
		}

		final String msg = getWho(mob,mobName!=null,mobFilter,mobSort, null);
		if((mobName!=null)&&(msg.length()==0))
			mob.tell(L("That person doesn't appear to be online.\n\r"));
		else
			mob.tell(msg);
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(args.length==0)
			return getWho(mob,false,null,null, null);
		else
		if(super.checkArguments(filterParameters, args))
		{
			if(args.length<=2)
				return getWho(mob,((Boolean)args[0]).booleanValue(),(Filterer<MOB>)args[1],null,null);
			else
			if(args[2] instanceof String)
				return getWho(mob,((Boolean)args[0]).booleanValue(),(Filterer<MOB>)args[1],null,(String)args[2]);
			else
				return getWho(mob,((Boolean)args[0]).booleanValue(),(Filterer<MOB>)args[1],null,null);
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
