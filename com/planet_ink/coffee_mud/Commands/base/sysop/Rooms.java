package com.planet_ink.coffee_mud.Commands.base.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Rooms
{
	private void exitifyNewPortal(MOB mob, Room room, int direction)
	{

		Room opRoom=mob.location().rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.ID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[Directions.getOpDirectionCode(direction)];

		if((reverseRoom!=null)&&(reverseRoom==mob.location()))
			mob.tell("Opposite room already exists and heads this way.  One-way link created.");

		if(opRoom!=null)
			mob.location().rawDoors()[direction]=null;

		mob.location().rawDoors()[direction]=room;
		Exit thisExit=mob.location().rawExits()[direction];
		if(thisExit==null)
		{
			thisExit=CMClass.getExit("StdOpenDoorway");
			mob.location().rawExits()[direction]=thisExit;
		}
		ExternalPlay.DBUpdateExits(mob.location());

		if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
		{
			room.rawDoors()[Directions.getOpDirectionCode(direction)]=mob.location();
			room.rawExits()[Directions.getOpDirectionCode(direction)]=thisExit;
			ExternalPlay.DBUpdateExits(room);
		}
	}

	public void create(MOB mob, Vector commands)
	{
		if(mob.location().ID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
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
			thisRoom.setArea(mob.location().getArea());
			thisRoom.setID(new Reset().getOpenRoomID(mob.location().getArea().name()));
			thisRoom.setDisplayText(CMClass.className(thisRoom)+"-"+thisRoom.ID());
			thisRoom.setDescription("");
			ExternalPlay.DBCreateRoom(thisRoom,Locale);
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
		if(mob.location().ID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
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

	private void flunkCmd2(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY AREA [NAME, DESCRIPTION, CLIMATE, AFFECTS, BEHAVIORS, ADDSUB, DELSUB] [TEXT]\n\r");
		mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}
	public void modifyArea(MOB mob, Vector commands)
		throws Exception
	{
		if(mob.location()==null) return;
		if(mob.location().getArea()==null) return;
		Area myArea=mob.location().getArea();

		String oldName=myArea.name();
		Resources.removeResource("HELP_"+myArea.name().toUpperCase());
		if(commands.size()==2)
		{
			new Generic().genName(mob,myArea);
			new Generic().genDescription(mob,myArea);
			new Generic().genClimateType(mob,myArea);
			new Generic().genSubOps(mob,myArea);
			new Generic().genBehaviors(mob,myArea);
			new Generic().genAffects(mob,myArea);
		}
		else
		{
			if(commands.size()<3) { flunkCmd1(mob); return;}

			String command=((String)commands.elementAt(2)).toUpperCase();
			String restStr="";
			if(commands.size()>=3)
				restStr=Util.combine(commands,3);

			if(command.equalsIgnoreCase("NAME"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				mob.location().setName(restStr);
			}
			else
			if(command.equalsIgnoreCase("DESC"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				mob.location().setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase("CLIMATE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				int newClimate=0;
				for(int i=0;i<restStr.length();i++)
					switch(Character.toUpperCase(restStr.charAt(i)))
					{
					case 'R':
						newClimate=newClimate|Area.CLIMASK_WET;
						break;
					case 'H':
						newClimate=newClimate|Area.CLIMASK_HOT;
						break;
					case 'C':
						newClimate=newClimate|Area.CLIMASK_COLD;
						break;
					case 'W':
						newClimate=newClimate|Area.CLIMATE_WINDY;
						break;
					case 'D':
						newClimate=newClimate|Area.CLIMATE_WINDY;
						break;
					case 'N':
						// do nothing
						break;
					default:
						mob.tell("Invalid CLIMATE code: '"+restStr.charAt(i)+"'.  Valid codes include: R)AINY, H)OT, C)OLD, D)RY, W)INDY, N)ORMAL.\n\r");
						mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
						return;
					}
				myArea.setClimateType(newClimate);
			}
			else
			if(command.equalsIgnoreCase("ADDSUB"))
			{
				if((commands.size()<4)||(!ExternalPlay.DBUserSearch(null,restStr)))
				{
					mob.tell("Unknown or invalid username given.\n\r");
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.addSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("DELSUB"))
			{
				if((commands.size()<4)||(!myArea.amISubOp(restStr)))
				{
					mob.tell("Unknown or invalid subOp name given.  Valid names are: "+myArea.getSubOpList()+".\n\r");
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.delSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("AFFECTS"))
			{
				new Generic().genAffects(mob,mob.location());
				myArea.recoverEnvStats();
			}
			else
			if(command.equalsIgnoreCase("BEHAVIORS"))
			{
				new Generic().genBehaviors(mob,mob.location());
				myArea.recoverEnvStats();
			}
			else
			{
				flunkCmd2(mob);
				return;
			}
			Log.sysOut("Rooms",mob.ID()+" modified area "+myArea.name()+".");
		}

		if((!myArea.name().equals(oldName))&&(!mob.isMonster()))
		{
			if(mob.session().confirm("Is changing the name of this area really necessary (y/N)?","N"))
			{
				Vector areaMap=myArea.getMyMap();
				for(int r=0;r<areaMap.size();r++)
					ExternalPlay.DBUpdateRoom((Room)areaMap.elementAt(r));
			}
			else
				myArea.setName(oldName);
		}
		else
			myArea.setName(oldName);
		mob.location().show(mob,null,Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
		ExternalPlay.DBUpdateArea(myArea);
	}
	public void modify(MOB mob, Vector commands)
		throws Exception
	{
		if(mob.location().ID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
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
			Area A=CMMap.getArea(restStr);
			String checkID=new Reset().getOpenRoomID(restStr);
			if(A==null)
			{
				if(!mob.isMonster())
				{
					if(mob.session().confirm("\n\rThis command will create a BRAND NEW AREA\n\r with Area code '"+restStr+"'.  Are you SURE (y/N)?","N"))
					{
						String areaType="";
						int tries=0;
						while((areaType.length()==0)&&((++tries)<10))
						{
							areaType=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
							if(CMClass.getAreaType(areaType)==null)
							{
								mob.session().println("Invalid area type! Valid ones are:");
								mob.session().println(new Lister().reallyList(CMClass.areaTypes,-1,null).toString());
								areaType="";
							}
						}
						if(areaType.length()==0) areaType="StdArea";
						A=ExternalPlay.DBCreateArea(restStr,areaType);
						mob.location().setArea(A);
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
								Room thatRoom=thisRoom.rawDoors()[dir];
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
				mob.location().setArea(A);
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
			flunkCmd1(mob);
			return;
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
				Room thatRoom=thisRoom.rawDoors()[dir];
				if(thatRoom==deadRoom)
				{
					thisRoom.rawDoors()[dir]=null;
					changes=true;
					if((thisRoom.rawExits()[dir]!=null)&&(thisRoom.rawExits()[dir].isGeneric()))
					{
						Exit GE=(Exit)thisRoom.rawExits()[dir];
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
		Resources.removeResource("areasListHTML");
		
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid();
		ExternalPlay.DBDeleteRoom(deadRoom);
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
			mob.location().rawDoors()[direction]=null;
			mob.location().rawExits()[direction]=null;
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
		if(CMMap.getArea(areaName)==null)
		{
			mob.tell("There is no such area as '"+areaName+"'");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
			return;
		}


		if(!confirmed);
		if(mob.session().confirm("Area: \""+areaName+"\", OBLITERATE IT???","N"))
		{
			if(mob.location().getArea().name().equalsIgnoreCase(areaName))
			{
				mob.tell("You dip!  You are IN that area!  Leave it first...");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
				return;
			}
			else
				confirmed=true;
		}

		Room foundOne=mob.location();
		while(foundOne!=null)
		{
			foundOne=null;
			for(Enumeration e=CMMap.map.elements();e.hasMoreElements();)
			{
				Room r=(Room)e.nextElement();
				if(r.getArea().name().equalsIgnoreCase(areaName))
				{
					foundOne=r;
					break;
				}
			}
			if(foundOne!=null)
				new Rooms().obliterateRoom(mob,foundOne);
		}

		Area A=CMMap.getArea(areaName);
		ExternalPlay.DBDeleteArea(A);
		CMMap.AREAS.removeElement(A);

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
