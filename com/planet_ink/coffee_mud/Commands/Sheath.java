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
   Copyright 2003-2022 Bo Zimmerman

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
public class Sheath extends StdCommand
{
	public Sheath()
	{
	}

	private final String[] access=I(new String[]{"SHEATH"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public final boolean canSheathIn(final Item I, final Container C, final Map<Container,int[]> sheaths)
	{
		if(C.canContain(I))
		{
			final int[] capRem = sheaths.get(C);
			if((capRem!=null)
			&&(capRem[0] >= I.phyStats().weight()))
			{
				capRem[0] -= I.phyStats().weight();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		boolean quiet=false;
		boolean noerrors=false;
		final Vector<String> origCmds=new XVector<String>(commands);
		if(((commands.size()>0)&&(commands.get(commands.size()-1).equalsIgnoreCase("QUIETLY")))
		||(CMath.bset(metaFlags, MUDCmdProcessor.METAFLAG_QUIETLY)))
		{
			commands.remove(commands.size()-1);
			quiet=true;
		}
		if((commands.size()>0)&&(commands.get(commands.size()-1).equalsIgnoreCase("IFPOSSIBLE")))
		{
			commands.remove(commands.size()-1);
			noerrors=true;
		}

		Item item1=null;
		Item item2=null;
		if(commands.size()>0)
			commands.remove(0);
		final OrderedMap<Container,int[]> sheaths=new OrderedMap<Container,int[]>();
		for(int i=0;i<mob.numItems();i++)
		{
			final Item I=mob.getItem(i);
			if(I != null)
			{
				if(!I.amWearingAt(Wearable.IN_INVENTORY))
				{
					if(I instanceof Weapon)
					{
						if(I.amWearingAt(Wearable.WORN_WIELD))
							item1=I;
						else
						if(I.amWearingAt(Wearable.WORN_HELD))
							item2=I;
					}
					else
					if((I instanceof Container)
					&&(!(I instanceof Drink))
					&&(((Container)I).capacity()>0)
					&&(((Container)I).containTypes()!=Container.CONTAIN_ANYTHING)
					&&(!sheaths.containsKey(I)))
						sheaths.put((Container)I, new int[] {((Container)I).capacity()});
				}
				else
				{
					final Container C=I.container();
					if((C != null)
					&&(!(C instanceof Drink))
					&&(C.capacity()>0)
					&&(C.containTypes()!=Container.CONTAIN_ANYTHING))
					{
						if(!sheaths.containsKey(C))
							sheaths.put(C, new int[] {C.capacity()});
						sheaths.get(C)[0] -= I.phyStats().weight();
					}
				}
			}
		}
		if(commands.size()==0)
		{
			if((noerrors)
			&&(item1==null)
			&&(item2==null))
				return false;
		}
		final OrderedMap<Item,Container> sheathMap=new OrderedMap<Item,Container>();
		Item sheathable=null;
		if(commands.size()==0)
		{
			if(item2==item1)
				item2=null;
			for(final Iterator<Pair<Container,int[]>> p = sheaths.pairIterator();p.hasNext();)
			{
				final Pair<Container,int[]> sheathPair = p.next();
				final Container sheath=sheathPair.first;
				if((item1!=null)
				&&(!sheathMap.containsKey(item1))
				&&(canSheathIn(item1,sheath,sheaths)))
					sheathMap.put(item1, sheath);
				else
				if((item2!=null)
				&&(!sheathMap.containsKey(item2))
				&&(canSheathIn(item2,sheath,sheaths)))
					sheathMap.put(item2, sheath);
			}
			if(item2!=null)
			{
				for(final Iterator<Pair<Container,int[]>> p = sheaths.pairIterator();p.hasNext();)
				{
					final Pair<Container,int[]> sheathPair = p.next();
					final Container sheath=sheathPair.first;
					if((!sheathMap.containsKey(item2))
					&&(canSheathIn(item2,sheath,sheaths)))
						sheathMap.put(item2, sheath);
				}
			}
			if(item1!=null)
				sheathable=item1;
			else
			if(item2!=null)
				sheathable=item2;
		}
		else
		{
			commands.add(0,"all");
			final Container container=(Container)CMLib.english().parsePossibleContainer(mob,commands,false,Wearable.FILTER_WORNONLY);
			String thingToPut=CMParms.combine(commands,0);
			int addendum=1;
			String addendumStr="";
			boolean allFlag=(commands.size()>0)?commands.get(0).equalsIgnoreCase("all"):false;
			if(thingToPut.toUpperCase().startsWith("ALL."))
			{
				allFlag=true;
				thingToPut="ALL "+thingToPut.substring(4);
			}
			if(thingToPut.toUpperCase().endsWith(".ALL"))
			{
				allFlag=true;
				thingToPut="ALL "+thingToPut.substring(0,thingToPut.length()-4);
			}
			boolean doBugFix = true;
			while(doBugFix || allFlag)
			{
				doBugFix=false;
				final Item putThis=mob.fetchItem(null,Wearable.FILTER_WORNONLY,thingToPut+addendumStr);
				if(putThis==null)
					break;
				if(((putThis.amWearingAt(Wearable.WORN_WIELD))
				   ||(putThis.amWearingAt(Wearable.WORN_HELD)))
				   &&(putThis instanceof Weapon))
				{
					if(CMLib.flags().canBeSeenBy(putThis,mob)
					&&(!sheathMap.containsKey(putThis)))
					{
						sheathable=putThis;
						if((container!=null)
						&&(canSheathIn(putThis,container,sheaths)))
							sheathMap.put(putThis, container);
						else
						{
							for(final Iterator<Pair<Container,int[]>> p = sheaths.pairIterator();p.hasNext();)
							{
								final Pair<Container,int[]> sheathPair = p.next();
								final Container sheath=sheathPair.first;
								if(canSheathIn(putThis,sheath,sheaths))
								{
									sheathMap.put(putThis, sheath);
									break;
								}
							}
						}
					}
				}
				addendumStr="."+(++addendum);
			}
		}

		if(sheathMap.size()==0)
		{
			if(!noerrors)
			{
				if(sheaths.size()==0)
					CMLib.commands().postCommandFail(mob,origCmds,L("You are not wearing an appropriate sheath."));
				else
				if(sheathable!=null)
					CMLib.commands().postCommandFail(mob,origCmds,L("You aren't wearing anything you can sheath @x1 in.",sheathable.name()));
				else
				if(commands.size()==0)
					CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be wielding anything you can sheath."));
				else
					CMLib.commands().postCommandFail(mob,origCmds,L("You don't seem to be wielding that."));
			}
		}
		else
		for(final Iterator<Pair<Item,Container>> p=sheathMap.pairIterator();p.hasNext();)
		{
			final Pair<Item,Container> P=p.next();
			final Item putThis=P.first;
			final Container container=P.second;
			if(CMLib.commands().postRemove(mob,putThis,true))
			{
				final CMMsg putMsg=CMClass.getMsg(mob,container,putThis,CMMsg.MSG_PUT,((quiet?null:L("<S-NAME> sheath(s) <O-NAME> in <T-NAME>."))));
				if(mob.location().okMessage(mob,putMsg))
					mob.location().send(mob,putMsg);
			}
		}
		return false;
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID(), CMath.div(CMProps.getIntVar(CMProps.Int.DEFCMDTIME),200.0));
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID(), CMath.div(CMProps.getIntVar(CMProps.Int.DEFCOMCMDTIME),200.0));
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

}
