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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2013 Bo Zimmerman

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
public class Copy extends StdCommand
{
	public Copy(){}

	private final String[] access={"COPY"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		commands.removeElementAt(0); // copy
		if(commands.size()<1)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is COPY (NUMBER) ([ITEM NAME]/[MOB NAME][ROOM ID] [DIRECTIONS]/[DIRECTIONS])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		int number=1;
		if(commands.size()>1)
		{
			number=CMath.s_int((String)commands.firstElement());
			if(number<1)
				number=1;
			else
				commands.removeElementAt(0);
		}
		String name=CMParms.combine(commands,0);
		Environmental dest=mob.location();
		Item srchContainer=null;
		int x=name.indexOf('@');
		if(x>0)
		{
			String rest=name.substring(x+1).trim();
			name=name.substring(0,x).trim();
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				MOB M=mob.location().fetchInhabitant(rest);
				if(M==null)
				{
					Item I = mob.location().findItem(null, rest);
					if(I instanceof Container)
						srchContainer=(Container)I;
					else
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
				}
				else
					dest=M;
			}
		}
		Environmental E=null;
		int dirCode=Directions.getGoodDirectionCode(name);
		if(dirCode>=0)
			E=mob.location();
		else
		if(commands.size()>1)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
			{
				commands.removeElementAt(commands.size()-1);
				name=CMParms.combine(commands,0);
				E=CMLib.map().getRoom(name);
				if(E==null)
				{
					if(commands.size()>1)
					{
						int subDirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
						if(subDirCode>=0)
						{
							commands.removeElementAt(commands.size()-1);
							name=CMParms.combine(commands,0);
							if(name.equalsIgnoreCase("exit"))
							{
								E=mob.location().getExitInDir(subDirCode);
								if(E==null) E=mob.location().getRawExit(subDirCode);
							}
							else
							if(name.equalsIgnoreCase("room"))
							{
								E=mob.location().getRoomInDir(subDirCode);
							}
							else
							{
								mob.tell(""+name+"' should be 'room' or 'exit'.");
								mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
								return false;
							}
						}
					}
					if(E==null)
					{
						mob.tell("Room ID '"+name+"' does not exist.  You can also try exit <dir> and room <dir>.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
				}
			}
		}
		if(E==null) E=mob.location().fetchFromRoomFavorItems(srchContainer,name);
		if(E==null) E=mob.location().fetchFromRoomFavorMOBs(srchContainer,name);
		if(E==null)	E=mob.findItem(name);
		if((E==null)&&(srchContainer==null))
		{
			try
			{
				E=CMLib.map().findFirstInhabitant(mob.location().getArea().getMetroMap(), mob, name, 50);
				if(E==null) 
					E=CMLib.map().findFirstRoomItem(mob.location().getArea().getMetroMap(), mob, name, true, 50);
				if(E==null) 
					E=CMLib.map().findFirstInventory(null, mob, name, 50);
				if(E==null) 
					E=CMLib.map().findFirstShopStock(null, mob, name, 50);
				if(E==null) 
					E=CMLib.map().findFirstInventory(CMLib.map().rooms(), mob, name, 50);
				if(E==null) 
					E=CMLib.map().findFirstShopStock(CMLib.map().rooms(), mob, name, 50);
			}catch(NoSuchElementException e){}
		}
		if((E==null)&&(srchContainer==null))
		{
			E=CMLib.map().getArea(name);
		}
		if(E==null)
		{
			mob.tell("There's no such thing in the living world as a '"+name+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		Room room=mob.location();
		for(int i=0;i<number;i++)
		{
			if(E instanceof MOB)
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COPYMOBS))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				MOB newMOB=(MOB)E.copyOf();
				newMOB.setSession(null);
				newMOB.setStartRoom(room);
				newMOB.setLocation(room);
				newMOB.recoverCharStats();
				newMOB.recoverPhyStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.bringToLife(room,true);
				if(i==0)
				{
					if(number>1)
						room.show(newMOB,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+newMOB.name()+"s instantiate from the Java Plane.");
					else
						room.show(newMOB,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java Plane.");
					Log.sysOut("SysopUtils",mob.Name()+" copied "+number+" mob "+newMOB.Name()+".");
				}
			}
			else
			if((E instanceof Item)&&(!(E instanceof ArchonOnly)))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COPYITEMS))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				Item newItem=(Item)E.copyOf();
				newItem.setContainer(null);
				newItem.wearAt(0);
				String end="from the sky";
				if(dest instanceof Room)
					((Room)dest).addItem(newItem);
				else
				if(dest instanceof MOB)
				{
					((MOB)dest).addItem(newItem);
					end="into "+dest.name()+"'s arms";
				}
				if(i==0)
				{
					if(number>1)
					{
						if(newItem.name().toLowerCase().endsWith("s"))
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+newItem.name()+" falls "+end+".");
						else
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+newItem.name()+"s falls "+end+".");
					}
					else
						room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" fall "+end+".");
					Log.sysOut("SysopUtils",mob.Name()+" "+number+" copied "+newItem.ID()+" item.");
				}
			}
			else
			if((E instanceof Room)&&(dirCode>=0))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COPYROOMS))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				if(room.getRoomInDir(dirCode)!=null)
				{
					mob.tell("A room already exists "+Directions.getInDirectionName(dirCode)+"!");
					return false;
				}
				synchronized(("SYNC"+room.roomID()).intern())
				{
					Room newRoom=(Room)E.copyOf();
					newRoom.clearSky();
					if(newRoom instanceof GridLocale)
						((GridLocale)newRoom).clearGrid(null);
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						newRoom.rawDoors()[d]=null;
						newRoom.setRawExit(d,null);
					}
					room.rawDoors()[dirCode]=newRoom;
					newRoom.rawDoors()[Directions.getOpDirectionCode(dirCode)]=room;
					if(room.getRawExit(dirCode)==null)
						room.setRawExit(dirCode,CMClass.getExit("Open"));
					newRoom.setRawExit(Directions.getOpDirectionCode(dirCode),(Exit)(room.getRawExit(dirCode).copyOf()));
					newRoom.setRoomID(room.getArea().getNewRoomID(room,dirCode));
					if(newRoom.roomID().length()==0)
					{
						mob.tell("A room may not be created in that direction.  Are you sure you havn't reached the edge of a grid?");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
					newRoom.setArea(room.getArea());
					CMLib.database().DBCreateRoom(newRoom);
					CMLib.database().DBUpdateExits(newRoom);
					CMLib.database().DBUpdateExits(room);
					if(newRoom.numInhabitants()>0)
						CMLib.database().DBUpdateMOBs(newRoom);
					if(newRoom.numItems()>0)
						CMLib.database().DBUpdateItems(newRoom);
					newRoom.getArea().fillInAreaRoom(newRoom);
					if(i==0)
					{
						if(number>1)
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+newRoom.roomTitle(mob)+"s fall "+Directions.getInDirectionName(dirCode)+".");
						else
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newRoom.roomTitle(mob)+" falls "+Directions.getInDirectionName(dirCode)+".");
						Log.sysOut("SysopUtils",mob.Name()+" copied "+number+" rooms "+newRoom.roomID()+".");
					}
					else
						room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newRoom.roomTitle(mob)+" falls "+Directions.getInDirectionName(dirCode)+".");
					room=newRoom;
				}
			}
			else
			if((E instanceof Exit)&&(dirCode>=0))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COPYROOMS))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				Room editRoom=room;
				if(editRoom.getRawExit(dirCode)==E)
				{
					editRoom=room.getRoomInDir(dirCode);
					dirCode=Directions.getOpDirectionCode(dirCode);
					if((editRoom==null)||(editRoom.getRoomInDir(dirCode)==null))
					{
						mob.tell("No opposite room exists to copy this exit into.");
						return false;
					}
				}
				synchronized(("SYNC"+editRoom.roomID()).intern())
				{
					Exit oldE=editRoom.getRawExit(dirCode);
					if((oldE==null)||(oldE!=E))
					{
						editRoom.setRawExit(dirCode, E);
						CMLib.database().DBUpdateExits(editRoom);
					}
					room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+E.name()+" falls "+Directions.getInDirectionName(dirCode)+".");
				}
			}
			else
			if(E instanceof Area)
			{
				if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDAREAS))
				||(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COPYROOMS)))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				Area newArea=(Area)E.copyOf();
				while(CMLib.map().getArea(newArea.Name())!=null)
					newArea.setName("Copy of "+newArea.Name());
				newArea.setSavable(true);
				if(!CMSecurity.isASysOp(mob))
					newArea.addSubOp(mob.Name());
				CMLib.map().addArea(newArea);
				CMLib.database().DBCreateArea(newArea);
				Map<Room,Room> translationMap=new HashMap<Room,Room>();
				for(Enumeration<Room> r=((Area)E).getCompleteMap();r.hasMoreElements();)
				{
					Room oldR=CMLib.map().getRoom(r.nextElement());
					if(oldR==null) continue;
					CMLib.map().resetRoom(oldR);
					Room R=(Room)oldR.copyOf();
					R.setArea(newArea); // adds the room to the area
					int hashDex=R.roomID().indexOf('#');
					if(hashDex>0)
						R.setRoomID(newArea.Name()+R.roomID().substring(hashDex));
					else
						R.setRoomID(newArea.Name()+R.roomID());
					translationMap.put(oldR, R);
				}
				for(Enumeration<Room> ir=newArea.getCompleteMap();ir.hasMoreElements();)
				{
					Room R=CMLib.map().getRoom(ir.nextElement());
					if(R==null) continue;
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						Room dirR=R.rawDoors()[d];
						if(translationMap.containsKey(dirR))
							R.rawDoors()[d]=translationMap.get(dirR);
					}
					R.setSavable(true);
					CMLib.database().DBCreateRoom(R);
					CMLib.database().DBUpdateMOBs(R);
					CMLib.database().DBUpdateItems(R);
					CMLib.database().DBUpdateExits(R);
					R.startItemRejuv();
				}
				room.show(mob,null,CMMsg.MSG_OK_ACTION,"Suddenly, a massive new landscape instantiates from the Java Plane.");
				Log.sysOut("SysopUtils",mob.Name()+" copied area "+E.Name());
			}
			else
			{
				mob.tell("I can't just make a copy of a '"+E.name()+"'.\n\r");
				room.showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
				break;
			}
		}
		if((E instanceof Item)&&(!(E instanceof ArchonOnly))&&(room!=null))
			room.recoverRoomStats();
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedContainsAny(mob,mob.location(),CMSecurity.SECURITY_COPY_GROUP);}

	
}
