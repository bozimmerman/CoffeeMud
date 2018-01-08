package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary.XMLTag;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
public class BeanCounter extends StdLibrary implements MoneyLibrary
{
	@Override
	public String ID()
	{
		return "BeanCounter";
	}

	public Hashtable<String,MoneyDenomination[]> currencies=new Hashtable<String,MoneyDenomination[]>();
	public static Hashtable<String,MoneyDenomination[]> defaultCurrencies=new Hashtable<String,MoneyDenomination[]>();
	public Vector<String> allCurrencyNames=new Vector<String>();
	public Hashtable<String,List<String>> allCurrencyDenominationNames=new Hashtable<String,List<String>>();

	private class MoneyDenominationImpl implements MoneyDenomination
	{
		private final double value;
		private final String name;
		private final String abbr;
		
		@Override
		public double value()
		{
			return value;
		}
		
		@Override
		public String name()
		{
			return name;
		}
		
		@Override
		public String abbr()
		{
			return abbr;
		}
		
		protected MoneyDenominationImpl(double value,String name,String abbr)
		{
			this.value=value;
			this.name=name;
			this.abbr=abbr;
		}
	}
	
	@Override
	public void unloadCurrencySet(String currency)
	{
		String code=currency.toUpperCase().trim();
		final int x=code.indexOf('=');
		if(x>=0)
			code=code.substring(0,x).trim();
		if(currencies.containsKey(code))
		{
			allCurrencyNames.removeElement(code);
			currencies.remove(code);
			allCurrencyDenominationNames.remove(code);
		}
	}

	@Override
	public MoneyDenomination[] createCurrencySet(String currency)
	{
		return createCurrencySet(currencies,currency);
	}

	protected MoneyDenomination[] createCurrencySet(Hashtable<String,MoneyDenomination[]> currencies, String currency)
	{
		int x=currency.indexOf('=');
		if(x<0)
			return null;
		final String code=currency.substring(0,x).trim().toUpperCase();
		if(currencies.containsKey(code))
			return currencies.get(code);
		currency=currency.substring(x+1).trim();
		final List<String> CV=CMParms.parseSemicolons(currency,true);
		final Vector<MoneyDenomination> DV=new Vector<MoneyDenomination>();
		String s=null;
		String num=null;
		double d=0.0;
		final Vector<String> currencyNames=new Vector<String>();
		for(int v=0;v<CV.size();v++)
		{
			s=CV.get(v);
			x=s.indexOf(' ');
			if(x<0)
				continue;
			num=s.substring(0,x).trim();
			if(CMath.isDouble(num))
				d=CMath.s_double(num);
			else
			if(CMath.isInteger(num))
				d=CMath.s_int(num);
			else
				continue;
			s=s.substring(x+1).trim();
			String shortName="";
			if(s.endsWith(")"))
			{
				x=s.lastIndexOf(' ');
				if((x>0)&&(x<s.length()-1)&&(s.charAt(x+1)=='('))
				{
					shortName=s.substring(x+2,s.length()-1).trim();
					s=s.substring(0,x).trim();
				}
			}
			if((s!=null)&&(s.length()>0)&&(d>0.0))
			{
				int insertAt=-1;
				for(int i=0;i<DV.size();i++)
				{
					if(DV.elementAt(i).value()>d)
					{
						insertAt = i;
						break;
					}
				}
				if((insertAt<0)||(insertAt>=DV.size()))
					DV.addElement(new MoneyDenominationImpl(d,s,shortName));
				else
					DV.insertElementAt(new MoneyDenominationImpl(d,s,shortName),insertAt);
				currencyNames.addElement(s);
				if(shortName.length()>0)
					currencyNames.add(shortName);
			}
		}
		final MoneyDenomination[] DVs=new MoneyDenominationImpl[DV.size()];
		for(int i=0;i<DV.size();i++)
			DVs[i]=DV.elementAt(i);
		currencies.put(code,DVs);
		allCurrencyNames.addElement(code);
		allCurrencyDenominationNames.put(code,currencyNames);
		return DVs;
	}

