package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class CMSecurity
{
	private CMSecurity(){}
	private static final long startTime=System.currentTimeMillis();
	private static Hashtable groups=new Hashtable();
	private static Vector compiledSysop=null;
	// supported: AFTER, AHELP, ANNOUNCE, AT, BAN, BEACON, BOOT, CHARGEN
	// COPYMOBS, COPYITEMS, COPYROOMS, CMDQUESTS, CMDSOCIALS, CMDROOMS,
	// CMDITEMS, CMDEXITS, CMDAREAS, CMDRACES, CMDCLASSES, NOPURGE, KILLBUGS,
	// KILLIDEAS, KILLTYPOS, CMDCLANS, DUMPFILE, GOTO, LOADUNLOAD, CMDPLAYERS
	// POSSESS, SHUTDOWN, SNOOP, STAT, SYSMSGS, TICKTOCK, TRANSFER, WHERE
	// RESET, RESETUTILS, KILLDEAD, MERGE, IMPORTROOMS, IMPORTMOBS, IMPORTITEMS
	// IMPORTPLAYERS, EXPORT, EXPORTPLAYERS, EXPORTFILE, RESTRING, PURGE, TASKS
	// ORDER (includes TAKE, GIVE, DRESS, mob passivity, all follow)
	// I3, ABOVELAW (also law books), WIZINV (includes see WIZINV)
	// CMDMOBS (also prevents walkaways), KILLASSIST
	// SUPERSKILL (never fails skills), IMMORT (never dies), MXPTAGS
	// JOURNALS, PKILL, SESSIONS, TRAILTO, CMDFACTIONS
	// LIST: (affected by killx, cmdplayers, loadunload, cmdclans, ban, nopurge,
	//		cmditems, cmdmobs, cmdrooms, sessions, cmdareas, listadmin, stat
	// 
	
	public static void setSysOp(String zapCheck)
	{
		if((zapCheck==null)||(zapCheck.trim().length()==0))
			zapCheck="-ANYCLASS +Archon";
		compiledSysop=MUDZapper.zapperCompile(zapCheck);
	}

	
	public static void clearGroups(){ groups.clear();}
	
	public static void parseGroups(Properties page)
	{
		clearGroups();
		if(page==null) return;
		for(Enumeration e=page.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key.startsWith("GROUP_"))
			{
				addGroup(key.substring(6),(String)page.get(key));
			}
		}
	}
	
	public static void addGroup(String name, HashSet set)
	{
		name=name.toUpperCase().trim();
		if(groups.containsKey(name)) groups.remove(name);
		groups.put(name,set);
	}
	
	public static void addGroup(String name, Vector set)
	{
		HashSet H=new HashSet();
		for(int v=0;v<set.size();v++)
		{
			String s=(String)set.elementAt(v);
			H.add(s.trim().toUpperCase());
		}
		addGroup(name,H);
	}
	public static void addGroup(String name, String set)
	{
		addGroup(name,Util.parseCommas(set,true));
	}
	
	public static boolean isASysOp(MOB mob)
	{
		return MUDZapper.zapperCheckReal(compiledSysop,mob);
	}
	
	
	public static boolean isStaff(MOB mob)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if((mob.playerStats()==null)||(mob.soulMate()!=null)) return false;
		if((mob.playerStats().getSecurityGroups().size()==0)
        &&(mob.charStats().getCurrentClass().getSecurityGroups(mob.charStats().getCurrentClassLevel()).size()==0))
            return false;
		return true;
	}
	
	public static Vector getSecurityCodes(MOB mob, Room room)
	{
		Vector codes=new Vector();
		HashSet tried=new HashSet();
		for(Enumeration e=groups.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			codes.addElement(key);
			HashSet H=(HashSet)groups.get(key);
			for(Iterator i=H.iterator();i.hasNext();)
			{
				String s=(String)i.next();
				if((!tried.contains(s))&&(!codes.contains(s)))
				{
					tried.add(s);
					if(isAllowed(mob,room,s))
					{
						if(s.startsWith("AREA ")) 
							s=s.substring(5).trim();
						codes.addElement(s);
					}
				}
			}
		}
		return codes;
	}
	
	
	public static boolean isAllowedStartsWith(MOB mob, Room room, String code)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if((mob.playerStats()==null)||(mob.soulMate()!=null)) return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        Util.addToVector(mob.charStats().getCurrentClass().getSecurityGroups(mob.charStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.startsWith(code)||(subop&&set.startsWith("AREA "+code)))
			   return true;
			HashSet H=(HashSet)groups.get(set);
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
		if((mob.playerStats()==null)||(mob.soulMate()!=null)) return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        Util.addToVector(mob.charStats().getCurrentClass().getSecurityGroups(mob.charStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code)||((subop)&&(set.equals("AREA "+code))))
			   return true;
			HashSet H=(HashSet)groups.get(set);
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
		if((mob.playerStats()==null)||(mob.soulMate()!=null)) return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        Util.addToVector(mob.charStats().getCurrentClass().getSecurityGroups(mob.charStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.startsWith(code))
			   return true;
			HashSet H=(HashSet)groups.get(set);
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
				String set=(String)V.elementAt(v);
				if(set.startsWith("AREA "+code))
				   return true;
				HashSet H=(HashSet)groups.get(set);
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
		if((mob.playerStats()==null)||(mob.soulMate()!=null)) return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        Util.addToVector(mob.charStats().getCurrentClass().getSecurityGroups(mob.charStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code)) return true;
			HashSet H=(HashSet)groups.get(set);
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
		if((mob.playerStats()==null)||(mob.soulMate()!=null)) return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        Util.addToVector(mob.charStats().getCurrentClass().getSecurityGroups(mob.charStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code))
			   return true;
			HashSet H=(HashSet)groups.get(set);
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
				String set=(String)V.elementAt(v);
				if(set.equals("AREA "+code)) return true;
				HashSet H=(HashSet)groups.get(set);
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
	
	public static boolean isSaveFlag(String key)
	{ return (saveFlags.size()>0)&&saveFlags.contains(key);}
	
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
			disVars.add(V.elementAt(v));
	}
	public static void setDisableVar(String var, boolean delete)
	{
		if((var!=null)&&(delete)&&(disVars.size()>0))
			disVars.remove(var);
		else
		if((var!=null)&&(!delete))
			disVars.add(var);
	}
	public static void setSaveFlags(String flags)
	{
		Vector V=Util.parseCommas(flags.toUpperCase(),true);
		saveFlags.clear();
		for(int v=0;v<V.size();v++)
		    saveFlags.add(V.elementAt(v));
	}
	public static void setSaveFlag(String flag, boolean delete)
	{
		if((flag!=null)&&(delete)&&(saveFlags.size()>0))
		    saveFlags.remove(flag);
		else
		if((flag!=null)&&(!delete))
		    saveFlags.add(flag);
	}
	
	private static HashSet disVars=new HashSet();
	private static HashSet dbgVars=new HashSet();
	private static HashSet saveFlags=new HashSet();

	public static long getStartTime(){return startTime;}
	
    public static boolean isBanned(String login)
    {
        if((login==null)||(login.length()<=0))
            return false;
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

    
    public static void unban(String unBanMe)
    {
        if((unBanMe==null)||(unBanMe.length()<=0))
            return;
        StringBuffer newBanned=new StringBuffer("");
        Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
        if((banned!=null)&&(banned.size()>0))
        {
            for(int b=0;b<banned.size();b++)
            {
                String B=(String)banned.elementAt(b);
                if((!B.equals(unBanMe))&&(B.trim().length()>0))
                    newBanned.append(B+"\n");
            }
            Resources.updateResource("banned.ini",newBanned);
            Resources.saveFileResource("banned.ini");
        }
    }
    
    public static void unban(int unBanMe)
    {
        StringBuffer newBanned=new StringBuffer("");
        Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
        if((banned!=null)&&(banned.size()>0))
        {
            for(int b=0;b<banned.size();b++)
            {
                String B=(String)banned.elementAt(b);
                if(((b+1)!=unBanMe)&&(B.trim().length()>0))
                    newBanned.append(B+"\n");
            }
            Resources.updateResource("banned.ini",newBanned);
            Resources.saveFileResource("banned.ini");
        }
    }
    
    public static int ban(String banMe)
    {
        if((banMe==null)||(banMe.length()<=0))
            return -1;
        Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
        if((banned!=null)&&(banned.size()>0))
        for(int b=0;b<banned.size();b++)
        {
            String B=(String)banned.elementAt(b);
            if(B.equals(banMe))
                return b;
        }
        StringBuffer str=Resources.getFileResource("banned.ini",false);
        if(banMe.trim().length()>0) str.append(banMe+"\n");
        Resources.updateResource("banned.ini",str);
        Resources.saveFileResource("banned.ini");
        return -1;
    }
}
