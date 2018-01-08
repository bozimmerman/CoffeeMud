package com.planet_ink.coffee_mud.MOBS;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.MiniJSON.MJSONException;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
 Copyright 2017-2018 Bo Zimmerman

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

public class StdLibrarian extends StdShopKeeper implements Librarian
{
	@Override
	public String ID()
	{
		return "StdLibrarian";
	}

	protected volatile boolean		shopApply				= false;
	protected volatile long			lastShopTime			= 0;
	protected volatile CoffeeShop	curShop					= null;

	protected double				overdueCharge			= DEFAULT_MIN_OVERDUE_CHARGE;
	protected double				overdueChargePct		= DEFAULT_PCT_OVERDUE_CHARGE;
	protected double				dailyOverdueCharge		= DEFAULT_MIN_OVERDUE_DAILY;
	protected double				dailyOverdueChargePct	= DEFAULT_PCT_OVERDUE_DAILY;
	protected int					minOverdueDays			= DEFAULT_MIN_OVERDUE_DAYS;
	protected int					maxOverdueDays			= DEFAULT_MAX_OVERDUE_DAYS;
	protected int					maxBorrowedItems		= DEFAULT_MAX_BORROWED;
	protected String				contributorMask			= "";

	public StdLibrarian()
	{
		super();
		username = "a librarian";
		setDescription("She\\`s just waiting for you to say something so she can shush you!");
		setDisplayText("The librarian is ready to help.");
		CMLib.factions().setAlignment(this, Faction.Align.GOOD);
		setMoney(0);
		whatIsSoldMask = ShopKeeper.DEAL_POSTMAN;
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE, 18);
		baseCharStats().setStat(CharStats.STAT_CHARISMA, 7);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	public static class CheckedOutRecord
	{
		public String	playerName		= "";
		public String	itemName		= "";
		public long		mudDueDateMs	= 0;
		public double	charges			= 0.0;
		public long		mudReclaimDateMs= 0;
	}

	protected String getLibraryRecordKey()
	{
		return "LIBRARY_RECORDS_" + this.libraryChain().toUpperCase().replace(' ', '_');
	}

	protected String getLibraryShopKey()
	{
		return "LIBRARY_SHOP_" + this.libraryChain().toUpperCase().replace(' ', '_');
	}

