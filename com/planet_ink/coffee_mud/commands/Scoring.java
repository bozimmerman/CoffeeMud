package com.planet_ink.coffee_mud.commands;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class Scoring
{
	public StringBuffer getInventory(MOB seer, MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		boolean foundButUnseen=false;
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem.location()==null)
			&&(thisItem.amWearingAt(Item.INVENTORY)))
	  	   {
				if(Sense.canBeSeenBy(thisItem,seer))
					msg.append(thisItem.name()+Sense.colorCodes(thisItem,mob)+"\n\r");
				else
					foundButUnseen=true;
			}
		}
		if((foundButUnseen)&&(Sense.isSleeping(seer)))
			msg.append("(nothing you can see right now)");
		else
		if(mob.getMoney()>0)
			msg.append(mob.getMoney()+" gold coins.\n\r");
		return msg;
	}
	public void inventory(MOB mob)
	{
		StringBuffer msg=getInventory(mob,mob);
		if(msg.length()==0)
			mob.tell("You are carrying:\n\rNothing!\n\r");
		else
		if(!mob.isMonster())
			mob.session().rawPrintln("You are carrying:\n\r"+msg.toString());
	}

	public void score(MOB mob)
	{
		TheFight theFight=new TheFight();
		
		int adjustedArmor=100-(int)theFight.adjustedArmor(mob);
		int adjustedAttack=(int)theFight.adjustedAttackBonus(mob);


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
		msg.append("You have "+mob.envStats().weight()+"/"+mob.maxCarry()+" pounds of encumbrance.\n\r");
		msg.append("You have "+mob.getPractices()+" practices, "+mob.getTrains()+" training sessions, and "+mob.getQuestPoint()+" quest points.\n\r");
		msg.append("You have scored "+mob.getExperience()+" experience points, and have been online for "+Math.round(Util.div(mob.getAgeHours(),60.0))+" hours.\n\r");
		msg.append("You need "+(mob.getExpNeededLevel())+" experience points to level.\n\r");
		msg.append("Your alignment is      : "+alignmentStr(mob)+" ("+mob.getAlignment()+").\n\r");
		msg.append("Your armored defense is: "+theFight.armorStr(adjustedArmor)+"\n\r");
		msg.append("Your combat prowess is : "+theFight.fightingProwessStr(adjustedAttack)+"\n\r");
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
		if(Sense.isClimbing(mob))
			msg.append("You are climbing.\n\r");
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

	public void affected(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rYou are affected by: "+getAffects(mob)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}

	public void skills(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		if(getAbilities(mob,Ability.THIEF_SKILL).length()<10)
			msg.append("\n\rYour skills: "+getAbilities(mob,Ability.SKILL)+"\n\r");
		else
		{
			msg.append("\n\rGeneral skills: "+getAbilities(mob,Ability.SKILL)+"\n\r");
			msg.append("\n\rThief skills: "+getAbilities(mob,Ability.THIEF_SKILL)+"\n\r");
		}

		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}

	public void qualify(MOB mob)
	{
		StringBuffer msg=new StringBuffer("You now qualify for the following:");
		msg.append("\n\rGeneral Skills: "+getQualifiedAbilities(mob,Ability.SKILL)+"\n\r");
		msg.append("\n\rThief Skills: "+getQualifiedAbilities(mob,Ability.THIEF_SKILL)+"\n\r");
		msg.append("\n\rSpells: "+getQualifiedAbilities(mob,Ability.SPELL)+"\n\r");
		msg.append("\n\rPrayers: "+getQualifiedAbilities(mob,Ability.PRAYER)+"\n\r");
		msg.append("\n\rSongs: "+getQualifiedAbilities(mob,Ability.SONG)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}

	public void prayers(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rPrayers known: "+getAbilities(mob,Ability.PRAYER)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}

	public void songs(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rSongs known: "+getAbilities(mob,Ability.SONG)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}

	public void spells(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\rYour spells: "+getAbilities(mob,Ability.SPELL)+"\n\r");
		if(!mob.isMonster())
			mob.session().rawPrintln(msg.toString());
	}

	public StringBuffer getAffects(MOB affected)
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


	public StringBuffer getAbilities(MOB able, int ofType)
	{
		int highestLevel=0;
		int lowestLevel=able.envStats().level()+1;
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<able.numAbilities();a++)
		{
			Ability thisAbility=able.fetchAbility(a);
			if((thisAbility.envStats().level()>highestLevel)
			&&(thisAbility.envStats().level()<lowestLevel)
			&&(thisAbility.classificationCode()==ofType))
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
					if((++col)>3)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("["+Util.padRight(Integer.toString(thisAbility.profficiency()),3)+"%] "+Util.padRight(thisAbility.name(),19));
				}
			}
			if(thisLine.length()>0)
				msg.append(thisLine);
		}
		if(msg.length()==0)
			msg.append("None!");
		return msg;
	}


	public StringBuffer getQualifiedAbilities(MOB able, int ofType)
	{
		int highestLevel=0;
		int lowestLevel=able.envStats().level()+1;
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability thisAbility=(Ability)CMClass.abilities.elementAt(a);
			int level=thisAbility.qualifyingLevel(able);
			if((thisAbility.qualifies(able))
			&&(level>highestLevel)
			&&(level<lowestLevel)
			&&(thisAbility.classificationCode()==ofType))
				highestLevel=level;
		}
		int col=0;
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability thisAbility=(Ability)CMClass.abilities.elementAt(a);
				if((thisAbility.qualifies(able))&&(thisAbility.qualifyingLevel(able)==l)&&(thisAbility.classificationCode()==ofType))
				{
					if((++col)>3)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("["+Util.padRight(""+l,3)+"] "+Util.padRight(thisAbility.name(),20));
				}
			}
			if(thisLine.length()>0)
			{
				if(msg.length()==0)
					msg.append("\n\r[Lvl]                     [Lvl]                     [Lvl]\n\r");
				msg.append(thisLine);
			}
		}
		if(msg.length()==0)
			msg.append("None!");
		return msg;
	}


	public StringBuffer getEquipment(MOB seer, MOB mob)
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
		if((msg.length()==0)&&(!Sense.isSleeping(seer)))
			msg.append("nothing!\n\r");
		else
		if(msg.length()==0)
			msg.append("(nothing you can see right now)");

		return msg;
	}
	public void equipment(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().rawPrintln("You are wearing:\n\r"+getEquipment(mob,mob));
	}

	public String shortAlignmentStr(MOB mob)
	{
		int al=mob.getAlignment();
		if(al<350)
			return "evil";
		else
		if(al<650)
			return "neutral";
		else
			return "good";
	}

	public String alignmentStr(MOB mob)
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
	public void commands(MOB mob, CommandSet commandSet)
	{
		if(!mob.isMonster())
			mob.session().rawPrintln("Complete commands list: \n\r"+commandSet.commandList());
	}
	public void socials(MOB mob, Socials socials)
	{
		if(!mob.isMonster())
			mob.session().rawPrintln("Complete socials list: \n\r"+socials.getSocialsList());
	}

	public void areas(MOB mob)
	{
		StringBuffer areasList=(StringBuffer)Resources.getResource("areasList");
		if(areasList==null)
		{

			Hashtable areasHash=new Hashtable();
			Vector areasVec=new Vector();
			for(int m=0;m<CMMap.map.size();m++)
			{
				Room room=(Room)CMMap.map.elementAt(m);
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
			Resources.submitResource("areasList",msg);
			areasList=msg;
		}



		if(!mob.isMonster())
			mob.session().rawPrintln(areasList.toString());
	}
}
