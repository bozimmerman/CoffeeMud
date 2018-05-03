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

public class Put extends StdCommand
{
	public Put()
	{
	}

	@SuppressWarnings("rawtypes")
	private final static Class[][] internalParameters=new Class[][]
	{
		{Item.class,Container.class,Boolean.class},
	};

	private final String[]	access	= I(new String[] { "PUT", "PU", "P" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public void putout(MOB mob, List<String> commands, boolean quiet)
	{
		List<String> origCmds=new XVector<String>(commands);
		final Room R=mob.location();
		if((commands.size()<3)||(R==null))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Put out what?"));
			return;
		}
		commands.remove(1);
		commands.remove(0);

		final List<Item> items=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_UNWORNONLY,true);
		if(items.size()==0)
		{
			Item I=R.findItem(null, CMParms.combine(commands,0));
			if(I!=null)
				items.add(I);
		}
		if(items.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be carrying that."));
		else
		for(int i=0;i<items.size();i++)
		{
			final Item I=items.get(i);
			if((items.size()==1)||(I instanceof Light))
			{
				final CMMsg msg=CMClass.getMsg(mob,I,null,CMMsg.MSG_EXTINGUISH,quiet?null:L("<S-NAME> put(s) out <T-NAME>."));
				if(R.okMessage(mob,msg))
					R.send(mob,msg);
			}
		}
	}

	public boolean put(MOB mob, Environmental container, Item putThis, boolean quiet)
	{
		final Room R=mob.location();
		final String putWord=(container instanceof Rideable)?((Rideable)container).putString(mob):"in";
		final CMMsg putMsg=CMClass.getMsg(mob,container,putThis,CMMsg.MASK_OPTIMIZE|CMMsg.MSG_PUT,quiet?null:L("<S-NAME> put(s) <O-NAME> @x1 <T-NAME>.",putWord));
		boolean success;
		if(R.okMessage(mob,putMsg))
		{
			R.send(mob,putMsg);
			success = true;
		}
		else
			success = false;
		if(putThis instanceof Coins)
			((Coins)putThis).putCoinsBack();
		if(putThis instanceof RawMaterial)
			((RawMaterial)putThis).rebundle();
		return success;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Put what where?"));
			return false;
		}

		if(commands.get(commands.size()-1).equalsIgnoreCase("on"))
		{
			commands.remove(commands.size()-1);
			final Command C=CMClass.getCommand("Wear");
			if(C!=null)
				C.execute(mob,commands,metaFlags);
			return false;
		}

		if(commands.size()>=4)
		{
			final String s=CMParms.combine(commands, 0).toLowerCase();
			final Wearable.CODES codes = Wearable.CODES.instance();
			for(int i=1;i<codes.total();i++)
			{
				if(s.endsWith(" on "+codes.name(i).toLowerCase())||s.endsWith(" on my "+codes.name(i).toLowerCase()))
				{
					final Command C=CMClass.getCommand("Wear");
					if(C!=null)
						C.execute(mob,commands,metaFlags);
					return false;
				}
			}
		}

		if(commands.get(1).equalsIgnoreCase("on"))
		{
			commands.remove(1);
			final Command C=CMClass.getCommand("Wear");
			if(C!=null)
				C.execute(mob,commands,metaFlags);
			return false;
		}

		if(commands.get(1).equalsIgnoreCase("out"))
		{
			putout(mob,commands,false);
			return false;
		}

		commands.remove(0);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Where should I put the @x1",commands.get(0)));
			return false;
		}

		final Room R=mob.location();
		if(R==null)
			return false;
		
		String containerName = commands.get(commands.size()-1);
		Environmental container=CMLib.english().possibleContainer(mob,commands,false,Wearable.FILTER_ANY);
		if(container == null)
		{
			container = R.fetchFromMOBRoomFavorsItems(mob,null, containerName, Wearable.FILTER_UNWORNONLY);
			commands.remove(commands.size()-1);
		}
		if(container == null)
		{
			final CMFlagLibrary flagLib=CMLib.flags();
			for(int i=0;i<R.numItems();i++)
			{
				final Item I=R.getItem(i);
				if(flagLib.isOpenAccessibleContainer(I))
				{
					Physical P=R.fetchFromRoomFavorItems(I, containerName);
					if(P instanceof Container)
					{
						container=P;
						break;
					}
				}
			}
		}
		if((container==null)||(!CMLib.flags().canBeSeenBy(container,mob)))
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("I don't see a @x1 here.",containerName));
			return false;
		}

		final int maxToPut=CMLib.english().calculateMaxToGive(mob,commands,true,mob,false);
		if(maxToPut<0)
			return false;

		String thingToPut=CMParms.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		final Vector<Item> V=new Vector<Item>();
		boolean allFlag = (commands.size() > 0) ? commands.get(0).equalsIgnoreCase("all") : false;
		if (thingToPut.toUpperCase().startsWith("ALL."))
		{
			allFlag = true;
			thingToPut = "ALL " + thingToPut.substring(4);
		}
		if (thingToPut.toUpperCase().endsWith(".ALL"))
		{
			allFlag = true;
			thingToPut = "ALL " + thingToPut.substring(0, thingToPut.length() - 4);
		}
		final boolean onlyGoldFlag=mob.hasOnlyGoldInInventory();
		Item putThis=CMLib.english().bestPossibleGold(mob,null,thingToPut);
		if(putThis!=null)
		{
			if(((Coins)putThis).getNumberOfCoins()<CMLib.english().numPossibleGold(mob,thingToPut))
				return false;
			if(CMLib.flags().canBeSeenBy(putThis,mob))
				V.add(putThis);
		}
		boolean doBugFix = true;
		if(V.size()==0)
		while(doBugFix || ((allFlag)&&(addendum<=maxToPut)))
		{
			doBugFix=false;
			putThis=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,thingToPut+addendumStr);
			if((allFlag)&&(!onlyGoldFlag)&&(putThis instanceof Coins)&&(thingToPut.equalsIgnoreCase("ALL")))
				putThis=null;
			else
			{
				if(putThis==null)
					break;
				if((CMLib.flags().canBeSeenBy(putThis,mob))
				&&(!V.contains(putThis)))
					V.add(putThis);
			}
			addendumStr="."+(++addendum);
		}

		if(V.contains(container))
			V.remove(container);

		if(V.size()==0)
			CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be carrying that."));
		else
		for(int i=0;i<V.size();i++)
		{
			putThis=V.get(i);
			put(mob,container,putThis,false);
		}
		R.recoverRoomStats();
		R.recoverRoomStats();
		return false;
	}
	
	@Override
	public Object executeInternal(MOB mob, int metaFlags, Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		
		if(args[0] instanceof Item)
		{
			final Item item=(Item)args[0];
			Item container=null;
			boolean quiet=false;
			for(int i=1;i<args.length;i++)
			{
				if(args[i] instanceof Container)
					container=(Item)args[1];
				else
				if(args[i] instanceof Boolean)
					quiet=((Boolean)args[i]).booleanValue();
			}
			final boolean success=put(mob,container,item,quiet);
			return Boolean.valueOf(success);
		}
		return Boolean.FALSE;
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
