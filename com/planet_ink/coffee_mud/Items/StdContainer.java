package com.planet_ink.coffee_mud.Items;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class StdContainer extends StdItem implements Container
{
	public String ID(){	return "StdContainer";}
	protected boolean isLocked=false;
	protected boolean hasALock=false;
	protected boolean isOpen=true;
	protected boolean hasALid=false;
	protected int capacity=0;
	protected long containType=0;

	public StdContainer()
	{
		super();
		setName("a container");
		setDisplayText("a nondescript container sits here.");
		setDescription("I`ll bet you could put stuff in it!");
		capacity=25;
		baseGoldValue=10;
		recoverEnvStats();
		material=EnvResource.RESOURCE_COTTON;
	}



	public int capacity()
	{
		return capacity;
	}
	public void setCapacity(int newValue)
	{
		capacity=newValue;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_PUT:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					if(!Sense.isDroppable((Item)msg.tool()))
					{
						mob.tell("You can't seem to let go of "+name()+".");
						return false;
					}
					Item newitem=(Item)msg.tool();
					if(hasALid()&&(!isOpen()))
					{
						mob.tell(name()+" is closed.");
						return false;
					}
					else
					if(newitem.amWearingAt(Item.WIELD))
					{
						mob.tell("You are wielding that!");
						return false;
					}
					else
					if(newitem.amWearingAt(Item.HELD))
					{
						mob.tell("You are holding that!");
						return false;
					}
					else
					if(!newitem.amWearingAt(Item.INVENTORY))
					{
						mob.tell("You are wearing that!");
						return false;
					}
					else
					if(capacity<=0)
					{
						mob.tell("You can't put anything in "+name()+"!");
						return false;
					}
					else
					{
						if(!canContain(newitem))
						{
							mob.tell("You can't put "+newitem.name()+" in "+name()+".");
							return false;
						}
						else
						if(newitem.envStats().weight()>capacity)
						{
							mob.tell(newitem.name()+" won't fit in "+name()+".");
							return false;
						}
						else
						if((recursiveWeight(this)+newitem.envStats().weight())>capacity)
						{
							mob.tell(name()+" is full.");
							return false;
						}
						if((!msg.source().isMine(this))&&(msg.source().isMine(newitem)))
							if(!CommonMsgs.drop(msg.source(),newitem,true,true))
								return false;
						return true;
					}
				}
				break;
			case CMMsg.TYP_GET:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					Item newitem=(Item)msg.tool();
					if(newitem.container()==this)
					{
						if((!Sense.canBeSeenBy(newitem,mob))
						&&((msg.sourceMajor()&CMMsg.MASK_GENERAL)==0))
						{
							mob.tell("You can't see that.");
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(name()+" is closed.");
							return false;
						}
						else
						if((mob.envStats().level()<newitem.envStats().level()-(10+(mob.envStats().level()/5)))
						&&(!(mob instanceof ShopKeeper)))
						{
							mob.tell(newitem.name()+" is too powerful to endure possessing it.");
							return false;
						}
						else
						if((recursiveWeight(newitem)>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
						{
							mob.tell(newitem.name()+" is too heavy.");
							return false;
						}
						else
						if(!Sense.isGettable(newitem))
						{
							mob.tell("You can't get "+newitem.name()+".");
							return false;
						}
						return true;
					}
					else
					{
						mob.tell("You don't see that here.");
						return false;
					}
				}
				else
				if((recursiveWeight(this)>(mob.maxCarry()-mob.envStats().weight()))&&(!mob.isMine(this)))
				{
					mob.tell(name()+" is too heavy.");
					return false;
				}
				break;
			case CMMsg.TYP_REMOVE:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					Item newitem=(Item)msg.tool();
					if(newitem.container()==this)
					{
						if((!Sense.canBeSeenBy(newitem,mob))
						&&((msg.sourceMajor()&CMMsg.MASK_GENERAL)==0))
						{
							mob.tell("You can't see that.");
							return false;
						}
						else
						if(hasALid()&&(!isOpen()))
						{
							mob.tell(name()+" is closed.");
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
				break;
			case CMMsg.TYP_CLOSE:
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
			case CMMsg.TYP_OPEN:
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
			case CMMsg.TYP_LOCK:
			case CMMsg.TYP_UNLOCK:
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
					if((!isLocked)&&(msg.targetMinor()==CMMsg.TYP_UNLOCK))
					{
						mob.tell(name()+" is not locked.");
						return false;
					}
					else
					if((isLocked)&&(msg.targetMinor()==CMMsg.TYP_LOCK))
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
							&&((Key)item).getKey().equals(keyName())
							&&((item.container()==null)
							   ||((item.container().container()==null)
								  &&(item.container() instanceof Container)
								  &&((((Container)item.container()).containTypes()&Container.CONTAIN_KEYS)>0)))
							&&(Sense.canBeSeenBy(item,mob)))
								return true;
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

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{

			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GET:
			case CMMsg.TYP_REMOVE:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					Item newitem=(Item)msg.tool();
					if(newitem.container()==this)
					{
						newitem.setContainer(null);
						newitem.unWear();
					}
				}
				else
				if(!mob.isMine(this))
				{
					setContainer(null);
					mob.giveItem(this);
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
					else
						mob.envStats().setWeight(mob.envStats().weight()+recursiveWeight(this));
				}
				else
				{
					setContainer(null);
					unWear();
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_PUT:
				if((msg.tool()!=null)
				&&(msg.tool() instanceof Item))
				{
					Item newitem=(Item)msg.tool();
					newitem.setContainer(this);
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_THROW:
				if((mob.isMine(this))
				&&(msg.tool()!=null)
				&&(msg.tool() instanceof Room))
				{
					setContainer(null);
					recursiveDropMOB(mob,(Room)msg.tool(),this,this instanceof DeadBody);
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
					{
						mob.location().recoverRoomStats();
						if(mob.location()!=msg.tool())
							((Room)msg.tool()).recoverRoomStats();
					}
				}
				break;
			case CMMsg.TYP_DROP:
				if(mob.isMine(this))
				{
					setContainer(null);
					recursiveDropMOB(mob,mob.location(),this,this instanceof DeadBody);
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
			case CMMsg.TYP_EXAMINESOMETHING:
				if(Sense.canBeSeenBy(this,mob))
				{
					StringBuffer buf=new StringBuffer("");
					if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
						buf.append(ID()+"\n\rRejuv :"+baseEnvStats().rejuv()+"\n\rUses  :"+usesRemaining()+"\n\rHeight: "+baseEnvStats().height()+"\n\rAbilty:"+baseEnvStats().ability()+"\n\rLevel :"+baseEnvStats().level()+"\n\rDeath : "+dispossessionTimeLeftString()+"\n\r"+description()+"'\n\rKey  : "+keyName()+"\n\rMisc  :'"+text());
					else
						buf.append(description()+"\n\r");
					if((isOpen)&&((capacity>0)||(getContents().size()>0)))
					{
						buf.append(name()+" contains:\n\r");
						Vector newItems=new Vector();
						if((this instanceof Drink)&&(((Drink)this).liquidRemaining()>0))
						{
							GenLiquidResource l=new GenLiquidResource();
							int myResource=((Drink)this).liquidType();
							l.setMaterial(myResource);
							((Drink)l).setLiquidType(myResource);
							l.setBaseValue(EnvResource.RESOURCE_DATA[myResource&EnvResource.RESOURCE_MASK][1]);
							l.baseEnvStats().setWeight(1);
							String name=EnvResource.RESOURCE_DESCS[myResource&EnvResource.RESOURCE_MASK].toLowerCase();
							l.setName("some "+name);
							l.setDisplayText("some "+name+" sits here.");
							l.setDescription("");
							l.recoverEnvStats();
							newItems.addElement(l);
						}

						if(mob.isMine(this))
						{
							for(int i=0;i<mob.inventorySize();i++)
							{
								Item item=mob.fetchInventory(i);
								if((item!=null)&&(item.container()==this))
									newItems.addElement(item);
							}
							buf.append(CMLister.niceLister(mob,newItems,true));
						}
						else
						{
							Room room=mob.location();
							if(room!=null)
							for(int i=0;i<room.numItems();i++)
							{
								Item item=room.fetchItem(i);
								if((item!=null)&&(item.container()==this))
									newItems.addElement(item);
							}
							buf.append(CMLister.niceLister(mob,newItems,true));
						}
					}
					else
					if(hasALid())
						buf.append(name()+" is closed.");
					mob.tell(buf.toString());
				}
				else
					mob.tell("You can't see that!");
				break;
			case CMMsg.TYP_CLOSE:
				if((!hasALid)||(!isOpen)) return;
				isOpen=false;
				break;
			case CMMsg.TYP_OPEN:
				if((!hasALid)||(isOpen)||(isLocked)) return;
				isLocked=false;
				isOpen=true;
				break;
			case CMMsg.TYP_LOCK:
				if((!hasALid)||(!hasALock)||(isLocked)) return;
				isOpen=false;
				isLocked=true;
				break;
			case CMMsg.TYP_UNLOCK:
				if((!hasALid)||(!hasALock)||(isOpen)||(!isLocked))
					return;
				isLocked=false;
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}
	protected int recursiveWeight(Item thisContainer)
	{
		int weight=thisContainer.envStats().weight();
		if(owner()==null) return weight;
		if(owner() instanceof MOB)
		{
			MOB M=(MOB)owner();
			for(int i=0;i<M.inventorySize();i++)
			{
				Item thisItem=M.fetchInventory(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
					weight+=recursiveWeight(thisItem);
			}
		}
		else
		if(owner() instanceof Room)
		{
			Room R=(Room)owner();
			for(int i=0;i<R.numItems();i++)
			{
				Item thisItem=R.fetchItem(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
					weight+=recursiveWeight(thisItem);
			}
		}

		return weight;
	}

	public long containTypes(){return containType;}
	public void setContainTypes(long containTypes){containType=containTypes;}
	public boolean canContain(Environmental E)
	{
		if (!(E instanceof Item)) return false;
		if(containType==0) return true;
		for(int i=0;i<20;i++)
			if(Util.isSet((int)containType,i))
				switch(Util.pow(2,i))
				{
				case CONTAIN_LIQUID:
					if((((Item)E).material()&EnvResource.MATERIAL_LIQUID)>0)
						return true;
					break;
				case CONTAIN_COINS:
					if(E instanceof Coins)
						return true;
					break;
				case CONTAIN_SWORDS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_SWORD))
						return true;
					break;
				case CONTAIN_DAGGERS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()==Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_KEYS:
					if(E instanceof Key)
						return true;
					break;
				case CONTAIN_DRINKABLES:
					if((E instanceof Drink)&&(E instanceof Item))
						return true;
					break;
				case CONTAIN_CLOTHES:
					if((E instanceof Armor)
					&&(((Armor)E).fitsOn(Item.ABOUT_BODY)
					   ||((Armor)E).fitsOn(Item.ON_ARMS)
					   ||((Armor)E).fitsOn(Item.ON_LEGS)
					   ||((Armor)E).fitsOn(Item.ON_HEAD)
					   ||((Armor)E).fitsOn(Item.ON_TORSO)
					   ||((Armor)E).fitsOn(Item.ON_WAIST)))
						return true;
					break;
				case CONTAIN_OTHERWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_SWORD)
					&&(((Weapon)E).weaponClassification()!=Weapon.CLASS_DAGGER))
						return true;
					break;
				case CONTAIN_ONEHANDWEAPONS:
					if((E instanceof Weapon)
					&&(((Weapon)E).rawLogicalAnd()==false))
						return true;
					break;
				case CONTAIN_BODIES:
					if(E instanceof DeadBody)
						return true;
					break;
				case CONTAIN_SMOKEABLES:
					if(E instanceof Item)
					{
						if((((Item)E).material()==EnvResource.RESOURCE_PIPEWEED)
						||(((Item)E).material()==EnvResource.RESOURCE_HERBS))
							return true;
					}
					break;
				case CONTAIN_CAGED:
					if(E instanceof CagedAnimal)
						return true;
					break;
				case CONTAIN_READABLES:
					if((E instanceof Item)
					&&(Sense.isReadable(((Item)E))))
						return true;
					break;
				case CONTAIN_SCROLLS:
					if(E instanceof Scroll)
						return true;
					break;
				}
		return false;
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
	protected static void recursiveDropMOB(MOB mob,
										   Room room,
										   Item thisContainer,
										   boolean bodyFlag)
	{
		// caller is responsible for recovering any env
		// stat changes!

		if(Sense.isHidden(thisContainer))
			thisContainer.baseEnvStats().setDisposition(thisContainer.baseEnvStats().disposition()&((int)EnvStats.ALLMASK-EnvStats.IS_HIDDEN));
		mob.delInventory(thisContainer);
		thisContainer.unWear();
		if(!bodyFlag) bodyFlag=(thisContainer instanceof DeadBody);
		if(bodyFlag)
		{
			room.addItem(thisContainer);
			thisContainer.setDispossessionTime(0);
		}
		else
			room.addItemRefuse(thisContainer,Item.REFUSE_PLAYER_DROP);
		thisContainer.recoverEnvStats();
		boolean nothingDone=true;
		do
		{
			nothingDone=true;
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item thisItem=mob.fetchInventory(i);
				if((thisItem!=null)&&(thisItem.container()==thisContainer))
				{
					recursiveDropMOB(mob,room,thisItem,bodyFlag);
					nothingDone=false;
					break;
				}
			}
		}while(!nothingDone);
	}

	public void emptyPlease()
	{
		Vector V=getContents();
		for(int v=0;v<V.size();v++)
		{
			Item I=(Item)V.elementAt(v);
			I.setContainer(null);
		}
	}
	protected void reallyGetContents(Item container, Environmental own, Vector V)
	{
		if(container==null) return;
		if(own instanceof MOB)
		{
			for(int i=0;i<((MOB)own).inventorySize();i++)
			{
				Item I=((MOB)own).fetchInventory(i);
				if((I.container()==container)
				&&(!V.contains(I)))
				{
					V.addElement(I);
					reallyGetContents(I,own,V);
				}
			}
		}
		else
		if(own instanceof Room)
		{
			for(int i=0;i<((Room)own).numItems();i++)
			{
				Item I=((Room)own).fetchItem(i);
				if((I!=null)
				&&(I.container()==container)
				&&(!V.contains(I)))
				{
					V.addElement(I);
					reallyGetContents(I,own,V);
				}
			}
		}
	}

	public Vector getContents()
	{
		Vector V=new Vector();
		if(owner()!=null)
			reallyGetContents(this,owner(),V);
		return V;
	}
}
