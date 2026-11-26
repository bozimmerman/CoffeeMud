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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2026 Bo Zimmerman

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

	final PairSVector<String, String>			rawRedirects	= new PairSVector<String, String>();
	final List<String>							rawArrivals		= new Vector<String>();
	final PairSVector<CompiledZMask, Object>	redirects		= new PairSVector<CompiledZMask, Object>();

	/**
	 * How long after arriving from another mud a player is left alone, so that
	 * a room which redirects back at the mud they came from doesn't bounce them
	 * straight back out again.
	 */
	private static final long					MPCP_ARRIVAL_GRACE_MS	= 5000;
	private static final Map<MOB, Room>			ENTER_ROOMS				= new ExpireHashMap<MOB, Room>(15000);

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
		return "Sends you somewhere else, or receives you from somewhere else..";
	}

	@Override
	public String miscTextFormat()
	{
		return "(("
				+"((#[NUMBER(prompt=Room #/colh=Rm#)])/"
				+"([CHECK(name=BEACON/prompt=Beacon/colh=Beacon)])/"
				+"([CHECK(name=HOME/prompt=Home/colh=Home)])/"
				+"([NUMBER(prompt=Room #/colh=Rm#)])/"
				+"([STRING(prompt=Remote Room ID (FROM room@host:port for arrivals)/colh=RRID)..@]@[STRING(prompt=hostname/colh=host)..:]:[NUMBER(prompt=port/colh=port)])"
				+"([ROOM_ID(prompt=Room ID/colh=RID)..=]))"
				+"="
				+"[ZAPPERMASK(prompt=Mask/colh=Mask)..;]"
				+")..;)";
	}

	protected Triad<String,Integer,String> makeRemoteTriad(final String roomIdStr)
	{
		if((roomIdStr.indexOf('@')>0)
		&&(roomIdStr.indexOf(':')>roomIdStr.lastIndexOf('@')))
		{
			int hostDex = roomIdStr.lastIndexOf('@');
			int portDex = roomIdStr.indexOf(':',hostDex);
			final String remoteRoomId = roomIdStr.substring(0,hostDex).trim();
			final String remoteHost = roomIdStr.substring(hostDex+1,portDex).trim();
			final int port = CMath.s_int(roomIdStr.substring(portDex+1).trim());
			if((port>0)&&(port<=65535))
				return new Triad<String,Integer,String>(remoteHost,Integer.valueOf(port),remoteRoomId);
		}
		return null;
	}
	
	protected void registerRemotes()
	{
		if((affected!=null)
		&&(this.rawArrivals.size()>0)
		&&(CMLib.map().roomLocation(affected)!=null))
		{
			for(int p=rawArrivals.size()-1;p>=0;p--)
			{
				final Triad<String,Integer,String> tri = makeRemoteTriad(rawArrivals.get(p));
				if(tri != null)
					CMLib.map().registerArrivalRoom(tri.first, tri.second.intValue(), tri.third, CMLib.map().roomLocation(affected));
			}
		}
	}
	
	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText((newMiscText==null) ? "" : newMiscText.trim());
		unregisterRemotes();
		this.rawRedirects.clear();
		this.rawArrivals.clear();
		this.redirects.clear();
		final String[] pairs = this.miscText.split(";");
		for(final String pair : pairs)
		{
			final int x=pair.indexOf('=');
			if(x>=0)
			{
				String spec=pair.substring(0,x).trim();
				final String upper=spec.toUpperCase();
				final boolean arrival=(upper.startsWith("FROM "))||(upper.startsWith("FROM:"));
				if((arrival)||(upper.startsWith("TO "))||(upper.startsWith("TO:")))
					spec=spec.substring(arrival?5:3).trim();
				if(arrival)
					rawArrivals.add(spec);
				else
					rawRedirects.add(new Pair<String,String>(spec,pair.substring(x+1).trim()));
			}
		}
		this.registerRemotes();
	}

	protected void unregisterRemotes()
	{
		if(this.rawArrivals.size()>0)
		{
			// when property is removed from its host, remove the room redirect registrations
			for(int p=rawArrivals.size()-1;p>=0;p--)
			{
				Triad<String,Integer,String> triad = this.makeRemoteTriad(rawArrivals.get(p));
				if(triad != null)
					CMLib.map().registerArrivalRoom(triad.first,triad.second.intValue(),triad.third,null);
			}
		}
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		unregisterRemotes();
		super.setAffectedOne(P);
		if((affected != null)&&((this.rawRedirects.size()>0)||(this.rawArrivals.size()>0)))
			this.registerRemotes();
	}
	
	protected synchronized final PairList<CompiledZMask, Object> getRedirects()
	{
		if(rawRedirects.size() != redirects.size())
		{
			this.redirects.clear();
			final Set<String> foundMasks = new HashSet<String>();
			final String roomID=(affected instanceof Environmental)?affected.Name():"";
			for(int p=rawRedirects.size()-1;p>=0;p--)
			{
				final Pair<String,String> pair=rawRedirects.get(p);
				final String maskStr = pair.second;
				final String maskUpp = maskStr.toUpperCase();
				final String roomIdStr = pair.first;
				if(maskStr.length()==0)
				{
					if(foundMasks.contains(maskUpp))
					{
						Log.errOut("Prop_RoomRedirect '"+roomID+"' has multiple mask '"+maskStr+"'.");
						rawRedirects.remove(p);
					}
					else
						foundMasks.add(maskUpp);
				}
				final CompiledZMask mask = CMLib.masking().getPreCompiledMask(maskStr);
				final Room R=CMLib.map().getRoom(roomIdStr);
				if((R==null)
				&&(roomIdStr.length()>0))
				{
					if(roomIdStr.startsWith("#"))
						redirects.add(mask, roomIdStr);
					else
					if(CMath.isInteger(roomIdStr))
						redirects.add(mask, "#"+roomIdStr);
					else
					if((roomIdStr.indexOf('@')>0)
					&&(roomIdStr.indexOf(':')>roomIdStr.lastIndexOf('@')))
					{
						final Triad<String,Integer,String> tri = makeRemoteTriad(roomIdStr);
						if(tri != null)
							redirects.add(mask, tri);
					}
					else
					if(roomIdStr.equalsIgnoreCase("beacon"))
						redirects.add(mask, "BEACON");
					else
					if(roomIdStr.equalsIgnoreCase("home"))
						redirects.add(mask, "HOME");
					else
					{
						Log.errOut("Prop_RoomRedirect '"+roomID+"' has invalid room id '"+roomIdStr+"'.");
						rawRedirects.remove(p);
					}
				}
				else
					redirects.add(mask, R);
			}
		}
		return redirects;
	}

	@SuppressWarnings("unchecked")
	public Room getRedirectRoom(final MOB mob, boolean look, boolean allowRemote)
	{
		final PairList<CompiledZMask, Object> redirects = this.getRedirects();
		Area area=CMLib.map().areaLocation(mob);
		if(area == null)
			area=CMLib.map().areaLocation(affected);
		for(final Pair<CompiledZMask, Object> p : redirects)
		{
			if(CMLib.masking().maskCheck(p.first, mob, true))
			{
				final Object o = p.second;
				if(o instanceof Room)
					return CMLib.map().getRoom((Room)o);
				else
				if(o instanceof Triad)
				{
					if(look)
						return null;
					// a remote destination has no local room; offer the player
					// to the other mud and let the local transfer finish.
					final Triad<String,Integer,String> tri = (Triad<String,Integer,String>)o;
					final PlayerStats.IMudTransferData fxfer = (mob.playerStats() != null)
							? mob.playerStats().getIMudTransferData() : null;
					if(allowRemote)
					{
						final Room lastRoom = ENTER_ROOMS.get(mob);
						if((lastRoom != null) && (mob.location() != lastRoom))
							lastRoom.bringMobHere(mob, false);
						final Session S = mob.session();
						if((mob.isPlayer())
						&&(S != null)
						&&(S.getClientTelnetMode(Session.TELNET_MPCP))
						&&((fxfer == null)
							||(fxfer.isLocal)
							||((System.currentTimeMillis() - fxfer.timestamp) >= MPCP_ARRIVAL_GRACE_MS)))
							S.doPing(Session.SessionPing.PLAYERTRANSFER, new Pair<String,Integer>(tri.first, tri.second));
					}
					return null;
				}
				else
				if(o instanceof String)
				{
					final String s = (String)o;
					if(s.startsWith("#")&&(area!=null))
					{
						final Room R=area.getRoom(area.Name()+s);
						if(R!=null)
							return R;
					}
					else
					if(s.equals("BEACON"))
					{
						// handle recall or dock
						if(mob.riding() instanceof Boardable)
						{
							final Room R = CMLib.map().getRoom(((Boardable)mob.riding()).getHomePortID());
							if(R != null)
								return R;
						}
						else
							return mob.getStartRoom();
					}
					else
					if(s.equals("HOME"))
					{
						if(look)
							return null;
						final PlayerStats.IMudTransferData fxfer = (mob.playerStats() != null)
								? mob.playerStats().getIMudTransferData() : null;
						if((fxfer != null) && (!fxfer.isLocal) && (fxfer.host != null))
						{
							if(allowRemote)
							{
								final Session S = mob.session();
								if((S != null)&&(S.getClientTelnetMode(Session.TELNET_MPCP)))
									S.doPing(Session.SessionPing.PLAYERTRANSFER, new Pair<String,Integer>(fxfer.host, Integer.valueOf(fxfer.port)));
							}
							return null;
						}
						final Room R = mob.getStartRoom();
						if((R == null) || (R == mob.location()))
							return null;
						return R;
					}
				}
			}
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
				ENTER_ROOMS.put(msg.source(), msg.source().location());
				if(!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS))
				{
					final Room realRoom=this.getRedirectRoom(msg.source(),false,false);
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
					final Room realRoom=this.getRedirectRoom(msg.source(),true,false);
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
					final Room realRoom=this.getRedirectRoom(msg.source(),false,true);
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
					final Room realRoom=this.getRedirectRoom(msg.source(),true,true);
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
					final Room realRoom=this.getRedirectRoom(msg.source(),true,true);
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
				final Room realRoom=this.getRedirectRoom(msg.source(),false,true);
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
