package com.planet_ink.coffee_mud.Commands.sysop;

import java.io.*;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class SysOpSkills
{
	private SysOpSkills(){}

	public static void ticktock(MOB mob, Vector commands)
	{
		Area A=CMMap.getFirstArea();
		int h=Util.s_int(Util.combine(commands,1));
		if(h==0) h=1;
		mob.tell("..tick..tock..");
		A.tickTock(h);
	}
	
	
	public static void stat(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		int ableTypes=-1;
		if(commands.size()>1)
		{
			String s=((String)commands.elementAt(0)).toUpperCase();
			for(int a=0;a<Ability.TYPE_DESCS.length;a++)
			{
				if(Ability.TYPE_DESCS[a].equals(s))
				{
					ableTypes=a;
					commands.removeElementAt(0);
					break;
				}
			}
		}
		String MOBname=(String)Util.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if((target==null)||((target!=null)&&(!target.isMonster())))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			Enumeration r=mob.isASysOp(null)?CMMap.rooms():mob.location().getArea().getMap();
			for(;r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MOB mob2=R.fetchInhabitant(MOBname);
				if(mob2!=null)
				{
					target=mob2;
					break;
				}
			}
		}
		if(target==null)
		{
			mob.tell("You can't stat '"+MOBname+"'  -- he doesn't exist.");
			return;
		}
		
		StringBuffer str=new StringBuffer("");
		if(ableTypes>=0)
		{
			Vector V=new Vector();
			int mask=Ability.ALL_CODES;
			V.addElement(new Integer(ableTypes));
			str=ExternalPlay.getAbilities(target,V,mask,false);
		}
		else
			str=ExternalPlay.getScore(target);
		if(!mob.isMonster())
			mob.session().unfilteredPrintln(str.toString());
		
	}

	public static void at(MOB mob, Vector commands)
		throws Exception
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("At where do what?");
			return;
		}
		String cmd=(String)commands.firstElement();
		commands.removeElementAt(0);
		Room room=findRoomLiberally(mob,new StringBuffer(cmd));
		if(room==null)
		{
			if(mob.isASysOp(mob.location()))
				mob.tell("At where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that.");
			return ;
		}
		if(!mob.isASysOp(room))
		{
			mob.tell("You aren't powerful enough to do that.");
			return ;
		}
		Room R=mob.location();
		if(R!=room)	room.bringMobHere(mob,false);
		ExternalPlay.doCommand(mob,commands);
		if(mob.location()!=R) R.bringMobHere(mob,false);
	}
	
	public static void load(MOB mob, Vector commands)
	{
		if(commands.size()<3)
		{
			mob.tell("LOAD what? Use LOAD RESOURCE/ABILITY/ITEM/WEAPON/ETC.. [CLASS NAME]");
			return;
		}
		String what=(String)commands.elementAt(1);
		String name=Util.combine(commands,2);
		if(what.equalsIgnoreCase("RESOURCE"))
		{
			StringBuffer buf=Resources.getFileResource(name);
			if((buf==null)||(buf.length()==0))
				mob.tell("Resource '"+name+"' was not found.");
			else
				mob.tell("Resource '"+name+"' was loaded.");
		}
		else
		if(CMClass.classCode(what)<0)
			mob.tell("'"+what+"' is not a valid class type.");
		else
		if(CMClass.loadClass(what,name))
			mob.tell(Util.capitalize(what)+" "+name+" was loaded.");
		else
			mob.tell(Util.capitalize(what)+" "+name+" was not loaded.");
	}

	public static void unload(MOB mob, Vector commands)
	{
		String str=Util.combine(commands,1);
		if(str.length()==0)
		{
			mob.tell("UNLOAD what?");
			return;
		}
		if(((String)commands.elementAt(1)).equalsIgnoreCase("CLASS"))
		{
			if(commands.size()<3)
			{
				mob.tell("Unload which class?");
				return;
			}
			commands.removeElementAt(0);
			commands.removeElementAt(0);
			for(int i=0;i<commands.size();i++)
			{
				String name=(String)commands.elementAt(0);
				Object O=CMClass.getClass(name);
				if((O==null)||(!CMClass.delClass(O)))
					mob.tell("Class '"+name+"' was not found in the library.");
				else
					mob.tell("Class '"+name+"' was unloaded.");
			}
			return;
		}
		if(str.equalsIgnoreCase("help"))
		{
			com.planet_ink.coffee_mud.Commands.base.Help.unloadHelpFile(mob);
			return;
		}
		if(str.equalsIgnoreCase("all"))
		{
			mob.tell("All resources unloaded.");
			Resources.clearResources();
			return;
		}
		Vector V=Resources.findResourceKeys(str);
		if(V.size()==0)
		{
			mob.tell("Unknown resource '"+str+"'.  Use LIST RESOURCES.");
			return;
		}
		for(int v=0;v<V.size();v++)
		{
			String key=(String)V.elementAt(v);
			Resources.removeResource(key);
			mob.tell("Resource '"+key+"' unloaded.");
		}
	}

	public static void ban(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		String banMe=Util.combine(commands,0);
		if(banMe.length()==0)
		{
			mob.tell("Ban what?  Enter an IP address or name mask.");
			return;
		}
		banMe=banMe.toUpperCase().trim();
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String B=(String)banned.elementAt(b);
			if(B.equals(banMe))
			{
				mob.tell("That is already banned.  Do LIST BANNED and check out #"+(b+1)+".");
				return;
			}
		}
		mob.tell("Logins and IPs matching '"+banMe+"' are now banned.");
		StringBuffer str=Resources.getFileResource("banned.ini",false);
		str.append(banMe+"\n\r");
		Resources.updateResource("banned.ini",str);
		Resources.saveFileResource("banned.ini");
	}

	public static void boot(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		if(mob.session()==null) return;
		if(commands.size()==0)
		{
			mob.tell("Boot out who?");
			return;
		}
		String whom=Util.combine(commands,0);
		boolean boot=false;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if((S.mob()!=null)&&(CoffeeUtensils.containsString(S.mob().name(),whom)))
			{
				if(S==mob.session())
				{
					mob.tell("Try QUIT.");
					return;
				}
				else
				if((mob.isASysOp(S.mob().location())))
				{
					mob.tell("You boot "+S.mob().name());
					if(S.mob().location()!=null)
						S.mob().location().show(S.mob(),null,Affect.MSG_OK_VISUAL,"Something is happening to <S-NAME>.");
					S.setKillFlag(true);
					boot=true;
					break;
				}
			}
		}
		if(!boot)
		mob.tell("You can't find anyone by that name.");
	}
	public static void i3Error(MOB mob)
	{
		if(mob.isASysOp(null))
			mob.tell("Try I3 LIST, I3 CHANNELS, I3 ADD [CHANNEL], I3 DELETE [CHANNEL], I3 LISTEN [CHANNEL], or I3 INFO [MUD].");
		else
			mob.tell("Try I3 LIST or I3 INFO [MUD-NAME].");
	}
	public static void i3(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		if(commands.size()<1)
		{
			i3Error(mob);
			return;
		}
		String str=(String)commands.firstElement();
		if(!(ExternalPlay.i3().i3online()))
			mob.tell("I3 is unavailable.");
		else
		if(str.equalsIgnoreCase("list"))
			ExternalPlay.i3().giveMudList(mob);
		else
		if(str.equalsIgnoreCase("add"))
		{
			if(!mob.isASysOp(null)){ i3Error(mob); return;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return;
			}
			ExternalPlay.i3().i3channelAdd(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("channels"))
			ExternalPlay.i3().giveChannelsList(mob);
		else
		if(str.equalsIgnoreCase("delete"))
		{
			if(!mob.isASysOp(null)){ i3Error(mob); return;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return;
			}
			ExternalPlay.i3().i3channelRemove(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("listen"))
		{
			if(!mob.isASysOp(null)){ i3Error(mob); return;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return;
			}
			ExternalPlay.i3().i3channelListen(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("silence"))
		{
			if(!mob.isASysOp(null)){ i3Error(mob); return;}
			if(commands.size()<2)
			{
				mob.tell("You did not specify a channel name!");
				return;
			}
			ExternalPlay.i3().i3channelSilence(mob,Util.combine(commands,1));
		}
		else
		if(str.equalsIgnoreCase("info"))
			ExternalPlay.i3().i3mudInfo(mob,Util.combine(commands,1));
		else
			i3Error(mob);
	}

	public static void toggleSysopMsgs(MOB mob)
	{
		if(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
			mob.setBitmap(Util.unsetb(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
		else
			mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
		mob.tell("Extended messages are now : "+((Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))?"ON":"OFF"));
	}

	public static void beacon(MOB mob, Vector commands)
	{
		if(commands.size()==0)
		{
			if(mob.getStartRoom()==mob.location())
				mob.tell("This is already your beacon.");
			else
			{
				mob.setStartRoom(mob.location());
				mob.tell("You have modified your beacon.");
			}
		}
		else
		{
			String name=Util.combine(commands,1);
			MOB M=null;
			for(int s=0;s<Sessions.size();s++)
			{
				Session S=Sessions.elementAt(s);
				if((S.mob()!=null)&&(CoffeeUtensils.containsString(S.mob().name(),name)))
				{ M=S.mob(); break;}
			}
			if(M==null)
			{
				mob.tell("No one is online called '"+name+"'!");
				return;
			}
			if(M.getStartRoom()==M.location())
			{
				mob.tell(M.name()+" is already at their beacon.");
				return;
			}
			M.setStartRoom(M.location());
			mob.tell("You have modified "+M.name()+"'s beacon.");
		}
	}
	private static MOB levelMOBup(int level, CharClass C)
	{
		MOB mob=(MOB)CMClass.getMOB("StdMOB");
		mob.setAlignment(500);
		mob.setName("Average Joe");
		mob.baseCharStats().setMyRace(CMClass.getRace("Human"));
		mob.baseCharStats().setStat(CharStats.GENDER,(int)'M');
		mob.baseCharStats().setStat(CharStats.STRENGTH,11);
		mob.baseCharStats().setStat(CharStats.WISDOM,11);
		mob.baseCharStats().setStat(CharStats.INTELLIGENCE,10);
		mob.baseCharStats().setStat(CharStats.DEXTERITY,11);
		mob.baseCharStats().setStat(CharStats.CONSTITUTION,10);
		mob.baseCharStats().setStat(CharStats.CHARISMA,10);
		mob.baseCharStats().setCurrentClass(C);
		mob.baseCharStats().setClassLevel(C,1);
		mob.baseEnvStats().setArmor(50);
		mob.baseEnvStats().setLevel(1);
		mob.baseEnvStats().setSensesMask(0);
		mob.baseState().setHitPoints(20);
		mob.baseState().setMovement(100);
		mob.baseState().setMana(100);
		mob.baseCharStats().getMyRace().startRacing(mob,false);
		mob.baseCharStats().getMyRace().outfit(mob);
		mob.recoverCharStats();
		mob.recoverEnvStats();
		mob.recoverMaxState();
		mob.resetToMaxState();
		mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);

		for(int lvl=1;lvl<level;lvl++)
		{
			switch(lvl % 6)
			{
			case 0:
				if(mob.baseCharStats().getStat(CharStats.STRENGTH)<mob.baseCharStats().getCurrentClass().getMaxStat(CharStats.STRENGTH)+5)
					mob.baseCharStats().setStat(CharStats.STRENGTH,mob.baseCharStats().getStat(CharStats.STRENGTH)+1);
				break;
			case 1:
				if(mob.baseCharStats().getStat(CharStats.DEXTERITY)<mob.baseCharStats().getCurrentClass().getMaxStat(CharStats.DEXTERITY)+5)
					mob.baseCharStats().setStat(CharStats.DEXTERITY,mob.baseCharStats().getStat(CharStats.DEXTERITY)+1);
				break;
			case 2:
				if(mob.baseCharStats().getStat(CharStats.INTELLIGENCE)<mob.baseCharStats().getCurrentClass().getMaxStat(CharStats.INTELLIGENCE))
					mob.baseCharStats().setStat(CharStats.INTELLIGENCE,mob.baseCharStats().getStat(CharStats.INTELLIGENCE)+1);
				break;
			case 3:
				if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<mob.baseCharStats().getCurrentClass().getMaxStat(CharStats.CONSTITUTION))
					mob.baseCharStats().setStat(CharStats.CONSTITUTION,mob.baseCharStats().getStat(CharStats.CONSTITUTION)+1);
				break;
			case 4:
				if(mob.baseCharStats().getStat(CharStats.CHARISMA)<mob.baseCharStats().getCurrentClass().getMaxStat(CharStats.CHARISMA))
					mob.baseCharStats().setStat(CharStats.CHARISMA,mob.baseCharStats().getStat(CharStats.CHARISMA)+1);
				break;
			case 5:
				if(mob.baseCharStats().getStat(CharStats.WISDOM)<mob.baseCharStats().getCurrentClass().getMaxStat(CharStats.WISDOM))
					mob.baseCharStats().setStat(CharStats.WISDOM,mob.baseCharStats().getStat(CharStats.WISDOM)+1);
				break;
			}
			int oldattack=mob.baseEnvStats().attackAdjustment();
			mob.charStats().getCurrentClass().gainExperience(mob,null,null,mob.getExpNeededLevel()+1,false);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
			int newAttack=mob.baseEnvStats().attackAdjustment()-oldattack;
			mob.baseEnvStats().setArmor(mob.baseEnvStats().armor()-newAttack);
			mob.recoverEnvStats();
			mob.recoverCharStats();
			mob.recoverMaxState();
		}
		return mob;
	}

	public static void averageout(MOB avgMob, int tries)
	{
		avgMob.baseCharStats().setStat(CharStats.STRENGTH,(int)Math.round(Util.div(avgMob.baseCharStats().getStat(CharStats.STRENGTH),tries)));
		avgMob.baseCharStats().setStat(CharStats.WISDOM,(int)Math.round(Util.div(avgMob.baseCharStats().getStat(CharStats.WISDOM),tries)));
		avgMob.baseCharStats().setStat(CharStats.INTELLIGENCE,(int)Math.round(Util.div(avgMob.baseCharStats().getStat(CharStats.INTELLIGENCE),tries)));
		avgMob.baseCharStats().setStat(CharStats.DEXTERITY,(int)Math.round(Util.div(avgMob.baseCharStats().getStat(CharStats.DEXTERITY),tries)));
		avgMob.baseCharStats().setStat(CharStats.CONSTITUTION,(int)Math.round(Util.div(avgMob.baseCharStats().getStat(CharStats.CONSTITUTION),tries)));
		avgMob.baseCharStats().setStat(CharStats.CHARISMA,(int)Math.round(Util.div(avgMob.baseCharStats().getStat(CharStats.CHARISMA),tries)));
		avgMob.baseEnvStats().setArmor((int)Math.round(Util.div(avgMob.baseEnvStats().armor(),tries)));
		avgMob.baseState().setHitPoints((int)Math.round(Util.div(avgMob.baseState().getHitPoints(),tries)));
		avgMob.baseState().setMovement((int)Math.round(Util.div(avgMob.baseState().getMovement(),tries)));
		avgMob.baseState().setMana((int)Math.round(Util.div(avgMob.baseState().getMana(),tries)));
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
		avgMob.setTrains(0);
	}

	public static void addHimIn(MOB avgMob, MOB mob2)
	{
		avgMob.baseCharStats().setStat(CharStats.STRENGTH,avgMob.baseCharStats().getStat(CharStats.STRENGTH)+mob2.baseCharStats().getStat(CharStats.STRENGTH));
		avgMob.baseCharStats().setStat(CharStats.WISDOM,avgMob.baseCharStats().getStat(CharStats.WISDOM)+mob2.baseCharStats().getStat(CharStats.WISDOM));
		avgMob.baseCharStats().setStat(CharStats.INTELLIGENCE,avgMob.baseCharStats().getStat(CharStats.INTELLIGENCE)+mob2.baseCharStats().getStat(CharStats.INTELLIGENCE));
		avgMob.baseCharStats().setStat(CharStats.DEXTERITY,avgMob.baseCharStats().getStat(CharStats.DEXTERITY)+mob2.baseCharStats().getStat(CharStats.DEXTERITY));
		avgMob.baseCharStats().setStat(CharStats.CONSTITUTION,avgMob.baseCharStats().getStat(CharStats.CONSTITUTION)+mob2.baseCharStats().getStat(CharStats.CONSTITUTION));
		avgMob.baseCharStats().setStat(CharStats.CHARISMA,avgMob.baseCharStats().getStat(CharStats.CHARISMA)+mob2.baseCharStats().getStat(CharStats.CHARISMA));
		avgMob.baseEnvStats().setArmor(avgMob.baseEnvStats().armor()+mob2.baseEnvStats().armor());
		avgMob.baseState().setHitPoints(avgMob.baseState().getHitPoints()+mob2.baseState().getHitPoints());
		avgMob.baseState().setMovement(avgMob.baseState().getMovement()+mob2.baseState().getMovement());
		avgMob.baseState().setMana(avgMob.baseState().getMana()+mob2.baseState().getMana());
		avgMob.recoverCharStats();
		avgMob.recoverEnvStats();
		avgMob.recoverMaxState();
		avgMob.resetToMaxState();
	}


	public static void snoop(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		if(mob.session()==null) return;
		boolean doneSomething=false;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if(S.amSnooping(mob.session()))
			{
				if(S.mob()!=null)
					mob.tell("You stop snooping on "+S.mob().name()+".");
				else
					mob.tell("You stop snooping on someone.");
				doneSomething=true;
				S.stopSnooping(mob.session());
			}
		}
		if(commands.size()==0)
		{
			if(!doneSomething)
				mob.tell("Snoop on whom?");
			return;
		}
		String whom=Util.combine(commands,0);
		boolean snoop=false;
		for(int s=0;s<Sessions.size();s++)
		{
			Session S=Sessions.elementAt(s);
			if((S.mob()!=null)&&(CoffeeUtensils.containsString(S.mob().name(),whom)))
			{
				if(S==mob.session())
				{
					mob.tell("no.");
					return;
				}
				else
				if((!S.amSnooping(mob.session()))
				&&(mob.isASysOp(S.mob().location())))
				{
					mob.tell("You start snooping on "+S.mob().name()+".");
					S.startSnooping(mob.session());
					snoop=true;
					break;
				}
			}
		}
		if(!snoop)
		mob.tell("You can't find anyone by that name.");
	}

	public static MOB AverageClassMOB(MOB mob, int level, CharClass C, int numTries)
	{
		MOB avgMob=(MOB)levelMOBup(level,C);
		int tries=0;
		for(;tries<numTries;tries++)
		{
			if((tries % 20)==0)
				mob.session().print(".");
			MOB mob2=(MOB)levelMOBup(level,C);
			addHimIn(avgMob,mob2);
		}
		averageout(avgMob,tries);
		return avgMob;
	}

	public static MOB AverageAllClassMOB(MOB mob, int level, int numTriesClass, int numTriesMOB)
	{
		MOB avgMob=null;
		int tries=0;
		int numClasses=0;
		for(;tries<numTriesClass;tries++)
		{
			for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=(CharClass)c.nextElement();
				if(C.playerSelectable())
				{
					numClasses++;
					MOB mob2=AverageClassMOB(mob,level,C,numTriesMOB);
					if(avgMob==null)
					{
						avgMob=mob2;
						numClasses--;
					}
					else
						addHimIn(avgMob,mob2);
				}
			}
		}
		averageout(avgMob,numClasses);
		return avgMob;
	}

	public static boolean chargen(MOB mob, Vector commands)
	{
		if(mob.isMonster())
			return false;
		commands.removeElementAt(0);
		CharClass C=null;
		int level=-1;
		String ClassName="";
		if(commands.size()>0)
		{
			ClassName=(String)commands.elementAt(0);
			C=CMClass.getCharClass(ClassName);
			level=Util.s_int(Util.combine(commands,1));
		}

		if((C==null)&&(ClassName.toUpperCase().indexOf("ALL")<0))
		{
			mob.tell("Enter 'ALL' for all classes.");
			try
			{
				ClassName=mob.session().prompt("Enter a class name: ");
			}
			catch(Exception e){return false;}

			C=CMClass.getCharClass(ClassName);
			if((C==null)&&(ClassName.toUpperCase().indexOf("ALL")<0))
				return false;
		}

		if(level<=0)
		{
			try
			{
				level=Util.s_int(mob.session().prompt("Enter a level (1-25): "));
			}
			catch(Exception e){return false;}
			if(level<=0)
				return false;
		}

		if(C!=null)
			mob.session().print("\n\rAverage "+C.name()+"...");
		else
			mob.session().print("\n\rAverage MOB stats, across all classes...");

		MOB avgMob=null;
		if(C!=null)
			avgMob=AverageClassMOB(mob, level,C, 100);
		else
			avgMob=AverageAllClassMOB(mob,level, 20, 40);

		mob.session().println("\n\r");

		if(avgMob!=null)
		{
			StringBuffer msg=ExternalPlay.getScore(avgMob);
			if(!mob.isMonster())
				mob.session().unfilteredPrintln(msg.toString());
		}
		return true;
	}

	public static Room findRoomLiberally(MOB mob, StringBuffer cmd)
	{
		Room room=null;
		Room curRoom=mob.location();
		int dirCode=Directions.getGoodDirectionCode(cmd.toString());
		if(dirCode>=0)
			room=mob.location().rawDoors()[dirCode];
		if(room==null)
			room = CMMap.getRoom(cmd.toString());
		if(room==null)
		{
			if((cmd.charAt(0)=='#')&&(curRoom!=null))
			{
				cmd.insert(0,curRoom.getArea().Name());
				room = CMMap.getRoom(cmd.toString());
			}
			else
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=(Session)Sessions.elementAt(s);
					if((thisSession.mob()!=null) && (!thisSession.killFlag())
					&&(thisSession.mob().location()!=null)
					&&(thisSession.mob().name().equalsIgnoreCase(cmd.toString())))
					{
						room = thisSession.mob().location();
						break;
					}
				}
				if(room==null)
					for(int s=0;s<Sessions.size();s++)
					{
						Session thisSession=(Session)Sessions.elementAt(s);
						if((thisSession.mob()!=null)&&(!thisSession.killFlag())
						&&(thisSession.mob().location()!=null)
						&&(CoffeeUtensils.containsString(thisSession.mob().name(),cmd.toString())))
						{
							room = thisSession.mob().location();
							break;
						}
					}
				if(room==null)
				{
					Vector candidates=new Vector();
					MOB target=null;
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						target=R.fetchInhabitant(cmd.toString());
						if(target!=null)
							candidates.addElement(target);
					}
					if(candidates.size()>0)
					{
						target=(MOB)candidates.elementAt(Dice.roll(1,candidates.size(),-1));
						room=target.location();
					}
				}
				if(room==null)
				{
					for(Enumeration a=CMMap.areas();a.hasMoreElements();)
					{
						Area A=(Area)a.nextElement();
						if((CoffeeUtensils.containsString(A.name(),cmd.toString()))
						&&(A.mapSize()>0))
						{
							int tries=0;
							while(((room==null)||(room.roomID().length()==0))&&((++tries)<200))
								room=(Room)A.getRandomRoom();
							break;
						}
					}
				}
				if(room==null)
				{
					String areaName=cmd.toString().toUpperCase();
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(CoffeeUtensils.containsString(R.displayText(),areaName))
						{
						   room=R;
						   break;
						}
					}
					if(room==null)
					for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
					{
						Room R=(Room)r.nextElement();
						if(CoffeeUtensils.containsString(R.description(),areaName))
						{
						   room=R;
						   break;
						}
					}
					if(room==null)
					{
						Vector candidates=new Vector();
						Item target=null;
						for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
						{
							Room R=(Room)r.nextElement();
							target=R.fetchItem(null,cmd.toString());
							if(target!=null)
								candidates.addElement(target);
						}
						if(candidates.size()>0)
						{
							target=(Item)candidates.elementAt(Dice.roll(1,candidates.size(),-1));
							if(target.owner() instanceof Room)
								room=(Room)target.owner();
						}
					}
				}
			}
		}
		return room;
	}
	
	public static boolean gotoCmd(MOB mob, Vector commands)
	{

		Room room=null;
		if(commands.size()<2)
		{
			mob.tell("Go where? Try a Room ID, player name, area name, or room text!");
			return false;
		}
		commands.removeElementAt(0);
		boolean chariot=false;
		if(((String)commands.lastElement()).equalsIgnoreCase("!"))
		{
		   chariot=true;
		   commands.removeElement(commands.lastElement());
		}
		StringBuffer cmd = new StringBuffer(Util.combine(commands,0));
		Room curRoom=mob.location();
		room=findRoomLiberally(mob,cmd);

		if(room==null)
		{
			if(mob.isASysOp(mob.location()))
				mob.tell("Goto where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that. Try 'GO'.");
			return false;
		}
		if(!mob.isASysOp(room))
		{
			mob.tell("You aren't powerful enough to do that. Try 'GO'.");
			return false;
		}
		if(curRoom==room)
		{
			mob.tell("Done.");
			return true;
		}
		else
		{
			room.bringMobHere(mob,true);
			if(chariot)
			{
				room.show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> ride(s) in on a flaming chariot.");
				ExternalPlay.look(mob,null,true);
			}
			else
				mob.tell("Done.");
			return true;
		}
	}
	public static boolean transferCmd(MOB mob, Vector commands)
	{

		Room room=null;
		if(commands.size()<3)
		{
			mob.tell("Transfer whom where? Try all or a mob name, followerd by a Room ID, target player name, area name, or room text!");
			return false;
		}
		commands.removeElementAt(0);
		String mobname=(String)commands.elementAt(0);
		Room curRoom=mob.location();
		Vector V=new Vector();
		if(mobname.equalsIgnoreCase("all"))
		{
			for(int i=0;i<curRoom.numInhabitants();i++)
			{
				MOB M=(MOB)curRoom.fetchInhabitant(i);
				if(M!=null)
					V.addElement(M);
			}
		}
		else
		{
			for(Enumeration r=mob.location().getArea().getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MOB M=null;
				int num=1;
				while((num<=1)||(M!=null))
				{
					M=R.fetchInhabitant(mobname+"."+num);
					if((M!=null)&&(!V.contains(M)))
					   V.addElement(M);
					num++;
				}
			}
			if(V.size()==0)
			{
				for(Enumeration r=CMMap.rooms();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					MOB M=null;
					int num=1;
					while((num<=1)||(M!=null))
					{
						M=R.fetchInhabitant(mobname+"."+num);
						if((M!=null)&&(!V.contains(M)))
						   V.addElement(M);
						num++;
					}
				}
			}
		}
		
		if(V.size()==0)
		{
			mob.tell("Transfer whom?  '"+mobname+"' is unknown to you.");
			return false;
		}
		
		StringBuffer cmd = new StringBuffer(Util.combine(commands,1));
		if(cmd.equals("here")||cmd.equals("."))
			room=mob.location();
		else
			room=findRoomLiberally(mob,cmd);

		if(room==null)
		{
			if(mob.isASysOp(mob.location()))
				mob.tell("Goto where? Try a Room ID, player name, area name, or room text!");
			else
				mob.tell("You aren't powerful enough to do that. Try 'GO'.");
			return false;
		}
		if(!mob.isASysOp(room))
		{
			mob.tell("You aren't powerful enough to do that. Try 'GO'.");
			return false;
		}
		for(int i=0;i<V.size();i++)
		{
			MOB M=(MOB)V.elementAt(i);
			if(!room.isInhabitant(M))
			{
				room.bringMobHere(M,true);
				if(!M.isMonster())
					ExternalPlay.look(M,null,true);
			}
		}
		mob.tell("Done.");
		return true;
	}
	public static MOB getTarget(MOB mob, Vector commands, boolean quiet)
	{
		String targetName=Util.combine(commands,0);
		MOB target=null;
		if(targetName.length()>0)
		{
			target=mob.location().fetchInhabitant(targetName);
			if(target==null)
			{
				Environmental t=mob.location().fetchFromRoomFavorItems(null,targetName,Item.WORN_REQ_UNWORNONLY);
				if((t!=null)&&(!(t instanceof MOB)))
				{
					if(!quiet)
						mob.tell(mob,t,null,"You can't do that to <T-NAMESELF>.");
					return null;
				}
			}
		}

		if(target!=null)
			targetName=target.name();

		if((target==null)||((!Sense.canBeSeenBy(target,mob))&&((!Sense.canBeHeardBy(target,mob))||(!target.isInCombat()))))
		{
			if(!quiet)
			{
				if(targetName.trim().length()==0)
					mob.tell("You don't see them here.");
				else
					mob.tell("You don't see '"+targetName+"' here.");
			}
			return null;
		}

		return target;
	}

	public static boolean possess(MOB mob, Vector commands)
	{
		commands.removeElementAt(0);
		String MOBname=Util.combine(commands,0);
		MOB target=getTarget(mob,commands,true);
		if((target==null)||((target!=null)&&(!target.isMonster())))
			target=mob.location().fetchInhabitant(MOBname);
		if((target==null)||((target!=null)&&(!target.isMonster())))
		{
			Enumeration r=mob.isASysOp(null)?CMMap.rooms():mob.location().getArea().getMap();
			for(;r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				MOB mob2=R.fetchInhabitant(MOBname);
				if((mob2!=null)&&(mob2.isMonster()))
				{
					target=mob2;
					break;
				}
			}
		}
		if((target==null)||(!target.isMonster()))
		{
			mob.tell("You can't possess '"+MOBname+"' right now.");
			return false;
		}

		mob.location().showOthers(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> get(s) a far away look, then seem(s) to fall limp.");

		Session s=mob.session();
		s.setMob(target);
		target.setSession(s);
		target.setSoulMate(mob);
		mob.setSession(null);
		ExternalPlay.look(target,null,true);
		target.tell("^HYour spirit has changed bodies...");
		return true;
	}

	public static void dispossess(MOB mob)
	{
		if(mob.soulMate()==null)
		{
			mob.tell("Huh?");
			return;
		}
		Session s=mob.session();
		s.setMob(mob.soulMate());
		mob.soulMate().setSession(s);
		mob.setSession(null);
		mob.soulMate().tell("^HYour spirit has returned to your body...\n\r\n\r^N");
		ExternalPlay.look(mob.soulMate(),null,true);
		mob.setSoulMate(null);
	}

	public static boolean wizinv(MOB mob, Vector commands)
	{
		String str=(String)commands.firstElement();
		if(Character.toUpperCase(str.charAt(0))!='W')
			commands.insertElementAt("OFF",1);
		commands.removeElementAt(0);
		str="Prop_WizInvis";
		Ability A=mob.fetchAffect(str);
		if(Util.combine(commands,0).trim().equalsIgnoreCase("OFF"))
		{
		   if(A!=null)
			   A.unInvoke();
		   else
			   mob.tell("You are not wizinvisible!");
		   return true;
		}
		else
		if(A!=null)
		{
			mob.tell("You have already faded from view!");
			return false;
		}

		// it worked, so build a copy of this ability,
		// and add it to the affects list of the
		// affected MOB.  Then tell everyone else
		// what happened.
		A=CMClass.getAbility(str);
		if(A!=null)
		{
			mob.location().show(mob,null,Affect.MSG_OK_VISUAL,"<S-NAME> fade(s) from view!");
			mob.addAffect((Ability)A.copyOf());
			mob.recoverEnvStats();
			mob.location().recoverRoomStats();
			mob.tell("You may uninvoke WIZINV with 'WIZINV OFF'.");
			return true;
		}
		else
		{
			mob.tell("Wizard invisibility is not available!");
			return false;
		}
	}

	private static void sendAnnounce(String announcement, Session S)
	{
	  	StringBuffer Message=new StringBuffer("");
	  	int alignType=1;
	  	if(S.mob().getAlignment()<350)
	  		alignType=0;
	  	else
	  	if(S.mob().getAlignment()<650) alignType= 2;
	  	switch(alignType)
	  	{
	  	  case 0:
	  	    Message.append("^rA terrifying voice bellows out of Hell '");
	  	    break;
	  	  case 1:
	  	    Message.append("^wAn awe-inspiring voice thunders down from Heaven '");
	  	    break;
	  	  case 2:
	  	    Message.append("^pA powerful voice rings out '");
	  	    break;
	  	}
	  	Message.append(announcement);
	  	Message.append("'.^N");
	  	S.stdPrintln(Message.toString());
	}

	public static void announce(MOB mob,Vector commands)
	{
		if(commands.size()>1)
		{
			if(((String)commands.elementAt(1)).toUpperCase().equals("ALL"))
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)&&(S.mob().location()!=null)&&(mob.isASysOp(S.mob().location())))
						sendAnnounce((String)Util.combine(commands,2),S);
				}
			}
			else
			{
				boolean found=false;
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(mob.isASysOp(S.mob().location()))
					&&(CoffeeUtensils.containsString(S.mob().name(),(String)commands.elementAt(1))))
					{
						sendAnnounce(Util.combine(commands,2),S);
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell("You can't find anyone by that name.");
			}
	    }
	    else
			mob.tell("You can either send a message to everyone on the server or a single user using \n\r    ANNOUNCE [ALL|(USER NAME)] (MESSAGE) \n\rGood aligned players will perceive it as coming from heaven, evil from hell, and neutral from out of nowhere.");
	}
	
	public static void wizemote(MOB mob,Vector commands)
	{
		if(commands.size()>2)
		{
			String who=(String)commands.elementAt(1);
			String msg=Util.combine(commands,2);
			if(who.toUpperCase().equals("ALL"))
			{
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)&&(S.mob().location()!=null)&&(mob.isASysOp(S.mob().location())))
	  					S.stdPrintln("^w"+msg+"^?");
				}
			}
			else
			{
				boolean found=false;
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S.mob()!=null)
					&&(S.mob().location()!=null)
					&&(mob.isASysOp(S.mob().location()))
					&&(CoffeeUtensils.containsString(S.mob().name(),who)
						||CoffeeUtensils.containsString(S.mob().location().getArea().name(),who)))
					{
	  					S.stdPrintln("^w"+msg+"^?");
						found=true;
						break;
					}
				}
				if(!found)
					mob.tell("You can't find anyone or anywhere by that name.");
			}
	    }
	    else
			mob.tell("You must specify either all, or an area/mob name, and an message.");
	}
}
