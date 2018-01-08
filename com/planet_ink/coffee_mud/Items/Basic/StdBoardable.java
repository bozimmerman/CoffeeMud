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
   Copyright 2014-2018 Bo Zimmerman

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
public class StdBoardable extends StdPortal implements PrivateProperty, BoardableShip
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
		final StdBoardable ship = (StdBoardable)super.newInstance();
		ship.area=null;
		ship.getShipArea();
		return ship;
	}
	
	@Override 
	public boolean subjectToWearAndTear()
	{
		return true;
	}
	
	@Override
	public Item getShipItem()
	{
		return this;
	}
	
	protected String getAreaClassType()
	{
		return "StdBoardableShip";
	}
	
	protected Room createFirstRoom()
	{
		final Room R=CMClass.getLocale("StdRoom");
		R.setDisplayText(L("The First Room"));
		return R;
	}
	
	@Override
	public void setDockableItem(Item dockableItem)
	{
		if(area instanceof BoardableShip)
			((BoardableShip)area).setDockableItem(dockableItem);
	}

	@Override
	public Area getShipArea()
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
			((BoardableShip)area).setDockableItem(this);
			readableText=R.roomID();
		}
		return area;
	}

	@Override
	public void setShipArea(String xml)
	{
		try
		{
			internalPrice = 0;
			area=CMLib.coffeeMaker().unpackAreaObjectFromXML(xml);
			if(area instanceof BoardableShip)
			{
				area.setSavable(false);
				((BoardableShip)area).setDockableItem(this);
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
				Log.warnOut("Failed to unpack a boardable area for the space ship");
				getShipArea();
			}
		}
		catch (final CMException e)
		{
			Log.warnOut("Unable to parse boardable xml for some reason.");
		}
	}
	
	@Override
	public void dockHere(Room R)
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
		if (area instanceof BoardableShip)
			((BoardableShip)area).dockHere(R);
	}

	@Override
	public Room unDock(boolean moveToOutside)
	{
		final Room R=getIsDocked();
		if(R!=null)
		{
			R.delItem(this);
			setOwner(null);
		}
		if (area instanceof BoardableShip)
			return ((BoardableShip)area).unDock(moveToOutside);
		return null;
	}

	@Override
	public Room getIsDocked()
	{
		if (area instanceof BoardableShip)
			return ((BoardableShip)area).getIsDocked();
		if(owner() instanceof Room)
			return ((Room)owner());
		return null;
	}

	@Override 
	public String getHomePortID() 
	{ 
		return this.homePortID; 
	}
	
	@Override 
	public void setHomePortID(String portID) 
	{ 
		this.homePortID = portID;
	}

	@Override 
	public String keyName() 
	{ 
		return readableText;
	}
	
	@Override 
	public void setKeyName(String newKeyName) 
	{ 
		// don't do this, as MUDGrinder mucks it up
	}

	@Override 
	public String readableText()
	{
		return readableText;
	}
	
	@Override 
	public void setReadableText(String text)
	{
		if((text!=null)&&(text.length()>0))
			readableText=text;
	}

	@Override
	public String text()
	{
		return CMLib.coffeeMaker().getPropertiesStr(this,false);
	}

	@Override
	public void setMiscText(String newText)
	{
		miscText="";
		CMLib.coffeeMaker().setPropertiesStr(this,newText,false);
		recoverPhyStats();
	}

	@Override
	public CMObject copyOf()
	{
		final StdBoardable s=(StdBoardable)super.copyOf();
		s.destroyed=false;
		s.setOwnerName("");
		final String xml=CMLib.coffeeMaker().getAreaObjectXML(getShipArea(), null, null, null, true).toString();
		s.setShipArea(xml);
		s.setReadableText(readableText()); // in case this was first call to getShipArea()
		/* Should we rename?
		final Area A=s.getShipArea();
		final String num=Double.toString(Math.random());
		final int x=num.indexOf('.')+1;
		final int len=((num.length()-x)/2)+1;
		String oldName=A.Name();
		A.setName(L("UNNAMED_@x1",num.substring(x,x+len)));
		for(Enumeration<Room> r=A.getCompleteMap();r.hasMoreElements();)
		{
			final Room R=r.nextElement();
			if((R!=null)&&(R.roomID().startsWith(oldName)))
				R.setRoomID(A.Name()+R.roomID().substring(oldName.length()));
		}
		s.renameDestinationRooms(oldName,A.Name());
		*/
		//TODO: when you buy a ship, none of its electronics is registered.  This is bad.
		//CMLib.tech().unregisterAllElectronics(CMLib.tech().getElectronicsKey(s.getShipArea()));
		return s;
	}

	@Override
	public void stopTicking()
	{
		if(area!=null)
		{
			CMLib.threads().deleteAllTicks(area);
			final String key=CMLib.tech().getElectronicsKey(area);
			CMLib.tech().unregisterAllElectronics(key);
		}
		super.stopTicking();
		this.destroyed=false; // undo the weird thing
	}

	@Override
	protected Room getDestinationRoom(Room fromRoom)
	{
		getShipArea();
		Room R=null;
		final List<String> V=CMParms.parseSemicolons(readableText(),true);
		if((V.size()>0)&&(getShipArea()!=null))
			R=getShipArea().getRoom(V.get(CMLib.dice().roll(1,V.size(),-1)));
		return R;
	}
	
	protected void renameDestinationRooms(String from, String to)
	{
		getShipArea();
		final List<String> V=CMParms.parseSemicolons(readableText().toUpperCase(),true);
		final List<String> nV=new ArrayList<String>();
		from=from.toUpperCase();
		for(String s : V)
		{
			if(s.startsWith(from))
				s=to+s.substring(from.length());
			if(getShipArea().getRoom(s)!=null)
				nV.add(s);
		}
		if((nV.size()==0)&&(getShipArea().getProperMap().hasMoreElements()))
			nV.add(getShipArea().getProperMap().nextElement().roomID());
		setReadableText(CMParms.toSemicolonListString(nV));
	}

	@Override
	public void destroy()
	{
		if(area!=null)
			CMLib.map().obliterateArea(area);
		super.destroy();
	}

	@Override 
	public int getPrice() 
	{ 
		return price; 
	}
	
	@Override 
	public void setPrice(int price) 
	{ 
		this.price=price; 
	}

	@Override 
	public int value() 
	{ 
		int value = baseGoldValue();
		if(price > 0)
			value += price;
		getShipArea();
		return value + internalPrice;
	}
	
	@Override 
	public String getOwnerName() 
	{ 
		return ownerName; 
	}
	
	@Override 
	public void setOwnerName(String owner) 
	{ 
		this.ownerName=owner;
	}

	@Override
	public long expirationDate()
	{
		return super.expirationDate();
	}

	@Override
	public void setExpirationDate(long time)
	{
		if((time>0)&&(owner() instanceof Room))
			super.setExpirationDate(0);
		else
			super.setExpirationDate(time);
	}
		
	@Override
	public CMObject getOwnerObject()
	{
		final String owner=getOwnerName();
		if(owner.length()==0) 
			return null;
		final Clan C=CMLib.clans().getClan(owner);
		if(C!=null) 
			return C;
		return CMLib.players().getLoadPlayer(owner);
	}
	
	@Override 
	public String getTitleID() 
	{ 
		return this.toString(); 
	}

	@Override
	public void renameShip(String newName)
	{
		final Area area=getShipArea();
		if(area instanceof BoardableShip)
		{
			String oldName=area.Name();
			((BoardableShip)area).renameShip(newName);
			renameDestinationRooms(oldName,area.Name());
			setShipArea(CMLib.coffeeMaker().getAreaObjectXML(area, null, null, null, true).toString());
		}
		for(final String word : new String[]{"NAME","NEWNAME","SHIPNAME","SHIP"})
		{
			for(final String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(Name().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setName(CMStrings.replaceAll(Name(), rubs.charAt(0)+word+rubs.charAt(1), newName));
			}
			for(final String rubs : new String[]{"<>","[]","{}","()"})
			{
				if(displayText().indexOf(rubs.charAt(0)+word+rubs.charAt(1))>=0)
					setDisplayText(CMStrings.replaceAll(displayText(), rubs.charAt(0)+word+rubs.charAt(1), newName));
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
			
			return true;
		}
		return super.tick(ticking, tickID);
	}

	protected synchronized void destroyThisShip()
	{
		if((this.getOwnerName().length()>0)&&(!this.getOwnerName().startsWith("#")))
		{
			final Clan clan = CMLib.clans().getClan(this.getOwnerName());
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
		final Area A = getShipArea();
		if(A!=null)
		{
			for(final Enumeration<Room> r = A.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
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
			A.destroy();
		}
		destroy();
	}
	
	protected boolean okAreaMessage(final CMMsg msg, boolean outdoorOnly)
	{
		boolean failed = false;
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
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
	
	protected boolean securityCheck(final MOB mob)
	{
		if(mob==null)
			return false;
		if(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDITEMS))
			return true;
		if(getOwnerName().length()==0)
			return true;
		return CMLib.law().doesOwnThisProperty(mob, this);
	}

	protected void announceToShip(final String msgStr)
	{
		final MOB mob = CMClass.getFactoryMOB(name(),phyStats().level(),CMLib.map().roomLocation(this));
		try
		{
			final CMMsg msg2=CMClass.getMsg(mob, CMMsg.MSG_OK_ACTION, msgStr);
			final Room R=CMLib.map().roomLocation(this);
			if((R!=null) && (R.okMessage(mob, msg2) && this.okAreaMessage(msg2, false)))
			{
				R.send(mob, msg2); // this lets the source know, i guess
				this.sendAreaMessage(msg2, false); // this just sends to "others"
			}
		}
		finally
		{
			mob.destroy();
		}
	}
	
	protected void sendAreaMessage(final CMMsg msg, boolean outdoorOnly)
	{
		final Area ship=getShipArea();
		if(ship!=null)
		{
			for(final Enumeration<Room> r = ship.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if(((!outdoorOnly)||((R.domainType()&Room.INDOORS)==0))
				&&(R.roomID().length()>0))
					R.sendOthers(msg.source(), msg);
			}
		}
	}
	
	protected boolean confirmAreaMessage(final CMMsg msg, boolean outdoorOnly)
	{
		final Area itemArea=CMLib.map().areaLocation(this.getShipItem());
		final Area shipArea=getShipArea();
		if(itemArea == shipArea)
		{
			Log.errOut("Ship "+name()+" is inside itself?!");
			return false;
		}
		if(shipArea!=null)
		{
			for(final Enumeration<Room> r = shipArea.getProperMap(); r.hasMoreElements();)
			{
				final Room R=r.nextElement();
				if((!outdoorOnly)||((R.domainType()&Room.INDOORS)==0))
				{
					if(!R.okMessage(msg.source(), msg))
						return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
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
			return false;
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
				final Clan C=CMLib.clans().getClan(getOwnerName());
				if(C!=null)
				{
					C.getExtItems().delItem(this);
					CMLib.database().DBUpdateClanItems(C);
				}
			}
			setOwnerName("");
		}
		if(clanSale)
		{
			final Pair<Clan,Integer> targetClan=CMLib.clans().findPrivilegedClan(buyer, Clan.Function.PROPERTY_OWNER);
			if(targetClan!=null)
				setOwnerName(targetClan.first.clanID());
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
					session.println(L("\n\rEnter a new name for your ship: "));
				}

				@Override
				public void timedOut()
				{
				}

				@Override 
				public void callBack()
				{
					for(final Enumeration<BoardableShip> s=CMLib.map().ships();s.hasMoreElements();)
					{
						final BoardableShip ship=s.nextElement();
						if((ship!=null)&&(!ship.amDestroyed())&&(ship.getShipArea()!=null)&&(ship.getShipArea().Name().equalsIgnoreCase(this.input.trim())))
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
					CMLib.tech().unregisterAllElectronics(CMLib.tech().getElectronicsKey(me.getShipArea()));
					me.renameShip(this.input.trim());
					buyer.tell(L("@x1 is now signed over to @x2.",name(),getOwnerName()));
					final Room finalR=findNearestDocks(R);
					if(finalR==null)
					{
						Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of spaceport.");
						buyer.tell(L("Nowhere was found to dock your ship.  Please contact the administrators!."));
					}
					else
					{
						me.dockHere(finalR);
						buyer.tell(L("You'll find your ship docked at '@x1'.",finalR.displayText(buyer)));
					}
					Clan C;
					if(clanSale && ((C=CMLib.clans().getClan(me.getOwnerName()))!=null))
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
				final Clan C=CMLib.clans().getClan(getOwnerName());
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
					buyer.tell(L("However, there is no entity to actually take ownership.  Wierd."));
				}
			}
			final Room finalR=findNearestDocks(R);
			if(finalR==null)
				Log.errOut("Could not dock ship in area "+R.getArea().Name()+" due to lack of docks.");
			else
				dockHere(finalR);
		}
	}

	protected Room findNearestDocks(Room R)
	{
		return R;
	}
	
	@Override
	public String putString(Rider R)
	{
		if((R==null)||(putString.length()==0))
			return "load(s)";
		return putString;
	}

	@Override
	public String mountString(int commandType, Rider R)
	{
		if((R==null)||(mountString.length()==0))
			return "board(s)";
		return mountString;
	}

	@Override
	public String dismountString(Rider R)
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
