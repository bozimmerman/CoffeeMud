package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Link extends StdCommand
{
	public Link(){}

	private String[] access={"LINK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You lack the power to link rooms.\n\r");
			return false;
		}
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is LINK [ROOM ID] [DIRECTION]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		int direction=Directions.getGoodDirectionCode(Util.combine(commands,2));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U, D, or V.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("Sorry Charlie! Not your room!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		Room thisRoom=null;
		String RoomID=(String)commands.elementAt(1);
		thisRoom=(Room)CMMap.getRoom(RoomID);
		if(thisRoom==null)
		{
			mob.tell("Room \""+RoomID+"\" is unknown.  Try again.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		exitifyNewPortal(mob,thisRoom,direction);
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);

		mob.location().recoverRoomStats();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a portal opens up in the landscape.\n\r");
		Log.sysOut("Link",mob.Name()+" linked to room "+thisRoom.roomID()+".");
		return false;
	}
	
	private void exitifyNewPortal(MOB mob, Room room, int direction)
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
		CMClass.DBEngine().DBUpdateExits(mob.location());

		if(room.rawDoors()[Directions.getOpDirectionCode(direction)]==null)
		{
			room.rawDoors()[Directions.getOpDirectionCode(direction)]=mob.location();
			room.rawExits()[Directions.getOpDirectionCode(direction)]=thisExit;
			CMClass.DBEngine().DBUpdateExits(room);
		}
	}

	
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
