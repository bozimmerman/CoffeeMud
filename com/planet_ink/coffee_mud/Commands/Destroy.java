package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.MQLException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.UpdateSet;
import com.planet_ink.coffee_mud.Libraries.interfaces.HelpLibrary.HelpSection;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.CommandJournalFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/*
   Copyright 2004-2025 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Destroy extends StdCommand
{
	public Destroy()
	{
	}

	private final String[] access=I(new String[]{"DESTROY","JUNK","TEAR"});

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public boolean errorOut(final MOB mob)
	{
		mob.tell(L("You are not allowed to do that here."));
		return false;
	}

	public String listOfThings()
	{
		return L("EXIT, ITEM, AREA, USER, MOB, QUEST, FACTION, "
			+ "SESSION, TICKS, THREAD, HOLIDAY, JOURNAL, SOCIAL, ACHIEVEMENT, CLASS, ABILITY, MANUFACTURER, "
			+ "LANGUAGE, COMPONENT, RACE, EXPERTISE, TITLE, CLAN, BAN, GOVERNMENT, NOPURGE, BUG, TYPO, IDEA, "
			+ "WEBSERVER, POLL, DEBUGFLAG, DISABLEFLAG, ENABLEFLAG, CRAFTSKILL, GATHERSKILL, CRON, COMMAND, "
			+ "TRAP, WRIGHTSKILL, AWARD, DELETE:, or a ROOM");
	}

	public void awards(final MOB mob, final List<String> commands)
	{
		if((commands.size()<3)
		||(!CMath.isInteger(commands.get(2)))
		||(CMath.s_int(commands.get(2))<1))
		{
			mob.tell(L("You have failed to specify the proper fields.\n\r"
					+ "DESTROY AWARD [NUM].\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final int num = CMath.s_int(commands.get(2));
		if(CMLib.awards().modifyAutoProperty(num, null))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The superstition of the players just decreased!"));
		else
		{
			mob.tell(L("@x1 is not a proper entry.  Try LIST AWARD.\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
	}

	public boolean mobs(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY MOB [MOB NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}

		String mobID=CMParms.combine(commands,2);
		boolean allFlag=commands.get(2).equalsIgnoreCase("all");
		if(mobID.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			mobID="ALL "+mobID.substring(4);
		}
		if(mobID.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			mobID="ALL "+mobID.substring(0,mobID.length()-4);
		}
		MOB deadMOB=mob.location().fetchInhabitant(mobID);
		boolean doneSomething=false;
		while(deadMOB!=null)
		{
			if(!deadMOB.isMonster())
			{
				mob.tell(L("@x1 is a PLAYER!!\n\r",deadMOB.name()));
				if(!doneSomething)
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return false;
			}
			doneSomething=true;
			mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("@x1 vanishes in a puff of smoke.",deadMOB.name()));
			Log.sysOut("Mobs",mob.Name()+" destroyed mob "+deadMOB.Name()+".");
			deadMOB.destroy();
			mob.location().delInhabitant(deadMOB);
			deadMOB=mob.location().fetchInhabitant(mobID);
			if(!allFlag)
				break;
		}
		if(!doneSomething)
		{
			mob.tell(L("I don't see '@x1 here.\n\r",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}
		return true;
	}

	public void manufacturer(final MOB mob, final List<String> commands) throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY MANUFACTURER [NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final String manufacturerID=CMParms.combine(commands,2);

		final Manufacturer manufacturer=CMLib.tech().getManufacturer(manufacturerID);
		if((manufacturer==null)||(manufacturer==CMLib.tech().getDefaultManufacturer()))
		{
			mob.tell(L("There's no manufacturer called '@x1' Try LIST MANUFACTURERS.\n\r",manufacturerID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		if(mob.session().confirm(L("This will complete OBLITERATE the manufacturer '@x1' forever.  "
				+ "This means all the stuff that is made by this manufacturer will get transferred to ACME. "
				+ "Are you SURE?! (y/N)?",manufacturerID),"N"))
		{
			CMLib.tech().delManufacturer(manufacturer);
			mob.location().recoverRoomStats();
			Log.sysOut(mob.Name()+" destroyed manufacturer "+manufacturer.name()+".");
		}
	}

	public void cron(final MOB mob, final List<String> commands) throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY CRON [NAME/#]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final String cronID=CMParms.combine(commands,2);
		final List<JournalEntry> jobs = CMLib.database().DBReadJournalMsgsByCreateDate("SYSTEM_CRON", true);
		JournalEntry killMe=null;
		if(CMath.isInteger(cronID)
		&&(CMath.s_int(cronID)>0)
		&&(CMath.s_int(cronID)<=jobs.size()))
			killMe=jobs.get(CMath.s_int(cronID)-1);
		else
		{
			for(final JournalEntry E : jobs)
			{
				if(E.subj().equalsIgnoreCase(cronID))
					killMe=E;
			}
			if(killMe == null)
			{
				for(final JournalEntry E : jobs)
				{
					if(E.subj().toUpperCase().indexOf(cronID.toUpperCase())>=0)
					{
						killMe=E;
						break;
					}
				}
			}
			if(killMe == null)
			{
				mob.tell(L("@x1 is not a valid [NAME/#].  Try LIST CRON.\n\r",cronID));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return;
			}
		}
		if(mob.session().confirm(L("This will complete OBLITERATE the cron job '@x1' forever.  "
				+ "Are you SURE?! (y/N)?",killMe.subj()),"N"))
		{
			CMLib.database().DBDeleteJournal("SYSTEM_CRON", killMe.key());
			Log.sysOut(mob.Name()+" destroyed cron job "+killMe.subj()+".");
			CMLib.journals().activate();
		}
	}

	public void accounts(final MOB mob, final List<String> commands)
	throws IOException
	{
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around the heavens."));
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY ACCOUNT ([NAME])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		final String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
		final PlayerAccount theAccount = CMLib.players().getLoadAccount(accountName);
		if(theAccount==null)
		{
			mob.tell(L("There is no account called '@x1'!\n\r",accountName));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		final String playerList = CMParms.toListString(theAccount.getPlayers());
		if(mob.session().confirm(L("This will complete OBLITERATE the account '@x1' and players '@x2' forever.  Are you SURE?! (y/N)?",theAccount.getAccountName(),playerList),"N"))
		{
			for(final Enumeration<String> p=theAccount.getPlayers();p.hasMoreElements();)
			{
				final MOB deadMOB=CMLib.players().getLoadPlayer(p.nextElement());
				CMLib.players().obliteratePlayer(deadMOB,true,CMSecurity.isDisabled(CMSecurity.DisFlag.DEATHCRY));
				mob.tell(L("The user '@x1' is no more!\n\r",CMParms.combine(commands,2)));
				Log.sysOut("Mobs",mob.Name()+" destroyed user "+deadMOB.Name()+".");
				deadMOB.destroy();
			}
			CMLib.players().obliterateAccountOnly(theAccount);
			mob.location().recoverRoomStats();
			Log.sysOut("Destroy",mob.Name()+" destroyed account "+theAccount.getAccountName()+" and players '"+playerList+"'.");
		}
	}

	public boolean players(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY USER [USER NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}

		final String name=CMStrings.capitalizeAndLower(CMParms.combine(commands,2));
		final boolean found=CMLib.players().playerExists(name);

		if(!found)
		{
			mob.tell(L("The user '@x1' does not exist!\n\r",CMParms.combine(commands,2)));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return false;
		}

		if(mob.session().confirm(L("This will complete OBLITERATE the user '@x1' forever.  Are you SURE?! (y/N)?",name),"N"))
		{
			final MOB deadMOB=CMLib.players().getLoadPlayer(name);
			CMLib.players().obliteratePlayer(deadMOB,true,CMSecurity.isDisabled(CMSecurity.DisFlag.DEATHCRY));
			mob.tell(L("The user '@x1' is no more!\n\r",CMParms.combine(commands,2)));
			Log.sysOut("Mobs",mob.Name()+" destroyed user "+deadMOB.Name()+".");
			deadMOB.destroy();
			return true;
		}
		return true;
	}

	public Thread findThreadGroup(final String threadName,final ThreadGroup tGroup)
	{
		final int ac = tGroup.activeCount();
		final int agc = tGroup.activeGroupCount();
		final Thread tArray[] = new Thread [ac+1];
		final ThreadGroup tgArray[] = new ThreadGroup [agc+1];

		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);

		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null)
			{
				if(tArray[i].getName().equalsIgnoreCase(threadName))
					return tArray[i];
				final Runnable R=CMLib.threads().findRunnableByThread(tArray[i]);
				if(R instanceof TickableGroup)
				{
					if(((TickableGroup)R).getName().equalsIgnoreCase(threadName))
						return tArray[i];
					final TickClient T=((TickableGroup)R).getLastTicked();
					if((T!=null)&&(T.getName().equalsIgnoreCase(threadName)))
						return tArray[i];
				}
			}
		}

		if (agc > 0)
		{
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
				{
					final Thread t=findThreadGroup(threadName,tgArray[i]);
					if(t!=null)
						return t;
				}
			}
		}
		return null;
	}

	public Thread findThread(final String threadName)
	{
		Thread t=null;
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				t=findThreadGroup(threadName,topTG);

		}
		catch (final Exception e)
		{
		}
		return t;

	}

	public void rooms(final MOB mob, final List<String> commands)
		throws IOException
	{
		final String thecmd=commands.get(0).toLowerCase();
		if(commands.size()<3)
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is UNLINK (N,S,E,W,U, or D)\n\r"));
			else
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY ROOM ([DIRECTION],[ROOM ID])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		boolean confirmed=false;
		if((commands.size()>3))
		{
			if(commands.get(commands.size()-1).equalsIgnoreCase("CONFIRMED"))
			{
				commands.remove(commands.size()-1);
				confirmed=true;
			}
		}
		final String roomdir=CMParms.combine(commands,2);
		final int direction=CMLib.directions().getGoodDirectionCode(roomdir);
		Room deadRoom=null;
		if(!thecmd.equalsIgnoreCase("UNLINK"))
			deadRoom=CMLib.map().getRoom(roomdir);
		if((deadRoom==null)&&(direction<0))
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell(L("You have failed to specify a direction.  Try (@x1).\n\r",Directions.LETTERS()));
			else
				mob.tell(L("You have failed to specify a direction.  Try a VALID ROOM ID, or (@x1).\n\r",Directions.LETTERS()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		else
		if(mob.isMonster())
		{
			mob.tell(L("Sorry Charlie!"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;

		}
		if(deadRoom!=null)
		{
			if(!CMSecurity.isAllowed(mob,deadRoom,CMSecurity.SecFlag.CMDROOMS))
			{
				mob.tell(L("Sorry Charlie! Not your room!"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
			if(mob.location()==deadRoom)
			{
				mob.tell(L("You dip! You have to leave this room first!"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}

			if(!confirmed)
			{
				if(!mob.session().confirm(L("You are fixing to permanantly destroy Room \"@x1\".  Are you ABSOLUTELY SURE (y/N)",deadRoom.roomID()),"N"))
					return;
			}
			CMLib.map().obliterateMapRoom(deadRoom);
			mob.tell(L("The sound of massive destruction rings in your ears."));
			mob.location().showOthers(mob,null,CMMsg.MSG_NOISE,L("The sound of massive destruction rings in your ears."));
			Log.sysOut("Rooms",mob.Name()+" destroyed room "+deadRoom.roomID()+".");
		}
		else
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS))
			{
				errorOut(mob);
				return;
			}
			Room unRoom=mob.location().rawDoors()[direction];
			if((unRoom!=null)&&(unRoom.getGridParent()!=null))
				unRoom=unRoom.getGridParent();
			if((mob.location().getGridParent()!=null)
			&&(!(mob.location() instanceof GridLocale)))
			{
				final GridLocale GL=mob.location().getGridParent();
				final int myX=GL.getGridChildX(mob.location());
				final int myY=GL.getGridChildY(mob.location());
				for(final Iterator<GridLocale.CrossExit> i=GL.outerExits();i.hasNext();)
				{
					final GridLocale.CrossExit CE=i.next();
					if((CE.out)
					&&(CE.x==myX)
					&&(CE.y==myY)
					&&(CE.dir==direction))
					   GL.delOuterExit(CE);
				}
				CMLib.database().DBUpdateExits(GL);
				mob.location().rawDoors()[direction]=null;
				mob.location().setRawExit(direction,null);
			}
			else
			{
				mob.location().rawDoors()[direction]=null;
				mob.location().setRawExit(direction,null);
				CMLib.database().DBUpdateExits(mob.location());
			}
			if(unRoom instanceof GridLocale)
			{
				final GridLocale GL=(GridLocale)unRoom;
				for(final Iterator<GridLocale.CrossExit> i=GL.outerExits();i.hasNext();)
				{
					final GridLocale.CrossExit CE=i.next();
					if((!CE.out)
					&&(CE.dir==direction)
					&&(CE.destRoomID.equalsIgnoreCase(CMLib.map().getExtendedRoomID(mob.location()))))
					   GL.delOuterExit(CE);
				}
				CMLib.database().DBUpdateExits(GL);
			}
			mob.location().getArea().fillInAreaRoom(mob.location());
			final Directions.DirType dirType = CMLib.flags().getInDirType(mob);
			final String inDirName=CMLib.directions().getDirectionName(direction, dirType);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A wall of inhibition falls @x1.",inDirName));
			final String dirName=CMLib.directions().getDirectionName(direction, dirType);
			Log.sysOut("Rooms",mob.Name()+" unlinked direction "+dirName+" from room "+mob.location().roomID()+".");
		}
	}

	public void exits(final MOB mob, final List<String> commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(L("This command is invalid from within a GridLocaleChild room."));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY EXIT [DIRECTION]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final int direction=CMLib.directions().getGoodDirectionCode((commands.get(2)));
		if(direction<0)
		{
			mob.tell(L("You have failed to specify a direction.  Try @x1.\n\r",Directions.LETTERS()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(mob.isMonster())
		{
			mob.tell(L("Sorry Charlie!"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;

		}
		mob.location().setRawExit(direction,null);
		CMLib.database().DBUpdateExits(mob.location());
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		final Directions.DirType dirType=CMLib.flags().getInDirType(mob);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A wall of inhibition falls @x1.",
				(CMLib.directions().getInDirectionName(direction, dirType))));
		Log.sysOut("Exits",mob.location().roomID()+" exits destroyed by "+mob.Name()+".");
	}

	public boolean helps(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY HELP [KEY]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		final boolean preferAHelp = commands.get(1).toUpperCase().startsWith("A");
		final String helpStr=CMParms.combine(commands,2).toUpperCase().trim().replace(' ','_');
		final HelpSection section = preferAHelp?HelpSection.ArchonOnly:HelpSection.NormalOnly;
		final String fileName = CMLib.help().findHelpFile(helpStr, section, true);
		if(fileName == null)
		{
			mob.tell(L("A help file with key '@x1' does not exist!",helpStr));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!CMLib.help().addModifyHelpEntry(mob, fileName, helpStr, true))
		{
			mob.tell(L("A help file with key '@x1' could not be destroyed!",helpStr));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The helpfullness of the world just decreased."));
		return true;
	}

	private Item getItem(final boolean allFlag, final Room srchRoom, final Item srchContainer, final MOB srchMob, final String itemID)
	{
		Item deadItem=null;
		deadItem=(srchRoom==null)?null:srchRoom.findItem(srchContainer,itemID);
		if((!allFlag)&&(deadItem==null))
			deadItem=(srchMob==null)?null:srchMob.findItem(null,itemID);
		if(deadItem==null)
		{
			Environmental E=CMLib.space().findSpaceObject(itemID,true);
			if(!(E instanceof Item))
				E=CMLib.space().findSpaceObject(itemID,false);
			if(E instanceof Item)
				deadItem=(Item)E;
		}
		return deadItem;
	}

	public boolean items(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY ITEM [ITEM NAME](@ room/[MOB NAME])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		String itemID=CMParms.combine(commands,2);
		MOB srchMob=mob;
		Item srchContainer=null;
		Room srchRoom=mob.location();
		final int x=itemID.indexOf('@');
		if(x>0)
		{
			final String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				final MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					final Item I = srchRoom.findItem(null, rest);
					if(I instanceof Container)
						srchContainer=I;
					else
					{
						mob.tell(L("MOB or Container '@x1' not found.",rest));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
						return false;
					}
				}
				else
				{
					srchMob=M;
					srchRoom=null;
				}
			}
		}

		int max=Integer.MAX_VALUE;
		boolean allFlag=commands.get(2).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			itemID="ALL "+itemID.substring(4);
		}
		if(itemID.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			itemID="ALL "+itemID.substring(0,itemID.length()-4);
		}
		boolean doneSomething=false;
		Item deadItem=getItem(allFlag,srchRoom,srchContainer,srchMob,itemID);

		final int spaceDex=itemID.indexOf(' ');
		if((deadItem == null)
		&&(spaceDex>0)
		&&(CMath.isInteger(itemID.substring(0,spaceDex))))
		{
			max=CMath.s_int(itemID.substring(0,spaceDex));
			itemID=itemID.substring(spaceDex+1).trim();
			deadItem=getItem(allFlag,srchRoom,srchContainer,srchMob,itemID);
			if(max>1)
				allFlag=true;
		}
		int num=0;
		while((deadItem!=null)&&(max-->0))
		{
			num++;
			if((num==100)||(num==1000)||(num==10000)||(num==100000)||(num==1000000))
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("Things are disintegrating..."));
			else
			if((num<100)
			||((num<1000)&&((num%100)==0))
			||((num<10000)&&((num%1000)==0))
			||((num<100000)&&((num%10000)==0))
			||((num<1000000)&&((num%100000)==0)))
			{
				mob.location().recoverRoomStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 disintegrates!",deadItem.name()));
				Log.sysOut("Items",mob.Name()+" destroyed item "+deadItem.name()+".");
			}
			doneSomething=true;
			if(deadItem instanceof SpaceObject)
			{
				CMLib.database().DBDeleteItem("SPACE", deadItem);
				deadItem.destroy();
				CMLib.space().delObjectInSpace((SpaceObject)deadItem);
				deadItem=null;
			}
			else
			{
				//if(srchMob!=null)
				//	deadItem.setOwner(srchMob);
				//else
				//if(srchRoom!=null)
				//	deadItem.setOwner(srchRoom);
				deadItem.destroy();
				if(srchMob!=null)
					srchMob.delItem(deadItem);
				mob.location().delItem(deadItem);
				deadItem=null;
				if(!allFlag)
					deadItem=(srchMob==null)?null:srchMob.findItem(null,itemID);
				if(deadItem==null)
					deadItem=(srchRoom==null)?null:srchRoom.findItem(null,itemID);
			}
			if(!allFlag)
				break;
		}
		if(num>=100)
			Log.sysOut("Items",mob.Name()+" destroyed "+num+" items called '"+itemID+"'.");
		mob.location().recoverRoomStats();
		if(!doneSomething)
		{
			if(mob.findItem(itemID)!=null)
				mob.tell(L("I don't see '@x1 here. You might need to drop it first.\n\r",itemID));
			else
				mob.tell(L("I don't see '@x1 here.\n\r",itemID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		return true;
	}

	public void areas(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY AREA [AREA NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a thunderous spell."));
			return;
		}
		boolean confirmed=false;
		if((commands.size()>3))
		{
			if(commands.get(commands.size()-1).equalsIgnoreCase("CONFIRMED"))
			{
				commands.remove(commands.size()-1);
				confirmed=true;
			}
		}
		final List<String> areaNames=new LinkedList<String>();
		areaNames.add(CMParms.combine(commands,2));
		if((commands.size()>4))
		{
			if(commands.get(2).equalsIgnoreCase("all"))
			{
				areaNames.clear();
				for(final Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
					areaNames.add(a.nextElement().Name());
				if(commands.get(3).equalsIgnoreCase("except"))
				{
					for(int i=4;i<commands.size();i++)
					{
						final Area A=CMLib.map().getArea(commands.get(i));
						if(A==null)
						{
							mob.tell(L("There is no such area as '@x1'",(commands.get(i))));
							mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a thunderous spell."));
							return;
						}
						areaNames.remove(A.Name());
					}
				}
			}
		}

		for(final String areaName : areaNames)
		{
			if(CMLib.map().getArea(areaName)==null)
			{
				mob.tell(L("There is no such area as '@x1'",areaName));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a thunderous spell."));
				return;
			}
			final Area A=CMLib.map().getArea(areaName);
			final Room R=A.getRandomProperRoom();
			if((R!=null)&&(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.CMDAREAS)))
			{
				errorOut(mob);
				return;
			}

			if(!confirmed)
			{
				if(mob.session().confirm(L("Area: \"@x1\", OBLITERATE IT???",areaName),"N"))
				{
					if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
					{
						mob.tell(L("You dip!  You are IN that area!  Leave it first..."));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a thunderous spell."));
						return;
					}
					confirmed=true;
				}
			}
			if(confirmed)
			{
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A thunderous boom of destruction is heard in the distance."));
				Log.sysOut("Rooms",mob.Name()+" destroyed area "+areaName+".");
				CMLib.map().obliterateMapArea(A);
			}
		}
	}

	public boolean races(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY RACE [RACE ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String raceID=CMParms.combine(commands,2);
		final Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell(L("'@x1' is an invalid race id.",raceID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell(L("'@x1' is not generic, and may not be deleted.",R.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		final String oldRID=R.ID();
		CMClass.delRace(R);
		CMLib.database().DBDeleteRace(R.ID());
		CMClass.loadClass(CMObjectType.RACE,"com/planet_ink/coffee_mud/Races/"+oldRID+".class",true);
		Race oldR=CMClass.getRace(oldRID);
		if(oldR==null)
			oldR=CMClass.getRace("StdRace");
		CMLib.utensils().swapRaces(oldR,R);
		if(!oldR.ID().equals("StdRace"))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The diversity of the world just changed?!"));
		else
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The diversity of the world just decreased!"));
		return true;
	}

	public boolean commands(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY COMMAND [COMMAND ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String commandID=CMParms.combine(commands,2);
		Command C=CMClass.getCommand(commandID);
		if(C==null)
		{
			mob.tell(L("'@x1' is an invalid command id.",commandID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell(L("'@x1' is not generic, and may not be deleted.",C.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		CMClass.delClass(CMObjectType.COMMAND, C);
		final DatabaseEngine.AckRecord rec = CMLib.database().DBDeleteCommand(C.ID());
		if((rec != null) && (rec.typeClass() != null) && (rec.typeClass().length() > 0))
		{
			try
			{
				final Class<?> classC = Class.forName(rec.typeClass(), true, CMClass.instance());
				C = (Command)classC.newInstance();
				CMClass.addClass(CMObjectType.COMMAND, C);
				CMClass.reloadCommandWords();
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The power of the world just changed?!"));
				return true;
			}
			catch (final Exception e)
			{
			}
		}
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The power of the world just decreased!"));
		CMClass.reloadCommandWords();
		return true;
	}

	public boolean components(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\r"
					+ "The format: DESTROY COMPONENT [SKILL ID]\n\r"
					+ "The format: DESTROY COMPONENT SOCIAL [SOCIAL ID]\n\r"
					));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(commands.get(2).equalsIgnoreCase("SOCIAL"))
		{
			final String classID=CMParms.combine(commands,3);
			final List<Social> socials=CMLib.ableComponents().getSocialsSet(classID);
			Social soc=null;
			if((socials!=null)&&(socials.size()>0))
			{
				final Map<String,List<Social>> allsoc = new HashMap<String,List<Social>>();
				allsoc.put(socials.get(0).baseName(), socials);
				soc = CMLib.socials().fetchSocialFromSet(allsoc, CMParms.parse(classID), true, true);
			}
			if(soc == null)
			{
				mob.tell(L("'@x1' does not exist, try LIST COMPONENTS.",classID));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return false;
			}
			CMLib.ableComponents().alterAbilityComponentFile(CMStrings.trimCRLF(soc.getEncodedLine()),true);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The complication of skill usage just decreased!"));
			return true;
		}
		final String classID=CMParms.combine(commands,2);
		if(CMLib.ableComponents().getAbilityComponentMap().get(classID.toUpperCase())==null)
		{
			mob.tell(L("'@x1' does not exist, try LIST COMPONENTS.",classID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		CMLib.ableComponents().alterAbilityComponentFile(classID,true);
		CMLib.ableComponents().getAbilityComponentMap().remove(classID.toUpperCase());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The complication of skill usage just decreased!"));
		return true;
	}

	public boolean plane(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY PLANE [PLANE NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String planeName=CMParms.combine(commands,2);
		final PlanarAbility planeSet = (PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		if((!planeSet.getAllPlaneKeys().contains(planeName.toUpperCase().trim()))
		||(!planeSet.deletePlane(planeName)))
		{
			mob.tell(L("'@x1' does not exist, try LIST COMPONENTS.",planeName));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The planar universe just decreased!"));
		return true;
	}

	public boolean expertises(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY EXPERTISE [CODE ID or HELP line]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		if(CMLib.expertises().delDefinition(classID, true))
		{
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The power of skill usage just decreased!"));
		}
		else
		{
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		return true;
	}

	public boolean titles(final MOB mob, final List<String> commands)
	{
		mob.tell(L("Destroying a title will not remove the title from all players who may have it."));
		mob.tell(L("If this is important, you should destroy and then re-add the exact same title with an unreachable mask "
				+ "for a few days to allow the system to remove the title from the players as they log in.\n\r"));
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY TITLE [TITLE STRING]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		if(!CMLib.awards().isExistingAutoTitle(classID))
		{
			mob.tell(L("'@x1' is not an existing auto-title, try LIST TITLES.",classID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String error = CMLib.awards().deleteTitleAndResave(classID); // also does CMLib.titles().reloadAutoTitles();
		if(error == null)
		{
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The prestige of players just decreased!"));
		}
		else
		{
			mob.tell(L(error));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		return true;
	}

	public boolean classes(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY CLASS [CLASS ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		final CharClass C=CMClass.getCharClass(classID);
		if(C==null)
		{
			mob.tell(L("'@x1' is an invalid class id.",classID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell(L("'@x1' is not generic, and may not be deleted.",C.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		CMClass.delCharClass(C);
		CMLib.database().DBDeleteClass(C.ID());
		final String oldCID=C.ID();
		CMClass.loadClass(CMObjectType.CHARCLASS,"com/planet_ink/coffee_mud/CharClasses/"+oldCID+".class",true);
		CharClass oldC=CMClass.getCharClass(oldCID);
		if(oldC==null)
			oldC=CMClass.getCharClass("StdCharClass");
		CMLib.utensils().reloadCharClasses(C);
		if(!oldC.ID().equals("StdCharClass"))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The diversity of the world just changed?!"));
		else
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The employment of the world just decreased!"));
		return true;
	}

	public boolean abilities(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY ABILITY [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(classID);
		if(A==null)
		{
			mob.tell(L("'@x1' is an invalid ability id.",classID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(A.isGeneric()))
		{
			mob.tell(L("'@x1' is not generic, and may not be deleted.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		final Object O=CMClass.getObjectOrPrototype(A.ID());
		if(!(O instanceof Ability))
		{
			mob.tell(L("'@x1' can not be deleted, because it is also an @x2.",classID,CMClass.getSimpleClassName(O)));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		CMClass.delClass(CMObjectType.ABILITY,(Ability)O);
		CMLib.database().DBDeleteAbility(A.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The skill of the world just decreased!"));
		return true;
	}

	public void destroyMQL(final MOB mob, final boolean areaFlag, final List<String> commands)
	{
		final StringBuilder lines = new StringBuilder("");
		try
		{
			commands.add(1,".");
			final String mql = CMParms.combineQuoted(commands, 0);
			final List<Map<String,Object>> res=CMLib.percolator().doMQLSelectObjects(areaFlag?(mob.location().getArea()):null, mql);
			if(res.size()==0)
				mob.tell(L("(empty set)"));
			else
			{
				final List<Environmental> delts = new LinkedList<Environmental>();
				lines.append("Delete preview:\n\r");
				for(final Map<String,Object> m : res)
				{
					for(final Map.Entry<String,Object> eo : m.entrySet())
					{
						if(eo.getValue() instanceof Environmental)
						{
							final Environmental E = (Environmental)eo.getValue();
							lines.append(E.name()+" ("+E.ID()+") @"+CMLib.map().getApproximateExtendedRoomID(CMLib.map().roomLocation(E))+":\n\r");
							delts.add(E);
						}
					}
				}
				final Runnable doDelete = new Runnable()
				{
					final List<Environmental> todo=delts;

					@Override
					public void run()
					{
						for(final Environmental E : todo)
						{
							final Room R=CMLib.map().roomLocation(E);
							if((R!=null) && R.isSavable() && (R.roomID().length()>0))
							{
								Environmental saveE = null;
								Environmental destroyE = null;
								String roomID = null;
								if(E instanceof Ability)
								{
									saveE=((Ability)E).affecting();
									if(saveE != null)
									{
										Log.infoOut(mob.name()+" deleted "+E.name()+" from "+saveE.Name()+" in "+R.roomID());
										((Physical)saveE).delEffect((Ability)E);
									}
								}
								else
								if(E instanceof Room)
								{
									Log.infoOut(mob.name()+" deleted room "+((Room)E).roomID());
									roomID=((Room)E).roomID();
									destroyE=E;
								}
								else
								if(E instanceof Area)
								{
									Log.infoOut(mob.name()+" deleted area "+((Area)E).Name());
									destroyE=E;
								}
								else
								if(E instanceof Item)
								{
									final Item I=(Item)E;
									if((I.owner() instanceof Room)
									&&(I.databaseID().length()>0))
									{
										roomID=R.roomID();
										Log.infoOut(mob.name()+" deleted "+E.name()+" in "+R.roomID());
										destroyE=I;
									}
									else
									if((I.owner() instanceof MOB)
									&&(((MOB)I.owner()).databaseID().length()>0)
									&&(((MOB)I.owner()).getStartRoom()!=null)
									&&(((MOB)I.owner()).getStartRoom().roomID().length()>0))
									{
										roomID=((MOB)I.owner()).getStartRoom().roomID();
										saveE=(I.owner());
										Log.infoOut(mob.name()+" deleted "+E.name()+" from "+((MOB)I.owner()).Name()+" in "+R.roomID());
									}
								}
								else
								if((E instanceof MOB)
								&&(((MOB)E).databaseID().length()>0))
								{
									roomID=R.roomID();
									Log.infoOut(mob.name()+" deleted "+E.name()+" in "+R.roomID());
									destroyE=E;
								}
								if((saveE != null)
								&&(saveE.isSavable()))
								{
									if(saveE instanceof Item)
									{
										final Item I=(Item)saveE;
										if((I.owner() instanceof Room)
										&&(I.databaseID().length()>0))
											CMLib.database().DBUpdateItem(R.roomID(), I);
										else
										if((I.owner() instanceof MOB)
										&&(((MOB)I.owner()).databaseID().length()>0)
										&&(((MOB)I.owner()).getStartRoom()!=null)
										&&(((MOB)I.owner()).getStartRoom().roomID().length()>0))
											CMLib.database().DBUpdateMOB(((MOB)I.owner()).getStartRoom().roomID(), (MOB)I.owner());
									}
									else
									if((saveE instanceof MOB)
									&&(((MOB)saveE).databaseID().length()>0))
										CMLib.database().DBUpdateMOB(R.roomID(), (MOB)saveE);
									else
									if((saveE instanceof Room)
									&&(((Room)saveE).roomID().length()>0))
										CMLib.database().DBUpdateRoom((Room)saveE);
									else
									if(saveE instanceof Area)
										CMLib.database().DBUpdateArea(saveE.Name(), (Area)saveE);
								}
								if((destroyE != null)
								&&(destroyE.isSavable()))
								{
									if(destroyE instanceof Item)
									{
										if(((Item)destroyE).databaseID().length()>0)
											CMLib.database().DBDeleteItem(roomID, (Item)destroyE);
										destroyE.destroy();
									}
									else
									if(destroyE instanceof MOB)
									{
										if(((MOB)destroyE).databaseID().length()>0)
											CMLib.database().DBDeleteMOB(roomID, (MOB)destroyE);
										destroyE.destroy();
									}
									else
									if((destroyE instanceof Room)
									&&(((Room)destroyE).roomID().length()>0))
										CMLib.map().obliterateMapRoom((Room)destroyE);
									else
									if(destroyE instanceof Area)
										CMLib.map().obliterateMapArea((Area)destroyE);
								}
							}
						}
						mob.tell(L("DELETE: completed."));
					}
				};
				final Session session = mob.session();
				if(session!=null)
				{
					final InputCallback callBack = new InputCallback(InputCallback.Type.CONFIRM,"N",0)
					{
						@Override
						public void showPrompt()
						{
							session.promptPrint(L("\n\rSave the above deletions (y/N)? "));
						}

						@Override
						public void timedOut()
						{
						}

						@Override
						public void callBack()
						{
							if(this.input.equals("Y"))
							{
								doDelete.run();
							}
						}
					};
					session.wraplessPrintln(lines.toString());
					lines.setLength(0);
					session.prompt(callBack);
				}
			}
		}
		catch(final MQLException e)
		{
			final ByteArrayOutputStream bout=new ByteArrayOutputStream();
			final PrintStream pw=new PrintStream(bout);
			e.printStackTrace(pw);
			pw.flush();
			mob.tell(e.getMessage()+"\n\r"+bout.toString());
		}
	}

	public boolean achievements(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is DESTROY ACHIEVEMENT [TATTOO]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		String tattoo=CMParms.combine(commands,2);
		final Achievement A=CMLib.achievements().getAchievement(tattoo);
		if(A==null)
		{
			mob.tell(L("An achievement with the TATTOO/ID '@x1' does not exist!",tattoo));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		tattoo = A.getTattoo();
		if(CMLib.achievements().deleteAchievement(tattoo)!=null)
		{
			for(final Enumeration<MOB> m = CMLib.players().players();m.hasMoreElements();)
			{
				final MOB M=m.nextElement();
				if(M.playerStats()!=null)
				{
					M.playerStats().rebuildAchievementTracker(mob, M, tattoo);
				}
			}
			CMLib.achievements().resaveAchievements(tattoo);
		}
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The vanity of the world just decreased!"));
		return true;
	}

	public void socials(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.session().safeRawPrintln(L("but fail to specify the proper fields.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		else
		if(commands.size()>3)
		{
			final String therest=CMParms.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")
					||therest.equalsIgnoreCase("SELF")
					||therest.equalsIgnoreCase("ALL"))))
			{
				mob.session().safeRawPrintln(L("but fail to specify the proper second parameter.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
		}

		final Social soc2=CMLib.socials().fetchSocial(CMParms.combine(commands,2).toUpperCase(),true);
		if(soc2==null)
		{
			mob.tell(L("but fail to specify an EXISTING SOCIAL!\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		if(mob.session().confirm(L("Are you sure you want to delete that social (y/N)? "),"N"))
		{
			CMLib.socials().delSocial(soc2.name());
			CMLib.socials().save(mob);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The happiness of all mankind has just decreased!"));
			Log.sysOut("SysopSocials",mob.Name()+" destroyed social "+soc2.name()+".");
		}
		else
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The happiness of all mankind has just increased!"));
	}

	public boolean destroyItem(final MOB mob, final Physical dropThis, final boolean quiet, final boolean optimize)
	{
		String msgstr=null;
		final int material=(dropThis instanceof Item)?((Item)dropThis).material():-1;
		if(!quiet)
		{
			switch(material&RawMaterial.MATERIAL_MASK)
			{
			case RawMaterial.MATERIAL_LIQUID:
				msgstr=L("<S-NAME> pour(s) out <T-NAME>.");
				break;
			case RawMaterial.MATERIAL_PAPER:
			case RawMaterial.MATERIAL_CLOTH:
				msgstr=L("<S-NAME> tear(s) up <T-NAME>.");
				break;
			case RawMaterial.MATERIAL_GLASS:
				msgstr=L("<S-NAME> smash(es) <T-NAME>.");
				break;
			default:
				return false;
			}
		}
		final String oldName = dropThis.Name();
		final int weight = dropThis.phyStats().weight();
		final ItemPossessor oldOwner = (dropThis instanceof Item)?((Item)dropThis).owner():null;
		final CMMsg msg;
		msg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_NOISYMOVEMENT,
				(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MASK_ALWAYS|CMMsg.MSG_DEATH,
				CMMsg.MSG_NOISYMOVEMENT,msgstr);
		if(mob.location().okMessage(mob,msg))
		{
			if(dropThis instanceof Container)
				((Container)dropThis).emptyPlease(false);
			mob.location().send(mob,msg);
			if((material&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_CLOTH)
			{
				if(dropThis.amDestroyed())
				{
					final String matName = RawMaterial.CODES.NAME(material).toLowerCase();
					final Item bitTemplateI = CMClass.getItem("GenItem");
					bitTemplateI.setMaterial(material);
					bitTemplateI.basePhyStats().setWeight(1);
					bitTemplateI.setName(L("a strip of @x1",matName));
					bitTemplateI.setDisplayText(L("@x1 has been dropped here.",bitTemplateI.name()));
					bitTemplateI.setDescription(L("It looks like it used to be part of @x1.",oldName));
					final Item pkgI;
					if(weight > 1)
					{
						pkgI = CMClass.getItem("GenPackagedStack");
						((PackagedItems)pkgI).packageMe(bitTemplateI, weight);
					}
					else
						pkgI = bitTemplateI;
					pkgI.recoverPhyStats();
					if(oldOwner == null)
						mob.location().addItem(pkgI, ItemPossessor.Expire.Player_Drop);
					else
						oldOwner.addItem(pkgI, ItemPossessor.Expire.Player_Drop);
					pkgI.recoverPhyStats();
				}
			}
			return true;
		}
		if(dropThis instanceof Coins)
			((Coins)dropThis).putCoinsBack();
		if(dropThis instanceof RawMaterial)
			((RawMaterial)dropThis).rebundle();
		return false;
	}

	public void allQualify(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ALLQUALIFY EACH/ALL [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String eachOrAll=commands.get(2);
		if((!eachOrAll.equalsIgnoreCase("each"))&&(!eachOrAll.equalsIgnoreCase("all")))
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ALLQUALIFY EACH/ALL [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String classD=CMParms.combine(commands,3);
		final Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		final Map<String,AbilityMapper.AbilityMapping> subMap=map.get(eachOrAll.toUpperCase().trim());
		if(!subMap.containsKey(classD.toUpperCase().trim()))
		{
			mob.tell(L("All-Qualify entry (@x1) ID '@x2' does not exist! Try LIST ALLQUALIFYS",eachOrAll,classD));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		subMap.remove(classD.toUpperCase().trim());
		CMLib.ableMapper().saveAllQualifysFile(map);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The skill of the world just decreased!"));
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if((!CMSecurity.isAllowedAnywhereContainsAny(mob,CMSecurity.SECURITY_CMD_GROUP))
		&&(!CMSecurity.isAllowedContainsAny(mob,mob.location(),CMSecurity.SECURITY_KILL_GROUP))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.BAN))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NOPURGE))
		&&(!((commands.size()>1)&&(CMSecurity.isJournalAccessAllowed(mob, commands.get(1))))))
		{
			final char cmd = Character.toUpperCase(commands.size()>0?commands.get(0).charAt(0):'D');
			if(commands.size()>0)
				commands.remove(0);
			if(commands.size()==0)
			{
				for(final String a : access) // access is localized, sortof
				{
					if(Character.toUpperCase(a.charAt(0)) == cmd)
					{
						final String cmdW = CMStrings.capitalizeAndLower(a);
						mob.tell(L("@x1 what?",cmdW));
						return false;
					}
				}
				mob.tell(L("Destroy what?"));
				return false;
			}
			final List<Item> V=new ArrayList<Item>();
			int maxToDrop=Integer.MAX_VALUE;

			if((commands.size()>1)
			&&(CMath.s_int(commands.get(0))>0))
			{
				maxToDrop=CMath.s_int(commands.get(0));
				commands.set(0,"all");
			}

			String whatToDrop=CMParms.combine(commands,0);
			boolean allFlag=(commands.size()>0)?commands.get(0).equalsIgnoreCase("all"):false;
			if(whatToDrop.toUpperCase().startsWith("ALL."))
			{
				allFlag=true;
				whatToDrop="ALL "+whatToDrop.substring(4);
			}
			if(whatToDrop.toUpperCase().endsWith(".ALL"))
			{
				allFlag=true;
				whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);
			}
			int addendum=1;
			String addendumStr="";
			boolean doBugFix = true;
			while(doBugFix || ((allFlag)&&(addendum<=maxToDrop)))
			{
				doBugFix=false;
				Item trashI=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,whatToDrop+addendumStr);
				if((trashI==null)
				&&(V.size()==0)
				&&(addendumStr.length()==0)
				&&(!allFlag))
					trashI=mob.fetchItem(null,Wearable.FILTER_WORNONLY,whatToDrop);

				if(trashI!=null)
				{
					final int matType=trashI.material()&RawMaterial.MATERIAL_MASK;
					if(matType==RawMaterial.MATERIAL_CLOTH)
					{
						if((trashI instanceof RawMaterial)
						||(trashI.phyStats().weight()<1))
						{
							mob.tell(L("@x1 is already in small pieces.",trashI.Name()));
							return false;
						}
					}
					else
					if((matType!=RawMaterial.MATERIAL_GLASS)
					&&(matType!=RawMaterial.MATERIAL_LIQUID)
					&&(matType!=RawMaterial.MATERIAL_PAPER))
					{
						mob.tell(L("@x1 can not be easily destroyed.",trashI.Name()));
						return false;
					}
					else
					if((!trashI.amWearingAt(Wearable.WORN_HELD))
					&&(!trashI.amWearingAt(Wearable.WORN_WIELD))
					&&(!trashI.amWearingAt(Wearable.IN_INVENTORY)))
					{
						mob.tell(L("You must remove that first."));
						return false;
					}
					else
					if(!trashI.amWearingAt(Wearable.IN_INVENTORY))
					{
						final CMMsg newMsg=CMClass.getMsg(mob,trashI,null,CMMsg.MSG_REMOVE,null);
						if(mob.location().okMessage(mob,newMsg))
							mob.location().send(mob,newMsg);
						else
							return false;
					}
				}
				if(trashI==null)
					break;
				if((CMLib.flags().canBeSeenBy(trashI,mob))
				&&(!V.contains(trashI)))
					V.add(trashI);
				addendumStr="."+(++addendum);
			}

			boolean didAnything=false;
			for(int i=0;i<V.size();i++)
			{
				final Item I = V.get(i);
				if(destroyItem(mob,I,false,true))
					didAnything=true;
				else
				if(I instanceof Coins)
					((Coins)I).putCoinsBack();
				else
				if(I instanceof RawMaterial)
					((RawMaterial)I).rebundle();
			}
			if(!didAnything)
			{
				final MOB destroyM=mob.location().fetchInhabitant(CMParms.combine(commands,0));
				if(destroyM!=null)
				{
					final Command C=CMClass.getCommand("Kill");
					final List<String> killCmd = new Vector<String>(commands.size()+1);
					killCmd.add(0,"KILL");
					killCmd.addAll(commands);
					if(C!=null)
					{
						final Session sess = mob.session();
						if(sess ==null)
							C.execute(mob,killCmd,metaFlags);
						else
						{
							sess.prompt(new InputCallback(InputCallback.Type.CONFIRM,"N",0)
							{
								@Override
								public void showPrompt()
								{
									sess.promptPrint(L("\n\rAre you sure you want to try to destroy '@x1'(y/N)? ", destroyM.name(mob)));
								}

								@Override
								public void timedOut()
								{
								}

								@Override
								public void callBack()
								{
									if (this.input.equals("Y"))
									{
										try
										{
											C.execute(mob,killCmd,metaFlags);
										}
										catch (final IOException e)
										{
										}
									}
								}
							});
						}
						return false;
					}
				}

				if(V.size()==0)
					mob.tell(L("You don't seem to be carrying that."));
				else
					mob.tell(L("You can't destroy that easily..."));
			}
			mob.location().recoverRoomStats();
			mob.location().recoverRoomStats();
			return false;
		}

		String commandType="";

		if(commands.size()>1)
		{
			commandType=commands.get(1).toUpperCase();
		}
		for(final Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			final JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMJ.NAME().equals(commandType))
			&&(CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME())))
			{
				int which = -1;
				String to = null;
				final String second=(commands.size()>2)?commands.get(2):"";
				final String third=(commands.size()>3)?CMParms.combine(commands,3):"";
				final List<String> flagsV=CMParms.parseAny(CMJ.getFlag(CommandJournalFlags.ASSIGN), ':', true);
				if(second.length()>0)
				{
					String possTo=null;
					if(CMath.isNumber(second))
					{
						which=CMath.s_int(second);
						if(third.length()>0)
							possTo=third;
					}
					else
					{
						possTo=second;
						if(third.length()>0)
						{
							if(CMath.isNumber(third))
								which=CMath.s_int(third);
							else
							{
								mob.tell(L("@x1 is not a number",third));
								return true;
							}
						}
					}
					if(possTo != null)
					{
						if(CMLib.players().playerExists(CMStrings.capitalizeAndLower(possTo)))
							to=CMStrings.capitalizeAndLower(possTo);
						else
						if(flagsV.contains(possTo))
							to=possTo;
						else
						if(possTo.equalsIgnoreCase("ALL"))
							to="ALL";
						else
						{
							mob.tell(L("@x1 is not a valid name. ",possTo));
		 					return true;
						}
					}
				}

				final String[] tos = (to != null)? new String[]{to} : new String[0];
				final List<JournalEntry> entries = CMLib.database().DBReadJournalMsgsByUpdateDate(CMJ.JOURNAL_NAME(), true, 100000, tos);

				if((which<=0)||(which>entries.size()))
					mob.tell(L("Please enter a valid @x1 number to delete.  Use LIST @x2S for more information.",CMJ.NAME().toLowerCase(),CMJ.NAME()));
				else
				{
					final JournalEntry entry = entries.get(which-1);
					CMLib.database().DBDeleteJournal(CMJ.JOURNAL_NAME(),entry.key());
					mob.tell(L("@x1 deletion submitted.",CMJ.NAME().toLowerCase()));

				}
				return true;
			}
		}
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			exits(mob,commands);
		}
		else
		if(commandType.equals("HELP")||commandType.equals("AHELP"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDHELP))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			helps(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			items(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if((commands.size()>2)
			&&(commands.get(2).equalsIgnoreCase("DELETE:"))
			&&(CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.CMDAREAS)))
			{
				commands.remove(1);
				commands.remove(0);
				destroyMQL(mob, true, commands);
			}
			else
				areas(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			rooms(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			races(mob,commands);
		}
		else
		if(commandType.equals("COMMAND"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCOMMANDS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			commands(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLASSES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			classes(mob,commands);
		}
		else
		if(commandType.equals("ABILITY")
		||commandType.equals("LANGUAGE")
		||commandType.equals("CRAFTSKILL")
		||commandType.equals("GATHERSKILL")
		||commandType.equals("WRIGHTSKILL")
		||commandType.equals("TRAP"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			abilities(mob,commands);
		}
		else
		if(commandType.equals("ACHIEVEMENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ACHIEVEMENTS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			achievements(mob,commands);
		}
		else
		if(commandType.equals("ALLQUALIFY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			allQualify(mob,commands);
		}
		else
		if(commandType.equals("COMPONENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COMPONENTS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			components(mob,commands);
		}
		else
		if(commandType.equals("PLANE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.PLANES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			plane(mob,commands);
		}
		else
		if(commandType.equals("EXPERTISE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.EXPERTISES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			expertises(mob,commands);
		}
		else
		if(commandType.equals("TITLE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TITLES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			titles(mob,commands);
		}
		else
		if(commandType.equals("AWARD"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AUTOAWARDS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			awards(mob,commands);
		}
		else
		if(commandType.equals("USER")||commandType.equals("PLAYER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			players(mob,commands);
		}
		else
		if(commandType.equals("DELETE:"))
		{
			if((!CMSecurity.isASysOp(mob))&&(!CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.CMDAREAS)))
				return errorOut(mob);
			commands.remove(0);
			destroyMQL(mob, false, commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.isUsingAccountSystem()))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			accounts(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDSOCIALS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			socials(mob,commands);
		}
		else
		if(commandType.equals("DISABLEFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return errorOut(mob);
			final String named=CMParms.combine(commands,2);
			if(!CMSecurity.isAnyFlagDisabled(named.toUpperCase()))
				mob.tell(L("'@x1' is not disabled",named));
			else
			{
				mob.tell(L("'@x1' is no longer disabled",named));
				CMSecurity.removeAnyDisableVar(named.toUpperCase().trim());
			}
			return false;
		}
		else
		if(commandType.equals("ENABLEFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return errorOut(mob);
			final String named=CMParms.combine(commands,2);
			if(!CMSecurity.isAnyFlagEnabled(named.toUpperCase()))
				mob.tell(L("'@x1' is not enabled",named));
			else
			{
				mob.tell(L("'@x1' is no longer enabled",named));
				CMSecurity.removeAnyEnableVar(named.toUpperCase().trim());
			}
			return false;
		}
		else
		if(commandType.equals("DEBUGFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return errorOut(mob);
			final String named=CMParms.combine(commands,2);
			final CMSecurity.DbgFlag flag = (CMSecurity.DbgFlag)CMath.s_valueOf(CMSecurity.DbgFlag.values(), named.toUpperCase().trim());
			if(flag==null)
			{
				mob.tell(L("'@x1' is not a valid flag.  Try: @x2",named,CMParms.toListString(CMSecurity.DbgFlag.values())));
				return false;
			}
			if(!CMSecurity.isDebugging(flag))
				mob.tell(L("'@x1' is not debugging",named));
			else
			{
				mob.tell(L("'@x1' is no longer debugging",named));
				if(flag == CMSecurity.DbgFlag.HTTPACCESS)
				{
					for(final MudHost host : CMLib.hosts())
					{
						try
						{
							host.executeCommand("WEBSERVER ADMIN ACCESS OFF");
							host.executeCommand("WEBSERVER PUB ACCESS OFF");
						}
						catch (final Exception e)
						{
							mob.tell(e.getMessage());
						}
					}
				}
				else
				if(flag == CMSecurity.DbgFlag.HTTPREQ)
				{
					for(final MudHost host : CMLib.hosts())
					{
						try
						{
							host.executeCommand("WEBSERVER ADMIN DEBUG OFF");
							host.executeCommand("WEBSERVER PUB DEBUG OFF");
						}
						catch (final Exception e)
						{
							mob.tell(e.getMessage());
						}
					}
				}
				CMSecurity.removeDebugVar(flag);
			}
			return false;
		}
		else
		if(commandType.equals("NOPURGE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NOPURGE))
				return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int(commands.get(2));
			if(which<=0)
				mob.tell(L("Please enter a valid player number to delete.  Use List nopurge for more information."));
			else
			{
				final StringBuffer newNoPurge=new StringBuffer("");
				final List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
				if((protectedOnes!=null)&&(protectedOnes.size()>0))
				{
					for(int b=0;b<protectedOnes.size();b++)
					{
						final String B=protectedOnes.get(b);
						if(((b+1)!=which)&&(B.trim().length()>0))
							newNoPurge.append(B+"\n");
					}
				}
				Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
				mob.tell(L("Ok."));
			}
		}
		else
		if(commandType.equals("HOLIDAY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS))
				return errorOut(mob);
			final String name=CMParms.combine(commands,2);
			int num=-1;
			if(CMath.isInteger(name))
				num=CMath.s_int(name);
			else
			if(name.length()>0)
				num=CMLib.quests().getHolidayIndex(name);
			if(num<0)
			{
				mob.tell(L("HOLIDAY '@x1' not found. Try LIST HOLIDAYS.",name));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return false;
			}
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			mob.tell(CMLib.quests().deleteHoliday(num));
			Log.sysOut("CreateEdit",mob.Name()+" deleted Holiday "+name+".");
		}
		else
		if(commandType.equals("TICKS"))
		{
			if(!CMSecurity.isASysOp(mob))
				return errorOut(mob);
			final String which=CMParms.combine(commands,2);
			List<Tickable> V=null;
			if(which.length()>0)
			{
				V=CMLib.threads().getNamedTickingObjects(which);
				if(V.size()==0)
					V=null;
			}
			if(V==null)
				mob.tell(L("Please enter a valid ticking object name to destroy.  Use List ticks for a list of groups and objects."));
			else
			{
				final StringBuffer list=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					list.append(V.get(v).name()+", ");
				if((mob.session()!=null)&&(mob.session().confirm(L("Destroy the following ticking objects: @x1  (y/N)? ",list.substring(0,list.length()-2)),"N")))
				{
					for(int v=0;v<V.size();v++)
						CMLib.threads().deleteTick(V.get(v),-1);
					Log.sysOut("CreateEdit",mob.Name()+" destroyed ticks named '"+which+"'.");
				}
			}
		}
		else
		if(commandType.equals("BAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.BAN))
				return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int(commands.get(2));
			if(which<=0)
				mob.tell(L("Please enter a valid ban number to delete.  Use List Banned for more information."));
			else
			{
				final String name = CMSecurity.unban(which);
				if(name.length()>0)
					Log.sysOut("CreateEdit",mob.Name()+" Unbanned "+name+".");
				mob.tell(L("Ok."));
			}
		}
		else
		if(commandType.equals("THREAD"))
		{
			if(!CMSecurity.isASysOp(mob))
				return errorOut(mob);
			final String which=CMParms.combine(commands,2);
			Thread whichT=null;
			if(which.length()>0)
				whichT=findThread(which);
			if(whichT==null)
				mob.tell(L("Please enter a valid thread name to destroy.  Use List threads for a list."));
			else
			{
				CMLib.killThread(whichT,500,1);
				Log.sysOut("CreateEdit",mob.Name()+" destroyed thread "+whichT.getName()+".");
				mob.tell(L("Stop sent to: @x1.",whichT.getName()));
			}
		}
		else
		if(commandType.startsWith("SESSION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.BOOT))
				return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int(commands.get(2));
			Session S=null;
			int x=0;
			for(final Session s : CMLib.sessions().allIterableAllHosts())
			{
				if(x++ == which)
				{
					S=s;
					break;
				}
			}
			if(S==null)
				mob.tell(L("Please enter a valid session number to delete.  Use LIST SESSIONS for more information."));
			else
			{
				CMLib.sessions().stopSessionAtAllCosts(S);
				if(S.getStatus()==Session.SessionStatus.LOGOUTFINAL)
					mob.tell(L("Ok."));
				else
					mob.tell(L("Failed to gracefully shutdown: @x1, but a forcable stop was issued.",S.getStatus().toString()));
			}
		}
		else
		if(commandType.equals("JOURNAL"))
		{
			if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS))
			&&((!((commands.size()>=3)&&(CMSecurity.isJournalAccessAllowed(mob,CMParms.combine(commands,2)))))))
				return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell(L("Destroy which journal? Try List Journal"));
				return errorOut(mob);
			}
			final List<String> V=CMLib.database().DBReadJournals();
			String name=CMParms.combine(commands,2);
			int which=-1;
			for(int v=0;v<V.size();v++)
			{
				if(V.get(v).equalsIgnoreCase(name))
				{
					name=V.get(v);
					which=v;
					break;
				}
			}
			if(which<0)
			for(int v=0;v<V.size();v++)
			{
				if(V.get(v).startsWith(name))
				{
					name=V.get(v);
					which=v;
					break;
				}
			}
			if(which<0)
				mob.tell(L("Please enter a valid journal name to delete.  Use List Journals for more information."));
			else
			if(mob.session().confirm(L("This will destroy all @x1 messages.  Are you SURE (y/N)? ",""+CMLib.database().DBCountJournal(name,null,null)),"N"))
			{
				CMLib.database().DBDeleteJournal(name,null);
				mob.tell(L("It is done."));
			}
		}
		else
		if(commandType.equals("GRACES"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			final DatabaseEngine dbE=CMLib.database();
			final PlayerLibrary pLib=CMLib.players();
			for(final String name : pLib.getPlayerLists())
			{
				final ThinPlayer T = pLib.getThinPlayer(name);
				if(T!=null)
					dbE.registerRaceUsed(CMClass.getRace(T.race()));
			}
			for(final MOB M : CMLib.database().DBScanFollowers(null))
			{
				dbE.registerRaceUsed(M.charStats().getMyRace());
				M.destroy();
			}
			CMLib.database().updateAllRaceDates();
			final int x = CMLib.database().pruneOldRaces();
			mob.tell(L("@x1 races pruned.",""+x));
		}
		else
		if(commandType.equals("FACTION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if(commands.size()<3)
				mob.tell(L("Destroy which faction?  Use list factions."));
			else
			{
				final String name=CMParms.combine(commands,2);
				Faction F=CMLib.factions().getFaction(name);
				if(F==null)
					F=CMLib.factions().getFactionByName(name);
				if(F==null)
					mob.tell(L("Faction '@x1' is unknown.  Try list factions.",name));
				else
				if((!mob.isMonster())&&(mob.session().confirm(L("Destroy faction '@x1' -- this could have unexpected consequences in the future -- (N/y)? ",F.factionID()),"N")))
				{
					try
					{
						final CMFile F2=new CMFile(Resources.makeFileResourceName(CMLib.factions().makeFactionFilename(F.factionID())),null);
						if(F2.exists())
							F2.deleteAll();
						else
							throw new IOException("Could not delete "+F2.getAbsolutePath());
						F.destroy();
						Log.sysOut("CreateEdit",mob.Name()+" destroyed Faction "+F.name()+" ("+F.factionID()+").");
						mob.tell(L("Faction File '@x1' deleted.",F.factionID()));
					}
					catch(final Exception e)
					{
						Log.errOut("CreateEdit",e);
						mob.tell(L("Faction '@x1' could NOT be deleted.",F.factionID()));
					}
				}
			}
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			mobs(mob,commands);
		}
		else
		if(commandType.equals("CRON"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCRON))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			cron(mob,commands);
		}
		else
		if(commandType.equals("MANUFACTURER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			manufacturer(mob,commands);
		}
		else
		if(commandType.equals("WEBSERVER"))
		{
			if(!CMSecurity.isASysOp(mob))
				return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell(L("You must specify a web server name."));
				return false;
			}
			else
			{
				try
				{
					mob.tell(CMLib.hosts().get(0).executeCommand("STOP WEB "+commands.get(2)));
				}
				catch (final Exception e)
				{
					mob.tell(L("Failure: @x1",e.getMessage()));
				}
			}
		}
		else
		if(commandType.equals("CM1") || commandType.equals("IMC2") || commandType.equals("I3")|| commandType.equals("SMTP"))
		{
			if(!CMSecurity.isASysOp(mob))
				return errorOut(mob);
			try
			{
				mob.tell(CMLib.hosts().get(0).executeCommand("STOP "+commandType));
			}
			catch (final Exception e)
			{
				mob.tell(L("Failure: @x1",e.getMessage()));
			}
		}
		else
		if(commandType.equals("POLL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.POLLS))
				return errorOut(mob);
			final String name=CMParms.combine(commands,2);
			Poll P=null;
			if(CMath.isInteger(name))
				P=CMLib.polls().getPoll(CMath.s_int(name)-1);
			else
			if(name.length()>0)
				P=CMLib.polls().getPoll(name);
			if(P==null)
			{
				mob.tell(L("POLL '@x1' not found. Try LIST POLLS.",name));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return false;
			}
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if((mob.session()!=null)&&(mob.session().confirm(L("Destroy POLL @x1, are you SURE? (Y/n)? ",P.getName()),"Y")))
			{
				CMLib.polls().deletePoll(P);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^SThe world has grown a bit more certain.^?"));
				Log.sysOut("CreateEdit",mob.Name()+" modified Poll "+P.getName()+".");
			}
			else
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
		}
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if(commands.size()<3)
				mob.tell(L("Destroy which quest?  Use list quests."));
			else
			{
				String name=CMParms.combine(commands,2);
				Quest Q=null;
				if(CMath.isInteger(name))
				{
					Q=CMLib.quests().fetchQuest(CMath.s_int(name)-1);
					if(Q!=null)
						name=Q.name();
				}
				if(Q==null)
					Q=CMLib.quests().fetchQuest(name);
				if(Q==null)
					mob.tell(L("Quest '@x1' is unknown.  Try list quests.",name));
				else
				{
					if(Q.running()&&(!Q.stopping()))
					{
						CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTSTOP);
						Q.stopQuest();
					}
					mob.tell(L("Quest '@x1' is destroyed!",Q.name()));
					CMLib.quests().delQuest(Q);
				}
			}
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if(commands.size()<3)
				mob.tell(L("Destroy which clan?  Use clanlist."));
			else
			{
				final String name=CMParms.combine(commands,2);
				final Clan C=CMLib.clans().findClan(name);
				if(C==null)
					mob.tell(L("Clan '@x1' is unknown.  Try clanlist.",name));
				else
				{
					mob.tell(L("Clan '@x1' is destroyed!",C.name()));
					C.destroyClan();
					Log.sysOut("CreateEdit","Clan '"+C.name()+" destroyed by "+mob.Name()+".");
				}
			}
		}
		else
		if(commandType.equals("GOVERNMENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if(commands.size()<3)
				mob.tell(L("Destroy which government?  Use list governments."));
			else
			{
				final String name=CMParms.combine(commands,2);
				ClanGovernment G=null;
				for(final ClanGovernment g : CMLib.clans().getStockGovernments())
				{
					if(g.getName().equalsIgnoreCase(name))
						G=g;
				}
				if(G==null)
					mob.tell(L("Government '@x1' is unknown.  Try list governments.",name));
				else
				if(CMLib.clans().removeGovernment(G))
				{
					mob.tell(L("Government '@x1' is destroyed!",G.getName()));
					CMLib.clans().reSaveGovernmentsXML();
					Log.sysOut("CreateEdit","Government '"+G.getName()+" destroyed by "+mob.Name()+".");
				}
				else
					mob.tell(L("You can't delete the last remaining clan government."));
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			Environmental thang=mob.location().fetchFromRoomFavorItems(null,allWord);
			if(thang==null)
				thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Wearable.FILTER_ANY);
			if((thang == null)
			&&(commands.size()>2)
			&&(CMath.isInteger(commands.get(1).toString())||commands.get(1).toString().equalsIgnoreCase("all")))
			{
				allWord=CMParms.combine(commands,2);
				thang=mob.location().fetchFromRoomFavorItems(null,allWord);
				if(thang==null)
					thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Wearable.FILTER_ANY);
				if(!(thang instanceof Item))
					thang=null;
			}
			if((thang!=null)&&(thang instanceof Item))
			{
				commands.add(1,"ITEM");
				execute(mob,commands,metaFlags);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.add(1,"MOB");
				else
					commands.add(1,"USER");
				execute(mob,commands,metaFlags);
			}
			else
			{
				Room theRoom=null;
				if(allWord.length()>0)
				{
					try
					{
						 theRoom=CMLib.map().getRoom(allWord);
					}
					catch(final NoSuchElementException e)
					{
					}
				}
				if(theRoom!=null)
				{
					commands=new Vector<String>();
					commands.add("DESTROY");
					commands.add("ROOM");
					commands.add(theRoom.roomID());
					execute(mob,commands,metaFlags);
				}
				else
				{
					if(CMLib.directions().getGoodDirectionCode(allWord)>=0)
					{
						commands=new Vector<String>();
						commands.add("DESTROY");
						commands.add("ROOM");
						commands.add(allWord);
						execute(mob,commands,metaFlags);

						commands=new Vector<String>();
						commands.add("DESTROY");
						commands.add("EXIT");
						commands.add(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if(CMLib.socials().fetchSocial(allWord,true)!=null)
					{
						commands.add(1,"SOCIAL");
						execute(mob,commands,metaFlags);
					}
					else
					if((thang=CMLib.space().findSpaceObject(allWord,true))!=null)
					{
						commands=new Vector<String>();
						commands.add("DESTROY");
						if(thang instanceof Area)
							commands.add("AREA");
						else
						if(thang instanceof Item)
							commands.add("ITEM");
						commands.add(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if(allWord.indexOf('@')>=0)
					{
						commands.add(1,"ITEM");
						execute(mob,commands,metaFlags);
					}
					else
					{
						mob.tell(
							L("\n\rYou cannot destroy a '@x1'. However, you might try a @x1",commandType,listOfThings()));
					}
				}
			}
		}
		return false;
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

}
