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
   Copyright 2003-2021 Bo Zimmerman

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
public class Prop_RoomView extends Property
{
	@Override
	public String ID()
	{
		return "Prop_RoomView";
	}

	@Override
	public String name()
	{
		return "Different Room View";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_ITEMS|Ability.CAN_EXITS;
	}

	protected Room newRoom=null;
	protected boolean longlook = false;
	protected String viewedRoomID = "";

	@Override
	public void setMiscText(final String text)
	{
		final int x=text.indexOf(';');
		longlook=false;
		if(x>=0)
		{
			final String parms=text.substring(0,x);
			for(final String str : CMParms.parse(parms))
				if(str.equalsIgnoreCase("LONGLOOK"))
					longlook=true;
			viewedRoomID=text.substring(x+1).trim();
		}
		else
			viewedRoomID=text.trim();
	}

	@Override
	public String accountForYourself()
	{
		return "Different View of "+viewedRoomID;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((newRoom==null)
		||(newRoom.amDestroyed())
		||(!CMLib.map().getExtendedRoomID(newRoom).equalsIgnoreCase(viewedRoomID)))
			newRoom=CMLib.map().getRoom(viewedRoomID);
		if(newRoom==null)
			return super.okMessage(myHost,msg);

		if((affected!=null)
		&&((affected instanceof Room)||(affected instanceof Exit)||(affected instanceof Item))
		&&(msg.amITarget(affected))
		&&(newRoom.fetchEffect(ID())==null))
		{
			if(longlook && (msg.targetMinor()==CMMsg.TYP_EXAMINE))
			{
				msg.addTrailerRunnable(new Runnable()
				{
					final Room R=newRoom;
					final CMMsg mmsg=msg;
					@Override
					public void run()
					{
						if(CMLib.flags().canBeSeenBy(R, mmsg.source()) && (mmsg.source().session()!=null))
							mmsg.source().session().print(L("In @x1 you can see:",R.displayText(mmsg.source())));
						final CMMsg msg2=CMClass.getMsg(mmsg.source(), R, mmsg.tool(), mmsg.sourceCode(), null, mmsg.targetCode(), null, mmsg.othersCode(), null);
						if((mmsg.source().isAttributeSet(MOB.Attrib.AUTOEXITS))
						&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
							msg2.addTrailerMsg(CMClass.getMsg(mmsg.source(),R,null,CMMsg.MSG_LOOK_EXITS,null));
						if(R.okMessage(mmsg.source(), mmsg))
							R.send(mmsg.source(),msg2);
					}
				});
			}
			else
			{
				if((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
				{
					final CMMsg msg2=CMClass.getMsg(msg.source(),newRoom,msg.tool(),
								  msg.sourceCode(),msg.sourceMessage(),
								  msg.targetCode(),msg.targetMessage(),
								  msg.othersCode(),msg.othersMessage());
					if(newRoom.okMessage(msg.source(),msg2))
					{
						newRoom.executeMsg(msg.source(),msg2);
						return false;
					}
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

}
