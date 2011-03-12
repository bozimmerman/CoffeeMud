package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.File; // does some cmfile type stuff
import java.util.*;

/* 
   Copyright 2000-2011 Bo Zimmerman

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
    protected final long 			 	startTime=System.currentTimeMillis();
    protected final static Set<String> 	disVars=new HashSet<String>();
    protected final static Set<String> 	dbgVars=new HashSet<String>();
    protected final static Set<String> 	saveFlags=new HashSet<String>();
    protected final Map<String,Set<String>> groups=new Hashtable<String,Set<String>>();
    protected MaskingLibrary.CompiledZapperMask compiledSysop=null;
    protected static boolean 		debuggingEverything=false;
    private final static CMSecurity[] secs=new CMSecurity[256];
    
    public CMSecurity()
    {
        super();
        char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(secs[c]==null) secs[c]=this;
    }
    public static final CMSecurity instance()
    {
    	final CMSecurity p=i();
        if(p==null) return new CMSecurity();
        return p;
    }
    public static final CMSecurity instance(char c){ return secs[c];}
    private static final CMSecurity i(){ return secs[Thread.currentThread().getThreadGroup().getName().charAt(0)];}  
    
    public final void markShared() 
    {
    	final char threadCode=Thread.currentThread().getThreadGroup().getName().charAt(0);
        if(threadCode==MudHost.MAIN_HOST)
            return;
        if(secs[MudHost.MAIN_HOST]==null)
            secs[MudHost.MAIN_HOST]=this;
        else
            secs[threadCode]=secs[MudHost.MAIN_HOST];
    }
    
	public static final void setSysOp(String zapCheck)
	{
		if((zapCheck==null)||(zapCheck.trim().length()==0))
			zapCheck="-ANYCLASS +Archon";
		instance().compiledSysop=CMLib.masking().maskCompile(zapCheck);
	}

	
	public static final void clearGroups(){ instance().groups.clear();}
	
	public static final void parseGroups(final Properties page)
	{
		clearGroups();
		if(page==null) return;
		for(Enumeration<Object> e=page.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key.startsWith("GROUP_"))
			{
				addGroup(key.substring(6),(String)page.get(key));
			}
		}
	}
	
	public static final void addGroup(String name, final Set<String> set)
	{
		name=name.toUpperCase().trim();
		if(instance().groups.containsKey(name)) 
            i().groups.remove(name);
        i().groups.put(name,set);
	}
	
	public static final void addGroup(String name, final List<String> set)
	{
		final Set<String> H=new HashSet<String>();
		for(int v=0;v<set.size();v++)
		{
			String s=(String)set.get(v);
			H.add(s.trim().toUpperCase());
		}
		addGroup(name,H);
	}
	public static final void addGroup(String name, final String set)
	{
		addGroup(name,CMParms.parseCommas(set,true));
	}
	
	public static final boolean isASysOp(final MOB mob)
	{
		return CMLib.masking().maskCheck(i().compiledSysop,mob,true)
				||((mob.soulMate()!=null)
					&&(CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS))
					&&(isASysOp(mob.soulMate())));
	}
	
	public static final boolean isASysOp(final PlayerLibrary.ThinPlayer mob)
	{
		return CMLib.masking().maskCheck(i().compiledSysop,mob);
	}
	
	
	public static final boolean isStaff(final MOB mob)
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
    
    public static final List<String> getAccessibleDirs(final MOB mob, final Room room)
    {
    	final List<String> DIRSV=new Vector<String>();
        if(isASysOp(mob)){ DIRSV.add("/"); return DIRSV; }
        if(mob==null) return DIRSV;
        if((mob.playerStats()==null)
        ||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
            return DIRSV;
        boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String set=null;
		Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=((String)g.next()).toUpperCase();
            if(set.startsWith("FS:"))
            {
                set=set.substring(3).trim();
                if(set.startsWith("AREA "))
                {
                    if(!subop)
                        continue;
                    DIRSV.add("//"+set.substring(4).trim());
                }
                else
                    DIRSV.add("//"+set);
            }
            else
            if(set.startsWith("VFS:"))
            {
                set=set.substring(4).trim();
                if(set.startsWith("AREA "))
                {
                    if(!subop) continue;
                    DIRSV.add("::"+set.substring(4).trim());
                }
                else
                    DIRSV.add("::"+set);
            }
            else
            {
                Set<String> H=i().groups.get(set);
                if(H==null) continue;
                for(Iterator<String> i=H.iterator();i.hasNext();)
                {
                    set=((String)i.next()).toUpperCase();
                    if(set.startsWith("FS:"))
                    {
                        set=set.substring(3).trim();
                        if(set.startsWith("AREA "))
                        {
                            if(!subop)
                                continue;
                            DIRSV.add("//"+set.substring(4).trim());
                        }
                        else
                            DIRSV.add("//"+set);
                    }
                    else
                    if(set.startsWith("VFS:"))
                    {
                        set=set.substring(4).trim();
                        if(set.startsWith("AREA "))
                        {
                            if(!subop) continue;
                            DIRSV.add("::"+set.substring(4).trim());
                        }
                        else
                            DIRSV.add("::"+set);
                    }
                }
            }
        }
        String dir=null;
        for(int d=0;d<DIRSV.size();d++)
        {
            dir=(String)DIRSV.get(d);
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
                DIRSV.set(d,"//"+path);
            }
        }
        return DIRSV;
    }
    
	public static final boolean hasAccessibleDir(final MOB mob, final Room room)
    {
        if(isASysOp(mob)) return true;
        if(mob==null) return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String set=null;
        final Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=((String)g.next()).toUpperCase();
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
            	final Set<String> H=i().groups.get(set);
                if(H==null) continue;
                for(Iterator<String> i=H.iterator();i.hasNext();)
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
    
    public static final boolean canTraverseDir(MOB mob, Room room, String path)
    {
        if(isASysOp(mob)) return true;
        if(mob==null) return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
        if(path.equals("/")||path.equals(".")) path="";
        final String areaPath=("AREA "+path).trim();
        final String pathSlash=path+"/";
        boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String set=null;
        String setSlash=null;
		Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(final Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=((String)g.next()).toUpperCase();
            if(set.startsWith("FS:"))
                set=set.substring(3).trim();
            else
            if(set.startsWith("VFS:"))
                set=set.substring(4).trim();
            else
            {
                Set<String> H=i().groups.get(set);
                if(H==null) continue;
                for(Iterator<String> i=H.iterator();i.hasNext();)
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
    
    public static final boolean canAccessFile(final MOB mob, final Room room, String path, final boolean isVFS)
    {
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
        if(path.equals("/")||path.equals(".")) path="";
        final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
        String setSlash=null;
        String set=null;
		Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(final Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=((String)g.next()).toUpperCase();
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
            	final Set<String> H=i().groups.get(set);
                if(H==null) continue;
                for(Iterator<String> i=H.iterator();i.hasNext();)
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
	
	public static final Iterator<String> getSecurityCodes(final MOB mob, final Room room)
	{
        if((mob==null)||(mob.playerStats()==null)) return EmptyIterator.STRINSTANCE;
        final List<String> codes = mob.playerStats().getSecurityGroups();
        final MultiIterator<String> it=new MultiIterator<String>();
        it.add(codes.iterator());
        it.add(mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator());
        final Set<String> tried=new HashSet<String>();
        final List<String> misc=new ArrayList<String>();
		for(final String key : i().groups.keySet())
		{
			misc.add(key);
			Set<String> H=i().groups.get(key);
			for(Iterator<String> i=H.iterator();i.hasNext();)
			{
				String s=(String)i.next();
				if(!tried.contains(s))
				{
					tried.add(s);
					if(isAllowed(mob,room,s))
					{
						if(s.startsWith("AREA ")) 
							s=s.substring(5).trim();
						if(!misc.contains(s))
							misc.add(s);
					}
				}
			}
		}
		it.add(misc.iterator());
		return it;
	}
	
	
	public static final boolean isAllowedStartsWith(final MOB mob, final Room room, final String code)
	{
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		
        String set=null;
        final Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(final Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=(String)g.next();
			if(set.startsWith(code)||(subop&&set.startsWith("AREA "+code)))
			   return true;
			final Set<String> H=i().groups.get(set);
			if(H!=null)
			{
				for(Iterator<String> i=H.iterator();i.hasNext();)
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
	
	public static final boolean isAllowed(final MOB mob, final Room room, final String code)
	{
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		
        String set=null;
        final Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(final Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=(String)g.next();
			if(set.equals(code)||((subop)&&(set.equals("AREA "+code))))
			   return true;
			final Set<String> H=i().groups.get(set);
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
	
	public static final boolean isAllowedStartsWith(final MOB mob, final String code)
	{
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
							 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(final Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			final String set=(String)g.next();
			if(set.startsWith(code))
			   return true;
			final Set<String> H=i().groups.get(set);
			if(H!=null)
			{
				for(final Iterator<String> i=H.iterator();i.hasNext();)
					if(i.next().startsWith(code))
						return true;
			}
		}
		for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			final boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
			if(!subop) continue;
		
	        String set=null;
			allGroups=new Iterator[]{mob.playerStats().getSecurityGroups().iterator(),
					 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
			for(Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
			{
				set=(String)g.next();
				if(set.startsWith("AREA "+code))
				   return true;
				final Set<String> H=i().groups.get(set);
				if((H!=null)&&(subop))
					for(final Iterator<String> i=H.iterator();i.hasNext();)
						if(i.next().startsWith("AREA "+code))
							return true;
			}
		}
		return false;
	}
	
	public static final boolean isAllowedEverywhere(final MOB mob, final String code)
	{
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        String set=null;
        final Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(final Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=(String)g.next();
			if(set.equals(code)) return true;
			final Set<String> H=i().groups.get(set);
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		return false;
	}
	
	public static final boolean isAllowedAnywhere(final MOB mob, final String code)
	{
        if(mob==null) return false;
        if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
        String set=null;
        Iterator[] allGroups={mob.playerStats().getSecurityGroups().iterator(),
				 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
		for(final Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
		{
			set=(String)g.next();
			if(set.equals(code))
			   return true;
			final Set<String> H=i().groups.get(set);
			if(H!=null)
			{
				if(H.contains(code))
					return true;
			}
		}
		for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			final boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
			if(!subop) continue;
		
			allGroups=new Iterator[]{mob.playerStats().getSecurityGroups().iterator(),
					 mob.baseCharStats().getCurrentClass().getSecurityGroups(mob.baseCharStats().getCurrentClassLevel()).iterator()};
			for(Iterator<String> g=new MultiIterator<String>(allGroups);g.hasNext();)
			{
				set=(String)g.next();
				if(set.equals("AREA "+code)) return true;
				final Set<String> H=i().groups.get(set);
				if(H!=null)
				{
					if(H.contains("AREA "+code))
						return true;
				}
			}
		}
		return false;
	}
	

	public static Enumeration<String> getDebugEnum() { return new IteratorEnumeration<String>(dbgVars.iterator());}
	public static final boolean isDebugging(final String key)
	{ return ((dbgVars.size()>0)&&dbgVars.contains(key))||debuggingEverything;}
	
	public static Enumeration<String> getDisablesEnum() { return new IteratorEnumeration<String>(disVars.iterator());}
	public static final boolean isDisabled(final String key)
	{ return (disVars.size()>0)&&disVars.contains(key);}
	
	public static final boolean isSaveFlag(final String key)
	{ return (saveFlags.size()>0)&&saveFlags.contains(key);}
    
    public static final void approveJScript(final String approver, final long hashCode)
    {
        if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)!=1)
            return;
        final Map<Long,String> approved=CMSecurity.getApprovedJScriptTable();
        if(approved.containsKey(Long.valueOf(hashCode)))
            approved.remove(Long.valueOf(hashCode));
        approved.put(Long.valueOf(hashCode),approver);
        final StringBuffer newApproved=new StringBuffer("");
        for(final Long L : approved.keySet())
        {
            Object O=approved.get(L);
            if(O instanceof String)
                newApproved.append(L.toString()+"="+((String)O)+"\n");
        }
        Resources.saveFileResource("::jscripts.ini",null,newApproved);
    }
    
    public static final Map<Long,String> getApprovedJScriptTable()
    {
        Map<Long,String> approved=(Map<Long, String>)Resources.getResource("APPROVEDJSCRIPTS");
        if(approved==null)
        {
            approved=new Hashtable<Long,String>();
            Resources.submitResource("APPROVEDJSCRIPTS",approved);
            List<String> jscripts=Resources.getFileLineVector(Resources.getFileResource("jscripts.ini",false));
            if((jscripts!=null)&&(jscripts.size()>0))
            {
                for(int i=0;i<jscripts.size();i++)
                {
                    String s=(String)jscripts.get(i);
                    int x=s.indexOf('=');
                    if(x>0)
                        approved.put(Long.valueOf(CMath.s_long(s.substring(0,x))),s.substring(x+1));
                }
            }
        }
        return approved;
    }
    
    public static final boolean isApprovedJScript(final StringBuffer script)
    {
        if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)==2)
            return true;
        if(CMProps.getIntVar(CMProps.SYSTEMI_JSCRIPTS)==0)
            return false;
        Map<Long,String> approved=CMSecurity.getApprovedJScriptTable();
        final Long hashCode=Long.valueOf(script.toString().hashCode());
        final Object approver=approved.get(hashCode);
        if(approver==null)
        {
            approved.put(hashCode,script.toString());
            return false;
        }
        return approver instanceof String;
    }
	
	public static final void setDebugVars(final String vars)
	{
		final List<String> V=CMParms.parseCommas(vars.toUpperCase(),true);
		dbgVars.clear();
		for(int v=0;v<V.size();v++)
			dbgVars.add(((String)V.get(v)).trim());
		debuggingEverything = dbgVars.contains("EVERYTHING");
	}
	
	public static final void setDisableVars(final String vars)
	{
		final List<String> V=CMParms.parseCommas(vars.toUpperCase(),true);
		disVars.clear();
		for(int v=0;v<V.size();v++)
			disVars.add((String)V.get(v));
	}
	
	public static final void setDebugVar(final String var, final boolean delete)
	{
		if((var!=null)&&(delete)&&(dbgVars.size()>0))
			dbgVars.remove(var.toUpperCase());
		else
		if((var!=null)&&(!delete))
			dbgVars.add(var.toUpperCase());
	}
	
	public static final void setDisableVar(final String var, final boolean delete)
	{
		if((var!=null)&&(delete)&&(disVars.size()>0))
			disVars.remove(var.toUpperCase());
		else
		if((var!=null)&&(!delete))
			disVars.add(var.toUpperCase());
	}
	
	public static final void setSaveFlags(final String flags)
	{
		final List<String> V=CMParms.parseCommas(flags.toUpperCase(),true);
		saveFlags.clear();
		for(int v=0;v<V.size();v++)
		    saveFlags.add(V.get(v));
	}
	
	public static final void setSaveFlag(final String flag, final boolean delete)
	{
		if((flag!=null)&&(delete)&&(saveFlags.size()>0))
		    saveFlags.remove(flag);
		else
		if((flag!=null)&&(!delete))
		    saveFlags.add(flag);
	}
	
	public static final long getStartTime(){return i().startTime;}
	
    public static final boolean isBanned(final String login)
    {
        if((login==null)||(login.length()<=0))
            return false;
        final String uplogin=login.toUpperCase();
        final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
        if((banned!=null)&&(banned.size()>0))
        for(int b=0;b<banned.size();b++)
        {
            String str=(String)banned.get(b);
            if(str.length()>0)
            {
                if(str.equals("*")||((str.indexOf('*')<0))&&(str.equals(uplogin))) return true;
                else
                if(str.startsWith("*")&&str.endsWith("*")&&(uplogin.indexOf(str.substring(1,str.length()-1))>=0)) return true;
                else
                if(str.startsWith("*")&&(uplogin.endsWith(str.substring(1)))) return true;
                else
                if(str.endsWith("*")&&(uplogin.startsWith(str.substring(0,str.length()-1)))) return true;
            }
        }
        return false;
    }

    
    public static final void unban(final String unBanMe)
    {
        if((unBanMe==null)||(unBanMe.length()<=0))
            return;
        final StringBuffer newBanned=new StringBuffer("");
        final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
        if((banned!=null)&&(banned.size()>0))
        {
            for(int b=0;b<banned.size();b++)
            {
                String B=(String)banned.get(b);
                if((!B.equals(unBanMe))&&(B.trim().length()>0))
                    newBanned.append(B+"\n");
            }
            Resources.updateFileResource("::banned.ini",newBanned);
        }
    }
    
    public static final void unban(final int unBanMe)
    {
    	final StringBuffer newBanned=new StringBuffer("");
    	final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
        if((banned!=null)&&(banned.size()>0))
        {
            for(int b=0;b<banned.size();b++)
            {
                String B=(String)banned.get(b);
                if(((b+1)!=unBanMe)&&(B.trim().length()>0))
                    newBanned.append(B+"\n");
            }
            Resources.updateFileResource("::banned.ini",newBanned);
        }
    }
    
    public static final int ban(final String banMe)
    {
        if((banMe==null)||(banMe.length()<=0))
            return -1;
        final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
        if((banned!=null)&&(banned.size()>0))
        for(int b=0;b<banned.size();b++)
        {
            String B=(String)banned.get(b);
            if(B.equals(banMe))
                return b;
        }
        final StringBuffer str=Resources.getFileResource("banned.ini",false);
        if(banMe.trim().length()>0) str.append(banMe+"\n");
        Resources.updateFileResource("::banned.ini",str);
        return -1;
    }
}
