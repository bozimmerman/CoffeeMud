package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.Exits.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;

public class Rooms
{
	public static String getOpenRoomID(String AreaID)
	{
		int highest=0;
		for(int m=0;m<MUD.map.size();m++)
		{
			Room thisRoom=(Room)MUD.map.elementAt(m);
			if(thisRoom.getAreaID().equals(AreaID))
			{
				if(thisRoom.ID().startsWith(AreaID+"#"))
				{
					int newnum=Util.s_int(thisRoom.ID().substring(AreaID.length()+1));
					if(newnum>=highest)
						highest=newnum+1;
				}
			}
		}
		return AreaID+"#"+highest;
	}

	public static void Create(MOB mob, Vector commands)
	{
		if(commands.size()<5)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [NEW [ROOM TYPE] / LINK [ROOM ID]) \n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, mob or D.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Room thisRoom=null;
		String WhatToDo=(String)commands.elementAt(3);
		if(WhatToDo.equalsIgnoreCase("NEW"))
		{
			String Locale=(String)commands.elementAt(4);
			thisRoom=(Room)MUD.getLocale(Locale);
			if(thisRoom==null)
			{
				mob.tell("You have failed to specify a valid room type '"+Locale+"'.\n\rThe format is CREATE ROOM [DIRECTION] [NEW [ROOM TYPE] / LINK [ROOM ID]) \n\r");
				mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
			else
			{
				thisRoom=(Room)thisRoom.newInstance();
				thisRoom.setAreaID(mob.location().getAreaID());
				thisRoom.setID(getOpenRoomID(mob.location().getAreaID()));
				thisRoom.setDisplayText(INI.className(thisRoom)+"-"+thisRoom.ID());
				thisRoom.setDescription("");
				RoomLoader.DBCreate(thisRoom,Locale);
				MUD.map.addElement(thisRoom);
			}
		}
		else
		if(WhatToDo.equalsIgnoreCase("LINK"))
		{
			String RoomID=(String)commands.elementAt(4);
			thisRoom=(Room)MUD.getRoom(RoomID);
			if(thisRoom==null)
			{
				mob.tell("Room \""+RoomID+"\" is unknown.  Try again.");
				mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		if(thisRoom==null)
		{
			mob.tell("You have  to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [NEW [ROOM TYPE] / LINK [ROOM ID]) \n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Room opRoom=mob.location().getRoom(direction);
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.getRoom(Directions.getOpDirectionCode(direction));

		if((reverseRoom!=null)&&(reverseRoom==mob.location()))
			mob.tell("Opposite room already exists and heads this way.  One-way link created.");

		if(opRoom!=null)
			mob.location().doors()[direction]=null;

		mob.location().doors()[direction]=thisRoom;
		Exit thisExit=mob.location().getExit(direction);
		if(thisExit==null)
		{
			thisExit=new StdOpenDoorway();
			mob.location().exits()[direction]=thisExit;
		}
		RoomLoader.DBUpdateExits(mob.location());

		if(thisRoom.doors()[Directions.getOpDirectionCode(direction)]==null)
		{
			thisRoom.doors()[Directions.getOpDirectionCode(direction)]=mob.location();
			thisRoom.exits()[Directions.getOpDirectionCode(direction)]=thisExit;
			RoomLoader.DBUpdateExits(thisRoom);
		}

		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"Suddenly a block of earth falls from the sky.\n\r");
		Log.sysOut("Rooms",mob.ID()+" created room "+thisRoom.ID()+".");
	}

	public static void Modify(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()==2)
		{
			// too dangerous 
			//Generic.genRoomType(mob,mob.location());
			Generic.genDisplayText(mob,mob.location());
			Generic.genDescription(mob,mob.location());
			RoomLoader.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.GENERAL,"There is something different about this place...\n\r");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION] [TEXT]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr=CommandProcessor.combine(commands,3);

		if(command.equalsIgnoreCase("AREA"))
		{
			String checkID=getOpenRoomID(restStr);
			if(checkID.endsWith("#0"))
			{
				if(!mob.isMonster())
				{
					if(mob.session().confirm("\n\rThis command will create a BRAND NEW AREA\n\r with Area code '"+restStr+"'.  Are you SURE (y/N)?","N"))
					{
						Scoring.areasList=null;
						mob.location().setAreaID(restStr);
						MUD.map.remove(mob.location());
						String oldID=mob.location().ID();
						mob.location().setID(checkID);

						RoomLoader.DBReCreate(mob.location(),oldID);

						MUD.map.addElement(mob.location());
						for(int m=0;m<MUD.map.size();m++)
						{
							Room thisRoom=(Room)MUD.map.elementAt(m);
							for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
							{
								Room thatRoom=thisRoom.getRoom(dir);
								if(thatRoom==mob.location())
								{
									RoomLoader.DBUpdateExits(thisRoom);
									break;
								}
							}
						}
					}
					mob.location().show(mob,null,Affect.GENERAL,"This entire area twitches.\n\r");
				}
				else
				{
					mob.tell("Sorry Charlie!");
					mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
				}
			}
			else
			{
				mob.location().setAreaID(restStr);
				RoomLoader.DBUpdateRoom(mob.location());
				mob.location().show(mob,null,Affect.GENERAL,"This area twitches.\n\r");
			}
		}
		else
		if(command.equalsIgnoreCase("NAME"))
		{
			mob.location().setDisplayText(restStr);
			RoomLoader.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.GENERAL,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			mob.location().setDescription(restStr);
			RoomLoader.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.GENERAL,"The very nature of reality changes.\n\r");
		}
		else
		{
			mob.tell("You have failed to specify an aspect.  Try AREA, NAME, or DESCRIPTION.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
		}
		Log.sysOut("Rooms",mob.ID()+" modified room "+mob.location().ID()+".");
	}

	public static void Destroy(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ROOM ([DIRECTION],[ROOM ID])\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		boolean confirmed=false;
		if((commands.size()>3))
		{
			if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("CONFIRMED"))
			{
				commands.removeElementAt(commands.size()-1);	
				confirmed=true;
			}
		}
		String roomdir=CommandProcessor.combine(commands,2);
		int direction=Directions.getDirectionCode(roomdir);
		Room deadRoom=MUD.getRoom(roomdir);
		if((deadRoom==null)&&(direction<0))
		{
			mob.tell("You have failed to specify a direction.  Try a VALID ROOM ID, or (N, S, E, W, U or D).\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(mob.isMonster())
		{
			mob.tell("Sorry Charlie!");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
			return;

		}
		if(deadRoom!=null)
		{
			if(mob.location()==deadRoom)
			{
				mob.tell("You dip! You have to leave this room first!");
				mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a powerful spell.");
				return;
			}

			if(!confirmed)
			{
				if(!mob.session().confirm("You are about to permanantly destroy EVERY occurrance of Room \""+deadRoom.ID()+"\" in existence.  Are you ABSOLUTELY SURE (y/N)","N")) return;
				if(!mob.session().confirm("But are you really REALLY sure (y/N)?","N")) return;
			}
			MUD.map.remove(deadRoom);
			for(int m=0;m<MUD.map.size();m++)
			{
				Room thisRoom=(Room)MUD.map.elementAt(m);
				for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
				{
					Room thatRoom=thisRoom.getRoom(dir);
					if(thatRoom==deadRoom)
					{
						thisRoom.doors()[dir]=null;
						thisRoom.exits()[dir]=null;
						RoomLoader.DBUpdateExits(thisRoom);
					}
				}
			}
			for(int s=0;s<MUD.allSessions.size();s++)
			{
				Session thisSess=(Session)MUD.allSessions.elementAt(s);
				if((thisSess.mob!=null)&&(thisSess.mob.location()==deadRoom))
				{
					thisSess.killFlag=true;
					thisSess.mob.setLocation(null);
				}
			}
			Scoring.areasList=null;
			if(deadRoom instanceof GridLocale)
				((GridLocale)deadRoom).clearGrid();
			RoomLoader.DBDelete(deadRoom);
			mob.tell("The sound of massive destruction rings in your ears.");
			mob.location().showOthers(mob,null,Affect.SOUND_NOISE,"The sound of massive destruction rings in your ears.");
			Log.sysOut("Rooms",mob.ID()+" destroyed room "+deadRoom.ID()+".");
		}
		else
		{
			mob.location().doors()[direction]=null;
			mob.location().exits()[direction]=null;
			RoomLoader.DBUpdateExits(mob.location());
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,"A wall of inhibition falls "+Directions.getInDirectionName(direction)+".");
			Log.sysOut("Rooms",mob.ID()+" unlinked direction "+Directions.getDirectionName(direction)+" from room "+mob.location().ID()+".");
		}
	}

	public static void clearTheRoom(Room room)
	{
		for(int m=room.numInhabitants()-1;m>=0;m--)
		{
			MOB mob2=room.fetchInhabitant(m);
			if(RoomLoader.isEligibleMonster(mob2))
			{
				if(mob2.getStartRoom()==room)
					mob2.destroy();
				else
				if(mob2.getStartRoom()!=null)
					mob2.getStartRoom().bringMobHere(mob2,true);
			}
		}
		for(int i=room.numItems()-1;i>=0;i--)
			room.delItem(room.fetchItem(i));
		clearDebri(room,0);
	}
	
	public static void clearDebri(Room room, int taskCode)
	{
		for(int v=0;v<ServiceEngine.tickGroup.size();v++)
		{
			Tick tock=(Tick)ServiceEngine.tickGroup.elementAt(v);
			int o=0;
			while(o<tock.tickers.size())
			{
				TockClient C=(TockClient)tock.tickers.elementAt(o);
				if((C.clientObject instanceof ItemRejuv)&&(taskCode<2))
				{
					ItemRejuv I=(ItemRejuv)C.clientObject;
					if(I.myProperLocation==room)
					{
						tock.tickers.removeElementAt(o);
						I.myProperLocation=null;
					}
					else
						o++;
					
				}
				else
				if((C.clientObject instanceof MOB)&&((taskCode==0)||(taskCode==2)))
				{
					MOB mob=(MOB)C.clientObject;
					if((mob.getStartRoom()==room)
					&&(mob.isMonster())
					&&(!room.isInhabitant(mob)))
					{
						mob.destroy();
						tock.tickers.removeElementAt(o);
					}
					else
						o++;
				}
				else
					o++;
			}
		}
	}
	
	public static void clearDebriAndRestart(Room room, int taskCode)
	{
		clearDebri(room,0);
		if(taskCode<2)
		{
			RoomLoader.DBUpdateItems(room);
			room.startItemRejuv();
		}
		if((taskCode==0)||(taskCode==2))
			RoomLoader.DBUpdateMOBs(room);
	}
}
