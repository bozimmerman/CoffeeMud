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
   Copyright 2005-2022 Bo Zimmerman

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

	public static Map<String,MoneyDefinition> defaultCurrencies=new Hashtable<String,MoneyDefinition>();

	public Map<String, MoneyDefinition	>	currencies						= new Hashtable<String, MoneyDefinition>();
	public List<String>						allCurrencyNames				= new Vector<String>();
	public Map<String, List<String>>		allCurrencyDenominationNames	= new Hashtable<String, List<String>>();

	public final String defaultCurrency="=1 gold coin(s);100 golden note(s);10000 whole note(s);1000000 Archon note(s)";
	public final String goldStandard=	"GOLD=0.01 copper piece(s) (cp);0.1 silver piece(s) (sp);1.0 gold piece(s) (gp);5.0 platinum piece(s) (pp)";
	public final String copperStandard=	"COPPER=1 copper bit(s) (cb);10 silver bit(s) (sb);100 gold bit(s) (gb);500 platinum bit(s) (pb);10000 aluminum bit(s) (ab)";
	public final String creditStandard=	"CREDIT=1 credit(s) (c);1000 megacredit(s) (mc);1000000 gigacredit(s) (gc)";
	public final String dollarStandard=	"DOLLAR=1 cent(s) (c);100 dollar(s) ($);100000 grand (k$)";
	public final String victoryStandard="VICTORY=notrade;1 victory point(s) (v)";


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

		protected MoneyDenominationImpl(final double value,final String name,final String abbr)
		{
			this.value=value;
			this.name=name;
			this.abbr=abbr;
		}
	}

	private class MoneyDefinitionImpl implements MoneyDefinition
	{
		private final String ID;
		private final boolean canTrade;
		private final MoneyDenomination[] denominations;

		@Override
		public String ID()
		{
			return ID;
		}

		@Override
		public boolean canTrade()
		{
			return canTrade;
		}

		@Override
		public MoneyDenomination[] denominations()
		{
			return denominations;
		}

		protected MoneyDefinitionImpl(final String ID, final MoneyDenomination[] denominations,final boolean canTrade)
		{
			this.ID=ID;
			this.denominations=denominations;
			this.canTrade=canTrade;
		}
	}

	@Override
	public void unloadCurrencySet(final String currency)
	{
		String code=currency.toUpperCase().trim();
		final int x=code.indexOf('=');
		if(x>=0)
			code=code.substring(0,x).trim();
		if(currencies.containsKey(code))
		{
			allCurrencyNames.remove(code);
			currencies.remove(code);
			allCurrencyDenominationNames.remove(code);
		}
	}

	@Override
	public MoneyDefinition createCurrencySet(final String currency)
	{
		return createCurrencySet(currencies,currency);
	}

	protected MoneyDefinition createCurrencySet(final Map<String,MoneyDefinition> currencies, String currency)
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
		boolean canTrade=true;
		final Vector<String> currencyNames=new Vector<String>();
		for(int v=0;v<CV.size();v++)
		{
			s=CV.get(v);
			x=s.indexOf(' ');
			if(x<0)
			{
				if(s.equalsIgnoreCase("notrade"))
					canTrade=false;
				continue;
			}
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
		final MoneyDenomination[] newDenomVs=new MoneyDenominationImpl[DV.size()];
		for(int i=0;i<DV.size();i++)
			newDenomVs[i]=DV.elementAt(i);
		final MoneyDefinition def=new MoneyDefinitionImpl(code,newDenomVs,canTrade);
		currencies.put(code,def);
		allCurrencyNames.add(code);
		allCurrencyDenominationNames.put(code,currencyNames);
		return def;
	}

	@Override
	public int getDenominationIndex(final String currency, final double value)
	{
		final MoneyDefinition DV=getCurrencySet(currency);
		if(DV!=null)
		{
			final MoneyDenomination[] V=DV.denominations();
			for(int d=0;d<V.length;d++)
			{
				if(value==V[d].value())
					return d;
			}
		}
		return -1;
	}

	@Override
	public MoneyDefinition getCurrencySet(final String currency)
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
				createCurrencySet(defaultCurrencies,defaultCurrency);
				createCurrencySet(defaultCurrencies,goldStandard);
				createCurrencySet(defaultCurrencies,copperStandard);
				createCurrencySet(defaultCurrencies,creditStandard);
				createCurrencySet(defaultCurrencies,dollarStandard);
				createCurrencySet(defaultCurrencies,victoryStandard);
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
	public List<String> getDenominationNameSet(final String currency)
	{
		if(allCurrencyDenominationNames.containsKey(currency))
			return allCurrencyDenominationNames.get(currency);
		return new Vector<String>(1);
	}

	@Override
	public double lowestAbbreviatedDenomination(final String currency)
	{
		final MoneyDefinition DV=getCurrencySet(currency);
		if(DV!=null)
		{
			for (final MoneyDenomination element : DV.denominations())
			{
				if(element.abbr().length()>0)
					return element.value();
			}
			return getLowestDenomination(currency);
		}
		return 1.0;
	}

	@Override
	public double lowestAbbreviatedDenomination(final String currency, final double absoluteAmount)
	{
		final MoneyDefinition DV=getCurrencySet(currency);
		if(DV!=null)
		{
			final double absoluteLowest=lowestAbbreviatedDenomination(currency);
			double lowestDenom=Double.MAX_VALUE;
			double diff=0.0;
			double denom=0.0;
			long num=0;
			final MoneyDenomination[] V=DV.denominations();
			for(int i=V.length-1;i>=0;i--)
			{
				if(V[i].abbr().length()>0)
				{
					denom=V[i].value();
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
	public double abbreviatedRePrice(final MOB shopkeeper, final double absoluteAmount)
	{
		return abbreviatedRePrice(getCurrency(shopkeeper),absoluteAmount);
	}

	@Override
	public double abbreviatedRePrice(final String currency, final double absoluteAmount)
	{
		final double lowDenom=lowestAbbreviatedDenomination(currency,absoluteAmount);
		final long lowAmt=Math.round(absoluteAmount/lowDenom);
		return CMath.mul(lowDenom,lowAmt);
	}

	@Override
	public String abbreviatedPrice(final MOB shopkeeper, final double absoluteAmount)
	{
		return abbreviatedPrice(getCurrency(shopkeeper),absoluteAmount);
	}

	@Override
	public String abbreviatedPrice(final String currency, final double absoluteAmount)
	{
		final double lowDenom=lowestAbbreviatedDenomination(currency,absoluteAmount);
		final long lowAmt=Math.round(absoluteAmount/lowDenom);
		final String denominationShortCode=getDenominationShortCode(currency,lowDenom);
		if(denominationShortCode.length()==0)
			return ""+lowAmt;
		return lowAmt+denominationShortCode;
	}

	@Override
	public String getDenominationShortCode(final String currency, final double denomination)
	{
		final MoneyDefinition DV=getCurrencySet(currency);
		if(DV==null)
			return "";
		final MoneyDenomination[] V=DV.denominations();
		for (final MoneyDenomination element : V)
		{
			if(element.value()==denomination)
				return element.abbr();
		}
		return "";
	}

	@Override
	public double getLowestDenomination(final String currency)
	{
		final MoneyDefinition DV=getCurrencySet(currency);
		if((DV==null)||(DV.denominations().length==0))
			return 1.0;
		return DV.denominations()[0].value();
	}

	@Override
	public String getDenominationName(final String currency)
	{
		return getDenominationName(currency,getLowestDenomination(currency));
	}

	@Override
	public String getDenominationName(final String currency, final double denomination, final long number)
	{
		final String s=getDenominationName(currency,denomination);
		if(s.endsWith(")"))
		{
			final String us=s.toUpperCase();
			if(us.endsWith("(S)"))
			{
				if(number>1)
					return number+" "+s.substring(0,s.length()-3)+s.charAt(s.length()-2);
				return number+" "+s.substring(0,s.length()-3);
			}
			else
			if(us.endsWith("(YS)"))
			{
				if(number>1)
					return number+" "+s.substring(0,s.length()-4)+CMStrings.sameCase("ies", s.charAt(s.length()-3));
				return number+" "+s.substring(0,s.length()-3);
			}
		}
		return number+" "+s;
	}

	@Override
	public double getBestDenomination(final String currency, final double absoluteValue)
	{
		final MoneyDefinition DV=getCurrencySet(currency);
		double denom=0.0;
		if(DV!=null)
		{
			final MoneyDenomination[] V=DV.denominations();
			final double low=getLowestDenomination(currency);
			for(int d=V.length-1;d>=0;d--)
			{
				denom=V[d].value();
				if((denom<=absoluteValue)
				&&(absoluteValue-(Math.floor(absoluteValue/denom)*denom)<low))
					return denom;
			}
		}
		return denom;
	}

	@Override
	public double getBestDenomination(final String currency, final int numberOfCoins, final double absoluteValue)
	{
		final MoneyDefinition DV=getCurrencySet(currency);
		double bestDenom=0.0;
		if(DV!=null)
		{
			final MoneyDenomination[] V=DV.denominations();
			for(int d=V.length-1;d>=0;d--)
			{
				final double denom=V[d].value();
				if(((denom*(numberOfCoins))<=absoluteValue)
				&&(denom>bestDenom))
					bestDenom=denom;
			}
		}
		return bestDenom;
	}

	@Override
	public double[] getBestDenominations(final String currency, double absoluteValue)
	{
		final MoneyDefinition def=getCurrencySet(currency);
		final Vector<Double> V=new Vector<Double>();
		if(def!=null)
		{
			final MoneyDenomination[] DV=def.denominations();
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
		}
		final double[] ds=new double[V.size()];
		for(int d=0;d<V.size();d++)
			ds[d]=V.elementAt(d).doubleValue();
		return ds;
	}

	@Override
	public String getConvertableDescription(final String currency, final double denomination)
	{
		final double low=getLowestDenomination(currency);
		if(low==denomination)
			return "";
		return "Equal to "+getDenominationName(currency,low,Math.round(Math.floor(denomination/low)))+".";
	}

	@Override
	public String getDenominationName(final MOB mob, final double denomination)
	{
		return getDenominationName(getCurrency(mob), denomination);
	}

	@Override
	public String getDenominationName(final String currency, final double denomination)
	{
		MoneyDefinition def=getCurrencySet(currency);
		if((def==null)||(def.denominations().length==0))
			def=getCurrencySet("");
		if((def==null)||(def.denominations().length==0))
			return "unknown!";
		int closestX=getDenominationIndex(currency, denomination);
		if(closestX<0)
		{
			final MoneyDenomination[] DV=def.denominations();
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
		}
		if(closestX<0)
			return "unknown";
		return def.denominations()[closestX].name();
	}

	@Override
	public String nameCurrencyShort(final MOB mob, final double absoluteValue)
	{
		return nameCurrencyShort(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyShort(final MOB mob, final int absoluteValue)
	{
		return nameCurrencyShort(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyShort(final String currency, final double absoluteValue)
	{
		final double denom=getBestDenomination(currency,absoluteValue);
		if(denom>0.0)
			return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue/denom)));
		return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue)));
	}

	@Override
	public String nameCurrencyLong(final MOB mob, final double absoluteValue)
	{
		return nameCurrencyLong(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyLong(final MOB mob, final int absoluteValue)
	{
		return nameCurrencyLong(getCurrency(mob),absoluteValue);
	}

	@Override
	public String nameCurrencyLong(final String currency, double absoluteValue)
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
	public Coins makeBestCurrency(final MOB mob, final double absoluteValue, final ItemCollection owner, final Container container)
	{
		return makeBestCurrency(getCurrency(mob),absoluteValue,owner,container);
	}

	@Override
	public Coins makeBestCurrency(final String currency, final double absoluteValue, final ItemCollection owner, final Container container)
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

	protected void parseDebt(final List<DebtItem> debt, final String debtor, final String xml)
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
			debt.add(new DebtItem()
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
				public void setAmt(final double amt)
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

	protected String unparseDebt(final List<DebtItem> debt, final String name, final String owedTo)
	{
		final StringBuffer xml=new StringBuffer("<DEBT>");
		for(int d=0;d<debt.size();d++)
		{
			final DebtItem D=debt.get(d);
			if((D.debtor().equalsIgnoreCase(name))
			&&(D.owedTo().equalsIgnoreCase(owedTo)))
			{
				xml.append("<OWE>");
				xml.append(CMLib.xml().convertXMLtoTag("TO",D.owedTo()));
				xml.append(CMLib.xml().convertXMLtoTag("AMT",""+D.amt()));
				xml.append(CMLib.xml().convertXMLtoTag("FOR",D.reason()));
				xml.append(CMLib.xml().convertXMLtoTag("DUE",""+D.due()));
				xml.append(CMLib.xml().convertXMLtoTag("INT",""+D.interest()));
				xml.append("</OWE>");
			}
		}
		xml.append("</DEBT>");
		return xml.toString();
	}

	@Override
	public double getDebtOwed(final String name, final String owedTo)
	{
		final String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			final List<DebtItem> debt=getDebt(name,owedTo);
			double total=0.0;
			for(int d=0;d<debt.size();d++)
				total+=debt.get(d).amt();
			return total;
		}
	}

	@Override
	public void delAllDebt(final String name, final String owedTo)
	{
		final String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			CMLib.database().DBDeletePlayerData(name.toUpperCase(),"DEBT",key);
		}
	}

	@Override
	public List<DebtItem> getDebtOwed(final String owedTo)
	{
		final List<PlayerData> rows=CMLib.database().DBReadPlayerDataByKeyMask("DEBT",".*-DEBT-"+owedTo.toUpperCase().trim());
		final List<DebtItem> debt=new Vector<DebtItem>(rows.size());
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
	public void adjustDebt(final String name, final String owedTo, final double adjustAmt, final String reason, final double interest, final long due)
	{
		final String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			final List<DebtItem> debts=getDebt(name,owedTo);
			final boolean update=debts.size()>0;
			boolean done=false;
			for(int d=0;d<debts.size();d++)
			{
				final DebtItem debt=debts.get(d);
				if((debt.debtor().equalsIgnoreCase(name))
				&&(debt.owedTo().equalsIgnoreCase(owedTo))
				&&(debt.interest()==interest)
				&&(debt.due()==due)
				&&(debt.reason().equalsIgnoreCase(reason)))
				{
					debt.setAmt(debt.amt()+adjustAmt);
					if(debt.amt()<=0.0)
						debts.remove(d);
					done=true;
					break;
				}
			}
			if((!done)&&(adjustAmt>=0.0))
			{
				final double initialAdjustedAmount = adjustAmt;
				debts.add(new DebtItem()
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
					public void setAmt(final double amt)
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
					CMLib.database().DBUpdatePlayerData(name().toUpperCase(), "DEBT", key,xml);
			}
			else
				CMLib.database().DBCreatePlayerData(name.toUpperCase(),"DEBT",key,xml);
		}
	}

	@Override
	public List<DebtItem> getDebt(final String name, final String owedTo)
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
	public List<DebtItem> getDebt(final String name)
	{
		final List<PlayerData> rows=CMLib.database().DBReadPlayerData(name.toUpperCase(),"DEBT");
		final List<DebtItem> debt=new Vector<DebtItem>(rows.size());
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
	public Coins makeBestCurrency(final MOB mob, final double absoluteValue)
	{
		return makeBestCurrency(getCurrency(mob),absoluteValue);
	}

	@Override
	public Coins makeCurrency(final String currency, final double denomination, final long numberOfCoins)
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
	public Coins makeBestCurrency(final String currency, final double absoluteValue)
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
	public List<Coins> makeAllCurrency(final String currency, double absoluteValue)
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
	public void addMoney(final ItemCollection IP, final int deltaValue)
	{
		addMoney(IP,getCurrency(IP),(double)deltaValue);
	}

	@Override
	public void addMoney(final ItemCollection IP, final double deltaValue)
	{
		addMoney(IP,getCurrency(IP),deltaValue);
	}

	@Override
	public void addMoney(final ItemCollection IP, final String currency,final int deltaValue)
	{
		addMoney(IP,currency,(double)deltaValue);
	}

	@Override
	public void addMoney(final ItemCollection IP, final Container container, final String currency,final int deltaValue)
	{
		addMoney(IP,container,currency,(double)deltaValue);
	}

	@Override
	public void addMoney(final ItemCollection IP, final String currency, final double deltaValue)
	{
		addMoney(IP,null,currency,deltaValue);
	}

	@Override
	public void addMoney(final ItemCollection IP, final Container container, final String currency, final double deltaValue)
	{
		if(IP==null)
			return;
		final List<Coins> V=makeAllCurrency(currency,deltaValue);
		for(int i=0;i<V.size();i++)
		{
			final Coins C=V.get(i);
			C.setContainer(container);
			IP.addItem(C);
			putCoinsBack(C,IP);
		}
		if(IP instanceof Physical)
			((Physical)IP).recoverPhyStats();
	}

	@Override
	public void giveSomeoneMoney(final MOB recipient, final double absoluteValue)
	{
		giveSomeoneMoney(recipient,recipient,getCurrency(recipient),absoluteValue);
	}

	@Override
	public void giveSomeoneMoney(final MOB recipient, final String currency, final double absoluteValue)
	{
		giveSomeoneMoney(recipient,recipient,currency,absoluteValue);
	}

	@Override
	public void giveSomeoneMoney(final MOB banker, final MOB customer, final double absoluteValue)
	{
		giveSomeoneMoney(banker,customer,getCurrency(banker),absoluteValue);
	}

	@Override
	public boolean putCoinsBack(final Coins C, final ItemCollection coll)
	{
		Coins alternative=null;
		for(int i=0;i<coll.numItems();i++)
		{
			final Item I=coll.getItem(i);
			if((I!=null)
			&&(I!=C)
			&&(I instanceof Coins)
			&&(!I.amDestroyed())
			&&(((Coins)I).getDenomination()==C.getDenomination())
			&&(CMLib.beanCounter().isCurrencyMatch(((Coins)I).getCurrency(),C.getCurrency()))
			&&(I.container()==C.container()))
			{
				alternative=(Coins)I;
				break;
			}
		}
		if((alternative!=null)&&(alternative!=this))
		{
			alternative.setNumberOfCoins(alternative.getNumberOfCoins()+C.getNumberOfCoins());
			C.destroy();
			return true;
		}
		return false;
	}

	@Override
	public void giveSomeoneMoney(MOB banker, final MOB customer, final String currency, final double absoluteValue)
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
	public void dropMoney(final Room R, final Container container, final String currency, final double absoluteValue)
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
	public void removeMoney(final Room R, final Container container, final String currency, final double absoluteValue)
	{
		double myMoney=getTotalAbsoluteValue(R,container,currency);
		final List<Coins> V=getMoneyItems(R,container,currency);
		for(int v=0;v<V.size();v++)
		{
			final Item I=V.get(v);
			I.destroy();
			R.delItem(I);
		}
		if(myMoney>=absoluteValue)
			myMoney-=absoluteValue;
		else
			myMoney=0.0;
		if(myMoney>0.0)
			dropMoney(R,container,currency,myMoney);
	}

	@Override
	public void addToBankLedger(final String bankName, final String owner, final String explanation)
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
	public Set<String> getBankAccountChains(final String owner)
	{
		final Set<String> chains = new HashSet<String>();
		final Set<String> bankChains = new XHashSet<String>(CMLib.map().bankChains(null));
		final Set<String> sections = CMLib.database().DBReadUniqueSections(owner);
		for(final String sect : sections)
		{
			if(bankChains.contains(sect))
				chains.add(sect);
		}
		return Collections.synchronizedSet(chains);
	}

	@Override
	public Pair<String,Double> getBankBalance(final String bankName, final String owner, final String optionalCurrency)
	{
		if((bankName==null)||(owner==null))
			return null;
		final List<PlayerData> V=CMLib.database().DBReadPlayerData(owner, bankName);
		for(int v=0;v<V.size();v++)
		{
			final DatabaseEngine.PlayerData D=V.get(v);
			final String last=D.xml();
			if(last.startsWith("COINS;"))
			{
				if(bankName.equals(D.section()))
				{
					final Coins C=(Coins)CMClass.getItem("StdCoins");
					CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(C,last.substring(6),true);
					if((C.getDenomination()==0.0)&&(C.getNumberOfCoins()>0))
						C.setDenomination(1.0);
					C.recoverPhyStats();
					final double value=C.getTotalValue();
					if((optionalCurrency==null)
					||(optionalCurrency.equalsIgnoreCase(C.getCurrency())))
						return new Pair<String,Double>(C.getCurrency(), Double.valueOf(value));
				}
			}
		}
		return null;
	}

	@Override
	public boolean modifyBankGold(final String bankName, final String owner, final String explanation, final String currency, final double deltaAmount)
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
					CMLib.coffeeMaker().unpackEnvironmentalMiscTextXML(C,last.substring(6),true);
					if((C.getDenomination()==0.0)&&(C.getNumberOfCoins()>0))
						C.setDenomination(1.0);
					C.recoverPhyStats();
					final double value=C.getTotalValue();
					if((deltaAmount>0.0)||(value>=(-deltaAmount)))
					{
						C=makeBestCurrency(currency,value+deltaAmount);
						if(C!=null)
							CMLib.database().DBReCreatePlayerData(owner,D.section(),D.key(),"COINS;"+CMLib.coffeeMaker().getEnvironmentalMiscTextXML(C,true));
						else
							CMLib.database().DBDeletePlayerData(owner,D.section(),D.key());
						addToBankLedger(bankName,owner,explanation);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean modifyThisAreaBankGold(final Area A,
										  final Set<String> triedBanks,
										  final String owner,
										  final String explanation,
										  final double deltaAmount)
	{
		Banker B=null;
		Room R=null;
		for(final Enumeration<Banker> e=CMLib.map().banks();e.hasMoreElements();)
		{
			B=e.nextElement();
			R=CMLib.map().roomLocation(B);
			if((R!=null)
			&&((A==null)
				||(A.inMyMetroArea(R.getArea())))
			&&(!triedBanks.contains(B.bankChain())))
			{
				triedBanks.add(B.bankChain());
				final String currency=getCurrency(B);
				if(modifyBankGold(B.bankChain(),owner,explanation,currency,deltaAmount))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean modifyLocalBankGold(final Area A,
									   final String owner,
									   final String explanation,
									   final double deltaAmount)
	{
		final HashSet<String> triedBanks=new HashSet<String>();
		if(modifyThisAreaBankGold(A,triedBanks,owner,explanation,deltaAmount))
			return true;
		for(final Enumeration<Area> e=A.getParents();e.hasMoreElements();)
		{
			final Area A2=e.nextElement();
			if(modifyThisAreaBankGold(A2,triedBanks,owner,explanation,deltaAmount))
				return true;
		}
		final String currency=getCurrency(A);
		return modifyBankGold(null,owner,explanation,currency,deltaAmount);
	}

	@Override
	public void subtractMoneyGiveChange(final MOB banker, final MOB mob, final int positiveDeltaAmount)
	{
		subtractMoneyGiveChange(banker,mob,(double)positiveDeltaAmount);
	}

	@Override
	public void subtractMoneyGiveChange(final MOB banker, final MOB mob, final double positiveDeltaAmount)
	{
		subtractMoneyGiveChange(banker, mob,(banker!=null)?getCurrency(banker):getCurrency(mob),positiveDeltaAmount);
	}

	@Override
	public void subtractMoneyGiveChange(final MOB banker, final MOB mob, final String currency, final double positiveDeltaAmount)
	{
		if(mob==null)
			return;
		double myMoney=getTotalAbsoluteValue(mob,currency);
		final List<Coins> V=getMoneyItems(mob,currency);
		for(int v=0;v<V.size();v++)
		{
			final Item I=V.get(v);
			I.destroy();
			mob.delItem(I);
		}
		if(myMoney>=positiveDeltaAmount)
			myMoney-=positiveDeltaAmount;
		else
			myMoney=0.0;
		if(myMoney>0.0)
			giveSomeoneMoney(banker,mob,currency,myMoney);
	}

	@Override
	public void setMoney(final MOB mob, final double absoluteAmount)
	{
		clearZeroMoney(mob,null);
		addMoney(mob,getCurrency(mob),absoluteAmount);
	}

	@Override
	public void setMoney(final MOB mob, final String currency, final double absoluteAmount)
	{
		clearZeroMoney(mob,currency);
		addMoney(mob,currency,absoluteAmount);
	}

	@Override
	public void subtractMoney(final ItemCollection IP, final double positiveDeltaAmount)
	{
		subtractMoney(IP,getCurrency(IP),positiveDeltaAmount);
	}

	@Override
	public void subtractMoney(final ItemCollection IP, final String currency, final double positiveDeltaAmount)
	{
		subtractMoney(IP,null,currency,positiveDeltaAmount);
	}

	@Override
	public void subtractMoney(final ItemCollection IP, final Container container, final String currency, final double positiveDeltaAmount)
	{
		if(IP==null)
			return;
		double myMoney=getTotalAbsoluteValue(IP,container,currency);
		final List<Coins> V=getMoneyItems(IP,container,currency);
		for(int v=0;v<V.size();v++)
		{
			final Item delItem=V.get(v);
			delItem.destroy();
			IP.delItem(delItem);
		}
		if(myMoney>=positiveDeltaAmount)
			myMoney-=positiveDeltaAmount;
		else
			myMoney=0.0;
		if(myMoney>0.0)
			addMoney(IP,container,currency,myMoney);
	}

	@Override
	public int getMoney(final MOB mob)
	{
		if(mob==null)
			return 0;
		long money=mob.getMoney();
		if(money>0)
			return mob.getMoney();
		final List<Coins> V=getMoneyItems(mob,null);
		for(int i=0;i<V.size();i++)
			money+=Math.round(V.get(i).getTotalValue());
		if(money>Integer.MAX_VALUE/2)
			return Integer.MAX_VALUE/2;
		return (int)money;
	}

	@Override
	public void setMoney(final MOB mob, final int amount)
	{
		if(mob==null)
			return;
		clearZeroMoney(mob,null);
		mob.setMoney(amount);
	}

	@Override
	public void clearZeroMoney(final MOB mob, final String currency)
	{
		if(mob==null)
			return;
		mob.setMoney(0);
		clearInventoryMoney(mob,currency);
	}

	@Override
	public void clearInventoryMoney(final MOB mob, final String currency)
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
				if(isCurrencyMatch(((Coins)I).getCurrency(),currency))
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
	public void subtractMoney(final ItemCollection IP, final double denomination, final double deltaAmount)
	{
		subtractMoney(IP,getCurrency(IP),denomination,deltaAmount);
	}

	@Override
	public void subtractMoney(final ItemCollection IP, final String currency, final double denomination, double deltaAmount)
	{
		if(IP==null)
			return;
		final List<Coins> V=getMoneyItems(IP,currency);
		Coins C=null;
		for(int v=0;v<V.size();v++)
		{
			C=V.get(v);
			if(C.getDenomination()==denomination)
			{
				if(C.getTotalValue()>deltaAmount)
				{
					C.setNumberOfCoins(Math.round(Math.floor((C.getTotalValue()-deltaAmount)/denomination)));
					C.text();
					break;
				}
				deltaAmount-=C.getTotalValue();
				C.destroy();
			}
		}
	}

	@Override
	public List<Coins> getMoneyItems(final ItemCollection IP, final String currency)
	{
		return getMoneyItems(IP, null, currency);
	}

	@Override
	public boolean isCurrencyMatch(final String curr1, final String curr2)
	{
		if(curr1 == null)
			return curr2==null;
		else
		if(curr2 == null)
			return false;
		if(curr1.length()==curr2.length())
			return curr1.equalsIgnoreCase(curr2);
		final int x1=curr1.indexOf('=');
		final int x2=curr2.indexOf('=');
		if((x1<0)&&(x2<0))
			return curr1.equalsIgnoreCase(curr2);
		final String curr1n = (x1 < 0)?curr1:curr1.substring(0, x1);
		final String curr2n = (x2 < 0)?curr2:curr2.substring(0, x2);
		return curr1n.equalsIgnoreCase(curr2n);
	}

	@Override
	public List<Coins> getMoneyItems(final ItemCollection IP, final Item container, final String currency)
	{
		final Vector<Coins> V=new Vector<Coins>();
		if(IP==null)
			return V;
		if((IP instanceof MOB)
		&&((currency==null)||(isCurrencyMatch(currency,getCurrency(IP))))
		&&(((MOB)IP).getMoney()>0)
		&&(container==null))
		{
			addMoney((MOB)IP,getCurrency(IP),(double)((MOB)IP).getMoney());
			((MOB)IP).setMoney(0);
		}
		for(int i=0;i<IP.numItems();i++)
		{
			final Item I=IP.getItem(i);
			if((I!=null)
			&&(I instanceof Coins)
			&&((currency==null)||isCurrencyMatch(((Coins)I).getCurrency(),currency))
			&&(I.container()==container))
				V.addElement((Coins)I);
		}
		return V;
	}

	@Override
	public long getNumberOfCoins(final MOB mob, final String currency, final double denomination)
	{
		final List<Coins> V=getMoneyItems(mob,currency);
		long gold=0;
		for(int v=0;v<V.size();v++)
		{
			if(V.get(v).getDenomination()==denomination)
				gold+=V.get(v).getNumberOfCoins();
		}
		return gold;
	}

	@Override
	public String getCurrency(CMObject E)
	{
		if(E instanceof ShopKeeper)
		{
			final String s=((ShopKeeper)E).getRawCurrency();
			if(s.length()>0)
			{
				final int x=s.indexOf('=');
				if(x<0)
					return s.toUpperCase().trim();
				return s.substring(0,x).toUpperCase().trim();
			}
		}
		if(E instanceof MOB)
		{
			if(((MOB)E).getStartRoom()!=null)
				E=(((MOB)E).getStartRoom());
			else
			if(((MOB)E).location()!=null)
				E=(((MOB)E).location());
		}
		if(E instanceof Room)
			E=((Room)E).getArea();
		if(E instanceof Area)
		{
			final String s=((Area)E).getFinalCurrency();
			final int x=s.indexOf('=');
			if(x<0)
				return s.toUpperCase().trim();
			return s.substring(0,x).toUpperCase().trim();
		}
		if(E instanceof Coins)
			return ((Coins)E).getCurrency();
		return "";
	}

	@Override
	public String getBankChainCurrency(final String bankChain)
	{
		final Map<String,int[]> currencies = new HashMap<String,int[]>();
		for(final Enumeration<Banker> b=CMLib.map().banks();b.hasMoreElements();)
		{
			final Banker B=b.nextElement();
			if((bankChain == null)||(B.bankChain().equalsIgnoreCase(bankChain)))
			{
				final String curr = getCurrency(B);
				if(!currencies.containsKey(curr))
					currencies.put(curr, new int[] {0});
				currencies.get(curr)[0]++;
			}
		}
		String winningCur=null;
		final int mostAmt = 0;
		for(final String curr : currencies.keySet())
		{
			final int amt = currencies.get(curr)[0];
			if(amt > mostAmt)
				winningCur=curr;
		}
		return (winningCur==null)?"":winningCur;
	}

	@Override
	public double getTotalAbsoluteValue(final ItemCollection IP, final String currency)
	{
		return getTotalAbsoluteValue(IP, null, currency);
	}

	@Override
	public double getTotalAbsoluteValue(final ItemCollection IP, final Item container, final String currency)
	{
		double money=0.0;
		final List<Coins> V=getMoneyItems(IP,container,currency);
		for(int v=0;v<V.size();v++)
			money+=V.get(v).getTotalValue();
		return money;
	}

	@Override
	public double getTotalAbsoluteNativeValue(final MOB mob)
	{
		double money=0.0;
		final List<Coins> V=getMoneyItems(mob,getCurrency(mob));
		for(int v=0;v<V.size();v++)
			money+=V.get(v).getTotalValue();
		return money;
	}

	@Override
	public double getTotalAbsoluteShopKeepersValue(final MOB mob, final MOB shopkeeper)
	{
		double money=0.0;
		final List<Coins> V=getMoneyItems(mob,getCurrency(shopkeeper));
		for(int v=0;v<V.size();v++)
			money+=V.get(v).getTotalValue();
		return money;
	}

	@Override
	public double getTotalAbsoluteValueAllCurrencies(final ItemCollection IP)
	{
		return getTotalAbsoluteValue(IP,null);
	}

}
