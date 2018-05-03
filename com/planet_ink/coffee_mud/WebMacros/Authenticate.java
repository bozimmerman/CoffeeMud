package com.planet_ink.coffee_mud.WebMacros;

import com.planet_ink.coffee_web.interfaces.*;
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
   Copyright 2002-2018 Bo Zimmerman

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
	@Override
	public String name()
	{
		return "Authenticate";
	}

	@Override
	public boolean isAdminMacro()
	{
		return false;
	}

	private static final long ONE_REAL_DAY=(long)1000*60*60*24;

	@Override
	public String runMacro(HTTPRequest httpReq, String parm, HTTPResponse httpResp)
	{
		final java.util.Map<String,String> parms=parseParms(parm);
		if((parms!=null)&&(parms.containsKey("AUTH")))
		{
			try
			{
				final String loginUrlStr= URLEncoder.encode(Authenticate.Encrypt(Authenticate.getLogin(httpReq)),"UTF-8");
				final String passwordUrlStr=URLEncoder.encode(Authenticate.Encrypt(Authenticate.getPassword(httpReq)),"UTF-8");
				return loginUrlStr+"-"+passwordUrlStr;
			}
			catch(final Exception u)
			{
				return "false";
			}
		}
		final String login=getLogin(httpReq);
		if((parms!=null)&&(parms.containsKey("SETPLAYER")))
			httpReq.addFakeUrlParameter("PLAYER",login);
		if((parms!=null)&&(parms.containsKey("SETACCOUNT"))&&(CMProps.isUsingAccountSystem()))
		{
			final MOB mob=getAuthenticatedMob(httpReq);
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
			final byte[] filterc = new String("wrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad").getBytes();
			FILTER=new byte[256];
			try
			{
				for(int i=0;i<256;i++)
					FILTER[i]=filterc[i % filterc.length];
				final String domain=CMProps.getVar(CMProps.Str.MUDDOMAIN);
				if(domain.length()>0)
				{
					for(int i=0;i<256;i++)
						FILTER[i]^=domain.charAt(i % domain.length());
				}
				final String name=CMProps.getVar(CMProps.Str.MUDNAME);
				if(name.length()>0)
				{
					for(int i=0;i<256;i++)
						FILTER[i]^=name.charAt(i % name.length());
				}
				final String email=CMProps.getVar(CMProps.Str.ADMINEMAIL);
				if(email.length()>0)
				{
					for(int i=0;i<256;i++)
						FILTER[i]^=email.charAt(i % email.length());
				}
				for(final Enumeration<NetworkInterface> nie = NetworkInterface.getNetworkInterfaces(); nie.hasMoreElements();)
				{
					final NetworkInterface ni = nie.nextElement();
					if(ni != null)
					{
						final byte[] mac = ni.getHardwareAddress();
						if((mac != null) && (mac.length > 0))
						{
							for(int i=0;i<256;i++)
								FILTER[i]^=Math.abs(mac[i % mac.length]);
						}
					}
				}
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
		}
		return FILTER;
	}

	protected static byte[] EnDeCrypt(byte[] bytes)
	{
		final byte[] FILTER=getFilter();
		for ( int i = 0, j = 0; i < bytes.length; i++, j++ )
		{
			if ( j >= FILTER.length ) 
				j = 0;
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
			for(final byte b : buf)
			{
				String s2=Integer.toHexString(b);
				while(s2.length()<2)
					s2="0"+s2;
				s.append(s2);
			}
			return s.toString();
		}
		catch(final Exception e)
		{
			return "";
		}
	}

	protected static String Decrypt(String DECRYPTME)
	{
		try
		{
			final byte[] buf=new byte[DECRYPTME.length()/2];
			for(int i=0;i<DECRYPTME.length();i+=2)
				buf[i/2]=(byte)(Integer.parseInt(DECRYPTME.substring(i,i+2),16) & 0xff);
			return new String(EnDeCrypt(B64Encoder.B64decode(new String(buf))));
		}
		catch(final Exception e)
		{
			return "";
		}
	}

	public static boolean authenticated(HTTPRequest httpReq, String login, String password)
	{
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return false;
		final MOB mob=CMLib.players().getLoadPlayer(login);
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
		if(CMProps.isUsingAccountSystem())
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
			final Object o=httpReq.getRequestObjects().get("AUTHENTICATED_USER");
			if(!(o instanceof MOB))
				return null;
			return (MOB)o;
		}
		MOB mob=null;
		final String login = getLogin(httpReq);
		if((login != null)&&(login.length()>0))
		{
			final String password = getPassword(httpReq);
			mob=CMLib.players().getLoadPlayer(login);
			if((mob==null)
			||(mob.playerStats()==null)
			||((CMProps.isUsingAccountSystem()) && (mob.playerStats().getAccount()==null)))
			{
				if(CMProps.isUsingAccountSystem())
				{
					final PlayerAccount acct=CMLib.players().getLoadAccount(login);
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
			if(CMProps.isUsingAccountSystem())
			{
				final PlayerAccount acct = CMLib.players().getLoadAccount(login);
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
					for(final Enumeration<MOB> m = acct.getLoadPlayers();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
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
		final String auth=httpReq.getUrlParameter("AUTH");
		if(auth==null)
			return "";
		final int x = auth.indexOf('-');
		if(x>=0)
			login=Decrypt(auth.substring(0,x));
		return login;
	}

	public static String getPassword(HTTPRequest httpReq)
	{
		String password=httpReq.getUrlParameter("PASSWORD");
		if((password!=null)&&(password.length()>0))
			return password;
		final String auth=httpReq.getUrlParameter("AUTH");
		if(auth==null)
			return "";
		final int x = auth.indexOf('-');
		if(x>=0)
			password=Decrypt(auth.substring(x+1));
		return password;
	}
}
