package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Destroy extends BaseItemParser
{
	public Destroy(){}

	private String[] access={"DESTROY"};
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

		String mobID=Util.combine(commands,2);
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
			deadMOB.destroy();
			mob.location().showHappens(CMMsg.MSG_OK_VISUAL,deadMOB.name()+" vanishes in a puff of smoke.");
			Log.sysOut("Mobs",mob.Name()+" destroyed mob "+deadMOB.Name()+".");
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


	public static boolean players(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY USER [USER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		MOB deadMOB=CMClass.getMOB("StdMOB");
		boolean found=CMClass.DBEngine().DBUserSearch(deadMOB,Util.combine(commands,2));

		if(!found)
		{
			mob.tell("The user '"+Util.combine(commands,2)+"' does not exist!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		if(mob.session().confirm("This will complete OBLITERATE the user '"+deadMOB.Name()+"' forever.  Are you SURE?! (y/N)?","N"))
		{
			CoffeeUtensils.obliteratePlayer(deadMOB,false);
			mob.tell("The user '"+Util.combine(commands,2)+"' is no more!\n\r");
			Log.sysOut("Mobs",mob.Name()+" destroyed user "+deadMOB.Name()+".");
			return true;
		}
		return true;
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
		String roomdir=Util.combine(commands,2);
		int direction=Directions.getGoodDirectionCode(roomdir);
		Room deadRoom=null;
		if(!thecmd.equalsIgnoreCase("UNLINK"))
			deadRoom=CMMap.getRoom(roomdir);
		if((deadRoom==null)&&(direction<0))
		{
			if(thecmd.equalsIgnoreCase("UNLINK"))
				mob.tell("You have failed to specify a direction.  Try ("+Directions.DIRECTIONS_DESC+").\n\r");
			else
				mob.tell("You have failed to specify a direction.  Try a VALID ROOM ID, or ("+Directions.DIRECTIONS_DESC+").\n\r");
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
			if(!CMSecurity.isAllowed(mob,deadRoom,"CMDROOMS"))
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
				if(!mob.session().confirm("You are fixing permanantly destroy Room \""+deadRoom.roomID()+"\".  Are you ABSOLUTELY SURE (y/N)","N")) return;
			CoffeeUtensils.obliterateRoom(deadRoom);
			mob.tell("The sound of massive destruction rings in your ears.");
			mob.location().showOthers(mob,null,CMMsg.MSG_NOISE,"The sound of massive destruction rings in your ears.");
			Log.sysOut("Rooms",mob.Name()+" destroyed room "+deadRoom.roomID()+".");
		}
		else
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS"))
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
				Vector outer=GL.outerExits();
				int myX=GL.getChildX(mob.location());
				int myY=GL.getChildY(mob.location());
				for(int v=0;v<outer.size();v++)
				{
					CMMap.CrossExit CE=(CMMap.CrossExit)outer.elementAt(v);
					if((CE.out)
					&&(CE.x==myX)
					&&(CE.y==myY)
					&&(CE.dir==direction))
					   GL.delOuterExit(CE);
				}
				CMClass.DBEngine().DBUpdateExits(GL);
				mob.location().rawDoors()[direction]=null;
				mob.location().rawExits()[direction]=null;
			}
			else
			{
				mob.location().rawDoors()[direction]=null;
				mob.location().rawExits()[direction]=null;
				CMClass.DBEngine().DBUpdateExits(mob.location());
			}
			if(unRoom instanceof GridLocale)
			{
				GridLocale GL=(GridLocale)unRoom;
				Vector outer=GL.outerExits();
				for(int v=0;v<outer.size();v++)
				{
					CMMap.CrossExit CE=(CMMap.CrossExit)outer.elementAt(v);
					if((!CE.out)
					&&(CE.dir==direction)
					&&(CE.destRoomID.equalsIgnoreCase(CMMap.getExtendedRoomID(mob.location()))))
					   GL.delOuterExit(CE);
				}
				CMClass.DBEngine().DBUpdateExits(GL);
			}
			mob.location().getArea().fillInAreaRoom(mob.location());
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A wall of inhibition falls "+Directions.getInDirectionName(direction)+".");
			Log.sysOut("Rooms",mob.Name()+" unlinked direction "+Directions.getDirectionName(direction)+" from room "+mob.location().roomID()+".");
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
			mob.tell("You have failed to specify a direction.  Try "+Directions.DIRECTIONS_DESC+".\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(mob.isMonster())
		{
			mob.tell("Sorry Charlie!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;

		}
		mob.location().rawExits()[direction]=null;
		CMClass.DBEngine().DBUpdateExits(mob.location());
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A wall of inhibition falls "+Directions.getInDirectionName(direction)+".");
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
		
		String itemID=Util.combine(commands,2);
		MOB srchMob=mob;
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
					mob.tell("MOB '"+rest+"' not found.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return false;
				}
				srchMob=M;
				srchRoom=null;
			}
		}
		
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL.")){ allFlag=true; itemID="ALL "+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(".ALL")){ allFlag=true; itemID="ALL "+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Item deadItem=null;
		if(deadItem==null) deadItem=(srchRoom==null)?null:srchRoom.fetchItem(null,itemID);
		if((!allFlag)&&(deadItem==null)) deadItem=(srchMob==null)?null:srchMob.fetchInventory(null,itemID);
		while(deadItem!=null)
		{
			deadItem.destroy();
			mob.location().recoverRoomStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,deadItem.name()+" disintegrates!");
			doneSomething=true;
			Log.sysOut("Items",mob.Name()+" destroyed item "+deadItem.name()+".");
			deadItem=null;
			if(!allFlag) deadItem=(srchMob==null)?null:srchMob.fetchInventory(null,itemID);
			if(deadItem==null) deadItem=(srchRoom==null)?null:srchRoom.fetchItem(null,itemID);
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

		String areaName=Util.combine(commands,2);
		if(CMMap.getArea(areaName)==null)
		{
			mob.tell("There is no such area as '"+areaName+"'");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a thunderous spell.");
			return;
		}
		Area A=CMMap.getArea(areaName);
		Room R=A.getRandomProperRoom();
		if((R!=null)&&(!CMSecurity.isAllowed(mob,R,"CMDAREAS")))
		{
			errorOut(mob);
			return;
		}
			
		if(!confirmed);
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
		CoffeeUtensils.obliterateArea(areaName);
		if(confirmed)
		{
			mob.location().showHappens(CMMsg.MSG_OK_ACTION,"A thunderous boom of destruction is heard in the distance.");
			Log.sysOut("Rooms",mob.Name()+" destroyed area "+areaName+".");
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

		String raceID=Util.combine(commands,2);
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
		try
		{
			for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
			{
				Room room=(Room)e.nextElement();
				for(int i=0;i<room.numInhabitants();i++)
				{
					MOB M=room.fetchInhabitant(i);
					if(M.baseCharStats().getMyRace()==R)
					{
						mob.tell("A MOB called '"+M.Name()+" in "+room.roomID()+" is this race, and must first be deleted.");
						mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
						return false;
					}
				}
			}
	    }catch(NoSuchElementException e){}
		CMClass.delRace(R);
		CMClass.DBEngine().DBDeleteRace(R.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The diversity of the world just decreased!");
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

		String classID=Util.combine(commands,2);
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
		CMClass.DBEngine().DBDeleteClass(C.ID());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The employment of the world just decreased!");
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
			String therest=Util.combine(commands,3);
			if(!((therest.equalsIgnoreCase("<T-NAME>")
                    ||therest.equalsIgnoreCase("SELF")
                    ||therest.equalsIgnoreCase("ALL"))))
			{
				mob.session().rawPrintln("but fail to specify the proper second parameter.\n\rThe format is DESTROY SOCIAL [NAME] ([<T-NAME>], [SELF])\n\r");
				mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
				return;
			}
		}

		Social soc2=Socials.FetchSocial(Util.combine(commands,2).toUpperCase(),true);
		if(soc2==null)
		{
			mob.tell("but fail to specify an EXISTING SOCIAL!\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(mob.session().confirm("Are you sure you want to delete that social (y/N)? ","N"))
		{
			Socials.remove(soc2.name());
			Resources.removeResource("SOCIALS LIST");
			Socials.save();
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
		switch(material&EnvResource.MATERIAL_MASK)
		{
		case EnvResource.MATERIAL_LIQUID:
			msgstr="<S-NAME> pour(s) out <T-NAME>.";
			break;
		case EnvResource.MATERIAL_PAPER:
			msgstr="<S-NAME> tear(s) up <T-NAME>.";
			break;
		case EnvResource.MATERIAL_GLASS:
			msgstr="<S-NAME> smash(es) <T-NAME>.";
			break;
		default:
			return false;
		}
		FullMsg msg=new FullMsg(mob,dropThis,null,CMMsg.MSG_NOISYMOVEMENT,(optimize?CMMsg.MASK_OPTIMIZE:0)|CMMsg.MASK_GENERAL|CMMsg.MSG_DEATH,CMMsg.MSG_NOISYMOVEMENT,msgstr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			return true;
		}
		else
		if(dropThis instanceof Coins)
		    ((Coins)dropThis).putCoinsBack();
		return false;
	}

	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if((!CMSecurity.isAllowedStartsWith(mob,"CMD"))
		&&(!CMSecurity.isAllowedStartsWith(mob,mob.location(),"KILL"))
		&&(!CMSecurity.isAllowed(mob,mob.location(),"BAN"))
		&&(!CMSecurity.isAllowed(mob,mob.location(),"NOPURGE")))
		{
			commands.removeElementAt(0);
			if(commands.size()==0)
			{
				mob.tell("Destroy what?");
				return false;
			}
			if(mob.location().fetchInhabitant(Util.combine(commands,0))!=null)
			{
				Command C=CMClass.getCommand("Kill");
				commands.insertElementAt("KILL",0);
				if(C!=null) C.execute(mob,commands);
				return false;
			}

			Vector V=new Vector();
			int maxToDrop=Integer.MAX_VALUE;
			
			if((commands.size()>1)
			&&(Util.s_int((String)commands.firstElement())>0))
			{
				maxToDrop=Util.s_int((String)commands.firstElement());
				commands.setElementAt("all",0);
			}

			String whatToDrop=Util.combine(commands,0);
			boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
			if(whatToDrop.toUpperCase().startsWith("ALL.")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(4);}
			if(whatToDrop.toUpperCase().endsWith(".ALL")){ allFlag=true; whatToDrop="ALL "+whatToDrop.substring(0,whatToDrop.length()-4);}
			int addendum=1;
			String addendumStr="";
			do
			{
				Item dropThis=mob.fetchCarried(null,whatToDrop+addendumStr);
				if((dropThis==null)
				&&(V.size()==0)
				&&(addendumStr.length()==0)
				&&(!allFlag))
				{
					dropThis=mob.fetchWornItem(whatToDrop);
					if(dropThis!=null)
					{
						int matType=dropThis.material()&EnvResource.MATERIAL_MASK;
						if((matType!=EnvResource.MATERIAL_GLASS)
						&&(matType!=EnvResource.MATERIAL_LIQUID)
						&&(matType!=EnvResource.MATERIAL_PAPER))
						{
							mob.tell(dropThis.Name()+" can not be easily destroyed.");
							return false;
						}
						else	
						if((!dropThis.amWearingAt(Item.HELD))&&(!dropThis.amWearingAt(Item.WIELD)))
						{
							mob.tell("You must remove that first.");
							return false;
						}
						else
						{
							FullMsg newMsg=new FullMsg(mob,dropThis,null,CMMsg.MSG_REMOVE,null);
							if(mob.location().okMessage(mob,newMsg))
								mob.location().send(mob,newMsg);
							else
								return false;
						}
					}
				}
				if(dropThis==null) break;
				if((Sense.canBeSeenBy(dropThis,mob))
				&&(!V.contains(dropThis)))
					V.addElement(dropThis);
				addendumStr="."+(++addendum);
			}
			while((allFlag)&&(addendum<=maxToDrop));

			boolean didAnything=false;
			for(int i=0;i<V.size();i++)
			{
				if(destroyItem(mob,(Item)V.elementAt(i),false,true))
					didAnything=true;
				else
				if(V.elementAt(i) instanceof Coins)
					((Coins)V.elementAt(i)).putCoinsBack();
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
        for(int i=0;i<ChannelSet.getNumCommandJournals();i++)
        {
            if((ChannelSet.getCommandJournalName(i).equals(commandType))
            &&(CMSecurity.isAllowed(mob,mob.location(),ChannelSet.getCommandJournalName(i))
                ||CMSecurity.isAllowed(mob,mob.location(),"KILL"+ChannelSet.getCommandJournalName(i)+"S")))
            {
                String nam=ChannelSet.getCommandJournalName(i);
                int which=-1;
                if(commands.size()>2)
                    which=Util.s_int((String)commands.elementAt(2));
                if(which<=0)
                    mob.tell("Please enter a valid "+nam.toLowerCase()+" number to delete.  Use LIST "+nam+"S for more information.");
                else
                {
                    CMClass.DBEngine().DBDeleteJournal("SYSTEM_"+nam+"S",which-1);
                    mob.tell(nam.toLowerCase()+" deletion submitted.");
                    
                }
                return true;
            }
        }
		if(commandType.equals("EXIT"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDEXITS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDITEMS")) return errorOut(mob);
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
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDRACES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLASSES")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			classes(mob,commands);
		}
		else
		if(commandType.equals("USER"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDPLAYERS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			players(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDSOCIALS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
		}
		else
		if(commandType.equals("NOPURGE"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"NOPURGE")) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=Util.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid player number to delete.  Use List nopurge for more information.");
			else
			{
				StringBuffer newNoPurge=new StringBuffer("");
				Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("protectedplayers.ini",false));
				if((protectedOnes!=null)&&(protectedOnes.size()>0))
					for(int b=0;b<protectedOnes.size();b++)
					{
						String B=(String)protectedOnes.elementAt(b);
						if(((b+1)!=which)&&(B.trim().length()>0))
							newNoPurge.append(B+"\n");
					}
				Resources.updateResource("protectedplayers.ini",newNoPurge);
				Resources.saveFileResource("protectedplayers.ini");
				mob.tell("Ok.");
			}
		}
		else
		if(commandType.equals("BAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"BAN")) return errorOut(mob);
			int which=-1;
			if(commands.size()>2)
				which=Util.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid ban number to delete.  Use List Banned for more information.");
			else
			{
                CMSecurity.unban(which);
				mob.tell("Ok.");
			}
		}
        else
        if(commandType.equals("JOURNAL"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"JOURNALS")) return errorOut(mob);
            if(commands.size()<3)
            {
                mob.tell("Destroy which journal? Try List Journal");
                return errorOut(mob);
            }
            Vector V=CMClass.DBEngine().DBReadJournal(null);
            String name=Util.combine(commands,2);
            int which=-1;
            for(int v=0;v<V.size();v++)
                if(((String)V.elementAt(v)).equalsIgnoreCase(name))
                {
                    name=(String)V.elementAt(v);
                    which=v;
                    break;
                }
            if(which<0)
            for(int v=0;v<V.size();v++)
                if(((String)V.elementAt(v)).startsWith(name))
                {
                    name=(String)V.elementAt(v);
                    which=v;
                    break;
                }
            if(which<0)
                mob.tell("Please enter a valid journal name to delete.  Use List Journals for more information.");
            else
            if(mob.session().confirm("This will destroy all "+CMClass.DBEngine().DBCountJournal(name,null,null)+" messages.  Are you SURE (y/N)? ","N"))
            {
                CMClass.DBEngine().DBDeleteJournal(name,Integer.MAX_VALUE);
                mob.tell("It is done.");
            }
        }
        else
        if(commandType.equals("FACTION"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"CMDFACTIONS")) return errorOut(mob);
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
            if(commands.size()<3)
                mob.tell("Destroy which faction?  Use list factions.");
            else
            {
                String name=Util.combine(commands,2);
                Faction F=Factions.getFaction(name);
                if(F==null) F=Factions.getFactionByName(name);
                if(F==null)
                    mob.tell("Faction '"+name+"' is unknown.  Try list factions.");
                else
                if((!mob.isMonster())&&(mob.session().confirm("Destroy file '"+F.ID+"' -- this could have unexpected consequences in the future -- (N/y)? ","N")))
                {
                    try
                    {
                        java.io.File F2=new java.io.File("resources"+java.io.File.separatorChar+F.ID);
                        if(F2.exists()) F2.delete();
                        Log.sysOut("CreateEdit",mob.Name()+" destroyed Faction "+F.name+" ("+F.ID+").");
                        mob.tell("Faction File '"+F.ID+"' deleted.");
                        Resources.removeResource(F.ID);
                    }
                    catch(Exception e)
                    {
                        Log.errOut("CreateEdit",e);
                        mob.tell("Faction File '"+F.ID+"' could NOT be deleted.");
                    }
                }
            }
        }
        else
		if(commandType.equals("MOB"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDMOBS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
        else
        if(commandType.equals("POLL"))
        {
            if(!CMSecurity.isAllowed(mob,mob.location(),"POLLS")) return errorOut(mob);
            String name=Util.combine(commands,2);
            Polls P=null;
            if(Util.isInteger(name))
                P=Polls.getPoll(Util.s_int(name)-1);
            else
            if(name.length()>0)
                P=Polls.getPoll(name);
            if(P==null)
            {
                mob.tell("POLL '"+name+"' not found. Try LIST POLLS.");
                mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
                return false;
            }
            mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
            if((mob.session()!=null)&&(mob.session().confirm("Destroy POLL "+P.getName()+", are you SURE? (Y/n)? ","Y")))
            {
                P.dbdelete();
                mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^SThe world has grown a bit more certain.^?");
                Log.sysOut("CreateEdit",mob.Name()+" modified Poll "+P.getName()+".");
            }
            else
                mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
        }
		else
		if(commandType.equals("QUEST"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDQUESTS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which quest?  Use list quests.");
			else
			{
				String name=Util.combine(commands,2);
                if(Util.isInteger(name))
                {
                    Quest Q=Quests.fetchQuest(Util.s_int(name)-1);
                    if(Q!=null) name=Q.name();
                }
				Quest Q=Quests.fetchQuest(name);
				if(Q==null)
					mob.tell("Quest '"+name+"' is unknown.  Try list quests.");
				else
				{
                    if(Q.running()&&(!Q.stopping())) Q.stopQuest();
					mob.tell("Quest '"+Q.name()+"' is destroyed!");
					Quests.delQuest(Q);
				}
			}
		}
		else
		if(commandType.equals("CLAN"))
		{
			if(!CMSecurity.isAllowed(mob,mob.location(),"CMDCLANS")) return errorOut(mob);
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which clan?  Use clanlist.");
			else
			{
				String name=Util.combine(commands,2);
				Clan C=Clans.findClan(name);
				if(C==null)
					mob.tell("Clan '"+name+"' is unknown.  Try clanlist.");
				else
				{
					mob.tell("Clan '"+C.name()+"' is destroyed!");
					C.destroyClan();
					Log.sysOut("CreateEdit","Clan '"+C.name()+" destroyed by "+mob.name()+".");
				}
			}
		}
		else
		{
			String allWord=Util.combine(commands,1);
			Environmental thang=mob.location().fetchFromRoomFavorItems(null,allWord,Item.WORN_REQ_ANY);
			if(thang==null)
			    thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Item.WORN_REQ_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				commands.insertElementAt("ITEM",1);
				execute(mob,commands);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.insertElementAt("MOB",1);
				else
					commands.insertElementAt("USER",1);
				execute(mob,commands);
			}
			else
			{
				Room theRoom=null;
				if(allWord.length()>0)
				{
				    try
				    {
						for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
						{
							Room room=(Room)r.nextElement();
							if(room.roomID().equalsIgnoreCase(allWord))
							{
								theRoom=room;
								break;
							}
						}
				    }catch(NoSuchElementException e){}
				}
				if(theRoom!=null)
				{
					commands=new Vector();
					commands.addElement("DESTROY");
					commands.addElement("ROOM");
					commands.addElement(theRoom.roomID());
					execute(mob,commands);
				}
				else
				{
					if(Directions.getGoodDirectionCode(allWord)>=0)
					{
						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("ROOM");
						commands.addElement(allWord);
						execute(mob,commands);

						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("EXIT");
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
					if(Socials.FetchSocial(allWord,true)!=null)
					{
						commands.insertElementAt("SOCIAL",1);
						execute(mob,commands);
					}
					else
					mob.tell(
						"\n\rYou cannot destroy a '"+commandType+"'. "
						+"However, you might try an "
						+"EXIT, ITEM, USER, MOB, QUEST, FACTION, JOURNAL, SOCIAL, CLAN, BAN, NOPURGE, BUG, TYPO, IDEA, POLL, or a ROOM.");
				}
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
