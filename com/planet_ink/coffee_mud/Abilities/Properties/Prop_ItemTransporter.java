package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prop_ItemTransporter extends Property
{
	public String ID() { return "Prop_ItemTransporter"; }
	public String name(){ return "Item Transporter";}
	protected int canAffectCode(){return Ability.CAN_MOBS|Ability.CAN_ITEMS|Ability.CAN_ROOMS;}
	protected Room roomDestination=null;
	protected MOB mobDestination=null;
	protected Item nextDestination=null;
	private static Hashtable possiblePossibilities=new Hashtable();
	private static Hashtable lastLooks=new Hashtable();
	public Environmental newInstance(){	Prop_ItemTransporter BOB=new Prop_ItemTransporter();	BOB.setMiscText(text()); return BOB;}

	public String accountForYourself()
	{ return "Item Transporter";	}

	public Item ultimateParent(Item item)
	{
		if(item==null) return null;
		if(item.container()==null) return item;
		if(item.container().container()==item)
			item.container().setContainer(null);
		if(item.container()==item)
			item.setContainer(null);
		return ultimateParent(item.container());
	}

	private synchronized boolean setDestination()
	{
		Vector possibilities=(Vector)possiblePossibilities.get(text());
		Integer lastLook=(Integer)lastLooks.get(text());
		if((possibilities==null)||(lastLook==null)||(lastLook.intValue()<0))
		{
			possibilities=new Vector();
			possiblePossibilities.put(text(),possibilities);
			lastLook=new Integer(10);
			lastLooks.put(text(),lastLook);
		}
		else
			lastLooks.put(text(),new Integer(lastLook.intValue()-1));
		if(possibilities.size()==0)
		{
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
				if((nextDestination!=null)&&(nextDestination.owner()!=null))
				{
					if(nextDestination.owner() instanceof Room)
						roomDestination=(Room)nextDestination.owner();
					else
					if(nextDestination.owner() instanceof MOB)
						mobDestination=(MOB)nextDestination.owner();
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
		
		if(((affect.amITarget(affected))
			&&((affect.targetMinor()==Affect.TYP_PUT)
			   ||(affect.targetMinor()==Affect.TYP_GIVE))
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Item))
		||((affected instanceof MOB)
			&&(affect.amISource((MOB)affected))
			&&(affect.targetMinor()==Affect.TYP_GET)
			&&(affect.target() !=null)
			&&(affect.target() instanceof Item))
		||((affected instanceof Room)
			&&(affect.targetMinor()==Affect.TYP_DROP)
			&&(affect.target()!=null)
			&&(affect.target() instanceof Item))
		||((affected instanceof Room)
			&&(affect.targetMinor()==Affect.TYP_THROW)
		    &&(affected==affect.tool())
			&&(affect.target()!=null)
			&&(affect.target() instanceof Item)))
		{
			if(!setDestination())
			{
				affect.source().tell("The transporter has no possible ItemTransReceiver with the code '"+text()+"'.");
				return false;
			}
		}
		return true;
	}
	
	public synchronized void tryToMoveStuff()
	{
		if((mobDestination!=null)||(roomDestination!=null))
		{
			Room room=roomDestination;
			MOB mob=mobDestination;
			Room roomMover=null;
			MOB mobMover=null;
			Item container=null;
			if(affected==null) return;
			if(affected instanceof Room)
				roomMover=(Room)affected;
			else
			if(affected instanceof MOB)
				mobMover=(MOB)affected;
			else
			if(affected instanceof Item)
			{
				container=(Item)affected;
				if((container.owner()!=null)&&(container.owner() instanceof Room))
					roomMover=(Room)container.owner();
				else
				if((container.owner()!=null)&&(container.owner() instanceof MOB))
					mobMover=(MOB)container.owner();
			}
			Vector itemsToMove=new Vector();
			if(roomMover!=null)
			{
				for(int i=0;i<roomMover.numItems();i++)
				{
					Item item=roomMover.fetchItem(i);
					if((item!=null)
					   &&(item!=container)
					   &&(item.amWearingAt(Item.INVENTORY))
					   &&((item.container()==container)||(ultimateParent(item)==container)))
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
					   &&((item.container()==container)||(ultimateParent(item)==container)))
					   itemsToMove.addElement(item);
				}
				for(int i=oldNum;i<itemsToMove.size();i++)
					mobMover.delInventory((Item)itemsToMove.elementAt(i));
			}
			if(itemsToMove.size()>0)
			{
				mobDestination=null;
				roomDestination=null;
				if(room!=null)
					for(int i=0;i<itemsToMove.size();i++)
					{
						Item item=(Item)itemsToMove.elementAt(i);
						if((item.container()==null)||(item.container()==container))
							item.setContainer(nextDestination);
						room.addItemRefuse(item,Item.REFUSE_PLAYER_DROP);
					}
				if(mob!=null)
					for(int i=0;i<itemsToMove.size();i++)
					{
						Item item=(Item)itemsToMove.elementAt(i);
						if((item.container()==null)||(item.container()==container))
							item.setContainer(nextDestination);
						if(mob instanceof ShopKeeper)
							((ShopKeeper)mob).addStoreInventory(item);
						else
							mob.addInventory(item);
					}
				if(room!=null) room.recoverRoomStats();
				if(mob!=null){
					mob.recoverCharStats();
					mob.recoverEnvStats();
					mob.recoverMaxState();
				}
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
		if((affect.targetMinor()==Affect.TYP_GET)
		||(affect.targetMinor()==Affect.TYP_GIVE)
		||(affect.targetMinor()==Affect.TYP_PUT)
		||(affect.targetMinor()==Affect.TYP_THROW)
		||(affect.targetMinor()==Affect.TYP_DROP))
			tryToMoveStuff();
	}
}