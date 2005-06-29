package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class GenCoins extends GenItem implements Coins
{
	public String ID(){	return "GenCoins";}
	public int value(){	return envStats().ability();}
	protected String currency="";
	protected double denomination=1.0;
	
	public GenCoins()
	{
		super();
		myContainer=null;
		setMaterial(EnvResource.RESOURCE_GOLD);
		setNumberOfCoins(100);
		setCurrency("");
		setDenomination(BeanCounter.getLowestDenomination(""));
		setDescription("");
	}
	
	public String Name()
	{
        return BeanCounter.getDenominationName(getCurrency(),getDenomination(),getNumberOfCoins());
	}
	public String displayText()
	{
        return BeanCounter.getDenominationName(getCurrency(),getDenomination(),getNumberOfCoins())+((getNumberOfCoins()==1)?" lies here.":" lie here.");
	}
	
	public void setDynamicMaterial()
	{
	    if((EnglishParser.containsString(name(),"note"))
	    ||(EnglishParser.containsString(name(),"bill"))
	    ||(EnglishParser.containsString(name(),"dollar")))
	        setMaterial(EnvResource.RESOURCE_PAPER);
	    else
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
		    if(EnglishParser.containsString(name(),EnvResource.RESOURCE_DESCS[i]))
		    {
		        setMaterial(EnvResource.RESOURCE_DATA[i][0]);
		        break;
		    }
		setDescription(BeanCounter.getConvertableDescription(getCurrency(),getDenomination()));
	}
	public long getNumberOfCoins(){return envStats().ability();}
	public void setNumberOfCoins(long number)
	{
	    if(number<Integer.MAX_VALUE)
		    baseEnvStats().setAbility((int)number); 
	    else
		    baseEnvStats().setAbility(Integer.MAX_VALUE); 
	    recoverEnvStats();
	}
	public double getDenomination(){return denomination;}
	public void setDenomination(double valuePerCoin)
	{
	    if(valuePerCoin==0.0)
	        valuePerCoin=1.0;
	    denomination=valuePerCoin; 
	    setDynamicMaterial();
	}
	public double getTotalValue(){return Util.mul(getDenomination(),getNumberOfCoins());}
	public String getCurrency(){return currency;}
	public void setCurrency(String named){currency=named; setDynamicMaterial();}

	public boolean isGeneric(){return true;}
	public void recoverEnvStats()
	{
		if(((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)
		&&((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PAPER))
			baseEnvStats.setWeight((int)Math.round((new Integer(baseEnvStats().ability()).doubleValue()/100.0)));
		envStats=baseEnvStats.cloneStats();
		// import not to sup this, otherwise 'ability' makes it magical!
		for(int a=0;a<numEffects();a++)
		{
			Ability effect=fetchEffect(a);
			effect.affectEnvStats(this,envStats);
		}
	}

	public boolean putCoinsBack()
	{
	    if(amDestroyed()) 
	        return false;
		Coins alternative=null;
		if(owner() instanceof Room)
		{
			Room R=(Room)owner();
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
			    &&(I!=this)
			    &&(I instanceof Coins)
			    &&(((Coins)I).getDenomination()==getDenomination())
			    &&((Coins)I).getCurrency().equals(getCurrency())
			    &&(I.container()==container()))
				{
					alternative=(Coins)I;
					break;
				}
			}
		}
		else
		if(owner() instanceof MOB)
		{
			MOB M=(MOB)owner();
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
			    &&(I!=this)
			    &&(I instanceof Coins)
			    &&(((Coins)I).getDenomination()==getDenomination())
			    &&((Coins)I).getCurrency().equals(getCurrency())
			    &&(I.container()==container()))
				{
					alternative=(Coins)I;
					break;
				}
			}
		}
		if((alternative!=null)&&(alternative!=this))
		{
System.out.println(alternative.owner().name()+"/"+alternative.name());            
			alternative.setNumberOfCoins(alternative.getNumberOfCoins()+getNumberOfCoins());
			destroy();
			return true;
		}
		return false;
	}

	private static String[] MYCODES={"NUMCOINS","CURRENCY","DENOM"};
	public String getStat(String code)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			return CoffeeMaker.getGenItemStat(this,code);
		else
		switch(getCodeNum(code))
		{
		case 0: return ""+getNumberOfCoins();
		case 1: return ""+getCurrency();
		case 2: return ""+getDenomination();
		}
		return "";
	}
	public void setStat(String code, String val)
	{
		if(CoffeeMaker.getGenItemCodeNum(code)>=0)
			CoffeeMaker.setGenItemStat(this,code,val);
		else
		switch(getCodeNum(code))
		{
		case 0: setNumberOfCoins(Util.s_int(val)); break;
		case 1: setCurrency(val); break;
		case 2: setDenomination(Util.s_double(val)); break;
		}
	}
	protected int getCodeNum(String code){
		for(int i=0;i<MYCODES.length;i++)
			if(code.equalsIgnoreCase(MYCODES[i])) return i;
		return -1;
	}
	private static String[] codes=null;
	public String[] getStatCodes()
	{
		if(codes!=null) return codes;
		String[] superCodes=CoffeeMaker.GENITEMCODES;
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}
	public boolean sameAs(Environmental E)
	{
		if(!(E instanceof GenCoins)) return false;
		String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		return true;
	}
}
