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
public class StdCoins extends StdItem implements Coins
{
	public String ID(){	return "StdCoins";}
	public int value(){	return envStats().ability();}
	double denomination=1.0;
	String currency="";
	
	public StdCoins()
	{
		super();
		myContainer=null;
		myUses=Integer.MAX_VALUE;
		material=EnvResource.RESOURCE_GOLD;
		myWornCode=0;
		baseEnvStats.setWeight(0);
		recoverEnvStats();
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
	    if(number<=Integer.MAX_VALUE)
		    baseEnvStats().setAbility((int)number);
	    else
	        baseEnvStats().setAbility(Integer.MAX_VALUE);
	    envStats().setAbility(baseEnvStats().ability());
    }
	public double getDenomination(){return denomination;}
	public void setDenomination(double valuePerCoin)
	{
	    denomination=valuePerCoin;
	    setMiscText(getCurrency()+"/"+valuePerCoin);
	}
	public double getTotalValue(){return Util.mul(getDenomination(),getNumberOfCoins());}
	public String getCurrency(){ return currency;}
	public void setCurrency(String named)
	{
	    currency=named;
	    setMiscText(named+"/"+getDenomination());
	}
	
	public void setMiscText(String text)
	{
	    super.setMiscText(text);
	    int x=text.indexOf("/");
	    if(x>=0)
	    {
	        currency=text.substring(0,x);
	        denomination=Util.s_double(text.substring(x+1));
	        setDynamicMaterial();
	    }
	    else
	    {
	        setDenomination(1.0);
	        setCurrency("");
	    }
	}
	public void recoverEnvStats()
	{
		if(((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)
		&&((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PAPER))
			baseEnvStats.setWeight((int)Math.round((new Integer(baseEnvStats().ability()).doubleValue()/100.0)));
		envStats=baseEnvStats.cloneStats();
		// import not to sup this, otherwise 'ability' makes it magical!
		for(int a=0;a<numEffects();a++)
		{
			Ability A=fetchEffect(a);
            if(A!=null)	A.affectEnvStats(this,envStats);
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
				&&(!I.amDestroyed())
				&&(((Coins)I).getDenomination()==getDenomination())
				&&(((Coins)I).getCurrency().equals(getCurrency()))
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
				&&(!I.amDestroyed())
				&&(((Coins)I).getDenomination()==getDenomination())
				&&(((Coins)I).getCurrency().equals(getCurrency()))
				&&(I.container()==container()))
				{
					alternative=(Coins)I;
					break;
				}
			}
		}
		if((alternative!=null)&&(alternative!=this))
		{
			alternative.setNumberOfCoins(alternative.getNumberOfCoins()+getNumberOfCoins());
			destroy();
			return true;
		}
		return false;
	}
}
