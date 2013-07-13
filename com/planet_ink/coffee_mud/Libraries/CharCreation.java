package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.miniweb.util.MWThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.util.*;


/*
   Copyright 2000-2013 Bo Zimmerman

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
public class CharCreation extends StdLibrary implements CharCreationLibrary
{
	public String ID(){return "CharCreation";}
	public Hashtable<String,String> startRooms=new Hashtable<String,String>();
	public Hashtable<String,String> deathRooms=new Hashtable<String,String>();
	public Hashtable<String,String> bodyRooms=new Hashtable<String,String>();

	protected int getTotalStatPoints()
	{
		final int basemax = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		final int basemin = CMProps.getIntVar(CMProps.Int.BASEMINSTAT);

		int points = CMProps.getIntVar(CMProps.Int.MAXSTAT);
		// Make sure there are enough points
		if (points < ((basemin + 1) * CharStats.CODES.BASE().length))
			points = (basemin + 1) * CharStats.CODES.BASE().length;

		// Make sure there aren't too many points
		if (points > (basemax - 1) * CharStats.CODES.BASE().length)
				points = (basemax - 1) * CharStats.CODES.BASE().length;

		// Subtract stat minimums from point total to get distributable points
		return points - (basemin * CharStats.CODES.BASE().length);
	}
	
	public void reRollStats(MOB mob, CharStats C, int pointsLeft)
	{
		final int basemax = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		
		int[] stats=new int[CharStats.CODES.BASE().length];
		for(int i=0;i<stats.length;i++)
			stats[i]=C.getStat(i);
		
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

	public List<CharClass> classQualifies(MOB mob, int theme)
	{
		Vector<CharClass> them=new Vector<CharClass>();
		HashSet<String> doneClasses=new HashSet<String>();
		for(Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=c.nextElement();
			if(doneClasses.contains(C.ID())) continue;
			C=CMClass.getCharClass(C.ID());
			doneClasses.add(C.ID());
			if(canChangeToThisClass(mob,C,theme))
				them.addElement(C);
		}
		return them;
	}

	public List<Race> raceQualifies(MOB mob, int theme)
	{
		Vector<Race> qualRaces = new Vector<Race>();
		HashSet<String> doneRaces=new HashSet<String>();
		for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
		{
			Race R=r.nextElement();
			if(doneRaces.contains(R.ID())) continue;
			R=CMClass.getRace(R.ID());
			doneRaces.add(R.ID());
			if((CMProps.isTheme(R.availabilityCode()))
			&&(!CMath.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK))
			&&((!CMSecurity.isDisabled(CMSecurity.DisFlag.STDRACES))||(R.isGeneric()))
			&&(CMath.bset(R.availabilityCode(),theme)))
				qualRaces.add(R);
		}
		return qualRaces;
	}
	
	public boolean isOkName(String login)
	{
		if(login.length()>20) return false;
		if(login.length()<3) return false;

		if(login.trim().indexOf(' ')>=0) return false;

		login=login.toUpperCase().trim();
		Vector<String> V=CMParms.parse(login);
		for(int v=V.size()-1;v>=0;v--)
		{
			String str=V.elementAt(v);
			if((" THE A AN ").indexOf(" "+str+" ")>=0)
				V.removeElementAt(v);
		}
		for(int v=0;v<V.size();v++)
		{
			String str=V.elementAt(v);
			if(DEFAULT_BADNAMES.indexOf(" "+str+" ")>=0)
				return false;
		}
		Vector<String> V2=CMParms.parseCommas(CMProps.getVar(CMProps.Str.BADNAMES),true);
		for(int v2=0;v2<V2.size();v2++)
		{
			String str2=V2.elementAt(v2);
			if(str2.length()>0)
			for(int v=0;v<V.size();v++)
			{
				String str=V.elementAt(v);
				if((str.length()>0)
				&&(str.equalsIgnoreCase(str2)))
					return false;
			}
		}

		for(int c=0;c<login.length();c++)
			if((login.charAt(c)!=' ')&&(!Character.isLetter(login.charAt(c))))
				return false;
		for(Enumeration<Deity> d=CMLib.map().deities();d.hasMoreElements();)
		{
			MOB D=d.nextElement();
			if((CMLib.english().containsString(D.ID(),login))
			||(CMLib.english().containsString(D.Name(),login)))
				return false;
		}
		for(Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
		{
			MOB M=m.nextElement();
			if((CMLib.english().containsString(M.Name(),login))
			||(CMLib.english().containsString(M.name(),login)))
				return false;
		}

		for(Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
		{
			Clan C=e.nextElement();
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
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
		{
			if(S.getClientTelnetMode(Session.TELNET_MXP))
			{
				StringBuffer mxpText=Resources.getFileResource("text/mxp.txt",true);
				if(mxpText!=null)
					S.rawOut("\033[6z"+mxpText.toString()+"\n\r");
			}
			else
				mob.tell("MXP codes have been disabled for this session.");
		}
		else
		if(S.getClientTelnetMode(Session.TELNET_MXP))
		{
			S.changeTelnetMode(Session.TELNET_MXP,false);
			S.setClientTelnetMode(Session.TELNET_MXP,false);
		}

		if((CMath.bset(mob.getBitmap(),MOB.ATT_SOUND))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP)))
		{
			if(!S.getClientTelnetMode(Session.TELNET_MSP))
				mob.tell("MSP sounds have been disabled for this session.");
		}
		else
		if(S.getClientTelnetMode(Session.TELNET_MSP))
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
		if(!CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION)) 
			return false;
		if((acct!=null)&&(acct.isSet(PlayerAccount.FLAG_NOEXPIRE)))
			return false;
		if((session.mob()!=null)
		&&((CMSecurity.isASysOp(session.mob()))||(CMSecurity.isAllowedEverywhere(session.mob(), CMSecurity.SecFlag.NOEXPIRE))))
			return false;
		if(expiration<=System.currentTimeMillis())
		{
			session.println("\n\r"+CMProps.getVar(CMProps.Str.EXPCONTACTLINE)+"\n\r\n\r");
			session.stopSession(false,false,false);
			return true;
		}
		return false;
	}

	private void executeScript(MOB mob, List<String> scripts) 
	{
		if(scripts==null) return;
		for(int s=0;s<scripts.size();s++) 
		{
			String script=scripts.get(s);
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

	private Map<String,List<String>> getLoginScripts()
	{
		Hashtable<String,List<String>> extraScripts=new Hashtable<String,List<String>>();
		final String[] VALID_SCRIPT_CODES={"PASSWORD","EMAIL","ANSI","THEME","RACE","GENDER","STATS","CLASS","FACTIONS","END"}; 				   
		Vector<String> extras=CMParms.parseCommas(CMProps.getVar(CMProps.Str.CHARCREATIONSCRIPTS),true);
		for(int e=0;e<extras.size();e++) {
			String s=extras.elementAt(e);
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
			List<String> V=extraScripts.get(code);
			if(V==null){ V=new Vector<String>(); extraScripts.put(code,V);}
			V.add(s.trim());
		}
		return extraScripts;
	}

	protected void finishCreateAccount(final LoginSession loginObj, final PlayerAccount acct, final String login, final String pw, final String emailAddy, final Session session)
	{
		acct.setAccountName(CMStrings.capitalizeAndLower(login.trim()));
		acct.setPassword(pw);
		acct.setEmail(emailAddy);
		acct.setLastIP(session.getAddress());
		acct.setLastDateTime(System.currentTimeMillis());
		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
			acct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*(CMProps.getIntVar(CMProps.Int.TRIALDAYS))));
		
		if(((pw==null)||(pw.length()==0))&&(!CMProps.getVar(CMProps.Str.EMAILREQ).startsWith("DISABLE")))
		{
			String password="";
			for(int i=0;i<6;i++)
				password+=(char)('a'+CMLib.dice().roll(1,26,-1));
			acct.setPassword(password);
			CMLib.database().DBCreateAccount(acct);
			CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.Str.MAILBOX),
					  acct.accountName(),
					  acct.accountName(),
					  "Password for "+acct.accountName(),
					  "Your password for "+acct.accountName()+" is: "+password+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.Str.MUDPORTS)+".\n\rAfter creating a character, you may use the PASSWORD command to change it once you are online.");
			session.println("Your account has been created.  You will receive an email with your password shortly.");
			try{Thread.sleep(2000);}catch(Exception e){}
			session.stopSession(false,false,false);
			Log.sysOut("Created account: "+acct.accountName());
			session.setAccount(null);
			loginObj.state=LoginState.START;
			loginObj.reset=true;
			return;
		}
		else
		{
			CMLib.database().DBCreateAccount(acct);
			StringBuffer doneText=new CMFile(Resources.buildResourcePath("text")+"doneacct.txt",null,true).text();
			try { doneText = CMLib.webMacroFilter().virtualPageFilter(doneText);}catch(Exception ex){}
			session.println(null,null,null,"\n\r\n\r"+doneText.toString());
		}
		session.setAccount(acct);
		Log.sysOut("Created account: "+acct.accountName());
		if(CMLib.players().playerExists(acct.accountName()))
		{
			loginObj.lastInput="IMPORT "+acct.accountName();
			loginObj.state=LoginState.ACCTMENU_COMMAND;
		}
		else
			loginObj.state=LoginState.ACCTMENU_START;
	}

	protected String promptEmailAddress(Session session, boolean[] emailConfirmation) throws java.io.IOException
	{
		if(CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE"))
		{
			return "";
		}
		boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));

		boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
		StringBuffer emailIntro=new CMFile(Resources.buildResourcePath("text")+"email.txt",null,true).text();
		try { emailIntro = CMLib.webMacroFilter().virtualPageFilter(emailIntro);}catch(Exception ex){}
		session.println(null,null,null,emailIntro.toString());
		while(!session.isStopped())
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

	protected String buildQualifyingClassList(List<CharClass> classes, String finalConnector)
	{
		StringBuilder list = new StringBuilder("");
		for(Iterator<CharClass> i=classes.iterator(); i.hasNext(); )
		{
			CharClass C = i.next();
			if(!i.hasNext())
			{
				if (list.length()>0)
				{
					list.append("^?"+finalConnector+" ^?");
				}
				list.append(C.name());
			}
			else
			{
				list.append(C.name()+"^?, ^?");
			}
		}
		return list.toString();
	}


	protected void promptFactions(int theme, MOB mob, Session session) throws IOException
	{
		Faction F=null;
		List<Integer> mine=null;
		int defaultValue=0;
		for(Enumeration<Faction> e=CMLib.factions().factions();e.hasMoreElements();)
		{
			F=e.nextElement();
			mine=F.findChoices(mob);
			defaultValue=F.findAutoDefault(mob);
			if(defaultValue!=Integer.MAX_VALUE)
				mob.addFaction(F.factionID(),defaultValue);
			if(mine.size()==1)
				mob.addFaction(F.factionID(),mine.get(0).intValue());
			else
			if(mine.size()>1)
			{
				if((F.choiceIntro()!=null)&&(F.choiceIntro().length()>0))
				{
					StringBuffer intro = new CMFile(Resources.makeFileResourceName(F.choiceIntro()),null,true).text();
					try { intro = CMLib.webMacroFilter().virtualPageFilter(intro);}catch(Exception ex){}
					session.println(null,null,null,"\n\r\n\r"+intro.toString());
				}
				StringBuffer menu=new StringBuffer("Select one: ");
				Vector<String> namedChoices=new Vector<String>();
				for(int m=0;m<mine.size();m++)
				{
					Faction.FRange FR=CMLib.factions().getRange(F.factionID(),mine.get(m).intValue());
					if(FR!=null)
					{
						namedChoices.addElement(FR.name().toUpperCase());
						menu.append(FR.name()+", ");
					}
					else
						namedChoices.addElement(""+mine.get(m).intValue());
				}
				if(mine.size()==namedChoices.size())
				{
					String alignment="";
					while((!namedChoices.contains(alignment))
					&&(!session.isStopped()))
					{
						alignment=session.prompt(menu.toString().substring(0,menu.length()-2)+".\n\r: ","").toUpperCase();
						if(!namedChoices.contains(alignment))
							for(int i=0;i<namedChoices.size();i++)
								if(namedChoices.elementAt(i).startsWith(alignment.toUpperCase()))
								{ alignment=namedChoices.elementAt(i); break;}
						if(!namedChoices.contains(alignment))
							for(int i=0;i<namedChoices.size();i++)
								if(namedChoices.elementAt(i).indexOf(alignment.toUpperCase())>=0)
								{ alignment=namedChoices.elementAt(i); break;}
					}
					if(!session.isStopped())
					{
						int valueIndex=namedChoices.indexOf(alignment);
						if(valueIndex>=0)
							mob.addFaction(F.factionID(),mine.get(valueIndex).intValue());
					}
				}
			}
		}
	}
	
	protected void getUniversalStartingItems(int theme, MOB mob)
	{
		List<String> newItemPartsV = CMParms.parseCommas(CMProps.getVar(CMProps.Str.STARTINGITEMS), true);
		for(String item : newItemPartsV)
		{
			item=item.trim();
			int num=1;
			int x = item.indexOf(' ');
			if((x>0)&&(CMath.isInteger(item.substring(0,x).trim())))
			{
				num=CMath.s_int(item.substring(0,x).trim());
				item=item.substring(x+1);
			}
			for(int i=0;i<num;i++)
			{
				Item I=CMClass.getBasicItem(item);
				if(I==null) I=CMClass.getItem(item);
				if(I==null)
				{
					I=CMLib.catalog().getCatalogItem(item);
					if(I!=null)
					{
						I=(Item)I.copyOf();
						CMLib.catalog().changeCatalogUsage(I,true);
					}
				}
				if(I==null)
				{
					Log.errOut("CharCreation","Unable to give new STARTINGITEM '"+item+"'");
				}
				else
				{
					mob.addItem(I);
				}
			}
		}
	}
	
	private boolean loginsDisabled(MOB mob)
	{
		if((CMSecurity.isDisabled(CMSecurity.DisFlag.LOGINS))
		&&(!CMSecurity.isASysOp(mob))
		&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, mob.Name()))
		&&(!((mob.playerStats()!=null)&&(mob.playerStats().getAccount()!=null)&&(CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, mob.playerStats().getAccount().accountName()))))
		&&(!((mob.session()!=null)&&(CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, mob.session().getAddress())))))
		{
			StringBuffer rejectText=Resources.getFileResource("text/nologins.txt",true);
			try { rejectText = CMLib.webMacroFilter().virtualPageFilter(rejectText);}catch(Exception ex){}
			if((rejectText!=null)&&(rejectText.length()>0))
				mob.session().println(rejectText.toString());
			try{Thread.sleep(1000);}catch(Exception e){}
			mob.session().stopSession(false,false,false);
			return true;
		}
		return false;
	}

	public LoginResult prelimChecks(Session session, String login, PlayerLibrary.ThinnerPlayer player)
	{
		if(CMSecurity.isBanned(login))
		{
			session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
			session.stopSession(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		if((player.email!=null)&&CMSecurity.isBanned(player.email))
		{
			session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
			session.stopSession(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		for(Session S : CMLib.sessions().allIterable())
		{
			MOB M=S.mob();
			if((M!=null)
			&&(S!=session)
			&&(M==player.loadedMOB))
			{
				Room oldRoom=M.location();
				if(oldRoom!=null)
					while(oldRoom.isInhabitant(M))
						oldRoom.delInhabitant(M);
				session.setMob(M);
				M.setSession(session);
				S.setMob(null);
				S.stopSession(false,false,false);
				Log.sysOut("Session swap for "+session.mob().Name()+".");
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
			for(Session S : CMLib.sessions().localOnlineIterable())
			{
				MOB listenerM=S.mob();
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
		} catch(Exception e){}
	}

	private String getMSSPPacket()
	{
		StringBuffer rpt = new StringBuffer("\r\nMSSP-REPLY-START");
		rpt.append("\r\n"); rpt.append("PLAYERS");
		rpt.append("\t"); rpt.append(Integer.toString(CMLib.sessions().getCountLocalOnline()));
		rpt.append("\r\n"); rpt.append("STATUS");
		rpt.append("\t");
		switch(CMProps.getIntVar(CMProps.Int.MUDSTATE))
		{
		case 0: rpt.append("Alpha"); break; 
		case 1: rpt.append("Closed Beta"); break; 
		case 2: rpt.append("Open Beta"); break; 
		case 3: rpt.append("Live"); break;
		default : rpt.append("Live"); break;
		}
		
		MudHost host = null;
		if(CMLib.hosts().size()>0)
			host = CMLib.hosts().get(0);
		if(host != null)
		{
			rpt.append("\r\n"); rpt.append("UPTIME");
			rpt.append("\t"); rpt.append(Long.toString(host.getUptimeSecs()));
			rpt.append("\r\n"); rpt.append("HOSTNAME");
			rpt.append("\t"); rpt.append(host.getHost());
			rpt.append("\r\n"); rpt.append("PORT");
			rpt.append("\t"); rpt.append(Integer.toString(host.getPort()));
			if(Thread.currentThread() instanceof MWThread)
			{
				String webServerPort=Integer.toString(((MWThread)Thread.currentThread()).getConfig().getHttpListenPorts()[0]);
				rpt.append("\r\n"); rpt.append("WEBSITE");
				rpt.append("\t"); rpt.append(("http://"+host.getHost()+":"+webServerPort));
			}
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
		rpt.append("\t"); rpt.append(CMProps.getVar(CMProps.Str.ADMINEMAIL));
		rpt.append("\r\n"); rpt.append("CODEBASE");
		rpt.append("\t"); rpt.append(("CoffeeMud v"+CMProps.getVar(CMProps.Str.MUDVER)));
		rpt.append("\r\n"); rpt.append("AREAS");
		rpt.append("\t"); rpt.append(Integer.toString(CMLib.map().numAreas()));
		rpt.append("\r\n"); rpt.append("HELPFILES");
		rpt.append("\t"); rpt.append(Integer.toString(CMLib.help().getHelpFile().size()));
		rpt.append("\r\n"); rpt.append("MOBILES");
		rpt.append("\t"); rpt.append(Long.toString(CMClass.numPrototypes(CMClass.CMObjectType.MOB)));
		rpt.append("\r\n"); rpt.append("OBJECTS");
		rpt.append("\t"); rpt.append(Long.toString(CMClass.numPrototypes(CMClass.OBJECTS_ITEMTYPES)));
		rpt.append("\r\n"); rpt.append("ROOMS");
		rpt.append("\t"); rpt.append(Long.toString(CMLib.map().numRooms()));
		rpt.append("\r\n"); rpt.append("CLASSES");
		int numClasses = 0;
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
			numClasses=CMLib.login().classQualifies(null, CMProps.getIntVar(CMProps.Int.MUDTHEME)&0x07).size();
		rpt.append("\t"); rpt.append(Long.toString(numClasses));
		rpt.append("\r\n"); rpt.append("RACES");
		int numRaces = 0;
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
			numRaces=CMLib.login().raceQualifies(null, CMProps.getIntVar(CMProps.Int.MUDTHEME)&0x07).size();
		rpt.append("\t"); rpt.append(Long.toString(numRaces));
		rpt.append("\r\n"); rpt.append("SKILLS");
		rpt.append("\t"); rpt.append(Long.toString(CMLib.ableMapper().numMappedAbilities()));
		rpt.append("\r\n"); rpt.append("ANSI");
		rpt.append("\t"); rpt.append((this!=null?"1":"0"));
		rpt.append("\r\n"); rpt.append("MCCP");
		rpt.append("\t"); rpt.append((!CMSecurity.isDisabled(CMSecurity.DisFlag.MCCP)?"1":"0"));
		rpt.append("\r\n"); rpt.append("MSP");
		rpt.append("\t"); rpt.append((!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP)?"1":"0"));
		rpt.append("\r\n"); rpt.append("MXP");
		rpt.append("\t"); rpt.append((!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)?"1":"0"));
		rpt.append("\r\nMSSP-REPLY-END\r\n");
		return rpt.toString();
	}
	
	public LoginResult loginSystem(Session session, LoginSession loginObj) throws IOException 
	{
		if(session==null) 
			return LoginResult.NO_LOGIN;
		if(loginObj==null)
			loginObj=new LoginSession();
		try
		{
			while(!session.isStopped())
			{
				switch(loginObj.state)
				{
				case START:
				{
					loginObj.wizi=false;
					session.setAccount(null);
					loginObj.attempt++;
					if(loginObj.attempt>=4)
					{
						session.stopSession(false,false,false);
						loginObj.reset=true;
						loginObj.state=LoginState.START;
						return LoginResult.NO_LOGIN;
					}
					if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
						session.promptPrint("\n\raccount name: ");
					else
						session.promptPrint("\n\rname: ");
					loginObj.state=LoginState.LOGIN_NAME;
					session.setStatus(Session.SessionStatus.LOGIN);
					return LoginResult.INPUT_REQUIRED;
				}
				case LOGIN_NAME:
				{
					loginObj.login=loginObj.lastInput;
					if(loginObj.login==null)
					{
						loginObj.state=LoginState.START;
						break;
					}
					loginObj.login=loginObj.login.trim();
					if(loginObj.login.length()==0)
					{
						loginObj.state=LoginState.START;
						break;
					}
					if(loginObj.login.equalsIgnoreCase("MSSP-REQUEST")&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSSP)))
					{
						session.rawOut(getMSSPPacket());
						session.stopSession(false,false,false);
						loginObj.reset=true;
						loginObj.state=LoginState.START;
						return LoginResult.NO_LOGIN;
					}
					if(loginObj.login.endsWith(" !"))
					{
						loginObj.login=loginObj.login.substring(0,loginObj.login.length()-2);
						loginObj.login=loginObj.login.trim();
						loginObj.wizi=true;
					}
					loginObj.login = CMStrings.capitalizeAndLower(loginObj.login);
					if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
					{
						loginObj.acct = CMLib.players().getLoadAccount(loginObj.login);
						if(loginObj.acct!=null)
						{
							loginObj.player=new PlayerLibrary.ThinnerPlayer();
							loginObj.player.name=loginObj.acct.accountName();
							loginObj.player.accountName=loginObj.acct.accountName();
							loginObj.player.email=loginObj.acct.getEmail();
							loginObj.player.expiration=loginObj.acct.getAccountExpiration();
							loginObj.player.password=loginObj.acct.getPasswordStr();
						}
						else
						{
							loginObj.player=CMLib.database().DBUserSearch(loginObj.login);
							if(loginObj.player != null)
							{
								session.promptPrint("password for "+loginObj.player.name+": ");
								loginObj.state=LoginState.ACCT_CHAR_PWORD;
								return LoginResult.INPUT_REQUIRED;
							}
							else
							{
								session.println("\n\rAccount '"+CMStrings.capitalizeAndLower(loginObj.login)+"' does not exist.");
								loginObj.player=null;
							}
						}
					}
					else
					{
						MOB mob=CMLib.players().getPlayer(loginObj.login);
						if((mob!=null)&&(mob.playerStats()!=null))
						{
							loginObj.player=new PlayerLibrary.ThinnerPlayer();
							loginObj.player.name=mob.Name();
							loginObj.player.email=mob.playerStats().getEmail();
							loginObj.player.expiration=mob.playerStats().getAccountExpiration();
							loginObj.player.password=mob.playerStats().getPasswordStr();
							loginObj.player.loadedMOB=mob;
						}
						else
							loginObj.player=CMLib.database().DBUserSearch(loginObj.login);
					}
					loginObj.state=LoginState.PLAYER_PASS_START;
					break;
				}
				case ACCT_CHAR_PWORD:
				{
					loginObj.password=loginObj.lastInput;
					if(loginObj.player.matchesPassword(loginObj.password))
					{
						if((loginObj.player.accountName==null)||(loginObj.player.accountName.trim().length()==0))
						{
							session.println("\n\rThis mud is now using an account system.  "
									+"Please create a new account and use the IMPORT command to add your character(s) to your account.");
							session.promptPrint("Would you like to create your new master account and call it '"+loginObj.player.name+"' (y/N)? ");
							loginObj.state=LoginState.ACCT_CONVERT_CONFIRM;
							return LoginResult.INPUT_REQUIRED;
						}
						else
						{
							session.println("\n\rThis mud uses an account system.  Your account name is `^H"+loginObj.player.accountName+"^N`.\n\r"
									+"Please use this account name when logging in.");
						}
					}
					loginObj.state=LoginState.START;
					break;
				}
				case ACCT_CONVERT_CONFIRM:
				{
					if(loginObj.lastInput.toUpperCase().startsWith("Y"))
					{
						loginObj.acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
						loginObj.state=LoginState.ACCTCREATE_START;
						break;
					}
					loginObj.state=LoginState.START;
					loginObj.reset=true;
					return LoginResult.NO_LOGIN;
				}
				case ACCTCREATE_START:
				{
					Log.sysOut("Creating account: "+loginObj.login);
					loginObj.state=LoginState.ACCTCREATE_ANSICONFIRM;
					session.promptPrint("\n\rDo you want ANSI colors (Y/n)?");
					return LoginResult.INPUT_REQUIRED;
				}
				case ACCTCREATE_ANSICONFIRM:
				{
					PlayerAccount acct=loginObj.acct;
					if(loginObj.lastInput.toUpperCase().startsWith("N"))
					{
						acct.setFlag(PlayerAccount.FLAG_ANSI, false);
						session.setServerTelnetMode(Session.TELNET_ANSI,false);
						session.setClientTelnetMode(Session.TELNET_ANSI,false);
					}
					else
					{
						acct.setFlag(PlayerAccount.FLAG_ANSI, true);
					}
					StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newacct.txt",null,true).text();
					try { introText = CMLib.webMacroFilter().virtualPageFilter(introText);}catch(Exception ex){}
					session.println(null,null,null,"\n\r\n\r"+introText.toString());
					final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
							 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
					if(!emailPassword)
					{
						session.promptPrint("\n\rEnter an account password\n\r: ");
						loginObj.state=LoginState.ACCTCREATE_PASSWORDED;
						return LoginResult.INPUT_REQUIRED;
					}
					loginObj.password=null;
					loginObj.state=LoginState.ACCTCREATE_EMAILSTART;
					break;
				}
				case ACCTCREATE_EMAILSTART:
				{
					if(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE"))
					{
						finishCreateAccount(loginObj, loginObj.acct, loginObj.login, loginObj.password, "", session);
						break;
					}
					StringBuffer emailIntro=new CMFile(Resources.buildResourcePath("text")+"email.txt",null,true).text();
					try { emailIntro = CMLib.webMacroFilter().virtualPageFilter(emailIntro);}catch(Exception ex){}
					session.println(null,null,null,emailIntro.toString());
					loginObj.state=LoginState.ACCTCREATE_EMAILPROMPT;
					break;
				}
				case ACCTCREATE_EMAILPROMPT:
				{
					session.promptPrint("\n\rEnter your e-mail address: ");
					loginObj.state=LoginState.ACCTCREATE_EMAILENTERED;
					return LoginResult.INPUT_REQUIRED;
				}
				case ACCTCREATE_EMAILENTERED:
				{
					boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
							 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
					boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
					String newEmail=loginObj.lastInput;
					if((emailReq||emailPassword) 
					&& ((newEmail==null)||(newEmail.trim().length()==0)||(!CMLib.smtp().isValidEmailAddress(newEmail))))
					{
						session.println("\n\rA valid email address is required.\n\r");
						loginObj.state=LoginState.ACCTCREATE_EMAILPROMPT;
						break;
					}
					loginObj.acct.setEmail(newEmail);
					loginObj.lastInput=newEmail;
					if(emailPassword) session.println("This email address will be used to send you a password.");
					if(emailReq||emailPassword)
					{
						session.promptPrint("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter: ");
						loginObj.state=LoginState.ACCTCREATE_EMAILCONFIRMED;
						return LoginResult.INPUT_REQUIRED;
					}
					loginObj.state=LoginState.ACCTCREATE_EMAILCONFIRMED;
					break;
				}
				case ACCTCREATE_EMAILCONFIRMED:
				{
					boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
					String newEmail=loginObj.acct.getEmail();
					boolean emailConfirmed=false;
					if((newEmail.length()>0)&&(newEmail.equalsIgnoreCase(loginObj.lastInput)))
						emailConfirmed=CMLib.smtp().isValidEmailAddress(newEmail);
					loginObj.acct.setEmail("");
					if(emailConfirmed||((!emailReq)&&(newEmail.trim().length()==0)))
					{
						finishCreateAccount(loginObj, loginObj.acct, loginObj.login, loginObj.password, newEmail, session);
						break;
					}
					session.println("\n\rThat email address combination was invalid.\n\r");
					loginObj.state=LoginState.ACCTCREATE_EMAILPROMPT;
					break;
				}
				case ACCTCREATE_PASSWORDED:
				{
					String password=loginObj.lastInput;
					if(password.length()==0)
					{
						session.println("\n\rAborting account creation.");
						loginObj.state=LoginState.START;
					}
					else
					{
						loginObj.password=loginObj.lastInput;
						loginObj.state=LoginState.ACCTCREATE_EMAILSTART;
					}
					break;
				}
				case PLAYER_PASS_START:
				{
					if(loginObj.player==null)
					{
						if(newCharactersAllowed(loginObj.login,session,loginObj.acct,CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)<=0))
						{
							if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
							{
								session.promptPrint("\n\r'"+CMStrings.capitalizeAndLower(loginObj.login)+"' does not exist.\n\rIs this a new account you would like to create (y/N)?");
								loginObj.state=LoginState.CREATE_ACCOUNT_CONFIRM;
								return LoginResult.INPUT_REQUIRED;
							}
							else
							{
								session.promptPrint("\n\r'"+CMStrings.capitalizeAndLower(loginObj.login)+"' does not exist.\n\rIs this a new character you would like to create (y/N)?");
								loginObj.state=LoginState.CREATE_CHAR_CONFIRM;
								return LoginResult.INPUT_REQUIRED;
							}
						}
						loginObj.state=LoginState.START;
						break;
					}
					else
					{
						for(Session otherSess : CMLib.sessions().allIterable())
							if((otherSess!=session)&&(otherSess.isPendingLogin(loginObj)))
							{
								session.println("A previous login is still pending.  Please be patient.");
								session.stopSession(false, false, false);
								loginObj.reset=true;
								loginObj.state=LoginState.START;
								return LoginResult.NO_LOGIN;
							}
						session.promptPrint("password: ");
						loginObj.state=LoginState.PLAYER_PASS_RECEIVED;
						return LoginResult.INPUT_REQUIRED;
					}
				}
				case CREATE_ACCOUNT_CONFIRM:
				{
					LoginResult result=LoginResult.NO_LOGIN;
					if(loginObj.lastInput.toUpperCase().startsWith("Y"))
					{
						loginObj.acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
						loginObj.state=LoginState.ACCTCREATE_START;
						break;
					}
					if(result==LoginResult.NO_LOGIN)
					{
						loginObj.state=LoginState.START;
						break;
					}
					return result;
				}
				case CREATE_CHAR_CONFIRM:
				{
					LoginResult result=LoginResult.NO_LOGIN;
					if(loginObj.lastInput.toUpperCase().startsWith("Y"))
					{
						//loginObj.state=LoginState.CREATE_CHAR_START;
						if(createCharacter(loginObj.acct,loginObj.login,session)==LoginResult.CCREATION_EXIT)
							result = LoginResult.NORMAL_LOGIN;
					}
					if(result==LoginResult.NO_LOGIN)
					{
						loginObj.state=LoginState.START;
						break;
					}
					return result;
				}
				case PLAYER_PASS_RECEIVED:
				{
					loginObj.password=loginObj.lastInput;
					if(loginObj.player.matchesPassword(loginObj.password))
					{
						LoginResult prelimResults = prelimChecks(session,loginObj.login,loginObj.player);
						if(prelimResults!=null)
						{
							if(prelimResults==LoginResult.NO_LOGIN)
							{
								loginObj.state=LoginState.START;
								break;
							}
							return prelimResults;
						}
						
						if(loginObj.acct!=null)
						{
							if(isExpired(loginObj.acct,session,loginObj.player.expiration)) 
								return LoginResult.NO_LOGIN;
							session.setAccount(loginObj.acct);
							StringBuilder loginMsg=new StringBuilder("");
							loginMsg.append(session.getAddress()).append(" "+session.getTerminalType())
							.append((session.getClientTelnetMode(Session.TELNET_COMPRESS)||session.getClientTelnetMode(Session.TELNET_COMPRESS2))?" CMP":"")
							.append(session.getClientTelnetMode(Session.TELNET_MSDP)?" MSDP":"")
							.append(", account login: "+loginObj.acct.accountName());
							Log.sysOut(loginMsg.toString());
							//session.setStatus(SessionStatus.ACCOUNTMENU);
							loginObj.state=LoginState.ACCTMENU_START;
							break;
						}
						else
						{
							LoginResult completeResult=completeCharacterLogin(session,loginObj.login, loginObj.wizi);
							if(completeResult == LoginResult.NO_LOGIN)
							{
								loginObj.state=LoginState.START;
								break;
							}
						}
					}
					else
					{
						Log.sysOut("Failed login: "+loginObj.player.name);
						session.println("\n\rInvalid password.\n\r");
						if((!session.isStopped())
						&&(loginObj.player.email.length()>0)
						&&(loginObj.player.email.indexOf('@')>0)
						&&(loginObj.attempt>2)
						&&(CMProps.getVar(CMProps.Str.MUDDOMAIN).length()>0))
						{
							if(CMProps.getBoolVar(CMProps.Bool.HASHPASSWORDS))
								session.promptPrint("Would you like you have a new password generated and e-mailed to you (y/N)? ");
							else
								session.promptPrint("Would you like your password e-mailed to you (y/N)? ");
							loginObj.state=LoginState.CONFIRM_EMAIL_PASSWORD;
							return LoginResult.INPUT_REQUIRED;
						}
						loginObj.state=LoginState.START;
						break;
					}
					session.println("\n\r");
					return LoginResult.NORMAL_LOGIN;
				}
				case CONFIRM_EMAIL_PASSWORD:
				{
					if(loginObj.lastInput.toUpperCase().startsWith("Y"))
					{
						String password=loginObj.player.password;
						if(CMProps.getBoolVar(CMProps.Bool.HASHPASSWORDS))
						{
							MOB playerM=CMLib.players().getLoadPlayer(loginObj.player.name);
							if((playerM!=null)&&(playerM.playerStats()!=null))
							{
								password=CMLib.encoder().generateRandomPassword();
								playerM.playerStats().setPassword(password);
								loginObj.player.password=playerM.playerStats().getPasswordStr();
								CMLib.database().DBUpdatePassword(loginObj.player.name, loginObj.player.password);
							}
						}
						if(CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.Str.SMTPSERVERNAME),
							"passwords@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(),
							"noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(),
							loginObj.player.email,
							"Password for "+loginObj.player.name,
							"Your password for "+loginObj.player.name+" at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" is: '"+password+"'."))
							session.println("Email sent.\n\r");
						else
							session.println("Error sending email.\n\r");
						session.stopSession(false,false,false);
						loginObj.reset=true;
						loginObj.state=LoginState.START;
						return LoginResult.NO_LOGIN;
					}
					loginObj.state=LoginState.START;
					break;
				}
				case ACCTMENU_START:
				{
					final PlayerAccount acct=loginObj.acct;
					session.setServerTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.FLAG_ANSI));
					session.setClientTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.FLAG_ANSI));
					// if its not a new account, do this?
					StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"selchar.txt",null,true).text();
					try { introText = CMLib.webMacroFilter().virtualPageFilter(introText);}catch(Exception ex){}
					session.println(null,null,null,"\n\r\n\r"+introText.toString());
					if(acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
					{
						loginObj.state=LoginState.ACCTMENU_SHOWCHARS;
					}
					else
					{
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
					}
					
					break;
				}
				case ACCTMENU_SHOWCHARS:
				{
					final PlayerAccount acct=loginObj.acct;
					StringBuffer buf = new StringBuffer("");
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
					loginObj.state=LoginState.ACCTMENU_PROMPT;
					break;
				}
				case ACCTMENU_SHOWMENU:
				{
					final PlayerAccount acct=loginObj.acct;
					StringBuffer buf = new StringBuffer("");
					if(!acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
					{
						buf.append(" ^XAccount Menu^.^N\n\r");
						buf.append(" ^XL^.^w)^Hist characters\n\r");
						buf.append(" ^XN^.^w)^Hew character\n\r");
						if(acct.isSet(PlayerAccount.FLAG_CANEXPORT))
						{
							buf.append(" ^XI^.^w)^Hmport character\n\r");
							buf.append(" ^XE^.^w)^Hxport character\n\r");
						}
						buf.append(" ^XD^.^w)^Helete/Retire character\n\r");
						buf.append(" ^XH^.^w)^Help\n\r");
						buf.append(" ^XM^.^w)^Henu OFF\n\r");
						buf.append(" ^XQ^.^w)^Huit (logout)\n\r");
						buf.append("\n\r^H ^w(^HEnter your character name to login^w)^H");
						session.println(buf.toString());
						buf.setLength(0);
					}
					loginObj.state=LoginState.ACCTMENU_PROMPT;
					break;
				}
				case ACCTMENU_PROMPT:
				{
					if(!session.isStopped())
						session.setInputLoopTime();
					session.setStatus(Session.SessionStatus.ACCOUNT_MENU);
					session.promptPrint("\n\r^wCommand or Name ^H(?)^w: ^N");
					loginObj.state=LoginState.ACCTMENU_COMMAND;
					loginObj.acctCmd=null;
					return LoginResult.INPUT_REQUIRED;
				}
				case ACCTMENU_CONFIRMCOMMAND:
				{
					if(loginObj.lastInput.toUpperCase().startsWith("Y"))
					{
						loginObj.lastInput=loginObj.acctCmd+" <CONFIRMED>";
						loginObj.state=LoginState.ACCTMENU_COMMAND;
					}
					else
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
					break;
				}
				case ACCTMENU_ADDTOCOMMAND:
				{
					if(loginObj.lastInput.length()>0)
					{
						loginObj.lastInput=loginObj.acctCmd+" "+loginObj.lastInput;
						loginObj.state=LoginState.ACCTMENU_COMMAND;
					}
					else
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
					break;
				}
				case ACCTMENU_COMMAND:
				{
					final PlayerAccount acct=loginObj.acct;
					final String s=loginObj.lastInput.trim();
					if(s==null) return LoginResult.NO_LOGIN;
					if(s.length()==0)
					{
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					loginObj.acctCmd=s;
					final String[] parms=CMParms.parse(s).toArray(new String[0]);
					final String cmd=parms[0].toUpperCase().trim();
					if(cmd.equalsIgnoreCase("?")||(("HELP").startsWith(cmd)))
					{
						StringBuffer accountHelp=new CMFile(Resources.buildResourcePath("help")+"accts.txt",null,true).text();
						try { accountHelp = CMLib.webMacroFilter().virtualPageFilter(accountHelp);}catch(Exception ex){}
						session.println(null,null,null,"\n\r\n\r"+accountHelp.toString());
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					if(("LIST").startsWith(cmd))
					{
						loginObj.state=LoginState.ACCTMENU_SHOWCHARS;
						break;
					}
					if(("QUIT").startsWith(cmd))
					{
						if((parms.length>1)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
						{
							session.println("Bye Bye!");
							session.stopSession(false,false,false);
							return LoginResult.NO_LOGIN;
						}
						session.promptPrint("Quit -- are you sure (y/N)?");
						loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
						return LoginResult.INPUT_REQUIRED;
					}
					if(("NEW ").startsWith(cmd))
					{
						if((parms.length<2)||(parms[1].length()==0))
						{
							session.promptPrint("\n\rPlease enter a name for your character: ");
							loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						if(newCharactersAllowed(parms[1],session,acct,parms[1].equalsIgnoreCase(acct.accountName())))
						{
							final String login=CMStrings.capitalizeAndLower(parms[1]);
							if((parms.length>2)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
							{
								if(!session.isStopped())
									session.setInputLoopTime();
								if(createCharacter(acct, login, session) == LoginResult.CCREATION_EXIT)
									return LoginResult.CCREATION_EXIT;
								loginObj.state=LoginState.ACCTMENU_SHOWMENU;
								break;
							}
							session.promptPrint("Create a new character called '"+login+"' (y/N)?");
							loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					if(("MENU").startsWith(cmd))
					{
						if((parms.length>1)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
						{
							if(acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
							{
								session.println("Menus are back on.");
								acct.setFlag(PlayerAccount.FLAG_ACCOUNTMENUSOFF, false);
							}
							else
							if(!acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF))
							{
								session.println("Menus are now off.");
								acct.setFlag(PlayerAccount.FLAG_ACCOUNTMENUSOFF, true);
							}
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
							
						}
						else
						{
							final String promptStr=acct.isSet(PlayerAccount.FLAG_ACCOUNTMENUSOFF)?"Turn menus back on (y/N)?":"Turn menus off (y/N)?";
							session.promptPrint(promptStr);
							loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						break;
					}
					if(("RETIRE").startsWith(cmd)||("DELETE ").startsWith(cmd))
					{
						if((parms.length<2)||(parms[1].length()==0))
						{
							session.promptPrint("\n\rPlease the name of the character: ");
							loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						PlayerLibrary.ThinPlayer delMeChk = null;
						for(Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
						{
							PlayerLibrary.ThinPlayer player = p.nextElement();
							if(player.name.equalsIgnoreCase(parms[1]))
								delMeChk=player;
						}
						if(delMeChk==null)
						{
							session.println("The character '"+CMStrings.capitalizeAndLower(parms[1])+"' is unknown.");
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
							break;
						}
						final PlayerLibrary.ThinPlayer delMe = delMeChk;
						if((parms.length>2)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
						{
							MOB M=CMLib.players().getLoadPlayer(delMe.name);
							if(M!=null)
							{
								CMLib.players().obliteratePlayer(M, true, false);
							}
							session.println(delMe.name+" has been deleted.");
						}
						else
						{
							session.promptPrint("Are you sure you want to retire and delete '"+delMe.name+"' (y/N)?");
							loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					if(("EXPORT ").startsWith(cmd)&&(acct.isSet(PlayerAccount.FLAG_CANEXPORT)))
					{
						if((parms.length<2)||(parms[1].length()==0))
						{
							session.promptPrint("\n\rPlease the name of the character: ");
							loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						PlayerLibrary.ThinPlayer delMe = null;
						for(Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
						{
							PlayerLibrary.ThinPlayer player = p.nextElement();
							if(player.name.equalsIgnoreCase(parms[1]))
								delMe=player;
						}
						if(delMe==null)
						{
							session.println("The character '"+CMStrings.capitalizeAndLower(parms[1])+"' is unknown.");
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
							break;
						}
						if((parms.length<3)||(parms[2].length()==0))
						{
							session.promptPrint("\n\rEnter a new password for this character: ");
							loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						String password=parms[2];
						if((password==null)||(password.trim().length()==0))
						{
							session.println("Aborted.");
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
							break;
						}
						if((parms.length>3)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
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
						else
						{
							session.promptPrint("Are you sure you want to remove character  '"+delMe.name+"' from your account (y/N)?");
							loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					if(("IMPORT ").startsWith(cmd))
					{
						if((parms.length<2)||(parms[1].length()==0))
						{
							session.promptPrint("\n\rPlease the name of the character: ");
							loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						if((CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)<=acct.numPlayers())
						&&(!acct.isSet(PlayerAccount.FLAG_NUMCHARSOVERRIDE)))
						{
							session.println("You may only have "+CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)+" characters.  Please delete one to create another.");
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
							break;
						}
						String name=CMStrings.capitalizeAndLower(parms[1]);
						final PlayerLibrary.ThinnerPlayer newCharT = CMLib.database().DBUserSearch(name);
						if((parms.length<3)||(parms[2].length()==0))
						{
							session.promptPrint("\n\rEnter the existing password for your character '"+name+"': ");
							loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						String password=parms[2];
						if((password==null)||(password.trim().length()==0))
						{
							session.println("Aborted.");
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
							break;
						}
						if((newCharT==null)
						||(!newCharT.matchesPassword(password))
						||((newCharT.accountName!=null)
							&&(newCharT.accountName.length()>0)
							&&(!newCharT.accountName.equalsIgnoreCase(acct.accountName()))))
						{
							session.println("Character name or password is incorrect.");
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
							break;
						}
						if((parms.length>3)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
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
							loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						}
						else
						{
							session.promptPrint("Are you sure you want to import character  '"+newCharT.name+"' into your account (y/N)?");
							loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
							return LoginResult.INPUT_REQUIRED;
						}
						break;
					}
					boolean wizi=(parms.length>1)&&(parms[parms.length-1]).equalsIgnoreCase("!");
					PlayerLibrary.ThinnerPlayer playMe = null;
					String name=CMStrings.capitalizeAndLower(cmd);
					final String playerName=acct.findPlayer(name);
					if(playerName!=null)
					{
						name=playerName;
						playMe = CMLib.database().DBUserSearch(name);
					}
					if(playMe == null)
					{
						session.println("'"+name+"' is an unknown character or command.  Use ? for help.");
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					MOB realMOB=CMLib.players().getLoadPlayer(playMe.name);
					if(realMOB==null)
					{
						session.println("Error loading character '"+name+"'.  Please contact the management.");
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					int numAccountOnline=0;
					for(Session S : CMLib.sessions().allIterable())
						if((S.mob()!=null)
						&&(S.mob().playerStats()!=null)
						&&(S.mob().playerStats().getAccount()==acct))
							numAccountOnline++;
					if((CMProps.getIntVar(CMProps.Int.MAXCONNSPERACCOUNT)>0)
					&&(numAccountOnline>=CMProps.getIntVar(CMProps.Int.MAXCONNSPERACCOUNT))
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAXCONNSPERACCOUNT))
					&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_CONNS, session.getAddress()))
					&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, playMe.accountName))
					&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, playMe.name))
					&&(!acct.isSet(PlayerAccount.FLAG_MAXCONNSOVERRIDE)))
					{
						session.println("You may only have "+CMProps.getIntVar(CMProps.Int.MAXCONNSPERACCOUNT)+" of your characters on at one time.");
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					playMe.loadedMOB=realMOB;
					LoginResult prelimResults = prelimChecks(session,playMe.name,playMe);
					if(prelimResults!=null)
						return prelimResults;
					LoginResult completeResult=completeCharacterLogin(session,playMe.name, wizi);
					if(completeResult == LoginResult.NO_LOGIN)
					{
						loginObj.state=LoginState.ACCTMENU_SHOWMENU;
						break;
					}
					return LoginResult.NORMAL_LOGIN;
				}
				case CHARCR_START:
				{
					session.setStatus(Session.SessionStatus.CHARCREATE);
					
					loginObj.login=CMStrings.capitalizeAndLower(loginObj.login.trim());
					Log.sysOut("Creating user: "+loginObj.login);
					
					StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newchar.txt",null,true).text();
					try { introText = CMLib.webMacroFilter().virtualPageFilter(introText); }catch(Exception ex){}
					session.println(null,null,null,"\n\r\n\r"+introText.toString());
					String password=(loginObj.acct!=null)?loginObj.acct.getPasswordStr():"";
					
					boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
							 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
					if((!emailPassword)&&(password.length()==0))
					{
						session.promptPrint("\n\rEnter a password: ");
						loginObj.state=LoginState.CHARCR_PWDONE;
						return LoginResult.INPUT_REQUIRED;
					}
					loginObj.state=LoginState.CHARCR_PWDONE;
					break;
				}
				case CHARCR_PWDONE:
				{
					String password=(loginObj.acct!=null)?loginObj.acct.getPasswordStr():"";
					boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
							 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
					if((!emailPassword)&&(password.length()==0))
					{
						password=loginObj.lastInput;
						if(password.length()==0)
						{
							session.println("\n\rYou must enter a password to continue.");
							session.promptPrint("\n\rEnter a password: ");
							loginObj.state=LoginState.CHARCR_PWDONE;
							return LoginResult.INPUT_REQUIRED;
						}
					}
					loginObj.password=password;
					PlayerAccount acct=loginObj.acct;
					MOB mob=CMClass.getMOB("StdMOB");
					loginObj.crM=mob;
					mob.setSession(session);
					session.setMob(mob);
					mob.setBitmap(MOB.ATT_AUTOEXITS|MOB.ATT_AUTOWEATHER);
					setGlobalBitmaps(mob);
					
					if((acct==null)||(acct.getPasswordStr().length()==0))
					{
						mob.playerStats().setPassword(password);
						executeScript(mob,getLoginScripts().get("PASSWORD"));
					}
					
					if((acct!=null)&&(acct.getEmail().length()>0))
					{
						mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_AUTOFORWARD));
						loginObj.state=LoginState.CHARCR_EMAILDONE;
					}
					else
					if(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE"))
					{
						mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_AUTOFORWARD));
						loginObj.state=LoginState.CHARCR_EMAILSTART;
					}
					else
					{
						mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_AUTOFORWARD));
						loginObj.state=LoginState.CHARCR_EMAILDONE;
					}
					break;
				}
				case CHARCR_EMAILSTART:
				{
					StringBuffer emailIntro=new CMFile(Resources.buildResourcePath("text")+"email.txt",null,true).text();
					try { emailIntro = CMLib.webMacroFilter().virtualPageFilter(emailIntro);}catch(Exception ex){}
					session.println(null,null,null,emailIntro.toString());
					loginObj.state=LoginState.CHARCR_EMAILPROMPT;
					break;
				}
				case CHARCR_EMAILPROMPT:
				{
					session.promptPrint("\n\rEnter your e-mail address: ");
					loginObj.state=LoginState.CHARCR_EMAILENTERED;
					return LoginResult.INPUT_REQUIRED;
				}
				case CHARCR_EMAILENTERED:
				{
					boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
							 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
					boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
					String newEmail=loginObj.lastInput;
					if((emailReq||emailPassword) 
					&& ((newEmail==null)||(newEmail.trim().length()==0)||(!CMLib.smtp().isValidEmailAddress(newEmail))))
					{
						session.println("\n\rA valid email address is required.\n\r");
						loginObj.state=LoginState.CHARCR_EMAILPROMPT;
						break;
					}
					loginObj.acct.setEmail(newEmail);
					loginObj.lastInput=newEmail;
					if(emailPassword) session.println("This email address will be used to send you a password.");
					if(emailReq||emailPassword)
					{
						session.promptPrint("Confirm that '"+newEmail+"' is correct by re-entering.\n\rRe-enter: ");
						loginObj.state=LoginState.CHARCR_EMAILCONFIRMED;
						return LoginResult.INPUT_REQUIRED;
					}
					loginObj.state=LoginState.CHARCR_EMAILCONFIRMED;
					break;
				}
				case CHARCR_EMAILCONFIRMED:
				{
					final MOB mob=loginObj.crM;
					boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
					String newEmail=loginObj.acct.getEmail();
					boolean emailConfirmed=false;
					if((newEmail.length()>0)&&(newEmail.equalsIgnoreCase(loginObj.lastInput)))
						emailConfirmed=CMLib.smtp().isValidEmailAddress(newEmail);
					loginObj.crM.playerStats().setEmail("");
					if(emailConfirmed||((!emailReq)&&(newEmail.trim().length()==0)))
					{
						loginObj.crM.playerStats().setEmail(newEmail);
						loginObj.crM.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_AUTOFORWARD));
						loginObj.state=LoginState.CHARCR_EMAILDONE;
						break;
					}
					session.println("\n\rThat email address combination was invalid.\n\r");
					loginObj.state=LoginState.CHARCR_EMAILPROMPT;
					break;
				}
				case CHARCR_EMAILDONE:
				{
					final MOB mob=loginObj.crM;
					final PlayerAccount acct=loginObj.acct;
					if((mob.playerStats().getEmail()!=null)&&CMSecurity.isBanned(mob.playerStats().getEmail()))
					{
						session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
						if(mob==session.mob())
							session.stopSession(false,false,false);
						throw new Exception("");
					}
					mob.playerStats().setAccount(acct);

					executeScript(mob,getLoginScripts().get("EMAIL"));
			
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
						loginObj.state=LoginState.CHARCR_ANSIDONE;
					}
					else
					{
						session.promptPrint("\n\rDo you want ANSI colors (Y/n)?");
						loginObj.state=LoginState.CHARCR_ANSICONFIRMED;
					}
					break;
				}
				case CHARCR_ANSICONFIRMED:
				{
					final MOB mob=loginObj.crM;
					loginObj.state=LoginState.CHARCR_ANSIDONE;
					if(loginObj.lastInput.toUpperCase().startsWith("N"))
					{
						mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_ANSI));
						session.setServerTelnetMode(Session.TELNET_ANSI,false);
						session.setClientTelnetMode(Session.TELNET_ANSI,false);
					}
					else
						mob.setBitmap(CMath.setb(mob.getBitmap(),MOB.ATT_ANSI));
					break;
				}
				case CHARCR_ANSIDONE:
				{
					final MOB mob=loginObj.crM;
					if((session.getClientTelnetMode(Session.TELNET_MSP))
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP)))
						mob.setBitmap(mob.getBitmap()|MOB.ATT_SOUND);
					if((session.getClientTelnetMode(Session.TELNET_MXP))
					&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
						mob.setBitmap(mob.getBitmap()|MOB.ATT_MXP);
			
					executeScript(mob,getLoginScripts().get("ANSI"));
					
					int themeCode=CMProps.getIntVar(CMProps.Int.MUDTHEME);
					switch(themeCode)
					{
						case Area.THEME_FANTASY:
						case Area.THEME_HEROIC:
						case Area.THEME_TECHNOLOGY:
							loginObj.crTheme=themeCode;
							loginObj.state=LoginState.CHARCR_THEMEDONE;
							break;
						default:
							loginObj.state=LoginState.CHARCR_THEMESTART;
							break;
					}
					break;
				}
				case CHARCR_THEMESTART:
				{
					int themeCode=CMProps.getIntVar(CMProps.Int.MUDTHEME);
					loginObj.crTheme=-1;
					String selections="";
					if(CMath.bset(themeCode,Area.THEME_FANTASY)){ selections+="/F";}
					if(CMath.bset(themeCode,Area.THEME_HEROIC)){ selections+="/H";}
					if(CMath.bset(themeCode,Area.THEME_TECHNOLOGY)){ selections+="/T";}
					if(selections.length()==0)
						selections="/F";
					StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"themes.txt",null,true).text();
					try { introText = CMLib.webMacroFilter().virtualPageFilter(introText);}catch(Exception ex){}
					session.println(null,null,null,introText.toString());
					session.promptPrint("\n\r^!Please select from the following:^N "+selections.substring(1)+"\n\r: ");
					loginObj.state=LoginState.CHARCR_THEMEPICKED;
					break;
				}
				case CHARCR_THEMEPICKED:
				{
					int themeCode=CMProps.getIntVar(CMProps.Int.MUDTHEME);
					String themeStr=loginObj.lastInput;
					if(themeStr.toUpperCase().startsWith("F") && CMath.bset(themeCode,Area.THEME_FANTASY))
						loginObj.crTheme=Area.THEME_FANTASY;
					if(themeStr.toUpperCase().startsWith("H") && CMath.bset(themeCode,Area.THEME_HEROIC))
						loginObj.crTheme=Area.THEME_HEROIC;
					if(themeStr.toUpperCase().startsWith("T") && CMath.bset(themeCode,Area.THEME_TECHNOLOGY))
						loginObj.crTheme=Area.THEME_TECHNOLOGY;
					if(loginObj.crTheme<0)
					{
						session.println("\n\rThat is not a valid choice.\n\r");
						loginObj.state=LoginState.CHARCR_THEMESTART;
					}
					else
						loginObj.state=LoginState.CHARCR_THEMEDONE;
					break;
				}
				case CHARCR_THEMEDONE:
				{
					final MOB mob=loginObj.crM;
					executeScript(mob,getLoginScripts().get("THEME"));
					loginObj.state=LoginState.CHARCR_RACESTART;
					break;
				}
				case CHARCR_RACESTART:
				{
					final MOB mob=loginObj.crM;
					if(CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
					{
						Race newRace=CMClass.getRace("PlayerRace");
						if(newRace==null)
							newRace=CMClass.getRace("StdRace");
						if(newRace != null)
						{
							mob.baseCharStats().setMyRace(newRace);
							loginObj.state=LoginState.CHARCR_RACEDONE;
							break;
						}
						else
						{
							Log.errOut("CharCreation","Races are disabled, but neither PlayerRace nor StdRace exists?!");
						}
					}
					if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
					{
						StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"races.txt",null,true).text();
						try { introText = CMLib.webMacroFilter().virtualPageFilter(introText);}catch(Exception ex){}
						session.println(null,null,null,introText.toString());
					}
					StringBuffer listOfRaces=new StringBuffer("[");
					boolean tmpFirst = true;
					List<Race> qualRaces = raceQualifies(mob,loginObj.crTheme);
					for(Race R : qualRaces)
					{
						if (!tmpFirst)
							listOfRaces.append(", ");
						else
							tmpFirst = false;
						listOfRaces.append("^H"+R.name()+"^N");
					}
					listOfRaces.append("]");
					session.println("\n\r^!Please choose from the following races (?):^N");
					session.print(listOfRaces.toString());
					session.promptPrint("\n\r: ");
					loginObj.state=LoginState.CHARCR_RACEENTERED;
					return LoginResult.INPUT_REQUIRED;
				}
				case CHARCR_RACEENTERED:
				{
					String raceStr=loginObj.lastInput;
					final MOB mob=loginObj.crM;

					if(raceStr.trim().equalsIgnoreCase("?"))
						session.println(null,null,null,"\n\r"+new CMFile(Resources.buildResourcePath("text")+"races.txt",null,true).text().toString());
					else
					{
						Race newRace=CMClass.getRace(raceStr);
						if((newRace!=null)&&((!CMProps.isTheme(newRace.availabilityCode()))
												||(!CMath.bset(newRace.availabilityCode(),loginObj.crTheme))
												||(CMath.bset(newRace.availabilityCode(),Area.THEME_SKILLONLYMASK))))
							newRace=null;
						if(newRace==null)
							for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
							{
								Race R=r.nextElement();
								if((R.name().equalsIgnoreCase(raceStr))
								&&(CMProps.isTheme(R.availabilityCode()))
								&&(CMath.bset(R.availabilityCode(),loginObj.crTheme))
								&&(!CMath.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK)))
								{
									newRace=R;
									break;
								}
							}
						if(newRace==null)
							for(Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
							{
								Race R=r.nextElement();
								if((R.name().toUpperCase().startsWith(raceStr.toUpperCase()))
								&&(CMProps.isTheme(R.availabilityCode()))
								&&(CMath.bset(R.availabilityCode(),loginObj.crTheme))
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
							session.promptPrint("^!Is ^H"+newRace.name()+"^N^! correct (Y/n)?^N");
							loginObj.state=LoginState.CHARCR_RACECONFIRMED;
							mob.baseCharStats().setMyRace(newRace);
							return LoginResult.INPUT_REQUIRED;
						}
					}
					loginObj.state=LoginState.CHARCR_RACESTART;
					break;
				}
				case CHARCR_RACECONFIRMED:
				{
					if(loginObj.lastInput.toUpperCase().startsWith("N"))
						loginObj.state=LoginState.CHARCR_RACESTART;
					else
						loginObj.state=LoginState.CHARCR_RACEDONE;
					break;
				}
				case CHARCR_RACEDONE:
				{
					final MOB mob=loginObj.crM;
					mob.baseState().setHitPoints(CMProps.getIntVar(CMProps.Int.STARTHP));
					mob.baseState().setMovement(CMProps.getIntVar(CMProps.Int.STARTMOVE));
					mob.baseState().setMana(CMProps.getIntVar(CMProps.Int.STARTMANA));
			
					executeScript(mob,getLoginScripts().get("RACE"));
					loginObj.state=LoginState.CHARCR_GENDERSTART;
					break;
				}
				case CHARCR_GENDERSTART:
				{
					session.promptPrint("\n\r^!What is your gender (M/F)?^N");
					loginObj.state=LoginState.CHARCR_GENDERDONE;
					break;
				}
				case CHARCR_GENDERDONE:
				{
					final MOB mob=loginObj.crM;
					final PlayerAccount acct=loginObj.acct;
					
					String gender=loginObj.lastInput.toUpperCase().trim();
					if((!gender.startsWith("M"))&&(!gender.startsWith("F")))
					{
						loginObj.state=LoginState.CHARCR_GENDERSTART;
						break;
					}
					mob.baseCharStats().setStat(CharStats.STAT_GENDER,gender.toUpperCase().charAt(0));
					
					mob.baseCharStats().getMyRace().startRacing(mob,false);
					
					executeScript(mob,getLoginScripts().get("GENDER"));
					
					if((CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))&&(mob.playerStats()!=null)&&(acct==null))
						mob.playerStats().setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*(CMProps.getIntVar(CMProps.Int.TRIALDAYS))));
					
					if(CMProps.getIntVar(CMProps.Int.STARTSTAT)>0)
					{
						mob.baseCharStats().setAllBaseValues(CMProps.getIntVar(CMProps.Int.STARTSTAT));
						mob.recoverCharStats();
						loginObj.state=LoginState.CHARCR_STATDONE;
					}
					else
					{
						StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"stats.txt",null,true).text();
						try { introText = CMLib.webMacroFilter().virtualPageFilter(introText);}catch(Exception ex){}
						session.println(null,null,null,"\n\r\n\r"+introText.toString());

						final boolean randomRoll = CMProps.getIntVar(CMProps.Int.STARTSTAT) == 0;
						loginObj.crPoints = getTotalStatPoints();
						for(int i=0;i<CharStats.CODES.BASE().length;i++)
							mob.baseCharStats().setStat(i,CMProps.getIntVar(CMProps.Int.BASEMINSTAT));
						mob.recoverCharStats();
						loginObj.state=LoginState.CHARCR_STATSTART;
					}
					break;
				}
				case CHARCR_STATSTART:
				{
					final MOB mob=loginObj.crM;
					final PlayerAccount acct=loginObj.acct;
					CharStats unmodifiedCT = (CharStats)mob.charStats().copyOf();
					List<String> validStats = new ArrayList<String>(CharStats.CODES.BASE().length);
					for(int i : CharStats.CODES.BASE())
						validStats.add(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(i)));
					List<CharClass> qualifyingClassListV=new Vector<CharClass>(1);
					final boolean randomRoll = CMProps.getIntVar(CMProps.Int.STARTSTAT) == 0;
					if(randomRoll)
					{
						unmodifiedCT.copyInto(mob.baseCharStats());
						reRollStats(mob,mob.baseCharStats(),loginObj.crPoints);
					}

					mob.recoverCharStats();
					qualifyingClassListV=classQualifies(mob,loginObj.crTheme);
						
					if(!randomRoll || (qualifyingClassListV.size()>0)||CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
					{
						int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
						StringBuffer statstr=new StringBuffer("Your current stats are: \n\r");
						CharStats CT=mob.baseCharStats();
						int total=0;
						for(int i : CharStats.CODES.BASE())
						{
							total += CT.getStat(i);
							statstr.append(CMStrings.padRight(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i)),15)
									+": "+CMStrings.padRight(Integer.toString(CT.getStat(i)),2)+"/"+(max+CT.getStat(CharStats.CODES.toMAXBASE(i)))+"\n\r");
						}
						statstr.append(CMStrings.padRight("STATS TOTAL",15)+": "+total+"/"+(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)*6));
						session.println(statstr.toString());
						if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)
						&&(!mob.baseCharStats().getMyRace().classless())
						&&(randomRoll || qualifyingClassListV.size()>0)
						&&((qualifyingClassListV.size()!=1)||(!CMProps.getVar(CMProps.Str.MULTICLASS).startsWith("APP-"))))
							session.println("\n\rThis would qualify you for ^H"+buildQualifyingClassList(qualifyingClassListV,"and")+"^N.");
						if(randomRoll)
						{
							session.promptPrint("^!Would you like to re-roll (y/N)?^N");
							loginObj.state=LoginState.CHARCR_STATCONFIRM;
						}
						else
						{
							String promptStr;
							if(loginObj.crPoints == 0)
							{
								session.println("\n\r^!You have no more points remaining.^N");
								promptStr = "^!Enter a Stat Name to remove a point, R for a random roll, or ENTER when done.^N\n\r: ^N";
							}
							else
							{
								session.println("\n\r^!You have "+loginObj.crPoints+" points remaining.^N");
								promptStr = "^!Enter a Stat Name to add or remove a point, or R for a random roll.^N\n\r: ^N";
							}
								
							session.promptPrint(promptStr);
							loginObj.state=LoginState.CHARCR_STATPICK;
						}
						break;
					}
					else
						loginObj.state=LoginState.CHARCR_STATDONE;
					break;
				}
				case CHARCR_STATCONFIRM:
				{
					if(loginObj.lastInput.toUpperCase().startsWith("Y"))
						loginObj.state=LoginState.CHARCR_STATSTART;
					else
						loginObj.state=LoginState.CHARCR_STATDONE;
					break;
				}
				case CHARCR_STATPICK:
				{
					final MOB mob=loginObj.crM;
					final PlayerAccount acct=loginObj.acct;
					CharStats CT=mob.baseCharStats();
					CharStats unmodifiedCT=(CharStats)mob.charStats().copyOf();
					final String prompt=loginObj.lastInput;
					if(prompt == null) throw new NullPointerException();
					List<CharClass> qualifyingClassListV=classQualifies(mob,loginObj.crTheme);
					if((loginObj.crPoints == 0)&&(prompt.trim().length()==0))
					{
						if(qualifyingClassListV.size()==0)
						{
							session.println("^rYou do not qualify for any classes.  Please modify your stats until you do.^N");
						}
						else
						{
							loginObj.state=LoginState.CHARCR_STATDONE;
							break;
						}
					}
					if(prompt.toLowerCase().startsWith("r"))
					{
						unmodifiedCT.copyInto(mob.baseCharStats());
						reRollStats(mob,mob.baseCharStats(),getTotalStatPoints());
						loginObj.crPoints=0;
						prompt="";
					}
					else
					if(prompt.trim().length()>0)
					{
						prompt = prompt.trim();
						boolean remove = prompt.startsWith("-");
						int statPointsChange = 0;
						if(remove) prompt = prompt.substring(1).trim();
						int space = prompt.lastIndexOf(' ');
						if((space > 0)&&(CMath.isInteger(prompt.substring(space+1).trim())||(prompt.substring(space+1).trim().startsWith("+"))))
						{
							String numStr = prompt.substring(space+1).trim();
							if(numStr.startsWith("+"))
								numStr=numStr.substring(1).trim();
							prompt = prompt.substring(0,space).trim();
							int num = CMath.s_int(numStr);
							if((num > -1000)&&(num < 1000)&&(num != 0))
								statPointsChange=num;
							if(statPointsChange < 0)
							{
								remove=true;
								statPointsChange = statPointsChange * -1;
							}
						}
						else
						if(remove) 
							statPointsChange=-1;
						
						List<String> validStats = new ArrayList<String>(CharStats.CODES.BASE().length);
						for(int i : CharStats.CODES.BASE())
							validStats.add(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(i)));
						int statCode = CharStats.CODES.findWhole(prompt, false);
						if((statCode < 0)||(!validStats.contains(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(statCode)))))
						{
							session.println("^r'"+prompt+"' is an unknown code.  Try one of these: "+CMParms.toStringList(validStats)+"^N");
							loginObj.state=LoginState.CHARCR_STATSTART;
							break;
						}
						if(statPointsChange == 0)
						{
							String numStr = session.prompt("^!How many points to add or remove (ex: +4, -1): ");
							if(numStr == null) numStr = "";
							numStr=numStr.trim();
							if(numStr.startsWith("+")) numStr=numStr.substring(1);
							if(CMath.isInteger(numStr))
							{
								int num = CMath.s_int(numStr);
								if((num > -1000)&&(num < 1000)&&(num != 0))
									statPointsChange=num;
								if(statPointsChange < 0)
								{
									remove=true;
									statPointsChange = statPointsChange * -1;
								}
							}
							else
							if(numStr.length()>0)
								session.println("^r'"+numStr+"' is not a positive or negative number.^N");
						}
						if(statPointsChange <= 0)
						{
							loginObj.state=LoginState.CHARCR_STATSTART;
							break;
						}
						final String list = CMProps.getVar(CMProps.Str.STATCOSTS);
						long[][] costs=CMLib.utensils().compileConditionalRange(CMParms.parseCommas(list.trim(),true), 1, 0, 101);
						int pointsCost=0;
						int curStatValue=CT.getStat(statCode);
						for(int i=0;i<statPointsChange;i++)
						{
							int statPoint=remove?curStatValue-1:curStatValue;
							int statCost=1;
							if((statPoint>0)&&(statPoint<costs.length)&&(costs[statPoint]!=null)&&(costs[statPoint].length>0)&&(costs[statPoint][0]!=0))
								statCost=(int)costs[statPoint][0];
							pointsCost += remove ? -statCost : statCost;
							curStatValue += remove ? -1 : 1;
						}
						if(loginObj.crPoints - pointsCost < 0)
						{
							if(loginObj.crPoints > 0)
								session.println("^rYou need "+pointsCost+" points to do that, but only have "+loginObj.crPoints+" remaining.^N");
							else
								session.println("^rYou don't have enough remaining points to do that.^N");
							loginObj.state=LoginState.CHARCR_STATSTART;
							break;
						}
						else
						{
							String friendlyName = CMStrings.capitalizeAndLower(CharStats.CODES.NAME(statCode));
							if(remove)
							{
								if(CT.getStat(statCode) <= unmodifiedCT.getStat(statCode))
								{
									session.println("^rYou can not lower '"+friendlyName+" any further.^N");
									loginObj.state=LoginState.CHARCR_STATSTART;
									break;
								}
								else
								if(CT.getStat(statCode)-statPointsChange < unmodifiedCT.getStat(statCode))
								{
									session.println("^rYou can not lower '"+friendlyName+" any further.^N");
									loginObj.state=LoginState.CHARCR_STATSTART;
									break;
								}
							}
							else
							{
								int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
								if(CT.getStat(statCode) >= max)
								{
									session.println("^rYou can not raise '"+friendlyName+" any further.^N");
									loginObj.state=LoginState.CHARCR_STATSTART;
									break;
								}
								else
								if(CT.getStat(statCode)+statPointsChange > max)
								{
									session.println("^rYou can not raise '"+friendlyName+" any by that amount.^N");
									loginObj.state=LoginState.CHARCR_STATSTART;
									break;
								}
							}
							if(remove) statPointsChange = statPointsChange * -1;
							CT.setStat(statCode, CT.getStat(statCode)+statPointsChange);
							loginObj.crPoints -= pointsCost;
						}
					}
					loginObj.state=LoginState.CHARCR_STATSTART;
					break;
				}
				case CHARCR_STATDONE:
				{
					final MOB mob=loginObj.crM;
					executeScript(mob,getLoginScripts().get("STATS"));
					break;
				}
				
				default:
					loginObj.state=LoginState.START;
					break;
				}
			}
		}
		catch(Exception e)
		{
			session.println("\n\r\n\rI'm sorry, but something bad happened. You'll need to re-log.\n\r\n\r");
			Log.errOut(e);
		}
		loginObj.reset=true;
		loginObj.state=LoginState.START;
		return LoginResult.NO_LOGIN;
	}

	public CharClass promptCharClass(int theme, MOB mob, Session session) throws IOException
	{
		CharClass newClass=null;
		List<CharClass> qualClassesV=classQualifies(mob,theme);
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)||mob.baseCharStats().getMyRace().classless())
		{
			if(CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
				newClass=CMClass.getCharClass("PlayerClass");
			if((newClass==null)&&(qualClassesV.size()>0))
				newClass=qualClassesV.get(CMLib.dice().roll(1,qualClassesV.size(),-1));
			if(newClass==null)
				newClass=CMClass.getCharClass("PlayerClass");
			if(newClass==null)
				newClass=CMClass.getCharClass("StdCharClass");
			if(newClass==null)
				Log.errOut("CharCreation", "Char Classes are disabled, but no PlayerClass or StdCharClass is defined?!");
		}
		else
		if(qualClassesV.size()==0)
		{
			for(Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				CharClass C=c.nextElement();
				if(C.getSubClassRule()==CharClass.SubClassRule.ANY)
					newClass=C;
			}
			if(newClass==null)
				newClass=CMClass.getCharClass("Apprentice");
			if(newClass==null) 
				newClass=CMClass.getCharClass("StdCharClass");
			if(newClass==null)
				Log.errOut("CharCreation", "No classes qualified for, but no Apprentice or StdCharClass is defined?!");
		}
		else
		if(qualClassesV.size()==1)
			newClass=qualClassesV.get(0);
		else
		if((session == null)||(session.isStopped()))
			newClass=qualClassesV.get(0);
		else
		{
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)
			&&!mob.baseCharStats().getMyRace().classless())
				session.println(null,null,null,new CMFile(Resources.buildResourcePath("text")+"classes.txt",null,true).text().toString());

			String listOfClasses = buildQualifyingClassList(qualClassesV, "or");
			while(newClass==null)
			{
				session.println("\n\r^!Please choose from the following Classes:");
				session.print("^H[" + listOfClasses + "]^N");
				String ClassStr=session.prompt("\n\r: ","");
				if(ClassStr.trim().equalsIgnoreCase("?"))
					session.println(null,null,null,"\n\r"+new CMFile(Resources.buildResourcePath("text")+"classes.txt",null,true).text().toString());
				else
				{
					newClass=CMClass.findCharClass(ClassStr);
					if(newClass==null)
					for(CharClass C : qualClassesV)
					{
						if(C.name().equalsIgnoreCase(ClassStr))
						{
							newClass=C;
							break;
						}
					}
					if(newClass==null)
					for(CharClass C : qualClassesV)
					{
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
		}
		return newClass;
	}
	
	public LoginResult createCharacter(PlayerAccount acct, String login, Session session) throws java.io.IOException
	{

			CharClass newClass = promptCharClass(theme, mob, session);
			
			mob.basePhyStats().setLevel(1);
			mob.baseCharStats().setCurrentClass(newClass);
			mob.baseCharStats().setClassLevel(newClass,1);
			mob.basePhyStats().setSensesMask(0);
	
			getUniversalStartingItems(theme, mob);
			mob.setWimpHitPoint(5);
	
			CMLib.utensils().outfit(mob,mob.baseCharStats().getMyRace().outfit(mob));
	
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.ALLERGIES)) {
				Ability A=CMClass.getAbility("Allergies");
				if(A!=null) A.invoke(mob,mob,true,0);
			}
	
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
			mob.resetToMaxState();
	
			executeScript(mob,extraScripts.get("CLASS"));
			
			promptFactions(theme, mob, session);
			
			executeScript(mob,extraScripts.get("FACTIONS"));
			
			mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);
			CMLib.utensils().outfit(mob,mob.baseCharStats().getCurrentClass().outfit(mob));
			mob.setStartRoom(getDefaultStartRoom(mob));
			mob.baseCharStats().setStat(CharStats.STAT_AGE,mob.playerStats().initializeBirthday(0,mob.baseCharStats().getMyRace()));
	
			introText=new CMFile(Resources.buildResourcePath("text")+"newchardone.txt",null,true).text();
			try { introText = CMLib.webMacroFilter().virtualPageFilter(introText);}catch(Exception ex){}
			session.println(null,null,null,"\n\r\n\r"+introText.toString());
			session.prompt("");
			if(!session.isStopped())
			{
				if(emailPassword)
				{
					password=CMLib.encoder().generateRandomPassword();
					mob.playerStats().setPassword(password);
					CMLib.database().DBUpdatePassword(mob.Name(),mob.playerStats().getPasswordStr());
					CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.Str.MAILBOX),
							  mob.Name(),
							  mob.Name(),
							  "Password for "+mob.Name(),
							  "Your password for "+mob.Name()+" is: "+password+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.Str.MUDPORTS)+".\n\rYou may use the PASSWORD command to change it once you are online.");
					session.println("Your character has been created.  You will receive an email with your password shortly.");
					try{Thread.sleep(2000);}catch(Exception e){}
					if(mob==session.mob())
						session.stopSession(false,false,false);
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
	
				executeScript(mob,extraScripts.get("END"));
				
				mob.playerStats().setLastIP(session.getAddress());
				Log.sysOut("Created user: "+mob.Name());
				CMProps.addNewUserByIP(session.getAddress());
				notifyFriends(mob,"^X"+mob.Name()+" has just been created.^.^?");
				if((CMProps.getVar(CMProps.Str.PKILL).startsWith("ALWAYS"))
				&&(!CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
					mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
				if((CMProps.getVar(CMProps.Str.PKILL).startsWith("NEVER"))
				&&(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
					mob.setBitmap(mob.getBitmap()-MOB.ATT_PLAYERKILL);
				CMLib.database().DBUpdatePlayer(mob);
				List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.NEWPLAYERS);
				for(int i=0;i<channels.size();i++)
					CMLib.commands().postChannel(channels.get(i),mob.clans(),mob.Name()+" has just been created.",true);
				CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
				CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_NEWPLAYERS);
			}
		}
		catch(Exception t)
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

	public NewCharNameCheckResult newCharNameCheck(String login, String ipAddress, boolean skipAccountNameCheck)
	{
		if((CMSecurity.isDisabled(CMSecurity.DisFlag.NEWPLAYERS))
		&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_NEWPLAYERS, login))
		&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_NEWPLAYERS, ipAddress)))
			return NewCharNameCheckResult.NO_NEW_PLAYERS;
		else
		if((!isOkName(login))
		|| (CMLib.players().playerExists(login))
		|| (!skipAccountNameCheck && CMLib.players().accountExists(login)))
			return NewCharNameCheckResult.BAD_USED_NAME;
		else
		if((CMProps.getIntVar(CMProps.Int.MUDTHEME)==0)
		||((CMSecurity.isDisabled(CMSecurity.DisFlag.LOGINS))
			&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, login))
			&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, ipAddress))))
				return NewCharNameCheckResult.NO_NEW_LOGINS;
		else
		if((CMProps.getIntVar(CMProps.Int.MAXNEWPERIP)>0)
		&&(CMProps.getCountNewUserByIP(ipAddress)>=CMProps.getIntVar(CMProps.Int.MAXNEWPERIP))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAXNEWPERIP))
		&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_NEWPLAYERS, login))
		&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_NEWPLAYERS, ipAddress)))
			return NewCharNameCheckResult.CREATE_LIMIT_REACHED;
		return NewCharNameCheckResult.OK;
	}
	
	public boolean newCharactersAllowed(String login, Session session, PlayerAccount acct, boolean skipAccountNameCheck)
	{
		switch(newCharNameCheck(login,session.getAddress(),skipAccountNameCheck))
		{
		case NO_NEW_PLAYERS:
			session.println("\n\r'"+CMStrings.capitalizeAndLower(login)+"' is not recognized.");
			return false;
		case BAD_USED_NAME:
			session.println("\n\r'"+CMStrings.capitalizeAndLower(login)+"' is not recognized.\n\rThat name is also not available for new players.\n\r  Choose another name (no spaces allowed)!\n\r");
			return false;
		case NO_NEW_LOGINS:
			session.println("\n\r'"+CMStrings.capitalizeAndLower(login)+"' does not exist.\n\rThis server is not accepting new accounts.\n\r");
			return false;
		case CREATE_LIMIT_REACHED:
			session.println("\n\rThat name is unrecognized.\n\rAlso, the maximum daily new player limit has already been reached for your location.");
			return false;
		default:
			session.println("\n\r'"+CMStrings.capitalizeAndLower(login)+"' is not recognized.");
			return false;
		case OK:
			if((acct!=null)
			&&(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)<=acct.numPlayers())
			&&(!acct.isSet(PlayerAccount.FLAG_NUMCHARSOVERRIDE)))
			{
				session.println("You may only have "+CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)+" characters.  Please retire one to create another.");
				return false;
			}
			return true;
		}
	}

	public void setGlobalBitmaps(MOB mob)
	{
		if(mob==null) return;
		Vector<String> defaultFlagsV=CMParms.parseCommas(CMProps.getVar(CMProps.Str.DEFAULTPLAYERFLAGS).toUpperCase(),true);
		for(int v=0;v<defaultFlagsV.size();v++)
		{
			int x=CMParms.indexOf(MOB.AUTODESC,defaultFlagsV.elementAt(v));
			if(x>=0)
				mob.setBitmap(mob.getBitmap()|(int)CMath.pow(2,x));
		}
	}
	
	public LoginResult completeCharacterLogin(Session session, String login, boolean wiziFlag) throws java.io.IOException
	{
		// count number of multiplays
		int numAtAddress=0;
		try{
			for(Session S : CMLib.sessions().allIterable())
				if((S!=session)&&(session.getAddress().equalsIgnoreCase(S.getAddress())))
					numAtAddress++;
		}catch(Exception e){}

		if((CMProps.getIntVar(CMProps.Int.MAXCONNSPERIP)>0)
		&&(numAtAddress>=CMProps.getIntVar(CMProps.Int.MAXCONNSPERIP))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAXCONNSPERIP))
		&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_CONNS, session.getAddress()))
		&&(!CMProps.isOnWhiteList(CMProps.SYSTEMWL_LOGINS, login)))
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
					C.execute(mob,new XVector<Object>("WIZINV"),0);
			}
			showTheNews(mob);
			Room startRoom = mob.location();
			if(startRoom==null)
			{
				Log.errOut("CharCreation",mob.name()+" has no location.. sending to start room");
				startRoom = mob.getStartRoom();
				if(startRoom == null) 
					startRoom = CMLib.map().getStartRoom(mob);
				
			}
			mob.bringToLife(startRoom,false);
			CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
			startRoom.showOthers(mob,startRoom,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
		}
		else
		{
			final boolean resetStats=(mob==null);
			if(resetStats)
				mob=CMLib.players().getLoadPlayer(login);
			if((mob == null) || (session == null))
			{
				Log.errOut("CharCreation",login+" does not exist ("+(session!=null)+")! FAIL!");
				return LoginResult.NO_LOGIN;
			}
			if(mob.playerStats()==null)
			{
				Log.errOut("CharCreation",login+" is not a player! FAIL!");
				session.println("Error occurred trying to login as this player. Please contact your technical support.");
				return LoginResult.NO_LOGIN;
			}
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
					C.execute(mob,new XVector<Object>("WIZINV"),0);
			}
			showTheNews(mob);
			mob.bringToLife(mob.location(),resetStats);
			CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
			mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,"<S-NAME> appears!");
		}
		for(int f=0;f<mob.numFollowers();f++)
		{
			MOB follower=mob.fetchFollower(f);
			if(follower==null) continue;
			Room R=follower.location();
			if((follower.isMonster())
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
		&&(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION")))
		{
			Command C=CMClass.getCommand("Email");
			if(C!=null)
			{
				if(!C.execute(mob,null,0))
				{
					session.stopSession(false,false,false);
					return LoginResult.NO_LOGIN;
				}
			}
			CMLib.database().DBUpdateEmail(mob);
		}
		if((pstats.getEmail()!=null)&&CMSecurity.isBanned(pstats.getEmail()))
		{
			session.println("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r");
			session.stopSession(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		if(mob.playerStats()!=null)
			mob.playerStats().setLastIP(session.getAddress());
		notifyFriends(mob,"^X"+mob.Name()+" has logged on.^.^?");
		if((CMProps.getVar(CMProps.Str.PKILL).startsWith("ALWAYS"))
		&&(!CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
			mob.setBitmap(mob.getBitmap()|MOB.ATT_PLAYERKILL);
		if((CMProps.getVar(CMProps.Str.PKILL).startsWith("NEVER"))
		&&(CMath.bset(mob.getBitmap(),MOB.ATT_PLAYERKILL)))
			mob.setBitmap(mob.getBitmap()-MOB.ATT_PLAYERKILL);
		List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOGINS);
		if(!CMLib.flags().isCloaked(mob))
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),mob.clans(),mob.Name()+" has logged on.",true);
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
		String roomID=startRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=startRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=startRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=startRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
			List<String> V=mob.fetchFactionRanges();
			for(int v=0;v<V.size();v++)
				if(startRooms.containsKey(V.get(v).toUpperCase()))
				{ roomID=startRooms.get(V.get(v).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=startRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=startRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.length()>0))
			room=CMLib.map().getRoom(roomID);
		if(room==null)
			room=CMLib.map().getRoom("START");
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=CMLib.map().rooms().nextElement();
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
		String roomID=deathRooms.get(race);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=deathRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=deathRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
			List<String> V=mob.fetchFactionRanges();
			for(int v=0;v<V.size();v++)
				if(deathRooms.containsKey(V.get(v).toUpperCase()))
				{ roomID=deathRooms.get(V.get(v).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=deathRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=deathRooms.get("ALL");

		if((roomID!=null)&&(roomID.equalsIgnoreCase("MORGUE")))
			return getDefaultBodyRoom(mob);
		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.getStartRoom();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=CMLib.map().getRoom(roomID);
		if(room==null)
			room=mob.getStartRoom();
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=CMLib.map().rooms().nextElement();
		return room;
	}

	public Room getDefaultBodyRoom(MOB mob)
	{
		Pair<Clan,Integer> clanMorgue=CMLib.clans().findPrivilegedClan(mob, Clan.Function.MORGUE);
		if((clanMorgue!=null)&&((!mob.isMonster())||(mob.getStartRoom()==null)))
		{
			Clan C=clanMorgue.first;
			if(C.getMorgue().length()>0)
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
		String roomID=bodyRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=bodyRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=bodyRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=bodyRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
			List<String> V=mob.fetchFactionRanges();
			for(int v=0;v<V.size();v++)
				if(bodyRooms.containsKey(V.get(v).toUpperCase()))
				{ roomID=bodyRooms.get(V.get(v).toUpperCase()); break;}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=bodyRooms.get(deity);
		if((roomID==null)||(roomID.length()==0))
			roomID=bodyRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.location();
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=CMLib.map().getRoom(roomID);
		if(room==null)
			room=mob.location();
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=CMLib.map().rooms().nextElement();
		return room;
	}

	public int getTrainingCost(MOB mob, int abilityCode, boolean quiet)
	{
		int curStat=mob.baseCharStats().getRacialStat(mob, abilityCode);
		final String list = CMProps.getVar(CMProps.Str.STATCOSTS);
		final int maxStat = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(abilityCode));
		long[][] costs=CMLib.utensils().compileConditionalRange(CMParms.parseCommas(list.trim(),true), 1, 0, maxStat+10);
		int curStatIndex=curStat;
		while((curStatIndex>0)
		&&((curStatIndex>=costs.length)||(costs[curStatIndex]==null)||(costs[curStatIndex].length==0)))
			curStatIndex--;
		int val=1;
		if(curStatIndex>0)
			val=(int)costs[curStatIndex][0];
		if((curStat>=maxStat)&&(!quiet))
		{
			mob.tell("You cannot train that any further.");
			if(val<=0) val=1;
			return -val;
		}
		return val;
	}
	
	public void pageRooms(CMProps page, Map<String, String> table, String start)
	{
		for(Enumeration<Object> i=page.keys();i.hasMoreElements();)
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
		startRooms=new Hashtable<String,String>();
		pageRooms(page,startRooms,"START");
	}

	public void initDeathRooms(CMProps page)
	{
		deathRooms=new Hashtable<String,String>();
		pageRooms(page,deathRooms,"DEATH");
	}

	public void initBodyRooms(CMProps page)
	{
		bodyRooms=new Hashtable<String,String>();
		pageRooms(page,bodyRooms,"MORGUE");
	}

	public boolean shutdown() {
		bodyRooms=new Hashtable<String,String>();
		startRooms=new Hashtable<String,String>();
		deathRooms=new Hashtable<String,String>();
		return true;
	}

}
