package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Deposit extends BaseItemParser
{
	public Deposit(){}

	private String[] access={"DEPOSIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		MOB shopkeeper=EnglishParser.parseShopkeeper(mob,commands,"Deposit how much with whom?");
		if(shopkeeper==null) return false;
		if(!(shopkeeper instanceof Banker))
		{
			mob.tell("You can not deposit anything with "+shopkeeper.name()+".");
			return false;
		}
		if(commands.size()==0)
		{
			mob.tell("Deposit what or how much?");
			return false;
		}
		String thisName=Util.combine(commands,0);
		Item thisThang=possibleGold(mob,thisName);
		if(thisThang==null)
		{
			thisThang=mob.fetchCarried(null,thisName);
			if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
			{
				mob.tell("You don't seem to be carrying that.");
				return false;
			}
		}
		FullMsg newMsg=new FullMsg(mob,shopkeeper,thisThang,CMMsg.MSG_DEPOSIT,"<S-NAME> deposit(s) <O-NAME> into <S-HIS-HER> account with <T-NAMESELF>.");
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
