package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.Items.Basic.StdPortal;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Move;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.Area.State;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2014-2025 Bo Zimmerman

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
public class StdBoardable extends StdPortal implements PrivateProperty, Boardable
{
	@Override
	public String ID()
	{
		return "StdBoardable";
	}

	protected String 	readableText	= "";
	protected String 	ownerName 		= "";
	protected int 		price 			= 1000;
	protected int		internalPrice	= 0;
	protected Area 		area			= null;
	protected String	homePortID		= "";

	protected static String	DEFAULT_NOUN_STRING = CMLib.lang().L("base");
	protected static String	DEFAULT_HEAD_OFFTHEDECK_STRING = CMLib.lang().L("^HOutside, you see: ^N");

	protected String	noun_word		= DEFAULT_NOUN_STRING;
	protected String	head_offTheDeck = DEFAULT_HEAD_OFFTHEDECK_STRING;

	public StdBoardable()
	{
		super();
		setName("a boardable [NEWNAME]");
		setDisplayText("a boardable [NEWNAME] is here.");
		setMaterial(RawMaterial.RESOURCE_OAK);
		setDescription("");
		myUses=100;
		this.doorName="gangplank";
		basePhyStats().setWeight(10000);
		super.setCapacity(0);
		setUsesRemaining(100);
		this.setBaseValue(20000);
		recoverPhyStats();
		CMLib.flags().setGettable(this, false);
	}

	@Override
	public CMObject newInstance()
	{
		final StdBoardable obj = (StdBoardable)super.newInstance();
		obj.area=null;
		obj.getArea();
		return obj;
	}

	@Override
	public boolean subjectToWearAndTear()
	{
		return true;
	}

	@Override
	public Item getBoardableItem()
	{
		return this;
	}

	protected String getAreaClassType()
	{
		return "StdBoardableShip";
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return false;
	}

	protected Room createFirstRoom()
	{
		final Room R=CMClass.getLocale("StdRoom");
		R.setDisplayText(L("The First Room"));
		return R;
	}

	@Override
	public void setDockableItem(final Item dockableItem)
	{
		if(area instanceof Boardable)
			((Boardable)area).setDockableItem(dockableItem);
	}

	@Override
	public Area getArea()
	{
		if(destroyed)
			return null;
		else
		if(area==null)
		{
			area=CMClass.getAreaType(getAreaClassType());
			CMLib.flags().setSavable(area, false);
			final String num=Double.toString(Math.random());
			final int x=num.indexOf('.')+1;
			final int len=((num.length()-x)/2)+1;
			area.setName(L("UNNAMED_@x1",num.substring(x,x+len)));

			final Room R=createFirstRoom();
			R.setRoomID(area.Name()+"#0");
			R.setSavable(false);
			area.addProperRoom(R);
			((Boardable)area).setDockableItem(this);
			readableText=R.roomID();
		}
		return area;
	}

