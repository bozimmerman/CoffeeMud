package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.miniweb.interfaces.*;

import java.net.URLEncoder;
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
public class AccountCreate extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	private enum AccountCreateErrors 
	{
		NO_NAME, NO_PASSWORD, NO_PASSWORDAGAIN, BAD_PASSWORDMATCH, NO_VERIFYKEY, NO_VERIFY,
		BAD_EMAILADDRESS, BAD_VERIFY
	}
	// OK, NO_NEW_PLAYERS, NO_NEW_LOGINS, BAD_USED_NAME, CREATE_LIMIT_REACHED
	
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		boolean emailPassword=((CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("PASS"))
				 &&(CMProps.getVar(CMProps.SYSTEM_MAILBOX).length()>0));
		boolean emailDisabled=CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("DISABLE");
		
		java.util.Map<String,String> parms=parseParms(parm);
		if(parms.containsKey("SHOWPASSWORD"))
			return Boolean.toString(!emailPassword);
		if(parms.containsKey("SHOWEMAILADDRESS"))
			return Boolean.toString(!emailDisabled);
		if(!parms.containsKey("CREATE"))
			return " @break@";
		
		String name=httpReq.getUrlParameter("ACCOUNTNAME");
		if(name==null) name=httpReq.getUrlParameter("LOGIN");
		if((name==null)||(name.length()==0)) return AccountCreateErrors.NO_NAME.toString();
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
			if((password==null)||(password.length()==0)) return AccountCreateErrors.NO_PASSWORD.toString();
			String passwordagain=httpReq.getUrlParameter("PASSWORDAGAIN");
			if((passwordagain==null)||(passwordagain.length()==0)) return AccountCreateErrors.NO_PASSWORDAGAIN.toString();
			if(!password.equalsIgnoreCase(passwordagain))
				return AccountCreateErrors.BAD_PASSWORDMATCH.toString();
		}
		String verifykey=httpReq.getUrlParameter("VERIFYKEY");
		if((verifykey==null)||(verifykey.length()==0)) return AccountCreateErrors.NO_VERIFYKEY.toString();
		String verify=httpReq.getUrlParameter("VERIFY");
		if((verify==null)||(verify.length()==0)) return AccountCreateErrors.NO_VERIFY.toString();
		String emailAddress="";
		if(!emailDisabled)
		{
			boolean emailReq=(!CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"));
			emailAddress=httpReq.getUrlParameter("EMAILADDRESS");
			if(emailReq)
			{
				if((emailAddress==null)||(emailAddress.length()==0)||!CMLib.smtp().isValidEmailAddress(emailAddress)) 
					return AccountCreateErrors.BAD_EMAILADDRESS.toString();
			}
		}
		synchronized(ImageVerificationImage.sync)
		{
		   	SLinkedList<ImageVerificationImage.ImgCacheEntry> cache = ImageVerificationImage.getVerifyCache();
		   	boolean found=false;
	   		final String hisIp=httpReq.getClientAddress().getHostAddress();
		   	for(Iterator<ImageVerificationImage.ImgCacheEntry> p =cache.descendingIterator();p.hasNext();)
		   	{
		   		ImageVerificationImage.ImgCacheEntry entry=p.next();
		   		if((entry.key.equalsIgnoreCase(verifykey))
		   		&&(entry.ip.equals(hisIp)))
		   		{
		   			found=true;
		   			if(!entry.value.equalsIgnoreCase(verify))
		   				return AccountCreateErrors.BAD_VERIFY.toString();
		   		}
		   	}
		   	if(!found)
		   		return AccountCreateErrors.NO_VERIFYKEY.toString();
		}
		name = CMStrings.capitalizeAndLower(name);
		CharCreationLibrary.NewCharNameCheckResult checkResult=CMLib.login().newCharNameCheck(name, httpReq.getClientAddress().getHostAddress(), false);
		if(checkResult!=CharCreationLibrary.NewCharNameCheckResult.OK)
			return checkResult.toString();
		PlayerAccount acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
		acct.setFlag(PlayerAccount.FLAG_ANSI, true);
		acct.setAccountName(name);
		acct.setPassword(password);
		acct.setEmail(emailAddress);
		acct.setLastIP(httpReq.getClientAddress().getHostAddress());
		acct.setLastDateTime(System.currentTimeMillis());
		if(CMProps.getBoolVar(CMProps.SYSTEMB_ACCOUNTEXPIRATION))
			acct.setAccountExpiration(System.currentTimeMillis()+(1000l*60l*60l*24l*((long)CMProps.getIntVar(CMProps.SYSTEMI_TRIALDAYS))));
		CMLib.database().DBCreateAccount(acct);
		if(emailPassword)
		{
			CMLib.database().DBWriteJournal(CMProps.getVar(CMProps.SYSTEM_MAILBOX),
					acct.accountName(),
					acct.accountName(),
					"Password for "+acct.accountName(),
					"Your password for "+acct.accountName()+" is: "+password+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".\n\rAfter creating a character, you may use the PASSWORD command to change it once you are online.");
		}
		if(parms.containsKey("LOGIN"))
		{
			httpReq.addFakeUrlParameter("PLAYER",name);
			if(Authenticate.authenticated(httpReq,name,password))
				try
				{
					httpReq.addFakeUrlParameter("AUTH", URLEncoder.encode(Authenticate.Encrypt(Authenticate.getLogin(httpReq))+"-"+Authenticate.Encrypt(Authenticate.getPassword(httpReq)),"UTF-8"));
				}
				catch(Exception u)
				{
				}
		}
		return "";
	}
}
