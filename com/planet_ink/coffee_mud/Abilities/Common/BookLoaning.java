package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ShopKeeper.ViewType;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Druid.Chant_ChargeMetal;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.TimeClock.TimePeriod;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.Librarian.CheckedOutRecord;
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
public class BookLoaning extends CommonSkill implements ShopKeeper, Librarian
{
	@Override
	public String ID()
	{
		return "BookLoaning";
	}

	private final static String	localizedName	= CMLib.lang().L("Book Loaning");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BOOKLOAN" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int overrideMana()
	{
		return 5;
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

	@Override
	protected ExpertiseLibrary.SkillCostDefinition getRawTrainingCost()
	{
		return CMProps.getNormalSkillGainCost(ID());
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS | Ability.CAN_ROOMS | Ability.CAN_EXITS | Ability.CAN_AREAS | Ability.CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	protected volatile CoffeeShop	curShop				= null;
	private volatile boolean		shopApply			= false;
	private volatile long			lastShopTime		= 0;
	private final long[] 			lastChanges			= new long[2];

	protected CoffeeShop	shop				= ((CoffeeShop) CMClass.getCommon("DefaultCoffeeShop")).build(this);
	private double[]		devalueRate			= null;
	private long			whatIsSoldMask		= ShopKeeper.DEAL_BOOKS;
	private String			prejudice			= "";
	private String			ignore				= "";
	private MOB				staticMOB			= null;
	private String[]		pricingAdjustments	= new String[0];
	private String			itemZapperMask		= "";
	private final String	contributorMask		= "";
	private int				minOverdueDays		= 12;
	private int				maxOverdueDays		= 48;
	private int				maxBorrowed			= 2;
	private final Set<ViewType>	viewTypes			= new XHashSet<ViewType>(ViewType.BASIC);

	private Pair<Long, TimePeriod>	budget	= new Pair<Long, TimePeriod>(Long.valueOf(100000), TimePeriod.DAY);
	private List<CheckedOutRecord>	records	= new LinkedList<CheckedOutRecord>();
	protected volatile Item			approvedI	= null;
	protected volatile String		approvedMob	= null;

	public BookLoaning()
	{
		super();
		displayText="";

		isAutoInvoked();
	}

	protected TimeClock getMyClock()
	{
		if(affected != null)
			return CMLib.time().localClock(affected);
		return CMLib.time().globalClock();
	}

	@Override
	public CMObject copyOf()
	{
		final BookLoaning obj=(BookLoaning)super.copyOf();
		obj.shop=(CoffeeShop)shop.copyOf();
		obj.shop.build(obj);
		obj.records = new LinkedList<CheckedOutRecord>();
		obj.records.addAll(records);
		if(budget!=null)
			obj.budget=new Pair<Long, TimePeriod>(budget.first, budget.second);
		return obj;
	}

	@Override
	public CMObject newInstance()
	{
		final BookLoaning obj = (BookLoaning)super.newInstance();
		obj.shop.build(obj);
		return obj;
	}

	@Override
	public CoffeeShop getShop()
	{
		if (shopApply)
		{
			final long lastChangeMs;
			synchronized(this)
			{
				lastChangeMs = lastChanges[0];
			}

			if ((this.lastShopTime < lastChangeMs) || (curShop == null))
			{
				this.lastShopTime = lastChangeMs;
				curShop = (CoffeeShop) shop.copyOf();
				final List<CheckedOutRecord> records = this.records;
				for (int i = 0; i < records.size(); i++)
				{
					try
					{
						final CheckedOutRecord rec = records.get(i);
						if (rec.itemName.length() > 0)
							curShop.lowerStock("$" + rec.itemName + "$");
					}
					catch (final java.lang.IndexOutOfBoundsException e)
					{
					}
				}
			}
			return curShop;
		}
		else
			return shop;
	}

	protected String getRecordsXML()
	{
		final StringBuilder records=new StringBuilder("<CHECKEDOUTRECORDS>");
		final XMLLibrary xml=CMLib.xml();
		for(final CheckedOutRecord rec : this.records)
		{
			records.append("<RECORD ITEM=\"").append(xml.parseOutAngleBracketsAndQuotes(rec.itemName)).append("\" ")
				.append("PLAYER=\"").append(xml.parseOutAngleBracketsAndQuotes(rec.playerName)).append("\" ")
				.append("CHARGES=").append(rec.charges).append(" ")
				.append("DUEDATE=").append(rec.mudDueDateMs).append(" ")
				.append("RECLAIMDATE=").append(rec.mudReclaimDateMs).append(" ")
				.append("/>");
		}
		records.append("</CHECKEDOUTRECORDS>");
		return records.toString();
	}

	@Override
	public Set<ViewType> viewFlags()
	{
		return viewTypes;
	}

	@Override
	public String text()
	{
		return shop.makeXML() + getRecordsXML();
	}

	@Override
	public String budget()
	{
		return budget == null ? "" : (budget.first + " " + budget.second.name());
	}

	@Override
	public void setBudget(final String factors)
	{
		budget = CMLib.coffeeShops().parseBudget(factors);
	}

	@Override
	public String devalueRate()
	{
		return devalueRate == null ? "" : (devalueRate[0] + " "+devalueRate[1]);
	}

	@Override
	public void setDevalueRate(final String factors)
	{
		devalueRate = CMLib.coffeeShops().parseDevalueRate(factors);
	}

	@Override
	public int invResetRate()
	{
		return 0;
	}

	@Override
	public void setInvResetRate(final int ticks)
	{
	}

	protected void parseRecords(final String text)
	{
		final XMLLibrary xml=CMLib.xml();
		final List<XMLLibrary.XMLTag> tags=xml.parseAllXML(text);
		final List<XMLLibrary.XMLTag> recs=xml.getContentsFromPieces(tags, "CHECKEDOUTRECORDS");
		this.records.clear();
		for(final XMLLibrary.XMLTag rec : recs)
		{
			final CheckedOutRecord R=new CheckedOutRecord();
			R.itemName=xml.restoreAngleBrackets(rec.getParmValue("ITEM"));
			R.playerName=xml.restoreAngleBrackets(rec.getParmValue("PLAYER"));
			R.charges=CMath.s_double(rec.getParmValue("CHARGES"));
			R.mudDueDateMs=CMath.s_long(rec.getParmValue("DUEDATE"));
			R.mudReclaimDateMs=CMath.s_long(rec.getParmValue("RECLAIMDATE"));
			this.records.add(R);
		}
	}

	@Override
	public void setMiscText(final String text)
	{
		synchronized(this)
		{
			minOverdueDays		= 12;
			maxOverdueDays		= 24;
			maxBorrowed			= 2;
			final int x=text.lastIndexOf("<CHECKEDOUTRECORDS>");
			if(x>0)
			{
				this.records.clear();
				final String recordXML = text.substring(x);
				final String shopXML = text.substring(0,x);
				parseRecords(recordXML);
				shop.buildShopFromXML(shopXML);
			}
			else
			if(text.trim().length()>0)
			{
				minOverdueDays=CMParms.getParmInt(text, "DUEDAYS", minOverdueDays);
				maxOverdueDays=CMParms.getParmInt(text, "MAXDAYS", maxOverdueDays);
				maxBorrowed=CMParms.getParmInt(text, "MAX", maxBorrowed);
			}
		}
		this.curShop=null;
	}

	@Override
	public void affectPhyStats(final Physical E, final PhyStats affectableStats)
	{
		if(E instanceof MOB)
			affectableStats.setWeight(affectableStats.weight()+shop.totalStockWeight());
	}

	@Override
	public boolean isSold(final int mask)
	{
		if(mask==0)
			return whatIsSoldMask==0;
		if((whatIsSoldMask&255)==mask)
			return true;
		return CMath.bset(whatIsSoldMask>>8, CMath.pow(2,mask-1));
	}

	@Override
	public void addSoldType(final int mask)
	{
	}

	@Override
	public long getWhatIsSoldMask()
	{
		return whatIsSoldMask;
	}

	@Override
	public void setWhatIsSoldMask(final long newSellCode)
	{
		whatIsSoldMask = newSellCode;
	}

	@Override
	public String storeKeeperString()
	{
		return CMLib.coffeeShops().storeKeeperString(getShop(), this);
	}

	@Override
	public boolean doISellThis(final Environmental thisThang)
	{
		return CMLib.coffeeShops().doISellThis(thisThang, this);
	}

	@Override
	public String prejudiceFactors()
	{
		return prejudice;
	}

	@Override
	public void setPrejudiceFactors(final String factors)
	{
		prejudice = factors;
	}

	@Override
	public String ignoreMask()
	{
		return ignore;
	}

	@Override
	public void setIgnoreMask(final String factors)
	{
		ignore = factors;
	}

	@Override
	public String[] itemPricingAdjustments()
	{
		return pricingAdjustments;
	}

	@Override
	public void setWhatIsSoldZappermask(final String newSellMask)
	{
		itemZapperMask = newSellMask;
	}

	@Override
	public String getWhatIsSoldZappermask()
	{
		return itemZapperMask;
	}

	@Override
	public double getOverdueCharge()
	{
		return 0;
	}

	@Override
	public void setOverdueCharge(final double charge)
	{
	}

	@Override
	public double getDailyOverdueCharge()
	{
		return 0.0;
	}

	@Override
	public void setDailyOverdueCharge(final double charge)
	{
	}

	@Override
	public double getOverdueChargePct()
	{
		return 0;
	}

	@Override
	public void setOverdueChargePct(final double pct)
	{
	}

	@Override
	public double getDailyOverdueChargePct()
	{
		return 0;
	}

	@Override
	public void setDailyOverdueChargePct(final double pct)
	{
	}

	@Override
	public int getMinOverdueDays()
	{
		return minOverdueDays;
	}

	@Override
	public void setMinOverdueDays(final int days)
	{
		minOverdueDays=days;
	}

	@Override
	public int getMaxOverdueDays()
	{
		return maxOverdueDays;
	}

	@Override
	public void setMaxOverdueDays(final int days)
	{
		maxOverdueDays=days;
	}

	@Override
	public int getMaxBorrowed()
	{
		return maxBorrowed;
	}

	@Override
	public void setMaxBorrowed(final int items)
	{
		maxBorrowed=items;
	}

	@Override
	public String libraryChain()
	{
		if(affected != null)
			return L("The @x1 Personal Library",affected.Name());
		return L("Unknown Personal Library");
	}

	@Override
	public void setLibraryChain(final String name)
	{
	}

	@Override
	public String contributorMask()
	{
		return this.contributorMask;
	}

	@Override
	public void setContributorMask(final String mask)
	{
	}

	protected String getLibraryShopKey()
	{
		return "LIBRARY_SHOP_" + this.libraryChain().toUpperCase().replace(' ', '_');
	}

	@Override
	public CoffeeShop getBaseLibrary()
	{
		Resources.removeResource(this.getLibraryShopKey());
		curShop=null;
		return shop;
	}

	@Override
	public void setItemPricingAdjustments(String[] factors)
	{
		if((!(affected instanceof MOB))||(!((MOB)affected).isMonster()))
			factors=new String[0];
		pricingAdjustments=factors;
	}

	protected Area getStartArea()
	{
		Area A=CMLib.map().getStartArea(affected);
		if(A==null)
			CMLib.map().areaLocation(affected);
		if(A==null)
			A=CMLib.map().areas().nextElement();
		return A;
	}

	@Override
	public int finalInvResetRate()
	{
		if((invResetRate()!=0)||((affected instanceof MOB)&&(!((MOB)affected).isMonster())))
			return invResetRate();
		return getStartArea().finalInvResetRate();
	}

	@Override
	public String finalPrejudiceFactors()
	{
		if((prejudiceFactors().length()>0)||((affected instanceof MOB)&&(!((MOB)affected).isMonster())))
			return prejudiceFactors();
		return getStartArea().finalPrejudiceFactors();
	}

	@Override
	public String finalIgnoreMask()
	{
		if((ignoreMask().length()>0)||((affected instanceof MOB)&&(!((MOB)affected).isMonster())))
			return ignoreMask();
		return getStartArea().finalIgnoreMask();
	}

	@Override
	public String[] finalItemPricingAdjustments()
	{
		if(((itemPricingAdjustments()!=null)&&(itemPricingAdjustments().length>0))
		||((affected instanceof MOB)&&(!((MOB)affected).isMonster())))
			return itemPricingAdjustments();
		return getStartArea().finalItemPricingAdjustments();
	}

	@Override
	public Pair<Long, TimePeriod> finalBudget()
	{
		if((budget != null)||((affected instanceof MOB)&&(!((MOB)affected).isMonster())))
			return budget;
		return getStartArea().finalBudget();
	}

	@Override
	public double[] finalDevalueRate()
	{
		if ((devalueRate != null)||((affected instanceof MOB)&&(!((MOB)affected).isMonster())))
			return devalueRate;
		return getStartArea().finalDevalueRate();
	}

	protected void updateCheckedOutRecords()
	{
		final long[] lastChanges = this.lastChanges;
		synchronized (lastChanges)
		{
			lastChanges[0] = System.currentTimeMillis();
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((unInvoked)&&(canBeUninvoked())) // override all normal common skill behavior!!
			return false;
		if (!super.tick(ticking, tickID))
			return false;
		if (!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;

		if ((tickID == Tickable.TICKID_MOB) && (getStartArea() != null))
		{
			final long[] lastChangeMs;
			synchronized(this)
			{
				lastChangeMs=this.lastChanges;
			}
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
				final List<CheckedOutRecord> recs = this.records;
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
							namesChecked.put(rec.playerName, Boolean.valueOf(CMLib.players().playerExistsAllHosts(rec.playerName)));
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
					catch (final IndexOutOfBoundsException e)
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
				final ShopKeeper.ShopPrice P = CMLib.coffeeShops().pawningPrice(deriveLibrarian(null), null, stockItem, this, shop);
				final double value = (P!=null)? P.absoluteGoldPrice : 10;
				if(rec.mudReclaimDateMs < rec.mudDueDateMs)
					rec.mudReclaimDateMs = rec.mudDueDateMs + TimeManager.MILI_DAY;
				final long dueOverdueMilliDiff = (rec.mudReclaimDateMs - rec.mudDueDateMs);
				final long actualOverdueMilli = (System.currentTimeMillis() - rec.mudDueDateMs);
				final long chargeableOverdueMilli = (actualOverdueMilli > dueOverdueMilliDiff) ? dueOverdueMilliDiff : actualOverdueMilli;
				final double percentOfOverdue = CMath.div(chargeableOverdueMilli, dueOverdueMilliDiff);
				final double newCharges = CMath.mul(value, percentOfOverdue);
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
				final ShopKeeper.ShopPrice P = CMLib.coffeeShops().pawningPrice(deriveLibrarian(null), null, stockItem, this, shop);
				final double value = (P!=null)? P.absoluteGoldPrice : 0;
				if(rec.charges < value)
					rec.charges = value;
				// the item is now reclaimed!
				rec.itemName = "";
				recordsChanged = true;
			}
		}
		return recordsChanged;
	}


	public MOB deriveLibrarian(final MOB roomHelper)
	{
		if(affected ==null)
			return null;
		if(affected instanceof MOB)
			return (MOB)affected;
		if(affected instanceof Item)
		{
			if(((Item)affected).owner() instanceof MOB)
				return (MOB)((Item)affected).owner();
			if(CMLib.flags().isGettable((Item)affected))
				return null;
		}
		Room room=CMLib.map().roomLocation(affected);
		if((affected instanceof Area)&&(roomHelper!=null))
			room=roomHelper.location();
		if(room==null)
			return null;
		if(staticMOB==null)
		{
			staticMOB=CMClass.getMOB("StdMOB");
			if((affected instanceof Room)
			||(affected instanceof Exit))
				staticMOB.setName(L("the shopkeeper"));
			else
			if(affected instanceof Area)
				staticMOB.setName(L("the shop"));
			else
				staticMOB.setName(affected.Name());
		}
		staticMOB.setStartRoom(room);
		staticMOB.setLocation(room);
		if(finalBudget() != null)
		{
			if( CMLib.beanCounter().getTotalAbsoluteNativeValue( staticMOB ) < finalBudget().first.longValue() )
				staticMOB.setMoney(finalBudget().first.intValue());
		}
		return staticMOB;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB merchantM=deriveLibrarian(msg.source());
		if(merchantM==null)
			return super.okMessage(myHost,msg);

		final MOB shopperM=msg.source();
		if((msg.source()==merchantM)
		&&(msg.targetMinor()==CMMsg.TYP_GET)
		&&(msg.target() instanceof Item))
		{
			final Item newitem=(Item)msg.target();
			if((newitem.numberOfItems()>(merchantM.maxItems()-(merchantM.numItems()+shop.totalStockSizeIncludingDuplicates())))
			&&(!merchantM.isMine(this)))
			{
				merchantM.tell(L("You can't carry that many items."));
				return false;
			}
		}

		if(msg.amISource(merchantM))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WITHDRAW:
			{
				if((msg.target() instanceof PostOffice)
				&&(affected instanceof MOB)
				&&(((MOB)affected).isPlayer())
				&&(msg.tool() instanceof Item)
				&&(this.shop.doIHaveThisInStock(msg.tool().Name(), null))
				&&(this.records.size()>0))
				{
					final PostOffice post=(PostOffice)msg.target();
					final PostOffice.MailPiece piece=post.findExactBoxData(merchantM.Name(), (Item)msg.tool());
					if(piece != null)
					{
						final List<CheckedOutRecord> recs=this.getItemRecords(msg.tool().Name());
						for(final CheckedOutRecord rec : recs)
						{
							if(rec.playerName.equalsIgnoreCase(piece.from))
							{
								this.approvedMob=piece.from;
								this.approvedI=(Item)msg.tool();
							}
						}
					}
				}
				break;
			}
			default:
				break;
			}
		}

		if(msg.amITarget(merchantM)||(msg.amITarget(affected)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if((affected instanceof MOB)
				&&(((MOB)affected).isPlayer()))
					break;
				//$FALL-THROUGH$
			case CMMsg.TYP_DEPOSIT:
				{
					if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), finalIgnoreMask(), merchantM))
						return false;
					if (msg.tool() == null)
						return false;
					if (!(msg.tool() instanceof Item))
					{
						shopperM.tell(L("@x1 doesn't look interested.", shopperM.charStats().HeShe()));
						return false;
					}
					if (CMLib.flags().isEnspelled((Item) msg.tool()) || CMLib.flags().isOnFire((Item) msg.tool()))
					{
						shopperM.tell(merchantM, msg.tool(), null, L("<S-HE-SHE> refuses to accept <T-NAME>."));
						return false;
					}
					boolean moneyPass = false;
					if (msg.tool() instanceof Coins)
						moneyPass = this.getTotalOverdueCharges(msg.source().Name()) > 0.0;
					if (!moneyPass)
					{
						if ((!this.shop.doIHaveThisInStock(msg.tool().Name(), null)) && (this.getItemRecords(msg.tool().Name()).size() == 0))
						{
							if(CMSecurity.isAllowed(shopperM, shopperM.location(), CMSecurity.SecFlag.CMDMOBS))
							{
								this.shop.addStoreInventory(msg.tool(), 1, 0);
								this.shopApply=true;
								msg.source().tell(merchantM, shopperM, null, L("I will now loan out @x1.",msg.tool().Name()));
							}
							else
							{
								shopperM.tell(merchantM, msg.tool(), null, L("<S-HE-SHE> has no interest in <T-NAME>."));
								msg.source().tell(merchantM, shopperM, null, L("That item was not checked out here."));
							}
							return false;
						}
					}
					return super.okMessage(myHost, msg);
				}
			case CMMsg.TYP_WITHDRAW:
			case CMMsg.TYP_BORROW:
				{
					if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), finalIgnoreMask(), merchantM))
						return false;
					if ((msg.tool() == null) || (!(msg.tool() instanceof Item)) || (msg.tool() instanceof Coins))
					{
						msg.source().tell(merchantM, shopperM, null, L("What do you want? I'm busy! Also, SHHHH!!!!"));
						return false;
					}
					if ((msg.tool() != null) && (!msg.tool().okMessage(myHost, msg)))
						return false;
					if (!this.getShop().doIHaveThisInStock(msg.tool().Name(), null))
					{
						msg.source().tell(merchantM, shopperM, null, L("We don't stock anything like that."));
						return false;
					}
					final double due = getTotalOverdueCharges(msg.source().Name());
					if (due > 0.0)
					{
						final String totalAmount = CMLib.beanCounter().nameCurrencyShort(merchantM, due);
						msg.source().tell(merchantM, shopperM, null, L("I'm sorry, but you have @x1 in overdue charges and may not borrow any more.", totalAmount));
						return false;
					}
					if (getAllMyRecords(msg.source().Name()).size() >= this.getMaxBorrowed())
					{
						msg.source().tell(merchantM, shopperM, null, L("I'm sorry, but you may only borrow @x1 items.", "" + getMaxBorrowed()));
						return false;
					}
					if (getRecord(msg.source().Name(), msg.tool().Name()) != null)
					{
						msg.source().tell(merchantM, shopperM, null, L("I'm sorry, but you already borrowed a copy of that.", "" + getMaxBorrowed()));
						return false;
					}
					final CoffeeShop shop = this.getShop();
					if (shop == this.shop) // never borrow from the main library
					{
						msg.source().tell(merchantM, shopperM, null, L("Please come back a little later."));
						return false;
					}
				}
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VALUE:
				if ((contributorMask().length() > 0) && (!CMLib.masking().maskCheck(contributorMask(), msg.source(), false)))
				{
					msg.source().tell(merchantM, shopperM, null, L("I'm afraid you lack the credentials to contribute to our stock."));
					return false;
				}
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_VIEW:
				return super.okMessage(myHost, msg);
			case CMMsg.TYP_BUY:
				msg.source().tell(merchantM, shopperM, null, L("I'm sorry, but nothing here is for sale."));
				return false;
			case CMMsg.TYP_LIST:
			{
				if (!CMLib.coffeeShops().ignoreIfNecessary(msg.source(), finalIgnoreMask(), merchantM))
					return false;
				return true;
			}
			default:
				break;
			}
		}
		else
		if(msg.amISource(merchantM)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			Item I=(Item)getShop().removeStock("all",merchantM);
			while(I!=null)
			{
				merchantM.addItem(I);
				I=(Item)getShop().removeStock("all",merchantM);
			}
			merchantM.recoverPhyStats();
		}
		return super.okMessage(myHost,msg);
	}

	public boolean putUpForLoan(final MOB source, final MOB merchantM, final Environmental tool)
	{
		if((tool!=null)
		&&(!tool.ID().endsWith("ClanApron"))
		&&(merchantM.isMonster())
		&&((CMSecurity.isAllowed(source,merchantM.location(),CMSecurity.SecFlag.ORDER)
			||(CMLib.law().doesHavePriviledgesHere(source,merchantM.getStartRoom()))
			||(CMSecurity.isAllowed(source,merchantM.location(),CMSecurity.SecFlag.CMDMOBS)&&(merchantM.isMonster()))
			||(CMSecurity.isAllowed(source,merchantM.location(),CMSecurity.SecFlag.CMDROOMS)&&(merchantM.isMonster())))
			||((CMLib.law().getLegalBehavior(merchantM.getStartRoom())!=null)
				&&(source.getClanRole(CMLib.law().getLegalBehavior(merchantM.getStartRoom()).rulingOrganization())!=null)))
		&&((doISellThis(tool))||(isSold(DEAL_INVENTORYONLY))))
		{
			CMLib.commands().postSay(merchantM,source,L("OK, I will now loan @x1.",tool.name()),false,false);
			shop.addStoreInventory(tool,1,-1);
			curShop=null;
			if(affected instanceof Area)
				CMLib.database().DBUpdateArea(affected.Name(),(Area)affected);
			else
			if(affected instanceof Exit)
				CMLib.database().DBUpdateExits(merchantM.location());
			else
			if(affected instanceof Room)
				CMLib.database().DBUpdateRoom(merchantM.location());
			return true;
		}
		return false;
	}

	public boolean canPossiblyLoan(final Environmental E, final Environmental what)
	{
		if(!(what instanceof Item))
			return false;
		if((what instanceof Container)
		&&(((Container)what).getContents().size()>0))
			return false;
		final Item whatI=(Item)what;
		if((E instanceof Container)
		&&(!(((Container)E).owner() instanceof MOB))
		&&(((Container)E).canContain(whatI))
		&&(((Container)E).capacity()>whatI.phyStats().weight()))
			return true;
		return false;
	}

	@Override
	public void destroy()
	{
		super.destroy();
		if(curShop!=null)
			curShop.destroyStoreInventory();
		curShop=null;
		shopApply=false;
		if(shop!=null)
			shop.destroyStoreInventory();
	}

	public List<CheckedOutRecord> getAllMyRecords(final String name)
	{
		final List<CheckedOutRecord> recs = this.records;
		final List<CheckedOutRecord> myRecs = new ArrayList<CheckedOutRecord>();
		for (final CheckedOutRecord rec : recs)
		{
			if (rec.playerName.equalsIgnoreCase(name))
				myRecs.add(rec);
		}
		return myRecs;
	}

	public CheckedOutRecord getRecord(final String playerName, final String itemName)
	{
		final List<CheckedOutRecord> recs = this.records;
		for (final CheckedOutRecord rec : recs)
		{
			if (rec.playerName.equalsIgnoreCase(playerName) && (rec.itemName.equalsIgnoreCase(itemName)))
				return rec;
		}
		return null;
	}

	public List<CheckedOutRecord> getItemRecords(final String itemName)
	{
		final List<CheckedOutRecord> recs = this.records;
		final List<CheckedOutRecord> myRecs = new ArrayList<CheckedOutRecord>();
		for (final CheckedOutRecord rec : recs)
		{
			if (rec.itemName.equalsIgnoreCase(itemName))
				myRecs.add(rec);
		}
		return myRecs;
	}

	protected double getTotalOverdueCharges(final String name)
	{
		final List<CheckedOutRecord> recs = this.getAllMyRecords(name);
		double totalDue = 0.0;
		for (int i = 0; i < recs.size(); i++)
		{
			try
			{
				totalDue += recs.get(i).charges;
			}
			catch (final java.lang.IndexOutOfBoundsException e)
			{
			}
		}
		return totalDue;
	}

	protected final Room location()
	{
		return CMLib.map().roomLocation(affected);
	}

	public void autoGive(final MOB src, final MOB tgt, final Item I)
	{
		CMMsg msg2 = CMClass.getMsg(src, I, null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_DROP | CMMsg.MASK_INTERMSG, null);
		location().send(src, msg2);
		msg2 = CMClass.getMsg(tgt, I, null, CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null, CMMsg.MSG_GET | CMMsg.MASK_INTERMSG, null);
		location().send(tgt, msg2);
	}

	protected boolean isAPossiblePayback(final MOB mob, final Environmental tool)
	{
		if(tool instanceof Coins)
		{
			if(this.getTotalOverdueCharges(mob.Name()) > 0.0)
				return true;
		}
		else
		if((tool instanceof Item)
		&&((this.shop.doIHaveThisInStock(tool.Name(), null))
			&&(this.getItemRecords(tool.Name()).size() > 0)))
			return true;
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB merchantM=deriveLibrarian(msg.source());
		if(merchantM==null)
		{
			super.executeMsg(myHost,msg);
			return;
		}

		if (msg.source().isPlayer()
		&& (!shopApply)
		&& ((msg.source().location() == merchantM.location()) || (msg.sourceMinor() == CMMsg.TYP_ENTER))
		&& (!msg.source().isAttributeSet(MOB.Attrib.SYSOPMSGS)))
			shopApply = true;

		if(msg.amISource(merchantM))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_WITHDRAW:
			{
				if((msg.target() instanceof PostOffice)
				&&(msg.tool()==approvedI)
				&&(approvedMob != null)
				&&(msg.tool() instanceof Item))
				{
					final List<CheckedOutRecord> recs=this.getItemRecords(msg.tool().Name());
					CheckedOutRecord finalRecord = null;
					for(final CheckedOutRecord rec : recs)
					{
						if(rec.playerName.equalsIgnoreCase(approvedMob))
							finalRecord = rec;
					}
					if(finalRecord != null)
					{
						final Item finalI=approvedI;
						final CheckedOutRecord r=finalRecord;
						approvedMob=null;
						approvedI=null;
						msg.addTrailerRunnable(new Runnable()
						{
							final CheckedOutRecord rr=r;
							final Item I=finalI;

							@Override
							public void run()
							{
								if(I != null)
								{
									I.destroy();
									merchantM.tell(L("@x1 has just returned @x2.",rr.playerName,rr.itemName));
									rr.itemName="";
									updateCheckedOutRecords();
								}
							}
						});
					}
				}
				break;
			}
			default:
				break;
			}
		}

		if(msg.amITarget(merchantM)||(msg.amITarget(affected)))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if((affected instanceof MOB)
				&&(((MOB)affected).isPlayer()))
				{
					if(!isAPossiblePayback(msg.source(), msg.tool()))
						break;
				}
				//$FALL-THROUGH$
			case CMMsg.TYP_DEPOSIT:
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					if (msg.tool() instanceof Container)
						((Container) msg.tool()).emptyPlease(true);
					final Session S = msg.source().session();
					if ((!msg.source().isMonster()) && (S != null) && (msg.tool() instanceof Item))
					{
						autoGive(msg.source(), merchantM, (Item) msg.tool());
						if (msg.tool() instanceof Coins)
						{
							final double totalGiven = ((Coins) msg.tool()).getTotalValue();
							final double totalDue = getTotalOverdueCharges(msg.source().Name());
							if (totalDue > 0.0)
							{
								double totalPaidDown = totalDue;
								boolean recordUpdated = false;
								for (final CheckedOutRecord rec : this.getAllMyRecords(msg.source().Name()))
								{
									if (rec.charges > 0)
									{
										if (totalPaidDown >= rec.charges)
										{
											totalPaidDown -= rec.charges;
											rec.charges = 0.0;
											recordUpdated = true;
											if (rec.itemName.length() == 0)
												this.records.remove(rec);
										}
										else
										if (totalPaidDown > 0.0)
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
								final String totalAmount = CMLib.beanCounter().nameCurrencyShort(merchantM, totalDue);
								msg.source().tell(merchantM, mob, null, L("Your total overdue charges were @x1.", totalAmount));
								if (totalGiven > totalDue)
									CMLib.beanCounter().giveSomeoneMoney(merchantM, msg.source(), totalGiven - totalDue);
								else
								if (totalPaidDown > 0)
								{
									final String totalStillDue = CMLib.beanCounter().nameCurrencyShort(merchantM, totalPaidDown);
									msg.source().tell(merchantM, mob, null, L("Your still owe @x1.", totalStillDue));
								}
							}
							else
								msg.source().tell(merchantM, mob, null, L("You didn't have any overdue charges, so thanks for the donation!"));
						}
						else
						if (merchantM.isMine(msg.tool()))
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
								final List<CheckedOutRecord> recs = this.getItemRecords(msg.tool().Name());
								for (int i = 0; i < recs.size(); i++)
								{
									if (recs.get(i).playerName.length() > 0)
									{
										rec = recs.get(i);
										break;
									}
								}
								if (rec != null)
									msg.source().tell(merchantM, mob, null, L("I assume you are returning this for @x1.", rec.playerName));
							}
							if (rec == null)
							{
								msg.source().tell(merchantM, mob, null, L("What is this?!"));
								super.executeMsg(myHost, msg);
								return;
							}
							msg.tool().destroy(); // it's almost done being
													// returned!
							if (rec.charges > 0.0)
							{
								final String amount = CMLib.beanCounter().nameCurrencyShort(merchantM, rec.charges);
								if (CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(), merchantM) < rec.charges)
								{
									if (!msg.source().Name().equalsIgnoreCase(rec.playerName))
										msg.source().tell(merchantM, mob, null, L("Charges due for this are @x2.  @x1 must directly pay this fee to me.", rec.playerName, amount));
									else
										msg.source().tell(merchantM, mob, null, L("Charges due for this are @x2.  You must come back and pay this fee to me.", rec.playerName, amount));
									rec.itemName = "";
									this.updateCheckedOutRecords();
								}
								else
								{
									CMLib.beanCounter().subtractMoney(mob, CMLib.beanCounter().getCurrency(this), rec.charges);
									msg.source().tell(merchantM, mob, null, L("Charges due for this were @x1.  Thank you!", amount));
									//CMLib.beanCounter().subtractMoneyGiveChange(this, msg.source(), rec.charges);
									rec.charges = 0.0;
									rec.itemName = "";
									rec.playerName = "";
									this.records.remove(rec);
									this.updateCheckedOutRecords();
								}
							}
							else
							{
								msg.source().tell(merchantM, mob, null, L("Thank you!", rec.playerName));
								rec.charges = 0.0;
								rec.itemName = "";
								rec.playerName = "";
								this.records.remove(rec);
								this.updateCheckedOutRecords();
							}
						}
					}
				}
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_PUT:
				if((canPossiblyLoan(affected,msg.tool()))
				&&(putUpForLoan(msg.source(),merchantM,msg.tool())))
					return;
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VALUE:
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if((msg.tool() instanceof Physical)
				&&(getShop().doIHaveThisInStock(msg.tool().Name(),mob)))
				{
					msg.source().tell(merchantM, mob, null, L("Interested in @x1? Here is some information for you:\n\rLevel @x2\n\rDescription: @x3",msg.tool().name(),""+((Physical)msg.tool()).phyStats().level(),msg.tool().description()));
				}
				break;
			case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
			{
				super.executeMsg(myHost,msg);
				break;
			}
			case CMMsg.TYP_BUY:
			{
				super.executeMsg(myHost,msg);
				final MOB mobFor=CMLib.coffeeShops().parseBuyingFor(msg.source(),msg.targetMessage());
				if((msg.tool()!=null)
				&&(getShop().doIHaveThisInStock("$"+msg.tool().Name()+"$",mobFor))
				&&(merchantM.location()!=null))
				{
					if(merchantM.isPlayer())
						mob.tell(L("You'll need to talk to @x1 personally about that.",merchantM.Name()));
					else
						mob.tell(L("That's not for sale."));
				}
				break;
			}
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
								final List<Environmental> items = shop.removeSellableProduct("$" + old.Name() + "$", mob);
								msg.source().tell(merchantM, mob, null, L("There ya go! This is due back here by @x1!", minClock.getShortTimeDescription()));
								final Room locationR=CMLib.map().roomLocation(affected);
								for (final Environmental E : items)
								{
									if (E instanceof Item)
									{
										final Item I = (Item) E;
										if (locationR != null)
										{
											locationR.addItem(I, ItemPossessor.Expire.Player_Drop);
											final CMMsg msg2 = CMClass.getMsg(mob, I, this, CMMsg.MSG_GET, null);
											if (locationR.okMessage(mob, msg2))
												locationR.send(mob, msg2);
										}
									}
								}
								this.records.add(rec);
								this.updateCheckedOutRecords();
							}
						}
					}
				}
				super.executeMsg(myHost, msg);
				return;
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				shopApply=true;
				final List<Environmental> inventory=new XVector<Environmental>(getShop().getStoreInventory());
				final String forMask=CMLib.coffeeShops().getListForMask(msg.targetMessage());
				final String s=CMLib.coffeeShops().getListInventory(merchantM,mob,inventory,0,this,forMask);
				if(s.length()>0)
					msg.source().tell(s);
				if (CMLib.flags().isAliveAwakeMobileUnbound(mob, true))
				{
					final StringBuilder str = new StringBuilder("");
					final List<CheckedOutRecord> recs = this.getAllMyRecords(msg.source().Name());
					double totalDue = 0.0;
					final TimeClock clock = getMyClock();
					if (clock != null)
					{
						boolean recordsChanged = false;
						for (final CheckedOutRecord rec : recs)
						{
							final boolean recordChanged = processCheckedOutRecord(rec);
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
						str.append(L("You owe the library @x1.\n\r", CMLib.beanCounter().abbreviatedPrice(merchantM, totalDue)));
					if (str.length() > 2)
						mob.tell(merchantM, mob, null, str.toString().substring(0, str.length() - 2));
					return;
				}
				break;
			}
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_DROP)
		&&(myHost==affected)
		&&((affected instanceof Room)
			||(affected instanceof Exit)
			||((affected instanceof Item)&&(!canPossiblyLoan(affected,msg.tool())))))
		{
			if(isAPossiblePayback(msg.source(), msg.tool()))
			{
				final CMMsg msg2=CMClass.getMsg(msg.source(), this, msg.tool(), CMMsg.MSG_DEPOSIT|CMMsg.MASK_ALWAYS, null);
				this.executeMsg(myHost, msg2);
			}
			else
			if(putUpForLoan(msg.source(),merchantM,msg.target()))
				return;
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_THROW)
		&&(myHost==affected)
		&&(affected instanceof Area)
		&&(msg.target() instanceof Room)
		&&(((Room)msg.target()).domainType()==Room.DOMAIN_OUTDOORS_AIR)
		&&(msg.source().location().getRoomInDir(Directions.UP)==msg.target()))
		{
			if(isAPossiblePayback(msg.source(), msg.tool()))
			{
				final CMMsg msg2=CMClass.getMsg(msg.source(), this, msg.tool(), CMMsg.MSG_DEPOSIT|CMMsg.MASK_ALWAYS, null);
				this.executeMsg(myHost, msg2);
			}
			else
			if(putUpForLoan(msg.source(),merchantM,msg.tool()))
				return;

		}
		else
			super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(commands.size()==0)
		{
			commonTell(mob,L("Loan what book? Enter \"bookloan list\" for a list or \"bookloan item\" to loan something."));
			return false;
		}
		if(CMParms.combine(commands,0).equalsIgnoreCase("list"))
		{
			final CMMsg msg=CMClass.getMsg(mob,mob,CMMsg.MSG_LIST,L("<S-NAME> review(s) <S-HIS-HER> selections."));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			boolean recordsChanged = false;
			final List<CheckedOutRecord> remove=new LinkedList<CheckedOutRecord>();
			for(final CheckedOutRecord rec : this.records)
			{
				final boolean recordChanged = processCheckedOutRecord(rec);
				recordsChanged = recordsChanged || recordChanged;
				TimeClock reClk = (TimeClock)this.getMyClock().copyOf();
				reClk=reClk.deriveClock(rec.mudDueDateMs);
				if((rec.itemName==null)
				||(rec.itemName.length()==0))
				{
					if(rec.charges==0.0)
						remove.add(rec);
					else
					{
						mob.tell(L("@x1 has owed @x2 since @x3.",
								rec.playerName,
								CMLib.beanCounter().abbreviatedPrice(mob, rec.charges),
								reClk.getShortTimeDescription()));
					}
				}
				else
				{
					mob.tell(L("@x1 checked out @x2, due on @x3 and now owes @x4.",
							rec.playerName,
							rec.itemName,
							reClk.getShortTimeDescription(),
							CMLib.beanCounter().abbreviatedPrice(mob, rec.charges)));
				}
			}
			this.records.removeAll(remove);
			if(recordsChanged)
				this.updateCheckedOutRecords();
			return true;
		}
		final BookLoaning loanA=(BookLoaning)mob.fetchEffect(ID());
		if((commands.get(0)).equalsIgnoreCase("remove")
		||(commands.get(0)).equalsIgnoreCase("delete"))
		{
			if(commands.size()==1)
			{
				commonTell(mob,L("Remove what item from the loan list?"));
				return false;
			}
			final String itemName=CMParms.combine(commands,1);
			loanA.shopApply=true; // make sure you are removing ONLY from the currentShop
			final CoffeeShop curShop=loanA.getShop(); // this should be curShop
			loanA.shopApply=false;
			Item I=(Item)curShop.removeStock(itemName,mob);
			if(I==null)
			{
				commonTell(mob,L("'@x1' is not on the list.",itemName));
				return false;
			}
			final String iname=I.name();
			while(I!=null)
			{
				curShop.delAllStoreInventory(I);
				mob.addItem(I);
				I=(Item)curShop.removeStock(itemName,mob);
			}
			I=(Item)loanA.shop.removeStock(itemName, mob); // now from the base Shop
			while(I!=null)
			{
				loanA.shop.delAllStoreInventory(I);
				I=(Item)loanA.shop.removeStock(itemName,mob);
			}

			loanA.shopApply=false;
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
			mob.tell(L("@x1 has been removed from your loanable selections.",iname));
			return true;
		}

		double val=-1;
		if(commands.size()>1)
		{
			final String s=commands.get(commands.size()-1);
			if(CMath.isInteger(s))
			{
				val=CMath.s_int( s );
				if(val>0)
					commands.remove(s);
			}
			else
			{
				final long numberCoins=CMLib.english().parseNumPossibleGold(mob,s);
				if(numberCoins>0)
				{
					final String currency=CMLib.english().parseNumPossibleGoldCurrency(mob,s);
					final double denom=CMLib.english().parseNumPossibleGoldDenomination(mob,currency,s);
					if(denom>0.0)
					{
						val=CMath.mul(numberCoins,denom);
						if(val>0)
							commands.remove(s);
					}
				}
			}
		}

		Environmental target=null;
		String itemName=CMParms.combine(commands,0);
		final List<Item> itemsV=new ArrayList<Item>();
		boolean allFlag=commands.get(0).equalsIgnoreCase("all");
		if(itemName.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			itemName="ALL "+itemName.substring(4);
		}
		if(itemName.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			itemName="ALL "+itemName.substring(0,itemName.length()-4);
		}
		int addendum=1;
		String addendumStr="";
		boolean doBugFix = true;
		while(doBugFix || allFlag)
		{
			doBugFix=false;
			final Item I=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,itemName+addendumStr);
			if(I==null)
				break;
			if((I instanceof Container)
			&&(((Container)I).getContents().size()>0))
			{
				commonTell(mob,I,null,L("You may not loan out <T-NAME>."));
				return false;
			}
			if(target==null)
				target=I;
			else
			if(!target.sameAs(I))
				break;
			if(CMLib.flags().canBeSeenBy(I,mob))
				itemsV.add(I);
			addendumStr="."+(++addendum);
		}

		if(itemsV.size()==0)
		{
			commonTell(mob,L("You don't seem to be carrying '@x1'.",itemName));
			return false;
		}

		if((getShop().numberInStock(target)<=0)&&(val<=0))
		{
			commonTell(mob,L("You failed to specify a value for '@x1'.",itemName));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!proficiencyCheck(mob,0,auto))
		{
			commonTell(mob,target,null,L("You fail to make <T-NAME> available to borrow."));
			return false;
		}

		final CMMsg msg=CMClass.getMsg(mob,target,CMMsg.MSG_SELL,L("<S-NAME> make(s) <T-NAME> available for borrowing."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(int i=0;i<itemsV.size();i++)
			{
				final Item I=itemsV.get(i);
				loanA.shop.addStoreInventory(I, 1, (int)val);
				loanA.curShop=null;
				loanA.shopApply=false;
				mob.delItem(I);
			}
		}
		mob.location().recoverRoomStats();
		mob.recoverPhyStats();
		return true;
	}

	@Override
	public boolean autoInvocation(final MOB mob, final boolean force)
	{
		if(mob instanceof ShopKeeper)
			return false;
		if(super.autoInvocation(mob, force))
		{
			final BookLoaning ook=(BookLoaning)mob.fetchEffect(ID());
			if(ook != null)
			{
				ook.shop=shop;
				ook.records=records;
			}
		}
		return false;
	}


}
