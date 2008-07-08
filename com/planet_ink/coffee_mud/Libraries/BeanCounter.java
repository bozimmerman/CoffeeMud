package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
   Copyright 2000-2008 Bo Zimmerman

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
    public String ID(){return "BeanCounter";}
    public Hashtable currencies=new Hashtable();
    public static Hashtable defaultCurrencies=new Hashtable();
    public Vector allCurrencyNames=new Vector();
    public Hashtable allCurrencyDenominationNames=new Hashtable();

	public void unloadCurrencySet(String currency)
	{
	    String code=currency.toUpperCase().trim();
	    int x=code.indexOf("=");
	    if(x>=0) code=code.substring(0,x).trim();
	    if((code.length()>0)&&(currencies.containsKey(code)))
	    {
	        allCurrencyNames.removeElement(code);
            currencies.remove(code);
            allCurrencyDenominationNames.remove(code);
	    }
	}

    public DVector createCurrencySet(String currency){ return createCurrencySet(currencies,currency);}
	protected DVector createCurrencySet(Hashtable currencies, String currency)
	{
	    int x=currency.indexOf("=");
	    if(x<0) return null;
	    String code=currency.substring(0,x).trim().toUpperCase();
	    if(currencies.containsKey(code))
	        return (DVector)currencies.get(code);
        currency=currency.substring(x+1).trim();
        Vector V=CMParms.parseSemicolons(currency,true);
        DVector DV=new DVector(3);
        String s=null;
        String num=null;
        double d=0.0;
        Vector currencyNames=new Vector();
        for(int v=0;v<V.size();v++)
        {
            s=(String)V.elementAt(v);
            x=s.indexOf(" ");
            if(x<0) continue;
            num=s.substring(0,x).trim();
            if(CMath.isDouble(num))
                d=CMath.s_double(num);
            else
            if(CMath.isInteger(num))
                d=(double)CMath.s_int(num);
            else
                continue;
            s=s.substring(x+1).trim();
            String shortName="";
            if(s.endsWith(")"))
            {
                x=s.lastIndexOf(" ");
                if((x>0)&&(x<s.length()-1)&&(s.charAt(x+1)=='('))
                {
                    shortName=s.substring(x+2,s.length()-1).trim();
                    s=s.substring(0,x).trim();
                }
            }
            if((s.length()>0)&&(d>0.0))
            {
                int insertAt=-1;
                for(int i=0;i<DV.size();i++)
                    if(((Double)DV.elementAt(i,1)).doubleValue()>d)
                    { insertAt=i; break;}
                if((insertAt<0)||(insertAt>=DV.size()))
	                DV.addElement(new Double(d),s,shortName);
                else
                    DV.insertElementAt(insertAt,new Double(d),s,shortName);
                currencyNames.addElement(s);
                if(shortName.length()>0)
                    currencyNames.add(shortName);
            }
        }
        currencies.put(code,DV);
        allCurrencyNames.addElement(code);
        allCurrencyDenominationNames.put(code,currencyNames);
        return DV;
	}

	public DVector getCurrencySet(String currency)
	{
	    if(currency==null) return null;
	    String code=currency.toUpperCase().trim();
	    int x=code.indexOf("=");
	    if(x<0)
	    {
	        if(currencies.containsKey(code))
	            return (DVector)currencies.get(code);
            if(defaultCurrencies.size()==0)
            {
                createCurrencySet(defaultCurrencies,defaultCurrencyDefinition);
                createCurrencySet(defaultCurrencies,goldStandard);
                createCurrencySet(defaultCurrencies,copperStandard);
            }
            if(defaultCurrencies.containsKey(code))
                return (DVector)defaultCurrencies.get(code);
	        return null;
	    }
        code=code.substring(0,x).trim();
        if(currencies.containsKey(code))
            return (DVector)currencies.get(code);
        return createCurrencySet(currency);
	}

	public Vector getAllCurrencies()
	{ return allCurrencyNames;}

	public Vector getDenominationNameSet(String currency)
	{
	    if(allCurrencyDenominationNames.containsKey(currency))
	        return (Vector)allCurrencyDenominationNames.get(currency);
        return new Vector();
	}

	public double lowestAbbreviatedDenomination(String currency)
	{
	    DVector DV=getCurrencySet(currency);
	    if(DV!=null)
	    {
	        for(int i=0;i<DV.size();i++)
	            if(((String)DV.elementAt(i,3)).length()>0)
	                return ((Double)DV.elementAt(i,1)).doubleValue();
	        return getLowestDenomination(currency);
	    }
	    return 1.0;
	}

	public double lowestAbbreviatedDenomination(String currency, double absoluteAmount)
	{
	    DVector DV=getCurrencySet(currency);
	    if(DV!=null)
	    {
	        double absoluteLowest=lowestAbbreviatedDenomination(currency);
	        double lowestDenom=Double.MAX_VALUE;
	        double diff=0.0;
	        double denom=0.0;
	        long num=0;
	        for(int i=DV.size()-1;i>=0;i--)
	            if(((String)DV.elementAt(i,3)).length()>0)
	            {
	                denom=((Double)DV.elementAt(i,1)).doubleValue();
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
	        if(lowestDenom==Double.MAX_VALUE) lowestDenom=absoluteLowest;
	        return lowestDenom;
	    }
        return 1.0;
	}

	public double abbreviatedRePrice(MOB shopkeeper, double absoluteAmount)
	{  return abbreviatedRePrice(getCurrency(shopkeeper),absoluteAmount);}
	public double abbreviatedRePrice(String currency, double absoluteAmount)
	{
	    double lowDenom=lowestAbbreviatedDenomination(currency,absoluteAmount);
	    long lowAmt=Math.round(absoluteAmount/lowDenom);
	    return CMath.mul(lowDenom,lowAmt);
	}
	public String abbreviatedPrice(MOB shopkeeper, double absoluteAmount)
	{ return abbreviatedPrice(getCurrency(shopkeeper),absoluteAmount);}
	public String abbreviatedPrice(String currency, double absoluteAmount)
	{
	    double lowDenom=lowestAbbreviatedDenomination(currency,absoluteAmount);
	    long lowAmt=Math.round(absoluteAmount/lowDenom);
	    String denominationShortCode=getDenominationShortCode(currency,lowDenom);
	    if(denominationShortCode.length()==0)
		    return ""+lowAmt;
	    return lowAmt+denominationShortCode;
	}

	public String getDenominationShortCode(String currency, double denomination)
	{
	    DVector DV=getCurrencySet(currency);
	    if(DV==null) return "";
	    for(int d=0;d<DV.size();d++)
	        if(((Double)DV.elementAt(d,1)).doubleValue()==denomination)
	            return (String)DV.elementAt(d,3);
	    return "";
	}

	public double getLowestDenomination(String currency)
	{
	    DVector DV=getCurrencySet(currency);
	    if(DV==null) return 1.0;
	    return ((Double)DV.elementAt(0,1)).doubleValue();
	}

	public String getDenominationName(String currency)
	{ return getDenominationName(currency,getLowestDenomination(currency));}

	public String getDenominationName(String currency,
	        								 double denomination,
	        								 long number)
	{
	    String s=getDenominationName(currency,denomination);
	    if(s.toUpperCase().endsWith("(S)"))
	    {
	        if(number>1)
	            return number+" "+s.substring(0,s.length()-3)+s.charAt(s.length()-2);
            return number+" "+s.substring(0,s.length()-3);
	    }
        return number+" "+s;
	}

	public double getBestDenomination(String currency, double absoluteValue)
	{
		DVector DV=getCurrencySet(currency);
		double denom=0.0;
		if(DV!=null)
		{
			double low=getLowestDenomination(currency);
			for(int d=DV.size()-1;d>=0;d--)
			{
			    denom=((Double)DV.elementAt(d,1)).doubleValue();
			    if((denom<=absoluteValue)
			    &&(absoluteValue-(Math.floor(absoluteValue/denom)*denom)<low))
			        return denom;
			}
		}
		return denom;
	}
	public Vector getBestDenominations(String currency, double absoluteValue)
	{
		DVector DV=getCurrencySet(currency);
		Vector V=new Vector();
		if(DV!=null)
		for(int d=DV.size()-1;d>=0;d--)
		{
		    double denom=((Double)DV.elementAt(d,1)).doubleValue();
		    if(denom<=absoluteValue)
		    {
		        long number=Math.round(Math.floor(absoluteValue/denom));
		        if(number>0)
		        {
		            V.addElement(new Double(denom));
		            absoluteValue-=CMath.mul(denom,number);
		        }
		    }
		}
		return V;
	}
	public String getConvertableDescription(String currency, double denomination)
	{
	    double low=getLowestDenomination(currency);
	    if(low==denomination) return "";
	    return "Equal to "+getDenominationName(currency,low,Math.round(Math.floor(denomination/low)))+".";
	}

	public String getDenominationName(String currency, double denomination)
	{
	    DVector DV=getCurrencySet(currency);
	    if((DV==null)||(DV.size()==0)) DV=getCurrencySet("");
	    if((DV==null)||(DV.size()==0)) return "unknown!";
	    int closestX=DV.indexOf(new Double(denomination));
	    if(closestX<0)
		    for(int i=0;i<DV.size();i++)
		        if((((Double)DV.elementAt(i,1)).doubleValue()<=denomination)
		        &&((closestX<0)||(((Double)DV.elementAt(i,1)).doubleValue()>=((Double)DV.elementAt(closestX,1)).doubleValue())))
		            closestX=i;
	    if(closestX<0)
	        return "unknown";
        return (String)DV.elementAt(closestX,2);
	}

	public String nameCurrencyShort(MOB mob, double absoluteValue)
	{   return nameCurrencyShort(getCurrency(mob),absoluteValue);}
	public String nameCurrencyShort(MOB mob, int absoluteValue)
	{   return nameCurrencyShort(getCurrency(mob),(double)absoluteValue);}
	public String nameCurrencyShort(String currency, double absoluteValue)
	{
		double denom=getBestDenomination(currency,absoluteValue);
		if(denom>0.0)
		    return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue/denom)));
	    return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue)));
	}
	public String nameCurrencyLong(MOB mob, double absoluteValue)
	{   return nameCurrencyLong(getCurrency(mob),absoluteValue);}
	public String nameCurrencyLong(MOB mob, int absoluteValue)
	{   return nameCurrencyLong(getCurrency(mob),(double)absoluteValue);}
	public String nameCurrencyLong(String currency, double absoluteValue)
	{
	    StringBuffer str=new StringBuffer("");
		Vector V=getBestDenominations(currency,absoluteValue);
		for(int d=0;d<V.size();d++)
		{
		    double denom=((Double)V.elementAt(d)).doubleValue();
	        long number=Math.round(Math.floor(absoluteValue/denom));
	        String name=getDenominationName(currency,denom,number);
            absoluteValue-=CMath.mul(denom,number);
            if(str.length()>0) str.append(", ");
            str.append(name);
		}
		return str.toString();
	}

	public Coins makeBestCurrency(MOB mob,
										 double absoluteValue,
										 Environmental owner,
										 Item container)
	{ return makeBestCurrency(getCurrency(mob),absoluteValue,owner,container);}
	public Coins makeBestCurrency(String currency,
	        							double absoluteValue,
	        							Environmental owner,
	        							Item container)
	{
	    Coins C=makeBestCurrency(currency,absoluteValue);
	    if(C!=null)
	    {
		    if(owner instanceof Room)
		        ((Room)owner).addItem(C);
		    if(owner instanceof MOB)
		        ((MOB)owner).addInventory(C);
		    C.setContainer(container);
		    C.recoverEnvStats();
	    }
	    return C;
	}

	protected void parseDebt(DVector debt, String debtor, String xml)
	{
		Vector V=CMLib.xml().parseAllXML(xml);
		if(xml==null){ Log.errOut("BeanCounter","Unable to parse: "+xml); return ;}
		Vector debtData=CMLib.xml().getRealContentsFromPieces(V,"DEBT");
		if(debtData==null){ Log.errOut("BeanCounter","Unable to get debt data"); return ;}
		for(int p=0;p<debtData.size();p++)
		{
			XMLLibrary.XMLpiece ablk=(XMLLibrary.XMLpiece)debtData.elementAt(p);
			if((!ablk.tag.equalsIgnoreCase("OWE"))||(ablk.contents==null)||(ablk.contents.size()==0)) continue;
			String owed=CMLib.xml().getValFromPieces(ablk.contents,"TO");
			Double amt=new Double(CMLib.xml().getDoubleFromPieces(ablk.contents,"AMT"));
			String reason=CMLib.xml().getValFromPieces(ablk.contents,"FOR");
			Long due=new Long(CMLib.xml().getLongFromPieces(ablk.contents,"DUE"));
			Double interest=new Double(CMLib.xml().getDoubleFromPieces(ablk.contents,"INT"));
			debt.addElement(debtor,owed,amt,reason,due,interest);
		}
	}
	protected String unparseDebt(DVector debt, String name, String owedTo)
	{
		StringBuffer xml=new StringBuffer("<DEBT>");
		for(int d=0;d<debt.size();d++)
		{
			if((((String)debt.elementAt(d,MoneyLibrary.DEBT_DEBTOR)).equalsIgnoreCase(name))
			&&(((String)debt.elementAt(d,MoneyLibrary.DEBT_OWEDTO)).equalsIgnoreCase(owedTo)))
			{
				xml.append("<OWE>");
				xml.append(CMLib.xml().convertXMLtoTag("TO",((String)debt.elementAt(d,MoneyLibrary.DEBT_OWEDTO))));
				xml.append(CMLib.xml().convertXMLtoTag("AMT",""+((Double)debt.elementAt(d,MoneyLibrary.DEBT_AMTDBL)).doubleValue()));
				xml.append(CMLib.xml().convertXMLtoTag("FOR",((String)debt.elementAt(d,MoneyLibrary.DEBT_REASON))));
				xml.append(CMLib.xml().convertXMLtoTag("DUE",((Long)debt.elementAt(d,MoneyLibrary.DEBT_DUELONG)).longValue()));
				xml.append(CMLib.xml().convertXMLtoTag("INT",""+((Double)debt.elementAt(d,MoneyLibrary.DEBT_INTDBL)).doubleValue()));
				xml.append("</OWE>");
			}
		}
		xml.append("</DEBT>");
		return xml.toString();
	}

	public double getDebtOwed(String name, String owedTo)
	{
		String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			DVector debt=getDebt(name,owedTo);
			double total=0.0;
			for(int d=0;d<debt.size();d++)
				total+=(((Double)debt.elementAt(d,MoneyLibrary.DEBT_AMTDBL)).doubleValue());
			return total;
		}
	}

	public void delAllDebt(String name, String owedTo)
	{
		String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			CMLib.database().DBDeleteData(name.toUpperCase(),"DEBT",key);
		}
	}

	public DVector getDebtOwed(String owedTo)
	{
		Vector rows=CMLib.database().DBReadDataKey("DEBT",".*-DEBT-"+owedTo.toUpperCase().trim());
		DVector debt=new DVector(6);
		for(int r=0;r<rows.size();r++)
		{
			Vector row=(Vector)rows.elementAt(r);
			String debtor=(String)row.elementAt(DatabaseEngine.PDAT_WHO);
			String xml=(String)row.elementAt(DatabaseEngine.PDAT_XML);
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	public void adjustDebt(String name, String owedTo, double adjustAmt, String reason, double interest, long due)
	{
		String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			DVector debt=getDebt(name,owedTo);
			boolean update=debt.size()>0;
			boolean done=false;
			for(int d=0;d<debt.size();d++)
				if((((String)debt.elementAt(d,MoneyLibrary.DEBT_DEBTOR)).equalsIgnoreCase(name))
				&&(((String)debt.elementAt(d,MoneyLibrary.DEBT_OWEDTO)).equalsIgnoreCase(owedTo))
				&&(((Double)debt.elementAt(d,MoneyLibrary.DEBT_INTDBL)).doubleValue()==interest)
				&&(((Long)debt.elementAt(d,MoneyLibrary.DEBT_DUELONG)).longValue()==due)
				&&(((String)debt.elementAt(d,MoneyLibrary.DEBT_REASON)).equalsIgnoreCase(reason)))
				{
					double oldAmt=((Double)debt.elementAt(d,MoneyLibrary.DEBT_AMTDBL)).doubleValue();
					oldAmt+=adjustAmt;
					debt.setElementAt(d,MoneyLibrary.DEBT_AMTDBL,new Double(oldAmt));
					if(oldAmt<=0.0) debt.removeElementsAt(d);
					done=true;
					break;
				}
			if((!done)&&(adjustAmt>=0.0))
				debt.addElement(name,owedTo,new Double(adjustAmt),reason,new Long(due),new Double(interest));
			String xml=unparseDebt(debt,name,owedTo);
			if(update)
			{
				if(debt.size()==0)
					CMLib.database().DBDeleteData(name.toUpperCase(),"DEBT",key);
				else
					CMLib.database().DBUpdateData(key,xml);
			}
			else
				CMLib.database().DBCreateData(name.toUpperCase(),"DEBT",key,xml);
		}
	}

	public DVector getDebt(String name, String owedTo)
	{
		Vector rows=CMLib.database().DBReadData(name.toUpperCase(),"DEBT",name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim());
		DVector debt=new DVector(6);
		for(int r=0;r<rows.size();r++)
		{
			Vector row=(Vector)rows.elementAt(r);
			String debtor=(String)row.elementAt(DatabaseEngine.PDAT_WHO);
			String xml=(String)row.elementAt(DatabaseEngine.PDAT_XML);
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	public DVector getDebt(String name)
	{
		Vector rows=CMLib.database().DBReadData(name.toUpperCase(),"DEBT");
		DVector debt=new DVector(4);
		for(int r=0;r<rows.size();r++)
		{
			Vector row=(Vector)rows.elementAt(r);
			String debtor=(String)row.elementAt(DatabaseEngine.PDAT_WHO);
			String xml=(String)row.elementAt(DatabaseEngine.PDAT_XML);
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}


	public Coins makeBestCurrency(MOB mob, double absoluteValue)
	{ return makeBestCurrency(getCurrency(mob),absoluteValue);}
	public Coins makeCurrency(String currency, double denomination, long numberOfCoins)
	{
	    if(numberOfCoins>0)
	    {
		    Coins C=(Coins)CMClass.getItem("StdCoins");
		    C.setCurrency(currency);
		    C.setDenomination(denomination);
		    C.setNumberOfCoins(numberOfCoins);
		    C.recoverEnvStats();
		    return C;
	    }
	    return null;
	}
	public Coins makeBestCurrency(String currency, double absoluteValue)
	{
	    double denom=getBestDenomination(currency,absoluteValue);
	    if(denom==0.0) return null;
	    long number=Math.round(Math.floor(absoluteValue/denom));
	    if(number>0)
		    return makeCurrency(currency,denom,number);
	    return null;
	}

	public Vector makeAllCurrency(String currency, double absoluteValue)
	{
	    Vector V=new Vector();
	    Vector DV=getBestDenominations(currency,absoluteValue);
		for(int d=0;d<DV.size();d++)
		{
		    double denom=((Double)DV.elementAt(d)).doubleValue();
		    long number=Math.round(Math.floor(absoluteValue/denom));
		    if(number>0)
		    {
			    Coins C=makeCurrency(currency,denom,number);
			    if(C!=null)
			    {
				    absoluteValue-=C.getTotalValue();
		            V.addElement(C);
			    }
		    }
		}
		return V;
	}

	public void addMoney(MOB customer, int absoluteValue)
	{  addMoney(customer,getCurrency(customer),(double)absoluteValue);}
	public void addMoney(MOB customer, double absoluteValue)
	{  addMoney(customer,getCurrency(customer),absoluteValue);}
	public void addMoney(MOB customer, String currency,int absoluteValue)
	{  addMoney(customer,currency,(double)absoluteValue);}
	public void addMoney(MOB mob, String currency, double absoluteValue)
	{
	    if(mob==null) return;
		Vector V=makeAllCurrency(currency,absoluteValue);
	    for(int i=0;i<V.size();i++)
	    {
	        Coins C=(Coins)V.elementAt(i);
	        mob.addInventory(C);
	        C.putCoinsBack();
		}
		mob.recoverEnvStats();
	}

    public void giveSomeoneMoney(MOB recipient, double absoluteValue)
    {  giveSomeoneMoney(recipient,recipient,getCurrency(recipient),absoluteValue); }
    public void giveSomeoneMoney(MOB recipient, String currency, double absoluteValue)
    {  giveSomeoneMoney(recipient,recipient,currency,absoluteValue); }
	public void giveSomeoneMoney(MOB banker, MOB customer, double absoluteValue)
	{  giveSomeoneMoney(banker,customer,getCurrency(banker),absoluteValue); }
	public void giveSomeoneMoney(MOB banker, MOB customer, String currency, double absoluteValue)
	{
		if(banker==null) banker=customer;
		if(banker==customer)
		{
		    addMoney(customer,currency,absoluteValue);
		    return;
		}

		Vector V=makeAllCurrency(currency,absoluteValue);
	    for(int i=0;i<V.size();i++)
	    {
	        Coins C=(Coins)V.elementAt(i);
	        banker.addInventory(C);
			CMMsg newMsg=CMClass.getMsg(banker,customer,C,CMMsg.MSG_GIVE,"<S-NAME> give(s) "+C.Name()+" to <T-NAMESELF>.");
			if(banker.location().okMessage(banker,newMsg))
			{
				banker.location().send(banker,newMsg);
				C.putCoinsBack();
		    }
			else
				CMLib.commands().postDrop(banker,C,true,false);
	    }
		banker.recoverEnvStats();
		customer.recoverEnvStats();
	}

	public void bankLedger(String bankName, String owner, String explanation)
	{
		Vector V=CMLib.database().DBReadData(owner,"LEDGER-"+bankName,"LEDGER-"+bankName+"/"+owner);
		if((V!=null)&&(V.size()>0))
		{
			Vector D=(Vector)V.firstElement();
			String last=(String)D.elementAt(3);
			if(last.length()>4096)
			{
			    int x=last.indexOf(";|;",1024);
			    if(x>=0) last=last.substring(x+3);
			}
			CMLib.database().DBReCreateData(owner,(String)D.elementAt(1),(String)D.elementAt(2),last+explanation+";|;");
		}
		else
			CMLib.database().DBCreateData(owner,"LEDGER-"+bankName,"LEDGER-"+bankName+"/"+owner,explanation+";|;");
	}

	public boolean modifyBankGold(String bankName,
    							  String owner,
    							  String explanation,
    							  String currency,
    							  double absoluteAmount)
	{
		Vector V=CMLib.database().DBReadAllPlayerData(owner);
		for(int v=0;v<V.size();v++)
		{
			Vector D=(Vector)V.elementAt(v);
			String last=(String)D.elementAt(3);
			if(last.startsWith("COINS;"))
			{
				if((bankName==null)||(bankName.length()==0)||(bankName.equals(D.elementAt(1))))
				{
					Coins C=(Coins)CMClass.getItem("StdCoins");
					CMLib.coffeeMaker().setPropertiesStr(C,last.substring(6),true);
					if((C.getDenomination()==0.0)&&(C.getNumberOfCoins()>0))
					    C.setDenomination(1.0);
					C.recoverEnvStats();
					double value=C.getTotalValue();
					if((absoluteAmount>0.0)||(value>=(-absoluteAmount)))
					{
					    C=makeBestCurrency(currency,value+absoluteAmount);
						if(C!=null)
							CMLib.database().DBReCreateData(owner,(String)D.elementAt(1),(String)D.elementAt(2),"COINS;"+CMLib.coffeeMaker().getPropertiesStr(C,true));
						else
							CMLib.database().DBDeleteData(owner,(String)D.elementAt(1),(String)D.elementAt(2));
						bankLedger(bankName,owner,explanation);
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean modifyThisAreaBankGold(Area A,
    									  HashSet triedBanks,
    									  String owner,
    									  String explanation,
    									  String currency,
    									  double absoluteAmount)
	{
		Banker B=null;
		Room R=null;
		for(Enumeration e=CMLib.map().banks();e.hasMoreElements();)
		{
			B=(Banker)e.nextElement();
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

	public boolean modifyLocalBankGold(Area A,
    								   String owner,
    								   String explanation,
    								   String currency,
    								   double absoluteAmount)
	{
		HashSet triedBanks=new HashSet();
		if(modifyThisAreaBankGold(A,triedBanks,owner,explanation,currency,absoluteAmount))
			return true;
		for(Enumeration e=A.getParents();e.hasMoreElements();)
		{
			Area A2=(Area)e.nextElement();
			if(modifyThisAreaBankGold(A2,triedBanks,owner,explanation,currency,absoluteAmount))
				return true;
		}
		return modifyBankGold(null,owner,explanation,currency,absoluteAmount);
	}

	public void subtractMoneyGiveChange(MOB banker, MOB mob, int absoluteAmount)
	{ subtractMoneyGiveChange(banker,mob,(double)absoluteAmount);}
	public void subtractMoneyGiveChange(MOB banker, MOB mob, double absoluteAmount)
	{ subtractMoneyGiveChange(banker, mob,(banker!=null)?getCurrency(banker):getCurrency(mob),absoluteAmount);}
	public void subtractMoneyGiveChange(MOB banker, MOB mob, String currency, double absoluteAmount)
	{
		if(mob==null) return;
		double myMoney=getTotalAbsoluteValue(mob,currency);
		Vector V=getStandardCurrency(mob,currency);
		for(int v=0;v<V.size();v++)
		    ((Item)V.elementAt(v)).destroy();
		if(myMoney>=absoluteAmount)
		    myMoney-=absoluteAmount;
		else
		    myMoney=0.0;
		if(myMoney>0.0)
			giveSomeoneMoney(banker,mob,currency,myMoney);
	}

	public void setMoney(MOB mob, double absoluteAmount)
	{
	    clearZeroMoney(mob,null);
	    addMoney(mob,getCurrency(mob),absoluteAmount);
	}
	public void setMoney(MOB mob, String currency, double absoluteAmount)
	{
	    clearZeroMoney(mob,currency);
	    addMoney(mob,currency,absoluteAmount);
	}

	public void subtractMoney(MOB mob, double absoluteAmount)
	{ subtractMoney(mob,getCurrency(mob),absoluteAmount);}
	public void subtractMoney(MOB mob, String currency, double absoluteAmount)
	{
		if(mob==null) return;
		double myMoney=getTotalAbsoluteValue(mob,currency);
		Vector V=getStandardCurrency(mob,currency);
		for(int v=0;v<V.size();v++)
		    ((Item)V.elementAt(v)).destroy();
		if(myMoney>=absoluteAmount)
		    myMoney-=absoluteAmount;
		else
		    myMoney=0.0;
		if(myMoney>0.0)
			addMoney(mob,currency,myMoney);
	}

	public int getMoney(MOB mob)
	{
	    if(mob==null) return 0;
	    long money=mob.getMoney();
	    if(money>0) return mob.getMoney();
	    Vector V=getStandardCurrency(mob,null);
	    for(int i=0;i<V.size();i++)
	        money+=Math.round(((Coins)V.elementAt(i)).getTotalValue());
	    if(money>Integer.MAX_VALUE) return Integer.MAX_VALUE;
	    return (int)money;
	}

	public void setMoney(MOB mob, int amount)
	{
	    if(mob==null) return;
	    clearZeroMoney(mob,null);
	    mob.setMoney(amount);
	}

	public void clearZeroMoney(MOB mob, String currency)
	{
	    if(mob==null) return;
	    mob.setMoney(0);
	    clearInventoryMoney(mob,currency);
	}

	public void clearInventoryMoney(MOB mob, String currency)
	{
	    if(mob==null) return;
	    Vector clear=null;
	    Item I=null;
	    for(int i=0;i<mob.inventorySize();i++)
	    {
	        I=mob.fetchInventory(i);
	        if(I instanceof Coins)
	        {
	            if(clear==null) clear=new Vector();
	            if(currency==null)
		            clear.addElement(I);
	            else
	            if(((Coins)I).getCurrency().equalsIgnoreCase(currency))
	                clear.addElement(I);
	        }
	    }
	    if(clear!=null)
	        for(int i=0;i<clear.size();i++)
	            ((Item)clear.elementAt(i)).destroy();
	}

	public void subtractMoney(MOB mob, double denomination, double absoluteAmount)
	{ subtractMoney(mob,getCurrency(mob),denomination,absoluteAmount);}
	public void subtractMoney(MOB mob, String currency, double denomination, double absoluteAmount)
	{
		if(mob==null) return;
		Vector V=getStandardCurrency(mob,currency);
		Coins C=null;
		for(int v=0;v<V.size();v++)
		{
		    C=(Coins)V.elementAt(v);
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

	public Vector getStandardCurrency(MOB mob, String currency)
	{
	    Vector V=new Vector();
		if(mob==null) return V;
		if(((currency==null)||(currency.equals(getCurrency(mob))))&&(mob.getMoney()>0))
		{
		    addMoney(mob,getCurrency(mob),(double)mob.getMoney());
		    mob.setMoney(0);
		}
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item I=mob.fetchInventory(i);
			if((I!=null)
			&&(I instanceof Coins)
			&&((currency==null)||((Coins)I).getCurrency().equalsIgnoreCase(currency))
			&&(I.container()==null))
				V.addElement(I);
		}
		return V;
	}

	public long getNumberOfCoins(MOB mob, String currency, double denomination)
	{
	    Vector V=getStandardCurrency(mob,currency);
	    long gold=0;
	    for(int v=0;v<V.size();v++)
	        if(((Coins)V.elementAt(v)).getDenomination()==denomination)
	            gold+=((Coins)V.elementAt(v)).getNumberOfCoins();
	    return gold;
	}

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
		        for(int p=0;p<((Area)E).getNumParents();p++)
		        {
		            s=getCurrency(((Area)E).getParent(p));
			        if(s.length()>0)
			            break;
		        }
	        int x=s.indexOf("=");
	        if(x<0) return s.toUpperCase().trim();
	        return s.substring(0,x).toUpperCase().trim();
	    }
	    return "";
	}

	public double getTotalAbsoluteValue(MOB mob, String currency)
	{
		double money=0.0;
	    Vector V=getStandardCurrency(mob,currency);
	    for(int v=0;v<V.size();v++)
			money+=((Coins)V.elementAt(v)).getTotalValue();
		return money;
	}

	public double getTotalAbsoluteNativeValue(MOB mob)
	{
		double money=0.0;
	    Vector V=getStandardCurrency(mob,getCurrency(mob));
	    for(int v=0;v<V.size();v++)
			money+=((Coins)V.elementAt(v)).getTotalValue();
		return money;
	}
	public double getTotalAbsoluteShopKeepersValue(MOB mob, MOB shopkeeper)
	{
		double money=0.0;
	    Vector V=getStandardCurrency(mob,getCurrency(shopkeeper));
	    for(int v=0;v<V.size();v++)
			money+=((Coins)V.elementAt(v)).getTotalValue();
		return money;
	}

    public double getTotalAbsoluteValueAllCurrencies(MOB mob)
    { return getTotalAbsoluteValue(mob,null);}
}
