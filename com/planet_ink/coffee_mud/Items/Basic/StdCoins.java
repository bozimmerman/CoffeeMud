package com.planet_ink.coffee_mud.Items.Basic;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2002-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "StdCoins";
	}

	@Override
	public int value()
	{
		return phyStats().ability();
	}
	double denomination=1.0;
	String currency="";

	public StdCoins()
	{
		super();
		myContainer=null;
		myUses=Integer.MAX_VALUE;
		material=RawMaterial.RESOURCE_GOLD;
		myWornCode=0;
		basePhyStats.setWeight(0);
		recoverPhyStats();
	}

	@Override
	protected boolean abilityImbuesMagic()
	{
		return false;
	}

	@Override
	public String Name()
	{
		return CMLib.beanCounter().getDenominationName(getCurrency(),getDenomination(),getNumberOfCoins());
	}

	@Override
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
			final RawMaterial.CODES codes = RawMaterial.CODES.instance();
			for(int s=0;s<codes.total();s++)
			{
				if(CMLib.english().containsString(name(),codes.name(s)))
				{
					setMaterial(codes.get(s));
					break;
				}
			}
		}
		setDescription(CMLib.beanCounter().getConvertableDescription(getCurrency(),getDenomination()));
	}

	@Override
	public long getNumberOfCoins()
	{
		return phyStats().ability();
	}

	@Override
	public void setNumberOfCoins(long number)
	{
		if(number<=Integer.MAX_VALUE)
			basePhyStats().setAbility((int)number);
		else
			basePhyStats().setAbility(Integer.MAX_VALUE);
		phyStats().setAbility(basePhyStats().ability());
	}

	@Override
	public double getDenomination()
	{
		return denomination;
	}

	@Override
	public void setDenomination(double valuePerCoin)
	{
		denomination=valuePerCoin;
		setMiscText(getCurrency()+"/"+valuePerCoin);
	}

	@Override
	public double getTotalValue()
	{
		return CMath.mul(getDenomination(),getNumberOfCoins());
	}

	@Override
	public String getCurrency()
	{
		return currency;
	}

	@Override
	public void setCurrency(String named)
	{
		currency=named;
		setMiscText(named+"/"+getDenomination());
	}

	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		final int x=text.indexOf('/');
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

	@Override
	public void recoverPhyStats()
	{
		if(((material&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_CLOTH)
		&&((material&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER))
			basePhyStats.setWeight((int)Math.round((basePhyStats().ability()/100.0)));
		basePhyStats.copyInto(phyStats);
		// import not to sup this, otherwise 'ability' makes it magical!
		for(final Enumeration<Ability> a=effects();a.hasMoreElements();)
		{
			final Ability A=a.nextElement();
			if(A!=null)
				A.affectPhyStats(this,phyStats);
		}
	}

	@Override
	public boolean putCoinsBack()
	{
		if(amDestroyed())
			return false;
		Coins alternative=null;
		if(owner() instanceof Room)
		{
			final Room R=(Room)owner();
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
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
			final MOB M=(MOB)owner();
			for(int i=0;i<M.numItems();i++)
			{
				final Item I=M.getItem(i);
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
