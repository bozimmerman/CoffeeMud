package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdContainer extends StdItem implements Container
{
	protected boolean isLocked=false;
	protected boolean hasALock=false;
	protected boolean isOpen=true;
	protected boolean hasALid=false;

	public StdContainer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a container";
		displayText="a nondescript container sits here.";
		description="I'll bet you could put stuff in it!";
		capacity=25;
		baseGoldValue=10;
		isAContainer=true;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new StdContainer();
	}

	public boolean okAffect(Affect affect)
	{
		if(!super.okAffect(affect))
			return false;
		if(affect.amITarget(this))
		{
			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_PUT:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					if(capacity>0)
					{
						if((envStats().weight()+newitem.envStats().weight())>capacity)
						{
							mob.tell(name()+" is full.");
							return false;
						}
						return true;
					}
					else
					if(hasALid()&&(!isOpen()))
					{
						mob.tell(mob,null,name()+" is closed.");
						return false;
					}
					else
					if(newitem.amWearingAt(Item.WIELD))
					{
						mob.tell(mob,null,"You are wielding that!");
						return false;
					}
					else
					if(newitem.amWearingAt(Item.HELD))
					{
						mob.tell(mob,null,"You are holding that!");
						return false;
					}
					else
					if(!newitem.amWearingAt(Item.INVENTORY))
					{
						mob.tell(mob,null,"You are wearing that!");
						return false;
					}
				}
				break;
			case Affect.TYP_GET:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					if(newitem.location()==this)
					{
						if(!Sense.canBeSeenBy(newitem,mob))
						{
							mob.tell("You can't see that.");
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(mob,null,name()+" is closed.");
							return false;
						}
						else
						if(mob.envStats().level()<newitem.envStats().level()-10)
						{
							mob.tell(newitem.name()+" is too powerful to endure possessing it.");
							return false;
						}
						else
						if((this.recursiveRoomWeight(mob,newitem)>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
						{
							mob.tell(newitem.name()+" is too heavy.");
							return false;
						}
						else
						return true;
					}
					else
					{
						mob.tell("You don't see that here.");
						return false;
					}
				}
				else
				if((this.recursiveRoomWeight(mob,this)>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(name()+" is too heavy.");
					return false;
				}
				break;
			case Affect.TYP_CLOSE:
				if(isOpen)
				{
					if(!hasALid)
					{
						mob.tell("There is nothing to close on "+name()+".");
						return false;
					}
					else
						return true;
				}
				else
				{
					mob.tell(name()+" is already closed.");
					return false;
				}
				//break;
			case Affect.TYP_OPEN:
				if(!hasALid)
				{
					mob.tell("There is nothing to open on "+name()+".");
					return false;
				}
				if(isOpen)
				{
					mob.tell(name()+" is already open!");
					return false;
				}
				else
				{
					if(isLocked)
					{
						mob.tell(name()+" is locked.");
						return false;
					}
					else
						return true;
				}
				//break;
			case Affect.TYP_LOCK:
			case Affect.TYP_UNLOCK:
				if(!hasALid)
				{
					mob.tell("There is nothing to lock or unlock on "+name()+".");
					return false;
				}
				if(isOpen)
				{
					mob.tell(name()+" is open!");
					return false;
				}
				else
				if(!hasALock)
				{
					mob.tell("There is no lock!");
					return false;
				}
				else
				{
					if((!isLocked)&&(affect.targetMinor()==Affect.TYP_UNLOCK))
					{
						mob.tell(name()+" is not locked.");
						return false;
					}
					else
					if((isLocked)&&(affect.targetMinor()==Affect.TYP_LOCK))
					{
						mob.tell(name()+" is already locked.");
						return false;
					}
					else
					{
						for(int i=0;i<mob.inventorySize();i++)
						{
							Item item=mob.fetchInventory(i);
							if((item!=null)
							&&(item instanceof Key)
							&&(item.location()==null)
							&&(Sense.canBeSeenBy(item,mob)))
							{
								if(((Key)item).getKey().equals(keyName()))
									return true;
							}
						}
						mob.tell("You don't have the key.");
						return false;
					}
				}
				//break;
				default:
					break;
			}
		}
		return true;
	}

	public void affect(Affect affect)
	{
		if(affect.amITarget(this))
		{

			MOB mob=affect.source();
			switch(affect.targetMinor())
			{
			case Affect.TYP_GET:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					if(newitem.location()==this)
						newitem.setLocation(null);
					remove();
				}
				else
				if(!mob.isMine(this))
				{
					this.setLocation(null);
					recursiveGetRoom(mob,this);
					mob.location().recoverRoomStats();
				}
				else
				{
					this.setLocation(null);
					remove();
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_PUT:
				if((affect.tool()!=null)
				&&(affect.tool() instanceof Item))
				{
					Item newitem=(Item)affect.tool();
					newitem.setLocation(this);
				}
				break;
			case Affect.TYP_DROP:
				if(mob.isMine(this))
				{
					this.setLocation(null);
					recursiveDropMOB(mob,this);
					mob.location().recoverRoomStats();
				}
				break;
			case Affect.TYP_EXAMINESOMETHING:
				if(Sense.canBeSeenBy(this,mob))
				{
					StringBuffer buf=new StringBuffer("");
					if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
					{
						buf.append(ID()+"\n\rRejuv:"+baseEnvStats().rejuv()+"\n\rUses :"+usesRemaining()+"\n\rAbile:"+baseEnvStats().ability()+"\n\rLevel:"+baseEnvStats().level()+"\n\rMisc :'"+text()+"\n\r"+description()+"'\n\r");
						buf.append("Key  : "+keyName()+"\n\r");
					}
					else
						buf.append(description()+"\n\r");
					if(this.isOpen)
					{
						buf.append(name()+" contains:\n\r");
						if(mob.isMine(this))
						{
							for(int i=0;i<mob.inventorySize();i++)
							{
								Item item=mob.fetchInventory(i);
								if((item!=null)&&(item.location()==this))
									buf.append(item.name()+"\n\r");
							}
						}
						else
						{
							Room room=mob.location();
							if(room!=null)
							for(int i=0;i<room.numItems();i++)
							{
								Item item=room.fetchItem(i);
								if((item!=null)&&(item.location()==this))
									buf.append(item.name()+"\n\r");
							}
						}
					}
					else
						buf.append(name()+" is closed.");
					mob.tell(buf.toString());
				}
				else
					mob.tell("You can't see that!");
				break;
			case Affect.TYP_CLOSE:
				if((!hasALid)||(!isOpen)) return;
				isOpen=false;
				break;
			case Affect.TYP_OPEN:
				if((!hasALid)||(isOpen)||(isLocked)) return;
				isLocked=false;
				isOpen=true;
				break;
			case Affect.TYP_LOCK:
				if((!hasALid)||(!hasALock)||(isLocked)) return;
				isOpen=false;
				isLocked=true;
				break;
			case Affect.TYP_UNLOCK:
				if((!hasALid)||(!hasALock)||(isOpen)||(!isLocked))
					return;
				isLocked=false;
				break;
			default:
				break;
			}
		}
		super.affect(affect);
	}
	private int recursiveRoomWeight(MOB mob, Item thisContainer)
	{
		int weight=thisContainer.envStats().weight();
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item thisItem=mob.location().fetchItem(i);
			if((thisItem!=null)&&(thisItem.location()==thisContainer))
				weight+=recursiveRoomWeight(mob,thisItem);
		}
		return weight;
	}


	public boolean isLocked(){return isLocked;}
	public boolean hasALock(){return hasALock;}
	public boolean isOpen(){return isOpen;}
	public boolean hasALid(){return hasALid;}
	public void setLidsNLocks(boolean newHasALid, boolean newIsOpen, boolean newHasALock, boolean newIsLocked)
	{
		hasALid=newHasALid;
		isOpen=newIsOpen;
		hasALock=newHasALock;
		isLocked=newIsLocked;
	}

	private static void recursiveGetRoom(MOB mob, Item thisContainer)
	{

		// caller is responsible for recovering any env
		// stat changes!
		if(Sense.isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)Sense.ALLMASK-Sense.IS_HIDDEN));
		mob.location().delItem(thisContainer);
		thisContainer.remove();
		if(!mob.isMine(thisContainer))
			mob.addInventory(thisContainer);
		thisContainer.recoverEnvStats();
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item thisItem=mob.location().fetchItem(i);
				if((thisItem!=null)&&(thisItem.location()==thisContainer))
				{
					recursiveGetRoom(mob,thisItem);
					nothingDone=false;
					break;
				}
			}
		}while(!nothingDone);
	}
	public void setMiscText(String newMiscText)
	{
		miscText=newMiscText;
		if(!(this instanceof GenContainer))
			setKeyName(miscText);
	}
	public String keyName()
	{
		return miscText;
	}
	public void setKeyName(String newKeyName)
	{
		miscText=newKeyName;
	}
	private static void recursiveDropMOB(MOB mob, Item thisContainer)
	{
		// caller is responsible for recovering any env
		// stat changes!

		if(Sense.isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)Sense.ALLMASK-Sense.IS_HIDDEN));
		mob.delInventory(thisContainer);
		thisContainer.remove();
		mob.location().addItem(thisContainer);
		thisContainer.setPossessionTime(Calendar.getInstance());
		thisContainer.recoverEnvStats();
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item thisItem=mob.fetchInventory(i);
				if((thisItem!=null)&&(thisItem.location()==thisContainer))
				{
					recursiveDropMOB(mob,thisItem);
					nothingDone=false;
					break;
				}
			}
		}while(!nothingDone);
	}
}
