package com.planet_ink.coffee_mud.Commands.extra;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Equipment extends StdCommand
{
	public Equipment(){}
	
	private String[] access={"EQUIPMENT","EQ","EQUIP"};
	public String[] getAccessWords(){return access;}
	
	public static StringBuffer getEquipment(MOB seer, MOB mob, boolean allPlaces)
	{
		StringBuffer msg=new StringBuffer("");
		if(Sense.isSleeping(seer))
			return new StringBuffer("(nothing you can see right now)");

		for(int l=0;l<Item.wornOrder.length;l++)
		{
			long wornCode=Item.wornOrder[l];
			String header="^N(^H"+Sense.wornLocation(wornCode)+"^?)";
			header+=Util.SPACES.substring(0,26-header.length())+": ^!";
			int found=0;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item thisItem=mob.fetchInventory(i);
				if((thisItem.container()==null)&&(thisItem.amWearingAt(wornCode)))
				{
					found++;
					if(Sense.canBeSeenBy(thisItem,seer))
					{
						String name=thisItem.name();
						if(name.length()>53) name=name.substring(0,50)+"...";
						msg.append(header+name+Sense.colorCodes(thisItem,seer)+"^?\n\r");
					}
					else
					if(seer==mob)
						msg.append(header+"(something you can`t see)"+Sense.colorCodes(thisItem,seer)+"^?\n\r");
				}
			}
			if((allPlaces)&&(wornCode!=Item.FLOATING_NEARBY))
			{
				int total=mob.getWearPositions(wornCode)-found;
				for(int i=0;i<total;i++)
					msg.append(header+"^?\n\r");
			}
		}
		if(msg.length()==0)
			msg.append("^!(nothing)^?\n\r");
		return msg;
	}
	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((commands.size()==1)&&(commands.firstElement() instanceof MOB))
		{
			commands.addElement(getEquipment((MOB)commands.firstElement(),mob,false));
			return true;
		}
		if(!mob.isMonster())
		{
			if((commands.size()>1)&&(Util.combine(commands,1).equalsIgnoreCase("long")))
				mob.session().unfilteredPrintln("You are wearing:\n\r"+getEquipment(mob,mob,true));
			else
				mob.session().unfilteredPrintln("You are wearing:\n\r"+getEquipment(mob,mob,false));
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	
	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
