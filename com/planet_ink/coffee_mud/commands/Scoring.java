package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;
public class Scoring
{
	public static String areasList=null;
	
	public static StringBuffer getInventory(MOB seer, MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem.location()==null)
			&&(thisItem.amWearingAt(Item.INVENTORY))
			&&(Sense.canBeSeenBy(thisItem,seer)))
				msg.append(thisItem.name()+Sense.colorCodes(thisItem,mob)+"\n\r");
		}
		if(mob.getMoney()>0)
			msg.append(mob.getMoney()+" gold coins.\n\r");
		return msg;
	}
	public static void inventory(MOB mob)
	{
		StringBuffer msg=getInventory(mob,mob);
		if(msg.length()==0)
			mob.tell("You are carrying:\n\rNothing!\n\r");
		else
		if(!mob.isMonster())
			mob.session().rawPrintln("You are carrying:\n\r"+msg.toString());
	}
	
	public static void score(MOB mob)
	{
		int adjustedArmor=100-(int)TheFight.adjustedArmor(mob);
		int adjustedAttack=(int)TheFight.adjustedAttackBonus(mob);
		
		
		StringBuffer msg=new StringBuffer("");
		msg.append("You are "+mob.name()+" the level "+mob.envStats().level()+" "+mob.charStats().getMyClass().name()+".\n\r");
		msg.append("You are a "+((mob.charStats().getGender()=='M')?"male":"female")+" "+mob.charStats().getMyRace().name());
		if(mob.getWorshipCharID().length()>0)
			msg.append(" who worships "+mob.getWorshipCharID()+"\n\r");
		msg.append(".\n\r");
		msg.append("\n\rYour stats are: \n\r"+mob.charStats().getStats(mob.charStats().getMyClass().maxStat())+"\n\r");
		msg.append("You have "+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+" hit points, ");
		msg.append(mob.curState().getMana()+"/"+mob.maxState().getMana()+" mana, and ");
		msg.append(mob.curState().getMovement()+"/"+mob.maxState().getMovement()+" movement.\n\r");
		msg.append("You have "+mob.envStats().weight()+"/"+mob.charStats().maxCarry()+" pounds of encumbrance.\n\r");
		msg.append("You have "+mob.getPractices()+" practices, "+mob.getTrains()+" training sessions, and "+mob.getQuestPoint()+" quest points.\n\r");
		msg.append("You have scored "+mob.getExperience()+" experience points, and have been online for "+Math.round(Util.div(mob.getAgeHours(),60.0))+" hours.\n\r");
		msg.append("You need "+(mob.getExpNeededLevel())+" experience points to level.\n\r");
		msg.append("Your alignment is      : "+alignmentStr(mob)+" ("+mob.getAlignment()+").\n\r");
		msg.append("Your armored defense is: "+TheFight.armorStr(adjustedArmor)+"\n\r");
		msg.append("Your combat prowess is : "+TheFight.fightingProwessStr(adjustedAttack)+"\n\r");
		msg.append("Wimpy is set to "+mob.getWimpHitPoint()+" hit points.\n\r");
		
		if(Sense.isSleeping(mob))
			msg.append("You are sleeping.\n\r");
		else
		if(Sense.isSitting(mob))
			msg.append("You are resting.\n\r");
		else
		if(Sense.isSwimming(mob))
			msg.append("You are swimming.\n\r");
		else
		if(Sense.isFlying(mob))
			msg.append("You are flying.\n\r");
		else
			msg.append("You are standing.\n\r");
		
		if(Sense.isInvisible(mob))
			msg.append("You are invisible.\n\r");
		if(Sense.isHidden(mob))
			msg.append("You are hidden.\n\r");
		if(Sense.isSneaking(mob))
			msg.append("You are sneaking.\n\r");
			
		if(mob.curState().getHunger()<1)
			msg.append("You are hungry.\n\r");
		if(mob.curState().getThirst()<1)
			msg.append("You are thirsty.\n\r");
		msg.append("\n\rYou are affected by: "+getAffects(mob)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}
	
	public static void affected(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rYou are affected by: "+getAffects(mob)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}
	
	public static void skills(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rYour skills: "+getAbilities(mob,Ability.SKILL)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}
	
	public static void prayers(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rPrayers known: "+getAbilities(mob,Ability.PRAYER)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}
	
	public static void songs(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rSongs known: "+getAbilities(mob,Ability.SONG)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}
	
	public static void spells(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rYour spells: "+getAbilities(mob,Ability.SPELL)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}
	
	public static StringBuffer getAffects(MOB affected)
	{
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<affected.numAffects();a++)
		{
			Ability thisAffect=affected.fetchAffect(a);
			if(thisAffect.displayText().length()>0)
				msg.append("\n\r"+thisAffect.displayText());
		}
		if(msg.length()==0)
			msg.append("Nothing!");
		return msg;
	}
	
	
	public static StringBuffer getAbilities(MOB able, int ofType)
	{
		int highestLevel=0;
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<able.numAbilities();a++)
		{
			Ability thisAbility=able.fetchAbility(a);
			if((thisAbility.envStats().level()>highestLevel)&&(thisAbility.classificationCode()==ofType))
				highestLevel=thisAbility.envStats().level();
		}
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			int col=0;
			for(int a=0;a<able.numAbilities();a++)
			{
				Ability thisAbility=able.fetchAbility(a);
				if((thisAbility.envStats().level()==l)&&(thisAbility.classificationCode()==ofType))
				{
					if(thisLine.length()==0)
						thisLine.append("\n\rLevel "+l+":\n\r");
					if(++col>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("["+Util.padRight(Integer.toString(thisAbility.profficiency()),3)+"%] "+Util.padRight(thisAbility.name(),22));
				}
			}
			if(thisLine.length()>0)
				msg.append(thisLine);
		}
		if(msg.length()==0)
			msg.append("None!");
		return msg;
	}
	
	
	public static StringBuffer getEquipment(MOB seer, MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		for(int l=0;l<16;l++)
		{
			int wornCode=new Double(Math.pow(new Integer(2).doubleValue(),new Integer(l).doubleValue())).intValue();
			String header="("+Sense.wornLocation(wornCode)+")";
			header+=Util.SPACES.substring(0,20-header.length())+": ";
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item thisItem=mob.fetchInventory(i);
				if((thisItem.location()==null)&&(thisItem.amWearingAt(wornCode))&&(Sense.canBeSeenBy(thisItem,seer)))
					msg.append(header+thisItem.name()+Sense.colorCodes(thisItem,seer)+"\n\r");
			}
		}
		if(msg.length()==0)
			msg.append("nothing!\n\r");
		
		return msg;
	}
	public static void equipment(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().rawPrintln("You are wearing:\n\r"+getEquipment(mob,mob));
	}
	
	public static String alignmentStr(MOB mob)
	{
		int al=mob.getAlignment();
		if(al<50)
			return "pure evil";
		else
		if(al<300)
			return "evil";
		else
		if(al<425)
			return "somewhat evil";
		else
		if(al<575)
			return "pure neutral";
		else
		if(al<700)
			return "somewhat good";
		else
		if(al<950)
			return "good";
		else
			return "pure goodness";
		
	}
	public static void commands(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().rawPrintln("Complete commands list: \n\r"+CommandProcessor.commandSet.commandList());
	}
	public static void socials(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().rawPrintln("Complete socials list: \n\r"+MUD.allSocials.getSocialsList());
	}
	
	public static void areas(MOB mob)
	{
		if(areasList==null)
		{
			
			Hashtable areasHash=new Hashtable();
			Vector areasVec=new Vector();
			for(int m=0;m<MUD.map.size();m++)
			{
				Room room=(Room)MUD.map.elementAt(m);
				if(areasHash.get(room.getAreaID())==null)
				{
					areasHash.put(room.getAreaID(),room.getAreaID());
					areasVec.addElement(room.getAreaID());
				}
			}
			Collections.sort((List)areasVec);
			StringBuffer msg=new StringBuffer("Complete areas list: \n\r");
			int col=0;
			for(int i=0;i<areasVec.size();i++)
			{
				if((++col)>3) 
				{
					msg.append("\n\r");
					col=1;
				}
				
				msg.append(Util.padRight((String)areasVec.elementAt(i),25));
			}
			areasList=msg.toString();
		}
		
		
		
		if(!mob.isMonster())
			mob.session().rawPrintln(areasList);
	}
}
