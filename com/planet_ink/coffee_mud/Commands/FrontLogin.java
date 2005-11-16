package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.exceptions.HTTPRedirectException;
import java.util.*;
import java.io.*;

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
public class FrontLogin extends StdCommand
{
	public FrontLogin(){}
	public Hashtable pendingLogins=new Hashtable();

	private static boolean classOkForMe(MOB mob, CharClass thisClass, int theme)
	{
		if((CommonStrings.isTheme(thisClass.availabilityCode()))
		   &&(Util.bset(thisClass.availabilityCode(),theme))
           &&(!Util.bset(thisClass.availabilityCode(),Area.THEME_SKILLONLYMASK))
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
        HashSet doneClasses=new HashSet();
		for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=(CharClass)c.nextElement();
            if(doneClasses.contains(C.ID())) continue;
            C=CMClass.getCharClass(C.ID());
            doneClasses.add(C.ID());
			if(classOkForMe(mob,C,theme))
				them.addElement(C);
		}
		return them;
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
			if((" YOU SHIT FUCK CUNT ALL FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP GOD JESUS CHRIST NOBODY SOMEBODY MESSIAH ADMIN SYSOP ").indexOf(" "+str+" ")>=0)
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
		return !CMSecurity.isBanned(login);
	}

    public void reloadTerminal(MOB mob)
    {
        if(mob.session()!=null)
        {
            mob.session().initTelnetMode(mob.getBitmap());
            if(Util.bset(mob.getBitmap(),MOB.ATT_MXP))
            {
                if(mob.session().clientTelnetMode(Session.TELNET_MXP))
                {
                    StringBuffer mxpText=Resources.getFileResource("text"+File.separatorChar+"mxp.txt");
                    if(mxpText!=null)
                        mob.session().rawPrintln("\033[6z"+mxpText.toString()+"\n\r");
                }
                else
                    mob.tell("MXP codes have been disabled for this session.");
            }
            else
            if(mob.session().clientTelnetMode(Session.TELNET_MXP))
            {
                mob.session().changeTelnetMode(Session.TELNET_MXP,false);
                mob.session().setClientTelnetMode(Session.TELNET_MXP,false);
            }
            if((Util.bset(mob.getBitmap(),MOB.ATT_SOUND))
            &&(!mob.session().clientTelnetMode(Session.TELNET_MSP)))
                mob.tell("MSP sounds have been disabled for this session.");
            else
            if(mob.session().clientTelnetMode(Session.TELNET_MSP))
            {
                mob.session().changeTelnetMode(Session.TELNET_MSP,false);
                mob.session().setClientTelnetMode(Session.TELNET_MSP,false);
            }
        }
    }
    
	public void showTheNews(MOB mob)
	{
        reloadTerminal(mob);
        Command C=CMClass.getCommand("Poll");
        try{ C.execute(mob,null);}catch(Exception e){}
        
		if((mob.session()==null)
		||(mob.isMonster())
		||(Util.bset(mob.getBitmap(),MOB.ATT_DAILYMESSAGE)))
			return;

		C=CMClass.getCommand("MOTD");
		try{ C.execute(mob,Util.parse("MOTD NEW"));}catch(Exception e){}
	}

