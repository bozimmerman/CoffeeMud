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

/*
   Copyright 2005-2018 Bo Zimmerman

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

public class Package extends StdCommand
{
	public Package(){}

	private final String[] access=I(new String[]{"PACKAGE"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Package what?"));
			return false;
		}
		commands.remove(0);
		String whatName="";
		if(commands.size()>0)
			whatName=commands.get(commands.size()-1);
		final int maxToGet=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToGet<0)
			return false;

		String whatToGet=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?commands.get(0).equalsIgnoreCase("all"):false;
		if(whatToGet.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			whatToGet="ALL "+whatToGet.substring(4);
		}
		if(whatToGet.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);
		}
		final Vector<Item> V=new Vector<Item>();
		int addendum=1;
		String addendumStr="";
		boolean packagingPackagesProblem=false;
		do
		{
			Environmental getThis=null;
			getThis=mob.location().fetchFromRoomFavorItems(null,whatToGet+addendumStr);
			if(getThis==null)
				break;
			if(getThis instanceof PackagedItems)
				packagingPackagesProblem=true;
			else
			{
				if((getThis instanceof Item)
				&&(CMLib.flags().canBeSeenBy(getThis,mob))
				&&((!allFlag)||CMLib.flags().isGettable(((Item)getThis))||(getThis.displayText().length()>0))
				&&(!V.contains(getThis)))
					V.add((Item)getThis);
			}
			addendumStr="."+(++addendum);
		}
		while((allFlag)&&(addendum<=maxToGet))
			;

		if(V.size()==0)
		{
			if(packagingPackagesProblem)
				CMLib.commands().postCommandFail(mob,origCmds,L("You can't package up packages.",whatName));
			else
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",whatName));
			return false;
		}

		for(int i=0;i<V.size();i++)
		{
			final Item I=V.get(i);
			if((I instanceof Coins)
			||(CMLib.flags().isEnspelled(I))
			||(CMLib.flags().isOnFire(I)))
			{
				CMLib.commands().postCommandFail(mob,origCmds,L("Items such as @x1 may not be packaged.",I.name(mob)));
				return false;
			}
		}
		final PackagedItems thePackage=(PackagedItems)CMClass.getItem("GenPackagedItems");
		if(thePackage==null)
			return false;
		if(!thePackage.isPackagable(V))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("All items in a package must be absolutely identical.  Some here are not."));
			return false;
		}
		Item getThis=null;
		for(int i=0;i<V.size();i++)
		{
			getThis=V.get(i);
			if((!mob.isMine(getThis))&&(!Get.get(mob,null,getThis,true,"get",true)))
				return false;
		}
		if(getThis==null)
			return false;
		final String name=CMLib.english().cleanArticles(getThis.name());
		final CMMsg msg=CMClass.getMsg(mob,getThis,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> package(s) up @x1 <T-NAMENOART>(s).",""+V.size()));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			thePackage.setName(name);
			if(thePackage.packageMe(getThis,V.size()))
			{
				for(int i=0;i<V.size();i++)
					V.get(i).destroy();
				mob.location().addItem(thePackage,ItemPossessor.Expire.Player_Drop);
				mob.location().recoverRoomStats();
				mob.location().recoverRoomStats();
			}
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
