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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary.TrailFlag;
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
public class TrailTo extends StdCommand
{
	public TrailTo()
	{
	}

	private final String[]	access	= I(new String[] { "TRAILTO" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public String trailTo(final Room R1, final List<String> commands, final Session sess)
	{
		int radius=Integer.MAX_VALUE;
		HashSet<Room> ignoreRooms=null;
		final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags();
		int minSize = 0;
		boolean fallback=false;
		final List<TrackingLibrary.TrackingFlag> removeOrder = new ArrayList<TrackingLibrary.TrackingFlag>();
		final Set<TrailFlag> trailFlags = new HashSet<TrailFlag>();
		boolean justTheFacts=false;
		for(int c=commands.size()-1;c>=1;c--)
		{
			String s=commands.get(c).toUpperCase();
			if(s.startsWith("RADIUS"))
			{
				s=s.substring(("RADIUS").length()).trim();
				if(!s.startsWith("="))
					continue;
				s=s.substring(1);
				commands.remove(c);
				radius=CMath.s_int(s);
			}
			else
			if(s.startsWith("FALLBACK"))
			{
				fallback=true;
				commands.remove(c);
			}
			else
			if(s.startsWith("IGNOREROOMS"))
			{
				s=s.substring(("IGNOREROOMS").length()).trim();
				if(!s.startsWith("="))
					continue;
				s=s.substring(1);
				commands.remove(c);
				final List<String> roomList=CMParms.parseCommas(s,true);
				ignoreRooms=new HashSet<Room>();
				for(int v=0;v<roomList.size();v++)
				{
					final Room R=CMLib.map().getRoom(roomList.get(v));
					if(R==null)
					{
						return "Ignored room "+roomList.get(v)+" is unknown!";
					}
					if(!ignoreRooms.contains(R))
						ignoreRooms.add(R);
				}
			}
			else
			if(s.startsWith("MINSIZE="))
			{
				minSize=CMath.s_int(s.substring(8));
				commands.remove(c);
				flags.plus(TrackingLibrary.TrackingFlag.NOHOMES);
			}
			else
			if(s.equals("JUSTTHEFACTS"))
			{
				commands.remove(c);
				justTheFacts=true;
			}
			else
			{
				final TrailFlag tflag = (TrailFlag)CMath.s_valueOf(TrailFlag.class, s);
				if(tflag != null)
				{
					trailFlags.add(tflag);
					commands.remove(c);
				}
				else
				for(final TrackingLibrary.TrackingFlag flag : TrackingLibrary.TrackingFlag.values())
				{
					if(s.equals(flag.toString()))
					{
						commands.remove(c);
						removeOrder.add(flag);
						flags.plus(flag);
						break;
					}
				}
			}
		}
		final String where=CMParms.combine(commands,1);
		if(where.length()==0)
			return "Trail to where? Try a Room ID, 'everyroom', or 'everyarea'.  You can also end the areas with 'areanames', 'ignorerooms=', and 'confirm!' flags.  You can also include one of these flags: "+CMParms.toListString(TrackingLibrary.TrackingFlag.values())+", FALLBACK, RADIUS=X, or MINSIZE=X";
		if(R1==null)
			return "Where are you?";
		final ArrayList<Room> set=new ArrayList<Room>();
		CMLib.tracking().getRadiantRooms(R1,set,flags,null,radius,ignoreRooms);
		if(where.equalsIgnoreCase("everyarea"))
		{
			final StringBuffer str=new StringBuffer("");
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if((!(A instanceof SpaceObject))
				&&((sess==null)||(!sess.isStopped()))
				&&(A.properSize() > minSize))
				{
					String trail = CMLib.tracking().getTrailToDescription(R1,set,A.name(),trailFlags,radius,ignoreRooms," ",300);
					if(fallback && (trail.startsWith("Unable to determine")||trail.startsWith("You can't")))
					{
						final TrackingLibrary.TrackingFlags workFlags = flags.copyOf();
						final ArrayList<TrackingLibrary.TrackingFlag> removables=new XArrayList<TrackingLibrary.TrackingFlag>(removeOrder);
						while((trail.startsWith("Unable to determine") ||trail.startsWith("You can't"))
						&& (removables.size()>0) && (workFlags.size()>0))
						{
							final ArrayList<Room> set2=new ArrayList<Room>(set.size());
							workFlags.minus(removables.remove(0));
							CMLib.tracking().getRadiantRooms(R1,set2,workFlags,null,radius,ignoreRooms);
							trail = CMLib.tracking().getTrailToDescription(R1,set2,A.name(),trailFlags,radius,ignoreRooms," ",300);
						}
						if(trail.startsWith("Unable to determine") ||trail.startsWith("You can't"))
							continue;
					}
					str.append(CMStrings.padRightPreserve(A.name(),30)+": "+trail+"\n\r");
				}
			}
			if(trailFlags.contains(TrailFlag.CONFIRM))
				Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		if(where.equalsIgnoreCase("everyroom"))
		{
			final StringBuffer str=new StringBuffer("");
			try
			{
				for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=R1)
					&&(R.roomID().length()>0)
					&&((sess==null)||(!sess.isStopped())))
					{
						String trail = CMLib.tracking().getTrailToDescription(R1,set,R.roomID(),trailFlags,radius,ignoreRooms," ",5);
						if(fallback && trail.startsWith("Unable to determine"))
						{
							final TrackingLibrary.TrackingFlags workFlags = flags.copyOf();
							final ArrayList<TrackingLibrary.TrackingFlag> removables=new XArrayList<TrackingLibrary.TrackingFlag>(removeOrder);
							while(trail.startsWith("Unable to determine") && (removables.size()>0) && (workFlags.size()>0))
							{
								final ArrayList<Room> set2=new ArrayList<Room>(set.size());
								workFlags.minus(removables.remove(0));
								CMLib.tracking().getRadiantRooms(R1,set2,workFlags,null,radius,ignoreRooms);
								trail = CMLib.tracking().getTrailToDescription(R1,set2,R.roomID(),trailFlags,radius,ignoreRooms," ",5);
							}
						}
						str.append(CMStrings.padRightPreserve(R.roomID(),30)+": "+trail+"\n\r");
					}
				}
			}
			catch (final NoSuchElementException nse)
			{
			}
			if(trailFlags.contains(TrailFlag.CONFIRM))
				Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		{
			String str=CMLib.tracking().getTrailToDescription(R1,set,where,trailFlags,radius,ignoreRooms," ",5);
			if(!justTheFacts)
			{
				if(fallback && str.startsWith("Unable to determine"))
				{
					final TrackingLibrary.TrackingFlags workFlags = flags.copyOf();
					final ArrayList<TrackingLibrary.TrackingFlag> removables=new XArrayList<TrackingLibrary.TrackingFlag>(removeOrder);
					while(str.startsWith("Unable to determine") && (removables.size()>0) && (workFlags.size()>0))
					{
						final ArrayList<Room> set2=new ArrayList<Room>(set.size());
						workFlags.minus(removables.remove(0));
						CMLib.tracking().getRadiantRooms(R1,set2,workFlags,null,radius,ignoreRooms);
						str = CMLib.tracking().getTrailToDescription(R1,set2,where,trailFlags,radius,ignoreRooms," ",5);
					}
				}

				str=CMStrings.padRightPreserve(where,30)+": "+str;
			}
			if(trailFlags.contains(TrailFlag.CONFIRM))
				Log.rawSysOut(str);
			return str;
		}
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>0)&&(commands.get(commands.size()-1).equalsIgnoreCase("QUIETLY")))
		{
			commands.remove(commands.size()-1);
			commands.set(0,trailTo(mob.location(),commands,mob.session()));
		}
		else
		if(!mob.isMonster())
			mob.session().safeRawPrintln(trailTo(mob.location(),commands,mob.session()));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.TRAILTO);
	}

}
