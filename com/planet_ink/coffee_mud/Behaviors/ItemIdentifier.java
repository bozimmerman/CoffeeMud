package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class ItemIdentifier extends StdBehavior
{
	public ItemIdentifier()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new ItemIdentifier();
	}
	private int cost(Item item)
	{
		int cost=500+(item.envStats().level()*20);
		return cost;
	}
	
	public boolean okAffect(Environmental affecting, Affect affect)
	{
		if(!super.okAffect(affecting,affect))
			return false;
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(affecting))
			return false;
		MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(affect.amITarget(observer))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&(!source.isASysOp(source.location()))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Item))
		{
			Item tool=(Item)affect.tool();
			if(source.getMoney()<cost(tool))
			{
				ExternalPlay.quickSay(observer,source,"You'll need "+cost((Item)affect.tool())+" gold coins for me to identify that.",true,false);
				return false;
			}
			return true;
		}
		return true;
	}
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every affect listed in the Affect class
	 * from the given Environmental source */
	public void affect(Environmental affecting, Affect affect)
	{
		super.affect(affecting,affect);
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(affecting))
			return;
		MOB observer=(MOB)affecting;
		
		if((source!=observer)
		&&(affect.amITarget(observer))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&(!source.isASysOp(source.location()))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Item))
		{
			int cost=cost((Item)affect.tool());
			source.setMoney(source.getMoney()-cost);
			FullMsg newMsg=new FullMsg(affect.source(),observer,null,Affect.MSG_OK_ACTION,"<S-NAME> give(s) "+cost+" gold coins to <T-NAMESELF>.");
			affect.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,affect.tool(),null,Affect.MSG_EXAMINESOMETHING,"<S-NAME> examine(s) "+affect.tool().name()+" very closely.");
			affect.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,null,null,Affect.MSG_SPEAK,"<S-NAME> say(s) '"+affect.tool().name()+" is made of "+EnvResource.RESOURCE_DESCS[((Item)affect.tool()).material()&EnvResource.RESOURCE_MASK].toLowerCase()+".\n\r"+((Item)affect.tool()).secretIdentity()+"'.");
			affect.addTrailerMsg(newMsg);
			newMsg=new FullMsg(observer,source,affect.tool(),Affect.MSG_GIVE,"<S-NAME> give(s) "+affect.tool().name()+" to <T-NAMESELF>.");
			affect.addTrailerMsg(newMsg);
		}
	}
}