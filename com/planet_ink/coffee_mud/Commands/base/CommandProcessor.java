package com.planet_ink.coffee_mud.Commands.base;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.sysop.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class CommandProcessor
{
	public CommandSet commandSet=new CommandSet();
	public Channels channels=new Channels();
	public BasicSenses basicSenses=new BasicSenses();
	public AbilityEvoker abilityEvoker=new AbilityEvoker();
	public FrontDoor frontDoor=new FrontDoor();
	public Grouping grouping=new Grouping();
	public ItemUsage itemUsage=new ItemUsage();
	public Movement movement=new Movement();
	public Scoring scoring=new Scoring();
	public SocialProcessor socialProcessor=new SocialProcessor();
	public Socials socials=new Socials();
	public TheFight theFight=new TheFight(grouping);
	public CreateEdit createEdit=new CreateEdit(socials);
	public Import importer=new Import();
	public SysopItemUsage sysopItemUsage=new SysopItemUsage();
	public SysOpSkills sysopSkills=new SysOpSkills();
	public XMLIO xmlIO=new XMLIO();
	public Reset reset=new Reset();
	public Properties helpFile=null;
	public Properties arcHelpFile=null;
	public Host myHost=null;

	public void doCommand(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()==0) return;
		if(mob.location()==null) return;

		String firstWord=((String)commands.elementAt(0)).toUpperCase();
		Command C=CMClass.findExtraCommand(firstWord);
		if((C!=null)&&(!C.execute(mob,commands)))
			return;

		Integer commandCodeObj=(Integer)commandSet.get(firstWord);
		if(commandCodeObj!=null)
		{
			int commandCode=commandCodeObj.intValue();
			if(commandCode>=0)
			{
				switch(commandCode)
				{
				case CommandSet.AFFECT:
					scoring.affected(mob);
					break;
				case CommandSet.ANSI:
					if(!mob.isMonster())
					{
						mob.setBitmap(mob.getBitmap()|MOB.ATT_ANSI);
						mob.tell("^BANSI^N ^Hcolour^N enabled.\n\r");
					}
					break;
				case CommandSet.ARCHELP:
					if(mob.isASysOp(mob.location()))
						arcHelp(mob,Util.combine(commands,1));
					else
						mob.tell("You are not powerful enough to even care.\n\r");
					break;
				case CommandSet.ARCTOPICS:
					if(mob.isASysOp(mob.location()))
						arcTopics(mob);
					else
						mob.tell("You are not powerful enough to even care.\n\r");
					break;
				case CommandSet.AREAS:
					scoring.areas(mob);
					break;
				case CommandSet.AUTOMELEE:
					theFight.autoMelee(mob);
					break;
				case CommandSet.AUTOASSIST:
					theFight.autoAssist(mob);
					break;
				case CommandSet.AUTOEXITS:
					basicSenses.autoExits(mob);
					break;
				case CommandSet.AUTOGOLD:
					theFight.autogold(mob);
					break;
				case CommandSet.AUTOLOOT:
					theFight.autoloot(mob);
					break;
				case CommandSet.PUSH:
					itemUsage.push(mob,Util.combine(commands,1),commandSet);
					break;
				case CommandSet.PULL:
					itemUsage.pull(mob,Util.combine(commands,1));
					break;
				case CommandSet.BUG:
					Log.errOut(mob.name(),Util.combine(commands,1));
					mob.tell("Thank you for your assistance in debugging CoffeeMud!");
					break;
				case CommandSet.BUY:
					socialProcessor.buy(mob,commands);
					break;
				case CommandSet.CLOSE:
					movement.close(mob,Util.combine(commands,1));
					break;
				case CommandSet.CHANNEL:
					channels.channel(mob,commands);
					break;
				case CommandSet.CHANNELS:
					channels.listChannels(mob);
					break;
				case CommandSet.CHANTS:
					scoring.chants(mob);
					break;
				case CommandSet.CHARGEN:
					if(mob.isASysOp(mob.location()))
						sysopSkills.chargen(mob,commands);
					else
						mob.tell("Sorry, you don't have that power!");
					break;
				case CommandSet.CONSIDER:
					socialProcessor.consider(mob,commands);
					break;
				case CommandSet.COMPARE:
					itemUsage.compare(mob,commands);
					break;
				case CommandSet.COMMANDS:
					scoring.commands(mob,commandSet);
					break;
				case CommandSet.CRAWL:
					movement.crawl(mob,commands);
					break;
				case CommandSet.CREATE:
					if(mob.isASysOp(mob.location()))
						createEdit.create(mob,commands);
					else
						mob.tell("You lack the power of creation.\n\r");
					break;
				case CommandSet.CREDITS:
					credits(mob);
					break;
				case CommandSet.DESCRIPTION:
					basicSenses.description(mob,commands);
					break;
				case CommandSet.DESTROY:
					if(mob.isASysOp(mob.location()))
						createEdit.destroy(mob,commands);
					else
						mob.tell("You lack the power to destroy things this way.  Did you mean kill?\n\r");
					break;
				case CommandSet.DISMOUNT:
					movement.dismount(mob,commands);
					break;
				case CommandSet.DOWN:
					movement.standAndGo(mob,Directions.DOWN);
					break;
				case CommandSet.DRINK:
					itemUsage.drink(mob,commands);
					break;
				case CommandSet.DROP:
					itemUsage.drop(mob,commands);
					break;
				case CommandSet.DUMPFILE:
					if(mob.isASysOp(null))
						cmdDumpfile(mob,commands);
					else
						mob.tell("Huh?\n\r");
					break;
				case CommandSet.EAST:
					movement.standAndGo(mob,Directions.EAST);
					break;
				case CommandSet.EAT:
					itemUsage.eat(mob,commands);
					break;
				case CommandSet.EMOTE:
					basicSenses.emote(mob,commands);
					break;
				case CommandSet.ENTER:
					movement.enter(mob,commands);
					break;
				case CommandSet.EVOKE: // an ability
					abilityEvoker.evoke(mob,commands);
					break;
				case CommandSet.EQUIPMENT:
					scoring.equipment(mob);
					break;
				case CommandSet.EXAMINE:
					basicSenses.look(mob,commands,false);
					break;
				case CommandSet.EXITS:
					mob.location().listExits(mob);
					break;
				case CommandSet.EXPORT:
					if(mob.isASysOp(null))
						importer.export(mob,commands);
					else
						mob.tell("Only the Archons may Export.\n\r");
					break;
				case CommandSet.FILL:
					itemUsage.fill(mob,commands);
					break;
				case CommandSet.FLEE:
					movement.flee(mob,Util.combine(commands,1));
					break;
				case CommandSet.FOLLOW:
					grouping.follow(mob,commands);
					break;
				case CommandSet.GET:
					itemUsage.get(mob,commands);
					break;
				case CommandSet.GIVE:
					socialProcessor.give(mob,commands,false);
					break;
				case CommandSet.GO:
					movement.go(mob,commands);
					break;
				case CommandSet.GOTO:
					if(mob.isASysOp(mob.location()))
						sysopSkills.gotoCmd(mob,commands);
					else
						mob.tell("Try 'GO'.");
					break;
				case CommandSet.GROUP:
					grouping.group(mob);
					break;
				case CommandSet.GTELL:
					grouping.gtell(mob,Util.combine(commands,1));
					break;
				case CommandSet.HELP:
					help(mob,Util.combine(commands,1));
					break;
				case CommandSet.HOLD:
					itemUsage.hold(mob,commands);
					break;
				case CommandSet.IMPORT:
					if(mob.isASysOp(null))
						importer.areimport(mob,commands);
					else
						mob.tell("Only the Archons may Import.\n\r");
					break;
				case CommandSet.INVENTORY:
					scoring.inventory(mob);
					break;
				case CommandSet.KILL:
					theFight.kill(mob,commands);
					break;
				case CommandSet.LANGUAGES:
					scoring.languages(mob);
					break;
				case CommandSet.LIST:
					socialProcessor.list(mob,commands);
					break;
				case CommandSet.LINK:
					if(mob.isASysOp(mob.location()))
						createEdit.link(mob,commands);
					else
						mob.tell("You lack the power to link rooms.\n\r");
					break;
				case CommandSet.LOCK:
					movement.lock(mob,Util.combine(commands,1));
					break;
				case CommandSet.LOOK:
					basicSenses.look(mob,commands,false);
					break;
				case CommandSet.MODIFY:
					if(mob.isASysOp(mob.location()))
						createEdit.edit(mob,commands);
					else
						mob.tell("You lack the power to modify things.\n\r");
					break;
				case CommandSet.MOUNT:
					movement.mount(mob,commands);
					break;
				case CommandSet.NOANSI:
					if(!mob.isMonster())
					{
						if((mob.getBitmap()&MOB.ATT_ANSI)>0)
							mob.setBitmap(mob.getBitmap()-MOB.ATT_ANSI);
						mob.tell("ANSI colour disabled.\n\r");
					}
					break;
				case CommandSet.NOFOLLOW:
					grouping.nofollow(mob,true,false);
					break;
				case CommandSet.NORTH:
					movement.standAndGo(mob,Directions.NORTH);
					break;
				case CommandSet.NOCHANNEL:
					channels.nochannel(mob,commands);
					break;
				case CommandSet.OPEN:
					movement.open(mob,Util.combine(commands,1));
					break;
				case CommandSet.ORDER:
					grouping.order(mob,commands);
					break;
				case CommandSet.OUTFIT:
					basicSenses.outfit(mob);
					break;
				case CommandSet.PASSWORD:
					basicSenses.password(mob,commands);
					break;
				case CommandSet.PLAYERKILL:
					theFight.playerkill(mob);
					break;
				case CommandSet.POSSESS:
					if(mob.isASysOp(mob.location()))
						sysopSkills.possess(mob,commands);
					else
						mob.tell("You aren't powerful enough to possess anyone.");
					break;
				case CommandSet.PRACTICE:
					abilityEvoker.practice(mob,commands);
					break;
				case CommandSet.PRAYERS:
					scoring.prayers(mob);
					break;
				case CommandSet.PREVIOUS_CMD:
					if(!mob.isMonster())
						doCommand(mob,Util.copyVector(mob.session().previousCMD()));
					break;
				case CommandSet.PUT:
					itemUsage.put(mob,commands);
					break;
				case CommandSet.QUALIFY:
					scoring.qualify(mob,commands);
					break;
				case CommandSet.QUIET:
					channels.quiet(mob);
					break;
				case CommandSet.QUIT:
					if(mob.soulMate()!=null)
						new SysOpSkills().dispossess(mob);
					else
					if(!mob.isMonster())
						mob.session().cmdExit(mob,commands);
					break;
				case CommandSet.READ:
					itemUsage.read(mob,commands);
					break;
				case CommandSet.REBUKE:
					socialProcessor.rebuke(mob,commands);
					break;
				case CommandSet.REMOVE:
					itemUsage.remove(mob,commands);
					break;
				case CommandSet.REPLY:
					if(mob.replyTo()==null)
						mob.tell("No one has told you anything yet!");
					else
						socialProcessor.quickSay(mob,mob.replyTo(),Util.combine(commands,1),true,!mob.location().isInhabitant(mob.replyTo()));
					break;
				case CommandSet.REPORT:
					socialProcessor.report(mob);
					break;
				case CommandSet.RESET:
					if(mob.isASysOp(mob.location()))
						reset.resetSomething(mob,commands);
					else
						mob.tell("You are not powerful enough.\n\r");
					break;
				case CommandSet.SAVE:
					if(mob.isASysOp(mob.location()))
						createEdit.save(mob,commands);
					else
						mob.tell("Your game is automatically being saved while you play.\n\r");
					break;
				case CommandSet.SAY:
					socialProcessor.cmdSay(mob,commands);
					break;
				case CommandSet.SHUTDOWN:
					if(mob.isASysOp(null))
						shutdown(mob, commands);
					else
						mob.tell("You are not powerful enough.\n\r");
					break;
				case CommandSet.SCORE:
					scoring.score(mob);
					break;
				case CommandSet.SELL:
					socialProcessor.sell(mob,commands);
					break;
				case CommandSet.SERVE:
					socialProcessor.serve(mob,commands);
					break;
				case CommandSet.SIT:
					movement.sit(mob,commands);
					break;
				case CommandSet.SKILLS:
					scoring.skills(mob);
					break;
				case CommandSet.SLEEP:
					movement.sleep(mob,commands);
					break;
				case CommandSet.SOCIALS:
					scoring.socials(mob,socials);
					break;
				case CommandSet.SONGS:
					scoring.songs(mob);
					break;
				case CommandSet.SOUTH:
					movement.standAndGo(mob,Directions.SOUTH);
					break;
				case CommandSet.SPELLS:
					scoring.spells(mob,commands);
					break;
				case CommandSet.SPLIT:
					grouping.split(mob,commands);
					break;
				case CommandSet.STAND:
					movement.stand(mob);
					break;
				case CommandSet.SYSMSGS:
					if(mob.isASysOp(mob.location()))
						sysopSkills.toggleSysopMsgs(mob);
					break;
				case CommandSet.TAKE:
					if(mob.isASysOp(mob.location()))
						sysopItemUsage.take(mob,commands);
					else
						basicSenses.mundaneTake(mob,commands);
					break;
				case CommandSet.TELL:
					socialProcessor.tell(mob,commands);
					break;
				case CommandSet.TEACH:
					abilityEvoker.teach(mob,commands);
					break;
				case CommandSet.TIME:
					basicSenses.time(mob,commands);
					break;
				case CommandSet.TOPICS:
					topics(mob);
					break;
				case CommandSet.TRAIN:
					basicSenses.train(mob,commands);
					break;
				case CommandSet.UNLOCK:
					movement.unlock(mob,Util.combine(commands,1));
					break;
				case CommandSet.UNLINK:
					createEdit.destroy(mob,commands);
					break;
				case CommandSet.UNLOADHELP:

					if(mob.isASysOp(null))
						unloadHelpFile(mob);
					else
						mob.tell("Only the Archons may unload the help files...\n\r");
					break;
				case CommandSet.UP:
					movement.standAndGo(mob,Directions.UP);
					break;
				case CommandSet.VALUE:
					socialProcessor.value(mob,commands);
					break;
				case CommandSet.VASSALS:
					socialProcessor.vassals(mob,commands);
					break;
				case CommandSet.VER:
					mob.tell(myHost.getVer());
					mob.tell("(C) 2000-2002 Bo Zimmerman");
					mob.tell("bo@zimmers.net");
					mob.tell("http://www.zimmers.net/home/mud.html");
					break;
				case CommandSet.WAKE:
					movement.wake(mob);
					break;
				case CommandSet.WEAR:
					itemUsage.wear(mob,commands);
					break;
				case CommandSet.WEATHER:
					basicSenses.weather(mob,commands);
					break;
				case CommandSet.WEST:
					movement.standAndGo(mob,Directions.WEST);
					break;
				case CommandSet.WHOIS:
					grouping.who(mob,Util.combine(commands,1));
					break;
				case CommandSet.WHO:
					grouping.who(mob,null);
					break;
				case CommandSet.WIELD:
					itemUsage.wield(mob,commands);
					break;
				case CommandSet.WIMPY:
					basicSenses.wimpy(mob,commands);
					break;
				case CommandSet.WIZINV:
					if(mob.isASysOp(null))
						sysopSkills.wizinv(mob,commands);
					else
						mob.tell("You aren't powerful enough to do that.");
					break;
				case CommandSet.XML:
					if(mob.isASysOp(null))
						xmlIO.xml(mob,commands);
					else
						mob.tell("You are not powerful enough.\n\r");
					break;
				case CommandSet.YELL:
					socialProcessor.yell(mob,commands);
					break;
				}
			}
		}
		else
		{
			Social social=socials.FetchSocial(commands);
			if(social!=null)
				socialProcessor.doSocial(social,mob,commands);
			else
				mob.tell("Huh?\n\r");
		}
	}

	public void credits(MOB mob)
	{
		StringBuffer credits=Resources.getFileResource("credits.txt");

		if((credits!=null)&&(mob.session()!=null))
			mob.session().rawPrintln(credits.toString());
		return;
	}

	private void unloadHelpFile(MOB mob)
	{
		if(Resources.getResource("PLAYER TOPICS")!=null)
			Resources.removeResource("PLAYER TOPICS");
		if(Resources.getResource("ARCHON TOPICS")!=null)
			Resources.removeResource("ARCHON TOPICS");
		if(Resources.getResource("help.txt")!=null)
			Resources.removeResource("help.txt");
		if(Resources.getResource("races.txt")!=null)
			Resources.removeResource("races.txt");
		if(Resources.getResource("newchar.txt")!=null)
			Resources.removeResource("newchar.txt");
		if(Resources.getResource("stats.txt")!=null)
			Resources.removeResource("stats.txt");
		if(Resources.getResource("classes.txt")!=null)
			Resources.removeResource("classes.txt");
		if(Resources.getResource("alignment.txt")!=null)
			Resources.removeResource("alignment.txt");
		if(Resources.getResource("arc_help.txt")!=null)
			Resources.removeResource("arc_help.txt");
		helpFile=null;
		arcHelpFile=null;

		// also the intro page
		if(Resources.getResource("intro.txt")!=null)
			Resources.removeResource("intro.txt");

		if(Resources.getResource("offline.txt")!=null)
			Resources.removeResource("offline.txt");

		mob.tell("Help files unloaded. Next HELP, AHELP, new char will reload.");
	}

	private boolean getArcHelpFile()
	{
		if(arcHelpFile==null)
		{
			arcHelpFile=new Properties();
			try{arcHelpFile.load(new FileInputStream("resources"+File.separatorChar+"arc_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
		}
		if(arcHelpFile==null) return false;
		return true;
	}

	private boolean getHelpFile()
	{
		if(helpFile==null)
		{
			helpFile=new Properties();
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"misc_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"skill_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"spell_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"songs_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"prayer_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
			try{helpFile.load(new FileInputStream("resources"+File.separatorChar+"chant_help.ini"));}catch(IOException e){Log.errOut("CommandProcessor",e);}
		}
		if(helpFile==null) return false;
		return true;
	}

	public void topics(MOB mob)
	{
		if(!getHelpFile())
		{
			mob.tell("No help is available.");
			return;
		}

		doTopics(mob,helpFile,"HELP", "PLAYER TOPICS");
	}

	public void arcTopics(MOB mob)
	{
		if(!getArcHelpFile())
		{
			mob.tell("No archon help is available.");
			return;
		}

		doTopics(mob,arcHelpFile,"AHELP", "ARCHON TOPICS");
	}

	private void doTopics(MOB mob, Properties rHelpFile, String helpName, String resName)
	{
		StringBuffer topicBuffer=(StringBuffer)Resources.getResource(resName);
		if(topicBuffer==null)
		{
			topicBuffer=new StringBuffer();

			Vector reverseList=new Vector();
			for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
			{
				String ptop = (String)e.nextElement();
				String thisTag=rHelpFile.getProperty(ptop);
				if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
					|| (rHelpFile.getProperty(thisTag)== null) )
						reverseList.addElement(ptop);
			}

			Collections.sort((List)reverseList);
			topicBuffer=new StringBuffer("Help topics: \n\r\n\r");
			int col=0;
			for(int i=0;i<reverseList.size();i++)
			{
				if((++col)>4)
				{
					topicBuffer.append("\n\r");
					col=1;
				}
				if(((String)reverseList.elementAt(i)).length()>19)
				{
					topicBuffer.append(Util.padRight((String)reverseList.elementAt(i),(19*2)+1)+" ");
					++col;
				}
				else
					topicBuffer.append(Util.padRight((String)reverseList.elementAt(i),19)+" ");
			}
			topicBuffer=new StringBuffer(topicBuffer.toString().replace('_',' '));
			Resources.submitResource(resName,topicBuffer);
		}
		if((topicBuffer!=null)&&(!mob.isMonster()))
			mob.session().rawPrintln(topicBuffer.toString()+"\n\r\n\rEnter "+helpName+" (TOPIC NAME) for more information.");
	}

	public void help(MOB mob, String helpStr)
	{
		if(helpStr.length()==0)
		{
			StringBuffer helpText=Resources.getFileResource("help.txt");
			if((helpText!=null)&&(mob.session()!=null))
				mob.session().unfilteredPrintln(helpText.toString());
			return;
		}
		else
		{
			if(!getHelpFile())
			{
				mob.tell("No help is available.");
				return;
			}
			// the area exception
			if(CMMap.getArea(helpStr.trim())!=null)
			{
				StringBuffer s=(StringBuffer)Resources.getResource("HELP_"+helpStr.trim().toUpperCase());
				if(s==null)
				{
					s=CMMap.getArea(helpStr.trim()).getAreaStats();
					Resources.submitResource("HELP_"+helpStr.trim().toUpperCase(),s);
				}
				mob.tell(s.toString());
				return;
			}
		}
		doHelp(mob,helpStr,helpFile);
	}

	public void arcHelp(MOB mob, String helpStr)
	{
		if(helpStr.length()==0)
		{
			StringBuffer helpText=Resources.getFileResource("arc_help.txt");
			if((helpText!=null)&&(mob.session()!=null))
				mob.session().unfilteredPrintln(helpText.toString());
			return;
		}
		else
		{
			if(!getArcHelpFile())
			{
				mob.tell("No archon help is available.");
				return;
			}
		}
		doHelp(mob,helpStr,arcHelpFile);
	}

	private void doHelp(MOB mob, String helpStr, Properties rHelpFile)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		String thisTag=rHelpFile.getProperty(helpStr);
		if(thisTag==null) thisTag=rHelpFile.getProperty("SPELL_"+helpStr);
		if(thisTag==null) thisTag=rHelpFile.getProperty("PRAYER_"+helpStr);
		if(thisTag==null) thisTag=rHelpFile.getProperty("SONG_"+helpStr);

		while((thisTag!=null)&&(thisTag.length()>0)&&(thisTag.length()<25))
		{
			String thisOtherTag=rHelpFile.getProperty(thisTag);
			if((thisOtherTag!=null)&&(thisOtherTag.equals(thisTag)))
				thisTag=null;
			else
				thisTag=thisOtherTag;
		}
		if((thisTag==null)||((thisTag!=null)&&(thisTag.length()==0)))
		{
			mob.tell("No help is available on '"+helpStr+"'.\nEnter 'COMMANDS' for a command list, or 'TOPICS' for a complete list.");
			return;
		}
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(thisTag);
	}

	public void shutdown(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster()) return;
		if(!mob.session().confirm("Are you fully aware of the consequences of this act (y/N)?","N"))
			return;
		boolean keepItDown=true;
		String externalCommand=null;
		if(commands.size()>1)
		{
			if(((String)commands.elementAt(1)).equalsIgnoreCase("RESTART"))
				keepItDown=false;
			if(commands.size()>2)
				externalCommand=Util.combine(commands,2);
		}
		if(keepItDown)
			Log.errOut("CommandProcessor",mob.name()+" starts system shutdown...");
		else
		if(externalCommand!=null)
			Log.errOut("CommandProcessor",mob.name()+" starts system restarting '"+externalCommand+"'...");
		else
			Log.errOut("CommandProcessor",mob.name()+" starts system restart...");
		if(myHost!=null)
			myHost.shutdown(mob.session(),keepItDown,externalCommand);
		else
			Log.errOut("CommandProcessor","Shutdown failed.  No host.");
	}

	public void cmdDumpfile(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("dumpfile {raw} username|all {filename1 ...}");
			return;
		}
		commands.removeElementAt(0);

		int numFiles = 0;
		int numSessions = 0;
		boolean rawMode=false;

		if(((String)commands.elementAt(0)).equalsIgnoreCase("raw"))
		{
			rawMode = true;
			commands.removeElementAt(0);
		}

		String targetName = new String((String)commands.elementAt(0));
		boolean allFlag=(targetName.equalsIgnoreCase("all"));

		commands.removeElementAt(0);

		// so they can do dumpfile (username) RAW filename too
		if(!rawMode && ( ((String)commands.elementAt(0)).equalsIgnoreCase("raw")) )
		{
			rawMode = true;
			commands.removeElementAt(0);
		}

		StringBuffer fileText = new StringBuffer("");
		while (commands.size() > 0)
		{
			boolean wipeAfter = true;
			String fn = new String ( (String)commands.elementAt(0) );
			// don't allow any path characters!
			fn.replace('/','_');
			fn.replace('\\','_');
			fn.replace(':','_');

			if (Resources.getResource(fn) != null)
				wipeAfter = false;

			StringBuffer ft = Resources.getFileResource(fn);
			if (ft != null && ft.length() > 0)
			{
				fileText.append("\n\r");
				fileText.append(ft);
				++numFiles;
			}

			if (wipeAfter)
				Resources.removeResource(fn);
			commands.removeElementAt(0);

		}
		if (fileText.length() > 0)
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session thisSession=(Session)Sessions.elementAt(s);

				if (thisSession==null) continue;
				if (thisSession.killFlag() || (thisSession.mob()==null)) continue;
				if (allFlag || thisSession.mob().name().equalsIgnoreCase(targetName))
				{
					if (rawMode)
						thisSession.rawPrintln(fileText.toString());
					else
						thisSession.colorOnlyPrintln(fileText.toString());
					++numSessions;
				}
			}
		}
		mob.tell("dumped " + numFiles + " files to " + numSessions + " user(s)");
	}

}
