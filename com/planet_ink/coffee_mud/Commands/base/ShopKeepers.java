package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import java.util.*;

public class ShopKeepers
{
	private ShopKeepers(){}

	private static Vector shopkeepers(MOB mob)
	{
		if(mob==null) return null;
		Room here=mob.location();
		if(here==null) return null;
		Vector V=new Vector();
		for(int i=0;i<here.numInhabitants();i++)
		{
			MOB thisMOB=here.fetchInhabitant(i);
			if((thisMOB!=null)
			&&(thisMOB!=mob)
			&&(CoffeeUtensils.getShopKeeper(thisMOB)!=null)
			&&(Sense.canBeSeenBy(thisMOB,mob)))
				V.addElement(thisMOB);
		}
		return V;
	}

	public static MOB parseShopkeeper(MOB mob, Vector commands, String error)
	{
		if(commands.size()==0)
		{
			mob.tell(error);
			return null;
		}
		commands.removeElementAt(0);

		Vector V=shopkeepers(mob);
		if(V.size()==0)
		{
			mob.tell(error);
			return null;
		}
		if(V.size()>1)
		{
			if(commands.size()<2)
			{
				mob.tell(error);
				return null;
			}
			MOB shopkeeper=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
			if((shopkeeper!=null)&&(CoffeeUtensils.getShopKeeper(shopkeeper)!=null)&&(Sense.canBeSeenBy(shopkeeper,mob)))
				commands.removeElementAt(commands.size()-1);
			else
			{
				mob.tell("You don't see anyone called '"+(String)commands.elementAt(commands.size()-1)+"' here buying or selling.");
				return null;
			}
			return shopkeeper;
		}
		else
		{
			MOB shopkeeper=(MOB)V.firstElement();
			if(commands.size()>1)
			{
				MOB M=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
				if((M!=null)&&(CoffeeUtensils.getShopKeeper(M)!=null)&&(Sense.canBeSeenBy(M,mob)))
				{
					shopkeeper=M;
					commands.removeElementAt(commands.size()-1);
				}
			}
			return shopkeeper;
		}
	}


	public static boolean doesOwnThisProperty(MOB mob, Room room)
	{
		String titleInName="";
		if((room==null)||(mob==null)) return false;
		for(int a=0;a<room.numAffects();a++)
		{
			Ability A=room.fetchAffect(a);
			if((A!=null)&&(A instanceof LandTitle))
			{ titleInName=((LandTitle)A).landOwner(); break;}
		}
		if(titleInName==null) return false;
		if(titleInName.length()==0) return false;
		if(titleInName.equals(mob.Name())) return true;
		if(titleInName.equals(mob.getClanID()))
		{
			if((mob.getClanRole()==Clan.POS_LEADER)
			||(mob.getClanRole()==Clan.POS_BOSS))
				return true;
		}
		return false;
	}

	public static void sell(MOB mob, Vector commands)
	{
		MOB shopkeeper=parseShopkeeper(mob,commands,"Sell what to whom?");
		if(shopkeeper==null) return;
		if(commands.size()==0)
		{
			mob.tell("Sell what?");
			return;
		}

		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToDo=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}
		
