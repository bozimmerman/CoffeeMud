package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class Authenticate extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		if((parms!=null)&&(parms.containsKey("AUTH")))
			return Encrypt(getLogin(httpReq))+"-"+Encrypt(getPassword(httpReq));
		else
		{
			if(authenticated(httpReq,getLogin(httpReq),getPassword(httpReq)))
				return "true";
			else
				return "false";
		}
	}
	
	private static final String ABCs="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
	private static final String FILTER="peniswrinkletellmetrueisthereanythingasnastyasyouwellmaybesothenumber7470issprettybad";

	public static MOB getAuthenticatedMOB(String login)
	{
		MOB mob=(MOB)CMMap.MOBs.get(login);
		if(mob==null)
		{
			mob=CMClass.getMOB("StdMOB");
			mob.setName(login);
			if(!ExternalPlay.DBReadUserOnly(mob))
				return null;
			mob.recoverEnvStats();
			mob.recoverCharStats();
		}
		return mob;
	}
	
	public static boolean authenticated(ExternalHTTPRequests httpReq, String login, String password)
	{
		MOB mob=getAuthenticatedMOB(login);
		if(mob==null) return false;
		boolean subOp=false;
		boolean sysop=mob.isASysOp(null);
		httpReq.getRequestParameters().put("SYSOP",""+sysop);
		String AREA=(String)httpReq.getRequestParameters().get("AREA");
		for(int a=0;a<CMMap.numAreas();a++)
		{
			Area A=(Area)CMMap.getArea(a);
			if((AREA==null)||(AREA.length()==0)||(AREA.equals(A.name())))
				if(A.amISubOp(mob.name()))
				{ subOp=true; break;}
		}
		httpReq.getRequestParameters().put("SUBOP",""+(sysop||subOp));
		return mob.password().equalsIgnoreCase(password)&&(mob.name().trim().length()>0);
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
		for(int S=0, D=0; S<ENCRYPTME.length();S++)
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
		for(int S=0, D=0; S<DECRYPTME.length();S++)
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
		String login=(String)httpReq.getRequestParameters().get("LOGIN");
		if((login!=null)&&(login.length()>0))
			return login;
		String auth=(String)httpReq.getRequestParameters().get("AUTH");
		if(auth==null) return "";
		if(auth.indexOf("-")>=0) auth=auth.substring(0,auth.indexOf("-"));
		login=Decrypt(auth);
		return login;
	}
	
	public static String getPassword(ExternalHTTPRequests httpReq)
	{
		String password=(String)httpReq.getRequestParameters().get("PASSWORD");
		if((password!=null)&&(password.length()>0))
			return password;
		String auth=(String)httpReq.getRequestParameters().get("AUTH");
		if(auth==null) return "";
		if(auth.indexOf("-")>=0) auth=auth.substring(auth.indexOf("-")+1);
		password=Decrypt(auth);
		return password;
	}
}
