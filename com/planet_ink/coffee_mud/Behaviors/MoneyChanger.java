package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

	public void startBehavior(Environmental forMe)
	{
		if(forMe==null) return;
		if(!(forMe instanceof MOB)) return;
		((MOB)forMe).baseCharStats().setStat(CharStats.STRENGTH,100);
		super.startBehavior(forMe);
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
		&&(!CMSecurity.isAllowed(source,source.location(),"CMROOMS"))
		&&(msg.tool()!=null)
		&&(!(msg.tool() instanceof Coins)))
		{
			CommonMsgs.say(observer,source,"I'm sorry, I can only accept gold coins.",true,false);
			return false;
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
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Coins))
		{
			int value=((Coins)msg.tool()).numberOfCoins();
			int numberToTake=1;
			if(value>20) numberToTake=value/20;
			value-=numberToTake;
			observer.setMoney(observer.getMoney()-value);
			observer.recoverEnvStats();
			if(value>0)
			{
				MoneyUtils.giveMoney(observer,source,value);
				FullMsg newMsg=new FullMsg(observer,source,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'Thank you for your business' to <T-NAMESELF>.^?");
				msg.addTrailerMsg(newMsg);
			}
			else
				CommonMsgs.say(observer,source,"Gee, thanks. :)",true,false);
		}
	}
}
