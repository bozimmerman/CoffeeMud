package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.IOException;

public class Create extends BaseGenerics
{
	public Create(){}

	private String[] access={"CREATE"};
	public String[] getAccessWords(){return access;}


	public void exits(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE EXIT [DIRECTION] [EXIT TYPE]\n\r");
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

		String Locale=(String)commands.elementAt(3);
		Exit thisExit=CMClass.getExit(Locale);
		if(thisExit==null)
		{
			mob.tell("You have failed to specify a valid exit type '"+Locale+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		Exit opExit=mob.location().rawExits()[direction];
		Room opRoom=mob.location().rawDoors()[direction];

		Exit reverseExit=null;
		if(opRoom!=null)
			reverseExit=opRoom.rawExits()[Directions.getOpDirectionCode(direction)];
		if(reverseExit!=null)
		{
			if((thisExit.isGeneric())&&(reverseExit.isGeneric()))
			{
				thisExit=(Exit)reverseExit.copyOf();
				modifyGenExit(mob,thisExit);
			}
		}


		mob.location().rawExits()[direction]=thisExit;
		if(mob.location() instanceof GridLocale)
			((GridLocale)mob.location()).buildGrid();
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a portal opens up "+Directions.getInDirectionName(direction)+".\n\r");
		CMClass.DBEngine().DBUpdateExits(mob.location());
		if((reverseExit!=null)&&(opExit!=null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if(opRoom.rawExits()[revDirCode]==reverseExit)
			{
				opRoom.rawExits()[revDirCode]=(Exit)thisExit.copyOf();
				CMClass.DBEngine().DBUpdateExits(opRoom);
			}
		}
		else
		if((reverseExit==null)&&(opExit==null)&&(opRoom!=null))
		{
			int revDirCode=Directions.getOpDirectionCode(direction);
			if((opRoom.rawExits()[revDirCode]==null)&&(opRoom.rawDoors()[revDirCode]==mob.location()))
			{
				opRoom.rawExits()[revDirCode]=(Exit)thisExit.copyOf();
				CMClass.DBEngine().DBUpdateExits(opRoom);
			}
		}
		mob.location().getArea().fillInAreaRoom(mob.location());
		if(opRoom!=null) opRoom.getArea().fillInAreaRoom(opRoom);
		Log.sysOut("Exits",mob.location().roomID()+" exits changed by "+mob.Name()+".");
	}

	public void items(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ITEM [ITEM NAME](@ room/[MOB NAME])\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		String itemID=Util.combine(commands,2);
		Environmental dest=mob.location();
		int x=itemID.indexOf("@");
		if(x>0)
		{
			String rest=itemID.substring(x+1).trim();
			itemID=itemID.substring(0,x).trim();
			if((!rest.equalsIgnoreCase("room"))
			&&(rest.length()>0))
			{
				MOB M=mob.location().fetchInhabitant(rest);
				if(M==null)
				{
					mob.tell("MOB '"+rest+"' not found.");
					mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
					return;
				}
				dest=M;
			}
		}
		Item newItem=(Item)CMClass.getItem(itemID);

		if(newItem==null)
		{
			mob.tell("There's no such thing as a '"+itemID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}

		if((newItem instanceof ArchonOnly)
		&&(!mob.isASysOp(null)))
		{
			mob.tell("NO!");
			return;
		}

		if(newItem.subjectToWearAndTear())
			newItem.setUsesRemaining(100);
		if(dest instanceof Room)
			((Room)dest).addItem(newItem);
		else
		if(dest instanceof MOB)
			((MOB)dest).addInventory(newItem);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" drops from the sky.");

		if(newItem.isGeneric())
			genMiscSet(mob,newItem);
		mob.location().recoverRoomStats();
		Log.sysOut("Items",mob.Name()+" created item "+newItem.ID()+".");
	}


	public void rooms(MOB mob, Vector commands)
	{
		if(mob.location().roomID().equals(""))
		{
			mob.tell("This command is invalid from within a GridLocaleChild room.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("Sorry Charlie! Not your room!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		if(commands.size()<4)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		int direction=Directions.getGoodDirectionCode(((String)commands.elementAt(2)));
		if(direction<0)
		{
			mob.tell("You have failed to specify a direction.  Try N, S, E, W, U, D, or V.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Room thisRoom=null;
		String Locale=(String)commands.elementAt(3);
		thisRoom=(Room)CMClass.getLocale(Locale);
		if(thisRoom==null)
		{
			mob.tell("You have failed to specify a valid room type '"+Locale+"'.\n\rThe format is CREATE ROOM [DIRECTION] [NEW [ROOM TYPE] / LINK [ROOM ID]) \n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}
		else
		{
			thisRoom.setArea(mob.location().getArea());
			thisRoom.setRoomID(CMMap.getOpenRoomID(mob.location().getArea().Name()));
			thisRoom.setDisplayText(CMClass.className(thisRoom)+"-"+thisRoom.roomID());
			thisRoom.setDescription("");
			CMClass.DBEngine().DBCreateRoom(thisRoom,Locale);
			CMMap.addRoom(thisRoom);
		}

		if(thisRoom==null)
		{
			mob.tell("You have  to specify the proper fields.\n\rThe format is CREATE ROOM [DIRECTION] [ROOM TYPE]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		CMMap.createNewExit(mob.location(),thisRoom,direction);

		mob.location().recoverRoomStats();
		thisRoom.recoverRoomStats();
		mob.location().getArea().fillInAreaRoom(mob.location());
		mob.location().getArea().fillInAreaRoom(thisRoom);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly a block of earth falls from the sky.\n\r");
		Log.sysOut("Rooms",mob.Name()+" created room "+thisRoom.roomID()+".");
	}

	public void mobs(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE MOB [MOB NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		String mobID=((String)commands.elementAt(2));
		MOB newMOB=(MOB)CMClass.getMOB(mobID);

		if(newMOB==null)
		{
			mob.tell("There's no such thing as a '"+mobID+"'.\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		if(newMOB.Name().length()==0)
			newMOB.setName("A Standard MOB");
		newMOB.setStartRoom(mob.location());
		newMOB.setLocation(mob.location());
		newMOB.envStats().setRejuv(5000);
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(mob.location(),true);
		mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,"Suddenly, "+newMOB.name()+" instantiates from the Java plain.");
		if(newMOB.isGeneric())
			genMiscSet(mob,newMOB);
		Log.sysOut("Mobs",mob.Name()+" created mob "+newMOB.Name()+".");
	}

	public void races(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE RACE [RACE ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String raceID=Util.combine(commands,2);
		Race R=CMClass.getRace(raceID);
		if((R!=null)&&(R.isGeneric()))
		{
			mob.tell("A generic race with the ID '"+R.ID()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(raceID.indexOf(" ")>=0)
		{
			mob.tell("'"+raceID+"' is an invalid race id, because it contains a space.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		Race GR=CMClass.getRace("GenRace").copyOf();
		GR.setRacialParms("<RACE><ID>"+Util.capitalize(raceID)+"</ID><NAME>"+Util.capitalize(raceID)+"</NAME></RACE>");
		CMClass.addRace(GR);
		modifyGenRace(mob,GR);
		CMClass.DBEngine().DBCreateRace(GR.ID(),GR.racialParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The diversity of the world just increased!");
	}

	public void areas(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE AREA [AREA NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String areaName=Util.combine(commands,2);
		Area A=CMMap.getArea(areaName);
		if(A!=null)
		{
			mob.tell("An area with the name '"+A.name()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String areaType="";
		int tries=0;
		while((areaType.length()==0)&&((++tries)<10))
		{
			areaType=mob.session().prompt("Enter an area type to create (default=StdArea): ","StdArea");
			if(CMClass.getAreaType(areaType)==null)
			{
				mob.session().println("Invalid area type! Valid ones are:");
				mob.session().println(CMLister.reallyList(CMClass.areaTypes(),-1,null).toString());
				areaType="";
			}
		}
		if(areaType.length()==0) areaType="StdArea";
		A=CMClass.DBEngine().DBCreateArea(areaName,areaType);
		A.setName(areaName);
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The size of the world just increased!");
	}

	public void classes(MOB mob, Vector commands)
		throws IOException
	{
		if(commands.size()<3)
		{
			mob.tell("You have failed to specify the proper fields.\n\rThe format is CREATE CLASS [CLASS ID]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		String classD=Util.combine(commands,2);
		CharClass C=CMClass.getCharClass(classD);
		if((C!=null)&&(C.isGeneric()))
		{
			mob.tell("A generic class with the ID '"+C.ID()+"' already exists!");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		if(classD.indexOf(" ")>=0)
		{
			mob.tell("'"+classD+"' is an invalid class id, because it contains a space.");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a spell..");
			return;
		}
		CharClass CR=CMClass.getCharClass("GenCharClass").copyOf();
		CR.setClassParms("<CCLASS><ID>"+Util.capitalize(classD)+"</ID><NAME>"+Util.capitalize(classD)+"</NAME></CCLASS>");
		CMClass.addCharClass(CR);
		modifyGenClass(mob,CR);
		CMClass.DBEngine().DBCreateClass(CR.ID(),CR.classParms());
		mob.location().showHappens(CMMsg.MSG_OK_ACTION,"The employment of the world just increased!");
	}

	public void socials(MOB mob, Vector commands)
		throws IOException
	{
		if(mob.isMonster())
			return;

		if(commands.size()<3)
		{
			mob.tell("but fail to specify the proper fields.\n\rThe format is CREATE SOCIAL [NAME]\n\r");
			mob.location().showOthers(mob,null,CMMsg.MSG_OK_ACTION,"<S-NAME> flub(s) a powerful spell.");
			return;
		}

		Social soc2=new Social();
		soc2.setName(((String)commands.elementAt(2)).toUpperCase());
		if(Socials.modifySocialInterface(mob,soc2))
		{
			soc2.setName(soc2.name().toUpperCase());
			if(Socials.FetchSocial(soc2.name(),true)!=null)
			{
				mob.tell("That social already exists.  Try MODIFY!");
				return;
			}
			else
			{
				Socials.put(soc2.name(),soc2);
				Resources.removeResource("SOCIALS LIST");
				Socials.save();
			}
			Log.sysOut("SysopSocials",mob.Name()+" created social "+soc2.name()+".");
		}
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(!mob.isASysOp(mob.location()))
		{
			mob.tell("You are not powerful enough to do that.");
			return false;
		}
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			exits(mob,commands);
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
		if(commandType.equals("AREA"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			areas(mob,commands);
		}
		else
		if(commandType.equals("CLAN"))
		{
			mob.tell("To create a new Clan, use the ClanCreate command.");
			return false;
		}
		else
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			items(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			rooms(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			socials(mob,commands);
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
				mob.tell("You must specify a valid quest string.  Try AHELP QUESTS.");
			else
			{
				String script=Util.combine(commands,2);
				Quest Q=new Quests();
				Q.setScript(script);
				if((Q.name().length()==0)||(Q.duration()<0))
					mob.tell("You must specify a VALID quest string.  This one contained errors.  Try AHELP QUESTS.");
				else
				if(Quests.fetchQuest(Q.name())!=null)
					mob.tell("That quest is already loaded.  Try list quests.");
				else
				{
					mob.tell("Quest added.");
					Quests.addQuest(Q);
				}
			}
		}
		else
		{
			String allWord=Util.combine(commands,1);
			String lastWord=null;
			if(commands.size()>2)
				lastWord=(String)commands.elementAt(commands.size()-1);
			Environmental E=null;
			E=CMClass.getItem(allWord);
			if((E!=null)&&(E instanceof Item))
			{
				commands.insertElementAt("ITEM",1);
				execute(mob,commands);
			}
			else
			{
				E=CMClass.getMOB(allWord);
				if((E!=null)&&(E instanceof MOB))
				{
					commands.insertElementAt("MOB",1);
					execute(mob,commands);
				}
				else
				if((lastWord!=null)&&(Directions.getGoodDirectionCode(lastWord)>=0))
				{
					commands.removeElementAt(commands.size()-1);
					allWord=Util.combine(commands,1);

					E=CMClass.getLocale(allWord);
					if(E==null)
						E=CMClass.getExit(allWord);
					if(E==null)
						E=CMClass.getAreaType(allWord);
					if((E!=null)&&(E instanceof Room))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("ROOM");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
					if((E!=null)&&(E instanceof Exit))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("EXIT");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
					if((E!=null)&&(E instanceof Area))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("AREA");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						execute(mob,commands);
					}
					else
						mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, QUEST, MOB, RACE, CLASS or ROOM.");
				}
				else
					mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, QUEST, MOB, RACE, CLASS, or ROOM.");
			}
		}
		return false;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return true;}
	public boolean arcCommand(){return true;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
