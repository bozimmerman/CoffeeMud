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
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class ItemRejuv extends StdAbility implements ItemTicker
{
	@Override
	public String ID()
	{
		return "ItemRejuv";
	}

	private final static String	localizedName	= CMLib.lang().L("ItemRejuv");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(ItemRejuv)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	protected Room			myProperLocation	= null;
	protected Vector<Item>	contents			= new Vector<Item>();
	protected Vector<Item>	ccontents			= new Vector<Item>();

	public synchronized void loadContent(ItemTicker ticker, Item item, Room room)
	{
		if(ticker instanceof ItemRejuv)
		{
			final ItemRejuv ability=(ItemRejuv)ticker;
			ability.contents.add(item);

			final Item newItem=(Item)item.copyOf();
			newItem.stopTicking();
			newItem.setContainer(item.container());
			ability.ccontents.add(newItem);

			for(int r=0;r<room.numItems();r++)
			{
				final Item content=room.getItem(r);
				if((content!=null)&&(content.container()==item))
					loadContent(ability,content,room);
			}
		}
	}

	@Override
	public Room properLocation()
	{
		return myProperLocation;
	}

	// this was briefly synchronized to fix some problem
	// but it caused a sync lock with service  engine.
	@Override
	public void setProperLocation(Room room)
	{
		myProperLocation = room;
	}

	@Override
	public void loadMeUp(Item item, Room room)
	{
		unloadIfNecessary(item);
		contents=new Vector<Item>();
		ccontents=new Vector<Item>();
		final ItemRejuv ability=new ItemRejuv();
		ability.myProperLocation=room;
		if(item.fetchEffect(ability.ID())==null)
			item.addEffect(ability);
		ability.setSavable(false);
		loadContent(ability,item,room);
		contents.trimToSize();
		ccontents.trimToSize();
		CMLib.threads().startTickDown(ability,Tickable.TICKID_ROOM_ITEM_REJUV,item.phyStats().rejuv());
	}

	@Override
	public void unloadIfNecessary(Item item)
	{
		final ItemRejuv a=(ItemRejuv)item.fetchEffect(ID());
		if(a!=null)
			a.unInvoke();
	}

	@Override
	public String accountForYourself()
	{
		return "";
	}

	@Override
	public boolean isVerifiedContents(Item item)
	{
		if(item==null)
			return false;
		return contents.contains(item);
	}

	public synchronized boolean verifyFixContents()
	{
		final Room R=myProperLocation;
		if((R==null)||(R.amDestroyed())
		||((R.getArea()!=null)&&(R.getArea().amDestroyed())))
			return false;
		for(int i=0;i<contents.size();i++)
		{
			final Item thisItem=contents.elementAt(i);
			if(thisItem!=null)
			{
				final Container thisContainer=ccontents.elementAt(i).container();
				if((!R.isContent(thisItem))
				&&((!CMLib.flags().isMobile(thisItem)) || (!CMLib.flags().isInTheGame(thisItem,true))))
				{
					final Item newThisItem=(Item)ccontents.elementAt(i).copyOf();
					contents.setElementAt(newThisItem,i);
					for(int c=0;c<ccontents.size();c++)
					{
						final Item thatItem=ccontents.elementAt(c);
						if((thatItem.container()==thisItem)&&(newThisItem instanceof Container))
							thatItem.setContainer((Container)newThisItem);
					}
					if(newThisItem instanceof Container)
					{
						final Container C=(Container)newThisItem;
						final boolean locked=C.defaultsLocked();
						final boolean open=(!locked) && (!C.defaultsClosed());
						C.setDoorsNLocks(C.hasADoor(),open,C.defaultsClosed(),C.hasALock(),locked,C.defaultsLocked());
					}
					newThisItem.setExpirationDate(0);
					R.addItem(newThisItem);
					
					newThisItem.setContainer(thisContainer);
				}
				else
					thisItem.setContainer(thisContainer);
			}
		}
		return this.myProperLocation != null;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		final Item item=(Item)affected;
		final Room R=myProperLocation;
		if((item==null)
		||(R==null)||(R.amDestroyed())
		||((R.getArea()!=null)&&(R.getArea().amDestroyed())))
			return false;

		if(tickID==Tickable.TICKID_ROOM_ITEM_REJUV)
		{
			if((CMLib.flags().canNotBeCamped(item)||CMLib.flags().canNotBeCamped(R))
			&& (R.numPCInhabitants() > 0) 
			&& (!CMLib.tracking().isAnAdminHere(R,false)))
			{
				CMLib.threads().setTickPending(ticking,Tickable.TICKID_ROOM_ITEM_REJUV);
				return true; // it will just come back next time
			}
			if(!verifyFixContents())
				return false;
			if((!R.isContent(item))
			&&((!CMLib.flags().isMobile(item)) || (!CMLib.flags().isInTheGame(item,true))))
			{
				unloadIfNecessary(item);
				loadMeUp(contents.elementAt(0),R);
				return false;
			}
			if(item instanceof Container)
			{
				final Container C=(Container)item;
				final boolean locked=C.defaultsLocked() && C.hasALock() && C.defaultsClosed();
				final boolean open=(!locked) && (!C.defaultsClosed());
				C.setDoorsNLocks(C.hasADoor(),open,C.defaultsClosed(),C.hasALock(),locked,C.defaultsClosed());
			}
		}
		return true;
	}
}
