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

public class Transfer extends At
{
	public Transfer()
	{
	}

	private final String[]	access	= I(new String[] { "TRANSFER" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Room room=null;
		if(commands.size()<3)
		{
			mob.tell(L("Transfer whom where? Try all or a mob name or item followed by item name, followerd by a Room ID, target player name, inventory, area name, or room text!"));
			return false;
		}
		commands.remove(0);
		String searchName=commands.get(0);
		final Room curRoom=mob.location();
		final Vector<Physical> V=new Vector<Physical>();
		boolean allFlag=false;
		if(searchName.equalsIgnoreCase("ALL"))
		{
			allFlag=true;
			if(commands.size()>2)
			{
				commands.remove(0);
				searchName=commands.get(0);
			}
			else
				searchName="";
		}
		boolean itemFlag=false;
		if((searchName.equalsIgnoreCase("item")||(searchName.equalsIgnoreCase("items"))))
		{
			itemFlag=true;
			if(commands.size()>2)
			{
				commands.remove(0);
				searchName=commands.get(0);
			}
			else
				searchName="";
		}
		if((searchName.length()==0)&&(allFlag))
		{
			if(itemFlag)
			{
				for(int i=0;i<curRoom.numItems();i++)
					V.add(curRoom.getItem(i));
			}
			else
			for(int i=0;i<curRoom.numInhabitants();i++)
			{
				final MOB M=curRoom.fetchInhabitant(i);
				if(M!=null)
					V.add(M);
			}
		}
		else
		if(itemFlag)
		{
			if(!allFlag)
			{
				final Environmental E=curRoom.fetchFromMOBRoomFavorsItems(mob,null,searchName,Wearable.FILTER_UNWORNONLY);
				if(E instanceof Item)
					V.add((Item)E);
			}
			else
			if(searchName.length()>0)
			{
				for(int i=0;i<curRoom.numItems();i++)
				{
					final Item I=curRoom.getItem(i);
					if((I!=null)&&(CMLib.english().containsString(I.name(),searchName)))
						V.add(I);
				}
			}
			if(V.size()==0)
			{
				for(final Enumeration<Room> r=mob.location().getArea().getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					Item I=null;
					int num=1;
					while((num<=1)||(I!=null))
					{
						I=R.findItem(searchName+"."+num);
						if((I!=null)&&(!V.contains(I)))
							V.add(I);
						num++;
						if((!allFlag)&&(V.size()>0))
							break;
					}
					if((!allFlag)&&(V.size()>0))
						break;
				}
			}
			if(V.size()==0)
			{
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						Item I=null;
						int num=1;
						while((num<=1)||(I!=null))
						{
							I=R.findItem(searchName+"."+num);
							if((I!=null)&&(!V.contains(I)))
								V.add(I);
							num++;
							if((!allFlag)&&(V.size()>0))
								break;
						}
						if((!allFlag)&&(V.size()>0))
							break;
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
			}
		}
		else
		{
			if(!allFlag)
			{
				final MOB M=CMLib.sessions().findPlayerOnline(searchName,true);
				if(M!=null)
					V.add(M);
			}
			if(V.size()==0)
			{
				for(final Enumeration<Room> r=mob.location().getArea().getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					MOB M=null;
					int num=1;
					while((num<=1)||(M!=null))
					{
						M=R.fetchInhabitant(searchName+"."+num);
						if((M!=null)&&(!V.contains(M)))
							V.add(M);
						num++;
						if((!allFlag)&&(V.size()>0))
							break;
					}
					if((!allFlag)&&(V.size()>0))
						break;
				}
			}
			if(V.size()==0)
			{
				try
				{
					for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						MOB M=null;
						int num=1;
						while((num<=1)||(M!=null))
						{
							M=R.fetchInhabitant(searchName+"."+num);
							if((M!=null)&&(!V.contains(M)))
								V.add(M);
							num++;
							if((!allFlag)&&(V.size()>0))
								break;
						}
						if((!allFlag)&&(V.size()>0))
							break;
					}
				}
				catch (final NoSuchElementException nse)
				{
				}
			}
			if((!allFlag)&&(V.size()==0))
			{
				final MOB M=CMLib.players().getLoadPlayer(searchName);
				if(M!=null)
					V.add(M);
			}
		}

		if(V.size()==0)
		{
			mob.tell(L("Transfer what?  '@x1' is unknown to you.",searchName));
			return false;
		}

		boolean inventoryFlag=false;
		final StringBuffer cmd = new StringBuffer(CMParms.combine(commands,1));
		if(cmd.toString().equalsIgnoreCase("inventory"))
		{
			room=mob.location();
			inventoryFlag=true;
		}
		else
		if(cmd.toString().equalsIgnoreCase("here")||cmd.toString().equalsIgnoreCase("."))
			room=mob.location();
		else
		if(CMLib.map().getRoom(cmd.toString())!=null)
			room=CMLib.map().getRoom(cmd.toString());
		else
		if(CMLib.directions().getDirectionCode(cmd.toString())>=0)
			room=mob.location().getRoomInDir(CMLib.directions().getDirectionCode(cmd.toString()));
		else
			room=CMLib.map().findWorldRoomLiberally(mob,cmd.toString(),"RIPME",100,120000);

		if(room==null)
		{
			mob.tell(L("Transfer where? '@x1' is unknown.  Enter a Room ID, player name, area name, or room text!",cmd.toString()));
			return false;
		}
		for(int i=0;i<V.size();i++)
		{
			if(V.get(i) instanceof Item)
			{
				final Item I=(Item)V.get(i);
				final Room itemRoom=CMLib.map().roomLocation(I);
				if((itemRoom!=null)
				&&((!room.isContent(I))||(inventoryFlag))
				&&(CMSecurity.isAllowed(mob, itemRoom, CMSecurity.SecFlag.TRANSFER))
				&&(CMSecurity.isAllowed(mob, room, CMSecurity.SecFlag.TRANSFER)))
				{
					if(inventoryFlag)
						mob.moveItemTo(I,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
					else
						room.moveItemTo(I,ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
				}
			}
			else
			if(V.get(i) instanceof MOB)
			{
				final MOB M=(MOB)V.get(i);
				final Room mobRoom=CMLib.map().roomLocation(M);
				if((mobRoom!=null)
				&&(!room.isInhabitant(M))
				&&(CMSecurity.isAllowed(mob, mobRoom, CMSecurity.SecFlag.TRANSFER))
				&&(CMSecurity.isAllowed(mob, room, CMSecurity.SecFlag.TRANSFER)))
				{
					if(M.isPlayer() && (!CMLib.flags().isInTheGame(M, true)))
						M.setLocation(room);
					else
					{
						if((mob.playerStats().getTranPoofOut().length()>0)&&(mob.location()!=null))
							M.location().show(M,M.location(),CMMsg.MSG_LEAVE|CMMsg.MASK_ALWAYS,mob.playerStats().getTranPoofOut());
						room.bringMobHere(M,true);
					}
					if(mob.playerStats().getTranPoofIn().length()>0)
						M.location().show(M,M.location(),CMMsg.MSG_ENTER|CMMsg.MASK_ALWAYS,mob.playerStats().getTranPoofIn());
					if(!M.isMonster() && (room.isInhabitant(M)))
						CMLib.commands().postLook(M,true);
				}
			}
		}
		if(mob.playerStats().getTranPoofOut().length()==0)
			mob.tell(L("Done."));
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
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.TRANSFER);
	}

}
