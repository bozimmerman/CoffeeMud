package com.planet_ink.coffee_mud.Behaviors;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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

public class DelayedTransporter extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "DelayedTransporter";
	}

	protected Hashtable<String,Integer> transportees=new Hashtable<String,Integer>();
	protected Vector<String> destRoomNames=new Vector<String>();

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS;
	}

	@Override
	public String accountForYourself()
	{
		return "away whisking";
	}

	public DelayedTransporter()
	{
		super();
		minTicks=5;maxTicks=5;chance=100;
		tickReset();
	}

	@Override
	public void setParms(String newParms)
	{
		String myParms=newParms;
		int x=myParms.indexOf(';');
		if(x>0)
		{
			final String parmText=myParms.substring(0,x);
			myParms=myParms.substring(x+1);
			super.setParms(parmText);
		}
		destRoomNames=new Vector<String>();
		transportees=new Hashtable<String,Integer>();
		while(myParms.length()>0)
		{
			String thisRoom=myParms;
			x=myParms.indexOf(';');
			if(x>0)
			{
				thisRoom=myParms.substring(0,x);
				myParms=myParms.substring(x+1);
			}
			else
				myParms="";

			if(CMLib.map().getRoom(thisRoom)!=null)
				destRoomNames.addElement(thisRoom);
		}
		parms=newParms;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		final Room room=this.getBehaversRoom(ticking);
		if((room!=null)&&(destRoomNames!=null)&&(destRoomNames.size()>0))
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB inhab=room.fetchInhabitant(i);
			if(inhab!=null)
			{
				Integer I=transportees.get(inhab.Name());
				if(I==null)
				{
					I=Integer.valueOf(0);
					transportees.put(inhab.Name(),I);
				}
				boolean gone=false;
				if(I.intValue()>=minTicks)
				{
					if((CMLib.dice().rollPercentage()<chance)||(I.intValue()>maxTicks))
					{
						final String roomName=destRoomNames.elementAt(CMLib.dice().roll(1,destRoomNames.size(),-1));
						final Room otherRoom=CMLib.map().getRoom(roomName);
						if(otherRoom==null)
							inhab.tell(L("You are whisked nowhere at all, since '@x1' is nowhere to be found.",roomName));
						else
							otherRoom.bringMobHere(inhab,true);
						transportees.remove(inhab.Name());
						gone=true;
					}
				}
				if(!gone)
				{
					I=Integer.valueOf(I.intValue()+1);
					transportees.put(inhab.Name(),I);
				}
			}
		}
		return true;
	}
}
