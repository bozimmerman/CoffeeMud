package com.planet_ink.coffee_mud.commands.base.sysop;


import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class Utils
{
	public void newSomething(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE NEW ([ITEM NAME]/[MOB NAME])\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String name=Util.combine(commands,2);
		Environmental E=mob.location().fetchFromRoomFavorItems(null,name);
		if(E==null)
			for(int m=0;m<CMMap.map.size();m++)
			{
				Room room=(Room)CMMap.map.elementAt(m);
				E=room.fetchFromRoomFavorMOBs(null,name);
				if(E!=null) break;
			}
		if(E==null)
			for(int r=0;r<CMMap.map.size();r++)
			{
				Room room=(Room)CMMap.map.elementAt(r);
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB mob2=room.fetchInhabitant(m);
					if(mob2!=null)
					{
						E=mob2.fetchInventory(name);
						if((E==null)&&(mob2 instanceof ShopKeeper))
							E=((ShopKeeper)mob2).getStock(name);
					}
				}
				if(E!=null) break;	
			}
		if(E==null)
		{
			mob.tell("There's no such thing in the living world as a '"+name+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(E instanceof MOB)
		{
			MOB newMOB=(MOB)E.copyOf();
			newMOB.setStartRoom(mob.location());
			newMOB.setLocation(mob.location());
			newMOB.recoverCharStats();
			newMOB.recoverEnvStats();
			newMOB.recoverMaxState();
			newMOB.resetToMaxState();
			newMOB.bringToLife(mob.location());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
			Log.sysOut("Mobs",mob.ID()+" created mob "+newMOB.ID()+".");
		}
		else
		if(E instanceof Item)
		{
			Item newItem=(Item)E.copyOf();
			newItem.setLocation(null);
			newItem.wearAt(0);
			mob.location().addItem(newItem);
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");
			mob.location().recoverRoomStats();
			Log.sysOut("Items",mob.ID()+" created item "+newItem.ID()+".");
		}
		else
		{
			mob.tell("I can't just make a copy of a '"+E.name()+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
	}
	
}
