package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CMSecurity
{
	private CMSecurity(){}
	private static final long startTime=System.currentTimeMillis();
	
	private HashSet groups=new HashSet();
										 
	
	public static boolean isDebugging(String key)
	{ return (dbgVars.size()>0)&&dbgVars.contains(key);}
	
	public static boolean isDisabled(String key)
	{ return (disVars.size()>0)&&disVars.contains(key);}
	
	public static void setDebugVars(String vars)
	{
		Vector V=Util.parseCommas(vars.toUpperCase(),true);
		dbgVars.clear();
		for(int v=0;v<V.size();v++)
			dbgVars.add(((String)V.elementAt(v)).trim());
	}
	
	public static void setDisableVars(String vars)
	{
		Vector V=Util.parseCommas(vars.toUpperCase(),true);
		disVars.clear();
		for(int v=0;v<V.size();v++)
			disVars.add((String)V.elementAt(v));
	}
	public static void setDisableVar(String var, boolean delete)
	{
		if((var!=null)&&(delete)&&(disVars.size()>0))
			disVars.remove(var);
		else
		if((var!=null)&&(!delete))
			disVars.add(var);
	}
	
	private static HashSet disVars=new HashSet();
	private static HashSet dbgVars=new HashSet();

	public static long getStartTime(){return startTime;}
	
}
