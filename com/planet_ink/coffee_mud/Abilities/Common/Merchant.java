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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Merchant extends CommonSkill implements ShopKeeper
{
	@Override
	public String ID()
	{
		return "Merchant";
	}

	private final static String	localizedName	= CMLib.lang().L("Marketeering");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MARKET" });

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

	protected CoffeeShop	shop				= ((CoffeeShop) CMClass.getCommon("DefaultCoffeeShop")).build(this);
	private double[]		devalueRate			= null;
	private long			whatIsSoldMask		= ShopKeeper.DEAL_ANYTHING;
	private String			prejudice			= "";
	private String			ignore				= "";
	private MOB				staticMOB			= null;
	private String[]		pricingAdjustments	= new String[0];
	private String			itemZapperMask		= "";
	
	private Pair<Long,TimePeriod> budget		= new Pair<Long,TimePeriod>(Long.valueOf(100000), TimePeriod.DAY);

	public Merchant()
	{
		super();
		displayText="";

		isAutoInvoked();
	}
	
	@Override
	public CoffeeShop getShop()
	{
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
	public void setBudget(String factors)
	{
		budget = CMLib.coffeeShops().parseBudget(factors);
	}

	@Override
	public String devalueRate()
	{
		return devalueRate == null ? "" : (devalueRate[0] + " "+devalueRate[1]);
	}

	@Override
	public void setDevalueRate(String factors)
	{
		devalueRate = CMLib.coffeeShops().parseDevalueRate(factors);
	}

	@Override
	public int invResetRate()
	{
		return 0;
	}

	@Override
	public void setInvResetRate(int ticks)
	{
	}

	@Override
	public void setMiscText(String text)
	{
		synchronized(this)
		{
			shop.buildShopFromXML(text);
		}
	}

	@Override
	public void affectPhyStats(Physical E, PhyStats affectableStats)
	{
		if(E instanceof MOB)
			affectableStats.setWeight(affectableStats.weight()+shop.totalStockWeight());
	}

	@Override
	public boolean isSold(int mask)
	{
		if(mask==0)
			return whatIsSoldMask==0;
		if((whatIsSoldMask&255)==mask)
			return true;
		return CMath.bset(whatIsSoldMask>>8, CMath.pow(2,mask-1));
	}

	@Override
	public void addSoldType(int mask)
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
	public void setWhatIsSoldMask(long newSellCode)
	{
		whatIsSoldMask = newSellCode;
	}

	@Override
	public String storeKeeperString()
	{
		return CMLib.coffeeShops().storeKeeperString(getShop(), this);
	}

	@Override
	public boolean doISellThis(Environmental thisThang)
	{
		return CMLib.coffeeShops().doISellThis(thisThang, this);
	}

	@Override
	public String prejudiceFactors()
	{
		return prejudice;
	}

	@Override
	public void setPrejudiceFactors(String factors)
	{
		prejudice = factors;
	}

	@Override
	public String ignoreMask()
	{
		return ignore;
	}

	@Override
	public void setIgnoreMask(String factors)
	{
		ignore = factors;
	}

	@Override
	public String[] itemPricingAdjustments()
	{
		return pricingAdjustments;
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
	public boolean tick(Tickable ticking, int tickID)
	{
		if((unInvoked)&&(canBeUninvoked())) // override all normal common skill behavior!!
			return false;
		return true;
	}

	public MOB deriveMerchant(MOB roomHelper)
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

	public boolean putUpForSale(MOB source, MOB merchantM, Environmental tool)
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
			CMLib.commands().postSay(merchantM,source,L("OK, I will now sell @x1.",tool.name()),false,false);
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

	public boolean canPossiblyVend(Environmental E, Environmental what)
	{
		if(!(what instanceof Item))
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
				if(!putUpForSale(msg.source(),merchantM,msg.tool()))
					super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_PUT:
				if((canPossiblyVend(affected,msg.tool()))
				&&(putUpForSale(msg.source(),merchantM,msg.tool())))
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
			||((affected instanceof Item)&&(!canPossiblyVend(affected,msg.tool()))))
		&&(putUpForSale(msg.source(),merchantM,msg.target())))
			return;
		else
		if((msg.targetMinor()==CMMsg.TYP_THROW)
		&&(myHost==affected)
		&&(affected instanceof Area)
		&&(msg.target() instanceof Room)
		&&(((Room)msg.target()).domainType()==Room.DOMAIN_OUTDOORS_AIR)
		&&(msg.source().location().getRoomInDir(Directions.UP)==msg.target())
		&&(putUpForSale(msg.source(),merchantM,msg.tool())))
			return;
		else
			super.executeMsg(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			commonTell(mob,L("Market what? Enter \"market list\" for a list or \"market item value\" to sell something."));
			return false;
		}
		if(CMParms.combine(commands,0).equalsIgnoreCase("list"))
		{
			final CMMsg msg=CMClass.getMsg(mob,mob,CMMsg.MSG_LIST,L("<S-NAME> review(s) <S-HIS-HER> inventory."));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return true;
		}
		if((commands.get(0)).equalsIgnoreCase("remove")
		||(commands.get(0)).equalsIgnoreCase("delete"))
		{
			if(commands.size()==1)
			{
				commonTell(mob,L("Remove what item from the marketing list?"));
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
			mob.tell(L("@x1 has been removed from your inventory list.",iname));
			return true;
		}

		Environmental target=null;
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
				final long numberCoins=CMLib.english().numPossibleGold(mob,s);
				if(numberCoins>0)
				{
					final String currency=CMLib.english().numPossibleGoldCurrency(mob,s);
					final double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,s);
					if(denom>0.0)
					{
						val=CMath.mul(numberCoins,denom);
						if(val>0)
							commands.remove(s);
					}
				}
			}
		}

		String itemName=CMParms.combine(commands,0);
		final Vector<Item> V=new Vector<Item>();
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
			if(target==null)
				target=I;
			else
			if(!target.sameAs(I))
				break;
			if(CMLib.flags().canBeSeenBy(I,mob))
				V.addElement(I);
			addendumStr="."+(++addendum);
		}

		if(V.size()==0)
		{
			commonTell(mob,L("You don't seem to be carrying '@x1'.",itemName));
			return false;
		}

		if((getShop().numberInStock(target)<=0)&&(val<=0))
		{
			commonTell(mob,L("You failed to specify a price for '@x1'.",itemName));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!proficiencyCheck(mob,0,auto))
		{
			commonTell(mob,target,null,L("You fail to put <T-NAME> up for sale."));
			return false;
		}

		final CMMsg msg=CMClass.getMsg(mob,target,CMMsg.MSG_SELL,L("<S-NAME> put(s) <T-NAME> up for sale."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(int i=0;i<V.size();i++)
			{
				final Item I=V.elementAt(i);
				if(val<=0)
					getShop().addStoreInventory(I);
				else
					getShop().addStoreInventory(I,1,(int)Math.round(val));
				mob.delItem(I);
			}
		}
		mob.location().recoverRoomStats();
		mob.recoverPhyStats();
		return true;
	}

	@Override
	public boolean autoInvocation(MOB mob, boolean force)
	{
		if(mob instanceof ShopKeeper)
			return false;
		return super.autoInvocation(mob, force);
	}
	

	@Override
	public void setWhatIsSoldZappermask(String newSellMask)
	{
		itemZapperMask = newSellMask;
	}

	@Override
	public String getWhatIsSoldZappermask()
	{
		return itemZapperMask;
	}
}
