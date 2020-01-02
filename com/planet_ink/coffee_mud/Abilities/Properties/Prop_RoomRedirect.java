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
   Copyright 2019-2020 Bo Zimmerman

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
public class Prop_RoomRedirect extends Property
{
	@Override
	public String ID()
	{
		return "Prop_RoomRedirect";
	}

	final PairSVector<String, String>						rawRedirects	= new PairSVector<String, String>();
	final PairSVector<MaskingLibrary.CompiledZMask, Room>	redirects		= new PairSVector<MaskingLibrary.CompiledZMask, Room>();

	@Override
	public String name()
	{
		return "Room Redirect Property";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS|Ability.CAN_AREAS;
	}

	@Override
	public String accountForYourself()
	{
		return "Redirects you somewhere else..";
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText((newMiscText==null) ? "" : newMiscText.trim());
		this.rawRedirects.clear();
		this.redirects.clear();
		final String[] pairs = this.miscText.split(";");
		for(final String pair : pairs)
		{
			final int x=pair.indexOf('=');
			if(x>=0)
				rawRedirects.add(new Pair<String,String>(pair.substring(0,x).trim(),pair.substring(x+1).trim()));
		}
	}

	protected synchronized final PairList<MaskingLibrary.CompiledZMask, Room> getRedirects()
	{
		if(rawRedirects.size() != redirects.size())
		{
			redirects.clear();
			final Set<String> foundMasks = new HashSet<String>();
			final String roomID=(affected instanceof Environmental)?affected.Name():"";
			for(int p=rawRedirects.size()-1;p>=0;p--)
			{
				final Pair<String,String> pair=rawRedirects.get(p);
				if(pair.second.length()==0)
				{
					if(foundMasks.contains(pair.second.toUpperCase().trim()))
					{
						Log.errOut("Prop_RoomRedirect '"+roomID+"' has multiple mask '"+pair.second+"'.");
						rawRedirects.remove(p);
					}
					else
						foundMasks.add(pair.second.toUpperCase().trim());
				}
				final MaskingLibrary.CompiledZMask mask = CMLib.masking().getPreCompiledMask(pair.second);
				final Room R=CMLib.map().getRoom(pair.first);
				if((R==null)
				&&(pair.first.length()>0))
				{
					Log.errOut("Prop_RoomRedirect '"+roomID+"' has invalid room id '"+pair.first+"'.");
					rawRedirects.remove(p);
				}
				else
					redirects.add(mask, R);
			}
		}
		return redirects;
	}

	public Room getRedirectRoom(final MOB mob)
	{
		final PairList<MaskingLibrary.CompiledZMask, Room> redirects = this.getRedirects();
		for(final Pair<MaskingLibrary.CompiledZMask, Room> p : redirects)
		{
			if(CMLib.masking().maskCheck(p.first, mob, true))
				return CMLib.map().getRoom(p.second);
		}
		return null;
	}

	public boolean isReallyHere(final MOB mob)
	{
		if(mob == null)
			return false;
		final Room R=mob.location();
		if(R==null)
			return false;
		if(affected instanceof Room)
			return (R==affected) && (R.isInhabitant(mob));
		else
		if(affected instanceof Area)
		{
			return (R.getArea()==affected) && (R.isInhabitant(mob));
		}
		return false;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(msg.amITarget(affected)
		||(((affected instanceof Area)
			&&(msg.target() instanceof Room)
			&&(((Area)affected).inMyMetroArea(((Room)msg.target()).getArea())))))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_RECALL:
				if(!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
				{
					final Room realRoom=this.getRedirectRoom(msg.source());
					if(realRoom != null)
					{
						if(msg.source().isPlayer())
						{
							msg.source().playerStats().addRoomVisit((Room)msg.target());
							msg.source().playerStats().addRoomVisit(realRoom);
						}
						msg.setTarget(realRoom);
						return realRoom.okMessage(myHost, msg);
					}
				}
				break;
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if((!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
				&&(isReallyHere(msg.source())))
				{
					final Room realRoom=this.getRedirectRoom(msg.source());
					if(realRoom != null)
					{
						msg.setTarget(realRoom);
						if((!realRoom.isInhabitant(msg.source()))
						||(msg.source().location()!=realRoom))
						{
							realRoom.bringMobHere(msg.source(), true);
							return realRoom.okMessage(myHost, msg);
						}
					}
				}
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(this,msg);
		if(msg.amITarget(affected)
		||(((affected instanceof Area)
			&&(msg.target() instanceof Room)
			&&(((Area)affected).inMyMetroArea(((Room)msg.target()).getArea())))))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_RECALL:
				if(!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
				{
					final Room realRoom=this.getRedirectRoom(msg.source());
					if(realRoom != null)
					{
						msg.setTarget(realRoom);
						realRoom.executeMsg(myHost, msg);
					}
				}
				break;
			case CMMsg.TYP_LOOK_EXITS:
				if(!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
				{
					final Room realRoom=this.getRedirectRoom(msg.source());
					if(realRoom != null)
					{
						msg.setTarget(realRoom);
						if(msg.value()==CMMsg.MASK_OPTIMIZE)
							CMLib.commands().lookAtExitsShort(realRoom, msg.source());
						else
							CMLib.commands().lookAtExits(realRoom, msg.source());
					}
				}
				break;
			case CMMsg.TYP_LOOK:
			case CMMsg.TYP_EXAMINE:
				if(!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
				{
					final Room realRoom=this.getRedirectRoom(msg.source());
					if(realRoom != null)
					{
						msg.setTarget(realRoom);
						CMLib.commands().handleBeingLookedAt(msg);
					}
				}
				break;
			}
		}
		else
		if(msg.sourceMinor()==CMMsg.TYP_LIFE)
		{
			if(!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
			{
				final Room realRoom=this.getRedirectRoom(msg.source());
				if(realRoom != null)
				{
					msg.setTarget(realRoom);
					realRoom.executeMsg(myHost, msg);
					msg.addTrailerRunnable(new Runnable()
					{
						final MOB mob=msg.source();
						final Room R=realRoom;
						@Override
						public void run()
						{
							if((!R.isInhabitant(mob))
							||(mob.location()!=R))
								R.bringMobHere(mob, true);
						}
					});
				}
			}
		}
	}
}
