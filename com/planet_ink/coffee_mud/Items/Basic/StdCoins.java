package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class StdCoins extends StdItem implements Coins
{
	public String ID(){	return "StdCoins";}
	public int value(){	return envStats().ability();}
	public StdCoins()
	{
		super();
		setName("a pile of gold coins");
		setDisplayText("some gold coins sit here.");
		myContainer=null;
		setDescription("Looks like someone left some gold sitting around.");
		myUses=Integer.MAX_VALUE;
		material=EnvResource.RESOURCE_GOLD;
		myWornCode=0;
		baseEnvStats.setWeight(0);
		recoverEnvStats();
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
		case CMMsg.TYP_GET:
		case CMMsg.TYP_REMOVE:
			if((msg.amITarget(this))||((msg.tool()==this)))
			{
				setContainer(null);
				unWear();
				destroy();
				msg.source().setMoney(msg.source().getMoney()+envStats().ability());
				if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					msg.source().location().recoverRoomStats();
			}
			break;
		default:
			break;
		}
	}
}
