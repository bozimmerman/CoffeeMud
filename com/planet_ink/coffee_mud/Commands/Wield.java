package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Wield extends BaseItemParser
{
	public Wield(){}

	private String[] access={"WIELD"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Wield what?");
			return false;
		}
		commands.removeElementAt(0);
		Vector items=fetchItemList(mob,mob,null,commands,Item.WORN_REQ_UNWORNONLY,false);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<items.size();i++)
			if((items.size()==1)||(((Item)items.elementAt(i)).canWear(mob,Item.WIELD)))
			{
				Item item=(Item)items.elementAt(i);
				FullMsg newMsg=new FullMsg(mob,item,null,CMMsg.MSG_WIELD,"<S-NAME> wield(s) <T-NAME>.");
				if(mob.location().okMessage(mob,newMsg))
					mob.location().send(mob,newMsg);
			}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
