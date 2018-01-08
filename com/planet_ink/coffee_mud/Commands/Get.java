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
public class Get extends StdCommand
{
	public Get()
	{
	}

	private final String[]	access	= I(new String[] { "GET", "G" });

	@SuppressWarnings("rawtypes")
	private final static Class[][] internalParameters=new Class[][]
	{
		{Item.class},
		{Item.class,Boolean.class},
		{Item.class,Container.class},
		{Item.class,Container.class,Boolean.class},
	};

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public static boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{
		return get(mob,container,getThis,quiet,"get",false);
	}

	public static boolean get(MOB mob, Item container, Item getThis, boolean quiet, String getWord, boolean optimize)
	{
		final Room R=mob.location();
		String theWhat="<T-NAME>";
		Item target=getThis;
		Item tool=null;
		if(container!=null)
		{
			tool=getThis;
			target=container;
			theWhat="<O-NAME> from <T-NAME>";
		}
		if(!getThis.amWearingAt(Wearable.IN_INVENTORY))
		{
			final CMMsg msg=CMClass.getMsg(mob,getThis,null,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_REMOVE,null);
			if(!R.okMessage(mob,msg))
				return false;
			R.send(mob,msg);
		}
		final CMMsg msg=CMClass.getMsg(mob,target,tool,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_GET,quiet?null:CMLib.lang().L("<S-NAME> @x1(s) @x2.",getWord,theWhat));
		if(!R.okMessage(mob,msg))
			return false;
		// we do this next step because, when a container is involved,
		// the item deserves to be the target of the GET.
		if((!mob.isMine(target))&&(target!=getThis))
		{
			final CMMsg msg2=CMClass.getMsg(mob,getThis,null,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MSG_GET,null);
			if(!R.okMessage(mob,msg2))
				return false;
			R.send(mob,msg);
			R.send(mob,msg2);
		}
		else
		{
			R.send(mob,msg);
		}
		return true;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		final Room R=mob.location();
		Vector<String> origCmds=new XVector<String>(commands);
		if(commands.size()<2)
		{
			CMLib.commands().postCommandFail(mob,origCmds,L("Get what?"));
			return false;
		}
		commands.remove(0);
		boolean quiet=false;
		if((commands.size()>0)&&(commands.get(commands.size()-1).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			quiet=true;
			commands.remove(commands.size()-1);
		}

		String containerName="";
		if(commands.size()>0)
			containerName=commands.get(commands.size()-1);
		final Vector<String> containerCommands=new XVector<String>(commands);
		final java.util.List<Container> containers=CMLib.english().possibleContainers(mob,commands,Wearable.FILTER_ANY,true);
		int c=0;

		int maxToGet=CMLib.english().calculateMaxToGive(mob,commands,containers.size()==0,R,true);
		if(maxToGet<0)
			return false;

		String whatToGet=CMParms.combine(commands,0);
		final String unmodifiedWhatToGet=whatToGet;
		boolean allFlag = (commands.size() > 0) ? commands.get(0).equalsIgnoreCase("all") : false;
		if (whatToGet.toUpperCase().startsWith("ALL."))
		{
			allFlag = true;
			whatToGet = "ALL " + whatToGet.substring(4);
		}
		if (whatToGet.toUpperCase().endsWith(".ALL"))
		{
			allFlag = true;
			whatToGet = "ALL " + whatToGet.substring(0, whatToGet.length() - 4);
		}
		boolean doneSomething=false;
		while((c<containers.size())||(containers.size()==0))
		{
			final Vector<Item> V=new Vector<Item>();
			Container container=null;
			if(containers.size()>0)
				container=containers.get(c++);
			int addendum=1;
			String addendumStr="";
			boolean doBugFix = true;
			while(doBugFix || ((allFlag)&&(addendum<=maxToGet)))
			{
				doBugFix=false;
				Environmental getThis=null;
				if((container!=null)&&(mob.isMine(container)))
					getThis=R.fetchFromMOBRoomFavorsItems(mob,container,whatToGet+addendumStr,Wearable.FILTER_UNWORNONLY);
				else
				{
					if(!allFlag)
						getThis=CMLib.english().possibleRoomGold(mob,R,container,whatToGet);
					if(getThis==null)
						getThis=R.fetchFromRoomFavorItems(container,whatToGet+addendumStr);
				}
				if(getThis==null)
					break;

				if((maxToGet>1)&&(getThis instanceof RawMaterial)&&(container!=null)
				&&(((RawMaterial)getThis).container()==container))
				{
					final int weight=((RawMaterial)getThis).phyStats().weight();
					if((weight>1) &&(weight>=maxToGet) &&(CMStrings.containsWordIgnoreCase(((RawMaterial)getThis).name(), "bundle")))
					{
						if(weight>maxToGet)
							getThis=CMLib.materials().splitBundle((RawMaterial)getThis, maxToGet,container);
						maxToGet=1;
					}
				}

				if((getThis instanceof Item)
				&&((CMLib.flags().canBeSeenBy(getThis,mob)||(getThis instanceof Light)))
				&&((!allFlag)||CMLib.flags().isGettable(((Item)getThis))||(getThis.displayText().length()>0))
				&&(!V.contains(getThis)))
					V.add((Item)getThis);

				addendumStr="."+(++addendum);
			}

			for(int i=0;i<V.size();i++)
			{
				final Item getThis=V.get(i);
				if(!getThis.amDestroyed())
				{
					get(mob,container,getThis,quiet,"get",true);
					if(getThis instanceof Coins)
						((Coins)getThis).putCoinsBack();
					if(getThis instanceof RawMaterial)
						((RawMaterial)getThis).rebundle();
					doneSomething=true;
				}
			}
			R.recoverRoomStats();
			R.recoverRoomStats();

			if(containers.size()==0)
				break;
		}
		if(!doneSomething)
		{
			if(containers.size()>0)
			{
				final Container container=containers.get(0);
				if(container.isOpen())
					CMLib.commands().postCommandFail(mob,container,null,commands,L("You don't see '@x1' in <T-NAME>.",unmodifiedWhatToGet));
				else
					CMLib.commands().postCommandFail(mob,origCmds,L("@x1 is closed.",container.name()));
			}
			else
			if(containerName.equalsIgnoreCase("all"))
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't see anything here."));
			else
			{
				final java.util.List<Container> V=CMLib.english().possibleContainers(mob,containerCommands,Wearable.FILTER_ANY,false);
				if(V.size()==0)
					CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' here.",containerName));
				else
				if(V.size()==1)
					CMLib.commands().postCommandFail(mob,V.get(0),null,commands,L("You don't see '@x1' in <T-NAME> here.",unmodifiedWhatToGet));
				else
					CMLib.commands().postCommandFail(mob,origCmds,L("You don't see '@x1' in any '@x2'.",unmodifiedWhatToGet,containerName));
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
			final boolean success=get(mob,container,item,quiet);
			if(item instanceof Coins)
				((Coins)item).putCoinsBack();
			if(item instanceof RawMaterial)
				((RawMaterial)item).rebundle();
			return Boolean.valueOf(success);
		}
		return Boolean.FALSE;
	}
}
