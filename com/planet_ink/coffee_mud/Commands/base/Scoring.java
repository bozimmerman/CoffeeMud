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
			&&(thisItem.container()==null)
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
	
	public void retire(MOB mob)
		throws IOException
	{
		if(mob.isMonster()) return;
		mob.tell("^HThis will delete your player from the system FOREVER!");
		String pwd=mob.session().prompt("If that's what you want, re-enter your password:","");
		if(pwd.length()==0) return;
		if(!pwd.equalsIgnoreCase(mob.password()))
		{
			mob.tell("Password incorrect.");
			return;
		}
		mob.tell("^HThis will delete your player from the system FOREVER!");
		pwd=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
		if(pwd.equalsIgnoreCase("Y"))
		{
			mob.tell("Fine!  Goodbye then!");
			CMMap.MOBs.remove(mob.ID());
			mob.destroy();
			ExternalPlay.DBDeleteMOB(mob);
			for(int m=0;m<CMMap.numRooms();m++)
			{
				Room R=CMMap.getRoom(m);
				if(R!=null)
					R.showOthers(mob,null,Affect.MSG_OK_ACTION,"A horrible death cry can be heard throughout the land.");
			}
			mob.session().setKillFlag(true);
			mob.session().setMob(null);
		}
		else
			mob.tell("Whew.  Close one.");
	}
	
	public void inventory(MOB mob)
	{
		StringBuffer msg=getInventory(mob,mob);
		if(msg.length()==0)
			mob.tell("^HYou are carrying:\n\r^!Nothing!^?\n\r");
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
					if((str.equals(str2))
					&&(Sense.seenTheSameWay(mob,item,item2)))
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
				if(item.envStats().replacementName()!=null)
				{
					if(useName)
						say.append(item.envStats().replacementName());
					else
					if(item.displayText().length()>0)
						say.append(item.envStats().replacementName()+" is here");
					else
						say.append(item.envStats().replacementName());
				}
				else
				{
					if(useName)
						say.append(item.name());
					else
					if(item.displayText().length()>0)
						say.append(item.displayText());
					else
						say.append(item.name());
				}
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
		int adjustedAttack=mob.adjustedAttackBonus();
		int adjustedArmor=(-mob.adjustedArmor())+50;

		StringBuffer msg=new StringBuffer("");

		msg.append("You are ^H"+mob.name()+"^? the level ^!"+mob.envStats().level()+" "+mob.charStats().getMyClass().name()+"^?.\n\r");
		msg.append("You are a ^!"+mob.charStats().genderName()+" "+mob.charStats().getMyRace().name() + "^?");
		if(mob.getLeigeID().length()>0)
			msg.append(" who serves ^H"+mob.getLeigeID()+"^?");
		if(mob.getWorshipCharID().length()>0)
			msg.append(" worshipping ^H"+mob.getWorshipCharID()+"^?");
		msg.append(".\n\r");
		msg.append("\n\rYour stats are: \n\r^!"+mob.charStats().getStats(mob.charStats().getMyClass().maxStat())+"^?\n\r");
		msg.append("You have ^H"+mob.curState().getHitPoints()+"/"+mob.maxState().getHitPoints()+"^? hit points, ^H");
		msg.append(mob.curState().getMana()+"/"+mob.maxState().getMana()+"^? mana, and ^H");
		msg.append(mob.curState().getMovement()+"/"+mob.maxState().getMovement()+"^? movement.\n\r");
		if(mob.envStats().height()<0)
			msg.append("You are incorporeal, but still weigh "+mob.baseEnvStats().weight()+" pounds.\n\r");
		else
			msg.append("You are "+mob.envStats().height()+" inches tall and weigh "+mob.baseEnvStats().weight()+" pounds.\n\r");
		msg.append("You have ^!"+mob.envStats().weight()+"^?/^!"+mob.maxCarry()+"^? pounds of encumbrance.\n\r");
		msg.append("You have ^!"+mob.getPractices()+"^? practices, ^!"+mob.getTrains()+"^? training sessions, and ^H"+mob.getQuestPoint()+"^? quest points.\n\r");
		msg.append("You have scored ^!"+mob.getExperience()+"^? experience points, and have been online for ^!"+Math.round(Util.div(mob.getAgeHours(),60.0))+"^? hours.\n\r");
		msg.append("You need ^!"+(mob.getExpNeededLevel())+"^? experience points to advance to the next level.\n\r");
		msg.append("Your alignment is      : ^H"+CommonStrings.alignmentStr(mob.getAlignment())+" ("+mob.getAlignment()+")^?.\n\r");
		msg.append("Your armored defense is: ^H"+CommonStrings.armorStr(adjustedArmor)+"^?.\n\r");
		msg.append("Your combat prowess is : ^H"+CommonStrings.fightingProwessStr(adjustedAttack)+"^?.\n\r");
		msg.append("Wimpy is set to ^!"+mob.getWimpHitPoint()+"^? hit points.\n\r");

		if(Sense.isFalling(mob))
			msg.append("^!You are falling!!!^?\n\r");
		else
		if(Sense.isSleeping(mob))
			msg.append("^!You are sleeping.^?\n\r");
		else
		if(Sense.isSitting(mob))
			msg.append("^!You are resting.^?\n\r");
		else
		if(Sense.isSwimming(mob))
			msg.append("^!You are swimming.^?\n\r");
		else
		if(Sense.isClimbing(mob))
			msg.append("^!You are climbing.^?\n\r");
		else
		if(Sense.isFlying(mob))
			msg.append("^!You are flying.^?\n\r");
		else
			msg.append("^!You are standing.^?\n\r");
		
		if(mob.riding()!=null)
			msg.append("^!You are "+mob.riding().stateString()+" "+mob.riding().name()+".^?\n\r");

		if(Sense.isInvisible(mob))
			msg.append("^!You are invisible.^?\n\r");
		if(Sense.isHidden(mob))
			msg.append("^!You are hidden.^?\n\r");
		if(Sense.isSneaking(mob))
			msg.append("^!You are sneaking.^?\n\r");

		if(mob.curState().getHunger()<1)
			msg.append("^!You are hungry.^?\n\r");
		if(mob.curState().getThirst()<1)
			msg.append("^!You are thirsty.^?\n\r");
		msg.append("\n\r^!You are affected by:^? "+getAffects(mob)+"\n\r");

		return msg;
	}

	public void affected(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^!You are affected by:^? "+getAffects(mob)+"\n\r");
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(msg.toString());
	}

	public void skills(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		Vector V=new Vector();
		V.addElement(new Integer(Ability.THIEF_SKILL));
		V.addElement(new Integer(Ability.SKILL));
		V.addElement(new Integer(Ability.COMMON_SKILL));
		msg.append("\n\r^HYour skills:^? "+getAbilities(mob,V,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void qualify(MOB mob, Vector commands)
	{
		StringBuffer msg=new StringBuffer("");
		String qual=Util.combine(commands,1);
		if((qual.length()==0)||(qual.equalsIgnoreCase("SKILLS"))||(qual.equalsIgnoreCase("SKILL")))
			msg.append(getQualifiedAbilities(mob,Ability.SKILL,-1,"\n\r^HGeneral Skills:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("COMMON SKILLS"))||(qual.equalsIgnoreCase("COMMON")))
			msg.append(getQualifiedAbilities(mob,Ability.COMMON_SKILL,-1,"\n\r^HCommon Skills:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("THIEVES"))||(qual.equalsIgnoreCase("THIEF"))||(qual.equalsIgnoreCase("THIEF SKILLS")))
			msg.append(getQualifiedAbilities(mob,Ability.THIEF_SKILL,-1,"\n\r^HThief Skills:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("SPELLS"))||(qual.equalsIgnoreCase("SPELL"))||(qual.equalsIgnoreCase("MAGE")))
			msg.append(getQualifiedAbilities(mob,Ability.SPELL,-1,"\n\r^HSpells:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("PRAYERS"))||(qual.equalsIgnoreCase("PRAYER"))||(qual.equalsIgnoreCase("CLERIC")))
			msg.append(getQualifiedAbilities(mob,Ability.PRAYER,-1,"\n\r^HPrayers:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("CHANTS"))||(qual.equalsIgnoreCase("CHANT"))||(qual.equalsIgnoreCase("DRUID")))
			msg.append(getQualifiedAbilities(mob,Ability.CHANT,-1,"\n\r^HDruidic Chants:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("SONGS"))||(qual.equalsIgnoreCase("SONG"))||(qual.equalsIgnoreCase("BARD")))
			msg.append(getQualifiedAbilities(mob,Ability.SONG,-1,"\n\r^HSongs:^? "));
		if((qual.length()==0)||(qual.equalsIgnoreCase("LANGS"))||(qual.equalsIgnoreCase("LANG"))||(qual.equalsIgnoreCase("LANGUAGES")))
			msg.append(getQualifiedAbilities(mob,Ability.LANGUAGE,-1,"\n\r^HLanguages:^? "));
		if(msg.length()==0)
			mob.tell("Valid parameters to the QUALIFY command include SKILLS, THIEF, COMMON, SPELLS, PRAYERS, CHANTS, SONGS, or LANGS.");
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln("^!You now qualify for the following unknown abilities:^?"+msg.toString());
	}

	public void prayers(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HPrayers known:^? "+getAbilities(mob,Ability.PRAYER,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void chants(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HDruidic Chants known:^? "+getAbilities(mob,Ability.CHANT,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void songs(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HSongs known:^? "+getAbilities(mob,Ability.SONG,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void languages(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HLanguages known:^? "+getAbilities(mob,Ability.LANGUAGE,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public void spells(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
		int domain=-1;
		String domainName="";
		if(qual.length()>0)
		for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
			if(Ability.DOMAIN_DESCS[i].startsWith(qual))
			{ domain=i<<5; break;}
			else
			if((Ability.DOMAIN_DESCS[i].indexOf("/")>=0)
			&&(Ability.DOMAIN_DESCS[i].substring(Ability.DOMAIN_DESCS[i].indexOf("/")+1).startsWith(qual)))
			{ domain=i<<5; break;}
		if(domain>0)
			domainName=Ability.DOMAIN_DESCS[domain>>5].toLowerCase();
		StringBuffer spells=new StringBuffer("");
		if((domain<0)&&(qual.length()>0))
		{
			spells.append("\n\rValid schools are: ");
			for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
				spells.append(Ability.DOMAIN_DESCS[i]+" ");
			
		}
		else
			spells.append("\n\r^HYour "+domainName+" spells:^? "+getAbilities(mob,Ability.SPELL,domain));
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(spells.toString()+"\n\r");
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


	public StringBuffer getAbilities(MOB able, int ofType, int ofDomain)
	{
		Vector V=new Vector();
		int mask=Ability.ALL_CODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_CODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.addElement(new Integer(ofType));
		return getAbilities(able,V,mask);
	}
	public StringBuffer getAbilities(MOB able, Vector ofTypes, int mask)
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
			&&(ofTypes.contains(new Integer(thisAbility.classificationCode()&mask))))
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
				&&(ofTypes.contains(new Integer(thisAbility.classificationCode()&mask))))
				{
					if(thisLine.length()==0)
						thisLine.append("\n\rLevel ^!"+l+"^?:\n\r");
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
			msg.append("^!None!^?");
		return msg;
	}


	public StringBuffer getQualifiedAbilities(MOB able, int ofType, int ofDomain, String prefix)
	{
		Vector V=new Vector();
		int mask=Ability.ALL_CODES;
		if(ofDomain>=0)
		{
			mask=Ability.ALL_CODES|Ability.ALL_DOMAINS;
			ofType=ofType|ofDomain;
		}
		V.addElement(new Integer(ofType));
		return getQualifiedAbilities(able,V,mask,prefix);
	}
	
	public StringBuffer getQualifiedAbilities(MOB able, Vector ofTypes, int mask, String prefix)
	{
		int highestLevel=0;
		int lowestLevel=able.envStats().level()+1;
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability thisAbility=(Ability)CMClass.abilities.elementAt(a);
			int level=thisAbility.qualifyingLevel(able);
			if((thisAbility.qualifiesByLevel(able))
			&&(level>highestLevel)
			&&(level<lowestLevel)
			&&(able.fetchAbility(thisAbility.ID())==null)
			&&(ofTypes.contains(new Integer(thisAbility.classificationCode()&mask))))
				highestLevel=level;
		}
		int col=0;
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			for(int a=0;a<CMClass.abilities.size();a++)
			{
				Ability thisAbility=(Ability)CMClass.abilities.elementAt(a);
				if((thisAbility.qualifiesByLevel(able))
				   &&(thisAbility.qualifyingLevel(able)==l)
				   &&(able.fetchAbility(thisAbility.ID())==null)
				   &&(ofTypes.contains(new Integer(thisAbility.classificationCode()&mask))))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(""+l,3)+"^?] "
					+Util.padRight(thisAbility.name(),19)+" "
					+Util.padRight(thisAbility.requirements(),(col==2)?12:13));
				}
			}
			if(thisLine.length()>0)
			{
				if(msg.length()==0)
					msg.append("\n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r");
				msg.append(thisLine);
			}
		}
		if(msg.length()==0)
			return msg;
		msg.insert(0,prefix);
		msg.append("\n\r");
		return msg;
	}


	public StringBuffer getEquipment(MOB seer, MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		boolean foundButUnseen=false;
		if(Sense.isSleeping(seer))
			return new StringBuffer("(nothing you can see right now)");
		
		for(int l=0;l<Item.wornOrder.length;l++)
		{
			long wornCode=Item.wornOrder[l];
			String header="^N(^H"+Sense.wornLocation(wornCode)+"^?)";
			header+=Util.SPACES.substring(0,26-header.length())+": ^!";
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item thisItem=mob.fetchInventory(i);
				if((thisItem.container()==null)&&(thisItem.amWearingAt(wornCode)))
				{
					if(Sense.canBeSeenBy(thisItem,seer))
					{
						String name=thisItem.name();
						if(name.length()>53) name=name.substring(0,50)+"...";
						msg.append(header+name+Sense.colorCodes(thisItem,seer)+"^?\n\r");
					}
					else
						msg.append(header+"(something you can`t see)"+Sense.colorCodes(thisItem,seer)+"^?\n\r");
				}
			}
		}
		if(msg.length()==0)
			msg.append("^!(nothing)^?\n\r");
		return msg;
	}
	public void equipment(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().unfilteredPrintln("You are wearing:\n\r"+getEquipment(mob,mob));
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
			for(int a=0;a<CMMap.numAreas();a++)
				areasVec.addElement((CMMap.getArea(a)).name());
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
