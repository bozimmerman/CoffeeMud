package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;

public class Rooms
{
	private Rooms(){}

	private static void exitifyNewPortal(MOB mob, Room room, int direction)
	{
		Room opRoom=mob.location().rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
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

	public static void create(MOB mob, Vector commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("Sorry Charlie! Not your room!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U, D, or V.\n\r");
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
			thisRoom.setArea(mob.location().getArea());
			thisRoom.setRoomID(Reset.getOpenRoomID(mob.location().getArea().Name()));
			thisRoom.setDisplayText(CMClass.className(thisRoom)+"-"+thisRoom.roomID());
			thisRoom.setDescription("");
			ExternalPlay.DBCreateRoom(thisRoom,Locale);
			CMMap.addRoom(thisRoom);
		}

		if(thisRoom==null)
		{
			mob.tell("You have  to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		exitifyNewPortal(mob,thisRoom,direction);

		mob.location().recoverRoomStats();
		thisRoom.recoverRoomStats();
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);
		mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly a block of earth falls from the sky.\n\r");
		Log.sysOut("Rooms",mob.Name()+" created room "+thisRoom.roomID()+".");
	}

	public static void link(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is LINK [ROOM ID] [DIRECTION]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		int direction=Directions.getGoodDirectionCode(Util.combine(commands,2));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U, D, or V.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("Sorry Charlie! Not your room!");
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
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);

		mob.location().recoverRoomStats();
		mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly a portal opens up in the landscape.\n\r");
		Log.sysOut("Rooms",mob.Name()+" linked to room "+thisRoom.roomID()+".");
	}

