package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class ItemUsage
{
	public Item possibleRoomGold(MOB seer, Room room, Item container, String itemID)
	{
		if(itemID.toUpperCase().trim().endsWith(" COINS"))
			itemID=itemID.substring(0,itemID.length()-6);
		if(itemID.toUpperCase().trim().endsWith(" GOLD"))
			itemID=itemID.substring(0,itemID.length()-5);
		int gold=Util.s_int(itemID);
		if(gold>0)
		{
			for(int i=0;i<room.numItems();i++)
			{
				Item I=room.fetchItem(i);
				if((I.container()==container)
				&&(I instanceof Coins)
				&&(Sense.canBeSeenBy(I,seer)))
				{
					if(((Coins)I).numberOfCoins()<=gold)
						return I;
					((Coins)I).setNumberOfCoins(((Coins)I).numberOfCoins()-gold);
					Item C=(Item)CMClass.getItem("StdCoins");
					C.baseEnvStats().setAbility(gold);
					C.recoverEnvStats();
					room.addItem(C);
					C.setDispossessionTime(I.dispossessionTime());
					return C;
				}
			}
		}
		return null;
	}

	public Item possibleContainer(MOB mob, Vector commands, int wornReqCode)
	{
		if(commands.size()==1)
			return null;

		String possibleContainerID=(String)commands.elementAt(commands.size()-1);
		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID,wornReqCode);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang) instanceof Container))
		{
			commands.removeElementAt(commands.size()-1);
			return (Item)thisThang;
		}
		return null;
	}

	public Vector possibleContainers(MOB mob, Vector commands, int wornReqCode)
	{
		Vector V=new Vector();
		if(commands.size()==1)
			return V;

		String possibleContainerID=(String)commands.elementAt(commands.size()-1);
		boolean allFlag=false;
		String preWord="";
		if(possibleContainerID.equalsIgnoreCase("all")) allFlag=true;
		if((commands.size()>2)&&(!allFlag))
			preWord=(String)commands.elementAt(commands.size()-2);
		
		if(preWord.equalsIgnoreCase("all")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID;}
		else
		if(possibleContainerID.toUpperCase().startsWith("ALL.")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(4);}
		else
		if(possibleContainerID.toUpperCase().endsWith(".ALL")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(0,possibleContainerID.length()-4);}
		
		int addendum=1;
		String addendumStr="";
		do
		{
			Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID+addendumStr,wornReqCode);
			if((thisThang!=null)
			&&(thisThang instanceof Item)
			&&(Sense.canBeSeenBy(thisThang,mob))
			&&(((Item)thisThang) instanceof Container)
			&&(((Container)thisThang).getContents().size()>0))
			{
				V.addElement(thisThang);
				if(V.size()==1)
				{
					commands.removeElementAt(commands.size()-1);
					if(allFlag&&(preWord.equalsIgnoreCase("all")))
						commands.removeElementAt(commands.size()-1);
				}
			}
			if(thisThang==null) return V;
			addendumStr="."+(++addendum);
		}
		while(allFlag);
		return V;
	}

	public void compare(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("Compare what to what?");
			return;
		}
		commands.removeElementAt(0);

		Item compareThis=mob.fetchInventory((String)commands.elementAt(0));
		if((compareThis==null)||((compareThis!=null)&&(!Sense.canBeSeenBy(compareThis,mob))))
		{
			mob.tell("You don't have a "+((String)commands.elementAt(0))+".");
			return;
		}
		Item toThis=mob.fetchInventory(Util.combine(commands,1));
		if((toThis==null)||((toThis!=null)&&(!Sense.canBeSeenBy(toThis,mob))))
		{
			mob.tell("You don't have a "+((String)commands.elementAt(1))+".");
			return;
		}

		if((compareThis instanceof Weapon)&&(toThis instanceof Weapon))
		{
			int cDmg=compareThis.baseEnvStats().damage();
			int tDmg=toThis.baseEnvStats().damage();
			cDmg+=(int)Math.round(Util.div(compareThis.baseEnvStats().attackAdjustment()-toThis.baseEnvStats().attackAdjustment(),100.0)*cDmg);

			if(cDmg==tDmg)
				mob.tell(compareThis.name()+" and "+toThis.name()+" look about the same.");
			else
			if(cDmg>tDmg)
				mob.tell(compareThis.name()+" looks better than "+toThis.name()+".");
			else
				mob.tell(compareThis.name()+" looks worse than "+toThis.name()+".");
		}
		else
		if((compareThis instanceof Armor)&&(toThis instanceof Armor))
		{
			if(!compareThis.compareProperLocations(toThis))
			{
				mob.tell(compareThis.name()+" is not worn the same way as "+toThis.name()+", and can't be compared to it.");
				return;
			}
			if(compareThis.baseEnvStats().armor()==toThis.baseEnvStats().armor())
				mob.tell(compareThis.name()+" and "+toThis.name()+" look about the same.");
			else
			if(compareThis.baseEnvStats().armor()>toThis.baseEnvStats().armor())
				mob.tell(compareThis.name()+" look better than "+toThis.name()+".");
			else
				mob.tell(compareThis.name()+" look worse than "+toThis.name()+".");

		}
		else
			mob.tell("You can't compare "+compareThis.name()+" and "+toThis.name()+".");
	}
	public boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{ return get(mob,container,getThis,quiet,"get");}

	public boolean get(MOB mob, 
					   Item container, 
					   Item getThis, 
					   boolean quiet, 
					   String getWord)
	{
		String theWhat=getThis.name();
		Environmental target=getThis;
		Environmental tool=null;
		if(container!=null)
		{
			tool=getThis;
			target=container;
			theWhat+=" from "+container.name();
		}

		FullMsg msg=new FullMsg(mob,target,tool,Affect.MSG_GET,quiet?null:"<S-NAME> "+getWord+"(s) "+theWhat);
		if(!mob.location().okAffect(msg))
			return false;
		mob.location().send(mob,msg);
		if(!mob.isMine(target))
		{
			msg=new FullMsg(mob,getThis,null,Affect.MSG_GET,null);
			if(!mob.location().okAffect(msg))
				return false;
			mob.location().send(mob,msg);
		}
		return true;
	}

	public void get(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Get what?");
			return;
		}
		commands.removeElementAt(0);

		String containerName="";
		if(commands.size()>0)
			containerName=(String)commands.lastElement();
		Vector containers=possibleContainers(mob,commands,Item.WORN_REQ_ANY);
		int c=0;
		String whatToGet=Util.combine(commands,0);
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatToGet.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(4);}
		if(whatToGet.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToGet="ALL "+whatToGet.substring(0,whatToGet.length()-4);}
		boolean doneSomething=false;
		while((c<containers.size())||(containers.size()==0))
		{
			Vector V=new Vector();
			Item container=null;
			if(containers.size()>0) container=(Item)containers.elementAt(c++);
			int addendum=1;
			String addendumStr="";
			do
			{
				Environmental getThis=null;
				if((container!=null)&&(mob.isMine(container)))
				   getThis=mob.location().fetchFromMOBRoomFavorsItems(mob,(Item)container,whatToGet+addendumStr,Item.WORN_REQ_UNWORNONLY);
				else
				{
					if(!allFlag)
						getThis=possibleRoomGold(mob,mob.location(),container,whatToGet);
					if(getThis==null)
						getThis=mob.location().fetchFromRoomFavorItems((Item)container,whatToGet+addendumStr,Item.WORN_REQ_UNWORNONLY);
				}
				if(getThis==null) break;
				if((getThis instanceof Item)&&(Sense.canBeSeenBy(getThis,mob)))
					V.addElement(getThis);
				addendumStr="."+(++addendum);
			}
			while(allFlag);
			
			for(int i=0;i<V.size();i++)
			{
				Item getThis=(Item)V.elementAt(i);
				if(!get(mob,container,(Item)getThis,false))
					if(getThis instanceof Coins)
						((Coins)getThis).putCoinsBack();
				doneSomething=true;
			}
			
			if(containers.size()==0) break;
		}
		if(!doneSomething)
		{
			if(containers.size()>0)
			{
				Item container=(Item)containers.elementAt(0);
				if(((Container)container).isOpen())
					mob.tell("You don't see that in "+container.name()+".");
				else
					mob.tell(container.name()+" is closed.");
			}
			else
				mob.tell("You don't see '"+containerName+"' here.");
		}
	}

	public boolean drop(MOB mob, Environmental dropThis, boolean quiet)
	{
		FullMsg msg=new FullMsg(mob,dropThis,null,Affect.MSG_DROP,quiet?null:"<S-NAME> drop(s) <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			return true;
		}
		else
		if(dropThis instanceof Coins)
		{
			mob.setMoney(mob.getMoney()+((Coins)dropThis).numberOfCoins());
			((Coins)dropThis).destroyThis();
		}
		return false;
	}
	
	public void drop(MOB mob, Vector commands)
	{
		String whatToDrop=null;
		if(commands.size()<2)
		{
			mob.tell("Drop what?");
			return;
		}
		commands.removeElementAt(0);

		Item container=possibleContainer(mob,commands,Item.WORN_REQ_UNWORNONLY);
		whatToDrop=Util.combine(commands,0);

		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
		if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Item dropThis=new SocialProcessor().possibleGold(mob,whatToDrop+addendumStr);
			if(dropThis!=null)
				allFlag=false;
			else
				dropThis=mob.fetchCarried(container,whatToDrop+addendumStr);
			if(dropThis==null) break;
			if(Sense.canBeSeenBy(dropThis,mob))
				V.addElement(dropThis);
			addendumStr="."+(++addendum);
		}
		while(allFlag);
		
		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			Item dropThis=(Item)V.elementAt(i);
			drop(mob,dropThis,false);
			if(dropThis instanceof Coins)
				((Coins)dropThis).putCoinsBack();
		}
	}

	public void put(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Put what where?");
			return;
		}

		if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("on"))
		{
			commands.removeElementAt(commands.size()-1);
			this.wear(mob,commands);
			return;
		}

		if(((String)commands.elementAt(1)).equalsIgnoreCase("on"))
		{
			commands.removeElementAt(1);
			this.wear(mob,commands);
			return;
		}

		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("Where should I put the "+(String)commands.elementAt(0));
			return;
		}

		Environmental container=possibleContainer(mob,commands,Item.WORN_REQ_ANY);
		if((container==null)||((container!=null)&&(!Sense.canBeSeenBy(container,mob))))
		{
			mob.tell("I don't see a "+(String)commands.elementAt(commands.size()-1)+" here.");
			return;
		}

		String thingToPut=Util.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thingToPut.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(4);}
		if(thingToPut.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToPut="ALL "+thingToPut.substring(0,thingToPut.length()-4);}
		do
		{
			Environmental putThis=new SocialProcessor().possibleGold(mob,thingToPut);
			if(putThis!=null)
				allFlag=false;
			else
				putThis=mob.fetchCarried(null,thingToPut+addendumStr);
			if(putThis==null) break;
			if(Sense.canBeSeenBy(putThis,mob))
				V.addElement(putThis);
			addendumStr="."+(++addendum);
		}
		while(allFlag);
		
		if((container!=null)&&(V.contains(container)))
			V.remove(container);
		
		if(V.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<V.size();i++)
		{
			Environmental putThis=(Environmental)V.elementAt(i);
			FullMsg putMsg=new FullMsg(mob,container,putThis,Affect.MSG_PUT,"<S-NAME> put(s) "+putThis.name()+" in <T-NAME>");
			if(mob.location().okAffect(putMsg))
				mob.location().send(mob,putMsg);
			if(putThis instanceof Coins)
				((Coins)putThis).putCoinsBack();
		}
	}

	public void fill(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Fill what, from what?");
			return;
		}
		commands.removeElementAt(0);
		if((commands.size()<2)&&(!(mob.location() instanceof Drink)))
		{
			mob.tell("From what should I fill the "+(String)commands.elementAt(0)+"?");
			return;
		}
		Environmental fillFromThis=null;
		if((commands.size()==1)&&(mob.location() instanceof Drink))
			fillFromThis=mob.location();
		else
		{
			String thingToFillFrom=(String)commands.elementAt(commands.size()-1);
			fillFromThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFillFrom,Item.WORN_REQ_ANY);
			if((fillFromThis==null)||((fillFromThis!=null)&&(!Sense.canBeSeenBy(fillFromThis,mob))))
			{
				mob.tell("I don't see a "+thingToFillFrom+" here.");
				return;
			}
			commands.removeElementAt(commands.size()-1);
		}

		String thingToFill=Util.combine(commands,0);
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(thingToFill.toUpperCase().startsWith("ALL.")){ allFlag=true; thingToFill="ALL "+thingToFill.substring(4);}
		if(thingToFill.toUpperCase().endsWith(".ALL")){ allFlag=true; thingToFill="ALL "+thingToFill.substring(0,thingToFill.length()-4);}
		do
		{
			Environmental fillThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFill+addendumStr,Item.WORN_REQ_ANY);
			if(fillThis==null) break;
			if(Sense.canBeSeenBy(fillThis,mob))
				V.addElement(fillThis);
			addendumStr="."+(++addendum);
		}
		while(allFlag);
		if(V.size()==0)
			mob.tell("I don't see '"+thingToFill+"' here.");
		else
		for(int i=0;i<V.size();i++)
		{
			Environmental fillThis=(Environmental)V.elementAt(i);
			FullMsg fillMsg=new FullMsg(mob,fillThis,fillFromThis,Affect.MSG_FILL,"<S-NAME> fill(s) <T-NAME> from "+fillFromThis.name()+".");
			if((!mob.isMine(fillThis))&&(fillThis instanceof Item))
			{
				if(get(mob,null,(Item)fillThis,false))
					if(mob.location().okAffect(fillMsg))
						mob.location().send(mob,fillMsg);
			}
			else
			if(mob.location().okAffect(fillMsg))
				mob.location().send(mob,fillMsg);
		}
	}

	public boolean wear(MOB mob, Item item, boolean quiet)
	{
		String str="<S-NAME> put(s) on <T-NAME>.";
		int msgType=Affect.MSG_WEAR;
		if(item.rawProperLocationBitmap()==Item.HELD)
		{
			str="<S-NAME> hold(s) <T-NAME>.";
			msgType=Affect.MSG_HOLD;
		}
		else
		if((item.rawProperLocationBitmap()==Item.WIELD)
		||(item.rawProperLocationBitmap()==(Item.HELD|Item.WIELD)))
		{
			str="<S-NAME> wield(s) <T-NAME>.";
			msgType=Affect.MSG_WIELD;
		}
		FullMsg newMsg=new FullMsg(mob,item,null,msgType,quiet?null:str);
		if(mob.location().okAffect(newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	public boolean wield(MOB mob, Item item, boolean quiet)
	{
		FullMsg newMsg=new FullMsg(mob,item,null,Affect.MSG_WIELD,quiet?null:"<S-NAME> wield(s) <T-NAME>.");
		if(mob.location().okAffect(newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	public boolean hold(MOB mob, Item item, boolean quiet)
	{
		int msgType=Affect.MSG_HOLD;
		String str="<S-NAME> hold(s) <T-NAME>.";
		if((!mob.amWearingSomethingHere(Item.WIELD))
		&&((item.rawProperLocationBitmap()==Item.WIELD)
		||(item.rawProperLocationBitmap()==(Item.HELD|Item.WIELD))))
		{
			str="<S-NAME> wield(s) <T-NAME>.";
			msgType=Affect.MSG_WIELD;
		}
		FullMsg newMsg=new FullMsg(mob,item,null,msgType,quiet?null:str);
		if(mob.location().okAffect(newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}
	
	public Vector fetchItemList(Environmental from, 
								MOB mob, 
								Item container, 
								Vector commands, 
								int preferredLoc,
								boolean visionMatters)
	{
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();
		String name=Util.combine(commands,0);
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(name.toUpperCase().startsWith("ALL.")){ allFlag=true; name="ALL "+name.substring(4);}
		if(name.toUpperCase().endsWith(".ALL")){ allFlag=true; name="ALL "+name.substring(0,name.length()-4);}
		do
		{
			Environmental item=null;
			if(from instanceof MOB)
			{
				if(preferredLoc==Item.WORN_REQ_UNWORNONLY)
					item=((MOB)from).fetchCarried(container,name+addendumStr);
				else
				if(preferredLoc==Item.WORN_REQ_WORNONLY)
					item=((MOB)from).fetchWornItem(name+addendumStr);
				else
					item=((MOB)from).fetchInventory(name+addendumStr);
			}
			else
			if(from instanceof Room)
				item=((Room)from).fetchFromMOBRoomFavorsItems(mob,container,name+addendumStr,preferredLoc);
			if((item!=null)&&(item instanceof Item)&&((!visionMatters)||(Sense.canBeSeenBy(item,mob))))
				V.addElement(item);
			if(item==null) return V;
			addendumStr="."+(++addendum);
		}
		while(allFlag);
		return V;
	}

	public void wear(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Wear what?");
			return;
		}
		commands.removeElementAt(0);
		Vector items=fetchItemList(mob,mob,null,commands,Item.WORN_REQ_UNWORNONLY,true);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<items.size();i++)
			wear(mob,(Item)items.elementAt(i),false);
	}

	public void hold(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Hold what?");
			return;
		}
		commands.removeElementAt(0);
		Vector items=fetchItemList(mob,mob,null,commands,Item.WORN_REQ_UNWORNONLY,true);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<items.size();i++)
			hold(mob,(Item)items.elementAt(i),false);
	}

	public void wield(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Wield what?");
			return;
		}
		commands.removeElementAt(0);
		Vector items=fetchItemList(mob,mob,null,commands,Item.WORN_REQ_UNWORNONLY,true);
		if(items.size()==0)
			mob.tell("You don't seem to be carrying that.");
		else
		for(int i=0;i<items.size();i++)
			wield(mob,(Item)items.elementAt(i),false);
	}

	public void drink(MOB mob, Vector commands)
	{
		if((commands.size()<2)&&(!(mob.location() instanceof Drink)))
		{
			mob.tell("Drink what?");
			return;
		}
		commands.removeElementAt(0);
		Environmental thisThang=null;
		if((commands.size()==0)&&(mob.location() instanceof Drink))
			thisThang=mob.location();
		else
		{
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0),Item.WORN_REQ_ANY);
			if((thisThang==null)
			||((thisThang!=null)
			   &&(!mob.isMine(thisThang))
			   &&(!Sense.canBeSeenBy(thisThang,mob))))
			{
				mob.tell("You don't see '"+Util.combine(commands,0)+"' here.");
				return;
			}
		}
		String ofWhat="";
		if((thisThang instanceof Drink)
		&&(((Drink)thisThang).liquidType()!=EnvResource.RESOURCE_FRESHWATER))
			ofWhat=" of "+EnvResource.RESOURCE_DESCS[((Drink)thisThang).liquidType()&EnvResource.RESOURCE_MASK].toLowerCase();
		FullMsg newMsg=new FullMsg(mob,thisThang,null,Affect.MSG_DRINK,"<S-NAME> take(s) a drink"+ofWhat+" from <T-NAMESELF>.");
		if(mob.location().okAffect(newMsg))
			mob.location().send(mob,newMsg);
	}

	public void eat(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Eat what?");
			return;
		}
		commands.removeElementAt(0);

		Environmental thisThang=null;
		thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0),Item.WORN_REQ_ANY);
		if((thisThang==null)
		||((thisThang!=null)
		   &&(!mob.isMine(thisThang))
		   &&(!Sense.canBeSeenBy(thisThang,mob))))
		{
			mob.tell("You don't see '"+Util.combine(commands,0)+"' here.");
			return;
		}
		FullMsg newMsg=new FullMsg(mob,thisThang,null,Affect.MSG_EAT,"<S-NAME> eat(s) <T-NAMESELF>.");
		if(mob.location().okAffect(newMsg))
			mob.location().send(mob,newMsg);
	}
	public void read(MOB mob, Environmental thisThang, String theRest)
	{
		if((thisThang==null)||((!(thisThang instanceof Item)&&(!(thisThang instanceof Exit))))||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
		{
			mob.tell("You don't seem to have that.");
			return;
		}
		if(thisThang instanceof Item)
		{
			Item thisItem=(Item)thisThang;
			if((thisItem.isGettable())&&(!mob.isMine(thisItem)))
			{
				mob.tell("You don't seem to be carrying that.");
				return;
			}
		}
		String soMsg="<S-NAME> read(s) <T-NAMESELF>.";
		String tMsg=theRest;
		if((tMsg.trim().length()==0)||(thisThang instanceof MOB)) tMsg=soMsg;
		FullMsg newMsg=new FullMsg(mob,thisThang,null,Affect.MSG_READSOMETHING,soMsg,Affect.MSG_READSOMETHING,tMsg,Affect.MSG_READSOMETHING,soMsg);
		if(mob.location().okAffect(newMsg))
			mob.location().send(mob,newMsg);

	}

	public void read(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Read what?");
			return;
		}
		commands.removeElementAt(0);

		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.elementAt(commands.size()-1),Item.WORN_REQ_ANY);
		String theRest=null;
		if(thisThang==null)
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0),Item.WORN_REQ_ANY);
		else
		{
			commands.removeElementAt(commands.size()-1);
			theRest=Util.combine(commands,0);
		}
		read(mob,thisThang, theRest);
	}

	public boolean remove(MOB mob, Item item, boolean quiet)
	{
		FullMsg newMsg=new FullMsg(mob,item,null,Affect.MSG_GET,quiet?null:"<S-NAME> remove(s) <T-NAME>.");
		if(mob.location().okAffect(newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	public void remove(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Remove what?");
			return;
		}
		commands.removeElementAt(0);
		Vector items=fetchItemList(mob,mob,null,commands,Item.WORN_REQ_WORNONLY,false);
		if(items.size()==0)
			mob.tell("You don't seem to be wearing that.");
		else
		for(int i=0;i<items.size();i++)
			remove(mob,(Item)items.elementAt(i),false);
	}

	public void push(MOB mob, String whatToOpen, CommandSet commandSet)
	{

		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen,Item.WORN_REQ_ANY);

		if(openThis==null)
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return;
		}
		int malmask=(openThis instanceof MOB)?Affect.MASK_MALICIOUS:0;
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_PUSH|malmask,"^F<S-NAME> push(es) <T-NAME>^?.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public void pull(MOB mob, String whatToOpen)
	{

		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().getExitInDir(dirCode);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen,Item.WORN_REQ_ANY);

		if(openThis==null)
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_PULL,"<S-NAME> pull(s) <T-NAME>.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

}
