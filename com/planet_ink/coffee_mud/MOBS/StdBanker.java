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
	
	public String bankChain(){return text();}
	public void setBankChain(String name){setMiscText(name);}
	
	public void addDepositInventory(MOB mob, Item thisThang)
	{
		String name=thisThang.name();
		if(thisThang instanceof Coins) name="COINS";
		ExternalPlay.DBWriteJournal(bankChain(),mob.name(),CMClass.className(thisThang),name,Generic.getPropertiesStr(thisThang,true),-1);
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
		Vector V=ExternalPlay.DBReadJournal(bankChain());
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
		Vector V=ExternalPlay.DBReadJournal(bankChain());
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
	
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		if(tickID==Host.MOB_TICK)
		{
			// ** to do = handle interest by watching the days go by...
			// put stuff up for sale if the account runs out
		}
		return true;
	}
	
	protected int getBalance(MOB mob)
	{
		Item old=findDepositInventory(mob,""+Integer.MAX_VALUE);
		if((old!=null)&&(old instanceof Coins))
			return ((Coins)old).numberOfCoins();
		return 0;
	}
	
	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
			case Affect.TYP_GIVE:
				if((affect.tool() instanceof Item)
				&&(!mob.isASysOp(mob.location())))
				{
					super.affect(affect);
					return;
				}
			case Affect.TYP_DEPOSIT:
				{
					if(affect.tool() instanceof Container)
					{
					    Vector V=((Container)affect.tool()).getContents();
					    for(int i=0;i<V.size();i++)
					    {
					   	 Item I=(Item)V.elementAt(i);
					   	 I.setContainer(null);
					    }
					}
					FullMsg msg=new FullMsg(affect.source(),affect.tool(),null,Affect.MSG_DROP,null);
					location().send(this,msg);
					msg=new FullMsg((MOB)affect.target(),affect.tool(),null,Affect.MSG_GET,null);
					location().send(this,msg);
					if(affect.tool() instanceof Coins)
					{
						Coins older=(Coins)affect.tool();
						Coins item=(Coins)CMClass.getItem("StdCoins");
						int newNum=older.numberOfCoins();
						Item old=findDepositInventory(affect.source(),""+Integer.MAX_VALUE);
						if((old!=null)&&(old instanceof Coins))
							newNum+=((Coins)old).numberOfCoins();
						item.setNumberOfCoins(newNum);
						if(old!=null)
							delDepositInventory(affect.source(),old);
						addDepositInventory(affect.source(),item);
					    ExternalPlay.quickSay(this,mob,"Ok, your new balance is: "+getBalance(affect.source())+" gold coins.",false,false);
					}
					else
					{
						addDepositInventory(affect.source(),(Item)affect.tool());
					    ExternalPlay.quickSay(this,mob,"Thank you, "+affect.tool().name()+" is safe with us.",false,false);
					}
				}
				return;
			case Affect.TYP_WITHDRAW:
				{
					Item old=(Item)affect.source();
					if(old instanceof Coins)
					{
						Item item=findDepositInventory(affect.source(),""+Integer.MAX_VALUE);
						if(item!=null)
							delDepositInventory(affect.source(),item);
						((Coins)item).setNumberOfCoins(((Coins)item).numberOfCoins()-((Coins)old).numberOfCoins());
						addDepositInventory(affect.source(),item);
					    ExternalPlay.quickSay(this,mob,"Ok, your new balance is: "+getBalance(affect.source())+" gold coins.",false,false);
					}
					else
					{
						delDepositInventory(affect.source(),old);
					    ExternalPlay.quickSay(this,mob,"Thank you for your trust.",false,false);
					}
					
					if(location()!=null)
						location().addItemRefuse(old,Item.REFUSE_PLAYER_DROP);
					FullMsg msg=new FullMsg(mob,old,this,Affect.MSG_GET,null);
					if(location().okAffect(msg))
						location().send(mob,msg);
				}
				return;
			case Affect.TYP_VALUE:
			case Affect.TYP_SELL:
				super.affect(affect);
				return;
			case Affect.TYP_BUY:
				super.affect(affect);
				return;
			case Affect.TYP_LIST:
			{
				super.affect(affect);
				Vector V=this.getDepositInventory(affect.source());
				StringBuffer msg=new StringBuffer("");
				String c="^x[Item                ] ";
				msg.append(c+c+"^^^N\n\r");
				int colNum=0;
				for(int i=0;i<V.size();i++)
				{
					Item I=(Item)inventory.elementAt(i);
					if(I instanceof Coins) continue;
					String col=null;
					col="["+Util.padRight(I.name(),20)+"] ";
					if((++colNum)>2)
					{
						msg.append("\n\r");
						colNum=1;
					}
					msg.append(col);
				}
				msg.append("\n\r^^^NYour balance with us is: ^H"+getBalance(affect.source())+".");
				ExternalPlay.quickSay(this,mob,"\n\r"+msg.toString()+"^T",true,false);
				return;
			}
			default:
				break;
			}
		}
		super.affect(affect);
	}
	
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
			case Affect.TYP_DEPOSIT:
				if(affect.tool()==null) return false;
				if(affect.tool() instanceof Coins)
					return true;
				if(!(affect.tool() instanceof Item))
				{
					mob.tell(mob.charStats().HeShe()+" doesn't look interested.");
					return false;
				}
				int balance=getBalance(affect.source());
				if(balance<((Item)affect.tool()).value())
				{
					ExternalPlay.quickSay(this,mob,"You'll need a balance of "+((Item)affect.tool()).value()+" for me to hold that.",false,false);
					return false;
				}
				return true;
			case Affect.TYP_WITHDRAW:
				if((affect.tool()==null)||(!(affect.tool() instanceof Item)))
				{
					ExternalPlay.quickSay(this,mob,"What do you want? I'm busy!",false,false);
					return false;
				}
				if(findDepositInventory(affect.source(),affect.tool().name())==null)
				{
					ExternalPlay.quickSay(this,mob,"You want WHAT?",false,false);
					return false;
				}
				return true;
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
