package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdCoins extends StdItem implements Coins
{
	public String ID(){	return "StdCoins";}
	public int value(){	return envStats().ability();}
	public StdCoins()
	{
		super();
		name="a pile of gold coins";
		displayText="some gold coins sit here.";
		myContainer=null;
		description="Looks like someone left some gold sitting around.";
		myUses=Integer.MAX_VALUE;
		material=EnvResource.RESOURCE_GOLD;
		myWornCode=0;
		miscText="";
		baseEnvStats.setWeight(0);
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new StdCoins();
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
	public int numberOfCoins(){return envStats().ability();}
	public void setNumberOfCoins(int number){baseEnvStats().setAbility(number); recoverEnvStats();}
	public String displayText()
	{
		if(envStats().ability()==1)
			return name()+" sits here.";
		else
			return name()+" sit here.";
	}
	public void recoverEnvStats()
	{
		baseEnvStats.setWeight((int)Math.round((new Integer(baseEnvStats().ability()).doubleValue()/100.0)));
		envStats=baseEnvStats.cloneStats();
	}

	public boolean putCoinsBack()
	{
		Coins alternative=null;
		if(owner() instanceof Room)
		{
			Room R=(Room)owner();
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				   &&(I!=this)
				   &&(I instanceof Coins)
				   &&(I.container()==container()))
				{
					alternative=(Coins)I;
					break;
				}
			}
		}
		else
		if(owner() instanceof MOB)
		{
			MOB M=(MOB)owner();
			if(container()==null)
			{
				M.setMoney(M.getMoney()+numberOfCoins());
				destroyThis();
				return true;
			}
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				   &&(I!=this)
				   &&(I instanceof Coins)
				   &&(I.container()==container()))
				{
					alternative=(Coins)I;
					break;
				}
			}
		}
		if(alternative!=null)
		{
			alternative.setNumberOfCoins(alternative.numberOfCoins()+numberOfCoins());
			destroyThis();
			return true;
		}
		return false;
	}

	public void affect(Environmental myHost, Affect affect)
	{
		super.affect(myHost,affect);
		switch(affect.targetMinor())
		{
		case Affect.TYP_GET:
			if((affect.amITarget(this))||((affect.tool()==this)))
			{
				setContainer(null);
				remove();
				destroyThis();
				affect.source().setMoney(affect.source().getMoney()+envStats().ability());
				affect.source().location().recoverRoomStats();
			}
			break;
		default:
			break;
		}
	}
}
