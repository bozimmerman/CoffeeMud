package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CMSecurity
{
	private CMSecurity(){}
	private static final long startTime=System.currentTimeMillis();
	private static Hashtable groups=new Hashtable();
	
	public static final int SEC_MASK_GLOBAL=65536;
	
	public static final int SEC_GOTO=0;
	public static final int SEC_WIZI=1;
	public static final int SEC_POOF=2;
	public static final int SEC_POSS=3;
	public static final int SEC_AT=4;
	public static final int SEC_STAT=5;
	public static final int SEC_RESTRING=6;
	public static final int SEC_COPYITEM=7;
	public static final int SEC_COPYMOB=8;
	public static final int SEC_PURGE=9;
	public static final int SEC_TRANSFER=10;
	public static final int SEC_SCATTER=11;
	public static final int SEC_SHUTUP=12;
	public static final int SEC_LIST=13;
	public static final int SEC_KILLDEAD=14;
	public static final int SEC_CMDMOBS=15;
	public static final int SEC_TEACH=16;
	public static final int SEC_RESET=17;
	public static final int SEC_IMPEXP=18;
	public static final int SEC_WHERE=19;
	public static final int SEC_ORDERMOBS=20;
	public static final int SEC_CMDITEMS=21;
	public static final int SEC_CMDROOMS=22;
	public static final int SEC_CMDEXITS=23;
	public static final int SEC_CMDAREAS=24;
	public static final int SEC_CMDUSERS=25;
	public static final int SEC_BOOT=26;
	public static final int SEC_BAN=27;
	public static final int SEC_CMDSOCIALS=28;
	public static final int SEC_CMDQUESTS=29;
	public static final int SEC_ORDERPLAYERS=30;
	
	public static final String[] SEC_CODES={
		"GOTO", "WIZI", "POOF", "POSS", "AT", "STAT",
		"RESTRING", "COPYITEM", "COPYMOB", "PURGE",
		"TRANSFER", "SCATTER", "SHUTUP", "LIST",
		"KILLDEAD", "CMDMOBS", "TEACH", "RESET",
		"IMPEXP", "WHERE", "ORDERMOBS", "CMDITEMS",
		"CMDROOMS", "CMDEXITS", "CMDAREAS", "CMDUSERS",
		"BOOT", "BAN", "CMDSOCIALS", "CMDQUESTS", "ORDERPLAYERS"
		};
	
	public static boolean isASysOp(MOB mob)
	{
		if(mob==null) return false;
		if(mob.baseCharStats()==null) return false;
		if(mob.baseCharStats().getClassLevel("Archon")>=0)
			return true;
		return false;
	}
	
	public static boolean isAllowed(MOB mob, Room room, int code)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null) return false;
		Vector V=mob.playerStats().getSecurityGroups();
		if(V==null) return false;
		
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		
		for(int v=0;v<V.size();v++)
		{
			HashSet H=(HashSet)groups.get((String)V.elementAt(v));
			if(H!=null)
			{
				if(H.contains(new Integer(code&SEC_MASK_GLOBAL)))
					return true;
				if(subop&&H.contains(new Integer(code)))
					return true;
			}
		}
		return false;
	}
	
	
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
