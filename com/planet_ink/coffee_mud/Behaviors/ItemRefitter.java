package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ItemRefitter extends StdBehavior
{
	public String ID(){return "ItemRefitter";}
	public Behavior newInstance()
	{
		return new ItemRefitter();
	}
	private int cost(Item item)
	{
		int cost=item.envStats().level()*100;
		if(Sense.isABonusItems(item))
			cost+=(item.envStats().level()*100);
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
		&&(!source.isASysOp(source.location()))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Item))
		{
			Item tool=(Item)msg.tool();
			if(!(tool instanceof Armor))
			{
				ExternalPlay.quickSay(observer,source,"I'm sorry, I can't refit that.",true,false);
				return false;
			}

			if(tool.baseEnvStats().height()==0)
			{
				ExternalPlay.quickSay(observer,source,"This already looks your size!",true,false);
				return false;
			}
			if(source.getMoney()<cost(tool))
			{
				ExternalPlay.quickSay(observer,source,"You'll need "+cost((Item)msg.tool())+" gold coins to refit that.",true,false);
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
		&&(!source.isASysOp(source.location()))
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Armor))
		{
			int cost=cost((Item)msg.tool());
			source.setMoney(source.getMoney()-cost);
			source.recoverEnvStats();
			((Item)msg.tool()).baseEnvStats().setHeight(0);
			((Item)msg.tool()).recoverEnvStats();

			FullMsg newMsg=new FullMsg(observer,source,msg.tool(),CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> and "+cost+" coins to <T-NAMESELF>.");
			msg.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,source,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'There she is, a perfect fit!  Thanks for your business' to <T-NAMESELF>.^?");
			msg.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,msg.tool(),null,CMMsg.MSG_DROP,null);
			msg.addTrailerMsg(newMsg);
		}
	}
}