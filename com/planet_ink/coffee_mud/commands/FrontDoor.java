package com.planet_ink.coffee_mud.commands;
import com.planet_ink.coffee_mud.MOBS.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.telnet.*;
import com.planet_ink.coffee_mud.service.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.application.*;
import com.planet_ink.coffee_mud.db.*;
import java.io.*;
import java.util.*;
public class FrontDoor
{
	
	private static Vector classQualifies(MOB mob)
	{
		Vector them=new Vector();
		for(int c=0;c<MUD.charClasses.size();c++)
		{
			CharClass thisClass=(CharClass)MUD.charClasses.elementAt(c);
			if((thisClass.playerSelectable())&&thisClass.qualifiesForThisClass(mob))
				them.addElement(thisClass);
		}
		return them;
	}
	
	private static boolean isOkName(String login)
	{
		login=login.trim();
		
		if(login.indexOf(" ")>0) return false;
		if((" SHIT FUCK CUNT ASS PUSSY COCK DAMN ").indexOf(" "+login.toUpperCase()+" ")>=0)
			return false;
		for(int m=0;m<MUD.MOBs.size();m++)
		{
			MOB tm=(MOB)MUD.MOBs.elementAt(m);
			if((Util.containsString(tm.ID(),login))
			||(Util.containsString(tm.name(),login)))
				return false;
		}
		for(Enumeration e=MOBloader.MOBs.elements();e.hasMoreElements();)
		{
			MOB tm=(MOB)e.nextElement();
			if((Util.containsString(tm.ID(),login))
			||(Util.containsString(tm.name(),login)))
				return false;
					
		}
		return true;
		
	}
	
