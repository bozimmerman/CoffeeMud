package com.planet_ink.coffee_mud.Commands.base;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import com.planet_ink.coffee_mud.Commands.*;
import java.io.*;
import java.util.*;
public class Scoring
{
	private Scoring(){}

	public static StringBuffer getInventory(MOB seer, MOB mob)
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

	public static void destroyUser(MOB deadMOB)
	{
		if(CMMap.getPlayer(deadMOB.ID())!=null)
		{
		   deadMOB=(MOB)CMMap.getPlayer(deadMOB.ID());
		   CMMap.delPlayer(deadMOB);
		}
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=(Session)Sessions.elementAt(s);
			if((!S.killFlag())&&(S.mob()!=null)&&(S.mob().ID().equals(deadMOB.ID())))
			   deadMOB=S.mob();
		}
		FullMsg msg=new FullMsg(deadMOB,null,Affect.MSG_RETIRE,"A horrible death cry is heard throughout the land.");
		if(deadMOB.location()!=null)
			deadMOB.location().send(deadMOB,msg);
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			if((R!=null)&&(R!=deadMOB.location()))
			{
				if(R.okAffect(deadMOB,msg))
					R.sendOthers(deadMOB,msg);
				else
				{
					CMMap.addPlayer(deadMOB);
					return;
				}
			}
		}
		deadMOB.destroy();
		ExternalPlay.DBDeleteMOB(deadMOB);
		if(deadMOB.session()!=null)
		{
			deadMOB.session().setKillFlag(true);
			deadMOB.session().setMob(null);
		}
		Log.sysOut("Scoring",deadMOB.name()+" has retired!");
	}

	public static void retire(MOB mob)
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
			destroyUser(mob);
		}
		else
			mob.tell("Whew.  Close one.");
	}

	public static void inventory(MOB mob)
	{
		StringBuffer msg=getInventory(mob,mob);
		if(msg.length()==0)
			mob.tell("^HYou are carrying:\n\r^!Nothing!^?\n\r");
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln("^HYou are carrying:^?\n\r"+msg.toString());
	}

	public static StringBuffer niceLister(MOB mob, Vector items, boolean useName)
	{
		StringBuffer say=new StringBuffer("");
		while(items.size()>0)
		{
			Item item=(Item)items.elementAt(0);
			String str=(useName||(item.displayText().length()==0))?item.name():item.displayText();
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
					String str2=(useName||(item2.displayText().length()==0))?item2.name():item2.displayText();
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
			&&(((item.displayText().length()>0)
				||useName
				||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))))
			{
				if(reps==0)	say.append("      ");
				else
				if(reps>=99)
					say.append("("+Util.padLeftPreserve(""+(reps+1),3)+") ");
				else
				if(reps>0)
					say.append(" ("+Util.padLeftPreserve(""+(reps+1),2)+") ");
				if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
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

	public static void score(MOB mob)
	{
		StringBuffer msg=getScore(mob);
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}
	public static StringBuffer getScore(MOB mob)
	{
		int adjustedAttack=mob.adjustedAttackBonus();
		int adjustedArmor=(-mob.adjustedArmor())+50;

		StringBuffer msg=new StringBuffer("");

		String levelStr=null;
		int classLevel=mob.charStats().getClassLevel(mob.charStats().getCurrentClass());
		if(classLevel>=mob.envStats().level())
			levelStr="level "+mob.envStats().level()+" "+mob.charStats().getCurrentClass().name();
		else
			levelStr=mob.charStats().getCurrentClass().name()+" "+classLevel+"/"+mob.envStats().level();
		msg.append("You are ^H"+mob.name()+"^? the ^H"+levelStr+"^?.\n\r");
		if(classLevel<mob.envStats().level())
		{
			msg.append("You also have levels in: ");
			StringBuffer classList=new StringBuffer("");
			for(int c=0;c<mob.charStats().numClasses()-1;c++)
			{
				CharClass C=mob.charStats().getMyClass(c);
				if(C!=mob.charStats().getCurrentClass())
				{
					if(classList.length()>0)
						if(c==mob.charStats().numClasses()-2)
							classList.append(", and ");
						else
							classList.append(", ");
					classList.append(C.name()+" ("+mob.charStats().getClassLevel(C)+") ");
				}
			}
			msg.append(classList.toString()+".\n\r");
		}
		msg.append("You are a ^!"+mob.charStats().genderName()+" "+mob.charStats().getMyRace().name() + "^?");
		if(mob.getLeigeID().length()>0)
			msg.append(" who serves ^H"+mob.getLeigeID()+"^?");
		if(mob.getWorshipCharID().length()>0)
			msg.append(" worshipping ^H"+mob.getWorshipCharID()+"^?");
		msg.append(".\n\r");
		msg.append("\n\rYour stats are: \n\r^!"+mob.charStats().getStats(mob.charStats().getCurrentClass().maxStat())+"^?\n\r");
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
			msg.append("^!You are "+mob.riding().stateString(mob)+" "+mob.riding().name()+".^?\n\r");

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

	public static void affected(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^!You are affected by:^? "+getAffects(mob)+"\n\r");
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(msg.toString());
	}

	public static void gods(MOB mob)
	{
		StringBuffer msg=new StringBuffer("\n\r^HThe known deities:^? \n\r");
		for(Enumeration d=CMMap.deities();d.hasMoreElements();)
		{
			Deity D=(Deity)d.nextElement();
			msg.append("\n\r^x"+D.name()+"^.^?\n\r");
			msg.append(D.description()+"\n\r");
			msg.append(D.getWorshipRequirementsDesc()+"\n\r");
			msg.append(D.getClericRequirementsDesc()+"\n\r");
			if(D.numBlessings()>0)
			{
				msg.append("Blessings: ");
				for(int b=0;b<D.numBlessings();b++)
					msg.append(D.fetchBlessing(b).name()+" ");
				msg.append("\n\r");
				msg.append(D.getWorshipTriggerDesc()+"\n\r");
				msg.append(D.getClericTriggerDesc()+"\n\r");
			}
		}
		mob.tell(msg.toString());
	}

	public static void skills(MOB mob)
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

	public static void qualify(MOB mob, Vector commands)
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
		int domain=-1;
		String domainName="";
		if(qual.length()>0)
		{
			for(int i=1;i<Ability.DOMAIN_DESCS.length;i++)
				if(Ability.DOMAIN_DESCS[i].startsWith(qual.toUpperCase()))
				{ domain=i<<5; break;}
				else
				if((Ability.DOMAIN_DESCS[i].indexOf("/")>=0)
				&&(Ability.DOMAIN_DESCS[i].substring(Ability.DOMAIN_DESCS[i].indexOf("/")+1).startsWith(qual.toUpperCase())))
				{ domain=i<<5; break;}
			if(domain>0)
			{
				domainName=Util.capitalize(Ability.DOMAIN_DESCS[domain>>5]);
				msg.append(getQualifiedAbilities(mob,Ability.SPELL,domain,"\n\r^H"+domainName+" spells:^? "));
			}
		}
		if((!CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
		&&(mob!=null)
		&&((qual.length()==0)
			||(qual.equalsIgnoreCase("CLASS"))
			||(qual.equalsIgnoreCase("CLASSES"))))
		{
			int col=0;
			StringBuffer msg2=new StringBuffer("");
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				StringBuffer thisLine=new StringBuffer("");
				if(C.playerSelectable()
				&&(mob.charStats().getCurrentClass()!=C)
				&&(C.qualifiesForThisClass(mob,true)))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(""+1,3)+"^?] "
					+Util.padRight(C.name(),19)+" "
					+Util.padRight("1 train",(col==2)?12:13));
				}
				if(thisLine.length()>0)
				{
					if(msg2.length()==0)
						msg2.append("\n\r^HClasses:^? \n\r^N[^HLvl^?] Name                Requires     [^HLvl^?] Name                Requires\n\r");
					msg2.append(thisLine);
				}
			}
			msg.append(msg2.toString());
		}

		if(msg.length()==0)
			mob.tell("Valid parameters to the QUALIFY command include SKILLS, THIEF, COMMON, SPELLS, PRAYERS, CHANTS, SONGS, or LANGS.");
		else
		if(!mob.isMonster())
			mob.session().unfilteredPrintln("^!You now qualify for the following unknown abilities:^?"+msg.toString());
	}

	public static void prayers(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HPrayers known:^? "+getAbilities(mob,Ability.PRAYER,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public static void chants(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HDruidic Chants known:^? "+getAbilities(mob,Ability.CHANT,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public static void songs(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HSongs known:^? "+getAbilities(mob,Ability.SONG,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public static void languages(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("\n\r^HLanguages known:^? "+getAbilities(mob,Ability.LANGUAGE,-1)+"\n\r");
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(msg.toString());
	}

	public static void spells(MOB mob, Vector commands)
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


	public static StringBuffer getAffects(MOB affected)
	{
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<affected.numAffects();a++)
		{
			Ability thisAffect=affected.fetchAffect(a);
			if((thisAffect!=null)&&(thisAffect.displayText().length()>0))
				msg.append("\n\r^S"+thisAffect.displayText());
		}
		if(msg.length()==0)
			msg.append("Nothing!");
		else
			msg.append("^?");
		return msg;
	}


	public static StringBuffer getAbilities(MOB able, int ofType, int ofDomain)
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
	public static StringBuffer getAbilities(MOB able, Vector ofTypes, int mask)
	{
		int highestLevel=0;
		int lowestLevel=able.envStats().level()+1;
		StringBuffer msg=new StringBuffer("");
		for(int a=0;a<able.numAbilities();a++)
		{
			Ability thisAbility=able.fetchAbility(a);
			int level=CMAble.qualifyingLevel(able,thisAbility);
			if(level<0) level=0;
			if((thisAbility!=null)
			&&(level>highestLevel)
			&&(level<lowestLevel)
			&&(ofTypes.contains(new Integer(thisAbility.classificationCode()&mask))))
				highestLevel=level;
		}
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			int col=0;
			for(int a=0;a<able.numAbilities();a++)
			{
				Ability thisAbility=able.fetchAbility(a);
				int level=CMAble.qualifyingLevel(able,thisAbility);
				if(level<0) level=0;
				if((thisAbility!=null)
				&&(level==l)
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
		else
			msg.append("\n\r\n\rUse QUALIFY to see additional skills you can GAIN.");
		return msg;
	}


	public static StringBuffer getQualifiedAbilities(MOB able, int ofType, int ofDomain, String prefix)
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

	public static StringBuffer getQualifiedAbilities(MOB able,
													 Vector ofTypes,
													 int mask,
													 String prefix)
	{
		int highestLevel=0;
		StringBuffer msg=new StringBuffer("");
		for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
		{
			Ability A=(Ability)a.nextElement();
			int level=CMAble.qualifyingLevel(able,A);
			if((CMAble.qualifiesByLevel(able,A))
			&&(level>highestLevel)
			&&(level<(CMAble.qualifyingClassLevel(able,A)+1))
			&&(able.fetchAbility(A.ID())==null)
			&&(ofTypes.contains(new Integer(A.classificationCode()&mask))))
				highestLevel=level;
		}
		int col=0;
		for(int l=0;l<=highestLevel;l++)
		{
			StringBuffer thisLine=new StringBuffer("");
			for(Enumeration a=CMClass.abilities();a.hasMoreElements();)
			{
				Ability A=(Ability)a.nextElement();
				if((CMAble.qualifiesByLevel(able,A))
				   &&(CMAble.qualifyingLevel(able,A)==l)
				   &&(able.fetchAbility(A.ID())==null)
				   &&(ofTypes.contains(new Integer(A.classificationCode()&mask))))
				{
					if((++col)>2)
					{
						thisLine.append("\n\r");
						col=1;
					}
					thisLine.append("^N[^H"+Util.padRight(""+l,3)+"^?] "
					+Util.padRight(A.name(),19)+" "
					+Util.padRight(A.requirements(),(col==2)?12:13));
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


	public static StringBuffer getEquipment(MOB seer, MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
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
	public static void equipment(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().unfilteredPrintln("You are wearing:\n\r"+getEquipment(mob,mob));
	}
	public static void commands(MOB mob, CommandSet commandSet)
	{
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln("^HComplete commands list:^?\n\r"+commandSet.commandList());
	}
	public static void socials(MOB mob)
	{
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln("^HComplete socials list:^?\n\r"+Socials.getSocialsList());
	}

	public static void prompt(MOB mob, Vector commands)
	{
		if(mob.session()==null) return;
		if(commands.size()==1)
			mob.session().rawPrintln("Your prompt is currently set at:\n\r"+mob.getPrompt());
		else
		{
			mob.setPrompt(Util.combine(commands,1));
			mob.session().rawPrintln("Your prompt is currently now set at:\n\r"+mob.getPrompt());
		}
	}

	public static void colorSet(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.session()==null) return;
		String[] clookup=(String[])mob.session().clookup().clone();
		if((commands.size()>1)
		   &&("DEFAULT".startsWith(Util.combine(commands,1).toUpperCase())))
		{
			mob.setColorStr("");
			mob.tell("Your colors have been changed back to default.");
			return;
		}
		if(clookup==null) return;
		String[][] theSet={{"Normal Text","N"},
						   {"Highlighted Text","H"},
						   {"Fight Text","F"},
						   {"Spells","S"},
						   {"Emotes","F"},
						   {"Talks","T"},
						   {"Room Titles","O"},
						   {"Room Descriptions","L"},
						   {"Doors","d"},
						   {"Items","I"},
						   {"MOBs","M"}};
		String[][] theColors={{"White","w"},
							  {"Green","g"},
							  {"Blue","b"},
							  {"Red","r"},
							  {"Yellow","y"},
							  {"Cyan","c"},
							  {"Purple","p"},
							  {"Grey","W"},
							  {"Dark Green","G"},
							  {"Dark Blue","B"},
							  {"Dark Red","R"},
							  {"Dark Yellow","Y"},
							  {"Dark Cyan","C"},
							  {"Dark Purple","P"}};
		String numToChange="!";
		while(numToChange.length()>0)
		{
			StringBuffer buf=new StringBuffer("");
			for(int i=0;i<theSet.length;i++)
			{
				buf.append("\n\r^H"+Util.padLeft(""+(i+1),2)+"^N) "+Util.padRight(theSet[i][0],20)+": ");
				String what=clookup[(int)theSet[i][1].charAt(0)];
				if(what!=null)
				for(int ii=0;ii<theColors.length;ii++)
					if(what.equals(clookup[(int)theColors[ii][1].charAt(0)]))
						buf.append("^"+theColors[ii][1]+theColors[ii][0]);
				buf.append("^N");
			}
			mob.session().println(buf.toString());
			numToChange=mob.session().prompt("Enter Number or RETURN: ","");
			int num=Util.s_int(numToChange);
			if(numToChange.length()==0) break;
			if((num<=0)||(num>=theSet.length))
				mob.tell("That is not a valid entry!");
			else
			{
				num--;
				buf=new StringBuffer("");
				buf.append("\n\r^c"+Util.padLeft(""+(num+1),2)+"^N)"+Util.padRight(theSet[num][0],20)+":");
				String what=clookup[(int)theSet[num][1].charAt(0)];
				if(what!=null)
				for(int ii=0;ii<theColors.length;ii++)
					if(what.equals(clookup[(int)theColors[ii][1].charAt(0)]))
						buf.append("^"+theColors[ii][1]+theColors[ii][0]);
				buf.append("^N\n\rAvailable Colors:");
				for(int ii=0;ii<theColors.length;ii++)
					buf.append("\n\r^"+theColors[ii][1]+theColors[ii][0]);
				mob.session().println(buf.toString()+"^N");
				String newColor=mob.session().prompt("Enter Name of New Color: ","");
				if(newColor.length()>0)
				{
					int colorNum=-1;
					for(int ii=0;ii<theColors.length;ii++)
						if(theColors[ii][0].toUpperCase().startsWith(newColor.toUpperCase()))
						{
							colorNum=ii; break;
						}
					if(colorNum<0)
						mob.tell("That is not a valid color!");
					else
					{
						clookup[(int)theSet[num][1].charAt(0)]=clookup[(int)theColors[colorNum][1].charAt(0)];
						String newChanges="";
						String[] common=CommonStrings.standardColorLookups();
						for(int i=0;i<theSet.length;i++)
						{
							char c=theSet[i][1].charAt(0);
							if(!clookup[(int)c].equals(common[(int)c]))
								for(int ii=0;ii<theColors.length;ii++)
									if(common[(int)theColors[ii][1].charAt(0)].equals(clookup[(int)c]))
									{
										newChanges+=c+"^"+theColors[ii][1]+"#";
										break;
									}
						}
						mob.setColorStr(newChanges);
						clookup=(String[])mob.session().clookup().clone();
					}
				}
			}
		}
	}

	public static void config(MOB mob, Vector commands)
	{
		StringBuffer msg=new StringBuffer("^HYour configuration flags:^?\n\r");
		for(int i=0;i<MOB.AUTODESC.length;i++)
		{
			msg.append(Util.padRight(MOB.AUTODESC[i],15)+": ");
			boolean set=Util.isSet(mob.getBitmap(),i);
			if(MOB.AUTOREV[i]) set=!set;
			msg.append(set?"ON":"OFF");
			msg.append("\n\r");
		}
		mob.tell(msg.toString());
	}
	
	public static void areas(MOB mob)
	{
		Vector areasVec=new Vector();
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if((!Sense.isHidden(A))||(mob.isASysOp(null)))
				areasVec.addElement(A.name());
		}
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
		if(!mob.isMonster())
			mob.session().colorOnlyPrintln(msg.toString());
	}
}
