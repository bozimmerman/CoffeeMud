package com.planet_ink.coffee_mud.Items.Basic;
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
   Copyright 2003-2024 Bo Zimmerman

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

public class StdTitle extends StdItem implements LandTitle
{
	@Override
	public String ID()
	{
		return "StdTitle";
	}

	@Override
	public String displayText()
	{
		return "an official looking document sits here";
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("a title");
	}

	@Override
	public int baseGoldValue()
	{
		return getPrice();
	}

	protected static final String	CANCEL_WORD	= "CANCEL";

	@Override
	public int value()
	{
		if(name().indexOf("(Copy)")>=0)
			baseGoldValue=10;
		else
			baseGoldValue=getPrice();
		return baseGoldValue;
	}

	public void setBaseGoldValue(final int newValue)
	{
		setPrice(newValue);
	}

	public StdTitle()
	{
		super();
		setName("a standard title");
		setDescription("Give or Sell this title to transfer ownership. **DON`T LOSE THIS!**");
		baseGoldValue=10000;
		basePhyStats().setSensesMask(PhyStats.SENSE_ITEMREADABLE);
		setMaterial(RawMaterial.RESOURCE_PAPER);
		recoverPhyStats();
	}

	@Override
	public boolean allowsExpansionConstruction()
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return false;
		return A.allowsExpansionConstruction();
	}

	@Override
	public int getPrice()
	{
		final PrivateProperty P=fetchAPropertyRecord();
		if(P instanceof LandTitle)
			return P.getPrice()+((LandTitle)P).backTaxes();
		else
		if(P!=null)
			return P.getPrice();
		return 0;
	}

	@Override
	public LandTitle generateNextRoomTitle()
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return this;
		return A.generateNextRoomTitle();
	}

	@Override
	public void setPrice(final int price)
	{
		final PrivateProperty P=fetchAPropertyRecord();
		if(P!=null)
			P.setPrice(price);
		if(P instanceof LandTitle)
			((LandTitle)P).updateTitle();
	}

	@Override
	public void setBackTaxes(final int amount)
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return;
		A.setBackTaxes(amount);
		A.updateTitle();
	}

	@Override
	public int backTaxes()
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return 0;
		return A.backTaxes();
	}

	@Override
	public boolean allowTheft()
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return false;
		return A.allowTheft();
	}

	@Override
	public void setAllowTheft(final boolean allow)
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return;
		A.setAllowTheft(allow);
		A.updateTitle();
	}

	@Override
	public void setGridLayout(final boolean layout)
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return;
		A.setGridLayout(layout);
		A.updateTitle();
	}

	@Override
	public boolean gridLayout()
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return false;
		return A.gridLayout();
	}

	@Override
	public boolean rentalProperty()
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return false;
		return A.rentalProperty();
	}

	@Override
	public String getUniqueLotID()
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return "";
		return A.getUniqueLotID();
	}

	@Override
	public void setRentalProperty(final boolean truefalse)
	{
		final LandTitle A=fetchALandTitle();
		if(A==null)
			return;
		A.setRentalProperty(truefalse);
		A.updateTitle();
	}

	@Override
	public boolean isProperlyOwned()
	{
		final PrivateProperty P=fetchAPropertyRecord();
		if(P==null)
			return false;
		final String owner=P.getOwnerName();
		if(owner.length()==0)
			return false;
		final Clan C=CMLib.clans().fetchClanAnyHost(owner);
		if(C!=null)
			return true;
		return CMLib.players().playerExistsAllHosts(owner);
	}

	@Override
	public String getOwnerName()
	{
		final PrivateProperty P=fetchAPropertyRecord();
		if(P!=null)
			return P.getOwnerName();
		return "";
	}

	@Override
	public void setOwnerName(final String owner)
	{
		final PrivateProperty P=fetchAPropertyRecord();
		if(P==null)
			return;
		P.setOwnerName(owner);
		if(P instanceof LandTitle)
			((LandTitle)P).updateTitle();
	}

	public LandTitle fetchALandTitle()
	{
		return CMLib.law().getLandTitle(getATitledRoom());
	}

	public PrivateProperty fetchAPropertyRecord()
	{
		final Room R=CMLib.map().getRoom(landPropertyID());
		if(R!=null)
		{
			final PrivateProperty A=CMLib.law().getPropertyRecord(R);
			if(A!=null)
				return A;
		}
		final Area area=CMLib.map().getArea(landPropertyID());
		if(area!=null)
		{
			final PrivateProperty A=CMLib.law().getPropertyRecord(area);
			if(A!=null)
				return A;
		}
		final Boardable ship = CMLib.map().getShip(landPropertyID());
		if(ship instanceof PrivateProperty)
			return (PrivateProperty)ship;
		return CMLib.law().getLandTitle(getATitledRoom());
	}

	@Override
	public String landPropertyID()
	{
		return text();
	}

	public void updateTitleName()
	{
		if(!_name.startsWith("the title to"))
		{
			final int num = getNumConnectedPropertyRooms();
			if((num<2)
			||(CMLib.map().getArea(landPropertyID())!=null)
			||(CMLib.map().getShip(landPropertyID())!=null))
				setName("the title to "+landPropertyID());
			else
			{
				final Room R = this.getATitledRoom();
				setName("the title to rooms around "+CMLib.map().getExtendedRoomID(R));
			}
		}
	}

	@Override
	public void setLandPropertyID(final String landID)
	{
		setMiscText(landID);
		updateTitleName();
	}

	@Override
	public void updateLot(final Set<String> optPlayerList)
	{
		for(final Room R : getTitledRooms())
		{
			final LandTitle T=CMLib.law().getLandTitle(R);
			if(T!=null)
				T.updateLot(optPlayerList);
		}
	}

	@Override
	public void updateTitle()
	{
		final LandTitle T=fetchALandTitle();
		if(T!=null)
			T.updateTitle();
	}

	protected LandTitle getLandTitleObject()
	{
		final Room R=CMLib.map().getRoom(landPropertyID());
		if(R!=null)
		{
			final LandTitle A=CMLib.law().getLandTitle(R);
			if(A!=null)
				return A;
		}
		final Area area=CMLib.map().getArea(landPropertyID());
		if(area!=null)
		{
			final LandTitle A=CMLib.law().getLandTitle(area);
			if(A!=null)
				return A;
		}
		return null;
	}

	@Override
	public Room getAConnectedPropertyRoom()
	{
		final LandTitle T = getLandTitleObject();
		if(T!=null)
			return T.getAConnectedPropertyRoom();
		return null;
	}

	@Override
	public int getNumConnectedPropertyRooms()
	{
		final LandTitle T = getLandTitleObject();
		if(T!=null)
			return T.getNumConnectedPropertyRooms();
		return 0;
	}

	@Override
	public List<Room> getTitledRooms()
	{
		final LandTitle T = getLandTitleObject();
		if(T!=null)
			return T.getTitledRooms();
		return new Vector<Room>(1);
	}

	@Override
	public int getNumTitledRooms()
	{
		final LandTitle T = getLandTitleObject();
		if(T!=null)
			return T.getNumTitledRooms();
		return 0;
	}

	@Override
	public Room getATitledRoom()
	{
		final LandTitle T = getLandTitleObject();
		if(T!=null)
			return T.getATitledRoom();
		return null;
	}

	@Override
	public String getTitleID()
	{
		final PrivateProperty P = this.fetchAPropertyRecord();
		if(P != null)
			return P.getTitleID();
		return "";
	}

	@Override
	public void recoverPhyStats()
	{
		CMLib.flags().setReadable(this, true);
		super.recoverPhyStats();
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMinor()==CMMsg.TYP_WRITE)||(msg.targetMinor()==CMMsg.TYP_REWRITE))
		&&(msg.amITarget(this)))
		{
			final MOB mob=msg.source();
			mob.tell(L("You shouldn't write on @x1.",name()));
			return false;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool() instanceof ShopKeeper))
		{
			final PrivateProperty P = this.fetchAPropertyRecord();
			if(P==null)
			{
				destroy();
				msg.source().tell(L("You can't buy that."));
				return false;
			}
			if((P instanceof LandTitle) && (P.getOwnerName().length()==0))
			{
				final Area AREA=CMLib.map().getArea(((LandTitle)P).landPropertyID());
				if((AREA!=null)&&(AREA.Name().indexOf("UNNAMED")>=0)&&(msg.source().isMonster()))
					return false;
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_BUY)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==this))
		{
			final PrivateProperty P = this.fetchAPropertyRecord();
			if((P!=null)&&(P.getOwnerName().length()>0))
			{
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.target());
				if(((SK.isSold(ShopKeeper.DEAL_CLANBANKER)||SK.isSold(ShopKeeper.DEAL_CLANDSELLER)||SK.isSold(ShopKeeper.DEAL_CSHIPSELLER))
					&&(msg.source().getClanRole(P.getOwnerName())==null))
				||(((SK.isSold(ShopKeeper.DEAL_BANKER))||(SK.isSold(ShopKeeper.DEAL_CLANBANKER)))
					&&(!P.getOwnerName().equals(msg.source().Name())))
				||(((SK.isSold(ShopKeeper.DEAL_POSTMAN))||(SK.isSold(ShopKeeper.DEAL_CLANPOSTMAN)))
					&&(!P.getOwnerName().equals(msg.source().Name()))))
				{
					final String str=L("I'm sorry, '@x1 is not for sale.  It already belongs to @x2.  It should be destroyed.",msg.tool().Name(),P.getOwnerName());
					if(((MOB)msg.target()).isMonster())
						CMLib.commands().postSay((MOB)msg.target(),msg.source(),str,false,false);
					else
						((MOB)msg.target()).tell(L("@x1 You might want to tell the customer.",str));
					SK.getShop().removeStock(Name(),msg.source());
					destroy();
					return false;
				}

			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_WITHDRAW)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==this))
		{
			final PrivateProperty P = this.fetchAPropertyRecord();
			if((P!=null)
			&&((P.getOwnerName().length()==0)
			||((P.getOwnerName().length()>0)
				&&(!P.getOwnerName().equals(msg.source().Name()))
				&&(!((msg.source().isMarriedToLiege())&&(P.getOwnerName().equals(msg.source().getLiegeID()))))
				&&(msg.source().getClanRole(P.getOwnerName())==null))))
			{
				final String str=L("I'm sorry, '@x1 must be destroyed.",msg.tool().Name());
				if(((MOB)msg.target()).isMonster())
					CMLib.commands().postSay((MOB)msg.target(),msg.source(),str,false,false);
				else
					((MOB)msg.target()).tell(L("@x1 You might want to tell the customer.",str));
				final ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(msg.target());
				if(SK!=null)
					SK.getShop().removeStock(msg.tool().Name(),msg.source());
				destroy();
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	private void removeBoardableProperty(final MOB buyer, final PrivateProperty P)
	{
		if(P instanceof Boardable)
		{
			final Item I=((Boardable)P).getBoardableItem();
			if((I!=null)
			&&(isProperlyOwned()))
			{
				CMObject owner = CMLib.clans().getClanExact(getOwnerName());
				if(owner instanceof Clan)
				{
					final Clan C=(Clan)owner;
					C.getExtItems().delItem(I);
					if(P instanceof LandTitle)
					{
						final Room R=((LandTitle)P).getATitledRoom();
						if(R != null)
							CMLib.achievements().possiblyBumpAchievement(buyer, AchievementLibrary.Event.CLANPROPERTY, -1, C, R);
					}
				}
				else
				{
					owner = CMLib.players().getLoadPlayer(getOwnerName());
					if(owner instanceof MOB)
					{
						final PlayerStats pStats = ((MOB)owner).playerStats();
						if(pStats != null)
							pStats.getExtItems().delItem(I);
					}
				}
			}
		}
	}

	private void addBoardableProperty(final PrivateProperty P, final CMObject obj)
	{
		if(P instanceof Boardable)
		{
			final Item I=((Boardable)P).getBoardableItem();
			if(I!=null)
			{
				final CMObject owner = obj;
				if(owner instanceof Clan)
				{
					final Clan C=(Clan)owner;
					if(!C.getExtItems().isContent(I))
					{
						I.setSavable(false); // if the clan is saving it, rooms are NOT.
						C.getExtItems().addItem(I);
					}
				}
				else
				if(owner instanceof MOB)
				{
					final PlayerStats pStats = ((MOB)owner).playerStats();
					if((pStats != null)
					&&(!pStats.getExtItems().isContent(I)))
					{
						I.setSavable(false); // if the pstats is saving it, rooms are NOT.
						pStats.getExtItems().addItem(I);
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(this))
		&&(msg.targetMinor()==CMMsg.TYP_READ))
		{
			if(CMLib.flags().canBeSeenBy(this,msg.source()))
			{
				if((landPropertyID()==null)||(landPropertyID().length()==0))
					msg.source().tell(L("It appears to be a blank property title."));
				else
				if((getOwnerName()==null)||(getOwnerName().length()==0))
					msg.source().tell(L("It states that the property herein known as '@x1' is available for ownership.",landPropertyID()));
				else
					msg.source().tell(L("It states that the property herein known as '@x1' is deeded to @x2.",landPropertyID(),getOwnerName()));
			}
			else
				msg.source().tell(L("You can't see that!"));
			msg.modify(msg.source(),msg.target(),msg.tool(),msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),CANCEL_WORD,msg.othersCode(),msg.othersMessage());
		}

		super.executeMsg(myHost,msg);

		if((msg.targetMinor()==CMMsg.TYP_SELL)
		&&(msg.tool()==this)
		&&(msg.target() instanceof ShopKeeper))
		{
			final PrivateProperty P = this.fetchAPropertyRecord();
			if(P==null)
			{
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
				destroy();
				return;
			}
			removeBoardableProperty(msg.source(), P);
			P.setOwnerName("");
			if(P instanceof LandTitle)
			{
				updateTitle();
				updateLot(null);
			}
			recoverPhyStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool()==this)
		&&(getOwnerName().length()>0)
		&&((msg.source().Name().equals(getOwnerName()))
			||(msg.source().getLiegeID().equals(getOwnerName())&&msg.source().isMarriedToLiege())
			||(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER)))
		&&(msg.target() instanceof MOB)
		&&(!(msg.target() instanceof Banker))
		&&(!(msg.target() instanceof Auctioneer))
		&&(!(msg.target() instanceof Librarian))
		&&(!(msg.target() instanceof PostOffice)))
		{
			final PrivateProperty P = this.fetchAPropertyRecord();
			if(P==null)
			{
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
				destroy();
				return;
			}
			final CMMsg buyMsg = CMClass.getMsg((MOB)msg.target(),msg.source(),this,CMMsg.MSG_BUY,CMMsg.MSG_BUY,CMMsg.MSG_BUY,"");
			if(!msg.target().okMessage(msg.target(), buyMsg))
			{
				msg.source().tell(L("@x1 can not seem to accept @x2.",((MOB)msg.target()).name(msg.source()),name()));
				return;
			}

			if(CMLib.clans().checkClanPrivilege(msg.source(), getOwnerName(), Clan.Function.PROPERTY_OWNER))
			{
				final Pair<Clan,Integer> targetClan=CMLib.clans().findPrivilegedClan((MOB)msg.target(), Clan.Function.PROPERTY_OWNER);
				if(targetClan!=null)
				{
					removeBoardableProperty(msg.source(), P);
					addBoardableProperty(P,targetClan.first);
					P.setOwnerName(targetClan.first.clanID());
					final Room R=getATitledRoom();
					if(R != null)
						CMLib.achievements().possiblyBumpAchievement((MOB)msg.target(), AchievementLibrary.Event.CLANPROPERTY, 1, targetClan, R);
				}
				else
				if(((MOB)msg.target()).getClanRole(getOwnerName())!=null)
				{
					// do nothing
					return;
				}
				else
				{
					removeBoardableProperty(msg.source(), P);
					addBoardableProperty(P,msg.target());
					P.setOwnerName(msg.target().Name());
				}
			}
			else
			{
				removeBoardableProperty(msg.source(), P);
				addBoardableProperty(P,msg.target());
				P.setOwnerName(msg.target().Name());
			}
			msg.source().tell(L("@x1 is now signed over to @x2.",name(),P.getOwnerName()));
			if(P instanceof LandTitle)
			{
				((LandTitle)P).setBackTaxes(0);
				updateTitle();
				updateLot(null);
				if(((LandTitle)P).rentalProperty())
					msg.source().tell(L("This property is a rental.  Your rent will be paid every mud-month out of your bank account."));
				else
				{
					final Room R=getATitledRoom();
					final LegalBehavior B=CMLib.law().getLegalBehavior(R);
					if(B!=null)
					{
						final Area A2=CMLib.law().getLegalObject(R);
						if(A2==null)
							Log.errOut("StdTitle",CMLib.map().getExtendedRoomID(R)+" has a legal behavior, but no area!");
						else
						{
							final Law theLaw=B.legalInfo(A2);
							if(theLaw==null)
								Log.errOut("StdTitle",A2.Name()+" has no law.");
							else
							{
								final String taxs=(String)theLaw.taxLaws().get("PROPERTYTAX");
								if((taxs!=null)&&(taxs.length()==0)&&(CMath.s_double(taxs)>0.0))
									msg.source().tell(L("A property tax of @x1% of @x2 will be paid monthly out of your bank account.",""+CMath.s_double(taxs),""+P.getPrice()));
							}
						}
					}
				}
			}
			recoverPhyStats();
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.amITarget(this))
		&&(msg.tool() instanceof ShopKeeper))
		{
			final PrivateProperty P = this.fetchAPropertyRecord();
			if(P==null)
			{
				Log.errOut("StdTitle","Unsellable room: "+landPropertyID());
				destroy();
				return;
			}
			if(P.getOwnerName().length()==0)
			{
				String newOwnerName=msg.source().Name();
				if((((ShopKeeper)msg.tool()).isSold(ShopKeeper.DEAL_CLANDSELLER))
				||(((ShopKeeper)msg.tool()).isSold(ShopKeeper.DEAL_CSHIPSELLER)))
				{
					final Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(msg.source(), Clan.Function.PROPERTY_OWNER);
					if(clanPair!=null)
					{
						newOwnerName=clanPair.first.clanID();
						addBoardableProperty(P,clanPair.first);
						final Room R=getATitledRoom();
						if(R != null)
							CMLib.achievements().possiblyBumpAchievement(msg.source(), AchievementLibrary.Event.CLANPROPERTY, 1, clanPair.first, R);
					}
					else
						addBoardableProperty(P,msg.source());
				}
				else
					addBoardableProperty(P,msg.source());

				P.setOwnerName(newOwnerName);
				if(P.getOwnerName().length()>0)
				{
					setBackTaxes(0);
					if(P instanceof LandTitle)
					{
						updateTitle();
						updateLot(null);
					}
					msg.source().tell(L("@x1 is now signed over to @x2.",name(),P.getOwnerName()));
				}
			}
			recoverPhyStats();
		}
	}
}
