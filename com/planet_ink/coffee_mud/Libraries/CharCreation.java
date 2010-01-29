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
    public Hashtable pendingLogins=new Hashtable();
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

    public boolean classOkForMe(MOB mob, CharClass thisClass, int theme)
    {
        if((CMProps.isTheme(thisClass.availabilityCode()))
           &&(CMath.bset(thisClass.availabilityCode(),theme))
           &&(!CMath.bset(thisClass.availabilityCode(),Area.THEME_SKILLONLYMASK))
           &&((CMProps.getVar(CMProps.SYSTEM_MULTICLASS).startsWith("NO"))
              ||(CMProps.getVar(CMProps.SYSTEM_MULTICLASS).startsWith("MULTI"))
              ||(thisClass.baseClass().equals(thisClass.ID())
              ||(thisClass.ID().equals("Apprentice"))))
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
            if(classOkForMe(mob,C,theme))
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
            if((" YOU SHIT FUCK CUNT ALL FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP GOD JESUS CHRIST NOBODY SOMEBODY MESSIAH ADMIN SYSOP ").indexOf(" "+str+" ")>=0)
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

        for(Enumeration e=CMLib.players().players();e.hasMoreElements();)
        {
            MOB tm=(MOB)e.nextElement();
            if((CMLib.english().containsString(tm.ID(),login))
            ||(CMLib.english().containsString(tm.Name(),login)))
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

    public boolean checkExpiration(MOB mob)
    {
        if(!CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION)) return true;
        MOB newMob=CMLib.players().getLoadPlayer(mob.Name());
        if(CMSecurity.isASysOp(newMob)) return true;
        if((newMob.playerStats()!=null)
        &&(newMob.playerStats().getAccountExpiration()<=System.currentTimeMillis()))
        {
            mob.tell("\n\r"+CMProps.getVar(CMProps.SYSTEM_EXPCONTACTLINE)+"\n\r\n\r");
            mob.session().logoff(false,false,false);
            if(pendingLogins.containsKey(mob.Name().toUpperCase()))
               pendingLogins.remove(mob.Name().toUpperCase());
            return false;
        }
        return true;
    }

    private void executeScript(MOB mob, Vector scripts) {
        if(scripts==null) return;
        for(int s=0;s<scripts.size();s++) {
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

    

    public boolean createAccountCharacter(PlayerAccount acct, Session session, MOB mob) throws java.io.IOException
    {
        String charName = "";
        while((charName.length()==0)&&(!session.killFlag()))
        {
        	charName=session.prompt("\n\rEnter a name for your first character\n\r: ","");
            if(charName.length()==0)
            {
            	session.println("\n\rAborting account creation");
            	return false;
            }
            else
            if((!isOkName(charName))
            ||(CMLib.players().getAccount(charName)!=null)
            ||(CMLib.players().getLoadAccount(charName)!=null))
            {
                mob.session().println("That name is not available for new characters.\n\r  Choose another name (no spaces allowed)!\n\r");
                charName="";
            }
        }
        return this.createCharacter(mob, charName, session);
    }
    
    public boolean selectAccountCharacter(PlayerAccount acct, Session session, MOB mob) throws java.io.IOException
    {
    	Session sess = mob.session();
    	if((acct==null)||(sess==null)||(sess.killFlag()))
    		return false;
    	
    	boolean charSelected = false;
    	while((!sess.killFlag())&&(!charSelected))
    	{
    		
    	}
    	// ...
    	
    	Long L=(Long)pendingLogins.remove(acct.accountName().toUpperCase());
    	if(L!=null) pendingLogins.put(mob.Name().toUpperCase(), L);
    	return true;
    }
    
    public boolean createAccount(PlayerAccount acct, MOB mob, String login, Session session)
	    throws java.io.IOException
	{
    	Hashtable extraScripts = getLoginScripts();
        
        login=CMStrings.capitalizeAndLower(login.trim());
        
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
	            	return false;
	            }
	        }
        executeScript(mob,(Vector)extraScripts.get("PASSWORD"));
        
    	String emailAddy = getEmailAddress(session, null);
    	if(emailAddy == null)
        {
        	session.println("\n\rAborting account creation.");
        	return false;
        }
        acct.setAccountName(login);
        acct.setPassword(password);
        acct.setEmail(emailAddy);
        acct.setLastDateTime(System.currentTimeMillis());
        if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
            acct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*((long)CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS))));
        executeScript(mob,(Vector)extraScripts.get("EMAIL"));
        
        CMLib.database().DBCreateAccount(acct);
        StringBuffer doneText=new CMFile(Resources.buildResourcePath("text")+"doneacct.txt",null,true).text();
    	try { doneText = CMLib.httpUtils().doVirtualPage(doneText);}catch(Exception ex){}
        session.println(null,null,null,"\n\r\n\r"+doneText.toString());
        
        if(emailPassword)
        {
            password="";
            for(int i=0;i<6;i++)
                password+=(char)('a'+CMLib.dice().roll(1,26,-1));
            acct.setPassword(password);
            CMLib.database().DBUpdatePassword(mob);
            CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
                      acct.accountName(),
                      acct.accountName(),
                      "Password for "+acct.accountName(),
                      "Your password for "+acct.accountName()+" is: "+acct.password()+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".\n\rAfter creating a character, you may use the PASSWORD command to change it once you are online.",-1);
            session.println("Your account has been created.  You will receive an email with your password shortly.");
            try{Thread.sleep(2000);}catch(Exception e){}
            if(mob.session()==session)
            {
                session.logoff(false,false,false);
	            return false;
            }
        }
        mob.setName(acct.accountName());
        return selectAccountCharacter(acct,session,mob);
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
            String newEmail=session.prompt("\n\rEnter your e-mail address:");
            String confirmEmail=newEmail;
            if(emailPassword) session.println("This email address will be used to send you a password.");
            if(emailReq) confirmEmail=session.prompt("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter:");
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
    
    public boolean createCharacter(MOB mob, String login, Session session)
        throws java.io.IOException
    {
    	
        PlayerAccount acct = mob.playerStats().getAccount();
        
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

        mob.setName(login);

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
            if(mob.session()==session)
            	session.logoff(false,false,false);
            if(pendingLogins.containsKey(mob.Name().toUpperCase()))
               pendingLogins.remove(mob.Name().toUpperCase());
            return false;
        }

        Log.sysOut("FrontDoor","Creating user: "+mob.Name());
        executeScript(mob,(Vector)extraScripts.get("EMAIL"));

        mob.setBitmap(MOB.ATT_AUTOEXITS|MOB.ATT_AUTOWEATHER);
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
                    StringBuffer str=CMLib.help().getHelpText(newRace.ID().toUpperCase(),mob,false);
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
                &&!mob.baseCharStats().getMyRace().classless())
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
                if((newClass!=null)&&(classOkForMe(mob,newClass,theme)))
                {
                    StringBuffer str=CMLib.help().getHelpText(newClass.ID().toUpperCase(),mob,false);
                    if(str!=null) session.println("\n\r^N"+str.toString()+"\n\r");
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
        boolean logoff=false;
        if(!session.killFlag())
        {
            if(emailPassword)
            {
                password="";
                for(int i=0;i<6;i++)
                    password+=(char)('a'+CMLib.dice().roll(1,26,-1));
                mob.playerStats().setPassword(password);
                CMLib.database().DBUpdatePassword(mob);
                CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
                          mob.Name(),
                          mob.Name(),
                          "Password for "+mob.Name(),
                          "Your password for "+mob.Name()+" is: "+mob.playerStats().password()+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.",-1);
                session.println("Your character has been created.  You will receive an email with your password shortly.");
                try{Thread.sleep(2000);}catch(Exception e){}
                if(mob.session()==session)
	                session.logoff(false,false,false);
            }
            else
            {
            	if(mob.session()==session)
	                reloadTerminal(mob);
                mob.bringToLife(mob.getStartRoom(),true);
                mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
            }
            mob.playerStats().leveledDateTime(0);
            CMLib.database().DBCreateCharacter(mob);
            CMLib.players().addPlayer(mob);

            executeScript(mob,(Vector)extraScripts.get("END"));
            
            if(mob.playerStats()==null) return false;
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
        if(pendingLogins.containsKey(mob.Name().toUpperCase()))
           pendingLogins.remove(mob.Name().toUpperCase());
        return !logoff;
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
            mob.session().logoff(false,false,false);
            if(pendingLogins.containsKey(mob.Name().toUpperCase()))
               pendingLogins.remove(mob.Name().toUpperCase());
            return true;
        }
        return false;
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
    	rpt.append("\r\n"); rpt.append(Integer.toString(CMLib.sessions().size()));
    	rpt.append("\r\n"); rpt.append("STATUS");
    	rpt.append("\r\n"); rpt.append(CMProps.getVar(CMProps.SYSTEM_MUDSTATE));
    	MudHost host = null;
    	if(CMLib.hosts().size()>0)
    		host = (MudHost)CMLib.hosts().firstElement();
    	if(host != null)
    	{
        	rpt.append("\r\n"); rpt.append("UPTIME");
        	rpt.append("\r\n"); rpt.append(Long.toString(host.getUptimeSecs()));
        	rpt.append("\r\n"); rpt.append("HOSTNAME");
        	rpt.append("\r\n"); rpt.append(host.getHost());
        	rpt.append("\r\n"); rpt.append("PORT");
        	rpt.append("\r\n"); rpt.append(Integer.toString(host.getPort()));
        	rpt.append("\r\n"); rpt.append("WEBSITE");
        	rpt.append("\r\n"); rpt.append(("http://"+host.getHost()+":"+CMLib.httpUtils().getWebServerPort()));
        	rpt.append("\r\n"); rpt.append("LANGUAGE");
        	rpt.append("\r\n"); rpt.append(host.getLanguage());
    	}
    	if(CMLib.intermud().i3online())
    	{
        	rpt.append("\r\n"); rpt.append("INTERMUD");
        	rpt.append("\r\n"); rpt.append("I3");
    	}
    	if(CMLib.intermud().imc2online())
    	{
        	rpt.append("\r\n"); rpt.append("INTERMUD");
        	rpt.append("\r\n"); rpt.append("IMC2");
    	}
    	rpt.append("\r\n"); rpt.append("FAMILY");
    	rpt.append("\r\n"); rpt.append("CoffeeMUD");
    	rpt.append("\r\n"); rpt.append("EMAIL");
    	rpt.append("\r\n"); rpt.append(CMProps.getVar(CMProps.SYSTEM_ADMINEMAIL));
    	rpt.append("\r\n"); rpt.append("CODEBASE");
    	rpt.append("\r\n"); rpt.append(("CoffeeMud v"+CMProps.getVar(CMProps.SYSTEM_MUDVER)));
    	rpt.append("\r\n"); rpt.append("AREAS");
    	rpt.append("\r\n"); rpt.append(Integer.toString(CMLib.map().numAreas()));
    	rpt.append("\r\n"); rpt.append("HELPFILES");
    	rpt.append("\r\n"); rpt.append(Integer.toString(CMLib.help().getHelpFile().size()));
    	rpt.append("\r\n"); rpt.append("MOBILES");
    	rpt.append("\r\n"); rpt.append(Long.toString(CMClass.numRemainingObjectCounts(CMClass.OBJECT_MOB)-CMClass.numPrototypes(CMClass.OBJECT_MOB)));
    	rpt.append("\r\n"); rpt.append("OBJECTS");
    	rpt.append("\r\n"); rpt.append(Long.toString(CMClass.numRemainingObjectCounts(CMClass.OBJECT_ITEM)-CMClass.numPrototypes(CMClass.OBJECTS_ITEMTYPES)));
    	rpt.append("\r\n"); rpt.append("ROOMS");
    	rpt.append("\r\n"); rpt.append(Long.toString(CMLib.map().numRooms()));
    	rpt.append("\r\n"); rpt.append("CLASSES");
    	int numClasses = 0;
        if(!CMSecurity.isDisabled("CLASSES"))
        	numClasses=CMLib.login().classQualifies(null, CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME)&0x07).size();
    	rpt.append("\r\n"); rpt.append(Long.toString(numClasses));
    	rpt.append("\r\n"); rpt.append("RACES");
    	int numRaces = 0;
        if(!CMSecurity.isDisabled("RACES"))
        	numRaces=CMLib.login().raceQualifies(null, CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME)&0x07).size();
    	rpt.append("\r\n"); rpt.append(Long.toString(numRaces));
    	rpt.append("\r\n"); rpt.append("SKILLS");
    	rpt.append("\r\n"); rpt.append(Long.toString(CMLib.ableMapper().numMappedAbilities()));
    	rpt.append("\r\n"); rpt.append("ANSI");
    	rpt.append("\r\n"); rpt.append((this!=null?"1":"0"));
    	rpt.append("\r\n"); rpt.append("MCCP");
    	rpt.append("\r\n"); rpt.append((!CMSecurity.isDisabled("MCCP")?"1":"0"));
    	rpt.append("\r\n"); rpt.append("MSP");
    	rpt.append("\r\n"); rpt.append((!CMSecurity.isDisabled("MSP")?"1":"0"));
    	rpt.append("\r\n"); rpt.append("MXP");
    	rpt.append("\r\n"); rpt.append((!CMSecurity.isDisabled("MXP")?"1":"0"));
    	rpt.append("\r\nMSSP-REPLY-END\r\n");
    	return rpt.toString();
    }
    
    public LoginResult login(MOB mob, int attempt)
        throws java.io.IOException
    {
        if(mob==null) 
        	return LoginResult.NO_LOGIN;
        if(mob.session()==null) 
        	return LoginResult.NO_LOGIN;

        boolean wizi=false;

        String login=mob.session().prompt("name: ");
        if(login==null) 
        	return LoginResult.NO_LOGIN;
        login=login.trim();
        if(login.length()==0) 
        	return LoginResult.NO_LOGIN;
        if(login.equalsIgnoreCase("MSSP-REQUEST")&&(!CMSecurity.isDisabled("MSSP")))
        {
        	mob.session().rawOut(getMSSPPacket());
            mob.session().logoff(false,false,false);
        	return LoginResult.NO_LOGIN;
        }
        	
        if(login.endsWith(" !"))
        {
            login=login.substring(0,login.length()-2);
            login=login.trim();
            wizi=true;
        }

        PlayerAccount acct = null;
        boolean found=false;
        if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
        {
        	acct = CMLib.database().DBReadAccount(login);
        	if(acct!=null)
    		{
        		found=true;
        		mob.playerStats().setAccount(acct);
        		mob.setName(acct.accountName());
    		}
        }
        else
	        found = CMLib.database().DBUserSearch(mob,login);
        if(found)
        {
            mob.session().print("password: ");
            String password=mob.session().blockingIn();
            PlayerStats pstats=mob.playerStats();

            if((pstats!=null)
            &&(pstats.password().equalsIgnoreCase(password))
            &&(mob.Name().trim().length()>0))
            {
                if(CMSecurity.isBanned(mob.Name()))
                {
                    mob.tell("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
                    mob.session().logoff(false,false,false);
                    if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                       pendingLogins.remove(mob.Name().toUpperCase());
                    return LoginResult.NO_LOGIN;
                }
                if(((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
                   &&(!CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION")))
                {
                    Command C=CMClass.getCommand("Email");
                    if(C!=null)
                    {
                        if(!C.execute(mob,null,0))
                            return LoginResult.NO_LOGIN;
                    }
                    CMLib.database().DBUpdateEmail(mob);
                }
                if((pstats.getEmail()!=null)&&CMSecurity.isBanned(pstats.getEmail()))
                {
                    mob.tell("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
                    mob.session().logoff(false,false,false);
                    if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                       pendingLogins.remove(mob.Name().toUpperCase());
                    return LoginResult.NO_LOGIN;
                }
                if(!checkExpiration(mob)) return LoginResult.NO_LOGIN;
                Long L=(Long)pendingLogins.get(mob.Name().toUpperCase());
                if((L!=null)&&((System.currentTimeMillis()-L.longValue())<(10*60*1000)))
                {
                    mob.session().println("A previous login is still pending.  Please be patient.");
                    return LoginResult.NO_LOGIN;
                }
                if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                   pendingLogins.remove(mob.Name().toUpperCase());
                pendingLogins.put(mob.Name().toUpperCase(),Long.valueOf(System.currentTimeMillis()));

                for(int s=0;s<CMLib.sessions().size();s++)
                {
                    Session thisSession=CMLib.sessions().elementAt(s);
                	MOB M=thisSession.mob();
                    if((M!=null)&&(thisSession!=mob.session())&&(M.playerStats()!=null))
                    {
                    	PlayerStats MPStats=M.playerStats();
                        if(M.Name().equals(mob.Name())
                        ||((MPStats.getAccount()!=null)&&(MPStats.getAccount().accountName().equals(mob.Name()))))
                        {
                            Room oldRoom=M.location();
                            if(oldRoom!=null)
	                            while(oldRoom.isInhabitant(M))
	                                oldRoom.delInhabitant(M);
                            mob.session().setMob(M);
                            M.setSession(mob.session());
                            thisSession.setMob(null);
                            thisSession.logoff(false,false,false);
                            Log.sysOut("FrontDoor","Session swap for "+mob.session().mob().Name()+".");
                            reloadTerminal(mob.session().mob());
                            mob.session().mob().bringToLife(oldRoom,false);
                            if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                               pendingLogins.remove(mob.Name().toUpperCase());
                            return LoginResult.SESSION_SWAP;
                        }
                    }
                }

                // count number of multiplays
                int numAtAddress=0;
                try{
                for(int s=0;s<CMLib.sessions().size();s++)
                {
                    if((CMLib.sessions().elementAt(s)!=mob.session())
                    &&(mob.session().getAddress().equalsIgnoreCase((CMLib.sessions().elementAt(s).getAddress()))))
                        numAtAddress++;
                }
                }catch(Exception e){}

                if((CMProps.getIntVar(CMProps.SYSTEMI_MAXCONNSPERIP)>0)
                &&(numAtAddress>=CMProps.getIntVar(CMProps.SYSTEMI_MAXCONNSPERIP))
                &&(!CMSecurity.isDisabled("MAXCONNSPERIP")))
                {
                    mob.session().println("The maximum player limit has already been reached for your IP address.");
                    if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                        pendingLogins.remove(mob.Name().toUpperCase());
                    return LoginResult.NO_LOGIN;
                }
                if(acct!=null)
                	if(!selectAccountCharacter(acct,mob.session(),mob))
                	{
                        if(pendingLogins.containsKey(acct.accountName().toUpperCase()))
                            pendingLogins.remove(acct.accountName().toUpperCase());
                        return LoginResult.NO_LOGIN;
                	}
                MOB oldMOB=mob;
                if(CMLib.players().getPlayer(oldMOB.Name())!=null)
                {
                    oldMOB.session().setMob(CMLib.players().getPlayer(oldMOB.Name()));
                    mob=oldMOB.session().mob();
                    mob.setSession(oldMOB.session());
                    if(mob!=oldMOB)
                        oldMOB.setSession(null);
                    if(loginsDisabled(mob))
                    	return LoginResult.NO_LOGIN;
                    if(wizi)
                    {
                        Command C=CMClass.getCommand("WizInv");
                        if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
                            C.execute(mob,CMParms.makeVector("WIZINV"),0);
                    }
                    showTheNews(mob);
                    mob.bringToLife(mob.location(),false);
                    CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
                    mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
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
                }
                else
                {
                    CMLib.database().DBReadPlayer(mob);
                    if(loginsDisabled(mob))
                    	return LoginResult.NO_LOGIN;
                    if(wizi)
                    {
                        Command C=CMClass.getCommand("WizInv");
                        if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
                            C.execute(mob,CMParms.makeVector("WIZINV"),0);
                    }
                    showTheNews(mob);
                    mob.bringToLife(mob.location(),true);
                    CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
                    mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
                    CMLib.database().DBReadFollowers(mob,true);
                }
                if((mob.session()!=null)&&(mob.playerStats()!=null))
                    mob.playerStats().setLastIP(mob.session().getAddress());
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
                if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                   pendingLogins.remove(mob.Name().toUpperCase());
            }
            else
            {
                String name=mob.Name();
                Log.sysOut("FrontDoor","Failed login: "+mob.Name());
                mob.setName("");
                mob.session().println("\n\rInvalid password.\n\r");
                if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                   pendingLogins.remove(mob.Name().toUpperCase());
                if((!mob.session().killFlag())
                &&(pstats!=null)
                &&(pstats.getEmail().length()>0)
                &&(pstats.getEmail().indexOf("@")>0)
                &&(attempt>2)
                &&(CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).length()>0))
                {
                    if(mob.session().confirm("Would you like you have your password e-mailed to you (y/N)? ","N"))
                    {
                        if(CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),
                                                   "passwords@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
                                                   "noreply@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
                                                   pstats.getEmail(),
                                                   "Password for "+name,
                                                   "Your password for "+name+" at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" is: '"+pstats.password()+"'."))
                            mob.session().println("Email sent.\n\r");
                        else
                            mob.session().println("Error sending email.\n\r");
                        mob.session().logoff(false,false,false);
                    }
                }
                return LoginResult.NO_LOGIN;
            }
        }
        else
        {
            if(CMSecurity.isDisabled("NEWPLAYERS"))
            {
                mob.session().print("\n\r'"+CMStrings.capitalizeAndLower(login)+"' is not recognized.\n\r");
                mob.setName("");
            }
            else
            if(!isOkName(login))
            {
                mob.session().println("\n\rThat name is unrecognized.\n\rThat name is also not available for new players.\n\r  Choose another name (no spaces allowed)!\n\r");
                mob.setName("");
            }
            else
            if((CMProps.getIntVar(CMProps.SYSTEMI_MUDTHEME)==0)
            ||(CMSecurity.isDisabled("LOGINS")))
            {
                mob.session().print("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rThis server is not accepting new accounts.\n\r\n\r");
                mob.setName("");
            }
            else
            if((CMProps.getIntVar(CMProps.SYSTEMI_MAXNEWPERIP)>0)
            &&(CMProps.getCountNewUserByIP(mob.session().getAddress())>=CMProps.getIntVar(CMProps.SYSTEMI_MAXNEWPERIP))
            &&(!CMSecurity.isDisabled("MAXCONNSPERIP")))
            {
                mob.session().println("\n\rThat name is unrecognized.\n\rAlso, the maximum daily new player limit has already been reached for your location.");
                mob.setName("");
            }
            else
            if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>1)
            {
                if(mob.session().confirm("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rIs this a new account you would like to create (y/N)?","N"))
                {
                	LoginResult result = LoginResult.NO_LOGIN;//TODO:createAccount(mob,login,mob.session())?1:0; 
                    if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                        pendingLogins.remove(mob.Name().toUpperCase());
                    return result;
                }
            }
            else
            if(mob.session().confirm("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rIs this a new character you would like to create (y/N)?","N"))
            {
            	LoginResult result = LoginResult.NO_LOGIN;
            	if(createCharacter(mob,login,mob.session()))
            		result = LoginResult.NORMAL_LOGIN;
                if(pendingLogins.containsKey(mob.Name().toUpperCase()))
                    pendingLogins.remove(mob.Name().toUpperCase());
                return result;
            }
            if(pendingLogins.containsKey(mob.Name().toUpperCase()))
               pendingLogins.remove(mob.Name().toUpperCase());
            return LoginResult.NO_LOGIN;
        }
        if(mob.session()!=null)
            mob.session().println("\n\r");
        if(pendingLogins.containsKey(mob.Name().toUpperCase()))
           pendingLogins.remove(mob.Name().toUpperCase());
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
