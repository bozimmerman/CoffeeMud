package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.IOException;

public class Destroy extends BaseItemParser
{
	public Destroy(){}

	private String[] access={"DESTROY"};
	public String[] getAccessWords(){return access;}

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
		MOB deadMOB=(MOB)mob.location().fetchInhabitant(mobID);
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
			deadMOB=(MOB)mob.location().fetchInhabitant(mobID);
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
		if(!mob.isASysOp(null))
		{
			mob.tell("Only Archons may destroy players.");
			return false;
		}
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY USER [USER NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return false;
		}

		MOB deadMOB=(MOB)CMClass.getMOB("StdMOB");
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
			if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("CONFIRMED"))
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
				mob.tell("You have failed to specify a direction.  Try (N, S, E, W, U, D, or V).\n\r");
			else
				mob.tell("You have failed to specify a direction.  Try a VALID ROOM ID, or (N, S, E, W, U, D, or V).\n\r");
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
			if(!mob.isASysOp(deadRoom))
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
			mob.location().rawDoors()[direction]=null;
			mob.location().rawExits()[direction]=null;
			CMClass.DBEngine().DBUpdateExits(mob.location());
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
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U, D, or V.\n\r");
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
			mob.tell("You have failed to specify the proper fields.\n\rThe format is DESTROY MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return false;
		}

		String itemID=Util.combine(commands,2);
		boolean allFlag=((String)commands.elementAt(2)).equalsIgnoreCase("all");
		if(itemID.toUpperCase().startsWith("ALL.")){ allFlag=true; itemID="ALL "+itemID.substring(4);}
		if(itemID.toUpperCase().endsWith(".ALL")){ allFlag=true; itemID="ALL "+itemID.substring(0,itemID.length()-4);}
		boolean doneSomething=false;
		Item deadItem=null;
		if(!allFlag) deadItem=mob.fetchInventory(null,itemID);
		if(deadItem==null) deadItem=(Item)mob.location().fetchItem(null,itemID);
		while(deadItem!=null)
		{
			deadItem.destroy();
			mob.location().recoverRoomStats();
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,deadItem.name()+" disintegrates!");
			doneSomething=true;
			Log.sysOut("Items",mob.Name()+" destroyed item "+deadItem.name()+".");
			deadItem=null;
			if(!allFlag) deadItem=mob.fetchInventory(null,itemID);
			if(deadItem==null) deadItem=(Item)mob.location().fetchItem(null,itemID);
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
			if(((String)commands.elementAt(commands.size()-1)).equalsIgnoreCase("CONFIRMED"))
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
		if((!mob.isASysOp(null))
		&&(!A.amISubOp(mob.Name())))
		{
			mob.tell("Sorry Charlie! Not your area!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
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
			else
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
		for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
		{
			Room room=(Room)e.nextElement();
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=(MOB)room.fetchInhabitant(i);
				if(M.baseCharStats().getMyRace()==R)
				{
					mob.tell("A MOB called '"+M.Name()+" in "+room.roomID()+" is this race, and must first be deleted.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return false;
				}
			}
		}
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
			if(!((therest.equalsIgnoreCase("<T-NAME>")||therest.equalsIgnoreCase("SELF"))))
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
		else
		{
			if(mob.session().confirm("Are you sure you want to delete that social (y/N)? ","N"))
			{
				Socials.remove(soc2.name());
				Resources.removeResource("SOCIALS LIST");
				Socials.save();
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The happiness of all mankind has just decreased!");
			}
			else
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The happiness of all mankind has just increased!");
			Log.sysOut("SysopSocials",mob.Name()+" destroyed social "+soc2.name()+".");
		}

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
		{
			mob.setMoney(mob.getMoney()+((Coins)dropThis).numberOfCoins());
			((Coins)dropThis).destroy();
		}
		return false;
	}

	
	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
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

			if(V.size()==0)
				mob.tell("You don't seem to be carrying that.");
			else
			for(int i=0;i<V.size();i++)
			{
				destroyItem(mob,(Item)V.elementAt(i),false,true);
				if(V.elementAt(i) instanceof Coins)
					((Coins)V.elementAt(i)).putCoinsBack();
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
		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if((commandType.equals("AREA"))&&(mob.isASysOp(null)))
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
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			races(mob,commands);
		}
		else
		if(commandType.equals("CLASS"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			classes(mob,commands);
		}
		else
		if((commandType.equals("USER"))&&(mob.isASysOp(null)))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			players(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
		}
		else
		if(commandType.equals("NOPURGE"))
		{
			int which=-1;
			if(commands.size()>2)
				which=Util.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid player number to delete.  Use List nopurge for more information.");
			else
			{
				StringBuffer newNoPurge=new StringBuffer("");
				Vector protectedOnes=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
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
			int which=-1;
			if(commands.size()>2)
				which=Util.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid ban number to delete.  Use List Banned for more information.");
			else
			{
				StringBuffer newBanned=new StringBuffer("");
				Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
				if((banned!=null)&&(banned.size()>0))
					for(int b=0;b<banned.size();b++)
					{
						String B=(String)banned.elementAt(b);
						if(((b+1)!=which)&&(B.trim().length()>0))
							newBanned.append(B+"\n");
					}
				Resources.updateResource("banned.ini",newBanned);
				Resources.saveFileResource("banned.ini");
				mob.tell("Ok.");
			}
		}
		else
		if(commandType.equals("BUG"))
		{
			int which=-1;
			if(commands.size()>2)
				which=Util.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid bug number to delete.  Use List Bugs for more information.");
			else
			{
				CMClass.DBEngine().DBDeleteJournal("SYSTEM_BUGS",which-1);
				mob.tell("Bug deletion submitted.");
			}
		}
		else
		if(commandType.equals("IDEA"))
		{
			int which=-1;
			if(commands.size()>2)
				which=Util.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid idea number to delete.  Use List ideas for more information.");
			else
			{
				CMClass.DBEngine().DBDeleteJournal("SYSTEM_IDEAS",which-1);
				mob.tell("Idea deletion submitted.");
			}
		}
		else
		if(commandType.equals("TYPO"))
		{
			int which=-1;
			if(commands.size()>2)
				which=Util.s_int((String)commands.elementAt(2));
			if(which<=0)
				mob.tell("Please enter a valid typo number to delete.  Use List typos for more information.");
			else
			{
				CMClass.DBEngine().DBDeleteJournal("SYSTEM_TYPOS",which-1);
				mob.tell("Typo deletion submitted.");
			}
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			mobs(mob,commands);
		}
		else
		if(commandType.equals("QUEST"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which quest?  Use list quests.");
			else
			{
				String name=Util.combine(commands,2);
				Quest Q=Quests.fetchQuest(name);
				if(Q==null)
					mob.tell("Quest '"+name+"' is unknown.  Try list quests.");
				else
				{
					mob.tell("Quest '"+Q.name()+"' is destroyed!");
					Quests.delQuest(Q);
				}
			}
		}
		else
		if(commandType.equals("CLAN"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Destroy which clan?  Use clanlist.");
			else
			{
				String name=Util.combine(commands,2);
				Clan C=Clans.getClan(name);
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
			Environmental thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Item.WORN_REQ_ANY);
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
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();
					if(room.roomID().equalsIgnoreCase(allWord))
					{
						theRoom=room;
						break;
					}
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
						+"EXIT, ITEM, USER, MOB, QUEST, SOCIAL, CLAN, BAN, NOPURGE, BUG, TYPO, IDEA, or a ROOM.");
				}
			}
		}
		return false;
	}
	public int ticksToExecute(){return 1;}
	public boolean canBeOrdered(){return false;}
	public boolean arcCommand(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
