package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class MoneyChanger extends StdBehavior
{
	public MoneyChanger()
	{
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
	}
	public Behavior newInstance()
	{
		return new MoneyChanger();
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
			if(value>0)
			{
				Container changeBag=(Container)CMClass.getItem("GenContainer");
				changeBag.setCapacity(0);
				changeBag.baseEnvStats().setWeight(1);
				changeBag.setBaseValue(0);
				changeBag.setLidsNLocks(false,true,false,false);
				changeBag.setName("a change bag");
				changeBag.setDisplayText("a small crumbled bag lies here.");
				changeBag.setMaterial(Item.CLOTH);
				changeBag.setDescription("This bag is provided courtesy of CoffeeMud Savings and Loan.");
				observer.addInventory(changeBag);
				int totalWeight=0;
				while(value>=100)
				{
					value-=100;
					Coins msliver=(Coins)CMClass.getItem("MithrilNote");
					totalWeight++;
					observer.addInventory(msliver);
					msliver.setLocation(changeBag);
				}
				while(value>=10)
				{
					value-=10;
					Coins msliver=(Coins)CMClass.getItem("PlatinumNote");
					totalWeight++;
					observer.addInventory(msliver);
					msliver.setLocation(changeBag);
				}
				if(value>0)
				{
					totalWeight+=value;
					Coins rest=(Coins)CMClass.getItem("StdCoins");
					rest.setNumberOfCoins(value);
					observer.addInventory(rest);
					rest.setLocation(changeBag);
				}
				changeBag.setCapacity(value);
				changeBag.recoverEnvStats();
				changeBag.text();
				FullMsg newMsg=new FullMsg(observer,source,changeBag,Affect.MSG_GIVE,"<S-NAME> give(s) "+changeBag.name()+" to <T-NAMESELF>.");
				affect.addTrailerMsg(newMsg);
				newMsg=new FullMsg(observer,source,changeBag,Affect.MSG_SPEAK,"<S-NAME> say(s) 'Thank you for your business' to <T-NAMESELF>.");
				affect.addTrailerMsg(newMsg);
			}
			else
				ExternalPlay.quickSay(observer,source,"Gee, thanks. :)",true,false);
		}
	}
}