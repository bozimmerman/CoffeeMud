package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CMSecurity
{
	private CMSecurity(){}
	private static final long startTime=System.currentTimeMillis();
	private static Hashtable groups=new Hashtable();
	private static Vector compiledSysop=null;
	// supported: AFTER, AHELP, ANNOUNCE, AT, BAN, BEACON, BOOT, MISC (chargen),
	// COPYMOBS, COPYITEMS, COPYROOMS, CMDQUESTS, CMDSOCIALS, CMDROOMS,
	// CMDITEMS, CMDEXITS, CMDAREAS, CMDRACES, CMDCLASSES, NOPURGE, KILLBUGS,
	// KILLIDEAS, KILLTYPOS, CMDCLANS, DUMPFILE, GOTO, LOADUNLOAD, CMDPLAYERS
	// POSSESS, SHUTDOWN, SNOOP, STAT, SYSMSGS, TICKTOCK, TRANSFER, WHERE
	// RESET, RESETUTILS, KILLDEAD, MERGE, IMPORTROOMS, IMPORTMOBS, IMPORTITEMS
	// IMPORTPLAYERS, EXPORT, EXPORTPLAYERS, EXPORTFILE
	// ORDER (includes TAKE, GIVE, DRESS, mob passivity, all follow)
	// I3, ABOVELAW (also law books), WIZINV (includes see WIZINV)
	// CMDMOBS (also prevents walkaways)
	// SUPERSKILL (never fails skills)
	// JOURNALS, PKILL
	// LIST: 
	// (list is also affected by killx, cmdplayers, loadunload, cmdclans, ban, nopurge,
	//  cmditems, cmdmobs, cmdrooms, 
	
	// todo: import, export, merge
	public static void setSysOp(String zapCheck)
	{
		if((zapCheck==null)||(zapCheck.trim().length()==0))
			zapCheck="-ANYCLASS +Archon";
		compiledSysop=MUDZapper.zapperCompile(zapCheck);
	}
	
	public static boolean isASysOp(MOB mob)
	{
		for(int v=0;v<compiledSysop.size();v++)
		{
			Vector V=(Vector)compiledSysop.elementAt(v);
			StringBuffer str=new StringBuffer("");
			for(int v2=0;v2<V.size();v2++)
			{
				if(V.elementAt(v2) instanceof Integer)
					str.append(((Integer)V.elementAt(v2)).intValue()+"/");
				else
				if(V.elementAt(v2) instanceof String)
					str.append(((String)V.elementAt(v2))+"/");
				else
					str.append("?/");
			}
		}
		return MUDZapper.zapperCheckReal(compiledSysop,mob);
	}
	
	
	public static boolean isStaff(MOB mob)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null) return false;
		if(mob.playerStats().getSecurityGroups()==null) return false;
		if(mob.playerStats().getSecurityGroups().size()==0) return false;
		return true;
	}
	public static boolean isAllowedStartsWith(MOB mob, Room room, String code)
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
				for(Iterator i=H.iterator();i.hasNext();)
				{
					String s=(String)i.next();
					if(s.startsWith(code))
						return true;
					if(subop&&s.startsWith("AREA "+code))
						return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isAllowed(MOB mob, Room room, String code)
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
				if(H.contains(code))
					return true;
				if(subop&&H.contains("AREA "+code))
					return true;
			}
		}
		return false;
	}
	public static boolean isAllowedStartsWith(MOB mob, String code)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null) return false;
		Vector V=mob.playerStats().getSecurityGroups();
		if(V==null) return false;
		
		for(int v=0;v<V.size();v++)
		{
			HashSet H=(HashSet)groups.get((String)V.elementAt(v));
			if(H!=null)
			{
				for(Iterator i=H.iterator();i.hasNext();)
				{
					String s=(String)i.next();
					if(s.startsWith(code))
						return true;
				}
			}
		}
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
		{
			boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
			if(!subop) continue;
		
			for(int v=0;v<V.size();v++)
			{
				HashSet H=(HashSet)groups.get((String)V.elementAt(v));
				if(H!=null)
				{
					for(Iterator i=H.iterator();i.hasNext();)
					{
						String s=(String)i.next();
						if(subop&&s.startsWith("AREA "+code))
							return true;
					}
				}
			}
		}
		return false;
	}
	public static boolean isAllowedEverywhere(MOB mob, String code)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null) return false;
		Vector V=mob.playerStats().getSecurityGroups();
		if(V==null) return false;
		
		for(int v=0;v<V.size();v++)
		{
			HashSet H=(HashSet)groups.get((String)V.elementAt(v));
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		return false;
	}
	public static boolean isAllowedAnywhere(MOB mob, String code)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if(mob.playerStats()==null) return false;
		Vector V=mob.playerStats().getSecurityGroups();
		if(V==null) return false;
		
		for(int v=0;v<V.size();v++)
		{
			HashSet H=(HashSet)groups.get((String)V.elementAt(v));
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
		{
			boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
			if(!subop) continue;
		
			for(int v=0;v<V.size();v++)
			{
				HashSet H=(HashSet)groups.get((String)V.elementAt(v));
				if(H!=null)
				{
					if(H.contains("AREA "+code))
						return true;
				}
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
