package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
public class Authenticate extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return false;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if((parms!=null)&&(parms.containsKey("AUTH")))
			return Encrypt(getLogin(httpReq))+"-"+Encrypt(getPassword(httpReq));
		else
		{
			String login=getLogin(httpReq);
			if((parms!=null)&&(parms.containsKey("SETPLAYER")))
				httpReq.addRequestParameters("PLAYER",login);
			if(authenticated(httpReq,login,getPassword(httpReq)))
				return "true";
			else
				return "false";
		}
	}
	
	private static final String ABCs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	private static final String FILTER="peniswrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad";

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
	
	public static boolean authenticated(ExternalHTTPRequests httpReq, String login, String password)
	{
		MOB mob=CMMap.getLoadPlayer(login);
		if(mob==null) return false;
		if((mob.playerStats()!=null)
		&&(mob.playerStats().password().equalsIgnoreCase(password))
		&&(mob.Name().trim().length()>0)
		&&(!bannedName(mob.Name())))
		{
			return true;
		}
		return false;
	}

	private static char ABCeq(char C)
	{
		for(int A=0;A<ABCs.length();A++)
			if(C==ABCs.charAt(A)) return ABCs.charAt(A);
		return (char)0;
	}

	private static int ABCindex(char C)
	{
		for(int A=0;A<ABCs.length();A++)
			if(C==ABCs.charAt(A)) return A;
		return 0;
	}

	private static String Encrypt(String ENCRYPTME)
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

	private static String Decrypt(String DECRYPTME)
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
