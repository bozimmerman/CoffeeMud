package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ItemIdentifier extends StdBehavior
{
	public String ID(){return "ItemIdentifier";}
	public Behavior newInstance()
	{
		return new ItemIdentifier();
	}
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
			Item tool=(Item)msg.tool();
			if(source.getMoney()<cost(tool))
			{
				CommonMsgs.say(observer,source,"You'll need "+cost((Item)msg.tool())+" gold coins for me to identify that.",true,false);
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
			source.setMoney(source.getMoney()-cost);
			source.recoverEnvStats();
			FullMsg newMsg=new FullMsg(msg.source(),observer,null,CMMsg.MSG_OK_ACTION,"<S-NAME> give(s) "+cost+" gold coins to <T-NAMESELF>.");
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