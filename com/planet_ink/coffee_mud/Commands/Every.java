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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2014-2018 Bo Zimmerman

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

public class Every extends StdCommand
{
	public Every(){}

	private final String[] access=I(new String[]{"EVERY"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}
	
	private enum EveryWhat {
		MOB,
		ITEM,
		ROOM,
		EXIT
	}

	protected List<String> makeNewCommands(List<String> commands, String replace)
	{
		final XVector<String> newCommands=new XVector<String>(commands);
		int x=newCommands.indexOf("*");
		if(x>=0)
			newCommands.set(x,replace);
		return newCommands;
	}
	
	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{	
		final Room room=mob.location();
		if(room == null)
			return false;
		commands=new XVector<String>(commands);
		Enumeration<Room> roomList = new XVector<Room>(room).elements();
		MaskingLibrary.CompiledZMask mask = null;
		boolean doPlayers = false;
		List<String> mskBunch=null;
		if(commands.size()==0)
			return false;
		commands.remove(0);
		if(commands.size()==0)
			return false;
		final String which=commands.get(0).toString().toUpperCase().trim();
		final EveryWhat what = (EveryWhat)CMath.s_valueOf(EveryWhat.class, which);
		if(what==null)
		{
			mob.tell(L("Every what? MOB, ITEM, ROOM, or EXIT please."));
			return false;
		}
		final CMSecurity.SecFlag secFlag;
		switch(what)
		{
		case ROOM: 
			secFlag=CMSecurity.SecFlag.CMDROOMS; 
			break;
		case MOB: 
			secFlag=CMSecurity.SecFlag.CMDMOBS; 
			break;
		case ITEM: 
			secFlag=CMSecurity.SecFlag.CMDITEMS; 
			break;
		case EXIT: 
			secFlag=CMSecurity.SecFlag.CMDROOMS; 
			break;
		default: 
			return false;
		}
		commands.remove(0);
		while(commands.size()>0)
		{
			if(mskBunch != null)
			{
				String s=commands.get(0).toString();
				int x=s.toUpperCase().indexOf("</MASK>");
				if(x<0)
					mskBunch.add(s);
				else
				{
					String rest=s.substring(x+7).trim();
					if(rest.length()>0)
					{
						if(commands.size()==1)
							commands.add(rest);
						else
							commands.add(1,rest);
					}
					s=s.substring(0,x).trim();
					if(s.length()>0)
						mskBunch.add(s);
					s=CMParms.combineQuoted(mskBunch, 0);
					mask=CMLib.masking().maskCompile(s);
					mskBunch = null;
				}
				commands.remove(0);
			}
			else
			if(commands.get(0).toString().equalsIgnoreCase("INAREA"))
			{
				if(CMSecurity.isAllowed(mob, room, secFlag))
				{
					roomList=room.getArea().getMetroMap();
				}
				else
				{
					mob.tell(L("Not permitted."));
					roomList = null;
				}
				commands.remove(0);
			}
			else
			if(commands.get(0).toString().equalsIgnoreCase("INWORLD"))
			{
				if(CMSecurity.isAllowedEverywhere(mob, secFlag) && CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.EVERY))
				{
					roomList=CMLib.map().rooms();
				}
				else
				{
					mob.tell(L("Not permitted."));
					roomList = null;
				}
				commands.remove(0);
			}
			else
			if(commands.get(0).toString().toUpperCase().startsWith("<MASK>"))
			{
				mskBunch=new XVector<String>(commands.get(0).toString().substring(6));
				commands.remove(0);
			}
			else
			if(commands.get(0).toString().equals("+PLAYERS"))
			{
				if(CMSecurity.isAllowed(mob, room, CMSecurity.SecFlag.CMDPLAYERS))
				{
					doPlayers = true;
				}
				else
				{
					mob.tell(L("Not permitted."));
					roomList = null;
				}
				commands.remove(0);
			}
			else
				break;
		}
		
		final Session session=mob.session();
		if(roomList == null)
		{
			mob.tell(L("You failed."));
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell(L("Do what? How about a command?"));
			return false;
		}
		
		final Room oldLocR=mob.location();
		while(roomList.hasMoreElements() && ((session==null)||(!session.isStopped())))
		{
			final Room R=roomList.nextElement();
			if(R!=null)
			{
				if(mob.location()!=R)
					R.bringMobHere(mob,false);
				switch(what)
				{
				case ROOM:
					mob.doCommand(makeNewCommands(commands,"*"),metaFlags);
					break;
				case ITEM:
					for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I!=null)
						&&((mask==null)||(CMLib.masking().maskCheck(mask, I, true))))
							mob.doCommand(makeNewCommands(commands,R.getContextName(I)),metaFlags);
					}
					break;
				case MOB:
					for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if((M!=null)
						&&(M!=mob)
						&&(M.isMonster() || (doPlayers))
						&&((mask==null)||(CMLib.masking().maskCheck(mask, M, true))))
							mob.doCommand(makeNewCommands(commands,R.getContextName(M)),metaFlags);
					}
					break;
				case EXIT:
				{
					final boolean inAShip =(R instanceof BoardableShip)||(R.getArea() instanceof BoardableShip);
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						final Exit E=R.getExitInDir(d);
						if((E!=null)
						&&((mask==null)||(CMLib.masking().maskCheck(mask, E, true))))
						{
							mob.doCommand(makeNewCommands(commands,inAShip ? CMLib.directions().getShipDirectionName(d) : CMLib.directions().getDirectionName(d)),metaFlags);
						}
					}
				}
				}
			}
		}
		if(mob.location()!=oldLocR) 
			oldLocR.bringMobHere(mob,false);
		mob.tell(L("Done."));
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.EVERY);
	}
}
