package com.planet_ink.coffee_mud.Abilities;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Locales.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.CharClasses.*;
import java.util.*;

public class ItemRejuv extends StdAbility
{
	public Room myProperLocation=null;
	private Vector contents=new Vector();
	private Vector ccontents=new Vector();
	
	public ItemRejuv()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="ItemRejuv";
		displayText="(ItemRejuv)";
		miscText="";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(999);
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new ItemRejuv();
	}

	public static void loadContent(ItemRejuv ability, 
								   Item item, 
								   Room room)
	{
		ability.contents.addElement(item);
		
		Item newItem=(Item)item.copyOf();
		newItem.setLocation(item.location());
		ability.ccontents.addElement(newItem);
		
		for(int r=0;r<room.numItems();r++)
		{
			Item content=room.fetchItem(r);
			if(content.location()==item)
				loadContent(ability,content,room);
		}
	}
	
	public static void loadMeUp(Item item, Room room)
	{
		unloadIfNecessary(item);
		
		ItemRejuv ability=new ItemRejuv();
		ability.myProperLocation=room;
		item.addAffect(ability);
		loadContent(ability,item,room);
		ServiceEngine.startTickDown(ability,ServiceEngine.ROOM_ITEM_REJUV,item.envStats().rejuv());
	}
	
	public static void unloadIfNecessary(Item item)
	{
		ItemRejuv a=(ItemRejuv)item.fetchAffect(new ItemRejuv().ID());
		if(a!=null)
			a.unInvoke();
	}
	
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
					if(thatItem.location()==thisItem)
						thatItem.setLocation(newThisItem);
				}
				thisItem=newThisItem;
				if(thisItem instanceof Container)
				{
					((Container)thisItem).isOpen=!((Container)thisItem).hasALid;
					((Container)thisItem).isLocked=((Container)thisItem).hasALock;
				}
				room.addItem(thisItem);
			}
			
			thisItem.setLocation(((Item)ccontents.elementAt(i)).location());
		}
	}
	public boolean tick(int tickID)
	{
		if(!super.tick(tickID))
			return false;
		
		Item item=(Item)affected;
		if((item==null)||(myProperLocation==null))
			return false;

		if(tickID==ServiceEngine.ROOM_ITEM_REJUV)
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
				((Container)item).isOpen=!((Container)item).hasALid;
				((Container)item).isLocked=((Container)item).hasALock;
			}
		}
		return true;
	}
}
