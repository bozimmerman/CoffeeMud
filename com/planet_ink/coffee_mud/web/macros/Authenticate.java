package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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

	public static MOB getMOB(String last)
	{
		if(!ExternalPlay.getSystemStarted())
			return null;

		MOB M=CMMap.getPlayer(last);
		if(M==null)
			for(Enumeration p=CMMap.players();p.hasMoreElements();)
			{
				MOB mob2=(MOB)p.nextElement();
				if(mob2.Name().equalsIgnoreCase(last))
				{ M=mob2; break;}
			}
		MOB TM=CMClass.getMOB("StdMOB");
		if((M==null)&&(ExternalPlay.DBUserSearch(TM,last)))
		{
			M=CMClass.getMOB("StdMOB");
			M.setName(TM.Name());
			ExternalPlay.DBReadMOB(M);
			ExternalPlay.DBReadFollowers(M,false);
			M.setUpdated(M.lastDateTime());
			M.recoverEnvStats();
			M.recoverCharStats();
		}
		return M;
	}
	
	private static final String ABCs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	private static final String FILTER="peniswrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad";

	public static boolean authenticated(ExternalHTTPRequests httpReq, String login, String password)
	{
		MOB mob=getMOB(login);
		if(mob==null) return false;
		boolean subOp=false;
		boolean sysop=mob.isASysOp(null);
		httpReq.addRequestParameters("SYSOP",""+sysop);
		String AREA=httpReq.getRequestParameter("AREA");
		for(Enumeration a=CMMap.areas();a.hasMoreElements();)
		{
			Area A=(Area)a.nextElement();
			if((AREA==null)||(AREA.length()==0)||(AREA.equals(A.Name())))
				if(A.amISubOp(mob.Name()))
				{ subOp=true; break;}
		}
		httpReq.addRequestParameters("SUBOP",""+(sysop||subOp));
		return mob.password().equalsIgnoreCase(password)&&(mob.Name().trim().length()>0);
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
