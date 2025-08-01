package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
import com.planet_ink.coffee_mud.core.exceptions.MQLException;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Achievement;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.UpdateSet;
import com.planet_ink.coffee_mud.Libraries.interfaces.AutoAwardsLibrary.AutoProperties;
import com.planet_ink.coffee_mud.Libraries.interfaces.HelpLibrary.HelpSection;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrCallback;
import com.planet_ink.coffee_mud.Libraries.interfaces.JournalsLibrary.MsgMkrResolution;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
public class Modify extends StdCommand
{
	public Modify()
	{
	}

	private final String[] access=I(new String[]{"MODIFY","MOD"});

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	private final static Class<?>[][] internalParameters=new Class<?>[][]{{Environmental.class}};

	public void items(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ITEM [ITEM NAME](@ room/[MOB NAME]) "
					+ "[LEVEL, ABILITY, REJUV, USES, MISC, ?] [NUMBER, TEXT]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		String itemID=(commands.get(2));
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
			command=commands.get(3).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);

		Environmental modE=null;
		if((srchMob!=null)&&(srchRoom!=null))
		{
			final Environmental E=srchRoom.fetchFromMOBRoomFavorsItems(srchMob,srchContainer,itemID,Wearable.FILTER_ANY);
			if(E instanceof Item)
				modE=E;
			else
				modE=srchRoom.findItem(srchContainer,itemID);
		}
		else
		if(srchMob!=null)
			modE=srchMob.findItem(itemID);
		else
		if(srchRoom!=null)
		{
			modE=srchRoom.findItem(srchContainer, itemID);
			if(modE==null)
				modE=srchRoom.findItem(itemID);
		}
		if(modE==null)
		{
			Environmental E=CMLib.space().findSpaceObject(itemID,true);
			if(!(E instanceof Item))
				E=CMLib.space().findSpaceObject(itemID,false);
			if(E instanceof Item)
				modE=E;
		}
		if(modE == null)
		{
			ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(srchMob);
			if(SK!=null)
			{
				final CoffeeShop shop=(SK instanceof Librarian)?((Librarian)SK).getBaseLibrary():SK.getShop();
				final Environmental E=shop.getStock(itemID,mob);
				if(E instanceof Item)
					modE=E;
			}
			if(modE == null)
			{
				SK=CMLib.coffeeShops().getShopKeeper(srchRoom);
				if(SK!=null)
				{
					final CoffeeShop shop=(SK instanceof Librarian)?((Librarian)SK).getBaseLibrary():SK.getShop();
					final Environmental E=shop.getStock(itemID,mob);
					if(E instanceof Item)
						modE=E;
				}
			}
		}
		if((modE==null)||(!(modE instanceof Item)))
		{
			mob.tell(L("I don't see '@x1 here.\n\r",itemID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final Item modItem=(Item)modE;
		mob.location().showOthers(mob,modItem,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>."));

		final Item copyItem=(Item)modItem.copyOf();
		if(command.equals("LEVEL"))
		{
			final int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.basePhyStats().setLevel(newLevel);
				modItem.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			final int newAbility=CMath.s_int(restStr);
			modItem.basePhyStats().setAbility(newAbility);
			modItem.recoverPhyStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
		}
		else
		if(command.equals("HEIGHT"))
		{
			final int newAbility=CMath.s_int(restStr);
			modItem.basePhyStats().setHeight(newAbility);
			modItem.recoverPhyStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
		}
		else
		if(command.equals("REJUV"))
		{
			final int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.basePhyStats().setRejuv(newRejuv);
				modItem.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
			}
			else
			{
				modItem.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				modItem.recoverPhyStats();
				mob.tell(L("@x1 will now never rejuvenate.",modItem.name()));
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
			}
		}
		else
		if(command.equals("USES"))
		{
			final int newUses=CMath.s_int(restStr);
			if(newUses>=0)
			{
				modItem.setUsesRemaining(newUses);
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
			}
		}
		else
		if(command.equals("SAVEABLE")
		&&(CMath.isBool(restStr)))
		{
			CMLib.flags().setSavable(modItem, CMath.s_bool(restStr));
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
		}
		else
		if(command.equals("MISC"))
		{
			if(modItem.isGeneric())
				CMLib.genEd().genMiscSet(mob,modItem,-1);
			else
				modItem.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
		}
		else
		if(CMLib.coffeeMaker().isAnyGenStat(modItem, command))
		{
			CMLib.coffeeMaker().setAnyGenStat(modItem,command, restStr);
			modItem.recoverPhyStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
		}
		else
		if((command.length()==0)&&(modItem.isGeneric()))
		{
			CMLib.genEd().genMiscSet(mob,modItem,-1);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modItem.name()));
		}
		else
		{
			final STreeSet<String> set=new STreeSet<String>();
			set.addAll(CMParms.parseCommas("LEVEL,ABILITY,HEIGHT,REJUV,USES,MISC",true));
			set.addAll(CMLib.coffeeMaker().getAllGenStats(modItem));
			mob.tell(L("...but failed to specify an aspect.  Try one of: @x1",CMParms.toListString(set)));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
		}
		if(!copyItem.sameAs(modItem))
		{
			Log.sysOut("Items",mob.Name()+" modified item "+modItem.ID()+".");
			if(modItem instanceof SpaceObject)
			{
				CMLib.database().DBUpdateItem("SPACE", modItem);
			}
		}
		if((copyItem instanceof SpaceShip)
		&&(((SpaceShip)copyItem).getArea().Name().equals(((SpaceShip)modItem).getArea().Name())))
		{
			copyItem.setName("DO NOT DE_REGISTER!!");
			((SpaceShip)copyItem).getArea().setName("DO NOT DE_REGISTER!!");
			((SpaceShip)copyItem).getArea().delBlurbFlag("REGISTRY");
		}
		copyItem.destroy();
	}

