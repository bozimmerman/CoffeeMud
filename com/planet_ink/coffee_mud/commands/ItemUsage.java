package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class ItemUsage
{
	public Item possibleContainer(MOB mob, Vector commands)
	{
		if(commands.size()==1)
			return null;

		String possibleContainerID=(String)commands.elementAt(commands.size()-1);
		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,possibleContainerID);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang).isAContainer()))
		{
			commands.removeElementAt(commands.size()-1);
			return (Item)thisThang;
		}
		return null;
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
			mob.tell("You don't have a "+((String)commands.elementAt(0))+".");
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
				mob.tell(compareThis.name()+" look better than "+toThis.name()+".");
			else
				mob.tell(compareThis.name()+" look worse than "+toThis.name()+".");
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

	public boolean get(MOB mob, Item container, Item getThis)
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

		FullMsg msg=new FullMsg(mob,target,tool,Affect.MSG_GET,"<S-NAME> get(s) "+theWhat);
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

		Item container=possibleContainer(mob,commands);
		String whatToGet=Util.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		int addendum=1;
		String addendumStr="";
		Environmental last=null;
		do
		{
			Environmental getThis=null;
			if((container!=null)&&(mob.isMine(container)))
			   getThis=mob.location().fetchFromMOBRoomFavorsItems(mob,(Item)container,whatToGet+addendumStr);
			else
			   getThis=mob.location().fetchFromRoomFavorItems((Item)container,whatToGet+addendumStr);

			if((getThis==null)
			||(!(getThis instanceof Item))
			||(!Sense.canBeSeenBy(getThis,mob)))
			{
				if(!doneSomething)
				{
					if(container!=null)
					{
						if(((Container)container).isOpen())
							mob.tell("You don't see anything in "+container.name()+".");
						else
							mob.tell(container.name()+" is closed.");
					}
					else
						mob.tell("You don't see that here.");
				}
				return;
			}

			if((last==getThis)||(!get(mob,container,(Item)getThis)))
				addendumStr="."+(++addendum);
			last=getThis;
			doneSomething=true;
		}while(allFlag);
	}

	public boolean drop(MOB mob, Environmental dropThis)
	{
		FullMsg msg=new FullMsg(mob,dropThis,null,Affect.MSG_DROP,"<S-NAME> drop(s) <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			return true;
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

		Item container=possibleContainer(mob,commands);
		whatToDrop=Util.combine(commands,0);

		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		int addendum=1;
		String addendumStr="";
		Environmental last=null;
		do
		{
			Item dropThis=new SocialProcessor().possibleGold(mob,whatToDrop+addendumStr);
			if(dropThis!=null)
				allFlag=false;
			else
				dropThis=mob.fetchCarried(container,whatToDrop+addendumStr);
			if((dropThis==null)||(dropThis.location()!=container)||(!Sense.canBeSeenBy(dropThis,mob)))
			{
				if((!doneSomething)&&(Util.s_int(whatToDrop)<=0))
					mob.tell("You aren't carrying that.");
				return;
			}
			else
			if(last==dropThis)
			{
				addendumStr="."+(++addendum);
				continue;
			}

			if(!drop(mob,dropThis))
				addendumStr="."+(++addendum);

			last=dropThis;
			doneSomething=true;
		}while(allFlag);
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

		Environmental container=possibleContainer(mob,commands);
		if((container==null)||((container!=null)&&(!Sense.canBeSeenBy(container,mob))))
		{
			mob.tell("I don't see a "+(String)commands.elementAt(commands.size()-1)+" here.");
			return;
		}

		String thingToPut=Util.combine(commands,0);
		boolean doneSomething=false;
		int addendum=1;
		String addendumStr="";
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		Environmental last=null;
		do
		{
			Environmental putThis=new SocialProcessor().possibleGold(mob,thingToPut);
			if(putThis!=null)
				allFlag=false;
			else
				putThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToPut+addendumStr);
			if((putThis==null)||((putThis!=null)&&(!Sense.canBeSeenBy(putThis,mob))))
			{
				if((!doneSomething)&&(Util.s_int(thingToPut)<=0))
					mob.tell("I don't see that here.");
				return;
			}
			else
			if(putThis==container)
			{
				addendumStr="."+(++addendum);
				continue;
			}
			else
			if((doneSomething)&&(!(putThis instanceof Item)))
				return;

			if((!mob.isMine(putThis))&&(doneSomething))
				return;

			if(!mob.isMine(container))
			{
				FullMsg newMsg=new FullMsg(mob,putThis,null,Affect.MSG_DROP,null);
				if(!mob.location().okAffect(newMsg))
					return;
				mob.location().send(mob,newMsg);
			}
			FullMsg newMsg=new FullMsg(mob,container,putThis,Affect.MSG_PUT,"<S-NAME> put(s) "+putThis.name()+" in <T-NAME>");
			if((last!=putThis)&&(mob.location().okAffect(newMsg)))
				mob.location().send(mob,newMsg);
			else
				addendumStr="."+(++addendum);
			last=putThis;
			doneSomething=true;
		}while(allFlag);
	}

	public void fill(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Fill what, from what?");
			return;
		}
		commands.removeElementAt(0);
		if(commands.size()<2)
		{
			mob.tell("From what should I fill the "+(String)commands.elementAt(0));
			return;
		}
		String thingToFillFrom=(String)commands.elementAt(commands.size()-1);
		Environmental fillFromThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFillFrom);
		if((fillFromThis==null)||((fillFromThis!=null)&&(!Sense.canBeSeenBy(fillFromThis,mob))))
		{
			mob.tell("I don't see a "+thingToFillFrom+" here.");
			return;
		}
		commands.removeElementAt(commands.size()-1);

		String thingToFill=Util.combine(commands,0);
		boolean doneSomething=false;
		int addendum=1;
		String addendumStr="";
		Environmental lastThingFilled=null;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Environmental fillThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,thingToFill+addendumStr);
			if((fillThis==null)||((fillThis!=null)&&(!Sense.canBeSeenBy(fillThis,mob))))
			{
				if((!doneSomething)&&(Util.s_int(thingToFill)<=0))
					mob.tell("I don't see that here.");
				return;
			}
			else
			if(fillThis==lastThingFilled)
			{
				addendumStr="."+(++addendum);
				continue;
			}
			else
			if((doneSomething)&&(!(fillThis instanceof Item)))
			   return;

			if((!mob.isMine(fillThis))&&(!doneSomething))
				if(!get(mob,null,(Item)fillThis))
					return;

			if(!mob.isMine(fillThis))
				return;
			FullMsg newMsg=new FullMsg(mob,fillThis,fillFromThis,Affect.MSG_FILL,"<S-NAME> fill(s) <T-NAME> from "+fillFromThis.name()+".");
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public boolean wear(MOB mob, Item item)
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
		FullMsg newMsg=new FullMsg(mob,item,null,msgType,str);
		if(mob.location().okAffect(newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	public boolean wield(MOB mob, Item item)
	{
		FullMsg newMsg=new FullMsg(mob,item,null,Affect.MSG_WIELD,"<S-NAME> wield(s) <T-NAME>.");
		if(mob.location().okAffect(newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	public boolean hold(MOB mob, Item item)
	{
		FullMsg newMsg=new FullMsg(mob,item,null,Affect.MSG_HOLD,"<S-NAME> hold(s) <T-NAME>.");
		if(mob.location().okAffect(newMsg))
		{
			mob.location().send(mob,newMsg);
			return true;
		}
		return false;
	}

	public void wear(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Wear what?");
			return;
		}
		commands.removeElementAt(0);

		boolean doneSomething=false;
		int addendum=1;
		String addendumStr="";
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		Environmental last=null;
		do
		{
			Item thisItem=mob.fetchCarried(null,Util.combine(commands,0)+addendumStr);
			if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't seem to be carrying that.");
				return;
			}
			if(!thisItem.amWearingAt(Item.INVENTORY))
			{
				addendumStr="."+(++addendum);
				continue;
			}
			if((last==thisItem)||(!wear(mob,thisItem)))
				addendumStr="."+(++addendum);
			doneSomething=true;
			last=thisItem;
		}while(allFlag);
	}

	public void hold(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Hold what?");
			return;
		}
		commands.removeElementAt(0);
		boolean doneSomething=false;
		int addendum=1;
		String addendumStr="";
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		Environmental last=null;
		do
		{
			Item thisItem=mob.fetchCarried(null,Util.combine(commands,0)+addendumStr);
			if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't seem to be carrying that.");
				return;
			}
			if(!thisItem.amWearingAt(Item.INVENTORY))
			{
				addendumStr="."+(++addendum);
				continue;
			}
			if((last==thisItem)
			||(!alreadyWornMsg(mob,thisItem))
			||(!hold(mob,thisItem)))
				addendumStr="."+(++addendum);
			last=thisItem;
			doneSomething=true;
		}while(allFlag);
	}

	public boolean alreadyWornMsg(MOB mob, Item thisItem)
	{
		if(!thisItem.amWearingAt(Item.INVENTORY))
		{
			if(thisItem.amWearingAt(Item.WIELD))
				mob.tell(thisItem.name()+" is already being wielded.");
			else
			if(thisItem.amWearingAt(Item.HELD))
				mob.tell(thisItem.name()+" is already being held.");
			else
			if(thisItem.amWearingAt(Item.FLOATING_NEARBY))
				mob.tell(thisItem.name()+" is floating nearby.");
			else
				mob.tell(thisItem.name()+"is already being worn.");
			return false;
		}
		return true;
	}

	public void wield(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Wield what?");
			return;
		}
		commands.removeElementAt(0);

		Item thisItem=mob.fetchCarried(null,Util.combine(commands,0));
		if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
		{
			mob.tell("You don't seem to be carrying that.");
			return;
		}
		if(!alreadyWornMsg(mob,thisItem))
			return;
		wield(mob,thisItem);
	}

	public void drink(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Drink what?");
			return;
		}
		commands.removeElementAt(0);

		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0));
		if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg newMsg=new FullMsg(mob,thisThang,null,Affect.MSG_DRINK,"<S-NAME> take(s) a drink from <T-NAMESELF>.");
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

		Item thisItem=mob.fetchInventory(Util.combine(commands,0));
		if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
		{
			mob.tell("You don't seem to be carrying that.");
			return;
		}
		FullMsg newMsg=new FullMsg(mob,thisItem,null,Affect.MSG_EAT,"<S-NAME> eat(s) <T-NAMESELF>.");
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

		Environmental thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,(String)commands.elementAt(commands.size()-1));
		String theRest=null;
		if(thisThang==null)
			thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,Util.combine(commands,0));
		else
		{
			commands.removeElementAt(commands.size()-1);
			theRest=Util.combine(commands,0);
		}
		read(mob,thisThang, theRest);
	}

	public boolean remove(MOB mob, Item item)
	{
		FullMsg newMsg=new FullMsg(mob,item,null,Affect.MSG_GET,"<S-NAME> remove(s) <T-NAME>.");
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
		boolean doneSomething=false;
		int addendum=1;
		String addendumStr="";
		Environmental last=null;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Item thisItem=mob.fetchWornItem(Util.combine(commands,0)+addendumStr);
			if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't seem to be wearing that.");
				return;
			}
			if(thisItem.amWearingAt(Item.INVENTORY))
			{
				if(!doneSomething)
					mob.tell("You aren't wearing that.");
				return;
			}
			if((last==thisItem)||(!remove(mob,thisItem)))
				addendumStr="."+(++addendum);
			last=thisItem;
			doneSomething=true;
		}while(allFlag);
	}


	public void push(MOB mob, String whatToOpen, CommandSet commandSet)
	{

		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().exits()[dirCode];
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen);

		if(openThis==null)
		{
			mob.tell("You don't see that here.");
			return;
		}
		int malmask=(openThis instanceof MOB)?Affect.MASK_MALICIOUS:0;
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_PUSH|malmask,"<S-NAME> push(es) <T-NAME>.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}
	public void pull(MOB mob, String whatToOpen)
	{

		Environmental openThis=null;
		int dirCode=Directions.getGoodDirectionCode(whatToOpen);
		if(dirCode>=0)
			openThis=mob.location().exits()[dirCode];
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen);

		if(openThis==null)
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.MSG_PULL,"<S-NAME> pull(s) <T-NAME>.");
		if(mob.location().okAffect(msg))
			mob.location().send(mob,msg);
	}

}
