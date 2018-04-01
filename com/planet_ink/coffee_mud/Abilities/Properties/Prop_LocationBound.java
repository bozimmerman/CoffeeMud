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

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class Prop_LocationBound extends Property
{
	@Override
	public String ID()
	{
		return "Prop_LocationBound";
	}

	@Override
	public String name()
	{
		return "Leave the specified area, or room";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS|Ability.CAN_MOBS;
	}
	
	protected enum BoundToType
	{
		CURRENT_ROOM,
		CURRENT_AREA,
		SPECIFIC_ROOM,
		SPECIFIC_AREA
	}
	protected BoundToType	type = null;
	protected Environmental boundTo = null;
	
	protected BoundToType getBoundToType()
	{
		if(type != null)
			return type;
		if(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		{
			final String text=super.text();
			if(text.length()>0)
			{
				int x=text.indexOf(';');
				String roomID = text;
				//String rest="";
				if(x>0)
				{
					roomID=text.substring(0,x).trim();
					//rest=text.substring(x+1).trim();
				}
				boundTo=null;
				if(roomID.equalsIgnoreCase("ROOM"))
					type=BoundToType.CURRENT_ROOM;
				else
				if(roomID.equalsIgnoreCase("AREA"))
					type=BoundToType.CURRENT_AREA;
				else
				{
					boundTo=CMLib.map().getRoom(roomID);
					if(boundTo instanceof Room)
						type=BoundToType.SPECIFIC_ROOM;
					else
					{
						boundTo=CMLib.map().getArea(roomID);
						if(boundTo instanceof Area)
							type=BoundToType.SPECIFIC_AREA;
					}
				}
			}
		}
		return type;
	}
	
	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		boundTo=null;
		type=null;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.sourceMinor()==CMMsg.TYP_ENTER)
		&&(msg.target() instanceof Room)
		&&((msg.source()==affected)
			||((affected instanceof Item)&&(msg.source()==((Item)affected).owner()))))
		{
			final Room whereTo=(Room)msg.target();
			final Room R=CMLib.map().roomLocation(affected);
			if((whereTo==null) || (R==null))
				return true;

			BoundToType type = this.getBoundToType();
			if(type == null)
			{
				if(affected instanceof MOB)
					msg.source().tell(L("You are not allowed to leave this place."));
				else
					msg.source().tell(L("@x1 prevents you from taking it that way.",affected.name()));
				return false;
			}
			else
			if(type == BoundToType.CURRENT_ROOM)
			{
				if(whereTo!=R)
				{
					if(affected instanceof MOB)
						msg.source().tell(L("You are not allowed to leave this place."));
					else
						msg.source().tell(L("@x1 prevents you from taking it that way.",affected.name()));
					return false;
				}
			}
			else
			if(type == BoundToType.CURRENT_AREA)
			{
				if(whereTo.getArea()!=R.getArea())
				{
					if(affected instanceof MOB)
						msg.source().tell(L("You are not allowed to leave this place."));
					else
						msg.source().tell(L("@x1 prevents you from taking it that way.",affected.name()));
					return false;
				}
			}
			else
			if(type == BoundToType.SPECIFIC_ROOM)
			{
				final Room tR=(Room)boundTo;
				if((tR!=null)&&(whereTo!=tR))
				{
					if(R!=tR)
					{
						if(affected instanceof MOB)
						{
							msg.source().tell(L("You are whisked back home!"));
							tR.bringMobHere((MOB)affected,false);
						}
						else
						{
							msg.source().tell(L("@x1 is whisked from you and back to its home.",affected.name()));
							tR.moveItemTo((Item)affected);
							return true;
						}
					}
					else
					{
						if(affected instanceof MOB)
							msg.source().tell(L("You are not allowed to leave this place."));
						else
							msg.source().tell(L("@x1 prevents you from taking it that way.",affected.name()));
					}
					return false;
				}
			}
			else
			if(type == BoundToType.SPECIFIC_AREA)
			{
				final Area A=(Area)boundTo;
				if((A!=null)&&(!A.inMyMetroArea(whereTo.getArea())))
				{
					if(!A.inMyMetroArea(R.getArea()))
					{
						if(affected instanceof MOB)
						{
							msg.source().tell(L("You are whisked back home!"));
							A.getRandomMetroRoom().bringMobHere((MOB)affected,false);
						}
						else
						{
							msg.source().tell(L("@x1 is whisked from you and back to its home.",affected.name()));
							A.getRandomMetroRoom().moveItemTo((Item)affected);
							return true;
						}
					}
					else
					{
						if(affected instanceof MOB)
							msg.source().tell(L("You are not allowed to leave this place."));
						else
							msg.source().tell(L("@x1 prevents you from taking it that way.",affected.name()));
					}
					return false;
				}
			}
		}
		return true;
	}
}
