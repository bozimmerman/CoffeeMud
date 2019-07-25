package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2019-2019 Bo Zimmerman

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
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_INFLUENTIAL;
	}

	protected volatile CoffeeShop	curShop				= null;
	private volatile boolean		shopApply			= false;
	private volatile long			lastShopTime		= 0;

	protected CoffeeShop	shop				= ((CoffeeShop) CMClass.getCommon("DefaultCoffeeShop")).build(this);
	private double[]		devalueRate			= null;
	private long			whatIsSoldMask		= ShopKeeper.DEAL_ANYTHING;
	private String			prejudice			= "";
	private String			ignore				= "";
	private MOB				staticMOB			= null;
	private String[]		pricingAdjustments	= new String[0];
	private String			itemZapperMask		= "";
	private final String	contributorMask		= "";
	private int				minOverdueDays		= 1;
	private int				maxOverdueDays		= 5;
	private int				maxBorrowed			= 5;

	private Pair<Long,TimePeriod> budget		= new Pair<Long,TimePeriod>(Long.valueOf(100000), TimePeriod.DAY);

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
				//TODO:
				/*
				final List<CheckedOutRecord> records = this.getCheckedOutRecords();
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
				*/
			}
			return curShop;
		}
		else
			return shop;
	}

	@Override
	public String text()
	{
		return shop.makeXML();
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

	@Override
	public void setMiscText(final String text)
	{
		synchronized(this)
		{
			shop.buildShopFromXML(text);
		}
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
		if(mask==0)
			whatIsSoldMask=0;
		else
		{
			if((whatIsSoldMask>0)&&(whatIsSoldMask<256))
				whatIsSoldMask=(CMath.pow(2,whatIsSoldMask-1)<<8);

			for(int c=0;c<ShopKeeper.DEAL_CONFLICTS.length;c++)
			{
				for(int c1=0;c1<ShopKeeper.DEAL_CONFLICTS[c].length;c1++)
				{
					if(ShopKeeper.DEAL_CONFLICTS[c][c1]==mask)
					{
						for(c1=0;c1<ShopKeeper.DEAL_CONFLICTS[c].length;c1++)
							if((ShopKeeper.DEAL_CONFLICTS[c][c1]!=mask)
							&&(isSold(ShopKeeper.DEAL_CONFLICTS[c][c1])))
								addSoldType(-ShopKeeper.DEAL_CONFLICTS[c][c1]);
						break;
					}
				}
			}

			if(mask>0)
				whatIsSoldMask|=(CMath.pow(2,mask-1)<<8);
			else
				whatIsSoldMask=CMath.unsetb(whatIsSoldMask,(CMath.pow(2,(-mask)-1)<<8));
		}
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
		return 0;
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

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if((unInvoked)&&(canBeUninvoked())) // override all normal common skill behavior!!
			return false;
		return true;
	}

	public MOB deriveMerchant(final MOB roomHelper)
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
		final MOB merchantM=deriveMerchant(msg.source());
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

		if(msg.amITarget(merchantM)||(msg.amITarget(affected)))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			{
				if(!merchantM.isMonster())
				{
					shopperM.tell(shopperM,null,null,L("You'll have to talk to <S-NAME> about that."));
					return false;
				}
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),merchantM))
					return false;
				final double budgetRemaining=CMLib.beanCounter().getTotalAbsoluteValue(merchantM,CMLib.beanCounter().getCurrency(merchantM));
				final double budgetMax=budgetRemaining*100;
				if(CMLib.coffeeShops().standardSellEvaluation(merchantM,msg.source(),msg.tool(),this,budgetRemaining,budgetMax,msg.targetMinor()==CMMsg.TYP_SELL))
					return super.okMessage(myHost,msg);
				return false;
			}
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),merchantM))
					return false;
				if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
					return false;
				if(CMLib.coffeeShops().standardBuyEvaluation(merchantM,msg.source(),msg.tool(),this,msg.targetMinor()==CMMsg.TYP_BUY))
					return super.okMessage(myHost,msg);
				return false;
			}
			case CMMsg.TYP_LIST:
				CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),merchantM);
				break;
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
			getShop().addStoreInventory(tool,1,-1);
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
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB merchantM=deriveMerchant(msg.source());
		if(merchantM==null)
		{
			super.executeMsg(myHost,msg);
			return;
		}

		if(msg.amITarget(merchantM)||(msg.amITarget(affected)))
		{
			final MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if(!putUpForLoan(msg.source(),merchantM,msg.tool()))
					super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_PUT:
				if((canPossiblyLoan(affected,msg.tool()))
				&&(putUpForLoan(msg.source(),merchantM,msg.tool())))
					return;
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VALUE:
				super.executeMsg(myHost,msg);
				if(merchantM.isMonster())
					CMLib.commands().postSay(merchantM,mob,L("I'll give you @x1 for @x2.",CMLib.beanCounter().nameCurrencyShort(merchantM,CMLib.coffeeShops().pawningPrice(merchantM,mob,msg.tool(),this, getShop()).absoluteGoldPrice),msg.tool().name()),true,false);
				break;
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if((msg.tool() instanceof Physical)
				&&(getShop().doIHaveThisInStock(msg.tool().Name(),mob)))
				{
					CMLib.commands().postSay(merchantM,msg.source(),L("Interested in @x1? Here is some information for you:\n\rLevel @x2\n\rDescription: @x3",msg.tool().name(),""+((Physical)msg.tool()).phyStats().level(),msg.tool().description()),true,false);
				}
				break;
			case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
			{
				super.executeMsg(myHost,msg);
				CMLib.coffeeShops().transactPawn(merchantM,msg.source(),this,msg.tool());
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
					final Environmental item=getShop().getStock("$"+msg.tool().Name()+"$",mobFor);
					if(item!=null)
						CMLib.coffeeShops().transactMoneyOnly(merchantM,msg.source(),this,item,!merchantM.isMonster());

					final List<Environmental> products=getShop().removeSellableProduct("$"+msg.tool().Name()+"$",mobFor);
					if(products.size()==0)
						break;
					final Environmental product=products.get(0);
					if(product instanceof Item)
					{
						if(!CMLib.coffeeShops().purchaseItems((Item)product,products,merchantM,mobFor))
							return;
					}
					else
					if(product instanceof MOB)
					{
						if(CMLib.coffeeShops().purchaseMOB((MOB)product,merchantM,this,mobFor))
						{
							msg.modify(msg.source(),msg.target(),product,msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
							product.executeMsg(myHost,msg);
						}
					}
					else
					if(product instanceof Ability)
						CMLib.coffeeShops().purchaseAbility((Ability)product,merchantM,this,mobFor);
				}
				break;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				final Vector<Environmental> inventory=new XVector<Environmental>(getShop().getStoreInventory());
				final String forMask=CMLib.coffeeShops().getListForMask(msg.targetMessage());
				final String s=CMLib.coffeeShops().getListInventory(merchantM,mob,inventory,0,this,forMask);
				if(s.length()>0)
					mob.tell(s);
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
			||((affected instanceof Item)&&(!canPossiblyLoan(affected,msg.tool()))))
		&&(putUpForLoan(msg.source(),merchantM,msg.target())))
			return;
		else
		if((msg.targetMinor()==CMMsg.TYP_THROW)
		&&(myHost==affected)
		&&(affected instanceof Area)
		&&(msg.target() instanceof Room)
		&&(((Room)msg.target()).domainType()==Room.DOMAIN_OUTDOORS_AIR)
		&&(msg.source().location().getRoomInDir(Directions.UP)==msg.target())
		&&(putUpForLoan(msg.source(),merchantM,msg.tool())))
			return;
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
			return true;
		}
		if((commands.get(0)).equalsIgnoreCase("remove")
		||(commands.get(0)).equalsIgnoreCase("delete"))
		{
			if(commands.size()==1)
			{
				commonTell(mob,L("Remove what item from the loan list?"));
				return false;
			}
			final String itemName=CMParms.combine(commands,1);
			Item I=(Item)getShop().removeStock(itemName,mob);
			if(I==null)
			{
				commonTell(mob,L("'@x1' is not on the list.",itemName));
				return false;
			}
			final String iname=I.name();
			while(I!=null)
			{
				mob.addItem(I);
				I=(Item)getShop().removeStock(itemName,mob);
			}
			getShop().delAllStoreInventory(I);
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
			mob.tell(L("@x1 has been removed from your loanable selections.",iname));
			return true;
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

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!proficiencyCheck(mob,0,auto))
		{
			commonTell(mob,target,null,L("You fail to male <T-NAME> available to borrow."));
			return false;
		}

		final CMMsg msg=CMClass.getMsg(mob,target,CMMsg.MSG_SELL,L("<S-NAME> make(s) <T-NAME> available for borrowing."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(int i=0;i<itemsV.size();i++)
			{
				final Item I=itemsV.get(i);
				getShop().addStoreInventory(I);
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
		return super.autoInvocation(mob, force);
	}


}
