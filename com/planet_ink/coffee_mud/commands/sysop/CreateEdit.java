package com.planet_ink.coffee_mud.commands.sysop;

import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.db.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.interfaces.*;
import java.util.*;
public class CreateEdit
{


	public static void Destroy(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";

		if(commands.size()>1)
		{
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}

		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Exits.Destroy(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Items.Destroy(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Rooms.Destroy(mob,commands);
		}
		else
		if(commandType.equals("USER"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Mobs.DestroyUser(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			SysopSocials.Destroy(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Mobs.Destroy(mob,commands);
		}
		else
		{
			String allWord=CommandProcessor.combine(commands,1);
			Environmental thang=mob.location().fetchFromMOBRoom(mob,null,allWord);
			if((thang!=null)&&(thang instanceof Item))
			{
				commands.insertElementAt("ITEM",1);
				Destroy(mob,commands);
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				if(((MOB)thang).isMonster())
					commands.insertElementAt("MOB",1);
				else
					commands.insertElementAt("USER",1);
				Destroy(mob,commands);
			}
			else
			{
				Room theRoom=null;
				for(int m=0;m<MUD.map.size();m++)
				{
					Room room=(Room)MUD.map.elementAt(m);
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
					Destroy(mob,commands);
				}
				else
				{
					if(Directions.getGoodDirectionCode(allWord)>=0)
					{
						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("ROOM");
						commands.addElement(allWord);
						Destroy(mob,commands);

						commands=new Vector();
						commands.addElement("DESTROY");
						commands.addElement("EXIT");
						commands.addElement(allWord);
						Destroy(mob,commands);
					}
					else
					if(MUD.allSocials.FetchSocial(allWord)!=null)
					{
						commands.insertElementAt("SOCIAL",1);
						Destroy(mob,commands);
					}
					else
					mob.tell(
						"\n\rYou cannot destroy a '"+commandType+"'. "
						+"However, you might try an "
						+"EXIT, ITEM, USER, MOB, SOCIAL, or a ROOM.");
				}
			}
		}
	}

	public static void Save(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";
		mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("USERS"))
		{
			for(int s=0;s<MUD.allSessions.size();s++)
			{
				Session session=(Session)MUD.allSessions.elementAt(s);
				if(session.mob!=null)
				{
					MOBloader.DBUpdate(session.mob);
					MOBloader.DBUpdateFollowers(mob);
				}
			}
			mob.location().show(mob,null,Affect.GENERAL,"A feeling of permanency envelopes everyone.\n\r");
		}
		else
		if(commandType.equals("ITEMS"))
		{
			Rooms.clearDebriAndRestart(mob.location(),1);
			mob.location().show(mob,null,Affect.GENERAL,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("ROOM"))
		{
			Rooms.clearDebriAndRestart(mob.location(),0);
			mob.location().show(mob,null,Affect.GENERAL,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		if(commandType.equals("MOBS"))
		{
			Rooms.clearDebriAndRestart(mob.location(),2);
			mob.location().show(mob,null,Affect.GENERAL,"A feeling of permanency envelopes the area.\n\r");
		}
		else
		{
			mob.tell(
				"\n\rYou cannot save '"+commandType+"'. "
				+"However, you might try "
				+"ITEMS, USERS, MOBS, or ROOM.");
		}
	}

	public static void Edit(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Items.Modify(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Rooms.Modify(mob,commands);
		}
		else
		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Exits.Modify(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			SysopSocials.Modify(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Mobs.Modify(mob,commands);
		}
		else
		{
			String allWord=CommandProcessor.combine(commands,1);
			Environmental thang=mob.location().fetchFromMOBRoom(mob,null,allWord);
			if((thang!=null)&&(thang instanceof Item))
			{
				Generic.genLevel(mob,thang);
				Generic.genAbility(mob,thang);
				Generic.genRejuv(mob,thang);
				Generic.genUses(mob,(Item)thang);
				Generic.genMiscText(mob,thang);
				thang.recoverEnvStats();
				mob.location().show(mob,null,Affect.VISUAL_ONLY,thang.name()+" shake(s) under the transforming power.");
				Log.sysOut("CreateEdit",mob.ID()+" modified item "+thang.ID()+".");
			}
			else
			if((thang!=null)&&(thang instanceof MOB))
			{
				Generic.genLevel(mob,thang);
				Generic.genAbility(mob,thang);
				Generic.genRejuv(mob,thang);
				Generic.genMiscText(mob,thang);
				thang.recoverEnvStats();
				mob.location().show(mob,null,Affect.VISUAL_ONLY,thang.name()+" shake(s) under the transforming power.");
				Log.sysOut("CreateEdit",mob.ID()+" modified mob "+thang.ID()+".");
			}
			else
			if((Directions.getGoodDirectionCode(allWord)>=0)||(thang instanceof Exit))
			{
				if(Directions.getGoodDirectionCode(allWord)>=0)
					thang=mob.location().exits()[Directions.getGoodDirectionCode(allWord)];

				if(thang!=null)
				{
					Generic.genMiscText(mob,thang);
					thang.recoverEnvStats();
					mob.location().show(mob,null,Affect.VISUAL_ONLY,thang.name()+" shake(s) under the transforming power.");
					Log.sysOut("CreateEdit",mob.ID()+" modified exit "+thang.ID()+".");
				}
				else
				{
					commands.insertElementAt("EXIT",1);
					Edit(mob,commands);
				}
			}
			else
			if(MUD.allSocials.FetchSocial(allWord)!=null)
			{
				commands.insertElementAt("SOCIAL",1);
				Edit(mob,commands);
			}
			else
				mob.tell("\n\rYou cannot modify a '"+commandType+"'. However, you might try an ITEM, EXIT, MOB, SOCIAL, or a ROOM.");
		}
	}

	public static void Create(MOB mob, Vector commands)
		throws Exception
	{
		String commandType="";
		if(commands.size()>1)
			commandType=((String)commands.elementAt(1)).toUpperCase();

		if(commandType.equals("EXIT"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Exits.Create(mob,commands);
		}
		else
		if(commandType.equals("ITEM"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Items.Create(mob,commands);
		}
		else
		if(commandType.equals("ROOM"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Rooms.Create(mob,commands);
		}
		else
		if(commandType.equals("SOCIAL"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			SysopSocials.Create(mob,commands);
		}
		else
		if(commandType.equals("MOB"))
		{
			mob.location().show(mob,null,Affect.VISUAL_ONLY,"<S-NAME> wave(s) <S-HIS-HER> arms...");
			Mobs.Create(mob,commands);
		}
		else
		{
			String allWord=CommandProcessor.combine(commands,1);
			String lastWord=null;
			if(commands.size()>2)
				lastWord=(String)commands.elementAt(commands.size()-1);
			Environmental E=null;
			E=MUD.getItem(allWord);
			if((E!=null)&&(E instanceof Item))
			{
				commands.insertElementAt("ITEM",1);
				Create(mob,commands);
			}
			else
			{
				E=MUD.getMOB(allWord);
				if((E!=null)&&(E instanceof MOB))
				{
					commands.insertElementAt("MOB",1);
					Create(mob,commands);
				}
				else
				if((lastWord!=null)&&(Directions.getGoodDirectionCode(lastWord)>=0))
				{
					commands.removeElementAt(commands.size()-1);
					allWord=CommandProcessor.combine(commands,1);

					E=MUD.getLocale(allWord);
					if(E==null)
						E=MUD.getExit(allWord);
					if((E!=null)&&(E instanceof Room))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("ROOM");
						commands.addElement(lastWord);
						commands.addElement("NEW");
						commands.addElement(allWord);
						Create(mob,commands);
					}
					else
					if((E!=null)&&(E instanceof Exit))
					{
						commands=new Vector();
						commands.addElement("CREATE");
						commands.addElement("EXIT");
						commands.addElement(lastWord);
						commands.addElement(allWord);
						Create(mob,commands);
					}
					else
					{
						Room theRoom=null;
						for(int m=0;m<MUD.map.size();m++)
						{
							Room room=(Room)MUD.map.elementAt(m);
							if(room.ID().equalsIgnoreCase(allWord))
							{
								theRoom=room;
								break;
							}
						}
						if(theRoom!=null)
						{
							commands=new Vector();
							commands.addElement("CREATE");
							commands.addElement("ROOM");
							commands.addElement(lastWord);
							commands.addElement("LINK");
							commands.addElement(theRoom.ID());
							Create(mob,commands);
						}
						else
							mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, MOB, or a ROOM.");
					}
				}
				else
					mob.tell("\n\rYou cannot create a '"+commandType+"'. However, you might try an EXIT, ITEM, MOB, or a ROOM.");
			}
		}
	}

}
