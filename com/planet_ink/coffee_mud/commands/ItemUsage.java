package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.Items.Weapons.*;
import com.planet_ink.coffee_mud.Items.Armor.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;
public class ItemUsage
{
	public static Item possibleContainer(MOB mob, Vector commands)
	{
		if(commands.size()==1)
			return null;

		String possibleContainerID=(String)commands.elementAt(commands.size()-1);
		Environmental thisThang=mob.location().fetchFromMOBRoom(mob,null,possibleContainerID);
		if((thisThang!=null)
		&&(thisThang instanceof Item)
		&&(((Item)thisThang).isAContainer()))
		{
			commands.removeElementAt(commands.size()-1);
			return (Item)thisThang;
		}
		return null;
	}

	public static void compare(MOB mob, Vector commands)
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
		Item toThis=mob.fetchInventory(CommandProcessor.combine(commands,1));
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

	public static void get(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Get what?");
			return;
		}
		commands.removeElementAt(0);

		Item container=possibleContainer(mob,commands);
		String whatToGet=CommandProcessor.combine(commands,0);
		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Environmental getThis=null;
			if((container!=null)&&(mob.isMine(container)))
			   getThis=mob.location().fetchFromMOBRoom(mob,(Item)container,whatToGet);
			else
			   getThis=mob.location().fetchFromRoom((Item)container,whatToGet);

			if((getThis==null)||((getThis!=null)&&(!Sense.canBeSeenBy(getThis,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't see that here.");
				return;
			}

			if((doneSomething)&&(!(getThis instanceof Item)))
			   return;

			String theWhat=getThis.name();
			Environmental target=getThis;
			Environmental tool=null;
			if(getThis instanceof Item)
			{
				if(container!=null)
				{
					tool=getThis;
					target=container;
					theWhat+=" from "+container.name();
				}
			}
			FullMsg msg=new FullMsg(mob,target,tool,Affect.HANDS_GET,"<S-NAME> get(s) "+theWhat,Affect.HANDS_GET,null,Affect.VISUAL_WNOISE,"<S-NAME> get(s) "+theWhat);
			if(!mob.location().okAffect(msg))
				return;
			mob.location().send(mob,msg);
			if(!mob.isMine(target))
			{
				msg=new FullMsg(mob,getThis,null,Affect.HANDS_GET,null,Affect.HANDS_GET,null,Affect.VISUAL_WNOISE,null);
				if(!mob.location().okAffect(msg))
					return;
				mob.location().send(mob,msg);
			}
			doneSomething=true;
		}while(allFlag);
	}

	public static void drop(MOB mob, Vector commands)
	{
		String whatToDrop=null;
		if(commands.size()<2)
		{
			mob.tell("Drop what?");
			return;
		}
		commands.removeElementAt(0);

		Item container=possibleContainer(mob,commands);
		whatToDrop=CommandProcessor.combine(commands,0);

		boolean doneSomething=false;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Item dropThis=SocialProcessor.possibleGold(mob,whatToDrop);
			if(dropThis!=null)
				allFlag=false;
			else
				dropThis=mob.fetchCarried(container,whatToDrop);
			if((dropThis==null)||(dropThis.location()!=container)||(!Sense.canBeSeenBy(dropThis,mob)))
			{
				if((!doneSomething)&&(Util.s_int(whatToDrop)<=0))
					mob.tell("You aren't carrying that.");
				return;
			}

			if((doneSomething)&&(!(dropThis instanceof Item)))
			   return;

			FullMsg msg=new FullMsg(mob,dropThis,null,Affect.HANDS_DROP,"<S-NAME> drop(s) "+dropThis.name(),Affect.HANDS_DROP,null,Affect.VISUAL_WNOISE,"<S-NAME> drop(s) "+dropThis.name());
			if(!mob.location().okAffect(msg))
				return;
			mob.location().send(mob,msg);
			doneSomething=true;
		}while(allFlag);
	}

