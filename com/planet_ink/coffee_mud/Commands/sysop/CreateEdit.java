package com.planet_ink.coffee_mud.Commands.sysop;

import com.planet_ink.coffee_mud.Commands.base.Socials;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Commands.base.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class CreateEdit
{
	private CreateEdit(){}

	public static void destroy(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";

		if(commands.size()>1)
		{
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}
		if(((String)commands.elementAt(0)).equalsIgnoreCase("UNLINK"))
		{
			commands.insertElementAt("ROOM",1);
			Rooms.destroy(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Exits.destroy(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Items.destroy(mob,commands);
		}
		else
		if((commandType.equals("AREA"))&&(mob.isASysOp(null)))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Rooms.destroyArea(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Rooms.destroy(mob,commands);
		}
		else
		if((commandType.equals("USER"))&&(mob.isASysOp(null)))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Mobs.destroyUser(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			SysopSocials.destroy(mob,commands);
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
				Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini"));
				if((banned!=null)&&(banned.size()>0))
					for(int b=0;b<banned.size();b++)
					{
						String B=(String)banned.elementAt(b);
						if((b+1)!=which)
							newBanned.append(B+"\n\r");
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
				ExternalPlay.DBDeleteJournal("SYSTEM_BUGS",which-1);
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
				ExternalPlay.DBDeleteJournal("SYSTEM_IDEAS",which-1);
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
				ExternalPlay.DBDeleteJournal("SYSTEM_TYPOS",which-1);
				mob.tell("Typo deletion submitted.");
			}
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Mobs.destroy(mob,commands);
		}
		else
		if(commandType.equals("QUEST"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
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
					mob.tell("Quest '"+Q.name()+"' destroyed!");
					Quests.delQuest(Q);
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
				destroy(mob,commands);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.insertElementAt("MOB",1);
				else
					commands.insertElementAt("USER",1);
				destroy(mob,commands);
			}
			else
			{
				Room theRoom=null;
				if(allWord.length()>0)
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room room=(Room)r.nextElement();
					if(room.ID().equalsIgnoreCase(allWord))
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
					commands.addElement(theRoom.ID());
					destroy(mob,commands);
				}
				else
				{
					if(Directions.getGoodDirectionCode(allWord)>=0)
					{
						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("ROOM");
						commands.addElement(allWord);
						destroy(mob,commands);

						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("EXIT");
						commands.addElement(allWord);
						destroy(mob,commands);
					}
					else
					if(Socials.FetchSocial(allWord)!=null)
					{
						commands.insertElementAt("SOCIAL",1);
						destroy(mob,commands);
					}
					else
					mob.tell(
						"\n\rYou cannot destroy a '"+commandType+"'. "
						+"However, you might try an "
						+"EXIT, ITEM, USER, MOB, QUEST, SOCIAL, BAN, BUG, TYPO, IDEA, or a ROOM.");
				}
			}
		}
	}

	public static void save(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";
		mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("USERS"))
		{
			for(int s=0;s<Sessions.size();s++)
			{
				Session session=(Session)Sessions.elementAt(s);
				if(session.mob()!=null)
				{
					ExternalPlay.DBUpdateMOB(session.mob());
					ExternalPlay.DBUpdateFollowers(session.mob());
				}
			}
			mob.location().showHappens(Affect.MSG_OK_ACTION,"A feeling of permanency envelopes everyone.\n\r");
		}
		else
		if(commandType.equals("ITEMS"))
		{
			Rooms.clearDebriAndRestart(mob.location(),1);
			Resources.removeResource("HELP_"+mob.location().name().toUpperCase());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("ROOM"))
		{
			Rooms.clearDebriAndRestart(mob.location(),0);
			Resources.removeResource("HELP_"+mob.location().name().toUpperCase());
			mob.location().show(mob,null,Affect.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("MOBS"))
		{
			Rooms.clearDebriAndRestart(mob.location(),2);
			Resources.removeResource("HELP_"+mob.location().name().toUpperCase());
			mob.location().showHappens(Affect.MSG_OK_ACTION,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("QUESTS"))
		{
			Quests.save();
			mob.tell("Quest list saved.");
		}
		else
		{
			mob.tell(
				"\n\rYou cannot save '"+commandType+"'. "
				+"However, you might try "
				+"ITEMS, USERS, MOBS, or ROOM.");
		}
	}

	public static void edit(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Items.modify(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Rooms.modify(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Rooms.modifyArea(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Exits.modify(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			SysopSocials.modify(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Mobs.modify(mob,commands);
		}
		else
		if(commandType.equals("QUEST"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("Start/Stop which quest?  Use list quests.");
			else
			{
				String name=Util.combine(commands,2);
				Quest Q=Quests.fetchQuest(name);
				if(Q==null)
					mob.tell("Quest '"+name+"' is unknown.  Try list quests.");
				else
				if(!mob.isMonster())
				{
					if((Q.running())&&(mob.session().confirm("Stop quest '"+Q.name()+"' (y/N)?","N")))
					{
						Q.stopQuest();
						mob.tell("Quest '"+Q.name()+"' stopped.");
					}
					else
					if((!Q.running())&&(mob.session().confirm("Start quest '"+Q.name()+"' (Y/n)?","Y")))
					{
						Q.startQuest();
						mob.tell("Quest '"+Q.name()+"' started.");
					}
				}
			}
		}
		else
		{
			String allWord=Util.combine(commands,1);
			Environmental thang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,allWord,Item.WORN_REQ_ANY);
			if((thang!=null)&&(thang instanceof Item))
			{
				if(!thang.isGeneric())
				{
					int showFlag=-1;
					if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
						showFlag=-999;
					boolean ok=false;
					while(!ok)
					{
						int showNumber=0;
						Generic.genLevel(mob,thang,++showNumber,showFlag);
						Generic.genAbility(mob,thang,++showNumber,showFlag);
						Generic.genRejuv(mob,thang,++showNumber,showFlag);
						Generic.genUses(mob,(Item)thang,++showNumber,showFlag);
						Generic.genMiscText(mob,thang,++showNumber,showFlag);
						if(showFlag<-900){ ok=true; break;}
						if(showFlag>0){ showFlag=-1; continue;}
						showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
						if(showFlag<=0) 
						{
							showFlag=-1;
							ok=true;
						}
					}
				}
				else
					Generic.genMiscSet(mob,thang);
				thang.recoverEnvStats();
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,thang.displayName()+" shake(s) under the transforming power.");
				Log.sysOut("CreateEdit",mob.ID()+" modified item "+thang.ID()+".");
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if((!thang.isGeneric())&&(((MOB)thang).isMonster()))
				{
					int showFlag=-1;
					if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_EDITORTYPE)>0)
						showFlag=-999;
					boolean ok=false;
					while(!ok)
					{
						int showNumber=0;
						Generic.genLevel(mob,thang,++showNumber,showFlag);
						Generic.genAbility(mob,thang,++showNumber,showFlag);
						Generic.genRejuv(mob,thang,++showNumber,showFlag);
						Generic.genMiscText(mob,thang,++showNumber,showFlag);
						if(showFlag<-900){ ok=true; break;}
						if(showFlag>0){ showFlag=-1; continue;}
						showFlag=Util.s_int(mob.session().prompt("Edit which? ",""));
						if(showFlag<=0) 
						{
							showFlag=-1;
							ok=true;
						}
					}
				}
				else
					Generic.genMiscSet(mob,thang);
				thang.recoverEnvStats();
				mob.location().show(mob,null,Affect.MSG_OK_VISUAL,thang.displayName()+" shake(s) under the transforming power.");
				Log.sysOut("CreateEdit",mob.ID()+" modified mob "+thang.ID()+".");
			}
			else
			if((Directions.getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(Directions.getGoodDirectionCode(allWord)>=0)
					thang=mob.location().rawExits()[Directions.getGoodDirectionCode(allWord)];

				if(thang!=null)
				{
					Generic.genMiscText(mob,thang,1,1);
					thang.recoverEnvStats();
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room room=(Room)r.nextElement();
						for(int e2=0;e2<Directions.NUM_DIRECTIONS;e2++)
						{
							Exit exit=room.rawExits()[e2];
							if((exit!=null)&&(exit==thang))
							{
								ExternalPlay.DBUpdateExits(room);
								break;
							}
						}
					}
					mob.location().show(mob,null,Affect.MSG_OK_VISUAL,thang.displayName()+" shake(s) under the transforming power.");
					Log.sysOut("CreateEdit",mob.ID()+" modified exit "+thang.ID()+".");
				}
				else
				{
					commands.insertElementAt("EXIT",1);
					edit(mob,commands);
				}
			}
			else
			if(Socials.FetchSocial(allWord)!=null)
			{
				commands.insertElementAt("SOCIAL",1);
				edit(mob,commands);
			}
			else
				mob.tell("\n\rYou cannot modify a '"+commandType+"'. However, you might try an ITEM, EXIT, QUEST, MOB, SOCIAL, or ROOM.");
		}
	}

	public static void link(MOB mob, Vector commands)
	{
		mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		Rooms.link(mob,commands);
	}
	
	public static void copy(MOB mob, Vector commands)
		throws Exception
	{
		mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
		Utils.newSomething(mob,commands);
	}
	
	public static void create(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Exits.create(mob,commands);
		}
		else
		if(commandType.equals("AREA"))
		{
			mob.tell("To create a new Area, you must first create a new room, then give that new room a new Area name.");
			return;
		}
		else
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Items.create(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Rooms.create(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			SysopSocials.create(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			Mobs.create(mob,commands);
		}
		else
		if(commandType.equals("QUEST"))
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"^S<S-NAME> wave(s) <S-HIS-HER> arms...^?");
			if(commands.size()<3)
				mob.tell("You must specify a valid quest string.  Try HELP QUESTS.");
			else
			{
				String script=Util.combine(commands,2);
				Quest Q=new Quests();
				Q.setScript(script);
				if((Q.name().length()==0)||(Q.duration()<0))
					mob.tell("You must specify a VALID quest string.  This one contained errors.  Try HELP QUESTS.");
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
				create(mob,commands);
			}
			else
			{
				E=CMClass.getMOB(allWord);
				if((E!=null)&&(E instanceof MOB))
				{
					commands.insertElementAt("MOB",1);
					create(mob,commands);
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
						create(mob,commands);
					}
					else
					if((E!=null)&&(E instanceof Exit))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("EXIT");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						create(mob,commands);
					}
					else
					if((E!=null)&&(E instanceof Area))
						mob.tell("To create a new Area, you must first create a new room, then give that new room a new Area name.");
					else
						mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, QUEST, MOB, or ROOM.");
				}
				else
					mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, QUEST, MOB, or ROOM.");
			}
		}
	}

}
