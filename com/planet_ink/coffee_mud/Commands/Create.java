package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;

/*
   Copyright 2004-2018 Bo Zimmerman

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

public class Create extends StdCommand
{
	public Create()
	{
	}

	private final String[]	access	= I(new String[] { "CREATE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public void exits(MOB mob, List<String> commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(L("This command is invalid from within a GridLocaleChild room."));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		if(commands.size()<4)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE EXIT [DIRECTION] [EXIT TYPE]\n\r"));
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

		final String Locale=commands.get(3);
		Exit thisExit=CMClass.getExit(Locale);
		if(thisExit==null)
		{
			mob.tell(L("You have failed to specify a valid exit type '@x1'.\n\r",Locale));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final Exit opExit=mob.location().getRawExit(direction);
		final Room opRoom=mob.location().rawDoors()[direction];
		Exit reverseExit=null;
		if(opExit != null)
		{
			if(opRoom!=null)
				reverseExit=opRoom.getRawExit(Directions.getOpDirectionCode(direction));
			if(reverseExit!=null)
			{
				if((thisExit.isGeneric())&&(reverseExit.isGeneric())&&(thisExit.ID().equals(reverseExit.ID())))
				{
					thisExit=(Exit)reverseExit.copyOf();
					CMLib.genEd().modifyGenExit(mob,thisExit,-1);
				}
			}
		}

		final boolean useShipDirs=(mob.location() instanceof BoardableShip)||(mob.location().getArea() instanceof BoardableShip);
		mob.location().setRawExit(direction,thisExit);
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly a portal opens up @x1.\n\r",
				(useShipDirs?CMLib.directions().getShipInDirectionName(direction):CMLib.directions().getInDirectionName(direction))));
		CMLib.database().DBUpdateExits(mob.location());
		if((reverseExit!=null)&&(opExit!=null)&&(opRoom!=null))
		{
			final int revDirCode=Directions.getOpDirectionCode(direction);
			if(opRoom.getRawExit(revDirCode)==reverseExit)
			{
				opRoom.setRawExit(revDirCode,(Exit)thisExit.copyOf());
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		else
		if((reverseExit==null)&&(opExit==null)&&(opRoom!=null))
		{
			final int revDirCode=Directions.getOpDirectionCode(direction);
			if((opRoom.getRawExit(revDirCode)==null)&&(opRoom.rawDoors()[revDirCode]==mob.location()))
			{
				opRoom.setRawExit(revDirCode,(Exit)thisExit.copyOf());
				CMLib.database().DBUpdateExits(opRoom);
			}
		}
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(opRoom!=null)
			opRoom.getArea().fillInAreaRoom(opRoom);
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.Name()+".");
	}

	private long[] makeSpaceLocation(MOB mob, SpaceObject newItem, String rest)
	{
		List<String> utokens=CMParms.parseSpaces(rest.toUpperCase(),true);
		final String distErrorMsg=L("Valid distance units include: @x1.",SpaceObject.Distance.getFullList());
		int x;
		if(((x=utokens.indexOf("FROM"))>0)&&(x<utokens.size()-1))
		{
			double[] direction=new double[]{Math.toRadians(CMLib.dice().roll(1, 360, -1)),Math.toRadians(CMLib.dice().roll(1,180,-1))};
			String distStr=CMParms.combine(utokens,0,x);
			String objName=CMParms.combine(utokens,x+1);
			Long dist=CMLib.english().parseSpaceDistance(distStr);
			if(dist==null)
			{
				mob.tell(L("Unknown distance for space object @x1:",newItem.ID())+" '"+distStr+"'. \n\r"+distErrorMsg);
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return null;
			}
			SpaceObject O=null;
			if(objName.trim().length()>0)
			{
				O=CMLib.map().findSpaceObject(objName, true);
				if(O==null)
					O=CMLib.map().findSpaceObject(objName, false);
			}
			if(O==null)
			{
				mob.tell(L("Unknown relative space object")+" '"+objName+"'.\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return null;
			}
			rest=CMParms.toListString(CMLib.map().getLocation(O.coordinates(), direction, dist.longValue()));
		}
		List<String> valsL=CMParms.parseCommas(rest,true);
		if(valsL.size()!=3)
		{
			mob.tell(L("Unknown location for space object @x1:",newItem.ID())+": '"+rest+"'. "
					+L("The format for coordinates is 3 distances from core, comma delimited, or [DISTANCE] FROM [PLACE]\n\r"+distErrorMsg));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return null;
		}
		else
		{
			boolean fail=true;
			Long[] valL=new Long[3];
			for(int i=0;i<3;i++)
			{
				Long newValue=CMLib.english().parseSpaceDistance(valsL.get(i));
				if(newValue==null)
				{
					mob.tell(L("Unknown coord: '@x2'. @x3 for space object @x1:",newItem.ID(),valsL.get(i),distErrorMsg));
					break;
				}
				else
				{
					valL[i]=newValue;
					if(i==2)
						fail=false;
				}
			}
			if(!fail)
			{
				return new long[]{valL[0].longValue(),valL[1].longValue(),valL[2].longValue()};
			}
			else
			{
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return null;
			}
		}
	}
	
	public void items(MOB mob, List<String> commands) throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\r"
					+ "The format for normal items is CREATE ITEM [ITEM NAME](@ room/[MOB NAME])\n\r"
					+ "The format for space items is CREATE ITEM [ITEM NAME](@ [COORDS]/[DISTANCE] FROM [SPACE OBJ])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		String itemID=CMParms.combine(commands,2);
		Environmental dest=mob.location();
		Container setContainer=null;
		String rest="";
		long [] coordinates=new long[]{
			CMLib.dice().getRandomizer().nextLong(),
			CMLib.dice().getRandomizer().nextLong(),
			CMLib.dice().getRandomizer().nextLong()
		};
		
		int x=itemID.indexOf('@');
		if(x>0)
		{
			rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
		}
		
		Item newItem=CMClass.getItem(itemID);
		if((newItem==null)&&(CMLib.english().numPossibleGold(null,itemID)>0))
		{
			final long numCoins=CMLib.english().numPossibleGold(null,itemID);
			final String currency=CMLib.english().numPossibleGoldCurrency(mob,itemID);
			final double denom=CMLib.english().numPossibleGoldDenomination(mob,currency,itemID);
			if((numCoins>0)&&(denom>0.0))
				newItem=CMLib.beanCounter().makeCurrency(currency,denom,numCoins);
		}

		boolean doGenerica=true;
		if(newItem==null)
		{
			newItem=getNewCatalogItem(itemID);
			doGenerica=newItem==null;
		}

		if(newItem==null)
		{
			mob.tell(L("There's no such thing as a '@x1'.\n\r",itemID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		
		if((newItem instanceof ArchonOnly)
		&&(!CMSecurity.isASysOp(mob)))
		{
			mob.tell(L("NO!"));
			return;
		}
		
		if(x>0)
		{
			if(newItem instanceof SpaceObject)
			{
				coordinates=makeSpaceLocation(mob,(SpaceObject)newItem,rest);
				if(coordinates==null)
				{
					return;
				}
			}
			else
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				final MOB M=mob.location().fetchInhabitant(rest);
				if(M==null)
				{
					final Item I = mob.location().findItem(null, rest);
					if(I instanceof Container)
						setContainer=(Container)I;
					else
					{
						mob.tell(L("MOB or Container '@x1' not found.",rest));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
						return;
					}
				}
				else
					dest=M;
			}
		}
		else
		if(newItem instanceof SpaceObject)
		{
			int i=21;
			while((--i>0)&&(CMLib.map().getSpaceObjectsByCenterpointWithin(coordinates, 0, SpaceObject.Distance.SolarSystemDiameter.dm).size()>0))
			{
				coordinates=new long[]{
					CMLib.dice().getRandomizer().nextLong(),
					CMLib.dice().getRandomizer().nextLong(),
					CMLib.dice().getRandomizer().nextLong()
				};
			}
		}
	
		if(newItem.subjectToWearAndTear())
			newItem.setUsesRemaining(100);
		if(newItem instanceof SpaceObject)
		{
			CMLib.map().addObjectToSpace(((SpaceObject)newItem), coordinates);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 appears in the sky.",newItem.name()));
		}
		else
		if(dest instanceof Room)
		{
			((Room)dest).addItem(newItem);
			newItem.setContainer(setContainer);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 drops from the sky.",newItem.name()));
		}
		else
		if(dest instanceof MOB)
		{
			((MOB)dest).addItem(newItem);
			newItem.setContainer(setContainer);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 drops into @x2's arms.",newItem.name(),dest.name()));
		}

		if((newItem.isGeneric())&&(doGenerica))
			CMLib.genEd().genMiscSet(mob,newItem,-1);
		if(newItem instanceof SpaceObject)
		{
			CMLib.database().DBCreateThisItem("SPACE", newItem);
		}
		mob.location().recoverRoomStats();
		Log.sysOut("Items",mob.Name()+" created item "+newItem.ID()+".");
	}

	public void manufacturer(MOB mob, List<String> commands) throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE MANUFACTURER [NEW NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final String manufacturerID=CMParms.combine(commands,2);
		final Manufacturer manufacturer=(Manufacturer)CMClass.getCommon("DefaultManufacturer");
		if(manufacturer==null)
		{
			mob.tell(L("DefaultManufacturer not found."));
			Log.errOut("DefaultManufacturer was not found in common classes.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final Manufacturer manuCheck=CMLib.tech().getManufacturer(manufacturerID);
		if(((manuCheck!=null)&&(manuCheck!=CMLib.tech().getDefaultManufacturer()))
		||manufacturerID.equalsIgnoreCase("RANDOM"))
		{
			mob.tell(L("There's already a manufacturer called '@x1'.\n\r",manufacturerID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		manufacturer.setName(manufacturerID);
		CMLib.tech().addManufacturer(manufacturer);
		CMLib.genEd().modifyManufacturer(mob, manufacturer, -1);
		mob.location().recoverRoomStats();
		Log.sysOut(mob.Name()+" created manufacturer "+manufacturer.name()+".");
	}

	public void players(MOB mob, List<String> commands) throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE USER [PLAYER NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			mob.tell(L("There is already a player called '@x1'!",M.Name()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		if(!CMLib.login().isOkName(mobID,false))
		{
			mob.tell(L("'@x1' is not a valid name.",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		mobID=CMStrings.capitalizeAndLower(mobID);
		CMLib.login().createCharacter(mobID,mob.session());
		M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			if(CMLib.flags().isInTheGame(M,true))
				M.removeFromGame(false,true);
			Log.sysOut("Mobs",mob.Name()+" created player "+M.Name()+".");
		}
	}

	public void rooms(MOB mob, List<String> commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell(L("This command is invalid from within a GridLocaleChild room."));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		if(commands.size()<4)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		final int direction=CMLib.directions().getGoodDirectionCode((commands.get(2)));
		if(direction<0)
		{
			mob.tell(L("You have failed to specify a direction.  Try @x1.\n\r",Directions.LETTERS()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		Room thisRoom=null;
		final String Locale=commands.get(3);
		thisRoom=CMClass.getLocale(Locale);
		if(thisRoom==null)
		{
			mob.tell(L("You have failed to specify a valid room type '@x1'.\n\rThe format is CREATE ROOM [DIRECTION] ([ROOM TYPE] / LINK [ROOM ID]) \n\r",Locale));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		final Room room=mob.location();
		thisRoom.setRoomID(room.getArea().getNewRoomID(room,direction));
		thisRoom.setArea(room.getArea());
		if(thisRoom.roomID().length()==0)
		{
			mob.tell(L("A room may not be created in that direction.  Are you sure you havn't reached the edge of a grid?"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		thisRoom.setDisplayText(CMClass.classID(thisRoom)+"-"+thisRoom.roomID());
		thisRoom.setDescription("");
		if(CMLib.flags().isSavable(thisRoom))
			CMLib.database().DBCreateRoom(thisRoom);

		CMLib.map().createNewExit(mob.location(),thisRoom,direction);

		mob.location().recoverRoomStats();
		thisRoom.recoverRoomStats();
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("Suddenly a block of earth falls from the sky.\n\r"));
		Log.sysOut("Rooms",mob.Name()+" created room "+thisRoom.roomID()+".");
	}

	public void accounts(MOB mob, List<String> commands)
	{
		if(commands.size()<4)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE ACCOUNT [NAME] [PASSWORD]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		final String AcctName=CMStrings.capitalizeAndLower(commands.get(2));
		final String password=CMParms.combine(commands,3).toLowerCase();
		if(CMLib.players().accountExists(AcctName))
		{
			mob.tell(L("Account '@x1' already exists!\n\r",AcctName));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		if(!CMLib.login().isOkName(AcctName,false))
		{
			mob.tell(L("Name '@x1' is not permitted.\n\r",AcctName));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		final PlayerAccount thisAcct=(PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		thisAcct.setAccountName(AcctName);
		thisAcct.setAccountExpiration(0);
		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
			thisAcct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*(CMProps.getIntVar(CMProps.Int.TRIALDAYS))));
		thisAcct.setLastDateTime(System.currentTimeMillis());
		thisAcct.setLastUpdated(System.currentTimeMillis());
		thisAcct.setPassword(password);
		CMLib.database().DBCreateAccount(thisAcct);
		CMLib.players().addAccount(thisAcct);

		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("A new soul descends from the heavens and dissipates.\n\r"));
		Log.sysOut("Create",mob.Name()+" created account "+thisAcct.getAccountName()+".");
	}

	public MOB getNewCatalogMob(String mobID)
	{
		MOB newMOB=CMLib.catalog().getCatalogMob(mobID);
		if(newMOB!=null)
		{
			newMOB=(MOB)newMOB.copyOf();
			try { CMLib.catalog().changeCatalogUsage(newMOB,true);} catch(final Exception t){}
			newMOB.text();
		}
		return newMOB;
	}

	public Item getNewCatalogItem(String itemID)
	{
		Item newItem=CMLib.catalog().getCatalogItem(itemID);
		if(newItem!=null)
		{
			newItem=(Item)newItem.copyOf();
			try { CMLib.catalog().changeCatalogUsage(newItem,true);} catch(final Exception t){}
			newItem.text();
		}
		return newItem;
	}

	public void mobs(MOB mob, List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE MOB [MOB NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		final String mobID=CMParms.combine(commands,2);
		MOB newMOB=CMClass.getMOB(mobID);

		boolean doGenerica=true;
		if(newMOB==null)
		{
			newMOB=getNewCatalogMob(mobID);
			doGenerica=newMOB==null;
		}

		if(newMOB == null)
		{
			final Race R=CMClass.getRace(mobID);
			if(R!=null)
			{
				newMOB = CMClass.getMOB("GenMob");
				newMOB.setName(CMLib.english().startWithAorAn(R.name()));
				newMOB.setDisplayText(L("@x1 is here.",newMOB.Name()));
			}
		}
		
		if(newMOB==null)
		{
			mob.tell(L("There's no such thing as a '@x1'.\n\r",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		if(newMOB.Name().length()==0)
			newMOB.setName(L("A Standard MOB"));
		newMOB.setStartRoom(mob.location());
		newMOB.setLocation(mob.location());
		long rejuv=CMProps.getTicksPerMinute()+CMProps.getTicksPerMinute()+(CMProps.getTicksPerMinute()/2);
		if(rejuv>(CMProps.getTicksPerMinute()*20))
			rejuv=(CMProps.getTicksPerMinute()*20);
		newMOB.phyStats().setRejuv((int)rejuv);
		newMOB.baseCharStats().getMyRace().setHeightWeight(newMOB.basePhyStats(),(char)newMOB.baseCharStats().getStat(CharStats.STAT_GENDER));
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(mob.location(),true);
		newMOB.recoverCharStats();
		newMOB.recoverPhyStats();
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("Suddenly, @x1 instantiates from the Java Plane.",newMOB.name()));
		if((newMOB.isGeneric())&&(doGenerica))
			CMLib.genEd().genMiscSet(mob,newMOB,-1);
		Log.sysOut("Mobs",mob.Name()+" created mob "+newMOB.Name()+".");
	}

	public void mixedrace(MOB mob, List<String> commands)
			throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE MIXEDRACE [RACE ID] [RACE ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		String raceID1=commands.get(2);
		String raceID2=CMParms.combine(commands,3);
		final Race R1=CMClass.getRace(raceID1);
		if(R1==null)
		{
			mob.tell("Unknown race: "+raceID1);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final Race R2=CMClass.getRace(raceID2);
		if(R2==null)
		{
			mob.tell("Unknown race: "+raceID2);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		
		final Race R=CMLib.utensils().getMixedRace(R1.ID(), R2.ID(), false);
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("Suddenly, @x1ness instantiates from the Java Plane.",R.name()));
		Log.sysOut("Mobs",mob.Name()+" created race "+R.ID()+".");
	}
	
	public void races(MOB mob, List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE RACE [RACE ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		String raceID=CMParms.combine(commands,2);
		final Race R=CMClass.getRace(raceID);
		if((R!=null)&&(R.isGeneric()))
		{
			mob.tell(L("This generic race already exists.. perhaps you might modify it?"));
			return;
		}
		Race GR=null;
		if((R!=null)&&(!R.isGeneric()))
		{
			if((mob.session()==null)
			||(!mob.session().confirm(L("Currently, @x1 is a standard race.  "
					+ "This will convert the race to a GenRace so that you can modify it.  "
					+ "Be warned that special functionality of the race may be lost by doing this.  "
					+ "You can undo this action by destroying the same race ID after creating it.  "
					+ "Do you wish to continue (y/N)?",R.ID()), L("N"))))
				return;
			GR=R.makeGenRace();
			raceID=GR.ID();
		}
		if(raceID.indexOf(' ')>=0)
		{
			mob.tell(L("'@x1' is an invalid race id, because it contains a space.",raceID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(GR==null)
		{
			GR=(Race)CMClass.getRace("GenRace").copyOf();
			GR.setRacialParms("<RACE><ID>"+CMStrings.capitalizeAndLower(raceID)+"</ID><NAME>"+CMStrings.capitalizeAndLower(raceID)+"</NAME></RACE>");
		}
		CMClass.addRace(GR);
		CMLib.genEd().modifyGenRace(mob,GR,-1);
		CMLib.database().DBCreateRace(GR.ID(),GR.racialParms());
		if(R!=null)
			CMLib.utensils().swapRaces(GR,R);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The diversity of the world just increased!"));
	}

	public void areas(MOB mob, List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\r"
					+ "The format for normal areas is CREATE AREA [AREA NAME]\n\r"
					+ "The format for space areas is CREATE AREA [AREA NAME] (@ [COORDS] / [DISTANCE] FROM [SPACE OBJECT]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		String areaName=CMParms.combine(commands,2);
		long [] coordinates=new long[]{
			CMLib.dice().getRandomizer().nextLong(),
			CMLib.dice().getRandomizer().nextLong(),
			CMLib.dice().getRandomizer().nextLong()
		};
			
		int x=areaName.indexOf('@');
		String spaceCoords="";
		if(x>0)
		{
			spaceCoords=areaName.substring(x+1).trim();
			areaName=areaName.substring(0,x).trim();
		}
		
		Area A=CMLib.map().getArea(areaName);
		if((A!=null)||(CMLib.map().getShip(areaName)!=null))
		{
			mob.tell(L("An area with the name '@x1' already exists!",areaName));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		
		String areaType="";
		int tries=0;
		String defaultArea=(spaceCoords.length()>0)?"Planet":"StdArea";
		while((areaType.length()==0)&&((++tries)<10))
		{
			areaType=mob.session().prompt(L("Enter an area type to create (default=@x1): ",defaultArea),defaultArea);
			if(CMClass.getAreaType(areaType)==null)
			{
				mob.session().println(L("Invalid area type! Valid ones are:"));
				mob.session().println(CMLib.lister().reallyList(mob,CMClass.areaTypes()).toString());
				areaType="";
			}
		}
		if(areaType.length()==0) 
			areaType=defaultArea;
		A=CMClass.getAreaType(areaType);
		if(A instanceof SpaceObject)
		{
			if(spaceCoords.length()>0)
			{
				coordinates=makeSpaceLocation(mob,(SpaceObject)A,spaceCoords);
				if(coordinates==null)
				{
					return;
				}
			}
			CMLib.map().addObjectToSpace(((SpaceObject)A), coordinates);
		}
		A.setName(areaName);
		CMLib.map().addArea(A);
		CMLib.database().DBCreateArea(A);

		final Room R=CMClass.getLocale((A instanceof SpaceObject)?"SpacePort":"StdRoom");
		R.setRoomID(A.getNewRoomID(R,-1));
		R.setArea(A);
		R.setDisplayText(CMClass.classID(R)+"-"+R.roomID());
		R.setDescription("");
		CMLib.database().DBCreateRoom(R);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The size of the world just increased!"));
		mob.tell(L("You are now at @x1.",R.roomID()));
		R.bringMobHere(mob,true);
		CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);
	}

	public void components(MOB mob, List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rFormat: CREATE COMPONENT [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		String skillID=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(skillID);
		if(A==null)
		{
			mob.tell(L("'@x1' is not a proper skill/spell ID.  Try LIST ABILITIES.",skillID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		skillID=A.ID();
		if(CMLib.ableComponents().getAbilityComponentMap().get(A.ID().toUpperCase())!=null)
		{
			mob.tell(L("'@x1' already exists, you'll need to destroy it first.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final Vector<AbilityComponent> DV=new Vector<AbilityComponent>();
		CMLib.ableComponents().getAbilityComponentMap().put(skillID.toUpperCase().trim(),DV);
		DV.add(CMLib.ableComponents().createBlankAbilityComponent());
		CMLib.genEd().modifyComponents(mob,skillID,-1);
		final String parms=CMLib.ableComponents().getAbilityComponentCodedString(skillID);
		final String error=CMLib.ableComponents().addAbilityComponent(parms,CMLib.ableComponents().getAbilityComponentMap());
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		CMLib.ableComponents().alterAbilityComponentFile(skillID,false);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The complication of skill usage just increased!"));
	}

	public void expertises(MOB mob, List<String> commands)
	{
		if((commands.size()<3)||(CMParms.combine(commands,1).indexOf('=')<0))
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rFormat: CREATE EXPERTISE [EXPERTISE ID]=[PARAMETERS] as follows: \n\r"));
			final StringBuffer buf=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,CMFile.FLAG_LOGERRORS).text();
			final StringBuffer inst=new StringBuffer("");
			List<String> V=new Vector<String>();
			if(buf!=null)
				V=Resources.getFileLineVector(buf);
			for(int v=0;v<V.size();v++)
			{
				if(V.get(v).startsWith("#"))
					inst.append(V.get(v).substring(1)+"\n\r");
				else
				if(V.get(v).length()>0)
					break;
			}
			if(mob.session()!=null)
				mob.session().wraplessPrintln(inst.toString());
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String parms=CMParms.combineQuoted(commands,2);
		final String skillID=parms.substring(0,parms.indexOf('='));
		if(skillID.indexOf(' ')>=0)
		{
			mob.tell(L("Spaces are not allowed in expertise codes."));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		String WKID=CMStrings.replaceAll(skillID.toUpperCase(),"@X1","");
		WKID=CMStrings.replaceAll(WKID,"@X2","").trim();
		if(CMLib.expertises().getStages(WKID)>0)
		{
			mob.tell(L("'@x1' already exists, you'll need to destroy it first.",WKID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String error=CMLib.expertises().confirmExpertiseLine(parms,null,false);
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final CMFile F=new CMFile(Resources.makeFileResourceName("skills/expertises.txt"),null,CMFile.FLAG_LOGERRORS);
		F.saveText("\n"+parms,true);
		Resources.removeResource("skills/expertises.txt");
		CMLib.expertises().recompileExpertises();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The power of skill usage just increased!"));
	}

	public void titles(MOB mob, List<String> commands)
	{
		if((commands.size()<3)||(CMParms.combine(commands,1).indexOf('=')<0))
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rFormat: CREATE TITLE [TITLE]=[ZAPPER MASK] as follows: \n\r"));
			final String inst = CMLib.titles().getAutoTitleInstructions();
			if(mob.session()!=null)
				mob.session().wraplessPrintln(inst);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String parms=CMParms.combineQuoted(commands,2);
		final String skillID=parms.substring(0,parms.indexOf('='));
		if(CMLib.titles().isExistingAutoTitle(skillID))
		{
			mob.tell(L("'@x1' already exists, you'll need to destroy it first.",skillID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String error=CMLib.titles().evaluateAutoTitle(parms,false);
		if(error!=null)
		{
			mob.tell(error);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		CMLib.titles().appendAutoTitle("\n"+parms);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The prestige of the players just increased!"));
	}

	public void abilities(MOB mob, List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE ABILITY [ABILITY ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String classD=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(classD);
		if((A!=null)&&(A.isGeneric()))
		{
			mob.tell(L("A generic ability with the ID '@x1' already exists!",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell(L("'@x1' is an invalid  id, because it contains a space.",classD));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final Ability CR=(Ability)CMClass.getAbility("GenAbility").copyOf();
		CR.setStat("CLASS",classD);
		CMLib.genEd().modifyGenAbility(mob,CR,-1);
		CMLib.database().DBCreateAbility(CR.ID(),"GenAbility",CR.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The skill of the world just increased!"));
	}

	public void achievements(MOB mob, List<String> commands)
	throws IOException
	{
		final boolean accountSys = CMProps.isUsingAccountSystem();
		if(commands.size()<((accountSys)?4:3))
		{
			if(accountSys)
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE ACHIEVEMENT (PLAYER/ACCOUNT/ALL) [TATTOO NAME]\n\r"));
			else
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE ACHIEVEMENT [TATTOO NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String agentStr=accountSys?commands.get(2).toString():"PLAYER";
		AccountStats.Agent agent=(AccountStats.Agent)CMath.s_valueOf(AccountStats.Agent.class, agentStr.toUpperCase().trim());
		if(agent == null)
		{
			mob.tell(L("'@x1' is an unknown achievement type.  Try @x2!",agentStr,CMParms.toListString(AccountStats.Agent.values())));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String tattoo=CMParms.combine(commands,((accountSys)?3:2));
		final Achievement A=CMLib.achievements().getAchievement(tattoo);
		if(A!=null)
		{
			mob.tell(L("An achievement with the TATTOO/ID '@x1' already exists!",A.getTattoo()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(tattoo.indexOf(' ')>=0)
		{
			mob.tell(L("'@x1' is an invalid tattoo, because it contains a space.",tattoo));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(Character.isDigit(tattoo.charAt(tattoo.length()-1)))
		{
			mob.tell(L("'@x1' is an invalid tattoo, because it ends with a digit.",tattoo));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(CMLib.achievements().addModifyAchievement(mob, agent, tattoo, null))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The vanity of the world just increased!"));
	}

	public void languages(MOB mob, List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE LANGUAGE [LANGUAGE ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String classD=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(classD);
		if((A!=null)&&(A.isGeneric()))
		{
			mob.tell(L("A generic ability with the ID '@x1' already exists!",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell(L("'@x1' is an invalid  id, because it contains a space.",classD));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final Language CR=(Language)CMClass.getAbility("GenLanguage").copyOf();
		CR.setStat("CLASS",classD);
		CMLib.genEd().modifyGenLanguage(mob,CR,-1);
		CMLib.database().DBCreateAbility(CR.ID(),"GenLanguage",CR.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The skill of the world just increased!"));
	}

	public void craftSkills(MOB mob, List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE CRAFTSKILL [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String classD=CMParms.combine(commands,2);
		final Ability A=CMClass.getAbility(classD);
		if((A!=null)&&(A.isGeneric()))
		{
			mob.tell(L("A generic ability with the ID '@x1' already exists!",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell(L("'@x1' is an invalid  id, because it contains a space.",classD));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final Ability CR=(Ability)CMClass.getAbility("GenCraftSkill").copyOf();
		CR.setStat("CLASS",classD);
		CMLib.genEd().modifyGenCraftSkill(mob,CR,-1);
		CMLib.database().DBCreateAbility(CR.ID(),"GenCraftSkill",CR.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The skill of the world just increased!"));
	}

	public void allQualify(MOB mob, List<String> commands)
	throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE ALLQUALIFY EACH/ALL [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String eachOrAll=commands.get(2);
		if((!eachOrAll.equalsIgnoreCase("each"))&&(!eachOrAll.equalsIgnoreCase("all")))
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE ALLQUALIFY EACH/ALL [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String classD=CMParms.combine(commands,3);
		final Ability A=CMClass.getAbility(classD);
		if(A==null)
		{
			mob.tell(L("Ability with the ID '@x1' does not exist! Try LIST ABILITIES.",classD));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get(eachOrAll.toUpperCase().trim());
		if(subMap.containsKey(classD.toUpperCase().trim()))
		{
			mob.tell(L("All-Qualify entry (@x1) ID '@x2' already exists.  Try DESTROY or MODIFY.",eachOrAll,A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final AbilityMapper.AbilityMapping mapped = CMLib.genEd().modifyAllQualifyEntry(mob,eachOrAll.toUpperCase().trim(),A, -1);
		map=CMLib.ableMapper().getAllQualifiesMap(null);
		subMap=map.get(eachOrAll.toUpperCase().trim());
		subMap.put(A.ID().toUpperCase().trim(), mapped);
		CMLib.ableMapper().saveAllQualifysFile(map);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The skill of the world just increased!"));
	}

	public void classes(MOB mob, List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is CREATE CLASS [CLASS ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		String classD=CMParms.combine(commands,2);
		final CharClass C=CMClass.getCharClass(classD);
		if((C!=null)&&(C.isGeneric()))
		{
			mob.tell(L("A generic class with the ID '@x1' already exists!",C.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		CharClass CR=null;
		if((C!=null)&&(!C.isGeneric()))
		{
			if((mob.session()==null)
			||(!mob.session().confirm(L("Currently, @x1 is a standard character class.  "
					+ "This will convert the class to a GenCharClass so that you can modify it.  "
					+ "Be warned that special functionality of the class may be lost by doing this.  "
					+ "You can undo this action by destroying the same class ID after creating it.  "
					+ "Do you wish to continue (y/N)?",C.ID()), L("N"))))
				return;
			CR=C.makeGenCharClass();
			classD=CR.ID();
		}
		if(classD.indexOf(' ')>=0)
		{
			mob.tell(L("'@x1' is an invalid class id, because it contains a space.",classD));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(CR==null)
		{
			CR=(CharClass)CMClass.getCharClass("GenCharClass").copyOf();
			CR.setClassParms("<CCLASS><ID>"+CMStrings.capitalizeAndLower(classD)+"</ID><NAME>"+CMStrings.capitalizeAndLower(classD)+"</NAME></CCLASS>");
		}
		CMClass.addCharClass(CR);
		CMLib.genEd().modifyGenClass(mob,CR,-1);
		CMLib.database().DBCreateClass(CR.ID(),CR.classParms());
		if(C!=null)
			CMLib.utensils().reloadCharClasses(C);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The employment of the world just increased!"));
	}

	public void socials(MOB mob, List<String> commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.tell(L("but fail to specify the proper fields.\n\rThe format is CREATE SOCIAL [NAME]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		final String stuff=CMParms.combine(commands,2);
		if(CMLib.socials().fetchSocial(stuff,false)!=null)
		{
			mob.tell(L("The social '@x1' already exists.",stuff));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		CMLib.socials().modifySocialInterface(mob,stuff);
	}

	public boolean errorOut(MOB mob)
	{
		mob.tell(L("You are not allowed to do that here."));
		return false;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=commands.get(1).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			exits(mob,commands);
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
		if(commandType.equals("MIXEDRACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			mixedrace(mob,commands);
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
		if(commandType.equals("ABILITY"))
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
		if(commandType.equals("LANGUAGE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			languages(mob,commands);
		}
		else
		if(commandType.equals("CRAFTSKILL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			craftSkills(mob,commands);
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
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDAREAS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			areas(mob,commands);
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
		if(commandType.equals("MANUFACTURER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			manufacturer(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			rooms(mob,commands);
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
		if(commandType.equals("DISABLEFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return errorOut(mob);
			final String named=CMParms.combine(commands,2);
			if(CMSecurity.isAnyFlagDisabled(named.toUpperCase()))
				mob.tell(L("'@x1' is already disabled",named));
			else
			{
				mob.tell(L("'@x1' is now disabled",named));
				CMSecurity.setAnyDisableVar(named.toUpperCase().trim());
			}
			return true;
		}
		else
		if(commandType.equals("ENABLEFLAG"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN))
				return errorOut(mob);
			final String named=CMParms.combine(commands,2);
			if(CMSecurity.isAnyFlagEnabled(named.toUpperCase()))
				mob.tell(L("'@x1' is already enabled",named));
			else
			{
				mob.tell(L("'@x1' is now enabled",named));
				CMSecurity.setAnyEnableVar(named.toUpperCase().trim());
			}
			return true;
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
			if(CMSecurity.isDebugging(flag))
				mob.tell(L("'@x1' is already debugging",named));
			else
			{
				mob.tell(L("'@x1' is now debugging",named));
				if(flag == CMSecurity.DbgFlag.HTTPACCESS)
				{
					for(MudHost host : CMLib.hosts())
					{
						try
						{
							host.executeCommand("WEBSERVER ADMIN ACCESS BOTH");
							host.executeCommand("WEBSERVER PUB ACCESS BOTH");
						}
						catch (Exception e)
						{
							mob.tell(e.getMessage());
						}
					}
				}
				else
				if(flag == CMSecurity.DbgFlag.HTTPREQ)
				{
					for(MudHost host : CMLib.hosts())
					{
						try
						{
							host.executeCommand("WEBSERVER ADMIN DEBUG BOTH");
							host.executeCommand("WEBSERVER PUB DEBUG BOTH");
						}
						catch (Exception e)
						{
							mob.tell(e.getMessage());
						}
					}
				}
				CMSecurity.setDebugVar(flag);
			}
			return true;
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
		if(commandType.equals("HOLIDAY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS))
				return errorOut(mob);
			final String named=CMParms.combine(commands,2);
			if(named.trim().length()==0)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
				mob.tell(L("Include a name!"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return false;
			}
			final String err=CMLib.quests().createHoliday(named,mob.location().getArea().name(),true);
			if(err.length()>0)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
				mob.tell(err);
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return false;
			}
			else
				mob.doCommand(new XVector<String>("MODIFY","HOLIDAY",named),metaFlags);
		}
		else
		if(commandType.equals("FACTION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if((commands.size()<3)||(CMParms.combine(commands,2).indexOf('.')<0))
			{
				mob.tell(L("Create which faction?  You must give a filename with an extension."));
				return false;
			}
			else
			{
				final String name=CMParms.combine(commands,2);
				if((name.indexOf(' ')>=0)||(name.length()==0))
				{
					mob.tell(L("That name is not allowed. No spaces!"));
					return false;
				}
				Faction F=CMLib.factions().getFaction(name);
				if(F==null)
					F=CMLib.factions().getFactionByName(name);
				if(F!=null)
				{
					mob.tell(L("Faction '@x1' already exists.  Try another.",name));
					return false;
				}
				else
				if((!mob.isMonster())&&(mob.session().confirm(L("Create a new faction with ID/filename: 'resources/@x1' (N/y)? ",name),"N")))
				{
					//name=Resources.buildResourcePath("")+name;
					final StringBuffer template=new CMFile(Resources.buildResourcePath("examples")+"factiontemplate.ini",null,CMFile.FLAG_LOGERRORS).text();
					if((template==null)||(template.length()==0))
					{
						mob.tell(L("The file 'resources/examples/factiontemplate.ini' could not be located and is required for command line faction creation."));
						return false;
					}
					//Resources.submitResource(name,template);
					Resources.saveFileResource("::"+CMLib.factions().makeFactionFilename(name),null,template);
					F=(Faction)CMClass.getCommon("DefaultFaction");
					F.initializeFaction(template,name);
					CMLib.factions().modifyFaction(mob,F);
					Log.sysOut("CreateEdit",mob.Name()+" created Faction "+F.name()+" ("+F.factionID()+").");
				}
				else
					return false;
			}
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			players(mob,commands);
		}
		else
		if(commandType.equals("NEWS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			final Item I=CMClass.getItem("StdJournal");
			I.setName(L("SYSTEM_NEWS"));
			I.setDescription(L("Enter `LIST NEWS [NUMBER]` to read an entry.%0D%0AEnter CREATE NEWS to add new entries. "));
			final CMMsg newMsg=CMClass.getMsg(mob,I,null,CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,null,
					CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,CMParms.combine(commands,2),
					CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,null);
			if(mob.location().okMessage(mob,newMsg)&&I.okMessage(mob, newMsg))
			{
				mob.location().send(mob,newMsg);
				I.executeMsg(mob,newMsg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^SThe world is now more informed!^?"));
			}
			else
				return false;
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
		if(commandType.equals("POLL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.POLLS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			final Poll P=(Poll)CMClass.getCommon("DefaultPoll");
			while(CMLib.polls().getPoll(P.getName())!=null)
				P.setName(P.getName()+"!");
			P.setFlags(Poll.FLAG_ACTIVE);
			CMLib.polls().createPoll(P);
			CMLib.polls().modifyVote(P, mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^SThe world has grown more uncertain.^?"));
			Log.sysOut("CreateEdit",mob.Name()+" created Poll "+P.getName()+".");
		}
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS))
				return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			if(commands.size()<3)
			{
				if((mob.session()!=null)&&(mob.session().confirm(L("Create a new Quest using the Quest Maker Wizard (y/N)? "),"N")))
					CMLib.quests().questMaker(mob);
				else
				{
					mob.tell(L("You must specify a valid quest string.  Try AHELP QUESTS."));
					return false;
				}
			}
			else
			{
				final String script=CMParms.combine(commands,2);
				final Quest Q=(Quest)CMClass.getCommon("DefaultQuest");
				Q.setAuthor(mob.Name());
				Q.setScript(script,true);
				if((Q.name().trim().length()==0)||(Q.duration()<0))
				{
					mob.tell(L("You must specify a VALID quest string.  This one contained errors.  Try AHELP QUESTS."));
					return false;
				}
				else
				if((CMLib.quests().fetchQuest(Q.name())!=null)
				&&((mob.isMonster())
					||(!mob.session().confirm(L("That quest is already loaded.  Load a duplicate (N/y)? "),"N"))))
						return false;
				else
				{
					mob.tell(L("Quest '@x1' added.",Q.name()));
					CMLib.quests().addQuest(Q);
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
			{
				mob.tell(L("You must specify a valid clan name.  Try CLANLIST and AHELP CLANS."));
				return false;
			}
			else
			{
				final String name=CMParms.combine(commands,2);
				final Clan C=(Clan)CMClass.getCommon("DefaultClan");
				C.setName(name);
				if(C.name().trim().length()==0)
				{
					mob.tell(L("You must specify a VALID clan name."));
					return false;
				}
				else
				if(CMLib.clans().getClan(C.clanID())!=null)
				{
					mob.tell(L("That clan already exists, try CLANLIST."));
					return false;
				}
				else
				{
					mob.tell(L("Clan '@x1' created.",C.name()));
					C.setStatus(Clan.CLANSTATUS_ACTIVE);
					C.create();
					if(CMLib.clans().getClan(C.clanID())==null)
						CMLib.clans().addClan(C);
				}
			}
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
					mob.tell(CMLib.hosts().get(0).executeCommand("START WEB "+commands.get(2)));
				}
				catch (Exception e)
				{
					mob.tell(L("Failure: @x1",e.getMessage()));
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
			{
				mob.tell(L("You must specify a valid government name.  Try LIST GOVERNMENTS."));
				return false;
			}
			else
			{
				final String name=CMParms.combine(commands,2);
				final ClanGovernment G=CMLib.clans().createGovernment(name);
				if(G==null)
				{
					mob.tell(L("You must specify an unused government name."));
					return false;
				}
				else
				if(!mob.isMonster())
				{
					mob.tell(L("Government '@x1' created.",G.getName()));
					CMLib.genEd().modifyGovernment(mob, G,-1);
					CMLib.clans().reSaveGovernmentsXML();
					Log.sysOut("CreateEdit",mob.Name()+" created Clan Government "+G.getName()+".");
				}
				else
					return false;
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			String firstWord=allWord;
			String lastWord=null;
			if(commands.size()>2)
			{
				firstWord=commands.get(1);
				lastWord=commands.get(commands.size()-1);
			}
			Environmental E=null;
			E=CMClass.getItem(allWord);
			if((E instanceof Item)
			||(CMLib.english().numPossibleGold(null,allWord)>0)
			||(CMLib.catalog().getCatalogItem(allWord)!=null))
			{
				commands.add(1,"ITEM");
				execute(mob,commands,metaFlags);
			}
			else
			{
				E=CMClass.getMOB(allWord);
				if((E instanceof MOB)
				||(CMLib.catalog().getCatalogMob(allWord)!=null))
				{
					commands.add(1,"MOB");
					execute(mob,commands,metaFlags);
				}
				else
				if((lastWord!=null)&&(CMLib.directions().getGoodDirectionCode(lastWord)>=0))
				{
					commands.remove(commands.size()-1);
					allWord=CMParms.combine(commands,1);

					E=CMClass.getLocale(allWord);
					if(E==null)
						E=CMClass.getExit(allWord);
					if(E==null)
						E=CMClass.getAreaType(allWord);
					if((E!=null)&&(E instanceof Room))
					{
						commands=new Vector<String>();
						commands.add("CREATE");
						commands.add("ROOM");
						commands.add(lastWord);
						commands.add(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if((E!=null)&&(E instanceof Exit))
					{
						commands=new Vector<String>();
						commands.add("CREATE");
						commands.add("EXIT");
						commands.add(lastWord);
						commands.add(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					if((E!=null)&&(E instanceof Area))
					{
						commands=new Vector<String>();
						commands.add("CREATE");
						commands.add("AREA");
						commands.add(lastWord);
						commands.add(allWord);
						execute(mob,commands,metaFlags);
					}
					else
					{
						mob.tell(L("\n\rYou cannot create a '@x1'. However, you might try an EXIT, ITEM, QUEST, FACTION, COMPONENT, GOVERNMENT, HOLIDAY, "
								+ "CLAN, MOB, RACE, MIXEDRACE, ABILITY, LANGUAGE, CRAFTSKILL, ACHIEVEMENT, MANUFACTURER, ALLQUALIFY, CLASS, POLL, DEBUGFLAG, "
								+ "WEBSERVER, DISABLEFLAG, ENABLEFLAG, NEWS, USER, or ROOM.",commandType));
						return false;
					}
				}
				else
				if(CMath.isInteger(firstWord)
				&&(lastWord!=null))
				{
					final int num=CMath.s_int(firstWord);
					final String theRest=CMParms.combine(commands,2);
					int matCode=RawMaterial.CODES.FIND_IgnoreCase(theRest);
					if(matCode<0)
						matCode=RawMaterial.CODES.FIND_StartsWith(theRest);
					if((theRest.length()>0)&&(matCode>=0))
					{
						for(int i=0;i<num;i++)
							mob.location().addItem(CMLib.materials().makeItemResource(matCode),ItemPossessor.Expire.Player_Drop);
						mob.location().showHappens(CMMsg.MSG_OK_VISUAL, L("Suddenly @x1 @x2 fall from the sky.",""+num,RawMaterial.CODES.NAME(matCode)));
					}
					else 
					if(CMLib.english().numPossibleGold(null,firstWord+" "+theRest)>0)
					{
						commands.add(1,"ITEM");
						return execute(mob,commands,metaFlags);
					}
					else
					{
						final List<String> V=new XVector<String>(commands);
						// this is some strange shit.
						V.remove(1);
						for(int i=0;i<num;i++)
							if((!execute(mob,V,metaFlags))||(!CMLib.flags().isInTheGame(mob, true)))
								return false;
					}
				}
				else
				if(CMClass.getRace(allWord)!=null)
				{
					commands.add(1,"MOB");
					execute(mob,commands,metaFlags);
				}
				else
				{
					E=CMClass.getItem(firstWord);
					if((E instanceof Item)
					||(CMLib.english().numPossibleGold(null,firstWord)>0)
					||(CMLib.catalog().getCatalogItem(firstWord)!=null))
					{
						commands.add(1,"ITEM");
						return execute(mob,commands,metaFlags);
					}
					mob.tell(L("\n\rYou cannot create a '@x1'. However, you might try an EXIT, ITEM, QUEST, FACTION, MOB, COMPONENT, GOVERNMENT, "
							+ "MANUFACTURER, HOLIDAY, CLAN, RACE, MIXEDRACE, ABILITY, LANGUAGE, CRAFTSKILL, ALLQUALIFY, ACHIEVEMENT, CLASS, POLL, USER, "
							+ "WEBSERVER, DEBUGFLAG, NEWS, DISABLEFLAG, ENABLEFLAG, or ROOM.",commandType));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(MOB mob)
	{
		return CMSecurity.isAllowedContainsAny(mob, mob.location(), CMSecurity.SECURITY_CMD_GROUP);
	}

}
