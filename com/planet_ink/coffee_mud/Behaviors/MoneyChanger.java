package com.planet_ink.coffee_mud.Behaviors;
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
public class MoneyChanger extends StdBehavior
{
	@Override
	public String ID()
	{
		return "MoneyChanger";
	}

	protected Map<String,Double> rates=new Hashtable<String,Double>();
	protected double spaceMaxCut=0.0;
	protected long spaceMaxDistance=SpaceObject.Distance.GalaxyRadius.dm;
	protected double cut=0.05;
	private boolean complainedAboutSpaceError=false;
	
	@Override
	public String accountForYourself()
	{
		return "money changing for a fee";
	}

	@Override
	public void startBehavior(PhysicalAgent forMe)
	{
		if(forMe==null)
			return;
		if(!(forMe instanceof MOB))
			return;
		// i suppose this is for accepting heavy loads of coin
		((MOB)forMe).baseCharStats().setStat(CharStats.STAT_STRENGTH,100);
		super.startBehavior(forMe);
	}

	protected final Map<String,Double> getRatesFor(final Environmental affecting, String currency)
	{
		if(spaceMaxCut<=0.0)
			return rates;
		currency=currency.toUpperCase();
		if(rates.containsKey(currency))
			return rates;
		String myCurrency=CMLib.beanCounter().getCurrency(affecting);
		if(myCurrency.equalsIgnoreCase(currency))
		{
			rates.put(currency, Double.valueOf(cut));
			return rates;
		}
		SpaceObject homeO=CMLib.map().getSpaceObject(affecting, false);
		if(homeO!=null)
		{
			myCurrency=CMLib.beanCounter().getCurrency(homeO);
			if(myCurrency.equalsIgnoreCase(currency))
			{
				rates.put(currency, Double.valueOf(cut));
				return rates;
			}
		}
		else
		{
			// no space object, wtf? this SHOULD fail
			if(!complainedAboutSpaceError)
			{
				complainedAboutSpaceError=true;
				Log.errOut("MoneyChanger",affecting.Name()+" is not on a planet, so space rates cannot apply!");
			}
			return rates;
		}
		for(Enumeration<Area> a=CMLib.map().spaceAreas();a.hasMoreElements();)
		{
			Area A=a.nextElement();
			if((A!=null)&&(A!=homeO))
			{
				myCurrency=CMLib.beanCounter().getCurrency(A);
				if(myCurrency.equalsIgnoreCase(currency))
				{
					SpaceObject oA=(SpaceObject)A;
					long distance=CMLib.map().getDistanceFrom(homeO, oA);
					if((distance<0)||(distance>spaceMaxDistance))
					{
						rates.put(currency, Double.valueOf(spaceMaxCut));
					}
					else
					{
						double pct=CMath.div(distance, spaceMaxDistance);
						double amt=spaceMaxCut*pct;
						if(amt < cut)
							amt=cut;
						rates.put(currency, Double.valueOf(cut));
					}
					return rates;
				}
			}
		}
		
		return rates;
	}
	
	protected boolean doIExchangeThisCurrency(final Environmental affecting, final String currency)
	{
		final Map<String,Double> rates=getRatesFor(affecting, currency);
		return ((rates.size()==0)||(rates.containsKey(currency.toUpperCase())));
	}
	
	protected double getMyCut(final Environmental affecting, final String currency)
	{
		final Map<String,Double> rates=getRatesFor(affecting,currency);
		if((rates.size()>0)&&(rates.containsKey(currency.toUpperCase())))
			return rates.get(currency.toUpperCase()).doubleValue();
		return cut;
	}
	
	@Override
	public void setParms(String newParm)
	{
		super.setParms(newParm);
		rates.clear();
		cut=0.05;
		spaceMaxCut=0.0;
		spaceMaxDistance=SpaceObject.Distance.GalaxyRadius.dm;
		newParm=newParm.toUpperCase();
		int x=newParm.indexOf('=');
		while(x>0)
		{
			int lastSp=newParm.lastIndexOf(' ',x);
			if(lastSp<0)
				lastSp=0;
			if((lastSp>=0)&&(lastSp<x-1)&&(Character.isLetter(newParm.charAt(x-1))))
			{
				String parm=newParm.substring(lastSp,x).trim().toUpperCase();
				while((x<newParm.length())&&(newParm.charAt(x)!='='))
					x++;
				if(x<newParm.length())
				{
					while((x<newParm.length())
					&&(!Character.isDigit(newParm.charAt(x)))
					&&(newParm.charAt(x)!='.'))
						x++;
					if(x<newParm.length())
					{
						newParm=newParm.substring(x);
						x=0;
						while((x<newParm.length())
						&&((Character.isDigit(newParm.charAt(x)))||(newParm.charAt(x)=='.')))
							x++;
						double val=CMath.s_double(newParm.substring(0,x));
						if(newParm.substring(0,x).indexOf('.')<0)
							val= CMath.s_long(newParm.substring(0,x));
						if(x<newParm.length())
							newParm=newParm.substring(x+1);
						else
							newParm="";
						if(parm.equalsIgnoreCase("default"))
							parm="";
						if(parm.equalsIgnoreCase("spacemaxcut"))
							spaceMaxCut=val/100.0;
						else
						if(parm.equalsIgnoreCase("spacemaxdistance"))
							spaceMaxDistance=Math.round(CMath.mul(SpaceObject.Distance.GalaxyRadius.dm, val/100.0));
						else
						if(parm.equalsIgnoreCase("cut"))
							cut=val/100.0;
						else
							rates.put(parm,Double.valueOf(val/100.0));
					}
				}
			}
			x=newParm.indexOf('=');
		}
	}