	private static void flunkCmd1(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION, AFFECTS, BEHAVIORS, CLASS, XGRID, YGRID] [TEXT]\n\r");
		mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}

	private static void flunkCmd2(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY AREA [NAME, DESCRIPTION, CLIMATE, FILE, AFFECTS, BEHAVIORS, ADDSUB, DELSUB] [TEXT]\n\r");
		mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}
	public static void modifyArea(MOB mob, Vector commands)
		throws Exception
	{
		if(mob.location()==null) return;
		if(mob.location().getArea()==null) return;
		Area myArea=mob.location().getArea();
		
		if((!mob.isASysOp(null))
		&&(!myArea.amISubOp(mob.Name())))
		{
			mob.tell("Sorry Charlie! Not your area!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String oldName=myArea.Name();
		Resources.removeResource("HELP_"+myArea.Name().toUpperCase());
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
				showFlag=-999;
			boolean ok=false;
			while(!ok)
			{
				int showNumber=0;
				Generic.genName(mob,myArea,++showNumber,showFlag);
				Generic.genDescription(mob,myArea,++showNumber,showFlag);
				Generic.genTechLevel(mob,myArea,++showNumber,showFlag);
				Generic.genClimateType(mob,myArea,++showNumber,showFlag);
				Generic.genArchivePath(mob,myArea,++showNumber,showFlag);
				Generic.genSubOps(mob,myArea,++showNumber,showFlag);
				Generic.genBehaviors(mob,myArea,++showNumber,showFlag);
				Generic.genAffects(mob,myArea,++showNumber,showFlag);
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
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
				myArea.setName(restStr);
			}
			else
			if(command.equalsIgnoreCase("DESC"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase("FILE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setArchivePath(restStr);
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
				Generic.genAffects(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			if(command.equalsIgnoreCase("BEHAVIORS"))
			{
				Generic.genBehaviors(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			{
				flunkCmd2(mob);
				return;
			}
			Log.sysOut("Rooms",mob.Name()+" modified area "+myArea.Name()+".");
		}

		if((!myArea.Name().equals(oldName))&&(!mob.isMonster()))
		{
			if(mob.session().confirm("Is changing the name of this area really necessary (y/N)?","N"))
			{
				for(Enumeration r=myArea.getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					if((R.roomID().startsWith(oldName+"#"))
					&&(CMMap.getRoom(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1))==null))
					{
						String oldID=R.roomID();
						R.setRoomID(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1));
						ExternalPlay.DBReCreate(R,oldID);
					}
					else
						ExternalPlay.DBUpdateRoom(R);
				}
				myArea.clearMap();
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					boolean doIt=false;
					for(int d=0;d<R.rawDoors().length;d++)
					{
						Room R2=(Room)R.rawDoors()[d];
						if((R2!=null)&&(R2.getArea()==myArea))
						{ doIt=true; break;}
					}
					if(doIt)
						ExternalPlay.DBUpdateExits(R);
				}
			}
			else
				myArea.setName(oldName);
		}
		else
			myArea.setName(oldName);
		myArea.recoverEnvStats();
		mob.location().recoverRoomStats();
		mob.location().showHappens(Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
		ExternalPlay.DBUpdateArea(myArea);
	}
	public static void modify(MOB mob, Vector commands)
		throws Exception
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("Sorry Charlie! Not your room!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()==2)
		{
			int showFlag=-1;
			if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
				showFlag=-999;
			boolean ok=false;
			while(!ok)
			{
				int showNumber=0;
				Generic.genRoomType(mob,mob.location(),++showNumber,showFlag);
				Generic.genDisplayText(mob,mob.location(),++showNumber,showFlag);
				Generic.genDescription(mob,mob.location(),++showNumber,showFlag);
				if(mob.location() instanceof GridLocale)
				{
					Generic.genGridLocaleX(mob,(GridLocale)mob.location(),++showNumber,showFlag);
					Generic.genGridLocaleY(mob,(GridLocale)mob.location(),++showNumber,showFlag);
					((GridLocale)mob.location()).buildGrid();
				}
				Generic.genBehaviors(mob,mob.location(),++showNumber,showFlag);
				Generic.genAffects(mob,mob.location(),++showNumber,showFlag);
				if(showFlag<-900){ ok=true; break;}
				if(showFlag>0){ showFlag=-1; continue;}
				showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
				if(showFlag<=0)
				{
					showFlag=-1;
					ok=true;
				}
			}
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
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
			String checkID=Reset.getOpenRoomID(restStr);
			if(A==null)
			{
				if((!mob.isMonster())||(!mob.isASysOp(null)))
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
								mob.session().println(Lister.reallyList(CMClass.areaTypes(),-1,null).toString());
								areaType="";
							}
						}
						if(areaType.length()==0) areaType="StdArea";
						A=ExternalPlay.DBCreateArea(restStr,areaType);
						mob.location().setArea(A);
						CMMap.delRoom(mob.location());
						String oldID=mob.location().roomID();
						mob.location().setRoomID(checkID);

						ExternalPlay.DBReCreate(mob.location(),oldID);

						CMMap.addRoom(mob.location());
						for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
							{
								Room thatRoom=R.rawDoors()[dir];
								if(thatRoom==mob.location())
								{
									ExternalPlay.DBUpdateExits(R);
									break;
								}
							}
						}
					}
					mob.location().showHappens(Affect.MSG_OK_ACTION,"This entire area twitches.\n\r");
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
				mob.location().showHappens(Affect.MSG_OK_ACTION,"This area twitches.\n\r");
			}
		}
		else
		if(command.equalsIgnoreCase("NAME"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDisplayText(restStr);
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Room newRoom=CMClass.getLocale(restStr);
			if(newRoom==null)
			{
				mob.tell("'"+restStr+"' is not a valid room locale.");
				return;
			}
			Generic.changeRoomType(mob.location(),newRoom);
			mob.location().showHappens(Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("XGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setXSize(Util.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("YGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setYSize(Util.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDescription(restStr);
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("AFFECTS"))
		{
			Generic.genAffects(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("BEHAVIORS"))
		{
			Generic.genBehaviors(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			ExternalPlay.DBUpdateRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		{
			flunkCmd1(mob);
			return;
		}
		mob.location().recoverRoomStats();
		Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
	}

	public static void obliterateRoom(Room deadRoom)
	{
		for(int a=deadRoom.numAffects()-1;a>=0;a--)
		{
			Ability A=deadRoom.fetchAffect(a);
			if(A!=null)
			{
				A.unInvoke();
				deadRoom.delAffect(A);
			}
		}
		CMMap.delRoom(deadRoom);
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			boolean changes=false;
			for(int dir=0;dir<Directions.NUM_DIRECTIONS;dir++)
			{
				Room thatRoom=R.rawDoors()[dir];
				if(thatRoom==deadRoom)
				{
					R.rawDoors()[dir]=null;
					changes=true;
					if((R.rawExits()[dir]!=null)&&(R.rawExits()[dir].isGeneric()))
					{
						Exit GE=(Exit)R.rawExits()[dir];
						GE.setTemporaryDoorLink(deadRoom.roomID());
					}
				}
			}
			if(changes)
				ExternalPlay.DBUpdateExits(R);
		}
		for(int mb=deadRoom.numInhabitants()-1;mb>=0;mb--)
		{
			MOB mob2=deadRoom.fetchInhabitant(mb);
			if(mob2!=null)
			{
				if((mob2.getStartRoom()!=deadRoom)&&(mob2.getStartRoom()!=null)&&(CMMap.getRoom(mob2.getStartRoom().roomID())!=null))
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
				item2.destroy();
			}
		}
		clearTheRoom(deadRoom);
		deadRoom.destroyRoom();
		if(deadRoom instanceof GridLocale)
			((GridLocale)deadRoom).clearGrid();
		ExternalPlay.DBDeleteRoom(deadRoom);
	}

	public static void destroy(MOB mob, Vector commands)
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
		int direction=Directions.getGoodDirectionCode(roomdir);
		Room deadRoom=null;
		if(!thecmd.equalsIgnoreCase("UNLINK"))
			deadRoom=CMMap.getRoom(roomdir);
		if((deadRoom==null)&&(direction<0))
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell("You have failed to specify a direction.  Try (N, S, E, W, U, D, or V).\n\r");
			else
				mob.tell("You have failed to specify a direction.  Try a VALID ROOM ID, or (N, S, E, W, U, D, or V).\n\r");
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
			if(!mob.isASysOp(deadRoom))
			{
				mob.tell("Sorry Charlie! Not your room!");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
			if(mob.location()==deadRoom)
			{
				mob.tell("You dip! You have to leave this room first!");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}

			if(!confirmed)
				if(!mob.session().confirm("You are fixing permanantly destroy Room \""+deadRoom.roomID()+"\".  Are you ABSOLUTELY SURE (y/N)","N")) return;
			obliterateRoom(deadRoom);
			mob.tell("The sound of massive destruction rings in your ears.");
			mob.location().showOthers(mob,null,Affect.MSG_NOISE,"The sound of massive destruction rings in your ears.");
			Log.sysOut("Rooms",mob.Name()+" destroyed room "+deadRoom.roomID()+".");
		}
		else
		{
			mob.location().rawDoors()[direction]=null;
			mob.location().rawExits()[direction]=null;
			ExternalPlay.DBUpdateExits(mob.location());
			mob.location().getArea().fillInAreaRoom(mob.location());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"A wall of inhibition falls "+Directions.getInDirectionName(direction)+".");
			Log.sysOut("Rooms",mob.Name()+" unlinked direction "+Directions.getDirectionName(direction)+" from room "+mob.location().roomID()+".");
		}
	}

	public static void obliterateArea(String areaName)
	{
		Room foundOne=CMMap.getFirstRoom();
		while(foundOne!=null)
		{
			foundOne=null;
			for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.getArea().Name().equalsIgnoreCase(areaName))
				{
					foundOne=R;
					break;
				}
			}
			if(foundOne!=null)
				obliterateRoom(foundOne);
		}

		Area A=CMMap.getArea(areaName);
		ExternalPlay.DBDeleteArea(A);
		CMMap.delArea(A);
	}

	public static void destroyArea(MOB mob, Vector commands)
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
		Area A=CMMap.getArea(areaName);
		if((!mob.isASysOp(null))
		&&(!A.amISubOp(mob.Name())))
		{
			mob.tell("Sorry Charlie! Not your area!");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}


		if(!confirmed);
		if(mob.session().confirm("Area: \""+areaName+"\", OBLITERATE IT???","N"))
		{
			if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
			{
				mob.tell("You dip!  You are IN that area!  Leave it first...");
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
				return;
			}
			else
				confirmed=true;
		}
		obliterateArea(areaName);
		if(confirmed)
		{
			mob.location().showHappens(Affect.MSG_OK_ACTION,"A thunderous boom of destruction is heard in the distance.");
			Log.sysOut("Rooms",mob.Name()+" destroyed area "+areaName+".");
		}
	}


	public static void clearTheRoom(Room room)
	{
		for(int m=room.numInhabitants()-1;m>=0;m--)
		{
			MOB mob2=room.fetchInhabitant(m);
			if((mob2!=null)&&(mob2.isEligibleMonster()))
			{
				if(mob2.getStartRoom()==room)
					mob2.destroy();
				else
				if(mob2.getStartRoom()!=null)
					mob2.getStartRoom().bringMobHere(mob2,true);
			}
		}
		while(room.numItems()>0)
		{
			Item I=room.fetchItem(0);
			I.destroy();
		}
		ExternalPlay.clearDebri(room,0);
	}


	public static void clearDebriAndRestart(Room room, int taskCode)
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
