package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.*;
import java.util.*;

public class ShopKeepers
{
	private ShopKeepers(){}
	
	private static ShopKeeper shopkeeper(Room here, MOB mob)
	{
		for(int i=0;i<here.numInhabitants();i++)
		{
			MOB thisMOB=here.fetchInhabitant(i);
			if((thisMOB!=null)&&(thisMOB instanceof ShopKeeper)&&(Sense.canBeSeenBy(thisMOB,mob)))
				return (ShopKeeper)thisMOB;
		}
		return null;
	}

	public static void sell(MOB mob, Vector commands)
	{
		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Sell what to whom?");
				return;
			}
			commands.removeElementAt(0);
			shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(shopkeeper instanceof ShopKeeper)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' buying anything.");
				return;
			}
			commands.removeElementAt(commands.size()-1);
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Sell what?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thisName.toUpperCase().startsWith("ALL.")){ allFlag=true; thisName="ALL "+thisName.substring(4);}
		if(thisName.toUpperCase().endsWith(".ALL")){ allFlag=true; thisName="ALL "+thisName.substring(0,thisName.length()-4);}
		do
		{
			Environmental thisThang=null;
			thisThang=mob.fetchCarried(null,thisName);
			if(thisThang==null)
				thisThang=mob.fetchFollower(thisName);
			if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't see '"+thisName+"' here.");
				return;
			}
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_SELL,"<S-NAME> sell(s) "+thisThang.name()+" to <T-NAMESELF>.");
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}


	public static void value(MOB mob, Vector commands)
	{
		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Value what with whom?");
				return;
			}
			commands.removeElementAt(0);
			shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(shopkeeper instanceof ShopKeeper)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' buying anything.");
				return;
			}
			commands.removeElementAt(commands.size()-1);
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Value what?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thisName.toUpperCase().startsWith("ALL.")){ allFlag=true; thisName="ALL "+thisName.substring(4);}
		if(thisName.toUpperCase().endsWith(".ALL")){ allFlag=true; thisName="ALL "+thisName.substring(0,thisName.length()-4);}
		do
		{
			Environmental thisThang=null;
			thisThang=mob.fetchInventory(thisName);
			if(thisThang==null)
				thisThang=mob.fetchFollower(thisName);
			if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't see '"+thisName+"' here.");
				return;
			}
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_VALUE,null);
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public static void view(MOB mob, Vector commands)
	{
		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("View what merchandise from whom?");
				return;
			}
			commands.removeElementAt(0);
			shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(shopkeeper instanceof ShopKeeper)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' selling anything.");
				return;
			}
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("View what merchandise?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thisName.toUpperCase().startsWith("ALL.")){ allFlag=true; thisName="ALL "+thisName.substring(4);}
		if(thisName.toUpperCase().endsWith(".ALL")){ allFlag=true; thisName="ALL "+thisName.substring(0,thisName.length()-4);}
		if(!(shopkeeper instanceof ShopKeeper))
		{
			mob.tell(shopkeeper.name()+" is not a shopkeeper!");
			return;
		}
		do
		{
			Environmental thisThang=((ShopKeeper)shopkeeper).getStock(thisName,mob);
			if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				if(!doneSomething)
					mob.tell("There doesn't appear to be any for sale.  Try LIST.");
				return;
			}
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_VIEW,null);
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public static void buy(MOB mob, Vector commands)
	{
		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Buy what from whom?");
				return;
			}
			commands.removeElementAt(0);
			shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(shopkeeper instanceof ShopKeeper)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' selling anything.");
				return;
			}
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Buy what?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thisName.toUpperCase().startsWith("ALL.")){ allFlag=true; thisName="ALL "+thisName.substring(4);}
		if(thisName.toUpperCase().endsWith(".ALL")){ allFlag=true; thisName="ALL "+thisName.substring(0,thisName.length()-4);}
		if(!(shopkeeper instanceof ShopKeeper))
		{
			mob.tell(shopkeeper.name()+" is not a shop keeper!");
			return;
		}
		do
		{
			Environmental thisThang=((ShopKeeper)shopkeeper).getStock(thisName,mob);
			if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				if(!doneSomething)
					mob.tell("There doesn't appear to be any for sale.  Try LIST.");
				return;
			}
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_BUY,"<S-NAME> buy(s) "+thisThang.name()+" from <T-NAMESELF>.");
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public static void deposit(MOB mob, Vector commands)
	{
		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Deposit how much with whom?");
				return;
			}
			commands.removeElementAt(0);
			shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(shopkeeper instanceof Banker)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' running a bank.");
				return;
			}
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Deposit what or how much?");
				return;
			}
			commands.removeElementAt(0);
		}
		String thisName=Util.combine(commands,0);
		Item thisThang=SocialProcessor.possibleGold(mob,thisName);
		if(thisThang==null)
		{
			thisThang=mob.fetchCarried(null,thisName);
			if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
			{
				mob.tell("You don't seem to be carrying that.");
				return;
			}
		}
		FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_DEPOSIT,"<S-NAME> deposit(s) "+thisThang.name()+" into <S-HIS-HER> account with <T-NAMESELF>.");
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}
	
	public static void withdraw(MOB mob, Vector commands)
	{
		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<3)
			{
				mob.tell("Withdraw how much from whom?");
				return;
			}
			commands.removeElementAt(0);
			shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(shopkeeper instanceof Banker)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' holding your money.");
				return;
			}
		}
		else
		{
			if(commands.size()<2)
			{
				mob.tell("Withdraw what or how much?");
				return;
			}
			commands.removeElementAt(0);
		}
		String str=(String)commands.firstElement();
		if(((String)commands.lastElement()).equalsIgnoreCase("coins"))
		{
			if((!str.equalsIgnoreCase("all"))
			&&(Util.s_int((String)commands.firstElement())<=0))
			{
				mob.tell("Withdraw how much?");
				return;
			}
			
			commands.removeElement(commands.lastElement());
		}
		if(((String)commands.lastElement()).equalsIgnoreCase("gold"))
		{
			if((!str.equalsIgnoreCase("all"))
			&&(Util.s_int((String)commands.firstElement())<=0))
			{
				mob.tell("Withdraw how much?");
				return;
			}
			commands.removeElement(commands.lastElement());
		}
		   
		String thisName=Util.combine(commands,0);
		Item thisThang=null;
		if(thisName.equalsIgnoreCase("all"))
			thisThang=((Banker)shopkeeper).findDepositInventory(mob.name(),""+Integer.MAX_VALUE);
		else
			thisThang=((Banker)shopkeeper).findDepositInventory(mob.name(),thisName);
		if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
		{
			mob.tell("That doesn't appear to be available.  Try LIST.");
			return;
		}
		if(thisThang instanceof Coins)
		{
			Coins oldThang=(Coins)thisThang;
			if(!thisName.equalsIgnoreCase("all"))
			{
				thisThang=(Item)oldThang.copyOf();
				((Coins)thisThang).setNumberOfCoins(Util.s_int(thisName));
				if(((Coins)thisThang).numberOfCoins()<=0)
				{
					mob.tell("Withdraw how much?");
					return;
				}
			}
		}
		FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_WITHDRAW,"<S-NAME> withdraw(s) "+thisThang.name()+" from <S-HIS-HER> account with "+shopkeeper.name()+".");
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}
	
	public static void list(MOB mob, Vector commands)
	{
		MOB shopkeeper=shopkeeper(mob.location(),mob);
		if(shopkeeper==null)
		{
			if(commands.size()<2)
			{
				if(mob.isASysOp(mob.location()))
					mob.tell("List what or from whom?");
				else
					mob.tell("List from whom?");
				return;
			}
			commands.removeElementAt(0);
			shopkeeper=mob.location().fetchInhabitant(Util.combine(commands,0));
			if((shopkeeper==null)||(!(shopkeeper instanceof ShopKeeper))||(!Sense.canBeSeenBy(shopkeeper,mob)))
			{
				if(mob.isASysOp(mob.location()))
					Lister.list(mob,commands);
				else
					mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' selling anything.");
				return;
			}
		}
		FullMsg newMsg=new FullMsg(mob,shopkeeper,null,Affect.MSG_LIST,null);
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}

	
	
}