	@Override
	public void setArea(final String xml)
	{
		try
		{
			internalPrice = 0;
			State resetState = null;
			if(area != null)
			{
				if(CMLib.threads().isTicking(area, -1))
					resetState=area.getAreaState();
				area.destroy();
			}
			area=CMLib.coffeeMaker().unpackAreaObjectFromXML(xml);
			if(area instanceof Boardable)
			{
				area.setSavable(false);
				((Boardable)area).setDockableItem(this);
				for(final Enumeration<Room> r=area.getCompleteMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(R!=null)
					{
						CMLib.flags().setSavable(R, false);
						for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if(I!=null)
								internalPrice += I.value();
						}
					}
				}
			}
			else
			{
				Log.warnOut("Failed to unpack a boardable area");
				getArea();
			}
			if(resetState!=null)
				area.setAreaState(resetState);
		}
		catch (final CMException e)
		{
			Log.warnOut("Unable to parse boardable xml for some reason.");
		}
	}

	@Override
	public void dockHere(final Room R)
	{
		if(!R.isContent(this))
		{
			if(owner()==null)
				R.addItem(this,Expire.Never);
			else
				R.moveItemTo(me, Expire.Never, Move.Followers);
		}
		if(this.homePortID.length()==0)
			this.homePortID=CMLib.map().getExtendedRoomID(R);
		if (area instanceof Boardable)
			((Boardable)area).dockHere(R);
	}

	@Override
	public Room unDock(final boolean moveToOutside)
	{
		final Room R=getIsDocked();
		if(R!=null)
		{
			R.delItem(this);
			setOwner(null);
		}
		if (area instanceof Boardable)
			return ((Boardable)area).unDock(moveToOutside);
		return null;
	}

	@Override
	public Room getIsDocked()
	{
		if(owner() instanceof Room)
			return ((Room)owner());
		if (area instanceof Boardable)
			return ((Boardable)area).getIsDocked();
		return null;
	}

	@Override
	public String getHomePortID()
	{
		return this.homePortID;
	}

	@Override
	public void setHomePortID(final String portID)
	{
		if(portID != null)
			this.homePortID = portID;
	}

	@Override
	public String keyName()
	{
		return readableText;
	}

	@Override
	public void setDatabaseID(final String id)
	{
		super.setDatabaseID(id);
		if((owner instanceof Room)
		&&(homePortID.length()==0))
		{
			final String rid=CMLib.map().getExtendedRoomID((Room)owner);
			if(rid != null)
				this.homePortID=rid;
		}
	}

	@Override
	public void setKeyName(final String newKeyName)
	{
		// don't do this, as MUDGrinder mucks it up
	}

	@Override
	public String readableText()
	{
		return readableText;
	}

	@Override
	public void setReadableText(final String text)
	{
		if((text!=null)&&(text.length()>0))
			readableText=text;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getEnvironmentalMiscTextXML(this,false);
	}

	@Override
	public void setMiscText(final String newText)
	{
		miscText="";
		CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(this,newText,false);
		recoverPhyStats();
	}

	@Override
	public CMObject copyOf()
	{
		final StdBoardable s=(StdBoardable)super.copyOf();
		s.destroyed=false;
		s.area=null; // otherwise it gets a copy of the rooms and mobs, which will be destroyed
		s.setOwnerName("");
		final String xml=CMLib.coffeeMaker().getAreaObjectXML(getArea(), null, null, null, true).toString();
		s.setArea(xml);
		s.setReadableText(readableText()); // in case this was first call to getArea()
		/* Should we rename?
		final Area A=s.getArea();
		final String num=Double.toString(Math.random());
		final int x=num.indexOf('.')+1;
		final int len=((num.length()-x)/2)+1;
		String oldName=A.Name();
		A.setName(L("UNNAMED_@x1",num.substring(x,x+len)));
		for(final Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if((R!=null)&&(R.roomID().startsWith(oldName)))
				R.setRoomID(A.Name()+R.roomID().substring(oldName.length()));
		}
		s.renameDestinationRooms(oldName,A.Name());
		*/
		//TODO: when you buy a boardable, none of its electronics is registered.  This is bad.
		//CMLib.tech().unregisterAllElectronics(CMLib.tech().getElectronicsKey(s.getArea()));
		return s;
	}

	@Override
	public void stopTicking()
	{
		if(area!=null)
		{
			CMLib.threads().unTickAll(area);
			final String key=CMLib.tech().getElectronicsKey(area);
			CMLib.tech().unregisterAllElectronics(key);
		}
		super.stopTicking();
		this.destroyed=false; // undo the weird thing
	}

	@Override
	protected Room getDestinationRoom(final Room fromRoom)
	{
		getArea();
		Room R=null;
		final List<String> V=CMParms.parseSemicolons(readableText(),true);
		if((V.size()>0)&&(getArea()!=null))
			R=getArea().getRoom(V.get(CMLib.dice().roll(1,V.size(),-1)));
		return R;
	}

	protected void renameDestinationRooms(String from, final String to)
	{
		getArea();
		final List<String> V=CMParms.parseSemicolons(readableText().toUpperCase(),true);
		final List<String> nV=new ArrayList<String>();
		from=from.toUpperCase();
		for(String s : V)
		{
			if(s.startsWith(from))
				s=to+s.substring(from.length());
			if(getArea().getRoom(s)!=null)
				nV.add(s);
		}
		if((nV.size()==0)&&(getArea().getProperMap().hasMoreElements()))
			nV.add(getArea().getProperMap().nextElement().roomID());
		setReadableText(CMParms.toSemicolonListString(nV));
	}

	@Override
	public void destroy()
	{
		final String ownerName=getOwnerName();
		if((ownerName!=null) && (ownerName.length()>0))
		{
			final Clan clan = CMLib.clans().fetchClanAnyHost(ownerName);
			if(clan != null)
				clan.getExtItems().delItem(this);
			else
			{
				final MOB mob=CMLib.players().getLoadPlayerAllHosts(ownerName);
				if((mob != null) && (mob.playerStats()!=null))
					mob.playerStats().getExtItems().delItem(this);
			}
		}
		if(area!=null)
		{
			final Area A=area;
			area=null; // to prevent recurse
			CMLib.map().destroyAreaObject(A);
		}
		super.destroy();
	}

	@Override
	public int getPrice()
	{
		return price;
	}

	@Override
	public void setPrice(final int price)
	{
		this.price=price;
	}

	@Override
	public int value()
	{
		int value = baseGoldValue();
		if(price > 0)
			value += price;
		getArea();
		return value + internalPrice;
	}

	@Override
	public String getOwnerName()
	{
		return ownerName;
	}

	@Override
	public void setOwnerName(final String owner)
	{
		this.ownerName=owner;
	}

	@Override
	public long expirationDate()
	{
		return super.expirationDate();
	}

	@Override
	public void setExpirationDate(final long time)
	{
		if((time>0)&&(owner() instanceof Room))
			super.setExpirationDate(0);
		else
			super.setExpirationDate(time);
	}

	@Override
	public boolean isProperlyOwned()
	{
		final String owner=getOwnerName();
		if(owner.length()==0)
			return false;
		final Clan C=CMLib.clans().fetchClanAnyHost(owner);
		if(C!=null)
			return true;
		return CMLib.players().playerExistsAllHosts(owner);
	}

	@Override
	public String getTitleID()
	{
		return this.toString();
	}

	@Override
	public void rename(final String newName)
	{
		final Area area=getArea();
		if(area instanceof Boardable)
		{
			final String oldName=area.Name();
			((Boardable)area).rename(newName);
			renameDestinationRooms(oldName,area.Name());
			setArea(CMLib.coffeeMaker().getAreaObjectXML(area, null, null, null, true).toString());
		}
		boolean wouldRecurse=false;
		for(final String word : Boardable.NAME_REPL_STRINGS)
			for(final String rubs : Boardable.NAME_REPL_MARKERS)
				wouldRecurse=wouldRecurse || newName.indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0;
		if(!wouldRecurse)
		{
			for(final String word : Boardable.NAME_REPL_STRINGS)
			{
				for(final String rubs : Boardable.NAME_REPL_MARKERS)
				{
					if(Name().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
						setName(CMStrings.replaceAll(Name(), rubs.charAt(0)+word+rubs.charAt(1), newName));
				}
				for(final String rubs : Boardable.NAME_REPL_MARKERS)
				{
					if(displayText().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
						setDisplayText(CMStrings.replaceAll(displayText(), rubs.charAt(0)+word+rubs.charAt(1), newName));
				}
			}
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(tickID == Tickable.TICKID_AREA)
		{
			if(amDestroyed())
				return false;
			/* this is necessary, but why wasn't it there before?
			if(numEffects()>0)
			{
				eachEffect(new EachApplicable<Ability>()
				{
					@Override
					public final void apply(final Ability A)
					{
						if(!A.tick(ticking,tickID))
							A.unInvoke();
					}
				});
			}
			*/
			return true;
		}
		return super.tick(ticking, tickID);
	}

	protected synchronized void destroyThisBoardable()
	{
		if((this.getOwnerName().length()>0)&&(!this.getOwnerName().startsWith("#")))
		{
			final Clan clan = CMLib.clans().fetchClanAnyHost(this.getOwnerName());
			if(clan != null)
				clan.getExtItems().delItem(this);
			else
			{
				final MOB M = CMLib.players().getLoadPlayer(this.getOwnerName());
				final PlayerStats pStats = (M!=null) ? M.playerStats() : null;
				final ItemCollection items= (pStats != null) ? pStats.getExtItems() : null;
				if(items != null)
					items.delItem(this);
			}
		}
		final CMMsg expireMsg=CMClass.getMsg(CMLib.map().deity(), this, CMMsg.MASK_ALWAYS|CMMsg.MSG_EXPIRE, L("<T-NAME> is destroyed!"));
		final Area A = getArea();
		if(A!=null)
		{
			final LinkedList<Room> propRooms = new LinkedList<Room>();
			for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				propRooms.add(r.nextElement());
			for(final Iterator<Room> e=propRooms.iterator();e.hasNext();)
			{
				final Room R=e.next();
				if(R!=null)
				{
					expireMsg.setTarget(R);
					final Set<MOB> players=CMLib.players().getPlayersHere(R);
					if(players.size()>0)
					{
						for(final MOB M : players)
						{
							//players will get some fancy message when appearing in death room -- which means they should die!
							M.executeMsg(expireMsg.source(), expireMsg);
							CMLib.combat().postDeath(expireMsg.source(), M,null);
						}
					}
					R.send(expireMsg.source(), expireMsg);
				}
			}
			propRooms.clear();
			A.destroy();
		}
		destroy();
	}

	protected boolean okAreaMessage(final CMMsg msg, final boolean outdoorOnly)
	{
		boolean failed = false;
		final Area boardedArea=getArea();
		if(boardedArea!=null)
		{
			for(final Enumeration<Room> r = boardedArea.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((!outdoorOnly)||((R.domainType()&Room.INDOORS)==0))
				{
					failed = failed || R.okMessage(R, msg);
					if(failed)
						break;
				}
			}
		}
		return failed;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		if(mob==null)
			return false;
		if(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDITEMS))
			return true;
		if(getOwnerName().length()==0)
			return true;
		if(CMLib.law().doesOwnThisProperty(mob, this))
			return true;
		final Item I=mob.fetchFirstWornItem(Wearable.WORN_HEAD);
		if((I instanceof ClanItem)
		&&(((ClanItem)I).getClanItemType()==ClanItem.ClanItemType.SAILORSCAP)
		&&(getOwnerName().length()>0)
		&&(getOwnerName().equalsIgnoreCase(((ClanItem)I).clanID())))
			return true;
		return false;
	}

	protected void announceToOuterViewers(final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(null, CMMsg.MSG_OK_ACTION, msgStr);
		announceToOuterViewers(msg);
	}

	protected boolean canViewOuterRoom(final Room R)
	{
		return ((R!=null) && ((R.domainType()&Room.INDOORS)==0));
	}

	protected void announceToOuterViewers(final CMMsg msg)
	{
		MOB mob = null;
		final MOB msgSrc = msg.source();
		try
		{
			final Area A=this.getArea();
			if(A!=null)
			{
				Room mobR = null;
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(canViewOuterRoom(R))
					{
						mobR=R;
						break;
					}
				}
				if(mobR!=null)
				{
					mob = CMClass.getFactoryMOB(name(),phyStats().level(),mobR);
					msg.setSource(mob);
					final Boolean oldValue = Boolean.valueOf(msg.suspendResumeTrailers(null));
					try
					{
						msg.suspendResumeTrailers(Boolean.TRUE);
						for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
						{
							final Room R=r.nextElement();
							if(canViewOuterRoom(R)
							&& (R.okMessage(mob, msg)))
							{
								if(R == mobR)
									R.send(mob, msg); // this lets the source know, i guess
								else
									R.sendOthers(mob, msg); // this lets the source know, i guess
							}
						}
					}
					finally
					{
						msg.suspendResumeTrailers(oldValue);
					}
				}
			}
		}
		finally
		{
			msg.setSource(msgSrc);
			if(mob != null)
				mob.destroy();
		}
	}

	protected void announceToOuterViewers(final MOB mob, final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, msgStr);
		sendAreaMessage(mob,msg, true);
	}

	protected void announceToOuterViewers(final MOB mob, final Environmental target, final Environmental tool, final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(mob, target, tool, CMMsg.MSG_OK_ACTION, msgStr);
		sendAreaMessage(mob,msg, true);
	}

	protected void announceToNonOuterViewers(final MOB mob, final String msgStr)
	{
		final CMMsg msg=CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, msgStr);
		sendAreaMessage(mob,msg, false);
	}

	protected void announceToAllAboard(final String msgStr)
	{
		final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),CMLib.map().roomLocation(this));
		try
		{
			final CMMsg msg2=CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, msgStr);
			final Room R=CMLib.map().roomLocation(this);
			if((R!=null) && (R.okMessage(mob, msg2) && this.okAreaMessage(msg2, false)))
			{
				//R.send(mob, msg2); // this lets the source know, i guess
				this.sendAreaMessage(msg2, false); // this just sends to "others"
			}
		}
		finally
		{
			mob.destroy();
		}
	}

	protected void sendAreaMessage(final MOB mob, final CMMsg msg, final boolean outerViewerStatus)
	{
		final Area A=this.getArea();
		final Room mobR=CMLib.map().roomLocation(mob);
		if(A!=null)
		{
			final Boolean oldValue = Boolean.valueOf(msg.suspendResumeTrailers(null));
			try
			{
				msg.suspendResumeTrailers(Boolean.TRUE);
				for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if((R!=null) && (canViewOuterRoom(R)==outerViewerStatus) && (R.okMessage(mob, msg)))
					{
						if(R == mobR)
							R.send(mob, msg); // this lets the source know, i guess
						else
							R.sendOthers(mob, msg); // this lets the source know, i guess
					}
				}
			}
			finally
			{
				msg.suspendResumeTrailers(oldValue);
			}
		}
	}

	protected void sendAreaMessage(final CMMsg msg, final boolean outerViewersOnly)
	{
		final Area boardedArea=getArea();
		if(boardedArea!=null)
		{
			// suspendResumeTrailers is disabled to have NO EFFECT, until I figure out why this is here
			// it was preventing death from trailing damage.
			//final Boolean oldValue = Boolean.valueOf(msg.suspendResumeTrailers(null));
			try
			{
				//msg.suspendResumeTrailers(Boolean.TRUE);
				for(final Enumeration<Room> r = boardedArea.getProperMap(); r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					if(((!outerViewersOnly)||canViewOuterRoom(R))
					&&(R.roomID().length()>0))
						R.sendOthers(msg.source(), msg);
				}
			}
			finally
			{
				//msg.suspendResumeTrailers(oldValue);
			}
		}
	}

	protected boolean confirmAreaMessage(final CMMsg msg, final boolean outerViewersOnly)
	{
		final Area itemArea=CMLib.map().areaLocation(this.getBoardableItem());
		final Area boardedArea=getArea();
		if(itemArea == boardedArea)
		{
			if(itemArea == null)
				return true;
			final Room srcR=(msg!=null && msg.source() != null) ? msg.source().location() : null;
			if((srcR!=null)
			&&(srcR != boardedArea))
			{
				if(srcR.isContent(this.getBoardableItem()))
				{
					Log.errOut("Boardable "+name()+" is inside itself?! Fixing bad owner ref.");
					getBoardableItem().setOwner(srcR);
				}
				else
				{
					Log.errOut("Boardable "+name()+" is inside itself?! Moving to message room...");
					srcR.moveItemTo(this.getBoardableItem());
				}
			}
			else
			if((this.getOwnerName().length()==0)||(this.getOwnerName().startsWith("#")))
			{
				Log.errOut("Boardable "+name()+" is inside itself?! It's unowned, so destroying!");
				this.destroyThisBoardable();
			}
			else
			if(srcR != null)
			{
				Log.errOut("Boardable "+name()+", owned by "+getOwnerName()+" is inside itself?! Not sure what to do.");
				return false;
			}
		}
		if(boardedArea!=null)
		{
			for(final Enumeration<Room> r = boardedArea.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((!outerViewersOnly)||canViewOuterRoom(R))
				{
					if((msg != null)
					&& (!R.okMessage(msg.source(), msg)))
						return false;
				}
			}
		}
		return true;
	}

	protected void cleanMsgForRepeat(final CMMsg msg)
	{
		msg.setSourceCode(CMMsg.NO_EFFECT);
		if(msg.trailerRunnables()!=null)
			msg.trailerRunnables().clear();
		if(msg.trailerMsgs()!=null)
		{
			for(final CMMsg msg2 : msg.trailerMsgs())
				cleanMsgForRepeat(msg2);
		}
	}

	protected void haveEveryoneLookOutside()
	{
		if((area != null)&&(owner() instanceof Room))
		{
			final Room targetR=(Room)owner();
			for(final Enumeration<Room> r=area.getProperMap(); r.hasMoreElements(); )
			{
				final Room R=r.nextElement();
				if((R!=null)
				&&(this.canViewOuterRoom(R)))
				{
					final Set<MOB> mobs=CMLib.players().getPlayersHere(R);
					if(mobs.size()>0)
					{
						for(final MOB mob : new XTreeSet<MOB>(mobs))
						{
							if(mob == null)
								continue;
							final CMMsg lookMsg=CMClass.getMsg(mob,targetR,null,CMMsg.MSG_LOOK,null);
							final CMMsg lookExitMsg=CMClass.getMsg(mob,targetR,null,CMMsg.MSG_LOOK_EXITS,null);
							if((mob.isAttributeSet(MOB.Attrib.AUTOEXITS))
							&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH)
							&&(CMLib.flags().canBeSeenBy(targetR,mob)))
							{
								if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=CMProps.Int.EXVIEW_MIXED)!=mob.isAttributeSet(MOB.Attrib.BRIEF))
									lookExitMsg.setValue(CMMsg.MASK_OPTIMIZE);
								lookMsg.addTrailerMsg(lookExitMsg);
							}
							if(targetR.okMessage(mob,lookMsg))
								targetR.send(mob,lookMsg);
						}
					}
				}
			}
		}
	}


	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_HUH:
		case CMMsg.TYP_COMMANDFAIL:
		case CMMsg.TYP_SKILLFAIL:
		case CMMsg.TYP_COMMAND:
			break;
		default:
			if(!confirmAreaMessage(msg, true))
				return false;
			break;
		}

		if((msg.target()==this)
		&&(msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.tool() instanceof ShopKeeper))
		{
			final ShopKeeper shop=(ShopKeeper)msg.tool();
			final boolean clanSale =
					   shop.isSold(ShopKeeper.DEAL_CLANPOSTMAN)
					|| shop.isSold(ShopKeeper.DEAL_CSHIPSELLER)
					|| shop.isSold(ShopKeeper.DEAL_CLANDSELLER);
			CMLib.map().registerWorldObjectLoaded(null, null, this);
			transferOwnership(msg.source(),clanSale);
			return false;
		}
		if(!super.okMessage(myHost, msg))
		{
			return false;
		}
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_OPEN:
			case CMMsg.TYP_CLOSE:
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_UNLOCK:
			{
				final String doorName = CMLib.english().startWithAorAn(doorName());
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAME>", L("@x1 on <T-NAME>",doorName)));
				msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(), "<T-NAMESELF>", L("@x1 on <T-NAMESELF>",doorName)));
				msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(), "<T-NAME>", L("@x1 on <T-NAME>",doorName)));
				msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(), "<T-NAMESELF>", L("@x1 on <T-NAMESELF>",doorName)));
				msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(), "<T-NAME>", L("@x1 on <T-NAME>",doorName)));
				msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(), "<T-NAMESELF>", L("@x1 on <T-NAMESELF>",doorName)));
				break;
			}
			}
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_GET:
			if(msg.amITarget(this))
			{
				if(msg.tool() instanceof ShopKeeper)
				{
					final ShopKeeper shop=(ShopKeeper)msg.tool();
					final boolean clanSale =
							   shop.isSold(ShopKeeper.DEAL_CLANPOSTMAN)
							|| shop.isSold(ShopKeeper.DEAL_CSHIPSELLER)
							|| shop.isSold(ShopKeeper.DEAL_CLANDSELLER);
					CMLib.map().registerWorldObjectLoaded(null, null, this);
					transferOwnership(msg.source(),clanSale);
				}
			}
			break;
		case CMMsg.TYP_SELL:
			if((msg.tool()==this)
			&&(msg.target() instanceof ShopKeeper))
			{
				setOwnerName("");
				recoverPhyStats();
			}
			break;
		case CMMsg.TYP_GIVE:
			if((msg.tool()==this)
			&&(getOwnerName().length()>0)
			&&((msg.source().Name().equals(getOwnerName()))
				||(msg.source().getLiegeID().equals(getOwnerName())&&msg.source().isMarriedToLiege())
				||(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER)))
			&&(msg.target() instanceof MOB)
			&&(!(msg.target() instanceof Banker))
			&&(!(msg.target() instanceof Librarian))
			&&(!(msg.target() instanceof Auctioneer))
			&&(!(msg.target() instanceof PostOffice)))
			{
				final boolean clanSale = CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER);
				transferOwnership((MOB)msg.target(),clanSale);
			}
			break;
		}
		switch(msg.sourceMinor())
		{
		case CMMsg.TYP_HUH:
		case CMMsg.TYP_COMMANDFAIL:
		case CMMsg.TYP_SKILLFAIL:
		case CMMsg.TYP_COMMAND:
			break;
		default:
			if(msg.source().riding() != this)
			{
				if(msg.othersMessage()==null)
					sendAreaMessage(msg, true);
				else
				{
					final CMMsg msg2=(CMMsg)msg.copyOf();
					msg2.setOthersMessage(head_offTheDeck+msg.othersMessage());
					cleanMsgForRepeat(msg2);
					sendAreaMessage(msg2, true);
				}
			}
			else
			if(msg.sourceMinor()!=CMMsg.TYP_ENTER)
				sendAreaMessage(msg, true);
			break;
		}
	}

	protected void transferOwnership(final MOB buyer, final boolean clanSale)
	{
		if((getOwnerName().length()>0)&&(!getOwnerName().startsWith("#")))
		{
			final MOB M=CMLib.players().getLoadPlayer(getOwnerName());
			if((M!=null)&&(M.playerStats()!=null))
			{
				M.playerStats().getExtItems().delItem(this);
				M.playerStats().setLastUpdated(0);
			}
			else
			{
				final Clan C=CMLib.clans().fetchClanAnyHost(getOwnerName());
				if(C!=null)
				{
					C.getExtItems().delItem(this);
					CMLib.database().DBUpdateClanItems(C);
					final MOB cM=CMLib.players().getLoadPlayerAllHosts(C.getResponsibleMemberName());
					if(cM != null)
						CMLib.achievements().possiblyBumpAchievement(cM, AchievementLibrary.Event.CLANPROPERTY, -1, C, getArea());
				}
			}
			setOwnerName("");
		}
		if(clanSale)
		{
			final Pair<Clan,Integer> targetClan=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER);
			if(targetClan!=null)
			{
				setOwnerName(targetClan.first.clanID());
				CMLib.achievements().possiblyBumpAchievement(buyer, AchievementLibrary.Event.CLANPROPERTY, 1, targetClan, getArea());
			}
			else
				setOwnerName(buyer.Name());
		}
		else
			setOwnerName(buyer.Name());
		recoverPhyStats();
		final Session session=buyer.session();
		final Room R=CMLib.map().roomLocation(this);
		if(session!=null)
		{
			final StdBoardable me=this;
			final InputCallback[] namer=new InputCallback[1];
			namer[0]=new InputCallback(InputCallback.Type.PROMPT)
			{
				@Override
				public void showPrompt()
				{
					session.println(L("\n\rEnter a new name for your @x1: ",noun_word));
				}

				@Override
				public void timedOut()
				{
				}

				@Override
				public void callBack()
				{
					for(final Enumeration<Boardable> s=CMLib.map().ships();s.hasMoreElements();)
					{
						final Boardable boardableItem=s.nextElement();
						if((boardableItem!=null)
						&&(!boardableItem.amDestroyed())
						&&(boardableItem.getArea()!=null)
						&&(boardableItem.getArea().Name().equalsIgnoreCase(this.input.trim())))
						{
							this.input="";
							break;
						}
					}
					if(CMLib.map().getArea(this.input.trim())!=null)
						this.input="";

					if((this.input.trim().length()==0)
					||(!CMLib.login().isOkName(this.input.trim(),true))
					||(CMLib.tech().getMakeRegisteredKeys().contains(this.input.trim())))
					{
						session.println(L("^ZThat is not a permitted name.^N"));
						session.prompt(namer[0].reset());
						return;
					}
					CMLib.tech().unregisterAllElectronics(CMLib.tech().getElectronicsKey(me.getArea()));
					final String oldName=me.Name();
					me.rename(this.input.trim());
					buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
					for(final Enumeration<Item> i=buyer.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if(I.ID().equalsIgnoreCase("GenTitle")
						&&(I instanceof LandTitle))
						{
							final LandTitle L=(LandTitle)I;
							if(L.landPropertyID().equals(oldName))
							{
								L.setName("");
								L.setLandPropertyID(me.Name());
								L.text(); // everything else is derived from the thing itself
								I.recoverPhyStats();
							}
						}
					}
					final Room finalR=findNearestDocks(R);
					if(finalR==null)
					{
						Log.errOut("Could not dock "+me.name()+" in area "+R.getArea().Name()+" due to lack of port.");
						buyer.tell(L("Nowhere was found to dock.  Please contact the administrators!."));
					}
					else
					{
						me.setHomePortID("");
						me.dockHere(finalR);
						buyer.tell(L("You'll find @x1 docked at '@x2'.",me.name(),finalR.displayText(buyer)));
					}
					// re-register all electronics by re-settings its owners.  That should do it.
					for(final Enumeration<Room> r=me.getArea().getProperMap();r.hasMoreElements();)
					{
						final Room R=r.nextElement();
						if(R!=null)
						{
							for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
							{
								final Item I=i.nextElement();
								if(I instanceof Electronics)
									I.setOwner(R);
							}
						}
					}
					Clan C;
					if(clanSale && ((C=CMLib.clans().getClanExact(me.getOwnerName()))!=null))
					{
						if(!C.getExtItems().isContent(me))
						{
							me.setSavable(false); // if the clan is saving it, rooms are NOT.
							C.getExtItems().addItem(me);
						}
					}
					else
					if ((buyer.playerStats() != null) && (!buyer.playerStats().getExtItems().isContent(me)))
					{
						me.setSavable(false); // if the player is saving it, rooms are NOT.
						buyer.playerStats().getExtItems().addItem(me);
					}
				}
			};
			session.prompt(namer[0]);
		}
		else
		{
			buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
			if((this.getOwnerName().equals(buyer.Name()) && (buyer.playerStats() != null)))
			{
				if(!buyer.playerStats().getExtItems().isContent(this))
				{
					this.setSavable(false); // if the player is saving it, rooms are NOT.
					buyer.playerStats().getExtItems().addItem(this);
				}
			}
			else
			{
				final Clan C=CMLib.clans().getClanExact(getOwnerName());
				if(C!=null)
				{
					if(!C.getExtItems().isContent(this))
					{
						this.setSavable(false); // if the clan is saving it, rooms are NOT.
						C.getExtItems().addItem(this);
					}
				}
				else
				{
					buyer.tell(L("However, there is no entity to actually take ownership.  Weird."));
				}
			}
			final Room finalR=findNearestDocks(R);
			if(finalR==null)
				Log.errOut("Could not dock in area "+R.getArea().Name()+" due to lack of docks.");
			else
			{
				this.setHomePortID("");
				dockHere(finalR);
			}
		}
	}

	protected Room getRandomOutsideRoom()
	{
		final Area A=this.getArea();
		if(A!=null)
		{
			final List<Room> deckRooms=new ArrayList<Room>(2);
			for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((R!=null)
				&& ((R.domainType()&Room.INDOORS)==0)
				&& (R.domainType()!=Room.DOMAIN_OUTDOORS_AIR))
					deckRooms.add(R);
			}
			if(deckRooms.size()>0)
			{
				return deckRooms.get(CMLib.dice().roll(1, deckRooms.size(), -1));
			}
		}
		return null;
	}

	protected Room findNearestDocks(final Room R)
	{
		return R;
	}

	@Override
	public String putString(final Rider R)
	{
		if((R==null)||(putString.length()==0))
			return "load(s)";
		return putString;
	}

	@Override
	public String mountString(final int commandType, final Rider R)
	{
		if((R==null)||(mountString.length()==0))
			return "board(s)";
		return mountString;
	}

	@Override
	public String dismountString(final Rider R)
	{
		if((R==null)||(dismountString.length()==0))
			return "disembark(s) from";
		return dismountString;
	}

	@Override
	public boolean isSavable()
	{
		if(!super.isSavable())
			return false;
		return (getOwnerName().length()==0);
	}
}
