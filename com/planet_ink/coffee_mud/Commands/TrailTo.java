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

	public String trailTo(Room R1, List<String> commands)
	{
		int radius=Integer.MAX_VALUE;
		HashSet<Room> ignoreRooms=null;
		final TrackingLibrary.TrackingFlags flags = CMLib.tracking().newFlags();
		int minSize = 0;
		boolean fallback=false;
		List<TrackingLibrary.TrackingFlag> removeOrder = new ArrayList<TrackingLibrary.TrackingFlag>();
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
			{
				for(TrackingLibrary.TrackingFlag flag : TrackingLibrary.TrackingFlag.values())
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
		String where=CMParms.combine(commands,1);
		if(where.length()==0)
			return "Trail to where? Try a Room ID, 'everyroom', or 'everyarea'.  You can also end the areas with 'areanames', 'ignorerooms=', and 'confirm!' flags.  You can also include one of these flags: "+CMParms.toListString(TrackingLibrary.TrackingFlag.values())+", FALLBACK, RADIUS=X, or MINSIZE=X";
		if(R1==null)
			return "Where are you?";
		boolean confirm=false;
		boolean areaNames=false;
		boolean justTheFacts=false;
		if(where.toUpperCase().endsWith(" AREANAMES"))
		{
			where=where.substring(0,where.length()-10).trim();
			areaNames=true;
		}
		if(where.toUpperCase().endsWith(" JUSTTHEFACTS"))
		{
			where=where.substring(0,where.length()-13).trim();
			justTheFacts=true;
		}
		if(where.toUpperCase().endsWith(" CONFIRM!"))
		{
			where=where.substring(0,where.length()-9).trim();
			confirm=true;
		}
		final Vector<Room> set=new Vector<Room>();
		CMLib.tracking().getRadiantRooms(R1,set,flags,null,radius,ignoreRooms);
		if(where.equalsIgnoreCase("everyarea"))
		{
			final StringBuffer str=new StringBuffer("");
			for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
			{
				final Area A=a.nextElement();
				if((!(A instanceof SpaceObject))
				&&(A.properSize() > minSize))
				{
					String trail = CMLib.tracking().getTrailToDescription(R1,set,A.name(),areaNames,confirm,radius,ignoreRooms,5);
					if(fallback && trail.startsWith("Unable to determine"))
					{
						TrackingLibrary.TrackingFlags workFlags = flags.copyOf();
						Vector<TrackingLibrary.TrackingFlag> removeables=new XVector<TrackingLibrary.TrackingFlag>(removeOrder); 
						while(trail.startsWith("Unable to determine") && (removeables.size()>0) && (workFlags.size()>0))
						{
							final Vector<Room> set2=new Vector<Room>(set.size());
							workFlags.minus(removeables.remove(0));
							CMLib.tracking().getRadiantRooms(R1,set2,workFlags,null,radius,ignoreRooms);
							trail = CMLib.tracking().getTrailToDescription(R1,set2,A.name(),areaNames,confirm,radius,ignoreRooms,5);
						}
					}
					str.append(CMStrings.padRightPreserve(A.name(),30)+": "+trail+"\n\r");
				}
			}
			if(confirm)
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
					if((R!=R1)&&(R.roomID().length()>0))
					{
						String trail = CMLib.tracking().getTrailToDescription(R1,set,R.roomID(),areaNames,confirm,radius,ignoreRooms,5);
						if(fallback && trail.startsWith("Unable to determine"))
						{
							TrackingLibrary.TrackingFlags workFlags = flags.copyOf();
							Vector<TrackingLibrary.TrackingFlag> removeables=new XVector<TrackingLibrary.TrackingFlag>(removeOrder); 
							while(trail.startsWith("Unable to determine") && (removeables.size()>0) && (workFlags.size()>0))
							{
								final Vector<Room> set2=new Vector<Room>(set.size());
								workFlags.minus(removeables.remove(0));
								CMLib.tracking().getRadiantRooms(R1,set2,workFlags,null,radius,ignoreRooms);
								trail = CMLib.tracking().getTrailToDescription(R1,set2,R.roomID(),areaNames,confirm,radius,ignoreRooms,5);
							}
						}
						str.append(CMStrings.padRightPreserve(R.roomID(),30)+": "+trail+"\n\r");
					}
				}
			}
			catch (final NoSuchElementException nse)
			{
			}
			if(confirm)
				Log.rawSysOut(str.toString());
			return str.toString();
		}
		else
		{
			String str=CMLib.tracking().getTrailToDescription(R1,set,where,areaNames,confirm,radius,ignoreRooms,5);
			if(!justTheFacts)
			{
				if(fallback && str.startsWith("Unable to determine"))
				{
					TrackingLibrary.TrackingFlags workFlags = flags.copyOf();
					Vector<TrackingLibrary.TrackingFlag> removeables=new XVector<TrackingLibrary.TrackingFlag>(removeOrder); 
					while(str.startsWith("Unable to determine") && (removeables.size()>0) && (workFlags.size()>0))
					{
						final Vector<Room> set2=new Vector<Room>(set.size());
						workFlags.minus(removeables.remove(0));
						CMLib.tracking().getRadiantRooms(R1,set2,workFlags,null,radius,ignoreRooms);
						str = CMLib.tracking().getTrailToDescription(R1,set2,where,areaNames,confirm,radius,ignoreRooms,5);
					}
				}
				str=CMStrings.padRightPreserve(where,30)+": "+str;
			}
			if(confirm)
				Log.rawSysOut(str);
			return str;
		}
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()>0)&&(commands.get(commands.size()-1).equalsIgnoreCase("QUIETLY")))
		{
			commands.remove(commands.size()-1);
			commands.set(0,trailTo(mob.location(),commands));
		}
		else
		if(!mob.isMonster())
			mob.session().safeRawPrintln(trailTo(mob.location(),commands));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.TRAILTO);
	}

}
