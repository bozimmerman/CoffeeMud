package com.planet_ink.coffee_mud.commands;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Abilities.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.sysop.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class CommandProcessor
{
	public static CommandSet commandSet=new CommandSet();
	public static Properties helpFile=null;

	
	public static void doCommand(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()==0) return;
		if(mob.location()==null) return;
		
		Integer commandCodeObj=(Integer)commandSet.get(((String)commands.elementAt(0)).toUpperCase());
		if(commandCodeObj!=null)
		{
			int commandCode=commandCodeObj.intValue();
			if(commandCode>=0)
			{
				switch(commandCode)
				{
					case CommandSet.AFFECT:
						Scoring.affected(mob);
						break;
					case CommandSet.AREAS:
						Scoring.areas(mob);
						break;
					case CommandSet.PUSH:
						ItemUsage.push(mob,combine(commands,1));
						break;
					case CommandSet.PULL:
						ItemUsage.pull(mob,combine(commands,1));
						break;
					case CommandSet.BUG:
						Log.errOut(mob.name(),combine(commands,1));
						mob.tell("Thank you for your assistance in debugging CoffeeMud!");
						break;
					case CommandSet.BUY:
						SocialProcessor.buy(mob,commands);
						break;
					case CommandSet.CLOSE:
						Movement.close(mob,combine(commands,1));
						break;
					case CommandSet.CHANNEL:
						Channels.channel(mob,commands);
						break;
					case CommandSet.CONSIDER:
						SocialProcessor.consider(mob,commands);
						break;
					case CommandSet.COMPARE:
						ItemUsage.compare(mob,commands);
						break;
					case CommandSet.COMMANDS:
						Scoring.commands(mob);
						break;
					case CommandSet.CREATE:
						if(mob.isASysOp())
							CreateEdit.Create(mob,commands);
						else
							mob.tell("Huh?\n\r");
						break;
					case CommandSet.CREDITS:
						credits(mob);
						break;
					case CommandSet.DESCRIPTION:
						BasicSenses.description(mob,commands);
						break;
					case CommandSet.DESTROY:
						if(mob.isASysOp())
							CreateEdit.Destroy(mob,commands);
						else
							mob.tell("Huh?\n\r");
						break;
					case CommandSet.DOWN:
						Movement.move(mob,Directions.DOWN,false);
						break;
					case CommandSet.DRINK:
						ItemUsage.drink(mob,commands);
						break;
					case CommandSet.DROP:
						ItemUsage.drop(mob,commands);
						break;
					case CommandSet.EAST:
						Movement.move(mob,Directions.EAST,false);
						break;
					case CommandSet.EAT:
						ItemUsage.eat(mob,commands);
						break;
					case CommandSet.EMOTE:
						BasicSenses.emote(mob,commands);
						break;
					case CommandSet.EVOKE: // an ability
						AbilityEvoker.evoke(mob,commands);
						break;
					case CommandSet.EQUIPMENT:
						Scoring.equipment(mob);
						break;
					case CommandSet.EXAMINE:
						BasicSenses.look(mob,commands,false);
						break;
					case CommandSet.EXITS:
						mob.location().listExits(mob);
						break;
					case CommandSet.FILL:
						ItemUsage.fill(mob,commands);
						break;
					case CommandSet.FLEE:
						Movement.flee(mob,combine(commands,1));
						break;
					case CommandSet.FOLLOW:
						Grouping.follow(mob,commands);
						break;
					case CommandSet.GET:
						ItemUsage.get(mob,commands);
						break;
					case CommandSet.GIVE:
						SocialProcessor.give(mob,commands,false);
						break;
					case CommandSet.GO:
						Movement.go(mob,commands);
						break;
					case CommandSet.GROUP:
						Grouping.group(mob);
						break;
					case CommandSet.GTELL:
						Grouping.gtell(mob,combine(commands,1));
						break;
					case CommandSet.HELP:
						help(mob,combine(commands,1));
						break;
					case CommandSet.HOLD:
						ItemUsage.hold(mob,commands);
						break;
					case CommandSet.INVENTORY:
						Scoring.inventory(mob);
						break;
					case CommandSet.KILL:
						TheFight.kill(mob,commands);
						break;
					case CommandSet.LIST:
						SocialProcessor.list(mob,commands);
						break;
					case CommandSet.LOCK:
						Movement.lock(mob,combine(commands,1));
						break;
					case CommandSet.LOOK:
						BasicSenses.look(mob,commands,false);
						break;
					case CommandSet.MODIFY:
						if(mob.isASysOp())
							CreateEdit.Edit(mob,commands);
						else
							mob.tell("Huh?\n\r");
						break;
					case CommandSet.NOFOLLOW:
						Grouping.nofollow(mob,true);
						break;
					case CommandSet.NORTH:
						Movement.move(mob,Directions.NORTH,false);
						break;
					case CommandSet.NOCHANNEL:
						Channels.nochannel(mob,commands);
						break;
					case CommandSet.OPEN:
						Movement.open(mob,combine(commands,1));
						break;
					case CommandSet.ORDER:
						Grouping.order(mob,commands);
						break;
					case CommandSet.PASSWORD:
						BasicSenses.password(mob,commands);
						break;
					case CommandSet.PRACTICE:
						AbilityEvoker.practice(mob,commands);
						break;
					case CommandSet.PRAYERS:
						Scoring.prayers(mob);
						break;
					case CommandSet.PREVIOUS_CMD:
						if(!mob.isMonster())
							doCommand(mob,mob.session().previousCmd);
						break;
					case CommandSet.PUT:
						ItemUsage.put(mob,commands);
						break;
					case CommandSet.QUIET:
						Channels.quiet(mob);
						break;
					case CommandSet.QUIT:
						if(mob.soulMate()!=null)
							Archon_Possess.dispossess(mob);
						else
						if(!mob.isMonster())
							mob.session().cmdExit(mob,commands); 
						break;
					case CommandSet.READ:
						ItemUsage.read(mob,commands);
						break;
					case CommandSet.REMOVE:
						ItemUsage.remove(mob,commands);
						break;
					case CommandSet.REPLY:
						SocialProcessor.quickSay(mob,mob.replyTo(),combine(commands,1),true);
						break;
					case CommandSet.REPORT:
						SocialProcessor.report(mob);
						break;
					case CommandSet.SAVE:
						if(mob.isASysOp())
							CreateEdit.Save(mob,commands);
						else
							mob.tell("Huh?\n\r");
						break;
					case CommandSet.SAY:
						SocialProcessor.cmdSay(mob,commands);
						break;
					case CommandSet.SCORE:
						Scoring.score(mob);
						break;
					case CommandSet.SELL:
						SocialProcessor.sell(mob,commands);
						break;
					case CommandSet.SIT:
						Movement.sit(mob);
						break;
					case CommandSet.SKILLS:
						Scoring.skills(mob);
						break;
					case CommandSet.SLEEP:
						Movement.sleep(mob);
						break;
					case CommandSet.SOCIALS:
						Scoring.socials(mob);
						break;
					case CommandSet.SONGS:
						Scoring.songs(mob);
						break;
					case CommandSet.SOUTH:
						Movement.move(mob,Directions.SOUTH,false);
						break;
					case CommandSet.SPELLS:
						Scoring.spells(mob);
						break;
					case CommandSet.SPLIT:
						Grouping.split(mob,commands);
						break;
					case CommandSet.STAND:
						Movement.stand(mob);
						break;
					case CommandSet.SYSMSGS:
						if(mob.isASysOp())
							mob.toggleReadSysopMsgs();
						break;
					case CommandSet.TAKE:
						if(mob.isASysOp())
							SysopItemUsage.take(mob,commands);
						else
							mob.tell("Huh?\n\r");
						break;
					case CommandSet.TELL:
						SocialProcessor.tell(mob,commands);
						break;
					case CommandSet.TEACH:
						AbilityEvoker.teach(mob,commands);
						break;
					case CommandSet.TRAIN:
						BasicSenses.train(mob,commands);
						break;
					case CommandSet.UNLOCK:
						Movement.unlock(mob,combine(commands,1));
						break;
					case CommandSet.UP:
						Movement.move(mob,Directions.UP,false);
						break;
					case CommandSet.WAKE:
						Movement.wake(mob);
						break;
					case CommandSet.WEAR:
						ItemUsage.wear(mob,commands);
						break;
					case CommandSet.WEST:
						Movement.move(mob,Directions.WEST,false);
						break;
					case CommandSet.WHOIS:
						Grouping.who(mob,combine(commands,1));
						break;
					case CommandSet.WHO:
						Grouping.who(mob,null);
						break;
					case CommandSet.WIELD:
						ItemUsage.wield(mob,commands);
						break;
					case CommandSet.WIMPY:
						BasicSenses.wimpy(mob,commands);
						break;
				}
			}
		}
		else
		{
			Social social=MUD.allSocials.FetchSocial(commands);
			if(social!=null)
				SocialProcessor.doSocial(social,mob,commands);
			else
				mob.tell("Huh?\n\r");
		}
	}
	
	public static String combine(Vector commands, int startAt)
	{
		StringBuffer Combined=new StringBuffer("");
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			Combined.append((String)commands.elementAt(commandIndex)+" ");
		return Combined.toString().trim();
	}
	
	public static Vector parse(String str)
	{
		Vector commands=new Vector();
		str=str.trim();
		while(!str.equals(""))
		{
			int spaceIndex=str.indexOf(" ");
			int strIndex=str.indexOf("\"");
			String CMD="";
			if((strIndex>=0)&&((strIndex<spaceIndex)||(spaceIndex<0)))
			{
				int endStrIndex=str.indexOf("\"",strIndex+1);
				if(endStrIndex>strIndex)
				{
					CMD=str.substring(strIndex+1,endStrIndex).trim();
					str=str.substring(endStrIndex+1).trim();
				}
				else
				{
					CMD=str.substring(strIndex+1).trim();
					str="";
				}
			}
			else
			if(spaceIndex>=0)
			{
				CMD=str.substring(0,spaceIndex).trim();
				str=str.substring(spaceIndex+1).trim();
			}
			else
			{
				CMD=str.trim();
				str="";
			}
			if(!CMD.equals(""))
				commands.addElement(CMD);
		}
		return commands;
	}
	
	public static void credits(MOB mob)
	{
		StringBuffer credits=Resources.getFileResource("credits.txt");
		
		if((credits!=null)&&(mob.session()!=null))
			mob.session().rawPrintln(credits.toString());
		return;
	}
	
	public static void help(MOB mob, String helpStr)
	{
		if(helpStr.length()==0)
		{
			StringBuffer helpText=Resources.getFileResource("help.txt");
			if((helpText!=null)&&(mob.session()!=null))
				mob.session().rawPrintln(helpText.toString());
			return;
		}
		else
			helpStr=helpStr.toUpperCase().trim();
		if(helpFile==null)
		{
			helpFile=new Properties();
			try
			{
				helpFile.load(new FileInputStream("resources"+File.separatorChar+"help.ini"));
				helpFile.load(new FileInputStream("resources"+File.separatorChar+"skill_help.ini"));
				helpFile.load(new FileInputStream("resources"+File.separatorChar+"spell_help.ini"));
				helpFile.load(new FileInputStream("resources"+File.separatorChar+"songs_help.ini"));
				helpFile.load(new FileInputStream("resources"+File.separatorChar+"prayer_help.ini"));
			}
			catch(IOException e)
			{
				helpFile=null;
			}
		}
		if(helpFile==null)
		{
			mob.tell("No help is available.");
			return;
		}
		
		String thisTag=helpFile.getProperty(helpStr);
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		{
			mob.tell("No help is available on '"+helpStr+"'. Try entering 'COMMANDS' for a command list.");
			return;
		}
		if(!mob.isMonster())
			mob.session().rawPrintln(thisTag);
	}
}
