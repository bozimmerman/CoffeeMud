package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.miniweb.interfaces.*;
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
import java.lang.reflect.Method;
import java.net.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
	@Override public String name() { return "Authenticate"; }
	@Override public boolean isAdminMacro()    {return false;}
	private static final long ONE_REAL_DAY=(long)1000*60*60*24;

	@Override
	public String runMacro(HTTPRequest httpReq, String parm)
	{
		java.util.Map<String,String> parms=parseParms(parm);
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
			httpReq.addFakeUrlParameter("PLAYER",login);
		if((parms!=null)&&(parms.containsKey("SETACCOUNT"))&&(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1))
		{
			MOB mob=getAuthenticatedMob(httpReq);
			if((mob!=null)&&(mob.playerStats()!=null)&&(mob.playerStats().getAccount()!=null))
				httpReq.addFakeUrlParameter("ACCOUNT",mob.playerStats().getAccount().getAccountName());
		}
		if(authenticated(httpReq,login,getPassword(httpReq)))
			return "true";
		return "false";
	}

	protected static byte[] FILTER=null;
	public static byte[] getFilter()
	{
		if(FILTER==null)
		{
			// this is coffeemud's unsophisticated xor(mac address) encryption system.
			byte[] filterc = new String("wrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad").getBytes();
			FILTER=new byte[256];
			try
			{
				for(int i=0;i<256;i++)
					FILTER[i]=filterc[i % filterc.length];
				String domain=CMProps.getVar(CMProps.Str.MUDDOMAIN);
				if(domain.length()>0)
					for(int i=0;i<256;i++)
						FILTER[i]^=domain.charAt(i % domain.length());
				String name=CMProps.getVar(CMProps.Str.MUDNAME);
				if(name.length()>0)
					for(int i=0;i<256;i++)
						FILTER[i]^=name.charAt(i % name.length());
				String email=CMProps.getVar(CMProps.Str.ADMINEMAIL);
				if(email.length()>0)
					for(int i=0;i<256;i++)
						FILTER[i]^=email.charAt(i % email.length());
				NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
				byte[] mac = ni.getHardwareAddress();
				if((mac != null) && (mac.length > 0))
				{
					for(int i=0;i<256;i++)
						FILTER[i]^=Math.abs(mac[i % mac.length]);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return FILTER;
	}

	protected static byte[] EnDeCrypt(byte[] bytes)
	{
		byte[] FILTER=getFilter();
		for ( int i = 0, j = 0; i < bytes.length; i++, j++ )
		{
		   if ( j >= FILTER.length ) j = 0;
		   bytes[i]=(byte)((bytes[i] ^ FILTER[j]) & 0xff);
		}
		return bytes;
	}

	protected static String Encrypt(String ENCRYPTME)
	{
		try
		{
			final byte[] buf=B64Encoder.B64encodeBytes(EnDeCrypt(ENCRYPTME.getBytes()),B64Encoder.DONT_BREAK_LINES).getBytes();
			final StringBuilder s=new StringBuilder("");
			for(byte b : buf)
			{
				String s2=Integer.toHexString(b);
				while(s2.length()<2)s2="0"+s2;
				s.append(s2);
			}
			return s.toString();
		}
		catch(Exception e)
		{
			return "";
		}
	}

	protected static String Decrypt(String DECRYPTME)
	{
		try
		{
			byte[] buf=new byte[DECRYPTME.length()/2];
			for(int i=0;i<DECRYPTME.length();i+=2)
				buf[i/2]=(byte)(Integer.parseInt(DECRYPTME.substring(i,i+2),16) & 0xff);
			return new String(EnDeCrypt(B64Encoder.B64decode(new String(buf))));
		}
		catch(Exception e)
		{
			return "";
		}
	}

	public static boolean authenticated(HTTPRequest httpReq, String login, String password)
	{
		MOB mob=CMLib.players().getLoadPlayer(login);
		if((mob!=null)
		&&(mob.playerStats()!=null)
		&&(mob.playerStats().matchesPassword(password))
		&&(mob.Name().trim().length()>0)
		&&(!CMSecurity.isBanned(mob.Name())))
		{
			final long lastLogin = System.currentTimeMillis() - mob.playerStats().getLastDateTime();
			if(lastLogin > ONE_REAL_DAY)
				mob.playerStats().setLastDateTime(System.currentTimeMillis());
			return true;
		}
		if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
		{
			final PlayerAccount acct=CMLib.players().getLoadAccount(login);
			if((acct!=null)
			&&(acct.matchesPassword(password))
			&&(!CMSecurity.isBanned(acct.getAccountName())))
			{
				final long lastLogin = System.currentTimeMillis() - acct.getLastDateTime();
				if(lastLogin > ONE_REAL_DAY)
					acct.setLastDateTime(System.currentTimeMillis());
				return true;
			}
		}
		return false;
	}

	public static MOB getAuthenticatedMob(HTTPRequest httpReq)
	{
		if(httpReq.getRequestObjects().get("AUTHENTICATED_USER")!=null)
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
			if((mob==null)
			||(mob.playerStats()==null)
			||((CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1) && (mob.playerStats().getAccount()==null)))
			{
				if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
				{
					PlayerAccount acct=CMLib.players().getLoadAccount(login);
					if((acct!=null)
					&&(acct.matchesPassword(password))
					&&(!CMSecurity.isBanned(acct.getAccountName())))
						mob=acct.getAccountMob();
					else
						mob=null;
				}
				else
					mob=null;
			}
			else
			if((!mob.playerStats().matchesPassword(password))
			||(mob.Name().trim().length()==0)
			||(CMSecurity.isBanned(mob.Name()))
			||((mob.playerStats().getAccount()!=null)&&(CMSecurity.isBanned(mob.playerStats().getAccount().getAccountName()))))
				mob=null;
		}
		if(mob!=null)
			httpReq.getRequestObjects().put("AUTHENTICATED_USER",mob);
		else
			httpReq.getRequestObjects().put("AUTHENTICATED_USER",new Object());
		return mob;
	}

	public static String getLogin(HTTPRequest httpReq)
	{
		String login=httpReq.getUrlParameter("LOGIN");
		if((login!=null)&&(login.length()>0))
		{
			if(CMProps.getIntVar(CMProps.Int.COMMONACCOUNTSYSTEM)>1)
			{
				PlayerAccount acct = CMLib.players().getLoadAccount(login);
				if(acct != null)
				{
					MOB highestM = null;
					final String playerName=acct.findPlayer(login);
					if(playerName!=null)
					{
						login=playerName;
						highestM=CMLib.players().getLoadPlayer(login);
					}
					else
					for(Enumeration<MOB> m = acct.getLoadPlayers();m.hasMoreElements();)
					{
						MOB M=m.nextElement();
						if((highestM==null)
						||((M!=null)&&(M.basePhyStats().level()>highestM.basePhyStats().level())))
							highestM = M;
					}
					if(highestM!=null)
					{
						if(!highestM.Name().equals(login))
							httpReq.addFakeUrlParameter("LOGIN", highestM.Name());
						return highestM.Name();
					}
				}
			}
			return login;
		}
		String auth=httpReq.getUrlParameter("AUTH");
		if(auth==null) return "";
		int x = auth.indexOf('-');
		if(x>=0)
			login=Decrypt(auth.substring(0,x));
		return login;
	}

	public static String getPassword(HTTPRequest httpReq)
	{
		String password=httpReq.getUrlParameter("PASSWORD");
		if((password!=null)&&(password.length()>0))
			return password;
		String auth=httpReq.getUrlParameter("AUTH");
		if(auth==null) return "";
		int x = auth.indexOf('-');
		if(x>=0)
			password=Decrypt(auth.substring(x+1));
		return password;
	}
}
