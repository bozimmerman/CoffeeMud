package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.interfaces.ItemPossessor.Expire;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Function;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.Authority;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.ClanVote;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine.PlayerData;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.StdRace;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.lang.ref.WeakReference;
import java.util.*;

/*
Copyright 2000-2011 Bo Zimmerman

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
/**
 * Abstract collection of item objects, complete with some 
 * finders and various accessors.  Also, the copyOf method
 * does a deep copy.
 */
public class DefaultItemCollection implements ItemCollection
{
	private SVector<Item> contents = new SVector<Item>(0);
	
	public String ID() { return "DefaultItemCollection"; }
	
	public CMObject copyOf() 
	{
		DefaultItemCollection c=(DefaultItemCollection)newInstance();
		for(int i=0;i<contents.size();i++)
		{
			Item I=contents.get(i);
			Item I2=(Item)I.copyOf();
			I2.setOwner(I.owner());
			c.contents.add(I2);
		}
		for(int i=0;i<contents.size();i++)
		{
			Item I=contents.get(i);
			Item I2=c.contents.get(i);
			if(I.container() != null)
				for(int i2=0;i2<contents.size();i2++)
					if(I.container() == contents.get(i2))
					{
						I2.setContainer((Container)c.contents.get(i2));
						break;
					}
		}
		return c;
	}
	
	public void initializeClass() {}
	public CMObject newInstance() { return new DefaultItemCollection(); }
	public int compareTo(CMObject o) { return o==this?0:1; }
	
	public Item findItem(String itemID)
	{
		Item item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,true);
		if(item==null) item=(Item)CMLib.english().fetchEnvironmental(contents,itemID,false);
		return item;
	}
	public Enumeration<Item> items() { return contents.elements();}
	
	public Item findItem(Item goodLocation, String itemID)
	{
		Item item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(item==null) item=CMLib.english().fetchAvailableItem(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return item;
	}
	
	public List<Item> findItems(Item goodLocation, String itemID)
	{
		List<Item> items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,true);
		if(items.size()==0)
			items=CMLib.english().fetchAvailableItems(contents,itemID,goodLocation,Wearable.FILTER_ANY,false);
		return items;
	}
	
	@SuppressWarnings("unchecked")
	public List<Item> findItems(String itemID)
	{
		List items=CMLib.english().fetchEnvironmentals(contents,itemID,true);
		if(items.size()==0)
			items=CMLib.english().fetchEnvironmentals(contents,itemID, false);
		return items;
	}
	
	public void addItem(Item item)
	{
		contents.addElement(item);
		item.recoverPhyStats();
	}
	
	public void delItem(Item item)
	{
		contents.removeElement(item);
		item.recoverPhyStats();
	}
	
	public int numItems()
	{
		return contents.size();
	}
	
	public boolean isContent(Item item)
	{
		return contents.contains(item);
	}
	
	public Item getItem(int i)
	{
		try
		{
            return (Item)contents.elementAt(i);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
}
