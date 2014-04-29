package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2000-2014 Bo Zimmerman

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
public class Modify extends StdCommand
{
	public Modify(){}

	private final String[] access={"MODIFY","MOD"};
	@Override public String[] getAccessWords(){return access;}

	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ITEM [ITEM NAME](@ room/[MOB NAME]) [LEVEL, ABILITY, REJUV, USES, MISC, ?] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
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
						mob.tell("MOB or Container '"+rest+"' not found.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return;
					}
				}
				else
				{
					srchMob=M;
					srchRoom=null;
				}
			}
		}
		String command="";
		if(commands.size()>3)
			command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);

		Item modItem=null;
		if((srchMob!=null)&&(srchRoom!=null))
			modItem=(Item)srchRoom.fetchFromMOBRoomFavorsItems(srchMob,srchContainer,itemID,Wearable.FILTER_ANY);
		else
		if(srchMob!=null)
			modItem=srchMob.findItem(itemID);
		else
		if(srchRoom!=null)
		{
			modItem=srchRoom.findItem(srchContainer, itemID);
			if(modItem==null)
				modItem=srchRoom.findItem(itemID);
		}
		if(modItem==null)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		mob.location().showOthers(mob,modItem,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");

		final Item copyItem=(Item)modItem.copyOf();
		if(command.equals("LEVEL"))
		{
			final int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.basePhyStats().setLevel(newLevel);
				modItem.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			final int newAbility=CMath.s_int(restStr);
			modItem.basePhyStats().setAbility(newAbility);
			modItem.recoverPhyStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("HEIGHT"))
		{
			final int newAbility=CMath.s_int(restStr);
			modItem.basePhyStats().setHeight(newAbility);
			modItem.recoverPhyStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			final int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.basePhyStats().setRejuv(newRejuv);
				modItem.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
			else
			{
				modItem.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				modItem.recoverPhyStats();
				mob.tell(modItem.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("USES"))
		{
			final int newUses=CMath.s_int(restStr);
			if(newUses>=0)
			{
				modItem.setUsesRemaining(newUses);
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modItem.isGeneric())
				CMLib.genEd().genMiscSet(mob,modItem);
			else
				modItem.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(CMLib.coffeeMaker().isAnyGenStat(modItem, command))
		{
			CMLib.coffeeMaker().setAnyGenStat(modItem,command, restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if((command.length()==0)&&(modItem.isGeneric()))
		{
			CMLib.genEd().genMiscSet(mob,modItem);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		{
			final STreeSet<String> set=new STreeSet<String>();
			set.addAll(CMParms.parseCommas("LEVEL,ABILITY,HEIGHT,REJUV,USES,MISC",true));
			set.addAll(CMLib.coffeeMaker().getAllGenStats(modItem));
			mob.tell("...but failed to specify an aspect.  Try one of: "+CMParms.toStringList(set));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
		if(!copyItem.sameAs(modItem))
			Log.sysOut("Items",mob.Name()+" modified item "+modItem.ID()+".");
		copyItem.destroy();
	}

	protected void flunkRoomCmd(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION, AFFECTS, BEHAVIORS, CLASS, XGRID, YGRID, ?] [TEXT]\n\r");
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}

	protected void flunkAreaCmd(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY AREA [NAME, DESCRIPTION, CLIMATE, FILE, AFFECTS, BEHAVIORS, ADDSUB, DELSUB, XGRID, YGRID, ?] [TEXT]\n\r");
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}

	public void rooms(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around the room.");
		if(commands.size()==2)
		{
			final Room oldRoom=(Room)mob.location().copyOf();
			final Room newRoom=CMLib.genEd().modifyRoom(mob,mob.location());
			if((!oldRoom.sameAs(newRoom))&&(!newRoom.amDestroyed()))
			{
				CMLib.database().DBUpdateRoom(newRoom);
				newRoom.showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
				Log.sysOut("Rooms",mob.Name()+" modified room "+newRoom.roomID()+".");
			}
			oldRoom.destroy();
			newRoom.getArea().fillInAreaRoom(newRoom);
			return;
		}
		if(commands.size()<3) { flunkRoomCmd(mob); return;}

		final String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr="";
		if(commands.size()>=3)
			restStr=CMParms.combine(commands,3);

		if(command.equalsIgnoreCase("AREA"))
		{
			if(commands.size()<4) { flunkRoomCmd(mob); return;}
			Area A=CMLib.map().getArea(restStr);
			boolean reid=false;
			if(A==null)
			{
				if(!mob.isMonster())
				{
					if(mob.session().confirm("\n\rThis command will create a BRAND NEW AREA\n\r with Area code '"+restStr+"'.  Are you SURE (y/N)?","N"))
					{
						String areaType="";
						int tries=0;
						while((areaType.length()==0)&&((++tries)<10))
						{
							areaType=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
							if(CMClass.getAreaType(areaType)==null)
							{
								mob.session().println("Invalid area type! Valid ones are:");
								mob.session().println(CMLib.lister().reallyList(mob,CMClass.areaTypes()).toString());
								areaType="";
							}
						}
						if(areaType.length()==0) areaType="StdArea";
						A=CMClass.getAreaType(areaType);
						A.setName(restStr);
						CMLib.map().addArea(A);
						CMLib.database().DBCreateArea(A);
						mob.location().setArea(A);
						CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);
						reid=true;
					}
					mob.location().showHappens(CMMsg.MSG_OK_ACTION,"This entire area twitches.\n\r");
				}
				else
				{
					mob.tell("Sorry Charlie!");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
			}
			else
			{
				mob.location().setArea(A);
				if(A.getRandomProperRoom()!=null)
					reid=true;
				else
					CMLib.database().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"This area twitches.\n\r");
			}

			if(reid)
			{
				Room R=mob.location();
				final String oldID=R.roomID();
				synchronized(("SYNC"+R.roomID()).intern())
				{
					R=CMLib.map().getRoom(R);
					final Room reference=CMLib.map().findConnectingRoom(R);
					String checkID=null;
					if(A!=null)
					{
						if(reference!=null)
							checkID=A.getNewRoomID(reference,CMLib.map().getRoomDir(reference,R));
						else
							checkID=A.getNewRoomID(R,-1);
						mob.location().setRoomID(checkID);
						CMLib.database().DBReCreate(R,oldID);
					}
				}
			}
		}
		else
		if(command.equalsIgnoreCase("NAME"))
		{
			if(commands.size()<4) { flunkRoomCmd(mob); return;}
			mob.location().setDisplayText(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<4) { flunkRoomCmd(mob); return;}
			final Room newRoom=CMClass.getLocale(restStr);
			if(newRoom==null)
			{
				mob.tell("'"+restStr+"' is not a valid room locale.");
				return;
			}
			CMLib.genEd().changeRoomType(mob.location(),newRoom);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("XGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkRoomCmd(mob); return;}
			((GridLocale)mob.location()).setXGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("YGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkRoomCmd(mob); return;}
			((GridLocale)mob.location()).setYGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			if(commands.size()<4) { flunkRoomCmd(mob); return;}
			mob.location().setDescription(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("AFFECTS"))
		{
			CMLib.genEd().genAffects(mob,mob.location(),1,1);
			mob.location().recoverPhyStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("BEHAVIORS"))
		{
			CMLib.genEd().genBehaviors(mob,mob.location(),1,1);
			mob.location().recoverPhyStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(CMLib.coffeeMaker().isAnyGenStat(mob.location(), command))
		{
			CMLib.coffeeMaker().setAnyGenStat(mob.location(),command, restStr);
			mob.location().recoverPhyStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		{
			final STreeSet<String> set=new STreeSet<String>();
			set.addAll(CMParms.parseCommas("NAME,AREA,DESCRIPTION,AFFECTS,BEHAVIORS,CLASS,XGRID,YGRID",true));
			set.addAll(CMLib.coffeeMaker().getAllGenStats(mob.location()));
			mob.tell("...but failed to specify an aspect.  Try one of: "+CMParms.toStringList(set));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		mob.location().recoverRoomStats();
		Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
	}

	public void accounts(MOB mob, Vector commands)
		throws IOException
	{
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around the heavens.");
		PlayerAccount theAccount = null;
		String oldName = null;
		if(commands.size()==2)
		{
			theAccount=mob.playerStats().getAccount();
			oldName=theAccount.getAccountName();
			CMLib.genEd().modifyAccount(mob,theAccount);
		}
		else
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ACCOUNT ([NAME])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		{
			final String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
			theAccount = CMLib.players().getLoadAccount(accountName);
			if(theAccount==null)
			{
				mob.tell("There is no account called '"+accountName+"'!\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
			oldName=theAccount.getAccountName();
			CMLib.genEd().modifyAccount(mob,theAccount);
			mob.location().recoverRoomStats();
		}
		Log.sysOut("Modify",mob.Name()+" modified account "+theAccount.getAccountName()+".");
		if(!oldName.equals(theAccount.getAccountName()))
		{
			final Vector<MOB> V=new Vector<MOB>();
			for(final Enumeration<String> es=theAccount.getPlayers();es.hasMoreElements();)
			{
				final String playerName=es.nextElement();
				final MOB playerM=CMLib.players().getLoadPlayer(playerName);
				if((playerM!=null)&&(!CMLib.flags().isInTheGame(playerM,true)))
					V.addElement(playerM);
			}
			final PlayerAccount acc = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
			acc.setAccountName(oldName);
			CMLib.database().DBDeleteAccount(acc);
			CMLib.database().DBCreateAccount(theAccount);
			for(final MOB playerM : V)
				CMLib.database().DBUpdatePlayerPlayerStats(playerM);
		}
		CMLib.database().DBUpdateAccount(theAccount);
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location()==null) return;
		if(mob.location().getArea()==null) return;
		Area myArea=mob.location().getArea();

		String oldName=myArea.Name();
		final Vector allMyDamnRooms=new Vector();
		for(final Enumeration e=myArea.getCompleteMap();e.hasMoreElements();)
			allMyDamnRooms.addElement(e.nextElement());

		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around wildly.");
		Resources.removeResource("HELP_"+myArea.Name().toUpperCase());
		if(commands.size()==2)
			CMLib.genEd().modifyGenArea(mob,myArea);
		else
		if((commands.size()==3)&&(CMLib.map().getArea((String)commands.elementAt(2))!=null))
		{
			myArea=CMLib.map().getArea((String)commands.elementAt(2));
			oldName=myArea.Name();
			CMLib.genEd().modifyGenArea(mob,myArea);
		}
		else
		{
			if(commands.size()<3) { flunkAreaCmd(mob); return;}

			String command=((String)commands.elementAt(2)).toUpperCase();
			final STreeSet<String> helpSet=new STreeSet<String>();
			helpSet.addAll(CMParms.parseCommas("NAME,DESCRIPTION,CLIMATE,FILE,AFFECTS,BEHAVIORS,ADDSUB,DELSUB,XGRID,YGRID,PASSIVE,ACTIVE,FROZEN,STOPPED",true));
			helpSet.addAll(CMLib.coffeeMaker().getAllGenStats(myArea));
			if((commands.size()>3)&&(!helpSet.contains(command)))
			{
				final Area possibleArea=CMLib.map().getArea(command);
				if(possibleArea!=null)
				{
					myArea=possibleArea;
					oldName=possibleArea.Name();
					commands.remove(2);
					command=((String)commands.elementAt(2)).toUpperCase();
				}
			}
			String restStr="";
			if(commands.size()>=3)
				restStr=CMParms.combine(commands,3);

			if(command.equalsIgnoreCase("NAME"))
			{
				if(commands.size()<4) { flunkAreaCmd(mob); return;}
				myArea.setName(restStr);
			}
			else
			if(command.equalsIgnoreCase("PASSIVE"))
			{
				myArea.setAreaState(Area.State.PASSIVE);
			}
			else
			if(command.equalsIgnoreCase("ACTIVE"))
			{
				myArea.setAreaState(Area.State.ACTIVE);
			}
			else
			if(command.equalsIgnoreCase("FROZEN"))
			{
				myArea.setAreaState(Area.State.FROZEN);
			}
			else
			if(command.equalsIgnoreCase("STOPPED"))
			{
				myArea.setAreaState(Area.State.STOPPED);
			}
			else
			if(command.equalsIgnoreCase("DESC"))
			{
				if(commands.size()<4) { flunkAreaCmd(mob); return;}
				myArea.setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase("FILE"))
			{
				if(commands.size()<4) { flunkAreaCmd(mob); return;}
				myArea.setArchivePath(restStr);
			}
			else
			if((command.equalsIgnoreCase("XGRID"))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkAreaCmd(mob); return;}
				((GridZones)myArea).setXGridSize(CMath.s_int(restStr));
			}
			else
			if((command.equalsIgnoreCase("YGRID"))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkAreaCmd(mob); return;}
				((GridZones)myArea).setYGridSize(CMath.s_int(restStr));
			}
			else
			if(command.equalsIgnoreCase("CLIMATE"))
			{
				if(commands.size()<4) { flunkAreaCmd(mob); return;}
				int newClimate=0;
				for(int i=0;i<restStr.length();i++)
					switch(Character.toUpperCase(restStr.charAt(i)))
					{
					case 'R':
						newClimate=newClimate|Places.CLIMASK_WET;
						break;
					case 'H':
						newClimate=newClimate|Places.CLIMASK_HOT;
						break;
					case 'C':
						newClimate=newClimate|Places.CLIMASK_COLD;
						break;
					case 'W':
						newClimate=newClimate|Places.CLIMASK_WINDY;
						break;
					case 'D':
						newClimate=newClimate|Places.CLIMASK_WINDY;
						break;
					case 'N':
						// do nothing
						break;
					default:
						mob.tell("Invalid CLIMATE code: '"+restStr.charAt(i)+"'.  Valid codes include: R)AINY, H)OT, C)OLD, D)RY, W)INDY, N)ORMAL.\n\r");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
						return;
					}
				myArea.setClimateType(newClimate);
			}
			else
			if(command.equalsIgnoreCase("ADDSUB"))
			{
				if((commands.size()<4)||(!CMLib.players().playerExists(restStr)))
				{
					mob.tell("Unknown or invalid username given.\n\r");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.addSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("DELSUB"))
			{
				if((commands.size()<4)||(!myArea.amISubOp(restStr)))
				{
					mob.tell("Unknown or invalid staff name given.  Valid names are: "+myArea.getSubOpList()+".\n\r");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				}
				myArea.delSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("AFFECTS"))
			{
				CMLib.genEd().genAffects(mob,myArea,1,1);
				myArea.recoverPhyStats();
			}
			else
			if(command.equalsIgnoreCase("BEHAVIORS"))
			{
				CMLib.genEd().genBehaviors(mob,myArea,1,1);
				myArea.recoverPhyStats();
			}
			else
			if(CMLib.coffeeMaker().isAnyGenStat(myArea, command))
			{
				CMLib.coffeeMaker().setAnyGenStat(myArea,command, restStr);
				myArea.recoverPhyStats();
			}
			else
			{
				mob.tell("...but failed to specify an aspect.  Try one of: "+CMParms.toStringList(helpSet));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
				return;
			}
		}

		if((!myArea.Name().equals(oldName))&&(!mob.isMonster()))
		{
			if(mob.session().confirm("Is changing the name of this area really necessary (y/N)?","N"))
			{
				for(final Enumeration r=myArea.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					synchronized(("SYNC"+R.roomID()).intern())
					{
						R=CMLib.map().getRoom(R);
						if((R.roomID().startsWith(oldName+"#"))
						&&(CMLib.map().getRoom(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1))==null))
						{
							R=CMLib.map().getRoom(R);
							final String oldID=R.roomID();
							R.setRoomID(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1));
							CMLib.database().DBReCreate(R,oldID);
						}
						else
							CMLib.database().DBUpdateRoom(R);
					}
				}
			}
			else
				myArea.setName(oldName);
		}
		else
			myArea.setName(oldName);
		myArea.recoverPhyStats();
		mob.location().recoverRoomStats();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		if(myArea.name().equals(oldName))
			CMLib.database().DBUpdateArea(myArea.Name(),myArea);
		else
		{
			CMLib.database().DBUpdateArea(oldName,myArea);
			CMLib.map().renameRooms(myArea,oldName,allMyDamnRooms);
		}
		Log.sysOut("Rooms",mob.Name()+" modified area "+myArea.Name()+".");
	}

	public void quests(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
			mob.tell("modify which quest?  Use list quests.");
		else
		{
			int cmdDex=-1;
			final String[] CMDS={"START","STOP","ENABLE","DISABLE"};
			if(commands.size()>3)
			{
				cmdDex=CMParms.indexOf(CMDS,((String)commands.lastElement()).toUpperCase());
				if(cmdDex>=0)
					commands.removeElementAt(commands.size()-1);
			}
			String name=CMParms.combine(commands,2);
			Quest Q=null;
			if(CMath.isInteger(name))
			{
				Q=CMLib.quests().fetchQuest(CMath.s_int(name)-1);
				if(Q!=null) name=Q.name();
			}
			if(Q==null) Q=CMLib.quests().fetchQuest(name);
			if(Q==null)
			{
				mob.tell("Quest '"+name+"' is unknown.  Try list quests.");
				return;
			}
			else
			if(!mob.isMonster())
			{
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around "+Q.name()+".");
				int showFlag=-1;
				if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
					showFlag=-999;
				boolean ok=false;
				while(!ok)
				{
					int showNumber=0;
					int doCmd=cmdDex;
					String newScript=null;
					if((doCmd<0)&&(CMLib.genEd().promptToggle(mob,++showNumber,showFlag,"Started: "+Q.running())))
						doCmd=Q.running()?1:0;
					if((doCmd<0)&&(CMLib.genEd().promptToggle(mob,++showNumber,showFlag,"Enabled: "+(!Q.suspended()))))
						doCmd=Q.suspended()?2:3;
					if(doCmd<0)
					{
						final String oldScript=Q.script();
						newScript=CMLib.genEd().prompt(mob,oldScript,++showNumber,showFlag,"Script",false,false,CMLib.help().getHelpText("QUESTS",mob,true).toString(),null,null);
						if(!newScript.equals(oldScript))
						{
							Q.setScript(newScript,true);
							boolean revert=false;
							if(Q.name().length()==0)
							{
								mob.tell("You must specify a VALID quest string.  This one contained no name.");
								revert=true;
							}
							else
							if(Q.duration()<0)
							{
								mob.tell("You must specify a VALID quest string.  This one contained no duration.");
								revert=true;
							}
							else
							for(int q=0;q<CMLib.quests().numQuests();q++)
							{
								final Quest Q1=CMLib.quests().fetchQuest(q);
								if(Q1.name().equalsIgnoreCase(Q.name())&&(Q1!=Q))
								{
									mob.tell("A quest with that name already exists.");
									revert=true;
								}
							}
							if(revert)
								Q.setScript(oldScript,true);
							else
								CMLib.quests().save();
						}
					}
					switch(doCmd)
					{
					case 0:
					{
						if(Q.running())
							mob.tell("That quest is already running.");
						else
						{
							Q.startQuest();
							if((!Q.running())&&(Q.getSpawn()!=Quest.SPAWN_ANY))
								mob.tell("Quest '"+Q.name()+"' NOT started -- check your mud.log for errors.");
							else
								mob.tell("Quest '"+Q.name()+"' started.");
						}
						break;
					}
					case 1:
					{
						if(!Q.running())
							mob.tell("That quest is not running.");
						else
						{
							Q.stopQuest();
							if(!Q.running())
								mob.tell("Quest '"+Q.name()+"' stopped.");
							else
								mob.tell("Quest '"+Q.name()+"' NOT stopped -- check your mud.log for errors.");
						}
						break;
					}
					case 2:
					{
						if(!Q.suspended())
							mob.tell("That quest is not disabled.");
						else
						{
							Q.setSuspended(false);
							CMLib.database().DBUpdateQuest(Q);
							mob.tell("Quest '"+Q.name()+"' enabled.");
						}
						break;
					}
					case 3:
					{
						if(Q.suspended())
							mob.tell("That quest is already disabled.");
						else
						{
							if(Q.running())
								Q.stopQuest();
							Q.setSuspended(true);
							CMLib.database().DBUpdateQuest(Q);
							mob.tell("Quest '"+Q.name()+"' disabled.");
						}
						break;
					}
					}

					if((showFlag<-900)||(cmdDex>=0)){ ok=true; break;}
					if(showFlag>0){ showFlag=-1; continue;}
					showFlag=CMath.s_int(mob.session().prompt("Edit which? ",""));
					if(showFlag<=0)
					{
						showFlag=-1;
						ok=true;
					}
				}
				Log.sysOut("Rooms",mob.Name()+" modified quest "+Q.name()+".");
			}
		}
	}

	public void updateChangedExit(MOB mob, Room baseRoom, Exit thisExit, Exit prevExit)
	{
		thisExit.recoverPhyStats();
		CMLib.database().DBUpdateExits(baseRoom);
		try
		{
			for(final Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room room=(Room)r.nextElement();
				synchronized(("SYNC"+room.roomID()).intern())
				{
					room=CMLib.map().getRoom(room);
					if(room != null)
					{
						for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						{
							final Exit exit=room.getRawExit(d);
							if((exit!=null)&&(exit==thisExit))
							{
								CMLib.database().DBUpdateExits(room);
								room.getArea().fillInAreaRoom(room);
								break;
							}
						}
					}
				}
			}
		}catch(final NoSuchElementException e){}
		if(!prevExit.sameAs(thisExit))
			Log.sysOut("CreateEdit",mob.Name()+" modified exit "+thisExit.ID()+".");
		prevExit.destroy();
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thisExit.name()+" shake(s) under the transforming power.");
		baseRoom.getArea().fillInAreaRoom(baseRoom);
	}

	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] (TEXT, ?) (VALUE)\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		final int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.LETTERS()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		final Exit thisExit=mob.location().getRawExit(direction);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit '"+((String)commands.elementAt(2))+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		final boolean useShipDirs=(mob.location() instanceof SpaceShip)||(mob.location().getArea() instanceof SpaceShip);
		final String inDirName=useShipDirs?Directions.getShipInDirectionName(direction):Directions.getInDirectionName(direction);
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around to the "+inDirName+".");
		final Exit copyExit=(Exit)thisExit.copyOf();
		if(thisExit.isGeneric() && (commands.size()<5))
		{
			CMLib.genEd().modifyGenExit(mob,thisExit);
			updateChangedExit(mob,mob.location(),thisExit,copyExit);
			return;
		}

		if(commands.size()<5)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] (TEXT, ?) (VALUE)\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		final String command=((String)commands.elementAt(3)).toUpperCase();
		final String restStr=CMParms.combine(commands,4);

		if(command.equalsIgnoreCase("text"))
		{
			if(thisExit.isGeneric())
				CMLib.genEd().modifyGenExit(mob,thisExit);
			else
				thisExit.setMiscText(restStr);
		}
		else
		if(CMLib.coffeeMaker().isAnyGenStat(thisExit, command))
		{
			CMLib.coffeeMaker().setAnyGenStat(thisExit,command, restStr);
			thisExit.recoverPhyStats();
		}
		else
		{
			final STreeSet<String> set=new STreeSet<String>();
			set.addAll(CMParms.parseCommas("TEXT",true));
			set.addAll(CMLib.coffeeMaker().getAllGenStats(thisExit));
			mob.tell("...but failed to specify an aspect.  Try one of: "+CMParms.toStringList(set));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		updateChangedExit(mob,mob.location(),thisExit,copyExit);
	}

	public boolean races(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		final String raceID=CMParms.combine(commands,2);
		final Race R=CMClass.getRace(raceID);
		if(R==null)
		{
			mob.tell("'"+raceID+"' is an invalid race id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(R.isGeneric()))
		{
			mob.tell("'"+R.ID()+"' is not generic, and may not be modified as it is.  Use CREATE RACE "+R.ID()+" to convert it to a generic race.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around all "+R.name()+"s.");
		CMLib.genEd().modifyGenRace(mob,R);
		CMLib.database().DBDeleteRace(R.ID());
		CMLib.database().DBCreateRace(R.ID(),R.racialParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,R.name()+"'s everywhere shake under the transforming power!");
		return true;
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
		final String eachOrAll=(String)commands.get(2);
		if((!eachOrAll.equalsIgnoreCase("each"))&&(!eachOrAll.equalsIgnoreCase("all")))
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ALLQUALIFY EACH/ALL [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		final String classD=CMParms.combine(commands,3);
		final Ability A=CMClass.getAbility(classD);
		if(A==null)
		{
			mob.tell("Ability with the ID '"+classD+"' does not exist! Try LIST ABILITIES.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get(eachOrAll.toUpperCase().trim());
		if(!subMap.containsKey(classD.toUpperCase().trim()))
		{
			mob.tell("All-Qualify entry ("+eachOrAll+") ID '"+A.ID()+"' does not exist!  Try CREATE, or LIST ALLQUALIFYS.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		final AbilityMapper.AbilityMapping mapped = CMLib.genEd().modifyAllQualifyEntry(mob,eachOrAll.toUpperCase().trim(),A);
		map=CMLib.ableMapper().getAllQualifiesMap(null);
		subMap=map.get(eachOrAll.toUpperCase().trim());
		subMap.put(A.ID().toUpperCase().trim(), mapped);
		CMLib.ableMapper().saveAllQualifysFile(map);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The skill of the world just changed!");
	}

	public boolean classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY CLASS [CLASS ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		final CharClass C=CMClass.getCharClass(classID);
		if(C==null)
		{
			mob.tell("'"+classID+"' is an invalid class id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell("'"+C.ID()+"' is not generic, and may not be modified as it is.  Use CREATE CLASS "+C.ID()+" to convert it to a generic character class.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around all "+C.name()+"s.");
		CMLib.genEd().modifyGenClass(mob,C);
		CMLib.database().DBDeleteClass(C.ID());
		CMLib.database().DBCreateClass(C.ID(),C.classParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,C.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public boolean abilities(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ABILITY [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(classID);
		if(A==null)
		{
			mob.tell("'"+classID+"' is an invalid ability id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(A.isGeneric()))
		{
			mob.tell("'"+A.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(A instanceof Language)
		{
			mob.tell("'"+A.ID()+"' is a language.  Try MODIFY LANGUAGE.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(A instanceof ItemCraftor)
		{
			mob.tell("'"+A.ID()+"' is a crafting skill.  Try MODIFY CRAFTSKILL.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around all "+A.name()+"s.");
		CMLib.genEd().modifyGenAbility(mob,A);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenAbility",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,A.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public boolean languages(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY LANGUAGE [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(classID);
		if(A==null)
		{
			mob.tell("'"+classID+"' is an invalid ability id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(A.isGeneric()))
		{
			mob.tell("'"+A.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(A instanceof ItemCraftor)
		{
			mob.tell("'"+A.ID()+"' is a crafting skill.  Try MODIFY CRAFTSKILL.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(A instanceof Language))
		{
			mob.tell("'"+A.ID()+"' is not a language.  Try MODIFY ABILITY.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around all "+A.name()+"s.");
		CMLib.genEd().modifyGenLanguage(mob,(Language)A);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenLanguage",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,A.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public boolean craftSkills(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY CRAFTSKILL [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		final String classID=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(classID);
		if(A==null)
		{
			mob.tell("'"+classID+"' is an invalid ability id.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(A.isGeneric()))
		{
			mob.tell("'"+A.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(A instanceof Language)
		{
			mob.tell("'"+A.ID()+"' is a crafting skill.  Try MODIFY LANGUAGE.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		if(!(A instanceof ItemCraftor))
		{
			mob.tell("'"+A.ID()+"' is not a crafting skill.  Try MODIFY ABILITY.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around all "+A.name()+"s.");
		CMLib.genEd().modifyGenCraftSkill(mob,A);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenCraftSkill",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,A.name()+"'s everywhere shake under the transforming power!");
		return true;
	}

	public void components(MOB mob, Vector commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rFormat: MODIFY COMPONENT [SKILL ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String skillID=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(skillID);
		if(A==null)
		{
			mob.tell("'"+skillID+"' is not a proper skill/spell ID.  Try LIST ABILITIES.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		skillID=A.ID();
		if(CMLib.ableMapper().getAbilityComponentMap().get(A.ID().toUpperCase())==null)
		{
			mob.tell("A component definition for '"+A.ID()+"' doesn't exists, you'll need to create it first.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CMLib.genEd().modifyComponents(mob,skillID);
		final String parms=CMLib.ableMapper().getAbilityComponentCodedString(skillID);
		final String error=CMLib.ableMapper().addAbilityComponent(parms,CMLib.ableMapper().getAbilityComponentMap());
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CMLib.ableMapper().alterAbilityComponentFile(skillID,false);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The complication of skill usage just increased!");
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.session().rawPrintln("but fail to specify the proper fields.\n\rThe format is MODIFY SOCIAL [NAME] ([PARAM])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		final String name=((String)commands.elementAt(2)).toUpperCase();
		String stuff="";
		if(commands.size()>3)
			stuff=CMParms.combine(commands,3).toUpperCase().trim();
		if(stuff.startsWith("<")||stuff.startsWith(">")||(stuff.startsWith("T-")))
			stuff="TNAME";
		if(stuff.equals("TNAME"))
			stuff="<T-NAME>";
		final String oldStuff=stuff;
		if(stuff.equals("NONE"))
			stuff="";
		final Social S=CMLib.socials().fetchSocial((name+" "+stuff).trim(),false);
		if(S==null)
		{
			mob.tell("The social '"+stuff+"' does not exist.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		final List<Social> oldSocials = new Vector();
		List<Social> allSocials = CMLib.socials().getSocialsSet(name);
		if(allSocials==null) allSocials=new Vector();
		for(int a = 0; a<allSocials.size();a++)
			oldSocials.add((Social)allSocials.get(a).copyOf());
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around the idea of  "+S.name()+"s.");
		CMLib.socials().modifySocialInterface(mob,(name+" "+oldStuff).trim());
		allSocials = CMLib.socials().getSocialsSet(name);
		boolean changed = allSocials.size() != oldSocials.size();
		if(!changed)
		for(int a=0;a<oldSocials.size();a++)
		{
			final Social oldSocial = oldSocials.get(a);
			boolean found = false;
			for(int a2=0;a2<allSocials.size();a2++)
			{
				final Social newSocial = allSocials.get(a2);
				if(oldSocial.name().equals(newSocial.name()))
				{
					found = true;
					changed = !oldSocial.sameAs(newSocial);
					break;
				}
			}
			if(!found) changed = true;
			if(changed) break;
		}
		if(changed)
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The happiness of all mankind has just fluxuated!");
	}

	public void players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY USER [PLAYER NAME] ([STAT],?) (VALUE)\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		final String mobID=(String)commands.elementAt(2);
		final MOB M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			CMLib.database().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setLastUpdated(M.playerStats().getLastDateTime());
			M.recoverPhyStats();
			M.recoverCharStats();
		}
		else
		{
			mob.tell("There is no such player as '"+mobID+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		mob.location().showOthers(mob,M,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
		final MOB copyMOB=(MOB)M.copyOf();
		if(commands.size()<5)
		{
			CMLib.genEd().modifyPlayer(mob,M);
			if(!copyMOB.sameAs(M))
				Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+".");
		}
		else
		{
			final String command=((String)commands.elementAt(3)).toUpperCase();
			final String restStr=CMParms.combine(commands,4);
			if(command.equalsIgnoreCase("PROFICIENCIES")||command.equalsIgnoreCase("PROFICIENCY"))
			{
				final int prof=CMath.s_int(restStr);
				for(int a=0;a<M.numAbilities();a++)
					M.fetchAbility(a).setProficiency(prof);
				for(int a=0;a<M.numEffects();a++)
				{
					final Ability A=M.fetchEffect(a);
					if((A!=null)&&(A.isNowAnAutoEffect()))
						A.setProficiency(prof);
				}
				mob.tell("All of "+M.Name()+"'s skill proficiencies set to "+prof);
				Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+" skill proficiencies.");
			}
			else
			if(command.toUpperCase().startsWith("PROFICIENCY(")&&command.endsWith(")"))
			{
				final int prof=CMath.s_int(restStr);
				final String ableName=command.substring(12,command.length()-1).trim();
				final Ability A=M.findAbility(ableName);
				if(A==null)
				{
					mob.tell("...but failed to specify an valid ability name.  Try one of: "+CMParms.toCMObjectStringList(M.abilities()));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return;
				}
				A.setProficiency(prof);
				final Ability eA=M.fetchEffect(A.ID());
				if((eA!=null)&&(eA.isNowAnAutoEffect()))
					eA.setProficiency(prof);
				mob.tell(M.Name()+"'s skill proficiency in "+A.ID()+" set to "+prof);
				Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+" skill proficiency in "+A.ID()+".");
			}
			else
			if(CMLib.coffeeMaker().isAnyGenStat(M, command))
			{
				CMLib.coffeeMaker().setAnyGenStat(M,command, restStr);
				M.recoverPhyStats();
				M.recoverCharStats();
				M.recoverMaxState();
				if(!copyMOB.sameAs(M))
					Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+".");
			}
			else
			{
				final STreeSet<String> set=new STreeSet<String>();
				set.addAll(CMLib.coffeeMaker().getAllGenStats(M));
				set.add("PROFICIENCIES");
				set.add("PROFICIENCY(ABILITY_ID)");
				mob.tell("...but failed to specify an aspect.  Try one of: "+CMParms.toStringList(set));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			}
			CMLib.database().DBUpdatePlayer(M);
			if(CMLib.flags().isInTheGame(M,true))
				CMLib.database().DBUpdateFollowers(M);
		}
		copyMOB.setSession(null); // prevents logoffs.
		copyMOB.setLocation(null);
		copyMOB.destroy();
	}

	public void manufacturer(MOB mob, Vector commands) throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY MANUFACTURER [NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		final String manufacturerID=CMParms.combine(commands,2);

		final Manufacturer manufacturer=CMLib.tech().getManufacturer(manufacturerID);
		if((manufacturer==null)||(manufacturer==CMLib.tech().getDefaultManufacturer()))
		{
			mob.tell("There's no manufacturer called '"+manufacturerID+"' Try LIST MANUFACTURERS.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		CMLib.genEd().modifyManufacturer(mob, manufacturer);
		CMLib.tech().updateManufacturer(manufacturer);
		mob.location().recoverRoomStats();
		Log.sysOut(mob.Name()+" modified manufacturer "+manufacturer.name()+".");
	}

	public void mobs(MOB mob, Vector commands)
		throws IOException
	{

		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY MOB [MOB NAME] [LEVEL, ABILITY, REJUV, MISC, ?] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		final String mobID=((String)commands.elementAt(2));
		final String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);


		final MOB modMOB=mob.location().fetchInhabitant(mobID);
		if(modMOB==null)
		{
			mob.tell("I don't see '"+mobID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		if(!modMOB.isMonster())
		{
			mob.tell(modMOB.Name()+" is a player! Try MODIFY USER!");
			return;
		}
		final MOB copyMOB=(MOB)modMOB.copyOf();
		mob.location().showOthers(mob,modMOB,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
		if(command.equals("LEVEL"))
		{
			final int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modMOB.basePhyStats().setLevel(newLevel);
				modMOB.recoverCharStats();
				modMOB.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			final int newAbility=CMath.s_int(restStr);
			modMOB.basePhyStats().setAbility(newAbility);
			modMOB.recoverPhyStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			final int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modMOB.basePhyStats().setRejuv(newRejuv);
				modMOB.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
			else
			{
				modMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				modMOB.recoverPhyStats();
				mob.tell(modMOB.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modMOB.isGeneric())
				CMLib.genEd().genMiscSet(mob,modMOB);
			else
				modMOB.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		if(CMLib.coffeeMaker().isAnyGenStat(modMOB, command))
		{
			CMLib.coffeeMaker().setAnyGenStat(modMOB,command, restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shake(s) under the transforming power.");
		}
		else
		{
			final STreeSet<String> set=new STreeSet<String>();
			set.addAll(CMParms.parseCommas("LEVEL,ABILITY,REJUV,MISC",true));
			set.addAll(CMLib.coffeeMaker().getAllGenStats(modMOB));
			mob.tell("...but failed to specify an aspect.  Try one of: "+CMParms.toStringList(set));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
		if(!modMOB.sameAs(copyMOB))
			Log.sysOut("Mobs",mob.Name()+" modified mob "+modMOB.Name()+".");
		copyMOB.destroy();
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell("You are not allowed to do that here.");
		return false;
	}

	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
		{
			final Object O = commands.elementAt(1);
			if(O instanceof Environmental)
			{
				CMLib.genEd().genMiscSet(mob,(Environmental)O);
				if(O instanceof Physical)
					((Physical)O).recoverPhyStats();
				((Environmental)O).text();
				return true;
			}
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS)) return errorOut(mob);
			items(mob,commands);
		}
		else
		if(commandType.equals("MANUFACTURER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS)) return errorOut(mob);
			manufacturer(mob,commands);
		}
		else
		if(commandType.equals("RECIPE"))
		{
			//mob.tell("Not yet implemented"); if(true) return true;
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRECIPES)) return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell("Modify which recipe?  Name a common skill ID -- use list abilities to find one.");
				return false;
			}
			final String name=CMParms.combine(commands,2);
			final Ability A=CMClass.findAbility(name,Ability.ACODE_COMMON_SKILL,-1,false);
			if(A==null)
			{
				mob.tell("'"+name+"' is not a valid skill id.");
				return false;
			}
			if(!(A instanceof ItemCraftor))
			{
				mob.tell("'"+A.ID()+"' is not a common crafting skill.");
				return false;
			}
			final ItemCraftor iA = (ItemCraftor)A;
			if((iA.parametersFormat()==null)
			||(iA.parametersFormat().length()==0)
			||(iA.parametersFile()==null)
			||(iA.parametersFile().length()==0))
			{
				mob.tell("'"+A.ID()+"' does not have modifiable recipes.");
				return false;
			}
			CMLib.ableParms().modifyRecipesList(mob,iA.parametersFile(),iA.parametersFormat());
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS)) return errorOut(mob);
			rooms(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)) return errorOut(mob);
			accounts(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES)) return errorOut(mob);
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLASSES)) return errorOut(mob);
			classes(mob,commands);
		}
		else
		if(commandType.equals("ABILITY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			abilities(mob,commands);
		}
		else
		if(commandType.equals("LANGUAGE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			languages(mob,commands);
		}
		else
		if(commandType.equals("CRAFTSKILL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			craftSkills(mob,commands);
		}
		else
		if(commandType.equals("ALLQUALIFY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES)) return errorOut(mob);
			allQualify(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDAREAS)) return errorOut(mob);
			areas(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS)) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("COMPONENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COMPONENTS)) return errorOut(mob);
			components(mob,commands);
			return false;
		}
		else
		if(commandType.equals("EXPERTISE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.EXPERTISES)) return errorOut(mob);
			mob.tell("You can't modify components, you can only LIST, CREATE, and DESTROY them.");
			return false;
		}
		else
		if(commandType.equals("TITLE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TITLES)) return errorOut(mob);
			mob.tell("You can't modify titles, you can only LIST, CREATE, and DESTROY them.");
			return false;
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDSOCIALS)) return errorOut(mob);
			socials(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS)) return errorOut(mob);
			mobs(mob,commands);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"The space-time continuum shake(s) under the transforming power.");
		}
		else
		if(commandType.equals("DAY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK)) return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY DAY [INT]\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell.");
				return false;
			}
			mob.location().getArea().getTimeObj().setDayOfMonth(CMath.s_int((String)commands.get(2)));
			mob.location().getArea().getTimeObj().save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"The space-time continuum shake(s) under the transforming power.");
		}
		else
		if(commandType.equals("MONTH"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK)) return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY MONTH [INT]\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell.");
				return false;
			}
			mob.location().getArea().getTimeObj().setMonth(CMath.s_int((String)commands.get(2)));
			mob.location().getArea().getTimeObj().save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"The space-time continuum shake(s) under the transforming power.");
		}
		else
		if(commandType.equals("YEAR"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK)) return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY YEAR [INT]\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell.");
				return false;
			}
			mob.location().getArea().getTimeObj().setYear(CMath.s_int((String)commands.get(2)));
			mob.location().getArea().getTimeObj().save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"The space-time continuum shake(s) under the transforming power.");
		}
		else
		if((commandType.equals("TIME"))||(commandType.equals("HOUR")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK)) return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY TIME [INT]\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell.");
				return false;
			}
			final TimeClock C=mob.location().getArea().getTimeObj();
			final TimeClock.TimeOfDay oldTOD=C.getTODCode();
			C.setHourOfDay(CMath.s_int((String)commands.get(2)));
			if(oldTOD!=C.getTODCode())
				C.handleTimeChange();
			C.save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"The space-time continuum shake(s) under the transforming power.");
		}
		else
		if(commandType.startsWith("JSCRIPT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JSCRIPTS))
				return errorOut(mob);
			if(CMProps.getIntVar(CMProps.Int.JSCRIPTS)!=1)
			{
				mob.tell("This command is only used when your Scriptable Javascripts require approval as specified in your coffeemud.ini file.");
				return true;
			}
			Object O=null;
			final Map<Long,String> j=CMSecurity.getApprovedJScriptTable();
			boolean somethingFound=false;
			for(final Long L : j.keySet())
			{
				O=j.get(L);
				if(O instanceof StringBuffer)
				{
					somethingFound=true;
					mob.tell("Unapproved script:\n\r"+((StringBuffer)O).toString()+"\n\r");
					if((!mob.isMonster())
					&&(mob.session().confirm("Approve this script (Y/n)?","Y")))
						CMSecurity.approveJScript(mob.Name(),L.longValue());
					else
						j.remove(L);
				}
			}
			if(!somethingFound)
				mob.tell("No Javascripts require approval at this time.");
		}
		else
		if(commandType.equals("USER")||commandType.equals("PLAYER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)) return errorOut(mob);
			players(mob,commands);
		}
		else
		if(commandType.equals("POLL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.POLLS)) return errorOut(mob);
			final String name=CMParms.combine(commands,2);
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
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms around the idea of "+P.getSubject()+".^?");
			CMLib.polls().modifyVote(P, mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world's uncertainty has changed.^?");
			Log.sysOut("CreateEdit",mob.Name()+" modified Poll "+P.getName()+".");
		}
		else
		if(commandType.equals("HOLIDAY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS)) return errorOut(mob);
			final String name=CMParms.combine(commands,2);
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
			CMLib.quests().modifyHoliday(mob,num);
			Log.sysOut("CreateEdit",mob.Name()+" modified Holiday "+name+".");
		}
		else
		if(commandType.equals("NEWS"))
		{
			if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS))
			&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NEWS)))
				return errorOut(mob);

			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			final Item I=CMClass.getItem("StdJournal");
			I.setName("SYSTEM_NEWS");
			I.setDescription("Enter `LIST NEWS [NUMBER]` to read an entry.%0D%0AEnter CREATE NEWS to add new entries. ");
			final CMMsg newMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,null,CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,CMParms.combine(commands,2),CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,null);
			if(mob.location().okMessage(mob,newMsg)&&I.okMessage(mob, newMsg))
			{
				mob.location().send(mob,newMsg);
				I.executeMsg(mob,newMsg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world is now more informed!^?");
			}
		}
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS)) return errorOut(mob);
			quests(mob,commands);
		}
		else
		if(commandType.equals("SQL"))
		{
			if(!CMSecurity.isASysOp(mob)) return errorOut(mob);
			try
			{
				final String sql=CMParms.combine(commands,2);
				if(sql.equals("ping"))
				{
					final int num=CMLib.database().pingAllConnections(1000);
					mob.tell("Pings completed="+num+".");
				}
				else
				if(sql.toUpperCase().trim().startsWith("SELECT"))
				{
					mob.tell("SQL Query: "+sql);
					final List<String[]> results=CMLib.database().DBRawQuery(sql.replace('`','\''));
					final StringBuilder buf=new StringBuilder("QueryResults\n\r");
					if(results.size()>0)
					{
						final String[] headerRow=results.get(0);
						for (final String element : headerRow)
							buf.append(element);
						buf.append("\n\r");
						for(int r=1;r<results.size();r++)
						{
							final String[] row=results.get(r);
							for(int c=0;c<row.length;c++)
							{
								if(c<headerRow.length)
									buf.append(CMStrings.padRight(row[c],headerRow[c].length()));
								else
									buf.append(row[c]);
							}
							buf.append("\n\r");
						}
					}
					if(mob.session()!=null)
						mob.session().rawPrint(buf.toString());
					mob.tell("Command completed.");
				}
				else
				{
					mob.tell("SQL Statement: "+sql);
					final int resp=CMLib.database().DBRawExecute(sql.replace('`','\''));
					mob.tell("Command completed. Response code: "+resp);
				}
			}
			catch(final Exception e)
			{
				mob.tell("SQL Error: "+e.getMessage());
			}
		}
		else
		if(commandType.equals("GOVERNMENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS)) return errorOut(mob);
			if(commands.size()<3)
				mob.tell("Modify which government?  Use list governments.");
			else
			{
				final String name=CMParms.combine(commands,2);
				ClanGovernment G = null;
				for(final ClanGovernment g : CMLib.clans().getStockGovernments())
					if(g.getName().equalsIgnoreCase(name))
						G=g;
				if(G==null)
					for(final ClanGovernment g : CMLib.clans().getStockGovernments())
						if(g.getName().toLowerCase().startsWith(name.toLowerCase()))
							G=g;
				if(G==null)
					mob.tell("Government '"+name+"' is unknown.  Try list governments.");
				else
				if(!mob.isMonster())
				{
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around "+G.getName()+".");
					CMLib.genEd().modifyGovernment(mob, G);
					CMLib.clans().reSaveGovernmentsXML();
					Log.sysOut("CreateEdit",mob.Name()+" modified Clan Government "+G.getName()+".");
				}
			}
		}
		else
		if(commandType.equals("FACTION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS)) return errorOut(mob);
			if(commands.size()<3)
				mob.tell("Modify which faction?  Use list factions.");
			else
			{
				final String name=CMParms.combine(commands,2);
				Faction F=CMLib.factions().getFaction(name);
				if(F==null) F=CMLib.factions().getFactionByName(name);
				if(F==null)
					mob.tell("Faction '"+name+"' is unknown.  Try list factions.");
				else
				if(!mob.isMonster())
				{
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around the idea of  "+F.name()+".");
					CMLib.factions().modifyFaction(mob,F);
					Log.sysOut("CreateEdit",mob.Name()+" modified Faction "+F.name()+" ("+F.factionID()+").");
				}
			}
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS)) return errorOut(mob);
			if(commands.size()<3)
				mob.tell("Modify which clan?  Use clanlist.");
			else
			{
				final String name=CMParms.combine(commands,2);
				final Clan C=CMLib.clans().findClan(name);
				if(C==null)
					mob.tell("Clan '"+name+"' is unknown.  Try clanlist.");
				else
				if(!mob.isMonster())
				{
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around "+C.name()+".");
					CMLib.genEd().modifyClan(mob,C);
					Log.sysOut("CreateEdit",mob.Name()+" modified Clan "+C.name()+".");
				}
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			final int x=allWord.indexOf('@');
			MOB srchMob=mob;
			Item srchContainer=null;
			Room srchRoom=mob.location();
			if(x>0)
			{
				final String rest=allWord.substring(x+1).trim();
				allWord=allWord.substring(0,x).trim();
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
			Environmental thang=null;
			if((srchMob!=null)&&(srchRoom!=null))
				thang=srchRoom.fetchFromMOBRoomFavorsItems(srchMob,srchContainer,allWord,Wearable.FILTER_ANY);
			else
			if(srchMob!=null)
				thang=srchMob.findItem(allWord);
			else
			if(srchRoom!=null)
				thang=srchRoom.fetchFromRoomFavorItems(srchContainer,allWord);
			if((thang!=null)&&(thang instanceof Item))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS))
					return errorOut(mob);
				final Item copyItem=(Item)thang.copyOf();
				mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
				if(!thang.isGeneric())
				{
					CMLib.genEd().modifyStdItem(mob,(Item)thang);
				}
				else
					CMLib.genEd().genMiscSet(mob,thang);
				((Item)thang).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
				if(!copyItem.sameAs(thang))
					Log.sysOut("CreateEdit",mob.Name()+" modified item "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(mob.location())+".");
				copyItem.destroy();
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS))
					return errorOut(mob);
				if((!thang.isGeneric())&&(((MOB)thang).isMonster()))
				{
					final MOB copyMOB=(MOB)thang.copyOf();
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
					CMLib.genEd().modifyStdMob(mob,(MOB)thang);
					if(!copyMOB.sameAs(thang))
						Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
				}
				else
				if(!((MOB)thang).isMonster())
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)) return errorOut(mob);
					players(mob,CMParms.parse("MODIFY USER \""+thang.Name()+"\""));
				}
				else
				{
					final MOB copyMOB=(MOB)thang.copyOf();
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
					CMLib.genEd().genMiscSet(mob,thang);
					if(!copyMOB.sameAs(thang))
						Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
					copyMOB.destroy();
				}
				((MOB)thang).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
			}
			else
			if((Directions.getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(Directions.getGoodDirectionCode(allWord)>=0)
					thang=mob.location().getRawExit(Directions.getGoodDirectionCode(allWord));

				if(thang!=null)
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS)) return errorOut(mob);
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
					final Exit copyExit=(Exit)thang.copyOf();
					CMLib.genEd().genMiscText(mob,thang,1,1);
					updateChangedExit(mob, mob.location(), (Exit)thang, copyExit);
				}
				else
				{
					commands.insertElementAt("EXIT",1);
					execute(mob,commands,metaFlags);
				}
			}
			else
			if(CMLib.socials().fetchSocial(allWord,true)!=null)
			{
				commands.insertElementAt("SOCIAL",1);
				execute(mob,commands,metaFlags);
			}
			else
				mob.tell("\n\rYou cannot modify a '"+commandType+"'. However, you might try an ITEM, RACE, CLASS, ABILITY, LANGUAGE, CRAFTSKILL, ALLQUALIFY, AREA, EXIT, COMPONENT, RECIPE, EXPERTISE, TITLE, QUEST, MOB, USER, HOLIDAY, MANUFACTURER, GOVERNMENT, JSCRIPT, FACTION, SOCIAL, CLAN, POLL, NEWS, DAY, MONTH, YEAR, TIME, HOUR, or ROOM.");
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return false;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowedContainsAny(mob,mob.location(),CMSecurity.SECURITY_CMD_GROUP);}
}
