package com.planet_ink.coffee_mud.Commands.sysop;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.*;

public class Items
{
	private Items(){}

	public static boolean destroy(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String itemID=Util.combine(commands,2);
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL.")){ allFlag=true; itemID="ALL "+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(".ALL")){ allFlag=true; itemID="ALL "+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Item deadItem=null;
		if(!allFlag) deadItem=mob.fetchInventory(null,itemID);
		if(deadItem==null) deadItem=(Item)mob.location().fetchItem(null,itemID);
		while(deadItem!=null)
		{
			deadItem.destroy();
			mob.location().recoverRoomStats();
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,deadItem.name()+" disintegrates!");
			doneSomething=true;
			Log.sysOut("Items",mob.Name()+" destroyed item "+deadItem.ID()+".");
			deadItem=null;
			if(!allFlag) deadItem=mob.fetchInventory(null,itemID);
			if(deadItem==null) deadItem=(Item)mob.location().fetchItem(null,itemID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		return true;
	}

	public static void modify(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ITEM [ITEM NAME] [LEVEL, ABILITY, REJUV, USES, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
		String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=Util.combine(commands,4);

		Item modItem=(Item)mob.location().fetchFromMOBRoomFavorsItems(mob,null,itemID,Item.WORN_REQ_ANY);
		if(modItem==null)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if(command.equals("LEVEL"))
		{
			int newLevel=Util.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.baseEnvStats().setLevel(newLevel);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=Util.s_int(restStr);
			modItem.baseEnvStats().setAbility(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("HEIGHT"))
		{
			int newAbility=Util.s_int(restStr);
			modItem.baseEnvStats().setHeight(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=Util.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.baseEnvStats().setRejuv(newRejuv);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
			else
			{
				modItem.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modItem.recoverEnvStats();
				mob.tell(modItem.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("USES"))
		{
			int newUses=Util.s_int(restStr);
			if(newUses>=0)
			{
				modItem.setUsesRemaining(newUses);
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modItem.isGeneric())
				Generic.genMiscSet(mob,modItem);
			else
				modItem.setMiscText(restStr);
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, HEIGHT, REJUV, USES, or MISC.");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
		Log.sysOut("Items",mob.Name()+" modified item "+modItem.ID()+".");
	}

	public static void create(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ITEM [ITEM NAME]\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
		Item newItem=(Item)CMClass.getItem(itemID);

		if(newItem==null)
		{
			mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
			mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if((newItem instanceof ArchonOnly)
		&&(!mob.isASysOp(null)))
		{
			mob.tell("NO!");
			return;
		}

		if(newItem.subjectToWearAndTear())
			newItem.setUsesRemaining(100);
		mob.location().addItem(newItem);
		mob.location().showHappens(Affect.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");

		if(newItem.isGeneric())
			Generic.genMiscSet(mob,newItem);
		mob.location().recoverRoomStats();
		Log.sysOut("Items",mob.Name()+" created item "+newItem.ID()+".");
	}
}
