package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class StdBanker extends StdShopKeeper implements Banker
{
	double coinInterest=0.1;
	double itemInterest=-0.1;
	
	public StdBanker()
	{
		super();
		Username="a banker";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A banker is waiting to serve you.");
		setAlignment(1000);
		setMoney(0);
		whatISell=ShopKeeper.BANKER;
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.CHARISMA,25);

		baseEnvStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}
	
	public Environmental newInstance()
	{
		return new StdBanker();
	}

	public int whatIsSold(){return ShopKeeper.BANKER;}
	public void setWhatIsSold(int newSellCode){whatISell=ShopKeeper.BANKER;}
	
	public void addDepositInventory(MOB mob, Item thisThang)
	{
		String name=thisThang.name();
		if(thisThang instanceof Coins) name="COINS";
		ExternalPlay.DBWriteJournal(name(),mob.name(),CMClass.className(thisThang),name,Generic.getPropertiesStr(thisThang,true),-1);
	};
	public void delDepositInventory(MOB mob, Item thisThang)
	{
		Vector V=getDepositInventory(mob);
		boolean money=thisThang instanceof Coins;
		for(int v=V.size()-1;v>=0;v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			String fullName=((String)V2.elementAt(4));
			if((money&&(fullName.equals("COINS")))
			||((fullName.equals(thisThang.name()))&&(((String)V2.elementAt(3)).equals(CMClass.className(thisThang)))))
				ExternalPlay.DBDeleteJournal(((String)V2.elementAt(0)),Integer.MAX_VALUE);
		}
	};
	public void delAllDeposits(MOB mob)
	{
		Vector V=ExternalPlay.DBReadJournal(name());
		int num=0;
		for(int v=V.size()-1;v>=0;v--)
		{
			Vector V2=(Vector)V.elementAt(v);
			if(((String)V2.elementAt(1)).equalsIgnoreCase(mob.name()))
				ExternalPlay.DBDeleteJournal(((String)V2.elementAt(0)),Integer.MAX_VALUE);
		}
	};
	public int numberDeposited(MOB mob)
	{
		return getDepositInventory(mob).size();
	};
	public Vector getDepositInventory(MOB mob)
	{
		Vector V=ExternalPlay.DBReadJournal(name());
		Vector mine=new Vector();
		int num=0;
		for(int v=0;v<V.size();v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			if(((String)V2.elementAt(1)).equalsIgnoreCase(mob.name()))
				mine.addElement(V2);
		}
		return mine;
	};
	public Item findDepositInventory(MOB mob, String likeThis)
	{
		Vector V=getDepositInventory(mob);
		boolean money=Util.s_int(likeThis)>0;
		for(int v=0;v<V.size();v++)
		{
			Vector V2=(Vector)V.elementAt(v);
			String fullName=((String)V2.elementAt(4));
			if((money&&(fullName.equals("COINS")))
			||(CoffeeUtensils.containsString(fullName,likeThis)))
			{
				Item I=CMClass.getItem(((String)V2.elementAt(3)));
				if(I!=null)
				{
					Generic.setPropertiesStr(I,((String)V2.elementAt(5)),true);
					return I;
				}
			}
		}
		return null;
	};
	public void setCoinInterest(double interest){coinInterest=interest;};
	public void setItemInterest(double interest){itemInterest=interest;};
	public double getCoinInterest(){return coinInterest;};
	public double getItemInterest(){return itemInterest;};
	
	public boolean okAffect(Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_GIVE:
				if(affect.tool()==null) return false;
				if((affect.tool() instanceof Item)
				&&(!mob.isASysOp(mob.location())))
					return true;
				if(affect.tool() instanceof Coins)
					return true;
				if(!(affect.tool() instanceof Item))
				{
					mob.tell(mob.charStats().HeShe()+" doesn't look interested.");
					return false;
				}
				break;
			case Affect.TYP_VALUE:
			case Affect.TYP_SELL:
				return super.okAffect(affect);
			case Affect.TYP_BUY:
				return super.okAffect(affect);
			case Affect.TYP_LIST:
			{
				if(numberDeposited(affect.source())==0)
				{
					ExternalPlay.quickSay(this,mob,"You are not presently a customer with us, I'm afraid.",false,false);
					return false;
				}
				else
					return true;
			}
			default:
				break;
			}
		}
		return super.okAffect(affect);
	}
}
