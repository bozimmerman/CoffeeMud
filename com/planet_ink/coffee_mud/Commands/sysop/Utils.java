package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class Utils
{
	private Utils(){}
	
	public static void newSomething(MOB mob, Vector commands)
	{
		commands.removeElementAt(0); // copy
		if(commands.size()<1)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is COPY (NUMBER) ([ITEM NAME]/[MOB NAME]/[DIRECTIONS])\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
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
		Environmental E=null;
		int dirCode=Directions.getGoodDirectionCode(name);
		if(dirCode>=0)
			E=mob.location();
		else
			E=mob.location().fetchFromRoomFavorItems(null,name,Item.WORN_REQ_UNWORNONLY);
		
		if(E==null)	E=mob.fetchInventory(name);
		if(E==null)
			for(Iterator r=CMMap.rooms();r.hasNext();)
			{
				Room R=(Room)r.next();
				E=R.fetchInhabitant(name);
				if(E==null) E=R.fetchItem(null,name);
				if(E!=null) break;
			}
		if(E==null)
			for(Iterator r=CMMap.rooms();r.hasNext();)
			{
				Room R=(Room)r.next();
				for(int m=0;m<R.numInhabitants();m++)
				{
					MOB mob2=R.fetchInhabitant(m);
					if(mob2!=null)
					{
						E=mob2.fetchInventory(name);
						if((E==null)&&(mob2 instanceof ShopKeeper))
							E=((ShopKeeper)mob2).getStock(name,null);
					}
					if(E!=null) break;
				}
				if(E!=null) break;
			}
		if(E==null)
		{
			mob.tell("There's no such thing in the living world as a '"+name+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Room room=mob.location();
		for(int i=0;i<number;i++)
		{
			if(E instanceof MOB)
			{
				MOB newMOB=(MOB)E.copyOf();
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
						room.show(newMOB,null,Affect.MSG_OK_ACTION,"Suddenly, "+number+" "+newMOB.name()+"s instantiate from the Java plain.");
					else
						room.show(newMOB,null,Affect.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
					Log.sysOut("SysopUtils",mob.ID()+" copied "+number+" mob "+newMOB.ID()+".");
				}
			}
			else
			if(E instanceof Item)
			{
				Item newItem=(Item)E.copyOf();
				newItem.setContainer(null);
				newItem.wearAt(0);
				room.addItem(newItem);
				room.recoverRoomStats();
				if(i==0)
				{
					if(number>1)
						room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+number+" "+newItem.name()+"s drop from the sky.");
					else
						room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
					Log.sysOut("SysopUtils",mob.ID()+" copied "+number+" item "+newItem.ID()+".");
				}
			}
			else
			if((E instanceof Room)&&(dirCode>=0))
			{
				if(room.getRoomInDir(dirCode)!=null)
				{
					mob.tell("A room already exists "+Directions.getInDirectionName(dirCode)+"!");
					return;
				}
				else
				{
					Room newRoom=(Room)room.copyOf();
					room.rawDoors()[dirCode]=newRoom;
					newRoom.rawDoors()[Directions.getOpDirectionCode(dirCode)]=room;
					if(room.rawExits()[dirCode]==null)
						room.rawExits()[dirCode]=CMClass.getExit("Open");
					newRoom.rawExits()[Directions.getOpDirectionCode(dirCode)]=(Exit)(room.rawExits()[dirCode].copyOf());
					newRoom.setID(ExternalPlay.getOpenRoomID(room.getArea().name()));
					newRoom.setArea(room.getArea());
					ExternalPlay.DBCreateRoom(newRoom,CMClass.className(newRoom));
					ExternalPlay.DBUpdateExits(newRoom);
					ExternalPlay.DBUpdateExits(room);
					if(newRoom.numInhabitants()>0)
						ExternalPlay.DBUpdateMOBs(newRoom);
					if(newRoom.numItems()>0)
						ExternalPlay.DBUpdateItems(newRoom);
					CMMap.addRoom(newRoom);
					newRoom.getArea().fillInAreaRoom(newRoom);
					if(i==0)
					{
						if(number>1)
							room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+number+" "+room.displayText()+"s fall "+Directions.getInDirectionName(dirCode)+".");
						else
							room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+room.displayText()+" falls "+Directions.getInDirectionName(dirCode)+".");
						Log.sysOut("SysopUtils",mob.ID()+" copied "+number+" rooms "+room.ID()+".");
					}
					else
						room.showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+room.displayText()+" falls "+Directions.getInDirectionName(dirCode)+".");
					room=newRoom;
				}
			}
			else
			{
				mob.tell("I can't just make a copy of a '"+E.name()+"'.\n\r");
				room.showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
				break;
			}
		}
	}

}
