package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ItemTransporter extends Property
{
	Room roomDestination=null;
	MOB mobDestination=null;
	Item nextDestination=null;
	Room roomMover=null;
	MOB mobMover=null;
	Item container=null;
	public Prop_ItemTransporter()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Item Transporter";
	}

	public Environmental newInstance()
	{
		return new Prop_ItemTransporter();
	}

	public String accountForYourself()
	{ return "Item Transporter";	}

	public Item ultimateParent(Item item)
	{
		if(item==null) return null;
		if(item.location()==null) return item;
		if(item.location().location()==item)
			item.location().setLocation(null);
		if(item.location()==item)
			item.setLocation(null);
		return ultimateParent(item.location());
	}

	private synchronized boolean setDestination()
	{
		Vector possibilities=new Vector();
		roomDestination=null;
		mobDestination=null;
		nextDestination=null;
		for(int r=0;r<CMMap.numRooms();r++)
		{
			Room room=CMMap.getRoom(r);
			Ability A=room.fetchAffect("Prop_ItemTransReceiver");
			if((A!=null)&&(A.text().equalsIgnoreCase(text())))
				possibilities.addElement(room);
			for(int i=0;i<room.numItems();i++)
			{
				Item item=room.fetchItem(i);
				if((item!=null)&&(item!=affected))
				{
					A=item.fetchAffect("Prop_ItemTransReceiver");
					if((A!=null)&&(A.text().equalsIgnoreCase(text())))
						possibilities.addElement(item);
				}
			}
			for(int m=0;m<room.numInhabitants();m++)
			{
				MOB mob=room.fetchInhabitant(m);
				if((mob!=null)&&(mob!=affected))
				{
					A=mob.fetchAffect("Prop_ItemTransReceiver");
					if((A!=null)&&(A.text().equalsIgnoreCase(text())))
						possibilities.addElement(mob);
					for(int i=0;i<mob.inventorySize();i++)
					{
						Item item=mob.fetchInventory(i);
						if((item!=null)&&(item!=affected))
						{
							A=item.fetchAffect("Prop_ItemTransReceiver");
							if((A!=null)&&(A.text().equalsIgnoreCase(text())))
								possibilities.addElement(item);
						}
					}
				}
			}
		}
		if(possibilities.size()>0)
		{
			Environmental E=(Environmental)possibilities.elementAt(Dice.roll(1,possibilities.size(),-1));
			nextDestination=null;
			if(E instanceof Room)
				roomDestination=(Room)E;
			else
			if(E instanceof MOB)
				mobDestination=(MOB)E;
			else
			if(E instanceof Item)
			{
				nextDestination=(Item)E;
				if((nextDestination!=null)&&(nextDestination.myOwner()!=null))
				{
					if(nextDestination.myOwner() instanceof Room)
						roomDestination=(Room)nextDestination.myOwner();
					else
					if(nextDestination.myOwner() instanceof MOB)
						mobDestination=(MOB)nextDestination.myOwner();
				}
				else
					nextDestination=null;
			}
		}
		if((mobDestination==null)&&(roomDestination==null))
			return false;
		return true;
	}
	
	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(affected==null) return true;
		if((affect.amITarget(affected))
		&&((affect.targetMinor()==Affect.TYP_PUT)
		   ||(affect.targetMinor()==Affect.TYP_DROP)
		   ||(affect.targetMinor()==Affect.TYP_GIVE))
		&&(affect.tool()!=null)
		&&(affect.tool() instanceof Item))
		{
			if(!setDestination())
			{
				affect.source().tell("The transporter has no possible ItemTransReceiver with the code '"+text()+"'.");
				return false;
			}
			setMover();
		}
		else
		if((affected instanceof MOB)
		&&(affect.amISource((MOB)affected))
		&&(affect.targetMinor()==Affect.TYP_GET)
		&&(affect.target() !=null)
		&&(affect.target() instanceof Item))
		{
			if(!setDestination())
			{
				affect.source().tell("The transporter has no possible ItemTransReceiver with the code '"+text()+"'.");
				return false;
			}
			setMover();
		}
		return true;
	}
	
	public void setMover()
	{
		if(affected instanceof Room)
			roomMover=(Room)affected;
		else
		if(affected instanceof MOB)
			mobMover=(MOB)affected;
		else
		if(affected instanceof Item)
		{
			container=(Item)affected;
			if((container.myOwner()!=null)&&(container.myOwner() instanceof Room))
				roomMover=(Room)container.myOwner();
			else
			if((container.myOwner()!=null)&&(container.myOwner() instanceof MOB))
				mobMover=(MOB)container.myOwner();
		}
	}
	
	public void tryToMoveStuff()
	{
		Vector itemsToMove=new Vector();
		if(roomMover!=null)
		{
			for(int i=0;i<roomMover.numItems();i++)
			{
				Item item=roomMover.fetchItem(i);
				if((item!=null)
				   &&(item!=container)
				   &&(item.amWearingAt(Item.INVENTORY))
				   &&((item.location()==container)||(ultimateParent(item)==container)))
				   itemsToMove.addElement(item);
			}
			for(int i=0;i<itemsToMove.size();i++)
				roomMover.delItem((Item)itemsToMove.elementAt(i));
		}
		else
		if(mobMover!=null)
		{
			int oldNum=itemsToMove.size();
			for(int i=0;i<mobMover.inventorySize();i++)
			{
				Item item=mobMover.fetchInventory(i);
				if((item!=null)
				   &&(item!=container)
				   &&(item.amWearingAt(Item.INVENTORY))
				   &&((item.location()==container)||(ultimateParent(item)==container)))
				   itemsToMove.addElement(item);
			}
			for(int i=oldNum;i<itemsToMove.size();i++)
				mobMover.delInventory((Item)itemsToMove.elementAt(i));
		}
		if(itemsToMove.size()>0)
		{
			if(roomDestination!=null)
				for(int i=0;i<itemsToMove.size();i++)
				{
					Item item=(Item)itemsToMove.elementAt(i);
					if((item.location()==null)||(item.location()==container))
						item.setLocation(nextDestination);
					roomDestination.addItem(item);
				}
			if(mobDestination!=null)
				for(int i=0;i<itemsToMove.size();i++)
				{
					Item item=(Item)itemsToMove.elementAt(i);
					if((item.location()==null)||(item.location()==container))
						item.setLocation(nextDestination);
					mobDestination.addInventory(item);
				}
		}
	}
	public boolean tick(int tickID)
	{
		if(tickID==Host.MOB_TICK)
			tryToMoveStuff();
		return true;
	}
	
	public void affect(Affect affect)
	{
		// amazingly important that this happens first!
		super.affect(affect);
		if(affected==null) return;
		if(((mobDestination!=null)||(roomDestination!=null))
		&&((affect.targetMinor()==Affect.TYP_GET)
		||(affect.targetMinor()==Affect.TYP_GIVE)
		||(affect.targetMinor()==Affect.TYP_PUT)
		||(affect.targetMinor()==Affect.TYP_DROP)))
			tryToMoveStuff();
	}
}