		String whatName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Item itemToDo=mob.fetchCarried(null,whatName+addendumStr);
			if(itemToDo==null) break;
			if((Sense.canBeSeenBy(itemToDo,mob))
			&&(!V.contains(itemToDo)))
				V.addElement(itemToDo);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDo));
		
		if(V.size()==0)
			mob.tell("You don't seem to have '"+whatName+"'.");
		else
		for(int v=0;v<V.size();v++)
		{
			Item thisThang=(Item)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_SELL,"<S-NAME> sell(s) <O-NAME> to <T-NAMESELF>.");
			if(mob.location().okAffect(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
	}


	public static void value(MOB mob, Vector commands)
	{
		MOB shopkeeper=parseShopkeeper(mob,commands,"Value what with whom?");
		if(shopkeeper==null) return;
		if(commands.size()==0)
		{
			mob.tell("Value what?");
			return;
		}
		
		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToDo=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}
		
		String whatName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Item itemToDo=mob.fetchCarried(null,whatName+addendumStr);
			if(itemToDo==null) break;
			if((Sense.canBeSeenBy(itemToDo,mob))
			&&(!V.contains(itemToDo)))
				V.addElement(itemToDo);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDo));
		
		if(V.size()==0)
			mob.tell("You don't seem to have '"+whatName+"'.");
		else
		for(int v=0;v<V.size();v++)
		{
			Item thisThang=(Item)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_VALUE,null);
			if(mob.location().okAffect(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
	}

	public static void view(MOB mob, Vector commands)
	{
		MOB shopkeeper=parseShopkeeper(mob,commands,"View what merchandise from whom?");
		if(shopkeeper==null) return;
		if(commands.size()==0)
		{
			mob.tell("View what merchandise?");
			return;
		}
		
		if(CoffeeUtensils.getShopKeeper(shopkeeper)==null)
		{
			mob.tell(shopkeeper.name()+" is not a shopkeeper!");
			return;
		}
		
		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToDo=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}
		
		String whatName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental itemToDo=CoffeeUtensils.getShopKeeper(shopkeeper).getStock(whatName,mob);
			if(itemToDo==null) break;
			if(Sense.canBeSeenBy(itemToDo,mob))
				V.addElement(itemToDo);
			if(addendum>=CoffeeUtensils.getShopKeeper(shopkeeper).numberInStock(itemToDo))
				break;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDo));
		
		if(V.size()==0)
			mob.tell(shopkeeper,null,null,"<S-NAME> doesn't appear to have any '"+whatName+"' for sale.  Try LIST.");
		else
		for(int v=0;v<V.size();v++)
		{
			Environmental thisThang=(Environmental)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_VIEW,null);
			if(mob.location().okAffect(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
	}

	public static void buy(MOB mob, Vector commands)
	{
		MOB shopkeeper=parseShopkeeper(mob,commands,"Buy what from whom?");
		if(shopkeeper==null) return;
		if(commands.size()==0)
		{
			mob.tell("Buy what?");
			return;
		}
		if(CoffeeUtensils.getShopKeeper(shopkeeper)==null)
		{
			mob.tell(shopkeeper.name()+" is not a shopkeeper!");
			return;
		}
		
		int maxToDo=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToDo=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}
		
		String whatName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatName.toUpperCase().startsWith("ALL.")){ allFlag=true; whatName="ALL "+whatName.substring(4);}
		if(whatName.toUpperCase().endsWith(".ALL")){ allFlag=true; whatName="ALL "+whatName.substring(0,whatName.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental itemToDo=CoffeeUtensils.getShopKeeper(shopkeeper).getStock(whatName,mob);
			if(itemToDo==null) break;
			if(Sense.canBeSeenBy(itemToDo,mob))
				V.addElement(itemToDo);
			if(addendum>=CoffeeUtensils.getShopKeeper(shopkeeper).numberInStock(itemToDo))
				break;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToDo));
		
		if(V.size()==0)
			mob.tell(shopkeeper,null,null,"<S-NAME> doesn't appear to have any '"+whatName+"' for sale.  Try LIST.");
		else
		for(int v=0;v<V.size();v++)
		{
			Environmental thisThang=(Environmental)V.elementAt(v);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_BUY,"<S-NAME> buy(s) <O-NAME> from <T-NAMESELF>.");
			if(mob.location().okAffect(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
	}



	public static void deposit(MOB mob, Vector commands)
	{
		MOB shopkeeper=parseShopkeeper(mob,commands,"Deposit how much with whom?");
		if(shopkeeper==null) return;
		if(!(shopkeeper instanceof Banker))
		{
			mob.tell("You can not deposit anything with "+shopkeeper.name()+".");
			return;
		}
		if(commands.size()==0)
		{
			mob.tell("Deposit what or how much?");
			return;
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
		FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_DEPOSIT,"<S-NAME> deposit(s) <O-NAME> into <S-HIS-HER> account with <T-NAMESELF>.");
		if(!mob.location().okAffect(mob,newMsg))
			return;
		mob.location().send(mob,newMsg);
	}



	public static void withdraw(MOB mob, Vector commands)
	{
		MOB shopkeeper=parseShopkeeper(mob,commands,"Withdraw how much from whom?");
		if(shopkeeper==null) return;
		if(!(shopkeeper instanceof Banker))
		{
			mob.tell("You can not withdraw anything from "+shopkeeper.name()+".");
			return;
		}
		if(commands.size()==0)
		{
			mob.tell("Withdraw what or how much?");
			return;
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
			thisThang=((Banker)shopkeeper).findDepositInventory(mob.Name(),""+Integer.MAX_VALUE);
		else
			thisThang=((Banker)shopkeeper).findDepositInventory(mob.Name(),thisName);
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
		FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,Affect.MSG_WITHDRAW,"<S-NAME> withdraw(s) <O-NAME> from <S-HIS-HER> account with "+shopkeeper.name()+".");
		if(!mob.location().okAffect(mob,newMsg))
			return;
		mob.location().send(mob,newMsg);
	}



	public static void list(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		Vector V=new Vector();
		if(commands.size()==0)
			V=shopkeepers(mob);
		else
		{
			MOB shopkeeper=mob.location().fetchInhabitant(Util.combine(commands,0));
			if((shopkeeper==null)||(CoffeeUtensils.getShopKeeper(shopkeeper)==null)||(!Sense.canBeSeenBy(shopkeeper,mob)))
			{
				if(mob.isASysOp(mob.location()))
				{
					Lister.list(mob,commands);
					return;
				}
			}
			else
				V.addElement(shopkeeper);
		}
		if(V.size()==0)
		{
			mob.tell("You don't see anyone here buying or selling.");
			return;
		}
		for(int i=0;i<V.size();i++)
		{
			MOB shopkeeper=(MOB)V.elementAt(i);
			FullMsg newMsg=new FullMsg(mob,shopkeeper,null,Affect.MSG_LIST,null);
			if(!mob.location().okAffect(mob,newMsg))
				return;
			mob.location().send(mob,newMsg);
		}
	}



}
