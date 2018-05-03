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

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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

public class Link extends At
{
	public Link(){}

	private final String[] access=I(new String[]{"LINK"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));

		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is LINK [ROOM ID] [DIRECTION]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}
		final String dirStr=commands.get(commands.size()-1);
		commands.remove(commands.size()-1);
		final int direction=CMLib.directions().getGoodDirectionCode(dirStr);
		if(direction<0)
		{
			mob.tell(L("You have failed to specify a direction.  Try @x1.\n\r",Directions.LETTERS()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}

		Room thisRoom=null;
		final String roomID=CMParms.combine(commands,1);
		thisRoom = mob.location().getArea().getRoom(roomID);
		if(thisRoom != null)
			thisRoom=CMLib.map().getRoom(roomID);
		if(thisRoom==null)
		{
			thisRoom=CMLib.map().findWorldRoomLiberally(mob,roomID,"R",100,120000);
			if(thisRoom==null)
			{
				mob.tell(L("Room \"@x1\" is unknown.  Try again.",roomID));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return false;
			}
		}
		exitifyNewPortal(mob,thisRoom,direction);
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);

		mob.location().recoverRoomStats();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly a portal opens up in the landscape.\n\r"));
		Log.sysOut("Link",mob.Name()+" linked "+CMLib.map().getExtendedRoomID(mob.location())+" to room "+CMLib.map().getExtendedRoomID(thisRoom)+".");
		return false;
	}

	protected void exitifyNewPortal(MOB mob, Room room, int direction)
	{
		Room opRoom=mob.location().rawDoors()[direction];
		if((opRoom!=null)&&(opRoom.roomID().length()==0))
			opRoom=null;
		Room reverseRoom=null;
		final int opDir=Directions.getOpDirectionCode(direction);
		if(opRoom!=null)
			reverseRoom=opRoom.rawDoors()[opDir];

		if((reverseRoom!=null)
		&&((reverseRoom==mob.location())||(reverseRoom==mob.location().getGridParent())))
			mob.tell(L("Opposite room already exists and heads this way.  One-way link created."));

		if(opRoom!=null)
			mob.location().rawDoors()[direction]=null;

		GridLocale.CrossExit CE=null;
		final GridLocale hereGL=(mob.location().getGridParent()!=null)?mob.location().getGridParent():null;
		final int hereX=(hereGL!=null)?hereGL.getGridChildX(mob.location()):-1;
		final int hereY=(hereGL!=null)?hereGL.getGridChildY(mob.location()):-1;
		final GridLocale thereGL=(room.getGridParent()!=null)?room.getGridParent():null;
		final int thereX=(thereGL!=null)?thereGL.getGridChildX(room):-1;
		final int thereY=(thereGL!=null)?thereGL.getGridChildY(room):-1;
		if(hereGL!=null)
		{
			for(final Iterator<GridLocale.CrossExit> hereIter=hereGL.outerExits();hereIter.hasNext();)
			{
				CE=hereIter.next();
				if((CE.out)
				&&(CE.dir==direction)
				&&(CE.x==hereX)&&(CE.y==hereY))
				   hereGL.delOuterExit(CE);
			}
			CE=GridLocale.CrossExit.make(hereX,hereY,direction,CMLib.map().getExtendedRoomID(room),true);
			hereGL.addOuterExit(CE);
		}

		if(thereGL!=null)
			mob.location().rawDoors()[direction]=thereGL;
		else
			mob.location().rawDoors()[direction]=room;

		Exit thisExit=mob.location().getRawExit(direction);
		if(thisExit==null)
		{
			thisExit=CMClass.getExit("StdOpenDoorway");
			mob.location().setRawExit(direction,thisExit);
		}
		if(thereGL!=null)
		{
			for(final Iterator<GridLocale.CrossExit> thereIter=thereGL.outerExits();thereIter.hasNext();)
			{
				CE=thereIter.next();
				if((!CE.out)
				&&(CE.dir==direction)
				&&(CE.destRoomID.equals(CMLib.map().getExtendedRoomID(mob.location()))))
					thereGL.delOuterExit(CE);
			}
			CE=GridLocale.CrossExit.make(thereX,thereY,direction,CMLib.map().getExtendedRoomID(mob.location()),false);
			thereGL.addOuterExit(CE);

			if((room.rawDoors()[opDir]==null)
			||(thereGL==room.rawDoors()[opDir])
			||(thereGL.isMyGridChild(room.rawDoors()[opDir])))
			{
				for(final Iterator<GridLocale.CrossExit> thereIter=thereGL.outerExits();thereIter.hasNext();)
				{
					CE=thereIter.next();
					if((CE.out)
					&&(CE.dir==opDir)
					&&(CE.x==thereX)&&(CE.y==thereY))
					   thereGL.delOuterExit(CE);
				}
				CE=GridLocale.CrossExit.make(thereX,thereY,opDir,CMLib.map().getExtendedRoomID(mob.location()),true);
				thereGL.addOuterExit(CE);
				if(hereGL!=null)
				{
					room.rawDoors()[opDir]=hereGL;
					for(final Iterator<GridLocale.CrossExit> hereIter=hereGL.outerExits();hereIter.hasNext();)
					{
						CE=hereIter.next();
						if((!CE.out)
						&&(CE.dir==opDir)
						&&(CE.destRoomID.equals(CMLib.map().getExtendedRoomID(room))))
						   hereGL.delOuterExit(CE);
					}
					CE=GridLocale.CrossExit.make(hereX,hereY,opDir,CMLib.map().getExtendedRoomID(room),false);
					hereGL.addOuterExit(CE);
				}
				else
					room.rawDoors()[opDir]=mob.location();
				room.setRawExit(opDir,thisExit);
			}
		}
		else
		if(room.rawDoors()[opDir]==null)
		{
			if(hereGL!=null)
			{
				room.rawDoors()[opDir]=hereGL;
				for(final Iterator<GridLocale.CrossExit> hereIter=hereGL.outerExits();hereIter.hasNext();)
				{
					CE=hereIter.next();
					if((!CE.out)
					&&(CE.dir==opDir)
					&&(CE.destRoomID.equals(room.roomID())))
						hereGL.delOuterExit(CE);
				}
				CE=GridLocale.CrossExit.make(hereX,hereY,opDir,CMLib.map().getExtendedRoomID(room),false);
				hereGL.addOuterExit(CE);
			}
			else
				room.rawDoors()[opDir]=mob.location();
			room.setRawExit(opDir,thisExit);
		}
		if(hereGL!=null)
			CMLib.database().DBUpdateExits(hereGL);
		else
			CMLib.database().DBUpdateExits(mob.location());
		if(thereGL!=null)
			CMLib.database().DBUpdateExits(thereGL);
		else
			CMLib.database().DBUpdateExits(room);
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS);
	}

}
