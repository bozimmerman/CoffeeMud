package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Split extends StdCommand
{
	public Split(){}

	private String[] access={"SPLIT"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Split how much gold?");
			return false;
		}
		int gold=Util.s_int((String)commands.elementAt(1));
		if(gold<0)
		{
			mob.tell("Split how much gold?!?");
			return false;
		}

		int num=0;
		HashSet H=mob.getGroupMembers(new HashSet());
		
		for(Iterator e=((HashSet)H.clone()).iterator();e.hasNext();)
		{
			MOB recipient=(MOB)e.next();
			if((!recipient.isMonster())
			&&(recipient!=mob)
			&&(recipient.location()==mob.location())
			&&(mob.location().isInhabitant(recipient)))
				num++;
			else
				H.remove(recipient);
		}
		if(num==0)
		{
			mob.tell("No one appears to be eligible to receive any of your gold.");
			return false;
		}

		gold=(int)Math.floor(Util.div(gold,num+1));

		if((gold*num)>mob.getMoney())
		{
			mob.tell("You don't have that much gold.");
			return false;
		}
		for(Iterator e=H.iterator();e.hasNext();)
		{
			MOB recipient=(MOB)e.next();
			mob.setMoney(mob.getMoney()-gold);
			Item C=(Item)CMClass.getItem("StdCoins");
			C.baseEnvStats().setAbility(gold);
			C.recoverEnvStats();
			mob.addInventory(C);
			FullMsg newMsg=new FullMsg(mob,recipient,C,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
