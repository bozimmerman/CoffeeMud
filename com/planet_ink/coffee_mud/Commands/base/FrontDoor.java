package com.planet_ink.coffee_mud.Commands.base;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.io.*;
import java.util.*;
public class FrontDoor
{
	private FrontDoor(){}
	
	private static boolean classOkForMe(MOB mob, CharClass thisClass)
	{
		if((thisClass.playerSelectable())
		   &&((CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
			  ||(CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("MULTI"))
			  ||(thisClass.baseClass().equals(thisClass.ID())))
		   &&thisClass.qualifiesForThisClass(mob,true))
			return true;
		return false;
	}
	
	private static Vector classQualifies(MOB mob)
	{
		Vector them=new Vector();
		for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=(CharClass)c.nextElement();
			if(classOkForMe(mob,C))
				them.addElement(C);
		}
		return them;
	}

	private static boolean isOkName(String login)
	{
		if(login.length()>20) return false;
		if(login.trim().indexOf(" ")>=0) return false;
		
		login=login.toUpperCase().trim();
		Vector V=Util.parse(login);
		for(int v=V.size()-1;v>=0;v--)
		{
			String str=(String)V.elementAt(v);
			if((" THE A AN ").indexOf(" "+str+" ")>=0)
				V.removeElementAt(v);
		}
		for(int v=0;v<V.size();v++)
		{
			String str=(String)V.elementAt(v);
			if((" YOU SHIT FUCK CUNT FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP ADMIN SYSOP ").indexOf(" "+str+" ")>=0)
				return false;
		}
		for(int c=0;c<login.length();c++)
		{
			char C=Character.toUpperCase(login.charAt(c));
			if(("ABCDEFGHIJKLMNOPQRSTUVWXYZ ").indexOf(C)<0)
				return false;
		}
		for(Enumeration d=CMMap.deities();d.hasMoreElements();)
		{
			MOB D=(MOB)d.nextElement();
			if((CoffeeUtensils.containsString(D.ID(),login))
			||(CoffeeUtensils.containsString(D.name(),login)))
				return false;
		}
		for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
		{
			MOB M=(MOB)m.nextElement();
			if((CoffeeUtensils.containsString(M.ID(),login))
			||(CoffeeUtensils.containsString(M.displayName(),login)))
				return false;
		}
		for(Enumeration e=CMMap.players();e.hasMoreElements();)
		{
			MOB tm=(MOB)e.nextElement();
			if((CoffeeUtensils.containsString(tm.ID(),login))
			||(CoffeeUtensils.containsString(tm.name(),login)))
				return false;

		}
		for(int c=0;c<login.length();c++)
		{
			char C=Character.toUpperCase(login.charAt(c));
			if(("ABCDEFGHIJKLMNOPQRSTUVWXYZ ").indexOf(C)<0)
				return false;
		}
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini"));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String str=(String)banned.elementAt(b);
			if(str.length()>0)
			{
				if(str.equals("*")||((str.indexOf("*")<0))&&(str.equals(login))) return false;
				else
				if(str.startsWith("*")&&str.endsWith("*")&&(login.indexOf(str.substring(1,str.length()-1))>=0)) return false;
				else
				if(str.startsWith("*")&&(login.endsWith(str.substring(1)))) return false;
				else
				if(str.endsWith("*")&&(login.startsWith(str.substring(0,str.length()-1)))) return false;
			}
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

		String login=mob.session().prompt("name:");
		if(login==null) return false;
		login=login.trim();
		if(login.length()==0) return false;

		boolean found=ExternalPlay.DBUserSearch(mob,login);
		if(found)
		{
			mob.session().print("password:");
			String password=mob.session().blockingIn();
			if((mob.password().equalsIgnoreCase(password))&&(mob.name().trim().length()>0))
			{
				if(((mob.getEmail()==null)||(mob.getEmail().length()==0))
				   &&(!CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION")))
				{
					if(!Scoring.email(mob,null,true)) 
						return false;
					ExternalPlay.DBUpdateEmail(mob);
				}

				boolean swapMade=false;
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=(Session)Sessions.elementAt(s);
					if((thisSession.mob()!=null)&&(thisSession!=mob.session()))
					{
						if((thisSession.mob().ID().equals(mob.ID())))
						{
							swapMade=true;
							Room oldRoom=thisSession.mob().location();
							if(oldRoom!=null)
							while(oldRoom.isInhabitant(thisSession.mob()))
								oldRoom.delInhabitant(thisSession.mob());
							mob.session().setMob(thisSession.mob());
							thisSession.mob().setSession(mob.session());
							thisSession.setMob(null);
							thisSession.setKillFlag(true);
							Log.sysOut("FrontDoor","Session swap for "+mob.session().mob().name()+".");
							mob.session().mob().bringToLife(oldRoom,false);
							return true;
						}
					}
				}
				MOB oldMOB=mob;
				if(CMMap.getPlayer(oldMOB.ID())!=null)
				{
					oldMOB.session().setMob((MOB)CMMap.getPlayer(oldMOB.ID()));
					mob=oldMOB.session().mob();
					mob.setSession(oldMOB.session());
					if(mob!=oldMOB)
						oldMOB.setSession(null);
					showTheNews(mob);
					mob.bringToLife(mob.location(),false);
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
					for(int f=0;f<mob.numFollowers();f++)
					{
						MOB follower=mob.fetchFollower(f);
						if(follower!=null)
						{
							follower.setLocation(mob.location());
							follower.bringToLife(mob.location(),false);
							follower.setFollowing(mob);
							follower.location().showOthers(follower,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
						}
					}
				}
				else
				{
					ExternalPlay.DBReadMOB(mob);
					mob.setUserInfo(mob.ID(),password);
					if(mob.baseCharStats()!=null)
					{
						mob.baseCharStats().getCurrentClass().startCharacter(mob,false,true);
						mob.baseCharStats().getMyRace().startRacing(mob,true);
					}
					showTheNews(mob);
					mob.bringToLife(mob.location(),true);
					mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
					ExternalPlay.DBReadFollowers(mob);
				}
				ExternalPlay.DBUpdateIP(mob);
			}
			else
			{
				Log.sysOut("FrontDoor","Failed login: "+mob.name());
				mob.setUserInfo("","");
				mob.session().println("\n\rInvalid password.\n\r");
				return false;
			}
		}
		else
		{
			if(!isOkName(login))
			{
				mob.session().println("\n\rThat name is unrecognized.\n\rThat name is also not available for new users.\n\r  Choose another name (no spaces allowed)!\n\r");
				mob.setUserInfo("","");
			}
			else
			if(mob.session().confirm("\n\r'"+Util.capitalize(login)+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
			{
				login=Util.capitalize(login.trim());
				mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource("text"+File.separatorChar+"newchar.txt").toString());

				String password="";
				while(password.length()==0)
				{
					password=mob.session().prompt("\n\rEnter a password: ","");
					if(password.length()==0)
						mob.session().println("\n\rYou must enter a password to continue.");
				}
				
				boolean emailReq=(!CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"));
				while(true)
				{
					String newEmail=mob.session().prompt("\n\rEnter your e-mail address:");
					String confirmEmail=newEmail;
					if(emailReq) confirmEmail=mob.session().prompt("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter:");
					if(((newEmail.length()>6)&&(newEmail.indexOf("@")>0)&&((newEmail.equalsIgnoreCase(confirmEmail))))
					   ||(!emailReq))
					{
						mob.setEmail(newEmail);
						break;
					}
					mob.session().println("\n\rThat email address combination was invalid.\n\r");
				}
				mob.setUserInfo(login,password);
				Log.sysOut("FrontDoor","Creating user: "+mob.name());

				mob.setBitmap(0);
				if(mob.session().confirm("\n\rDo want ANSI colors (Y/n)?","Y"))
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_ANSI));

				mob.session().println(null,null,null,Resources.getFileResource("text"+File.separatorChar+"races.txt").toString());

				StringBuffer listOfRaces=new StringBuffer("[");
				boolean tmpFirst = true;
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					if(R.playerSelectable())
					{
						if (!tmpFirst)
							listOfRaces.append(", ");
						else
							tmpFirst = false;
						listOfRaces.append("^H"+R.name()+"^N");
					}
				}
				listOfRaces.append("]");

				Race newRace=null;
				while(newRace==null)
				{
					mob.session().print("\n\r^!Please choose from the following races (?):^N\n\r");
					mob.session().print(listOfRaces.toString());
					String raceStr=mob.session().prompt("\n\r: ","");
					if(raceStr.trim().equalsIgnoreCase("?"))
						mob.session().println(null,null,null,"\n\r"+Resources.getFileResource("text"+File.separatorChar+"races.txt").toString());
					else
					{
						newRace=CMClass.getRace(raceStr);
						if((newRace!=null)&&(!newRace.playerSelectable()))
							newRace=null;
						if(newRace==null)
							for(Enumeration r=CMClass.races();r.hasMoreElements();)
							{
								Race R=(Race)r.nextElement();
								if((R.name().equalsIgnoreCase(raceStr))
								&&(R.playerSelectable()))
								{
									newRace=(Race)R;
									break;
								}
							}
						if(newRace==null)
							for(Enumeration r=CMClass.races();r.hasMoreElements();)
							{
								Race R=(Race)r.nextElement();
								if((R.name().toUpperCase().startsWith(raceStr.toUpperCase()))
								&&(R.playerSelectable()))
								{
									newRace=(Race)R;
									break;
								}
							}
						if(newRace!=null)
						{
							StringBuffer str=ExternalPlay.getHelpText(newRace.ID().toUpperCase());
							if(str!=null) mob.tell("\n\r^N"+str.toString()+"\n\r");
							if(!mob.session().confirm("^!Is ^H"+newRace.name()+"^N^! correct (Y/n)?^N","Y"))
								newRace=null;
						}
					}
				}
				mob.baseCharStats().setMyRace(newRace);

				String Gender="";
				while(Gender.length()==0)
					Gender=mob.session().choose("\n\r^!What is your gender (M/F)?^N","MF","");

				mob.baseCharStats().setStat(CharStats.GENDER,(int)Gender.toUpperCase().charAt(0));
				mob.baseCharStats().getMyRace().startRacing(mob,false);

				mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource("text"+File.separatorChar+"stats.txt").toString());

				boolean mayCont=true;
				int maxStat[]={18,18,18,18,18,18};
				StringBuffer listOfClasses=new StringBuffer("??? no classes ???");
				while(mayCont)
				{
					mob.baseCharStats().getMyRace().reRoll(mob,mob.baseCharStats());
					mob.recoverCharStats();
					Vector V=classQualifies(mob);
					if(V.size()>1)
					{
						StringBuffer classes=new StringBuffer("");
						listOfClasses = new StringBuffer("");
						for(int v=0;v<V.size();v++)
							if(v==V.size()-1)
							{
								if (v != 0)
								{
									classes.append("^?and ^?");
									listOfClasses.append("^?or ^?");
								}
								classes.append(((CharClass)V.elementAt(v)).name());
								listOfClasses.append(((CharClass)V.elementAt(v)).name());
							}
							else
							{
								classes.append(((CharClass)V.elementAt(v)).name()+"^?, ^?");
								listOfClasses.append(((CharClass)V.elementAt(v)).name()+"^?, ^?");
							}

						mob.session().println("Your current stats are: \n\r"
											  +mob.charStats().getStats(maxStat)
											  +Util.padRight("TOTAL POINTS",15)+": "
											  +CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXSTAT)+"/"+(18*6));
						
						mob.session().println("\n\rThis would qualify you for ^H"+classes.toString()+"^N.");

						if(!mob.session().confirm("^!Would you like to re-roll (y/N)?^N","N"))
							mayCont=false;
					}
				}
				mob.session().println(null,null,null,Resources.getFileResource("text"+File.separatorChar+"classes.txt").toString());

				CharClass newClass=null;
				while(newClass==null)
				{
					mob.session().print("\n\r^!Please choose from the following Classes:\n\r");
					mob.session().print("^H[" + listOfClasses.toString() + "]^N");
					String ClassStr=mob.session().prompt("\n\r: ","");
					if(ClassStr.trim().equalsIgnoreCase("?"))
						mob.session().println(null,null,null,"\n\r"+Resources.getFileResource("text"+File.separatorChar+"classes.txt").toString());
					else
					{
						newClass=CMClass.getCharClass(ClassStr);
						if(newClass==null)
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(classOkForMe(mob,C))
								if(C.name().equalsIgnoreCase(ClassStr))
								{
									newClass=C;
									break;
								}
						}
						if(newClass==null)
						for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(classOkForMe(mob,C))
								if(C.name().toUpperCase().startsWith(ClassStr.toUpperCase()))
								{
									newClass=C;
									break;
								}
						}
						if((newClass!=null)&&(classOkForMe(mob,newClass)))
						{
							StringBuffer str=ExternalPlay.getHelpText(newClass.ID().toUpperCase());
							if(str!=null) mob.tell("\n\r^N"+str.toString()+"\n\r");
							if(!mob.session().confirm("^NIs ^H"+newClass.name()+"^N correct (Y/n)?","Y"))
								newClass=null;
						}
						else
							newClass=null;
					}
				}
				mob.baseEnvStats().setLevel(1);
				mob.baseCharStats().setCurrentClass(newClass);
				mob.baseCharStats().setClassLevel(newClass,1);
				mob.baseEnvStats().setSensesMask(0);

