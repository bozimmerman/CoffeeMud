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

import java.util.*;

/*
   Copyright 2004-2019 Bo Zimmerman

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

	public static Map<String,List<Item>> instances=new Hashtable<String,List<Item>>();
	
	public static boolean[]		playersLoaded	= new boolean[1];
	protected volatile boolean	norecurse		= false;
	protected boolean			destroy			= false;
	protected int				maxItems		= 0;

	@Override
	public String accountForYourself()
	{
		if(CMath.s_int(text())<=0)
			return "Only 1 may exist";
		return "Only "+CMath.s_int(text())+" may exist.";
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		maxItems=1;
		if(newMiscText.length()>0)
		{
			final int x=newMiscText.indexOf(';');
			final String numStr;
			if(x<0)
				numStr = newMiscText;
			else
				numStr=newMiscText.substring(0,x);
			if(CMath.isInteger(numStr))
				maxItems = CMath.s_int(numStr);
		}
	}
	
	protected void countIfNecessary(final Item I)
	{
		if(CMLib.flags().isInTheGame(I,false))
		{
			List<Item> myInstances=null;
			synchronized(instances)
			{
				if(!instances.containsKey(I.Name()))
				{
					myInstances=new Vector<Item>();
					instances.put(I.Name(),myInstances);
					myInstances.add(I);
				}
			}
			myInstances=instances.get(I.Name());
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
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
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
			synchronized(playersLoaded)
			{
				if(!playersLoaded[0])
				{
					playersLoaded[0]=true;
					Log.sysOut("Prop_LimitedItems","Checking player inventories");
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
								&&(p.second.indexOf(affected.Name())>0))
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
					Log.sysOut("Prop_LimitedItems","Done checking player inventories");
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
