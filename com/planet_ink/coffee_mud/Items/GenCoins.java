package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCoins extends GenItem implements Coins
{
	public String ID(){	return "GenCoins";}
	public int value(){	return envStats().ability();}
	public GenCoins()
	{
		super();
		name="a pile of gold coins";
		displayText="some gold coins sit here.";
		myContainer=null;
		setMaterial(EnvResource.RESOURCE_GOLD);
		description="";
		isReadable=false;
	}

	public Environmental newInstance()
	{
		return new GenCoins();
	}
	
	public int numberOfCoins(){return envStats().ability();}
	public void setNumberOfCoins(int number){baseEnvStats().setAbility(number); recoverEnvStats();}
	
	public boolean isGeneric(){return true;}
	public void recoverEnvStats()
	{
		if(((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)
		&&((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PAPER))
			baseEnvStats.setWeight((int)Math.round((new Integer(baseEnvStats().ability()).doubleValue()/100.0)));
		envStats=baseEnvStats.cloneStats();
		// import not to sup this, otherwise 'ability' makes it magical!
		for(int a=0;a<affects.size();a++)
		{
			Ability affect=(Ability)affects.elementAt(a);
			affect.affectEnvStats(this,envStats);
		}
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
				affect.source().setMoney(affect.source().getMoney()+envStats().ability());
				unWear();
				destroyThis();
				affect.source().location().recoverRoomStats();
			}
			break;
		default:
			break;
		}
	}
}