	public static boolean login(MOB mob)
		throws IOException
	{
		if(mob==null) 
			return false;
		if(mob.session()==null) 
			return false;
		try
		{
			String login=mob.session().prompt("name:");
			if(login==null) return false;
			login=login.trim();
			if(login.length()==0) return false;
			
			if(login.equalsIgnoreCase("You"))
				return false;
			MOBloader.DBUserSearch(mob,login);
			if(mob.ID().trim().length()>0)
			{
				mob.session().print("password:");
				String password=mob.session().blockingIn();
				if((mob.password().equalsIgnoreCase(password))&&(mob.name().trim().length()>0))
				{
					boolean swapMade=false;
					for(int s=0;s<MUD.allSessions.size();s++)
					{
						Session thisSession=(Session)MUD.allSessions.elementAt(s);
						if((thisSession.mob!=null)&&(thisSession!=mob.session()))
						{
							if((thisSession.mob.ID().equals(mob.ID())))
							{
								swapMade=true;
								mob.session().mob=thisSession.mob;
								thisSession.mob.setSession(mob.session());
								thisSession.mob=null;
								if(thisSession.out!=null)
									thisSession.out.close();
								if(thisSession.in!=null)
									thisSession.in.close();
								if(thisSession.sock!=null)
									thisSession.sock.close();
								thisSession.out=null;
								thisSession.in=null;
								thisSession.sock=null;
								thisSession.killFlag=true;
								Log.sysOut("FrontDoor","Session swap for "+mob.session().mob.name()+".");
								mob.session().mob.bringToLife(mob.location());
								return true;
							}
						}
					}
					MOB oldMOB=mob;
					if(MOBloader.MOBs.get(oldMOB.ID())!=null)
					{
						oldMOB.session().mob=(MOB)MOBloader.MOBs.get(oldMOB.ID());
						mob=oldMOB.session().mob;
						mob.setSession(oldMOB.session());
						if(mob!=oldMOB)
							oldMOB.setSession(null);
					}
					else
					{
						MOBloader.DBRead(mob);
						mob.setUserInfo(mob.ID(),password,Calendar.getInstance());
						MOBloader.DBUpdate(mob);
						if(mob.baseCharStats()!=null)
							if(mob.baseCharStats().getMyClass()!=null)
								mob.baseCharStats().getMyClass().logon(mob);
					}
					mob.bringToLife(mob.location());
					MOBloader.DBReadFollowers(mob);
				}
				else
				{
					Log.sysOut("FrontDoor","Failed login: "+mob.name());
					mob.setUserInfo("","",Calendar.getInstance());
					mob.session().println("\n\rInvalid password.\n\r");
					return false;
				}
			}
			else
			{
				if(!isOkName(login))
				{
					mob.session().println("\n\rThat name is unrecognized.\n\rThat name is also not available for new users.\n\r  Choose another name (no spaces allowed)!\n\r");
					mob.setUserInfo("","",Calendar.getInstance());
				}
				else
				if(mob.session().confirm("\n\r'"+login+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
				{
					
					mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("newchar.txt").toString());
					
					String password="";
					while(password.length()==0)
					{
						password=mob.session().prompt("\n\rEnter a password: ","");
						if(password.length()==0)
							mob.session().println("\n\rYou must enter a password to continue.");
					}
					mob.setUserInfo(login,password,Calendar.getInstance());
					Log.sysOut("FrontDoor","Creating user: "+mob.name());
					
					mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("races.txt").toString());
					
					mob.session().print("\n\rPlease choose from the following races: ");
					for(int r=0;r<MUD.races.size();r++)
					{
						Race thisRace=(Race)MUD.races.elementAt(r);
						if(thisRace.playerSelectable())
							mob.session().print(thisRace.name()+" ");
					}
					Race newRace=null;
					while(newRace==null)
					{
						String raceStr=mob.session().prompt("\n\r: ","");
						newRace=MUD.getRace(raceStr);
						if(newRace==null)
							for(int r=0;r<MUD.races.size();r++)
								if(((Race)MUD.races.elementAt(r)).name().equalsIgnoreCase(raceStr))
								{
									newRace=(Race)MUD.races.elementAt(r);
									break;
								}
						if(newRace==null)
							for(int r=0;r<MUD.races.size();r++)
								if(((Race)MUD.races.elementAt(r)).name().toUpperCase().startsWith(raceStr.toUpperCase()))
								{
									newRace=(Race)MUD.races.elementAt(r);
									break;
								}
						if(newRace!=null)
							if(!mob.session().confirm("Is "+newRace.name()+" correct (Y/n)?","Y"))
								newRace=null;
					}
					mob.baseCharStats().setMyRace(newRace);
					
					String Gender="";
					while(Gender.length()==0)
						Gender=mob.session().choose("\n\rWhat is your gender (M/F)?","MF","");
					
					mob.baseCharStats().setGender(Gender.toUpperCase().charAt(0));
					
					
					mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("stats.txt").toString());
					
					boolean mayCont=true;
					int maxStat[]={18,18,18,18,18,18};
					while(mayCont)
					{
						mob.baseCharStats().reRoll();
						mob.recoverCharStats();
						Vector V=classQualifies(mob);
						if(V.size()>1)
						{
							StringBuffer classes=new StringBuffer("");
							for(int v=0;v<V.size();v++)
								if(v==V.size()-1)
									classes.append("and "+((CharClass)V.elementAt(v)).name());
								else
									classes.append(((CharClass)V.elementAt(v)).name()+", ");
							
							mob.session().println("Your current stats are: \n\r"+mob.baseCharStats().getStats(maxStat));
							mob.session().println("This would qualify you for "+classes.toString()+".");
							if(!mob.session().confirm("Would you like to re-roll (y/N)?","N"))
								mayCont=false;
						}
					}
					
					
					mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("classes.txt").toString());
					
					mob.session().print("\n\rPlease choose from the following Classes: ");
					for(int c=0;c<MUD.charClasses.size();c++)
					{
						CharClass thisClass=(CharClass)MUD.charClasses.elementAt(c);
						if((thisClass.playerSelectable())&&thisClass.qualifiesForThisClass(mob))
							mob.session().print(thisClass.name()+" ");
					}
					CharClass newClass=null;
					while(newClass==null)
					{
						String ClassStr=mob.session().prompt("\n\r: ","");
						newClass=MUD.getCharClass(ClassStr);
						if(newClass==null)
						for(int c=0;c<MUD.charClasses.size();c++)
						{
							CharClass thisClass=(CharClass)MUD.charClasses.elementAt(c);
							if((thisClass.playerSelectable())&&thisClass.qualifiesForThisClass(mob))
								if(thisClass.name().equalsIgnoreCase(ClassStr))
								{
									newClass=thisClass;
									break;
								}
						}
						if(newClass==null)
						for(int c=0;c<MUD.charClasses.size();c++)
						{
							CharClass thisClass=(CharClass)MUD.charClasses.elementAt(c);
							if((thisClass.playerSelectable())&&thisClass.qualifiesForThisClass(mob))
								if(thisClass.name().toUpperCase().startsWith(ClassStr.toUpperCase()))
								{
									newClass=thisClass;
									break;
								}
						}
						if(newClass!=null)
							if(!mob.session().confirm("Is "+newClass.name()+" correct (Y/n)?","Y"))
								newClass=null;
					}
					mob.baseCharStats().setMyClass(newClass);
					
					mob.baseEnvStats().setLevel(1);
					mob.baseEnvStats().setSensesMask(0);
					
					
					mob.maxState().setHitPoints(20);
					mob.maxState().setMovement(100);
					mob.maxState().setMana(100);
					
					mob.setStartRoom(MUD.getRoom("Start"));
					Ration r=new Ration();
					Wineskin w=new Wineskin();
					mob.addInventory(r);
					mob.addInventory(w);
					
					mob.baseCharStats().getMyRace().newCharacter(mob);
					
					mob.recoverCharStats();
					mob.recoverEnvStats();
					mob.recoverMaxState();
					
					mob.baseCharStats().getMyClass().newCharacter(mob);
					
					mob.session().println(null,null,"\n\r\n\r"+Resources.getFileResource("alignment.txt").toString());
					
					String alignment="";
					while(alignment.length()==0)
						alignment=mob.session().choose("Select a starting alignment:\n\r Good, Evil, or Neutral (G/N/E): ","GNE","");
					switch(alignment.charAt(0))
					{
					case 'G':
						mob.setAlignment(1000);
						break;
					case 'E':
						mob.setAlignment(0);
						break;
					case 'N':
					default: 
						mob.setAlignment(500);
						break;
					}
					
					mob.bringToLife(mob.location());
					MOBloader.DBCreateCharacter(mob);
					if(MOBloader.MOBs.get(mob.ID())==null)
						MOBloader.MOBs.put(mob.ID(),mob);
					
					Log.sysOut("FrontDoor","Created user: "+mob.name());
					return true;
				}
				return false;
			}
			mob.session().println("\n\r");
			return true;
		}
		catch(java.io.IOException e)
		{
			throw e;
		}
		catch(Exception t)
		{
			Log.errOut("FrontDoor",t);
			return false;
		}
	}
}
