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
   Copyright 2000-2008 Bo Zimmerman

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
public class At extends StdCommand
{
	public At(){}

	private String[] access={"AT"};
	public String[] getAccessWords(){return access;}

	public Room findRoomLiberally(MOB mob, StringBuffer cmd)
	{
		Room room=null;
		Room curRoom=mob.location();
		int dirCode=Directions.getGoodDirectionCode(cmd.toString());
		if(dirCode>=0)
			room=mob.location().rawDoors()[dirCode];
		if(room==null)
			room = CMLib.map().getRoom(cmd.toString());
		if(room==null)
		{
		    // first get room ids
			if((cmd.charAt(0)=='#')&&(curRoom!=null))
			{
				cmd.insert(0,curRoom.getArea().Name());
				room = CMLib.map().getRoom(cmd.toString());
			}
			else
			{
                String srchStr=cmd.toString();
                // then look for players
				for(int s=0;s<CMLib.sessions().size();s++)
				{
					Session thisSession=CMLib.sessions().elementAt(s);
					if((thisSession.mob()!=null) && (!thisSession.killFlag())
					&&(thisSession.mob().location()!=null)
					&&(thisSession.mob().name().equalsIgnoreCase(srchStr)))
					{
						room = thisSession.mob().location();
						break;
					}
				}
				// keep looking for players
				if(room==null)
					for(int s=0;s<CMLib.sessions().size();s++)
					{
						Session thisSession=CMLib.sessions().elementAt(s);
						if((thisSession.mob()!=null)&&(!thisSession.killFlag())
						&&(thisSession.mob().location()!=null)
						&&(CMLib.english().containsString(thisSession.mob().name(),srchStr)))
						{
							room = thisSession.mob().location();
							break;
						}
					}
                if(room==null)
                {
                    // now look for area names
                    for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
                    {
                        Area A=(Area)a.nextElement();
                        if((A.Name().equalsIgnoreCase(srchStr))
                        &&(A.properSize()>0))
                        {
                            int tries=0;
                            while(((room==null)||(room.roomID().length()==0))&&((++tries)<200))
                                room=A.getRandomProperRoom();
                            break;
                        }
                    }
                }
                // keep looking at area names
				if(room==null)
				{
					for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
					{
						Area A=(Area)a.nextElement();
						if((CMLib.english().containsString(A.name(),srchStr))
						&&(A.getProperRoomnumbers().roomCountAllAreas()>0))
						{
							int tries=0;
							while(((room==null)||(room.roomID().length()==0))&&((++tries)<200))
								room=A.getRandomProperRoom();
							break;
						}
					}
				}
				// no good, so look for room inhabitants
				if(room==null)
				{
					Vector candidates=new Vector();
					MOB target=null;
					try
					{
                        for(Enumeration r=CMLib.map().roomsFilled();r.hasMoreElements();)
                        {
                            Room R=(Room)r.nextElement();
                            target=R.fetchInhabitant(srchStr);
                            if(target!=null)
                                candidates.addElement(target);
                        }
				    }
				    catch(NoSuchElementException e){}
					if(candidates.size()>0)
					{
						target=(MOB)candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
						room=target.location();
					}
				}
				// now check room descriptions
				if(room==null)
				{
					String areaName=srchStr.toUpperCase();
					for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(CMLib.english().containsString(CMStrings.removeColors(R.displayText()),areaName))
						{
						   room=R;
						   break;
						}
					}
				}
                // still check room descriptions
				if(room==null)
				{
                    String areaName=srchStr.toUpperCase();
				    try
				    {
						for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							if(CMLib.english().containsString(CMStrings.removeColors(R.description()),areaName))
							{
							   room=R;
							   break;
							}
						}
				    }catch(NoSuchElementException e){}
				}
				// lastly, check floor items
				if(room==null)
				{
					Vector candidates=new Vector();
					Item target=null;
					try
					{
						for(Enumeration r=CMLib.map().roomsFilled();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							target=R.fetchItem(null,srchStr);
							if(target!=null)
								candidates.addElement(target);
						}
				    }catch(NoSuchElementException e){}
					if(candidates.size()>0)
					{
						target=(Item)candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
						if(target.owner() instanceof Room)
							room=(Room)target.owner();
					}
				}
			}
		}
		return room;
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("At where do what?");
			return false;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		Room room=findRoomLiberally(mob,new StringBuffer(cmd));
		if(room==null)
		{
			if(CMSecurity.isAllowedAnywhere(mob,"AT"))
				mob.tell("At where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that.");
			return false;
		}
		if(!CMSecurity.isAllowed(mob,room,"AT"))
		{
			mob.tell("You aren't powerful enough to do that there.");
			return false;
		}
		Room R=mob.location();
		if(R!=room)	room.bringMobHere(mob,false);
		mob.doCommand(commands,metaFlags);
		if(mob.location()!=R) R.bringMobHere(mob,false);
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedAnywhere(mob,"AT");}

	
}
