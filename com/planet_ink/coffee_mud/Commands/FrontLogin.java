package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.exceptions.HTTPRedirectException;
import java.util.*;
import java.io.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class FrontLogin extends StdCommand
{
	public FrontLogin(){}
	public Hashtable pendingLogins=new Hashtable();

	private static boolean classOkForMe(MOB mob, CharClass thisClass, int theme)
	{
		if((CommonStrings.isTheme(thisClass.availabilityCode()))
		   &&(Util.bset(thisClass.availabilityCode(),theme))
		   &&((CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("NO"))
			  ||(CommonStrings.getVar(CommonStrings.SYSTEM_MULTICLASS).startsWith("MULTI"))
			  ||(thisClass.baseClass().equals(thisClass.ID())
			  ||(thisClass.ID().equals("Apprentice"))))
		   &&thisClass.qualifiesForThisClass(mob,true))
			return true;
		return false;
	}

	private static Vector classQualifies(MOB mob, int theme)
	{
		Vector them=new Vector();
		for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=(CharClass)c.nextElement();
			if(classOkForMe(mob,C,theme))
				them.addElement(C);
		}
		return them;
	}

	private static boolean bannedName(String login)
	{
		Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			String str=(String)banned.elementAt(b);
			if(str.length()>0)
			{
				if(str.equals("*")||((str.indexOf("*")<0))&&(str.equals(login))) return true;
				else
				if(str.startsWith("*")&&str.endsWith("*")&&(login.indexOf(str.substring(1,str.length()-1))>=0)) return true;
				else
				if(str.startsWith("*")&&(login.endsWith(str.substring(1)))) return true;
				else
				if(str.endsWith("*")&&(login.startsWith(str.substring(0,str.length()-1)))) return true;
			}
		}
		return false;
	}

	private static boolean isOkName(String login)
	{
        if(login.length()>20) return false;
        if(login.length()<3) return false;

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
			if((" YOU SHIT FUCK CUNT ALL FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP GOD JESUS CHRIST MESSIAH ADMIN SYSOP ").indexOf(" "+str+" ")>=0)
				return false;
		}
		Vector V2=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_BADNAMES),true);
		for(int v2=0;v2<V2.size();v2++)
		{
			String str2=(String)V2.elementAt(v2);
			if(str2.length()>0)
			for(int v=0;v<V.size();v++)
			{
				String str=(String)V.elementAt(v);
				if((str.length()>0)
				&&(str.equalsIgnoreCase(str2)))
					return false;
			}
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
			if((EnglishParser.containsString(D.ID(),login))
			||(EnglishParser.containsString(D.Name(),login)))
				return false;
		}
		for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
		{
			MOB M=(MOB)m.nextElement();
			if((EnglishParser.containsString(M.Name(),login))
			||(EnglishParser.containsString(M.name(),login)))
				return false;
		}

		for(Enumeration e=Clans.clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			if((EnglishParser.containsString(C.ID(),login))
			||(EnglishParser.containsString(C.name(),login)))
				return false;
		}

		for(Enumeration e=CMMap.players();e.hasMoreElements();)
		{
			MOB tm=(MOB)e.nextElement();
			if((EnglishParser.containsString(tm.ID(),login))
			||(EnglishParser.containsString(tm.Name(),login)))
				return false;

		}
		for(int c=0;c<login.length();c++)
		{
			char C=Character.toUpperCase(login.charAt(c));
			if(("ABCDEFGHIJKLMNOPQRSTUVWXYZ ").indexOf(C)<0)
				return false;
		}
		return !bannedName(login);
	}

	public void showTheNews(MOB mob)
	{
		if(mob.session()!=null)	
		{
		    mob.session().initTermID(mob.getBitmap());
		    if(Util.bset(mob.getBitmap(),MOB.ATT_MXP))
		    {
		        if(mob.session().supports(Session.TERM_MXP))
		        {
		            mob.session().setTermID(mob.session().getTermID()|Session.TERM_MXP);
					StringBuffer mxpText=Resources.getFileResource("text"+File.separatorChar+"mxp.txt");
			        if(mxpText!=null)
			            mob.session().rawPrintln("\033[6z"+mxpText.toString()+"\n\r");
		        }
		        else
		        {
			        mob.tell("MXP codes have been disabled for this session.");
			        mob.session().setTermID(Util.unsetb(mob.session().getTermID(),Session.TERM_MXP));
		        }
		    }
		}
		if((mob.session()==null)
		||(mob.isMonster())
		||(Util.bset(mob.getBitmap(),MOB.ATT_DAILYMESSAGE)))
			return;
		
		Command C=CMClass.getCommand("MOTD");
		try{ C.execute(mob,Util.parse("MOTD NEW"));}catch(Exception e){}
	}

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob==null)
			return false;
		if(mob.session()==null)
			return false;

		String login=mob.session().prompt("name:^<USER^>");
		if(login==null) return false;
		login=login.trim();
		if(login.length()==0) return false;

		boolean found=CMClass.DBEngine().DBUserSearch(mob,login);
		if(found)
		{
			mob.session().print("password:^<PASSWORD^>");
			String password=mob.session().blockingIn();
			PlayerStats pstats=mob.playerStats();
			
			if((pstats!=null)
			&&(pstats.password().equalsIgnoreCase(password))
			&&(mob.Name().trim().length()>0))
			{
				if(bannedName(mob.Name()))
				{
					mob.tell("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
					mob.session().setKillFlag(true);
					if(pendingLogins.containsKey(mob.Name().toUpperCase()))
					   pendingLogins.remove(mob.Name().toUpperCase());
					return false;
				}
				if(((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
				   &&(!CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION")))
				{
					Command C=CMClass.getCommand("Email");
					if(C!=null)
					{
						if(!C.execute(mob,null))
							return false;
					}
					CMClass.DBEngine().DBUpdateEmail(mob);
				}
				
				Long L=(Long)pendingLogins.get(mob.Name().toUpperCase());
				if((L!=null)&&((System.currentTimeMillis()-L.longValue())<(10*60*1000)))
				{
					mob.session().println("A previous login is still pending.  Please be patient.");
					return false;
				}
				if(pendingLogins.containsKey(mob.Name().toUpperCase()))
				   pendingLogins.remove(mob.Name().toUpperCase());
				pendingLogins.put(mob.Name().toUpperCase(),new Long(System.currentTimeMillis()));
				
				for(int s=0;s<Sessions.size();s++)
				{
					Session thisSession=Sessions.elementAt(s);
					if((thisSession.mob()!=null)&&(thisSession!=mob.session()))
					{
						if((thisSession.mob().Name().equals(mob.Name())))
						{
							Room oldRoom=thisSession.mob().location();
							if(oldRoom!=null)
							while(oldRoom.isInhabitant(thisSession.mob()))
								oldRoom.delInhabitant(thisSession.mob());
							mob.session().setMob(thisSession.mob());
							thisSession.mob().setSession(mob.session());
							thisSession.setMob(null);
							thisSession.setKillFlag(true);
							Log.sysOut("FrontDoor","Session swap for "+mob.session().mob().Name()+".");
							mob.session().mob().bringToLife(oldRoom,false);
							if(pendingLogins.containsKey(mob.Name().toUpperCase()))
							   pendingLogins.remove(mob.Name().toUpperCase());
							return true;
						}
					}
				}
				
				MOB oldMOB=mob;
				if(CMMap.getPlayer(oldMOB.Name())!=null)
				{
					oldMOB.session().setMob(CMMap.getPlayer(oldMOB.Name()));
					mob=oldMOB.session().mob();
					mob.setSession(oldMOB.session());
					if(mob!=oldMOB)
						oldMOB.setSession(null);
					showTheNews(mob);
					mob.bringToLife(mob.location(),false);
					CoffeeTables.bump(mob,CoffeeTables.STAT_LOGINS);
					mob.location().showOthers(mob,mob.location(),CMMsg.MASK_GENERAL|CMMsg.MSG_ENTER,"<S-NAME> appears!");
					for(int f=0;f<mob.numFollowers();f++)
					{
						MOB follower=mob.fetchFollower(f);
						Room R=follower.location();
						if((follower!=null)
						&&(follower.isMonster())
						&&((R==null)||(!R.isInhabitant(follower))))
						{
							follower.setLocation(mob.location());
							follower.bringToLife(mob.location(),false);
							follower.setFollowing(mob);
							follower.location().showOthers(follower,mob.location(),CMMsg.MASK_GENERAL|CMMsg.MSG_ENTER,"<S-NAME> appears!");
						}
					}
				}
				else
				{
					CMClass.DBEngine().DBReadMOB(mob);
					showTheNews(mob);
					mob.bringToLife(mob.location(),true);
					CoffeeTables.bump(mob,CoffeeTables.STAT_LOGINS);
					mob.location().showOthers(mob,mob.location(),CMMsg.MASK_GENERAL|CMMsg.MSG_ENTER,"<S-NAME> appears!");
					CMClass.DBEngine().DBReadFollowers(mob,true);
				}
				if((mob.session()!=null)&&(mob.playerStats()!=null))
					mob.playerStats().setLastIP(mob.session().getAddress());
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S!=null)
					&&(S.mob()!=null)
					&&(S.mob()!=mob)
					&&((!Sense.isCloaked(mob))||(CMSecurity.isASysOp(S.mob())))
					&&(Util.bset(S.mob().getBitmap(),MOB.ATT_AUTONOTIFY))
					&&(S.mob().playerStats()!=null)
					&&((S.mob().playerStats().getFriends().contains(mob.Name())||S.mob().playerStats().getFriends().contains("All"))))
						S.mob().tell("^X"+mob.Name()+" has logged on.^.^?");
				}
				if((CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ALWAYS"))
				&&(!Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
					mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
				if((CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("NEVER"))
				&&(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
					mob.setBitmap(mob.getBitmap()-MOB.ATT_PLAYERKILL);
				CommonMsgs.channel("WIZINFO","",mob.Name()+" has logged on.",true);
				if(pendingLogins.containsKey(mob.Name().toUpperCase()))
				   pendingLogins.remove(mob.Name().toUpperCase());
			}
			else
			{
				Log.sysOut("FrontDoor","Failed login: "+mob.Name());
				mob.setName("");
				mob.setPlayerStats(null);
				mob.session().println("\n\rInvalid password.\n\r");
				if(pendingLogins.containsKey(mob.Name().toUpperCase()))
				   pendingLogins.remove(mob.Name().toUpperCase());
				return false;
			}
		}
		else
		{
			if(!isOkName(login))
			{
				mob.session().println("\n\rThat name is unrecognized.\n\rThat name is also not available for new users.\n\r  Choose another name (no spaces allowed)!\n\r");
				mob.setName("");
				mob.setPlayerStats(null);
			}
			else
			if(CommonStrings.getIntVar(CommonStrings.SYSTEMI_MUDTHEME)==0)
			{
				mob.session().print("\n\r'"+Util.capitalize(login)+"' does not exist.\n\rThis server is not accepting new accounts.\n\r\n\r");
				mob.setName("");
				mob.setPlayerStats(null);
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

				mob.setName(login);
				mob.setPlayerStats(new DefaultPlayerStats());
				mob.playerStats().setPassword(password);

				boolean emailReq=(!CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"));
				while(true)
				{
					String newEmail=mob.session().prompt("\n\rEnter your e-mail address:");
					String confirmEmail=newEmail;
					if(emailReq) confirmEmail=mob.session().prompt("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter:");
					if(((newEmail.length()>6)&&(newEmail.indexOf("@")>0)&&((newEmail.equalsIgnoreCase(confirmEmail))))
					   ||(!emailReq))
					{
						mob.playerStats().setEmail(newEmail);
						break;
					}
					mob.session().println("\n\rThat email address combination was invalid.\n\r");
				}
				Log.sysOut("FrontDoor","Creating user: "+mob.Name());

				mob.setBitmap(MOB.ATT_AUTOEXITS);
				if(mob.session().confirm("\n\rDo you want ANSI colors (Y/n)?","Y"))
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_ANSI));
				
				int themeCode=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MUDTHEME);
				int theme=Area.THEME_FANTASY;
				switch(themeCode)
				{
					case Area.THEME_FANTASY:
					case Area.THEME_HEROIC:
					case Area.THEME_TECHNOLOGY:
					    theme=themeCode;
						break;
					default:
					    theme=-1;
				        String choices="";
				        String selections="";
						if(Util.bset(themeCode,Area.THEME_FANTASY)){ choices+="F"; selections+="/F";}
						if(Util.bset(themeCode,Area.THEME_HEROIC)){ choices+="H"; selections+="/H";}
						if(Util.bset(themeCode,Area.THEME_TECHNOLOGY)){ choices+="T"; selections+="/T";}
						if(choices.length()==0)
						{
						    choices="F"; 
						    selections="/F";
						}
					    while((theme<0)&&(!mob.session().killFlag()))
					    {
							mob.session().println(null,null,null,Resources.getFileResource("text"+File.separatorChar+"themes.txt").toString());
							mob.session().print("\n\r^!Please select from the following:^N "+selections.substring(1)+"\n\r");
							String themeStr=mob.session().choose("\n\r: ",choices,"");
							if(themeStr.toUpperCase().startsWith("F"))
							    theme=Area.THEME_FANTASY;
							if(themeStr.toUpperCase().startsWith("H"))
							    theme=Area.THEME_HEROIC;
							if(themeStr.toUpperCase().startsWith("T"))
							    theme=Area.THEME_TECHNOLOGY;
					    }
					    break;
				}
				if(!CMSecurity.isDisabled("RACES"))
					mob.session().println(null,null,null,Resources.getFileResource("text"+File.separatorChar+"races.txt").toString());

				StringBuffer listOfRaces=new StringBuffer("[");
				boolean tmpFirst = true;
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
					if((CommonStrings.isTheme(R.availabilityCode()))
					&&(!Util.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK))
					&&(Util.bset(R.availabilityCode(),theme)))
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
				if(CMSecurity.isDisabled("RACES"))
				{
					newRace=CMClass.getRace("PlayerRace");
					if(newRace==null)
					    newRace=CMClass.getRace("StdRace");
				}
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
						if((newRace!=null)&&((!CommonStrings.isTheme(newRace.availabilityCode()))
												||(!Util.bset(newRace.availabilityCode(),theme))
						        				||(Util.bset(newRace.availabilityCode(),Area.THEME_SKILLONLYMASK))))
							newRace=null;
						if(newRace==null)
							for(Enumeration r=CMClass.races();r.hasMoreElements();)
							{
								Race R=(Race)r.nextElement();
								if((R.name().equalsIgnoreCase(raceStr))
								&&(CommonStrings.isTheme(newRace.availabilityCode()))
								&&(Util.bset(R.availabilityCode(),theme))
								&&(!Util.bset(newRace.availabilityCode(),Area.THEME_SKILLONLYMASK)))
								{
									newRace=R;
									break;
								}
							}
						if(newRace==null)
							for(Enumeration r=CMClass.races();r.hasMoreElements();)
							{
								Race R=(Race)r.nextElement();
								if((R.name().toUpperCase().startsWith(raceStr.toUpperCase()))
						        &&(CommonStrings.isTheme(R.availabilityCode()))
								&&(Util.bset(R.availabilityCode(),theme))
						        &&(!Util.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK)))
								{
									newRace=R;
									break;
								}
							}
						if(newRace!=null)
						{
							StringBuffer str=MUDHelp.getHelpText(newRace.ID().toUpperCase(),mob);
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

				mob.baseCharStats().setStat(CharStats.GENDER,Gender.toUpperCase().charAt(0));
				mob.baseCharStats().getMyRace().startRacing(mob,false);

				mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource("text"+File.separatorChar+"stats.txt").toString());

				boolean mayCont=true;
				StringBuffer listOfClasses=new StringBuffer("??? no classes ???");
				while(mayCont)
				{
					mob.baseCharStats().getMyRace().reRoll(mob,mob.baseCharStats());
					mob.recoverCharStats();
					Vector V=classQualifies(mob,theme);
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
											  +mob.charStats().getStats()
											  +Util.padRight("TOTAL POINTS",15)+": "
											  +CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXSTAT)+"/"+(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)*6));

						if(!CMSecurity.isDisabled("CLASSES"))
							mob.session().println("\n\rThis would qualify you for ^H"+classes.toString()+"^N.");

						if(!mob.session().confirm("^!Would you like to re-roll (y/N)?^N","N"))
							mayCont=false;
					}
				}
				if(!CMSecurity.isDisabled("CLASSES"))
					mob.session().println(null,null,null,Resources.getFileResource("text"+File.separatorChar+"classes.txt").toString());

				CharClass newClass=null;
				Vector qualClasses=classQualifies(mob,theme);
				if(CMSecurity.isDisabled("CLASSES"))
				{
				    newClass=CMClass.getCharClass("PlayerClass");
					if(newClass==null)
					    newClass=CMClass.getCharClass("StdCharClass");
				}
				else
				if(qualClasses.size()==0)
				{
					newClass=CMClass.getCharClass("Apprentice");
					if(newClass==null) newClass=CMClass.getCharClass("StdCharClass");
				}
				else
				if(qualClasses.size()==1)
					newClass=(CharClass)qualClasses.firstElement();
				else
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
						for(Enumeration c=qualClasses.elements();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(C.name().equalsIgnoreCase(ClassStr))
							{
								newClass=C;
								break;
							}
						}
						if(newClass==null)
						for(Enumeration c=qualClasses.elements();c.hasMoreElements();)
						{
							CharClass C=(CharClass)c.nextElement();
							if(C.name().toUpperCase().startsWith(ClassStr.toUpperCase()))
							{
								newClass=C;
								break;
							}
						}
						if((newClass!=null)&&(classOkForMe(mob,newClass,theme)))
						{
							StringBuffer str=MUDHelp.getHelpText(newClass.ID().toUpperCase(),mob);
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

				Item r=CMClass.getItem("Ration");
				Item w=CMClass.getItem("Waterskin");
				Item t=CMClass.getItem("Torch");
				mob.addInventory(r);
				mob.addInventory(w);
				mob.addInventory(t);
				mob.setWimpHitPoint(5);

				CoffeeUtensils.outfit(mob,mob.baseCharStats().getMyRace().outfit());

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
				CoffeeUtensils.outfit(mob,mob.baseCharStats().getCurrentClass().outfit());
				mob.setStartRoom(CMMap.getStartRoom(mob));
			    mob.baseCharStats().setStat(CharStats.AGE,mob.playerStats().initializeBirthday(0,mob.baseCharStats().getMyRace()));
				mob.bringToLife(mob.getStartRoom(),true);
				mob.location().showOthers(mob,mob.location(),CMMsg.MASK_GENERAL|CMMsg.MSG_ENTER,"<S-NAME> appears!");
				CMClass.DBEngine().DBCreateCharacter(mob);
				if(CMMap.getPlayer(mob.Name())==null)
					CMMap.addPlayer(mob);

				mob.playerStats().setLastIP(mob.session().getAddress());
				Log.sysOut("FrontDoor","Created user: "+mob.Name());
				for(int s=0;s<Sessions.size();s++)
				{
					Session S=Sessions.elementAt(s);
					if((S!=null)
					&&(S.mob()!=null)
					&&((!Sense.isCloaked(mob))||(CMSecurity.isASysOp(S.mob())))
					&&(Util.bset(S.mob().getBitmap(),MOB.ATT_AUTONOTIFY))
					&&(S.mob().playerStats()!=null)
					&&((S.mob().playerStats().getFriends().contains(mob.Name())||S.mob().playerStats().getFriends().contains("All"))))
						S.mob().tell("^X"+mob.Name()+" has just been created.^.^?");
				}
				if((CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("ALWAYS"))
				&&(!Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
					mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
				if((CommonStrings.getVar(CommonStrings.SYSTEM_PKILL).startsWith("NEVER"))
				&&(Util.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
					mob.setBitmap(mob.getBitmap()-MOB.ATT_PLAYERKILL);
				CMClass.DBEngine().DBUpdateMOB(mob);
				CommonMsgs.channel("WIZINFO","",mob.Name()+" has just been created.",true);
				CoffeeTables.bump(mob,CoffeeTables.STAT_LOGINS);
				CoffeeTables.bump(mob,CoffeeTables.STAT_NEWPLAYERS);
				if(pendingLogins.containsKey(mob.Name().toUpperCase()))
				   pendingLogins.remove(mob.Name().toUpperCase());
				return true;
			}
			if(pendingLogins.containsKey(mob.Name().toUpperCase()))
			   pendingLogins.remove(mob.Name().toUpperCase());
			return false;
		}
		if((mob!=null)&&(mob.session()!=null))
			mob.session().println("\n\r");
		if(pendingLogins.containsKey(mob.Name().toUpperCase()))
		   pendingLogins.remove(mob.Name().toUpperCase());
		return true;
	}
	public int ticksToExecute(){return 0;}
	public boolean canBeOrdered(){return false;}

	public int compareTo(Object o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
}
