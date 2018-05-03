package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2006-2018 Bo Zimmerman

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

public class SoundEcho extends StdAbility
{
	@Override
	public String ID()
	{
		return "SoundEcho";
	}

	private final static String	localizedName	= CMLib.lang().L("Sound Echo");

	@Override
	public String name()
	{
		return localizedName;
	}

	protected String	displayText	= "";

	@Override
	public String displayText()
	{
		return displayText;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS | CAN_AREAS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	public static MOB	bmob	= null;

	public synchronized MOB blindMOB()
	{
		if(bmob!=null)
			return bmob;
		bmob=CMClass.getMOB("StdMOB");
		if(bmob!=null)
		{
			bmob.setName(L("Someone"));
			bmob.basePhyStats().setSensesMask(PhyStats.CAN_NOT_SEE);
			bmob.recoverPhyStats();
		}
		return bmob;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((CMath.bset(msg.othersMajor(),CMMsg.MASK_SOUND))
		&&(CMath.bset(msg.sourceMajor(),CMMsg.MASK_SOUND))
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().indexOf("You hear an echo: ")<0)
		&&(msg.source().location()!=null))
		{
			synchronized(this)
			{
				int range=CMath.s_int(text());
				if(range==0)
					range=10;
				final Room sourceRoom=msg.source().location();
				String str=msg.othersMessage();
				str=CMLib.coffeeFilter().fullOutFilter(null,blindMOB(),msg.source(),msg.target(),msg.tool(),str,false);
				CMMsg echoMsg=(CMMsg)msg.copyOf();
				final Vector<Room> doneRooms=new Vector<Room>();
				if(echoMsg.sourceMessage()!=null)
					echoMsg.setSourceMessage("You hear an echo: "+CMLib.coffeeFilter().fullOutFilter(null,blindMOB(),msg.source(),msg.target(),msg.tool(),msg.sourceMessage(),false));
				if(echoMsg.targetMessage()!=null)
					echoMsg.setTargetMessage("You hear an echo: "+CMLib.coffeeFilter().fullOutFilter(null,blindMOB(),msg.source(),msg.target(),msg.tool(),msg.targetMessage(),false));
				if(echoMsg.othersMessage()!=null)
					echoMsg.setOthersMessage("You hear an echo: "+CMLib.coffeeFilter().fullOutFilter(null,blindMOB(),msg.source(),msg.target(),msg.tool(),msg.othersMessage(),false));
				msg.addTrailerMsg(echoMsg);
				echoMsg=CMClass.getMsg(msg.source(),msg.target(),msg.tool(),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,msg.othersCode(),str);
				final Vector<Room> rooms=new Vector<Room>();
				TrackingLibrary.TrackingFlags flags;
				flags = CMLib.tracking().newFlags()
						.plus(TrackingLibrary.TrackingFlag.OPENONLY)
						.plus(TrackingLibrary.TrackingFlag.AREAONLY);
				CMLib.tracking().getRadiantRooms(sourceRoom,rooms,flags,null,range/2,null);
				Room room=null;
				for(int v=0;v<rooms.size();v++)
				{
					room=rooms.elementAt(v);
					if((room!=sourceRoom)&&(!doneRooms.contains(room)))
					{
						doneRooms.add(room);
						if(CMLib.dice().rollPercentage()<50)
						{
							final int direction=CMLib.tracking().radiatesFromDir(room,rooms);
							echoMsg.setOthersMessage("You hear an echo coming from "+CMLib.directions().getFromCompassDirectionName(direction)+": "+str);
						}
						else
							echoMsg.setOthersMessage("You hear an echo coming from "+CMLib.directions().getFromCompassDirectionName(CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1))+": "+str);
						room.sendOthers(msg.source(),echoMsg);
					}
				}
				rooms.clear();
				CMLib.tracking().getRadiantRooms(sourceRoom,rooms,flags,null,range,null);
				for(int v=0;v<rooms.size();v++)
				{
					room=rooms.elementAt(v);
					if((room!=sourceRoom)&&(!doneRooms.contains(room)))
					{
						doneRooms.add(room);
						if(room.numInhabitants()>0)
						{
							if(CMLib.dice().rollPercentage()<50)
							{
								final int direction=CMLib.tracking().radiatesFromDir(room,rooms);
								echoMsg.setOthersMessage("You hear a faint echo coming from "+CMLib.directions().getFromCompassDirectionName(direction)+".");
							}
							else
								echoMsg.setOthersMessage("You hear a faint echo coming from "+CMLib.directions().getDirectionName(CMLib.dice().roll(1,Directions.NUM_DIRECTIONS(),-1))+".");
							room.sendOthers(msg.source(),echoMsg);
						}
					}
				}
			}
		}
	}
}
