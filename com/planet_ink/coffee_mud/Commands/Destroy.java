package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.CMSecurity.SecFlag;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Destroy extends StdCommand
{
	public Destroy(){}

	private final String[] access={"DESTROY","JUNK"};
	public String[] getAccessWords(){return access;}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}
	
	public boolean mobs(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		String mobID=CMParms.combine(commands,2);
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(mobID.toUpperCase().startsWith("ALL.")){ allFlag=true; mobID="ALL "+mobID.substring(4);}
		if(mobID.toUpperCase().endsWith(".ALL")){ allFlag=true; mobID="ALL "+mobID.substring(0,mobID.length()-4);}
		MOB deadMOB=mob.location().fetchInhabitant(mobID);
		boolean doneSomething=false;
		while(deadMOB!=null)
		{
			if(!deadMOB.isMonster())
			{
				mob.tell(deadMOB.name()+" is a PLAYER!!\n\r");
				if(!doneSomething)
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return false;
			}
			doneSomething=true;
			mob.location().showHappens(CMMsg.MSG_OK_VISUAL,deadMOB.name()+" vanishes in a puff of smoke.");
			Log.sysOut("Mobs",mob.Name()+" destroyed mob "+deadMOB.Name()+".");
			deadMOB.destroy();
			mob.location().delInhabitant(deadMOB);
			deadMOB=mob.location().fetchInhabitant(mobID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}
		return true;
	}


	public void accounts(MOB mob, Vector commands)
	throws IOException
	{
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around the heavens.");
		if(commands.size()<3) 
		{ 
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ACCOUNT ([NAME])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
		PlayerAccount theAccount = CMLib.players().getLoadAccount(accountName);
		if(theAccount==null)
		{
			mob.tell("There is no account called '"+accountName+"'!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		String playerList = CMParms.toStringList(theAccount.getPlayers());
		if(mob.session().confirm("This will complete OBLITERATE the account '"+theAccount.accountName()+"' and players '"+playerList+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			for(Enumeration<String> p=theAccount.getPlayers();p.hasMoreElements();)
			{
				MOB deadMOB=CMLib.players().getLoadPlayer(p.nextElement());
				CMLib.players().obliteratePlayer(deadMOB,true,false);
				mob.tell("The user '"+CMParms.combine(commands,2)+"' is no more!\n\r");
				Log.sysOut("Mobs",mob.Name()+" destroyed user "+deadMOB.Name()+".");
				deadMOB.destroy();
			}
			CMLib.players().obliterateAccountOnly(theAccount);
			mob.location().recoverRoomStats();
			Log.sysOut("Destroy",mob.Name()+" destroyed account "+theAccount.accountName()+" and players '"+playerList+"'.");
		}
	}

	public static boolean players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY USER [USER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		
		String name=CMStrings.capitalizeAndLower(CMParms.combine(commands,2));
		boolean found=CMLib.players().playerExists(name);

		if(!found)
		{
			mob.tell("The user '"+CMParms.combine(commands,2)+"' does not exist!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		if(mob.session().confirm("This will complete OBLITERATE the user '"+name+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			MOB deadMOB=CMLib.players().getLoadPlayer(name);
			CMLib.players().obliteratePlayer(deadMOB,true,false);
			mob.tell("The user '"+CMParms.combine(commands,2)+"' is no more!\n\r");
			Log.sysOut("Mobs",mob.Name()+" destroyed user "+deadMOB.Name()+".");
			deadMOB.destroy();
			return true;
		}
		return true;
	}
	
	
	public Thread findThreadGroup(String threadName,ThreadGroup tGroup)
	{
		int ac = tGroup.activeCount();
		int agc = tGroup.activeGroupCount();
		Thread tArray[] = new Thread [ac+1];
		ThreadGroup tgArray[] = new ThreadGroup [agc+1];

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
					Thread t=findThreadGroup(threadName,tgArray[i]);
					if(t!=null) return t;
				}
			}
		}
		return null;
	}


	public Thread findThread(String threadName)
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
		catch (Exception e)
		{
		}
		return t;

	}

	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		String thecmd=((String)commands.elementAt(0)).toLowerCase();
		if(commands.size()<3)
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell("You have failed to specify the proper fields.\n\rThe format is UNLINK (N,S,E,W,U, or D)\n\r");
			else
				mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ROOM ([DIRECTION],[ROOM ID])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		boolean confirmed=false;
		if((commands.size()>3))
		{
			if(((String)commands.lastElement()).equalsIgnoreCase("CONFIRMED"))
			{
				commands.removeElementAt(commands.size()-1);
				confirmed=true;
			}
		}
		String roomdir=CMParms.combine(commands,2);
		int direction=Directions.getGoodDirectionCode(roomdir);
		Room deadRoom=null;
		if(!thecmd.equalsIgnoreCase("UNLINK"))
			deadRoom=CMLib.map().getRoom(roomdir);
		if((deadRoom==null)&&(direction<0))
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell("You have failed to specify a direction.  Try ("+Directions.LETTERS()+").\n\r");
			else
				mob.tell("You have failed to specify a direction.  Try a VALID ROOM ID, or ("+Directions.LETTERS()+").\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(mob.isMonster())
		{
			mob.tell("Sorry Charlie!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;

		}
		if(deadRoom!=null)
		{
			if(!CMSecurity.isAllowed(mob,deadRoom,CMSecurity.SecFlag.CMDROOMS))
			{
				mob.tell("Sorry Charlie! Not your room!");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
			if(mob.location()==deadRoom)
			{
				mob.tell("You dip! You have to leave this room first!");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}

			if(!confirmed)
				if(!mob.session().confirm("You are fixing to permanantly destroy Room \""+deadRoom.roomID()+"\".  Are you ABSOLUTELY SURE (y/N)","N")) 
					return;
			CMLib.map().obliterateRoom(deadRoom);
			mob.tell("The sound of massive destruction rings in your ears.");
			mob.location().showOthers(mob,null,CMMsg.MSG_NOISE,"The sound of massive destruction rings in your ears.");
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
				GridLocale GL=mob.location().getGridParent();
				int myX=GL.getGridChildX(mob.location());
				int myY=GL.getGridChildY(mob.location());
				for(Iterator<WorldMap.CrossExit> i=GL.outerExits();i.hasNext();)
				{
					WorldMap.CrossExit CE=i.next();
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
				GridLocale GL=(GridLocale)unRoom;
				for(Iterator<WorldMap.CrossExit> i=GL.outerExits();i.hasNext();)
				{
					WorldMap.CrossExit CE=i.next();
					if((!CE.out)
					&&(CE.dir==direction)
					&&(CE.destRoomID.equalsIgnoreCase(CMLib.map().getExtendedRoomID(mob.location()))))
					   GL.delOuterExit(CE);
				}
				CMLib.database().DBUpdateExits(GL);
			}
			mob.location().getArea().fillInAreaRoom(mob.location());
			final boolean useShipDirs=(mob.location() instanceof SpaceShip)||(mob.location().getArea() instanceof SpaceShip);
			final String inDirName=useShipDirs?Directions.getShipDirectionName(direction):Directions.getDirectionName(direction);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A wall of inhibition falls "+inDirName+".");
			final String dirName=useShipDirs?Directions.getShipDirectionName(direction):Directions.getDirectionName(direction);
			Log.sysOut("Rooms",mob.Name()+" unlinked direction "+dirName+" from room "+mob.location().roomID()+".");
		}
	}

	public void exits(MOB mob, Vector commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY EXIT [DIRECTION]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.LETTERS()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(mob.isMonster())
		{
			mob.tell("Sorry Charlie!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;

		}
		mob.location().setRawExit(direction,null);
		CMLib.database().DBUpdateExits(mob.location());
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		final boolean useShipDirs=(mob.location() instanceof SpaceShip)||(mob.location().getArea() instanceof SpaceShip);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A wall of inhibition falls "+(useShipDirs?Directions.getShipInDirectionName(direction):Directions.getInDirectionName(direction))+".");
		Log.sysOut("Exits",mob.location().roomID()+" exits destroyed by "+mob.Name()+".");
	}

	public boolean items(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ITEM [ITEM NAME](@ room/[MOB NAME])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		
		String itemID=CMParms.combine(commands,2);
		MOB srchMob=mob;
		Item srchContainer=null;
		Room srchRoom=mob.location();
		int x=itemID.indexOf('@');
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if(rest.equalsIgnoreCase("room"))
				srchMob=null;
			else
			if(rest.length()>0)
			{
				MOB M=srchRoom.fetchInhabitant(rest);
				if(M==null)
				{
					Item I = srchRoom.findItem(null, rest);
					if(I instanceof Container)
						srchContainer=I;
					else
					{
						mob.tell("MOB or Container '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
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
		
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL.")){ allFlag=true; itemID="ALL "+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(".ALL")){ allFlag=true; itemID="ALL "+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Item deadItem=null;
		deadItem=(srchRoom==null)?null:srchRoom.findItem(srchContainer,itemID);
		if((!allFlag)&&(deadItem==null)) deadItem=(srchMob==null)?null:srchMob.findItem(null,itemID);
		while(deadItem!=null)
		{
			mob.location().recoverRoomStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,deadItem.name()+" disintegrates!");
			doneSomething=true;
			Log.sysOut("Items",mob.Name()+" destroyed item "+deadItem.name()+".");
			if(srchMob!=null)
				deadItem.setOwner(srchMob);
			else
			if(srchRoom!=null)
				deadItem.setOwner(srchRoom);
			deadItem.destroy();
			mob.location().delItem(deadItem);
			deadItem=null;
			if(!allFlag) deadItem=(srchMob==null)?null:srchMob.findItem(null,itemID);
			if(deadItem==null) deadItem=(srchRoom==null)?null:srchRoom.findItem(null,itemID);
			if(!allFlag) break;
		}
		if(!doneSomething)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		return true;
	}


	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY AREA [AREA NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
			return;
		}
		boolean confirmed=false;
		if((commands.size()>3))
		{
			if(((String)commands.lastElement()).equalsIgnoreCase("CONFIRMED"))
			{
				commands.removeElementAt(commands.size()-1);
				confirmed=true;
			}
		}
		List<String> areaNames=new LinkedList<String>();
		areaNames.add(CMParms.combine(commands,2));
		if((commands.size()>4))
		{
			if(((String)commands.get(2)).equalsIgnoreCase("all"))
			{
				areaNames.clear();
				for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
					areaNames.add(a.nextElement().Name());
				if(((String)commands.get(3)).equalsIgnoreCase("except"))
				{
					for(int i=4;i<commands.size();i++)
					{
						final Area A=CMLib.map().getArea((String)commands.elementAt(i));
						if(A==null)
						{
							mob.tell("There is no such area as '"+((String)commands.elementAt(i))+"'");
							mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
							return;
						}
						areaNames.remove(A.Name());
					}
				}
			}
		}
		
		for(String areaName : areaNames)
		{
			if(CMLib.map().getArea(areaName)==null)
			{
				mob.tell("There is no such area as '"+areaName+"'");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
				return;
			}
			Area A=CMLib.map().getArea(areaName);
			Room R=A.getRandomProperRoom();
			if((R!=null)&&(!CMSecurity.isAllowed(mob,R,CMSecurity.SecFlag.CMDAREAS)))
			{
				errorOut(mob);
				return;
			}
				
			if(!confirmed)
				if(mob.session().confirm("Area: \""+areaName+"\", OBLITERATE IT???","N"))
				{
					if(mob.location().getArea().Name().equalsIgnoreCase(areaName))
					{
						mob.tell("You dip!  You are IN that area!  Leave it first...");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
						return;
					}
					confirmed=true;
				}
			if(confirmed)
			{
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A thunderous boom of destruction is heard in the distance.");
				Log.sysOut("Rooms",mob.Name()+" destroyed area "+areaName+".");
				CMLib.map().obliterateArea(A);
			}
		}
	}

	public boolean races(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String raceID=CMParms.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be deleted.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		String oldRID=R.ID();
		CMClass.delRace(R);
		CMLib.database().DBDeleteRace(R.ID());
		CMClass.loadClass(CMObjectType.RACE,"com/planet_ink/coffee_mud/Races/"+oldRID+".class",true);
		Race oldR=CMClass.getRace(oldRID);
		if(oldR==null) oldR=CMClass.getRace("StdRace");
		CMLib.utensils().swapRaces(oldR,R);
		if(!oldR.ID().equals("StdRace"))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The diversity of the world just changed?!");
		else
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The diversity of the world just decreased!");
		return true;
	}

	public boolean components(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY COMPONENT [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String classID=CMParms.combine(commands,2);
		if(CMLib.ableMapper().getAbilityComponentMap().get(classID.toUpperCase())==null)
		{
			mob.tell("'"+classID+"' does not exist, try LIST COMPONENTS.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		CMLib.ableMapper().alterAbilityComponentFile(classID,true);
		CMLib.ableMapper().getAbilityComponentMap().remove(classID.toUpperCase());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The complication of skill usage just decreased!");
		return true;
	}
	
	public boolean expertises(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY EXPERTISE [CODE ID or HELP line]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String classID=CMParms.combine(commands,2);
		CMFile F=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,CMFile.FLAG_LOGERRORS);
		boolean removed=Resources.findRemoveProperty(F, classID);
		if(removed)
		{
			Resources.removeResource("skills/expertises.txt");
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The power of skill usage just decreased!");
			CMLib.expertises().recompileExpertises();
		}
		return true;
	}
	
	public boolean titles(MOB mob, Vector commands)
	{
		mob.tell("Destroying a title will not remove the title from all players who may have it.");
		mob.tell("If this is important, you should destroy and then re-add the exact same title with an unreachable " +
				 "mask for a few days to allow the system to remove the title from the players as they log in.\n\r");
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY TITLE [TITLE STRING]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String classID=CMParms.combine(commands,2);
		if(!CMLib.titles().isExistingAutoTitle(classID))
		{
			mob.tell("'"+classID+"' is not an existing auto-title, try LIST TITLES.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		CMLib.titles().dispossesTitle(classID);
		CMFile F=new CMFile(Resources.makeFileResourceName("titles.txt"),null,CMFile.FLAG_LOGERRORS);
		boolean removed=Resources.findRemoveProperty(F, classID);
		if(removed)
		{
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The prestige of players just decreased!");
			Resources.removeResource("titles.txt");
			CMLib.titles().reloadAutoTitles();
		}
		return true;
	}
	
	public boolean classes(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY CLASS [CLASS ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String classID=CMParms.combine(commands,2);
		CharClass C=CMClass.getCharClass(classID);
		if(C==null)
		{
			mob.tell("'"+classID+"' is an invalid class id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell("'"+C.ID()+"' is not generic, and may not be deleted.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		CMClass.delCharClass(C);
		CMLib.database().DBDeleteClass(C.ID());
		String oldCID=C.ID();
		CMClass.loadClass(CMObjectType.CHARCLASS,"com/planet_ink/coffee_mud/CharClasses/"+oldCID+".class",true);
		CharClass oldC=CMClass.getCharClass(oldCID);
		if(oldC==null) oldC=CMClass.getCharClass("StdCharClass");
		CMLib.utensils().reloadCharClasses(C);
		if(!oldC.ID().equals("StdCharClass"))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The diversity of the world just changed?!");
		else
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The employment of the world just decreased!");
		return true;
	}

	public boolean abilities(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY ABILITY [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String classID=CMParms.combine(commands,2);
		Ability A=CMClass.getAbility(classID);
		if(A==null)
		{
			mob.tell("'"+classID+"' is an invalid ability id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(A.isGeneric()))
		{
			mob.tell("'"+A.ID()+"' is not generic, and may not be deleted.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		Object O=CMClass.getObjectOrPrototype(A.ID());
		if(!(O instanceof Ability))
		{
			mob.tell("'"+classID+"' can not be deleted, because it is also an "+CMClass.getSimpleClassName(O)+".");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		CMClass.delClass(CMObjectType.ABILITY,(Ability)O);
		CMLib.database().DBDeleteAbility(A.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The skill of the world just decreased!");
		return true;
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		if(commands.size()>3)
		{
			String therest=CMParms.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")
					||therest.equalsIgnoreCase("SELF")
					||therest.equalsIgnoreCase("ALL"))))
			{
				mob.session().rawPrintln("but fail to specify the proper second parameter.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		Social soc2=CMLib.socials().fetchSocial(CMParms.combine(commands,2).toUpperCase(),true);
		if(soc2==null)
		{
			mob.tell("but fail to specify an EXISTING SOCIAL!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(mob.session().confirm("Are you sure you want to delete that social (y/N)? ","N"))
		{
			CMLib.socials().remove(soc2.name());
			CMLib.socials().save(mob);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The happiness of all mankind has just decreased!");
			Log.sysOut("SysopSocials",mob.Name()+" destroyed social "+soc2.name()+".");
		}
		else
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The happiness of all mankind has just increased!");
	}
	
	public static boolean destroyItem(MOB mob, Environmental dropThis, boolean quiet, boolean optimize)
	{
		String msgstr=null;
		int material=(dropThis instanceof Item)?((Item)dropThis).material():-1;
		if(!quiet)
		switch(material&RawMaterial.MATERIAL_MASK)
		{
		case RawMaterial.MATERIAL_LIQUID:
			msgstr="<S-NAME> pour(s) out <T-NAME>.";
			break;
		case RawMaterial.MATERIAL_PAPER:
			msgstr="<S-NAME> tear(s) up <T-NAME>.";
			break;
		case RawMaterial.MATERIAL_GLASS:
			msgstr="<S-NAME> smash(es) <T-NAME>.";
			break;
		default:
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_NOISYMOVEMENT,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MASK_ALWAYS|CMMsg.MSG_DEATH,CMMsg.MSG_NOISYMOVEMENT,msgstr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			return true;
		}
		if(dropThis instanceof Coins)
			((Coins)dropThis).putCoinsBack();
		if(dropThis instanceof RawMaterial)
			((RawMaterial)dropThis).rebundle();
		return false;
	}

	public void allQualify(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ALLQUALIFY EACH/ALL [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String eachOrAll=(String)commands.get(2);
		if((!eachOrAll.equalsIgnoreCase("each"))&&(!eachOrAll.equalsIgnoreCase("all")))
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ALLQUALIFY EACH/ALL [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}	
		String classD=CMParms.combine(commands,3);
		Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get(eachOrAll.toUpperCase().trim());
		if(!subMap.containsKey(classD.toUpperCase().trim()))
		{
			mob.tell("All-Qualify entry ("+eachOrAll+") ID '"+classD+"' does not exist! Try LIST ALLQUALIFYS");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		subMap.remove(classD.toUpperCase().trim());
		CMLib.ableMapper().saveAllQualifysFile(map);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The skill of the world just decreased!");
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((!CMSecurity.isAllowedContainsAny(mob,CMSecurity.SECURITY_CMD_GROUP))
		&&(!CMSecurity.isAllowedContainsAny(mob,mob.location(),CMSecurity.SECURITY_KILL_GROUP))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.BAN))
		&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NOPURGE)))
		{
			commands.removeElementAt(0);
			if(commands.size()==0)
			{
				mob.tell("Destroy what?");
				return false;
			}
			if(mob.location().fetchInhabitant(CMParms.combine(commands,0))!=null)
			{
				Command C=CMClass.getCommand("Kill");
				commands.insertElementAt("KILL",0);
				if(C!=null) C.execute(mob,commands,metaFlags);
				return false;
			}

			Vector V=new Vector();
			int maxToDrop=Integer.MAX_VALUE;
			
			if((commands.size()>1)
			&&(CMath.s_int((String)commands.firstElement())>0))
			{
				maxToDrop=CMath.s_int((String)commands.firstElement());
				commands.setElementAt("all",0);
			}

			String whatToDrop=CMParms.combine(commands,0);
			boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
			if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
			if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}
			int addendum=1;
			String addendumStr="";
			boolean doBugFix = true;
			while(doBugFix || ((allFlag)&&(addendum<=maxToDrop)))
			{
				doBugFix=false;
				Item dropThis=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,whatToDrop+addendumStr);
				if((dropThis==null)
				&&(V.size()==0)
				&&(addendumStr.length()==0)
				&&(!allFlag))
				{
					dropThis=mob.fetchItem(null,Wearable.FILTER_WORNONLY,whatToDrop);
					if(dropThis!=null)
					{
						int matType=dropThis.material()&RawMaterial.MATERIAL_MASK;
						if((matType!=RawMaterial.MATERIAL_GLASS)
						&&(matType!=RawMaterial.MATERIAL_LIQUID)
						&&(matType!=RawMaterial.MATERIAL_PAPER))
						{
							mob.tell(dropThis.Name()+" can not be easily destroyed.");
							return false;
						}
						else	
						if((!dropThis.amWearingAt(Wearable.WORN_HELD))&&(!dropThis.amWearingAt(Wearable.WORN_WIELD)))
						{
							mob.tell("You must remove that first.");
							return false;
						}
						else
						{
							CMMsg newMsg=CMClass.getMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
							if(mob.location().okMessage(mob,newMsg))
								mob.location().send(mob,newMsg);
							else
								return false;
						}
					}
				}
				if(dropThis==null) break;
				if((CMLib.flags().canBeSeenBy(dropThis,mob))
				&&(!V.contains(dropThis)))
					V.addElement(dropThis);
				addendumStr="."+(++addendum);
			}

			boolean didAnything=false;
			for(int i=0;i<V.size();i++)
			{
				if(destroyItem(mob,(Item)V.elementAt(i),false,true))
					didAnything=true;
				else
				if(V.elementAt(i) instanceof Coins)
					((Coins)V.elementAt(i)).putCoinsBack();
				else
				if(V.elementAt(i) instanceof RawMaterial)
					((RawMaterial)V.elementAt(i)).rebundle();
			}
			if(!didAnything)
			{
				if(V.size()==0)
					mob.tell("You don't seem to be carrying that.");
				else
					mob.tell("You can't destroy that easily...");
			}
			mob.location().recoverRoomStats();
			mob.location().recoverRoomStats();
			return false;
		}
		
		String commandType="";

		if(commands.size()>1)
		{
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}
		for(Enumeration<JournalsLibrary.CommandJournal> e=CMLib.journals().commandJournals();e.hasMoreElements();)
		{
			JournalsLibrary.CommandJournal CMJ=e.nextElement();
			if((CMJ.NAME().equals(commandType))
			&&(CMSecurity.isJournalAccessAllowed(mob,CMJ.NAME())))
			{
				int which=-1;
				if(commands.size()>2)
					which=CMath.s_int((String)commands.elementAt(2));
				List<JournalsLibrary.JournalEntry> entries = CMLib.database().DBReadJournalMsgs(CMJ.JOURNAL_NAME());
				
				if((which<=0)||(which>entries.size()))
					mob.tell("Please enter a valid "+CMJ.NAME().toLowerCase()+" number to delete.  Use LIST "+CMJ.NAME()+"S for more information.");
				else
				{
					JournalsLibrary.JournalEntry entry = entries.get(which-1);
					CMLib.database().DBDeleteJournal(CMJ.JOURNAL_NAME(),entry.key);
					mob.tell(CMJ.NAME().toLowerCase()+" deletion submitted.");
					
				}
				return true;
			}
		}
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			areas(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			rooms(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLASSES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			classes(mob,commands);
		}
		else
		if(commandType.equals("ABILITY")||commandType.equals("LANGUAGE")||commandType.equals("CRAFTSKILL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			abilities(mob,commands);
		}
		else
		if(commandType.equals("ALLQUALIFY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			allQualify(mob,commands);
		}
		else
		if(commandType.equals("COMPONENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COMPONENTS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			components(mob,commands);
		}
		else
		if(commandType.equals("EXPERTISE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.EXPERTISES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			expertises(mob,commands);
		}
		else
		if(commandType.equals("TITLE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TITLES)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			titles(mob,commands);
		}
		else
	if(commandType.equals("USER")||commandType.equals("PLAYER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			players(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			accounts(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDSOCIALS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
		}
		else
		if(commandType.equals("DISABLEFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)) 
				return errorOut(mob);
			String named=CMParms.combine(commands,2);
			if(!CMSecurity.isDisabledSearch(named.toUpperCase()))
				mob.tell("'"+named+"' is not disabled");
			else
			{
				mob.tell("'"+named+"' is no longer disabled");
				CMSecurity.setDisableVar(named.toUpperCase().trim(), true);
			}
			return false;
		}
		else
		if(commandType.equals("DEBUGFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN)) 
				return errorOut(mob);
			String named=CMParms.combine(commands,2);
			CMSecurity.DbgFlag flag = (CMSecurity.DbgFlag)CMath.s_valueOf(CMSecurity.DbgFlag.values(), named.toUpperCase().trim());
			if(flag==null)
			{
				mob.tell("'"+named+"' is not a valid flag.  Try: "+CMParms.toStringList(CMSecurity.DbgFlag.values()));
				return false;
			}
			if(!CMSecurity.isDebugging(flag))
				mob.tell("'"+named+"' is not debugging");
			else
			{
				mob.tell("'"+named+"' is no longer debugging");
				CMSecurity.setDebugVar(flag, true);
			}
			return false;
		}
		else
		if(commandType.equals("NOPURGE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NOPURGE)) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid player number to delete.  Use List nopurge for more information.");
			else
			{
				StringBuffer newNoPurge=new StringBuffer("");
				List<String> protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
				if((protectedOnes!=null)&&(protectedOnes.size()>0))
					for(int b=0;b<protectedOnes.size();b++)
					{
						String B=protectedOnes.get(b);
						if(((b+1)!=which)&&(B.trim().length()>0))
							newNoPurge.append(B+"\n");
					}
				Resources.updateFileResource("::protectedplayers.ini",newNoPurge);
				mob.tell("Ok.");
			}
		}
		else
		if(commandType.equals("HOLIDAY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS)) return errorOut(mob);
			String name=CMParms.combine(commands,2);
			int num=-1;
			if(CMath.isInteger(name))
				num=CMath.s_int(name);
			else
			if(name.length()>0)
				num=CMLib.quests().getHolidayIndex(name);
			if(num<0)
			{
				mob.tell("HOLIDAY '"+name+"' not found. Try LIST HOLIDAYS.");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
				return false;
			}
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mob.tell(CMLib.quests().deleteHoliday(num));
			Log.sysOut("CreateEdit",mob.Name()+" deleted Holiday "+name+".");
		}
		else
		if(commandType.equals("TICKS"))
		{
			if(!CMSecurity.isASysOp(mob)) return errorOut(mob);
			String which=CMParms.combine(commands,2);
			List<Tickable> V=null;
			if(which.length()>0)
			{
				V=CMLib.threads().getNamedTickingObjects(which);
				if(V.size()==0) V=null;
			}
			if(V==null)
				mob.tell("Please enter a valid ticking object name to destroy.  Use List ticks for a list of groups and objects.");
			else
			{
				StringBuffer list=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					list.append(V.get(v).name()+", ");
				if((mob.session()!=null)&&(mob.session().confirm("Destroy the following ticking objects: "+list.substring(0,list.length()-2)+"  (y/N)? ","N")))
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
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.BAN)) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid ban number to delete.  Use List Banned for more information.");
			else
			{
				CMSecurity.unban(which);
				mob.tell("Ok.");
			}
		}
		else
		if(commandType.equals("THREAD"))
		{
			if(!CMSecurity.isASysOp(mob)) return errorOut(mob);
			String which=CMParms.combine(commands,2);
			Thread whichT=null;
			if(which.length()>0)
				whichT=findThread(which);
			if(whichT==null)
				mob.tell("Please enter a valid thread name to destroy.  Use List threads for a list.");
			else
			{
				CMLib.killThread(whichT,500,1);
				Log.sysOut("CreateEdit",mob.Name()+" destroyed thread "+whichT.getName()+".");
				mob.tell("Stop sent to: "+whichT.getName()+".");
			}
		}
		else
		if(commandType.startsWith("SESSION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.BOOT)) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=CMath.s_int((String)commands.elementAt(2));
			Session S=CMLib.sessions().getAllSessionAt(which);
			if(S==null)
				mob.tell("Please enter a valid session number to delete.  Use SESSIONS for more information.");
			else
			{
				CMLib.sessions().stopSessionAtAllCosts(S);
				if(S.getStatus()==Session.SessionStatus.LOGOUTFINAL)
					mob.tell("Ok.");
				else
					mob.tell("Failed to gracefully shutdown: "+S.getStatus().toString()+", but a forcable stop was issued.");
			}
		}
		else
		if(commandType.equals("JOURNAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS)) return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell("Destroy which journal? Try List Journal");
				return errorOut(mob);
			}
			List<String> V=CMLib.database().DBReadJournals();
			String name=CMParms.combine(commands,2);
			int which=-1;
			for(int v=0;v<V.size();v++)
				if(V.get(v).equalsIgnoreCase(name))
				{
					name=V.get(v);
					which=v;
					break;
				}
			if(which<0)
			for(int v=0;v<V.size();v++)
				if(V.get(v).startsWith(name))
				{
					name=V.get(v);
					which=v;
					break;
				}
			if(which<0)
				mob.tell("Please enter a valid journal name to delete.  Use List Journals for more information.");
			else
			if(mob.session().confirm("This will destroy all "+CMLib.database().DBCountJournal(name,null,null)+" messages.  Are you SURE (y/N)? ","N"))
			{
				CMLib.database().DBDeleteJournal(name,null);
				mob.tell("It is done.");
			}
		}
		else
		if(commandType.equals("FACTION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which faction?  Use list factions.");
			else
			{
				String name=CMParms.combine(commands,2);
				Faction F=CMLib.factions().getFaction(name);
				if(F==null) F=CMLib.factions().getFactionByName(name);
				if(F==null)
					mob.tell("Faction '"+name+"' is unknown.  Try list factions.");
				else
				if((!mob.isMonster())&&(mob.session().confirm("Destroy faction '"+F.factionID()+"' -- this could have unexpected consequences in the future -- (N/y)? ","N")))
				{
					try
					{
						CMFile F2=new CMFile(Resources.makeFileResourceName(CMLib.factions().makeFactionFilename(F.factionID())),null);
						if(F2.exists()) 
							F2.deleteAll();
						else
							throw new IOException("Could not delete "+F2.getAbsolutePath());
						F.destroy();
						Log.sysOut("CreateEdit",mob.Name()+" destroyed Faction "+F.name()+" ("+F.factionID()+").");
						mob.tell("Faction File '"+F.factionID()+"' deleted.");
					}
					catch(Exception e)
					{
						Log.errOut("CreateEdit",e);
						mob.tell("Faction '"+F.factionID()+"' could NOT be deleted.");
					}
				}
			}
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
		else
		if(commandType.equals("POLL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.POLLS)) return errorOut(mob);
			String name=CMParms.combine(commands,2);
			Poll P=null;
			if(CMath.isInteger(name))
				P=CMLib.polls().getPoll(CMath.s_int(name)-1);
			else
			if(name.length()>0)
				P=CMLib.polls().getPoll(name);
			if(P==null)
			{
				mob.tell("POLL '"+name+"' not found. Try LIST POLLS.");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
				return false;
			}
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if((mob.session()!=null)&&(mob.session().confirm("Destroy POLL "+P.getName()+", are you SURE? (Y/n)? ","Y")))
			{
				CMLib.polls().deletePoll(P);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world has grown a bit more certain.^?");
				Log.sysOut("CreateEdit",mob.Name()+" modified Poll "+P.getName()+".");
			}
			else
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which quest?  Use list quests.");
			else
			{
				String name=CMParms.combine(commands,2);
				Quest Q=null;
				if(CMath.isInteger(name))
				{
					Q=CMLib.quests().fetchQuest(CMath.s_int(name)-1);
					if(Q!=null) name=Q.name();
				}
				if(Q==null) Q=CMLib.quests().fetchQuest(name);
				if(Q==null)
					mob.tell("Quest '"+name+"' is unknown.  Try list quests.");
				else
				{
					if(Q.running()&&(!Q.stopping())) Q.stopQuest();
					mob.tell("Quest '"+Q.name()+"' is destroyed!");
					CMLib.quests().delQuest(Q);
				}
			}
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which clan?  Use clanlist.");
			else
			{
				String name=CMParms.combine(commands,2);
				Clan C=CMLib.clans().findClan(name);
				if(C==null)
					mob.tell("Clan '"+name+"' is unknown.  Try clanlist.");
				else
				{
					mob.tell("Clan '"+C.name()+"' is destroyed!");
					C.destroyClan();
					Log.sysOut("CreateEdit","Clan '"+C.name()+" destroyed by "+mob.Name()+".");
				}
			}
		}
		else
		if(commandType.equals("GOVERNMENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS)) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which government?  Use list governments.");
			else
			{
				String name=CMParms.combine(commands,2);
				ClanGovernment G=null;
				for(ClanGovernment g : CMLib.clans().getStockGovernments())
					if(g.getName().equalsIgnoreCase(name))
						G=g;
				if(G==null)
					mob.tell("Government '"+name+"' is unknown.  Try list governments.");
				else
				if(CMLib.clans().removeGovernment(G))
				{
					mob.tell("Government '"+G.getName()+"' is destroyed!");
					CMLib.clans().reSaveGovernmentsXML();
					Log.sysOut("CreateEdit","Government '"+G.getName()+" destroyed by "+mob.Name()+".");
				}
				else
					mob.tell("You can't delete the last remaining clan government.");
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			Environmental thang=mob.location().fetchFromRoomFavorItems(null,allWord);
			if(thang==null)
				thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Wearable.FILTER_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				commands.insertElementAt("ITEM",1);
				execute(mob,commands,metaFlags);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.insertElementAt("MOB",1);
				else
					commands.insertElementAt("USER",1);
				execute(mob,commands,metaFlags);
			}
			else
			{
				Room theRoom=null;
				if(allWord.length()>0)
				{
					try{ theRoom=CMLib.map().getRoom(allWord); }catch(NoSuchElementException e){}
				}
				if(theRoom!=null)
				{
					commands=new Vector();
					commands.addElement("DESTROY");
					commands.addElement("ROOM");
					commands.addElement(theRoom.roomID());
					execute(mob,commands,metaFlags);
				}
				else
				{
					if(Directions.getGoodDirectionCode(allWord)>=0)
					{
						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("ROOM");
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);

						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("EXIT");
						commands.addElement(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if(CMLib.socials().fetchSocial(allWord,true)!=null)
					{
						commands.insertElementAt("SOCIAL",1);
						execute(mob,commands,metaFlags);
					}
					else
					mob.tell(
						"\n\rYou cannot destroy a '"+commandType+"'. "
						+"However, you might try an "
						+"EXIT, ITEM, AREA, USER, MOB, QUEST, FACTION, SESSION, TICKS, THREAD, HOLIDAY, JOURNAL, SOCIAL, CLASS, ABILITY, LANGUAGE, COMPONENT, RACE, EXPERTISE, TITLE, CLAN, BAN, GOVERNMENT, NOPURGE, BUG, TYPO, IDEA, POLL, DEBUGFLAG, DISABLEFLAG, or a ROOM.");
				}
			}
		}
		return false;
	}
	public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
	public boolean canBeOrdered(){return false;}

	
}
