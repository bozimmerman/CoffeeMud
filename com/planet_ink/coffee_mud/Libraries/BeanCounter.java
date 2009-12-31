package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class BeanCounter extends StdLibrary implements MoneyLibrary
{
    public String ID(){return "BeanCounter";}
    public Hashtable<String,MoneyDenomination[]> currencies=new Hashtable<String,MoneyDenomination[]>();
    public static Hashtable<String,MoneyDenomination[]> defaultCurrencies=new Hashtable<String,MoneyDenomination[]>();
    public Vector<String> allCurrencyNames=new Vector<String>();
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

    public MoneyDenomination[] createCurrencySet(String currency)
    { 
    	return createCurrencySet(currencies,currency);
    }
    
	protected MoneyDenomination[] createCurrencySet(Hashtable currencies, String currency)
	{
	    int x=currency.indexOf("=");
	    if(x<0) return null;
	    String code=currency.substring(0,x).trim().toUpperCase();
	    if(currencies.containsKey(code))
	        return (MoneyDenomination[])currencies.get(code);
        currency=currency.substring(x+1).trim();
        Vector CV=CMParms.parseSemicolons(currency,true);
        Vector<MoneyDenomination> DV=new Vector<MoneyDenomination>();
        String s=null;
        String num=null;
        double d=0.0;
        Vector currencyNames=new Vector();
        for(int v=0;v<CV.size();v++)
        {
            s=(String)CV.elementAt(v);
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
                    if(((MoneyDenomination)DV.elementAt(i)).value>d)
                    { insertAt=i; break;}
                if((insertAt<0)||(insertAt>=DV.size()))
	                DV.addElement(new MoneyDenomination(d,s,shortName));
                else
                    DV.insertElementAt(new MoneyDenomination(d,s,shortName),insertAt);
                currencyNames.addElement(s);
                if(shortName.length()>0)
                    currencyNames.add(shortName);
            }
        }
        MoneyDenomination[] DVs=new MoneyDenomination[DV.size()];
        for(int i=0;i<DV.size();i++)
        	DVs[i]=(MoneyDenomination)DV.elementAt(i);
        currencies.put(code,DVs);
        allCurrencyNames.addElement(code);
        allCurrencyDenominationNames.put(code,currencyNames);
        return DVs;
	}

	public int getDenominationIndex(String currency, double value)
	{
		MoneyDenomination[] DV=getCurrencySet(currency);
		if(DV!=null)
		for(int d=0;d<DV.length;d++)
			if(value==DV[d].value) return d;
		return -1;
	}
	
	public MoneyDenomination[] getCurrencySet(String currency)
	{
	    if(currency==null) return null;
	    String code=currency.toUpperCase().trim();
	    int x=code.indexOf("=");
	    if(x<0)
	    {
	        if(currencies.containsKey(code))
	            return (MoneyDenomination[])currencies.get(code);
            if(defaultCurrencies.size()==0)
            {
                createCurrencySet(defaultCurrencies,defaultCurrencyDefinition);
                createCurrencySet(defaultCurrencies,goldStandard);
                createCurrencySet(defaultCurrencies,copperStandard);
            }
            if(defaultCurrencies.containsKey(code))
                return (MoneyDenomination[])defaultCurrencies.get(code);
	        return null;
	    }
        code=code.substring(0,x).trim();
        if(currencies.containsKey(code))
            return (MoneyDenomination[])currencies.get(code);
        return createCurrencySet(currency);
	}

	public Vector getAllCurrencies()
	{ 
		return allCurrencyNames;
	}

	public Vector getDenominationNameSet(String currency)
	{
	    if(allCurrencyDenominationNames.containsKey(currency))
	        return (Vector)allCurrencyDenominationNames.get(currency);
        return new Vector();
	}

	public double lowestAbbreviatedDenomination(String currency)
	{
		MoneyDenomination[] DV=getCurrencySet(currency);
	    if(DV!=null)
	    {
	        for(int i=0;i<DV.length;i++)
	            if(DV[i].abbr.length()>0)
	                return DV[i].value;
	        return getLowestDenomination(currency);
	    }
	    return 1.0;
	}

	public double lowestAbbreviatedDenomination(String currency, double absoluteAmount)
	{
		MoneyDenomination[] DV=getCurrencySet(currency);
	    if(DV!=null)
	    {
	        double absoluteLowest=lowestAbbreviatedDenomination(currency);
	        double lowestDenom=Double.MAX_VALUE;
	        double diff=0.0;
	        double denom=0.0;
	        long num=0;
	        for(int i=DV.length-1;i>=0;i--)
	            if(DV[i].abbr.length()>0)
	            {
	                denom=DV[i].value;
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
	{  
		return abbreviatedRePrice(getCurrency(shopkeeper),absoluteAmount);
	}
	
	public double abbreviatedRePrice(String currency, double absoluteAmount)
	{
	    double lowDenom=lowestAbbreviatedDenomination(currency,absoluteAmount);
	    long lowAmt=Math.round(absoluteAmount/lowDenom);
	    return CMath.mul(lowDenom,lowAmt);
	}
	
	public String abbreviatedPrice(MOB shopkeeper, double absoluteAmount)
	{ 
		return abbreviatedPrice(getCurrency(shopkeeper),absoluteAmount);
	}
	
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
		MoneyDenomination[] DV=getCurrencySet(currency);
	    if(DV==null) return "";
	    for(int d=0;d<DV.length;d++)
	        if(DV[d].value==denomination)
	            return DV[d].abbr;
	    return "";
	}

	public double getLowestDenomination(String currency)
	{
		MoneyDenomination[] DV=getCurrencySet(currency);
	    if((DV==null)||(DV.length==0)) return 1.0;
	    return DV[0].value;
	}

	public String getDenominationName(String currency)
	{ 
		return getDenominationName(currency,getLowestDenomination(currency));
	}

	public String getDenominationName(String currency, double denomination, long number)
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
		MoneyDenomination[] DV=getCurrencySet(currency);
		double denom=0.0;
		if(DV!=null)
		{
			double low=getLowestDenomination(currency);
			for(int d=DV.length-1;d>=0;d--)
			{
			    denom=DV[d].value;
			    if((denom<=absoluteValue)
			    &&(absoluteValue-(Math.floor(absoluteValue/denom)*denom)<low))
			        return denom;
			}
		}
		return denom;
	}
	
    public double getBestDenomination(String currency, int numberOfCoins, double absoluteValue)
    {
    	MoneyDenomination[] DV=getCurrencySet(currency);
        double bestDenom=0.0;
        if(DV!=null)
        {
            for(int d=DV.length-1;d>=0;d--)
            {
                double denom=DV[d].value;
                if(((denom*((double)numberOfCoins))<=absoluteValue)
                &&(denom>bestDenom))
                    bestDenom=denom;
            }
        }
        return bestDenom;
    }
    
	public double[] getBestDenominations(String currency, double absoluteValue)
	{
		MoneyDenomination[] DV=getCurrencySet(currency);
		Vector V=new Vector();
		if(DV!=null)
		for(int d=DV.length-1;d>=0;d--)
		{
		    double denom=DV[d].value;
		    if(denom<=absoluteValue)
		    {
		        long number=Math.round(Math.floor(absoluteValue/denom));
		        if(number>0)
		        {
		            V.addElement(Double.valueOf(denom));
		            absoluteValue-=CMath.mul(denom,number);
		        }
		    }
		}
		double[] ds=new double[V.size()];
		for(int d=0;d<V.size();d++)
			ds[d]=((Double)V.elementAt(d)).doubleValue();
		return ds;
	}
	
	public String getConvertableDescription(String currency, double denomination)
	{
	    double low=getLowestDenomination(currency);
	    if(low==denomination) return "";
	    return "Equal to "+getDenominationName(currency,low,Math.round(Math.floor(denomination/low)))+".";
	}

	public String getDenominationName(String currency, double denomination)
	{
		MoneyDenomination[] DV=getCurrencySet(currency);
	    if((DV==null)||(DV.length==0)) DV=getCurrencySet("");
	    if((DV==null)||(DV.length==0)) return "unknown!";
	    int closestX=getDenominationIndex(currency, denomination);
	    if(closestX<0)
	    for(int i=0;i<DV.length;i++)
	    	if(DV[i].value<=denomination)
	    	{
		        if((DV[i].value==denomination)
    	        ||(closestX<0)
    	        ||((denomination-DV[i].value)<(denomination-DV[closestX].value)))
    	            closestX=i;
	    	}
	    if(closestX<0)
	        return "unknown";
        return DV[closestX].name;
	}

	public String nameCurrencyShort(MOB mob, double absoluteValue)
	{   
		return nameCurrencyShort(getCurrency(mob),absoluteValue);
	}
	
	public String nameCurrencyShort(MOB mob, int absoluteValue)
	{   
		return nameCurrencyShort(getCurrency(mob),(double)absoluteValue);
	}
	
	public String nameCurrencyShort(String currency, double absoluteValue)
	{
		double denom=getBestDenomination(currency,absoluteValue);
		if(denom>0.0)
		    return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue/denom)));
	    return getDenominationName(currency,denom,Math.round(Math.floor(absoluteValue)));
	}
	
	public String nameCurrencyLong(MOB mob, double absoluteValue)
	{   
		return nameCurrencyLong(getCurrency(mob),absoluteValue);
	}
	
	public String nameCurrencyLong(MOB mob, int absoluteValue)
	{   
		return nameCurrencyLong(getCurrency(mob),(double)absoluteValue);
	}
	
	public String nameCurrencyLong(String currency, double absoluteValue)
	{
	    StringBuffer str=new StringBuffer("");
		double[] ds=getBestDenominations(currency,absoluteValue);
		for(int d=0;d<ds.length;d++)
		{
		    double denom=ds[d];
	        long number=Math.round(Math.floor(absoluteValue/denom));
	        String name=getDenominationName(currency,denom,number);
            absoluteValue-=CMath.mul(denom,number);
            if(str.length()>0) str.append(", ");
            str.append(name);
		}
		return str.toString();
	}

	public Coins makeBestCurrency(MOB mob, double absoluteValue, Environmental owner, Item container)
	{ 
		return makeBestCurrency(getCurrency(mob),absoluteValue,owner,container);
	}
	
	public Coins makeBestCurrency(String currency, double absoluteValue, Environmental owner, Item container)
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

	protected void parseDebt(Vector<DebtItem> debt, String debtor, String xml)
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
			double amt=CMLib.xml().getDoubleFromPieces(ablk.contents,"AMT");
			String reason=CMLib.xml().getValFromPieces(ablk.contents,"FOR");
			long due=CMLib.xml().getLongFromPieces(ablk.contents,"DUE");
			double interest=CMLib.xml().getDoubleFromPieces(ablk.contents,"INT");
			debt.addElement(new DebtItem(debtor,owed,amt,reason,due,interest));
		}
	}
	
	protected String unparseDebt(Vector<DebtItem> debt, String name, String owedTo)
	{
		StringBuffer xml=new StringBuffer("<DEBT>");
		for(int d=0;d<debt.size();d++)
		{
			if((debt.elementAt(d).debtor.equalsIgnoreCase(name))
			&&(debt.elementAt(d).owedTo.equalsIgnoreCase(owedTo)))
			{
				xml.append("<OWE>");
				xml.append(CMLib.xml().convertXMLtoTag("TO",debt.elementAt(d).owedTo));
				xml.append(CMLib.xml().convertXMLtoTag("AMT",""+debt.elementAt(d).amt));
				xml.append(CMLib.xml().convertXMLtoTag("FOR",debt.elementAt(d).reason));
				xml.append(CMLib.xml().convertXMLtoTag("DUE",""+debt.elementAt(d).due));
				xml.append(CMLib.xml().convertXMLtoTag("INT",""+debt.elementAt(d).interest));
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
			Vector<DebtItem> debt=getDebt(name,owedTo);
			double total=0.0;
			for(int d=0;d<debt.size();d++)
				total+=debt.elementAt(d).amt;
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

	public Vector<DebtItem> getDebtOwed(String owedTo)
	{
		Vector rows=CMLib.database().DBReadDataKey("DEBT",".*-DEBT-"+owedTo.toUpperCase().trim());
		Vector<DebtItem> debt=new Vector<DebtItem>(rows.size());
		for(int r=0;r<rows.size();r++)
		{
			PlayerData row=(PlayerData)rows.elementAt(r);
			String debtor=row.who;
			String xml=row.xml;
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	public void adjustDebt(String name, String owedTo, double adjustAmt, String reason, double interest, long due)
	{
		String key=name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim();
		synchronized(key.intern())
		{
			Vector<DebtItem> debts=getDebt(name,owedTo);
			boolean update=debts.size()>0;
			boolean done=false;
			for(int d=0;d<debts.size();d++)
			{
				DebtItem debt=debts.elementAt(d);
				if((debt.debtor.equalsIgnoreCase(name))
				&&(debt.owedTo.equalsIgnoreCase(owedTo))
				&&(debt.interest==interest)
				&&(debt.due==due)
				&&(debt.reason.equalsIgnoreCase(reason)))
				{
					debt.amt+=adjustAmt;
					if(debt.amt<=0.0) debts.removeElementAt(d);
					done=true;
					break;
				}
			}
			if((!done)&&(adjustAmt>=0.0))
				debts.addElement(new DebtItem(name,owedTo,adjustAmt,reason,due,interest));
			
			String xml=unparseDebt(debts,name,owedTo);
			if(update)
			{
				if(debts.size()==0)
					CMLib.database().DBDeleteData(name.toUpperCase(),"DEBT",key);
				else
					CMLib.database().DBUpdateData(key,xml);
			}
			else
				CMLib.database().DBCreateData(name.toUpperCase(),"DEBT",key,xml);
		}
	}

	public Vector<DebtItem> getDebt(String name, String owedTo)
	{
		Vector rows=CMLib.database().DBReadData(name.toUpperCase(),"DEBT",name.toUpperCase()+"-DEBT-"+owedTo.toUpperCase().trim());
		Vector<DebtItem> debt=new Vector<DebtItem>(rows.size());
		for(int r=0;r<rows.size();r++)
		{
			PlayerData row=(PlayerData)rows.elementAt(r);
			String debtor=row.who;
			String xml=row.xml;
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	public Vector<DebtItem> getDebt(String name)
	{
		Vector rows=CMLib.database().DBReadData(name.toUpperCase(),"DEBT");
		Vector<DebtItem> debt=new Vector<DebtItem>(rows.size());
		for(int r=0;r<rows.size();r++)
		{
			PlayerData row=(PlayerData)rows.elementAt(r);
			String debtor=row.who;
			String xml=row.xml;
			parseDebt(debt,debtor,xml);
		}
		return debt;
	}

	public Coins makeBestCurrency(MOB mob, double absoluteValue)
	{ 
		return makeBestCurrency(getCurrency(mob),absoluteValue);
	}
	
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
	    double[] ds=getBestDenominations(currency,absoluteValue);
		for(int d=0;d<ds.length;d++)
		{
		    double denom=ds[d];
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
	{  
		addMoney(customer,getCurrency(customer),(double)absoluteValue);
	}
	
	public void addMoney(MOB customer, double absoluteValue)
	{  
		addMoney(customer,getCurrency(customer),absoluteValue);
	}
	
	public void addMoney(MOB customer, String currency,int absoluteValue)
	{  
		addMoney(customer,currency,(double)absoluteValue);
	}
	
	public void addMoney(MOB customer, Item container, String currency,int absoluteValue)
	{  
		addMoney(customer,container,currency,(double)absoluteValue);
	}
	
	public void addMoney(MOB mob, String currency, double absoluteValue)
	{
		addMoney(mob,null,currency,(double)absoluteValue);
	}
	
	public void addMoney(MOB mob, Item container, String currency, double absoluteValue)
	{
	    if(mob==null) return;
		Vector V=makeAllCurrency(currency,absoluteValue);
	    for(int i=0;i<V.size();i++)
	    {
	        Coins C=(Coins)V.elementAt(i);
	        C.setContainer(container);
	        mob.addInventory(C);
	        C.putCoinsBack();
		}
		mob.recoverEnvStats();
	}

    public void giveSomeoneMoney(MOB recipient, double absoluteValue)
    {  
    	giveSomeoneMoney(recipient,recipient,getCurrency(recipient),absoluteValue); 
    }
    
    public void giveSomeoneMoney(MOB recipient, String currency, double absoluteValue)
    {  
    	giveSomeoneMoney(recipient,recipient,currency,absoluteValue); 
    }
    
	public void giveSomeoneMoney(MOB banker, MOB customer, double absoluteValue)
	{  
		giveSomeoneMoney(banker,customer,getCurrency(banker),absoluteValue); 
	}
	
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

	public void dropMoney(Room R, Item container, String currency, double absoluteValue)
	{
		Vector V=makeAllCurrency(currency,absoluteValue);
    	for(Enumeration e=V.elements();e.hasMoreElements();)
    	{
    		Coins I = (Coins)e.nextElement();
    		I.setContainer(container);
    		R.addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
		    ((Coins)I).putCoinsBack();
    	}
	}

	public void removeMoney(Room R, Item container, String currency, double absoluteValue)
	{
		double myMoney=getTotalAbsoluteValue(R,container,currency);
		Vector V=getStandardCurrency(R,container,currency);
		for(int v=0;v<V.size();v++)
		    ((Item)V.elementAt(v)).destroy();
		if(myMoney>=absoluteValue)
		    myMoney-=absoluteValue;
		else
		    myMoney=0.0;
		if(myMoney>0.0)
			dropMoney(R,container,currency,myMoney);
	}

	public void bankLedger(String bankName, String owner, String explanation)
	{
		synchronized((this+"LEDGER"+bankName).intern())
		{
			Vector V=CMLib.database().DBReadData(owner,"LEDGER-"+bankName,"LEDGER-"+bankName+"/"+owner);
			if((V!=null)&&(V.size()>0))
			{
				DatabaseEngine.PlayerData D=(DatabaseEngine.PlayerData)V.firstElement();
				String last=D.xml;
				if(last.length()>4096)
				{
				    int x=last.indexOf(";|;",1024);
				    if(x>=0) last=last.substring(x+3);
				}
				CMLib.database().DBReCreateData(owner,D.section,D.key,last+explanation+";|;");
			}
			else
				CMLib.database().DBCreateData(owner,"LEDGER-"+bankName,"LEDGER-"+bankName+"/"+owner,explanation+";|;");
		}
	}

	public boolean modifyBankGold(String bankName, String owner, String explanation, String currency, double absoluteAmount)
	{
		Vector V=CMLib.database().DBReadAllPlayerData(owner);
		for(int v=0;v<V.size();v++)
		{
			DatabaseEngine.PlayerData D=(DatabaseEngine.PlayerData)V.elementAt(v);
			String last=D.xml;
			if(last.startsWith("COINS;"))
			{
				if((bankName==null)||(bankName.length()==0)||(bankName.equals(D.section)))
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
							CMLib.database().DBReCreateData(owner,D.section,D.key,"COINS;"+CMLib.coffeeMaker().getPropertiesStr(C,true));
						else
							CMLib.database().DBDeleteData(owner,D.section,D.key);
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
	{ 
		subtractMoneyGiveChange(banker,mob,(double)absoluteAmount);
	}
	
	public void subtractMoneyGiveChange(MOB banker, MOB mob, double absoluteAmount)
	{ 
		subtractMoneyGiveChange(banker, mob,(banker!=null)?getCurrency(banker):getCurrency(mob),absoluteAmount);
	}
	
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
	{ 
		subtractMoney(mob,getCurrency(mob),absoluteAmount);
	}
	
	public void subtractMoney(MOB mob, String currency, double absoluteAmount)
	{
		subtractMoney(mob,null,getCurrency(mob),absoluteAmount);
	}
	public void subtractMoney(MOB mob, Item container, String currency, double absoluteAmount)
	{
		if(mob==null) return;
		double myMoney=getTotalAbsoluteValue(mob,container,currency);
		Vector V=getStandardCurrency(mob,container,currency);
		for(int v=0;v<V.size();v++)
		    ((Item)V.elementAt(v)).destroy();
		if(myMoney>=absoluteAmount)
		    myMoney-=absoluteAmount;
		else
		    myMoney=0.0;
		if(myMoney>0.0)
			addMoney(mob,container,currency,myMoney);
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
	{ 
		subtractMoney(mob,getCurrency(mob),denomination,absoluteAmount);
	}
	
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

	public Vector getStandardCurrency(Room R, Item container, String currency)
	{
	    Vector V=new Vector();
		if(R==null) return V;
		for(int i=0;i<R.numItems();i++)
		{
			Item I=R.fetchItem(i);
			if((I!=null)
			&&(I instanceof Coins)
			&&((currency==null)||((Coins)I).getCurrency().equalsIgnoreCase(currency))
			&&(I.container()==container))
				V.addElement(I);
		}
		return V;
	}

	public Vector getStandardCurrency(MOB mob, String currency)
	{
		return getStandardCurrency(mob, null, currency);
	}
	
	public Vector getStandardCurrency(MOB mob, Item container, String currency)
	{
	    Vector V=new Vector();
		if(mob==null) return V;
		if(((currency==null)||(currency.equals(getCurrency(mob))))&&(mob.getMoney()>0)&&(container==null))
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
			&&(I.container()==container))
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

	public double getTotalAbsoluteValue(Room R, Item container, String currency)
	{
		double money=0.0;
	    Vector V=getStandardCurrency(R,container,currency);
	    for(int v=0;v<V.size();v++)
			money+=((Coins)V.elementAt(v)).getTotalValue();
		return money;
	}

	public double getTotalAbsoluteValue(MOB mob, String currency)
	{
		return getTotalAbsoluteValue(mob, null, currency);
	}
	
	public double getTotalAbsoluteValue(MOB mob, Item container, String currency)
	{
		double money=0.0;
	    Vector V=getStandardCurrency(mob,container,currency);
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
    { 
    	return getTotalAbsoluteValue(mob,null);
    }
    
}
