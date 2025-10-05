package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMProps.Str;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary.ThinPlayer;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_web.interfaces.*;

import java.net.URLEncoder;
import java.util.*;

/*
   Copyright 2011-2025 Bo Zimmerman

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
public class AccountCreate extends StdWebMacro
{
	@Override
	public String name()
	{
		return "AccountCreate";
	}

	private enum AccountCreateErrors
	{
		NO_NAME,
		NO_PASSWORD,
		NO_PASSWORDAGAIN,
		BAD_PASSWORDMATCH,
		NO_VERIFYKEY,
		NO_VERIFY,
		BAD_EMAILADDRESS,
		BAD_VERIFY
	}
	// OK, NO_NEW_PLAYERS, NO_NEW_LOGINS, BAD_USED_NAME, CREATE_LIMIT_REACHED

	public String checkImageVerification(final HTTPRequest httpReq, final String verify, final String verifyKey)
	{
		synchronized(ImageVerificationImage.sync)
		{
			final SLinkedList<ImageVerificationImage.ImgCacheEntry> cache = ImageVerificationImage.getVerifyCache();
			boolean found=false;
			final String hisIp=httpReq.getClientAddress().getHostAddress();
			for(final Iterator<ImageVerificationImage.ImgCacheEntry> p =cache.descendingIterator();p.hasNext();)
			{
				final ImageVerificationImage.ImgCacheEntry entry=p.next();
				if((entry.key.equalsIgnoreCase(verifyKey))
				&&(entry.ip.equals(hisIp)))
				{
					found=true;
					if(!entry.value.equalsIgnoreCase(verify))
						return AccountCreateErrors.BAD_VERIFY.toString();
				}
			}
			if(!found)
				return AccountCreateErrors.NO_VERIFYKEY.toString();
			return "";
		}
	}

	@Override
	public String runMacro(final HTTPRequest httpReq, final String parm, final HTTPResponse httpResp)
	{
		final boolean emailPassword=((CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.Str.MAILBOX).length()>0));
		final boolean emailDisabled=CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("DISABLE");

		final java.util.Map<String,String> parms=parseParms(parm);
		if(parms.containsKey("SHOWPASSWORD"))
			return Boolean.toString(!emailPassword);
		if(parms.containsKey("SHOWEMAILADDRESS"))
			return Boolean.toString(!emailDisabled);

		String address="unknown";
		try
		{
			address=httpReq.getClientAddress().getHostAddress().trim();
		}
		catch(final Exception e)
		{
		}
		if(!CMLib.login().performSpamConnectionCheck(address))
			return "GO_AWAY";

		if(parms.containsKey("FORGOT"))
		{
			if(!httpReq.isUrlParameter("EMAIL"))
				return "NO_EMAIL";
			String emailAddress=httpReq.getUrlParameter("EMAIL");
			if((emailAddress==null)||(emailAddress.trim().length()==0))
				return "NO_EMAIL";
			emailAddress = emailAddress.trim();
			if(!CMLib.smtp().isValidEmailAddress(emailAddress))
				return "NO_EMAIL";
			final String verifykey=httpReq.getUrlParameter("IMGVERKEY");
			if((verifykey==null)||(verifykey.length()==0))
				return AccountCreateErrors.NO_VERIFYKEY.toString();
			final String verify=httpReq.getUrlParameter("VERIFY");
			if((verify==null)||(verify.length()==0))
				return AccountCreateErrors.NO_VERIFY.toString();
			final String verifyResult = checkImageVerification(httpReq,verify,verifykey);
			if(verifyResult.length()>0)
				return verifyResult;
			String login=null;
			String password="";
			AccountStats acctStats=null;
			String emailToName=null;
			if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
			{
				final PlayerAccount acct = CMLib.players().getLoadAccountByEmail(emailAddress);
				if(acct != null)
				{
					acctStats=acct;
					login=acct.getAccountName();
					password=acct.getPasswordStr();
					int highestLevel=0;
					for(final Enumeration<ThinPlayer> tp=acct.getThinPlayers();tp.hasMoreElements();)
					{
						final ThinPlayer TP=tp.nextElement();
						if(TP.level()>highestLevel)
						{
							highestLevel=TP.level();
							emailToName=TP.name();
						}
					}
					if(emailToName==null)
						emailToName=acct.getAccountName();
				}
			}
			if(login == null)
			{
				final MOB mob = CMLib.players().getLoadPlayerByEmail(emailAddress);
				if(mob == null)
					return "";
				if(mob.playerStats()!=null)
				{
					emailToName=mob.name();
					if((CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
					&&(mob.playerStats().getAccount()!=null))
					{
						acctStats=mob.playerStats().getAccount();
						login=mob.playerStats().getAccount().getAccountName();
						password=mob.playerStats().getAccount().getPasswordStr();
					}
					else
					{
						acctStats=mob.playerStats();
						login=mob.name();
						password=mob.playerStats().getPasswordStr();
					}
				}
				else
					return "";
			}
			if((acctStats==null)
			||(login==null)
			||(emailToName==null))
				return "INTERNAL_FAILURE";
			if(CMProps.getBoolVar(CMProps.Bool.HASHPASSWORDS))
			{
				password=CMLib.encoder().generateRandomPassword();
				acctStats.setPassword(password);
				if(acctStats instanceof PlayerAccount)
					CMLib.database().DBUpdateAccount((PlayerAccount)acctStats);
				else
				if(acctStats instanceof PlayerStats)
					CMLib.database().DBUpdatePassword(login,password);
			}
			CMLib.smtp().emailOrJournal(emailToName, "noreply",
					emailToName, L("Password for @x1",login),
				L("Your password for @x1 at @x2 is '@x3'.",login,CMProps.getVar(CMProps.Str.MUDDOMAIN),password));
			return "";
		}

		if(!parms.containsKey("CREATE"))
			return " @break@";

		String name=httpReq.getUrlParameter("ACCOUNTNAME");
		if(name==null)
			name=httpReq.getUrlParameter("LOGIN");
		if((name==null)||(name.length()==0))
			return AccountCreateErrors.NO_NAME.toString();
		String password;
		if(emailPassword)
		{
			password="";
			for(int i=0;i<6;i++)
				password+=(char)('a'+CMLib.dice().roll(1,26,-1));
		}
		else
		{
			password=httpReq.getUrlParameter("PASSWORD");
			if((password==null)||(password.length()==0))
				return AccountCreateErrors.NO_PASSWORD.toString();
			final String passwordagain=httpReq.getUrlParameter("PASSWORDAGAIN");
			if((passwordagain==null)||(passwordagain.length()==0))
				return AccountCreateErrors.NO_PASSWORDAGAIN.toString();
			if(!password.equalsIgnoreCase(passwordagain))
				return AccountCreateErrors.BAD_PASSWORDMATCH.toString();
		}
		final String verifykey=httpReq.getUrlParameter("IMGVERKEY");
		if((verifykey==null)||(verifykey.length()==0))
			return AccountCreateErrors.NO_VERIFYKEY.toString();
		final String verify=httpReq.getUrlParameter("VERIFY");
		if((verify==null)||(verify.length()==0))
			return AccountCreateErrors.NO_VERIFY.toString();
		String emailAddress="";
		if(!emailDisabled)
		{
			final boolean emailReq=(!CMProps.getVar(CMProps.Str.EMAILREQ).toUpperCase().startsWith("OPTION"));
			emailAddress=httpReq.getUrlParameter("EMAILADDRESS");
			if(emailReq)
			{
				if((emailAddress==null)||(emailAddress.length()==0)||!CMLib.smtp().isValidEmailAddress(emailAddress))
					return AccountCreateErrors.BAD_EMAILADDRESS.toString();
			}
		}
		final String verifyResult = checkImageVerification(httpReq,verify,verifykey);
		if(verifyResult.length()>0)
			return verifyResult;
		name = CMStrings.capitalizeAndLower(name);
		final CharCreationLibrary.NewCharNameCheckResult checkResult=CMLib.login().newAccountNameCheck(name, httpReq.getClientAddress().getHostAddress());
		if(checkResult!=CharCreationLibrary.NewCharNameCheckResult.OK)
			return checkResult.toString();
		final PlayerAccount acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		acct.setFlag(PlayerAccount.AccountFlag.ANSI, true);
		acct.setAccountName(name);
		acct.setPassword(password);
		acct.setEmail(emailAddress);
		acct.setLastIP(httpReq.getClientAddress().getHostAddress());
		acct.setLastDateTime(System.currentTimeMillis());
		if(CMProps.getBoolVar(CMProps.Bool.ACCOUNTEXPIRATION))
			acct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*(CMProps.getIntVar(CMProps.Int.TRIALDAYS))));
		CMLib.database().DBCreateAccount(acct);
		CMLib.players().addAccount(acct);
		if(emailPassword)
		{
			CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.Str.MAILBOX),
					acct.getAccountName(),
					acct.getAccountName(),
					L("Password for @x1",acct.getAccountName()),
					L("Your password for @x1 is '@x2'.\n\r",acct.getAccountName(),password)+
					L("You can login by pointing your mud client at @x1 port(s): @x2.\n\r",
							CMProps.getVar(CMProps.Str.MUDDOMAIN),CMProps.getVar(CMProps.Str.ALLMUDPORTS))+
					L("After creating a character, you may use the PASSWORD command to change it once you are online."));
		}
		if(parms.containsKey("LOGIN"))
		{
			httpReq.addFakeUrlParameter("PLAYER",name);
			if(Authenticate.authenticated(httpReq,httpResp,name,password))
			{
				try
				{
					final String loginUrlStr= URLEncoder.encode(CMLib.encoder().filterEncrypt(Authenticate.getLogin(httpReq)),"UTF-8");
					final String passwordUrlStr=URLEncoder.encode(CMLib.encoder().filterEncrypt(Authenticate.getPassword(httpReq)),"UTF-8");
					httpReq.addFakeUrlParameter("CMAUTH", loginUrlStr+"-"+passwordUrlStr);
				}
				catch(final Exception u)
				{
				}
			}
		}
		return "";
	}
}
