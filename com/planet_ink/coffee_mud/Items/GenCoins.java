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
		setName("a pile of gold coins");
		setDisplayText("some gold coins sit here.");
		myContainer=null;
		setMaterial(EnvResource.RESOURCE_GOLD);
		setDescription("");
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
		for(int a=0;a<numEffects();a++)
		{
			Ability effect=fetchEffect(a);
			effect.affectEnvStats(this,envStats);
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
				destroy();
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
			destroy();
			return true;
		}
		return false;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_REMOVE:
		case CMMsg.TYP_GET:
			if((msg.amITarget(this))||((msg.tool()==this)))
			{
				setContainer(null);
				msg.source().setMoney(msg.source().getMoney()+envStats().ability());
				unWear();
				destroy();
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					msg.source().location().recoverRoomStats();
			}
			break;
		default:
			break;
		}
	}
}