	protected List<CheckedOutRecord> getCheckedOutRecords()
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<CheckedOutRecord> records = (List) Resources.getResource(this.getLibraryRecordKey());
		if (records == null)
		{
			records = new SVector<CheckedOutRecord>();
			Resources.submitResource(this.getLibraryRecordKey(), records);
			final XMLLibrary xml = CMLib.xml();
			synchronized (records)
			{
				final List<PlayerData> pData = CMLib.database().DBReadPlayerDataEntry(this.getLibraryRecordKey());
				for (final PlayerData data : pData)
				{
					try
					{
						for (XMLLibrary.XMLTag tag : xml.parseAllXML(data.xml()))
						{
							if (tag.tag().equalsIgnoreCase("OBJECT"))
							{
								CheckedOutRecord r = new CheckedOutRecord();
								xml.fromXMLtoPOJO(tag.contents(), r);
								records.add(r);
							}
						}
					}
					catch (IllegalArgumentException e)
					{
						Log.errOut(getLibraryRecordKey(), e);
					}
				}
			}
		}
		return records;
	}

	public List<CheckedOutRecord> getAllMyRecords(final String name)
	{
		List<CheckedOutRecord> recs = this.getCheckedOutRecords();
		List<CheckedOutRecord> myRecs = new ArrayList<CheckedOutRecord>();
		for (CheckedOutRecord rec : recs)
		{
			if (rec.playerName.equalsIgnoreCase(name))
				myRecs.add(rec);
		}
		return myRecs;
	}

	public CheckedOutRecord getRecord(final String playerName, final String itemName)
	{
		List<CheckedOutRecord> recs = this.getCheckedOutRecords();
		for (CheckedOutRecord rec : recs)
		{
			if (rec.playerName.equalsIgnoreCase(playerName) && (rec.itemName.equalsIgnoreCase(itemName)))
				return rec;
		}
		return null;
	}

	public List<CheckedOutRecord> getItemRecords(final String itemName)
	{
		List<CheckedOutRecord> recs = this.getCheckedOutRecords();
		List<CheckedOutRecord> myRecs = new ArrayList<CheckedOutRecord>();
		for (CheckedOutRecord rec : recs)
		{
			if (rec.itemName.equalsIgnoreCase(itemName))
				myRecs.add(rec);
		}
		return myRecs;
	}

	protected double getTotalOverdueCharges(final String name)
	{
		List<CheckedOutRecord> recs = this.getAllMyRecords(name);
		double totalDue = 0.0;
		for (int i = 0; i < recs.size(); i++)
		{
			try
			{
				totalDue += recs.get(i).charges;
			}
			catch (java.lang.IndexOutOfBoundsException e)
			{
			}
		}
		return totalDue;
	}

	protected void updateCheckedOutRecords()
	{
		final long[] lastChanges = getRecordChangeIndexes();
		synchronized (lastChanges)
		{
			lastChanges[0] = System.currentTimeMillis();
		}
		List<CheckedOutRecord> records = this.getCheckedOutRecords();
		final StringBuilder json = new StringBuilder("");
		final XMLLibrary xml = CMLib.xml();
		for (int r = 0; r < records.size(); r++)
		{
			try
			{
				final CheckedOutRecord record = records.get(r);
				final String subXML = xml.fromPOJOtoXML(record);
				json.append("<OBJECT>");
				json.append(subXML);
				json.append("</OBJECT>");
			}
			catch (Exception e)
			{
				Log.errOut(getLibraryRecordKey(), e);
			}
		}
		CMLib.database().DBReCreatePlayerData(getLibraryRecordKey(), "LIBRARY_RECORDS", getLibraryRecordKey(), json.toString());
	}

	@Override
	public String libraryChain()
	{
		return text();
	}

	@Override
	public void setLibraryChain(String name)
	{
		setMiscText(name);
	}

	@Override
	public double getOverdueCharge()
	{
		return overdueCharge;
	}

	@Override
	public void setOverdueCharge(double charge)
	{
		overdueCharge = charge;
	}

	@Override
	public double getDailyOverdueCharge()
	{
		return dailyOverdueCharge;
	}

	@Override
	public void setDailyOverdueCharge(double charge)
	{
		dailyOverdueCharge = charge;
	}

	@Override
	public double getOverdueChargePct()
	{
		return overdueChargePct;
	}

	@Override
	public void setOverdueChargePct(double pct)
	{
		overdueChargePct = pct;
	}

	@Override
	public double getDailyOverdueChargePct()
	{
		return dailyOverdueChargePct;
	}

	@Override
	public void setDailyOverdueChargePct(double pct)
	{
		dailyOverdueChargePct = pct;
	}

	@Override
	public int getMinOverdueDays()
	{
		return minOverdueDays;
	}

	@Override
	public void setMinOverdueDays(int days)
	{
		minOverdueDays = days;
	}

	@Override
	public int getMaxOverdueDays()
	{
		return maxOverdueDays;
	}

	@Override
	public void setMaxOverdueDays(int days)
	{
		maxOverdueDays = days;
	}

	@Override
	public String contributorMask()
	{
		return contributorMask;
	}

	@Override
	public void setContributorMask(String mask)
	{
		contributorMask = mask;
	}

	@Override
	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
	}

	protected TimeClock getMyClock()
	{
		final Room room = this.getStartRoom();
		if (room == null)
			return null;
		final Area area = room.getArea();
		if (area == null)
			return null;
		return area.getTimeObj();
	}

	protected long[] getRecordChangeIndexes()
	{
		final long[] lastChange = (long[]) Resources.getResource(this.getLibraryShopKey());
		if (lastChange != null)
			return lastChange;
		final long[] lastChange2 = new long[2];
		Resources.submitResource(this.getLibraryShopKey(), lastChange2);
		return lastChange2;
	}

	@Override
	protected void doInventoryReset()
	{
		this.lastShopTime = 0;
	}

	@Override
	public CoffeeShop getShop()
	{
		if (shopApply)
		{
			final long[] lastChanges = getRecordChangeIndexes();
			final long lastChangeMs;
			synchronized (lastChanges)
			{
				lastChangeMs = lastChanges[0];
			}

			if ((this.lastShopTime < lastChangeMs) || (curShop == null))
			{
				this.lastShopTime = lastChangeMs;
				curShop = (CoffeeShop) shop.copyOf();
				final List<CheckedOutRecord> records = this.getCheckedOutRecords();
				for (int i = 0; i < records.size(); i++)
				{
					try
					{
						CheckedOutRecord rec = records.get(i);
						if (rec.itemName.length() > 0)
							curShop.lowerStock("$" + rec.itemName + "$");
					}
					catch (java.lang.IndexOutOfBoundsException e)
					{
					}
				}

			}
			return curShop;
		}
		else
			return shop;
	}

	@Override
	public void destroy()
	{
		super.destroy();
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if (!super.tick(ticking, tickID))
			return false;
		if (!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;

		if ((tickID == Tickable.TICKID_MOB) && (getStartRoom() != null))
		{
			final long[] lastChangeMs = this.getRecordChangeIndexes();
			boolean doMaintenance = false;
			synchronized (lastChangeMs)
			{
				if (System.currentTimeMillis() > lastChangeMs[1])
				{
					final TimeClock clock = getMyClock();
					lastChangeMs[1] = System.currentTimeMillis() + (CMProps.getMillisPerMudHour() * (clock==null?1:clock.getHoursInDay()));
					doMaintenance = true;
				}
			}
			if (doMaintenance)
			{
				final List<CheckedOutRecord> recs = this.getCheckedOutRecords();
				final Map<String, Boolean> namesChecked = new TreeMap<String, Boolean>();
				boolean recordsChanged = false;
				for (int i = 0; i < recs.size(); i++)
				{
					final CheckedOutRecord rec;
					try
					{
						rec = recs.get(i);
						if (rec.playerName.length() == 0)
						{
							recs.remove(rec);
							i--;
							recordsChanged = true;
							continue;
						}
						if (!namesChecked.containsKey(rec.playerName))
							namesChecked.put(rec.playerName, Boolean.valueOf(CMLib.players().playerExists(rec.playerName)));
						if (!namesChecked.get(rec.playerName).booleanValue())
						{
							recs.remove(rec);
							i--;
							recordsChanged = true;
							continue;
						}
						final boolean recordChanged = processCheckedOutRecord(rec);
						recordsChanged = recordsChanged || recordChanged;
					}
					catch (IndexOutOfBoundsException e)
					{
					}
				}
				if (recordsChanged)
					this.updateCheckedOutRecords();
			}
		}
		return true;
	}

	protected boolean processCheckedOutRecord(final CheckedOutRecord rec)
	{
		final TimeClock clock = getMyClock();
		final long nowTime = (clock != null) ? clock.toHoursSinceEpoc() : 0;
		if ((clock == null) || (nowTime == 0))
			return false;
		boolean recordsChanged=false;
		if (rec.itemName.length() > 0)
		{
			Environmental stockItem = null;
			if (System.currentTimeMillis() > rec.mudDueDateMs)
			{
				stockItem = shop.getStock("$" + rec.itemName + "$", null);
				final ShopKeeper.ShopPrice P = CMLib.coffeeShops().pawningPrice(this, null, stockItem, this, shop);
				final double value = (P!=null)? P.absoluteGoldPrice : 0;
				double newCharges = this.getOverdueCharge();
				if (value > 0)
					newCharges += CMath.mul(value, this.getOverdueChargePct());
				final long hrsDiff = Math.round(Math.floor((System.currentTimeMillis() - rec.mudDueDateMs)/CMProps.getMillisPerMudHour()));
				if (hrsDiff > 0)
				{
					double daysPast = CMath.floor(CMath.div(hrsDiff, (double) clock.getHoursInDay()));
					if (daysPast > 0)
					{
						newCharges += (daysPast * this.getDailyOverdueCharge());
						if (value > 0)
							newCharges += (daysPast * value * this.getDailyOverdueChargePct());
					}
				}
				if (newCharges != rec.charges)
				{
					rec.charges = newCharges;
					recordsChanged = true;
				}
			}
			if (System.currentTimeMillis() > rec.mudReclaimDateMs)
			{
				if(stockItem == null)
					stockItem = shop.getStock("$" + rec.itemName+"$", null);
				final ShopKeeper.ShopPrice P = CMLib.coffeeShops().pawningPrice(this, null, stockItem, this, shop);
				final double value = (P!=null)? P.absoluteGoldPrice : 0;
				if(rec.charges < value)
					rec.charges = value;
				rec.itemName = ""; // the item is now
									// reclaimed!
				recordsChanged = true;
			}
		}
		return recordsChanged;
	}
	
	public void autoGive(MOB src, MOB tgt, Item I)
	{
		CMMsg msg2 = CMClass.getMsg(src, I, null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null);
		location().send(this, msg2);
		msg2 = CMClass.getMsg(tgt, I, null, CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null);
		location().send(this, msg2);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob = msg.source();
		if (msg.source().isPlayer() && (!shopApply) && ((msg.source().location() == location()) || (msg.sourceMinor() == CMMsg.TYP_ENTER)) && (!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS)))
			shopApply = true;
		if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					if (msg.tool() instanceof Container)
						((Container) msg.tool()).emptyPlease(true);
					final Session S = msg.source().session();
					if ((!msg.source().isMonster()) && (S != null) && (msg.tool() instanceof Item))
					{
						autoGive(msg.source(), this, (Item) msg.tool());
						if (msg.tool() instanceof Coins)
						{
							double totalGiven = ((Coins) msg.tool()).getTotalValue();
							double totalDue = getTotalOverdueCharges(msg.source().Name());
							if (totalDue > 0.0)
							{
								double totalPaidDown = totalDue;
								boolean recordUpdated = false;
								for (CheckedOutRecord rec : this.getAllMyRecords(msg.source().Name()))
								{
									if (rec.charges > 0)
									{
										if (totalPaidDown >= rec.charges)
										{
											totalPaidDown -= rec.charges;
											rec.charges = 0.0;
											recordUpdated = true;
											if (rec.itemName.length() == 0)
												this.getCheckedOutRecords().remove(rec);
										}
										else if (totalPaidDown > 0.0)
										{
											rec.charges -= totalPaidDown;
											totalPaidDown = 0.0;
											recordUpdated = true;
										}
									}
								}
								if (recordUpdated)
									this.updateCheckedOutRecords();
								msg.tool().destroy();
								String totalAmount = CMLib.beanCounter().nameCurrencyShort(this, totalDue);
								CMLib.commands().postSay(this, mob, L("Your total overdue charges were @x1.", totalAmount), true, false);
								if (totalGiven > totalDue)
									CMLib.beanCounter().giveSomeoneMoney(this, msg.source(), totalGiven - totalDue);
								else if (totalPaidDown > 0)
								{
									String totalStillDue = CMLib.beanCounter().nameCurrencyShort(this, totalPaidDown);
									CMLib.commands().postSay(this, mob, L("Your still owe @x1.", totalStillDue), true, false);
								}
							}
							else
								CMLib.commands().postSay(this, mob, L("You didn't have any overdue charges, so thanks for the donation!"), true, false);
						}
						else
						if (isMine(msg.tool()))
						{
							CheckedOutRecord rec = this.getRecord(msg.source().Name(), msg.tool().Name());
							if ((rec == null) && (msg.source().amFollowing() != null))
								rec = this.getRecord(msg.source().amFollowing().Name(), msg.tool().Name());
							if ((rec == null) && (msg.source().isMonster()) && (msg.source().getStartRoom() != null))
							{
								final String name = CMLib.law().getPropertyOwnerName(msg.source().getStartRoom());
								if (name.length() > 0)
									rec = this.getRecord(name, msg.tool().Name());
							}
							if (rec == null)
							{
								List<CheckedOutRecord> recs = this.getItemRecords(msg.tool().Name());
								for (int i = 0; i < recs.size(); i++)
								{
									if (recs.get(i).playerName.length() > 0)
									{
										rec = recs.get(i);
										break;
									}
								}
								if (rec != null)
									CMLib.commands().postSay(this, mob, L("I assume you are returning this for @x1.", rec.playerName), true, false);
							}
							if (rec == null)
							{
								CMLib.commands().postSay(this, mob, L("What is this?!"), true, false);
								return;
							}
							msg.tool().destroy(); // it's almost done being
													// returned!
							if (rec.charges > 0.0)
							{
								String amount = CMLib.beanCounter().nameCurrencyShort(this, rec.charges);
								if (CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(), this) < rec.charges)
								{
									if (!msg.source().Name().equalsIgnoreCase(rec.playerName))
										CMLib.commands().postSay(this, mob, L("Charges due for this are @x2.  @x1 must directly pay this fee to me.", rec.playerName, amount), true, false);
									else
										CMLib.commands().postSay(this, mob, L("Charges due for this are @x2.  You must come back and pay this fee to me.", rec.playerName, amount), true, false);
									rec.itemName = "";
									this.updateCheckedOutRecords();
								}
								else
								{
									CMLib.beanCounter().subtractMoney(mob, CMLib.beanCounter().getCurrency(this), rec.charges);
									CMLib.commands().postSay(this, mob, L("Charges due for this were @x1.  Thank you!", amount), true, false);
									//CMLib.beanCounter().subtractMoneyGiveChange(this, msg.source(), rec.charges);
									rec.charges = 0.0;
									rec.itemName = "";
									rec.playerName = "";
									this.getCheckedOutRecords().remove(rec);
									this.updateCheckedOutRecords();
								}
							}
							else
							{
								CMLib.commands().postSay(this, mob, L("Thank you!", rec.playerName), true, false);
								rec.charges = 0.0;
								rec.itemName = "";
								rec.playerName = "";
								this.getCheckedOutRecords().remove(rec);
								this.updateCheckedOutRecords();
							}
						}
					}
				}
				return;
			case CMMsg.TYP_BORROW:
			case CMMsg.TYP_WITHDRAW:
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final Item old = (Item) msg.tool();
					if ((getRecord(msg.source().Name(), old.Name()) == null) && (msg.source().isPlayer()))
					{
						final TimeClock clock = getMyClock();
						if (clock != null)
						{
							final long millisPerMudDay = clock.getHoursInDay() * CMProps.getMillisPerMudHour();
							final CheckedOutRecord rec = new CheckedOutRecord();
							final TimeClock minClock = (TimeClock) clock.copyOf();
							minClock.bumpDays(this.getMinOverdueDays());
							rec.itemName = old.Name();
							rec.charges = 0.0;
							rec.playerName = msg.source().Name();
							rec.mudDueDateMs = System.currentTimeMillis() + (millisPerMudDay * this.getMinOverdueDays());
							rec.mudReclaimDateMs = System.currentTimeMillis() + (millisPerMudDay * this.getMaxOverdueDays());
							final CoffeeShop shop = this.getShop();
							if (shop != this.shop) // never borrow from the main
													// library
							{
								List<Environmental> items = shop.removeSellableProduct("$" + old.Name() + "$", mob);
								CMLib.commands().postSay(this, mob, L("There ya go! This is due back here by @x1!", minClock.getShortestTimeDescription()), true, false);
								for (Environmental E : items)
								{
									if (E instanceof Item)
									{
										Item I = (Item) E;
										if (location() != null)
											location().addItem(I, ItemPossessor.Expire.Player_Drop);
										final CMMsg msg2 = CMClass.getMsg(mob, I, this, CMMsg.MSG_GET, null);
										if (location().okMessage(mob, msg2))
											location().send(mob, msg2);
									}
								}
								this.getCheckedOutRecords().add(rec);
								this.updateCheckedOutRecords();
							}
						}
					}
				}
				return;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_SPEAK:
			{
				super.executeMsg(myHost, msg);
				CMStrings.getSayFromMessage(msg.targetMessage());
				return;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost, msg);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					StringBuilder str = new StringBuilder("");
					final List<CheckedOutRecord> recs = this.getAllMyRecords(msg.source().Name());
					double totalDue = 0.0;
					final TimeClock clock = getMyClock();
					if (clock != null)
					{
						boolean recordsChanged = false;
						for (CheckedOutRecord rec : recs)
						{
							boolean recordChanged = processCheckedOutRecord(rec);
							recordsChanged = recordsChanged || recordChanged;
							totalDue += rec.charges;
							if (rec.itemName.length() > 0)
							{
								str.append(L("You have @x1 checked out.", rec.itemName));
								if (System.currentTimeMillis() > rec.mudDueDateMs)
									str.append(L(" It is past due."));
								else
								{
									TimeClock reClk = (TimeClock)clock.copyOf();
									reClk=reClk.deriveClock(rec.mudDueDateMs);
									str.append(L(" It is due by @x1.", reClk.getShortTimeDescription()));
								}
								str.append("\n\r");
							}
						}
						
					}
					if (totalDue > 0.0)
						str.append(L("You owe the library @x1.\n\r", CMLib.beanCounter().abbreviatedPrice(this, totalDue)));
					if (str.length() > 2)
					{
						mob.tell("");
						CMLib.commands().postSay(this, mob, str.toString().substring(0, str.length() - 2), true, false);
					}
					return;
				}
				break;
			}
			default:
				break;
			}
		}
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob = msg.source();
		if ((msg.targetMinor() == CMMsg.TYP_EXPIRE) && (msg.target() == location()) && (CMLib.flags().isInTheGame(this, true)))
			return false;
		else if (msg.amITarget(this))
		{
			switch (msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), finalIgnoreMask(), this))
					return false;
				if (msg.tool() == null)
					return false;
				if (!(msg.tool() instanceof Item))
				{
					mob.tell(L("@x1 doesn't look interested.", mob.charStats().HeShe()));
					return false;
				}
				if (CMLib.flags().isEnspelled((Item) msg.tool()) || CMLib.flags().isOnFire((Item) msg.tool()))
				{
					mob.tell(this, msg.tool(), null, L("<S-HE-SHE> refuses to accept <T-NAME>."));
					return false;
				}
				boolean moneyPass = false;
				if (msg.tool() instanceof Coins)
					moneyPass = this.getTotalOverdueCharges(msg.source().Name()) > 0.0;
				if (!moneyPass)
				{
					if ((!this.shop.doIHaveThisInStock(msg.tool().Name(), null)) && (this.getItemRecords(msg.tool().Name()).size() == 0))
					{
						mob.tell(this, msg.tool(), null, L("<S-HE-SHE> has no interest in <T-NAME>."));
						CMLib.commands().postSay(this, mob, L("That item was not checked out here."), true, false);
						return false;
					}
				}
				return true;
			}
			case CMMsg.TYP_WITHDRAW:
			case CMMsg.TYP_BORROW:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), finalIgnoreMask(), this))
					return false;
				if ((msg.tool() == null) || (!(msg.tool() instanceof Item)) || (msg.tool() instanceof Coins))
				{
					CMLib.commands().postSay(this, mob, L("What do you want? I'm busy! Also, SHHHH!!!!"), true, false);
					return false;
				}
				if ((msg.tool() != null) && (!msg.tool().okMessage(myHost, msg)))
					return false;
				if (!this.shop.doIHaveThisInStock(msg.tool().Name(), null))
				{
					CMLib.commands().postSay(this, mob, L("We don't stock anything like that."), true, false);
					return false;
				}
				final double due = getTotalOverdueCharges(msg.source().Name());
				if (due > 0.0)
				{
					String totalAmount = CMLib.beanCounter().nameCurrencyShort(this, due);
					CMLib.commands().postSay(this, mob, L("I'm sorry, but you have @x1 in overdue charges and may not borrow any more.", totalAmount), true, false);
					return false;
				}
				if (getAllMyRecords(msg.source().Name()).size() >= this.getMaxBorrowed())
				{
					CMLib.commands().postSay(this, mob, L("I'm sorry, but you may only borrow @x1 items.", "" + getMaxBorrowed()), true, false);
					return false;
				}
				if (getRecord(msg.source().Name(), msg.tool().Name()) != null)
				{
					CMLib.commands().postSay(this, mob, L("I'm sorry, but you already borrowed a copy of that.", "" + getMaxBorrowed()), true, false);
					return false;
				}
				final CoffeeShop shop = this.getShop();
				if (shop == this.shop) // never borrow from the main library
				{
					CMLib.commands().postSay(this, mob, L("Please come back a little later."), true, false);
					return false;
				}
			}
				return true;
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VALUE:
				if ((contributorMask().length() > 0) && (!CMLib.masking().maskCheck(contributorMask(), msg.source(), false)))
				{
					CMLib.commands().postSay(this, mob, L("I'm afraid you lack the credentials to contribute to our stock."), true, false);
					return false;
				}
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_VIEW:
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_BUY:
				CMLib.commands().postSay(this, mob, L("I'm sorry, but nothing here is for sale."), true, false);
				return false;
			case CMMsg.TYP_LIST:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), finalIgnoreMask(), this))
					return false;
				return true;
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost, msg);
	}

	@Override
	public int getMaxBorrowed()
	{
		return maxBorrowedItems;
	}

	@Override
	public void setMaxBorrowed(int items)
	{
		maxBorrowedItems = items;
	}
}
