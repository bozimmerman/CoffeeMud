package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class SysopItemUsage
{
	private SysopItemUsage(){}

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

	public static void take(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Take what from whom?");
			return;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("From whom should I take the "+(String)commands.elementAt(0));
			return;
		}

		MOB victim=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
		if((victim==null)||((victim!=null)&&(!Sense.canBeSeenBy(victim,mob))))
		{
			mob.tell("I don't see anyone called "+(String)commands.elementAt(commands.size()-1)+" here.");
			return;
		}
		commands.removeElementAt(commands.size()-1);
		if((commands.size()>0)&&(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("from")))
			commands.removeElementAt(commands.size()-1);

		int maxToGive=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(Util.s_int((String)commands.firstElement())>0))
		{
			maxToGive=Util.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}
		
		String thingToGive=Util.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
		do
		{
			Environmental giveThis=possibleGold(victim,thingToGive);
			if(giveThis!=null)
				allFlag=false;
			else
				giveThis=victim.fetchCarried(null,thingToGive+addendumStr);
			if((giveThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
				giveThis=victim.fetchWornItem(thingToGive);
			if(giveThis==null) break;
			if(giveThis instanceof Item)
			{
				((Item)giveThis).unWear();
				V.addElement(giveThis);
			}
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToGive));

		if(V.size()==0)
			mob.tell(victim.name()+" does not seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			Item giveThis=(Item)V.elementAt(i);
			FullMsg newMsg=new FullMsg(victim,mob,giveThis,Affect.MASK_GENERAL|Affect.MSG_GIVE,"<T-NAME> take(s) <O-NAME> from <S-NAMESELF>.");
			if(victim.location().okAffect(victim,newMsg))
				victim.location().send(victim,newMsg);
			if(!mob.isMine(giveThis)) mob.giveItem(giveThis);
		}
	}
}
