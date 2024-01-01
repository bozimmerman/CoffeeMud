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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
   Copyright 2001-2024 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "GenCoins";
	}

	@Override
	public int value()
	{
		return phyStats().ability();
	}

	protected String currency="";
	protected double denomination=1.0;

	public GenCoins()
	{
		super();
		myContainer=null;
		setMaterial(RawMaterial.RESOURCE_GOLD);
		setNumberOfCoins(100);
		setCurrency("");
		setDenomination(CMLib.beanCounter().getLowestDenomination(""));
		setDescription("");
	}

	@Override
	public String genericName()
	{
		if(CMLib.english().startsWithAnIndefiniteArticle(name())&&(CMStrings.numWords(name())<4))
			return CMStrings.removeColors(name());
		return L("some money");
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

	@Override
	protected boolean abilityImbuesMagic()
	{
		return false;
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
		if(number<Integer.MAX_VALUE)
			basePhyStats().setAbility((int)number);
		else
			basePhyStats().setAbility(Integer.MAX_VALUE);
		recoverPhyStats();
	}

	@Override
	public double getDenomination()
	{
		return denomination;
	}

	@Override
	public void setDenomination(double valuePerCoin)
	{
		if(valuePerCoin==0.0)
			valuePerCoin=1.0;
		denomination=valuePerCoin;
		setDynamicMaterial();
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
		setDynamicMaterial();
	}

	@Override
	public boolean isGeneric()
	{
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
	public boolean putCoinsBack()
	{
		if(amDestroyed())
			return false;
		return CMLib.beanCounter().putCoinsBack(this, owner());
	}

	private final static String[] MYCODES={"NUMCOINS","CURRENCY","DENOM"};
	@Override
	public String getStat(final String code)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			return CMLib.coffeeMaker().getGenItemStat(this,code);
		switch(getInternalCodeNum(code))
		{
		case 0:
			return "" + getNumberOfCoins();
		case 1:
			return "" + getCurrency();
		case 2:
			return "" + getDenomination();
		default:
			return CMProps.getStatCodeExtensionValue(getStatCodes(), xtraValues, code);
		}
	}

	@Override
	public void setStat(final String code, final String val)
	{
		if(CMLib.coffeeMaker().getGenItemCodeNum(code)>=0)
			CMLib.coffeeMaker().setGenItemStat(this,code,val);
		else
		switch(getInternalCodeNum(code))
		{
		case 0:
			setNumberOfCoins(CMath.s_parseIntExpression(val));
			break;
		case 1:
			setCurrency(val);
			break;
		case 2:
			setDenomination(CMath.s_double(val));
			break;
		default:
			CMProps.setStatCodeExtensionValue(getStatCodes(), xtraValues, code, val);
			break;
		}
	}

	private int getInternalCodeNum(final String code)
	{
		for(int i=0;i<MYCODES.length;i++)
		{
			if(code.equalsIgnoreCase(MYCODES[i]))
				return i;
		}
		return -1;
	}

	private static String[] codes=null;

	@Override
	public String[] getStatCodes()
	{
		if(codes!=null)
			return codes;
		final String[] MYCODES=CMProps.getStatCodesList(GenCoins.MYCODES,this);
		final String[] superCodes=CMParms.toStringArray(GenericBuilder.GenItemCode.values());
		codes=new String[superCodes.length+MYCODES.length];
		int i=0;
		for(;i<superCodes.length;i++)
			codes[i]=superCodes[i];
		for(int x=0;x<MYCODES.length;i++,x++)
			codes[i]=MYCODES[x];
		return codes;
	}

	@Override
	public boolean sameAs(final Environmental E)
	{
		if(!(E instanceof GenCoins))
			return false;
		final String[] codes=getStatCodes();
		for(int i=0;i<codes.length;i++)
		{
			if(!E.getStat(codes[i]).equals(getStat(codes[i])))
				return false;
		}
		return true;
	}
}
