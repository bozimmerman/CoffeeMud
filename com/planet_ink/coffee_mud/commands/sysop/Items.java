package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
import java.io.*;

public class Items
{
	public static void Destroy(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));

		Item deadItem=(Item)mob.location().fetchFromMOBRoom(mob,null,itemID);
		if(deadItem==null)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}
		deadItem.destroyThis();
		mob.location().recoverRoomStats();
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,deadItem.name()+" disintegrates!");
		Log.sysOut("Items",mob.ID()+" destroyed item "+deadItem.ID()+".");
	}

	public static void Modify(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<5)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ITEM [ITEM NAME] [LEVEL, ABILITY, REJUV, USES, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
		String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr=CommandProcessor.combine(commands,4);

		Item modItem=(Item)mob.location().fetchFromMOBRoom(mob,null,itemID);
		if(modItem==null)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		if(command.equals("LEVEL"))
		{
			int newLevel=Util.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.baseEnvStats().setLevel(newLevel);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=Util.s_int(restStr);
			modItem.baseEnvStats().setAbility(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=Util.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.baseEnvStats().setRejuv(newRejuv);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,modItem.name()+" shake(s) under the transforming power.");
			}
			else
			{
				modItem.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modItem.recoverEnvStats();
				mob.tell(modItem.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("USES"))
		{
			int newUses=Util.s_int(restStr);
			if(newUses>=0)
			{
				modItem.setUsesRemaining(newUses);
				mob.location().show(mob,null,Affect.VISUAL_WNOISE,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modItem instanceof GenArmor)
				Generic.modifyGenArmor(mob,(GenArmor)modItem);
			else
			if(modItem instanceof GenWeapon)
				Generic.modifyGenWeapon(mob,(GenWeapon)modItem);
			else
			if(modItem instanceof GenItem)
				Generic.modifyGenItem(mob,(GenItem)modItem);
			else
			if(modItem instanceof GenReadable)
				Generic.modifyGenReadable(mob,(GenReadable)modItem);
			else
			if(modItem instanceof GenFood)
				Generic.modifyGenFood(mob,(GenFood)modItem);
			else
			if(modItem instanceof GenWater)
				Generic.modifyGenDrink(mob,(GenWater)modItem);
			else
			if(modItem instanceof GenContainer)
				Generic.modifyGenContainer(mob,(GenContainer)modItem);
			else
				modItem.setMiscText(restStr);
			mob.location().show(mob,null,Affect.VISUAL_WNOISE,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, REJUV, USES, or MISC.");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
		}
		Log.sysOut("Items",mob.ID()+" modified item "+modItem.ID()+".");
	}

	public static void Create(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ITEM [ITEM NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
		Item newItem=(Item)MUD.getItem(itemID);

		if(newItem==null)
		{
			mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.VISUAL_WNOISE,"<S-NAME> flub(s) a spell..");
			return;
		}

		newItem=(Item)newItem.newInstance();
		mob.location().addItem(newItem);
		mob.location().show(mob,null,Affect.VISUAL_WNOISE,"Suddenly, "+newItem.name()+" drops from the sky.");

		if(newItem instanceof GenArmor)
			Generic.modifyGenArmor(mob,(GenArmor)newItem);
		else
		if(newItem instanceof GenWeapon)
			Generic.modifyGenWeapon(mob,(GenWeapon)newItem);
		else
		if(newItem instanceof GenReadable)
			Generic.modifyGenReadable(mob,(GenReadable)newItem);
		else
		if(newItem instanceof GenItem)
			Generic.modifyGenItem(mob,(GenItem)newItem);
		else
		if(newItem instanceof GenFood)
			Generic.modifyGenFood(mob,(GenFood)newItem);
		else
		if(newItem instanceof GenWater)
			Generic.modifyGenDrink(mob,(GenWater)newItem);
		else
		if(newItem instanceof GenContainer)
			Generic.modifyGenContainer(mob,(GenContainer)newItem);
		mob.location().recoverRoomStats();
		Log.sysOut("Items",mob.ID()+" created item "+newItem.ID()+".");
	}
}