	@Override
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		final MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		final MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),CMSecurity.SecFlag.CMDROOMS))
		&&(msg.tool()!=null))
		{
			if(!(msg.tool() instanceof Coins))
			{
				CMLib.commands().postSay(observer,source,L("I'm sorry, I can only accept money."),true,false);
				return false;
			}
			else
			if(!doIExchangeThisCurrency(affecting,((Coins)msg.tool()).getCurrency()))
			{
				CMLib.commands().postSay(observer,source,L("I'm sorry, I don't accept that kind of currency."),true,false);
				return false;
			}
			double value=((Coins)msg.tool()).getTotalValue();
			final String currency=((Coins)msg.tool()).getCurrency().toUpperCase();
			double takeCut=getMyCut(affecting,currency);
			double amountToTake=CMLib.beanCounter().abbreviatedRePrice(observer,value*takeCut);
			if((amountToTake>0.0)&&(amountToTake<CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(observer))))
				amountToTake=CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(observer));
			value-=amountToTake;
			observer.recoverPhyStats();
			final Coins C=CMLib.beanCounter().makeBestCurrency(observer,value);
			if((value<=0)||(C==null))
			{
				CMLib.commands().postSay(observer,source,L("I'm sorry, I can not change such a small amount."),true,false);
				return false;
			}
		}
		return true;
	}

	@Override
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		final MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		final MOB observer=(MOB)affecting;

		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool() instanceof Coins))
		{
			if((CMLib.flags().canBeSeenBy(source,observer))
			&&(CMLib.flags().canBeSeenBy(observer,source)))
			{
				double value=((Coins)msg.tool()).getTotalValue();
				final String currency=((Coins)msg.tool()).getCurrency().toUpperCase();
				double takeCut=getMyCut(affecting,currency);
				double amountToTake=CMLib.beanCounter().abbreviatedRePrice(observer,value*takeCut);
				if((amountToTake>0.0)&&(amountToTake<CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(observer))))
					amountToTake=CMLib.beanCounter().getLowestDenomination(CMLib.beanCounter().getCurrency(observer));
				value-=amountToTake;
				observer.recoverPhyStats();
				final Coins C=CMLib.beanCounter().makeBestCurrency(observer,value);
				if((value>0.0)&&(C!=null))
				{
					// this message will actually end up triggering the hand-over.
					final CMMsg newMsg=CMClass.getMsg(observer,source,C,CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'Thank you for your business' to <T-NAMESELF>.^?"));
					C.setOwner(observer);
					final long num=C.getNumberOfCoins();
					final String curr=C.getCurrency();
					final double denom=C.getDenomination();
					C.destroy();
					C.setNumberOfCoins(num);
					C.setCurrency(curr);
					C.setDenomination(denom);
					msg.addTrailerMsg(newMsg);
				}
				else
					CMLib.commands().postSay(observer,source,L("Gee, thanks. :)"),true,false);
				((Coins)msg.tool()).destroy();
			}
			else
			if(!CMLib.flags().canBeSeenBy(source,observer))
				CMLib.commands().postSay(observer,null,L("Wha?  Where did this come from?  Cool!"),true,false);
		}
		else
		if((msg.source()==observer)
		&&(msg.target() instanceof MOB)
		&&(msg.targetMinor()==CMMsg.TYP_SPEAK)
		&&(msg.tool() instanceof Coins)
		&&(((Coins)msg.tool()).amDestroyed())
		&&(!msg.source().isMine(msg.tool()))
		&&(!((MOB)msg.target()).isMine(msg.tool())))
			CMLib.beanCounter().giveSomeoneMoney(msg.source(),(MOB)msg.target(),((Coins)msg.tool()).getTotalValue());
	}
}
