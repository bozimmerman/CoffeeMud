package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Feed extends StdCommand
{
	public Feed(){}

	private String[] access={"FEED"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(commands.size()<3)
		{
			mob.tell("Feed who what?");
			return false;
		}
		commands.removeElementAt(0);
		String what=(String)commands.lastElement();
		commands.removeElement(what);
		String whom=Util.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(whom);
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("I don't see "+whom+" here.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("Not while you are in combat!");
			return false;
		}
		if(target.willFollowOrdersOf(mob))
		{
			Item item=mob.fetchInventory(what);
			if((item==null)||(!Sense.canBeSeenBy(item,mob)))
			{
				mob.tell("I don't see "+what+" here.");
				return false;
			}
			if(!item.amWearingAt(Item.INVENTORY))
			{
				mob.tell("You might want to remove that first.");
				return false;
			}
			if((!(item instanceof Food))&&(!(item instanceof Drink)))
			{
				mob.tell("You might want to try feeding them something edibile or drinkable.");
				return false;
			}
			if(target.isInCombat())
			{
				mob.tell("Not while "+target.name()+" is in combat!");
				return false;
			}
			FullMsg msg=new FullMsg(mob,target,item,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> feed(s) "+item.name()+" to <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if((CommonMsgs.drop(mob,item,true,false))
				   &&(mob.location().isContent(item)))
				{
					msg=new FullMsg(target,item,CMMsg.MASK_GENERAL|CMMsg.MSG_GET,null);
					target.location().send(target,msg);
					if(target.isMine(item))
					{
						if(item instanceof Food)
							msg=new FullMsg(target,item,null,CMMsg.MASK_GENERAL|CMMsg.MSG_EAT,CMMsg.MSG_EAT,CMMsg.MSG_EAT,null);
						else
							msg=new FullMsg(target,item,null,CMMsg.MASK_GENERAL|CMMsg.MSG_DRINK,CMMsg.MSG_DRINK,CMMsg.MSG_DRINK,null);
						if(target.location().okMessage(target,msg))
							target.location().send(target,msg);
						if(target.isMine(item))
						{
							msg=new FullMsg(target,item,null,CMMsg.MASK_GENERAL|CMMsg.MSG_DROP,CMMsg.MSG_DROP,CMMsg.MSG_DROP,null);
							if(mob.location().okMessage(mob,msg))
							{
								mob.location().send(mob,msg);
								CommonMsgs.get(mob,null,item,true);
							}
						}
					}
				}
			}
		}
		else
			mob.tell(target.name()+" won't let you.");
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
