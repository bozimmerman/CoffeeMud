package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	public String ID() { return "ItemRejuv"; }
	public String name(){ return "ItemRejuv";}
	public String displayText(){ return "(ItemRejuv)";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.MALICIOUS;}
	private Room myProperLocation=null;
	private Vector contents=new Vector();
	private Vector ccontents=new Vector();

	public void loadContent(ItemTicker ticker,
							Item item,
							Room room)
	{
		if(ticker instanceof ItemRejuv)
		{
			ItemRejuv ability=(ItemRejuv)ticker;
			ability.contents.addElement(item);

			Item newItem=(Item)item.copyOf();
			newItem.setContainer(item.container());
			ability.ccontents.addElement(newItem);

			for(int r=0;r<room.numItems();r++)
			{
				Item content=room.fetchItem(r);
				if((content!=null)&&(content.container()==item))
					loadContent(ability,content,room);
			}
		}
	}

	public Room properLocation(){return myProperLocation;}
	public void setProperLocation(Room room)
	{ myProperLocation=room; }
	public void loadMeUp(Item item, Room room)
	{
		unloadIfNecessary(item);

		ItemRejuv ability=new ItemRejuv();
		ability.myProperLocation=room;
		if(item.fetchEffect(ability.ID())==null)
			item.addEffect(ability);
		ability.setBorrowed(item,true);
		loadContent(ability,item,room);
		CMClass.ThreadEngine().startTickDown(ability,MudHost.TICK_ROOM_ITEM_REJUV,item.envStats().rejuv());
	}

	public void unloadIfNecessary(Item item)
	{
		ItemRejuv a=(ItemRejuv)item.fetchEffect(new ItemRejuv().ID());
		if(a!=null)
			a.unInvoke();
	}

	public String accountForYourself()
	{ return ""; }

	public void verifyFixContents(Item item, Room room)
	{
		for(int i=0;i<contents.size();i++)
		{
			Item thisItem=(Item)contents.elementAt(i);
			if(!room.isContent(thisItem))
			{
				Item newThisItem=(Item)((Item)ccontents.elementAt(i)).copyOf();

				contents.setElementAt(newThisItem,i);
				for(int c=0;c<ccontents.size();c++)
				{
					Item thatItem=(Item)ccontents.elementAt(c);
					if(thatItem.container()==thisItem)
						thatItem.setContainer(newThisItem);
				}
				thisItem=newThisItem;
				if(thisItem instanceof Container)
				{
					Container C=(Container)thisItem;
					boolean open=!C.hasALid();
					boolean locked=C.hasALock();
					C.setLidsNLocks(C.hasALid(),open,C.hasALock(),locked);
				}
				thisItem.setDispossessionTime(0);
				room.addItem(thisItem);
			}
			thisItem.setContainer(((Item)ccontents.elementAt(i)).container());
		}
	}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		Item item=(Item)affected;
		if((item==null)||(myProperLocation==null))
			return false;

		if(tickID==MudHost.TICK_ROOM_ITEM_REJUV)
		{
			verifyFixContents(item,myProperLocation);
			if(!myProperLocation.isContent(item))
			{
				unloadIfNecessary(item);
				loadMeUp((Item)contents.elementAt(0),myProperLocation);
				return false;
			}
			if(item instanceof Container)
			{
				Container C=(Container)item;
				boolean open=!C.hasALid();
				boolean locked=C.hasALock();
				C.setLidsNLocks(C.hasALid(),open,C.hasALock(),locked);
			}
		}
		return true;
	}
}
