package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Link extends StdCommand
{
	public Link(){}

	private String[] access={"LINK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
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
		Log.sysOut("Link",mob.Name()+" linked "+CMMap.getExtendedRoomID(mob.location())+" to room "+CMMap.getExtendedRoomID(thisRoom)+".");
		return false;
	}
	
	private void exitifyNewPortal(MOB mob, Room room, int direction)
	{
		Room opRoom=mob.location().rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		int opDir=Directions.getOpDirectionCode(direction);
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[opDir];

		if((reverseRoom!=null)
		&&((reverseRoom==mob.location())||(reverseRoom==mob.location().getGridParent())))
			mob.tell("Opposite room already exists and heads this way.  One-way link created.");

		if(opRoom!=null)
			mob.location().rawDoors()[direction]=null;
		
		CMMap.CrossExit CE=null;
		GridLocale hereGL=(mob.location().getGridParent()!=null)?mob.location().getGridParent():null;
		int hereX=(hereGL!=null)?hereGL.getChildX(mob.location()):-1;
		int hereY=(hereGL!=null)?hereGL.getChildY(mob.location()):-1;
		Vector hereSet=(hereGL!=null)?hereGL.outerExits():null;
		GridLocale thereGL=(room.getGridParent()!=null)?room.getGridParent():null;
		int thereX=(thereGL!=null)?thereGL.getChildX(room):-1;
		int thereY=(thereGL!=null)?thereGL.getChildY(room):-1;
		Vector thereSet=(thereGL!=null)?thereGL.outerExits():null;
		if(hereGL!=null)
		{
			for(int v=0;v<hereSet.size();v++)
			{
				CE=(CMMap.CrossExit)hereSet.elementAt(v);
				if((CE.out)
				&&(CE.dir==direction)
				&&(CE.x==hereX)&&(CE.y==hereY))
				   hereGL.delOuterExit(CE);
			}
			CE=CMMap.CrossExit.make(hereX,hereY,direction,CMMap.getExtendedRoomID(room),true);
			hereGL.addOuterExit(CE);
		}
		
		if(thereGL!=null)
			mob.location().rawDoors()[direction]=thereGL;
		else
			mob.location().rawDoors()[direction]=room;
		
		Exit thisExit=mob.location().rawExits()[direction];
		if(thisExit==null)
		{
			thisExit=CMClass.getExit("StdOpenDoorway");
			mob.location().rawExits()[direction]=thisExit;
		}
		if(thereGL!=null)
		{
			for(int v=0;v<thereSet.size();v++)
			{
				CE=(CMMap.CrossExit)thereSet.elementAt(v);
				if((!CE.out)
				&&(CE.dir==direction)
				&&(CE.destRoomID.equals(CMMap.getExtendedRoomID(mob.location()))))
				   thereGL.delOuterExit(CE);
			}
			CE=CMMap.CrossExit.make(thereX,thereY,direction,CMMap.getExtendedRoomID(mob.location()),false);
			thereGL.addOuterExit(CE);
			
			if((room.rawDoors()[opDir]==null)
			||(thereGL==room.rawDoors()[opDir])
			||(thereGL.isMyChild(room.rawDoors()[opDir])))
			{
				for(int v=0;v<thereSet.size();v++)
				{
					CE=(CMMap.CrossExit)thereSet.elementAt(v);
					if((CE.out)
					&&(CE.dir==opDir)
					&&(CE.x==thereX)&&(CE.y==thereY))
					   thereGL.delOuterExit(CE);
				}
				CE=CMMap.CrossExit.make(thereX,thereY,opDir,CMMap.getExtendedRoomID(mob.location()),true);
				thereGL.addOuterExit(CE);
				if(hereGL!=null)
				{
					room.rawDoors()[opDir]=hereGL;
					for(int v=0;v<hereSet.size();v++)
					{
						CE=(CMMap.CrossExit)hereSet.elementAt(v);
						if((!CE.out)
						&&(CE.dir==opDir)
						&&(CE.destRoomID.equals(CMMap.getExtendedRoomID(room))))
						   hereGL.delOuterExit(CE);
					}
					CE=CMMap.CrossExit.make(hereX,hereY,opDir,CMMap.getExtendedRoomID(room),false);
					hereGL.addOuterExit(CE);
				}
				else
					room.rawDoors()[opDir]=mob.location();
				room.rawExits()[opDir]=thisExit;
			}
		}
		else
		if(room.rawDoors()[opDir]==null)
		{
			if(hereGL!=null)
			{
				room.rawDoors()[opDir]=hereGL;
				for(int v=0;v<hereSet.size();v++)
				{
					CE=(CMMap.CrossExit)hereSet.elementAt(v);
					if((!CE.out)
					&&(CE.dir==opDir)
					&&(CE.destRoomID.equals(room.roomID())))
					   hereGL.delOuterExit(CE);
				}
				CE=CMMap.CrossExit.make(hereX,hereY,opDir,CMMap.getExtendedRoomID(room),false);
				hereGL.addOuterExit(CE);
			}
			else
				room.rawDoors()[opDir]=mob.location();
			room.rawExits()[opDir]=thisExit;
		}
		if(hereGL!=null)
			CMClass.DBEngine().DBUpdateExits(hereGL);
		else
			CMClass.DBEngine().DBUpdateExits(mob.location());
		if(thereGL!=null)
			CMClass.DBEngine().DBUpdateExits(thereGL);
		else
			CMClass.DBEngine().DBUpdateExits(room);
	}

	
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS");}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