	@Override
	public int getDenominationIndex(String currency, double value)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		if(DV!=null)
		for(int d=0;d<DV.length;d++)
		{
			if(value==DV[d].value())
				return d;
		}
		return -1;
	}

	@Override
	public MoneyDenomination[] getCurrencySet(String currency)
	{
		if(currency==null)
			return null;
		String code=currency.toUpperCase().trim();
		final int x=code.indexOf('=');
		if(x<0)
		{
			if(currencies.containsKey(code))
				return currencies.get(code);
			if(defaultCurrencies.size()==0)
			{
				createCurrencySet(defaultCurrencies,defaultCurrencyDefinition);
				createCurrencySet(defaultCurrencies,goldStandard);
				createCurrencySet(defaultCurrencies,copperStandard);
				createCurrencySet(defaultCurrencies,creditStandard);
				createCurrencySet(defaultCurrencies,dollarStandard);
			}
			if(defaultCurrencies.containsKey(code))
				return defaultCurrencies.get(code);
			return null;
		}
		code=code.substring(0,x).trim();
		if(currencies.containsKey(code))
			return currencies.get(code);
		return createCurrencySet(currency);
	}

	@Override
	public List<String> getAllCurrencies()
	{
		return allCurrencyNames;
	}

	@Override
	public List<String> getDenominationNameSet(String currency)
	{
		if(allCurrencyDenominationNames.containsKey(currency))
			return allCurrencyDenominationNames.get(currency);
		return new Vector<String>(1);
	}

	@Override
	public double lowestAbbreviatedDenomination(String currency)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		if(DV!=null)
		{
			for (final MoneyDenomination element : DV)
			{
				if(element.abbr().length()>0)
					return element.value();
			}
			return getLowestDenomination(currency);
		}
		return 1.0;
	}

	@Override
	public double lowestAbbreviatedDenomination(String currency, double absoluteAmount)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		if(DV!=null)
		{
			final double absoluteLowest=lowestAbbreviatedDenomination(currency);
			double lowestDenom=Double.MAX_VALUE;
			double diff=0.0;
			double denom=0.0;
			long num=0;
			for(int i=DV.length-1;i>=0;i--)
			{
				if(DV[i].abbr().length()>0)
				{
					denom=DV[i].value();
					if(denom<absoluteAmount)
					{
						num=Math.round(Math.floor(absoluteAmount/denom));
						diff=Math.abs(absoluteAmount-CMath.mul(denom,num));
						if(((diff/absoluteAmount)<0.05)&&(num>=10))
						{
							lowestDenom=denom;
							break;
						}
					}
				}
			}
			if(lowestDenom==Double.MAX_VALUE)
				lowestDenom=absoluteLowest;
			return lowestDenom;
		}
		return 1.0;
	}

	@Override
	public double abbreviatedRePrice(MOB shopkeeper, double absoluteAmount)
	{
		return abbreviatedRePrice(getCurrency(shopkeeper),absoluteAmount);
	}

	@Override
	public double abbreviatedRePrice(String currency, double absoluteAmount)
	{
		final double lowDenom=lowestAbbreviatedDenomination(currency,absoluteAmount);
		final long lowAmt=Math.round(absoluteAmount/lowDenom);
		return CMath.mul(lowDenom,lowAmt);
	}

	@Override
	public String abbreviatedPrice(MOB shopkeeper, double absoluteAmount)
	{
		return abbreviatedPrice(getCurrency(shopkeeper),absoluteAmount);
	}

	@Override
	public String abbreviatedPrice(String currency, double absoluteAmount)
	{
		final double lowDenom=lowestAbbreviatedDenomination(currency,absoluteAmount);
		final long lowAmt=Math.round(absoluteAmount/lowDenom);
		final String denominationShortCode=getDenominationShortCode(currency,lowDenom);
		if(denominationShortCode.length()==0)
			return ""+lowAmt;
		return lowAmt+denominationShortCode;
	}

	@Override
	public String getDenominationShortCode(String currency, double denomination)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		if(DV==null)
			return "";
		for (final MoneyDenomination element : DV)
		{
			if(element.value()==denomination)
				return element.abbr();
		}
		return "";
	}

	@Override
	public double getLowestDenomination(String currency)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		if((DV==null)||(DV.length==0))
			return 1.0;
		return DV[0].value();
	}

	@Override
	public String getDenominationName(String currency)
	{
		return getDenominationName(currency,getLowestDenomination(currency));
	}

	@Override
	public String getDenominationName(String currency, double denomination, long number)
	{
		final String s=getDenominationName(currency,denomination);
		if(s.toUpperCase().endsWith("(S)"))
		{
			if(number>1)
				return number+" "+s.substring(0,s.length()-3)+s.charAt(s.length()-2);
			return number+" "+s.substring(0,s.length()-3);
		}
		return number+" "+s;
	}

	@Override
	public double getBestDenomination(String currency, double absoluteValue)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		double denom=0.0;
		if(DV!=null)
		{
			final double low=getLowestDenomination(currency);
			for(int d=DV.length-1;d>=0;d--)
			{
				denom=DV[d].value();
				if((denom<=absoluteValue)
				&&(absoluteValue-(Math.floor(absoluteValue/denom)*denom)<low))
					return denom;
			}
		}
		return denom;
	}

	@Override
	public double getBestDenomination(String currency, int numberOfCoins, double absoluteValue)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		double bestDenom=0.0;
		if(DV!=null)
		{
			for(int d=DV.length-1;d>=0;d--)
			{
				final double denom=DV[d].value();
				if(((denom*(numberOfCoins))<=absoluteValue)
				&&(denom>bestDenom))
					bestDenom=denom;
			}
		}
		return bestDenom;
	}

	@Override
	public double[] getBestDenominations(String currency, double absoluteValue)
	{
		final MoneyDenomination[] DV=getCurrencySet(currency);
		final Vector<Double> V=new Vector<Double>();
		if(DV!=null)
		for(int d=DV.length-1;d>=0;d--)
		{
			final double denom=DV[d].value();
			if(denom<=absoluteValue)
			{
				final long number=Math.round(Math.floor(absoluteValue/denom));
				if(number>0)
				{
					V.addElement(Double.valueOf(denom));
					absoluteValue-=CMath.mul(denom,number);
				}
			}
		}
		final double[] ds=new double[V.size()];
		for(int d=0;d<V.size();d++)
			ds[d]=V.elementAt(d).doubleValue();
		return ds;
	}

	@Override
	public String getConvertableDescription(String currency, double denomination)
	{
		final double low=getLowestDenomination(currency);
		if(low==denomination)
			return "";
		return "Equal to "+getDenominationName(currency,low,Math.round(Math.floor(denomination/low)))+".";
	}

	@Override
	public String getDenominationName(final MOB mob, double denomination)
	{
		return getDenominationName(getCurrency(mob), denomination);
	}

	@Override
	public String getDenominationName(String currency, double denomination)
	{
		MoneyDenomination[] DV=getCurrencySet(currency);
		if((DV==null)||(DV.length==0))
			DV=getCurrencySet("");
		if((DV==null)||(DV.length==0))
			return "unknown!";
		int closestX=getDenominationIndex(currency, denomination);
		if(closestX<0)
		for(int i=0;i<DV.length;i++)
		{
			if(DV[i].value()<=denomination)
			{
				if((DV[i].value()==denomination)
				||(closestX<0)
				||((denomination-DV[i].value())<(denomination-DV[closestX].value())))
					closestX=i;
			}
		}
		if(closestX<0)
			return "unknown";
		return DV[closestX].name();
	}

	@Override
	public String nameCurrencyShort(MOB mob, double absoluteValue)
	{
		return nameCurrencyShort(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyShort(MOB mob, int absoluteValue)
	{
		return nameCurrencyShort(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyShort(String currency, double absoluteValue)
	{
		final double denom=getBestDenomination(currency,absoluteValue);
		if(denom>0.0)
			return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue/denom)));
		return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue)));
	}

	@Override
	public String nameCurrencyLong(MOB mob, double absoluteValue)
	{
		return nameCurrencyLong(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyLong(MOB mob, int absoluteValue)
	{
		return nameCurrencyLong(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyLong(String currency, double absoluteValue)
	{
		final StringBuffer str=new StringBuffer("");
		final double[] ds=getBestDenominations(currency,absoluteValue);
		for (final double denom : ds)
		{
			final long number=Math.round(Math.floor(absoluteValue/denom));
			final String name=getDenominationName(currency,denom,number);
			absoluteValue-=CMath.mul(denom,number);
			if(str.length()>0)
				str.append(", ");
			str.append(name);
		}
		return str.toString();
	}

	@Override
	public Coins makeBestCurrency(MOB mob, double absoluteValue, Environmental owner, Container container)
	{
		return makeBestCurrency(getCurrency(mob),absoluteValue,owner,container);
	}

	@Override
	public Coins makeBestCurrency(String currency, double absoluteValue, Environmental owner, Container container)
	{
		final Coins C=makeBestCurrency(currency,absoluteValue);
		if(C!=null)
		{
			if(owner instanceof Room)
				((Room)owner).addItem(C);
			if(owner instanceof MOB)
				((MOB)owner).addItem(C);
			C.setContainer(container);
			C.recoverPhyStats();
		}
		return C;
	}

	protected void parseDebt(Vector<DebtItem> debt, final String debtor, String xml)
	{
		final List<XMLLibrary.XMLTag> V=CMLib.xml().parseAllXML(xml);
		if(xml==null)
		{
			Log.errOut("BeanCounter","Unable to parse: "+xml);
			return;
		}
		final List<XMLLibrary.XMLTag> debtData=CMLib.xml().getContentsFromPieces(V,"DEBT");
		if(debtData==null)
		{
			Log.errOut("BeanCounter","Unable to get debt data");
			return;
		}
		for(int p=0;p<debtData.size();p++)
		{
			final XMLTag ablk=debtData.get(p);
			if((!ablk.tag().equalsIgnoreCase("OWE"))||(ablk.contents()==null)||(ablk.contents().size()==0))
				continue;
			final String owed=ablk.getValFromPieces("TO");
			final double amt=ablk.getDoubleFromPieces("AMT");
			final String reason=ablk.getValFromPieces("FOR");
			final long due=ablk.getLongFromPieces("DUE");
			final double interest=ablk.getDoubleFromPieces("INT");
			debt.addElement(new DebtItem()
			{
				double amount = amt;

				@Override
				public String debtor()
				{
					return debtor;
				}

				@Override
				public String owedTo()
				{
					return owed;
				}

				@Override
				public double amt()
				{
					return amount;
				}

				@Override
				public void setAmt(double amt)
				{
					amount = amt;
				}

				@Override
				public long due()
				{
					return due;
				}

				@Override
				public double interest()
				{
					return interest;
				}

				@Override
				public String reason()
				{
					return reason;
				}
			});
		}
	}

	protected String unparseDebt(Vector<DebtItem> debt, String name, String owedTo)
	{
		final StringBuffer xml=new StringBuffer("<DEBT>");
		for(int d=0;d<debt.size();d++)
		{
			if((debt.elementAt(d).debtor().equalsIgnoreCase(name))
			&&(debt.elementAt(d).owedTo().equalsIgnoreCase(owedTo)))
			{
				xml.append("<OWE>");
				xml.append(CMLib.xml().convertXMLtoTag("TO",debt.elementAt(d).owedTo()));
				xml.append(CMLib.xml().convertXMLtoTag("AMT",""+debt.elementAt(d).amt()));
				xml.append(CMLib.xml().convertXMLtoTag("FOR",debt.elementAt(d).reason()));
				xml.append(CMLib.xml().convertXMLtoTag("DUE",""+debt.elementAt(d).due()));
				xml.append(CMLib.xml().convertXMLtoTag("INT",""+debt.elementAt(d).interest()));
				xml.append("</OWE>");
			}
		}
		xml.append("</DEBT>");
		return xml.toString();
	}

	@Override
	public double getDebtOwed(String name, String owedTo)
	{
		final String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			final Vector<DebtItem> debt=getDebt(name,owedTo);
			double total=0.0;
			for(int d=0;d<debt.size();d++)
				total+=debt.elementAt(d).amt();
			return total;
		}
	}

	@Override
	public void delAllDebt(String name, String owedTo)
	{
		final String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			CMLib.database().DBDeletePlayerData(name.toUpperCase(),"DEBT",key);
		}
	}

	@Override
	public Vector<DebtItem> getDebtOwed(String owedTo)
	{
		final List<PlayerData> rows=CMLib.database().DBReadPlayerDataByKeyMask("DEBT",".*-DEBT-"+owedTo.toUpperCase().trim());
		final Vector<DebtItem> debt=new Vector<DebtItem>(rows.size());
		for(int r=0;r<rows.size();r++)
		{
			final PlayerData row=rows.get(r);
			final String debtor=row.who();
			final String xml=row.xml();
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	@Override
	public void adjustDebt(final String name, final String owedTo, double adjustAmt, final String reason, final double interest, final long due)
	{
		final String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			final Vector<DebtItem> debts=getDebt(name,owedTo);
			final boolean update=debts.size()>0;
			boolean done=false;
			for(int d=0;d<debts.size();d++)
			{
				final DebtItem debt=debts.elementAt(d);
				if((debt.debtor().equalsIgnoreCase(name))
				&&(debt.owedTo().equalsIgnoreCase(owedTo))
				&&(debt.interest()==interest)
				&&(debt.due()==due)
				&&(debt.reason().equalsIgnoreCase(reason)))
				{
					debt.setAmt(debt.amt()+adjustAmt);
					if(debt.amt()<=0.0)
						debts.removeElementAt(d);
					done=true;
					break;
				}
			}
			if((!done)&&(adjustAmt>=0.0))
			{
				final double initialAdjustedAmount = adjustAmt;
				debts.addElement(new DebtItem()
				{
					double amount = initialAdjustedAmount;

					@Override
					public String debtor()
					{
						return name;
					}

					@Override
					public String owedTo()
					{
						return owedTo;
					}

					@Override
					public double amt()
					{
						return amount;
					}

					@Override
					public void setAmt(double amt)
					{
						amount = amt;
					}

					@Override
					public long due()
					{
						return due;
					}

					@Override
					public double interest()
					{
						return interest;
					}

					@Override
					public String reason()
					{
						return reason;
					}
				});
			}

			final String xml=unparseDebt(debts,name,owedTo);
			if(update)
			{
				if(debts.size()==0)
					CMLib.database().DBDeletePlayerData(name.toUpperCase(),"DEBT",key);
				else
					CMLib.database().DBUpdatePlayerData(key,xml);
			}
			else
				CMLib.database().DBCreatePlayerData(name.toUpperCase(),"DEBT",key,xml);
		}
	}

	@Override
	public Vector<DebtItem> getDebt(String name, String owedTo)
	{
		final List<PlayerData> rows=CMLib.database().DBReadPlayerData(name.toUpperCase(),"DEBT",name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim());
		final Vector<DebtItem> debt=new Vector<DebtItem>(rows.size());
		for(int r=0;r<rows.size();r++)
		{
			final PlayerData row=rows.get(r);
			final String debtor=row.who();
			final String xml=row.xml();
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	@Override
	public Vector<DebtItem> getDebt(String name)
	{
		final List<PlayerData> rows=CMLib.database().DBReadPlayerData(name.toUpperCase(),"DEBT");
		final Vector<DebtItem> debt=new Vector<DebtItem>(rows.size());
		for(int r=0;r<rows.size();r++)
		{
			final PlayerData row=rows.get(r);
			final String debtor=row.who();
			final String xml=row.xml();
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	@Override
	public Coins makeBestCurrency(MOB mob, double absoluteValue)
	{
		return makeBestCurrency(getCurrency(mob),absoluteValue);
	}

	@Override
	public Coins makeCurrency(String currency, double denomination, long numberOfCoins)
	{
		if(numberOfCoins>0)
		{
			final Coins C=(Coins)CMClass.getItem("StdCoins");
			C.setCurrency(currency);
			C.setDenomination(denomination);
			C.setNumberOfCoins(numberOfCoins);
			C.recoverPhyStats();
			return C;
		}
		return null;
	}

	@Override
	public Coins makeBestCurrency(String currency, double absoluteValue)
	{
		final double denom=getBestDenomination(currency,absoluteValue);
		if(denom==0.0)
			return null;
		final long number=Math.round(Math.floor(absoluteValue/denom));
		if(number>0)
			return makeCurrency(currency,denom,number);
		return null;
	}

	@Override
	public List<Coins> makeAllCurrency(String currency, double absoluteValue)
	{
		final Vector<Coins> V=new Vector<Coins>();
		final double[] ds=getBestDenominations(currency,absoluteValue);
		for (final double denom : ds)
		{
			final long number=Math.round(Math.floor(absoluteValue/denom));
			if(number>0)
			{
				final Coins C=makeCurrency(currency,denom,number);
				if(C!=null)
				{
					absoluteValue-=C.getTotalValue();
					V.addElement(C);
				}
			}
		}
		return V;
	}

	@Override
	public void addMoney(MOB customer, int absoluteValue)
	{
		addMoney(customer,getCurrency(customer),(double)absoluteValue);
	}

	@Override
	public void addMoney(MOB customer, double absoluteValue)
	{
		addMoney(customer,getCurrency(customer),absoluteValue);
	}

	@Override
	public void addMoney(MOB customer, String currency,int absoluteValue)
	{
		addMoney(customer,currency,(double)absoluteValue);
	}

	@Override
	public void addMoney(MOB customer, Container container, String currency,int absoluteValue)
	{
		addMoney(customer,container,currency,(double)absoluteValue);
	}

	@Override
	public void addMoney(MOB mob, String currency, double absoluteValue)
	{
		addMoney(mob,null,currency,absoluteValue);
	}

	@Override
	public void addMoney(MOB mob, Container container, String currency, double absoluteValue)
	{
		if(mob==null)
			return;
		final List<Coins> V=makeAllCurrency(currency,absoluteValue);
		for(int i=0;i<V.size();i++)
		{
			final Coins C=V.get(i);
			C.setContainer(container);
			mob.addItem(C);
			C.putCoinsBack();
		}
		mob.recoverPhyStats();
	}

	@Override
	public void giveSomeoneMoney(MOB recipient, double absoluteValue)
	{
		giveSomeoneMoney(recipient,recipient,getCurrency(recipient),absoluteValue);
	}

	@Override
	public void giveSomeoneMoney(MOB recipient, String currency, double absoluteValue)
	{
		giveSomeoneMoney(recipient,recipient,currency,absoluteValue);
	}

	@Override
	public void giveSomeoneMoney(MOB banker, MOB customer, double absoluteValue)
	{
		giveSomeoneMoney(banker,customer,getCurrency(banker),absoluteValue);
	}

	@Override
	public void giveSomeoneMoney(MOB banker, MOB customer, String currency, double absoluteValue)
	{
		if(banker==null)
			banker=customer;
		if(banker==customer)
		{
			addMoney(customer,currency,absoluteValue);
			return;
		}

		final List<Coins> V=makeAllCurrency(currency,absoluteValue);
		for(int i=0;i<V.size();i++)
		{
			final Coins C=V.get(i);
			banker.addItem(C);
			final CMMsg newMsg=CMClass.getMsg(banker,customer,C,CMMsg.MSG_GIVE,L("<S-NAME> give(s) @x1 to <T-NAMESELF>.",C.Name()));
			if(banker.location().okMessage(banker,newMsg))
			{
				banker.location().send(banker,newMsg);
				C.putCoinsBack();
			}
			else
				CMLib.commands().postDrop(banker,C,true,false,false);
		}
		banker.recoverPhyStats();
		customer.recoverPhyStats();
	}

	@Override
	public void dropMoney(Room R, Container container, String currency, double absoluteValue)
	{
		final List<Coins> V=makeAllCurrency(currency,absoluteValue);
		for(final Coins I : V)
		{
			I.setContainer(container);
			R.addItem(I,ItemPossessor.Expire.Monster_EQ);
			I.putCoinsBack();
		}
	}

	@Override
	public void removeMoney(Room R, Container container, String currency, double absoluteValue)
	{
		double myMoney=getTotalAbsoluteValue(R,container,currency);
		final List<Coins> V=getStandardCurrency(R,container,currency);
		for(int v=0;v<V.size();v++)
			((Item)V.get(v)).destroy();
		if(myMoney>=absoluteValue)
			myMoney-=absoluteValue;
		else
			myMoney=0.0;
		if(myMoney>0.0)
			dropMoney(R,container,currency,myMoney);
	}

	@Override
	public void bankLedger(String bankName, String owner, String explanation)
	{
		synchronized((this+"LEDGER"+bankName).intern())
		{
			final List<PlayerData> V=CMLib.database().DBReadPlayerData(owner,"LEDGER-"+bankName,"LEDGER-"+bankName+"/"+owner);
			if((V!=null)&&(V.size()>0))
			{
				final DatabaseEngine.PlayerData D=V.get(0);
				String last=D.xml();
				if(last.length()>4096)
				{
					final int x=last.indexOf(";|;",1024);
					if(x>=0)
						last=last.substring(x+3);
				}
				CMLib.database().DBReCreatePlayerData(owner,D.section(),D.key(),last+explanation+";|;");
			}
			else
				CMLib.database().DBCreatePlayerData(owner,"LEDGER-"+bankName,"LEDGER-"+bankName+"/"+owner,explanation+";|;");
		}
	}

	@Override
	public boolean modifyBankGold(String bankName, String owner, String explanation, String currency, double absoluteAmount)
	{
		final List<PlayerData> V;
		if((bankName==null)||(bankName.length()==0))
			V=CMLib.database().DBReadAllPlayerData(owner);
		else
			V=CMLib.database().DBReadPlayerData(owner, bankName);
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData D=V.get(v);
			final String last=D.xml();
			if(last.startsWith("COINS;"))
			{
				if((bankName==null)||(bankName.length()==0)||(bankName.equals(D.section())))
				{
					Coins C=(Coins)CMClass.getItem("StdCoins");
					CMLib.coffeeMaker().setPropertiesStr(C,last.substring(6),true);
					if((C.getDenomination()==0.0)&&(C.getNumberOfCoins()>0))
						C.setDenomination(1.0);
					C.recoverPhyStats();
					final double value=C.getTotalValue();
					if((absoluteAmount>0.0)||(value>=(-absoluteAmount)))
					{
						C=makeBestCurrency(currency,value+absoluteAmount);
						if(C!=null)
							CMLib.database().DBReCreatePlayerData(owner,D.section(),D.key(),"COINS;"+CMLib.coffeeMaker().getPropertiesStr(C,true));
						else
							CMLib.database().DBDeletePlayerData(owner,D.section(),D.key());
						bankLedger(bankName,owner,explanation);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean modifyThisAreaBankGold(Area A,
										  Set<String> triedBanks,
										  String owner,
										  String explanation,
										  String currency,
										  double absoluteAmount)
	{
		Banker B=null;
		Room R=null;
		for(final Enumeration<Banker> e=CMLib.map().banks();e.hasMoreElements();)
		{
			B=e.nextElement();
			R=CMLib.map().roomLocation(B);
			if((R!=null)
			&&(R.getArea()==A)
			&&(!triedBanks.contains(B.bankChain())))
			{
				triedBanks.add(B.bankChain());
				if(modifyBankGold(B.bankChain(),owner,explanation,currency,absoluteAmount))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean modifyLocalBankGold(Area A,
									   String owner,
									   String explanation,
									   String currency,
									   double absoluteAmount)
	{
		final HashSet<String> triedBanks=new HashSet<String>();
		if(modifyThisAreaBankGold(A,triedBanks,owner,explanation,currency,absoluteAmount))
			return true;
		for(final Enumeration<Area> e=A.getParents();e.hasMoreElements();)
		{
			final Area A2=e.nextElement();
			if(modifyThisAreaBankGold(A2,triedBanks,owner,explanation,currency,absoluteAmount))
				return true;
		}
		return modifyBankGold(null,owner,explanation,currency,absoluteAmount);
	}

	@Override
	public void subtractMoneyGiveChange(MOB banker, MOB mob, int absoluteAmount)
	{
		subtractMoneyGiveChange(banker,mob,(double)absoluteAmount);
	}

	@Override
	public void subtractMoneyGiveChange(MOB banker, MOB mob, double absoluteAmount)
	{
		subtractMoneyGiveChange(banker, mob,(banker!=null)?getCurrency(banker):getCurrency(mob),absoluteAmount);
	}

	@Override
	public void subtractMoneyGiveChange(MOB banker, MOB mob, String currency, double absoluteAmount)
	{
		if(mob==null)
			return;
		double myMoney=getTotalAbsoluteValue(mob,currency);
		final List<Coins> V=getStandardCurrency(mob,currency);
		for(int v=0;v<V.size();v++)
			((Item)V.get(v)).destroy();
		if(myMoney>=absoluteAmount)
			myMoney-=absoluteAmount;
		else
			myMoney=0.0;
		if(myMoney>0.0)
			giveSomeoneMoney(banker,mob,currency,myMoney);
	}

	@Override
	public void setMoney(MOB mob, double absoluteAmount)
	{
		clearZeroMoney(mob,null);
		addMoney(mob,getCurrency(mob),absoluteAmount);
	}

	@Override
	public void setMoney(MOB mob, String currency, double absoluteAmount)
	{
		clearZeroMoney(mob,currency);
		addMoney(mob,currency,absoluteAmount);
	}

	@Override
	public void subtractMoney(MOB mob, double absoluteAmount)
	{
		subtractMoney(mob,getCurrency(mob),absoluteAmount);
	}

	@Override
	public void subtractMoney(MOB mob, String currency, double absoluteAmount)
	{
		subtractMoney(mob,null,currency,absoluteAmount);
	}

	@Override
	public void subtractMoney(MOB mob, Container container, String currency, double absoluteAmount)
	{
		if(mob==null)
			return;
		double myMoney=getTotalAbsoluteValue(mob,container,currency);
		final List<Coins> V=getStandardCurrency(mob,container,currency);
		for(int v=0;v<V.size();v++)
			((Item)V.get(v)).destroy();
		if(myMoney>=absoluteAmount)
			myMoney-=absoluteAmount;
		else
			myMoney=0.0;
		if(myMoney>0.0)
			addMoney(mob,container,currency,myMoney);
	}

	@Override
	public int getMoney(MOB mob)
	{
		if(mob==null)
			return 0;
		long money=mob.getMoney();
		if(money>0)
			return mob.getMoney();
		final List<Coins> V=getStandardCurrency(mob,null);
		for(int i=0;i<V.size();i++)
			money+=Math.round(V.get(i).getTotalValue());
		if(money>Integer.MAX_VALUE/2)
			return Integer.MAX_VALUE/2;
		return (int)money;
	}

	@Override
	public void setMoney(MOB mob, int amount)
	{
		if(mob==null)
			return;
		clearZeroMoney(mob,null);
		mob.setMoney(amount);
	}

	@Override
	public void clearZeroMoney(MOB mob, String currency)
	{
		if(mob==null)
			return;
		mob.setMoney(0);
		clearInventoryMoney(mob,currency);
	}

	@Override
	public void clearInventoryMoney(MOB mob, String currency)
	{
		if(mob==null) 
			return;
		List<Item> clear=null;
		Item I=null;
		for(int i=0;i<mob.numItems();i++)
		{
			I=mob.getItem(i);
			if((I instanceof Coins)
			&&(((Coins)I).container()==null))
			{
				if(clear==null) 
					clear=new ArrayList<Item>();
				if(currency==null)
					clear.add(I);
				else
				if(((Coins)I).getCurrency().equalsIgnoreCase(currency))
					clear.add(I);
			}
		}
		if(clear!=null)
		{
			for(int i=0;i<clear.size();i++)
				clear.get(i).destroy();
		}
	}

	@Override
	public void subtractMoney(MOB mob, double denomination, double absoluteAmount)
	{
		subtractMoney(mob,getCurrency(mob),denomination,absoluteAmount);
	}

	@Override
	public void subtractMoney(MOB mob, String currency, double denomination, double absoluteAmount)
	{
		if(mob==null)
			return;
		final List<Coins> V=getStandardCurrency(mob,currency);
		Coins C=null;
		for(int v=0;v<V.size();v++)
		{
			C=V.get(v);
			if(C.getDenomination()==denomination)
			{
				if(C.getTotalValue()>absoluteAmount)
				{
					C.setNumberOfCoins(Math.round(Math.floor((C.getTotalValue()-absoluteAmount)/denomination)));
					C.text();
					break;
				}
				absoluteAmount-=C.getTotalValue();
				C.destroy();
			}
		}
	}

	@Override
	public List<Coins> getStandardCurrency(Room R, Item container, String currency)
	{
		final Vector<Coins> V=new Vector<Coins>();
		if(R==null)
			return V;
		for(int i=0;i<R.numItems();i++)
		{
			final Item I=R.getItem(i);
			if((I!=null)
			&&(I instanceof Coins)
			&&((currency==null)||((Coins)I).getCurrency().equalsIgnoreCase(currency))
			&&(I.container()==container))
				V.addElement((Coins)I);
		}
		return V;
	}

	@Override
	public List<Coins> getStandardCurrency(MOB mob, String currency)
	{
		return getStandardCurrency(mob, null, currency);
	}

	@Override
	public List<Coins> getStandardCurrency(MOB mob, Item container, String currency)
	{
		final Vector<Coins> V=new Vector<Coins>();
		if(mob==null)
			return V;
		if(((currency==null)||(currency.equals(getCurrency(mob))))&&(mob.getMoney()>0)&&(container==null))
		{
			addMoney(mob,getCurrency(mob),(double)mob.getMoney());
			mob.setMoney(0);
		}
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if((I!=null)
			&&(I instanceof Coins)
			&&((currency==null)||((Coins)I).getCurrency().equalsIgnoreCase(currency))
			&&(I.container()==container))
				V.addElement((Coins)I);
		}
		return V;
	}

	@Override
	public long getNumberOfCoins(MOB mob, String currency, double denomination)
	{
		final List<Coins> V=getStandardCurrency(mob,currency);
		long gold=0;
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).getDenomination()==denomination)
				gold+=V.get(v).getNumberOfCoins();
		}
		return gold;
	}

	@Override
	public String getCurrency(Environmental E)
	{
		if(E instanceof MOB)
		{
			if(((MOB)E).getStartRoom()!=null)
				return getCurrency(((MOB)E).getStartRoom());
			else
			if(((MOB)E).location()!=null)
				return getCurrency(((MOB)E).location());
		}
		else
		if(E instanceof Room)
			return getCurrency(((Room)E).getArea());
		else
		if(E instanceof Coins)
			return ((Coins)E).getCurrency();
		else
		if(E instanceof Area)
		{
			String s=((Area)E).getCurrency();
			if(s.length()==0)
			{
				for(final Enumeration<Area> p=((Area)E).getParents();p.hasMoreElements();)
				{
					s=getCurrency(p.nextElement());
					if(s.length()>0)
						break;
				}
			}
			final int x=s.indexOf('=');
			if(x<0)
				return s.toUpperCase().trim();
			return s.substring(0,x).toUpperCase().trim();
		}
		return "";
	}

	@Override
	public double getTotalAbsoluteValue(Room R, Item container, String currency)
	{
		double money=0.0;
		final List<Coins> V=getStandardCurrency(R,container,currency);
		for(int v=0;v<V.size();v++)
			money+=V.get(v).getTotalValue();
		return money;
	}

	@Override
	public double getTotalAbsoluteValue(MOB mob, String currency)
	{
		return getTotalAbsoluteValue(mob, null, currency);
	}

	@Override
	public double getTotalAbsoluteValue(MOB mob, Item container, String currency)
	{
		double money=0.0;
		final List<Coins> V=getStandardCurrency(mob,container,currency);
		for(int v=0;v<V.size();v++)
			money+=V.get(v).getTotalValue();
		return money;
	}

	@Override
	public double getTotalAbsoluteNativeValue(MOB mob)
	{
		double money=0.0;
		final List<Coins> V=getStandardCurrency(mob,getCurrency(mob));
		for(int v=0;v<V.size();v++)
			money+=V.get(v).getTotalValue();
		return money;
	}

	@Override
	public double getTotalAbsoluteShopKeepersValue(MOB mob, MOB shopkeeper)
	{
		double money=0.0;
		final List<Coins> V=getStandardCurrency(mob,getCurrency(shopkeeper));
		for(int v=0;v<V.size();v++)
			money+=V.get(v).getTotalValue();
		return money;
	}

	@Override
	public double getTotalAbsoluteValueAllCurrencies(MOB mob)
	{
		return getTotalAbsoluteValue(mob,null);
	}

}
