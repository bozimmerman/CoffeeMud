package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Rooms
{
	public String getOpenRoomID(String AreaID)
	{
		int highest=0;
		AreaID+="#";
		for(int m=0;m<CMMap.map.size();m++)
		{
			Room thisRoom=(Room)CMMap.map.elementAt(m);
			if(thisRoom.ID().startsWith(AreaID))
			{
				int newnum=Util.s_int(thisRoom.ID().substring(AreaID.length()));
				if(newnum>=highest)
					highest=newnum+1;
			}
		}
		return AreaID+highest;
	}

	private void exitifyNewPortal(MOB mob, Room room, int direction)
	{

		Room opRoom=mob.location().doors()[direction];
		if((opRoom!=null)&&(opRoom.ID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.doors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==mob.location()))
			mob.tell("Opposite room already exists and heads this way.  One-way link created.");

		if(opRoom!=null)
			mob.location().doors()[direction]=null;

		mob.location().doors()[direction]=room;
		Exit thisExit=mob.location().exits()[direction];
		if(thisExit==null)
		{
			thisExit=CMClass.getExit("StdOpenDoorway");
			mob.location().exits()[direction]=thisExit;
		}
		ExternalPlay.DBUpdateExits(mob.location());

		if(room.doors()[Directions.getOpDirectionCode(direction)]==null)
		{
			room.doors()[Directions.getOpDirectionCode(direction)]=mob.location();
			room.exits()[Directions.getOpDirectionCode(direction)]=thisExit;
			ExternalPlay.DBUpdateExits(room);
		}
	}

	public void create(MOB mob, Vector commands)
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		int direction=Directions.getDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, mob or D.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Room thisRoom=null;
		String Locale=(String)commands.elementAt(3);
		thisRoom=(Room)CMClass.getLocale(Locale);
		if(thisRoom==null)
		{
			mob.tell("You have failed to specify a valid room type '"+Locale+"'.\n\rThe format is CREATE ROOM [DIRECTION] [NEW [ROOM TYPE] / LINK [ROOM ID]) \n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		{
			thisRoom=(Room)thisRoom.newInstance();
			thisRoom.setAreaID(mob.location().getAreaID());
			thisRoom.setID(getOpenRoomID(mob.location().getAreaID()));
			thisRoom.setDisplayText(CMClass.className(thisRoom)+"-"+thisRoom.ID());
			thisRoom.setDescription("");
			ExternalPlay.DBCreate(thisRoom,Locale);
			CMMap.map.addElement(thisRoom);
		}

		if(thisRoom==null)
		{
			mob.tell("You have  to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		exitifyNewPortal(mob,thisRoom,direction);

		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly a block of earth falls from the sky.\n\r");
		Log.sysOut("Rooms",mob.ID()+" created room "+thisRoom.ID()+".");
	}

	public void link(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is LINK [ROOM ID] [DIRECTION]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		int direction=Directions.getDirectionCode(Util.combine(commands,2));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, mob or D.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Room thisRoom=null;
		String RoomID=(String)commands.elementAt(1);
		thisRoom=(Room)CMMap.getRoom(RoomID);
		if(thisRoom==null)
		{
			mob.tell("Room \""+RoomID+"\" is unknown.  Try again.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		exitifyNewPortal(mob,thisRoom,direction);

		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly a portal opens up in the landscape.\n\r");
		Log.sysOut("Rooms",mob.ID()+" linked to room "+thisRoom.ID()+".");
	}

	private void flunkCmd1(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION, AFFECTS, BEHAVIORS] [TEXT]\n\r");
		mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}

	public void modify(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()==2)
		{
			// too dangerous
			//Generic.genRoomType(mob,mob.location());
			new Generic().genDisplayText(mob,mob.location());
			new Generic().genDescription(mob,mob.location());
			new Generic().genBehaviors(mob,mob.location());
			new Generic().genAffects(mob,mob.location());
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
			return;
		}
		if(commands.size()<3) { flunkCmd1(mob); return;}

		String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr="";
		if(commands.size()>=3)
			restStr=Util.combine(commands,3);

		if(command.equalsIgnoreCase("AREA"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}

			String checkID=getOpenRoomID(restStr);
			if(checkID.endsWith("#0"))
			{
				if(!mob.isMonster())
				{
					if(mob.session().confirm("\n\rThis command will create a BRAND NEW AREA\n\r with Area code '"+restStr+"'.  Are you SURE (y/N)?","N"))
					{
						Resources.removeResource("areasList");
						mob.location().setAreaID(restStr);
						CMMap.map.remove(mob.location());
						String oldID=mob.location().ID();
						mob.location().setID(checkID);

						ExternalPlay.DBReCreate(mob.location(),oldID);

						CMMap.map.addElement(mob.location());
						for(int m=0;m<CMMap.map.size();m++)
						{
							Room thisRoom=(Room)CMMap.map.elementAt(m);
							for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
							{
								Room thatRoom=thisRoom.doors()[dir];
								if(thatRoom==mob.location())
								{
									ExternalPlay.DBUpdateExits(thisRoom);
									break;
								}
							}
						}
					}
					mob.location().show(mob,null,Affect.MSG_OK_ACTION,"This entire area twitches.\n\r");
				}
				else
				{
					mob.tell("Sorry Charlie!");
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
			}
			else
			{
				mob.location().setAreaID(restStr);
				ExternalPlay.DBUpdateRoom(mob.location());
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"This area twitches.\n\r");
			}
		}
		else
		if(command.equalsIgnoreCase("NAME"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDisplayText(restStr);
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDescription(restStr);
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("AFFECTS"))
		{
			new Generic().genAffects(mob,mob.location());
			mob.location().recoverEnvStats();
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("BEHAVIORS"))
		{
			new Generic().genBehaviors(mob,mob.location());
			mob.location().recoverEnvStats();
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		{
			mob.tell("You have failed to specify an aspect.  Try AREA, NAME, AFFECTS, BEHAVIORS, or DESCRIPTION.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
		}
		Log.sysOut("Rooms",mob.ID()+" modified room "+mob.location().ID()+".");
	}

	public void obliterateRoom(MOB mob, Room deadRoom)
	{
		CMMap.map.remove(deadRoom);
		for(int m=0;m<CMMap.map.size();m++)
		{
			Room thisRoom=(Room)CMMap.map.elementAt(m);
			boolean changes=false;
			for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
			{
				Room thatRoom=thisRoom.doors()[dir];
				if(thatRoom==deadRoom)
				{
					thisRoom.doors()[dir]=null;
					changes=true;
					if((thisRoom.exits()[dir]!=null)&&(thisRoom.exits()[dir].isGeneric()))
					{
						Exit GE=(Exit)thisRoom.exits()[dir];
						GE.setExitParams(GE.doorName(),deadRoom.ID(),GE.openWord(),GE.closedText());
					}
				}
			}
			if(changes)
				ExternalPlay.DBUpdateExits(thisRoom);
		}
		for(int mb=deadRoom.numInhabitants()-1;mb>=0;mb--)
		{
			MOB mob2=deadRoom.fetchInhabitant(mb);
			if(mob2!=null)
			{
				if((mob2.getStartRoom()!=deadRoom)&&(mob2.getStartRoom()!=null)&&(CMMap.getRoom(mob2.getStartRoom().ID())!=null))
					mob2.getStartRoom().bringMobHere(mob2,true);
				else
				{
					ExternalPlay.deleteTick(mob2,-1);
					mob2.destroy();
				}
			}
		}
		for(int i=deadRoom.numItems()-1;i>=0;i--)
		{
			Item item2=deadRoom.fetchItem(i);
			if(item2!=null)
			{
				ExternalPlay.deleteTick(item2,-1);
				item2.destroyThis();
			}
		}
		clearTheRoom(deadRoom);
		Resources.removeResource("areasList");
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid();
		ExternalPlay.DBDelete(deadRoom);
	}

	public void destroy(MOB mob, Vector commands)
		throws Exception
	{
		String thecmd=((String)commands.elementAt(0)).toLowerCase();
		if(commands.size()<3)
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell("You have failed to specify the proper fields.\n\rThe format is UNLINK (N,S,E,W,U, or D)\n\r");
			else
				mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ROOM ([DIRECTION],[ROOM ID])\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
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
		String roomdir=Util.combine(commands,2);
		int direction=Directions.getDirectionCode(roomdir);
		Room deadRoom=null;
		if(!thecmd.equalsIgnoreCase("UNLINK"))
			deadRoom=CMMap.getRoom(roomdir);
		if((deadRoom==null)&&(direction<0))
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell("You have failed to specify a direction.  Try (N, S, E, W, U or D).\n\r");
			else
				mob.tell("You have failed to specify a direction.  Try a VALID ROOM ID, or (N, S, E, W, U or D).\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(mob.isMonster())
		{
			mob.tell("Sorry Charlie!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;

		}
		if(deadRoom!=null)
		{
			if(mob.location()==deadRoom)
			{
				mob.tell("You dip! You have to leave this room first!");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}

			if(!confirmed)
				if(!mob.session().confirm("You are about to permanantly destroy Room \""+deadRoom.ID()+"\".  Are you ABSOLUTELY SURE (y/N)","N")) return;
			obliterateRoom(mob,deadRoom);
			mob.tell("The sound of massive destruction rings in your ears.");
			mob.location().showOthers(mob,null,Affect.MSG_NOISE,"The sound of massive destruction rings in your ears.");
			Log.sysOut("Rooms",mob.ID()+" destroyed room "+deadRoom.ID()+".");
		}
		else
		{
			mob.location().doors()[direction]=null;
			mob.location().exits()[direction]=null;
			ExternalPlay.DBUpdateExits(mob.location());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"A wall of inhibition falls "+Directions.getInDirectionName(direction)+".");
			Log.sysOut("Rooms",mob.ID()+" unlinked direction "+Directions.getDirectionName(direction)+" from room "+mob.location().ID()+".");
		}
	}

	public void destroyArea(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY AREA [AREA NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
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

		String areaName=Util.combine(commands,2);
		Room foundOne=null;
		for(Enumeration e=CMMap.map.elements();e.hasMoreElements();)
		{
			Room r=(Room)e.nextElement();
			if(r.getAreaID().equalsIgnoreCase(areaName))
			{
				foundOne=r;
				break;
			}
		}
		if(foundOne==null)
		{
			mob.tell("There is no such area as '"+areaName+"'");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
			return;
		}


		if(!confirmed);
		if(mob.session().confirm("Area: \""+areaName+"\", OBLITERATE IT???","N"))
		{
			if(mob.location().getAreaID().equalsIgnoreCase(areaName))
			{
				mob.tell("You dip!  You are IN that area!  Leave it first...");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
				return;
			}
			else
				confirmed=true;
		}
		foundOne=CMClass.getLocale("StdRoom");
		while(foundOne!=null)
		{
			foundOne=null;
			for(Enumeration e=CMMap.map.elements();e.hasMoreElements();)
			{
				Room r=(Room)e.nextElement();
				if(r.getAreaID().equalsIgnoreCase(areaName))
				{
					foundOne=r;
					break;
				}
			}
			if(foundOne!=null)
				new Rooms().obliterateRoom(mob,foundOne);
		}
		if(confirmed)
		{
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"A thunderous boom of destruction is heard in the distance.");
			Log.sysOut("Rooms",mob.ID()+" destroyed area "+areaName+".");
		}
	}


	public void clearTheRoom(Room room)
	{
		for(int m=room.numInhabitants()-1;m>=0;m--)
		{
			MOB mob2=room.fetchInhabitant(m);
			if((mob2!=null)&&(CoffeeUtensils.isEligibleMonster(mob2)))
			{
				if(mob2.getStartRoom()==room)
					mob2.destroy();
				else
				if(mob2.getStartRoom()!=null)
					mob2.getStartRoom().bringMobHere(mob2,true);
			}
		}
		for(int i=room.numItems()-1;i>=0;i--)
		{
			Item item=room.fetchItem(i);
			if(item!=null)
				room.delItem(item);
		}
		ExternalPlay.clearDebri(room,0);
	}


	public void clearDebriAndRestart(Room room, int taskCode)
	{
		ExternalPlay.clearDebri(room,0);
		if(taskCode<2)
		{
			ExternalPlay.DBUpdateItems(room);
			room.startItemRejuv();
		}
		if((taskCode==0)||(taskCode==2))
			ExternalPlay.DBUpdateMOBs(room);
	}
}
