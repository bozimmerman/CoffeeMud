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
   Copyright 2002-2022 Bo Zimmerman

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

	protected double denomination=1.0;
	protected String currency="";

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
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name()))
			return CMStrings.removeColors(name());
		return L("some money");
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
	public void setNumberOfCoins(final long number)
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
	public void setDenomination(final double valuePerCoin)
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
	public void setCurrency(final String named)
	{
		currency=named;
		setMiscText(named+"/"+getDenomination());
	}

	@Override
	public void setMiscText(final String text)
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
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_DROP:
			if(msg.target()==this)
			{
				final MoneyLibrary.MoneyDefinition def=CMLib.beanCounter().getCurrencySet(currency);
				if(((def != null) && (!def.canTrade()))
				&&(!CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.CMDPLAYERS)))
				{
					msg.source().tell(L("You can't seem to let go of @x1.",name()));
					return false;
				}
			}
			break;
		case CMMsg.TYP_GIVE:
		case CMMsg.TYP_PUT:
		case CMMsg.TYP_DEPOSIT:
			if(msg.tool()==this)
			{
				final MoneyLibrary.MoneyDefinition def=CMLib.beanCounter().getCurrencySet(currency);
				if(((def != null) && (!def.canTrade()))
				&&(!CMSecurity.isAllowed(msg.source(), msg.source().location(), CMSecurity.SecFlag.CMDPLAYERS)))
				{
					msg.source().tell(L("You can't seem to do that with @x1.",name()));
					return false;
				}
			}
			break;
		default:
			break;
		}
		return true;
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
		return CMLib.beanCounter().putCoinsBack(this, owner());
	}
}
