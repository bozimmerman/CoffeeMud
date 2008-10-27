package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
import java.net.*;



/* 
   Copyright 2000-2008 Bo Zimmerman

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
public class Authenticate extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return false;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable<String,String> parms=parseParms(parm);
		if((parms!=null)&&(parms.containsKey("AUTH")))
		{
		    try
		    {
				return URLEncoder.encode(Encrypt(getLogin(httpReq))+"-"+Encrypt(getPassword(httpReq)),"UTF-8");
		    }
		    catch(Exception u)
		    {
		        return "false";
		    }
		}
		String login=getLogin(httpReq);
		if((parms!=null)&&(parms.containsKey("SETPLAYER")))
			httpReq.addRequestParameters("PLAYER",login);
		if(authenticated(httpReq,login,getPassword(httpReq)))
			return "true";
		return "false";
	}
	
    protected static final String ABCs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    protected static final String FILTER="peniswrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad";

	public static boolean authenticated(ExternalHTTPRequests httpReq, String login, String password)
	{
		MOB mob=CMLib.players().getLoadPlayer(login);
		if(mob==null) return false;
		if((mob.playerStats()!=null)
		&&(mob.playerStats().password().equalsIgnoreCase(password))
		&&(mob.Name().trim().length()>0)
		&&(!CMSecurity.isBanned(mob.Name())))
			return true;
		return false;
	}

    protected static char ABCeq(char C)
	{
		for(int A=0;A<ABCs.length();A++)
			if(C==ABCs.charAt(A)) return ABCs.charAt(A);
		return (char)0;
	}

    protected static int ABCindex(char C)
	{
		for(int A=0;A<ABCs.length();A++)
			if(C==ABCs.charAt(A)) return A;
		return 0;
	}

    protected static String Encrypt(String ENCRYPTME)
	{
		StringBuffer INTOME=new StringBuffer("");
		INTOME.setLength(ENCRYPTME.length());
		for(int S=0; S<ENCRYPTME.length();S++)
		{
			INTOME.setCharAt(S,ABCeq(ENCRYPTME.charAt(S)));
			if (INTOME.charAt(S)==(char)0)
				INTOME.setCharAt(S,ENCRYPTME.charAt(S));
			else
			for(int F=S;F<FILTER.length();F+=ENCRYPTME.length())
			{
				int X = ABCindex(INTOME.charAt(S));
				X = X + ABCindex(FILTER.charAt(F));
				if (X>=ABCs.length())
					X = X-ABCs.length();
				INTOME.setCharAt(S,ABCs.charAt(X));
			}
		}
		return INTOME.toString();
	}

    protected static String Decrypt(String DECRYPTME)
	{
		StringBuffer INTOME=new StringBuffer("");
		INTOME.setLength(DECRYPTME.length());
		for(int S=0; S<DECRYPTME.length();S++)
		{
			INTOME.setCharAt(S,ABCeq(DECRYPTME.charAt(S)));
			if (INTOME.charAt(S)==(char)0)
				INTOME.setCharAt(S,DECRYPTME.charAt(S));
			else
			for(int F=S;F<FILTER.length();F+=DECRYPTME.length())
			{
				int X = ABCindex(INTOME.charAt(S));
				X = X - ABCindex(FILTER.charAt(F));
				if (X<0)
					X = X+ABCs.length();
				INTOME.setCharAt(S,ABCs.charAt(X));
			}
		}
		return INTOME.toString();
	}

	public static String getLogin(ExternalHTTPRequests httpReq)
	{
		String login=httpReq.getRequestParameter("LOGIN");
		if((login!=null)&&(login.length()>0))
			return login;
		String auth=httpReq.getRequestParameter("AUTH");
		if(auth==null) return "";
		if(auth.indexOf("-")>=0) auth=auth.substring(0,auth.indexOf("-"));
		login=Decrypt(auth);
		return login;
	}

	public static String getPassword(ExternalHTTPRequests httpReq)
	{
		String password=httpReq.getRequestParameter("PASSWORD");
		if((password!=null)&&(password.length()>0))
			return password;
		String auth=httpReq.getRequestParameter("AUTH");
		if(auth==null) return "";
		if(auth.indexOf("-")>=0) auth=auth.substring(auth.indexOf("-")+1);
		password=Decrypt(auth);
		return password;
	}
}
