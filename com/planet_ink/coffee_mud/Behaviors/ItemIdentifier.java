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
public class ItemIdentifier extends StdBehavior
{
	public String ID(){return "ItemIdentifier";}

	private int cost(Item item)
	{
		int cost=500+(item.envStats().level()*20);
		return cost;
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
		&&(msg.tool() instanceof Item))
		{
			int cost=cost((Item)msg.tool());
			if(BeanCounter.getTotalAbsoluteShopKeepersValue(msg.source(),observer)<new Integer(cost).doubleValue())
			{
			    String costStr=BeanCounter.nameCurrencyShort(observer,new Integer(cost).doubleValue());
				CommonMsgs.say(observer,source,"You'll need "+costStr+" for me to identify that.",true,false);
				return false;
			}
			return true;
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
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(!CMSecurity.isAllowed(source,source.location(),"CMROOMS"))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
		{
			int cost=cost((Item)msg.tool());
			BeanCounter.subtractMoney(source,BeanCounter.getCurrency(observer),new Integer(cost).doubleValue());
			String costStr=BeanCounter.nameCurrencyLong(observer,new Integer(cost).doubleValue());
			source.recoverEnvStats();
			FullMsg newMsg=new FullMsg(msg.source(),observer,null,CMMsg.MSG_OK_ACTION,"<S-NAME> give(s) "+costStr+" to <T-NAMESELF>.");
			msg.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,msg.tool(),null,CMMsg.MSG_EXAMINESOMETHING,"<S-NAME> examine(s) <T-NAME> very closely.");
			msg.addTrailerMsg(newMsg);
			StringBuffer up=new StringBuffer(msg.tool().name()+" is made of "+EnvResource.RESOURCE_DESCS[((Item)msg.tool()).material()&EnvResource.RESOURCE_MASK].toLowerCase()+".\n\r");
			if((msg.tool() instanceof Armor)&&(msg.tool().envStats().height()>0))
				up.append("It is a size "+msg.tool().envStats().height()+".");
			int weight=msg.tool().envStats().weight();
			if((weight!=msg.tool().baseEnvStats().weight())&&(msg.tool() instanceof Container))
				up.append("It weighs "+msg.tool().baseEnvStats().weight()+" pounds empty and "+weight+" pounds right now.\n\r");
			else
				up.append("It weighs "+weight+" pounds.\n\r");
			if(msg.tool() instanceof Weapon)
			{
				Weapon w=(Weapon)msg.tool();
				up.append("It is a "+Weapon.classifictionDescription[w.weaponClassification()].toLowerCase()+" weapon.\n\r");
				up.append("It does "+Weapon.typeDescription[w.weaponType()].toLowerCase()+" damage.\n\r");
			}
			up.append(((Item)msg.tool()).secretIdentity());
			newMsg=new FullMsg(observer,null,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) '"+up.toString()+"'^?.");
			msg.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,source,msg.tool(),CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
			msg.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,msg.tool(),null,CMMsg.MSG_DROP,null);
			msg.addTrailerMsg(newMsg);
		}
	}
}
