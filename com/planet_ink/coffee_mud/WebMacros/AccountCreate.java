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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;
/* 
   Copyright 2000-2011 Bo Zimmerman

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

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
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
        
		String name=httpReq.getRequestParameter("ACCOUNTNAME");
		if((name==null)||(name.length()==0)) return "NO_NAME";
		String password;
		if(emailPassword)
		{
            password="";
            for(int i=0;i<6;i++)
                password+=(char)('a'+CMLib.dice().roll(1,26,-1));
		}
		else
		{
			password=httpReq.getRequestParameter("PASSWORD");
			if((password==null)||(password.length()==0)) return "NO_PASSWORD";
			String passwordagain=httpReq.getRequestParameter("PASSWORDAGAIN");
			if((passwordagain==null)||(passwordagain.length()==0)) return "NO_PASSWORDAGAIN";
			if(!password.equalsIgnoreCase(passwordagain))
				return "BAD_PASSWORDMATCH";
		}
		String verifykey=httpReq.getRequestParameter("VERIFYKEY");
		if((verifykey==null)||(verifykey.length()==0)) return "NO_VERIFYKEY";
		String verify=httpReq.getRequestParameter("VERIFY");
		if((verify==null)||(verify.length()==0)) return "NO_VERIFY";
		String emailAddress="";
        if(!emailDisabled)
        {
            boolean emailReq=(!CMProps.getVar(CMProps.SYSTEM_EMAILREQ).toUpperCase().startsWith("OPTION"));
            emailAddress=httpReq.getRequestParameter("EMAILADDRESS");
            if(emailReq)
            {
        		if((emailAddress==null)||(emailAddress.length()==0)||!CMLib.smtp().isValidEmailAddress(emailAddress)) 
        			return "BAD_EMAILADDRESS";
            }
        }
		synchronized(ImageVerificationImage.sync)
		{
		   	SLinkedList<Pair<String,String>> cache = ImageVerificationImage.getVerifyCache();
		   	boolean found=false;
		   	for(Pair<String,String> p : cache)
		   	{
		   		if(p.first.equalsIgnoreCase(verifykey))
		   		{
		   			found=true;
		   			if(!p.second.equalsIgnoreCase(verify))
		   				return "BAD_VERIFY";
		   		}
		   	}
		   	if(!found)
		   		return "NO_VERIFYKEY";
		}
		name = CMStrings.capitalizeAndLower(name);
		CharCreationLibrary.NewCharNameCheckResult checkResult=CMLib.login().newCharNameCheck(name, httpReq.getHTTPclientIP(), false);
		if(checkResult!=CharCreationLibrary.NewCharNameCheckResult.OK)
			return checkResult.toString();
    	PlayerAccount acct = (PlayerAccount)CMClass.getCommon("DefaultPlayerAccount");
    	acct.setFlag(PlayerAccount.FLAG_ANSI, true);
        acct.setAccountName(name);
        acct.setPassword(password);
        acct.setEmail(emailAddress);
        acct.setLastIP(httpReq.getHTTPclientIP());
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
                    "Your password for "+acct.accountName()+" is: "+acct.password()+"\n\rYou can login by pointing your mud client at "+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN)+" port(s):"+CMProps.getVar(CMProps.SYSTEM_MUDPORTS)+".\n\rAfter creating a character, you may use the PASSWORD command to change it once you are online.");
        }
		return "";
	}
}
