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


/*
   Copyright 2000-2014 Bo Zimmerman

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
	@Override public String ID(){return "StdBanker";}

	protected double coinInterest=-0.008;
	protected double itemInterest=-0.001;
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

	@Override public void addSoldType(int mask){setWhatIsSoldMask(CMath.abs(mask));}
	@Override
	public void setWhatIsSoldMask(long newSellCode)
	{
		super.setWhatIsSoldMask(newSellCode);
		if(!isSold(ShopKeeper.DEAL_CLANBANKER))
			whatIsSoldMask=ShopKeeper.DEAL_BANKER;
		else
			whatIsSoldMask=ShopKeeper.DEAL_CLANBANKER;
	}

	@Override public String bankChain(){return text();}
	@Override public void setBankChain(String name){setMiscText(name);}

	@Override
	public void addDepositInventory(String depositorName, Item thisThang)
	{
		String name=thisThang.ID();
		if(thisThang instanceof Coins) name="COINS";
		CMLib.catalog().updateCatalogIntegrity(thisThang);
		CMLib.database().DBCreateData(depositorName,bankChain(),""+thisThang+Math.random(),name+";"+CMLib.coffeeMaker().getPropertiesStr(thisThang,true));
	}

	protected Item makeItem(String data)
	{
		int x=data.indexOf(';');
		if(x<0) return null;
		Item I=null;
		if(data.substring(0,x).equals("COINS"))
			I=CMClass.getItem("StdCoins");
		else
			I=CMClass.getItem(data.substring(0,x));
		if(I!=null)
		{
			CMLib.coffeeMaker().setPropertiesStr(I,data.substring(x+1),true);
			if((I instanceof Coins)
			&&(((Coins)I).getDenomination()==0.0)
			&&(((Coins)I).getNumberOfCoins()>0))
				((Coins)I).setDenomination(1.0);
			I.recoverPhyStats();
			I.text();
			return I;
		}
		return null;
	}

	@Override
	public boolean delDepositInventory(String depositorName, Item thisThang)
	{
		List<PlayerData> V=getRawPDDepositInventory(depositorName);
		boolean money=thisThang instanceof Coins;
		boolean found=false;
		for(int v=V.size()-1;v>=0;v--)
		{
			DatabaseEngine.PlayerData PD=V.get(v);
			if(money&&(PD.xml).startsWith("COINS;"))
			{
				CMLib.database().DBDeleteData(PD.who,PD.section,PD.key);
				found=true;
			}
			if(!money)
			{
				Item I=makeItem(PD.xml);
				if(I==null) continue;
				if(thisThang.sameAs(I))
				{
					CMLib.database().DBDeleteData(PD.who,PD.section,PD.key);
					return true;
				}
			}
		}
		return found;
	}
	@Override
	public void delAllDeposits(String depositorName)
	{
		CMLib.database().DBDeleteData(depositorName,bankChain());
	}
	@Override
	public int numberDeposited(String depositorName)
	{
		return getRawPDDepositInventory(depositorName).size();
	}
	@Override
	public List<Item> getDepositedItems(String depositorName)
	{
		if((depositorName==null)||(depositorName.length()==0)) return new Vector<Item>();
		List<PlayerData> V=getRawPDDepositInventory(depositorName);
		Vector<Item> mine=new Vector<Item>();
		for(int v=0;v<V.size();v++)
		{
			DatabaseEngine.PlayerData PD=V.get(v);
			Item I=makeItem(PD.xml);
			if(I!=null)	mine.addElement(I);
		}
		return mine;
	}
	@Override
	public List<PlayerData> getRawPDDepositInventory(String depositorName)
	{
		return CMLib.database().DBReadData(depositorName,bankChain());
	}
	@Override
	public List<String> getAccountNames()
	{
		List<PlayerData> V=CMLib.database().DBReadData(bankChain());
		HashSet<String> h=new HashSet<String>();
		Vector<String> mine=new Vector<String>();
		for(int v=0;v<V.size();v++)
		{
			DatabaseEngine.PlayerData V2=V.get(v);
			if(!h.contains(V2.who))
			{
				h.add(V2.who);
				mine.addElement(V2.who);
			}
		}
		return mine;
	}

	protected void bankLedger(String depositorName, String msg)
	{
		String date=CMLib.utensils().getFormattedDate(this);
		CMLib.beanCounter().bankLedger(bankChain(),depositorName,date+": "+msg);
	}

	@Override
	public Item findDepositInventory(String depositorName, String likeThis)
	{
		List<PlayerData> V=getRawPDDepositInventory(depositorName);
		if(CMath.s_int(likeThis)>0)
			for(int v=0;v<V.size();v++)
			{
				DatabaseEngine.PlayerData PD=V.get(v);
				if(PD.xml.startsWith("COINS;"))
					return makeItem(PD.xml);
			}
		else
		for(int v=0;v<V.size();v++)
		{
			DatabaseEngine.PlayerData PD=V.get(v);
			Item I=makeItem(PD.xml);
			if(I==null) continue;
			if(CMLib.english().containsString(I.Name(),likeThis))
				return I;
		}
		return null;
	}

	public long timeInterval()
	{
		return (location().getArea().getTimeObj().getHoursInDay())
				*CMProps.getMillisPerMudHour()
				*location().getArea().getTimeObj().getDaysInMonth();
	}

	@Override public void setCoinInterest(double interest){coinInterest=interest;}
	@Override public void setItemInterest(double interest){itemInterest=interest;}
	@Override public double getCoinInterest(){return coinInterest;}
	@Override public double getItemInterest(){return itemInterest;}
	@Override public void setLoanInterest(double interest){loanInterest=interest;}
	@Override public double getLoanInterest(){return loanInterest;}
	@Override
	public MoneyLibrary.DebtItem getDebtInfo(String depositorName)
	{
		Vector<MoneyLibrary.DebtItem> debt=CMLib.beanCounter().getDebtOwed(bankChain());
		if(depositorName.length()==0) return null;
		for(int d=0;d<debt.size();d++)
			if(debt.elementAt(d).debtor.equalsIgnoreCase(depositorName))
				return debt.elementAt(d);
		return null;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)) return true;
		try
		{
		if(tickID==Tickable.TICKID_MOB)
		{
			boolean proceed=false;
			// handle interest by watching the days go by...
			// put stuff up for sale if the account runs out
			synchronized(bankChain().intern())
			{
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
					List<PlayerData> bankDataV=CMLib.database().DBReadData(bankChain());
					Vector<String> userNames=new Vector<String>();
					for(int v=0;v<bankDataV.size();v++)
					{
						DatabaseEngine.PlayerData dat=bankDataV.get(v);
						String name=dat.who;
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
					Vector<MoneyLibrary.DebtItem> debts=CMLib.beanCounter().getDebtOwed(bankChain());
					for(int u=0;u<userNames.size();u++)
					{
						String name=userNames.elementAt(u);
						Coins coinItem=null;
						int totalValue=0;
						List<Item> items=getDepositedItems(name);
						for(int v=0;v<items.size();v++)
						{
							Item I=items.get(v);
							if(I instanceof Coins)
								coinItem=(Coins)I;
							else
							if(itemInterest!=0.0)
								totalValue+=I.value();
						}
						double newBalance=0.0;
						if(coinItem!=null) newBalance=coinItem.getTotalValue();
						newBalance+=CMath.mul(newBalance,coinInterest);
						if(totalValue>0)
							newBalance+=CMath.mul(totalValue,itemInterest);
						for(int d=debts.size()-1;d>=0;d--)
						{
							MoneyLibrary.DebtItem debtItem=debts.elementAt(d);
							String debtor=debtItem.debtor;
							if(debtor.equalsIgnoreCase(name))
							{
								long debtDueAt=debtItem.due;
								double intRate=debtItem.interest;
								double dueAmount=debtItem.amt;
								String reason=debtItem.reason;
								double intDue=CMath.mul(intRate,dueAmount);
								long timeRemaining=debtDueAt-System.currentTimeMillis();
								if((timeRemaining<0)&&(newBalance<((dueAmount)+intDue)))
									newBalance=-1.0;
								else
								{
									double amtDueNow=(timeRemaining<0)?(dueAmount+intDue):CMath.div((dueAmount+intDue),(timeRemaining/timeInterval));
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
								Item I=items.get(v);
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
							String currency=CMLib.beanCounter().getCurrency(this);
							coinItem=CMLib.beanCounter().makeBestCurrency(currency,newBalance);
							if(coinItem!=null)
								addDepositInventory(name,coinItem);
						}
					}
					for(int d=debts.size()-1;d>=0;d--)
						CMLib.beanCounter().delAllDebt(debts.elementAt(d).debtor,bankChain());
				}
			}
		}
		}catch(Exception e){Log.errOut("StdBanker",e);}
		return true;
	}

	@Override
	public double getBalance(String depositorName)
	{
		Item old=findDepositInventory(depositorName,""+Integer.MAX_VALUE);
		if((old!=null)&&(old instanceof Coins))
			return ((Coins)old).getTotalValue();
		return 0;
	}

	@Override
	public double totalItemsWorth(String depositorName)
	{
		List<Item> V=getDepositedItems(depositorName);
		double min=0;
		for(int v=0;v<V.size();v++)
		{
			Item I=V.get(v);
			if(!(I instanceof Coins))
				min+=I.value();
		}
		return min;
	}

	@Override
	public String getBankClientName(MOB mob, Clan.Function func, boolean checked)
	{
		if(isSold(ShopKeeper.DEAL_CLANBANKER))
		{
			Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(mob, func);
			if(clanPair!=null)
				return clanPair.first.clanID();
			else
			if(checked)
			{
				if(mob.clans().iterator().hasNext())
				{
					CMLib.commands().postSay(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
					return null;
				}
				else
				{
					CMLib.commands().postSay(this,mob,"I'm sorry, I only deal with clans.",true,false);
					return null;
				}
			}
		}
		return mob.Name();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		MOB mob=msg.source();
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
			case CMMsg.TYP_DEPOSIT:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					String depositorName=getBankClientName(msg.source(),Clan.Function.DEPOSIT,false);
					if(msg.tool() instanceof Container)
						((Container)msg.tool()).emptyPlease(true);
					CMMsg msg2=CMClass.getMsg(msg.source(),msg.tool(),null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
					location().send(this,msg2);
					msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
					location().send(this,msg2);
					if(msg.tool() instanceof Coins)
					{
						Coins older=(Coins)msg.tool();
						double newValue=older.getTotalValue();
						Item old=findDepositInventory(depositorName,""+Integer.MAX_VALUE);
						if((old==null)
						&&(!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege()))
						{
							old=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if(old!=null)
							{
								MOB owner=CMLib.players().getPlayer(msg.source().getLiegeID());
								if(owner != null)
									depositorName=owner.Name();
							}
						}
						if((old!=null)&&(old instanceof Coins))
							newValue+=((Coins)old).getTotalValue();
						Coins item=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(this),newValue);
						bankLedger(depositorName,"Deposit of "+CMLib.beanCounter().nameCurrencyShort(this,newValue)+": "+msg.source().Name());
						if(old!=null) delDepositInventory(depositorName,old);
						if(item!=null)
							addDepositInventory(depositorName,item);
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							CMLib.commands().postSay(this,mob,"Ok, "+CMStrings.capitalizeFirstLetter(depositorName)+" now has a balance of "+CMLib.beanCounter().nameCurrencyLong(this,getBalance(depositorName))+".",true,false);
						else
							CMLib.commands().postSay(this,mob,"Ok, your new balance is "+CMLib.beanCounter().nameCurrencyLong(this,getBalance(depositorName))+".",true,false);
						recoverPhyStats();

						if(msg.sourceMessage()!=null) msg.setSourceMessage(CMStrings.replaceAll(msg.sourceMessage(),"<O-NAME>",msg.tool().name()));
						if(msg.targetMessage()!=null) msg.setTargetMessage(CMStrings.replaceAll(msg.targetMessage(),"<O-NAME>",msg.tool().name()));
						if(msg.othersMessage()!=null) msg.setOthersMessage(CMStrings.replaceAll(msg.othersMessage(),"<O-NAME>",msg.tool().name()));
						((Coins)msg.tool()).setNumberOfCoins(0); // prevents banker from accumulating wealth
						double riches=CMLib.beanCounter().getTotalAbsoluteNativeValue(this);
						if(riches>0.0) CMLib.beanCounter().subtractMoney(this,riches);
					}
					else
					{
						Item item =(Item)msg.tool().copyOf();
						if(!item.amDestroyed())
						{
							addDepositInventory(depositorName,item);
							CMLib.commands().postSay(this,mob,"Thank you, "+item.name()+" is safe with us.",true,false);
							((Item)msg.tool()).destroy();
						}
						else
							CMLib.commands().postSay(this,mob,"Whoops! Where'd it go?",true,false);
					}
				}
				return;
			case CMMsg.TYP_BORROW:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					Item old=(Item)msg.tool();
					if(old instanceof Coins)
					{
						String borrowerName=getBankClientName(msg.source(),Clan.Function.WITHDRAW,false);
						bankLedger(borrowerName,"Loan of "+old.Name()+": "+msg.source().Name());
						addItem(old);
						double amt=((Coins)old).getTotalValue();
						CMMsg newMsg=CMClass.getMsg(this,msg.source(),old,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
						if(location().okMessage(this,newMsg))
						{
							location().send(this,newMsg);
							((Coins)old).putCoinsBack();
						}
						else
							CMLib.commands().postDrop(this,old,true,false,false);
						double interestRate=getLoanInterest();
						int months=2;
						while((months<(location().getArea().getTimeObj().getMonthsInYear()*10))
							&&(CMath.div(amt,months)>250.0)) months++;
						long dueAt=System.currentTimeMillis()+(timeInterval()*months);
						CMLib.beanCounter().adjustDebt(borrowerName,bankChain(),amt,"Bank Loan",interestRate,dueAt);
					}
				}
				break;
			case CMMsg.TYP_WITHDRAW:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					String withdrawerName=getBankClientName(msg.source(),Clan.Function.WITHDRAW,false);
					Item old=(Item)msg.tool();
					if(old instanceof Coins)
					{
						Item depositInventoryItem=findDepositInventory(withdrawerName,""+Integer.MAX_VALUE);
						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege())
						&&((depositInventoryItem==null)
								||((depositInventoryItem instanceof Coins)
										&&(((Coins)depositInventoryItem).getTotalValue()<((Coins)old).getTotalValue()))))
						{
							Item item2=findDepositInventory(msg.source().getLiegeID(),""+Integer.MAX_VALUE);
							if((item2!=null)&&(item2 instanceof Coins)&&(((Coins)item2).getTotalValue()>=((Coins)old).getTotalValue()))
							{
								depositInventoryItem=item2;
								MOB owner=CMLib.players().getPlayer(msg.source().getLiegeID());
								if(owner!=null)
									withdrawerName=owner.Name();
							}
						}
						if((depositInventoryItem!=null)
						&&(depositInventoryItem instanceof Coins)
						&&(old instanceof Coins)
						&&(((Coins)depositInventoryItem).getTotalValue()>=((Coins)old).getTotalValue()))
						{
							Coins coins=CMLib.beanCounter().makeBestCurrency(this,((Coins)depositInventoryItem).getTotalValue()-((Coins)old).getTotalValue());
							bankLedger(withdrawerName,"Withdrawl of "+CMLib.beanCounter().nameCurrencyShort(this,((Coins)old).getTotalValue())+": "+msg.source().Name());
							delDepositInventory(withdrawerName,depositInventoryItem);

							addItem(old);
							CMMsg newMsg=CMClass.getMsg(this,msg.source(),old,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
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
									CMLib.commands().postSay(this,mob,"I have closed the account for "+CMStrings.capitalizeFirstLetter(withdrawerName)+". Thanks for your business.",true,false);
								else
									CMLib.commands().postSay(this,mob,"I have closed that account. Thanks for your business.",true,false);
								return;
							}
							addDepositInventory(withdrawerName,coins);
							if(isSold(ShopKeeper.DEAL_CLANBANKER))
								CMLib.commands().postSay(this,mob,"Ok, "+CMStrings.capitalizeFirstLetter(withdrawerName)+" now has a balance of "+CMLib.beanCounter().nameCurrencyLong(this,coins.getTotalValue())+".",true,false);
							else
								CMLib.commands().postSay(this,mob,"Ok, your new balance is "+CMLib.beanCounter().nameCurrencyLong(this,coins.getTotalValue())+".",true,false);
						}
						else
						if(depositInventoryItem!=null)
							CMLib.commands().postSay(this,mob,"But, your balance is "+CMLib.beanCounter().nameCurrencyLong(this,((Coins)depositInventoryItem).getTotalValue())+".",true,false);
					}
					else
					{
						if((!delDepositInventory(withdrawerName,old))
						&&(!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(msg.source().isMarriedToLiege()))
							delDepositInventory(msg.source().getLiegeID(),old);

						CMLib.commands().postSay(this,mob,"Thank you for your trust.",true,false);
						if(location()!=null)
							location().addItem(old,ItemPossessor.Expire.Player_Drop);
						CMMsg msg2=CMClass.getMsg(mob,old,this,CMMsg.MSG_GET,null);
						if(location().okMessage(mob,msg2))
							location().send(mob,msg2);
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
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					String listerName=getBankClientName(msg.source(),Clan.Function.DEPOSIT_LIST,false);
					List<Item> V=null;
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						V=getDepositedItems(listerName);
					else
					{
						V=getDepositedItems(listerName);
						if(mob.isMarriedToLiege())
						{
							List<Item> V2=getDepositedItems(mob.getLiegeID());
							if((V2!=null)&&(V2.size()>0))
								V.addAll(V2);
						}
					}
					for(int v=V.size()-1;v>=0;v--)
						if(V.get(v) instanceof LandTitle)
						{
							LandTitle L=(LandTitle)V.get(v);
							if(L.getOwnerObject()==null)
							{
								delDepositInventory(listerName,(Item)L);
								V.remove(L);
							}
						}

					final int COL_LEN=ListingLibrary.ColFixer.fixColWidth(34.0,mob);
					StringBuffer str=new StringBuffer("");
					str.append("\n\rAccount balance at '"+bankChain()+"'.\n\r");
					String c="^x[Item                              ] ";
					str.append(c+c+"^.^N\n\r");
					int colNum=0;
					boolean otherThanCoins=false;
					for(int i=0;i<V.size();i++)
					{
						Item I=V.get(i);
						if(!(I instanceof Coins))
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
					double balance=getBalance(listerName);
					if(balance>0)
					{
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							str.append(CMStrings.capitalizeFirstLetter(listerName)+" has a balance of ^H"+CMLib.beanCounter().nameCurrencyLong(this,balance)+"^?.");
						else
							str.append("Your balance is ^H"+CMLib.beanCounter().nameCurrencyLong(this,balance)+"^?.");
					}
					Vector<MoneyLibrary.DebtItem> debts=CMLib.beanCounter().getDebt(listerName,bankChain());
					if(debts!=null)
					for(int d=0;d<debts.size();d++)
					{
						MoneyLibrary.DebtItem debt=debts.elementAt(d);
						long debtDueAt=debt.due;
						double intRate=debt.interest;
						double dueAmount=debt.amt;
						long timeRemaining=debtDueAt-System.currentTimeMillis();
						if(timeRemaining>0)
							str.append("\n\r"
									+((isSold(ShopKeeper.DEAL_CLANBANKER))?CMStrings.capitalizeFirstLetter(listerName):"You")
									+" owe ^H"+CMLib.beanCounter().nameCurrencyLong(this,dueAmount)+"^? in debt.\n\r"
									+"Monthly interest is "+(intRate*100.0)+"%.  "
									+"The loan must be paid in full in "
									+(timeRemaining/timeInterval())+" months.");
					}
					if(coinInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(coinInterest),100.0);
						String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rThey "+ci+"monthly on money deposited here.");
					}
					if(itemInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(itemInterest),100.0);
						String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rThey "+ci+"monthly on items deposited here.");
					}
					mob.tell(str.toString()+"^T");
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
		MOB mob=msg.source();
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
					String listerName=getBankClientName(msg.source(),Clan.Function.DEPOSIT,true);
					if(listerName==null)
						return false;
					double balance=getBalance(listerName);
					if(msg.tool() instanceof Coins)
					{
						if((Double.MAX_VALUE-balance)<=((Coins)msg.tool()).getTotalValue())
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, the law prevents us from holding that much money in one account.",true,false);
							return false;
						}
						if(!((Coins)msg.tool()).getCurrency().equalsIgnoreCase(CMLib.beanCounter().getCurrency(this)))
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, this bank only deals in "+CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(this))+".",true,false);
							return false;
						}
						return true;
					}
					if(!(msg.tool() instanceof Item))
					{
						mob.tell(mob.charStats().HeShe()+" doesn't look interested.");
						return false;
					}
					if(CMLib.flags().isEnspelled((Item)msg.tool()) || CMLib.flags().isOnFire((Item)msg.tool()))
					{
						mob.tell(this,msg.tool(),null,"<S-HE-SHE> refuses to accept <T-NAME> for deposit.");
						return false;
					}
					double minbalance=(totalItemsWorth(listerName)/MIN_ITEM_BALANCE_DIVIDEND)+CMath.div(((Item)msg.tool()).value(),MIN_ITEM_BALANCE_DIVIDEND);
					if(balance<minbalance)
					{
						if(isSold(ShopKeeper.DEAL_CLANBANKER))
							CMLib.commands().postSay(this,mob,CMStrings.capitalizeFirstLetter(listerName)+" will need a total balance of "+CMLib.beanCounter().nameCurrencyShort(this,minbalance)+" for me to hold that.",true,false);
						else
							CMLib.commands().postSay(this,mob,"You'll need a total balance of "+CMLib.beanCounter().nameCurrencyShort(this,minbalance)+" for me to hold that.",true,false);
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
						CMLib.commands().postSay(this,mob,"What do you want? I'm busy!",true,false);
						return false;
					}
					if((msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
						return false;
					MOB owner=msg.source();
					double balance=getBalance(withdrawerName);
					double collateral=totalItemsWorth(withdrawerName);
					if(msg.tool() instanceof Coins)
					{
						if(!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(this)))
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, I can only give you "+CMLib.beanCounter().getDenominationName(CMLib.beanCounter().getCurrency(this))+".",true,false);
							return false;
						}

						if((!isSold(ShopKeeper.DEAL_CLANBANKER))
						&&(owner.isMarriedToLiege())
						&&(balance<((Coins)msg.tool()).getTotalValue()))
						{
							MOB M=CMLib.players().getLoadPlayer(owner.getLiegeID());
							double b=0.0;
							if(M!=null) b=getBalance(M.Name());
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
							CMLib.commands().postSay(this,mob,"You want WHAT?",true,false);
							return false;
						}
					}
					else
					if(msg.tool() instanceof Item)
					{
						double debt=CMLib.beanCounter().getDebtOwed(withdrawerName,bankChain());
						if((debt>0.0)
						&&((collateral-((Item)msg.tool()).value())<debt))
						{
							CMLib.commands().postSay(this,mob,"I'm sorry, but that item is being held as collateral against your debt at this time.",true,false);
							return false;
						}
					}
					double minbalance=(collateral/MIN_ITEM_BALANCE_DIVIDEND);
					if(msg.tool() instanceof Coins)
					{
						if(((Coins)msg.tool()).getTotalValue()>balance)
						{
							if(isSold(ShopKeeper.DEAL_CLANBANKER))
								CMLib.commands().postSay(this,mob,"I'm sorry,  "+CMStrings.capitalizeFirstLetter(withdrawerName)+" has only "+CMLib.beanCounter().nameCurrencyShort(this,balance)+" in its account.",true,false);
							else
								CMLib.commands().postSay(this,mob,"I'm sorry, you have only "+CMLib.beanCounter().nameCurrencyShort(this,balance)+" in that account.",true,false);
							return false;
						}
						if(minbalance==0) return true;
						if(((Coins)msg.tool()).getTotalValue()>(balance-minbalance))
						{
							if((balance-minbalance)>0)
								CMLib.commands().postSay(this,mob,"I'm sorry, you may only withdraw "+CMLib.beanCounter().nameCurrencyShort(this,balance-minbalance)+"  at this time.",true,false);
							else
								CMLib.commands().postSay(this,mob,"I am holding other items in trust, so you may not withdraw funds at this time.",true,false);
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
					CMLib.commands().postSay(this,mob,"I'm sorry, only MONEY can be borrowed.",true,false);
					return false;
				}
				String withdrawerName=getBankClientName(msg.source(),Clan.Function.WITHDRAW,true);
				if(withdrawerName==null)
					return false;
				if((numberDeposited(withdrawerName)==0)
				&&((isSold(ShopKeeper.DEAL_CLANBANKER))
					||(!msg.source().isMarriedToLiege())
					||(numberDeposited(msg.source().getLiegeID())==0)))
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(CMStrings.capitalizeFirstLetter(withdrawerName)+" does not have an account with us, I'm afraid.");
					else
						str.append("You don't have an account with us, I'm afraid.");
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				double debt=CMLib.beanCounter().getDebtOwed(withdrawerName,bankChain());
				if(debt>0.0)
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(CMStrings.capitalizeFirstLetter(withdrawerName)+" already has a "+CMLib.beanCounter().nameCurrencyShort(this,debt)+" loan out with us.");
					else
						str.append("You already have a "+CMLib.beanCounter().nameCurrencyShort(this,debt)+" loan out with us.");
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				double collateralRemaining=((Coins)msg.tool()).getTotalValue()-totalItemsWorth(withdrawerName);
				if(collateralRemaining>0)
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(CMStrings.capitalizeFirstLetter(withdrawerName)+" ");
					else
						str.append("You ");
					str.append("will need to deposit enough items with us as collateral.  You'll need items worth "
							+CMLib.beanCounter().nameCurrencyShort(this,collateralRemaining)+" more to qualify.");
					CMLib.commands().postSay(this,mob,str.toString()+"^T",true,false);
					return false;
				}
				return true;
			}
			case CMMsg.TYP_LIST:
			{
				if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this))
					return false;
				String listerName=getBankClientName(msg.source(),Clan.Function.DEPOSIT_LIST,true);
				if(listerName==null)
					return false;
				if((numberDeposited(listerName)==0)
				&&((isSold(ShopKeeper.DEAL_CLANBANKER))
					||(!msg.source().isMarriedToLiege())
					||(numberDeposited(msg.source().getLiegeID())==0)))
				{
					StringBuffer str=new StringBuffer("");
					if(isSold(ShopKeeper.DEAL_CLANBANKER))
						str.append(CMStrings.capitalizeFirstLetter(listerName)+" does not have an account with us, I'm afraid.");
					else
						str.append("You don't have an account with us, I'm afraid.");
					if(coinInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(coinInterest),100.0);
						String ci=((coinInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rWe "+ci+"monthly on money deposited here.");
					}
					if(itemInterest!=0.0)
					{
						double cci=CMath.mul(Math.abs(itemInterest),100.0);
						String ci=((itemInterest>0.0)?"pay ":"charge ")+cci+"% interest ";
						str.append("\n\rWe "+ci+"monthly on items kept with us.");
					}
					if(bankChain().length()>0)
						str.append("\n\rI am a banker for "+bankChain()+".");
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
