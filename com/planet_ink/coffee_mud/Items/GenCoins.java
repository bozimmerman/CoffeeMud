package com.planet_ink.coffee_mud.Items;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class GenCoins extends GenItem implements Coins
{
	public GenCoins()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a pile of gold coins";
		displayText="some gold coins sit here.";
		myContainer=null;
		setMaterial(EnvResource.RESOURCE_GOLD);
		description="Looks like someone left some gold sitting around.";
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
		if(((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)&&((material&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PAPER))
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
		super.affect(affect);
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
