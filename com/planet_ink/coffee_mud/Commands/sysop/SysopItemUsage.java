package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class SysopItemUsage
{
	private SysopItemUsage(){}
	
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

		MOB victim=mob.location().fetchInhabitant((String)commands.lastElement());

		if((victim==null)||((victim!=null)&&(!Sense.canBeSeenBy(victim,mob))))
		{
			mob.tell("I don't see "+(String)commands.lastElement()+" here.");
			return;
		}
		commands.removeElement(commands.lastElement());
		String itemName=Util.combine(commands,0);
		Item I=victim.fetchInventory(itemName);
		if((I==null)||((I!=null)&&(!Sense.canBeSeenBy(I,mob))))
		{
			mob.tell(victim.name()+" doesn't seem to have a '"+itemName+"'.");
			return;
		}
		I.remove();
		FullMsg newMsg=new FullMsg(victim,mob,I,Affect.MASK_GENERAL|Affect.MSG_GIVE,"<T-NAME> take(s) "+I.name()+" from <S-NAMESELF>.");
		if(victim.location().okAffect(victim,newMsg))
			victim.location().send(victim,newMsg);
	}
}