				mob.baseState().setHitPoints(20);
				mob.baseState().setMovement(100);
				mob.baseState().setMana(100);

				Item r=(Item)CMClass.getItem("Ration");
				Item w=(Item)CMClass.getItem("Waterskin");
				Item t=(Item)CMClass.getItem("Torch");
				mob.addInventory(r);
				mob.addInventory(w);
				mob.addInventory(t);
				mob.setWimpHitPoint(5);

				mob.baseCharStats().getMyRace().startRacing(mob,false);
				mob.baseCharStats().getMyRace().outfit(mob);

				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.resetToMaxState();

				mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource("text"+File.separatorChar+"alignment.txt").toString());

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
				mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);
				mob.baseCharStats().getCurrentClass().outfit(mob);
				mob.setStartRoom(CMMap.getStartRoom(mob));
				mob.bringToLife(mob.getStartRoom(),true);
				mob.location().showOthers(mob,null,Affect.MSG_OK_ACTION,"<S-NAME> appears!");
				ExternalPlay.DBCreateCharacter(mob);
				if(CMMap.getPlayer(mob.ID())==null)
					CMMap.addPlayer(mob);

				Log.sysOut("FrontDoor","Created user: "+mob.name());
				return true;
			}
			return false;
		}
		mob.session().println("\n\r");
		return true;
	}
	
	public static void showTheNews(MOB mob)
	{
		StringBuffer buf=new StringBuffer("");
		Vector journal=ExternalPlay.DBReadJournal("CoffeeMud News");
		for(int which=0;which<journal.size();which++)
		{
			Vector entry=(Vector)journal.elementAt(which);
			String from=(String)entry.elementAt(1);
			long last=Util.s_long((String)entry.elementAt(2));
			String to=(String)entry.elementAt(3);
			String subject=(String)entry.elementAt(4);
			String message=(String)entry.elementAt(5);
			boolean mineAble=to.equalsIgnoreCase(mob.name())||from.equalsIgnoreCase(mob.name());
			if((last>mob.lastDateTime())
			&&(to.equals("ALL")||mineAble))
			{
				buf.append("\n\r--------------------------------------\n\r");
				buf.append("\n\rNews: "+IQCalendar.d2String(last)+"\n\r"+"FROM: "+Util.padRight(from,15)+"\n\rTO  : "+Util.padRight(to,15)+"\n\rSUBJ: "+subject+"\n\r"+message);
			}
		}
		if((!mob.isMonster())&&(buf.length()>0))
			mob.session().unfilteredPrintln(buf.toString()+"\n\r--------------------------------------\n\r");
	}
}
