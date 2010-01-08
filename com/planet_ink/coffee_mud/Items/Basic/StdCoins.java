package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
		material=RawMaterial.RESOURCE_GOLD;
		myWornCode=0;
		baseEnvStats.setWeight(0);
		recoverEnvStats();
	}
    protected boolean abilityImbuesMagic(){return false;}

	public String Name()
	{
        return CMLib.beanCounter().getDenominationName(getCurrency(),getDenomination(),getNumberOfCoins());
	}
	public String displayText()
	{
        return CMLib.beanCounter().getDenominationName(getCurrency(),getDenomination(),getNumberOfCoins())+((getNumberOfCoins()==1)?" lies here.":" lie here.");
	}

	public void setDynamicMaterial()
	{
	    if((CMLib.english().containsString(name(),"note"))
	    ||(CMLib.english().containsString(name(),"bill"))
	    ||(CMLib.english().containsString(name(),"dollar")))
	        setMaterial(RawMaterial.RESOURCE_PAPER);
	    else
	    {
	    	RawMaterial.CODES codes = RawMaterial.CODES.instance();
			for(int s=0;s<codes.total();s++)
			    if(CMLib.english().containsString(name(),codes.name(s)))
			    {
			        setMaterial(codes.get(s));
			        break;
			    }
	    }
		setDescription(CMLib.beanCounter().getConvertableDescription(getCurrency(),getDenomination()));
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
	public double getTotalValue(){return CMath.mul(getDenomination(),getNumberOfCoins());}
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
	        denomination=CMath.s_double(text.substring(x+1));
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
		if(((material&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
		&&((material&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER))
			baseEnvStats.setWeight((int)Math.round((baseEnvStats().ability()/100.0)));
		baseEnvStats.copyInto(envStats);
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
