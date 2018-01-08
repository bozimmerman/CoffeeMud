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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.IOException;
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
public class Save extends StdCommand
{
	public Save(){}

	private final String[] access=I(new String[]{"SAVE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public enum SaveTask
	{
		ALL,
		ITEMS,
		MOBS
	}
	
	private int numSavableInhabitants(Room room)
	{
		int ct=0;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)&&(M.isSavable()))
				ct++;
		}
		return ct;
	}
	
	private int numSavableItems(Room room)
	{
		int ct=0;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.getItem(i);
			if((I!=null)&&(I.isSavable()))
				ct++;
		}
		return ct;
	}
	
	public boolean clearSaveAndRestart(final MOB mob, Room room, SaveTask taskCode, boolean noPrompt) throws IOException
	{
		synchronized(("SYNC"+room.roomID()).intern())
		{
			room=CMLib.map().getRoom(room);
			CMLib.threads().clearDebri(room,0);
			if((!noPrompt)&&(mob!=null)&&(mob.session()!=null))
			{
				int[] counts = CMLib.database().DBCountRoomMobsItems(room.roomID());
				int mobCountDiff = 0;
				if((taskCode == SaveTask.ALL) || (taskCode == SaveTask.MOBS))
					mobCountDiff= counts[0] - this.numSavableInhabitants(room);
				int itemCountDiff = 0;
				if((taskCode == SaveTask.ALL) || (taskCode == SaveTask.ITEMS))
					itemCountDiff= counts[1] - this.numSavableItems(room);
				StringBuilder msg = new StringBuilder();
				if(mobCountDiff < 0)
					msg.append(L("add @x1 mob(s) ",""+(-mobCountDiff)));
				else
				if(mobCountDiff > 0)
					msg.append(L("delete @x1 mob(s) ",""+(mobCountDiff)));
				
				if((itemCountDiff != 0) && (mobCountDiff != 0))
					msg.append(L("and "));
				
				if(itemCountDiff < 0)
					msg.append(L("add @x1 item(s) ",""+(-itemCountDiff)));
				else
				if(itemCountDiff > 0)
					msg.append(L("delete @x1 item(s) ",""+(itemCountDiff)));
				
				if(msg.length() > 0)
				{
					if((!mob.session().confirm(L("Saving @x1 will @x2. Are you sure (Y/n)?",
						CMLib.map().getExtendedRoomID(room),msg.toString()), "Y"))
					||(mob.session().isStopped()))
					{
						return false;
					}
				}
			}
			
			if((taskCode == SaveTask.ALL) || (taskCode == SaveTask.ITEMS))
			{
				CMLib.database().DBUpdateItems(room);
				room.startItemRejuv();
			}
			if((taskCode == SaveTask.ALL) || (taskCode == SaveTask.MOBS))
				CMLib.database().DBUpdateMOBs(room);
			return true;
		}
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		if((commands.size()==1)&&CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPLAYERS))
		{
			if(!mob.isMonster())
			{
				CMLib.database().DBUpdatePlayer(mob);
				CMLib.database().DBUpdateFollowers(mob);
				mob.tell(L("Your player record has been updated."));
			}
			return false;
		}

		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
		String firstCommand="";
		String lastCommand = "";
		if(commands.size()>1)
		{
			firstCommand=commands.get(1).toUpperCase();
			lastCommand=commands.get(commands.size()-1).toUpperCase();
		}

		if(lastCommand.equals("USERS")||lastCommand.equals("PLAYERS")||lastCommand.equals("CHARACTERS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS))
			{
				mob.tell(L("You are not allowed to save players."));
				return false;
			}
			for(final Session S : CMLib.sessions().allIterable())
			{
				final MOB M=S.mob();
				if(M!=null)
				{
					CMLib.database().DBUpdatePlayer(M);
					CMLib.database().DBUpdateFollowers(M);
				}
			}
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes everyone.\n\r"));
		}
		else
		if(lastCommand.equals("ITEMS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS))
			{
				mob.tell(L("You are not allowed to save the mobs here."));
				return false;
			}
			if(firstCommand.equals("AREA"))
			{
				if((mob.session()!=null)&&(mob.session().confirm(L("Doing this assumes every item in every room in this area is correctly placed.  Are you sure (N/y)?"),"N")))
				{
					final Area A=mob.location().getArea();
					boolean saved = false;
					for(final Enumeration<Room> e=A.getProperMap();e.hasMoreElements();)
						saved = clearSaveAndRestart(mob,e.nextElement(),SaveTask.ITEMS, true) || saved;
					if(saved)
						mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes the area.\n\r"));
				}
				else
					return false;
			}
			else
			{
				if(clearSaveAndRestart(mob,mob.location(),SaveTask.ITEMS, false))
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes the room.\n\r"));
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(lastCommand.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS))
			{
				mob.tell(L("You are not allowed to save the contents here."));
				return false;
			}
			if(firstCommand.equals("AREA"))
			{
				if((mob.session()!=null)
				&&(mob.session().confirm(L("Doing this assumes every mob and item in every room in this area is correctly placed.  Are you sure (N/y)?"),"N")))
				{
					final Area A=mob.location().getArea();
					boolean saved = false;
					for(final Enumeration e=A.getProperMap();e.hasMoreElements();)
						saved = clearSaveAndRestart(mob,(Room)e.nextElement(),SaveTask.ALL, true) || saved;
					if(saved)
						mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes the area.\n\r"));
				}
				else
					return false;
			}
			else
			{
				if(clearSaveAndRestart(mob,mob.location(),SaveTask.ALL, false))
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes the room.\n\r"));
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(lastCommand.equals("MOBS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS))
			{
				mob.tell(L("You are not allowed to save the mobs here."));
				return false;
			}
			if(firstCommand.equals("AREA"))
			{
				if((mob.session()!=null)&&(mob.session().confirm(L("Doing this assumes every mob in every room in this area is correctly placed.  Are you sure (N/y)?"),"N")))
				{
					final Area A=mob.location().getArea();
					boolean saved = false;
					for(final Enumeration e=A.getProperMap();e.hasMoreElements();)
						saved = clearSaveAndRestart(mob,(Room)e.nextElement(),SaveTask.MOBS, true) || saved;
					if(saved)
						mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes the area.\n\r"));
				}
				else
					return false;

			}
			else
			{
				if(clearSaveAndRestart(mob,mob.location(),SaveTask.MOBS, false))
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes the room.\n\r"));
			}
			Resources.removeResource("HELP_"+mob.location().getArea().Name().toUpperCase());
		}
		else
		if(firstCommand.equals("QUESTS")||lastCommand.equals("QUESTS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS))
			{
				mob.tell(L("You are not allowed to save the contents here."));
				return false;
			}
			CMLib.quests().save();
			mob.tell(L("Quest list saved."));
		}
		else
		if(firstCommand.equals("FACTION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS))
			{
				mob.tell(L("You are not allowed to save factions."));
				return false;
			}
			final String factionID = (commands.size()>2) ? CMParms.combine(commands,2) : ""; 
			final Faction F=CMLib.factions().getFaction(factionID);
			if(F==null)
			{
				mob.tell(L("No such faction '@x1'.",factionID));
				return false;
			}
			else
			{
				F.setInternalFlags(CMath.unsetb(F.getInternalFlags(), Faction.IFLAG_NEVERSAVE));
				CMLib.factions().resaveFaction(F);
				mob.tell(L("Faction @x1 saved.",F.factionID()));
			}
		}
		else
		if(firstCommand.equals("USER")||firstCommand.equals("PLAYER")||firstCommand.equals("CHARACTER")||firstCommand.equals("CHAR"))
		{
			final MOB M=CMLib.players().getPlayer(lastCommand);
			if(M==null)
			{
				mob.tell(L("No user named @x1",lastCommand));
				return false;
			}
			CMLib.database().DBUpdatePlayer(M);
			if(CMLib.flags().isInTheGame(M,true))
				CMLib.database().DBUpdateFollowers(M);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes '@x1'.\n\r",M.name()));
		}
		else
		if(CMLib.players().getPlayer(firstCommand)!=null)
		{
			final MOB M=CMLib.players().getPlayer(firstCommand);
			CMLib.database().DBUpdatePlayer(M);
			if(CMLib.flags().isInTheGame(M,true))
				CMLib.database().DBUpdateFollowers(M);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes '@x1'.\n\r",M.name()));
		}
		else
		if(CMLib.players().getPlayer(lastCommand)!=null)
		{
			final MOB M=CMLib.players().getPlayer(lastCommand);
			CMLib.database().DBUpdatePlayer(M);
			if(CMLib.flags().isInTheGame(M,true))
				CMLib.database().DBUpdateFollowers(M);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A feeling of permanency envelopes '@x1'.\n\r",M.name()));
		}
		else
		{
			mob.tell(
				L("\n\rYou cannot save '@x1'. However, you might try ITEMS, USERS, [PLAYERNAME], FACTION, QUESTS, MOBS, or ROOM.",firstCommand));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS)
												 ||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)
												 ||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS)
												 ||CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS)
												 ||CMSecurity.isSaveFlag(CMSecurity.SaveFlag.NOPLAYERS);}

}
