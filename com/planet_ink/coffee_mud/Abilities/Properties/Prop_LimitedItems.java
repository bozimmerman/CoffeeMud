package com.planet_ink.coffee_mud.Abilities.Properties;
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

import java.lang.ref.WeakReference;
import java.util.*;

/*
   Copyright 2004-2020 Bo Zimmerman

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
public class Prop_LimitedItems extends Property
{
	@Override
	public String ID()
	{
		return "Prop_LimitedItems";
	}

	@Override
	public String name()
	{
		return "Limited Item";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	public static Map<String, List<Item>>	instances			= new Hashtable<String, List<Item>>();
	public static Set<String>				inventoriesChecked	= new HashSet<String>();

	protected volatile WeakReference<Physical> lastAdded = new WeakReference<Physical>(null);

	protected volatile boolean	norecurse		= false;
	protected boolean			destroy			= false;
	protected int				maxItems		= 0;
	protected String			id				= null;

	@Override
	public String accountForYourself()
	{
		if(CMath.s_int(text())<=0)
			return "Only 1 may exist";
		final int x=text().indexOf(';');
		final String numStr;
		if(x<0)
			numStr = text();
		else
			numStr=text().substring(0,x);
		return "Only "+CMath.s_int(numStr)+" may exist.";
	}

	@Override
	public void setAffectedOne(final Physical P)
	{
		super.setAffectedOne(P);
		if(P != null)
			lastAdded = new WeakReference<Physical>(P);
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		norecurse = false;
		maxItems=1;
		this.id=null;
		if(newMiscText.length()>0)
		{
			final int x=newMiscText.indexOf(';');
			final String numStr;
			if(x<0)
				numStr = newMiscText;
			else
			{
				final String parms = newMiscText.substring(x+1).trim();
				final String newId = CMParms.getParmStr(parms, "ID", "");
				if(newId.trim().length()>0)
					this.id=newId;
				numStr=newMiscText.substring(0,x);
			}
			if(CMath.isInteger(numStr))
				maxItems = CMath.s_int(numStr);
		}
	}

	@Override
	public CMObject copyOf()
	{
		final Physical affP = affected;
		final Prop_LimitedItems meCopy = (Prop_LimitedItems)super.copyOf();
		meCopy.norecurse=false;
		if((affP instanceof Item)
		&&(((Item)affP).owner() != null)
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		&&(!canBeUninvoked()))
		{
			CMLib.threads().scheduleRunnable(new Runnable(){
				final Prop_LimitedItems chkA = meCopy;
				final String miscText = text();
				@Override
				public void run()
				{
					final Physical chkAoldP;
					synchronized(chkA)
					{
						chkAoldP = chkA.lastAdded.get();
					}
					if(chkAoldP instanceof Item)
					{
						final Item I=(Item)chkAoldP;
						final ItemPossessor P;
						synchronized(I)
						{
							P=I.owner();
						}
						if((!I.amDestroyed())
						&&(CMLib.flags().isInTheGame(I, false))
						&&(I.fetchEffect(ID())==null))
						{
							chkA.lastAdded = new WeakReference<Physical>(null);
							final Prop_LimitedItems copyMe = new Prop_LimitedItems();
							copyMe.setMiscText(miscText);
							copyMe.norecurse=false;
							I.addNonUninvokableEffect(copyMe);
							if(P instanceof Physical)
								((Physical)P).recoverPhyStats();
						}
					}
				}
			}, 5000);
		}
		return meCopy;
	}

	protected String getId(final Physical I)
	{
		if(I == affected)
		{
			if(this.id != null)
				return this.id;
			return I.Name();
		}
		else
		if(I != null)
		{
			final Prop_LimitedItems liA = (Prop_LimitedItems)I.fetchEffect(ID());
			if((liA==null) || (liA.id == null))
				return I.Name();
			return liA.id;
		}
		return "N/A";
	}

	protected void countIfNecessary(final Item I)
	{
		final String Iid = getId(I);
		if(CMLib.flags().isInTheGame(I,false))
		{
			List<Item> myInstances=null;
			synchronized(instances)
			{
				if(!instances.containsKey(Iid))
				{
					myInstances=new Vector<Item>();
					instances.put(Iid,myInstances);
					myInstances.add(I);
				}
			}
			myInstances=instances.get(Iid);
			if(myInstances!=null)
			{
				synchronized(myInstances)
				{
					if(!myInstances.contains(I))
					{
						if(maxItems==0)
							maxItems++;
						int num=0;
						for(int i=myInstances.size()-1;i>=0;i--)
						{
							if(!myInstances.get(i).amDestroyed())
								num++;
							else
								myInstances.remove(i);
						}
						if(num>=maxItems)
						{
							I.destroy();
							destroy=true;
						}
						else
							myInstances.add(I);
					}
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if(((msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_LOOK)
			||(msg.targetMinor()==CMMsg.TYP_GET))
		&&(CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
		&&(affected instanceof Item)
		&&((!(((Item)affected).owner() instanceof MOB))
		   ||((MOB)((Item)affected).owner()).playerStats()==null))
			countIfNecessary((Item)affected);
	}

	@Override
	public void affectPhyStats(final Physical E, final PhyStats affectableStats)
	{
		super.affectPhyStats(E,affectableStats);

		if((!(E instanceof Item))||(((Item)E).owner()==null))
			return;

		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return;

		if(norecurse)
			return;
		try
		{
			norecurse=true;

			final Physical affected = this.affected;
			if(affected == null)
				return;

			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.SENSE_UNLOCATABLE);
			final String affId = getId(affected);
			if(!inventoriesChecked.contains(affId))
			{
				boolean doScan = false;
				synchronized(inventoriesChecked)
				{
					if(!inventoriesChecked.contains(affId))
						doScan = true;
				}
				if(doScan)
				{
					synchronized((ID()+"_"+affId).intern())
					{
						if(!inventoriesChecked.contains(affId))
						{
							inventoriesChecked.add(affId);
							Log.sysOut("Prop_LimitedItems","Checking player inventories for "+affId);
							// known problem: players from different hosts who share a world map might allow
							// duplicates/excess items.
							final List<String> V=CMLib.database().getUserList();
							for(final String name : V)
							{
								MOB M=CMLib.players().getPlayer(name);
								if(M==null)
								{
									final PairList<String,String> matchingItems = CMLib.database().DBReadPlayerItemData(name,ID());
									for(final Pair<String,String> p : matchingItems)
									{
										if(p.first.equalsIgnoreCase(affected.ID())
										&&(p.second.indexOf(affId)>0))
										{
											M=CMLib.players().getLoadPlayer(name);
											break;
										}
									}
									if(M!=null)
									{
										final Room R=M.location();
										if((R!=null)
										&&(R.isInhabitant(M)))
											Log.sysOut("Prop_LimitedItems",M.name()+" was found in the game, even though this is supposed to be a temporary load.");
										else
										if(M.session()!=null)
											Log.sysOut("Prop_LimitedItems",M.name()+" had a session, even though this is supposed to be a temporary load.");
									}
								}
							}
							Log.sysOut("Prop_LimitedItems","Done checking player inventories for "+affId);
						}
					}
				}
			}
			if((((Item)affected).owner() instanceof MOB)
			&&(((MOB)((Item)affected).owner()).playerStats()!=null))
				countIfNecessary((Item)affected);
			if(destroy)
				((Item)affected).destroy();
		}
		finally
		{
			norecurse=false;
		}
	}
}
