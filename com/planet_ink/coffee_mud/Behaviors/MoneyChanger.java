package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MoneyChanger extends StdBehavior
{
	public String ID(){return "MoneyChanger";}
	public Behavior newInstance()
	{
		return new MoneyChanger();
	}
	public void startBehavior(Environmental forMe)
	{
		if(forMe==null) return;
		if(!(forMe instanceof MOB)) return;
		((MOB)forMe).baseCharStats().setStat(CharStats.STRENGTH,100);
		((MOB)forMe).baseEnvStats().setWeight(10);
		super.startBehavior(forMe);
	}
	
	public boolean okAffect(Environmental affecting, Affect affect)
	{
		if(!super.okAffect(affecting,affect))
			return false;
		MOB source=affect.source();
		if(!canFreelyBehaveNormal(affecting))
			return true;
		MOB observer=(MOB)affecting;
		if((source!=observer)
		&&(affect.amITarget(observer))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&(!source.isASysOp(source.location()))
		&&(affect.tool()!=null)
		&&(!(affect.tool() instanceof Coins)))
		{
			ExternalPlay.quickSay(observer,source,"I'm sorry, I can only accept gold coins.",true,false);
			return false;
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
		&&(Sense.canBeSeenBy(source,observer))
		&&(Sense.canBeSeenBy(observer,source))
		&&(affect.targetMinor()==Affect.TYP_GIVE)
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Coins))
		{
			int value=((Coins)affect.tool()).numberOfCoins();
			int numberToTake=1;
			if(value>20) numberToTake=value/20;
			value-=numberToTake;
			observer.setMoney(observer.getMoney()-value);
			observer.recoverEnvStats();
			if(value>0)
			{
				Banker B=(Banker)CMClass.getMOB("StdBanker");
				B.makeChange(observer,source,value);
				FullMsg newMsg=new FullMsg(observer,source,null,Affect.MSG_SPEAK,"^T<S-NAME> say(s) 'Thank you for your business' to <T-NAMESELF>.^?");
				affect.addTrailerMsg(newMsg);
			}
			else
				ExternalPlay.quickSay(observer,source,"Gee, thanks. :)",true,false);
		}
	}
}