	protected void flunkRoomCmd(final MOB mob)
	{
		mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION, "
				+ "AFFECTS, BEHAVIORS, CLASS, XGRID, YGRID, ?] [TEXT]\n\r"));
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
	}

	protected void flunkAreaCmd(final MOB mob)
	{
		mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY AREA [NAME, DESCRIPTION, CLIMATE, "
				+ "FILE, AFFECTS, BEHAVIORS, ADDSUB, DELSUB, XGRID, YGRID, ?] [TEXT]\n\r"));
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
	}

	protected void fixDeities(final Room R)
	{
		if(R==null)
			return;
		//OK! Keep this weirdness here!  It's necessary because oldRoom will have blown
		//away any real deities with copy deities in the CMMap, and this will restore
		//the real ones.
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M instanceof Deity)
			&&(M.isMonster())
			&&(M.isSavable())
			&&(M.getStartRoom()==R))
				CMLib.map().registerWorldObjectLoaded(R.getArea(), R, M);
		}
	}

	public void autoawards(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rFormat: MODIFY AWARD [NUMBER].\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final String numStr=CMParms.combine(commands,2);
		final int numTarget=CMath.s_int(numStr);
		if(numTarget<=0)
		{
			mob.tell(L("@x1 is not a number.",numStr));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		int i=1;
		AutoProperties autoProp = null;
		for(final Enumeration<AutoProperties> e=CMLib.awards().getAutoProperties();e.hasMoreElements();)
		{
			final AutoProperties a = e.nextElement();
			if(i==numTarget)
			{
				autoProp = a;
				break;
			}
			i++;
		}
		if(autoProp==null)
		{
			mob.tell(L("@x1 is not a value number. Try LIST AWARD.",numStr));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		try
		{
			autoProp = CMLib.awards().modifyAutoProperty(mob, autoProp, -1);
			if(autoProp == null)
			{
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return;
			}
			final StringBuilder line = new StringBuilder("");
			line.append(autoProp.getPlayerMask()).append(" :: ");
			line.append(autoProp.getDateMask()).append(" :: ");
			for(final Pair<String,String> p : autoProp.getProps())
			{
				line.append(p.first.trim())
				.append("(")
				.append(CMStrings.replaceAll(p.second,")","\\)"))
				.append(") ");
			}
			CMLib.awards().modifyAutoProperty(numTarget, line.toString());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The power of time just went sideways!"));
		}
		catch(final IOException ioe)
		{
		}
	}

	public void expertises(final MOB mob, final List<String> commands)
	{
		if((commands.size()<3)||(CMParms.combine(commands,1).indexOf('=')<0))
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rFormat: MODIFY EXPERTISE [EXPERTISE ID]=[PARAMETERS] as follows: \n\r"));
			final String inst=CMLib.expertises().getExpertiseInstructions();
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
		if(CMLib.expertises().numStages(WKID)<=0)
		{
			mob.tell(L("'@x1' does not exist, try LIST EXPERTISES.",WKID));
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
		if(!CMLib.expertises().addModifyDefinition(parms, true))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The power of skill usage just went sideways!"));
	}

	public void rooms(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(mob.location().getGridParent() != null)
		{
			mob.tell(L("This command is invalid from within a GridLocaleChild room."));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around the room."));
		if(commands.size()==2)
		{
			final Room oldRoom=(Room)mob.location().copyOf();
			final Room newRoom=CMLib.genEd().modifyRoom(mob,mob.location(),-1);
			if((!oldRoom.sameAs(newRoom))&&(!newRoom.amDestroyed()))
			{
				CMLib.database().DBUpdateRoom(newRoom);
				newRoom.showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
				Log.sysOut("Rooms",mob.Name()+" modified room "+newRoom.roomID()+".");
			}
			oldRoom.destroy();
			newRoom.getArea().fillInAreaRoom(newRoom);
			fixDeities(newRoom);
			return;
		}
		if (commands.size() < 3)
		{
			flunkRoomCmd(mob);
			return;
		}

		final String command=commands.get(2).toUpperCase();
		String restStr="";
		if(commands.size()>=3)
			restStr=CMParms.combine(commands,3);

		if(command.equalsIgnoreCase("AREA"))
		{
			if (commands.size() < 4)
			{
				flunkRoomCmd(mob);
				return;
			}
			boolean confirmed=false;
			String areaType="";
			if((commands.size()>4)&&(CMClass.getAreaType(commands.get(commands.size()-1).toString())!=null))
			{
				areaType=commands.remove(commands.size()-1).toString();
				restStr=CMParms.combine(commands,3);
				confirmed=true;
			}
			Area A=CMLib.map().getArea(restStr);
			boolean reid=false;
			if((A==null)&&(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDAREAS)))
			{
				mob.session().println(L("Not permitted."));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
			else
			if(A==null)
			{
				if((!mob.isMonster())&&(CMLib.map().getShip(restStr)==null))
				{
					if(confirmed||mob.session().confirm(L("\n\rThis command will create a BRAND NEW AREA\n\r with Area code '@x1'.  Are you SURE (y/N)?",restStr),"N"))
					{
						int tries=0;
						while((areaType.length()==0)&&((++tries)<10)&&(mob.session()!=null)&&(!mob.session().isStopped()))
						{
							areaType=mob.session().prompt(L("Enter an area type to create (default=StdArea): "),L("StdArea"));
							if(CMClass.getAreaType(areaType)==null)
							{
								mob.session().println(L("Invalid area type! Valid ones are:"));
								mob.session().println(CMLib.lister().build3ColTable(mob,CMClass.areaTypes()).toString());
								areaType="";
							}
						}
						if(areaType.length()==0)
							areaType="StdArea";
						A=CMClass.getAreaType(areaType);
						A.setName(restStr);
						CMLib.map().addArea(A);
						CMLib.database().DBCreateArea(A);
						mob.location().setArea(A);
						CMLib.map().registerWorldObjectLoaded(A, null, A);
						CMLib.coffeeMaker().addAutoPropsToAreaIfNecessary(A);
						reid=true;
						mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("This entire area twitches.\n\r"));
					}
				}
				else
				{
					mob.tell(L("Sorry Charlie!"));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				}
			}
			else
			{
				mob.location().setArea(A);
				if(A.getRandomProperRoom()!=null)
					reid=true;
				else
					CMLib.database().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("This area twitches.\n\r"));
			}

			if(reid)
			{
				Room R=mob.location();
				final String oldID=R.roomID();
				synchronized(CMClass.getSync("SYNC"+R.roomID()))
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
		if(command.equalsIgnoreCase("ID"))
		{
			if (commands.size() < 4)
			{
				flunkRoomCmd(mob);
				return;
			}
			Room R=mob.location();
			final Area A = R.getArea();
			final String oldID=R.roomID();
			String newID=restStr.trim();
			if(CMath.isInteger(newID))
				newID=A.Name()+"#"+newID;
			else
			if(newID.startsWith("#") && CMath.isInteger(newID.substring(1)))
				newID=A.Name()+newID;
			else
			if(newID.toLowerCase().startsWith(A.Name().toLowerCase()+"#")
			&&(CMath.isInteger(newID.substring(A.Name().length()+1))))
				newID=A.Name()+"#"+newID.substring(A.Name().length()+1);
			else
			{
				mob.tell(L("'@x1' must start with its area name.",newID));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
			if(CMLib.map().getRoom(newID)==R)
			{
				mob.tell(L("Uh, well done?"));
				return;
			}
			if(CMLib.map().getRoom(newID)!=null)
			{
				mob.tell(L("'@x1' is already being used by another room.",newID));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
			synchronized(CMClass.getSync("SYNC"+R.roomID()))
			{
				R=CMLib.map().getRoom(R);
				if(CMLib.map().getRoom(newID)==R)
				{
					mob.tell(L("Uh, well done?"));
					return;
				}
				mob.location().setRoomID(newID);
				CMLib.database().DBReCreate(R,oldID);
			}
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
		}
		else
		if(command.equalsIgnoreCase("NAME"))
		{
			if (commands.size() < 4)
			{
				flunkRoomCmd(mob);
				return;
			}
			mob.location().setDisplayText(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
		}
		else
		if(command.equalsIgnoreCase("CLASS"))
		{
			if (commands.size() < 4)
			{
				flunkRoomCmd(mob);
				return;
			}
			final Room newRoom=CMClass.getLocale(restStr);
			if(newRoom==null)
			{
				mob.tell(L("'@x1' is not a valid room locale.",restStr));
				return;
			}
			CMLib.genEd().changeRoomType(mob.location(),newRoom);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
		}
		else
		if((command.equalsIgnoreCase("XGRID"))&&(mob.location() instanceof GridLocale))
		{
			if (commands.size() < 4)
			{
				flunkRoomCmd(mob);
				return;
			}
			((GridLocale)mob.location()).setXGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
		}
		else
		if((command.equalsIgnoreCase("YGRID"))&&(mob.location() instanceof GridLocale))
		{
			if (commands.size() < 4)
			{
				flunkRoomCmd(mob);
				return;
			}
			((GridLocale)mob.location()).setYGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			if (commands.size() < 4)
			{
				flunkRoomCmd(mob);
				return;
			}
			mob.location().setDescription(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The very nature of reality changes.\n\r"));
		}
		else
		if(command.equalsIgnoreCase("AFFECTS"))
		{
			CMLib.genEd().genAffects(mob,mob.location(),1,1);
			mob.location().recoverPhyStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The very nature of reality changes.\n\r"));
		}
		else
		if(command.equalsIgnoreCase("BEHAVIORS"))
		{
			CMLib.genEd().genBehaviors(mob,mob.location(),1,1);
			mob.location().recoverPhyStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The very nature of reality changes.\n\r"));
		}
		else
		if(CMLib.coffeeMaker().isAnyGenStat(mob.location(), command))
		{
			final String roomID = mob.location().roomID();
			final boolean roomIDChange=command.equalsIgnoreCase("roomID");
			if(roomIDChange)
			{
				if(restStr.length()==0)
				{
					mob.tell(L("'@x1' is not a valid room id.",restStr));
					return;
				}
				else
				if((CMLib.map().getRoom(restStr) != null) && (CMLib.map().getRoom(restStr) != mob.location()))
				{
					mob.tell(L("'@x1' is not a valid room id: Already Exists.",restStr));
					return;
				}
			}
			CMLib.coffeeMaker().setAnyGenStat(mob.location(),command, restStr);
			if(roomIDChange && (!roomID.equals(mob.location().roomID())))
			{
				if(mob.location().roomID().length()==0)
					mob.location().setRoomID(roomID);
				else
					CMLib.database().DBReCreate(mob.location(),roomID);
			}
			mob.location().recoverPhyStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The very nature of reality changes.\n\r"));
		}
		else
		{
			final Room R=CMLib.map().getRoom(command);
			if((R==null)
			||(!CMSecurity.isAllowed(mob, R, CMSecurity.SecFlag.CMDROOMS)))
			{
				final STreeSet<String> set=new STreeSet<String>();
				set.addAll(CMParms.parseCommas("NAME,AREA,DESCRIPTION,AFFECTS,BEHAVIORS,CLASS,XGRID,YGRID,[ROOM ID]",true));
				set.addAll(CMLib.coffeeMaker().getAllGenStats(mob.location()));
				mob.tell(L("...but failed to specify an aspect.  Try one of: @x1",CMParms.toListString(set)));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			}
			else
			{
				final Room oldRoom=(Room)mob.location().copyOf();
				final Room newRoom=CMLib.genEd().modifyRoom(mob,R,-1);
				if((!oldRoom.sameAs(newRoom))&&(!newRoom.amDestroyed()))
				{
					CMLib.database().DBUpdateRoom(newRoom);
					newRoom.showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
					Log.sysOut("Rooms",mob.Name()+" modified room "+newRoom.roomID()+".");
				}
				oldRoom.destroy();
				newRoom.getArea().fillInAreaRoom(newRoom);
				fixDeities(newRoom);
			}
			return;
		}
		mob.location().recoverRoomStats();
		Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
	}

	public void accounts(final MOB mob, final List<String> commands)
		throws IOException
	{
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around the heavens."));
		PlayerAccount theAccount = null;
		String oldName = null;
		if(commands.size()==2)
		{
			theAccount=mob.playerStats().getAccount();
			oldName=theAccount.getAccountName();
			CMLib.genEd().modifyAccount(mob,theAccount,-1);
		}
		else
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ACCOUNT ([NAME])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		else
		{
			final String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
			theAccount = CMLib.players().getLoadAccount(accountName);
			if(theAccount==null)
			{
				mob.tell(L("There is no account called '@x1'!\n\r",accountName));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
			oldName=theAccount.getAccountName();
			CMLib.genEd().modifyAccount(mob,theAccount,-1);
			mob.location().recoverRoomStats();
		}
		Log.sysOut("Modify",mob.Name()+" modified account "+theAccount.getAccountName()+".");
		if(!oldName.equals(theAccount.getAccountName()))
		{
			final List<MOB> V=new ArrayList<MOB>();
			for(final Enumeration<String> es=theAccount.getPlayers();es.hasMoreElements();)
			{
				final String playerName=es.nextElement();
				final MOB playerM=CMLib.players().getLoadPlayer(playerName);
				if((playerM!=null)&&(!CMLib.flags().isInTheGame(playerM,true)))
					V.add(playerM);
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

	public void doMql(final MOB mob, final boolean areaFlag, final List<String> commands)
		throws IOException
	{
		if(commands.size()<2)
			mob.tell(L("This is not valid MQL."));
		else
		{
			final StringBuilder lines = new StringBuilder("");
			final String mql=CMParms.combineQuoted(commands,0);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around."));
			try
			{
				final List<UpdateSet> res=CMLib.percolator().doMQLUpdateObjects(areaFlag?(mob.location().getArea()):null, mql);
				if(res.size()==0)
					lines.append("(nothing to do)");
				else
				{
					lines.append("Update preview:\n\r");
					for(final UpdateSet o : res)
					{
						if(o.first instanceof Environmental)
							lines.append(o.first.name()+" ("+o.first.ID()+") @"+CMLib.map().getApproximateExtendedRoomID(CMLib.map().roomLocation((Environmental)o.first))+":\n\r");
						else
							lines.append(o.first.name()+" ("+o.first.ID()+"):\n\r");
						lines.append("  OLD: "+o.second+"="+o.first.getStat(o.second)).append("\n\r");
						lines.append("  NEW: "+o.second+"="+o.third).append("\n\r");
					}
					final Runnable doUpdate = new Runnable()
					{
						final List<UpdateSet> todo=res;
						@Override
						public void run()
						{
							for(final UpdateSet o : todo)
							{
								o.first.setStat(o.second, o.third);
								if(o.first instanceof Environmental)
								{
									Environmental E=(Environmental)o.first;
									final Room R=CMLib.map().roomLocation(E);
									if((R!=null) && R.isSavable() && (R.roomID().length()>0))
									{
										Log.infoOut(mob.name()+" modified "+E.name()+" at "+R.roomID());
										if(E instanceof Ability)
											E=((Ability)E).affecting();
										if(E instanceof Room)
											CMLib.database().DBUpdateRoom(R);
										else
										if(E instanceof Item)
										{
											final Item I=(Item)E;
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
										if((E instanceof MOB)
										&&(((MOB)E).databaseID().length()>0))
											CMLib.database().DBUpdateMOB(R.roomID(), (MOB)E);
									}
								}
							}
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
								session.promptPrint(L("\n\rSave the above changes (y/N)? "));
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
									doUpdate.run();
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
				lines.append(e.getMessage()+"\n\r"+bout.toString());
				if(mob.session()!=null)
					mob.session().wraplessPrintln(lines.toString());
				lines.setLength(0);
			}
		}
	}

	public void areas(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(mob.location()==null)
			return;
		if(mob.location().getArea()==null)
			return;
		Area myArea=mob.location().getArea();

		String oldName=myArea.Name();
		final List<Room> allMyDamnRooms=new Vector<Room>();
		for(final Enumeration<Room> e=myArea.getCompleteMap();e.hasMoreElements();)
			allMyDamnRooms.add(e.nextElement());

		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around wildly."));
		Resources.removeResource("HELP_"+myArea.Name().toUpperCase());
		final Set<Area> alsoUpdateAreas=new HashSet<Area>();
		final Area copyA = (Area)myArea.copyOf();
		final TimeClock copyClock = (TimeClock)myArea.getTimeObj().copyOf();
		final TimeClock oldClock = myArea.getTimeObj();
		if(commands.size()==2)
			CMLib.genEd().modifyArea(mob,myArea,alsoUpdateAreas, -1);
		else
		if((commands.size()==3)&&(CMLib.map().getArea(commands.get(2))!=null))
		{
			myArea=CMLib.map().getArea(commands.get(2));
			oldName=myArea.Name();
			CMLib.genEd().modifyArea(mob,myArea,alsoUpdateAreas, -1);
		}
		else
		{
			if (commands.size() < 3)
			{
				flunkAreaCmd(mob);
				return;
			}

			String command=commands.get(2).toUpperCase();
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
					command=commands.get(2).toUpperCase();
				}
			}
			String restStr="";
			if(commands.size()>=3)
				restStr=CMParms.combine(commands,3);

			if(command.equalsIgnoreCase("NAME"))
			{
				if (commands.size() < 4)
				{
					flunkAreaCmd(mob);
					return;
				}
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
				if (commands.size() < 4)
				{
					flunkAreaCmd(mob);
					return;
				}
				myArea.setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase("FILE"))
			{
				if (commands.size() < 4)
				{
					flunkAreaCmd(mob);
					return;
				}
				myArea.setArchivePath(restStr);
			}
			else
			if((command.equalsIgnoreCase("XGRID"))&&(myArea instanceof GridZones))
			{
				if (commands.size() < 4)
				{
					flunkAreaCmd(mob);
					return;
				}
				((GridZones)myArea).setXGridSize(CMath.s_int(restStr));
			}
			else
			if((command.equalsIgnoreCase("YGRID"))&&(myArea instanceof GridZones))
			{
				if (commands.size() < 4)
				{
					flunkAreaCmd(mob);
					return;
				}
				((GridZones)myArea).setYGridSize(CMath.s_int(restStr));
			}
			else
			if(command.equalsIgnoreCase("CLIMATE"))
			{
				if (commands.size() < 4)
				{
					flunkAreaCmd(mob);
					return;
				}
				int newClimate=0;
				for(int i=0;i<restStr.length();i++)
				{
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
						mob.tell(L("Invalid CLIMATE code: '@x1'.  Valid codes include: R)AINY, H)OT, C)OLD, D)RY, W)INDY, N)ORMAL.\n\r",""+restStr.charAt(i)));
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
						return;
					}
				}
				myArea.setClimateType(newClimate);
			}
			else
			if(command.equalsIgnoreCase("ADDSUB"))
			{
				if((commands.size()<4)||(!CMLib.players().playerExists(restStr)))
				{
					mob.tell(L("Unknown or invalid username given.\n\r"));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				}
				myArea.addSubOp(restStr);
			}
			else
			if(command.equalsIgnoreCase("DELSUB"))
			{
				if((commands.size()<4)||(!myArea.amISubOp(restStr)))
				{
					mob.tell(L("Unknown or invalid staff name given.  Valid names are: @x1.\n\r",myArea.getSubOpList()));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
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
				mob.tell(L("...but failed to specify an aspect.  Try one of: @x1",CMParms.toListString(helpSet)));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return;
			}
		}

		if((!myArea.Name().equals(oldName))&&(!mob.isMonster()))
		{
			if(mob.session().confirm(L("Is changing the name of this area really necessary (y/N)?"),"N"))
			{
				for(final Enumeration<Room> r=myArea.getCompleteMap();r.hasMoreElements();)
				{
					Room R=r.nextElement();
					synchronized(CMClass.getSync("SYNC"+R.roomID()))
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
		if(!copyA.sameAs(myArea))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("There is something different about this place...\n\r"));
		if(myArea.name().equals(oldName))
			CMLib.database().DBUpdateArea(myArea.Name(),myArea);
		else
		{
			CMLib.database().DBUpdateArea(oldName,myArea);
			CMLib.map().renameRooms(myArea,oldName,allMyDamnRooms);
		}
		for(final Area A : alsoUpdateAreas)
		{
			if((A!=myArea)
			&&(!A.Name().equals(myArea.Name())))
				CMLib.database().DBUpdateArea(A.Name(),A);
		}
		copyClock.setHourOfDay(myArea.getTimeObj().getHourOfDay());
		if((!copyA.sameAs(myArea))
		||(oldClock != myArea.getTimeObj())
		||(oldClock.getDaysInMonth() != copyClock.getDaysInMonth())
		||(oldClock.getMonthsInYear() != copyClock.getMonthsInYear())
		||(oldClock.getHoursInDay() != copyClock.getHoursInDay())
		||(!Arrays.equals(oldClock.getDawnToDusk(), copyClock.getDawnToDusk())))
			Log.sysOut("Rooms",mob.Name()+" modified area "+myArea.Name()+".");
		copyA.destroy();
	}

	public void cron(final MOB mob, final List<String> commands) throws IOException
	{
		if(commands.size()<4)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\r"
					+ "The format is MODIFY CRON [NAME/#] [SUBJECT, INTERVAL, SCRIPT] [VALUE]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final String cronID=commands.get(2);
		final String field=commands.get(3).toUpperCase().trim();
		final String value=CMParms.combine(commands, 4);
		final List<JournalEntry> jobs = CMLib.database().DBReadJournalMsgsByCreateDate("SYSTEM_CRON", true);
		JournalEntry modJob=null;
		if(CMath.isInteger(cronID)
		&&(CMath.s_int(cronID)>0)
		&&(CMath.s_int(cronID)<=jobs.size()))
			modJob=jobs.get(CMath.s_int(cronID)-1);
		else
		{
			for(final JournalEntry E : jobs)
			{
				if(E.subj().equalsIgnoreCase(cronID))
					modJob=E;
			}
			if(modJob == null)
			{
				for(final JournalEntry E : jobs)
				{
					if(E.subj().toUpperCase().indexOf(cronID.toUpperCase())>=0)
					{
						modJob=E;
						break;
					}
				}
			}
			if(modJob == null)
			{
				mob.tell(L("@x1 is not a valid [NAME/#].  Try LIST CRON.\n\r",cronID));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return;
			}
		}
		if(field.equals("SUBJECT"))
		{
			if(value.trim().length()==0)
			{
				mob.tell(L("'@x1' is not a valid subject.\n\r",value));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return;
			}
			modJob.subj(field);
			CMLib.database().DBUpdateJournal("SYSTEM_CRON", modJob);
			Log.sysOut(mob.Name()+" modified cron job "+modJob.subj()+".");
		}
		else
		if(field.equals("INTERVAL"))
		{
			long tm;
			try
			{
				if(value.trim().length()==0)
					throw new CMException("Bad value: "+value);
				tm = CMLib.time().parseTickExpression(CMLib.time().homeClock(mob), value);
				if(tm < 0)
					throw new CMException("Bad value: "+tm);
			}
			catch(final CMException e)
			{
				mob.tell(L("@x1 is not a valid interval.  Try like 10 minutes (@x2)!",value,e.getMessage()));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return;
			}
			tm = tm * CMProps.getTickMillis();
			modJob.data("INTERVAL=\""+value+"\"");
			modJob.update(System.currentTimeMillis()+tm);
			CMLib.database().DBUpdateJournal("SYSTEM_CRON", modJob);
			Log.sysOut(mob.Name()+" modified cron job "+modJob.subj()+".");
			CMLib.journals().activate();
		}
		else
		if(field.equals("SCRIPT"))
		{
			final JournalEntry E=modJob;
			final List<String> msgV=Resources.getFileLineVector(new StringBuffer(modJob.msg()));
			CMLib.journals().makeMessageASync(mob, modJob.subj(),msgV, false, new MsgMkrCallback() {
				final JournalEntry E2 = E;
				@Override
				public void callBack(final MOB mob, final Session sess, final MsgMkrResolution res)
				{
					if(res == MsgMkrResolution.SAVEFILE)
					{
						E2.msg(CMParms.combineWith(msgV, "\n\r"));
						CMLib.database().DBUpdateJournal("SYSTEM_CRON", E2);
						Log.sysOut(mob.Name()+" modified cron job "+E2.subj()+".");
					}
				}
			});
		}
	}

	public void quests(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
			mob.tell(L("modify which quest?  Use list quests."));
		else
		{
			int cmdDex=-1;
			final String[] CMDS={"START","STOP","ENABLE","DISABLE"};
			if(commands.size()>3)
			{
				cmdDex=CMParms.indexOf(CMDS,commands.get(commands.size()-1).toUpperCase());
				if(cmdDex>=0)
					commands.remove(commands.size()-1);
			}
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
			{
				mob.tell(L("Quest '@x1' is unknown.  Try list quests.",name));
				return;
			}
			else
			if(!mob.isMonster())
			{
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around @x1.",Q.name()));
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
						newScript=CMLib.genEd().prompt(mob,oldScript,++showNumber,showFlag,L("Script"),false,true,CMLib.help().getHelpText("QUESTS",mob,true).toString(),null,null);
						if(!newScript.equals(oldScript))
						{
							Q.setScript(newScript,true);
							boolean revert=false;
							if(Q.name().length()==0)
							{
								mob.tell(L("You must specify a VALID quest string.  This one contained no name."));
								revert=true;
							}
							else
							if(Q.duration()<0)
							{
								mob.tell(L("You must specify a VALID quest string.  This one contained no duration."));
								revert=true;
							}
							else
							for(int q=0;q<CMLib.quests().numQuests();q++)
							{
								final Quest Q1=CMLib.quests().fetchQuest(q);
								if(Q1.name().equalsIgnoreCase(Q.name())&&(Q1!=Q))
								{
									mob.tell(L("A quest with that name already exists."));
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
							mob.tell(L("That quest is already running."));
						else
						{
							Q.startQuest();
							if((!Q.running())
							&&(Q.getSpawn() != Quest.Spawn.ANY.ordinal())
							&&(Q.getSpawn() != Quest.Spawn.ALL.ordinal()))
								mob.tell(L("Quest '@x1' NOT started -- check your mud.log for errors.",Q.name()));
							else
								mob.tell(L("Quest '@x1' started.",Q.name()));
						}
						break;
					}
					case 1:
					{
						if(!Q.running())
							mob.tell(L("That quest is not running."));
						else
						{
							CMLib.coffeeTables().bump(Q,CoffeeTableRow.STAT_QUESTSTOP);
							Q.stopQuest();
							if(!Q.running())
								mob.tell(L("Quest '@x1' stopped.",Q.name()));
							else
								mob.tell(L("Quest '@x1' NOT stopped -- check your mud.log for errors.",Q.name()));
						}
						break;
					}
					case 2:
					{
						if(!Q.suspended())
							mob.tell(L("That quest is not disabled."));
						else
						{
							Q.setSuspended(false);
							CMLib.database().DBUpdateQuest(Q);
							mob.tell(L("Quest '@x1' enabled.",Q.name()));
						}
						break;
					}
					case 3:
					{
						if(Q.suspended())
							mob.tell(L("That quest is already disabled."));
						else
						{
							if(Q.running())
								Q.stopQuest();
							Q.setSuspended(true);
							CMLib.database().DBUpdateQuest(Q);
							mob.tell(L("Quest '@x1' disabled.",Q.name()));
						}
						break;
					}
					}

					if ((showFlag < -900) || (cmdDex >= 0))
					{
						ok = true;
						break;
					}
					if (showFlag > 0)
					{
						showFlag = -1;
						continue;
					}
					showFlag=CMath.s_int(mob.session().prompt(L("Edit which? "),""));
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

	public void updateChangedExit(final MOB mob, final Room baseRoom, final Exit thisExit, final Exit prevExit)
	{
		thisExit.recoverPhyStats();
		CMLib.database().DBUpdateExits(baseRoom);
		try
		{
			for(final Enumeration<Room> r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room room=r.nextElement();
				synchronized(CMClass.getSync("SYNC"+room.roomID()))
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
		}
		catch (final NoSuchElementException e)
		{
		}
		if(!prevExit.sameAs(thisExit))
			Log.sysOut("CreateEdit",mob.Name()+" modified exit "+thisExit.ID()+".");
		prevExit.destroy();
		mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 shake(s) under the transforming power.",thisExit.name()));
		baseRoom.getArea().fillInAreaRoom(baseRoom);
	}

	public void exits(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(mob.location().getGridParent() != null)
		{
			mob.tell(L("This command is invalid from within a GridLocaleChild room."));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] (TEXT, ?) (VALUE)\n\r"));
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

		final Exit thisExit=mob.location().getRawExit(direction);
		if(thisExit==null)
		{
			mob.tell(L("You have failed to specify a valid exit '@x1'.\n\r",(commands.get(2))));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final Directions.DirType dirType=CMLib.flags().getInDirType(mob);
		final String inDirName=CMLib.directions().getInDirectionName(direction, dirType);
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around to the @x1.",inDirName));
		final Exit copyExit=(Exit)thisExit.copyOf();
		if(thisExit.isGeneric() && (commands.size()<5))
		{
			CMLib.genEd().modifyGenExit(mob,thisExit,-1);
			updateChangedExit(mob,mob.location(),thisExit,copyExit);
			return;
		}

		if(commands.size()<5)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] (TEXT, ?) (VALUE)\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}

		final String command=commands.get(3).toUpperCase();
		final String restStr=CMParms.combine(commands,4);

		if(command.equalsIgnoreCase("text"))
		{
			if(thisExit.isGeneric())
				CMLib.genEd().modifyGenExit(mob,thisExit,-1);
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
			mob.tell(L("...but failed to specify an aspect.  Try one of: @x1",CMParms.toListString(set)));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		updateChangedExit(mob,mob.location(),thisExit,copyExit);
	}

	public boolean races(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY RACE [RACE ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified as it is.  Use CREATE RACE @x2 to convert it to a generic race.",R.ID(),R.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",R.name()));
		CMLib.genEd().modifyGenRace(mob,R,-1);
		CMLib.database().DBDeleteRace(R.ID());
		CMLib.database().DBCreateRace(R.ID(),R.racialParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",R.name()));
		return true;
	}

	public boolean commands(final MOB mob, final List<String> commands)
			throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY COMMAND [COMMAND ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String cmdID=CMParms.combine(commands,2);
		final Command C=CMClass.getCommand(cmdID);
		if(C==null)
		{
			mob.tell(L("'@x1' is an invalid command id.",cmdID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(C.isGeneric()))
		{
			mob.tell(L("'@x1' is not generic, and may not be modified as it is.  Use CREATE COMMAND @x2 to convert it to a generic command.",C.ID(),C.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",C.name()));
		CMLib.genEd().modifyGenCommand(mob,(Modifiable)C,-1);
		final DatabaseEngine.AckRecord rec = CMLib.database().DBDeleteCommand(C.ID());
		if(C instanceof Modifiable)
			CMLib.database().DBCreateCommand(C.ID(),rec.typeClass(),((Modifiable)C).getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",C.name()));
		return true;
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
		final Ability A=CMClass.getAbility(classD);
		if(A==null)
		{
			mob.tell(L("Ability with the ID '@x1' does not exist! Try LIST ABILITIES.",classD));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		Map<String,Map<String,AbilityMapper.AbilityMapping>> map=CMLib.ableMapper().getAllQualifiesMap(null);
		Map<String,AbilityMapper.AbilityMapping> subMap=map.get(eachOrAll.toUpperCase().trim());
		if(!subMap.containsKey(classD.toUpperCase().trim()))
		{
			mob.tell(L("All-Qualify entry (@x1) ID '@x2' does not exist!  Try CREATE, or LIST ALLQUALIFYS.",eachOrAll,A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		final AbilityMapper.AbilityMapping mapped = CMLib.genEd().modifyAllQualifyEntry(mob,eachOrAll.toUpperCase().trim(),A, -1);
		map=CMLib.ableMapper().getAllQualifiesMap(null);
		subMap=map.get(eachOrAll.toUpperCase().trim());
		subMap.put(A.ID().toUpperCase().trim(), mapped);
		CMLib.ableMapper().saveAllQualifysFile(map);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The skill of the world just changed!"));
	}

	public boolean classes(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY CLASS [CLASS ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified as it is.  Use CREATE CLASS @x2 to convert it to a generic character class.",C.ID(),C.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",C.name()));
		CMLib.genEd().modifyGenClass(mob,C,-1);
		CMLib.database().DBDeleteClass(C.ID());
		CMLib.database().DBCreateClass(C.ID(),C.classParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",C.name()));
		return true;
	}

	public boolean abilities(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ABILITY [ABILITY ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof Language)
		{
			mob.tell(L("'@x1' is a language.  Try MODIFY LANGUAGE.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof Trap)
		{
			mob.tell(L("'@x1' is a trap.  Try MODIFY TRAP.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof ItemCraftor)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY CRAFTSKILL.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",A.name()));
		CMLib.genEd().modifyGenAbility(mob,A,-1);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenAbility",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",A.name()));
		return true;
	}

	public boolean traps(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY TRAP [ABILITY ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof Language)
		{
			mob.tell(L("'@x1' is a language.  Try MODIFY LANGUAGE.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof ItemCraftor)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY CRAFTSKILL.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(A instanceof Trap))
		{
			mob.tell(L("'@x1' is not a trap.  Try MODIFY ABILITY.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",A.name()));
		CMLib.genEd().modifyGenTrap(mob,(Trap)A,-1);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenTrap",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",A.name()));
		return true;
	}

	public boolean languages(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY LANGUAGE [SKILL ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof ItemCraftor)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY CRAFTSKILL.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof ItemCollection)
		{
			mob.tell(L("'@x1' is a gathering skill.  Try MODIFY GATHERSKILL.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(A instanceof Language))
		{
			mob.tell(L("'@x1' is not a language.  Try MODIFY ABILITY.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",A.name()));
		CMLib.genEd().modifyGenLanguage(mob,(Language)A,-1);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenLanguage",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",A.name()));
		return true;
	}

	public boolean craftSkills(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY CRAFTSKILL [SKILL ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof Language)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY LANGUAGE.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(A instanceof ItemCraftor))
		{
			if(A instanceof ItemCollection)
				mob.tell(L("'@x1' is a gathering skill.  Try MODIFY GATHERSKILL.",A.ID()));
			else
				mob.tell(L("'@x1' is not a crafting skill.  Try MODIFY ABILITY.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(((ItemCraftor)A).getCraftorType()==ItemCraftor.CraftorType.LargeConstructions)
		{
			mob.tell(L("'@x1' is a building/wrighting skill.  Try MODIFY WRIGHTSKILL.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",A.name()));
		CMLib.genEd().modifyGenCraftSkill(mob,A,-1);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenCraftSkill",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",A.name()));
		return true;
	}

	public boolean gatherSkills(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY GATHERSKILL [SKILL ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof Language)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY LANGUAGE.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof ItemCraftor)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY CRAFTSKILL.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(A instanceof ItemCollection))
		{
			mob.tell(L("'@x1' is not a gathering skill.  Try MODIFY ABILITY.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",A.name()));
		CMLib.genEd().modifyGenGatheringSkill(mob,A,-1);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenGatheringSkill",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",A.name()));
		return true;
	}

	public boolean wrightSkills(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY WRIGHTSKILL [SKILL ID]\n\r"));
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
			mob.tell(L("'@x1' is not generic, and may not be modified.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(A instanceof Language)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY LANGUAGE.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!(A instanceof ItemCraftor))
		{
			if(A instanceof ItemCollection)
				mob.tell(L("'@x1' is a gathering skill.  Try MODIFY GATHERSKILL.",A.ID()));
			else
				mob.tell(L("'@x1' is not a crafting skill.  Try MODIFY ABILITY.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(((ItemCraftor)A).getCraftorType()!=ItemCraftor.CraftorType.LargeConstructions)
		{
			mob.tell(L("'@x1' is a crafting skill.  Try MODIFY CRAFTSKILL.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around all @x1s.",A.name()));
		CMLib.genEd().modifyGenWrightSkill(mob,A,-1);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),"GenWrightSkill",A.getStat("ALLXML"));
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("@x1's everywhere shake under the transforming power!",A.name()));
		return true;
	}

	public void components(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\r"
					+ "Format: MODIFY COMPONENT [SKILL ID]\n\r"
					+ "Format: MODIFY COMPONENT SOCIAL [SOCIAL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		if(commands.get(2).equalsIgnoreCase("SOCIAL"))
		{
			final String socialID=CMParms.combine(commands,3);
			final Vector<String> socialsParse=CMParms.parse(socialID);
			if(socialsParse.size()==0)
			{
				mob.tell(L("Which social? That doesn't exist.  Try LIST COMPONENTS"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
			final String name=socialsParse.firstElement().toUpperCase().trim();
			final String rest=socialsParse.size()>1?CMParms.combine(socialsParse,1):"";
			final List<Social> socials=CMLib.ableComponents().getSocialsSet(name);
			if((socials==null)||(socials.size()==0))
			{
				mob.tell(L("'@x1' does not exist, try LIST COMPONENTS.",socialID));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
				return;
			}
			final List<Social> copy = new XArrayList<Social>(socials);
			final List<String> socEncV = new ArrayList<String>(socials.size());
			for(final Social S : copy)
				socEncV.add(S.getEncodedLine());
			CMLib.socials().modifySocialInterface(mob, socials, name, rest);
			for(final Social copyS : copy)
				if(!socials.contains(copyS))
					CMLib.ableComponents().alterAbilityComponentFile(CMStrings.trimCRLF(copyS.getEncodedLine()), true);
			for(final Social newS : socials)
			{
				if((!socials.contains(newS))||(!socEncV.contains(newS.getEncodedLine())))
					CMLib.ableComponents().alterAbilityComponentFile(CMStrings.trimCRLF(newS.getEncodedLine()), false);
			}
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The complication of skill usage just increased!"));
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
		if(CMLib.ableComponents().getAbilityComponentMap().get(A.ID().toUpperCase())==null)
		{
			mob.tell(L("A component definition for '@x1' doesn't exists, you'll need to create it first.",A.ID()));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
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

	public void plane(final MOB mob, final List<String> commands)
	throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rFormat: MODIFY PLANE [SKILL ID]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		String planeName=CMParms.combine(commands,2);
		final PlanarAbility planeSet = (PlanarAbility)CMClass.getAbility("StdPlanarAbility");
		final Map<String,String> planeVars = planeSet.getPlanarVars(planeName);
		if(planeVars == null)
		{
			mob.tell(L("'@x1' already exists, you'll need to destroy it first.",planeName));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		planeName = planeVars.get(PlanarVar.ID.name());
		final Map<String,String> alterParms = new XHashtable<String,String>(planeVars);
		final String modifiedRule = CMLib.genEd().modifyPlane(mob,planeName,alterParms,-1);
		final String err = planeSet.addOrEditPlane(planeName, modifiedRule);
		if(err.startsWith("ERROR"))
		{
			mob.tell(err);
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return;
		}
		else
		if(err.length()>0)
		{
			Log.infoOut(mob.Name()+" modified plane: "+planeName+": \n\r"+err);
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The planar universe just changed!"));
		}
	}

	public void socials(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.session().safeRawPrintln(L("but fail to specify the proper fields.\n\rThe format is MODIFY SOCIAL [NAME] ([PARAM])\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		final String name=commands.get(2).toUpperCase();
		String stuff="";
		if(commands.size()>3)
			stuff=CMParms.combine(commands,3).toUpperCase().trim();
		if(stuff.startsWith("T-"))
			stuff="TNAME";
		if(stuff.equals("TNAME"))
			stuff="<T-NAME>";
		final String oldStuff=stuff;
		if(stuff.equals("NONE"))
			stuff="";
		final Social S=CMLib.socials().fetchSocial((name+" "+stuff).trim(),false);
		if(S==null)
		{
			mob.tell(L("The social '@x1' does not exist.",stuff));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		final List<Social> oldSocials = new ArrayList<Social>();
		List<Social> allSocials = CMLib.socials().getSocialsSet(name);
		if(allSocials==null)
			allSocials=new ArrayList<Social>();
		for(int a = 0; a<allSocials.size();a++)
			oldSocials.add((Social)allSocials.get(a).copyOf());
		{
			final Vector<String> socialsParse=CMParms.parse((name+" "+oldStuff).trim());
			if(socialsParse.size()==0)
			{
				mob.tell(L("Which social?"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
				return;
			}
			final String sname=socialsParse.firstElement().toUpperCase().trim();
			final String rest=socialsParse.size()>1?CMParms.combine(socialsParse,1):"";
			final List<Social> socials=CMLib.socials().getSocialsSet(socialsParse.firstElement());
			if((socials==null)||(socials.size()==0))
			{
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("The social '@x1' does not exist. Try LIST SOCIALS ",sname));
				return;
			}
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around the idea of  @x1s.",S.name()));
			final List<Social> copy=new XArrayList<Social>(socials);
			CMLib.socials().modifySocialInterface(mob,socials, sname, rest);
			{
				for(final Social copyS : copy)
					if(!socials.contains(copyS))
						CMLib.socials().delSocial(copyS.name());
				for(final Social newS : socials)
				{
					if(!socials.contains(newS))
						CMLib.socials().addSocial(newS);
				}
			}
			CMLib.socials().save(mob);
		}
		allSocials = CMLib.socials().getSocialsSet(name);
		if(allSocials==null)
			allSocials=new ArrayList<Social>();
		boolean changed = allSocials. size() != oldSocials.size();
		if(!changed)
		{
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
				if(!found)
					changed = true;
				if(changed)
					break;
			}
		}
		if(changed)
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The happiness of all mankind has just fluxuated!"));
	}

	public void players(final MOB mob, final List<String> commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY USER [PLAYER NAME] ([STAT],?) (VALUE)\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		final String mobID=commands.get(2);
		final MOB M=CMLib.players().getLoadPlayerAllHosts(mobID);
		if(M!=null)
		{
			if(M.playerStats()!=null)
				M.playerStats().setLastUpdated(M.playerStats().getLastDateTime());
			M.recoverPhyStats();
			M.recoverCharStats();
		}
		else
		{
			mob.tell(L("There is no such player as '@x1'!",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}
		mob.location().showOthers(mob,M,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>."));
		final MOB copyMOB=(MOB)M.copyOf();
		M.basePhyStats().copyInto(copyMOB.basePhyStats());
		M.phyStats().copyInto(copyMOB.phyStats());
		mob.location().delInhabitant(copyMOB); //necc because location is cleared later, before destroy is called.
		if(M.location()!=null)
			M.location().delInhabitant(copyMOB); //necc because location is cleared later, before destroy is called.
		if(commands.size()<4)
		{
			CMLib.genEd().modifyPlayer(mob,M,-1);
			if(!copyMOB.sameAs(M))
			{
				final StringBuilder changed=new StringBuilder("");
				final String[] codes = M.getStatCodes();
				for (int i = 0; i < codes.length; i++)
				{
					if (!M.getStat(codes[i]).equals(copyMOB.getStat(codes[i])))
						changed.append(codes[i]).append(" ");
				}
				Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+": "+changed.toString()+".");
			}
		}
		else
		{
			final String command=commands.get(3).toUpperCase();
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
				mob.tell(L("All of @x1's skill proficiencies set to @x2",M.Name(),""+prof));
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
					mob.tell(L("...but failed to specify an valid ability name.  Try one of: @x1",CMParms.toCMObjectListString(M.abilities())));
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
					return;
				}
				A.setProficiency(prof);
				final Ability eA=M.fetchEffect(A.ID());
				if((eA!=null)&&(eA.isNowAnAutoEffect()))
					eA.setProficiency(prof);
				mob.tell(L("@x1's skill proficiency in @x2 set to @x3",M.Name(),A.ID(),""+prof));
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
				mob.tell(L("...but failed to specify an aspect.  Try one of: @x1",CMParms.toListString(set)));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			}
			CMLib.database().DBUpdatePlayer(M);
			if(CMLib.flags().isInTheGame(M,true))
				CMLib.database().DBUpdateFollowers(M);
		}
		copyMOB.setSession(null); // prevents logoffs.
		copyMOB.setLocation(null);
		copyMOB.destroy();
	}

	public boolean achievements(final MOB mob, final List<String> commands)
	{
		final boolean accountSys = CMProps.isUsingAccountSystem();
		if(commands.size()<((accountSys)?4:3))
		{
			if(accountSys)
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ACHIEVEMENT (PLAYER/ACCOUNT/ALL) [TATTOO]\n\r"));
			else
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY ACHIEVEMENT [TATTOO]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}

		final String agentStr=accountSys?commands.get(2).toString():"PLAYER";
		final AccountStats.Agent agent=(AccountStats.Agent)CMath.s_valueOf(AccountStats.Agent.class, agentStr.toUpperCase().trim());
		if(agent == null)
		{
			mob.tell(L("'@x1' is an unknown achievement type.  Try @x2!",agentStr,CMParms.toListString(AccountStats.Agent.values())));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		final String tattoo=CMParms.combine(commands,((accountSys)?3:2));
		final Achievement A=CMLib.achievements().getAchievement(tattoo);
		if(A==null)
		{
			mob.tell(L("An achievement with the TATTOO/ID '@x1' does not exist!",tattoo));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(CMLib.achievements().addModifyAchievement(mob, agent, A.getTattoo(), A))
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The vanity of the world just changed!"));
		return true;
	}

	public boolean helps(final MOB mob, final List<String> commands)
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY HELP [KEY]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		final boolean preferAHelp = commands.get(1).toUpperCase().startsWith("A");
		final String helpStr=CMParms.combine(commands,2).toUpperCase().trim().replace(' ','_');
		final List<Properties> ps;
		if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AHELP))
			ps = new XVector<Properties>(CMLib.help().getHelpFile());
		else
		if(preferAHelp)
			ps = new XVector<Properties>(CMLib.help().getArcHelpFile(), CMLib.help().getHelpFile());
		else
			ps = new XVector<Properties>(CMLib.help().getHelpFile(), CMLib.help().getArcHelpFile());
		Pair<String, String> match=null;
		for(final Properties p : ps)
		{
			match = CMLib.help().getHelpMatch(helpStr,p,mob, 0);
			if((match!=null)&&(match.second!=null))
				break;
		}
		if((match==null)||(match.second==null))
		{
			mob.tell(L("An help entry with key '@x1' does not exist!",helpStr));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		final HelpSection section = preferAHelp?HelpSection.ArchonFirst:HelpSection.NormalFirst;
		final String fileName = CMLib.help().findHelpFile(match.first, section, true);
		if(fileName == null)
		{
			mob.tell(L("A help file with key '@x1' does not exist!",match.first));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		if(!CMLib.help().addModifyHelpEntry(mob, fileName, match.first, false))
		{
			mob.tell(L("A help file with key '@x1' could not be edited!",match.first));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
			return false;
		}
		//mob.location().showHappens(CMMsg.MSG_OK_ACTION,L("The vanity of the world just changed!"));
		return true;
	}

	public void manufacturer(final MOB mob, final List<String> commands) throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY MANUFACTURER [NAME]\n\r"));
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

		CMLib.genEd().modifyManufacturer(mob, manufacturer, -1);
		CMLib.tech().updateManufacturer(manufacturer);
		mob.location().recoverRoomStats();
		Log.sysOut(mob.Name()+" modified manufacturer "+manufacturer.name()+".");
	}

	public void mobs(final MOB mob, final List<String> commands)
		throws IOException
	{

		if(commands.size()<4)
		{
			mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY MOB [MOB NAME] [LEVEL, ABILITY, REJUV, MISC, ?] [NUMBER, TEXT]\n\r"));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		final String mobID=(commands.get(2));
		final String command=commands.get(3).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);

		final MOB modMOB=mob.location().fetchInhabitant(mobID);
		if(modMOB==null)
		{
			mob.tell(L("I don't see '@x1 here.\n\r",mobID));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a powerful spell."));
			return;
		}

		if(!modMOB.isMonster())
		{
			mob.tell(L("@x1 is a player! Try MODIFY USER!",modMOB.Name()));
			return;
		}
		final int oldLevel = modMOB.basePhyStats().level();
		final MOB copyMOB=(MOB)modMOB.copyOf();
		mob.location().showOthers(mob,modMOB,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>."));
		if(command.equals("LEVEL"))
		{
			final int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modMOB.basePhyStats().setLevel(newLevel);
				modMOB.recoverCharStats();
				modMOB.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shakes under the transforming power.",modMOB.name()));
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			final int newAbility=CMath.s_int(restStr);
			modMOB.basePhyStats().setAbility(newAbility);
			modMOB.recoverPhyStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shakes under the transforming power.",modMOB.name()));
		}
		else
		if(command.equals("REJUV"))
		{
			final int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modMOB.basePhyStats().setRejuv(newRejuv);
				modMOB.recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shakes under the transforming power.",modMOB.name()));
			}
			else
			{
				modMOB.basePhyStats().setRejuv(PhyStats.NO_REJUV);
				modMOB.recoverPhyStats();
				mob.tell(L("@x1 will now never rejuvenate.",modMOB.name()));
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shakes under the transforming power.",modMOB.name()));
			}
		}
		else
		if(command.equals("MISC"))
		{
			if(modMOB.isGeneric())
				CMLib.genEd().genMiscSet(mob,modMOB,-1);
			else
				modMOB.setMiscText(restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shakes under the transforming power.",modMOB.name()));
		}
		else
		if(CMLib.coffeeMaker().isAnyGenStat(modMOB, command))
		{
			CMLib.coffeeMaker().setAnyGenStat(modMOB,command, restStr);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("@x1 shake(s) under the transforming power.",modMOB.name()));
		}
		else
		{
			final STreeSet<String> set=new STreeSet<String>();
			set.addAll(CMParms.parseCommas("LEVEL,ABILITY,REJUV,MISC",true));
			set.addAll(CMLib.coffeeMaker().getAllGenStats(modMOB));
			mob.tell(L("...but failed to specify an aspect.  Try one of: @x1",CMParms.toListString(set)));
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell.."));
		}
		if((!mob.isGeneric())
		&&(modMOB.basePhyStats().level() != oldLevel))
		{
			final int oldRejuv = modMOB.basePhyStats().rejuv();
			CMLib.leveler().fillOutMOB(modMOB, modMOB.basePhyStats().level());
			modMOB.basePhyStats().setRejuv(oldRejuv);
			modMOB.recoverCharStats();
			modMOB.recoverPhyStats();
			modMOB.recoverMaxState();
			modMOB.resetToMaxState();
		}
		if(!modMOB.sameAs(copyMOB))
			Log.sysOut("Mobs",mob.Name()+" modified mob "+modMOB.Name()+".");
		copyMOB.destroy();
	}

	public boolean errorOut(final MOB mob)
	{
		mob.tell(L("You are not allowed to do that here."));
		return false;
	}

	protected String listOfThings()
	{
		return L("ITEM, RACE, CLASS, ABILITY, LANGUAGE, CRAFTSKILL, GATHERSKILL, WRIGHTSKILL, "
			+ "ALLQUALIFY, AREA, EXIT, COMPONENT, RECIPE, EXPERTISE, QUEST, COMMAND, "
			+ "MOB, USER, HOLIDAY, ACHIEVEMENT, MANUFACTURER, HELP/AHELP, TRAP, CRON, "
			+ "GOVERNMENT, JSCRIPT, FACTION, SOCIAL, CLAN, POLL, NEWS, DAY, MONTH, YEAR, "
			+ "TIME, HOUR, UPDATE:, or ROOM");
	}

	@Override
	public boolean execute(final MOB mob, List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
			commandType=commands.get(1).toUpperCase();
		if(commands.size()==1)
		{
			mob.tell(L("\n\rModify what? Try an @x1.",listOfThings()));
		}
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS))
				return errorOut(mob);
			items(mob,commands);
		}
		else
		if(commandType.equals("MANUFACTURER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDITEMS))
				return errorOut(mob);
			manufacturer(mob,commands);
		}
		else
		if(commandType.equals("CRON"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCRON))
				return errorOut(mob);
			cron(mob,commands);
		}
		else
		if(commandType.equals("RECIPE"))
		{
			//mob.tell(L("Not yet implemented")); if(true) return true;
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRECIPES))
				return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell(L("Modify which recipe?  Name a common skill ID -- use list abilities to find one."));
				return false;
			}
			final String name=CMParms.combine(commands,2);
			final Ability A=CMClass.findAbility(name,Ability.ACODE_COMMON_SKILL,-1,false);
			if(A==null)
			{
				mob.tell(L("'@x1' is not a valid skill id.",name));
				return false;
			}
			if(!(A instanceof RecipeDriven))
			{
				mob.tell(L("'@x1' is not a valid recipe skill.",A.ID()));
				return false;
			}
			final RecipeDriven iA = (RecipeDriven)A;
			if((iA.getRecipeFormat()==null)
			||(iA.getRecipeFormat().length()==0)
			||(iA.getRecipeFilename()==null)
			||(iA.getRecipeFilename().length()==0))
			{
				mob.tell(L("'@x1' does not have modifiable recipes.",A.ID()));
				return false;
			}
			CMLib.ableParms().modifyRecipesList(mob,iA.getRecipeFilename(),iA.getRecipeFormat());
		}
		else
		if(commandType.equals("ROOM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDROOMS))
				return errorOut(mob);
			rooms(mob,commands);
		}
		else
		if((commandType.equals("ACCOUNT"))&&(CMProps.isUsingAccountSystem()))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
				return errorOut(mob);
			accounts(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDRACES))
				return errorOut(mob);
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLASSES))
				return errorOut(mob);
			classes(mob,commands);
		}
		else
		if(commandType.equals("ABILITY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			abilities(mob,commands);
		}
		else
		if(commandType.equals("TRAP"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			traps(mob,commands);
		}
		else
		if(commandType.equals("COMMAND"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCOMMANDS))
				return errorOut(mob);
			commands(mob,commands);
		}
		else
		if(commandType.equals("LANGUAGE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			languages(mob,commands);
		}
		else
		if(commandType.equals("CRAFTSKILL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			craftSkills(mob,commands);
		}
		else
		if(commandType.equals("WRIGHTSKILL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			wrightSkills(mob,commands);
		}
		else
		if(commandType.equals("GATHERSKILL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			gatherSkills(mob,commands);
		}
		else
		if(commandType.equals("ALLQUALIFY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDABILITIES))
				return errorOut(mob);
			allQualify(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDAREAS))
				return errorOut(mob);
			if((commands.size()>2)
			&&(commands.get(2).equalsIgnoreCase("UPDATE:"))
			&&(CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.CMDAREAS)))
			{
				commands.remove(1);
				commands.remove(0);
				doMql(mob, true, commands);
			}
			else
				areas(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS))
				return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("COMPONENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.COMPONENTS))
				return errorOut(mob);
			components(mob,commands);
			return false;
		}
		else
		if(commandType.equals("PLANE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.PLANES))
				return errorOut(mob);
			plane(mob,commands);
			return false;
		}
		else
		if(commandType.equals("EXPERTISE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.EXPERTISES))
				return errorOut(mob);
			mob.tell(L("You can't modify components, you can only LIST, CREATE, and DESTROY them."));
			return false;
		}
		else
		if(commandType.equals("HELP")||commandType.equals("AHELP"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDHELP))
				return errorOut(mob);
			helps(mob, commands);
			return false;
		}
		else
		if(commandType.equals("TITLE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TITLES))
				return errorOut(mob);
			mob.tell(L("You can't modify titles, you can only LIST, CREATE, and DESTROY them."));
			return false;
		}
		else
		if(commandType.equals("AWARD"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.AUTOAWARDS))
				return errorOut(mob);
			autoawards(mob, commands);
			return false;
		}
		else
		if(commandType.equals("ACHIEVEMENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.ACHIEVEMENTS))
				return errorOut(mob);
			achievements(mob,commands);
			return false;
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDSOCIALS))
				return errorOut(mob);
			socials(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDMOBS))
				return errorOut(mob);
			mobs(mob,commands);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("The space-time continuum shake(s) under the transforming power."));
		}
		else
		if(commandType.equals("DAY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK))
				return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY DAY [INT]\n\r"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell."));
				return false;
			}
			mob.location().getArea().getTimeObj().setDayOfMonth(CMath.s_int(commands.get(2)));
			mob.location().getArea().getTimeObj().save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("The space-time continuum shake(s) under the transforming power."));
		}
		else
		if(commandType.equals("MONTH"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK))
				return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY MONTH [INT]\n\r"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell."));
				return false;
			}
			mob.location().getArea().getTimeObj().setMonth(CMath.s_int(commands.get(2)));
			mob.location().getArea().getTimeObj().save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("The space-time continuum shake(s) under the transforming power."));
		}
		else
		if(commandType.equals("YEAR"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK))
				return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY YEAR [INT]\n\r"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell."));
				return false;
			}
			mob.location().getArea().getTimeObj().setYear(CMath.s_int(commands.get(2)));
			mob.location().getArea().getTimeObj().save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("The space-time continuum shake(s) under the transforming power."));
		}
		else
		if((commandType.equals("TIME"))||(commandType.equals("HOUR")))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.TICKTOCK))
				return errorOut(mob);
			if(commands.size()<3)
			{
				mob.tell(L("You have failed to specify the proper fields.\n\rThe format is MODIFY TIME [INT]\n\r"));
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> flub(s) a spell."));
				return false;
			}
			final TimeClock C=mob.location().getArea().getTimeObj();
			final TimeClock.TimeOfDay oldTOD=C.getTODCode();
			C.setHourOfDay(CMath.s_int(commands.get(2)));
			if(oldTOD!=C.getTODCode())
				C.handleTimeChange();
			C.save();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("The space-time continuum shake(s) under the transforming power."));
		}
		else
		if(commandType.startsWith("JSCRIPT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JSCRIPTS))
				return errorOut(mob);
			if(CMProps.getIntVar(CMProps.Int.JSCRIPTS)!=CMSecurity.JSCRIPT_REQ_APPROVAL)
			{
				mob.tell(L("This command is only used when your Scriptable Javascripts require approval as specified in your coffeemud.ini file."));
				return true;
			}
			Object O=null;
			final Map<Long,String> j=CMSecurity.getApprovedJScriptTable();
			boolean somethingFound=false;
			final List<Long> todoKeys = new XArrayList<Long>(j.keySet());
			for(final Long L : todoKeys)
			{
				O=j.get(L);
				if(!CMLib.players().playerExists(O.toString()))
				{
					somethingFound=true;
					mob.tell(L("Unapproved script:\n\r@x1\n\r",O.toString()));
					if((!mob.isMonster())
					&&(mob.session().confirm(L("Approve this script (Y/n)?"),"Y")))
						CMSecurity.approveJScript(mob.Name(),L.longValue());
					else
						j.remove(L);
				}
			}
			if(!somethingFound)
				mob.tell(L("No Javascripts require approval at this time."));
		}
		else
		if(commandType.equals("USER")||commandType.equals("PLAYER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
				return errorOut(mob);
			players(mob,commands);
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
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms around the idea of @x1.^?",P.getSubject()));
			CMLib.polls().modifyVote(P, mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^SThe world's uncertainty has changed.^?"));
			Log.sysOut("CreateEdit",mob.Name()+" modified Poll "+P.getName()+".");
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
			CMLib.quests().modifyHoliday(mob,num);
			Log.sysOut("CreateEdit",mob.Name()+" modified Holiday "+name+".");
		}
		else
		if(commandType.equals("NEWS"))
		{
			if((!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.JOURNALS))
			&&(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.NEWS)))
				return errorOut(mob);

			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^S<S-NAME> wave(s) <S-HIS-HER> arms...^?"));
			final Item I=CMClass.getItem("StdJournal");
			I.setName(L("SYSTEM_NEWS"));
			I.setDescription(L("Enter `LIST NEWS [NUMBER]` to read an entry.%0D%0AEnter CREATE NEWS to add new entries. "));
			final CMMsg newMsg=CMClass.getMsg(mob,I,null,
											CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,L("<S-NAME> modif(ys) the news."),
											CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,CMParms.combine(commands,2),
											CMMsg.MSG_WRITE|CMMsg.MASK_ALWAYS,null);
			if(mob.location().okMessage(mob,newMsg)&&I.okMessage(mob, newMsg))
			{
				mob.location().send(mob,newMsg);
				I.executeMsg(mob,newMsg);
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("^SThe world is now more informed!^?"));
			}
		}
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDQUESTS))
				return errorOut(mob);
			quests(mob,commands);
		}
		else
		if(commandType.equals("SQL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDDATABASE))
				return errorOut(mob);
			try
			{
				final String sql=CMParms.combine(commands,2);
				if(sql.equals("ping"))
				{
					final int num=CMLib.database().pingAllConnections(1000);
					mob.tell(L("Pings completed=@x1.",""+num));
				}
				else
				if(sql.toUpperCase().trim().startsWith("SELECT"))
				{
					mob.tell(L("SQL Query: @x1",sql));
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
					mob.tell(L("Command completed."));
				}
				else
				{
					mob.tell(L("SQL Statement: @x1",sql));
					final int resp=CMLib.database().DBRawExecute(sql.replace('`','\''));
					mob.tell(L("Command completed. Response code: @x1",""+resp));
				}
			}
			catch(final Exception e)
			{
				mob.tell(L("SQL Error: @x1",e.getMessage()));
			}
		}
		else
		if(commandType.equals("GOVERNMENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS))
				return errorOut(mob);
			if(commands.size()<3)
				mob.tell(L("Modify which government?  Use list governments."));
			else
			{
				final String name=CMParms.combine(commands,2);
				ClanGovernment G = null;
				for(final ClanGovernment g : CMLib.clans().getStockGovernments())
				{
					if(g.getName().equalsIgnoreCase(name))
						G=g;
				}
				if(G==null)
				{
					for(final ClanGovernment g : CMLib.clans().getStockGovernments())
					{
						if(g.getName().toLowerCase().startsWith(name.toLowerCase()))
							G=g;
					}
				}
				if(G==null)
					mob.tell(L("Government '@x1' is unknown.  Try list governments.",name));
				else
				if(!mob.isMonster())
				{
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around @x1.",G.getName()));
					CMLib.genEd().modifyGovernment(mob, G,-1);
					CMLib.clans().reSaveGovernmentsXML();
					Log.sysOut("CreateEdit",mob.Name()+" modified Clan Government "+G.getName()+".");
				}
			}
		}
		else
		if(commandType.equals("FACTION"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDFACTIONS))
				return errorOut(mob);
			if(commands.size()<3)
				mob.tell(L("Modify which faction?  Use list factions."));
			else
			{
				final String name=CMParms.combine(commands,2);
				Faction F=CMLib.factions().getFaction(name);
				if(F==null)
					F=CMLib.factions().getFactionByName(name);
				if(F==null)
					mob.tell(L("Faction '@x1' is unknown.  Try list factions.",name));
				else
				if(!mob.isMonster())
				{
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around the idea of  @x1.",F.name()));
					CMLib.factions().modifyFaction(mob,F);
					Log.sysOut("CreateEdit",mob.Name()+" modified Faction "+F.name()+" ("+F.factionID()+").");
				}
			}
		}
		else
		if(commandType.equals("UPDATE:"))
		{
			if((!CMSecurity.isASysOp(mob))&&(!CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.CMDAREAS)))
				return errorOut(mob);
			commands.remove(0);
			doMql(mob, false, commands);
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDCLANS))
				return errorOut(mob);
			if(commands.size()<3)
				mob.tell(L("Modify which clan?  Use clanlist."));
			else
			{
				final String name=CMParms.combine(commands,2);
				final Clan C=CMLib.clans().findClan(name);
				if(C==null)
					mob.tell(L("Clan '@x1' is unknown.  Try clanlist.",name));
				else
				if(!mob.isMonster())
				{
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around @x1.",C.name()));
					CMLib.genEd().modifyClan(mob,C,-1);
					Log.sysOut("CreateEdit",mob.Name()+" modified Clan "+C.name()+".");
				}
			}
		}
		else
		{
			String allWord=CMParms.combine(commands,1);
			Environmental thang=null;
			MOB srchMob=mob;
			Item srchContainer=null;
			Room srchRoom=mob.location();
			if(allWord.length()>0)
			{
				final int x=allWord.indexOf('@');
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
			}
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
				mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>."));
				if(!thang.isGeneric())
				{
					CMLib.genEd().modifyStdItem(mob,(Item)thang,-1);
				}
				else
					CMLib.genEd().genMiscSet(mob,thang,-1);
				((Item)thang).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 shake(s) under the transforming power.",thang.name()));
				if(!copyItem.sameAs(thang))
					Log.sysOut("CreateEdit",mob.Name()+" modified item "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getDescriptiveExtendedRoomID(mob.location())+".");
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
					if(thang instanceof Deity)
						CMLib.map().registerWorldObjectLoaded(((Deity) thang).location().getArea(), ((Deity) thang).location(), thang);
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>."));
					CMLib.genEd().modifyStdMob(mob,(MOB)thang,-1);
					if(!copyMOB.sameAs(thang))
						Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getDescriptiveExtendedRoomID(((MOB)thang).location())+".");
					copyMOB.destroy();
					if(thang instanceof Deity)
						CMLib.map().registerWorldObjectLoaded(((Deity) thang).location().getArea(), ((Deity) thang).location(), thang);
				}
				else
				if(!((MOB)thang).isMonster())
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS))
						return errorOut(mob);
					players(mob,CMParms.parse("MODIFY USER \""+thang.Name()+"\""));
				}
				else
				{
					final MOB copyMOB=(MOB)thang.copyOf();
					if(thang instanceof Deity)
						CMLib.map().registerWorldObjectLoaded(((Deity) thang).location().getArea(), ((Deity) thang).location(), thang);
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>."));
					CMLib.genEd().genMiscSet(mob,thang,-1);
					if(!copyMOB.sameAs(thang))
						Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getDescriptiveExtendedRoomID(((MOB)thang).location())+".");
					copyMOB.destroy();
					if(thang instanceof Deity)
						CMLib.map().registerWorldObjectLoaded(((Deity) thang).location().getArea(), ((Deity) thang).location(), thang);
				}
				((MOB)thang).recoverPhyStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("@x1 shake(s) under the transforming power.",thang.name()));
			}
			else
			if((CMLib.directions().getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(CMLib.directions().getGoodDirectionCode(allWord)>=0)
					thang=mob.location().getRawExit(CMLib.directions().getGoodDirectionCode(allWord));

				if(thang!=null)
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDEXITS))
						return errorOut(mob);
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,L("<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>."));
					final Exit copyExit=(Exit)thang.copyOf();
					int showFlag=-1;
					if(CMProps.getIntVar(CMProps.Int.EDITORTYPE)>0)
						showFlag=-999;
					CMLib.genEd().genMiscText(mob,thang,0,showFlag);
					updateChangedExit(mob, mob.location(), (Exit)thang, copyExit);
				}
				else
				{
					commands.add(1,"EXIT");
					execute(mob,commands,metaFlags);
				}
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
				commands.add("MODIFY");
				if(thang instanceof Area)
					commands.add("AREA");
				else
				if(thang instanceof Item)
					commands.add("ITEM");
				commands.add(allWord);
				execute(mob,commands,metaFlags);
			}
			else
			if((thang=CMLib.map().findArea(allWord))!=null)
			{
				commands=new Vector<String>();
				commands.add("MODIFY");
				commands.add("AREA");
				commands.add(allWord);
				execute(mob,commands,metaFlags);
			}
			else
			if((thang=CMLib.space().findSpaceObject(allWord,false))!=null)
			{
				if(thang instanceof Area)
					commands.add(1,"AREA");
				else
				if(thang instanceof Item)
					commands.add(1,"ITEM");
				execute(mob,commands,metaFlags);
			}
			else
			if((thang=CMLib.players().getLoadPlayer(allWord))!=null)
			{
				commands.add(1,"USER");
				execute(mob,commands,metaFlags);
			}
			else
			if(allWord.indexOf('@')>=0)
			{
				commands.add(1,"ITEM");
				execute(mob,commands,metaFlags);
			}
			else
				mob.tell(L("\n\rYou cannot modify a '@x1'. However, you might try an @x2.",allWord,listOfThings()));
		}
		return false;
	}

	@Override
	public Object executeInternal(final MOB mob, final int metaFlags, final Object... args) throws java.io.IOException
	{
		if(!super.checkArguments(internalParameters, args))
			return Boolean.FALSE;
		final Object O = args[0];
		if(O instanceof Environmental)
		{
			CMLib.genEd().genMiscSet(mob,(Environmental)O,-1);
			if(O instanceof Physical)
				((Physical)O).recoverPhyStats();
			((Environmental)O).text();
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowedContainsAny(mob, mob.location(), CMSecurity.SECURITY_CMD_GROUP);
	}

}
