package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Give extends BaseItemParser
{
	public Give(){}
	
	private String[] access={"GIVE","GI"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<2)
		{
			mob.tell("Give what to whom?");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("To whom should I give that?");
			return false;
		}

		MOB recipient=mob.location().fetchInhabitant((String)commands.elementAt(commands.size()-1));
		if((recipient==null)||((recipient!=null)&&(!Sense.canBeSeenBy(recipient,mob))))
		{
			mob.tell("I don't see anyone called "+(String)commands.elementAt(commands.size()-1)+" here.");
			return false;
		}
		commands.removeElementAt(commands.size()-1);
		if((commands.size()>0)&&(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("to")))
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
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(thingToGive.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(4);}
		if(thingToGive.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToGive="ALL "+thingToGive.substring(0,thingToGive.length()-4);}
		do
		{
			Environmental giveThis=possibleGold(mob,thingToGive);
			if(giveThis!=null)
				allFlag=false;
			else
				giveThis=mob.fetchCarried(null,thingToGive+addendumStr);
			if((giveThis==null)
			&&(V.size()==0)
			&&(addendumStr.length()==0)
			&&(!allFlag))
			{
				giveThis=mob.fetchWornItem(thingToGive);
				if(giveThis!=null)
				{
					if((!((Item)giveThis).amWearingAt(Item.HELD))&&(!((Item)giveThis).amWearingAt(Item.WIELD)))
					{
						mob.tell("You must remove that first.");
						return false;
					}
					else
					{
						FullMsg newMsg=new FullMsg(mob,giveThis,null,CMMsg.MSG_REMOVE,null);
						if(mob.location().okMessage(mob,newMsg))
							mob.location().send(mob,newMsg);
						else
							return false;
					}
				}
			}
			if(giveThis==null) break;
			if(Sense.canBeSeenBy(giveThis,mob))
				V.addElement(giveThis);
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToGive));

		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			Environmental giveThis=(Environmental)V.elementAt(i);
			FullMsg newMsg=new FullMsg(mob,recipient,giveThis,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
			if(mob.location().okMessage(mob,newMsg))
				mob.location().send(mob,newMsg);
			else
			if(giveThis instanceof Coins)
				((Coins)giveThis).putCoinsBack();
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
