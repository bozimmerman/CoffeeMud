package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class BaseItemParser extends StdCommand
{
	public static Item possibleGold(MOB mob, String itemID)
	{
		if(itemID.toUpperCase().trim().endsWith(" COINS"))
			itemID=itemID.substring(0,itemID.length()-6);
		if(itemID.toUpperCase().trim().endsWith(" GOLD"))
			itemID=itemID.substring(0,itemID.length()-5);
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		int gold=Util.s_int(itemID);
		if(gold>0)
		{
			if(mob.getMoney()>=gold)
			{
				mob.setMoney(mob.getMoney()-gold);
				Item C=(Item)CMClass.getItem("StdCoins");
				C.baseEnvStats().setAbility(gold);
				C.recoverEnvStats();
				mob.addInventory(C);
				return C;
			}
			else
				mob.tell("You don't have that much gold.");
		}
		return null;
	}
	
	public static Vector possibleContainers(MOB mob, Vector commands, int wornReqCode)
	{
		Vector V=new Vector();
		if(commands.size()==1)
			return V;

		String possibleContainerID=(String)commands.elementAt(commands.size()-1);
		boolean allFlag=false;
		String preWord="";
		if(possibleContainerID.equalsIgnoreCase("all"))
			allFlag=true;
		else
		if(commands.size()>2)
			preWord=(String)commands.elementAt(commands.size()-2);

		int maxContained=Integer.MAX_VALUE;
		if(Util.s_int(preWord)>0)
		{
			maxContained=Util.s_int(preWord);
			commands.setElementAt("all",commands.size()-2);
			preWord="all";
		}

		if(preWord.equalsIgnoreCase("all")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID;}
		else
		if(possibleContainerID.toUpperCase().startsWith("ALL.")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(4);}
		else
		if(possibleContainerID.toUpperCase().endsWith(".ALL")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(0,possibleContainerID.length()-4);}

		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID+addendumStr,wornReqCode);
			if((thisThang!=null)
			&&(thisThang instanceof Item)
			&&(Sense.canBeSeenBy(thisThang,mob))
			&&(((Item)thisThang) instanceof Container)
			&&(((Container)thisThang).getContents().size()>0))
			{
				V.addElement(thisThang);
				if(V.size()==1)
				{
					commands.removeElementAt(commands.size()-1);
					if(allFlag&&(preWord.equalsIgnoreCase("all")))
						commands.removeElementAt(commands.size()-1);
					else
					if((!allFlag)&&(preWord.equalsIgnoreCase("from")))
						commands.removeElementAt(commands.size()-1);
					preWord="";
				}
			}
			if(thisThang==null) return V;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxContained));
		return V;
	}
	
	public static int numPossibleGold(String itemID)
	{
		if(itemID.toUpperCase().trim().endsWith(" COINS"))
			itemID=itemID.substring(0,itemID.length()-6);
		if(itemID.toUpperCase().trim().endsWith(" GOLD"))
			itemID=itemID.substring(0,itemID.length()-5);
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		int gold=Util.s_int(itemID);
		return gold;
	}

	public static Item possibleContainer(MOB mob, Vector commands, boolean withStuff, int wornReqCode)
	{
		if(commands.size()==1)
			return null;

		String possibleContainerID=(String)commands.elementAt(commands.size()-1);
		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,wornReqCode);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang) instanceof Container)
		&&((!withStuff)||(((Container)thisThang).getContents().size()>0)))
		{
			commands.removeElementAt(commands.size()-1);
			return (Item)thisThang;
		}
		return null;
	}

	public static Vector fetchItemList(Environmental from,
								MOB mob,
								Item container,
								Vector commands,
								int preferredLoc,
								boolean visionMatters)
	{
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();

		int maxToItem=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToItem=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}


		String name=Util.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(name.toUpperCase().startsWith("ALL.")){ allFlag=true; name="ALL "+name.substring(4);}
		if(name.toUpperCase().endsWith(".ALL")){ allFlag=true; name="ALL "+name.substring(0,name.length()-4);}
		do
		{
			Environmental item=null;
			if(from instanceof MOB)
			{
				if(preferredLoc==Item.WORN_REQ_UNWORNONLY)
					item=((MOB)from).fetchCarried(container,name+addendumStr);
				else
				if(preferredLoc==Item.WORN_REQ_WORNONLY)
					item=((MOB)from).fetchWornItem(name+addendumStr);
				else
					item=((MOB)from).fetchInventory(name+addendumStr);
			}
			else
			if(from instanceof Room)
				item=((Room)from).fetchFromMOBRoomFavorsItems(mob,container,name+addendumStr,preferredLoc);
			if((item!=null)
			&&(item instanceof Item)
			&&((!visionMatters)||(Sense.canBeSeenBy(item,mob)))
			&&(!V.contains(item)))
				V.addElement(item);
			if(item==null) return V;
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToItem));
		return V;
	}
}
