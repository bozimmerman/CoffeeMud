package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2003-2018 Bo Zimmerman

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

public class Prop_RoomWatch extends Property
{
	@Override
	public String ID()
	{
		return "Prop_RoomWatch";
	}

	@Override
	public String name()
	{
		return "Different Room Can Watch";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_ITEMS;
	}

	protected Vector<Room> newRooms=null;
	protected String prefix=null;

	@Override
	public String accountForYourself()
	{
		return "Different View of "+text();
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		newRooms=null;
		prefix=null;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(newRooms==null)
		{
			final List<String> V=CMParms.parseSemicolons(text(),true);
			newRooms=new Vector<Room>();
			for(int v=0;v<V.size();v++)
			{
				final String roomID=V.get(v);
				final int x=roomID.indexOf('=');
				if(x>0)
				{
					final String var=roomID.substring(0,x).trim().toLowerCase();
					if(var.equalsIgnoreCase("prefix"))
					{
						prefix=CMStrings.trimQuotes(roomID.substring(x+1).trim());
						continue;
					}
				}
				final Room R=CMLib.map().getRoom(roomID);
				if(R!=null)
					newRooms.addElement(R);
			}
		}

		if((affected!=null)
		&&(msg.othersCode()!=CMMsg.NO_EFFECT)
		&&(msg.othersMessage()!=null)
		&&(msg.othersMessage().length()>0))
		{
			final Room thisRoom=CMLib.map().roomLocation(affected);
			for(int r=0;r<newRooms.size();r++)
			{
				final Room R=newRooms.elementAt(r);
				if((R!=null)&&(R.fetchEffect(ID())==null)&&(R!=thisRoom))
				{
					final CMMsg msg2=CMClass.getMsg(msg.source(),msg.target(),msg.tool(),
								  CMMsg.NO_EFFECT,null,
								  CMMsg.NO_EFFECT,null,
								  CMMsg.MSG_OK_VISUAL,(prefix!=null)?(prefix+msg.othersMessage()):msg.othersMessage());
					if(R.okMessage(msg.source(),msg2))
					for(int i=0;i<R.numInhabitants();i++)
					{
						final MOB M=R.fetchInhabitant(i);
						if((M!=null)
						&&(CMLib.flags().canSee(M))
						&&(CMLib.flags().canBeSeenBy(R,M))
						&&(CMLib.flags().canBeSeenBy(msg2.source(),M)))
							M.executeMsg(M,msg2);
					}
				}
			}
		}
	}
}
