package com.planet_ink.coffee_mud.Commands.base;

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
		boolean foundAndSeen=false;
		Vector viewItems=new Vector();
		for(int i=0;i<mob.inventorySize();i++)
		{
			Item thisItem=mob.fetchInventory(i);
			if((thisItem!=null)
			&&(thisItem.location()==null)
			&&(thisItem.amWearingAt(Item.INVENTORY)))
			{
				viewItems.addElement(thisItem);
				if(Sense.canBeSeenBy(thisItem,seer))
					foundAndSeen=true;
			}
		}
		if((viewItems.size()>0)&&(!foundAndSeen))
			msg.append("(nothing you can see right now)");
		else
		{
			msg.append(niceLister(seer,viewItems,true));
			if((mob.getMoney()>0)&&(!Sense.canBeSeenBy(mob.location(),seer)))
				msg.append("(some ^ygold^? coins you can't see)");
			else
			if(mob.getMoney()>0)
				msg.append(mob.getMoney()+" ^ygold^? coins.\n\r");
		}
		return msg;
	}
	public void inventory(MOB mob)
	{
		StringBuffer msg=getInventory(mob,mob);
		if(msg.length()==0)
			mob.tell("^HYou are carrying:\n\r^BNothing!^?\n\r");
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln("^HYou are carrying:^?\n\r"+msg.toString());
	}

	public StringBuffer niceLister(MOB mob, Vector items, boolean useName)
	{
		StringBuffer say=new StringBuffer("");
		while(items.size()>0)
		{
			Item item=(Item)items.elementAt(0);
			String str=(useName)?item.name():item.displayText();
			int reps=0;
			items.removeElement(item);
			int here=0;
			while(here<items.size())
			{
				Item item2=(Item)items.elementAt(here);
				if(item2==null)
					break;
				else
				{
					String str2=(useName)?item2.name():item2.displayText();
					if(str2.length()==0)
						items.removeElement(item2);
					else
					if(str.equals(str2))
					{
						reps++;
						items.removeElement(item2);
					}
					else
						here++;
				}
			}
			if((Sense.canBeSeenBy(item,mob))
			&&(((item.displayText().length()>0)||useName||((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0))))
			{
				if(reps==0)	say.append("      ");
				else
				if(reps>0)	say.append(" ("+Util.padLeft(""+(reps+1),2)+") ");
				if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
					say.append("^H("+CMClass.className(item)+")^N ");
				say.append("^I");
				if(useName)
					say.append(item.name());
				else
				if(item.displayText().length()>0)
					say.append(item.displayText());
				else
					say.append(item.name());
				say.append(" "+Sense.colorCodes(item,mob)+"^N\n\r");
			}
		}
		return say;
	}
	
	public void score(MOB mob)
	{
		StringBuffer msg=getScore(mob);
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}
	public StringBuffer getScore(MOB mob)
	{
		TheFight theFight=new TheFight();

		int adjustedArmor=100-(int)theFight.adjustedArmor(mob);
		int adjustedAttack=(int)theFight.adjustedAttackBonus(mob);


		StringBuffer msg=new StringBuffer("");

		msg.append("You are ^H"+mob.name()+"^? the level ^B"+mob.envStats().level()+" "+mob.charStats().getMyClass().name()+"^?.\n\r");
		msg.append("You are a ^B"+((mob.charStats().getStat(CharStats.GENDER)=='M')?"male":"female")+" "+mob.charStats().getMyRace().name() + "^?");
		if(mob.getWorshipCharID().length()>0)
			msg.append(" who serves ^H"+mob.getWorshipCharID()+"^?");
		msg.append(".\n\r");
		msg.append("\n\rYour stats are: \n\r^B"+mob.charStats().getStats(mob.charStats().getMyClass().maxStat())+"^?\n\r");
		msg.append("You have ^H"+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+"^? hit points, ^H");
		msg.append(mob.curState().getMana()+"/"+mob.maxState().getMana()+"^? mana, and ^H");
		msg.append(mob.curState().getMovement()+"/"+mob.maxState().getMovement()+"^? movement.\n\r");
		msg.append("You have ^B"+mob.envStats().weight()+"^?/^B"+mob.maxCarry()+"^? pounds of encumbrance.\n\r");
		msg.append("You have ^B"+mob.getPractices()+"^? practices, ^B"+mob.getTrains()+"^? training sessions, and ^H"+mob.getQuestPoint()+"^? quest points.\n\r");
		msg.append("You have scored ^B"+mob.getExperience()+"^? experience points, and have been online for ^B"+Math.round(Util.div(mob.getAgeHours(),60.0))+"^? hours.\n\r");
		msg.append("You need ^B"+(mob.getExpNeededLevel())+"^? experience points to advance to the next level.\n\r");
		msg.append("Your alignment is      : ^H"+alignmentStr(mob.getAlignment())+" ("+mob.getAlignment()+")^?.\n\r");
		msg.append("Your armored defense is: ^H"+theFight.armorStr(adjustedArmor)+"^?.\n\r");
		msg.append("Your combat prowess is : ^H"+theFight.fightingProwessStr(adjustedAttack)+"^?.\n\r");
		msg.append("Wimpy is set to ^B"+mob.getWimpHitPoint()+"^? hit points.\n\r");

		if(Sense.isFlying(mob))
			msg.append("^BYou are falling!!!^?\n\r");
		else
		if(Sense.isSleeping(mob))
			msg.append("^BYou are sleeping.^?\n\r");
		else
		if(Sense.isSitting(mob))
			msg.append("^BYou are resting.^?\n\r");
		else
		if(Sense.isSwimming(mob))
			msg.append("^BYou are swimming.^?\n\r");
		else
		if(Sense.isClimbing(mob))
			msg.append("^BYou are climbing.^?\n\r");
		else
		if(Sense.isFlying(mob))
			msg.append("^BYou are flying.^?\n\r");
		else
			msg.append("^BYou are standing.^?\n\r");
		
		if(mob.riding()!=null)
			msg.append("^BYou are riding "+mob.riding().name()+".^?\n\r");

		if(Sense.isInvisible(mob))
			msg.append("^BYou are invisible.^?\n\r");
		if(Sense.isHidden(mob))
			msg.append("^BYou are hidden.^?\n\r");
		if(Sense.isSneaking(mob))
			msg.append("^BYou are sneaking.^?\n\r");

		if(mob.curState().getHunger()<1)
			msg.append("^BYou are hungry.^?\n\r");
		if(mob.curState().getThirst()<1)
			msg.append("^BYou are thirsty.^?\n\r");
		msg.append("\n\r^BYou are affected by:^? "+getAffects(mob)+"\n\r");

		return msg;
	}

	public void affected(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^BYou are affected by:^? "+getAffects(mob)+"\n\r");
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(msg.toString());
	}

	public void skills(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		if(getAbilities(mob,Ability.THIEF_SKILL).length()<10)
			msg.append("\n\r^HYour skills:^? "+getAbilities(mob,Ability.SKILL)+"\n\r");
		else
		{
			msg.append("\n\r^HGeneral skills:^? "+getAbilities(mob,Ability.SKILL)+"\n\r");
			msg.append("\n\r^HThief skills:^? "+getAbilities(mob,Ability.THIEF_SKILL)+"\n\r");
		}

		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void qualify(MOB mob)
	{
		StringBuffer msg=new StringBuffer("^BYou now qualify for the following:^?");
		msg.append("\n\r^HGeneral Skills:^? "+getQualifiedAbilities(mob,Ability.SKILL)+"\n\r");
		msg.append("\n\r^HThief Skills:^? "+getQualifiedAbilities(mob,Ability.THIEF_SKILL)+"\n\r");
		msg.append("\n\r^HSpells:^? "+getQualifiedAbilities(mob,Ability.SPELL)+"\n\r");
		msg.append("\n\r^HPrayers:^? "+getQualifiedAbilities(mob,Ability.PRAYER)+"\n\r");
		msg.append("\n\r^HSongs:^? "+getQualifiedAbilities(mob,Ability.SONG)+"\n\r");
		msg.append("\n\r^HLanguages:^? "+getQualifiedAbilities(mob,Ability.LANGUAGE)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void prayers(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HPrayers known:^? "+getAbilities(mob,Ability.PRAYER)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void songs(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HSongs known:^? "+getAbilities(mob,Ability.SONG)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void languages(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HLanguages known:^? "+getAbilities(mob,Ability.LANGUAGE)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void spells(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HYour spells:^? "+getAbilities(mob,Ability.SPELL)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}


	public StringBuffer getAffects(MOB affected)
	{
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<affected.numAffects();a++)
		{
			Ability thisAffect=affected.fetchAffect(a);
			if((thisAffect!=null)&&(thisAffect.displayText().length()>0))
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
			if((thisAbility!=null)
			&&(thisAbility.envStats().level()>highestLevel)
			&&(thisAbility.envStats().level()<lowestLevel)
			&&((thisAbility.classificationCode()&Ability.ALL_CODES)==ofType))
				highestLevel=thisAbility.envStats().level();
		}
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			int col=0;
			for(int a=0;a<able.numAbilities();a++)
			{
				Ability thisAbility=able.fetchAbility(a);
				if((thisAbility!=null)
				&&(thisAbility.envStats().level()==l)
				&&((thisAbility.classificationCode()&Ability.ALL_CODES)==ofType))
				{
					if(thisLine.length()==0)
						thisLine.append("\n\rLevel ^B"+l+"^?:\n\r");
					if((++col)>3)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(Integer.toString(thisAbility.profficiency()),3)+"%^?] ^N"+Util.padRight(thisAbility.name(),(col==3)?18:19));
				}
			}
			if(thisLine.length()>0)
				msg.append(thisLine);
		}
		if(msg.length()==0)
			msg.append("^BNone!^?");
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
			&&((thisAbility.classificationCode()&Ability.ALL_CODES)==ofType))
				highestLevel=level;
		}
		int col=0;
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability thisAbility=(Ability)CMClass.abilities.elementAt(a);
				if((thisAbility.qualifies(able))&&(thisAbility.qualifyingLevel(able)==l)&&((thisAbility.classificationCode()&Ability.ALL_CODES)==ofType))
				{
					if((++col)>3)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(""+l,3)+"^?] "+Util.padRight(thisAbility.name(),(col==3)?19:20));
				}
			}
			if(thisLine.length()>0)
			{
				if(msg.length()==0)
					msg.append("\n\r^N[^HLvl^?]                     [^HLvl^?]                     [^HLvl^?]\n\r");
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
		boolean foundButUnseen=false;
		for(int l=0;l<16;l++)
		{
			int wornCode=new Double(Math.pow(new Integer(2).doubleValue(),new Integer(l).doubleValue())).intValue();
			String header="^N(^H"+Sense.wornLocation(wornCode)+"^?)";
			header+=Util.SPACES.substring(0,26-header.length())+": ^B";
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item thisItem=mob.fetchInventory(i);
				if((thisItem.location()==null)&&(thisItem.amWearingAt(wornCode)))
				{
					if(Sense.canBeSeenBy(thisItem,seer))
					{
						String name=thisItem.name();
						if(name.length()>53) name=name.substring(0,50)+"...";
						msg.append(header+name+Sense.colorCodes(thisItem,seer)+"^?\n\r");
					}
					else
						foundButUnseen=true;
				}
			}
		}
		if(foundButUnseen)
			msg.append("(nothing you can see right now)");
		else
		if(msg.length()==0)
			msg.append("^B(nothing)^?\n\r");

		return msg;
	}
	public void equipment(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().unfilteredPrintln("You are wearing:\n\r"+getEquipment(mob,mob));
	}

	public String shortAlignmentStr(int al)
	{
		if(al<350)
			return "evil";
		else
		if(al<650)
			return "neutral";
		else
			return "good";
	}

	public String alignmentStr(int al)
	{
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
			mob.session().colorOnlyPrintln("^HComplete commands list:^?\n\r"+commandSet.commandList());
	}
	public void socials(MOB mob, Socials socials)
	{
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln("^HComplete socials list:^?\n\r"+socials.getSocialsList());
	}

	public void areas(MOB mob)
	{
		StringBuffer areasList=(StringBuffer)Resources.getResource("areasList");
		if(areasList==null)
		{

			Vector areasVec=new Vector();
			for(int a=0;a<CMMap.AREAS.size();a++)
				areasVec.addElement(((Area)CMMap.AREAS.elementAt(a)).name());
			Collections.sort((List)areasVec);
			StringBuffer msg=new StringBuffer("^HComplete areas list:^?\n\r");
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
			msg.append("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?");
			Resources.submitResource("areasList",msg);
			areasList=msg;
		}



		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(areasList.toString());
	}
}
