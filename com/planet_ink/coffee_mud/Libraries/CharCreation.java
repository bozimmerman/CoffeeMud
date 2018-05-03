package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.exceptions.BadEmailAddressException;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Int;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.AchievementLoadFlag;
import com.planet_ink.coffee_mud.Libraries.interfaces.CharCreationLibrary.LoginSession;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.PlayerAccount.AccountFlag;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Common.interfaces.Session.SessionStatus;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_web.util.CWThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.*;

/*
   Copyright 2005-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CharCreation";
	}

	public Map<String, String>				startRooms			= new Hashtable<String, String>();
	public PairList<CompiledZMask, String>	startRoomMasks		= new PairVector<CompiledZMask, String>();
	public Map<String, String>				deathRooms			= new Hashtable<String, String>();
	public PairList<CompiledZMask, String>	deathRoomMasks		= new PairVector<CompiledZMask, String>();
	public Map<String, String>				bodyRooms			= new Hashtable<String, String>();
	public PairList<CompiledZMask, String>	bodyRoomMasks		= new PairVector<CompiledZMask, String>();
	public Pair<String, Integer>[]			randomNameVowels	= null;
	public Pair<String, Integer>[]			randomNameConsonants= null;

	protected final String RECONFIRMSTR="\n\r^WTry entering ^HY^W or ^HN^W: ";
	
	public final static String[] DEFAULT_BADNAMES = new String[]
	{
		"LIST","DELETE","QUIT","NEW","HERE","YOU","SHIT","FUCK","CUNT",
		"FAGGOT","ASSHOLE","NIGGER","ARSEHOLE","PUSSY", "COCK","SLUT",
		"BITCH","DAMN","CRAP","GOD","JESUS","CHRIST","NOBODY","SOMEBODY",
		"MESSIAH","ADMIN","SYSOP"
	};

	private final CharCreation me = this;

	public enum LoginState
	{
		LOGIN_START,
		LOGIN_AUTOLOGIN,
		LOGIN_NAME,
		LOGIN_ACCTCHAR_PWORD,
		LOGIN_PASS_START,
		LOGIN_NEWACCOUNT_CONFIRM,
		LOGIN_NEWCHAR_CONFIRM,
		LOGIN_PASS_RECEIVED,
		LOGIN_EMAIL_PASSWORD,
		LOGIN_ACCTCONV_CONFIRM,
		ACCTMENU_COMMAND,
		ACCTMENU_PROMPT,
		ACCTMENU_CONFIRMCOMMAND,
		ACCTMENU_ADDTOCOMMAND,
		ACCTMENU_SHOWMENU,
		ACCTMENU_SHOWCHARS,
		ACCTMENU_START,
		ACCTCREATE_START,
		ACCTCREATE_ANSICONFIRM,
		ACCTCREATE_PASSWORDED,
		ACCTCREATE_EMAILSTART,
		ACCTCREATE_EMAILPROMPT,
		ACCTCREATE_EMAILENTERED,
		ACCTCREATE_EMAILCONFIRMED,
		CHARCR_EMAILCONFIRMED,
		CHARCR_EMAILPROMPT,
		CHARCR_EMAILENTERED,
		CHARCR_EMAILSTART,
		CHARCR_EMAILDONE,
		CHARCR_PASSWORDDONE,
		CHARCR_START,
		CHARCR_ANSIDONE,
		CHARCR_ANSICONFIRMED,
		CHARCR_THEMEDONE,
		CHARCR_THEMEPICKED,
		CHARCR_THEMESTART,
		CHARCR_GENDERSTART,
		CHARCR_GENDERDONE,
		CHARCR_RACEDONE,
		CHARCR_RACESTART,
		CHARCR_RACEENTERED,
		CHARCR_RACECONFIRMED,
		CHARCR_STATDONE,
		CHARCR_STATSTART,
		CHARCR_STATCONFIRM,
		CHARCR_STATPICK,
		CHARCR_STATPICKADD,
		CHARCR_CLASSSTART,
		CHARCR_CLASSDONE,
		CHARCR_CLASSPICKED,
		CHARCR_CLASSCONFIRM,
		CHARCR_FACTIONNEXT,
		CHARCR_FACTIONDONE,
		CHARCR_FACTIONPICK,
		CHARCR_FINISH
	}
	
	private class LoginSessionImpl implements LoginSession
	{
		public boolean 		 wizi	   = false;
		public boolean		 reset	   = false;
		public boolean		 skipInput = false;
		public LoginState 	 state	   = LoginState.LOGIN_START;
		public String 		 login	   = null;
		public PlayerAccount acct 	   = null;
		public String 		 lastInput = null;
		public String		 savedInput= null;
		public String 		 password  = null;
		public int			 attempt   = 0;
		public MOB			 mob	   = null;
		public int			 theme	   = -1;
		public int			 statPoints= 0;
		public CharStats	 baseStats = null;
		public int			 index	   = 0;
		public PlayerLibrary.ThinnerPlayer player = null;
		
		@Override
		public String login()
		{
			return login;
		}
		
		@Override
		public boolean reset()
		{
			return reset;
		}
		
		@Override
		public LoginResult loginSystem(Session session) throws IOException
		{
			return me.loginSystem(session, this);
		}
		
		@Override
		public void logoutLoginSession()
		{
			me.logoutLoginSession(this);
		}
		
		@Override
		public boolean skipInputThisTime()
		{
			final boolean doSkip = skipInput;
			this.skipInput = false;
			return doSkip;
		}
		
		@Override
		public String acceptInput(Session session) throws SocketException, IOException
		{
			final boolean wasNull = (lastInput == null) || (session == null);
			lastInput=(session == null) ? null : session.readlineContinue();
			if(wasNull && (lastInput != null))
				lastInput=CMLib.lang().rawInputParser(lastInput);
			return lastInput;
		}
	}

	@Override
	public LoginSession createLoginSession(final Session session)
	{
		LoginSessionImpl loginSession = new LoginSessionImpl();
		loginSession.reset=false;
		return loginSession;
	}
	
	@Override
	public int getTotalBonusStatPoints(PlayerStats playerStats, PlayerAccount account)
	{
		final int basemax = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
		final int basemin = CMProps.getIntVar(CMProps.Int.BASEMINSTAT);

		int points = CMProps.getIntVar(CMProps.Int.MAXSTAT);
		// Make sure there are enough points
		if (points < ((basemin + 1) * CharStats.CODES.BASECODES().length))
			points = (basemin + 1) * CharStats.CODES.BASECODES().length;

		// Make sure there aren't too many points
		if (points > (basemax - 1) * CharStats.CODES.BASECODES().length)
			points = (basemax - 1) * CharStats.CODES.BASECODES().length;

		if(playerStats != null)
			points += playerStats.getBonusCharStatPoints();
		if(account != null)
			points += account.getBonusCharStatPoints();
		
		// Subtract stat minimums from point total to get distributable points
		return points - (basemin * CharStats.CODES.BASECODES().length);
	}

	@Override
	public void reRollStats(CharStats baseCharStats, int pointsLeft)
	{
		final int basemax = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);

		final int[] stats=new int[CharStats.CODES.BASECODES().length];
		for(int i=0;i<stats.length;i++)
			stats[i]=baseCharStats.getStat(CharStats.CODES.BASECODES()[i]);

		while (pointsLeft > 0)
		{
			final int whichNum = CMLib.dice().roll(1,CharStats.CODES.BASECODES().length,-1);
			if(stats[whichNum]<basemax)
			{
				stats[whichNum]++;
				--pointsLeft;
			}
		}

		for(int i=0;i<stats.length;i++)
			baseCharStats.setStat(CharStats.CODES.BASECODES()[i],stats[i]);
	}

	@Override
	public boolean canChangeToThisClass(MOB mob, CharClass thisClass, int theme)
	{
		if(isAvailableCharClass(thisClass)
		&&((theme<0)||(CMath.bset(thisClass.availabilityCode(),theme)))
		&&((mob==null)||(thisClass.qualifiesForThisClass(mob,true))))
			return true;
		return false;
	}
	
	@Override
	public boolean isAvailableCharClass(CharClass C)
	{
		if(C==null)
			return false;
		if((CMProps.isTheme(C.availabilityCode()))
		&&((!CMath.bset(C.availabilityCode(),Area.THEME_SKILLONLYMASK))||(CMSecurity.isCharClassEnabled(C.ID())))
		&&(!CMSecurity.isCharClassDisabled(C.ID())))
			return true;
		return false;
	}

	@Override
	public List<CharClass> classQualifies(MOB mob, int theme)
	{
		mob.recoverCharStats();
		final Vector<CharClass> them=new Vector<CharClass>();
		final HashSet<String> doneClasses=new HashSet<String>();
		for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
		{
			CharClass C=c.nextElement();
			if(doneClasses.contains(C.ID()))
				continue;
			C=CMClass.getCharClass(C.ID());
			doneClasses.add(C.ID());
			if(canChangeToThisClass(mob,C,theme))
				them.addElement(C);
		}
		return them;
	}

	@Override
	public boolean isAvailableRace(Race R)
	{
		if((CMProps.isTheme(R.availabilityCode()))
		&&((!CMath.bset(R.availabilityCode(),Area.THEME_SKILLONLYMASK))||(CMSecurity.isRaceEnabled(R.ID())))
		&&((!CMSecurity.isDisabled(CMSecurity.DisFlag.STDRACES))||(R.isGeneric()))
		&&(!CMSecurity.isRaceDisabled(R.ID())))
			return true;
		return false;
	}
	
	@Override
	public List<Race> raceQualifies(int theme)
	{
		final Vector<Race> qualRaces = new Vector<Race>();
		final HashSet<String> doneRaces=new HashSet<String>();
		for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
		{
			Race R=r.nextElement();
			if(doneRaces.contains(R.ID()))
				continue;
			R=CMClass.getRace(R.ID());
			doneRaces.add(R.ID());
			if(isAvailableRace(R)
			&&(CMath.bset(R.availabilityCode(),theme)))
				qualRaces.add(R);
		}
		return qualRaces;
	}

	@Override
	public boolean isBadName(String login)
	{
		login=login.toUpperCase().trim();
		if(login.equalsIgnoreCase("all"))
			return true;
		for (final String element : DEFAULT_BADNAMES)
		{
			if(CMLib.english().containsString(login, element))
				return true;
		}
		final List<String> V2=CMParms.parseCommas(CMProps.getVar(CMProps.Str.BADNAMES),true);
		for(int v2=0;v2<V2.size();v2++)
		{
			final String str2=V2.get(v2);
			if(str2.length()>0)
			{
				if(CMLib.english().containsString(login, str2))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isOkName(String login, boolean spacesOk)
	{
		if(login.length()>20)
			return false;
		if(login.length()<3)
			return false;

		if((!spacesOk)&&(login.trim().indexOf(' ')>=0))
			return false;

		login=login.toUpperCase().trim();
		final Vector<String> V=CMParms.parse(login);
		for(int v=V.size()-1;v>=0;v--)
		{
			final String str=V.elementAt(v);
			if((" THE A AN ").indexOf(" "+str+" ")>=0)
				V.removeElementAt(v);
		}

		if(isBadName(login))
			return false;

		for(int c=0;c<login.length();c++)
		{
			if((login.charAt(c)!=' ')&&(!Character.isLetter(login.charAt(c))))
				return false;
		}
		
		for(final Enumeration<Deity> d=CMLib.map().deities();d.hasMoreElements();)
		{
			final MOB D=d.nextElement();
			if((CMLib.english().containsString(D.ID(),login))
			||(CMLib.english().containsString(D.Name(),login)))
				return false;
		}
		for(final Enumeration<MOB> m=CMClass.mobTypes();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((CMLib.english().containsString(M.Name(),login))
			||(CMLib.english().containsString(M.name(),login)))
				return false;
		}

		for(final Enumeration<Clan> e=CMLib.clans().clans();e.hasMoreElements();)
		{
			final Clan C=e.nextElement();
			if((CMLib.english().containsString(C.clanID(),login))
			||(CMLib.english().containsString(C.name(),login)))
				return false;
		}

		return !CMSecurity.isBanned(login);
	}

	@Override
	public void reloadTerminal(MOB mob)
	{
		if(mob==null)
			return;

		final Session S=mob.session();
		if(S==null)
			return;

		mob.clearCommandQueue();
		S.initTelnetMode(mob.getAttributesBitmap());
		if((mob.isAttributeSet(MOB.Attrib.MXP))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
		{
			if(S.getClientTelnetMode(Session.TELNET_MXP))
			{
				final StringBuffer mxpText=Resources.getFileResource("text/mxp.txt",true);
				if(mxpText!=null)
					S.rawOut("\033[6z"+mxpText.toString()+"\n\r");
			}
			else
				mob.tell(L("MXP codes have been disabled for this session."));
		}
		else
		if(S.getClientTelnetMode(Session.TELNET_MXP))
		{
			S.rawOut("\033[7z\n\r"); // for mudlet mostly.
			S.changeTelnetMode(Session.TELNET_MXP,false);
			S.setClientTelnetMode(Session.TELNET_MXP,false);
		}

		if((mob.isAttributeSet(MOB.Attrib.SOUND))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP)))
		{
			if(!S.getClientTelnetMode(Session.TELNET_MSP))
				mob.tell(L("MSP sounds have been disabled for this session."));
		}
		else
		if(S.getClientTelnetMode(Session.TELNET_MSP))
		{
			S.changeTelnetMode(Session.TELNET_MSP,false);
			S.setClientTelnetMode(Session.TELNET_MSP,false);
		}
	}

	@Override
	public void showTheNews(MOB mob)
	{
		reloadTerminal(mob);
		Command C=CMClass.getCommand("PollCmd");
		try
		{
			C.execute(mob, null, 0);
		}
		catch (final Exception e)
		{
		}

		if((mob.session()==null)
		||(mob.isMonster())
		||(mob.isAttributeSet(MOB.Attrib.DAILYMESSAGE)))
			return;

		C=CMClass.getCommand("MOTD");
		try
		{
			 C.execute(mob,CMParms.parse("MOTD NEW PAUSE"),0);
		}
		catch(final Exception e)
		{
		}
	}

	@Override
	public List<String> getExpiredAcctOrCharsList()
	{
		final List<String> expired = new ArrayList<String>();
		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
		{
			final long now=System.currentTimeMillis();
			if(CMProps.isUsingAccountSystem())
			{
				for(final Enumeration<PlayerAccount> e = CMLib.players().accounts(null,null); e.hasMoreElements(); )
				{
					final PlayerAccount A=e.nextElement();
					if(A.isSet(PlayerAccount.AccountFlag.NOEXPIRE))
						continue;
					if(now>=A.getAccountExpiration())
						expired.add(A.getAccountName());
				}
			}
			else
			{
				final HashSet<String> skipNames=new HashSet<String>();
				for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
				{
					final MOB M=e.nextElement();
					skipNames.add(M.Name());
					if((CMSecurity.isASysOp(M)||CMSecurity.isAllowedEverywhere(M, CMSecurity.SecFlag.NOEXPIRE)))
						continue;
					if(now>=M.playerStats().getAccountExpiration())
						expired.add(M.Name());
				}
				expired.addAll(CMLib.database().DBExpiredCharNameSearch(skipNames));
			}
		}
		return expired;
	}

	public boolean isExpired(PlayerAccount acct, Session session, MOB mob)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
			return false;
		if((acct!=null)&&(acct.isSet(PlayerAccount.AccountFlag.NOEXPIRE)))
			return false;
		long expiration;
		if(mob!=null)
		{
			if((CMSecurity.isASysOp(mob)||CMSecurity.isAllowedEverywhere(mob, CMSecurity.SecFlag.NOEXPIRE)))
				return false;
			expiration = mob.playerStats().getAccountExpiration();
		}
		else
		if(acct != null)
		{
			expiration = acct.getAccountExpiration();
		}
		else
			return false;
		if(expiration<=System.currentTimeMillis())
		{
			session.println("\n\r"+CMProps.getVar(CMProps.Str.EXPCONTACTLINE)+"\n\r\n\r");
			return true;
		}
		return false;
	}

	private void executeScript(MOB mob, List<String> scripts)
	{
		if(scripts==null)
			return;
		for(int s=0;s<scripts.size();s++)
		{
			final String script=scripts.get(s);
			final ScriptingEngine S=(ScriptingEngine)CMClass.getCommon("DefaultScriptingEngine");
			S.setSavable(false);
			S.setVarScope("*");
			S.setScript(script);
			final Room oldRoom=mob.location();
			mob.setLocation(CMLib.map().getRandomRoom());
			final CMMsg msg2=CMClass.getMsg(mob,mob,null,CMMsg.MSG_OK_VISUAL,null,null,L("CHARCREATION"));
			S.executeMsg(mob, msg2);
			S.dequeResponses();
			S.tick(mob,Tickable.TICKID_MOB);
			mob.setLocation(oldRoom);
		}
	}

	private Map<String,List<String>> getLoginScripts()
	{
		final Hashtable<String,List<String>> extraScripts=new Hashtable<String,List<String>>();
		final String[] VALID_SCRIPT_CODES={"PASSWORD","EMAIL","ANSI","THEME","RACE","GENDER","STATS","CLASS","FACTIONS","END"};
		final List<String> extras=CMParms.parseCommas(CMProps.getVar(CMProps.Str.CHARCREATIONSCRIPTS),true);
		for(int e=0;e<extras.size();e++)
		{
			String s=extras.get(e);
			final int x=s.indexOf(':');
			String code="END";
			if(x>0)
			{
				code=s.substring(0,x).toUpperCase().trim();
				boolean found=false;
				for (final String element : VALID_SCRIPT_CODES)
				{
					if(element.equals(code))
					{ 
						code=element; 
						found=true; 
						break;
					}
					else
					if(element.startsWith(code))
					{ 
						code=element; 
						found=true; 
						break;
					}
				}
				if(!found)
				{
					Log.errOut("CharCreation","Error in CHARCREATIONSCRIPTS, invalid code: "+code);
					continue;
				}
				s=s.substring(x+1);
			}
			List<String> V=extraScripts.get(code);
			if(V==null)
			{ 
				V=new Vector<String>(); 
				extraScripts.put(code,V);
			}
			V.add(s.trim());
		}
		return extraScripts;
	}

	protected void finishCreateAccount(final LoginSessionImpl loginObj, final PlayerAccount acct, final String login, final String pw, final String emailAddy, final Session session)
	{
		acct.setAccountName(CMStrings.capitalizeAndLower(login.trim()));
		acct.setEmail(emailAddy);
		acct.setLastIP(session.getAddress());
		acct.setLastDateTime(System.currentTimeMillis());
		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
			acct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*(CMProps.getIntVar(CMProps.Int.TRIALDAYS))));

		if(((pw==null)||(pw.length()==0))&&(!CMProps.getVar(CMProps.Str.EMAILREQ).startsWith("DISABLE")))
		{
			final String password=CMLib.encoder().generateRandomPassword();
			acct.setPassword(password);
			CMLib.database().DBCreateAccount(acct);
			CMLib.players().addAccount(acct);
			CMLib.smtp().emailOrJournal(CMProps.getVar(CMProps.Str.SMTPSERVERNAME), acct.getAccountName(), 
				"noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(), acct.getAccountName(),
				L("Password for @x1",acct.getAccountName()),
				L("Your password for @x1 is: @x2\n\r"
				+ "You can login by pointing your mud client at @x3 port(s): @x4.\n\r"
				+ "After creating a character, you may use the PASSWORD command to change it once you are online.",
				acct.getAccountName(),password,CMProps.getVar(CMProps.Str.MUDDOMAIN),CMProps.getVar(CMProps.Str.MUDPORTS)));
			session.println(L("Your account has been created.  You will receive an email with your password shortly."));
			CMLib.s_sleep(2000);
			session.stopSession(false,false,false);
			Log.sysOut("Created account: "+acct.getAccountName());
			session.setAccount(null);
			loginObj.state=LoginState.LOGIN_START;
			loginObj.reset=true;
			return;
		}
		else
		{
			acct.setPassword(pw);
			CMLib.database().DBCreateAccount(acct);
			CMLib.players().addAccount(acct);
			StringBuffer doneText=new CMFile(Resources.buildResourcePath("text")+"doneacct.txt",null,CMFile.FLAG_LOGERRORS).text();
			try
			{
				doneText = CMLib.webMacroFilter().virtualPageFilter(doneText);
			}
			catch (final Exception ex)
			{
			}
			session.println(null,null,null,"\n\r\n\r"+doneText.toString());
		}
		session.setAccount(acct);
		Log.sysOut("Created account: "+acct.getAccountName());
		if(CMLib.players().playerExists(acct.getAccountName()))
		{
			loginObj.lastInput="IMPORT "+acct.getAccountName();
			loginObj.state=LoginState.ACCTMENU_COMMAND;
		}
		else
			loginObj.state=LoginState.ACCTMENU_START;
	}

	protected String buildQualifyingClassList(MOB mob, List<CharClass> classes, String finalConnector)
	{
		final StringBuilder list = new StringBuilder("");
		int highestAttribute=-1;
		for(final int attrib : CharStats.CODES.BASECODES())
		{
			if((highestAttribute<0)
			||(mob.baseCharStats().getStat(attrib)>mob.baseCharStats().getStat(highestAttribute)))
				highestAttribute=attrib;
		}
		for(final Iterator<CharClass> i=classes.iterator(); i.hasNext(); )
		{
			final CharClass C = i.next();
			final String color=(C.getAttackAttribute()==highestAttribute)?"^H":"^w";
			if(!i.hasNext())
			{
				if (list.length()>0)
				{
					list.append("^N"+finalConnector+" "+color);
				}
				list.append(C.name());
			}
			else
			{
				list.append(color+C.name()+"^N, ");
			}
		}
		return list.toString();
	}

	protected void getUniversalStartingItems(int theme, MOB mob)
	{
		final String raceID = mob.baseCharStats().getMyRace().ID();
		final Map<String,List<Item>> itemSets = new HashMap<String,List<Item>>();
		List<Item> itemPartsV = new ArrayList<Item>();
		itemSets.put("",itemPartsV);
		for(String item : CMParms.parseCommas(CMProps.getVar(CMProps.Str.STARTINGITEMS), true))
		{
			item=item.trim();
			final Race R1=CMClass.getRace(item);
			if(R1 != null)
			{
				itemPartsV = new ArrayList<Item>();
				itemSets.put(R1.ID(), itemPartsV);
			}
			else
			{
				int num=1;
				final int x = item.indexOf(' ');
				if((x>0)&&(CMath.isInteger(item.substring(0,x).trim())))
				{
					num=CMath.s_int(item.substring(0,x).trim());
					item=item.substring(x+1);
				}
				for(int i=0;i<num;i++)
				{
					Item I=CMClass.getBasicItem(item);
					if(I==null)
						I=CMClass.getItem(item);
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
						Log.errOut("CharCreation","Unable to give new STARTINGITEM '"+item+"'");
					else
						itemPartsV.add(I);
				}
			}
		}
		final List<Item> myItemPartsV = itemSets.containsKey(raceID) ? itemSets.remove(raceID) : itemSets.remove("");
		if(myItemPartsV != null)
		{
			for(Item item : myItemPartsV)
				mob.addItem(item);
		}
		for(final List<Item> l : itemSets.values())
		{
			for(Item I : l)
				I.destroy();
		}
	}

	private boolean loginsDisabled(MOB mob)
	{
		if((CMSecurity.isDisabled(CMSecurity.DisFlag.LOGINS))
		&&(!CMSecurity.isASysOp(mob))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, mob.Name()))
		&&(!((mob.playerStats()!=null)
			&&(mob.playerStats().getAccount()!=null)
			&&(CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, mob.playerStats().getAccount().getAccountName()))))
		&&(!((mob.session()!=null)&&(CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, mob.session().getAddress())))))
		{
			StringBuffer rejectText=Resources.getFileResource("text/nologins.txt",true);
			try
			{
				rejectText = CMLib.webMacroFilter().virtualPageFilter(rejectText);
			}
			catch (final Exception ex)
			{
			}
			if((rejectText!=null)&&(rejectText.length()>0))
				mob.session().println(rejectText.toString());
			CMLib.s_sleep(1000);
			mob.session().stopSession(false,false,false);
			return true;
		}
		return false;
	}

	public LoginResult prelimChecks(Session session, MOB pickedMOB, String login, String email)
	{
		if(CMSecurity.isBanned(login))
		{
			session.println(L("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r"));
			session.stopSession(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		if((email!=null)&&CMSecurity.isBanned(email))
		{
			session.println(L("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r"));
			session.stopSession(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		for(final Session S : CMLib.sessions().allIterable())
		{
			final MOB M=S.mob();
			if((M!=null)
			&&(S!=session)
			&&(M==pickedMOB))
			{
				final Room oldRoom=M.location();
				if(oldRoom!=null)
				{
					while(oldRoom.isInhabitant(M))
						oldRoom.delInhabitant(M);
				}
				session.setMob(M);
				M.setSession(session);
				S.setMob(null);
				S.stopSession(false,false,false);
				Log.sysOut("Session swap for "+session.mob().Name()+".");
				reloadTerminal(session.mob());
				session.mob().bringToLife(oldRoom,false);
				return LoginResult.NORMAL_LOGIN;
			}
		}
		return null;
	}

	@Override
	public void notifyFriends(MOB mob, String message)
	{
		try
		{
			for(final Session S : CMLib.sessions().localOnlineIterable())
			{
				final MOB listenerM=S.mob();
				if((listenerM!=null)
				&&(listenerM!=mob)
				&&((!CMLib.flags().isCloaked(mob))||(CMSecurity.isASysOp(listenerM)))
				&&(listenerM.isAttributeSet(MOB.Attrib.AUTONOTIFY)))
				{
					final PlayerStats listenerPStats=listenerM.playerStats();
					if((listenerPStats!=null)
					&&((listenerPStats.getFriends().contains(mob.Name())||listenerPStats.getFriends().contains("All"))))
						listenerM.tell(message);
				}
			}
		}
		catch(final Exception e)
		{
		}
	}

	private String getMSSPPacket()
	{
		final StringBuffer rpt = new StringBuffer("\r\nMSSP-REPLY-START");
		rpt.append("\r\n").append("PLAYERS");
		rpt.append("\t").append(Integer.toString(CMLib.sessions().getCountLocalOnline()));
		rpt.append("\r\n").append("STATUS");
		rpt.append("\t");
		switch(CMProps.getIntVar(CMProps.Int.MUDSTATE))
		{
		case 0:
			rpt.append("Alpha");
			break;
		case 1:
			rpt.append("Closed Beta");
			break;
		case 2:
			rpt.append("Open Beta");
			break;
		case 3:
			rpt.append("Live");
			break;
		default:
			rpt.append("Live");
			break;
		}

		MudHost host = null;
		if(CMLib.hosts().size()>0)
			host = CMLib.hosts().get(0);
		if(host != null)
		{
			rpt.append("\r\n").append("UPTIME");
			rpt.append("\t").append(Long.toString(host.getUptimeSecs()));
			rpt.append("\r\n").append("HOSTNAME");
			rpt.append("\t").append(host.getHost());
			rpt.append("\r\n").append("PORT");
			rpt.append("\t").append(Integer.toString(host.getPort()));
			if(Thread.currentThread() instanceof CWThread)
			{
				final String webServerPort=Integer.toString(((CWThread)Thread.currentThread()).getConfig().getHttpListenPorts()[0]);
				rpt.append("\r\n").append("WEBSITE");
				rpt.append("\t").append(("http://"+host.getHost()+":"+webServerPort));
			}
			rpt.append("\r\n").append("LANGUAGE");
			rpt.append("\t").append(host.getLanguage());
		}
		if(CMLib.intermud().i3online())
		{
			rpt.append("\r\n").append("INTERMUD");
			rpt.append("\t").append("I3");
		}
		if(CMLib.intermud().imc2online())
		{
			rpt.append("\r\n").append("INTERMUD");
			rpt.append("\t").append("IMC2");
		}
		rpt.append("\r\n").append("FAMILY");
		rpt.append("\t").append("CoffeeMUD");
		rpt.append("\r\n").append("EMAIL");
		rpt.append("\t").append(CMProps.getVar(CMProps.Str.ADMINEMAIL));
		rpt.append("\r\n").append("CODEBASE");
		rpt.append("\t").append(("CoffeeMud v"+CMProps.getVar(CMProps.Str.MUDVER)));
		rpt.append("\r\n").append("AREAS");
		rpt.append("\t").append(Integer.toString(CMLib.map().numAreas()));
		rpt.append("\r\n").append("HELPFILES");
		rpt.append("\t").append(Integer.toString(CMLib.help().getHelpFile().size()));
		rpt.append("\r\n").append("MOBILES");
		rpt.append("\t").append(Long.toString(CMClass.numPrototypes(CMClass.CMObjectType.MOB)));
		rpt.append("\r\n").append("OBJECTS");
		rpt.append("\t").append(Long.toString(CMClass.numPrototypes(CMClass.OBJECTS_ITEMTYPES)));
		rpt.append("\r\n").append("ROOMS");
		rpt.append("\t").append(Long.toString(CMLib.map().numRooms()));
		rpt.append("\r\n").append("CLASSES");
		int numClasses = 0;
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
			numClasses=CMLib.login().classQualifies(null, CMProps.getIntVar(CMProps.Int.MUDTHEME)&0x07).size();
		rpt.append("\t").append(Long.toString(numClasses));
		rpt.append("\r\n").append("RACES");
		int numRaces = 0;
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
			numRaces=CMLib.login().raceQualifies(CMProps.getIntVar(CMProps.Int.MUDTHEME)&0x07).size();
		rpt.append("\t").append(Long.toString(numRaces));
		rpt.append("\r\n").append("SKILLS");
		rpt.append("\t").append(Long.toString(CMLib.ableMapper().numMappedAbilities()));
		rpt.append("\r\n").append("ANSI");
		rpt.append("\t").append("1");
		rpt.append("\r\n").append("MCCP");
		rpt.append("\t").append((!CMSecurity.isDisabled(CMSecurity.DisFlag.MCCP)?"1":"0"));
		rpt.append("\r\n").append("MSP");
		rpt.append("\t").append((!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP)?"1":"0"));
		rpt.append("\r\n").append("MXP");
		rpt.append("\t").append((!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)?"1":"0"));
		rpt.append("\r\nMSSP-REPLY-END\r\n");
		return rpt.toString();
	}

	protected LoginResult loginSubsystem(final LoginSessionImpl loginObj, final Session session) throws IOException
	{
		switch(loginObj.state)
		{
		case LOGIN_START:
			return loginStart(loginObj, session);
		case LOGIN_NAME:
			return loginName(loginObj, session);
		case LOGIN_ACCTCHAR_PWORD:
			return loginAcctcharPword(loginObj, session);
		case LOGIN_ACCTCONV_CONFIRM:
			return loginAcctconvConfirm(loginObj, session);
		case ACCTCREATE_START:
			return acctcreateStart(loginObj, session);
		case ACCTCREATE_ANSICONFIRM:
			return acctcreateANSIConfirm(loginObj, session);
		case ACCTCREATE_EMAILSTART:
			return acctcreateEmailStart(loginObj, session);
		case ACCTCREATE_EMAILPROMPT:
			return acctcreateEmailPrompt(loginObj, session);
		case ACCTCREATE_EMAILENTERED:
			return acctcreateEmailEntered(loginObj, session);
		case ACCTCREATE_EMAILCONFIRMED:
			return acctcreateEmailConfirmed(loginObj, session);
		case ACCTCREATE_PASSWORDED:
			return acctcreatePassworded(loginObj, session);
		case LOGIN_PASS_START:
			return loginPassStart(loginObj, session);
		case LOGIN_NEWACCOUNT_CONFIRM:
			return loginNewaccountConfirm(loginObj, session);
		case LOGIN_NEWCHAR_CONFIRM:
			return loginNewcharConfirm(loginObj, session);
		case LOGIN_PASS_RECEIVED:
			return loginPassReceived(loginObj, session);
		case LOGIN_EMAIL_PASSWORD:
			return loginEmailPassword(loginObj, session);
		case ACCTMENU_START:
			return acctmenuStart(loginObj, session);
		case ACCTMENU_SHOWCHARS:
			return acctmenuShowChars(loginObj, session);
		case ACCTMENU_SHOWMENU:
			return acctmenuShowMenu(loginObj, session);
		case ACCTMENU_PROMPT:
			return acctmenuPrompt(loginObj, session);
		case ACCTMENU_CONFIRMCOMMAND:
			return acctmenuConfirmCommand(loginObj, session);
		case ACCTMENU_ADDTOCOMMAND:
			return acctmenuAddToCommand(loginObj, session);
		case ACCTMENU_COMMAND:
			return acctmenuCommand(loginObj, session);
		case CHARCR_START:
			return charcrStart(loginObj, session);
		case CHARCR_PASSWORDDONE:
			return charcrPasswordDone(loginObj, session);
		case CHARCR_EMAILSTART:
			return charcrEmailStart(loginObj, session);
		case CHARCR_EMAILPROMPT:
			return charcrEmailPrompt(loginObj, session);
		case CHARCR_EMAILENTERED:
			return charcrEmailEntered(loginObj, session);
		case CHARCR_EMAILCONFIRMED:
			return charcrEmailConfirmed(loginObj, session);
		case CHARCR_EMAILDONE:
			return charcrEmailDone(loginObj, session);
		case CHARCR_ANSICONFIRMED:
			return charcrANSIConfirmed(loginObj, session);
		case CHARCR_ANSIDONE:
			return charcrANSIDone(loginObj, session);
		case CHARCR_THEMESTART:
			return charcrThemeStart(loginObj, session);
		case CHARCR_THEMEPICKED:
			return charcrThemePicked(loginObj, session);
		case CHARCR_THEMEDONE:
			return charcrThemeDone(loginObj, session);
		case CHARCR_RACESTART:
			return charcrRaceStart(loginObj, session);
		case CHARCR_RACEENTERED:
			return charcrRaceReEntered(loginObj, session);
		case CHARCR_RACECONFIRMED:
			return charcrRaceConfirmed(loginObj, session);
		case CHARCR_RACEDONE:
			return charcrRaceDone(loginObj, session);
		case CHARCR_GENDERSTART:
			return charcrGenderStart(loginObj, session);
		case CHARCR_GENDERDONE:
			return charcrGenderDone(loginObj, session);
		case CHARCR_STATSTART:
			return charcrStatStart(loginObj, session);
		case CHARCR_STATCONFIRM:
			return charcrStatConfirm(loginObj, session);
		case CHARCR_STATPICKADD:
			return charcrStatPickAdd(loginObj, session);
		case CHARCR_STATPICK:
			return charcrStatPick(loginObj, session);
		case CHARCR_STATDONE:
			return charcrStatDone(loginObj, session);
		case CHARCR_CLASSSTART:
			return charcrClassStart(loginObj, session);
		case CHARCR_CLASSPICKED:
			return charcrClassPicked(loginObj, session);
		case CHARCR_CLASSCONFIRM:
			return charcrClassConfirm(loginObj, session);
		case CHARCR_CLASSDONE:
			return charcrClassDone(loginObj, session);
		case CHARCR_FACTIONNEXT:
			return charcrFactionNext(loginObj, session);
		case CHARCR_FACTIONPICK:
			return charcrFactionPick(loginObj, session);
		case CHARCR_FACTIONDONE:
			return charcrFactionDone(loginObj, session);
		case CHARCR_FINISH:
			return charcrFinish(loginObj, session);
		default:
			loginObj.state=LoginState.LOGIN_START;
			return null;
		}
	}

	protected LoginResult loginStart(final LoginSessionImpl loginObj, final Session session)
	{
		loginObj.wizi=false;
		session.setAccount(null);
		loginObj.attempt++;
		if(loginObj.attempt>=4)
		{
			session.stopSession(false,false,false);
			loginObj.reset=true;
			loginObj.state=LoginState.LOGIN_START;
			return LoginResult.NO_LOGIN;
		}
		
		// an input eating pause for stray characters (esp from WINTIN)
		final long startTime = System.currentTimeMillis() + 500;
		while(System.currentTimeMillis() < startTime)
		{
			try
			{
				session.readlineContinue();
			}
			catch(Exception x)
			{
				return LoginResult.NO_LOGIN;
			}
		}
			
		if(CMProps.isUsingAccountSystem())
			session.promptPrint(L("\n\raccount name: "));
		else
			session.promptPrint(L("\n\rname: "));
		loginObj.state=LoginState.LOGIN_NAME;
		session.setStatus(Session.SessionStatus.LOGIN);
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult loginName(final LoginSessionImpl loginObj, final Session session)
	{
		loginObj.login=loginObj.lastInput;
		if(loginObj.login==null)
		{
			loginObj.state=LoginState.LOGIN_START;
			return null;
		}
		// this loop is for malformed ansi responses, it strips the trailing letters
		for(int i=loginObj.login.length()-2;i>=0;i--)
		{
			if(!Character.isLetterOrDigit(loginObj.login.charAt(i)))
			{
				loginObj.login=loginObj.login.substring(i+1);
				break;
			}
		}
		loginObj.login=loginObj.login.trim();
		if(loginObj.login.length()==0)
		{
			session.println("^w ^N");
			session.println(L("^w* Enter an existing name to log in.^N"));
			session.println(L("^w* Enter a new name to create @x1.^N",((CMProps.isUsingAccountSystem())?"an account":" a character")));
			session.println(L("^w* Enter '*' to generate a random @x1 name.^N",((CMProps.isUsingAccountSystem())?"account":"character")));
			loginObj.state=LoginState.LOGIN_START;
			if((Math.random()>0.5)&&(loginObj.attempt>0))
				loginObj.attempt--;
			return null;
		}
		if(loginObj.login.equalsIgnoreCase("MSSP-REQUEST")&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSSP)))
		{
			session.rawOut(getMSSPPacket());
			session.stopSession(false,false,false);
			loginObj.reset=true;
			loginObj.state=LoginState.LOGIN_START;
			return LoginResult.NO_LOGIN;
		}
		if(loginObj.login.indexOf('*')>=0)
		{
			loginObj.login = generateRandomName(3, 6);
			while((!isOkName(loginObj.login,false)) || (CMLib.players().playerExists(loginObj.login)) || (CMLib.players().accountExists(loginObj.login)))
				loginObj.login = generateRandomName(3, 8);
		}
		if(loginObj.login.endsWith(" !"))
		{
			loginObj.login=loginObj.login.substring(0,loginObj.login.length()-2);
			loginObj.login=loginObj.login.trim();
			loginObj.wizi=true;
		}
		loginObj.login = CMStrings.capitalizeAndLower(loginObj.login);
		if(CMProps.isUsingAccountSystem())
		{
			loginObj.acct = CMLib.players().getLoadAccount(loginObj.login);
			if(loginObj.acct!=null)
			{
				loginObj.player=new PlayerLibrary.ThinnerPlayer();
				loginObj.player.name=loginObj.acct.getAccountName();
				loginObj.player.accountName=loginObj.acct.getAccountName();
				loginObj.player.email=loginObj.acct.getEmail();
				loginObj.player.expiration=loginObj.acct.getAccountExpiration();
				loginObj.player.password=loginObj.acct.getPasswordStr();
			}
			else
			{
				loginObj.player=CMLib.database().DBUserSearch(loginObj.login);
				if(loginObj.player != null)
				{
					session.promptPrint(L("password for @x1: ",loginObj.player.name));
					loginObj.state=LoginState.LOGIN_ACCTCHAR_PWORD;
					return LoginResult.INPUT_REQUIRED;
				}
				else
				{
					session.println(L("\n\rAccount '@x1' does not exist.",CMStrings.capitalizeAndLower(loginObj.login)));
					loginObj.player=null;
				}
			}
		}
		else
		{
			final MOB mob=CMLib.players().getPlayer(loginObj.login);
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
		loginObj.state=LoginState.LOGIN_PASS_START;
		return null;
	}

	protected LoginResult loginAcctcharPword(final LoginSessionImpl loginObj, final Session session)
	{
		loginObj.password=loginObj.lastInput;
		if(loginObj.player.matchesPassword(loginObj.password))
		{
			if((loginObj.player.accountName==null)||(loginObj.player.accountName.trim().length()==0))
			{
				session.println(L("\n\rThis mud is now using an account system.  Please create a new account "
								+ "and use the IMPORT command to add your character(s) to your account."));
				session.promptPrint(L("Would you like to create your new master account and call it '@x1' (y/N)? ",loginObj.player.name));
				loginObj.state=LoginState.LOGIN_ACCTCONV_CONFIRM;
				return LoginResult.INPUT_REQUIRED;
			}
			else
			{
				session.println(L("\n\rThis mud uses an account system.  Your account name is `^H@x1^N`.\n\r"
								+ "Please use this account name when logging in.",loginObj.player.accountName));
			}
		}
		loginObj.state=LoginState.LOGIN_START;
		return null;
	}

	protected LoginResult loginAcctconvConfirm(final LoginSessionImpl loginObj, final Session session)
	{
		final String input=loginObj.lastInput.toUpperCase().trim();
		if(input.startsWith("Y"))
		{
			loginObj.acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
			loginObj.state=LoginState.ACCTCREATE_START;
			return null;
		}
		else
		if((input.length()>0)&&(!input.startsWith("N")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		loginObj.state=LoginState.LOGIN_START;
		loginObj.reset=true;
		return LoginResult.NO_LOGIN;
	}

	protected LoginResult acctcreateStart(final LoginSessionImpl loginObj, final Session session)
	{
		Log.sysOut("Creating account: "+loginObj.login);
		loginObj.state=LoginState.ACCTCREATE_ANSICONFIRM;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.ANSIPROMPT))
		{
			loginObj.lastInput = "Y";
			return acctcreateANSIConfirm(loginObj, session);
		}
		else
		{
			session.promptPrint(L("\n\rDo you want ANSI colors (Y/n)?"));
			return LoginResult.INPUT_REQUIRED;
		}
	}

	protected LoginResult acctcreateANSIConfirm(final LoginSessionImpl loginObj, final Session session)
	{
		final PlayerAccount acct=loginObj.acct;
		final String input=loginObj.lastInput.toUpperCase().trim();
		if(input.startsWith("N"))
		{
			acct.setFlag(PlayerAccount.AccountFlag.ANSI, false);
			session.setServerTelnetMode(Session.TELNET_ANSI,false);
			session.setClientTelnetMode(Session.TELNET_ANSI,false);
		}
		else
		if((input.length()>0)&&(!input.startsWith("Y")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		else
		{
			acct.setFlag(PlayerAccount.AccountFlag.ANSI, true);
		}
		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newacct.txt",null,CMFile.FLAG_LOGERRORS).text();
		try
		{
			introText = CMLib.webMacroFilter().virtualPageFilter(introText);
		}
		catch (final Exception ex)
		{
		}
		session.println(null,null,null,"\n\r\n\r"+introText.toString());
		final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
		if(!emailPassword)
		{
			session.promptPrint(L("\n\rEnter an account password\n\r: "));
			loginObj.state=LoginState.ACCTCREATE_PASSWORDED;
			return LoginResult.INPUT_REQUIRED;
		}
		loginObj.password=null;
		loginObj.state=LoginState.ACCTCREATE_EMAILSTART;
		return null;
	}

	protected LoginResult acctcreateEmailStart(final LoginSessionImpl loginObj, final Session session)
	{
		if(CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE"))
		{
			finishCreateAccount(loginObj, loginObj.acct, loginObj.login, loginObj.password, "", session);
			return null;
		}
		StringBuffer emailIntro=new CMFile(Resources.buildResourcePath("text")+"email.txt",null,CMFile.FLAG_LOGERRORS).text();
		try
		{
			 emailIntro = CMLib.webMacroFilter().virtualPageFilter(emailIntro);
		}
		catch(final Exception ex)
		{
		}
		session.println(null,null,null,emailIntro.toString());
		loginObj.state=LoginState.ACCTCREATE_EMAILPROMPT;
		return null;
	}

	protected LoginResult acctcreateEmailPrompt(final LoginSessionImpl loginObj, final Session session)
	{
		session.promptPrint(L("\n\rEnter your e-mail address: "));
		loginObj.state=LoginState.ACCTCREATE_EMAILENTERED;
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult acctcreateEmailEntered(final LoginSessionImpl loginObj, final Session session)
	{
		final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
		final boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
		final String newEmail=loginObj.lastInput;
		if((emailReq||emailPassword)
		&& ((newEmail==null)||(newEmail.trim().length()==0)||(!CMLib.smtp().isValidEmailAddress(newEmail))))
		{
			session.println(L("\n\rA valid email address is required.\n\r"));
			loginObj.state=LoginState.ACCTCREATE_EMAILPROMPT;
			return null;
		}
		loginObj.savedInput=newEmail;
		if(emailPassword)
			session.println(L("This email address will be used to send you a password."));
		if(emailReq||emailPassword)
		{
			session.promptPrint(L("Confirm that '@x1' is correct by re-entering.\n\rRe-enter: ",newEmail));
			loginObj.state=LoginState.ACCTCREATE_EMAILCONFIRMED;
			return LoginResult.INPUT_REQUIRED;
		}
		loginObj.state=LoginState.ACCTCREATE_EMAILCONFIRMED;
		return null;
	}

	protected LoginResult acctcreateEmailConfirmed(final LoginSessionImpl loginObj, final Session session)
	{
		final boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
		final String newEmail=loginObj.savedInput;
		boolean emailConfirmed=false;
		if((newEmail.length()>0)&&(newEmail.equalsIgnoreCase(loginObj.lastInput)))
			emailConfirmed=CMLib.smtp().isValidEmailAddress(newEmail);
		loginObj.acct.setEmail("");
		if(emailConfirmed||((!emailReq)&&(newEmail.trim().length()==0)))
		{
			finishCreateAccount(loginObj, loginObj.acct, loginObj.login, loginObj.password, newEmail, session);
			return null;
		}
		session.println(L("\n\rThat email address combination was invalid.\n\r"));
		loginObj.state=LoginState.ACCTCREATE_EMAILPROMPT;
		return null;
	}

	protected LoginResult acctcreatePassworded(final LoginSessionImpl loginObj, final Session session)
	{
		final String password=loginObj.lastInput;
		if(password.length()==0)
		{
			session.println(L("\n\rAborting account creation."));
			loginObj.state=LoginState.LOGIN_START;
		}
		else
		{
			loginObj.password=loginObj.lastInput;
			loginObj.state=LoginState.ACCTCREATE_EMAILSTART;
		}
		return null;
	}

	protected LoginResult loginPassStart(final LoginSessionImpl loginObj, final Session session)
	{
		if(loginObj.player==null)
		{
			if(CMProps.isUsingAccountSystem())
			{
				if(newAccountsAllowed(loginObj.login,session,loginObj.acct))
				{
					session.promptPrint(L("\n\r'@x1' does not exist.\n\rIs this a new account you would like to create (y/N)?",CMStrings.capitalizeAndLower(loginObj.login)));
					loginObj.state=LoginState.LOGIN_NEWACCOUNT_CONFIRM;
					return LoginResult.INPUT_REQUIRED;
				}
			}
			else
			if(newCharactersAllowed(loginObj.login,session,loginObj.acct,false))
			{
				session.promptPrint(L("\n\r'@x1' does not exist.\n\rIs this a new character you would like to create (y/N)?",CMStrings.capitalizeAndLower(loginObj.login)));
				loginObj.state=LoginState.LOGIN_NEWCHAR_CONFIRM;
				return LoginResult.INPUT_REQUIRED;
			}
			loginObj.state=LoginState.LOGIN_START;
			return null;
		}
		else
		{
			final String otherLoginName = loginObj.login;
			for(final Session otherSess : CMLib.sessions().allIterable())
			{
				if((otherSess!=session)&&(otherSess.isPendingLogin(otherLoginName)))
				{
					session.println(L("A previous login is still pending.  Please be patient."));
					session.stopSession(false, false, false);
					loginObj.reset=true;
					loginObj.state=LoginState.LOGIN_START;
					return LoginResult.NO_LOGIN;
				}
			}
			session.promptPrint(L("password: "));
			loginObj.state=LoginState.LOGIN_PASS_RECEIVED;
			return LoginResult.INPUT_REQUIRED;
		}
	}

	protected LoginResult loginNewcharConfirm(final LoginSessionImpl loginObj, final Session session)
	{
		final String input=loginObj.lastInput.trim().toUpperCase();
		if(input.startsWith("Y"))
		{
			loginObj.state=LoginState.CHARCR_START;
			return null;
		}
		else
		if((input.length()>0)&&(!input.startsWith("N")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		loginObj.state=LoginState.LOGIN_START;
		return null;
	}

	protected LoginResult loginNewaccountConfirm(final LoginSessionImpl loginObj, final Session session)
	{
		final LoginResult result=LoginResult.NO_LOGIN;
		final String input=loginObj.lastInput.trim().toUpperCase();
		if(input.startsWith("Y"))
		{
			loginObj.acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
			loginObj.state=LoginState.ACCTCREATE_START;
			return null;
		}
		else
		if((input.length()>0)&&(!input.startsWith("N")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		if(result==LoginResult.NO_LOGIN)
		{
			loginObj.state=LoginState.LOGIN_START;
			return null;
		}
		return result;
	}

	protected LoginResult loginPassReceived(final LoginSessionImpl loginObj, final Session session) throws IOException
	{
		loginObj.password=loginObj.lastInput;
		if(loginObj.player.matchesPassword(loginObj.password))
		{
			final LoginResult prelimResults = prelimChecks(session,loginObj.mob,loginObj.login,loginObj.player.email);
			if(prelimResults!=null)
			{
				if(prelimResults==LoginResult.NO_LOGIN)
				{
					loginObj.state=LoginState.LOGIN_START;
					return null;
				}
				return prelimResults;
			}

			if(loginObj.acct!=null)
			{
				//if(isExpired(loginObj.acct,session,loginObj.player.expiration))  return LoginResult.NO_LOGIN; // this blocks archons!
				session.setAccount(loginObj.acct);
				final StringBuilder loginMsg=new StringBuilder("");
				loginMsg.append(session.getAddress()).append(" "+session.getTerminalType())
						.append(session.getClientTelnetMode(Session.TELNET_MXP)?" MXP":"")
						.append(session.getClientTelnetMode(Session.TELNET_MSDP)?" MSDP":"")
						.append(session.getClientTelnetMode(Session.TELNET_ATCP)?" ATCP":"")
						.append(session.getClientTelnetMode(Session.TELNET_GMCP)?" GMCP":"")
						.append((session.getClientTelnetMode(Session.TELNET_COMPRESS)||session.getClientTelnetMode(Session.TELNET_COMPRESS2))?" CMP":"")
						.append(session.getClientTelnetMode(Session.TELNET_ANSI)?" ANSI":"")
						.append(", account login: "+loginObj.acct.getAccountName());
				Log.sysOut(loginMsg.toString());
				//session.setStatus(SessionStatus.ACCOUNTMENU);
				loginObj.state=LoginState.ACCTMENU_START;
				return null;
			}
			else
			{
				final LoginResult completeResult=completeCharacterLogin(session,loginObj.login, loginObj.wizi);
				if(completeResult == LoginResult.NO_LOGIN)
				{
					loginObj.state=LoginState.LOGIN_START;
					return null;
				}
			}
		}
		else
		{
			Log.sysOut("Failed login: "+loginObj.player.name);
			session.println(L("\n\rInvalid password.\n\r"));
			if((!session.isStopped())
			&&(loginObj.player.email.length()>0)
			&&(loginObj.player.email.indexOf('@')>0)
			&&(loginObj.attempt>2)
			&&(CMProps.getVar(CMProps.Str.MUDDOMAIN).length()>0))
			{
				if(CMProps.getBoolVar(CMProps.Bool.HASHPASSWORDS))
					session.promptPrint(L("Would you like you have a new password generated and e-mailed to you (y/N)? "));
				else
					session.promptPrint(L("Would you like your password e-mailed to you (y/N)? "));
				loginObj.state=LoginState.LOGIN_EMAIL_PASSWORD;
				return LoginResult.INPUT_REQUIRED;
			}
			loginObj.state=LoginState.LOGIN_START;
			return null;
		}
		session.println("\n\r");
		return LoginResult.NORMAL_LOGIN;
	}

	protected LoginResult loginEmailPassword(final LoginSessionImpl loginObj, final Session session) throws IOException
	{
		final String input=loginObj.lastInput.toUpperCase().trim();
		if(input.startsWith("Y"))
		{
			String password=loginObj.player.password;
			if(CMProps.getBoolVar(CMProps.Bool.HASHPASSWORDS))
			{
				if(loginObj.acct!=null)
				{
					password=CMLib.encoder().generateRandomPassword();
					loginObj.acct.setPassword(password);
					loginObj.player.password=loginObj.acct.getPasswordStr();
					CMLib.database().DBUpdateAccount(loginObj.acct);
				}
				else
				{
					final MOB playerM=CMLib.players().getLoadPlayer(loginObj.player.name);
					if((playerM!=null)&&(playerM.playerStats()!=null))
					{
						password=CMLib.encoder().generateRandomPassword();
						playerM.playerStats().setPassword(password);
						loginObj.player.password=playerM.playerStats().getPasswordStr();
						CMLib.database().DBUpdatePassword(loginObj.player.name, loginObj.player.password);
					}
				}
			}
			CMLib.smtp().emailOrJournal(CMProps.getVar(CMProps.Str.SMTPSERVERNAME), loginObj.player.name, "noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(), loginObj.player.name,
				"Password for "+loginObj.player.name,
				"Your password for "+loginObj.player.name+" at "+CMProps.getVar(CMProps.Str.MUDDOMAIN)+" is: '"+password+"'.");
			session.stopSession(false,false,false);
			loginObj.reset=true;
			loginObj.state=LoginState.LOGIN_START;
			return LoginResult.NO_LOGIN;
		}
		else
		if((input.length()>0)&&(!input.startsWith("N")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		loginObj.state=LoginState.LOGIN_START;
		return null;
	}

	protected LoginResult acctmenuStart(final LoginSessionImpl loginObj, final Session session)
	{
		final PlayerAccount acct=loginObj.acct;
		session.setServerTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.AccountFlag.ANSI));
		session.setClientTelnetMode(Session.TELNET_ANSI,acct.isSet(PlayerAccount.AccountFlag.ANSI));
		// if its not a new account, do this?
		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"selchar.txt",null,CMFile.FLAG_LOGERRORS).text();
		try
		{
			introText = CMLib.webMacroFilter().virtualPageFilter(introText);
		}
		catch (final Exception ex)
		{
		}
		session.println(null,null,null,"\n\r\n\r"+introText.toString());
		if(acct.isSet(PlayerAccount.AccountFlag.ACCOUNTMENUSOFF))
		{
			loginObj.state=LoginState.ACCTMENU_SHOWCHARS;
		}
		else
		{
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
		}
		return null;
	}

	protected LoginResult acctmenuShowChars(final LoginSessionImpl loginObj, final Session session)
	{
		final PlayerAccount acct=loginObj.acct;
		final StringBuffer buf = new StringBuffer("");
		int longest = 12;
		for(final Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
		{
			final PlayerLibrary.ThinPlayer player = p.nextElement();
			if(player.name().length()+2 > longest)
				longest = player.name().length()+2;
		}
		longest = CMLib.lister().fixColWidth(longest, session);
		buf.append("^X");
		buf.append(CMStrings.padRight(L("Character"),longest));
		buf.append(" " + CMStrings.padRight(L("Race"),10));
		buf.append(" " + CMStrings.padRight(L("Level"),5));
		buf.append(" " + CMStrings.padRight(L("Class"),15));
		buf.append("^.^N\n\r");
		for(final Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
		{
			final PlayerLibrary.ThinPlayer player = p.nextElement();
			buf.append("^H");
			final MOB mob=CMLib.players().getPlayer(player.name());
			final String onc = ((mob == null)||(mob.session()==null)||(mob.session().isStopped())) ?"":" ^y*^?";
			buf.append(CMStrings.padRight(player.name()+onc,longest));
			buf.append("^.^N");
			if(mob != null)
				buf.append(" " + CMStrings.padRight(mob.baseCharStats().raceName(),10));
			else
				buf.append(" " + CMStrings.padRight(player.race(),10));
			if(mob != null)
				buf.append(" " + CMStrings.padRight(""+mob.basePhyStats().level(),5));
			else
				buf.append(" " + CMStrings.padRight(""+player.level(),5));
			if(mob!=null)
				buf.append(" " + CMStrings.padRight(mob.baseCharStats().displayClassName(),15));
			else
				buf.append(" " + CMStrings.padRight(player.charClass(),15));
			buf.append("^.^N\n\r");
		}
		session.println(buf.toString());
		buf.setLength(0);
		loginObj.state=LoginState.ACCTMENU_PROMPT;
		return null;
	}

	protected LoginResult acctmenuShowMenu(final LoginSessionImpl loginObj, final Session session)
	{
		final PlayerAccount acct=loginObj.acct;
		final StringBuffer buf = new StringBuffer("");
		if(!acct.isSet(PlayerAccount.AccountFlag.ACCOUNTMENUSOFF))
		{
			buf.append(L(" ^XAccount Menu^.^N\n\r"));
			buf.append(L(" ^XL^.^w)^Hist characters\n\r"));
			buf.append(L(" ^XN^.^w)^Hew character\n\r"));
			if(acct.isSet(PlayerAccount.AccountFlag.CANEXPORT))
			{
				buf.append(L(" ^XI^.^w)^Hmport character\n\r"));
				buf.append(L(" ^XE^.^w)^Hxport character\n\r"));
			}
			buf.append(L(" ^XD^.^w)^Helete/Retire character\n\r"));
			buf.append(L(" ^XH^.^w)^Help\n\r"));
			buf.append(L(" ^XM^.^w)^Henu OFF\n\r"));
			buf.append(L(" ^XP^.^w)^Hassword change\n\r"));
			if(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE"))
				buf.append(L(" ^XE^.^w)^Hmail change\n\r"));
			buf.append(L(" ^XQ^.^w)^Huit (logout)\n\r"));
			buf.append(L("\n\r^H ^w(^HEnter your character name to login^w)^H"));
			session.println(buf.toString());
			buf.setLength(0);
		}
		loginObj.state=LoginState.ACCTMENU_PROMPT;
		return null;
	}

	protected LoginResult acctmenuPrompt(final LoginSessionImpl loginObj, final Session session)
	{
		session.setStatus(Session.SessionStatus.ACCOUNT_MENU);
		session.promptPrint(L("\n\r^wCommand or Name ^H(?)^w: ^N "));
		loginObj.state=LoginState.ACCTMENU_COMMAND;
		loginObj.savedInput="";
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult acctmenuConfirmCommand(final LoginSessionImpl loginObj, final Session session)
	{
		final String input=loginObj.lastInput.trim().toUpperCase();
		if(input.startsWith("Y"))
		{
			loginObj.lastInput=String.valueOf(loginObj.savedInput)+" <CONFIRMED>";
			loginObj.state=LoginState.ACCTMENU_COMMAND;
		}
		else
		if((input.length()>0)&&(!input.startsWith("N")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		else
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
		return null;
	}

	protected LoginResult acctmenuAddToCommand(final LoginSessionImpl loginObj, final Session session)
	{
		if(loginObj.lastInput.length()>0)
		{
			loginObj.lastInput=loginObj.savedInput+" "+loginObj.lastInput;
			loginObj.state=LoginState.ACCTMENU_COMMAND;
		}
		else
		{
			session.println(L("Aborted."));
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
		}
		return null;
	}

	protected LoginResult acctmenuCommand(final LoginSessionImpl loginObj, final Session session) throws IOException
	{
		final PlayerAccount acct=loginObj.acct;
		if(acct==null)
			return LoginResult.NO_LOGIN;
		final String s=loginObj.lastInput.trim();
		if(s==null)
			return LoginResult.NO_LOGIN;
		if(s.length()==0)
		{
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		loginObj.savedInput=s;
		final String[] parms=CMParms.parse(s).toArray(new String[0]);
		final String cmd=parms[0].toUpperCase().trim();
		if(cmd.equalsIgnoreCase("?")||(("HELP").startsWith(cmd)))
		{
			StringBuffer accountHelp=new CMFile(Resources.buildResourcePath("help")+"accts.txt",null,CMFile.FLAG_LOGERRORS).text();
			try
			{
				 accountHelp = CMLib.webMacroFilter().virtualPageFilter(accountHelp);
			}
			catch(final Exception ex)
			{
			}
			session.println(null,null,null,"\n\r\n\r"+accountHelp.toString());
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		if(("LIST").startsWith(cmd))
		{
			loginObj.state=LoginState.ACCTMENU_SHOWCHARS;
			return null;
		}
		if(("QUIT").startsWith(cmd))
		{
			if((parms.length>1)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
			{
				session.println(L("Bye Bye!"));
				session.stopSession(false,false,false);
				return LoginResult.NO_LOGIN;
			}
			session.promptPrint(L("Quit -- are you sure (y/N)?"));
			loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
			return LoginResult.INPUT_REQUIRED;
		}
		if(("NEW ").startsWith(cmd))
		{
			if(parms.length<2)
			{
				session.promptPrint(L("\n\rPlease enter a name for your character, or '*'\n\r: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[1].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(parms[1].indexOf('*')>=0)
			{
				parms[1]=generateRandomName(3,6);
				while((!isOkName(parms[1],false)) || (CMLib.players().playerExists(parms[1])) || (CMLib.players().accountExists(parms[1])))
					parms[1] = generateRandomName(3, 8);
				loginObj.savedInput=CMStrings.replaceFirst(loginObj.savedInput, "*", parms[1]);
			}
			if(newCharactersAllowed(parms[1],session,acct,parms[1].equalsIgnoreCase(acct.getAccountName())))
			{
				final String login=CMStrings.capitalizeAndLower(parms[1]);
				if((parms.length>2)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
				{
					loginObj.login=login;
					loginObj.state=LoginState.CHARCR_START;
					return null;
				}
				session.promptPrint(L("Create a new character called '@x1' (y/N)?",login));
				loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		if(("MENU").startsWith(cmd))
		{
			if((parms.length>1)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
			{
				if(acct.isSet(PlayerAccount.AccountFlag.ACCOUNTMENUSOFF))
				{
					session.println(L("Menus are back on."));
					acct.setFlag(PlayerAccount.AccountFlag.ACCOUNTMENUSOFF, false);
				}
				else
				if(!acct.isSet(PlayerAccount.AccountFlag.ACCOUNTMENUSOFF))
				{
					session.println(L("Menus are now off."));
					acct.setFlag(PlayerAccount.AccountFlag.ACCOUNTMENUSOFF, true);
				}
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;

			}
			else
			{
				final String promptStr=acct.isSet(PlayerAccount.AccountFlag.ACCOUNTMENUSOFF)?L("Turn menus back on (y/N)?"):"Turn menus off (y/N)?";
				session.promptPrint(promptStr);
				loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			return null;
		}
		if(cmd.startsWith("ANSI")||cmd.startsWith("COLOR"))
		{
			if(cmd.equals("ANSI")||cmd.equals("COLOR"))
			{
				acct.setFlag(AccountFlag.ANSI, !acct.isSet(AccountFlag.ANSI));
				if(acct.isSet(AccountFlag.ANSI))
					session.println(L("ANSI color is now ON."));
				else
					session.println(L("ANSI color is now OFF."));
				session.setServerTelnetMode(Session.TELNET_ANSI,acct.isSet(AccountFlag.ANSI));
				session.setClientTelnetMode(Session.TELNET_ANSI,acct.isSet(AccountFlag.ANSI));
			}
			else
			if((parms.length>1)&&(parms[1].equalsIgnoreCase("ON")))
			{
				if(acct.isSet(AccountFlag.ANSI))
					session.println(L("ANSI color is already on."));
				else
				{
					acct.setFlag(AccountFlag.ANSI, true);
					session.println(L("ANSI color is now ON."));
					session.setServerTelnetMode(Session.TELNET_ANSI,acct.isSet(AccountFlag.ANSI));
					session.setClientTelnetMode(Session.TELNET_ANSI,acct.isSet(AccountFlag.ANSI));
				}
			}
			else
			if((parms.length>1)&&(parms[1].equalsIgnoreCase("OFF")))
			{
				if(!acct.isSet(AccountFlag.ANSI))
					session.println(L("ANSI color is already off."));
				else
				{
					acct.setFlag(AccountFlag.ANSI, false);
					session.println(L("ANSI color is now OFF."));
					session.setServerTelnetMode(Session.TELNET_ANSI,acct.isSet(AccountFlag.ANSI));
					session.setClientTelnetMode(Session.TELNET_ANSI,acct.isSet(AccountFlag.ANSI));
				}
			}
			loginObj.state=LoginState.ACCTMENU_PROMPT;
			return null;
		}
		if("PASSWORD".startsWith(cmd))
		{
			if(parms.length<2)
			{
				session.promptPrint(L("\n\rPlease enter your existing password: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[1].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(parms.length<3)
			{
				session.promptPrint(L("\n\rPlease a new password: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[2].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(parms.length<4)
			{
				session.promptPrint(L("\n\rEnter the password again: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[3].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(!parms[2].equals(parms[3]))
			{
				session.println(L("\n\rPasswords don't match.  Change cancelled."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(!acct.matchesPassword(parms[1]))
			{
				session.println(L("\n\rThat's not your old password.  Change cancelled."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			acct.setPassword(parms[2]);
			CMLib.database().DBUpdateAccount(acct);
			session.println(L("\n\rPassword changed!"));
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		if("EMAIL".startsWith(cmd) && (!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE")))
		{
			if(parms.length<2)
			{
				if(CMProps.getVar(CMProps.Str.EMAILREQ).equalsIgnoreCase("PASSWORD"))
					session.println(L("\n\r** Changing your email address will cause a new password to be generated and emailed."));
				session.promptPrint(L("\n\rPlease enter a new email address: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if((parms[1].length()==0)||(parms[1].indexOf('@')<0)||(parms[1].length()<6))
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(parms.length<3)
			{
				session.promptPrint(L("\n\rEnter the email address again: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[2].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(!parms[1].equals(parms[2]))
			{
				session.println(L("\n\rEmail addresses don't match.  Change aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			acct.setEmail(parms[1]);
			if(CMProps.getVar(CMProps.Str.EMAILREQ).equalsIgnoreCase("PASSWORD"))
			{
				final String password=CMLib.encoder().generateRandomPassword();
				acct.setPassword(password);
				CMLib.smtp().emailOrJournal(CMProps.getVar(CMProps.Str.SMTPSERVERNAME), acct.getAccountName(), "noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(), acct.getAccountName(),
					L("Password for @x1",acct.getAccountName()),
					L("Your password for @x1 is: @x2\n\r"
					+ "You can login by pointing your mud client at @x3 port(s): @x4.\n\r"
					+ "After creating a character, you may use the PASSWORD command to change it once you are online.",
					acct.getAccountName(),password,CMProps.getVar(CMProps.Str.MUDDOMAIN),CMProps.getVar(CMProps.Str.MUDPORTS)));
				session.println(L("Your account email address has been updated.  You will receive an email with your new password shortly."));
				session.stopSession(false,false,false);
				CMLib.s_sleep(1000);
				CMLib.database().DBUpdateAccount(acct);
				return LoginResult.NO_LOGIN;
			}
			CMLib.database().DBUpdateAccount(acct);
			session.println(L("Email address changed."));
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		if(("RETIRE").startsWith(cmd)||("DELETE ").startsWith(cmd))
		{
			if(parms.length<2)
			{
				session.promptPrint(L("\n\rPlease the name of the character: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[1].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			PlayerLibrary.ThinPlayer delPlayerChk = null;
			for(final Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
			{
				final PlayerLibrary.ThinPlayer player = p.nextElement();
				if(player.name().equalsIgnoreCase(parms[1]))
					delPlayerChk=player;
			}
			final String properName=CMStrings.capitalizeAndLower(parms[1]);
			if(delPlayerChk==null)
			{
				acct.delPlayer(properName);
				session.println(L("The character '@x1' is unknown.",CMStrings.capitalizeAndLower(parms[1])));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			final PlayerLibrary.ThinPlayer delPlayer = delPlayerChk;
			if((parms.length>2)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
			{
				final MOB M=CMLib.players().getLoadPlayer(delPlayer.name());
				if(M!=null)
				{
					CMLib.players().obliteratePlayer(M, true, false);
				}
				acct.delPlayer(delPlayer.name());
				session.println(L("@x1 has been deleted.",delPlayer.name()));
			}
			else
			{
				session.promptPrint(L("Are you sure you want to retire and delete '@x1' (y/N)?",delPlayer.name()));
				loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		if(("EXPORT ").startsWith(cmd)&&(acct.isSet(PlayerAccount.AccountFlag.CANEXPORT)))
		{
			if(parms.length<2)
			{
				session.promptPrint(L("\n\rPlease the name of the character: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[1].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			PlayerLibrary.ThinPlayer delPlayer = null;
			for(final Enumeration<PlayerLibrary.ThinPlayer> p = acct.getThinPlayers(); p.hasMoreElements();)
			{
				final PlayerLibrary.ThinPlayer player = p.nextElement();
				if(player.name().equalsIgnoreCase(parms[1]))
					delPlayer=player;
			}
			if(delPlayer==null)
			{
				session.println(L("The character '@x1' is unknown.",CMStrings.capitalizeAndLower(parms[1])));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if(parms.length<3)
			{
				session.promptPrint(L("\n\rEnter a new password for this character: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			final String password=parms[2];
			if((password==null)||(password.trim().length()==0))
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if((parms.length>3)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
			{
				final MOB M=CMLib.players().getLoadPlayer(delPlayer.name());
				if(M!=null)
				{
					acct.delPlayer(M);
					M.playerStats().setAccount(null);
					CMLib.database().DBUpdateAccount(acct);
					M.playerStats().setLastDateTime(System.currentTimeMillis());
					M.playerStats().setLastUpdated(System.currentTimeMillis());
					M.playerStats().setPassword(password);
					CMLib.database().DBUpdatePlayer(M);
					session.println(L("@x1 has been exported from your account.",delPlayer.name()));
				}
			}
			else
			{
				session.promptPrint(L("Are you sure you want to remove character  '@x1' from your account (y/N)?",delPlayer.name()));
				loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		if(("IMPORT ").startsWith(cmd))
		{
			if(parms.length<2)
			{
				session.promptPrint(L("\n\rPlease the name of the character: "));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			if(parms[1].length()==0)
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			int maxPlayersOnAccount = CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM);
			if(maxPlayersOnAccount < Integer.MAX_VALUE)
				maxPlayersOnAccount += acct.getBonusCharsLimit();
			if((maxPlayersOnAccount<=acct.numPlayers())
			&&(!acct.isSet(PlayerAccount.AccountFlag.NUMCHARSOVERRIDE)))
			{
				session.println(L("You may only have @x1 characters.  Please delete one to create another.",""+maxPlayersOnAccount));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			final String name=CMStrings.capitalizeAndLower(parms[1]);
			final PlayerLibrary.ThinnerPlayer newCharT = CMLib.database().DBUserSearch(name);
			if(parms.length<3)
			{
				session.promptPrint(L("\n\rEnter the existing password for your character '@x1': ",name));
				loginObj.state=LoginState.ACCTMENU_ADDTOCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			final String password=parms[2];
			if((password==null)||(password.trim().length()==0))
			{
				session.println(L("Aborted."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if((newCharT==null)
			||(!newCharT.matchesPassword(password))
			||((newCharT.accountName!=null)
				&&(newCharT.accountName.length()>0)
				&&(!newCharT.accountName.equalsIgnoreCase(acct.getAccountName()))))
			{
				session.println(L("Character name or password is incorrect."));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			if((parms.length>3)&&(parms[parms.length-1].equalsIgnoreCase("<CONFIRMED>")))
			{
				final MOB M=CMLib.players().getLoadPlayer(newCharT.name);
				if(M!=null)
				{
					acct.addNewPlayer(M);
					M.playerStats().setAccount(acct);
					CMLib.database().DBUpdateAccount(acct);
					CMLib.database().DBUpdatePlayer(M);
					session.println(L("@x1 has been imported into your account.",M.name()));
				}
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			}
			else
			{
				session.promptPrint(L("Are you sure you want to import character  '@x1' into your account (y/N)?",newCharT.name));
				loginObj.state=LoginState.ACCTMENU_CONFIRMCOMMAND;
				return LoginResult.INPUT_REQUIRED;
			}
			return null;
		}
		final boolean wizi=(parms.length>1)&&(parms[parms.length-1]).equalsIgnoreCase("!");
		PlayerLibrary.ThinnerPlayer playMe = null;
		String name=CMStrings.capitalizeAndLower(cmd);
		synchronized(("LOGIN_"+acct.name()).intern())
		{
			final String playerName=acct.findPlayer(name);
			if(playerName!=null)
			{
				name=playerName;
				playMe = CMLib.database().DBUserSearch(name);
			}
			if(playMe == null)
			{
				session.println(L("'@x1' is an unknown character or command.  Use ? for help.",name));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			final MOB realMOB=CMLib.players().getLoadPlayer(playMe.name);
			if(realMOB==null)
			{
				session.println(L("Error loading character '@x1'.  Please contact the management.",name));
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
			session.setMob(realMOB);
			playMe.loadedMOB=realMOB;
			if(this.completePlayerLogin(session, wizi)!=LoginResult.NORMAL_LOGIN)
			{
				session.setMob(null);
				playMe.loadedMOB=null;
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
		}
		return LoginResult.NORMAL_LOGIN;
	}

	@Override
	public LoginResult completePlayerLogin(final Session session, boolean wizi) throws IOException
	{
		final MOB realMOB = session.mob();
		if(realMOB==null || (realMOB.playerStats()==null))
			return null;
		final PlayerAccount acct = realMOB.playerStats().getAccount();
		if(acct != null)
		{
			int numAccountOnline=0;
			session.setStatus(SessionStatus.LOGIN2);
			for(final Session S : CMLib.sessions().allIterable())
			{
				if(S.mob()!=null)
				{
					if((S.mob()!=realMOB)
					&&(S.mob().playerStats()!=null)
					&&(S.mob().playerStats().getAccount()==acct)
					&&(CMLib.flags().isInTheGame(S.mob(), true)))
						numAccountOnline++;
				}
			}
			int maxConnectionsPerAccount = CMProps.getIntVar(CMProps.Int.MAXCONNSPERACCOUNT);
			if(maxConnectionsPerAccount > 0)
				maxConnectionsPerAccount += acct.getBonusCharsOnlineLimit();
			if((maxConnectionsPerAccount>0)
			&&(numAccountOnline>=maxConnectionsPerAccount)
			&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAXCONNSPERACCOUNT))
			&&(!CMProps.isOnWhiteList(CMProps.WhiteList.CONNS, session.getAddress()))
			&&(!CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, acct.getAccountName()))
			&&(!CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, realMOB.Name()))
			&&(!acct.isSet(PlayerAccount.AccountFlag.MAXCONNSOVERRIDE)))
			{
				session.println(L("You may only have @x1 of your characters on at one time.",""+CMProps.getIntVar(CMProps.Int.MAXCONNSPERACCOUNT)));
				return null;
			}
		}

		final LoginResult prelimResults = prelimChecks(session,realMOB,realMOB.Name(),realMOB.playerStats().getEmail());
		if(prelimResults!=null)
			return prelimResults;
		if(isExpired(acct,session,realMOB))
			return null;
		final LoginResult completeResult=completeCharacterLogin(session,realMOB.Name(), wizi);
		if(completeResult == LoginResult.NO_LOGIN)
			return null;
		return LoginResult.NORMAL_LOGIN;
	}
	
	protected LoginResult charcrStart(final LoginSessionImpl loginObj, final Session session)
	{
		session.setStatus(Session.SessionStatus.CHARCREATE);

		loginObj.login=CMStrings.capitalizeAndLower(loginObj.login.trim());
		Log.sysOut("Creating user: "+loginObj.login);

		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newchar.txt",null,CMFile.FLAG_LOGERRORS).text();
		try
		{
			introText = CMLib.webMacroFilter().virtualPageFilter(introText);
		}
		catch (final Exception ex)
		{
		}
		session.println(null,null,null,"\n\r\n\r"+introText.toString());
		final String password=(loginObj.acct!=null)?loginObj.acct.getPasswordStr():"";

		final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
		if((!emailPassword)&&(password.length()==0))
		{
			session.promptPrint(L("\n\rEnter a password: "));
			loginObj.state=LoginState.CHARCR_PASSWORDDONE;
			return LoginResult.INPUT_REQUIRED;
		}
		loginObj.state=LoginState.CHARCR_PASSWORDDONE;
		return null;
	}

	protected LoginResult charcrPasswordDone(final LoginSessionImpl loginObj, final Session session)
	{
		String password=(loginObj.acct!=null)?loginObj.acct.getPasswordStr():"";
		final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
		if((!emailPassword)&&(password.length()==0))
		{
			password=loginObj.lastInput;
			if(password.length()==0)
			{
				session.println(L("\n\rYou must enter a password to continue."));
				session.promptPrint(L("\n\rEnter a password: "));
				loginObj.state=LoginState.CHARCR_PASSWORDDONE;
				return LoginResult.INPUT_REQUIRED;
			}
		}
		loginObj.password=password;
		final PlayerAccount acct=loginObj.acct;
		final MOB mob=CMClass.getMOB("StdMOB");
		mob.setName(loginObj.login);
		loginObj.mob=mob;
		mob.setSession(session);
		session.setMob(mob);
		if(mob.playerStats()==null)
			mob.setPlayerStats((PlayerStats)CMClass.getCommon("DefaultPlayerStats"));
		mob.setAttributesBitmap(0);
		mob.setAttribute(MOB.Attrib.AUTOEXITS, true);
		mob.setAttribute(MOB.Attrib.AUTOWEATHER, true);
		setGlobalBitmaps(mob);

		if((acct==null)||(acct.getPasswordStr().length()==0))
		{
			mob.playerStats().setPassword(password);
			executeScript(mob,getLoginScripts().get("PASSWORD"));
		}

		if((acct!=null)&&(acct.getEmail().length()>0))
		{
			mob.setAttribute(MOB.Attrib.AUTOFORWARD,false);
			loginObj.state=LoginState.CHARCR_EMAILDONE;
		}
		else
		if(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE"))
		{
			mob.setAttribute(MOB.Attrib.AUTOFORWARD,false);
			loginObj.state=LoginState.CHARCR_EMAILSTART;
		}
		else
		{
			mob.setAttribute(MOB.Attrib.AUTOFORWARD,true);
			loginObj.state=LoginState.CHARCR_EMAILDONE;
		}
		return null;
	}

	protected LoginResult charcrEmailStart(final LoginSessionImpl loginObj, final Session session)
	{
		StringBuffer emailIntro=new CMFile(Resources.buildResourcePath("text")+"email.txt",null,CMFile.FLAG_LOGERRORS).text();
		try
		{
			 emailIntro = CMLib.webMacroFilter().virtualPageFilter(emailIntro);
		}
		catch(final Exception ex)
		{
		}
		session.println(null,null,null,emailIntro.toString());
		loginObj.state=LoginState.CHARCR_EMAILPROMPT;
		return null;
	}

	protected LoginResult charcrEmailPrompt(final LoginSessionImpl loginObj, final Session session)
	{
		session.promptPrint(L("\n\rEnter your e-mail address: "));
		loginObj.state=LoginState.CHARCR_EMAILENTERED;
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult charcrEmailEntered(final LoginSessionImpl loginObj, final Session session)
	{
		final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
		final boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
		final String newEmail=loginObj.lastInput;
		if((emailReq||emailPassword)
		&& ((newEmail==null)||(newEmail.trim().length()==0)||(!CMLib.smtp().isValidEmailAddress(newEmail))))
		{
			session.println(L("\n\rA valid email address is required.\n\r"));
			loginObj.state=LoginState.CHARCR_EMAILPROMPT;
			return null;
		}

		loginObj.savedInput=newEmail;
		if(emailPassword)
			session.println(L("This email address will be used to send you a password."));
		if(emailReq||emailPassword)
		{
			session.promptPrint(L("Confirm that '@x1' is correct by re-entering.\n\rRe-enter: ",newEmail));
			loginObj.state=LoginState.CHARCR_EMAILCONFIRMED;
			return LoginResult.INPUT_REQUIRED;
		}
		loginObj.state=LoginState.CHARCR_EMAILCONFIRMED;
		return null;
	}

	protected LoginResult charcrEmailConfirmed(final LoginSessionImpl loginObj, final Session session)
	{
		final boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
		final String newEmail=loginObj.savedInput;
		boolean emailConfirmed=false;
		if((newEmail.length()>0)&&(newEmail.equalsIgnoreCase(loginObj.lastInput)))
			emailConfirmed=CMLib.smtp().isValidEmailAddress(newEmail);
		loginObj.mob.playerStats().setEmail("");
		if(emailConfirmed||((!emailReq)&&(newEmail.trim().length()==0)))
		{
			loginObj.mob.playerStats().setEmail(newEmail);
			loginObj.mob.setAttribute(MOB.Attrib.AUTOFORWARD,true);
			loginObj.state=LoginState.CHARCR_EMAILDONE;
			return null;
		}
		session.println(L("\n\rThat email address combination was invalid.\n\r"));
		loginObj.state=LoginState.CHARCR_EMAILPROMPT;
		return null;
	}

	protected LoginResult charcrEmailDone(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		session.setMob(loginObj.mob);
		final PlayerAccount acct=loginObj.acct;
		if((mob.playerStats().getEmail()!=null)&&CMSecurity.isBanned(mob.playerStats().getEmail()))
		{
			session.println(L("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r"));
			if(mob==session.mob())
				session.stopSession(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		mob.playerStats().setAccount(acct);

		executeScript(mob,getLoginScripts().get("EMAIL"));

		if(acct!=null)
		{
			if(acct.isSet(PlayerAccount.AccountFlag.ANSI))
				mob.setAttribute(MOB.Attrib.ANSI,true);
			else
			{
				mob.setAttribute(MOB.Attrib.ANSI,false);
				session.setServerTelnetMode(Session.TELNET_ANSI,false);
				session.setClientTelnetMode(Session.TELNET_ANSI,false);
			}
			loginObj.state=LoginState.CHARCR_ANSIDONE;
		}
		else
		{
			session.promptPrint(L("\n\rDo you want ANSI colors (Y/n)?"));
			loginObj.state=LoginState.CHARCR_ANSICONFIRMED;
			return LoginResult.INPUT_REQUIRED;
		}
		return null;
	}

	protected LoginResult charcrANSIConfirmed(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		session.setMob(loginObj.mob);
		final String input=loginObj.lastInput.trim().toUpperCase();
		if(input.startsWith("N"))
		{
			mob.setAttribute(MOB.Attrib.ANSI,false);
			session.setServerTelnetMode(Session.TELNET_ANSI,false);
			session.setClientTelnetMode(Session.TELNET_ANSI,false);
		}
		else
		if((input.length()>0)&&(!input.startsWith("Y")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		else
			mob.setAttribute(MOB.Attrib.ANSI,true);
		loginObj.state=LoginState.CHARCR_ANSIDONE;
		return null;
	}

	protected LoginResult charcrANSIDone(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		session.setMob(loginObj.mob);
		if((session.getClientTelnetMode(Session.TELNET_MSP))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MSP)))
			mob.setAttribute(MOB.Attrib.SOUND,true);
		if((session.getClientTelnetMode(Session.TELNET_MXP))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MXP)))
			mob.setAttribute(MOB.Attrib.MXP,true);

		executeScript(mob,getLoginScripts().get("ANSI"));

		final int themeCode=CMProps.getIntVar(CMProps.Int.MUDTHEME);
		switch(themeCode)
		{
			case Area.THEME_FANTASY:
			case Area.THEME_HEROIC:
			case Area.THEME_TECHNOLOGY:
				loginObj.theme=themeCode;
				loginObj.state=LoginState.CHARCR_THEMEDONE;
				break;
			default:
				loginObj.state=LoginState.CHARCR_THEMESTART;
				break;
		}
		return null;
	}

	protected LoginResult charcrThemeStart(final LoginSessionImpl loginObj, final Session session)
	{
		session.setMob(loginObj.mob);
		final int themeCode=CMProps.getIntVar(CMProps.Int.MUDTHEME);
		loginObj.theme=-1;
		String selections="";
		if(CMath.bset(themeCode,Area.THEME_FANTASY))
		{ 
			selections+="/F";
		}
		if(CMath.bset(themeCode,Area.THEME_HEROIC))
		{ 
			selections+="/H";
		}
		if(CMath.bset(themeCode,Area.THEME_TECHNOLOGY))
		{ 
			selections+="/T";
		}
		if(selections.length()==0)
			selections="/F";
		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"themes.txt",null,CMFile.FLAG_LOGERRORS).text();
		try
		{
			introText = CMLib.webMacroFilter().virtualPageFilter(introText);
		}
		catch (final Exception ex)
		{
		}
		session.println(null,null,null,introText.toString());
		session.promptPrint(L("\n\r^!Please select from the following:^N @x1\n\r: ",selections.substring(1)));
		loginObj.state=LoginState.CHARCR_THEMEPICKED;
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult charcrThemePicked(final LoginSessionImpl loginObj, final Session session)
	{
		session.setMob(loginObj.mob);
		final int themeCode=CMProps.getIntVar(CMProps.Int.MUDTHEME);
		final String themeStr=loginObj.lastInput;
		if(themeStr.toUpperCase().startsWith("F") && CMath.bset(themeCode,Area.THEME_FANTASY))
			loginObj.theme=Area.THEME_FANTASY;
		if(themeStr.toUpperCase().startsWith("H") && CMath.bset(themeCode,Area.THEME_HEROIC))
			loginObj.theme=Area.THEME_HEROIC;
		if(themeStr.toUpperCase().startsWith("T") && CMath.bset(themeCode,Area.THEME_TECHNOLOGY))
			loginObj.theme=Area.THEME_TECHNOLOGY;
		if(loginObj.theme<0)
		{
			session.println(L("\n\rThat is not a valid choice.\n\r"));
			loginObj.state=LoginState.CHARCR_THEMESTART;
		}
		else
			loginObj.state=LoginState.CHARCR_THEMEDONE;
		return null;
	}

	protected LoginResult charcrThemeDone(final LoginSessionImpl loginObj, final Session session)
	{
		CMLib.sessions().moveSessionToCorrectThreadGroup(session,loginObj.theme);
		final MOB mob=loginObj.mob;
		session.setMob(loginObj.mob);
		executeScript(mob,getLoginScripts().get("THEME"));
		loginObj.state=LoginState.CHARCR_RACESTART;
		return null;
	}

	protected MOB setMOBClass(final String classID, final LoginSessionImpl loginObj, final Session session)
	{
		MOB mob=loginObj.mob;
		if(!mob.ID().equalsIgnoreCase(classID))
		{
			MOB newM = CMClass.getMOB(classID);
			final PlayerStats playerStats=mob.playerStats();
			PlayerAccount account=null;
			if(playerStats!=null)
				account=playerStats.getAccount();
			newM.setName(mob.Name());
			newM.setAttributesBitmap(mob.getAttributesBitmap());
			newM.setDisplayText(mob.displayText());
			newM.setDescription(mob.description());
			newM.setPlayerStats(playerStats);
			newM.setBaseCharStats(mob.baseCharStats());
			newM.setBasePhyStats(mob.basePhyStats());
			newM.setBaseState(mob.baseState());
			newM.recoverCharStats();
			newM.recoverPhyStats();
			newM.recoverMaxState();
			loginObj.mob=newM;
			session.setMob(newM);
			newM.setSession(session);
			mob.setSession(null);
			mob.destroy();
			if(account!=null)
			{
				account.delPlayer(mob);
				account.addNewPlayer(newM);
			}
			return newM;
		}
		return mob;
	}
	
	protected LoginResult charcrRaceStart(final LoginSessionImpl loginObj, final Session session)
	{
		MOB mob=loginObj.mob;
		session.setMob(loginObj.mob);
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		{
			Race newRace=CMClass.getRace("PlayerRace");
			if(newRace==null)
				newRace=CMClass.getRace("StdRace");
			if(newRace != null)
			{
				mob=setMOBClass(newRace.useRideClass() ? "StdRideable" : "StdMOB", loginObj,session);
				mob.baseCharStats().setMyRace(newRace);
				loginObj.state=LoginState.CHARCR_RACEDONE;
				return null;
			}
			else
			{
				Log.errOut("CharCreation","Races are disabled, but neither PlayerRace nor StdRace exists?!");
			}
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
		{
			StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"races.txt",null,CMFile.FLAG_LOGERRORS).text();
			try 
			{ 
				introText = CMLib.webMacroFilter().virtualPageFilter(introText);
			}
			catch (final Exception ex)
			{
			}
			session.println(null,null,null,introText.toString());
		}
		final StringBuffer listOfRaces=new StringBuffer("[");
		boolean tmpFirst = true;
		final List<Race> qualRaces = raceQualifies(loginObj.theme);
		for(final Race R : qualRaces)
		{
			if (!tmpFirst)
				listOfRaces.append(", ");
			else
				tmpFirst = false;
			listOfRaces.append("^H"+R.name()+"^N");
		}
		listOfRaces.append("]");
		session.println(L("\n\r^!Please choose from the following races (?):^N"));
		session.print(listOfRaces.toString());
		session.promptPrint("\n\r: ");
		loginObj.state=LoginState.CHARCR_RACEENTERED;
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult charcrRaceReEntered(final LoginSessionImpl loginObj, final Session session)
	{
		final String raceStr=loginObj.lastInput.trim();
		MOB mob=loginObj.mob;

		if(raceStr.trim().equalsIgnoreCase("?")||(raceStr.length()==0))
			session.println(null,null,null,"\n\r"+new CMFile(Resources.buildResourcePath("text")+"races.txt",null,CMFile.FLAG_LOGERRORS).text().toString());
		else
		{
			Race newRace=CMClass.getRace(raceStr);
			if((newRace!=null)&&((!CMLib.login().isAvailableRace(newRace))
								||(!CMath.bset(newRace.availabilityCode(),loginObj.theme))))
				newRace=null;
			if(newRace==null)
			{
				for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				{
					final Race R=r.nextElement();
					if((R.name().equalsIgnoreCase(raceStr))
					&&(CMLib.login().isAvailableRace(R))
					&&(CMath.bset(R.availabilityCode(),loginObj.theme)))
					{
						newRace=R;
						break;
					}
				}
			}
			if(newRace==null)
			{
				for(final Enumeration<Race> r=CMClass.races();r.hasMoreElements();)
				{
					final Race R=r.nextElement();
					if((R.name().toUpperCase().startsWith(raceStr.toUpperCase()))
					&&(CMLib.login().isAvailableRace(R))
					&&(CMath.bset(R.availabilityCode(),loginObj.theme)))
					{
						newRace=R;
						break;
					}
				}
			}
			if(newRace!=null)
			{
				final StringBuilder str=CMLib.help().getHelpText(newRace.ID().toUpperCase(),mob,false);
				if(str!=null) 
					session.println("\n\r^N"+str.toString()+"\n\r");
				session.promptPrint(L("^!Is ^H@x1^N^! correct (Y/n)?^N",newRace.name()));
				mob=setMOBClass(newRace.useRideClass() ? "StdRideable" : "StdMOB", loginObj, session);
				mob.baseCharStats().setMyRace(newRace);
				mob.charStats().setMyRace(newRace);
				mob.charStats().setWearableRestrictionsBitmap(mob.charStats().getWearableRestrictionsBitmap()|mob.charStats().getMyRace().forbiddenWornBits());
				loginObj.state=LoginState.CHARCR_RACECONFIRMED;
				return LoginResult.INPUT_REQUIRED;
			}
		}
		loginObj.state=LoginState.CHARCR_RACESTART;
		return null;
	}

	protected LoginResult charcrRaceConfirmed(final LoginSessionImpl loginObj, final Session session)
	{
		final String input=loginObj.lastInput.trim().toUpperCase();
		if(input.startsWith("N"))
			loginObj.state=LoginState.CHARCR_RACESTART;
		else
		if((input.length()>0)&&(!input.startsWith("Y")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		else
			loginObj.state=LoginState.CHARCR_RACEDONE;
		return null;
	}

	protected LoginResult charcrRaceDone(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		mob.baseState().setHitPoints(CMProps.getIntVar(CMProps.Int.STARTHP));
		mob.baseState().setMovement(CMProps.getIntVar(CMProps.Int.STARTMOVE));
		mob.baseState().setMana(CMProps.getIntVar(CMProps.Int.STARTMANA));

		executeScript(mob,getLoginScripts().get("RACE"));
		loginObj.state=LoginState.CHARCR_GENDERSTART;
		return null;
	}

	protected LoginResult charcrGenderStart(final LoginSessionImpl loginObj, final Session session)
	{
		session.promptPrint(L("\n\r^!What is your gender (M/F)?^N"));
		loginObj.state=LoginState.CHARCR_GENDERDONE;
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult charcrGenderDone(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		final PlayerAccount acct=loginObj.acct;

		final String gender=loginObj.lastInput.toUpperCase().trim();
		if((!gender.startsWith("M"))&&(!gender.startsWith("F")))
		{
			loginObj.state=LoginState.CHARCR_GENDERSTART;
			return null;
		}
		mob.baseCharStats().setStat(CharStats.STAT_GENDER,gender.toUpperCase().charAt(0));
		mob.charStats().setStat(CharStats.STAT_GENDER,gender.toUpperCase().charAt(0));

		mob.baseCharStats().getMyRace().startRacing(mob,false);

		executeScript(mob,getLoginScripts().get("GENDER"));

		if((CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))&&(mob.playerStats()!=null)&&(acct==null))
			mob.playerStats().setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*(CMProps.getIntVar(CMProps.Int.TRIALDAYS))));
		return charcrStatInit(loginObj, session, 0);
	}

	protected LoginResult charcrStatInit(final LoginSessionImpl loginObj, final Session session, final int bonusPoints)
	{
		final MOB mob=loginObj.mob;
		int startStat=CMProps.getIntVar(CMProps.Int.STARTSTAT);
		if((CMSecurity.isDisabled(CMSecurity.DisFlag.ATTRIBS)&&(startStat<=0)))
			startStat=10;
		if(startStat>0)
		{
			mob.baseCharStats().setAllBaseValues(CMProps.getIntVar(CMProps.Int.STARTSTAT));
			for(int i=0;i<bonusPoints;i++)
			{
				final int randStat=CMLib.dice().roll(1, CharStats.CODES.BASECODES().length, -1);
				mob.baseCharStats().setStat(CharStats.CODES.BASECODES()[randStat], mob.baseCharStats().getStat(CharStats.CODES.BASECODES()[randStat])+1);
			}
			mob.recoverCharStats();
			loginObj.state=LoginState.CHARCR_STATDONE;
		}
		else
		{
			StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"stats.txt",null,CMFile.FLAG_LOGERRORS).text();
			try
			{
				introText = CMLib.webMacroFilter().virtualPageFilter(introText);
			}
			catch (final Exception ex)
			{
			}
			session.println(null,null,null,"\n\r\n\r"+introText.toString());

			for(int i=0;i<CharStats.CODES.BASECODES().length;i++)
				mob.baseCharStats().setStat(CharStats.CODES.BASECODES()[i],CMProps.getIntVar(CMProps.Int.BASEMINSTAT));
			mob.recoverCharStats();
			loginObj.state=LoginState.CHARCR_STATSTART;
			CMLib.achievements().loadAccountAchievements(mob,AchievementLoadFlag.CHARCR_PRELOAD);
			loginObj.statPoints = getTotalBonusStatPoints(mob.playerStats(), loginObj.acct)+bonusPoints;
		}
		loginObj.baseStats = (CharStats)mob.baseCharStats().copyOf();
		return null;
	}

	protected LoginResult charcrStatStart(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		if(loginObj.baseStats==null)
			loginObj.baseStats = (CharStats)mob.baseCharStats().copyOf();
		final List<String> validStats = new ArrayList<String>(CharStats.CODES.BASECODES().length);
		for(final int i : CharStats.CODES.BASECODES())
			validStats.add(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(i)));
		List<CharClass> qualifyingClassListV=new Vector<CharClass>(1);
		final boolean randomRoll = CMProps.getIntVar(CMProps.Int.STARTSTAT) == 0;
		if(randomRoll)
		{
			loginObj.baseStats.copyInto(mob.baseCharStats());
			mob.baseCharStats().setWearableRestrictionsBitmap(0);
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
			{
				for(int i=0;i<50 && qualifyingClassListV.size()==0;i++)
				{
					reRollStats(mob.baseCharStats(),loginObj.statPoints);
					mob.recoverCharStats();
					qualifyingClassListV=classQualifies(mob,loginObj.theme);
				}
			}
		}
		mob.recoverCharStats();
		qualifyingClassListV=classQualifies(mob,loginObj.theme);

		if(!randomRoll || (qualifyingClassListV.size()>0) ||CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
		{
			final int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
			final StringBuffer statstr=new StringBuffer(L("Your current stats are: \n\r"));
			final CharStats CT=mob.baseCharStats();
			int total=0;
			for(final int i : CharStats.CODES.BASECODES())
			{
				total += CT.getStat(i);
				statstr.append("^H"+CMStrings.padRight(CMStrings.capitalizeAndLower(CharStats.CODES.DESC(i)),15)
							  +"^N: ^w"+CMStrings.padRight(Integer.toString(CT.getStat(i)),2)
							  +"^N/^w"+(max+CT.getStat(CharStats.CODES.toMAXBASE(i)))+"^N\n\r");
			}
			statstr.append("^w"+CMStrings.padRight(L("STATS TOTAL"),15)+"^N: ^w"+total+"^N/^w"+(CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)*6)+"^.^N");
			session.println(statstr.toString());
			if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)
			&&(!mob.baseCharStats().getMyRace().classless())
			&&(randomRoll || qualifyingClassListV.size()>0)
			&&((qualifyingClassListV.size()!=1)||(!CMProps.getVar(CMProps.Str.MULTICLASS).startsWith("APP-"))))
				session.println(L("\n\rThis would qualify you for ^H@x1^N.",buildQualifyingClassList(mob,qualifyingClassListV,"and")));
			if(randomRoll)
			{
				session.promptPrint(L("^!Would you like to re-roll (y/N)?^N"));
				loginObj.state=LoginState.CHARCR_STATCONFIRM;
				return LoginResult.INPUT_REQUIRED;
			}
			else
			{
				String promptStr;
				if(loginObj.statPoints == 0)
				{
					session.println(L("\n\r^!You have no more points remaining.^N"));
					promptStr = L("^NEnter a Stat to remove points, ? for help, R for random roll, or ENTER to complete.^N\n\r: ^N");
				}
				else
				{
					session.println(L("\n\r^NYou have ^w@x1^N points remaining.^N",""+loginObj.statPoints));
					promptStr = L("^NEnter a Stat to add or remove points, ? for help, or R for random roll.^N\n\r: ^N");
				}

				session.promptPrint(promptStr);
				loginObj.state=LoginState.CHARCR_STATPICK;
				return LoginResult.INPUT_REQUIRED;
			}
		}
		else
			loginObj.state=LoginState.CHARCR_STATDONE;
		return null;
	}

	protected LoginResult charcrStatConfirm(final LoginSessionImpl loginObj, final Session session)
	{
		final String input=loginObj.lastInput.toUpperCase().trim();
		if(input.startsWith("Y"))
			loginObj.state=LoginState.CHARCR_STATSTART;
		else
		if((input.length()>0)&&(!input.startsWith("N")))
		{
			session.promptPrint(L(RECONFIRMSTR));
			return LoginResult.INPUT_REQUIRED;
		}
		else
			loginObj.state=LoginState.CHARCR_STATDONE;
		return null;
	}

	protected LoginResult charcrStatPickAdd(final LoginSessionImpl loginObj, final Session session)
	{
		loginObj.lastInput=loginObj.savedInput+" "+loginObj.lastInput;
		loginObj.state=LoginState.CHARCR_STATPICK;
		return null;
	}

	protected LoginResult charcrStatPick(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		if(loginObj.baseStats==null)
			loginObj.baseStats = (CharStats)mob.baseCharStats().copyOf();
		final CharStats CT=mob.baseCharStats();
		String prompt=loginObj.lastInput.trim();
		final List<CharClass> qualifyingClassListV=classQualifies(mob,loginObj.theme);
		if((loginObj.statPoints == 0)&&(prompt.trim().length()==0))
		{
			if(qualifyingClassListV.size()==0)
			{
				session.println(L("^rYou do not qualify for any classes.  Please modify your stats until you do.^N"));
			}
			else
			{
				loginObj.state=LoginState.CHARCR_STATDONE;
				return null;
			}
		}
		if(prompt.trim().equals("?"))
		{
			StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"stats.txt",null,CMFile.FLAG_LOGERRORS).text();
			try
			{
				 introText = CMLib.webMacroFilter().virtualPageFilter(introText);
			}
			catch(final Exception ex)
			{
			}
			session.println(null,null,null,"\n\r\n\r"+introText.toString());
			loginObj.state=LoginState.CHARCR_STATSTART;
			return null;
		}
		if(prompt.toLowerCase().startsWith("r"))
		{
			loginObj.baseStats.copyInto(mob.baseCharStats());
			mob.baseCharStats().setWearableRestrictionsBitmap(0);
			reRollStats(mob.baseCharStats(),getTotalBonusStatPoints(mob.playerStats(), loginObj.acct));
			loginObj.statPoints=0;
			loginObj.state=LoginState.CHARCR_STATSTART;
			return null;
		}
		if(prompt.trim().length()>0)
		{
			boolean remove = prompt.startsWith("-");
			int statPointsChange = 0;
			if(remove)
				prompt = prompt.substring(1).trim();
			final int space = prompt.lastIndexOf(' ');
			if((space > 0)&&(CMath.isInteger(prompt.substring(space+1).trim())||(prompt.substring(space+1).trim().startsWith("+"))))
			{
				String numStr = prompt.substring(space+1).trim();
				if(numStr.startsWith("+"))
					numStr=numStr.substring(1).trim();
				prompt = prompt.substring(0,space).trim();
				final int num = CMath.s_int(numStr);
				if((num > -1000)&&(num < 1000)&&(num != 0))
					statPointsChange=num;
				else
				{
					session.println(L("^r'@x1' is not a positive or negative number.^N",numStr));
					loginObj.state=LoginState.CHARCR_STATSTART;
					return null;
				}
				if(statPointsChange < 0)
				{
					remove=true;
					statPointsChange = statPointsChange * -1;
				}
			}
			else
			if(space>0)
			{
				session.println(L("^r'@x1' is not a positive or negative number.^N",prompt.substring(space+1)));
				loginObj.state=LoginState.CHARCR_STATSTART;
				return null;
			}
			else
			if(remove)
				statPointsChange=-1;

			final List<String> validStats = new ArrayList<String>(CharStats.CODES.BASECODES().length);
			for(final int i : CharStats.CODES.BASECODES())
				validStats.add(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(i)));
			final int statCode = CharStats.CODES.findWhole(prompt, false);
			if((statCode < 0)||(!validStats.contains(CMStrings.capitalizeAndLower(CharStats.CODES.NAME(statCode)))))
			{
				session.println(L("^r'@x1' is an unknown code.  Try one of these: @x2^N",prompt,CMParms.toListString(validStats)));
				loginObj.state=LoginState.CHARCR_STATSTART;
				return null;
			}
			if(statPointsChange == 0)
			{
				loginObj.savedInput=loginObj.lastInput;
				session.promptPrint(L("^!How many points to add or remove (ex: +4, -1): "));
				loginObj.state=LoginState.CHARCR_STATPICKADD;
				return LoginResult.INPUT_REQUIRED;
			}
			if(statPointsChange <= 0)
			{
				loginObj.state=LoginState.CHARCR_STATSTART;
				return null;
			}
			final String list = CMProps.getVar(CMProps.Str.STATCOSTS);
			final long[][] costs=CMLib.utensils().compileConditionalRange(CMParms.parseCommas(list.trim(),true), 1, 0, 101);
			int pointsCost=0;
			int curStatValue=CT.getStat(statCode);
			for(int i=0;i<statPointsChange;i++)
			{
				final int statPoint=remove?curStatValue-1:curStatValue;
				int statCost=1;
				if((statPoint>0)&&(statPoint<costs.length)&&(costs[statPoint]!=null)&&(costs[statPoint].length>0)&&(costs[statPoint][0]!=0))
					statCost=(int)costs[statPoint][0];
				pointsCost += remove ? -statCost : statCost;
				curStatValue += remove ? -1 : 1;
			}
			if(loginObj.statPoints - pointsCost < 0)
			{
				if(loginObj.statPoints > 0)
					session.println(L("^rYou need @x1 points to do that, but only have @x2 remaining.^N",""+pointsCost,""+loginObj.statPoints));
				else
					session.println(L("^rYou don't have enough remaining points to do that.^N"));
				loginObj.state=LoginState.CHARCR_STATSTART;
				return null;
			}
			else
			{
				final String friendlyName = CMStrings.capitalizeAndLower(CharStats.CODES.NAME(statCode));
				if(remove)
				{
					if(CT.getStat(statCode) <= loginObj.baseStats.getStat(statCode))
					{
						session.println(L("^rYou can not lower '@x1 any further.^N",friendlyName));
						loginObj.state=LoginState.CHARCR_STATSTART;
						return null;
					}
					else
					if(CT.getStat(statCode)-statPointsChange < loginObj.baseStats.getStat(statCode))
					{
						session.println(L("^rYou can not lower '@x1 any further.^N",friendlyName));
						loginObj.state=LoginState.CHARCR_STATSTART;
						return null;
					}
				}
				else
				{
					final int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
					if(CT.getStat(statCode) >= max)
					{
						session.println(L("^rYou can not raise '@x1 any further.^N",friendlyName));
						loginObj.state=LoginState.CHARCR_STATSTART;
						return null;
					}
					else
					if(CT.getStat(statCode)+statPointsChange > max)
					{
						session.println(L("^rYou can not raise '@x1 any by that amount.^N",friendlyName));
						loginObj.state=LoginState.CHARCR_STATSTART;
						return null;
					}
				}
				if(remove)
					statPointsChange = statPointsChange * -1;
				CT.setStat(statCode, CT.getStat(statCode)+statPointsChange);
				loginObj.statPoints -= pointsCost;
			}
		}
		loginObj.state=LoginState.CHARCR_STATSTART;
		return null;
	}

	protected LoginResult charcrStatDone(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		executeScript(mob,getLoginScripts().get("STATS"));
		return charcrClassInit(loginObj, session);
	}

	protected LoginResult charcrClassInit(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		final List<CharClass> qualClassesV=classQualifies(mob,loginObj.theme);
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)||mob.baseCharStats().getMyRace().classless())
		{
			CharClass newClass=null;
			if(CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
				newClass=CMClass.getCharClass("PlayerClass");
			if((newClass==null)&&(qualClassesV.size()>0))
				newClass=qualClassesV.get(CMLib.dice().roll(1,qualClassesV.size(),-1));
			if(newClass==null)
				newClass=CMClass.getCharClass("PlayerClass");
			if(newClass==null)
				newClass=CMClass.getCharClass("StdCharClass");
			if(newClass==null)
			{
				Log.errOut("CharCreation", "Char Classes are disabled, but no PlayerClass or StdCharClass is defined?!");
				loginObj.state=LoginState.CHARCR_CLASSSTART;
			}
			else
			{
				mob.baseCharStats().setCurrentClass(newClass);
				mob.charStats().setCurrentClass(newClass);
				loginObj.state=LoginState.CHARCR_CLASSDONE;
			}
			return null;
		}
		if(qualClassesV.size()==0)
		{
			CharClass newClass=null;
			for(final Enumeration<CharClass> c=CMClass.charClasses();c.hasMoreElements();)
			{
				final CharClass C=c.nextElement();
				if(C.getSubClassRule()==CharClass.SubClassRule.ANY)
					newClass=C;
			}
			if(newClass==null)
				newClass=CMClass.getCharClass("Apprentice");
			if(newClass==null)
				newClass=CMClass.getCharClass("StdCharClass");
			if(newClass==null)
			{
				Log.errOut("CharCreation", "No classes qualified for, but no Apprentice or StdCharClass is defined?!");
				loginObj.state=LoginState.CHARCR_CLASSSTART;
			}
			else
			{
				mob.baseCharStats().setCurrentClass(newClass);
				mob.charStats().setCurrentClass(newClass);
				loginObj.state=LoginState.CHARCR_CLASSDONE;
			}
			return null;
		}
		else
		if((qualClassesV.size()==1)||(session==null)||(session.isStopped()))
		{
			mob.baseCharStats().setCurrentClass(qualClassesV.get(0));
			mob.charStats().setCurrentClass(qualClassesV.get(0));
			loginObj.state=LoginState.CHARCR_CLASSDONE;
			return null;
		}
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES)
		&&!mob.baseCharStats().getMyRace().classless())
			session.println(null,null,null,new CMFile(Resources.buildResourcePath("text")+"classes.txt",null,CMFile.FLAG_LOGERRORS).text().toString());
		loginObj.state=LoginState.CHARCR_CLASSSTART;
		return null;
	}

	protected LoginResult charcrClassStart(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		mob.baseCharStats().setMyClasses("StdCharClass");
		mob.baseCharStats().setMyLevels("0");
		final List<CharClass> qualClassesV=classQualifies(mob,loginObj.theme);
		final String listOfClasses = buildQualifyingClassList(mob, qualClassesV, "or");
		session.println(L("\n\r^!Please choose from the following Classes:"));
		session.print("^N[" + listOfClasses + "^N]");
		session.promptPrint("\n\r: ");
		loginObj.state=LoginState.CHARCR_CLASSPICKED;
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult charcrClassPicked(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		final String ClassStr=loginObj.lastInput;
		if(ClassStr.trim().equalsIgnoreCase("?"))
		{
			session.println(null,null,null,"\n\r"+new CMFile(Resources.buildResourcePath("text")+"classes.txt",null,CMFile.FLAG_LOGERRORS).text().toString());
			loginObj.state=LoginState.CHARCR_CLASSSTART;
			return null;
		}
		final List<CharClass> qualClassesV=classQualifies(mob,loginObj.theme);
		CharClass newClass=CMClass.findCharClass(ClassStr);
		if(newClass==null)
		{
			for(final CharClass C : qualClassesV)
			{
				if(C.name().equalsIgnoreCase(ClassStr))
				{
					newClass=C;
					break;
				}
			}
		}
		if(newClass==null)
		{
			for(final CharClass C : qualClassesV)
			{
				if(C.name().toUpperCase().startsWith(ClassStr.toUpperCase()))
				{
					newClass=C;
					break;
				}
			}
		}
		if((newClass!=null)&&(canChangeToThisClass(mob,newClass,loginObj.theme)))
		{
			final StringBuilder str=CMLib.help().getHelpText(newClass.ID().toUpperCase(),mob,false,false);
			if(str!=null)
			{
				session.println("\n\r^N"+str.toString()+"\n\r");
			}
			session.promptPrint(L("^NIs ^H@x1^N correct (Y/n)?",newClass.name()));
			mob.baseCharStats().setCurrentClass(newClass);
			mob.charStats().setCurrentClass(newClass);
			loginObj.state=LoginState.CHARCR_CLASSCONFIRM;
			return LoginResult.INPUT_REQUIRED;
		}
		else
		{
			loginObj.state=LoginState.CHARCR_CLASSSTART;
		}
		return null;
	}

	protected LoginResult charcrClassConfirm(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		if(loginObj.lastInput.toUpperCase().trim().startsWith("N"))
			loginObj.state=LoginState.CHARCR_CLASSSTART;
		else
		if(loginObj.lastInput.toUpperCase().trim().startsWith("Y"))
			loginObj.state=LoginState.CHARCR_CLASSDONE;
		else
		{
			session.promptPrint(L("^NIs ^H@x1^N correct (Y/n)?",mob.baseCharStats().getCurrentClass().name()));
			return LoginResult.INPUT_REQUIRED;
		}
		return null;
	}

	protected LoginResult charcrClassDone(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		mob.basePhyStats().setLevel(1);
		mob.baseCharStats().setClassLevel(mob.baseCharStats().getCurrentClass(),1);
		mob.basePhyStats().setSensesMask(0);

		getUniversalStartingItems(loginObj.theme, mob);
		mob.setWimpHitPoint(5);

		CMLib.utensils().outfit(mob,mob.baseCharStats().getMyRace().outfit(mob));

		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.ALLERGIES))
		{
			final Ability A=CMClass.getAbility("Allergies");
			if(A!=null)
				A.invoke(mob,mob,true,0);
		}

		mob.recoverCharStats();
		mob.recoverPhyStats();
		mob.recoverMaxState();
		mob.resetToMaxState();

		executeScript(mob,getLoginScripts().get("CLASS"));

		loginObj.index=-1;
		loginObj.state=LoginState.CHARCR_FACTIONNEXT;
		return null;
	}

	protected LoginResult charcrFactionNext(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		Faction F= null;
		while((F==null)||(CMSecurity.isFactionDisabled(F.factionID())))
		{
			loginObj.index++;
			if(loginObj.index>=CMLib.factions().numFactions())
			{
				loginObj.state=LoginState.CHARCR_FACTIONDONE;
				return null;
			}
			F=CMLib.factions().getFactionByNumber(loginObj.index);
		}
		final List<Integer> mine=F.findChoices(mob);
		final int defaultValue=F.findAutoDefault(mob);
		if(defaultValue!=Integer.MAX_VALUE)
			mob.addFaction(F.factionID(),defaultValue);
		if(mine.size()==1)
		{
			mob.addFaction(F.factionID(),mine.get(0).intValue());
			return null;
		}
		if(mine.size()==0)
		{
			return null;
		}

		if((F.choiceIntro()!=null)&&(F.choiceIntro().length()>0))
		{
			StringBuffer intro = new CMFile(Resources.makeFileResourceName(F.choiceIntro()),null,CMFile.FLAG_LOGERRORS).text();
			try
			{
				intro = CMLib.webMacroFilter().virtualPageFilter(intro);
			}
			catch (final Exception ex)
			{
			}
			session.println(null,null,null,"\n\r\n\r"+intro.toString());
		}
		loginObj.lastInput="";
		loginObj.state=LoginState.CHARCR_FACTIONPICK;
		return null;
	}

	protected LoginResult charcrFactionPick(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		final Faction F=CMLib.factions().getFactionByNumber(loginObj.index);
		if(F==null)
		{
			Log.errOut("CharCreation","Failure in Faction algorithm");
			loginObj.state=LoginState.CHARCR_FACTIONDONE;
			return null;
		}
		final List<Integer> mine=F.findChoices(mob);
		final StringBuffer menu=new StringBuffer("Select one: ");
		final List<String> namedChoices=getNamedFactionChoices(F, mine);
		String choice=loginObj.lastInput.toUpperCase().trim();
		if(choice.length()>0)
		{
			if(!namedChoices.contains(choice))
			{
				for(int i=0;i<namedChoices.size();i++)
				{
					if(namedChoices.get(i).startsWith(choice.toUpperCase()))
					{
						choice = namedChoices.get(i);
						break;
					}
				}
			}
			if(!namedChoices.contains(choice))
			{
				for(int i=0;i<namedChoices.size();i++)
				{
					if(namedChoices.get(i).indexOf(choice.toUpperCase())>=0)
					{
						choice = namedChoices.get(i);
						break;
					}
				}
			}
		}
		if(namedChoices.contains(choice))
		{
			final int valueIndex=namedChoices.indexOf(choice);
			if(valueIndex>=0)
				mob.addFaction(F.factionID(),mine.get(valueIndex).intValue());
			loginObj.state=LoginState.CHARCR_FACTIONNEXT;
			return null;
		}
		for(final String menuChoice : namedChoices)
			menu.append(menuChoice.toLowerCase()+", ");
		loginObj.lastInput="";
		session.promptPrint(menu.toString().substring(0,menu.length()-2)+".\n\r: ");
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult charcrFactionDone(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		executeScript(mob,getLoginScripts().get("FACTIONS"));

		mob.baseCharStats().getCurrentClass().startCharacter(mob,false,false);
		CMLib.utensils().outfit(mob,mob.baseCharStats().getCurrentClass().outfit(mob));
		mob.setStartRoom(getDefaultStartRoom(mob));
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.ALL_AGEING))
		{
			final Race R=mob.baseCharStats().getMyRace();
			final Room startR=mob.getStartRoom();
			final TimeClock C=CMLib.time().localClock(startR);
			final PlayerStats pStats=mob.playerStats();
			final int age=pStats.initializeBirthday(C,0,R);
			mob.baseCharStats().setStat(CharStats.STAT_AGE,age);
		}
		final String startingMoney=mob.baseCharStats().getCurrentClass().getStartingMoney();
		if((startingMoney!=null)&&(startingMoney.trim().length()>0))
		{
			String currency=CMLib.english().numPossibleGoldCurrency(mob,startingMoney);
			if(currency.length()==0)
				currency=CMLib.beanCounter().getCurrency(mob);
			final double denomination=CMLib.english().numPossibleGoldDenomination(null,currency,startingMoney);
			final long num=CMLib.english().numPossibleGold(null,startingMoney);
			if((num>0)&&(denomination>0.0)&&(currency!=null))
				CMLib.beanCounter().giveSomeoneMoney(mob, currency, denomination * num);
		}

		StringBuffer introText=new CMFile(Resources.buildResourcePath("text")+"newchardone.txt",null,CMFile.FLAG_LOGERRORS).text();
		try
		{
			introText = CMLib.webMacroFilter().virtualPageFilter(introText);
		}
		catch (final Exception ex)
		{
		}
		session.println(null,null,null,"\n\r\n\r"+introText.toString());
		loginObj.state=LoginState.CHARCR_FINISH;
		return LoginResult.INPUT_REQUIRED;
	}

	protected LoginResult charcrFinish(final LoginSessionImpl loginObj, final Session session)
	{
		final MOB mob=loginObj.mob;
		final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
		if(emailPassword && (loginObj.acct==null))
		{
			final String password=CMLib.encoder().generateRandomPassword();
			mob.playerStats().setPassword(password);
			CMLib.database().DBUpdatePassword(mob.Name(),mob.playerStats().getPasswordStr());
			CMLib.smtp().emailOrJournal(CMProps.getVar(CMProps.Str.SMTPSERVERNAME), mob.Name(), "noreply@"+CMProps.getVar(CMProps.Str.MUDDOMAIN).toLowerCase(), mob.Name(),
				L("Password for @x1",mob.Name()),
				L("Your password for @x1 is: @x2\n\r"
				+ "You can login by pointing your mud client at @x3 port(s): @x4.\n\r"
				+ "You may use the PASSWORD command to change it once you are online.",
				mob.Name(),password,CMProps.getVar(CMProps.Str.MUDDOMAIN),CMProps.getVar(CMProps.Str.MUDPORTS)));
			session.println(L("Your character has been created.  You will receive an email with your password shortly."));
			CMLib.s_sleep(1000);
			if(mob==session.mob())
				session.stopSession(false,false,false);
		}
		else
		{
			if(mob==session.mob())
				reloadTerminal(mob);
			mob.bringToLife(mob.getStartRoom(),true);
			mob.location().showOthers(mob,mob.location(),CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,L("<S-NAME> appears!"));
		}
		CMLib.achievements().loadAccountAchievements(mob,AchievementLoadFlag.CHARCR_POSTLOAD);
		mob.playerStats().leveledDateTime(0);
		CMLib.database().DBCreateCharacter(mob);
		CMLib.players().addPlayer(mob);

		executeScript(mob,getLoginScripts().get("END"));

		mob.playerStats().setLastIP(session.getAddress());
		Log.sysOut("Created user: "+mob.Name());
		CMProps.addNewUserByIP(session.getAddress());
		notifyFriends(mob,L("^X@x1 has just been created.^.^?",mob.Name()));
		if((CMProps.getVar(CMProps.Str.PKILL).startsWith("ALWAYS"))
		&&(!mob.isAttributeSet(MOB.Attrib.PLAYERKILL)))
			mob.setAttribute(MOB.Attrib.PLAYERKILL,true);
		if((CMProps.getVar(CMProps.Str.PKILL).startsWith("NEVER"))
		&&(mob.isAttributeSet(MOB.Attrib.PLAYERKILL)))
			mob.setAttribute(MOB.Attrib.PLAYERKILL,false);
		CMLib.database().DBUpdatePlayer(mob);
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.NEWPLAYERS);
		for(int i=0;i<channels.size();i++)
			CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has just been created.",mob.Name()),true);
		CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_NEWPLAYERS);
		if(isExpired(mob.playerStats().getAccount(),session,mob))
		{
			if(loginObj.acct!=null)
				loginObj.state=LoginState.ACCTMENU_START;
			mob.setSession(null);
			session.setMob(null);
			return LoginResult.NO_LOGIN;
		}
		CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
		session.setMob(mob);
		mob.setSession(session);
		try
		{
			if((loginObj.acct!=null)&&(completePlayerLogin(session, false)!=LoginResult.NORMAL_LOGIN))
			{
				session.setMob(null);
				loginObj.state=LoginState.ACCTMENU_SHOWMENU;
				return null;
			}
		}
		catch (IOException e)
		{
			Log.errOut(e);
			loginObj.state=LoginState.ACCTMENU_SHOWMENU;
			return null;
		}
		return LoginResult.NORMAL_LOGIN;
	}

	protected List<String> getNamedFactionChoices(Faction F, List<Integer> mine)
	{
		final Vector<String> namedChoices=new Vector<String>();
		for(int m=0;m<mine.size();m++)
		{
			final Faction.FRange FR=CMLib.factions().getRange(F.factionID(),mine.get(m).intValue());
			if(FR!=null)
				namedChoices.addElement(FR.name().toUpperCase());
			else
				namedChoices.addElement(""+mine.get(m).intValue());
		}
		return namedChoices;
	}

	public NewCharNameCheckResult finishNameCheck(String login, String ipAddress)
	{
		if((CMProps.getIntVar(CMProps.Int.MUDTHEME)==0)
		||((CMSecurity.isDisabled(CMSecurity.DisFlag.LOGINS))
			&&(!CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, login))
			&&(!CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, ipAddress))))
				return NewCharNameCheckResult.NO_NEW_LOGINS;
		else
		if((CMProps.getIntVar(CMProps.Int.MAXNEWPERIP)>0)
		&&(CMProps.getCountNewUserByIP(ipAddress)>=CMProps.getIntVar(CMProps.Int.MAXNEWPERIP))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAXNEWPERIP))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.NEWPLAYERS, login))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.NEWPLAYERS, ipAddress)))
			return NewCharNameCheckResult.CREATE_LIMIT_REACHED;
		return NewCharNameCheckResult.OK;
	}
	
	@Override
	public boolean performSpamConnectionCheck(final String address)
	{
		int proceed=0;
		if(CMSecurity.isBanned(address))
			proceed=1;
		int numAtThisAddress=0;
		final long LastConnectionDelay=(5*60*1000);
		boolean anyAtThisAddress=false;
		final int maxAtThisAddress=6;
		if(!CMSecurity.isDisabled(CMSecurity.DisFlag.CONNSPAMBLOCK))
		{
			if(!CMProps.isOnWhiteList(CMProps.WhiteList.CONNS, address))
			{
				if(CMSecurity.isIPBlocked(address))
				{
					proceed = 1;
				}
				else
				{
					@SuppressWarnings("unchecked")
					List<Triad<String,Long,Integer>> accessed= (LinkedList<Triad<String,Long,Integer>>)Resources.staticInstance()._getResource("SYSTEM_IPACCESS_STATS");
					if(accessed == null)
					{
						accessed= new LinkedList<Triad<String,Long,Integer>>();
						Resources.staticInstance()._submitResource("SYSTEM_IPACCESS_STATS",accessed);
					}
					synchronized(accessed)
					{
						for(final Iterator<Triad<String,Long,Integer>> i=accessed.iterator();i.hasNext();)
						{
							final Triad<String,Long,Integer> triad=i.next();
							if((triad.second.longValue()+LastConnectionDelay)<System.currentTimeMillis())
								i.remove();
							else
							if(triad.first.trim().equalsIgnoreCase(address))
							{
								anyAtThisAddress=true;
								triad.second=Long.valueOf(System.currentTimeMillis());
								numAtThisAddress=triad.third.intValue()+1;
								triad.third=Integer.valueOf(numAtThisAddress);
							}
						}
						if(!anyAtThisAddress)
							accessed.add(new Triad<String,Long,Integer>(address,Long.valueOf(System.currentTimeMillis()),Integer.valueOf(1)));
					}
				}
				@SuppressWarnings("unchecked")
				Set<String> autoblocked= (Set<String>)Resources.staticInstance()._getResource("SYSTEM_IPACCESS_AUTOBLOCK");
				if(autoblocked == null)
				{
					autoblocked= new TreeSet<String>();
					Resources.staticInstance()._submitResource("SYSTEM_IPACCESS_AUTOBLOCK",autoblocked);
				}
				if(autoblocked.contains(address.toUpperCase()))
				{
					if(!anyAtThisAddress)
						autoblocked.remove(address.toUpperCase());
					else
						proceed=2;
				}
				else
				if(numAtThisAddress>=maxAtThisAddress)
				{
					autoblocked.add(address.toUpperCase());
					proceed=2;
				}
			}
		}
		return proceed == 0;
	}

	@Override
	public NewCharNameCheckResult newCharNameCheck(String login, String ipAddress, boolean skipAccountNameCheck)
	{
		final boolean accountSystemEnabled = CMProps.isUsingAccountSystem();

		if(((CMSecurity.isDisabled(CMSecurity.DisFlag.NEWPLAYERS)&&(!accountSystemEnabled))
			||(CMSecurity.isDisabled(CMSecurity.DisFlag.NEWCHARACTERS)))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.NEWPLAYERS, login))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.NEWPLAYERS, ipAddress)))
			return NewCharNameCheckResult.NO_NEW_PLAYERS;
		else
		if((!isOkName(login,false))
		|| (CMLib.players().playerExists(login))
		|| (!skipAccountNameCheck && CMLib.players().accountExists(login)))
			return NewCharNameCheckResult.BAD_USED_NAME;
		else
			return finishNameCheck(login,ipAddress);
	}

	@Override
	public NewCharNameCheckResult newAccountNameCheck(String login, String ipAddress)
	{
		if((CMSecurity.isDisabled(CMSecurity.DisFlag.NEWPLAYERS))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.NEWPLAYERS, login))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.NEWPLAYERS, ipAddress)))
			return NewCharNameCheckResult.NO_NEW_PLAYERS;
		else
		if((!isOkName(login,false))
		|| (CMLib.players().playerExists(login))
		|| (CMLib.players().accountExists(login)))
			return NewCharNameCheckResult.BAD_USED_NAME;
		else
			return finishNameCheck(login,ipAddress);
	}

	public boolean newCharactersAllowed(String login, Session session, PlayerAccount acct, boolean skipAccountNameCheck)
	{
		switch(newCharNameCheck(login,session.getAddress(),skipAccountNameCheck))
		{
		case NO_NEW_PLAYERS:
			session.println(L("\n\r'@x1' is not recognized.",CMStrings.capitalizeAndLower(login)));
			return false;
		case BAD_USED_NAME:
			session.println(L("\n\r'@x1' is not recognized.\n\rThat name is also not available for new players.\n\r  Choose another name (no spaces allowed)!\n\r",CMStrings.capitalizeAndLower(login)));
			return false;
		case NO_NEW_LOGINS:
			session.println(L("\n\r'@x1' does not exist.\n\rThis server is not accepting new accounts.\n\r",CMStrings.capitalizeAndLower(login)));
			return false;
		case CREATE_LIMIT_REACHED:
			session.println(L("\n\rThat name is unrecognized.\n\rAlso, the maximum daily new player limit has already been reached for your location."));
			return false;
		default:
			session.println(L("\n\r'@x1' is not recognized.",CMStrings.capitalizeAndLower(login)));
			return false;
		case OK:
			if(acct!=null)
			{
				int maxPlayersOnAccount = CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM);
				if(maxPlayersOnAccount < Integer.MAX_VALUE)
					maxPlayersOnAccount += acct.getBonusCharsLimit();
				if((maxPlayersOnAccount<=acct.numPlayers())
				&&(!acct.isSet(PlayerAccount.AccountFlag.NUMCHARSOVERRIDE)))
				{
					session.println(L("You may only have @x1 characters.  Please retire one to create another.",""+maxPlayersOnAccount));
					return false;
				}
			}
			return true;
		}
	}

	public boolean newAccountsAllowed(String login, Session session, PlayerAccount acct)
	{
		switch(newAccountNameCheck(login,session.getAddress()))
		{
		case NO_NEW_PLAYERS:
			session.println(L("\n\r'@x1' is not recognized.",CMStrings.capitalizeAndLower(login)));
			return false;
		case BAD_USED_NAME:
			session.println(L("\n\r'@x1' is not recognized.\n\rThat name is also not available for new accounts.\n\r  Choose another name (no spaces allowed)!\n\r",CMStrings.capitalizeAndLower(login)));
			return false;
		case NO_NEW_LOGINS:
			session.println(L("\n\r'@x1' does not exist.\n\rThis server is not accepting new accounts.\n\r",CMStrings.capitalizeAndLower(login)));
			return false;
		case CREATE_LIMIT_REACHED:
			session.println(L("\n\rThat name is unrecognized.\n\rAlso, the maximum daily new account limit has already been reached for your location."));
			return false;
		default:
			session.println(L("\n\r'@x1' is not recognized.",CMStrings.capitalizeAndLower(login)));
			return false;
		case OK:
		{
			if(acct!=null)
			{
				int maxPlayersOnAccount = CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM);
				if(maxPlayersOnAccount < Integer.MAX_VALUE)
					maxPlayersOnAccount += acct.getBonusCharsLimit();
				if((maxPlayersOnAccount<=acct.numPlayers())
				&&(!acct.isSet(PlayerAccount.AccountFlag.NUMCHARSOVERRIDE)))
				{
					session.println(L("You may only have @x1 characters.  Please retire one to create another.",""+maxPlayersOnAccount));
					return false;
				}
			}
			return true;
		}
		}
	}

	public void setGlobalBitmaps(MOB mob)
	{
		if(mob==null)
			return;
		final List<String> defaultFlagsV=CMParms.parseCommas(CMProps.getVar(CMProps.Str.DEFAULTPLAYERFLAGS).toUpperCase(),true);
		for(int v=0;v<defaultFlagsV.size();v++)
		{
			final String flagName = defaultFlagsV.get(v); 
			for(MOB.Attrib a : MOB.Attrib.values())
			{
				if(a.getName().equals(flagName) || a.name().equals(flagName))
					mob.setAttribute(a,true);
			}
		}
	}

	@Override
	public LoginResult finishLogin(final Session session, final MOB mob, final Room startRoom, final boolean resetStats) throws IOException
	{
		if(loginsDisabled(mob))
			return LoginResult.NO_LOGIN;
		mob.bringToLife(startRoom,resetStats);
		CMLib.coffeeTables().bump(mob,CoffeeTableRow.STAT_LOGINS);
		mob.location().showOthers(mob,startRoom,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,L("<S-NAME> appears!"));
		for(int f=0;f<mob.numFollowers();f++)
		{
			final MOB follower=mob.fetchFollower(f);
			if(follower==null)
				continue;
			Room R=follower.location();
			if((follower.isMonster())
			&&(!follower.isPossessing())
			&&(!CMLib.flags().isInTheGame(follower, true))
			&&((R==null)||(!R.isInhabitant(follower))))
			{
				if(R==null)
					R=mob.location();
				follower.setLocation(R);
				follower.setFollowing(mob); // before for bestow names sake
				follower.bringToLife(R,false);
				follower.setFollowing(mob);
				R.showOthers(follower,R,CMMsg.MASK_ALWAYS|CMMsg.MSG_ENTER,L("<S-NAME> appears!"));
			}
		}
		@SuppressWarnings("unchecked")
		List<Triad<String,Long,Integer>> accessed= (LinkedList<Triad<String,Long,Integer>>)Resources.staticInstance()._getResource("SYSTEM_IPACCESS_STATS");
		if(accessed != null)
		{
			synchronized(accessed)
			{
				for(final Iterator<Triad<String,Long,Integer>> i=accessed.iterator();i.hasNext();)
				{
					final Triad<String,Long,Integer> triad=i.next();
					if(triad.first.equals(session.getAddress()))
					{
						i.remove();
						break;
					}
				}
			}
		}
		final PlayerStats pstats = mob.playerStats();
		if(((pstats.getEmail()==null)||(pstats.getEmail().length()==0))
		&&(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION")))
		{
			final Command C=CMClass.getCommand("Email");
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
			session.println(L("\n\rYou are unwelcome.  No one likes you here. Go away.\n\r\n\r"));
			session.stopSession(false,false,false);
			return LoginResult.NO_LOGIN;
		}
		if(mob.playerStats()!=null)
			mob.playerStats().setLastIP(session.getAddress());
		notifyFriends(mob,L("^X@x1 has logged on.^.^?",mob.Name()));
		if((CMProps.getVar(CMProps.Str.PKILL).startsWith("ALWAYS"))
		&&(!mob.isAttributeSet(MOB.Attrib.PLAYERKILL)))
			mob.setAttribute(MOB.Attrib.PLAYERKILL,true);
		if((CMProps.getVar(CMProps.Str.PKILL).startsWith("NEVER"))
		&&(mob.isAttributeSet(MOB.Attrib.PLAYERKILL)))
			mob.setAttribute(MOB.Attrib.PLAYERKILL,false);
		final List<String> channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.LOGINS);
		if(!CMLib.flags().isCloaked(mob))
		{
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.get(i),mob.clans(),L("@x1 has logged on.",mob.Name()),true);
		}
		setGlobalBitmaps(mob);
		return LoginResult.NORMAL_LOGIN;
	}
	
	public LoginResult completeCharacterLogin(Session session, String login, boolean wiziFlag) throws IOException
	{
		// count number of multiplays
		int numAtAddress=0;
		try
		{
			for(final Session S : CMLib.sessions().allIterable())
			{
				if((S!=session)&&(session.getAddress().equalsIgnoreCase(S.getAddress())))
					numAtAddress++;
			}
		}
		catch(final Exception e)
		{
		}

		if((CMProps.getIntVar(CMProps.Int.MAXCONNSPERIP)>0)
		&&(numAtAddress>=CMProps.getIntVar(CMProps.Int.MAXCONNSPERIP))
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.MAXCONNSPERIP))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.CONNS, session.getAddress()))
		&&(!CMProps.isOnWhiteList(CMProps.WhiteList.LOGINS, login)))
		{
			session.println(L("The maximum player limit has already been reached for your IP address."));
			return LoginResult.NO_LOGIN;
		}

		final boolean resetStats;
		Room startRoom;
		MOB mob=CMLib.players().getPlayer(login);
		if((mob!=null)&&(mob.session()!=null))
		{
			session.setMob(mob);
			mob.setSession(session);
			if(isExpired(mob.playerStats().getAccount(),session,mob))
			{
				return LoginResult.NO_LOGIN;
			}
			if(loginsDisabled(mob))
				return LoginResult.NO_LOGIN;
			if(wiziFlag)
			{
				final Command C=CMClass.getCommand("WizInv");
				if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
					C.execute(mob,new XVector<String>("WIZINV"),0);
			}
			showTheNews(mob);
			resetStats = false;
		}
		else
		{
			resetStats=(mob==null);
			if(resetStats)
				mob=CMLib.players().getLoadPlayer(login);
			if(mob == null)
			{
				Log.errOut("CharCreation",login+" does not exist! FAIL!");
				return LoginResult.NO_LOGIN;
			}
			if(mob.playerStats()==null)
			{
				Log.errOut("CharCreation",login+" is not a player! FAIL!");
				session.println(L("Error occurred trying to login as this player. Please contact your technical support."));
				return LoginResult.NO_LOGIN;
			}
			mob.setSession(session);
			session.setMob(mob);
			if(isExpired(mob.playerStats().getAccount(),session,mob))
				return LoginResult.NO_LOGIN;
			if(loginsDisabled(mob))
				return LoginResult.NO_LOGIN;
			if(wiziFlag)
			{
				final Command C=CMClass.getCommand("WizInv");
				if((C!=null)&&(C.securityCheck(mob)||C.securityCheck(mob)))
					C.execute(mob,new XVector<String>("WIZINV"),0);
			}
			showTheNews(mob);
		}
		startRoom = CMLib.map().getRoom(mob.location());
		if(startRoom==null)
		{
			Log.debugOut("CharCreation",mob.name()+" has no/lost location.. sending to start room");
			startRoom = CMLib.map().getRoom(mob.getStartRoom());
			if(startRoom == null)
				startRoom = CMLib.map().getStartRoom(mob);

		}
		return this.finishLogin(session, mob, startRoom, resetStats);
	}

	@Override
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
		final String align=CMLib.flags().getAlignmentName(mob);
		
		String roomID=null;
		if((startRoomMasks.size()>0)&&(mob.isPlayer()))
		{
			for(Pair<CompiledZMask,String> p : startRoomMasks)
			{
				if(CMLib.masking().maskCheck(p.first, mob, true))
				{
					roomID=p.second;
					break;
				}
			}
		}
		
		if((roomID==null)||(roomID.length()==0))
			roomID=startRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=startRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=startRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=startRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
			final List<String> V=mob.fetchFactionRanges();
			for(int v=0;v<V.size();v++)
			{
				if(startRooms.containsKey(V.get(v).toUpperCase()))
				{
					roomID = startRooms.get(V.get(v).toUpperCase());
					break;
				}
			}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=startRooms.get(deity);

		if(((roomID==null)||(roomID.length()==0)))
			roomID=startRooms.get(""+mob.phyStats().level());

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

	@Override
	public Room getDefaultDeathRoom(MOB mob)
	{
		String charClass=mob.baseCharStats().getCurrentClass().ID().toUpperCase();
		charClass=charClass.replace(' ','_');
		String race=mob.baseCharStats().getMyRace().racialCategory().toUpperCase();
		race=race.replace(' ','_');
		String deity=mob.getWorshipCharID().toUpperCase();
		deity=deity.replace(' ','_');
		final String align=CMLib.flags().getAlignmentName(mob);
		
		String roomID=null;
		if((deathRoomMasks.size()>0)
		&&(mob.isPlayer()))
		{
			for(Pair<CompiledZMask,String> p : deathRoomMasks)
			{
				if(CMLib.masking().maskCheck(p.first, mob, true))
				{
					roomID=p.second;
					break;
				}
			}
		}
		
		if(((roomID==null)||(roomID.length()==0)))
			roomID=deathRooms.get(race);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=deathRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=deathRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
			final List<String> V=mob.fetchFactionRanges();
			for(int v=0;v<V.size();v++)
			{
				if(deathRooms.containsKey(V.get(v).toUpperCase()))
				{
					roomID = deathRooms.get(V.get(v).toUpperCase());
					break;
				}
			}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=deathRooms.get(deity);

		if(((roomID==null)||(roomID.length()==0)))
			roomID=deathRooms.get(""+mob.phyStats().level());
		
		if((roomID==null)||(roomID.length()==0))
			roomID=deathRooms.get("ALL");

		if((roomID!=null)&&(roomID.equalsIgnoreCase("MORGUE")))
			return getDefaultBodyRoom(mob);
		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("HERE")))
			room=mob.location();
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

	@Override
	public Room getDefaultBodyRoom(MOB mob)
	{
		final Pair<Clan,Integer> clanMorgue=CMLib.clans().findPrivilegedClan(mob, Clan.Function.MORGUE);
		if((clanMorgue!=null)&&((!mob.isMonster())||(mob.getStartRoom()==null)))
		{
			final Clan C=clanMorgue.first;
			if(C.getMorgue().length()>0)
			{
				final Room room=CMLib.map().getRoom(C.getMorgue());
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
		final String align=CMLib.flags().getAlignmentName(mob);
		
		String roomID=null;
		if((bodyRoomMasks.size()>0)&&(mob.isPlayer()))
		{
			for(Pair<CompiledZMask,String> p : bodyRoomMasks)
			{
				if(CMLib.masking().maskCheck(p.first, mob, true))
				{
					roomID=p.second;
					break;
				}
			}
		}
		
		if((roomID==null)||(roomID.length()==0))
			roomID=bodyRooms.get(race);
		if((roomID==null)||(roomID.length()==0))
			roomID=bodyRooms.get(realrace);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=bodyRooms.get(align);
		if(((roomID==null)||(roomID.length()==0)))
			roomID=bodyRooms.get(charClass);
		if(((roomID==null)||(roomID.length()==0)))
		{
			final List<String> V=mob.fetchFactionRanges();
			for(int v=0;v<V.size();v++)
			{
				if(bodyRooms.containsKey(V.get(v).toUpperCase()))
				{
					roomID = bodyRooms.get(V.get(v).toUpperCase());
					break;
				}
			}
		}
		if(((roomID==null)||(roomID.length()==0))&&(deity.length()>0))
			roomID=bodyRooms.get(deity);
		
		if(((roomID==null)||(roomID.length()==0)))
			roomID=bodyRooms.get(""+mob.phyStats().level());
		
		if((roomID==null)||(roomID.length()==0))
			roomID=bodyRooms.get("ALL");

		Room room=null;
		if((roomID!=null)&&(roomID.equalsIgnoreCase("START")))
			room=mob.getStartRoom();
		if((roomID!=null)&&(roomID.equalsIgnoreCase("HERE")))
			room=mob.location();
		if((roomID!=null)&&(roomID.equalsIgnoreCase("DEATH")))
			room=getDefaultDeathRoom(mob);
		if((room==null)&&(roomID!=null)&&(roomID.length()>0))
			room=CMLib.map().getRoom(roomID);
		if(room==null)
			room=mob.location();
		if((room==null)&&(CMLib.map().numRooms()>0))
			room=CMLib.map().rooms().nextElement();
		return room;
	}

	@Override
	public int getTrainingCost(MOB mob, int abilityCode, boolean quiet)
	{
		final int curStat=mob.baseCharStats().getRacialStat(mob, abilityCode);
		final String list = CMProps.getVar(CMProps.Str.STATCOSTS);
		final int maxStat = CMProps.getIntVar(CMProps.Int.BASEMAXSTAT)+mob.charStats().getStat(CharStats.CODES.toMAXBASE(abilityCode));
		final long[][] costs=CMLib.utensils().compileConditionalRange(CMParms.parseCommas(list.trim(),true), 1, 0, maxStat+10);
		int curStatIndex=curStat;
		while((curStatIndex>0)
		&&((curStatIndex>=costs.length)||(costs[curStatIndex]==null)||(costs[curStatIndex].length==0)))
			curStatIndex--;
		int val=1;
		if(curStatIndex>0)
			val=(int)costs[curStatIndex][0];
		if((curStat>=maxStat)&&(!quiet))
		{
			mob.tell(L("You cannot train that any further."));
			if(val<=0)
				val=1;
			return -val;
		}
		return val;
	}

	protected void pageRooms(CMProps page, Map<String, String> table, PairList<CompiledZMask,String> masks, String start)
	{
		List<Integer> ints=new ArrayList<Integer>();
		for(final Enumeration<Object> i=page.keys();i.hasMoreElements();)
		{
			final String k=(String)i.nextElement();
			if(k.startsWith(start+"_"))
			{
				final String kr=k.substring(start.length()+1);
				if(kr.startsWith("MASK"))
				{
					final String m=page.getProperty(k);
					int x=m.indexOf('=');
					if(x<0)
						Log.errOut("INI Entry '"+k+m+"' is malformed!");
					else
						masks.add(CMLib.masking().maskCompile(m.substring(x+1).trim()), m.substring(0,x).trim());
				}
				else
				if(CMath.isInteger(kr))
					ints.add(Integer.valueOf(CMath.s_int(kr)));
				else
					table.put(kr,page.getProperty(k));
			}
		}
		Collections.sort(ints);
		int lastPlayerLevel = CMath.s_int(page.getProperty(Int.LASTPLAYERLEVEL.name()))+10;
		for(int i=ints.size()-1;i>=0;i--)
		{
			final Integer I=ints.get(i);
			final String k=start+"_"+I.toString();
			final int upTo=(lastPlayerLevel > I.intValue()) ? lastPlayerLevel : (I.intValue()+1);
			if(page.containsKey(k))
			{
				for(int lvl=I.intValue();lvl<upTo;lvl++)
				{
					if(!table.containsKey(""+lvl))
						table.put(""+lvl,page.getProperty(k));
				}
			}
		}
		final String thisOne=page.getProperty(start);
		if((thisOne!=null)&&(thisOne.length()>0))
			table.put("ALL",thisOne);
	}

	@Override
	public void initStartRooms(CMProps page)
	{
		startRooms=new Hashtable<String,String>();
		startRoomMasks=new PairVector<CompiledZMask,String>();
		pageRooms(page,startRooms,startRoomMasks,"START");
	}

	@Override
	public void initDeathRooms(CMProps page)
	{
		deathRooms=new Hashtable<String,String>();
		deathRoomMasks=new PairVector<CompiledZMask,String>();
		pageRooms(page,deathRooms,deathRoomMasks,"DEATH");
	}

	@Override
	public void initBodyRooms(CMProps page)
	{
		bodyRooms=new Hashtable<String,String>();
		bodyRoomMasks=new PairVector<CompiledZMask,String>();
		pageRooms(page,bodyRooms,bodyRoomMasks,"MORGUE");
	}

	@Override
	public boolean shutdown()
	{
		bodyRooms=new Hashtable<String,String>();
		startRooms=new Hashtable<String,String>();
		deathRooms=new Hashtable<String,String>();
		return true;
	}

	@Override
	public void promptBaseCharStats(int theme, MOB mob, int timeoutSecs, Session session, int bonusPoints) throws IOException
	{
		final LoginSessionImpl loginObj=new LoginSessionImpl();
		if(mob.playerStats()!=null)
			loginObj.acct=mob.playerStats().getAccount();
		loginObj.login=mob.Name();
		loginObj.mob=mob;
		loginObj.theme=theme;
		LoginResult res=charcrStatInit(loginObj, session, bonusPoints);
		while(!session.isStopped())
		{
			if(res==LoginResult.INPUT_REQUIRED)
				loginObj.lastInput=session.blockingIn(timeoutSecs*1000, true);
			if(loginObj.state==LoginState.CHARCR_STATDONE)
				return;
			if(loginObj.state.toString().startsWith("CHARCR_STAT"))
				res=loginSubsystem(loginObj, session);
			else
				return;
		}
	}

	@Override
	public CharClass promptCharClass(int theme, MOB mob, Session session) throws IOException
	{
		final LoginSessionImpl loginObj=new LoginSessionImpl();
		final PlayerStats pStats=mob.playerStats();
		if(pStats!=null)
			loginObj.acct=pStats.getAccount();
		loginObj.login=mob.Name();
		loginObj.mob=mob;
		loginObj.theme=theme;
		LoginResult res=charcrClassInit(loginObj, session);
		while((session!=null)&&(!session.isStopped()))
		{
			if(res==LoginResult.INPUT_REQUIRED)
				loginObj.lastInput=session.blockingIn(90000, true);
			if(loginObj.state==LoginState.CHARCR_CLASSDONE)
				return mob.baseCharStats().getCurrentClass();
			if(loginObj.state.toString().startsWith("CHARCR_CLASS"))
				res=loginSubsystem(loginObj, session);
			else
				return mob.baseCharStats().getCurrentClass();
		}
		return mob.baseCharStats().getCurrentClass();
	}

	@Override
	public Race promptRace(int theme, MOB mob, Session session) throws IOException
	{
		final LoginSessionImpl loginObj=new LoginSessionImpl();
		final PlayerStats pStats=mob.playerStats();
		if(pStats!=null)
			loginObj.acct=pStats.getAccount();
		loginObj.login=mob.Name();
		loginObj.mob=mob;
		loginObj.theme=theme;
		LoginResult res=charcrRaceStart(loginObj, session);
		while((session!=null)&&(!session.isStopped()))
		{
			if(res==LoginResult.INPUT_REQUIRED)
				loginObj.lastInput=session.blockingIn(90000, true);
			if(loginObj.state==LoginState.CHARCR_RACEDONE)
				return mob.baseCharStats().getMyRace();
			if(loginObj.state.toString().startsWith("CHARCR_RACE"))
				res=loginSubsystem(loginObj, session);
			else
				return mob.baseCharStats().getMyRace();
		}
		return mob.baseCharStats().getMyRace();
	}

	@Override
	public char promptGender(int theme, MOB mob, Session session) throws IOException
	{
		final LoginSessionImpl loginObj=new LoginSessionImpl();
		final PlayerStats pStats=mob.playerStats();
		if(pStats!=null)
			loginObj.acct=pStats.getAccount();
		loginObj.login=mob.Name();
		loginObj.mob=mob;
		loginObj.theme=theme;
		LoginResult res=charcrGenderStart(loginObj, session);
		while((session!=null)&&(!session.isStopped()))
		{
			if(res==LoginResult.INPUT_REQUIRED)
				loginObj.lastInput=session.blockingIn(90000, true);
			if(loginObj.state==LoginState.CHARCR_GENDERDONE)
				return (char)mob.baseCharStats().getStat(CharStats.STAT_GENDER);
			if(loginObj.state.toString().startsWith("CHARCR_GENDER"))
				res=loginSubsystem(loginObj, session);
			else
				return (char)mob.baseCharStats().getStat(CharStats.STAT_GENDER);
		}
		return (char)mob.baseCharStats().getStat(CharStats.STAT_GENDER);
	}

	@Override
	public LoginResult createCharacter(String login, Session session) throws IOException
	{
		final SessionStatus status=session.getStatus();
		final LoginSessionImpl loginObj=new LoginSessionImpl();
		loginObj.acct=null;
		loginObj.login=login;
		loginObj.mob=null;
		final MOB prevMOB=session.mob();
		LoginResult res=charcrStart(loginObj, session);
		try
		{
			while(!session.isStopped())
			{
				if(res==LoginResult.INPUT_REQUIRED)
					loginObj.lastInput=session.blockingIn(90000, true);
				if((res==LoginResult.NORMAL_LOGIN)||(res==LoginResult.NO_LOGIN))
					return res;
				if(loginObj.state.toString().startsWith("CHARCR_"))
					res=loginSubsystem(loginObj, session);
				else
					return res;
			}
			return LoginResult.NO_LOGIN;
		}
		finally
		{
			if(prevMOB!=null)
			{
				if((session.mob()!=null) && (session.mob()!=prevMOB))
					session.mob().setSession(null);
				session.setMob(prevMOB);
				prevMOB.setSession(session);
			}
			session.setStatus(status);
		}
	}

	@SuppressWarnings("unchecked")
	protected Pair<String,Integer>[] makeRandomNameSets(final String rawData)
	{
		final List<Pair<String,Integer>> set=new ArrayList<Pair<String,Integer>>();
		for(int i=0;i<rawData.length();i++)
		{
			final int start=i;
			while((i+1<rawData.length())&&(!Character.isDigit(rawData.charAt(i))))
				i++;
			set.add(new Pair<String,Integer>(rawData.substring(start,i),Integer.valueOf(rawData.substring(i,i+1))));
		}
		return set.toArray(new Pair[0]);
	}

	protected Pair<String,Integer>[] getRandomVowels()
	{
		if(randomNameVowels == null)
		{
			randomNameVowels=makeRandomNameSets("a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7"
					+"a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7a7e7i7o7u7ae7ai7ao7au7aa7ea7eo7eu7ee7eau7ia7io7iu7ii7oa7oe7oi7ou7oo7'4y7ay7ay7ei7ei7ei7ua7ua7");
		}
		return randomNameVowels;
	}

	protected Pair<String,Integer>[] getRandomConsonants()
	{
		if(randomNameConsonants == null)
		{
			randomNameConsonants=makeRandomNameSets("b7c7d7f7g7h7j7k7l7m7n7p7qu6r7s7t7v7w7x7y7z7sc7ch7gh7ph7sh7th7wh6ck5nk5rk5sk7wk0cl6fl6gl6kl6ll6pl6sl6"
					+"br6cr6dr6fr6gr6kr6pr6sr6tr6ss5st7str6b7c7d7f7g7h7j7k7l7m7n7p7r7s7t7v7w7b7c7d7f7g7h7j7k7l7m7n7p7r7s7t7v7w7br6dr6fr6gr6kr6");
		}
		return randomNameConsonants;
	}

	@Override
	public String generateRandomName(int minSyllable, int maxSyllable)
	{
		final StringBuilder name=new StringBuilder("");
		final DiceLibrary dice=CMLib.dice();
		final int numSyllables = (maxSyllable<=minSyllable)?minSyllable:dice.roll(1, maxSyllable-minSyllable+1, minSyllable-1);
		boolean isVowel=CMLib.dice().rollPercentage()>0;
		final Pair<String,Integer>[] vowels=getRandomVowels();
		final Pair<String,Integer>[] cons=getRandomConsonants();
		for(int i=1;i<=numSyllables;i++)
		{
			Pair<String,Integer> data = null;
			for(int x=0;x<100;x++)
			{
				if (isVowel)
				{
					data=vowels[dice.roll(1, vowels.length, -1)];
				}
				else
				{
					data=cons[dice.roll(1, cons.length, -1)];
				}
				if(i==1)
				{
					if(CMath.bset(data.second.intValue(),2))
					{
						break;
					}
				}
				else
				if(i==numSyllables)
				{
					if(CMath.bset(data.second.intValue(),1))
					{
						break;
					}
				}
				else
				{
					if(CMath.bset(data.second.intValue(),4))
					{
						break;
					}
				}
			}
			if(data != null)
			{
				name.append(data.first);
			}
			isVowel = !isVowel;
		}
		return CMStrings.capitalizeFirstLetter(name.toString());
	}
	
	public LoginResult loginSystem(Session session, LoginSessionImpl loginObj) throws IOException
	{
		if(session==null)
			return LoginResult.NO_LOGIN;
		if(loginObj==null)
			return LoginResult.NO_LOGIN;
		try
		{
			while(!session.isStopped())
			{
				switch(loginObj.state)
				{
				case LOGIN_AUTOLOGIN:
					return LoginResult.NORMAL_LOGIN;
				case LOGIN_START:
				case LOGIN_NAME:
				case ACCTMENU_SHOWMENU:
					session.setMob(null);
					break;
				default:
					break;
				}
				final LoginResult res = loginSubsystem(loginObj, session);
				if(res != null)
				{
					return res;
				}
			}
		}
		catch(final Exception e)
		{
			session.println(L("\n\r\n\rI'm sorry, but something bad happened. You'll need to re-log.\n\r\n\r"));
			Log.errOut(e);
		}
		loginObj.reset=true;
		loginObj.state=LoginState.LOGIN_START;
		return LoginResult.NO_LOGIN;
	}

	protected void logoutLoginSession(LoginSessionImpl loginSession)
	{
		if(loginSession!=null)
		{
			if(loginSession.acct!=null)
				loginSession.state=LoginState.ACCTMENU_START;
			else
				loginSession.state=LoginState.LOGIN_START;
			loginSession.skipInput=true;
			loginSession.attempt=0;
		}
	}
}
