package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCoins extends GenItem
{
	public GenCoins()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pile of gold coins";
		displayText="some gold coins sit here.";
		myLocation=null;
		description="Looks like someone left some gold sitting around.";
		isReadable=false;
	}

	public Environmental newInstance()
	{
		return new GenCoins();
	}
	public String name()
	{
		if(envStats().ability()==1)
			return "a gold coin";
		else
		if(envStats().ability()==2)
			return "two gold coins";
		else
			return "a pile of "+envStats().ability()+" gold coins";
	}
	
	public String displayText()
	{
		if(envStats().ability()==1)
			return name()+" sits here.";
		else
			return name()+" sit here.";
	}
	
	public boolean isGeneric(){return true;}
	public void recoverEnvStats()
	{
		baseEnvStats.setWeight((int)Math.round((new Integer(baseEnvStats().ability()).doubleValue()/100.0)));
		envStats=baseEnvStats.cloneStats();
		goldValue=envStats().ability();
		// import not to sup this, otherwise 'ability' makes it magical!
		for(int a=0;a<affects.size();a++)
		{
			Ability affect=(Ability)affects.elementAt(a);
			affect.affectEnvStats(this,envStats);
		}
	}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_GET:
				{
				setLocation(null);
				if(mob.location().isContent(this))
					destroyThis();
				if(!mob.isMine(this))
					mob.setMoney(mob.getMoney()+envStats().ability());
				remove();
				mob.location().recoverRoomStats();
				return;
				}
			default:
				break;
			}
		}
		super.affect(affect);
	}
}
