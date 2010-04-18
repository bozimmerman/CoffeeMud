package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


/*
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class CharCreation extends StdLibrary implements CharCreationLibrary
{
    public String ID(){return "CharCreation";}
    public Hashtable<String,Long> pendingLogins=new Hashtable<String,Long>();
    public Hashtable startRooms=new Hashtable();
    public Hashtable deathRooms=new Hashtable();
    public Hashtable bodyRooms=new Hashtable();

    public void reRollStats(MOB mob, CharStats C)
    {
        // from Ashera
        int basemax = CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
        int basemin = 3;

        int points = CMProps.getIntVar(CMProps.SYSTEMI_MAXSTAT);
        // Make sure there are enough points
        if (points < ((basemin + 1) * CharStats.CODES.BASE().length))
            points = (basemin + 1) * CharStats.CODES.BASE().length;

        // Make sure there aren't too many points
        if (points > (basemax - 1) * CharStats.CODES.BASE().length)
                points = (basemax - 1) * CharStats.CODES.BASE().length;

        int[] stats=new int[CharStats.CODES.TOTAL()];
        for(int i=0;i<stats.length;i++)
            stats[i]=basemin;

        // Subtract stat minimums from point total to get distributable points
        int pointsLeft = points - (basemin * CharStats.CODES.BASE().length);

        while (pointsLeft > 0)
        {
            int whichStat = CharStats.CODES.BASE()[CMLib.dice().roll(1,CharStats.CODES.BASE().length,-1)];
            if(stats[whichStat]<basemax)
            {
                stats[whichStat]++;
                --pointsLeft;
            }
        }

		for(int i : CharStats.CODES.BASE())
            C.setStat(i,stats[i]);
    }

    public boolean canChangeToThisClass(MOB mob, CharClass thisClass, int theme)
    {
        if((CMProps.isTheme(thisClass.availabilityCode()))
        &&((theme<0)||(CMath.bset(thisClass.availabilityCode(),theme)))
        &&(!CMath.bset(thisClass.availabilityCode(),Area.THEME_SKILLONLYMASK))
        &&((mob==null)||(thisClass.qualifiesForThisClass(mob,true))))
        	return true;
        return false;
    }

    public Vector classQualifies(MOB mob, int theme)
    {
        Vector them=new Vector();
        HashSet doneClasses=new HashSet();
        for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
        {
            CharClass C=(CharClass)c.nextElement();
            if(doneClasses.contains(C.ID())) continue;
            C=CMClass.getCharClass(C.ID());
            doneClasses.add(C.ID());
            if(canChangeToThisClass(mob,C,theme))
                them.addElement(C);
        }
        return them;
    }

    public Vector raceQualifies(MOB mob, int theme)
    {
    	Vector qualRaces = new Vector();
        HashSet doneRaces=new HashSet();
        for(Enumeration r=CMClass.races();r.hasMoreElements();)
        {
            Race R=(Race)r.nextElement();
            if(doneRaces.contains(R.ID())) continue;
            R=CMClass.getRace(R.ID());
            doneRaces.add(R.ID());
            if((CMProps.isTheme(R.availabilityCode()))
            &&(!CMath.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK))
            &&(CMath.bset(R.availabilityCode(),theme)))
            	qualRaces.add(R);
        }
        return qualRaces;
    }
    
    public boolean isOkName(String login)
    {
        if(login.length()>20) return false;
        if(login.length()<3) return false;

        if(login.trim().indexOf(" ")>=0) return false;

        login=login.toUpperCase().trim();
        Vector V=CMParms.parse(login);
        for(int v=V.size()-1;v>=0;v--)
        {
            String str=(String)V.elementAt(v);
            if((" THE A AN ").indexOf(" "+str+" ")>=0)
                V.removeElementAt(v);
        }
        for(int v=0;v<V.size();v++)
        {
            String str=(String)V.elementAt(v);
            if(DEFAULT_BADNAMES.indexOf(" "+str+" ")>=0)
                return false;
        }
        Vector V2=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_BADNAMES),true);
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
        for(Enumeration d=CMLib.map().deities();d.hasMoreElements();)
        {
            MOB D=(MOB)d.nextElement();
            if((CMLib.english().containsString(D.ID(),login))
            ||(CMLib.english().containsString(D.Name(),login)))
                return false;
        }
        for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
        {
            MOB M=(MOB)m.nextElement();
            if((CMLib.english().containsString(M.Name(),login))
            ||(CMLib.english().containsString(M.name(),login)))
                return false;
        }

        for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
        {
            Clan C=(Clan)e.nextElement();
            if((CMLib.english().containsString(C.clanID(),login))
            ||(CMLib.english().containsString(C.name(),login)))
                return false;
        }

        return !CMSecurity.isBanned(login);
    }

    public void reloadTerminal(MOB mob)
    {
        if(mob==null) return;

        Session S=mob.session();
        if(S==null) return;

        S.initTelnetMode(mob.getBitmap());
        if((CMath.bset(mob.getBitmap(),MOB.ATT_MXP))
        &&(!CMSecurity.isDisabled("MXP")))
        {
            if(S.clientTelnetMode(Session.TELNET_MXP))
            {
                StringBuffer mxpText=Resources.getFileResource("text/mxp.txt",true);
                if(mxpText!=null)
                    S.rawOut("\033[6z"+mxpText.toString()+"\n\r");
            }
            else
                mob.tell("MXP codes have been disabled for this session.");
        }
        else
        if(S.clientTelnetMode(Session.TELNET_MXP))
        {
            S.changeTelnetMode(Session.TELNET_MXP,false);
            S.setClientTelnetMode(Session.TELNET_MXP,false);
        }

        if((CMath.bset(mob.getBitmap(),MOB.ATT_SOUND))
        &&(!CMSecurity.isDisabled("MSP")))
        {
            if(!S.clientTelnetMode(Session.TELNET_MSP))
                mob.tell("MSP sounds have been disabled for this session.");
        }
        else
        if(S.clientTelnetMode(Session.TELNET_MSP))
        {
            S.changeTelnetMode(Session.TELNET_MSP,false);
            S.setClientTelnetMode(Session.TELNET_MSP,false);
        }
    }

    public void showTheNews(MOB mob)
    {
        reloadTerminal(mob);
        Command C=CMClass.getCommand("PollCmd");
        try{ C.execute(mob,null,0);}catch(Exception e){}

        if((mob.session()==null)
        ||(mob.isMonster())
        ||(CMath.bset(mob.getBitmap(),MOB.ATT_DAILYMESSAGE)))
            return;

        C=CMClass.getCommand("MOTD");
        try{ C.execute(mob,CMParms.parse("MOTD NEW PAUSE"),0);}catch(Exception e){}
    }

    public boolean isExpired(PlayerAccount acct, Session session, long expiration)
    {
        if(!CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION)) 
        	return false;
        if((acct!=null)&&(acct.isSet(PlayerAccount.FLAG_NOEXPIRE)))
        	return false;
        if((session.mob()!=null)
        &&((CMSecurity.isASysOp(session.mob()))||(CMSecurity.isAllowedEverywhere(session.mob(), "NOEXPIRE"))))
        	return false;
        if(expiration<=System.currentTimeMillis())
        {
            session.println("\n\r"+CMProps.getVar(CMProps.SYSTEM_EXPCONTACTLINE)+"\n\r\n\r");
            session.kill(false,false,false);
            return true;
        }
        return false;
    }

    private void executeScript(MOB mob, Vector scripts) 
    {
        if(scripts==null) return;
        for(int s=0;s<scripts.size();s++) 
        {
            String script=(String)scripts.elementAt(s);
            ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
            S.setSavable(false);
            S.setVarScope("*");
            S.setScript(script);
            Room oldRoom=mob.location();
            mob.setLocation(CMLib.map().getRandomRoom());
            CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,"CHARCREATION");
            S.executeMsg(mob, msg2);
            S.dequeResponses();
            S.tick(mob,Tickable.TICKID_MOB);
            mob.setLocation(oldRoom);
        }
    }

    private Hashtable getLoginScripts()
    {
        Hashtable extraScripts=new Hashtable();
        final String[] VALID_SCRIPT_CODES={"PASSWORD","EMAIL","ANSI","THEME","RACE","GENDER","STATS","CLASS","FACTIONS","END"};                    
        Vector extras=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_CHARCREATIONSCRIPTS),true);
        for(int e=0;e<extras.size();e++) {
            String s=(String)extras.elementAt(e);
            int x=s.indexOf(':');
            String code="END";
            if(x>0) {
                code=s.substring(0,x).toUpperCase().trim();
                boolean found=false;
                for(int v=0;v<VALID_SCRIPT_CODES.length;v++)
                    if(VALID_SCRIPT_CODES[v].equals(code))
                    { code=VALID_SCRIPT_CODES[v]; found=true; break;}
                    else
                    if(VALID_SCRIPT_CODES[v].startsWith(code))
                    { code=VALID_SCRIPT_CODES[v]; found=true; break;}
                if(!found)
                {
                    Log.errOut("CharCreation","Error in CHARCREATIONSCRIPTS, invalid code: "+code);
                    continue;
                }
                s=s.substring(x+1);
            }
            Vector V=(Vector)extraScripts.get(code);
            if(V==null){ V=new Vector(); extraScripts.put(code,V);}
            V.addElement(s.trim());
        }
        return extraScripts;
    }

    public LoginResult selectAccountCharacter(PlayerAccount acct, Session session) throws java.io.IOException
    {
    	if((acct==null)||(session==null)||(session.killFlag()))
    		return LoginResult.NO_LOGIN;
        session.setServerTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.FLAG_ANSI));
        session.setClientTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.FLAG_ANSI));
    	boolean charSelected = false;
    	boolean showList = acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF);
        StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"selchar.txt",null,true).text();
    	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
        session.println(null,null,null,"\n\r\n\r"+introText.toString());
    	while((!session.killFlag())&&(!charSelected))
    	{
    		StringBuffer buf = new StringBuffer("");
    		if(showList)
    		{
    			showList = false;
	    		buf.append("^X");
	    		buf.append(CMStrings.padRight("Character",20));
	    		buf.append(" " + CMStrings.padRight("Race",10));
	    		buf.append(" " + CMStrings.padRight("Level",5));
	    		buf.append(" " + CMStrings.padRight("Class",15));
	    		buf.append("^.^N\n\r");
	    		for(Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
	    		{
	    			PlayerLibrary.ThinPlayer player = p.nextElement();
	        		buf.append("^H");
	        		buf.append(CMStrings.padRight(player.name,20));
	        		buf.append("^.^N");
	        		buf.append(" " + CMStrings.padRight(player.race,10));
	        		buf.append(" " + CMStrings.padRight(""+player.level,5));
	        		buf.append(" " + CMStrings.padRight(player.charClass,15));
	        		buf.append("^.^N\n\r");
	    		}
	    		session.println(buf.toString());
    			buf.setLength(0);
    		}
    		if(!acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
    		{
	    		buf.append(" ^XAccount Menu^.^N\n\r");
	    		buf.append(" ^XL^.^w)^Hist characters\n\r");
	    		buf.append(" ^XN^.^w)^Hew character\n\r");
	    		buf.append(" ^XI^.^w)^Hmport character\n\r");
	    		if(acct.isSet(PlayerAccount.FLAG_CANEXPORT))
		    		buf.append(" ^XE^.^w)^Hxport character\n\r");
	    		buf.append(" ^XD^.^w)^Helete/Retire character\n\r");
	    		buf.append(" ^XH^.^w)^Help\n\r");
	    		buf.append(" ^XM^.^w)^Henu OFF\n\r");
	    		buf.append(" ^XQ^.^w)^Huit (logout)\n\r");
	    		buf.append("\n\r^H ^w(^HEnter your character name to login^w)^H");
	    		session.println(buf.toString());
    			buf.setLength(0);
    		}
    		if(!session.killFlag())
    			session.updateLoopTime();
    		String s = session.prompt("\n\r^wCommand or Name ^H(?)^w: ^N", TimeClock.TIME_MILIS_PER_MUDHOUR);
    		if(s==null) return LoginResult.NO_LOGIN;
    		if(s.trim().length()==0) continue;
    		if(s.equalsIgnoreCase("?")||(s.equalsIgnoreCase("HELP"))||s.equalsIgnoreCase("H"))
    		{
    	        introText=new CMFile(Resources.buildResourcePath("help")+"accts.txt",null,true).text();
    	    	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
    	        session.println(null,null,null,"\n\r\n\r"+introText.toString());
    	        continue;
    		}
    		if(s.equalsIgnoreCase("LIST")||s.equalsIgnoreCase("L"))
    		{
    			showList = true;
    			continue;
    		}
    		if(s.equalsIgnoreCase("QUIT")||s.equalsIgnoreCase("Q"))
    		{
    			if(session.confirm("Quit -- are you sure (y/N)?", "N"))
				{
    				session.kill(false,false,false);
    	            return LoginResult.NO_LOGIN;
				}
    			continue;
    		}
    		if(s.toUpperCase().startsWith("NEW ")||s.equalsIgnoreCase("NEW")||s.equalsIgnoreCase("N"))
    		{
    			if(s.length()>=3)
	    			s=s.substring(3).trim();
    			else
    				s=s.substring(1).trim();
    			if(s.length()==0)
    			{
    				s=session.prompt("\n\rPlease enter a name for your character: ","");
    				if(s.length()==0) continue;
    			}
                if((!isOkName(s))
                ||(CMLib.players().playerExists(s))
                ||(CMLib.players().accountExists(s)&&(!s.equalsIgnoreCase(acct.accountName()))))
                {
                	session.println("\n\rThat name is not available for new characters.\n\r  Choose another name (no spaces allowed)!\n\r");
                    continue;
                }
                if((CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)<=acct.numPlayers())
                &&(!acct.isSet(PlayerAccount.FLAG_NUMCHARSOVERRIDE)))
                {
                	session.println("You may only have "+CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)+" characters.  Please retire one to create another.");
                	continue;
                }
            	if(newCharactersAllowed(s,session,true))
            	{
            		String login=CMStrings.capitalizeAndLower(s);
	                if(session.confirm("Create a new character called '"+login+"' (y/N)?", "N"))
	                {
	            		if(!session.killFlag())
	            			session.updateLoopTime();
	                	if(createCharacter(acct, login, session) == LoginResult.CCREATION_EXIT)
		            		return LoginResult.CCREATION_EXIT;
	                }
            	}
    			continue;
    		}
    		if(s.equalsIgnoreCase("MENU")||s.equalsIgnoreCase("M"))
    		{
    			if(acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF)&&(session.confirm("Turn menus back on (y/N)?", "N")))
    				acct.setFlag(PlayerAccount.FLAG_ACCOUNTMENUSOFF, false);
    			else
    			if(!acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF)&&(session.confirm("Turn menus off (y/N)?", "N")))
    				acct.setFlag(PlayerAccount.FLAG_ACCOUNTMENUSOFF, true);
    			continue;
    		}
    		if(s.toUpperCase().startsWith("RETIRE ")||s.toUpperCase().startsWith("DELETE ")||s.equalsIgnoreCase("D"))
    		{
    			if(s.length()>=7)
	    			s=s.substring(7).trim();
    			else
    				s=s.substring(1).trim();
    			if(s.length()==0)
    			{
    				s=session.prompt("\n\rPlease enter the name of the character: ","");
    				if(s.length()==0) continue;
    			}
    			PlayerLibrary.ThinPlayer delMe = null;
        		for(Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
        		{
        			PlayerLibrary.ThinPlayer player = p.nextElement();
        			if(player.name.equalsIgnoreCase(s))
        				delMe=player;
        		}
    			if(delMe==null)
    			{
    				session.println("The character '"+s+"' is unknown.");
    				continue;
    			}
    			if(session.confirm("Are you sure you want to retire and delete '"+delMe.name+"' (y/N)?", "N"))
    			{
    				MOB M=CMLib.players().getLoadPlayer(delMe.name);
    				if(M!=null)
	    				CMLib.players().obliteratePlayer(M, false);
    				session.println(delMe.name+" has been deleted.");
    			}
    			continue;
    		}
    		if((s.toUpperCase().startsWith("EXPORT "))&&(acct.isSet(PlayerAccount.FLAG_CANEXPORT))||s.equalsIgnoreCase("E"))
    		{
    			if(s.length()>=7)
	    			s=s.substring(7).trim();
    			else
    				s=s.substring(1).trim();
    			if(s.length()==0)
    			{
    				s=session.prompt("\n\rPlease enter the name of the character: ","");
    				if(s.length()==0) continue;
    			}
    			PlayerLibrary.ThinPlayer delMe = null;
        		for(Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
        		{
        			PlayerLibrary.ThinPlayer player = p.nextElement();
        			if(player.name.equalsIgnoreCase(s))
        				delMe=player;
        		}
    			if(delMe==null)
    			{
    				session.println("The character '"+s+"' is unknown.");
    				continue;
    			}
    			if(session.confirm("Are you sure you want to remove character  '"+delMe.name+"' from your account (y/N)?", "N"))
    			{
    				String password;
    				password = session.prompt("Enter a final password for this character: ");
    				if((password==null)||(password.trim().length()==0))
    					session.println("Aborted.");
    				else
    				{
	    				MOB M=CMLib.players().getLoadPlayer(delMe.name);
	    				if(M!=null)
	    				{
	    					acct.delPlayer(M);
	    					M.playerStats().setAccount(null);
	    					CMLib.database().DBUpdateAccount(acct);
	    					M.playerStats().setLastDateTime(System.currentTimeMillis());
	    					M.playerStats().setLastUpdated(System.currentTimeMillis());
	    					M.playerStats().setPassword(password);
	    					CMLib.database().DBUpdatePlayer(M);
		    				session.println(delMe.name+" has been exported from your account.");
	    				}
    				}
    			}
    			continue;
    		}
    		if(s.toUpperCase().startsWith("IMPORT ")||s.equalsIgnoreCase("I"))
    		{
    			if(s.length()>=7)
	    			s=s.substring(7).trim();
    			else
    				s=s.substring(1).trim();
    			if(s.length()==0)
    			{
    				s=session.prompt("\n\rPlease enter the name of the character: ","");
    				if(s.length()==0) continue;
    			}
                if((CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)<=acct.numPlayers())
                &&(!acct.isSet(PlayerAccount.FLAG_NUMCHARSOVERRIDE)))
                {
                	session.println("You may only have "+CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)+" characters.  Please delete one to create another.");
                	continue;
                }
    			PlayerLibrary.ThinnerPlayer newCharT = CMLib.database().DBUserSearch(s); 
				String password;
				password = session.prompt("Enter the password for this character: ");
				if((password==null)||(password.trim().length()==0))
					session.println("Aborted.");
				else
				if((newCharT==null)
				||(!password.equalsIgnoreCase(newCharT.password))
				||((newCharT.accountName!=null)
						&&(newCharT.accountName.length()>0)
						&&(!newCharT.accountName.equalsIgnoreCase(acct.accountName()))))
					session.println("Character name or password is incorrect.");
				else
    			if(session.confirm("Are you sure you want to import character  '"+newCharT.name+"' into your account (y/N)?", "N"))
    			{
    				MOB M=CMLib.players().getLoadPlayer(newCharT.name);
    				if(M!=null)
    				{
    					acct.addNewPlayer(M);
    					M.playerStats().setAccount(acct);
    					CMLib.database().DBUpdateAccount(acct);
    					CMLib.database().DBUpdatePlayer(M);
	    				session.println(M.name()+" has been imported into your account.");
    				}
    			}
    			continue;
    		}
    		boolean wizi=s.trim().endsWith(" !");
    		if(wizi) s=s.substring(0,s.length()-2).trim();
			PlayerLibrary.ThinnerPlayer playMe = null;
			if(acct.isPlayer(s))
				playMe = CMLib.database().DBUserSearch(s);
    		if(playMe == null)
    		{
    			session.println("'"+s+"' is an unknown character or command.  Use ? for help.");
    			continue;
    		}
    		MOB realMOB=CMLib.players().getLoadPlayer(playMe.name);
    		if(realMOB==null)
    		{
    			session.println("Error loading character '"+s+"'.  Please contact the management.");
    			continue;
    		}
    		int numAccountOnline=0;
    		for(int si=0;si<CMLib.sessions().size();si++)
    		{
    			Session S=CMLib.sessions().elementAt(si);
    			if((S!=null)
    			&&(S.mob()!=null)
    			&&(S.mob().playerStats()!=null)
    			&&(S.mob().playerStats().getAccount()==acct))
    				numAccountOnline++;
    		}
            if((CMProps.getIntVar(CMProps.SYSTEMI_MAXCONNSPERACCOUNT)>0)
            &&(numAccountOnline>=CMProps.getIntVar(CMProps.SYSTEMI_MAXCONNSPERACCOUNT))
            &&(!CMSecurity.isDisabled("MAXCONNSPERACCOUNT"))
            &&(!acct.isSet(PlayerAccount.FLAG_MAXCONNSOVERRIDE)))
            {
                session.println("You may only have "+CMProps.getIntVar(CMProps.SYSTEMI_MAXCONNSPERACCOUNT)+" of your characters on at one time.");
    			continue;
            }
            playMe.loadedMOB=realMOB;
        	LoginResult prelimResults = prelimChecks(session,playMe.name,playMe);
        	if(prelimResults!=null)
        		return prelimResults;
            LoginResult completeResult=completeCharacterLogin(session,playMe.name, wizi);
            if(completeResult == LoginResult.NO_LOGIN)
            	continue;
        	charSelected=true;
    	}
    	return LoginResult.NORMAL_LOGIN;
    }
    
    public LoginResult createAccount(PlayerAccount acct, String login, Session session)
	    throws java.io.IOException
	{
        Log.sysOut("FrontDoor","Creating account: "+acct.accountName());
        login=CMStrings.capitalizeAndLower(login.trim());
        if(session.confirm("\n\rDo you want ANSI colors (Y/n)?","Y"))
        	acct.setFlag(PlayerAccount.FLAG_ANSI, true);
        else
        {
        	acct.setFlag(PlayerAccount.FLAG_ANSI, false);
            session.setServerTelnetMode(Session.TELNET_ANSI,false);
            session.setClientTelnetMode(Session.TELNET_ANSI,false);
        }
        
        StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newacct.txt",null,true).text();
    	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
        session.println(null,null,null,"\n\r\n\r"+introText.toString());
        
        boolean emailPassword=((CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0));
        String password = "";
        if(!emailPassword)
	        while((password.length()==0)&&(!session.killFlag()))
	        {
	            password=session.prompt("\n\rEnter an account password\n\r: ","");
	            if(password.length()==0)
	            {
	            	session.println("\n\rAborting account creation.");
	            	return LoginResult.NO_LOGIN;
	            }
	        }
    	String emailAddy = getEmailAddress(session, null);
    	if(emailAddy == null)
        {
        	session.println("\n\rAborting account creation.");
        	return LoginResult.NO_LOGIN;
        }
        acct.setAccountName(login);
        acct.setPassword(password);
        acct.setEmail(emailAddy);
        acct.setLastIP(session.getAddress());
        acct.setLastDateTime(System.currentTimeMillis());
        if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
            acct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*((long)CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS))));
        
        if(emailPassword)
        {
            password="";
            for(int i=0;i<6;i++)
                password+=(char)('a'+CMLib.dice().roll(1,26,-1));
            acct.setPassword(password);
            CMLib.database().DBCreateAccount(acct);
            CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
                      acct.accountName(),
                      acct.accountName(),
                      "Password for "+acct.accountName(),
                      "Your password for "+acct.accountName()+" is: "+acct.password()+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".\n\rAfter creating a character, you may use the PASSWORD command to change it once you are online.");
            session.println("Your account has been created.  You will receive an email with your password shortly.");
            try{Thread.sleep(2000);}catch(Exception e){}
            session.kill(false,false,false);
            Log.sysOut("FrontDoor","Created account: "+acct.accountName());
            session.setAccount(null);
            return LoginResult.NO_LOGIN;
        }
        else
        {
            CMLib.database().DBCreateAccount(acct);
            StringBuffer doneText=new CMFile(Resources.buildResourcePath("text")+"doneacct.txt",null,true).text();
        	try { doneText = CMLib.httpUtils().doVirtualPage(doneText);}catch(Exception ex){}
            session.println(null,null,null,"\n\r\n\r"+doneText.toString());
        }
        session.setAccount(acct);
        Log.sysOut("FrontDoor","Created account: "+acct.accountName());
        return LoginResult.ACCOUNT_LOGIN;
	}

    public String getEmailAddress(Session session, boolean[] emailConfirmation) throws java.io.IOException
    {
        boolean emailPassword=((CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0));

        boolean emailReq=(!CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"));
        StringBuffer emailIntro=new CMFile(Resources.buildResourcePath("text")+"email.txt",null,true).text();
    	try { emailIntro = CMLib.httpUtils().doVirtualPage(emailIntro);}catch(Exception ex){}
    	session.println(null,null,null,emailIntro.toString());
        while(!session.killFlag())
        {
            String newEmail=session.prompt("\n\rEnter your e-mail address: ");
            if((emailReq||emailPassword) 
            && ((newEmail==null)||(newEmail.trim().length()==0)||(!CMLib.smtp().isValidEmailAddress(newEmail))))
            {
            	session.println("\n\rA valid email address is required.\n\r");
            	continue;
            }
            String confirmEmail=newEmail;
            if(emailPassword) session.println("This email address will be used to send you a password.");
            if(emailReq||emailPassword) confirmEmail=session.prompt("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter: ");
            boolean emailConfirmed=false;
            if((newEmail.length()>0)&&(newEmail.equalsIgnoreCase(confirmEmail)))
            	emailConfirmed=CMLib.smtp().isValidEmailAddress(newEmail);
            
            if(emailConfirmed||((!emailReq)&&(newEmail.trim().length()==0)))
            {
            	if((emailConfirmed)&&(emailConfirmation!=null)&&(emailConfirmation.length>0))
            		emailConfirmation[0]=true;
            	return newEmail;
            }
            session.println("\n\rThat email address combination was invalid.\n\r");
        }
        throw new java.io.IOException("Session is dead!");
    }
    
    public LoginResult createCharacter(PlayerAccount acct, String login, Session session)
        throws java.io.IOException
    {
    	Hashtable extraScripts = getLoginScripts();
        
        login=CMStrings.capitalizeAndLower(login.trim());
        
        StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newchar.txt",null,true).text();
    	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
        session.println(null,null,null,"\n\r\n\r"+introText.toString());

        boolean emailPassword=((CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
        					 &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0));

        String password=(acct!=null)?acct.password():"";
        if((!emailPassword)&&(password.length()==0))
	        while((password.length()==0)&&(!session.killFlag()))
	        {
	            password=session.prompt("\n\rEnter a password: ","");
	            if(password.length()==0)
	            	session.println("\n\rYou must enter a password to continue.");
	        }
        MOB mob=CMClass.getMOB("StdMOB");
        mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
        mob.setName(login);
        boolean logoff=false;
        try
        {
	        mob.setSession(session);
	        session.setMob(mob);
	        setGlobalBitmaps(mob);
			
	        if((acct==null)||(acct.password().length()==0))
	        {
		        mob.playerStats().setPassword(password);
		        executeScript(mob,(Vector)extraScripts.get("PASSWORD"));
	        }
	        
	        if((acct!=null)&&(acct.getEmail().length()>0))
	        	mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_AUTOFORWARD));
	        else
	        {
	        	mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_AUTOFORWARD));
	        	boolean[] emailConfirmed = new boolean[]{false};
	        	String emailAddy = getEmailAddress(session, emailConfirmed);
	        	if(emailAddy != null)
	        		mob.playerStats().setEmail(emailAddy);
	        	if(emailConfirmed[0])
	            	mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_AUTOFORWARD));
	        }
	        
	        if((mob.playerStats().getEmail()!=null)&&CMSecurity.isBanned(mob.playerStats().getEmail()))
	        {
	            session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
	            if(mob==session.mob())
		        	session.kill(false,false,false);
	            throw new Exception("");
	        }
	        mob.playerStats().setAccount(acct);
	        Log.sysOut("FrontDoor","Creating user: "+mob.Name());
	        executeScript(mob,(Vector)extraScripts.get("EMAIL"));
	
	        mob.setBitmap(MOB.ATT_AUTOEXITS|MOB.ATT_AUTOWEATHER);
	        if(acct!=null)
	        {
	        	if(acct.isSet(PlayerAccount.FLAG_ANSI))
		            mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_ANSI));
	        	else
	        	{
		            mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_ANSI));
		            session.setServerTelnetMode(Session.TELNET_ANSI,false);
		            session.setClientTelnetMode(Session.TELNET_ANSI,false);
	        	}
	        }
	        else
	        if(session.confirm("\n\rDo you want ANSI colors (Y/n)?","Y"))
	            mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_ANSI));
	        else
	        {
	            mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_ANSI));
	            session.setServerTelnetMode(Session.TELNET_ANSI,false);
	            session.setClientTelnetMode(Session.TELNET_ANSI,false);
	        }
	        if((session.clientTelnetMode(Session.TELNET_MSP))
	        &&(!CMSecurity.isDisabled("MSP")))
	            mob.setBitmap(mob.getBitmap()|MOB.ATT_SOUND);
	        if((session.clientTelnetMode(Session.TELNET_MXP))
	        &&(!CMSecurity.isDisabled("MXP")))
	            mob.setBitmap(mob.getBitmap()|MOB.ATT_MXP);
	
	        executeScript(mob,(Vector)extraScripts.get("ANSI"));
	        
	        int themeCode=CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME);
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
	                if(CMath.bset(themeCode,Area.THEME_FANTASY)){ choices+="F"; selections+="/F";}
	                if(CMath.bset(themeCode,Area.THEME_HEROIC)){ choices+="H"; selections+="/H";}
	                if(CMath.bset(themeCode,Area.THEME_TECHNOLOGY)){ choices+="T"; selections+="/T";}
	                if(choices.length()==0)
	                {
	                    choices="F";
	                    selections="/F";
	                }
	                while((theme<0)&&(!session.killFlag()))
	                {
	                    introText=new CMFile(Resources.buildResourcePath("text")+"themes.txt",null,true).text();
	                	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
	                	session.println(null,null,null,introText.toString());
	                	session.print("\n\r^!Please select from the following:^N "+selections.substring(1)+"\n\r");
	                    String themeStr=session.choose(": ",choices,"");
	                    if(themeStr.toUpperCase().startsWith("F"))
	                        theme=Area.THEME_FANTASY;
	                    if(themeStr.toUpperCase().startsWith("H"))
	                        theme=Area.THEME_HEROIC;
	                    if(themeStr.toUpperCase().startsWith("T"))
	                        theme=Area.THEME_TECHNOLOGY;
	                }
	                break;
	        }
	        
	        executeScript(mob,(Vector)extraScripts.get("THEME"));
	        
	        if(!CMSecurity.isDisabled("RACES"))
	        {
	            introText=new CMFile(Resources.buildResourcePath("text")+"races.txt",null,true).text();
	        	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
	        	session.println(null,null,null,introText.toString());
	        }
	
	        StringBuffer listOfRaces=new StringBuffer("[");
	        boolean tmpFirst = true;
	        Vector qualRaces = raceQualifies(mob,theme);
	        for(Enumeration r=qualRaces.elements();r.hasMoreElements();)
	        {
	            Race R=(Race)r.nextElement();
	            if (!tmpFirst)
	                listOfRaces.append(", ");
	            else
	                tmpFirst = false;
	            listOfRaces.append("^H"+R.name()+"^N");
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
	        	session.print("\n\r^!Please choose from the following races (?):^N\n\r");
	        	session.print(listOfRaces.toString());
	            String raceStr=session.prompt("\n\r: ","");
	            if(raceStr.trim().equalsIgnoreCase("?"))
	            	session.println(null,null,null,"\n\r"+new CMFile(Resources.buildResourcePath("text")+"races.txt",null,true).text().toString());
	            else
	            {
	                newRace=CMClass.getRace(raceStr);
	                if((newRace!=null)&&((!CMProps.isTheme(newRace.availabilityCode()))
	                                        ||(!CMath.bset(newRace.availabilityCode(),theme))
	                                        ||(CMath.bset(newRace.availabilityCode(),Area.THEME_SKILLONLYMASK))))
	                    newRace=null;
	                if(newRace==null)
	                    for(Enumeration r=CMClass.races();r.hasMoreElements();)
	                    {
	                        Race R=(Race)r.nextElement();
	                        if((R.name().equalsIgnoreCase(raceStr))
	                        &&(CMProps.isTheme(R.availabilityCode()))
	                        &&(CMath.bset(R.availabilityCode(),theme))
	                        &&(!CMath.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK)))
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
	                        &&(CMProps.isTheme(R.availabilityCode()))
	                        &&(CMath.bset(R.availabilityCode(),theme))
	                        &&(!CMath.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK)))
	                        {
	                            newRace=R;
	                            break;
	                        }
	                    }
	                if(newRace!=null)
	                {
	                	StringBuilder str=CMLib.help().getHelpText(newRace.ID().toUpperCase(),mob,false);
	                    if(str!=null) session.println("\n\r^N"+str.toString()+"\n\r");
	                    if(!session.confirm("^!Is ^H"+newRace.name()+"^N^! correct (Y/n)?^N","Y"))
	                        newRace=null;
	                }
	            }
	        }
	        mob.baseCharStats().setMyRace(newRace);
	
	        mob.baseState().setHitPoints(CMProps.getIntVar(CMProps.SYSTEMI_STARTHP));
	        mob.baseState().setMovement(CMProps.getIntVar(CMProps.SYSTEMI_STARTMOVE));
	        mob.baseState().setMana(CMProps.getIntVar(CMProps.SYSTEMI_STARTMANA));
	
	        executeScript(mob,(Vector)extraScripts.get("RACE"));
	        
	        String Gender="";
	        while(Gender.length()==0)
	            Gender=session.choose("\n\r^!What is your gender (M/F)?^N","MF","");
	
	        mob.baseCharStats().setStat(CharStats.STAT_GENDER,Gender.toUpperCase().charAt(0));
	        mob.baseCharStats().getMyRace().startRacing(mob,false);
	
	        if((CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))&&(mob.playerStats()!=null)&&(acct==null))
	            mob.playerStats().setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*((long)CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS))));
	
	        executeScript(mob,(Vector)extraScripts.get("GENDER"));
	        
	        introText=new CMFile(Resources.buildResourcePath("text")+"stats.txt",null,true).text();
	    	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
	        session.println(null,null,null,"\n\r\n\r"+introText.toString());
	
	        boolean mayCont=true;
	        StringBuffer listOfClasses=new StringBuffer("??? no classes ???");
	        if(CMProps.getIntVar(CMProps.SYSTEMI_STARTSTAT)>0) {
	            mob.baseCharStats().setAllBaseValues(CMProps.getIntVar(CMProps.SYSTEMI_STARTSTAT));
	            mob.recoverCharStats();
	        }
	        else
	        while(mayCont)
	        {
	            reRollStats(mob,mob.baseCharStats());
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
	
	                int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
	                StringBuffer statstr=new StringBuffer("Your current stats are: \n\r");
	                CharStats CT=mob.charStats();
	        		for(int i : CharStats.CODES.BASE())
	                    statstr.append(CMStrings.padRight(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i)),15)
	                    		+": "+CMStrings.padRight(Integer.toString(CT.getStat(i)),2)+"/"+(max+CT.getStat(CharStats.CODES.toMAXBASE(i)))+"\n\r");
	                statstr.append(CMStrings.padRight("TOTAL POINTS",15)+": "+CMProps.getIntVar(CMProps.SYSTEMI_MAXSTAT)+"/"+(CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT)*6));
	                session.println(statstr.toString());
	                if(!CMSecurity.isDisabled("CLASSES")
	                &&!mob.baseCharStats().getMyRace().classless()
	                &&((V.size()!=1)||(!CMProps.getVar(CMProps.SYSTEM_MULTICLASS).startsWith("APP-"))))
	                	session.println("\n\rThis would qualify you for ^H"+classes.toString()+"^N.");
	
	                if(!session.confirm("^!Would you like to re-roll (y/N)?^N","N"))
	                    mayCont=false;
	            }
	        }
	        executeScript(mob,(Vector)extraScripts.get("STATS"));
	        
	        if(!CMSecurity.isDisabled("CLASSES")
	        &&!mob.baseCharStats().getMyRace().classless())
	            session.println(null,null,null,new CMFile(Resources.buildResourcePath("text")+"classes.txt",null,true).text().toString());
	
	        CharClass newClass=null;
	        Vector qualClasses=classQualifies(mob,theme);
	        if(CMSecurity.isDisabled("CLASSES")||mob.baseCharStats().getMyRace().classless())
	        {
	            if(CMSecurity.isDisabled("CLASSES"))
	                newClass=CMClass.getCharClass("PlayerClass");
	            if((newClass==null)&&(qualClasses.size()>0))
	                newClass=(CharClass)qualClasses.elementAt(CMLib.dice().roll(1,qualClasses.size(),-1));
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
	            session.print("\n\r^!Please choose from the following Classes:\n\r");
	            session.print("^H[" + listOfClasses.toString() + "]^N");
	            String ClassStr=session.prompt("\n\r: ","");
	            if(ClassStr.trim().equalsIgnoreCase("?"))
	                session.println(null,null,null,"\n\r"+new CMFile(Resources.buildResourcePath("text")+"classes.txt",null,true).text().toString());
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
	                if((newClass!=null)&&(canChangeToThisClass(mob,newClass,theme)))
	                {
	                	StringBuilder str=CMLib.help().getHelpText(newClass.ID().toUpperCase(),mob,false,false);
	                    if(str!=null){
	                    	session.println("\n\r^N"+str.toString()+"\n\r");
	                    }
	                    if(!session.confirm("^NIs ^H"+newClass.name()+"^N correct (Y/n)?","Y"))
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
	
	        CMLib.utensils().outfit(mob,mob.baseCharStats().getMyRace().outfit(mob));
	
	        if(!CMSecurity.isDisabled("ALLERGIES")) {
	            Ability A=CMClass.getAbility("Allergies");
	            if(A!=null) A.invoke(mob,mob,true,0);
	        }
	
	        mob.recoverCharStats();
	        mob.recoverEnvStats();
	        mob.recoverMaxState();
	        mob.resetToMaxState();
	
	        executeScript(mob,(Vector)extraScripts.get("CLASS"));
	        Faction F=null;
	        Vector mine=null;
	        int defaultValue=0;
	        for(Enumeration e=CMLib.factions().factions();e.hasMoreElements();)
	        {
	            F=(Faction)e.nextElement();
	            mine=F.findChoices(mob);
	            defaultValue=F.findAutoDefault(mob);
	            if(defaultValue!=Integer.MAX_VALUE)
	                mob.addFaction(F.factionID(),defaultValue);
	            if(mine.size()==1)
	                mob.addFaction(F.factionID(),((Integer)mine.firstElement()).intValue());
	            else
	            if(mine.size()>1)
	            {
	                if((F.choiceIntro()!=null)&&(F.choiceIntro().length()>0))
	                {
	                	StringBuffer intro = new CMFile(Resources.makeFileResourceName(F.choiceIntro()),null,true).text();
	                	try { intro = CMLib.httpUtils().doVirtualPage(intro);}catch(Exception ex){}
	                    session.println(null,null,null,"\n\r\n\r"+intro.toString());
	                }
	                StringBuffer menu=new StringBuffer("Select one: ");
	                Vector namedChoices=new Vector();
	                for(int m=0;m<mine.size();m++)
	                {
	                    Faction.FactionRange FR=CMLib.factions().getRange(F.factionID(),((Integer)mine.elementAt(m)).intValue());
	                    if(FR!=null)
	                    {
	                        namedChoices.addElement(FR.name().toUpperCase());
	                        menu.append(FR.name()+", ");
	                    }
	                    else
	                        namedChoices.addElement(""+((Integer)mine.elementAt(m)).intValue());
	                }
	                if(mine.size()==namedChoices.size())
	                {
	                    String alignment="";
	                    while((!namedChoices.contains(alignment))
	                    &&(!session.killFlag()))
	                    {
	                        alignment=session.prompt(menu.toString().substring(0,menu.length()-2)+".\n\r: ","").toUpperCase();
	                        if(!namedChoices.contains(alignment))
	                            for(int i=0;i<namedChoices.size();i++)
	                                if(((String)namedChoices.elementAt(i)).startsWith(alignment.toUpperCase()))
	                                { alignment=(String)namedChoices.elementAt(i); break;}
	                        if(!namedChoices.contains(alignment))
	                            for(int i=0;i<namedChoices.size();i++)
	                                if(((String)namedChoices.elementAt(i)).indexOf(alignment.toUpperCase())>=0)
	                                { alignment=(String)namedChoices.elementAt(i); break;}
	                    }
	                    if(!session.killFlag())
	                    {
	                        int valueIndex=namedChoices.indexOf(alignment);
	                        if(valueIndex>=0)
	                            mob.addFaction(F.factionID(),((Integer)mine.elementAt(valueIndex)).intValue());
	                    }
	                }
	            }
	        }
	        
	        executeScript(mob,(Vector)extraScripts.get("FACTIONS"));
	        
	        mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);
	        CMLib.utensils().outfit(mob,mob.baseCharStats().getCurrentClass().outfit(mob));
	        mob.setStartRoom(getDefaultStartRoom(mob));
	        mob.baseCharStats().setStat(CharStats.STAT_AGE,mob.playerStats().initializeBirthday(0,mob.baseCharStats().getMyRace()));
	
	        introText=new CMFile(Resources.buildResourcePath("text")+"newchardone.txt",null,true).text();
	    	try { introText = CMLib.httpUtils().doVirtualPage(introText);}catch(Exception ex){}
	        session.println(null,null,null,"\n\r\n\r"+introText.toString());
	        session.prompt("");
	        if(!session.killFlag())
	        {
	            if(emailPassword)
	            {
	                password="";
	                for(int i=0;i<6;i++)
	                    password+=(char)('a'+CMLib.dice().roll(1,26,-1));
	                mob.playerStats().setPassword(password);
	                CMLib.database().DBUpdatePassword(mob.Name(),password);
	                CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
	                          mob.Name(),
	                          mob.Name(),
	                          "Password for "+mob.Name(),
	                          "Your password for "+mob.Name()+" is: "+mob.playerStats().password()+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.");
	                session.println("Your character has been created.  You will receive an email with your password shortly.");
	                try{Thread.sleep(2000);}catch(Exception e){}
	                if(mob==session.mob())
		                session.kill(false,false,false);
	            }
	            else
	            {
	            	if(mob==session.mob())
		                reloadTerminal(mob);
	                mob.bringToLife(mob.getStartRoom(),true);
	                mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
	            }
	            mob.playerStats().leveledDateTime(0);
	            CMLib.database().DBCreateCharacter(mob);
	            CMLib.players().addPlayer(mob);
	
	            executeScript(mob,(Vector)extraScripts.get("END"));
	            
	            mob.playerStats().setLastIP(session.getAddress());
	            Log.sysOut("FrontDoor","Created user: "+mob.Name());
	            CMProps.addNewUserByIP(session.getAddress());
	            notifyFriends(mob,"^X"+mob.Name()+" has just been created.^.^?");
	            if((CMProps.getVar(CMProps.SYSTEM_PKILL).startsWith("ALWAYS"))
	            &&(!CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
	                mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
	            if((CMProps.getVar(CMProps.SYSTEM_PKILL).startsWith("NEVER"))
	            &&(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
	                mob.setBitmap(mob.getBitmap()-MOB.ATT_PLAYERKILL);
	            CMLib.database().DBUpdatePlayer(mob);
	            Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.NEWPLAYERS);
	            for(int i=0;i<channels.size();i++)
	                CMLib.commands().postChannel((String)channels.elementAt(i),mob.getClanID(),mob.Name()+" has just been created.",true);
	            CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
	            CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_NEWPLAYERS);
	        }
        }
        catch(Throwable t)
        {
        	logoff=true;
        	mob.playerStats().setAccount(null);
        	mob.setPlayerStats(null);
        	mob.setSession(null);
            session.setMob(null);
            mob.destroy();
            if((t.getMessage()!=null)&&(t.getMessage().trim().length()>0))
            	Log.errOut("CharCreation",t);
        }
        return logoff?LoginResult.NO_LOGIN:LoginResult.CCREATION_EXIT;
    }

    private boolean loginsDisabled(MOB mob)
    {
        if((CMSecurity.isDisabled("LOGINS"))&&(!CMSecurity.isASysOp(mob)))
        {
			StringBuffer rejectText=Resources.getFileResource("text/nologins.txt",true);
        	try { rejectText = CMLib.httpUtils().doVirtualPage(rejectText);}catch(Exception ex){}
			if((rejectText!=null)&&(rejectText.length()>0))
				mob.session().println(rejectText.toString());
			try{Thread.sleep(1000);}catch(Exception e){}
            mob.session().kill(false,false,false);
            return true;
        }
        return false;
    }

    public LoginResult prelimChecks(Session session, String login, PlayerLibrary.ThinnerPlayer player)
    {
        if(CMSecurity.isBanned(login))
        {
            session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
            session.kill(false,false,false);
            return LoginResult.NO_LOGIN;
        }
        if((player.email!=null)&&CMSecurity.isBanned(player.email))
        {
            session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
            session.kill(false,false,false);
            return LoginResult.NO_LOGIN;
        }
        for(int s=0;s<CMLib.sessions().size();s++)
        {
            Session thisSession=CMLib.sessions().elementAt(s);
        	MOB M=thisSession.mob();
            if((M!=null)
            &&(thisSession!=session)
            &&(M==player.loadedMOB))
            {
                Room oldRoom=M.location();
                if(oldRoom!=null)
                    while(oldRoom.isInhabitant(M))
                        oldRoom.delInhabitant(M);
                session.setMob(M);
                M.setSession(session);
                thisSession.setMob(null);
                thisSession.kill(false,false,false);
                Log.sysOut("FrontDoor","Session swap for "+session.mob().Name()+".");
                reloadTerminal(session.mob());
                session.mob().bringToLife(oldRoom,false);
                return LoginResult.SESSION_SWAP;
            }
        }
        return null;
    }
    
    public void notifyFriends(MOB mob, String message)
    {
    	try {
	        for(int s=0;s<CMLib.sessions().size();s++)
	        {
	            Session sessionS=CMLib.sessions().elementAt(s);
	            if(sessionS!=null)
	            {
	            	MOB listenerM=sessionS.mob();
		            if((listenerM!=null)
		            &&(listenerM!=mob)
		            &&((!CMLib.flags().isCloaked(mob))||(CMSecurity.isASysOp(listenerM)))
		            &&(CMath.bset(listenerM.getBitmap(),MOB.ATT_AUTONOTIFY)))
		            {
		            	PlayerStats listenerPStats=listenerM.playerStats();
			            if((listenerPStats!=null)
			            &&((listenerPStats.getFriends().contains(mob.Name())||listenerPStats.getFriends().contains("All"))))
			            	listenerM.tell(message);
		            }
	            }
	        }
    	} catch(Exception e){}
    }

    private String getMSSPPacket()
    {
    	StringBuffer rpt = new StringBuffer("\r\nMSSP-REPLY-START");
    	rpt.append("\r\n"); rpt.append("PLAYERS");
    	rpt.append("\t"); rpt.append(Integer.toString(CMLib.sessions().size()));
    	rpt.append("\r\n"); rpt.append("STATUS");
    	rpt.append("\t");
    	switch(CMProps.getIntVar(CMProps.SYSTEMI_MUDSTATE))
    	{
    	case 0: rpt.append("Alpha"); break; 
    	case 1: rpt.append("Closed Beta"); break; 
    	case 2: rpt.append("Open Beta"); break; 
    	case 3: rpt.append("Live"); break;
    	default : rpt.append("Live"); break;
    	}
    	
    	MudHost host = null;
    	if(CMLib.hosts().size()>0)
    		host = (MudHost)CMLib.hosts().firstElement();
    	if(host != null)
    	{
        	rpt.append("\r\n"); rpt.append("UPTIME");
        	rpt.append("\t"); rpt.append(Long.toString(host.getUptimeSecs()));
        	rpt.append("\r\n"); rpt.append("HOSTNAME");
        	rpt.append("\t"); rpt.append(host.getHost());
        	rpt.append("\r\n"); rpt.append("PORT");
        	rpt.append("\t"); rpt.append(Integer.toString(host.getPort()));
        	rpt.append("\r\n"); rpt.append("WEBSITE");
        	rpt.append("\t"); rpt.append(("http://"+host.getHost()+":"+CMLib.httpUtils().getWebServerPort()));
        	rpt.append("\r\n"); rpt.append("LANGUAGE");
        	rpt.append("\t"); rpt.append(host.getLanguage());
    	}
    	if(CMLib.intermud().i3online())
    	{
        	rpt.append("\r\n"); rpt.append("INTERMUD");
        	rpt.append("\t"); rpt.append("I3");
    	}
    	if(CMLib.intermud().imc2online())
    	{
        	rpt.append("\r\n"); rpt.append("INTERMUD");
        	rpt.append("\t"); rpt.append("IMC2");
    	}
    	rpt.append("\r\n"); rpt.append("FAMILY");
    	rpt.append("\t"); rpt.append("CoffeeMUD");
    	rpt.append("\r\n"); rpt.append("EMAIL");
    	rpt.append("\t"); rpt.append(CMProps.getVar(CMProps.SYSTEM_ADMINEMAIL));
    	rpt.append("\r\n"); rpt.append("CODEBASE");
    	rpt.append("\t"); rpt.append(("CoffeeMud v"+CMProps.getVar(CMProps.SYSTEM_MUDVER)));
    	rpt.append("\r\n"); rpt.append("AREAS");
    	rpt.append("\t"); rpt.append(Integer.toString(CMLib.map().numAreas()));
    	rpt.append("\r\n"); rpt.append("HELPFILES");
    	rpt.append("\t"); rpt.append(Integer.toString(CMLib.help().getHelpFile().size()));
    	rpt.append("\r\n"); rpt.append("MOBILES");
    	rpt.append("\t"); rpt.append(Long.toString(CMClass.numRemainingObjectCounts(CMClass.OBJECT_MOB)-CMClass.numPrototypes(CMClass.OBJECT_MOB)));
    	rpt.append("\r\n"); rpt.append("OBJECTS");
    	rpt.append("\t"); rpt.append(Long.toString(CMClass.numRemainingObjectCounts(CMClass.OBJECT_ITEM)-CMClass.numPrototypes(CMClass.OBJECTS_ITEMTYPES)));
    	rpt.append("\r\n"); rpt.append("ROOMS");
    	rpt.append("\t"); rpt.append(Long.toString(CMLib.map().numRooms()));
    	rpt.append("\r\n"); rpt.append("CLASSES");
    	int numClasses = 0;
        if(!CMSecurity.isDisabled("CLASSES"))
        	numClasses=CMLib.login().classQualifies(null, CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME)&0x07).size();
    	rpt.append("\t"); rpt.append(Long.toString(numClasses));
    	rpt.append("\r\n"); rpt.append("RACES");
    	int numRaces = 0;
        if(!CMSecurity.isDisabled("RACES"))
        	numRaces=CMLib.login().raceQualifies(null, CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME)&0x07).size();
    	rpt.append("\t"); rpt.append(Long.toString(numRaces));
    	rpt.append("\r\n"); rpt.append("SKILLS");
    	rpt.append("\t"); rpt.append(Long.toString(CMLib.ableMapper().numMappedAbilities()));
    	rpt.append("\r\n"); rpt.append("ANSI");
    	rpt.append("\t"); rpt.append((this!=null?"1":"0"));
    	rpt.append("\r\n"); rpt.append("MCCP");
    	rpt.append("\t"); rpt.append((!CMSecurity.isDisabled("MCCP")?"1":"0"));
    	rpt.append("\r\n"); rpt.append("MSP");
    	rpt.append("\t"); rpt.append((!CMSecurity.isDisabled("MSP")?"1":"0"));
    	rpt.append("\r\n"); rpt.append("MXP");
    	rpt.append("\t"); rpt.append((!CMSecurity.isDisabled("MXP")?"1":"0"));
    	rpt.append("\r\nMSSP-REPLY-END\r\n");
    	return rpt.toString();
    }
    
    public LoginResult login(Session session, int attempt)
        throws java.io.IOException
    {
        if(session==null) 
        	return LoginResult.NO_LOGIN;

        boolean wizi=false;

        session.setAccount(null);
        
        String login;
        if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
        	login=session.prompt("\n\raccount name: ");
        else
        	login=session.prompt("\n\rname: ");
        if(login==null) 
        	return LoginResult.NO_LOGIN;
        login=login.trim();
        if(login.length()==0) 
        	return LoginResult.NO_LOGIN;
        if(login.equalsIgnoreCase("MSSP-REQUEST")&&(!CMSecurity.isDisabled("MSSP")))
        {
        	session.rawOut(getMSSPPacket());
            session.kill(false,false,false);
        	return LoginResult.NO_LOGIN;
        }
        	
        if(login.endsWith(" !"))
        {
            login=login.substring(0,login.length()-2);
            login=login.trim();
            wizi=true;
        }
        login = CMStrings.capitalizeAndLower(login);
        PlayerAccount acct = null;
        PlayerLibrary.ThinnerPlayer player = null;
        if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
        {
        	acct = CMLib.players().getLoadAccount(login);
        	if(acct!=null)
        	{
        		player=new PlayerLibrary.ThinnerPlayer();
        		player.name=acct.accountName();
        		player.accountName=acct.accountName();
        		player.email=acct.getEmail();
        		player.expiration=acct.getAccountExpiration();
        		player.password=acct.password();
        	}
        	else
        	{
	            player=CMLib.database().DBUserSearch(login);
	            if((player != null)
	            &&((player.accountName==null)
            		||(player.accountName.trim().length()==0)))
	            {
		            session.print("password for "+player.name+": ");
		            String password=session.blockingIn();
		            boolean done = true;
		            if(password.equalsIgnoreCase(player.password))
		            {
		            	session.println("\n\rThis mud is now using an account system.  "
		            			+"Please create a new account and use the IMPORT command to add your character(s) to your account.");
		            	done = !session.confirm("Would you like to create your new master account and call it '"+player.name+"' (y/N)? ", "N");
		            }
		            player = null;
		            if(done)
		            	return LoginResult.NO_LOGIN;
	            }
	            else
	            if(player!=null)
	            {
	                session.println("\n\rAccount '"+CMStrings.capitalizeAndLower(login)+"' does not exist.");
	            	player=null;
	            	return LoginResult.NO_LOGIN;
	            }
        	}
        }
        else
        {
        	MOB mob=CMLib.players().getPlayer(login);
        	if((mob!=null)&&(mob.playerStats()!=null))
        	{
        		player=new PlayerLibrary.ThinnerPlayer();
        		player.name=mob.Name();
        		player.email=mob.playerStats().getEmail();
        		player.expiration=mob.playerStats().getAccountExpiration();
        		player.password=mob.playerStats().password();
        		player.loadedMOB=mob;
        	}
	        else
	            player=CMLib.database().DBUserSearch(login);
        }
        if(player!=null)
        {
            try
            {
                Long L=(Long)pendingLogins.get(login.toUpperCase());
                if((L!=null)&&((System.currentTimeMillis()-L.longValue())<(10*60*1000)))
                {
                    session.println("A previous login is still pending.  Please be patient.");
                    return LoginResult.NO_LOGIN;
                }
                pendingLogins.put(login.toUpperCase(),Long.valueOf(System.currentTimeMillis()));
                
	            session.print("password: ");
	            String password=session.blockingIn();
	            if(password.equalsIgnoreCase(player.password))
	            {
	
	            	LoginResult prelimResults = prelimChecks(session,login,player);
	            	if(prelimResults!=null)
	            		return prelimResults;
	            	
	                if(acct!=null)
	                {
		                if(isExpired(acct,session,player.expiration)) 
		                	return LoginResult.NO_LOGIN;
		                session.setAccount(acct);
		                return LoginResult.ACCOUNT_LOGIN;
	                }
	                else
	                {
		                LoginResult completeResult=completeCharacterLogin(session,login, wizi);
		                if(completeResult == LoginResult.NO_LOGIN)
		                	return completeResult;
	                }
	            }
	            else
	            {
	                Log.sysOut("FrontDoor","Failed login: "+player.name);
	                session.println("\n\rInvalid password.\n\r");
	                if((!session.killFlag())
	                &&(player.email.length()>0)
	                &&(player.email.indexOf("@")>0)
	                &&(attempt>2)
	                &&(CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).length()>0))
	                {
	                    if(session.confirm("Would you like you have your password e-mailed to you (y/N)? ","N"))
	                    {
	                        if(CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),
	                                                   "passwords@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
	                                                   "noreply@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
	                                                   player.email,
	                                                   "Password for "+player.name,
	                                                   "Your password for "+player.name+" at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" is: '"+player.password+"'."))
	                            session.println("Email sent.\n\r");
	                        else
	                            session.println("Error sending email.\n\r");
	                        session.kill(false,false,false);
	                    }
	                }
	                return LoginResult.NO_LOGIN;
	            }
            }
            finally
            {
            	if(login!=null) pendingLogins.remove(login.toUpperCase().trim());
            	if(acct!=null) pendingLogins.remove(acct.accountName().toUpperCase().trim());
            	if(player!=null)
            	{
	            	pendingLogins.remove(player.name.toUpperCase().trim());
	            	if(player.accountName!=null) pendingLogins.remove(player.accountName.toUpperCase().trim());
	            	if((player.loadedMOB!=null)&&(player.loadedMOB.playerStats()!=null)&&(player.loadedMOB.playerStats().getAccount()!=null))
	            		pendingLogins.remove(player.loadedMOB.playerStats().getAccount().accountName().toUpperCase().trim());
            	}
            }
        }
        else
        {
        	if(newCharactersAllowed(login,session,!(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)))
        	{
	            if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
	            {
	                if(session.confirm("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rIs this a new account you would like to create (y/N)?","N"))
	                {
	                	acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
	                	return createAccount(acct,login,session);
	                }
	            }
	            else
	            if(session.confirm("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
	            {
	            	LoginResult result = LoginResult.NO_LOGIN;
	            	if(createCharacter(acct,login,session)==LoginResult.CCREATION_EXIT)
	            		result = LoginResult.NORMAL_LOGIN;
	                return result;
	            }
        	}
            return LoginResult.NO_LOGIN;
        }
        if(session!=null)
            session.println("\n\r");
        return LoginResult.NORMAL_LOGIN;
    }

    public boolean newCharactersAllowed(String login, Session session, boolean checkPlayerName)
    {
        if(CMSecurity.isDisabled("NEWPLAYERS"))
        {
            session.print("\n\r'"+CMStrings.capitalizeAndLower(login)+"' is not recognized.\n\r");
            return false;
        }
        else
        if((!isOkName(login))
        || (checkPlayerName && CMLib.players().playerExists(login))
        || (!checkPlayerName && CMLib.players().accountExists(login)))
        {
            session.println("\n\r'"+CMStrings.capitalizeAndLower(login)+"' is not recognized.\n\rThat name is also not available for new players.\n\r  Choose another name (no spaces allowed)!\n\r");
            return false;
        }
        else
        if((CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME)==0)
        ||(CMSecurity.isDisabled("LOGINS")))
        {
            session.print("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rThis server is not accepting new accounts.\n\r\n\r");
            return false;
        }
        else
        if((CMProps.getIntVar(CMProps.SYSTEMI_MAXNEWPERIP)>0)
        &&(CMProps.getCountNewUserByIP(session.getAddress())>=CMProps.getIntVar(CMProps.SYSTEMI_MAXNEWPERIP))
        &&(!CMSecurity.isDisabled("MAXNEWPERIP")))
        {
            session.println("\n\rThat name is unrecognized.\n\rAlso, the maximum daily new player limit has already been reached for your location.");
            return false;
        }
        return true;
    }

    public void setGlobalBitmaps(MOB mob)
    {
    	if(mob==null) return;
		Vector defaultFlagsV=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_DEFAULTPLAYERFLAGS).toUpperCase(),true);
		for(int v=0;v<defaultFlagsV.size();v++)
		{
			int x=CMParms.indexOf(MOB.AUTODESC,(String)defaultFlagsV.elementAt(v));
			if(x>=0)
				mob.setBitmap(mob.getBitmap()|(int)CMath.pow(2,x));
		}
    }
    
    public LoginResult completeCharacterLogin(Session session, String login, boolean wiziFlag) throws java.io.IOException
    {
        // count number of multiplays
        int numAtAddress=0;
        try{
        for(int s=0;s<CMLib.sessions().size();s++)
        {
            if((CMLib.sessions().elementAt(s)!=session)
            &&(session.getAddress().equalsIgnoreCase((CMLib.sessions().elementAt(s).getAddress()))))
                numAtAddress++;
        }
        }catch(Exception e){}

        if((CMProps.getIntVar(CMProps.SYSTEMI_MAXCONNSPERIP)>0)
        &&(numAtAddress>=CMProps.getIntVar(CMProps.SYSTEMI_MAXCONNSPERIP))
        &&(!CMSecurity.isDisabled("MAXCONNSPERIP")))
        {
            session.println("The maximum player limit has already been reached for your IP address.");
            return LoginResult.NO_LOGIN;
        }
        
        MOB mob=CMLib.players().getPlayer(login);
        if((mob!=null)&&(mob.session()!=null))
        {
            session.setMob(mob);
            mob.setSession(session);
            if(isExpired(mob.playerStats().getAccount(),session,mob.playerStats().getAccountExpiration())) 
            	return LoginResult.NO_LOGIN;
            if(loginsDisabled(mob))
            	return LoginResult.NO_LOGIN;
            if(wiziFlag)
            {
                Command C=CMClass.getCommand("WizInv");
                if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
                    C.execute(mob,CMParms.makeVector("WIZINV"),0);
            }
            showTheNews(mob);
            mob.bringToLife(mob.location(),false);
            CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
            mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
        }
        else
        {
        	mob=CMLib.players().getLoadPlayer(login);
        	mob.setSession(session);
        	session.setMob(mob);
            if(isExpired(mob.playerStats().getAccount(),session,mob.playerStats().getAccountExpiration())) 
            	return LoginResult.NO_LOGIN;
            if(loginsDisabled(mob))
            	return LoginResult.NO_LOGIN;
            if(wiziFlag)
            {
                Command C=CMClass.getCommand("WizInv");
                if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
                    C.execute(mob,CMParms.makeVector("WIZINV"),0);
            }
            showTheNews(mob);
            mob.bringToLife(mob.location(),true);
            CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
            mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
        }
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
                follower.setFollowing(mob); // before for bestow names sake
                follower.bringToLife(R,false);
                follower.setFollowing(mob);
                R.showOthers(follower,R,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
            }
        }
        PlayerStats pstats = mob.playerStats();
        if(((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
        &&(!CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION")))
	    {
	        Command C=CMClass.getCommand("Email");
	        if(C!=null)
	        {
	            if(!C.execute(mob,null,0))
	            {
	    	        session.kill(false,false,false);
	                return LoginResult.NO_LOGIN;
	            }
	        }
	        CMLib.database().DBUpdateEmail(mob);
	    }
	    if((pstats.getEmail()!=null)&&CMSecurity.isBanned(pstats.getEmail()))
	    {
	        session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
	        session.kill(false,false,false);
	        return LoginResult.NO_LOGIN;
	    }
        if((session!=null)&&(mob.playerStats()!=null))
            mob.playerStats().setLastIP(session.getAddress());
        notifyFriends(mob,"^X"+mob.Name()+" has logged on.^.^?");
        if((CMProps.getVar(CMProps.SYSTEM_PKILL).startsWith("ALWAYS"))
        &&(!CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
            mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
        if((CMProps.getVar(CMProps.SYSTEM_PKILL).startsWith("NEVER"))
        &&(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
            mob.setBitmap(mob.getBitmap()-MOB.ATT_PLAYERKILL);
        Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOGINS);
        if(!CMLib.flags().isCloaked(mob))
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel((String)channels.elementAt(i),mob.getClanID(),mob.Name()+" has logged on.",true);
        setGlobalBitmaps(mob);
        return LoginResult.NORMAL_LOGIN;
    }

    public Room getDefaultStartRoom(MOB mob)
    {
        String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
        race=race.replace(' ','_');
        String charClass=mob.baseCharStats().getCurrentClass().ID().toUpperCase();
        charClass=charClass.replace(' ','_');
        String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
        realrace=realrace.replace(' ','_');
        String deity=mob.getWorshipCharID().toUpperCase();
        deity=deity.replace(' ','_');
        String align=CMLib.flags().getAlignmentName(mob);
        String roomID=(String)startRooms.get(race);
        if((roomID==null)||(roomID.length()==0))
            roomID=(String)startRooms.get(realrace);
        if(((roomID==null)||(roomID.length()==0)))
            roomID=(String)startRooms.get(align);
        if(((roomID==null)||(roomID.length()==0)))
            roomID=(String)startRooms.get(charClass);
        if(((roomID==null)||(roomID.length()==0)))
        {
            Vector V=mob.fetchFactionRanges();
            for(int v=0;v<V.size();v++)
                if(startRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
                { roomID=(String)startRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
        }
        if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
            roomID=(String)startRooms.get(deity);
        if((roomID==null)||(roomID.length()==0))
            roomID=(String)startRooms.get("ALL");

        Room room=null;
        if((roomID!=null)&&(roomID.length()>0))
            room=CMLib.map().getRoom(roomID);
        if(room==null)
            room=CMLib.map().getRoom("START");
        if((room==null)&&(CMLib.map().numRooms()>0))
            room=(Room)CMLib.map().rooms().nextElement();
        return room;
    }

    public Room getDefaultDeathRoom(MOB mob)
    {
        String charClass=mob.baseCharStats().getCurrentClass().ID().toUpperCase();
        charClass=charClass.replace(' ','_');
        String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
        race=race.replace(' ','_');
        String deity=mob.getWorshipCharID().toUpperCase();
        deity=deity.replace(' ','_');
        String align=CMLib.flags().getAlignmentName(mob);
        String roomID=(String)deathRooms.get(race);
        if(((roomID==null)||(roomID.length()==0)))
            roomID=(String)deathRooms.get(align);
        if(((roomID==null)||(roomID.length()==0)))
            roomID=(String)deathRooms.get(charClass);
        if(((roomID==null)||(roomID.length()==0)))
        {
            Vector V=mob.fetchFactionRanges();
            for(int v=0;v<V.size();v++)
                if(deathRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
                { roomID=(String)deathRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
        }
        if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
            roomID=(String)deathRooms.get(deity);
        if((roomID==null)||(roomID.length()==0))
            roomID=(String)deathRooms.get("ALL");

        Room room=null;
        if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
            room=mob.getStartRoom();
        if((room==null)&&(roomID!=null)&&(roomID.length()>0))
            room=CMLib.map().getRoom(roomID);
        if(room==null)
            room=mob.getStartRoom();
        if((room==null)&&(CMLib.map().numRooms()>0))
            room=(Room)CMLib.map().rooms().nextElement();
        return room;
    }

    public Room getDefaultBodyRoom(MOB mob)
    {
        if((mob.getClanID().length()>0)
        &&(mob.getClanRole()!=Clan.POS_APPLICANT)
        &&((!mob.isMonster())||(mob.getStartRoom()==null)))
        {
            Clan C=CMLib.clans().getClan(mob.getClanID());
            if((C!=null)&&(C.getMorgue().length()>0))
            {
                Room room=CMLib.map().getRoom(C.getMorgue());
                if((room!=null)&&(CMLib.law().doesHavePriviledgesHere(mob,room)))
                    return room;
            }
        }
        String charClass=mob.baseCharStats().getCurrentClass().ID().toUpperCase();
        charClass=charClass.replace(' ','_');
        String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
        race=race.replace(' ','_');
        String realrace=mob.baseCharStats().getMyRace().ID().toUpperCase();
        realrace=realrace.replace(' ','_');
        String deity=mob.getWorshipCharID().toUpperCase();
        deity=deity.replace(' ','_');
        String align=CMLib.flags().getAlignmentName(mob);
        String roomID=(String)bodyRooms.get(race);
        if((roomID==null)||(roomID.length()==0))
            roomID=(String)bodyRooms.get(realrace);
        if(((roomID==null)||(roomID.length()==0)))
            roomID=(String)bodyRooms.get(align);
        if(((roomID==null)||(roomID.length()==0)))
            roomID=(String)bodyRooms.get(charClass);
        if(((roomID==null)||(roomID.length()==0)))
        {
            Vector V=mob.fetchFactionRanges();
            for(int v=0;v<V.size();v++)
                if(bodyRooms.containsKey(((String)V.elementAt(v)).toUpperCase()))
                { roomID=(String)bodyRooms.get(((String)V.elementAt(v)).toUpperCase()); break;}
        }
        if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
            roomID=(String)bodyRooms.get(deity);
        if((roomID==null)||(roomID.length()==0))
            roomID=(String)bodyRooms.get("ALL");

        Room room=null;
        if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
            room=mob.location();
        if((room==null)&&(roomID!=null)&&(roomID.length()>0))
            room=CMLib.map().getRoom(roomID);
        if(room==null)
            room=mob.location();
        if((room==null)&&(CMLib.map().numRooms()>0))
            room=(Room)CMLib.map().rooms().nextElement();
        return room;
    }

    public void pageRooms(CMProps page, Hashtable table, String start)
    {
        for(Enumeration i=page.keys();i.hasMoreElements();)
        {
            String k=(String)i.nextElement();
            if(k.startsWith(start+"_"))
                table.put(k.substring(start.length()+1),page.getProperty(k));
        }
        String thisOne=page.getProperty(start);
        if((thisOne!=null)&&(thisOne.length()>0))
            table.put("ALL",thisOne);
    }

    public void initStartRooms(CMProps page)
    {
        startRooms=new Hashtable();
        pageRooms(page,startRooms,"START");
    }

    public void initDeathRooms(CMProps page)
    {
        deathRooms=new Hashtable();
        pageRooms(page,deathRooms,"DEATH");
    }

    public void initBodyRooms(CMProps page)
    {
        bodyRooms=new Hashtable();
        pageRooms(page,bodyRooms,"MORGUE");
    }

    public boolean shutdown() {
        bodyRooms=new Hashtable();
        startRooms=new Hashtable();
        deathRooms=new Hashtable();
        return true;
    }

}