    public boolean checkExpiration(MOB mob)
    {
        if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ACCOUNTEXPIRATION)) return true;
        MOB newMob=CMMap.getLoadPlayer(mob.name());
        if(CMSecurity.isASysOp(newMob)) return true;
        if((newMob.playerStats()!=null)
        &&(newMob.playerStats().getAccountExpiration()<=System.currentTimeMillis()))
        {
            mob.tell("\n\r"+CommonStrings.getVar(CommonStrings.SYSTEM_EXPCONTACTLINE)+"\n\r\n\r");
            mob.session().setKillFlag(true);
            if(pendingLogins.containsKey(mob.Name().toUpperCase()))
               pendingLogins.remove(mob.Name().toUpperCase());
            return false;
        }
        return true;
    }

	public boolean execute(MOB mob, Vector commands)
		throws java.io.IOException
	{
		if(mob==null)
			return false;
		if(mob.session()==null)
			return false;

		int attempt=0;
		if(commands!=null)
	    for(int i=0;i<commands.size();i++)
	        if(Util.isInteger((String)commands.elementAt(i)))
			    attempt=Util.s_int((String)commands.elementAt(i));
	        else
	        if(((String)commands.elementAt(i)).equalsIgnoreCase("LAST"))
	            attempt=Integer.MAX_VALUE;
        boolean wizi=false;

		String login=mob.session().prompt("name:^<USER^>");
		if(login==null) return false;
		login=login.trim();
		if(login.length()==0) return false;
        if(login.endsWith(" !"))
        {
            login=login.substring(0,login.length()-2);
            login=login.trim();
            wizi=true;
        }
        
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
				if(CMSecurity.isBanned(mob.Name()))
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
                if((pstats.getEmail()!=null)&&CMSecurity.isBanned(pstats.getEmail()))
                {
                    mob.tell("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
                    mob.session().setKillFlag(true);
                    if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                       pendingLogins.remove(mob.Name().toUpperCase());
                    return false;
                }
                if(!checkExpiration(mob)) return false;

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
                            commands.clear();
                            commands.addElement("SWAPPED");
                            reloadTerminal(mob.session().mob());
							mob.session().mob().bringToLife(oldRoom,false);
							if(pendingLogins.containsKey(mob.Name().toUpperCase()))
							   pendingLogins.remove(mob.Name().toUpperCase());
							return true;
						}
					}
				}

                // count number of multiplays
                int numAtAddress=0;
                try{
                for(int s=0;s<Sessions.size();s++)
                {
                    if((Sessions.elementAt(s)!=mob.session())
                    &&(mob.session().getAddress().equalsIgnoreCase((Sessions.elementAt(s).getAddress()))))
                        numAtAddress++;
                }
                }catch(Exception e){}
                
                if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXCONNSPERIP)>0)
                &&(numAtAddress>=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXCONNSPERIP))
                &&(!CMSecurity.isDisabled("MAXCONNSPERIP")))
                {
                    mob.session().println("The maximum player limit has already been reached for your IP address.");
                    if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                        pendingLogins.remove(mob.Name().toUpperCase());
                    return false;
                }
                
				MOB oldMOB=mob;
				if(CMMap.getPlayer(oldMOB.Name())!=null)
				{
					oldMOB.session().setMob(CMMap.getPlayer(oldMOB.Name()));
					mob=oldMOB.session().mob();
					mob.setSession(oldMOB.session());
					if(mob!=oldMOB)
						oldMOB.setSession(null);
                    if(wizi)
                    {
                        Command C=CMClass.getCommand("WizInv");
                        if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
                            C.execute(mob,Util.makeVector("WIZINV"));
                    }
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
						&&(!follower.isPossessing())
						&&((R==null)||(!R.isInhabitant(follower))))
						{
						    if(R==null) R=mob.location();
							follower.setLocation(R);
							follower.bringToLife(R,false);
							follower.setFollowing(mob);
							follower.location().showOthers(follower,R,CMMsg.MASK_GENERAL|CMMsg.MSG_ENTER,"<S-NAME> appears!");
						}
					}
				}
				else
				{
					CMClass.DBEngine().DBReadPlayer(mob);
                    if(wizi)
                    {
                        Command C=CMClass.getCommand("WizInv");
                        if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
                            C.execute(mob,Util.makeVector("WIZINV"));
                    }
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
                Vector channels=ChannelSet.getFlaggedChannelNames("LOGINS");
                if(!Sense.isCloaked(mob))
                for(int i=0;i<channels.size();i++)
                    CommonMsgs.channel((String)channels.elementAt(i),mob.getClanID(),mob.Name()+" has logged on.",true);
				if(pendingLogins.containsKey(mob.Name().toUpperCase()))
				   pendingLogins.remove(mob.Name().toUpperCase());
			}
			else
			{
			    String name=mob.Name();
				Log.sysOut("FrontDoor","Failed login: "+mob.Name());
				mob.setName("");
				mob.setPlayerStats(null);
				mob.session().println("\n\rInvalid password.\n\r");
				if(pendingLogins.containsKey(mob.Name().toUpperCase()))
				   pendingLogins.remove(mob.Name().toUpperCase());
				if((!mob.session().killFlag())
				&&(pstats!=null)
				&&(pstats.getEmail().length()>0)
				&&(pstats.getEmail().indexOf("@")>0)
				&&(attempt>2)
				&&(CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN).length()>0))
				{
				    if(mob.session().confirm("Would you like you have your password e-mailed to you (y/N)? ","N"))
				    {
				        if(SMTPclient.emailIfPossible(CommonStrings.getVar(CommonStrings.SYSTEM_SMTPSERVERNAME),
				                				   "passwords@"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN).toLowerCase(),
				                				   "noreply@"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN).toLowerCase(),
				                				   pstats.getEmail(),
				                				   "Password for "+name,
				                				   "Your password for "+name+" at "+CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN)+" is: '"+pstats.password()+"'."))
				            mob.session().println("Email sent.\n\r");
				        else
				            mob.session().println("Error sending email.\n\r");
				        mob.session().setKillFlag(true);
				    }
				}
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
				mob.session().print("\n\r'"+Util.capitalizeAndLower(login)+"' does not exist.\n\rThis server is not accepting new accounts.\n\r\n\r");
				mob.setName("");
				mob.setPlayerStats(null);
			}
			else
            if((CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXNEWPERIP)>0)
            &&(CommonStrings.getCountNewUserByIP(mob.session().getAddress())>=CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXNEWPERIP))
            &&(!CMSecurity.isDisabled("MAXCONNSPERIP")))
            {
                mob.session().println("\n\rThat name is unrecognized.\n\rAlso, the maximum daily new player limit has already been reached for your location.");
                mob.setName("");
                mob.setPlayerStats(null);
            }
            else
			if(mob.session().confirm("\n\r'"+Util.capitalizeAndLower(login)+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
			{
				login=Util.capitalizeAndLower(login.trim());
				mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource("text"+File.separatorChar+"newchar.txt").toString());
                
                boolean emailPassword=((CommonStrings.getVar(CommonStrings.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))&&(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX).length()>0));

				String password="";
                if(!emailPassword)
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
                    if(emailPassword) mob.session().println("This email address will be used to send you a password.");
					if(emailReq) confirmEmail=mob.session().prompt("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter:");
					if(((newEmail.length()>6)&&(newEmail.indexOf("@")>0)&&((newEmail.equalsIgnoreCase(confirmEmail))))
					   ||(!emailReq))
					{
						mob.playerStats().setEmail(newEmail);
						break;
					}
					mob.session().println("\n\rThat email address combination was invalid.\n\r");
				}
                if((mob.playerStats().getEmail()!=null)&&CMSecurity.isBanned(mob.playerStats().getEmail()))
                {
                    mob.tell("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
                    mob.session().setKillFlag(true);
                    if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                       pendingLogins.remove(mob.Name().toUpperCase());
                    return false;
                }
                
				Log.sysOut("FrontDoor","Creating user: "+mob.Name());

				mob.setBitmap(MOB.ATT_AUTOEXITS|MOB.ATT_AUTOWEATHER);
				if(mob.session().confirm("\n\rDo you want ANSI colors (Y/n)?","Y"))
					mob.setBitmap(Util.setb(mob.getBitmap(),MOB.ATT_ANSI));
                if(mob.session().clientTelnetMode(Session.TELNET_MSP))
                    mob.setBitmap(mob.getBitmap()|MOB.ATT_SOUND);
                if(mob.session().clientTelnetMode(Session.TELNET_MXP))
                    mob.setBitmap(mob.getBitmap()|MOB.ATT_MXP);

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
							String themeStr=mob.session().choose(": ",choices,"");
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
                HashSet doneRaces=new HashSet();
				for(Enumeration r=CMClass.races();r.hasMoreElements();)
				{
					Race R=(Race)r.nextElement();
                    if(doneRaces.contains(R.ID())) continue;
                    R=CMClass.getRace(R.ID());
                    doneRaces.add(R.ID());
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
								&&(CommonStrings.isTheme(R.availabilityCode()))
								&&(Util.bset(R.availabilityCode(),theme))
								&&(!Util.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK)))
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
							StringBuffer str=MUDHelp.getHelpText(newRace.ID().toUpperCase(),mob,false);
							if(str!=null) mob.tell("\n\r^N"+str.toString()+"\n\r");
							if(!mob.session().confirm("^!Is ^H"+newRace.name()+"^N^! correct (Y/n)?^N","Y"))
								newRace=null;
						}
					}
				}
				mob.baseCharStats().setMyRace(newRace);

				mob.baseState().setHitPoints(CommonStrings.getIntVar(CommonStrings.SYSTEMI_STARTHP));
				mob.baseState().setMovement(CommonStrings.getIntVar(CommonStrings.SYSTEMI_STARTMOVE));
				mob.baseState().setMana(CommonStrings.getIntVar(CommonStrings.SYSTEMI_STARTMANA));

				String Gender="";
				while(Gender.length()==0)
					Gender=mob.session().choose("\n\r^!What is your gender (M/F)?^N","MF","");

				mob.baseCharStats().setStat(CharStats.GENDER,Gender.toUpperCase().charAt(0));
				mob.baseCharStats().getMyRace().startRacing(mob,false);

                if((CommonStrings.getBoolVar(CommonStrings.SYSTEMB_ACCOUNTEXPIRATION))&&(mob.playerStats()!=null))
                    mob.playerStats().setAccountExpiration(System.currentTimeMillis()+(1000*60*60*24*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TRIALDAYS)));

				mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource("text"+File.separatorChar+"stats.txt").toString());

				boolean mayCont=true;
				StringBuffer listOfClasses=new StringBuffer("??? no classes ???");
				while(mayCont)
				{
					mob.baseCharStats().getMyRace().reRoll(mob,mob.baseCharStats());
					mob.recoverCharStats();
					Vector V=classQualifies(mob,theme);
					if(V.size()>0)
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

                        int max=CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT);
                        StringBuffer statstr=new StringBuffer("Your current stats are: \n\r");
                        CharStats CT=mob.charStats();
                        statstr.append(Util.padRight("Strength",15)+": "+Util.padRight(Integer.toString(CT.getStat(CharStats.STRENGTH)),2)+"/"+(max+CT.getStat(CharStats.MAX_STRENGTH_ADJ))+"\n\r");
                        statstr.append(Util.padRight("Intelligence",15)+": "+Util.padRight(Integer.toString(CT.getStat(CharStats.INTELLIGENCE)),2)+"/"+(max+CT.getStat(CharStats.MAX_INTELLIGENCE_ADJ))+"\n\r");
                        statstr.append(Util.padRight("Dexterity",15)+": "+Util.padRight(Integer.toString(CT.getStat(CharStats.DEXTERITY)),2)+"/"+(max+CT.getStat(CharStats.MAX_DEXTERITY_ADJ))+"\n\r");
                        statstr.append(Util.padRight("Wisdom",15)+": "+Util.padRight(Integer.toString(CT.getStat(CharStats.WISDOM)),2)+"/"+(max+CT.getStat(CharStats.MAX_WISDOM_ADJ))+"\n\r");
                        statstr.append(Util.padRight("Constitution",15)+": "+Util.padRight(Integer.toString(CT.getStat(CharStats.CONSTITUTION)),2)+"/"+(max+CT.getStat(CharStats.MAX_CONSTITUTION_ADJ))+"\n\r");
                        statstr.append(Util.padRight("Charisma",15)+": "+Util.padRight(Integer.toString(CT.getStat(CharStats.CHARISMA)),2)+"/"+(max+CT.getStat(CharStats.MAX_CHARISMA_ADJ))+"\n\r");
                        statstr.append(Util.padRight("TOTAL POINTS",15)+": "+CommonStrings.getIntVar(CommonStrings.SYSTEMI_MAXSTAT)+"/"+(CommonStrings.getIntVar(CommonStrings.SYSTEMI_BASEMAXSTAT)*6));
                        mob.session().println(statstr.toString());
						if(!CMSecurity.isDisabled("CLASSES")
						&&!mob.baseCharStats().getMyRace().classless())
							mob.session().println("\n\rThis would qualify you for ^H"+classes.toString()+"^N.");

						if(!mob.session().confirm("^!Would you like to re-roll (y/N)?^N","N"))
							mayCont=false;
					}
				}
				if(!CMSecurity.isDisabled("CLASSES")
				&&!mob.baseCharStats().getMyRace().classless())
					mob.session().println(null,null,null,Resources.getFileResource("text"+File.separatorChar+"classes.txt").toString());

				CharClass newClass=null;
				Vector qualClasses=classQualifies(mob,theme);
				if(CMSecurity.isDisabled("CLASSES")
				||mob.baseCharStats().getMyRace().classless())
				{
				    if(qualClasses.size()>0)
				        newClass=(CharClass)qualClasses.elementAt(Dice.roll(1,qualClasses.size(),-1));
				    if(newClass==null)
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
						newClass=CMClass.findCharClass(ClassStr);
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
							StringBuffer str=MUDHelp.getHelpText(newClass.ID().toUpperCase(),mob,false);
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


				Item r=CMClass.getItem("Ration");
				Item w=CMClass.getItem("Waterskin");
				Item t=CMClass.getItem("Torch");
				mob.addInventory(r);
				mob.addInventory(w);
				mob.addInventory(t);
				mob.setWimpHitPoint(5);

				CoffeeUtensils.outfit(mob,mob.baseCharStats().getMyRace().outfit());

		        Ability A=CMClass.getAbility("Allergies");
		        if(A!=null) A.invoke(mob,mob,true,0);

				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob.resetToMaxState();

				Faction F=null;
				Vector mine=null;
				int defaultValue=0;
				for(Enumeration e=Factions.factionSet.elements();e.hasMoreElements();)
				{
				    F=(Faction)e.nextElement();
				    mine=F.findChoices(mob);
				    defaultValue=F.findAutoDefault(mob);
				    if(defaultValue!=Integer.MAX_VALUE)
				        mob.addFaction(F.ID,defaultValue);
				    if(mine.size()==1)
				        mob.addFaction(F.ID,((Integer)mine.firstElement()).intValue());
				    else
				    if(mine.size()>1)
				    {
				        if((F.choiceIntro!=null)&&(F.choiceIntro.length()>0))
							mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource(F.choiceIntro).toString());
				        StringBuffer menu=new StringBuffer("Select one: ");
				        Vector namedChoices=new Vector();
				        for(int m=0;m<mine.size();m++)
				        {
				            Faction.FactionRange FR=Factions.getRange(F.ID,((Integer)mine.elementAt(m)).intValue());
				            if(FR!=null)
				            {
				                namedChoices.addElement(FR.Name.toUpperCase());
				                menu.append(FR.Name+", ");
				            }
				            else
				                namedChoices.addElement(""+((Integer)mine.elementAt(m)).intValue());
				        }
						if(mine.size()==namedChoices.size())
						{
							String alignment="";
							while((!namedChoices.contains(alignment))
							&&(!mob.session().killFlag()))
							{
								alignment=mob.session().prompt(menu.toString().substring(0,menu.length()-2)+".\n\r: ","").toUpperCase();
								if(!namedChoices.contains(alignment))
								    for(int i=0;i<namedChoices.size();i++)
								        if(((String)namedChoices.elementAt(i)).startsWith(alignment.toUpperCase()))
								        { alignment=(String)namedChoices.elementAt(i); break;}
								if(!namedChoices.contains(alignment))
								    for(int i=0;i<namedChoices.size();i++)
								        if(((String)namedChoices.elementAt(i)).indexOf(alignment.toUpperCase())>=0)
								        { alignment=(String)namedChoices.elementAt(i); break;}
							}
							if(!mob.session().killFlag())
							{
								int valueIndex=namedChoices.indexOf(alignment);
								if(valueIndex>=0)
								    mob.addFaction(F.ID,((Integer)mine.elementAt(valueIndex)).intValue());
							}
						}
				    }
				}
				mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);
				CoffeeUtensils.outfit(mob,mob.baseCharStats().getCurrentClass().outfit());
				mob.setStartRoom(CMMap.getStartRoom(mob));
			    mob.baseCharStats().setStat(CharStats.AGE,mob.playerStats().initializeBirthday(0,mob.baseCharStats().getMyRace()));
				mob.session().println(null,null,null,"\n\r\n\r"+Resources.getFileResource("text"+File.separatorChar+"newchardone.txt").toString());
				mob.session().prompt("");
                boolean logoff=false;
                if(emailPassword)
                {
                    password="";
                    for(int i=0;i<6;i++)
                        password+=(char)('a'+Dice.roll(1,26,-1));
                    mob.playerStats().setPassword(password);
                    CMClass.DBEngine().DBUpdatePassword(mob);
                    CMClass.DBEngine().DBWriteJournal(CommonStrings.getVar(CommonStrings.SYSTEM_MAILBOX),
                              mob.Name(),
                              mob.Name(),
                              "Password for "+mob.Name(),
                              "Your password for "+mob.Name()+" is: "+mob.playerStats().password()+"\n\rYou can login by pointing your mud client at "+CommonStrings.getVar(CommonStrings.SYSTEM_MUDDOMAIN)+" port(s):"+CommonStrings.getVar(CommonStrings.SYSTEM_MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.",-1);
                    mob.tell("Your account has been created.  You will receive an email with your password shortly.");
                    try{Thread.sleep(2000);}catch(Exception e){}
                    mob.session().setKillFlag(true);
                }
                else
                {
                    reloadTerminal(mob);
    				mob.bringToLife(mob.getStartRoom(),true);
    				mob.location().showOthers(mob,mob.location(),CMMsg.MASK_GENERAL|CMMsg.MSG_ENTER,"<S-NAME> appears!");
                }
                mob.playerStats().leveledDateTime(0);
				CMClass.DBEngine().DBCreateCharacter(mob);
				if(CMMap.getPlayer(mob.Name())==null)
					CMMap.addPlayer(mob);

                if((mob.session()==null)||(mob.playerStats()==null)) return false;
				mob.playerStats().setLastIP(mob.session().getAddress());
				Log.sysOut("FrontDoor","Created user: "+mob.Name());
                CommonStrings.addNewUserByIP(mob.session().getAddress());
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
				CMClass.DBEngine().DBUpdatePlayer(mob);
                Vector channels=ChannelSet.getFlaggedChannelNames("NEWPLAYERS");
                for(int i=0;i<channels.size();i++)
                    CommonMsgs.channel((String)channels.elementAt(i),mob.getClanID(),mob.Name()+" has just been created.",true);
				CoffeeTables.bump(mob,CoffeeTables.STAT_LOGINS);
				CoffeeTables.bump(mob,CoffeeTables.STAT_NEWPLAYERS);
				if(pendingLogins.containsKey(mob.Name().toUpperCase()))
				   pendingLogins.remove(mob.Name().toUpperCase());
				return !logoff;
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
