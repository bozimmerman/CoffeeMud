package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Remove extends BaseItemParser
{
	public Remove(){}

	private String[] access={"REMOVE","REM"};
	public String[] getAccessWords(){return access;}



	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Remove what?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.firstElement() instanceof Item)
		{
			boolean quiet=((commands.size()>1)&&(commands.lastElement() instanceof String)&&(((String)commands.lastElement()).equalsIgnoreCase("QUIETLY")));
			Item item=(Item)commands.firstElement();
			FullMsg newMsg=new FullMsg(mob,item,null,CMMsg.MSG_REMOVE,quiet?null:"<S-NAME> remove(s) <T-NAME>.");
			if(mob.location().okMessage(mob,newMsg))
			{
				mob.location().send(mob,newMsg);
				return true;
			}
			return false;
		}

		Vector items=fetchItemList(mob,mob,null,commands,Item.WORN_REQ_WORNONLY,false);
		if(items.size()==0)
			mob.tell("You don't seem to be wearing that.");
		else
		for(int i=0;i<items.size();i++)
		{
			Item item=(Item)items.elementAt(i);
			FullMsg newMsg=new FullMsg(mob,item,null,CMMsg.MSG_REMOVE,"<S-NAME> remove(s) <T-NAME>.");
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