	public static void put(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Put what where?");
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

		String thingToPut=CommandProcessor.combine(commands,0);
		boolean doneSomething=false;
		int addendum=1;
		String addendumStr="";
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Environmental putThis=SocialProcessor.possibleGold(mob,thingToPut);
			if(putThis!=null)
				allFlag=false;
			else
				putThis=mob.location().fetchFromMOBRoom(mob,null,thingToPut+addendumStr);
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

			if(!mob.isMine(putThis))
			{
				FullMsg newMsg=new FullMsg(mob,putThis,null,Affect.HANDS_GET,"<S-NAME> get(s) "+putThis.name(),Affect.HANDS_GET,"<S-NAME> get(s) you",Affect.VISUAL_WNOISE,"<S-NAME> get(s) "+putThis.name());
				if(!mob.location().okAffect(newMsg))
					return;
				mob.location().send(mob,newMsg);
			}
			if(!mob.isMine(putThis))
				return;
			if(!mob.isMine(container))
			{
				FullMsg newMsg=new FullMsg(mob,putThis,null,Affect.HANDS_DROP,null,Affect.HANDS_DROP,null,Affect.VISUAL_WNOISE,null);
				if(!mob.location().okAffect(newMsg))
					return;
				mob.location().send(mob,newMsg);
			}
			FullMsg newMsg=new FullMsg(mob,container,putThis,Affect.HANDS_PUT,"<S-NAME> put(s) "+putThis.name()+" in "+container.name(),Affect.HANDS_PUT,mob.name()+" put(s) something in you.",Affect.VISUAL_WNOISE,"<S-NAME> put(s) "+putThis.name()+" in "+container.name());
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public static void fill(MOB mob, Vector commands)
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
		Environmental fillFromThis=mob.location().fetchFromMOBRoom(mob,null,thingToFillFrom);
		if((fillFromThis==null)||((fillFromThis!=null)&&(!Sense.canBeSeenBy(fillFromThis,mob))))
		{
			mob.tell("I don't see a "+thingToFillFrom+" here.");
			return;
		}
		commands.removeElementAt(commands.size()-1);

		String thingToFill=CommandProcessor.combine(commands,0);
		boolean doneSomething=false;
		int addendum=1;
		String addendumStr="";
		Environmental lastThingFilled=null;
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Environmental fillThis=mob.location().fetchFromMOBRoom(mob,null,thingToFill+addendumStr);
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

			if(!mob.isMine(fillThis))
			{
				FullMsg newMsg=new FullMsg(mob,fillThis,null,Affect.HANDS_GET,"<S-NAME> get(s) "+fillThis.name(),Affect.HANDS_GET,"<S-NAME> get(s) you",Affect.VISUAL_WNOISE,"<S-NAME> get(s) "+fillThis.name());
				if(!mob.location().okAffect(newMsg))
					return;
				mob.location().send(mob,newMsg);
			}
			if(!mob.isMine(fillThis))
				return;
			FullMsg newMsg=new FullMsg(mob,fillThis,fillFromThis,Affect.HANDS_FILL,"<S-NAME> fill(s) "+fillThis.name()+" from "+fillFromThis.name()+".",Affect.HANDS_FILL,mob.name()+" fill(s) you up.",Affect.VISUAL_WNOISE,"<S-NAME> fill(s) "+fillThis.name()+" from "+fillFromThis.name()+".");
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public static void wear(MOB mob, Vector commands)
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
		do
		{
			Item thisItem=mob.fetchCarried(null,CommandProcessor.combine(commands,0)+addendumStr);
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
			FullMsg newMsg=new FullMsg(mob,thisItem,null,Affect.HANDS_WEAR,"You put on "+thisItem.name()+".",Affect.HANDS_WEAR,null,Affect.NO_EFFECT,null);
			doneSomething=true;
			if(!mob.location().okAffect(newMsg))
			{
				addendumStr="."+(++addendum);
				continue;
			}
			mob.location().send(mob,newMsg);
		}while(allFlag);
	}

	public static void hold(MOB mob, Vector commands)
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
		do
		{
			Item thisItem=mob.fetchCarried(null,CommandProcessor.combine(commands,0)+addendumStr);
			if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
			{
				if(!doneSomething)
					mob.tell("You don't seem to have that.");
				return;
			}
			if(!thisItem.amWearingAt(Item.INVENTORY))
			{
				addendumStr="."+(++addendum);
				continue;
			}
			FullMsg newMsg=new FullMsg(mob,thisItem,null,Affect.HANDS_HOLD,"You hold "+thisItem.name()+".",Affect.HANDS_HOLD,null,Affect.NO_EFFECT,null);
			if(!mob.location().okAffect(newMsg))
			{
				addendumStr="."+(++addendum);
				continue;
			}
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}

	public static void wield(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Wield what?");
			return;
		}
		commands.removeElementAt(0);

