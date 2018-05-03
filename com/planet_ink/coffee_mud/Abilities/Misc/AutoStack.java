package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.MOB.Attrib;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2015-2018 Bo Zimmerman

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
public class AutoStack extends StdAbility
{
	@Override
	public String ID()
	{
		return "AutoStack";
	}

	private final static String	localizedName	= CMLib.lang().L("AutoStack");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS | Ability.CAN_AREAS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PROPERTY;
	}

	protected final static int	INTERVAL				= (2 * 60) / 4000;

	protected int				numberOfItemsToTrigger	= 1000;
	protected int				intervalTicks			= INTERVAL;
	protected long				nextTriggeringTime		= System.currentTimeMillis() + (INTERVAL * 4000);
	protected int				numberInGroupToTrigger	= 10;

	@Override
	public void setMiscText(String newMiscText)
	{
		super.setMiscText(newMiscText);
		numberOfItemsToTrigger = CMParms.getParmInt(newMiscText, "TRIGGERCOUNT", 1000);
		numberInGroupToTrigger = CMParms.getParmInt(newMiscText, "GROUPCOUNT", 10);
		intervalTicks = CMParms.getParmInt(newMiscText, "CHECKTICKS", INTERVAL);
		tickDown = intervalTicks;
		nextTriggeringTime = System.currentTimeMillis() + (CMProps.getTickMillis() * tickDown);
	}

	protected synchronized void packageAll(Room R)
	{
		if (R == null)
			return;

		if (R.numItems() < numberOfItemsToTrigger)
			return;

		final Map<String, List<Item>> groupedByName = new TreeMap<String, List<Item>>();
		final List<PackagedItems> oldPackages = new LinkedList<PackagedItems>();
		for (Enumeration<Item> i = R.items(); i.hasMoreElements();)
		{
			final Item I = i.nextElement();
			if (I instanceof PackagedItems)
				oldPackages.add((PackagedItems) I);
			else 
			if ((I != null) && (!(I instanceof RawMaterial)))
			{
				List<Item> itemList = groupedByName.get(I.name());
				if (itemList == null)
				{
					itemList = new LinkedList<Item>();
					groupedByName.put(I.name(), itemList);
				}
				itemList.add(I);
			}
		}

		for (final Iterator<List<Item>> i = groupedByName.values().iterator(); i.hasNext();)
		{
			final List<Item> itemList = i.next();
			if (itemList.size() < numberInGroupToTrigger)
				i.remove();
			else
			{
				final List<Pair<Item, List<Item>>> sameItems = new LinkedList<Pair<Item, List<Item>>>();
				boolean winner = false;
				for (final Item I : itemList)
				{
					winner = false;
					for (final Pair<Item, List<Item>> trialList : sameItems)
					{
						if((I.container()==trialList.first.container())
						&&(trialList.first.sameAs(I)))
						{
							winner = true;
							trialList.second.add(I);
						}
					}
					if (!winner)
					{
						final LinkedList<Item> newList = new LinkedList<Item>();
						newList.add(I);
						sameItems.add(new Pair<Item, List<Item>>(I, newList));
					}
				}
				for (final Iterator<Pair<Item, List<Item>>> t = sameItems.iterator(); t.hasNext();)
				{
					final Pair<Item, List<Item>> set = t.next();
					if (set.second.size() < numberInGroupToTrigger)
						t.remove();
				}
				if (sameItems.size() > 0)
				{
					final List<Pair<Item, PackagedItems>> oldPackChecks = new LinkedList<Pair<Item, PackagedItems>>();
					try
					{
						for (final PackagedItems P : oldPackages)
							oldPackChecks.add(new Pair<Item, PackagedItems>(P.peekFirstItem(), P));
						oldPackages.clear();
						final List<PackagedItems> newPackages = new LinkedList<PackagedItems>();
						for (final Iterator<Pair<Item, List<Item>>> t = sameItems.iterator(); t.hasNext();)
						{
							final Pair<Item, List<Item>> set = t.next();
							if (set.second.size() < numberInGroupToTrigger)
								t.remove();
							else
							{
								boolean alreadyPacked = false;
								for (Pair<Item, PackagedItems> oldPackPair : oldPackChecks)
								{
									if((set.first.name().equals(oldPackPair.first.name()))
									&&(set.first.container() == oldPackPair.second.container()))
									{
										if (set.first.sameAs(oldPackPair.first))
										{
											alreadyPacked = true;
											oldPackPair.second.packageMe(oldPackPair.first, oldPackPair.second.numberOfItems() + set.second.size());
										}
									}
								}
								if (!alreadyPacked)
								{
									final PackagedItems newPack = (PackagedItems) CMClass.getBasicItem("GenPackagedStack");
									newPack.packageMe(set.first, set.second.size());
									long time = set.first.expirationDate();
									if (time != 0)
									{
										for (final Item I : set.second)
										{
											if (I.expirationDate() == 0)
											{
												time = 0;
												break;
											}
											else 
											if (I.expirationDate() > time)
												time = I.expirationDate();
										}
									}
									newPack.setExpirationDate(time);
									newPack.setContainer(set.first.container());
									newPackages.add(newPack);
								}
								for (final Item I : set.second)
								{
									I.destroy();
								}
							}
						}
						for (final PackagedItems newPack : newPackages)
						{
							R.addItem(newPack); // yay!
						}
					}
					finally
					{
						for (final Pair<Item, PackagedItems> oldPackPair : oldPackChecks)
							oldPackPair.first.destroy();
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(affected instanceof Room) // because areas can rely on ticks
		{
			if(System.currentTimeMillis() >= nextTriggeringTime)
			{
				final Room R=(Room)affected;
				if((R!=null)&&(!R.amDestroyed()))
				{
					if(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED)
					&&(!CMProps.getBoolVar(CMProps.Bool.MUDSHUTTINGDOWN)))
					{
						tickDown = intervalTicks;
						nextTriggeringTime = System.currentTimeMillis() + (CMProps.getTickMillis() * tickDown);
						this.packageAll(R);
						tickDown = intervalTicks;
						nextTriggeringTime = System.currentTimeMillis() + (CMProps.getTickMillis() * tickDown);
					}
				}
			}
		}
		super.executeMsg(myHost, msg);
	}
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(affected instanceof Room)
		{
			final Room R=(Room)affected;
			if((R==null)||(R.amDestroyed()))
				return false;
			
			if (tickID == Tickable.TICKID_MOB)
			{
				if((--tickDown)<=0)
				{
					tickDown = intervalTicks;
					nextTriggeringTime = System.currentTimeMillis() + (CMProps.getTickMillis() * tickDown);
					this.packageAll(R);
					tickDown = intervalTicks;
					nextTriggeringTime = System.currentTimeMillis() + (CMProps.getTickMillis() * tickDown);
				}
			}
			return true;
		}
		else
		if(affected instanceof Area)
		{
			final Area A=(Area)affected;
			if((A==null)||(A.amDestroyed()))
				return false;
			
			if (tickID == Tickable.TICKID_MOB)
			{
				if((--tickDown)<=0)
				{
					tickDown = intervalTicks;
					nextTriggeringTime = System.currentTimeMillis() + (CMProps.getTickMillis() * tickDown);
					for(final Enumeration<Room> r=A.getMetroMap();r.hasMoreElements();)
					{
						this.packageAll(r.nextElement());
					}
					tickDown = intervalTicks;
					nextTriggeringTime = System.currentTimeMillis() + (CMProps.getTickMillis() * tickDown);
				}
			}
		}
		return false;
	}
}
