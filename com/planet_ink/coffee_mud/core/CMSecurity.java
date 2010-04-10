package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.File; // does some cmfile type stuff
import java.util.*;

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
/**
 * 
supported: AFTER, AHELP, ANNOUNCE, AT, BAN, BEACON, BOOT, CHARGEN
COPYMOBS, COPYITEMS, COPYROOMS, CMDQUESTS, CMDSOCIALS, CMDROOMS,
CMDITEMS, CMDEXITS, CMDAREAS, CMDRACES, CMDCLASSES, NOPURGE, KILLBUGS,
KILLIDEAS, KILLTYPOS, CMDCLANS, DUMPFILE, GOTO, LOADUNLOAD, CMDPLAYERS
POSSESS, SHUTDOWN, SNOOP, STAT, SYSMSGS, TICKTOCK, TRANSFER, WHERE
RESET, RESETUTILS, KILLDEAD, MERGE, IMPORTROOMS, IMPORTMOBS, IMPORTITEMS
IMPORTPLAYERS, EXPORT, EXPORTPLAYERS, EXPORTFILE, RESTRING, PURGE, TASKS
ORDER (includes TAKE, GIVE, DRESS, mob passivity, all follow)
I3, ABOVELAW (also law books), WIZINV (includes see WIZINV), CMDABILITIES
CMDMOBS (also prevents walkaways), KILLASSIST, ALLSKILLS, GMODIFY, CATALOG
SUPERSKILL (never fails skills), IMMORT (never dies), MXPTAGS, IDLEOK
JOURNALS, PKILL, SESSIONS, TRAILTO, CMDFACTIONS, COMPONENTS, EXPERTISES, TITLES
FS:relative path from /coffeemud/ -- read/write access to regular file sys
VFS:relative path from /coffeemud/ -- read/write access to virtual file sys
LIST: (affected by killx, cmdplayers, loadunload, cmdclans, ban, nopurge,
cmditems, cmdmobs, cmdrooms, sessions, cmdareas, listadmin, stat
*/ 

@SuppressWarnings("unchecked")
public class CMSecurity
{
    protected final long startTime=System.currentTimeMillis();
    private static CMSecurity[] secs=new CMSecurity[256];
    public CMSecurity()
    {
        super();
        char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(secs==null) secs=new CMSecurity[256];
        if(secs[c]==null) secs[c]=this;
    }
    public static CMSecurity instance()
    {
        CMSecurity p=i();
        if(p==null) p=new CMSecurity();
        return p;
    }
    public static CMSecurity instance(char c){ return secs[c];}
    private static CMSecurity i(){ return secs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}  
    
    protected Hashtable groups=new Hashtable();
    protected Vector compiledSysop=null;
    
    public void markShared() {
        char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(threadCode==MudHost.MAIN_HOST)
            return;
        if(secs[MudHost.MAIN_HOST]==null)
            secs[MudHost.MAIN_HOST]=this;
        else
            secs[threadCode]=secs[MudHost.MAIN_HOST];
    }
    
