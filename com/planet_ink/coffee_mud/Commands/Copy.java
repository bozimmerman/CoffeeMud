package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Copy extends StdCommand
{
	public Copy(){}

	private String[] access={"COPY"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		commands.removeElementAt(0); // copy
		if(commands.size()<1)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is COPY (NUMBER) ([ITEM NAME]/[MOB NAME]/[DIRECTIONS])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		int number=1;
		if(commands.size()>1)
		{
			number=Util.s_int((String)commands.firstElement());
			if(number<1)
				number=1;
			else
				commands.removeElementAt(0);
		}
		String name=Util.combine(commands,0);
		Environmental dest=mob.location();
		int x=name.indexOf("@");
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
					mob.tell("MOB '"+rest+"' not found.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return false;
				}
				dest=M;
			}
		}
		Environmental E=null;
		int dirCode=Directions.getGoodDirectionCode(name);
		if(dirCode>=0)
			E=mob.location();
		else
			E=mob.location().fetchFromRoomFavorItems(null,name,Item.WORN_REQ_UNWORNONLY);

		if(E==null)	E=mob.fetchInventory(name);
		if(E==null)
		{
		    try
		    {
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					E=R.fetchInhabitant(name);
					if(E==null) E=R.fetchItem(null,name);
					if(E!=null) break;
				}
		    }catch(NoSuchElementException e){}
		}
		if(E==null)
		{
		    try
		    {
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					for(int m=0;m<R.numInhabitants();m++)
					{
						MOB mob2=R.fetchInhabitant(m);
						if(mob2!=null)
						{
							E=mob2.fetchInventory(name);
							if((E==null)&&(CoffeeUtensils.getShopKeeper(mob2)!=null))
								E=CoffeeUtensils.getShopKeeper(mob2).getStock(name,null);
						}
						if(E!=null) break;
					}
					if(E!=null) break;
				}
		    }catch(NoSuchElementException e){}
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
				if(!CMSecurity.isAllowed(mob,mob.location(),"COPYMOBS"))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				MOB newMOB=(MOB)E.copyOf();
				newMOB.setSession(null);
				newMOB.setStartRoom(room);
				newMOB.setLocation(room);
				newMOB.recoverCharStats();
				newMOB.recoverEnvStats();
				newMOB.recoverMaxState();
				newMOB.resetToMaxState();
				newMOB.bringToLife(room,true);
				if(i==0)
				{
					if(number>1)
						room.show(newMOB,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+newMOB.name()+"s instantiate from the Java plain.");
					else
						room.show(newMOB,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
					Log.sysOut("SysopUtils",mob.Name()+" copied "+number+" mob "+newMOB.Name()+".");
				}
			}
			else
			if((E instanceof Item)&&(!(E instanceof ArchonOnly)))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"COPYITEMS"))
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
					((MOB)dest).addInventory(newItem);
					end="into "+dest.name()+"'s arms";
				}
				if(i==0)
				{
					if(number>1)
						room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+newItem.name()+"s falls "+end+".");
					else
						room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" fall "+end+".");
					Log.sysOut("SysopUtils",mob.Name()+" copied "+number+" item "+newItem.ID()+".");
				}
			}
			else
			if((E instanceof Room)&&(dirCode>=0))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"COPYROOMS"))
				{
					mob.tell("You are not allowed to copy "+E.name());
					return false;
				}
				if(room.getRoomInDir(dirCode)!=null)
				{
					mob.tell("A room already exists "+Directions.getInDirectionName(dirCode)+"!");
					return false;
				}
				else
				{
					Room newRoom=(Room)room.copyOf();
					newRoom.clearSky();
					if(newRoom instanceof GridLocale)
						((GridLocale)newRoom).clearGrid(null);
					for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
					{
						newRoom.rawDoors()[d]=null;
						newRoom.rawExits()[d]=null;
					}
					room.rawDoors()[dirCode]=newRoom;
					newRoom.rawDoors()[Directions.getOpDirectionCode(dirCode)]=room;
					if(room.rawExits()[dirCode]==null)
						room.rawExits()[dirCode]=CMClass.getExit("Open");
					newRoom.rawExits()[Directions.getOpDirectionCode(dirCode)]=(Exit)(room.rawExits()[dirCode].copyOf());
					newRoom.setRoomID(CMMap.getOpenRoomID(room.getArea().Name()));
					newRoom.setArea(room.getArea());
					CMClass.DBEngine().DBCreateRoom(newRoom,CMClass.className(newRoom));
					CMClass.DBEngine().DBUpdateExits(newRoom);
					CMClass.DBEngine().DBUpdateExits(room);
					if(newRoom.numInhabitants()>0)
						CMClass.DBEngine().DBUpdateMOBs(newRoom);
					if(newRoom.numItems()>0)
						CMClass.DBEngine().DBUpdateItems(newRoom);
					CMMap.addRoom(newRoom);
					newRoom.getArea().fillInAreaRoom(newRoom);
					if(i==0)
					{
						if(number>1)
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+number+" "+room.roomTitle()+"s fall "+Directions.getInDirectionName(dirCode)+".");
						else
							room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+room.roomTitle()+" falls "+Directions.getInDirectionName(dirCode)+".");
						Log.sysOut("SysopUtils",mob.Name()+" copied "+number+" rooms "+room.roomID()+".");
					}
					else
						room.showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+room.roomTitle()+" falls "+Directions.getInDirectionName(dirCode)+".");
					room=newRoom;
				}
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
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"COPY");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
