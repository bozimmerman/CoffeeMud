package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class Purge extends StdCommand
{
	public Purge(){}

	private String[] access={"PURGE"};
	public String[] getAccessWords(){return access;}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean mobs(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is PURGE MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		String mobID=CMParms.combine(commands,2);
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(mobID.toUpperCase().startsWith("ALL.")){ allFlag=true; mobID="ALL "+mobID.substring(4);}
		if(mobID.toUpperCase().endsWith(".ALL")){ allFlag=true; mobID="ALL "+mobID.substring(0,mobID.length()-4);}
		MOB deadMOB=mob.location().fetchInhabitant(mobID);
		boolean doneSomething=false;
		while(deadMOB!=null)
		{
			if(!deadMOB.isMonster())
			{
				mob.tell(deadMOB.name()+" is a PLAYER!!\n\r");
				if(!doneSomething)
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return false;
			}
			doneSomething=true;
			deadMOB.killMeDead(false);
			mob.location().showHappens(CMMsg.MSG_OK_VISUAL,deadMOB.name()+" vanishes in a puff of smoke.");
			deadMOB=mob.location().fetchInhabitant(mobID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		return true;
	}


	public boolean items(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is PURGE ITEM [ITEM NAME](@ room/[MOB NAME])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		
		String itemID=CMParms.combine(commands,2);
		MOB srchMob=mob;
		Item srchContainer=null;
		Room srchRoom=mob.location();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					Item I = srchRoom.fetchItem(null, rest);
					if(I instanceof Container)
						srchContainer=(Container)I;
					else
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
				}
				else
				{
					srchMob=M;
					srchRoom=null;
				}
			}
		}
		
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL.")){ allFlag=true; itemID="ALL "+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(".ALL")){ allFlag=true; itemID="ALL "+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Item deadItem=null;
		if(!allFlag) deadItem=(srchMob==null)?null:srchMob.fetchInventory(null,itemID);
		if(deadItem==null) deadItem=(srchRoom==null)?null:srchRoom.fetchItem(srchContainer,itemID);
		while(deadItem!=null)
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,deadItem.name()+" disintegrates!");
            deadItem.destroy();
            mob.location().recoverRoomStats();
			doneSomething=true;
			deadItem=null;
			if(!allFlag) deadItem=(srchMob==null)?null:srchMob.fetchInventory(null,itemID);
			if(deadItem==null) deadItem=(srchRoom==null)?null:srchRoom.fetchItem(null,itemID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		return true;
	}


	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";

		if(commands.size()>1)
		{
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			Environmental thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Wearable.FILTER_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				commands.insertElementAt("ITEM",1);
				execute(mob,commands,metaFlags);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.insertElementAt("MOB",1);
				else
				{
					mob.tell(thang.name()+" is a player!");
					return false;
				}
				execute(mob,commands,metaFlags);
			}
			else
			{
				mob.tell(
					"\n\rYou cannot purge a '"+commandType+"'. "
					+"However, you might try an ITEM or a MOB.");
			}
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"PURGE");}

	
}
