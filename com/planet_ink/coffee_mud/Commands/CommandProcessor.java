package com.planet_ink.coffee_mud.Commands;

import java.util.*;
import java.io.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.sysop.*;
import com.planet_ink.coffee_mud.Commands.base.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class CommandProcessor
{
	public static CommandSet commandSet=CommandSet.getInstance();
	public static Host myHost=null;

	private CommandProcessor(){}

	public static void doCommand(MOB mob, Vector commands)
		throws Exception
	{
		if(commands.size()==0) return;
		if(mob.location()==null) return;

		String firstWord=((String)commands.elementAt(0)).toUpperCase();
		Integer commandCodeObj=(Integer)commandSet.get(firstWord);
		if((commandCodeObj==null)&&(firstWord.length()>0))
		{
			if(!Character.isLetterOrDigit(firstWord.charAt(0)))
				commandCodeObj=(Integer)commandSet.get(""+firstWord.charAt(0));
			if(commandCodeObj!=null)
			{
				commands.insertElementAt(((String)commands.elementAt(0)).substring(1),1);
				commands.setElementAt(""+firstWord.charAt(0),0);
			}
			else
			{
				Command C=CMClass.findExtraCommand(firstWord);
				if((C!=null)&&(!C.execute(mob,commands)))
					return;
				for(Enumeration e=commandSet.keys();e.hasMoreElements();)
				{
					String key=(String)e.nextElement();
					if(key.toUpperCase().startsWith(firstWord))
					{
						commandCodeObj=(Integer)commandSet.get(key);
						commands.setElementAt(key.toLowerCase(),0);
						break;
					}
				}
			}
		}
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
				case CommandSet.ANNOUNCE:
					if(mob.isASysOp(mob.location()))
						SysOpSkills.announce(mob,commands);
					else
						mob.tell("You are not powerful enough to do that.\n\r");
					break;
				case CommandSet.ANSI:
					BasicSenses.ansi(mob,1);
					break;
				case CommandSet.ARCHELP:
					if(mob.isASysOp(mob.location()))
						Help.arcHelp(mob,Util.combine(commands,1));
					else
						mob.tell("You are not powerful enough to even care.\n\r");
					break;
				case CommandSet.ARCTOPICS:
					if(mob.isASysOp(mob.location()))
						Help.arcTopics(mob);
					else
						mob.tell("You are not powerful enough to even care.\n\r");
					break;
				case CommandSet.AREAS:
					Scoring.areas(mob);
					break;
				case CommandSet.AUCTION:
					Channels.auction(mob,commands);
					break;
				case CommandSet.AUTOMELEE:
					TheFight.autoMelee(mob);
					break;
				case CommandSet.AUTOASSIST:
					TheFight.autoAssist(mob);
					break;
				case CommandSet.AUTOEXITS:
					BasicSenses.autoExits(mob);
					break;
				case CommandSet.AUTOGOLD:
					TheFight.autogold(mob);
					break;
				case CommandSet.AUTOLOOT:
					TheFight.autoloot(mob);
					break;
				case CommandSet.AUTOWEATHER:
					BasicSenses.autoweather(mob);
					break;
				case CommandSet.AUTOGUARD:
					TheFight.autoGuard(mob,commands);
					break;
				case CommandSet.AUTODRAW:
					TheFight.autoDraw(mob);
					break;
				case CommandSet.BAN:
					if(mob.isASysOp(null))
						SysOpSkills.ban(mob,commands);
					else
						mob.tell("You are not powerful enough to do that.\n\r");
					break;
				case CommandSet.BEACON:
					if(mob.isASysOp(mob.location()))
						SysOpSkills.beacon(mob,commands);
					else
						mob.tell("You are not powerful enough to do that.\n\r");
					break;
				case CommandSet.BOOT:
					if(mob.isASysOp(mob.location()))
						SysOpSkills.boot(mob,commands);
					else
						mob.tell("You are not powerful enough to do that.\n\r");
					break;
				case CommandSet.BRIEF:
					BasicSenses.brief(mob);
					break;
				case CommandSet.BUG:
					if(Util.combine(commands,1).length()>0)
					{
						ExternalPlay.DBWriteJournal("SYSTEM_BUGS",mob.Name(),"ALL","BUG",Util.combine(commands,1),-1);
						mob.tell("Thank you for your assistance in debugging CoffeeMud!");
					}
					else
						mob.tell("What's the bug? Be Specific!");
					break;
				case CommandSet.BUY:
					ShopKeepers.buy(mob,commands);
					break;
				case CommandSet.CLOSE:
					Movement.close(mob,Util.combine(commands,1));
					break;
				case CommandSet.CHANNEL:
					Channels.channel(mob,commands);
					break;
				case CommandSet.CHANNELS:
					Channels.listChannels(mob);
					break;
				case CommandSet.CHANTS:
					Scoring.chants(mob);
					break;
				case CommandSet.CHANWHO:
					Channels.channelWho(mob,Util.combine(commands,1));
					break;
				case CommandSet.CLANACCEPT:
					ClanCommands.clanaccept(mob,commands);
					break;
				case CommandSet.CLANAPPLY:
					ClanCommands.clanapply(mob,commands);
					break;
				case CommandSet.CLANASSIGN:
					ClanCommands.clanassign(mob,commands);
					break;
				case CommandSet.CLANBALANCE:
					ClanCommands.clanBalance(mob,commands);
					break;
				case CommandSet.CLANCREATE:
					ClanCommands.clanCreate(mob,commands);
					break;
				case CommandSet.CLANDEPOSIT:
					ClanCommands.clanDeposit(mob,commands);
					break;
				case CommandSet.CLANDETAILS:
					ClanCommands.clandetails(mob,commands);
					break;
				case CommandSet.CLANDONATE:
					ClanCommands.clanDonate(mob,commands);
					break;
				case CommandSet.CLANDONATESET:
					ClanCommands.clanDonate(mob,commands);
					break;
				case CommandSet.CLANEXILE:
					ClanCommands.clanexile(mob,commands);
					break;
				case CommandSet.CLANHOMESET:
					ClanCommands.clanhomeset(mob,commands);
					break;
				case CommandSet.CLANLIST:
					ClanCommands.clanlist(mob,commands);
					break;
				case CommandSet.CLANREJECT:
					ClanCommands.clanreject(mob,commands);
					break;
				case CommandSet.CLANRESIGN:
					ClanCommands.clanresign(mob,commands);
					break;
				case CommandSet.CLANWITHDRAW:
					ClanCommands.clanWithdraw(mob,commands);
					break;
				case CommandSet.CONSIDER:
					SocialProcessor.consider(mob,commands);
					break;
				case CommandSet.COMPARE:
					ItemUsage.compare(mob,commands);
					break;
				case CommandSet.COLORSET:
					Scoring.colorSet(mob,commands);
					break;
				case CommandSet.COMMANDS:
					Scoring.commands(mob,commandSet);
					break;
				case CommandSet.CONFIG:
					Scoring.config(mob,commands);
					break;
				case CommandSet.COPY:
					if(mob.isASysOp(mob.location()))
						CreateEdit.copy(mob,commands);
					else
						mob.tell("You lack the power of creation.\n\r");
					break;
				case CommandSet.CRAWL:
					Movement.crawl(mob,commands);
					break;
				case CommandSet.CREATE:
					if(mob.isASysOp(mob.location()))
						CreateEdit.create(mob,commands);
					else
						mob.tell("You lack the power of creation.\n\r");
					break;
				case CommandSet.CREDITS:
					credits(mob);
					break;
				case CommandSet.DEPOSIT:
					ShopKeepers.deposit(mob,commands);
					break;
				case CommandSet.DESCRIPTION:
					BasicSenses.description(mob,commands);
					break;
				case CommandSet.DESTROY:
					if(mob.isASysOp(mob.location()))
						CreateEdit.destroy(mob,commands);
					else
						mob.tell("You lack the power to destroy things this way.  Did you mean kill?\n\r");
					break;
				case CommandSet.DISMOUNT:
					Movement.dismount(mob,commands);
					break;
				case CommandSet.DOWN:
					Movement.standAndGo(mob,Directions.DOWN);
					break;
				case CommandSet.DRAW:
					TheFight.draw(mob,commands,false,false);
					break;
				case CommandSet.DRESS:
					Grouping.dress(mob,commands);
					break;
				case CommandSet.DRINK:
					ItemUsage.drink(mob,commands);
					break;
				case CommandSet.DROP:
					ItemUsage.drop(mob,commands);
					break;
				case CommandSet.DUMPFILE:
					if(mob.isASysOp(null))
						cmdDumpfile(mob,commands);
					else
						mob.tell("Huh?\n\r");
					break;
				case CommandSet.EAST:
					Movement.standAndGo(mob,Directions.EAST);
					break;
				case CommandSet.EAT:
					ItemUsage.eat(mob,commands);
					break;
				case CommandSet.EMAIL:
					Scoring.email(mob,commands,false);
					break;
				case CommandSet.EMOTE:
					BasicSenses.emote(mob,commands);
					break;
				case CommandSet.ENTER:
					Movement.enter(mob,commands);
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
				case CommandSet.EXPORT:
					if(mob.isASysOp(null))
						Import.export(mob,commands);
					else
						mob.tell("Only the Archons may Export.\n\r");
					break;
				case CommandSet.FEED:
					Grouping.feed(mob,commands);
					break;
				case CommandSet.FILL:
					ItemUsage.fill(mob,commands);
					break;
				case CommandSet.FIRE:
					SocialProcessor.fire(mob,Util.combine(commands,1));
					break;
				case CommandSet.FLEE:
					Movement.flee(mob,Util.combine(commands,1));
					break;
				case CommandSet.FOLLOW:
					Grouping.follow(mob,commands);
					break;
				case CommandSet.GAIN:
					AbilityEvoker.gain(mob,commands);
					break;
				case CommandSet.GENCHAR:
					if(mob.isASysOp(mob.location()))
						SysOpSkills.chargen(mob,commands);
					else
						mob.tell("Sorry, you don't have that power!");
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
				case CommandSet.GODS:
					Scoring.gods(mob);
					break;
				case CommandSet.GOTO:
					SysOpSkills.gotoCmd(mob,commands);
					break;
				case CommandSet.GROUP:
					Grouping.group(mob);
					break;
				case CommandSet.GTELL:
					Grouping.gtell(mob,Util.combine(commands,1));
					break;
				case CommandSet.HELP:
					Help.help(mob,Util.combine(commands,1));
					break;
				case CommandSet.HIRE:
					SocialProcessor.hire(mob,Util.combine(commands,1));
					break;
				case CommandSet.HOLD:
					ItemUsage.hold(mob,commands);
					break;
				case CommandSet.I3:
					if(!(ExternalPlay.i3().i3online()))
						mob.tell("I3 is unavailable.");
					else
						SysOpSkills.i3(mob,commands);
					break;
				case CommandSet.I3LOCATE:
					if(!(ExternalPlay.i3().i3online()))
						mob.tell("I3 is unavailable.");
					else
						ExternalPlay.i3().i3locate(mob,Util.combine(commands,1));
					break;
				case CommandSet.IDEA:
					if(Util.combine(commands,1).length()>0)
					{
						ExternalPlay.DBWriteJournal("SYSTEM_IDEAS",mob.Name(),"ALL","IDEA",Util.combine(commands,1),-1);
						mob.tell("Thank you for your contribution!");
					}
					else
						mob.tell("What's your idea?");
					break;
				case CommandSet.IMPORT:
					if(mob.isASysOp(null))
						Import.areimport(mob,commands);
					else
						mob.tell("Only the Archons may Import.\n\r");
					break;
				case CommandSet.INVENTORY:
					Scoring.inventory(mob);
					break;
				case CommandSet.KILL:
					TheFight.kill(mob,commands);
					break;
				case CommandSet.LANGUAGES:
					Scoring.languages(mob);
					break;
				case CommandSet.LIST:
					ShopKeepers.list(mob,commands);
					break;
				case CommandSet.LINK:
					if(mob.isASysOp(mob.location()))
						CreateEdit.link(mob,commands);
					else
						mob.tell("You lack the power to link rooms.\n\r");
					break;
				case CommandSet.LOAD:
					if(mob.isASysOp(null))
						SysOpSkills.load(mob,commands);
					else
						mob.tell("Only the Archons may load stuff!\n\r");
					break;
				case CommandSet.LOCK:
					Movement.lock(mob,Util.combine(commands,1));
					break;
				case CommandSet.LOOK:
					BasicSenses.look(mob,commands,false);
					break;
				case CommandSet.MERGE:
					if(mob.isASysOp(null))
						Import.merge(mob,commands);
					else
						mob.tell("You lack that power.\n\r");
					break;
				case CommandSet.MODIFY:
					if(mob.isASysOp(mob.location()))
						CreateEdit.edit(mob,commands);
					else
						mob.tell("You lack the power to modify things.\n\r");
					break;
				case CommandSet.MOUNT:
					Movement.mount(mob,commands);
					break;
				case CommandSet.NOCOLOR:
					BasicSenses.ansi(mob,0);
					break;
				case CommandSet.NOFOLLOW:
					Grouping.togglenofollow(mob);
					break;
				case CommandSet.NORTH:
					Movement.standAndGo(mob,Directions.NORTH);
					break;
				case CommandSet.NOCHANNEL:
					Channels.nochannel(mob,commands);
					break;
				case CommandSet.OPEN:
					Movement.open(mob,Util.combine(commands,1));
					break;
				case CommandSet.ORDER:
					Grouping.order(mob,commands);
					break;
				case CommandSet.OUTFIT:
					BasicSenses.outfit(mob);
					break;
				case CommandSet.PASSWORD:
					BasicSenses.password(mob,commands);
					break;
				case CommandSet.PLAYERKILL:
					TheFight.playerkill(mob);
					break;
				case CommandSet.POSSESS:
					if(mob.isASysOp(mob.location()))
						SysOpSkills.possess(mob,commands);
					else
						mob.tell("You aren't powerful enough to possess anyone.");
					break;
				case CommandSet.PRACTICE:
					AbilityEvoker.practice(mob,commands);
					break;
				case CommandSet.PRAYERS:
					Scoring.prayers(mob);
					break;
				case CommandSet.PREVIOUS_CMD:
					if(!mob.isMonster())
						doCommand(mob,Util.copyVector(mob.session().previousCMD()));
					break;
				case CommandSet.PROMPT:
					Scoring.prompt(mob,commands);
					break;
				case CommandSet.PULL:
					ItemUsage.pull(mob,Util.combine(commands,1));
					break;
				case CommandSet.PUSH:
					ItemUsage.push(mob,Util.combine(commands,1),commandSet);
					break;
				case CommandSet.PUT:
					ItemUsage.put(mob,commands);
					break;
				case CommandSet.QUALIFY:
					Scoring.qualify(mob,commands);
					break;
				case CommandSet.QUIET:
					Channels.quiet(mob);
					break;
				case CommandSet.QUIT:
					if(mob.soulMate()!=null)
						SysOpSkills.dispossess(mob);
					else
					if(!mob.isMonster())
						mob.session().cmdExit(mob,commands);
					break;
				case CommandSet.READ:
					ItemUsage.read(mob,commands);
					break;
				case CommandSet.REBUKE:
					SocialProcessor.rebuke(mob,commands);
					break;
				case CommandSet.REMOVE:
					ItemUsage.remove(mob,commands);
					break;
				case CommandSet.REPLY:
					SocialProcessor.reply(mob,commands);
					break;
				case CommandSet.REPORT:
					SocialProcessor.report(mob);
					break;
				case CommandSet.RESET:
					if(mob.isASysOp(mob.location()))
						Reset.resetSomething(mob,commands);
					else
						mob.tell("You are not powerful enough.\n\r");
					break;
				case CommandSet.RETIRE:
					Scoring.retire(mob);
					break;
				case CommandSet.SAVE:
					if(mob.isASysOp(mob.location()))
						CreateEdit.save(mob,commands);
					else
						mob.tell("Your game is automatically being saved while you play.\n\r");
					break;
				case CommandSet.SAY:
					SocialProcessor.say(mob,commands);
					break;
				case CommandSet.SHUTDOWN:
					if(mob.isASysOp(null))
						shutdown(mob, commands);
					else
						mob.tell("You are not powerful enough.\n\r");
					break;
				case CommandSet.SCORE:
					Scoring.score(mob);
					break;
				case CommandSet.SELL:
					ShopKeepers.sell(mob,commands);
					break;
				case CommandSet.SERVE:
					SocialProcessor.serve(mob,commands);
					break;
				case CommandSet.SHEATH:
					TheFight.sheath(mob,commands);
					break;
				case CommandSet.SIT:
					Movement.sit(mob,commands);
					break;
				case CommandSet.SKILLS:
					Scoring.skills(mob);
					break;
				case CommandSet.SLEEP:
					Movement.sleep(mob,commands);
					break;
				case CommandSet.SNOOP:
					if(mob.isASysOp(mob.location()))
						SysOpSkills.snoop(mob, commands);
					else
						mob.tell("Mind your own business!\n\r");
					break;
				case CommandSet.SOCIALS:
					Scoring.socials(mob);
					break;
				case CommandSet.SONGS:
					Scoring.songs(mob);
					break;
				case CommandSet.SOUTH:
					Movement.standAndGo(mob,Directions.SOUTH);
					break;
				case CommandSet.SPELLS:
					Scoring.spells(mob,commands);
					break;
				case CommandSet.SPLIT:
					Grouping.split(mob,commands);
					break;
				case CommandSet.STAND:
					Movement.stand(mob);
					break;
				case CommandSet.SYSMSGS:
					if(mob.isASysOp(mob.location()))
						SysOpSkills.toggleSysopMsgs(mob);
					break;
				case CommandSet.TAKE:
					if(mob.isASysOp(mob.location()))
						SysopItemUsage.take(mob,commands);
					else
						BasicSenses.mundaneTake(mob,commands);
					break;
				case CommandSet.THROW:
					TheFight.throwit(mob,commands);
					break;
				case CommandSet.TELL:
					SocialProcessor.tell(mob,commands);
					break;
				case CommandSet.TEACH:
					AbilityEvoker.teach(mob,commands);
					break;
				case CommandSet.TICKTOCK:
					if(mob.isASysOp(null))
						SysOpSkills.ticktock(mob,commands);
					else
						mob.tell("Huh?\n\r");
					break;
				case CommandSet.TIME:
					BasicSenses.time(mob,commands);
					break;
				case CommandSet.TOPICS:
					Help.topics(mob);
					break;
				case CommandSet.TRAIN:
					BasicSenses.train(mob,commands);
					break;
				case CommandSet.TYPO:
					if(Util.combine(commands,1).length()>0)
					{
						ExternalPlay.DBWriteJournal("SYSTEM_TYPOS",mob.Name(),"ALL","TYPOS",Util.combine(commands,1),-1);
						mob.tell("Thank you for your assistance!");
					}
					else
						mob.tell("What's the typo?");
					break;
				case CommandSet.UNLOCK:
					Movement.unlock(mob,Util.combine(commands,1));
					break;
				case CommandSet.UNLINK:
					if(mob.isASysOp(mob.location()))
						CreateEdit.destroy(mob,commands);
					else
						mob.tell("You lack the power to link rooms.\n\r");
					break;
				case CommandSet.UNLOAD:
					if(mob.isASysOp(null))
						SysOpSkills.unload(mob,commands);
					else
						mob.tell("Only the Archons may unload stuff!\n\r");
					break;
				case CommandSet.UNDRESS:
					Grouping.undress(mob,commands);
					break;
				case CommandSet.UP:
					Movement.standAndGo(mob,Directions.UP);
					break;
				case CommandSet.VALUE:
					ShopKeepers.value(mob,commands);
					break;
				case CommandSet.VASSALS:
					SocialProcessor.vassals(mob,commands);
					break;
				case CommandSet.VER:
					mob.tell(myHost.getVer());
					mob.tell("(C) 2000-2003 Bo Zimmerman");
					mob.tell("bo@zimmers.net");
					mob.tell("http://www.zimmers.net/home/mud.html");
					break;
				case CommandSet.VIEW:
					ShopKeepers.view(mob,commands);
					break;
				case CommandSet.WAKE:
					Movement.wake(mob,commands);
					break;
				case CommandSet.WEAR:
					ItemUsage.wear(mob,commands);
					break;
				case CommandSet.WEATHER:
					BasicSenses.weather(mob,commands);
					break;
				case CommandSet.WEST:
					Movement.standAndGo(mob,Directions.WEST);
					break;
				case CommandSet.WHERE:
					if(mob.isASysOp(mob.location()))
						Lister.where(mob,commands);
					else
						mob.tell("Only the Archons may divine that.");
					break;
				case CommandSet.WHISPER:
					SocialProcessor.whisper(mob,commands);
					break;
				case CommandSet.WHOIS:
					Grouping.whois(mob,Util.combine(commands,1));
					break;
				case CommandSet.WHO:
					Grouping.who(mob,Util.combine(commands,1));
					break;
				case CommandSet.WIELD:
					ItemUsage.wield(mob,commands);
					break;
				case CommandSet.WIMPY:
					BasicSenses.wimpy(mob,commands);
					break;
				case CommandSet.WITHDRAW:
					ShopKeepers.withdraw(mob,commands);
					break;
				case CommandSet.WIZINV:
					if(mob.isASysOp(null))
						SysOpSkills.wizinv(mob,commands);
					else
						mob.tell("You aren't powerful enough to do that.");
					break;
				case CommandSet.XML:
					if(mob.isASysOp(null))
						XMLIO.xml(mob,commands);
					else
						mob.tell("You are not powerful enough.\n\r");
					break;
				case CommandSet.YELL:
					SocialProcessor.yell(mob,commands);
					break;
				}
			}
		}
		else
		{
			Social social=Socials.FetchSocial(commands);
			if(social!=null)
				social.invoke(mob,commands,null,false);
			else
				mob.tell("Huh?\n\r");
		}
	}

	public static void credits(MOB mob)
	{
		StringBuffer credits=Resources.getFileResource("text"+File.separatorChar+"credits.txt");

		if((credits!=null)&&(mob.session()!=null))
			mob.session().rawPrintln(credits.toString());
		return;
	}

	public static void shutdown(MOB mob, Vector commands)
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
			Log.errOut("CommandProcessor",mob.Name()+" starts system shutdown...");
		else
		if(externalCommand!=null)
			Log.errOut("CommandProcessor",mob.Name()+" starts system restarting '"+externalCommand+"'...");
		else
			Log.errOut("CommandProcessor",mob.Name()+" starts system restart...");
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_SHUTDOWN,null);
		for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
		{
			Room R=(Room)r.nextElement();
			R.send(mob,msg);
		}
		if(myHost!=null)
			myHost.shutdown(mob.session(),keepItDown,externalCommand);
		else
			Log.errOut("CommandProcessor","Shutdown failed.  No host.");
	}

	public static void cmdDumpfile(MOB mob, Vector commands)
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
