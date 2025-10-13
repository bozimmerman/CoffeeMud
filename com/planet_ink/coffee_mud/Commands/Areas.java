package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.Stats;
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
public class Areas extends StdCommand
{
	public Areas()
	{
	}

	private final String[] access = I(new String[] { "AREAS" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		String expression=null;
		Enumeration<Area> a=CMLib.map().areas();
		Area.Stats addStat=null;
		boolean addAuthor=false;
		String append="";
		int numCols=3;
		boolean explored=false;

		for(int i=1;i<commands.size();i++)
		{
			final String s=commands.get(i);
			if((s.toUpperCase().equals("ORDER")||s.toUpperCase().equals("SORT"))
			&&((i<commands.size()-2)
			&&(commands.get(i+1).toUpperCase().equals("BY"))))
			{
				commands.set(i,"SORT="+CMParms.combine(commands,i+2));
				while (commands.size() > i + 1)
					commands.remove(i + 1);
				break;
			}
			if((s.toUpperCase().equals("SORT"))
			&&(i<commands.size()-1))
			{
				if((i<commands.size()-2)
				&& (commands.get(i + 1).toUpperCase().equals("=")))
					commands.remove(i+1);
				if(commands.get(i+1).startsWith("="))
					commands.set(i,"SORT"+CMParms.combine(commands,i+1));
				else
					commands.set(i,"SORT="+CMParms.combine(commands,i+1));
				while (commands.size() > i + 1)
					commands.remove(i + 1);
				break;
			}
		}
		for(int i=1;i<commands.size();i++)
		{
			String s=commands.get(i);
			if(s.toUpperCase().startsWith("SORT=NA"))
			{
				append = " (sorted by name)";
				commands.remove(i);
				i--;
			}
			else
			if(s.toUpperCase().startsWith("EXPLORED"))
			{
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.ROOMVISITS))
				&&(mob!=null)
				&&(mob.playerStats()!=null))
				{
					append = " (with explored %)";
					commands.remove(i);
					i--;
					explored=true;
					numCols=2;
				}
			}
			else
			if(s.toUpperCase().startsWith("SORT=REV"))
			{
				final List<Area> levelSorted=new ArrayList<Area>();
				for (; a.hasMoreElements();)
					levelSorted.add(a.nextElement());
				Collections.sort(levelSorted, new Comparator<Area>()
				{
					@Override
					public int compare(final Area arg0, final Area arg1)
					{
						return arg1.Name().compareTo(arg0.Name());
					}
				});
				a=new IteratorEnumeration<Area>(levelSorted.iterator());
				append = " (sorted by name, reverse)";
				commands.remove(i);
				i--;
			}
			else
			if(s.toUpperCase().startsWith("SORT=LEV"))
			{
				final List<Area> levelSorted=new ArrayList<Area>();
				for(;a.hasMoreElements();) levelSorted.add(a.nextElement());
				Collections.sort(levelSorted, new Comparator<Area>()
				{
					@Override
					public int compare(final Area arg0, final Area arg1)
					{
						final int lvl1=arg0.getIStat(Stats.MED_LEVEL);
						final int lvl2=arg1.getIStat(Stats.MED_LEVEL);
						if(lvl1==lvl2)
							return 1;
						return Integer.valueOf(lvl1).compareTo(Integer.valueOf(lvl2));
					}
				});
				a=new IteratorEnumeration<Area>(levelSorted.iterator());
				append = " (sorted by level)";
				addStat=Stats.MED_LEVEL;
				commands.remove(i);
				i--;
			}
			else
			if(s.toUpperCase().startsWith("SORT=AUTH"))
			{
				final List<Area> levelSorted=new ArrayList<Area>();
				for(;a.hasMoreElements();) levelSorted.add(a.nextElement());
				Collections.sort(levelSorted, new Comparator<Area>()
				{
					@Override
					public int compare(final Area arg0, final Area arg1)
					{
						return arg0.getAuthorID().compareTo(arg1.getAuthorID());
					}
				});
				a=new IteratorEnumeration<Area>(levelSorted.iterator());
				append = " (sorted by author)";
				commands.remove(i);
				addAuthor=true;
				numCols=2;
				i--;
			}
			else
			if(s.toUpperCase().startsWith("SORT="))
			{
				Area.Stats statVal=null;
				s=s.substring(5).trim();
				final String us = (s.startsWith("$")?s.substring(1):s).toUpperCase();
				for(int x=0;x<Area.Stats.values().length;x++)
				{
					if(us.equals(Area.Stats.values()[x].name()))
						statVal=Area.Stats.values()[x];
				}
				if(statVal==null)
				{
					mob.tell(L("There was an error in your SORT= qualifier: '@x1' is unknown.",s));
					return false;
				}
				final Area.Stats sortStat=statVal;
				final List<Area> levelSorted=new ArrayList<Area>();
				for (; a.hasMoreElements();)
					levelSorted.add(a.nextElement());
				Collections.sort(levelSorted, new Comparator<Area>()
				{
					@Override
					public int compare(final Area arg0, final Area arg1)
					{
						final int lvl1=arg0.getIStat(sortStat);
						final int lvl2=arg1.getIStat(sortStat);
						if(lvl1==lvl2)
							return 0;
						return Integer.valueOf(lvl1).compareTo(Integer.valueOf(lvl2));
					}
				});
				a=new IteratorEnumeration<Area>(levelSorted.iterator());
				append = " (sorted by "+statVal.name().toLowerCase()+")";
				addStat=sortStat;
				commands.remove(i);
				i--;
			}
		}

		StringBuffer msg=new StringBuffer(L("^HComplete areas list@x1:^?^N\n\r",append));
		if(commands.size()>1)
		{
			expression=CMParms.combineQuoted(commands,1);
			msg=new StringBuffer(L("^HFiltered areas list@x1:^?^N\n\r",append));
		}
		final List<String> areasVec=new ArrayList<String>();
		final boolean sysop=(mob!=null)&&CMSecurity.isASysOp(mob);
		final int colWidth=CMLib.lister().fixColWidth(66.0/numCols,mob);
		for(;a.hasMoreElements();)
		{
			final Area A=a.nextElement();
			if(CMLib.flags().canAccess(mob,A)
			&&(!CMath.bset(A.flags(),Area.FLAG_INSTANCE_CHILD))
			&&(!(A instanceof SpaceObject)))
			{
				String levelStr = (addStat!=null?(Integer.toString(A.getIStat(addStat))+":"):"");
				if(addAuthor)
					levelStr=CMStrings.padRight(A.getAuthorID(),10)+":";
				final String areaName = A.name().replace('`', '\'');
				String name=levelStr+((!CMLib.flags().isHidden(A))?" "+areaName:"("+areaName+")");
				if(sysop)
				{
					switch(A.getAreaState())
					{
					case ACTIVE:
						name = "^w" + name + "^?";
						break;
					case PASSIVE:
						name = "^W" + name + "^?";
						break;
					case FROZEN:
						name = "^b" + name + "^?";
						break;
					case STOPPED:
						name = "^r" + name + "^?";
						break;
					}
				}
				if(expression!=null)
				{
					final Map<String,Object> H=new Hashtable<String,Object>();
					for(final Area.Stats stat : Area.Stats.values())
						H.put(stat.name(),Integer.toString(A.getIStat(stat)));
					H.put("AUTHOR", A.getAuthorID());
					try
					{
						if(!CMStrings.parseStringExpression(expression, H,false))
							continue;
					}
					catch(final Exception e)
					{
						if(mob!=null)
						{
							mob.tell(L("There was an error in your AREA qualifier parameters. See help on AREA for more information. "
									+ "The error was: @x1", (e.getMessage()!=null)?e.getMessage():
										L("bad syntax (did you forget quotes?)")));
						}
						return false;
					}
				}
				if(explored && (mob!=null) && (mob.playerStats()!=null))
					name+=" ("+mob.playerStats().percentVisited(mob, A)+"%)";
				areasVec.add(name);
			}
		}
		int col=0;
		if(areasVec.size()==0)
			msg.append(L("There appear to be no other areas on this world.\n\r"));
		else
		for(int i=0;i<areasVec.size();i++)
		{
			if((++col)>numCols)
			{
				msg.append("\n\r");
				col=1;
			}
			msg.append(CMStrings.padRight(areasVec.get(i),colWidth)+"^N");
		}
		if(explored && (mob!=null))
		{
			msg.append(L("\n\rYou have explored @x1% of the world.",
					""+mob.playerStats().percentVisited(mob,null)));
		}
		msg.append(L("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?"));
		if((mob!=null)&&(!mob.isMonster()))
			mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