		Item thisItem=mob.fetchCarried(null,CommandProcessor.combine(commands,0));
		if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
		{
			mob.tell("You don't seem to have that.");
			return;
		}
		if(!thisItem.amWearingAt(Item.INVENTORY))
		{
			if(thisItem.amWearingAt(Item.WIELD))
				mob.tell("That is already being wielded.");
			else
			if(thisItem.amWearingAt(Item.HELD))
				mob.tell("That is already being held.");
			else
			if(thisItem.amWearingAt(Item.FLOATING_NEARBY))
				mob.tell("That is already floating nearby.");
			else
				mob.tell("That is already being worn.");
			return;
		}
		FullMsg newMsg=new FullMsg(mob,thisItem,null,Affect.HANDS_WIELD,"You wield "+thisItem.name()+".",Affect.HANDS_WIELD,null,Affect.NO_EFFECT,null);
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}

	public static void drink(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Drink what?");
			return;
		}
		commands.removeElementAt(0);

		Environmental thisThang=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		if((thisThang==null)||((thisThang!=null)&&(!Sense.canBeSeenBy(thisThang,mob))))
		{
			mob.tell("You don't seem to have that.");
			return;
		}
		FullMsg newMsg=new FullMsg(mob,thisThang,null,Affect.TASTE_WATER,"You take a drink from "+thisThang.name()+".",Affect.TASTE_WATER,null,Affect.VISUAL_WNOISE,"<S-NAME> takes a drink from <T-NAME>.");
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}

	public static void eat(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Drink what?");
			return;
		}
		commands.removeElementAt(0);

		Item thisItem=mob.fetchInventory(CommandProcessor.combine(commands,0));
		if((thisItem==null)||((thisItem!=null)&&(!Sense.canBeSeenBy(thisItem,mob))))
		{
			mob.tell("You don't seem to have that.");
			return;
		}
		FullMsg newMsg=new FullMsg(mob,thisItem,null,Affect.TASTE_FOOD,"You eat "+thisItem.name()+".",Affect.TASTE_FOOD,null,Affect.VISUAL_WNOISE,"<S-NAME> eats <T-NAME>.");
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}

	public static void read(MOB mob, Vector commands)
	{
		if(commands.size()<2)
		{
			mob.tell("Read what?");
			return;
		}
		commands.removeElementAt(0);

		Environmental thisThang=mob.location().fetchFromMOBRoom(mob,null,(String)commands.elementAt(commands.size()-1));
		String theRest=null;
		if(thisThang==null)
			thisThang=mob.location().fetchFromMOBRoom(mob,null,CommandProcessor.combine(commands,0));
		else
		{
			commands.removeElementAt(commands.size()-1);
			theRest=CommandProcessor.combine(commands,0);
		}
		
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
				mob.tell("You don't seem to have that.");
				return;
			}
		}
		
		FullMsg newMsg=new FullMsg(mob,thisThang,null,Affect.VISUAL_READ,"You read <T-NAME>.",Affect.VISUAL_READ,theRest,Affect.VISUAL_WNOISE,"<S-NAME> reads <T-NAME>.");
		if(!mob.location().okAffect(newMsg))
			return;
		mob.location().send(mob,newMsg);
	}

	public static void remove(MOB mob, Vector commands)
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
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		do
		{
			Item thisItem=mob.fetchWornItem(CommandProcessor.combine(commands,0));
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
			FullMsg newMsg=new FullMsg(mob,thisItem,null,Affect.HANDS_GET,"You remove "+thisItem.name()+".",Affect.HANDS_GET,null,Affect.NO_EFFECT,null);
			if(!mob.location().okAffect(newMsg))
				return;
			mob.location().send(mob,newMsg);
			doneSomething=true;
		}while(allFlag);
	}


	public static void push(MOB mob, String whatToOpen)
	{

		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatToOpen.toUpperCase());
		Environmental openThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
			{
				int dirCode=Directions.getDirectionCode(whatToOpen);
				openThis=mob.location().getExit(dirCode);
			}
		}
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoom(mob,null,whatToOpen);

		if(openThis==null)
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.HANDS_PUSH,Affect.HANDS_PUSH,Affect.VISUAL_WNOISE,"<S-NAME> push(es) "+openThis.name());
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
	}
	public static void pull(MOB mob, String whatToOpen)
	{

		Integer cmd=(Integer)CommandProcessor.commandSet.get(whatToOpen.toUpperCase());
		Environmental openThis=null;
		if(cmd!=null)
		{
			int dir=cmd.intValue();
			if(
			  (dir==CommandSet.NORTH)
			||(dir==CommandSet.SOUTH)
			||(dir==CommandSet.EAST)
			||(dir==CommandSet.WEST)
			||(dir==CommandSet.UP)
			||(dir==CommandSet.DOWN))
			{
				int dirCode=Directions.getDirectionCode(whatToOpen);
				openThis=mob.location().getExit(dirCode);
			}
		}
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoom(mob,null,whatToOpen);

		if(openThis==null)
		{
			mob.tell("You don't see that here.");
			return;
		}
		FullMsg msg=new FullMsg(mob,openThis,null,Affect.HANDS_PULL,Affect.HANDS_PULL,Affect.VISUAL_WNOISE,"<S-NAME> pull(s) "+openThis.name());
		if(!mob.location().okAffect(msg))
			return;
		mob.location().send(mob,msg);
	}

}
