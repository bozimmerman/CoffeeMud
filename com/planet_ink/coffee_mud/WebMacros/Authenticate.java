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
public class Authenticate extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return false;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
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
	
    protected static final char[] ABCs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
    protected static final char[] FILTER;
    protected static final int[] ABCDEXs;
    static 
    {
    	// this is coffeemud's unsophisticated rot(mac address) encryption system.
    	char[] filterc = "wrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad".toCharArray(); 
    	try
    	{
    		NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
    		byte[] mac = ni.getHardwareAddress();
    		if(mac != null)
    		{
    			int i=0;
    			while(i<filterc.length)
    			{
    				filterc[i]=ABCs[Math.abs(mac[i % mac.length]) % ABCs.length];
    				i++;
    			}
    		}
    	} catch(Exception e) {
    		Log.errOut("Authenticate",e);
    	}
    	FILTER=filterc;
    	int[] abcdex=new int[256];
    	for(int i=0;i<ABCs.length;i++)
    		abcdex[ABCs[i]]=i+1;
    	ABCDEXs=abcdex;
    }

	public static boolean authenticated(ExternalHTTPRequests httpReq, String login, String password)
	{
		MOB mob=CMLib.players().getLoadPlayer(login);
		if((mob!=null)
		&&(mob.playerStats()!=null)
		&&(mob.playerStats().password().equalsIgnoreCase(password))
		&&(mob.Name().trim().length()>0)
		&&(!CMSecurity.isBanned(mob.Name())))
			return true;
		if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>0)
		{
			PlayerAccount acct=CMLib.players().getLoadAccount(login);
			if((acct!=null)
			&&(acct.password().equalsIgnoreCase(password))
			&&(!CMSecurity.isBanned(acct.accountName())))
				return true;
		}
			
		return false;
	}

    protected static String Encrypt(String ENCRYPTME)
	{
    	char[] INTOME=new char[ENCRYPTME.length()];
    	char[] ENCRYPTMEC = ENCRYPTME.toCharArray(); 
		for(int i=0; i<ENCRYPTMEC.length;i++)
		{
			char c = ENCRYPTMEC[i];
			INTOME[i]=c;
			int dex = ABCDEXs[c]-1; 
			if (dex>=0)
			{
				for(int f=i;f<FILTER.length;f+=ENCRYPTMEC.length)
				{
					dex = dex + ABCDEXs[FILTER[f]];
					if (dex>=ABCs.length)
						dex = dex-ABCs.length;
					INTOME[i]=ABCs[dex];
				}
			}
		}
		return new String(INTOME);
	}

    protected static String Decrypt(String DECRYPTME)
	{
    	char[] INTOME=new char[DECRYPTME.length()];
    	char[] DECRYPTMEC = DECRYPTME.toCharArray(); 
		for(int i=0; i<DECRYPTMEC.length;i++)
		{
			char c = DECRYPTMEC[i];
			INTOME[i]=c;
			int dex = ABCDEXs[c]-1; 
			if (dex >=0)
			{
				for(int f=i;f<FILTER.length;f+=DECRYPTMEC.length)
				{
					dex = dex - ABCDEXs[FILTER[f]];
					if (dex<0)
						dex = dex+ABCs.length;
					INTOME[i]=ABCs[dex];
				}
			}
		}
		return new String(INTOME);
	}

    public static MOB getAuthenticatedMob(ExternalHTTPRequests httpReq)
    {
    	if(httpReq.getRequestObjects().containsKey("AUTHENTICATED_USER"))
    	{
    		Object o=httpReq.getRequestObjects().get("AUTHENTICATED_USER");
    		if(!(o instanceof MOB)) return null;
    		return (MOB)o;
    	}
    	MOB mob=null;
    	String login = getLogin(httpReq);
    	if((login != null)&&(login.length()>0))
    	{
	    	String password = getPassword(httpReq);
			mob=CMLib.players().getLoadPlayer(login);
			if((mob==null)||(mob.playerStats()==null))
			{
				if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>0)
				{
					PlayerAccount acct=CMLib.players().getLoadAccount(login);
					if((acct!=null)
					&&(acct.password().equalsIgnoreCase(password))
					&&(!CMSecurity.isBanned(acct.accountName())))
						mob=acct.getAccountMob();
					else
						mob=null;
				}
				else
					mob=null;
			}
			else
			if((!mob.playerStats().password().equalsIgnoreCase(password))
			||(mob.Name().trim().length()==0)
			||(CMSecurity.isBanned(mob.Name()))
			||((mob.playerStats().getAccount()!=null)&&(CMSecurity.isBanned(mob.playerStats().getAccount().accountName()))))
				mob=null;
    	}
    	if(mob!=null)
			httpReq.getRequestObjects().put("AUTHENTICATED_USER",mob);
    	else
			httpReq.getRequestObjects().put("AUTHENTICATED_USER",new Object());
		return mob;
    }
    
	public static String getLogin(ExternalHTTPRequests httpReq)
	{
		String login=httpReq.getRequestParameter("LOGIN");
		if((login!=null)&&(login.length()>0))
		{
			if(CMProps.getIntVar(CMProps.SYSTEMI_COMMONACCOUNTSYSTEM)>0)
			{
				PlayerAccount acct = CMLib.players().getLoadAccount(login);
				if(acct != null)
				{
					MOB highestM = null;
					if(acct.isPlayer(login))
						highestM=CMLib.players().getLoadPlayer(login);
					else
					for(Enumeration<MOB> m = acct.getLoadPlayers();m.hasMoreElements();)
					{
						MOB M=m.nextElement();
						if((highestM==null)
						||((M!=null)&&(M.baseEnvStats().level()>highestM.baseEnvStats().level())))
							highestM = M;
					}
					if(highestM!=null)
					{
						if(!highestM.Name().equals(login))
							httpReq.addRequestParameters("LOGIN", highestM.Name());
						return highestM.Name();
					}
				}
			}
			return login;
		}
		String auth=httpReq.getRequestParameter("AUTH");
		if(auth==null) return "";
		int x = auth.indexOf('-');
		if(x>=0) 
			login=Decrypt(auth.substring(0,x));
		return login;
	}

	public static String getPassword(ExternalHTTPRequests httpReq)
	{
		String password=httpReq.getRequestParameter("PASSWORD");
		if((password!=null)&&(password.length()>0))
			return password;
		String auth=httpReq.getRequestParameter("AUTH");
		if(auth==null) return "";
		int x = auth.indexOf('-');
		if(x>=0) 
			password=Decrypt(auth.substring(x+1));
		return password;
	}
}