	public static void setSysOp(String zapCheck)
	{
		if((zapCheck==null)||(zapCheck.trim().length()==0))
			zapCheck="-ANYCLASS +Archon";
		instance().compiledSysop=CMLib.masking().maskCompile(zapCheck);
	}

	
	public static void clearGroups(){ instance().groups.clear();}
	
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
		if(instance().groups.containsKey(name)) 
            i().groups.remove(name);
        i().groups.put(name,set);
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
		addGroup(name,CMParms.parseCommas(set,true));
	}
	
	public static boolean isASysOp(MOB mob)
	{
		return CMLib.masking().maskCheck(i().compiledSysop,mob,true)
				||((mob.soulMate()!=null)
					&&(CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS))
					&&(isASysOp(mob.soulMate())));
	}
	
	
	public static boolean isStaff(MOB mob)
	{
		if(isASysOp(mob)) return true;
		if(mob==null) return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		if((mob.playerStats().getSecurityGroups().size()==0)
        &&(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).size()==0))
            return false;
		return true;
	}
    
    public static Vector getAccessibleDirs(MOB mob, Room room)
    {
        Vector DIRSV=new Vector();
        if(isASysOp(mob)){ DIRSV.addElement("/"); return DIRSV; }
        if(mob==null) return DIRSV;
        if((mob.playerStats()==null)
        ||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
            return DIRSV;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
        if(V.size()==0) return DIRSV;
        boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String set=null;
        for(int v=0;v<V.size();v++)
        {
            set=((String)V.elementAt(v)).toUpperCase();
            if(set.startsWith("FS:"))
            {
                set=set.substring(3).trim();
                if(set.startsWith("AREA "))
                {
                    if(!subop)
                        continue;
                    DIRSV.addElement("//"+set.substring(4).trim());
                }
                else
                    DIRSV.addElement("//"+set);
            }
            else
            if(set.startsWith("VFS:"))
            {
                set=set.substring(4).trim();
                if(set.startsWith("AREA "))
                {
                    if(!subop) continue;
                    DIRSV.addElement("::"+set.substring(4).trim());
                }
                else
                    DIRSV.addElement("::"+set);
            }
            else
            {
                HashSet H=(HashSet)i().groups.get(set);
                if(H==null) continue;
                for(Iterator i=H.iterator();i.hasNext();)
                {
                    set=((String)i.next()).toUpperCase();
                    if(set.startsWith("FS:"))
                    {
                        set=set.substring(3).trim();
                        if(set.startsWith("AREA "))
                        {
                            if(!subop)
                                continue;
                            DIRSV.addElement("//"+set.substring(4).trim());
                        }
                        else
                            DIRSV.addElement("//"+set);
                    }
                    else
                    if(set.startsWith("VFS:"))
                    {
                        set=set.substring(4).trim();
                        if(set.startsWith("AREA "))
                        {
                            if(!subop) continue;
                            DIRSV.addElement("::"+set.substring(4).trim());
                        }
                        else
                            DIRSV.addElement("::"+set);
                    }
                }
            }
        }
        String dir=null;
        for(int d=0;d<DIRSV.size();d++)
        {
            dir=(String)DIRSV.elementAt(d);
            if(dir.startsWith("//"))
            {
                dir=dir.substring(2);
                String path="";
                String subPath=null;
                while(dir.startsWith("/")) dir=dir.substring(1);
                while(dir.length()>0)
                {
                    while(dir.startsWith("/")) dir=dir.substring(1);
                    int x=dir.indexOf('/');
                    subPath=dir;
                    if(x>0)
                    { 
                        subPath=dir.substring(0,x).trim(); 
                        dir=dir.substring(x+1).trim();
                    }
                    else
                    {
                        subPath=dir.trim();
                        dir="";
                    }
                    CMFile F=new CMFile(path,null,true,false);
                    if((F.exists())&&(F.canRead())&&(F.isDirectory()))
                    {
                        String[] files=F.list();
                        for(int f=0;f<files.length;f++)
                            if(files[f].equalsIgnoreCase(subPath))
                            {
                                if(path.length()>0)
                                    path+="/";
                                path+=files[f];
                                break;
                            }
                    }
                }
                DIRSV.setElementAt("//"+path,d);
            }
        }
        return DIRSV;
    }
    
    public static boolean hasAccessibleDir(MOB mob, Room room)
    {
        if(isASysOp(mob)) return true;
        if(mob==null) return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
        if(V.size()==0) return false;
        boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String set=null;
        for(int v=0;v<V.size();v++)
        {
            set=((String)V.elementAt(v)).toUpperCase();
            if(set.startsWith("FS:"))
            {
                set=set.substring(3).trim();
                if(set.startsWith("AREA ")&&(!subop))
                    continue;
                return true;
            }
            else
            if(set.startsWith("VFS:"))
            {
                set=set.substring(4).trim();
                if(set.startsWith("AREA ")&&(!subop))
                    continue;
                return true;
            }
            else
            {
                HashSet H=(HashSet)i().groups.get(set);
                if(H==null) continue;
                for(Iterator i=H.iterator();i.hasNext();)
                {
                    set=((String)i.next()).toUpperCase();
                    if(set.startsWith("FS:"))
                    {
                        set=set.substring(3).trim();
                        if(set.startsWith("AREA ")&&(!subop))
                            continue;
                        return true;
                    }
                    else
                    if(set.startsWith("VFS:"))
                    {
                        set=set.substring(4).trim();
                        if(set.startsWith("AREA ")&&(!subop))
                            continue;
                        return true;
                    }
                    else
                        continue;
                }
                continue;
            }
        }
        return false;
    }
    
    public static boolean canTraverseDir(MOB mob, Room room, String path)
    {
        if(isASysOp(mob)) return true;
        if(mob==null) return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
        if(V.size()==0) return false;
        path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
        if(path.equals("/")||path.equals(".")) path="";
        String areaPath=("AREA "+path).trim();
        String pathSlash=path+"/";
        boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String set=null;
        String setSlash=null;
        for(int v=0;v<V.size();v++)
        {
            set=((String)V.elementAt(v)).toUpperCase();
            if(set.startsWith("FS:"))
                set=set.substring(3).trim();
            else
            if(set.startsWith("VFS:"))
                set=set.substring(4).trim();
            else
            {
                HashSet H=(HashSet)i().groups.get(set);
                if(H==null) continue;
                for(Iterator i=H.iterator();i.hasNext();)
                {
                    set=((String)i.next()).toUpperCase();
                    if(set.startsWith("FS:"))
                        set=set.substring(3).trim();
                    else
                    if(set.startsWith("VFS:"))
                        set=set.substring(4).trim();
                    else
                        continue;
                    if((set.length()==0)||(subop&&set.equals("AREA"))||(path.length()==0)) 
                        return true;
                    setSlash=set.endsWith("/")?set:set+"/";
                    if(set.startsWith(pathSlash)
                    ||path.startsWith(setSlash)
                    ||set.equals(path)
                    ||(subop&&areaPath.startsWith(setSlash))
                    ||(subop&&("AREA "+set).startsWith(pathSlash))
                    ||(subop&&("AREA "+set).equals(path)))
                        return true;
                }
                continue;
            }
            if((set.length()==0)||(path.length()==0)) return true;
            if(set.startsWith("/")) set=set.substring(1);
            if(set.startsWith(pathSlash)
            ||path.startsWith(set+"/")
            ||set.equals(path)
            ||(subop&&areaPath.startsWith(set+"/"))
            ||(subop&&("AREA "+set).startsWith(pathSlash))
            ||(subop&&("AREA "+set).equals(path)))
               return true;
        }
        return false;
    }
    
    public static boolean canAccessFile(MOB mob, Room room, String path, boolean isVFS)
    {
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
        if(V.size()==0) return false;
        path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
        if(path.equals("/")||path.equals(".")) path="";
        boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String set=null;
        String setSlash=null;
        for(int v=0;v<V.size();v++)
        {
            set=((String)V.elementAt(v)).toUpperCase();
            if(set.startsWith("FS:"))
                set=set.substring(3).trim();
            else
            if(set.startsWith("VFS:"))
            {
                if(!isVFS) continue;
                set=set.substring(4).trim();
            }
            else
            {
                HashSet H=(HashSet)i().groups.get(set);
                if(H==null) continue;
                for(Iterator i=H.iterator();i.hasNext();)
                {
                    set=((String)i.next()).toUpperCase();
                    if(set.startsWith("FS:"))
                        set=set.substring(3).trim();
                    else
                    if(set.startsWith("VFS:"))
                    {
                        if(!isVFS) continue;
                        set=set.substring(4).trim();
                    }
                    else
                        continue;
                    if((set.length()==0)||(subop&&set.equals("AREA"))) 
                        return true;
                    if(set.startsWith("/")) set=set.substring(1);
                    setSlash=set.endsWith("/")?set:set+"/";
                    if(path.startsWith(setSlash)
                    ||(path.equals(set))
                    ||(subop&&("AREA "+path).startsWith(setSlash))
                    ||(subop&&("AREA "+path).equals(set)))
                        return true;
                }
                continue;
            }
            if(set.length()==0) return true;
            if(set.startsWith("/")) set=set.substring(1);
            setSlash=set.endsWith("/")?set:set+"/";
            if(path.startsWith(setSlash)
            ||(path.equals(set))
            ||(subop&&("AREA "+path).startsWith(setSlash))
            ||(subop&&("AREA "+path).equals(set)))
               return true;
        }
        return false;
    }
	
	public static Vector getSecurityCodes(MOB mob, Room room)
	{
        if((mob==null)||(mob.playerStats()==null)) return new Vector();
		Vector codes=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),codes);
		HashSet tried=new HashSet();
		for(Enumeration e=i().groups.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			codes.addElement(key);
			HashSet H=(HashSet)i().groups.get(key);
			for(Iterator i=H.iterator();i.hasNext();)
			{
				String s=(String)i.next();
				if(!tried.contains(s))
				{
					tried.add(s);
					if(isAllowed(mob,room,s))
					{
						if(s.startsWith("AREA ")) 
							s=s.substring(5).trim();
						if(!codes.contains(s))
							codes.addElement(s);
					}
				}
			}
		}
		return codes;
	}
	
	
	public static boolean isAllowedStartsWith(MOB mob, Room room, String code)
	{
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.startsWith(code)||(subop&&set.startsWith("AREA "+code)))
			   return true;
			HashSet H=(HashSet)i().groups.get(set);
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
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code)||((subop)&&(set.equals("AREA "+code))))
			   return true;
			HashSet H=(HashSet)i().groups.get(set);
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
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.startsWith(code))
			   return true;
			HashSet H=(HashSet)i().groups.get(set);
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
		for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
		{
			boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
			if(!subop) continue;
		
			for(int v=0;v<V.size();v++)
			{
				String set=(String)V.elementAt(v);
				if(set.startsWith("AREA "+code))
				   return true;
				HashSet H=(HashSet)i().groups.get(set);
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
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code)) return true;
			HashSet H=(HashSet)i().groups.get(set);
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
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        Vector V=(Vector)mob.playerStats().getSecurityGroups().clone();
        CMParms.addToVector(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()),V);
		if(V.size()==0) return false;
		
		for(int v=0;v<V.size();v++)
		{
			String set=(String)V.elementAt(v);
			if(set.equals(code))
			   return true;
			HashSet H=(HashSet)i().groups.get(set);
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
		{
			boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
			if(!subop) continue;
		
			for(int v=0;v<V.size();v++)
			{
				String set=(String)V.elementAt(v);
				if(set.equals("AREA "+code)) return true;
				HashSet H=(HashSet)i().groups.get(set);
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
    
    public static void approveJScript(String approver, long hashCode)
    {
        if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)!=1)
            return;
        Hashtable approved=CMSecurity.getApprovedJScriptTable();
        if(approved.containsKey(Long.valueOf(hashCode)))
            approved.remove(Long.valueOf(hashCode));
        approved.put(Long.valueOf(hashCode),approver);
        StringBuffer newApproved=new StringBuffer("");
        for(Enumeration e=approved.keys();e.hasMoreElements();)
        {
            Long L=(Long)e.nextElement();
            Object O=approved.get(L);
            if(O instanceof String)
                newApproved.append(L.toString()+"="+((String)O)+"\n");
        }
        Resources.saveFileResource("::jscripts.ini",null,newApproved);
    }
    
    public static Hashtable getApprovedJScriptTable()
    {
        Hashtable approved=(Hashtable)Resources.getResource("APPROVEDJSCRIPTS");
        if(approved==null)
        {
            approved=new Hashtable();
            Resources.submitResource("APPROVEDJSCRIPTS",approved);
            Vector jscripts=Resources.getFileLineVector(Resources.getFileResource("jscripts.ini",false));
            if((jscripts!=null)&&(jscripts.size()>0))
            {
                for(int i=0;i<jscripts.size();i++)
                {
                    String s=(String)jscripts.elementAt(i);
                    int x=s.indexOf("=");
                    if(x>0)
                        approved.put(Long.valueOf(CMath.s_long(s.substring(0,x))),s.substring(x+1));
                }
            }
        }
        return approved;
    }
    
    public static boolean isApprovedJScript(StringBuffer script)
    {
        if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)==2)
            return true;
        if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)==0)
            return false;
        Hashtable approved=CMSecurity.getApprovedJScriptTable();
        Long hashCode=Long.valueOf(script.toString().hashCode());
        Object approver=approved.get(hashCode);
        if(approver==null)
        {
            approved.put(hashCode,script);
            return false;
        }
        return approver instanceof String;
    }
	
	public static void setDebugVars(String vars)
	{
		Vector V=CMParms.parseCommas(vars.toUpperCase(),true);
		dbgVars.clear();
		for(int v=0;v<V.size();v++)
			dbgVars.add(((String)V.elementAt(v)).trim());
	}
	
	public static void setDisableVars(String vars)
	{
		Vector V=CMParms.parseCommas(vars.toUpperCase(),true);
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
	public static void setSaveFlags(String flags)
	{
		Vector V=CMParms.parseCommas(flags.toUpperCase(),true);
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
	
    protected static HashSet disVars=new HashSet();
    protected static HashSet dbgVars=new HashSet();
    protected static HashSet saveFlags=new HashSet();

	public static long getStartTime(){return i().startTime;}
	
    public static boolean isBanned(String login)
    {
        if((login==null)||(login.length()<=0))
            return false;
        login=login.toUpperCase();
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
            Resources.updateFileResource("::banned.ini",newBanned);
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
            Resources.updateFileResource("::banned.ini",newBanned);
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
        Resources.updateFileResource("::banned.ini",str);
        return -1;
    }
}
