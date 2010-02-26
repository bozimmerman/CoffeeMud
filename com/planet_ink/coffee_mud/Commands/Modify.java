package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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


import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Modify extends StdCommand
{
	public Modify(){}

	private String[] access={"MODIFY","MOD"};
	public String[] getAccessWords(){return access;}

	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ITEM [ITEM NAME](@ room/[MOB NAME]) [LEVEL, ABILITY, REJUV, USES, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=((String)commands.elementAt(2));
		MOB srchMob=mob;
		Item srchContainer=null;
		Room srchRoom=mob.location();
		int x=itemID.indexOf("@");
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
					Item I = srchRoom.fetchItem(null, rest);
					if(I instanceof Container)
						srchContainer=(Container)I;
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
			modItem=srchMob.fetchInventory(itemID);
		else
		if(srchRoom!=null)
		{
			modItem=srchRoom.fetchItem(srchContainer, itemID);
			if(modItem==null)
				modItem=srchRoom.fetchAnyItem(itemID);
		}
		if(modItem==null)
		{
			mob.tell("I don't see '"+itemID+" here.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		mob.location().showOthers(mob,modItem,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");

		Item copyItem=(Item)modItem.copyOf();
		if(command.equals("LEVEL"))
		{
			int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modItem.baseEnvStats().setLevel(newLevel);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=CMath.s_int(restStr);
			modItem.baseEnvStats().setAbility(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("HEIGHT"))
		{
			int newAbility=CMath.s_int(restStr);
			modItem.baseEnvStats().setHeight(newAbility);
			modItem.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modItem.baseEnvStats().setRejuv(newRejuv);
				modItem.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
			else
			{
				modItem.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modItem.recoverEnvStats();
				mob.tell(modItem.name()+" will now never rejuvinate.");
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
			}
		}
		else
		if(command.equals("USES"))
		{
			int newUses=CMath.s_int(restStr);
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
		if((command.length()==0)&&(modItem.isGeneric()))
		{
			CMLib.genEd().genMiscSet(mob,modItem);
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modItem.name()+" shake(s) under the transforming power.");
		}
		else
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, HEIGHT, REJUV, USES, or MISC.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
		}
		if(!copyItem.sameAs(modItem))
			Log.sysOut("Items",mob.Name()+" modified item "+modItem.ID()+".");
		copyItem.destroy();
	}

    protected void flunkCmd1(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY ROOM [NAME, AREA, DESCRIPTION, AFFECTS, BEHAVIORS, CLASS, XGRID, YGRID] [TEXT]\n\r");
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
	}

    protected void flunkCmd2(MOB mob)
	{
		mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY AREA [NAME, DESCRIPTION, CLIMATE, FILE, AFFECTS, BEHAVIORS, ADDSUB, DELSUB, XGRID, YGRID] [TEXT]\n\r");
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
			Room oldRoom=(Room)mob.location().copyOf();
            CMLib.genEd().modifyRoom(mob,mob.location());
			if((!oldRoom.sameAs(mob.location()))&&(!mob.location().amDestroyed()))
			{
				CMLib.database().DBUpdateRoom(mob.location());
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
				Log.sysOut("Rooms",mob.Name()+" modified room "+mob.location().roomID()+".");
			}
			oldRoom.destroy();
			return;
		}
		if(commands.size()<3) { flunkCmd1(mob); return;}

		String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr="";
		if(commands.size()>=3)
			restStr=CMParms.combine(commands,3);

		if(command.equalsIgnoreCase("AREA"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
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
								mob.session().println(CMLib.lister().reallyList(CMClass.areaTypes(),-1,null).toString());
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
				String oldID=R.roomID();
	    		synchronized(("SYNC"+R.roomID()).intern())
	    		{
	    			R=CMLib.map().getRoom(R);
					Room reference=CMLib.map().findConnectingRoom(R);
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
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDisplayText(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			Room newRoom=CMClass.getLocale(restStr);
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
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setXGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if((command.equalsIgnoreCase("YGRID"))&&(mob.location() instanceof GridLocale))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			((GridLocale)mob.location()).setYGridSize(CMath.s_int(restStr));
			((GridLocale)mob.location()).buildGrid();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"There is something different about this place...\n\r");
		}
		else
		if(command.equalsIgnoreCase("DESCRIPTION"))
		{
			if(commands.size()<4) { flunkCmd1(mob); return;}
			mob.location().setDescription(restStr);
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("AFFECTS"))
		{
            CMLib.genEd().genAffects(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		if(command.equalsIgnoreCase("BEHAVIORS"))
		{
            CMLib.genEd().genBehaviors(mob,mob.location(),1,1);
			mob.location().recoverEnvStats();
			CMLib.database().DBUpdateRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The very nature of reality changes.\n\r");
		}
		else
		{
			flunkCmd1(mob);
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
			oldName=theAccount.accountName();
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
			String accountName=CMStrings.capitalizeAndLower(CMParms.combine(commands, 2));
		    theAccount = CMLib.players().getLoadAccount(accountName);
		    if(theAccount==null)
		    {
				mob.tell("There is no account called '"+accountName+"'!\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
		    }
			oldName=theAccount.accountName();
	        CMLib.genEd().modifyAccount(mob,theAccount);
			mob.location().recoverRoomStats();
		}
		if(theAccount != null)
		{
			Log.sysOut("Modify",mob.Name()+" modified account "+theAccount.accountName()+".");
	        if(!oldName.equals(theAccount.accountName()))
	        {
            	Vector<MOB> V=new Vector<MOB>();
	        	for(Enumeration<String> es=theAccount.getPlayers();es.hasMoreElements();)
	        	{
	        		String playerName=es.nextElement();
	        		MOB playerM=CMLib.players().getLoadPlayer(playerName);
	        		if((playerM!=null)&&(!CMLib.flags().isInTheGame(playerM,true)))
	        			V.addElement(playerM);
	        	}
	        	PlayerAccount acc = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
	        	acc.setAccountName(oldName);
	        	CMLib.database().DBDeleteAccount(acc);
	        	CMLib.database().DBCreateAccount(theAccount);
	        	for(MOB playerM : V)
        			CMLib.database().DBUpdatePlayerPlayerStats(playerM);
	        }
        	CMLib.database().DBUpdateAccount(theAccount);
		}
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location()==null) return;
		if(mob.location().getArea()==null) return;
		Area myArea=mob.location().getArea();

		String oldName=myArea.Name();
		Vector allMyDamnRooms=new Vector();
		for(Enumeration e=myArea.getCompleteMap();e.hasMoreElements();)
			allMyDamnRooms.addElement(e.nextElement());

		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around wildly.");
		Resources.removeResource("HELP_"+myArea.Name().toUpperCase());
		if(commands.size()==2)
            CMLib.genEd().modifyGenArea(mob,myArea);
		else
		{
			if(commands.size()<3) { flunkCmd1(mob); return;}

			String command=((String)commands.elementAt(2)).toUpperCase();
			String restStr="";
			if(commands.size()>=3)
				restStr=CMParms.combine(commands,3);

			if(command.equalsIgnoreCase("NAME"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setName(restStr);
			}
			else
			if(command.equalsIgnoreCase("DESC"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setDescription(restStr);
			}
			else
			if(command.equalsIgnoreCase("FILE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				myArea.setArchivePath(restStr);
			}
			else
			if((command.equalsIgnoreCase("XGRID"))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				((GridZones)myArea).setXGridSize(CMath.s_int(restStr));
			}
			else
			if((command.equalsIgnoreCase("YGRID"))&&(myArea instanceof GridZones))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				((GridZones)myArea).setYGridSize(CMath.s_int(restStr));
			}
			if(command.equalsIgnoreCase("CLIMATE"))
			{
				if(commands.size()<4) { flunkCmd2(mob); return;}
				int newClimate=0;
				for(int i=0;i<restStr.length();i++)
					switch(Character.toUpperCase(restStr.charAt(i)))
					{
					case 'R':
						newClimate=newClimate|Area.CLIMASK_WET;
						break;
					case 'H':
						newClimate=newClimate|Area.CLIMASK_HOT;
						break;
					case 'C':
						newClimate=newClimate|Area.CLIMASK_COLD;
						break;
					case 'W':
						newClimate=newClimate|Area.CLIMATE_WINDY;
						break;
					case 'D':
						newClimate=newClimate|Area.CLIMATE_WINDY;
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
				myArea.recoverEnvStats();
			}
			else
			if(command.equalsIgnoreCase("BEHAVIORS"))
			{
                CMLib.genEd().genBehaviors(mob,myArea,1,1);
				myArea.recoverEnvStats();
			}
			else
			{
				flunkCmd2(mob);
				return;
			}
		}

		if((!myArea.Name().equals(oldName))&&(!mob.isMonster()))
		{
			if(mob.session().confirm("Is changing the name of this area really necessary (y/N)?","N"))
			{
				for(Enumeration r=myArea.getCompleteMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
		    		synchronized(("SYNC"+R.roomID()).intern())
		    		{
		    			R=CMLib.map().getRoom(R);
						if((R.roomID().startsWith(oldName+"#"))
						&&(CMLib.map().getRoom(myArea.Name()+"#"+R.roomID().substring(oldName.length()+1))==null))
						{
			    			R=CMLib.map().getRoom(R);
							String oldID=R.roomID();
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
		myArea.recoverEnvStats();
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
            String[] CMDS={"START","STOP","ENABLE","DISABLE"};
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
                if(CMProps.getIntVar(CMProps.SYSTEMI_EDITORTYPE)>0)
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
                        String oldScript=Q.script();
                        newScript=CMLib.genEd().prompt(mob,oldScript,++showNumber,showFlag,"Script",false,false,CMLib.help().getHelpText("QUESTS",mob,true).toString(),null,null);
                        if(!newScript.equals(oldScript))
                        {
                            Q.setScript(newScript);
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
                                Quest Q1=CMLib.quests().fetchQuest(q);
                                if(Q1.name().equalsIgnoreCase(Q.name())&&(Q1!=Q))
                                {
                                    mob.tell("A quest with that name already exists.");
                                    revert=true;
                                }
                            }
                            if(revert)
                                Q.setScript(oldScript);
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
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC()+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		
		Exit thisExit=mob.location().getRawExit(direction);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit '"+((String)commands.elementAt(2))+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around to the "+Directions.getInDirectionName(direction)+".");

		if(thisExit.isGeneric())
		{
            CMLib.genEd().modifyGenExit(mob,thisExit);
			return;
		}
		
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		//String command=((String)commands.elementAt(2)).toUpperCase();
		String restStr=CMParms.combine(commands,3);

		if(thisExit.isGeneric())
            CMLib.genEd().modifyGenExit(mob,thisExit);
		else
		if(restStr.length()>0)
			thisExit.setMiscText(restStr);
		else
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY EXIT [DIRECTION] ([NEW MISC TEXT])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room room=(Room)r.nextElement();
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					Exit exit=room.getRawExit(d);
					if((exit!=null)&&(exit==thisExit))
					{
						CMLib.database().DBUpdateExits(room);
						room.getArea().fillInAreaRoom(room);
						break;
					}
				}
			}
	    }catch(NoSuchElementException e){}
		
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,thisExit.name()+" shake(s) under the transforming power.");
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.Name()+".");
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

	public boolean classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY CLASS [CLASS ID]\n\r");
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
			mob.tell("'"+A.ID()+"' is not generic, and may not be modified.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around all "+A.name()+"s.");
        CMLib.genEd().modifyGenAbility(mob,A);
		CMLib.database().DBDeleteAbility(A.ID());
		CMLib.database().DBCreateAbility(A.ID(),A.getStat("ALLXML"));
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
        Ability A=CMClass.getAbility(skillID);
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
        String parms=CMLib.ableMapper().getAbilityComponentCodedString(skillID);
        String error=CMLib.ableMapper().addAbilityComponent(parms,CMLib.ableMapper().getAbilityComponentMap());
        if(error!=null)
        {
            mob.tell(error);
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
            return;
        }
        CMFile F=new CMFile(Resources.makeFileResourceName("skills/components.txt"),null,true);
        StringBuffer text=F.textUnformatted();
        boolean lastWasCR=true;
        int delFromHere=-1;
        String upID=skillID.toUpperCase();
        for(int t=0;t<text.length();t++)
        {
            if(text.charAt(t)=='\n')
                lastWasCR=true;
            else
            if(text.charAt(t)=='\r')
                lastWasCR=true;
            else
            if(Character.isWhitespace(text.charAt(t)))
                continue;
            else
            if((lastWasCR)&&(delFromHere>=0))
            {
                text.delete(delFromHere,t);
                text.insert(delFromHere,parms+'\n');
                delFromHere=-1;
                break;
            }
            else
            if((lastWasCR)&&(Character.toUpperCase(text.charAt(t))==upID.charAt(0)))
            {
                if((text.substring(t).toUpperCase().startsWith(upID))
                &&(text.substring(t+upID.length()).trim().startsWith("=")))
                    delFromHere=t;
                lastWasCR=false;
            }
            else
                lastWasCR=false;
        }
        if(delFromHere>0)
        {
            text.delete(delFromHere,text.length());
            text.append(parms+'\n');
        }
        F.saveText(text.toString(),false);
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
        String name=((String)commands.elementAt(2)).toUpperCase();
        String stuff="";
        if(commands.size()>3)
            stuff=CMParms.combine(commands,3).toUpperCase().trim();
        if(stuff.startsWith("<")||stuff.startsWith(">")||(stuff.startsWith("T-")))
            stuff="TNAME";
        if(stuff.equals("TNAME")) 
            stuff="<T-NAME>";
        String oldStuff=stuff;
        if(stuff.equals("NONE")) 
            stuff="";
        Social S=CMLib.socials().fetchSocial((name+" "+stuff).trim(),false);
        if(S==null)
        {
            mob.tell("The social '"+stuff+"' does not exist.");
            mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
            return;
        }
        Vector oldSocials = new Vector();
        Vector allSocials = CMLib.socials().getSocialsSet(name);
        for(int a = 0; a<allSocials.size();a++)
        	oldSocials.add(((Social)allSocials.elementAt(a)).copyOf());
		mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around the idea of  "+S.name()+"s.");
		CMLib.socials().modifySocialInterface(mob,(name+" "+oldStuff).trim());
        allSocials = CMLib.socials().getSocialsSet(name);
        boolean changed = allSocials.size() != oldSocials.size();
        if(!changed)
        for(int a=0;a<oldSocials.size();a++)
        {
        	Social oldSocial = (Social)oldSocials.elementAt(a);
        	boolean found = false;
        	for(int a2=0;a2<allSocials.size();a2++)
        	{
        		Social newSocial = (Social)allSocials.elementAt(a2);
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
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY USER [PLAYER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=CMParms.combine(commands,2);
		MOB M=CMLib.players().getLoadPlayer(mobID);
		if(M!=null)
		{
			CMLib.database().DBReadFollowers(M,false);
			if(M.playerStats()!=null)
				M.playerStats().setLastUpdated(M.playerStats().lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
		else
		{
			mob.tell("There is no such player as '"+mobID+"'!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		mob.location().showOthers(mob,M,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
		MOB copyMOB=(MOB)M.copyOf();
        CMLib.genEd().modifyPlayer(mob,M);
		if(!copyMOB.sameAs(M))
			Log.sysOut("Mobs",mob.Name()+" modified player "+M.Name()+".");
		copyMOB.setSession(null); // prevents logoffs.
		copyMOB.setLocation(null);
		copyMOB.destroy();
	}
	
	public void mobs(MOB mob, Vector commands)
		throws IOException
	{

		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is MODIFY MOB [MOB NAME] [LEVEL, ABILITY, REJUV, MISC] [NUMBER, TEXT]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=((String)commands.elementAt(2));
		String command=((String)commands.elementAt(3)).toUpperCase();
		String restStr="";
		if(commands.size()>4)
			restStr=CMParms.combine(commands,4);


		MOB modMOB=mob.location().fetchInhabitant(mobID);
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
		MOB copyMOB=(MOB)modMOB.copyOf();
		mob.location().showOthers(mob,modMOB,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
		if(command.equals("LEVEL"))
		{
			int newLevel=CMath.s_int(restStr);
			if(newLevel>=0)
			{
				modMOB.baseEnvStats().setLevel(newLevel);
				modMOB.recoverCharStats();
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
		}
		else
		if(command.equals("ABILITY"))
		{
			int newAbility=CMath.s_int(restStr);
			modMOB.baseEnvStats().setAbility(newAbility);
			modMOB.recoverEnvStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
		}
		else
		if(command.equals("REJUV"))
		{
			int newRejuv=CMath.s_int(restStr);
			if(newRejuv>0)
			{
				modMOB.baseEnvStats().setRejuv(newRejuv);
				modMOB.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,modMOB.name()+" shakes under the transforming power.");
			}
			else
			{
				modMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
				modMOB.recoverEnvStats();
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
		{
			mob.tell("...but failed to specify an aspect.  Try LEVEL, ABILITY, REJUV, or MISC.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
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
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String commandType="";
		if(commands.size()>1)
        {
            if(commands.elementAt(1) instanceof Environmental)
            {
                CMLib.genEd().genMiscSet(mob,(Environmental)commands.elementAt(1));
                ((Environmental)commands.elementAt(1)).recoverEnvStats();
                ((Environmental)commands.elementAt(1)).text();
                return true;
            }
			commandType=((String)commands.elementAt(1)).toUpperCase();
        }
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
			items(mob,commands);
		}
		else
        if(commandType.equals("RECIPE"))
        {
        	//mob.tell("Not yet implemented"); if(true) return true;
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRECIPES")) return errorOut(mob);
            if(commands.size()<3)
            {
                mob.tell("Modify which recipe?  Name a common skill ID -- use list abilities to find one.");
                return false;
            }
            String name=CMParms.combine(commands,2);
            Ability A=CMClass.findAbility(name,Ability.ACODE_COMMON_SKILL,-1,false);
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
            ItemCraftor iA = (ItemCraftor)A;
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
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDROOMS")) return errorOut(mob);
			rooms(mob,commands);
		}
        else
		if((commandType.equals("ACCOUNT"))&&(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			accounts(mob,commands);
		}
		else
		if(commandType.equals("RACE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")) return errorOut(mob);
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLASSES")) return errorOut(mob);
			classes(mob,commands);
		}
		else
		if(commandType.equals("ABILITY"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDABILITIES")) return errorOut(mob);
			abilities(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDAREAS")) return errorOut(mob);
			areas(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			exits(mob,commands);
		}
		else
		if(commandType.equals("COMPONENT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"COMPONENTS")) return errorOut(mob);
            components(mob,commands);
			return false;
		}
        else
        if(commandType.equals("EXPERTISE"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"EXPERTISES")) return errorOut(mob);
            mob.tell("You can't modify components, you can only LIST, CREATE, and DESTROY them.");
            return false;
        }
        else
        if(commandType.equals("TITLE"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"TITLES")) return errorOut(mob);
            mob.tell("You can't modify titles, you can only LIST, CREATE, and DESTROY them.");
            return false;
        }
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			socials(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mobs(mob,commands);
		}
		else
        if(commandType.startsWith("JSCRIPT"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"JSCRIPTS")) 
                return errorOut(mob);
            if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)!=1)
            {
                mob.tell("This command is only used when your Scriptable Javascripts require approval as specified in your coffeemud.ini file.");
                return true;
            }
            Long L=null;
            Object O=null;
            Hashtable j=CMSecurity.getApprovedJScriptTable();
            boolean somethingFound=false;
            for(Enumeration e=j.keys();e.hasMoreElements();)
            {
                L=(Long)e.nextElement();
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
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			players(mob,commands);
		}
		else
        if(commandType.equals("POLL"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"POLLS")) return errorOut(mob);
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
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms around the idea of "+P.getSubject()+".^?");
            CMLib.polls().modifyVote(P, mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world's uncertainty has changed.^?");
            Log.sysOut("CreateEdit",mob.Name()+" modified Poll "+P.getName()+".");
        }
        else
        if(commandType.equals("HOLIDAY"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
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
            CMLib.quests().modifyHoliday(mob,num);
            Log.sysOut("CreateEdit",mob.Name()+" modified Holiday "+name+".");
        }
        else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
            quests(mob,commands);
		}
        else
        if(commandType.equals("FACTION"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDFACTIONS")) return errorOut(mob);
            if(commands.size()<3)
                mob.tell("Modify which faction?  Use list factions.");
            else
            {
                String name=CMParms.combine(commands,2);
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
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLANS")) return errorOut(mob);
			if(commands.size()<3)
				mob.tell("Modify which clan?  Use clanlist.");
			else
			{
				String name=CMParms.combine(commands,2);
				Clan C=CMLib.clans().findClan(name);
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
			int x=allWord.indexOf("@");
			MOB srchMob=mob;
			Item srchContainer=null;
			Room srchRoom=mob.location();
			if(x>0)
			{
				String rest=allWord.substring(x+1).trim();
				allWord=allWord.substring(0,x).trim();
				if(rest.equalsIgnoreCase("room"))
					srchMob=null;
				else
				if(rest.length()>0)
				{
					MOB M=srchRoom.fetchInhabitant(rest);
					if(M==null)
					{
						Item I = srchRoom.fetchItem(null, rest);
						if(I instanceof Container)
							srchContainer=(Container)I;
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
				thang=srchMob.fetchInventory(allWord);
			else
			if(srchRoom!=null)
				thang=srchRoom.fetchFromRoomFavorItems(srchContainer,allWord,Wearable.FILTER_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) 
                    return errorOut(mob);
				Item copyItem=(Item)thang.copyOf();
				mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
				if(!thang.isGeneric())
				{
                    CMLib.genEd().modifyStdItem(mob,(Item)thang);
				}
				else
					CMLib.genEd().genMiscSet(mob,thang);
				thang.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
				if(!copyItem.sameAs(thang))
	                Log.sysOut("CreateEdit",mob.Name()+" modified item "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(mob.location())+".");
				copyItem.destroy();
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) 
                    return errorOut(mob);
				if((!thang.isGeneric())&&(((MOB)thang).isMonster()))
				{
					MOB copyMOB=(MOB)thang.copyOf();
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
                    CMLib.genEd().modifyStdMob(mob,(MOB)thang);
					if(!copyMOB.sameAs(thang))
	                    Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
				}
				else
				if(!((MOB)thang).isMonster())
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
					players(mob,CMParms.parse("MODIFY USER \""+thang.Name()+"\""));
				}
				else
                {
					MOB copyMOB=(MOB)thang.copyOf();
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
					CMLib.genEd().genMiscSet(mob,thang);
					if(!copyMOB.sameAs(thang))
	                    Log.sysOut("CreateEdit",mob.Name()+" modified mob "+thang.Name()+" ("+thang.ID()+") in "+CMLib.map().getExtendedRoomID(((MOB)thang).location())+".");
					copyMOB.destroy();
                }
				thang.recoverEnvStats();
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
			}
			else
			if((Directions.getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(Directions.getGoodDirectionCode(allWord)>=0)
					thang=mob.location().getRawExit(Directions.getGoodDirectionCode(allWord));

				if(thang!=null)
				{
					if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
					mob.location().showOthers(mob,thang,CMMsg.MSG_OK_ACTION,"<S-NAME> wave(s) <S-HIS-HER> hands around <T-NAMESELF>.");
					Exit copyExit=(Exit)thang.copyOf();
                    CMLib.genEd().genMiscText(mob,thang,1,1);
					thang.recoverEnvStats();
					try
					{
						for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
						{
							Room room=(Room)r.nextElement();
				    		synchronized(("SYNC"+room.roomID()).intern())
				    		{
				    			room=CMLib.map().getRoom(room);
								for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
								{
									Exit exit=room.getRawExit(d);
									if((exit!=null)&&(exit==thang))
									{
										CMLib.database().DBUpdateExits(room);
										break;
									}
								}
				    		}
						}
				    }catch(NoSuchElementException e){}
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,thang.name()+" shake(s) under the transforming power.");
					if(!copyExit.sameAs(thang))
						Log.sysOut("CreateEdit",mob.Name()+" modified exit "+thang.ID()+".");
					copyExit.destroy();
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
				mob.tell("\n\rYou cannot modify a '"+commandType+"'. However, you might try an ITEM, RACE, CLASS, ABILITY, AREA, EXIT, COMPONENT, RECIPE, EXPERTISE, TITLE, QUEST, MOB, USER, HOLIDAY, JSCRIPT, FACTION, SOCIAL, CLAN, POLL, or ROOM.");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowedStartsWith(mob,mob.location(),"CMD");}

	
}
