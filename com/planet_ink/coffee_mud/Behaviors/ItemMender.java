package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ItemMender extends StdBehavior
{
	public String ID(){return "ItemMender";}
	public Behavior newInstance()
	{
		return new ItemMender();
	}
	private int cost(Item item)
	{
		int cost=((100-item.usesRemaining())*2)+item.envStats().level();
		if(Sense.isABonusItems(item))
			cost+=100+(item.envStats().level()*2);
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
			if(!tool.subjectToWearAndTear())
			{
				ExternalPlay.quickSay(observer,source,"I'm sorry, I can't work on these.",true,false);
				return false;
			}
			else
			if(tool.usesRemaining()>100)
			{
				ExternalPlay.quickSay(observer,source,"Take this thing away from me.  It's so perfect, it's scary.",true,false);
				return false;
			}
			else
			if(tool.usesRemaining()==100)
			{
				ExternalPlay.quickSay(observer,source,tool.name()+" doesn't require repair.",true,false);
				return false;
			}
			if(source.getMoney()<cost(tool))
			{
				ExternalPlay.quickSay(observer,source,"You'll need "+cost((Item)msg.tool())+" gold coins to repair that.",true,false);
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
		&&(msg.tool() instanceof Item))
		{
			int cost=cost((Item)msg.tool());
			source.setMoney(source.getMoney()-cost);
			source.recoverEnvStats();
			((Item)msg.tool()).setUsesRemaining(100);
			FullMsg newMsg=new FullMsg(observer,source,msg.tool(),CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> and "+cost+" coins to <T-NAMESELF>.");
			msg.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,source,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) 'There she is, good as new!  Thanks for your business' to <T-NAMESELF>.^?");
			msg.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,msg.tool(),null,CMMsg.MSG_DROP,null);
			msg.addTrailerMsg(newMsg);
		}
	}
}