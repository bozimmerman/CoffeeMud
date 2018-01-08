package com.planet_ink.coffee_mud.MOBS;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class StdBanker extends StdShopKeeper implements Banker
{
	@Override
	public String ID()
	{
		return "StdBanker";
	}

	protected double coinInterest=-0.000;
	protected double itemInterest=-0.0001;
	protected double loanInterest=0.01;
	protected static Hashtable<String,Long> bankTimes=new Hashtable<String,Long>();

	public StdBanker()
	{
		super();
		username="a banker";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A banker is waiting to serve you.");
		CMLib.factions().setAlignment(this,Faction.Align.GOOD);
		setMoney(0);
		whatIsSoldMask=ShopKeeper.DEAL_BANKER;
		basePhyStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

		basePhyStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverPhyStats();
		recoverCharStats();
	}

	@Override
	public void addSoldType(int mask)
	{
		setWhatIsSoldMask(CMath.abs(mask));
	}

	@Override
	public void setWhatIsSoldMask(long newSellCode)
	{
		super.setWhatIsSoldMask(newSellCode);
		if(!isSold(ShopKeeper.DEAL_CLANBANKER))
			whatIsSoldMask=ShopKeeper.DEAL_BANKER;
		else
			whatIsSoldMask=ShopKeeper.DEAL_CLANBANKER;
	}

	@Override
	public String bankChain()
	{
		return text();
	}

	@Override
	public void setBankChain(String name)
	{
		setMiscText(name);
	}

	@Override
	public void addDepositInventory(String depositorName, Item item, Item container)
	{
		final String classID;
		if((item instanceof Coins)&&(container == null)) 
			classID="COINS";
		else
			classID=item.ID();
		CMLib.catalog().updateCatalogIntegrity(item);
		final String key=""+item+item.hashCode();
		if(container != null)
		{
			final String containerKey=""+container+container.hashCode();
			CMLib.database().DBCreatePlayerData(depositorName,bankChain(),key,classID+";CONTAINER="+containerKey+";"+CMLib.coffeeMaker().getPropertiesStr(item,true));
		}
		else
			CMLib.database().DBCreatePlayerData(depositorName,bankChain(),key,classID+";"+CMLib.coffeeMaker().getPropertiesStr(item,true));
	}

	protected Pair<Item,String> makeItemContainer(String data)
	{
		int x=data.indexOf(';');
		if(x<0) 
			return null;
		Item I=null;
		if(data.substring(0,x).equals("COINS"))
			I=CMClass.getItem("StdCoins");
		else
			I=CMClass.getItem(data.substring(0,x));
		if(I!=null)
		{
			String container="";
			String xml = data.substring(x+1);
			if(xml.startsWith("CONTAINER="))
			{
				x=xml.indexOf(';');
				if(x>0)
				{
					container=xml.substring(10,x);
					xml=xml.substring(x+1);
				}
			}
			CMLib.coffeeMaker().setPropertiesStr(I,xml,true);
			if((I instanceof Coins)
			&&(((Coins)I).getDenomination()==0.0)
			&&(((Coins)I).getNumberOfCoins()>0))
				((Coins)I).setDenomination(1.0);
			I.recoverPhyStats();
			I.text();
			return new Pair<Item,String>(I,container);
		}
		return null;
	}
	
	protected List<Item> findDeleteRecursiveDepositInventoryByContainerKey(Container C, List<PlayerData> rawInventoryV, String key)
	{
		final List<Item> inventory=new LinkedList<Item>();
		for(int v=rawInventoryV.size()-1;v>=0;v--)
		{
			final DatabaseEngine.PlayerData PD=rawInventoryV.get(v);
			final int IDx=PD.xml().indexOf(';');
			if(IDx>0)
			{
				if(PD.xml().substring(IDx+1).startsWith("CONTAINER="+key+";"))
				{
					Item I=makeItemContainer(PD.xml()).first;
					I.setContainer(C);
					inventory.add(I);
					rawInventoryV.remove(v);
					if(I instanceof Container)
						inventory.addAll(findDeleteRecursiveDepositInventoryByContainerKey((Container)I,rawInventoryV,PD.key()));
					CMLib.database().DBDeletePlayerData(PD.who(),PD.section(),PD.key());
				}
			}
		}
		return inventory;
	}

	@Override
	public List<Item> delDepositInventory(String depositorName, Item likeItem)
	{
		final List<PlayerData> rawInventoryV=getRawPDDepositInventory(depositorName);
		final List<Item> items = new ArrayList<Item>();
		if(likeItem.container()==null)
		{
			if(likeItem instanceof Coins)
			{
				for(int v=rawInventoryV.size()-1;v>=0;v--)
				{
					final DatabaseEngine.PlayerData PD=rawInventoryV.get(v);
					if(PD.xml().startsWith("COINS;"))
					{
						CMLib.database().DBDeletePlayerData(PD.who(),PD.section(),PD.key());
						items.add(makeItemContainer(PD.xml()).first);
					}
				}
			}
			else
			{
				for(int v=rawInventoryV.size()-1;v>=0;v--)
				{
					final DatabaseEngine.PlayerData PD=rawInventoryV.get(v);
					if(PD.xml().startsWith(likeItem.ID()+";") && (!PD.xml().startsWith(likeItem.ID()+";CONTAINER=")))
					{
						final Pair<Item,String> pI=makeItemContainer(PD.xml());
						if((pI!=null) && likeItem.sameAs(pI.first))
						{
							pI.first.setContainer(null);
							if(pI.first instanceof Container)
							{
								items.add(pI.first);
								final Hashtable<String,List<DatabaseEngine.PlayerData>> pairings=new Hashtable<String,List<DatabaseEngine.PlayerData>>(); 
								for(final PlayerData PDp : rawInventoryV)
								{
									final int IDx=PDp.xml().indexOf(';');
									if(IDx>0)
									{
										final String subXML=PDp.xml().substring(IDx+1);
										if(subXML.startsWith("CONTAINER="))
										{
											int x=subXML.indexOf(';');
											if(x>0)
											{
												final String contKey=subXML.substring(10,x);
												if(!pairings.containsKey(contKey))
													pairings.put(contKey, new LinkedList<DatabaseEngine.PlayerData>());
												pairings.get(contKey).add(PDp);
											}
										}
									}
								}
								CMLib.database().DBDeletePlayerData(PD.who(),PD.section(),PD.key());
								final Map<String,Container> containerMap=new Hashtable<String,Container>();
								containerMap.put(PD.key(), (Container)pI.first);
								while(containerMap.size()>0)
								{
									final String contKey=containerMap.keySet().iterator().next();
									final Container container=containerMap.remove(contKey);
									List<DatabaseEngine.PlayerData> contents=pairings.get(contKey);
									if(contents != null)
									{
										for(DatabaseEngine.PlayerData PDi : contents)
										{
											Pair<Item,String> pairI=makeItemContainer(PDi.xml());
											CMLib.database().DBDeletePlayerData(PDi.who(),PDi.section(),PDi.key());
											pairI.first.setContainer(container);
											items.add(pairI.first);
											if(pairI.first instanceof Container)
												containerMap.put(PDi.key(), (Container)pairI.first);
										}
									}
								}
							}
							else
							{
								items.add(pI.first);
								CMLib.database().DBDeletePlayerData(PD.who(),PD.section(),PD.key());
							}
							break;
						}
					}
				}
			}
		}
		return items;
	}
	
	@Override
	public void delAllDeposits(String depositorName)
	{
		CMLib.database().DBDeletePlayerData(depositorName,bankChain());
	}
	
	@Override
	public int numberDeposited(String depositorName)
	{
		return getRawPDDepositInventory(depositorName).size();
	}
	
	@Override
	public List<Item> getDepositedItems(String depositorName)
	{
		if((depositorName==null)||(depositorName.length()==0)) 
			return new ArrayList<Item>();
		final List<Item> items=new Vector<Item>();
		final Hashtable<String,Pair<Item,String>> pairings=new Hashtable<String,Pair<Item,String>>(); 
		for(final PlayerData PD : getRawPDDepositInventory(depositorName))
		{
			final Pair<Item,String> pair=makeItemContainer(PD.xml());
			if(pair!=null)
				pairings.put(PD.key(), pair);
		}
		for(final Pair<Item,String> pair : pairings.values())
		{
			if(pair.second.length()>0)
			{
				Pair<Item,String> otherPair = pairings.get(pair.second);
				if((otherPair != null)&&(otherPair.first instanceof Container))
					pair.first.setContainer((Container)otherPair.first);
			}
			items.add(pair.first);
		}
		return items;
	}
	
	protected List<PlayerData> getRawPDDepositInventory(String depositorName)
	{
		return CMLib.database().DBReadPlayerData(depositorName,bankChain());
	}
	
	@Override
	public List<String> getAccountNames()
	{
		final List<String> V=CMLib.database().DBReadPlayerDataPlayersBySection(bankChain());
		final HashSet<String> h=new HashSet<String>();
		final Vector<String> mine=new Vector<String>();
		for(int v=0;v<V.size();v++)
		{
			final String name=V.get(v);
			if(!h.contains(name))
			{
				h.add(name);
				mine.addElement(name);
			}
		}
		return mine;
	}

	protected void bankLedger(String depositorName, String msg)
	{
		final String date=CMLib.utensils().getFormattedDate(this);
		CMLib.beanCounter().bankLedger(bankChain(),depositorName,date+": "+msg);
	}

	@Override
	public Item findDepositInventory(String depositorName, String itemName)
	{
		final List<PlayerData> V=getRawPDDepositInventory(depositorName);
		if(CMath.s_int(itemName)>0)
		{
			for(int v=0;v<V.size();v++)
			{
				final DatabaseEngine.PlayerData PD=V.get(v);
				if(PD.xml().startsWith("COINS;"))
					return makeItemContainer(PD.xml()).first;
			}
		}
		else
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData PD=V.get(v);
			if(PD.xml().lastIndexOf(";CONTAINER=",81)<0)
			{
				final Pair<Item,String> pair=makeItemContainer(PD.xml());
				if(pair!=null)
				{
					if(CMLib.english().containsString(pair.first.Name(),itemName))
						return pair.first;
					pair.first.destroy();
				}
			}
		}
		return null;
	}

	public long timeInterval()
	{
		return (location().getArea().getTimeObj().getHoursInDay())
				*CMProps.getMillisPerMudHour()
				*location().getArea().getTimeObj().getDaysInMonth();
	}

	@Override
	public void setCoinInterest(double interest)
	{
		coinInterest = interest;
	}

	@Override
	public void setItemInterest(double interest)
	{
		itemInterest = interest;
	}

	@Override
	public double getCoinInterest()
	{
		return coinInterest;
	}

	@Override
	public double getItemInterest()
	{
		return itemInterest;
	}

	@Override
	public void setLoanInterest(double interest)
	{
		loanInterest = interest;
	}

	@Override
	public double getLoanInterest()
	{
		return loanInterest;
	}

	@Override
	public MoneyLibrary.DebtItem getDebtInfo(String depositorName)
	{
		final Vector<MoneyLibrary.DebtItem> debt=CMLib.beanCounter().getDebtOwed(bankChain());
		if(depositorName.length()==0)
			return null;
		for(int d=0;d<debt.size();d++)
		{
			if(debt.elementAt(d).debtor().equalsIgnoreCase(depositorName))
				return debt.elementAt(d);
		}
		return null;
	}

	protected void processAccounts()
	{
		boolean proceed=false;
		Long L=bankTimes.get(bankChain());
		long timeInterval=1;
		if(((L==null)||(L.longValue()<System.currentTimeMillis()))
		&&(location!=null)
		&&(location.getArea()!=null)
		&&(location.getArea().getTimeObj()!=null)
		&&(CMLib.flags().isInTheGame(this,true)))
		{
			timeInterval=timeInterval();
			L=Long.valueOf(System.currentTimeMillis()+timeInterval);
			proceed=true;
			bankTimes.remove(bankChain());
			bankTimes.put(bankChain(),L);
		}
		if(proceed)
		{
			final List<String> bankDataV=CMLib.database().DBReadPlayerDataPlayersBySection(bankChain());
			final Vector<String> userNames=new Vector<String>();
			for(int v=0;v<bankDataV.size();v++)
			{
				final String name=bankDataV.get(v);
				if(!userNames.contains(name))
				{
					if(!CMLib.players().playerExists(name))
					{
						if((CMLib.clans().getClan(name))==null)
							delAllDeposits(name);
						else
							userNames.addElement(name);
					}
					else
						userNames.addElement(name);
				}
			}
			final Vector<MoneyLibrary.DebtItem> debts=CMLib.beanCounter().getDebtOwed(bankChain());
			for(int u=0;u<userNames.size();u++)
			{
				final String name=userNames.elementAt(u);
				Coins coinItem=null;
				int totalValue=0;
				final List<Item> items=getDepositedItems(name);
				for(int v=0;v<items.size();v++)
				{
					final Item I=items.get(v);
					if(I instanceof Coins)
						coinItem=(Coins)I;
					else
					if(itemInterest!=0.0)
						totalValue+=I.value();
				}
				double newBalance=0.0;
				if(coinItem!=null)
					newBalance=coinItem.getTotalValue();
				newBalance+=CMath.mul(newBalance,coinInterest);
				if(totalValue>0)
					newBalance+=CMath.mul(totalValue,itemInterest);
				for(int d=debts.size()-1;d>=0;d--)
				{
					final MoneyLibrary.DebtItem debtItem=debts.elementAt(d);
					final String debtor=debtItem.debtor();
					if(debtor.equalsIgnoreCase(name))
					{
						final long debtDueAt=debtItem.due();
						final double intRate=debtItem.interest();
						final double dueAmount=debtItem.amt();
						final String reason=debtItem.reason();
						final double intDue=CMath.mul(intRate,dueAmount);
						final long timeRemaining=debtDueAt-System.currentTimeMillis();
						if((timeRemaining<0)&&(newBalance<((dueAmount)+intDue)))
							newBalance=-1.0;
						else
						{
							final double amtDueNow=(timeRemaining<0)?(dueAmount+intDue):CMath.div((dueAmount+intDue),(timeRemaining/timeInterval));
							if(newBalance>=amtDueNow)
							{
								CMLib.beanCounter().bankLedger(bankChain(),name,CMLib.utensils().getFormattedDate(this)+": Withdrawal of "+CMLib.beanCounter().nameCurrencyShort(this,amtDueNow)+": Loan payment made.");
								CMLib.beanCounter().adjustDebt(debtor,bankChain(),intDue-amtDueNow,reason,intRate,debtDueAt);
								newBalance-=amtDueNow;
							}
							else
								CMLib.beanCounter().adjustDebt(debtor,bankChain(),intDue,reason,intRate,debtDueAt);
						}
						debts.removeElementAt(d);
					}
				}
				if(newBalance<0)
				{
					for(int v=0;v<items.size();v++)
					{
						final Item I=items.get(v);
						if((I instanceof LandTitle)&&(((LandTitle)I).getOwnerName().length()>0))
						{
							((LandTitle)I).setOwnerName("");
							((LandTitle)I).updateTitle();
							((LandTitle)I).updateLot(null);
						}
						if(!(I instanceof Coins))
							getShop().addStoreInventory(I);
					}
					delAllDeposits(name);
					CMLib.beanCounter().delAllDebt(name,bankChain());
				}
				else
				if((coinItem==null)||(newBalance!=coinItem.getTotalValue()))
				{
					if(coinItem!=null)
					{
						if(newBalance>coinItem.getTotalValue())
							CMLib.beanCounter().bankLedger(bankChain(),name,CMLib.utensils().getFormattedDate(this)+": Deposit of "+CMLib.beanCounter().nameCurrencyShort(this,newBalance-coinItem.getTotalValue())+": Interest paid.");
						else
							CMLib.beanCounter().bankLedger(bankChain(),name,CMLib.utensils().getFormattedDate(this)+": Withdrawl of "+CMLib.beanCounter().nameCurrencyShort(this,coinItem.getTotalValue()-newBalance)+": Interest charged.");
						delDepositInventory(name,coinItem);
					}
					final String currency=CMLib.beanCounter().getCurrency(this);
					coinItem=CMLib.beanCounter().makeBestCurrency(currency,newBalance);
					if(coinItem!=null)
						addDepositInventory(name,coinItem,null);
				}
				for(int v=0;v<items.size();v++)
				{
					final Item I=items.get(v);
					if(I!=null)
						I.destroy();
				}
			}
			for(int d=debts.size()-1;d>=0;d--)
				CMLib.beanCounter().delAllDebt(debts.elementAt(d).debtor(),bankChain());
		}
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;
		try
		{
			if(tickID==Tickable.TICKID_MOB)
			{
				// handle interest by watching the days go by...
				// put stuff up for sale if the account runs out
				AtomicBoolean flag;
				synchronized(StdBanker.class)
				{
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Map<String,AtomicBoolean> processingMap = (Map)Resources.getResource("SYSTEM_SEMAPHORES_BANKING");
					if(processingMap == null)
					{
						processingMap = new TreeMap<String,AtomicBoolean>();
						Resources.submitResource("SYSTEM_SEMAPHORES_BANKING", processingMap);
					}
					flag = processingMap.get(bankChain());
					if(flag == null)
					{
						flag = new AtomicBoolean(false);
						processingMap.put(bankChain(), flag);
					}
				}
				boolean isProcessing=true;
				synchronized(flag)
				{
					isProcessing = flag.get();
					if(!isProcessing)
						flag.set(true);
				}
				if(!isProcessing)
				{
					try
					{
						processAccounts();
					}
					finally
					{
						flag.set(false);
					}
				}
			}
		}
		catch (final Exception e)
		{
			Log.errOut("StdBanker", e);
		}
		return true;
	}

	@Override
	public double getBalance(String depositorName)
	{
		final Item old=findDepositInventory(depositorName,""+Integer.MAX_VALUE);
		if((old!=null)&&(old instanceof Coins))
			return ((Coins)old).getTotalValue();
		return 0;
	}

	@Override
	public double totalItemsWorth(String depositorName)
	{
		final List<Item> V=getDepositedItems(depositorName);
		double min=0;
		for(int v=0;v<V.size();v++)
		{
			final Item I=V.get(v);
			if(!(I instanceof Coins))
				min+=I.value();
			I.destroy();
		}
		return min;
	}

	@Override
	public String getBankClientName(MOB mob, Clan.Function func, boolean checked)
	{
		if(isSold(ShopKeeper.DEAL_CLANBANKER))
		{
			final Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(mob, func);
			if(clanPair!=null)
				return clanPair.first.clanID();
			else
			if(checked)
			{
				if(mob.clans().iterator().hasNext())
				{
					CMLib.commands().postSay(this,mob,L("I'm sorry, you aren't authorized by your clan to do that."),true,false);
					return null;
				}
				else
				{
					CMLib.commands().postSay(this,mob,L("I'm sorry, I only deal with clans."),true,false);
					return null;
				}
			}
		}
		return mob.Name();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					String depositorName=getBankClientName(msg.source(),Clan.Function.DEPOSIT,false);
					//if(msg.tool() instanceof Container)
					//	((Container)msg.tool()).emptyPlease(true);
					CMMsg msg2=CMClass.getMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
					location().send(this,msg2);
					msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
					location().send(this,msg2);
					if(msg.tool() instanceof Coins)
					{
						final Coins older=(Coins)msg.tool();
						double newValue=older.getTotalValue();
						Item oldCoins=findDepositInventory(depositorName,""+Integer.MAX_VALUE);
						if((oldCoins==null)
						&&(!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege()))
						{
							oldCoins=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if(oldCoins!=null)
							{
								final MOB owner=CMLib.players().getPlayer(msg.source().getLiegeID());
								if(owner != null)
									depositorName=owner.Name();
							}
						}
						if((oldCoins!=null)&&(oldCoins instanceof Coins))
							newValue+=((Coins)oldCoins).getTotalValue();
						final Coins coins=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(this),newValue);
						bankLedger(depositorName,"Deposit of "+CMLib.beanCounter().nameCurrencyShort(this,newValue)+": "+msg.source().Name());
						if(oldCoins!=null) 
							delDepositInventory(depositorName,oldCoins);
						if(coins!=null)
							addDepositInventory(depositorName,coins,null);
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							CMLib.commands().postSay(this,mob,L("Ok, @x1 now has a balance of @x2.",CMStrings.capitalizeFirstLetter(depositorName),CMLib.beanCounter().nameCurrencyLong(this,getBalance(depositorName))),true,false);
						else
							CMLib.commands().postSay(this,mob,L("Ok, your new balance is @x1.",CMLib.beanCounter().nameCurrencyLong(this,getBalance(depositorName))),true,false);
						recoverPhyStats();

						if(msg.sourceMessage()!=null)
							msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(),"<O-NAME>",msg.tool().name()));
						if(msg.targetMessage()!=null)
							msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(),"<O-NAME>",msg.tool().name()));
						if(msg.othersMessage()!=null)
							msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(),"<O-NAME>",msg.tool().name()));
						((Coins)msg.tool()).setNumberOfCoins(0); // prevents banker from accumulating wealth
						final double riches=CMLib.beanCounter().getTotalAbsoluteNativeValue(this);
						if(riches>0.0)
							CMLib.beanCounter().subtractMoney(this,riches);
					}
					else
					{
						List<Item> items;
						if(msg.tool() instanceof Container)
							items=CMLib.utensils().deepCopyOf((Item)msg.tool());
						else
							items=new XVector<Item>((Item)msg.tool().copyOf());
						for(Item I : items)
						{
							if(!I.amDestroyed())
							{
								addDepositInventory(depositorName,I,I.container());
							}
							else
							{
								CMLib.commands().postSay(this,mob,L("Whoops! Where'd it go?"),true,false);
								return;
							}
						}
						CMLib.commands().postSay(this,mob,L("Thank you, @x1 is safe with us.",items.get(0).name()),true,false);
						((Item)msg.tool()).destroy();
					}
				}
				return;
			case CMMsg.TYP_BORROW:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					final Item old=(Item)msg.tool();
					if(old instanceof Coins)
					{
						final String borrowerName=getBankClientName(msg.source(),Clan.Function.WITHDRAW,false);
						bankLedger(borrowerName,"Loan of "+old.Name()+": "+msg.source().Name());
						addItem(old);
						final double amt=((Coins)old).getTotalValue();
						final CMMsg newMsg=CMClass.getMsg(this,msg.source(),old,CMMsg.MSG_GIVE,L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF>."));
						if(location().okMessage(this,newMsg))
						{
							location().send(this,newMsg);
							((Coins)old).putCoinsBack();
						}
						else
							CMLib.commands().postDrop(this,old,true,false,false);
						final double interestRate=getLoanInterest();
						int months=2;
						while((months<(location().getArea().getTimeObj().getMonthsInYear()*10))
							&&(CMath.div(amt,months)>250.0)) months++;
						final long dueAt=System.currentTimeMillis()+(timeInterval()*months);
						CMLib.beanCounter().adjustDebt(borrowerName,bankChain(),amt,"Bank Loan",interestRate,dueAt);
					}
				}
				break;
			case CMMsg.TYP_WITHDRAW:
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					String withdrawerName=getBankClientName(msg.source(),Clan.Function.WITHDRAW,false);
					final Item old=(Item)msg.tool();
					if((old instanceof Coins)&&(old.container()==null))
					{
						Item depositInventoryItem=findDepositInventory(withdrawerName,""+Integer.MAX_VALUE);
						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege())
						&&((depositInventoryItem==null)
							||((depositInventoryItem instanceof Coins)
								&&(((Coins)depositInventoryItem).getTotalValue()<((Coins)old).getTotalValue()))))
						{
							final Item item2=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if((item2!=null)&&(item2 instanceof Coins)&&(((Coins)item2).getTotalValue()>=((Coins)old).getTotalValue()))
							{
								depositInventoryItem=item2;
								final MOB owner=CMLib.players().getPlayer(msg.source().getLiegeID());
								if(owner!=null)
									withdrawerName=owner.Name();
							}
						}
						if((depositInventoryItem instanceof Coins)
						&&(old instanceof Coins)
						&&(((Coins)depositInventoryItem).getTotalValue()>=((Coins)old).getTotalValue()))
						{
							final Coins coins=CMLib.beanCounter().makeBestCurrency(this,((Coins)depositInventoryItem).getTotalValue()-((Coins)old).getTotalValue());
							bankLedger(withdrawerName,"Withdrawl of "+CMLib.beanCounter().nameCurrencyShort(this,((Coins)old).getTotalValue())+": "+msg.source().Name());
							delDepositInventory(withdrawerName,depositInventoryItem);

							addItem(old);
							final CMMsg newMsg=CMClass.getMsg(this,msg.source(),old,CMMsg.MSG_GIVE,L("<S-NAME> give(s) <O-NAME> to <T-NAMESELF>."));
							if(location().okMessage(this,newMsg))
							{
								location().send(this,newMsg);
								((Coins)old).putCoinsBack();
							}
							else
								CMLib.commands().postDrop(this,old,true,false,false);
							if((coins==null)||(coins.getNumberOfCoins()<=0))
							{
								if(isSold(ShopKeeper.DEAL_CLANBANKER))
									CMLib.commands().postSay(this,mob,L("I have closed the account for @x1. Thanks for your business.",CMStrings.capitalizeFirstLetter(withdrawerName)),true,false);
								else
									CMLib.commands().postSay(this,mob,L("I have closed that account. Thanks for your business."),true,false);
								return;
							}
							addDepositInventory(withdrawerName,coins,null);
							if(isSold(ShopKeeper.DEAL_CLANBANKER))
								CMLib.commands().postSay(this,mob,L("Ok, @x1 now has a balance of @x2.",CMStrings.capitalizeFirstLetter(withdrawerName),CMLib.beanCounter().nameCurrencyLong(this,coins.getTotalValue())),true,false);
							else
								CMLib.commands().postSay(this,mob,L("Ok, your new balance is @x1.",CMLib.beanCounter().nameCurrencyLong(this,coins.getTotalValue())),true,false);
						}
						else
						if(depositInventoryItem!=null)
							CMLib.commands().postSay(this,mob,L("But, your balance is @x1.",CMLib.beanCounter().nameCurrencyLong(this,((Coins)depositInventoryItem).getTotalValue())),true,false);
					}
					else
					{
						List<Item> deletedItems=delDepositInventory(withdrawerName,old);
						if((deletedItems.size()==0)
						&&(!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege()))
							deletedItems = delDepositInventory(msg.source().getLiegeID(),old);
						CMLib.commands().postSay(this,mob,L("Thank you for your trust."),true,false);
						for(Item I : deletedItems)
						{
							if((I instanceof Coins)&&(I.container()!=null))
							{
								mob.addItem(I);
							}
							else
							{
								final Container C=I.container();
								if(location()!=null)
									location().addItem(I,ItemPossessor.Expire.Player_Drop);
								final CMMsg msg2=CMClass.getMsg(mob,I,this,CMMsg.MSG_GET,null);
								if(location().okMessage(mob,msg2))
								{
									location().send(mob,msg2);
									I.setContainer(C);
								}
							}
						}
					}

				}
				return;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_BUY:
				super.executeMsg(myHost,msg);
				return;
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
				{
					final String listerName=getBankClientName(msg.source(),Clan.Function.DEPOSIT_LIST,false);
					List<Item> V=null;
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						V=getDepositedItems(listerName);
					else
					{
						V=getDepositedItems(listerName);
						if(mob.isMarriedToLiege())
						{
							final List<Item> V2=getDepositedItems(mob.getLiegeID());
							if((V2!=null)&&(V2.size()>0))
								V.addAll(V2);
						}
					}
					for(int v=V.size()-1;v>=0;v--)
					{
						if(V.get(v) instanceof LandTitle)
						{
							final LandTitle L=(LandTitle)V.get(v);
							if(L.getOwnerObject()==null)
							{
								delDepositInventory(listerName,(Item)L);
								V.remove(L);
							}
						}
					}

					final int COL_LEN=CMLib.lister().fixColWidth(34.0,mob);
					StringBuffer str=new StringBuffer("");
					str.append(L("\n\rAccount balance at '@x1'.\n\r",bankChain()));
					final String c="^x[Item                              ] ";
					str.append(c+c+"^.^N\n\r");
					int colNum=0;
					boolean otherThanCoins=false;
					for(int i=0;i<V.size();i++)
					{
						final Item I=V.get(i);
						if((!(I instanceof Coins))&&(I.container()==null))
						{
							otherThanCoins=true;
							String col=null;
							col="["+CMStrings.padRight(I.name(mob),COL_LEN)+"] ";
							if((++colNum)>2)
							{
								str.append("\n\r");
								colNum=1;
							}
							str.append(col);
						}
					}
					if(!otherThanCoins)
						str=new StringBuffer("\n\r^N");
					else
						str.append("\n\r\n\r");
					final double balance=getBalance(listerName);
					if(balance>0)
					{
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							str.append(CMStrings.capitalizeFirstLetter(listerName)+" has a balance of ^H"+CMLib.beanCounter().nameCurrencyLong(this,balance)+"^?.");
						else
							str.append("Your balance is ^H"+CMLib.beanCounter().nameCurrencyLong(this,balance)+"^?.");
					}
					final Vector<MoneyLibrary.DebtItem> debts=CMLib.beanCounter().getDebt(listerName,bankChain());
					if(debts!=null)
					{
						for(int d=0;d<debts.size();d++)
						{
							final MoneyLibrary.DebtItem debt=debts.elementAt(d);
							final long debtDueAt=debt.due();
							final double intRate=debt.interest();
							final double dueAmount=debt.amt();
							final long timeRemaining=debtDueAt-System.currentTimeMillis();
							if(timeRemaining>0)
								str.append(L("\n\r@x1 owe ^H@x2^? in debt.\n\rMonthly interest is @x3%.  The loan must be paid in full in @x4 months.",((isSold(ShopKeeper.DEAL_CLANBANKER))?CMStrings.capitalizeFirstLetter(listerName):L("You")),CMLib.beanCounter().nameCurrencyLong(this,dueAmount),""+(intRate*100.0),""+(timeRemaining/timeInterval())));
						}
					}
					if(coinInterest!=0.0)
					{
						final double cci=CMath.mul(Math.abs(coinInterest),100.0);
						final String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append(L("\n\rThey @x1monthly on money deposited here.",ci));
					}
					if(itemInterest!=0.0)
					{
						final double cci=CMath.mul(Math.abs(itemInterest),100.0);
						final String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append(L("\n\rThey @x1monthly on items deposited here.",ci));
					}
					mob.tell(str.toString()+"^T");
					for(int i=0;i<V.size();i++)
					{
						final Item I=V.get(i);
						if(I!=null)
							I.destroy();
					}
				}
				return;
			}
			default:
				break;
			}
		}
		else
		if(msg.sourceMinor()==CMMsg.TYP_RETIRE)
			delAllDeposits(msg.source().Name());
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		final MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					if(msg.tool()==null)
						return false;
					final String listerName=getBankClientName(msg.source(),Clan.Function.DEPOSIT,true);
					if(listerName==null)
						return false;
					final double balance=getBalance(listerName);
					if(msg.tool() instanceof Coins)
					{
						if((Double.MAX_VALUE-balance)<=((Coins)msg.tool()).getTotalValue())
						{
							CMLib.commands().postSay(this,mob,L("I'm sorry, the law prevents us from holding that much money in one account."),true,false);
							return false;
						}
						if(!((Coins)msg.tool()).getCurrency().equalsIgnoreCase(CMLib.beanCounter().getCurrency(this)))
						{
							CMLib.commands().postSay(this,mob,L("I'm sorry, this bank only deals in @x1.",CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(this))),true,false);
							return false;
						}
						return true;
					}
					if(!(msg.tool() instanceof Item))
					{
						mob.tell(L("@x1 doesn't look interested.",mob.charStats().HeShe()));
						return false;
					}
					if(CMLib.flags().isEnspelled((Item)msg.tool()) 
					|| CMLib.flags().isOnFire((Item)msg.tool())
					||(msg.tool() instanceof CagedAnimal))
					{
						mob.tell(this,msg.tool(),null,L("<S-HE-SHE> refuses to accept <T-NAME> for deposit."));
						return false;
					}
					final double minbalance=(totalItemsWorth(listerName)/MIN_ITEM_BALANCE_DIVISOR)+CMath.div(((Item)msg.tool()).value(),MIN_ITEM_BALANCE_DIVISOR);
					if(balance<minbalance)
					{
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							CMLib.commands().postSay(this,mob,L("@x1 will need a total balance of @x2 for me to hold that.",CMStrings.capitalizeFirstLetter(listerName),CMLib.beanCounter().nameCurrencyShort(this,minbalance)),true,false);
						else
							CMLib.commands().postSay(this,mob,L("You'll need a total balance of @x1 for me to hold that.",CMLib.beanCounter().nameCurrencyShort(this,minbalance)),true,false);
						return false;
					}
				}
				return true;
			case CMMsg.TYP_WITHDRAW:
				{
					if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
						return false;
					String withdrawerName=getBankClientName(msg.source(),Clan.Function.WITHDRAW,true);
					if(withdrawerName==null)
						return false;
					if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
					{
						CMLib.commands().postSay(this,mob,L("What do you want? I'm busy!"),true,false);
						return false;
					}
					if((msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
					MOB owner=msg.source();
					double balance=getBalance(withdrawerName);
					final double collateral=totalItemsWorth(withdrawerName);
					if(msg.tool() instanceof Coins)
					{
						if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(this)))
						{
							CMLib.commands().postSay(this,mob,L("I'm sorry, I can only give you @x1.",CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(this))),true,false);
							return false;
						}

						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(owner.isMarriedToLiege())
						&&(balance<((Coins)msg.tool()).getTotalValue()))
						{
							final MOB M=CMLib.players().getLoadPlayer(owner.getLiegeID());
							double b=0.0;
							if(M!=null)
								b=getBalance(M.Name());
							if((M!=null)&&(b>=((Coins)msg.tool()).getTotalValue()))
							{
								owner=M;
								balance=b;
								withdrawerName=owner.Name();
							}
						}
					}
					else
					if(findDepositInventory(withdrawerName,msg.tool().Name())==null)
					{
						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege())
						&&(findDepositInventory(msg.source().getLiegeID(),msg.tool().Name())!=null))
							owner=CMLib.players().getPlayer(msg.source().getLiegeID());
						else
						{
							CMLib.commands().postSay(this,mob,L("You want WHAT?"),true,false);
							return false;
						}
					}
					else
					if(msg.tool() instanceof Item)
					{
						final double debt=CMLib.beanCounter().getDebtOwed(withdrawerName,bankChain());
						if((debt>0.0)
						&&((collateral-((Item)msg.tool()).value())<debt))
						{
							CMLib.commands().postSay(this,mob,L("I'm sorry, but that item is being held as collateral against your debt at this time."),true,false);
							return false;
						}
					}
					final double minbalance=(collateral/MIN_ITEM_BALANCE_DIVISOR);
					if(msg.tool() instanceof Coins)
					{
						if(((Coins)msg.tool()).getTotalValue()>balance)
						{
							if(isSold(ShopKeeper.DEAL_CLANBANKER))
								CMLib.commands().postSay(this,mob,L("I'm sorry,  @x1 has only @x2 in its account.",CMStrings.capitalizeFirstLetter(withdrawerName),CMLib.beanCounter().nameCurrencyShort(this,balance)),true,false);
							else
								CMLib.commands().postSay(this,mob,L("I'm sorry, you have only @x1 in that account.",CMLib.beanCounter().nameCurrencyShort(this,balance)),true,false);
							return false;
						}
						if(minbalance==0)
							return true;
						if(((Coins)msg.tool()).getTotalValue()>(balance-minbalance))
						{
							if((balance-minbalance)>0)
								CMLib.commands().postSay(this,mob,L("I'm sorry, you may only withdraw @x1  at this time.",CMLib.beanCounter().nameCurrencyShort(this,balance-minbalance)),true,false);
							else
								CMLib.commands().postSay(this,mob,L("I am holding other items in trust, so you may not withdraw funds at this time."),true,false);
							return false;
						}
					}
				}
				return true;
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			case CMMsg.TYP_VIEW:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_BUY:
				return super.okMessage(myHost,msg);
			case CMMsg.TYP_BORROW:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				if(!(msg.tool() instanceof Coins))
				{
					CMLib.commands().postSay(this,mob,L("I'm sorry, only MONEY can be borrowed."),true,false);
					return false;
				}
				final String withdrawerName=getBankClientName(msg.source(),Clan.Function.WITHDRAW,true);
				if(withdrawerName==null)
					return false;
				if((numberDeposited(withdrawerName)==0)
				&&((isSold(ShopKeeper.DEAL_CLANBANKER))
					||(!msg.source().isMarriedToLiege())
					||(numberDeposited(msg.source().getLiegeID())==0)))
				{
					final StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(L("@x1 does not have an account with us, I'm afraid.",CMStrings.capitalizeFirstLetter(withdrawerName)));
					else
						str.append(L("You don't have an account with us, I'm afraid."));
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				final double debt=CMLib.beanCounter().getDebtOwed(withdrawerName,bankChain());
				if(debt>0.0)
				{
					final StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(L("@x1 already has a @x2 loan out with us.",CMStrings.capitalizeFirstLetter(withdrawerName),CMLib.beanCounter().nameCurrencyShort(this,debt)));
					else
						str.append(L("You already have a @x1 loan out with us.",CMLib.beanCounter().nameCurrencyShort(this,debt)));
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				final double collateralRemaining=((Coins)msg.tool()).getTotalValue()-totalItemsWorth(withdrawerName);
				if(collateralRemaining>0)
				{
					final StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(CMStrings.capitalizeFirstLetter(withdrawerName)+" ");
					else
						str.append("You ");
					str.append(L("will need to deposit enough items with us as collateral.  You'll need items worth @x1 more to qualify.",CMLib.beanCounter().nameCurrencyShort(this,collateralRemaining)));
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				return true;
			}
			case CMMsg.TYP_LIST:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				final String listerName=getBankClientName(msg.source(),Clan.Function.DEPOSIT_LIST,true);
				if(listerName==null)
					return false;
				if((numberDeposited(listerName)==0)
				&&((isSold(ShopKeeper.DEAL_CLANBANKER))
					||(!msg.source().isMarriedToLiege())
					||(numberDeposited(msg.source().getLiegeID())==0)))
				{
					final StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(L("@x1 does not have an account with us, I'm afraid.",CMStrings.capitalizeFirstLetter(listerName)));
					else
						str.append(L("You don't have an account with us, I'm afraid."));
					if(coinInterest!=0.0)
					{
						final double cci=CMath.mul(Math.abs(coinInterest),100.0);
						final String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append(L("\n\rWe @x1monthly on money deposited here.",ci));
					}
					if(itemInterest!=0.0)
					{
						final double cci=CMath.mul(Math.abs(itemInterest),100.0);
						final String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append(L("\n\rWe @x1monthly on items kept with us.",ci));
					}
					if(bankChain().length()>0)
						str.append(L("\n\rI am a banker for @x1.",bankChain()));
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				return true;
			}
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}
}
