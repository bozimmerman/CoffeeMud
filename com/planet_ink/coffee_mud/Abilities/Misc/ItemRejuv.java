package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class ItemRejuv extends StdAbility implements ItemTicker
{
	public String ID() { return "ItemRejuv"; }
	public String name(){ return "ItemRejuv";}
	public String displayText(){ return "(ItemRejuv)";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
    protected Room myProperLocation=null;
    protected Vector contents=new Vector();
    protected Vector ccontents=new Vector();

	public void loadContent(ItemTicker ticker,
							Item item,
							Room room)
	{
		if(ticker instanceof ItemRejuv)
		{
			ItemRejuv ability=(ItemRejuv)ticker;
			ability.contents.addElement(item);

			Item newItem=(Item)item.copyOf();
			newItem.stopTicking();
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
        contents=new Vector();
        ccontents=new Vector();
		ItemRejuv ability=new ItemRejuv();
		ability.myProperLocation=room;
		if(item.fetchEffect(ability.ID())==null)
			item.addEffect(ability);
		ability.setSavable(false);
		loadContent(ability,item,room);
		contents.trimToSize();
		ccontents.trimToSize();
		CMLib.threads().startTickDown(ability,Tickable.TICKID_ROOM_ITEM_REJUV,item.envStats().rejuv());
	}

	public void unloadIfNecessary(Item item)
	{
		ItemRejuv a=(ItemRejuv)item.fetchEffect(new ItemRejuv().ID());
		if(a!=null)
			a.unInvoke();
	}

	public String accountForYourself()
	{ return ""; }

	public void verifyFixContents()
	{
		for(int i=0;i<contents.size();i++)
		{
			Item thisItem=(Item)contents.elementAt(i);
			if((!myProperLocation.isContent(thisItem))
			&&((!CMLib.flags().isMobile(thisItem)) || (!CMLib.flags().isInTheGame(thisItem,true))))
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
				thisItem.setExpirationDate(0);
				myProperLocation.addItem(thisItem);
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

		if(tickID==Tickable.TICKID_ROOM_ITEM_REJUV)
		{
			verifyFixContents();
			if((!myProperLocation.isContent(item))
			&&((!CMLib.flags().isMobile(item)) || (!CMLib.flags().isInTheGame(item,true))))
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
