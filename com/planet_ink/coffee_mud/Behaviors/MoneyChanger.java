package com.planet_ink.coffee_mud.Behaviors;

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
public class MoneyChanger extends StdBehavior
{
	public String ID(){return "MoneyChanger";}
	
	public Hashtable rates=new Hashtable();
	public double cut=0.05;

	public void startBehavior(Environmental forMe)
	{
		if(forMe==null) return;
		if(!(forMe instanceof MOB)) return;
		((MOB)forMe).baseCharStats().setStat(CharStats.STRENGTH,100);
		super.startBehavior(forMe);
	}

	public void setParms(String newParm)
	{
	    super.setParms(newParm);
	    rates.clear();
	    cut=0.05;
	    newParm=newParm.toUpperCase();
		int x=newParm.indexOf("=");
		while(x>0)
		{
		    int lastSp=newParm.lastIndexOf(" ",x);
		    if(lastSp<0) lastSp=0;
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
						double val=Util.s_double(newParm.substring(0,x));
						if(newParm.substring(0,x).indexOf(".")<0)
							val= new Long(Util.s_long(newParm.substring(0,x))).doubleValue();
						if(x<newParm.length())
							newParm=newParm.substring(x+1);
						else
						    newParm="";
						if(parm.equalsIgnoreCase("default"))
						    parm="";
						if(parm.equalsIgnoreCase("cut"))
						    cut=val/100.0;
						else
						    rates.put(parm,new Double(val/100.0));
					}
				}
				
			}
			x=newParm.indexOf("=");
		}
	}
	
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),"CMDROOMS"))
		&&(msg.tool()!=null))
		{
			if(!(msg.tool() instanceof Coins))
			{
				CommonMsgs.say(observer,source,"I'm sorry, I can only accept money.",true,false);
				return false;
			}
			else
			if(((rates.size()>0)&&(!rates.containsKey(((Coins)msg.tool()).getCurrency().toUpperCase()))))
			{
				CommonMsgs.say(observer,source,"I'm sorry, I don't accept that kind of currency.",true,false);
				return false;
			}
			double value=((Coins)msg.tool()).getTotalValue();
			double takeCut=cut;
			String currency=((Coins)msg.tool()).getCurrency().toUpperCase();
			if((rates.size()>0)&&(rates.containsKey(currency)))
			    takeCut=((Double)rates.get(currency)).doubleValue();
			double amountToTake=BeanCounter.abbreviatedRePrice(observer,value*takeCut);
			if((amountToTake>0.0)&&(amountToTake<BeanCounter.getLowestDenomination(BeanCounter.getCurrency(observer))))
			    amountToTake=BeanCounter.getLowestDenomination(BeanCounter.getCurrency(observer));
			value-=amountToTake;
			observer.recoverEnvStats();
			Coins C=BeanCounter.makeBestCurrency(observer,value);
			if((value<=0)||(C==null))
			{
				CommonMsgs.say(observer,source,"I'm sorry, I can not change such a small amount.",true,false);
				return false;
			}
		}
		return true;
	}
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		MOB source=msg.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;

		if((source!=observer)
		&&(msg.amITarget(observer))
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(observer,source))
        &&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool() instanceof Coins))
		{
			double value=((Coins)msg.tool()).getTotalValue();
			double takeCut=cut;
			String currency=((Coins)msg.tool()).getCurrency().toUpperCase();
			if((rates.size()>0)&&(rates.containsKey(currency)))
			    takeCut=((Double)rates.get(currency)).doubleValue();
			double amountToTake=BeanCounter.abbreviatedRePrice(observer,value*takeCut);
			if((amountToTake>0.0)&&(amountToTake<BeanCounter.getLowestDenomination(BeanCounter.getCurrency(observer))))
			    amountToTake=BeanCounter.getLowestDenomination(BeanCounter.getCurrency(observer));
			value-=amountToTake;
			observer.recoverEnvStats();
            Coins C=BeanCounter.makeBestCurrency(observer,value);
			if((value>0.0)&&(C!=null))
			{
				FullMsg newMsg=new FullMsg(observer,source,C,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Thank you for your business' to <T-NAMESELF>.^?");
                C.setOwner(observer);
                C.destroy();
				msg.addTrailerMsg(newMsg);
			}
			else
				CommonMsgs.say(observer,source,"Gee, thanks. :)",true,false);
            ((Coins)msg.tool()).destroy();
		}
        else
        if((msg.source()==observer)
        &&(msg.target() instanceof MOB)
        &&(msg.targetMinor()==CMMsg.TYP_SPEAK)
        &&(msg.tool() instanceof Coins)
        &&(((Coins)msg.tool()).amDestroyed())
        &&(!msg.source().isMine(msg.tool()))
        &&(!((MOB)msg.target()).isMine(msg.tool())))
            BeanCounter.giveSomeoneMoney(msg.source(),(MOB)msg.target(),((Coins)msg.tool()).getTotalValue());
	}
}
