package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class DrinkCmd extends StdCommand
{
	public DrinkCmd(){}

	private String[] access={"DRINK","DR","DRI"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()<2)&&(!(mob.location() instanceof Drink)))
		{
			mob.tell("Drink what?");
			return false;
		}
		commands.removeElementAt(0);
		Environmental thisThang=null;
		if((commands.size()==0)&&(mob.location() instanceof Drink))
			thisThang=mob.location();
		else
		{
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0),Item.WORN_REQ_ANY);
			if((thisThang==null)
			||((thisThang!=null)
			   &&(!mob.isMine(thisThang))
			   &&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				mob.tell("You don't see '"+Util.combine(commands,0)+"' here.");
				return false;
			}
		}
		String str="<S-NAME> take(s) a drink from <T-NAMESELF>.";
		Environmental tool=null;
		if((thisThang instanceof Drink)
		&&(((Drink)thisThang).liquidRemaining()>0)
		&&(((Drink)thisThang).liquidType()!=EnvResource.RESOURCE_FRESHWATER))
			str="<S-NAME> take(s) a drink of "+EnvResource.RESOURCE_DESCS[((Drink)thisThang).liquidType()&EnvResource.RESOURCE_MASK].toLowerCase()+" from <T-NAMESELF>.";
		else
		if(thisThang instanceof Container)
		{
			Vector V=((Container)thisThang).getContents();
			for(int v=0;v<V.size();v++)
			{
				Item I=(Item)V.elementAt(v);
				if((I instanceof Drink)&&(I instanceof EnvResource))
				{
					tool=thisThang;
					thisThang=I;
					str="<S-NAME> take(s) a drink of <T-NAMESELF> from <O-NAMESELF>.";
					break;
				}
			}
		}
		FullMsg newMsg=new FullMsg(mob,thisThang,tool,CMMsg.MSG_DRINK,str+CommonStrings.msp("drink.wav",10));
		if(mob.location().okMessage(mob,newMsg))
			mob.location().send(mob,newMsg);
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
