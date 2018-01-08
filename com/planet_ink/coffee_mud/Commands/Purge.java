package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2004-2018 Bo Zimmerman

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
public class Purge extends StdCommand
{
	public Purge(){}

	private final String[] access=I(new String[]{"PURGE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell(L("You are not allowed to do that here."));
		return false;
	}

	public boolean mobs(MOB mob, List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is PURGE MOB [MOB NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}

		String mobID=CMParms.combine(commands,2);
		boolean allFlag=commands.get(2).equalsIgnoreCase("all");
		if(mobID.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			mobID="ALL "+mobID.substring(4);
		}
		if(mobID.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			mobID="ALL "+mobID.substring(0,mobID.length()-4);
		}
		MOB deadMOB=mob.location().fetchInhabitant(mobID);
		boolean doneSomething=false;
		while(deadMOB!=null)
		{
			if(!deadMOB.isMonster())
			{
				mob.tell(L("@x1 is a PLAYER!!\n\r",deadMOB.name()));
				if(!doneSomething)
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return false;
			}
			doneSomething=true;
			deadMOB.killMeDead(false);
			mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 vanishes in a puff of smoke.",deadMOB.name()));
			deadMOB=mob.location().fetchInhabitant(mobID);
			if(!allFlag)
				break;
		}
		if(!doneSomething)
		{
			mob.tell(L("I don't see '@x1 here.\n\r",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}
		return true;
	}

	public boolean items(MOB mob, List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is PURGE ITEM [ITEM NAME](@ room/[MOB NAME])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		String itemID=CMParms.combine(commands,2);
		MOB srchMob=mob;
		Item srchContainer=null;
		Room srchRoom=mob.location();
		final int x=itemID.indexOf('@');
		if(x>0)
		{
			final String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				final MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					final Item I = srchRoom.findItem(null, rest);
					if(I instanceof Container)
						srchContainer=I;
					else
					{
						mob.tell(L("MOB or Container '@x1' not found.",rest));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
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

		boolean allFlag=commands.get(2).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			itemID="ALL "+itemID.substring(4);
		}
		if(itemID.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			itemID="ALL "+itemID.substring(0,itemID.length()-4);
		}
		boolean doneSomething=false;
		Item deadItem=null;
		if(!allFlag)
			deadItem=(srchMob==null)?null:srchMob.findItem(null,itemID);
		if(deadItem==null)
			deadItem=(srchRoom==null)?null:srchRoom.findItem(srchContainer,itemID);
		while(deadItem!=null)
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 disintegrates!",deadItem.name()));
			deadItem.destroy();
			mob.location().recoverRoomStats();
			doneSomething=true;
			deadItem=null;
			if(!allFlag)
				deadItem=(srchMob==null)?null:srchMob.findItem(null,itemID);
			if(deadItem==null)
				deadItem=(srchRoom==null)?null:srchRoom.findItem(null,itemID);
			if(!allFlag)
				break;
		}
		if(!doneSomething)
		{
			mob.tell(L("I don't see '@x1 here.\n\r",itemID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		return true;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";

		if(commands.size()>1)
		{
			commandType=commands.get(1).toUpperCase();
		}
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			items(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			mobs(mob,commands);
		}
		else
		{
			final String allWord=CMParms.combine(commands,1);
			final Environmental thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Wearable.FILTER_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				commands.add(1,"ITEM");
				execute(mob,commands,metaFlags);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.add(1,"MOB");
				else
				{
					mob.tell(L("@x1 is a player!",thang.name()));
					return false;
				}
				execute(mob,commands,metaFlags);
			}
			else
			{
				mob.tell(
					L("\n\rYou cannot purge a '@x1'. However, you might try an ITEM or a MOB.",commandType));
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.PURGE);
	}

}
