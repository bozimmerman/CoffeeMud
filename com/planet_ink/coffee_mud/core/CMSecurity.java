package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.core.exceptions.CMException;
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
   Copyright 2000-2013 Bo Zimmerman

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
Supported: (see SecFlag)
ORDER includes TAKE, GIVE, DRESS, mob passivity, all follow
ABOVELAW (also law books), 
WIZINV (includes see WIZINV), 
CMDMOBS (also prevents walkaways)
SUPERSKILL (never fails skills), 
IMMORT (never dies),
KILL* for deleting journal entries
FS:relative path from /coffeemud/ -- read/write access to regular file sys
VFS:relative path from /coffeemud/ -- read/write access to virtual file sys
LIST: (affected by killx, cmdplayers, loadunload, cmdclans, ban, nopurge,
	   cmditems, cmdmobs, cmdrooms, sessions, cmdareas, listadmin, stat)
*/ 
@SuppressWarnings({"unchecked","rawtypes"})
public class CMSecurity
{
	protected final static Set<DisFlag> 	disVars 		 = new HashSet<DisFlag>();
	protected final static Set<String>  	cmdDisVars  	 = new HashSet<String>();
	protected final static Set<String>  	ablDisVars  	 = new HashSet<String>();
	protected final static Set<String>  	expDisVars  	 = new HashSet<String>();
	protected final static Set<DbgFlag> 	dbgVars 		 = new HashSet<DbgFlag>();
	protected final static Set<String>  	saveFlags   	 = new HashSet<String>();
	protected final static Set<String>  	journalFlags	 = new HashSet<String>(); // global, because of cross-library issues
	
	protected final long						startTime	 = System.currentTimeMillis();
	protected MaskingLibrary.CompiledZapperMask compiledSysop= null;
	protected final Map<String,SecGroup> 		groups  	 = new Hashtable<String,SecGroup>();
	
	protected static boolean					debuggingEverything=false;

	private final static CMSecurity[]   		secs=new CMSecurity[256];
	private final static Iterator<SecFlag>		EMPTYSECFLAGS=new EnumerationIterator<SecFlag>(new EmptyEnumeration<SecFlag>());
	
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

	
	public static final void registerJournal(String journalName)
	{
		journalName=journalName.toUpperCase().trim();
		journalFlags.add(journalName);
	}

	public static final void clearGroups()
	{
		instance().groups.clear();
	}
	
	public static final void parseGroups(final Properties page)
	{
		clearGroups();
		if(page==null) return;
		List<Pair<String,String>> allGroups=new LinkedList<Pair<String,String>>();
		for(Enumeration<Object> e=page.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			if(key.startsWith("GROUP_"))
			{
				String groupName=key.substring(6).trim().toUpperCase();
				allGroups.add(new Pair<String,String>(groupName,(String)page.get(key)));
				addGroup(groupName,"");
			}
		}
		for(Iterator<Pair<String,String>> i=allGroups.iterator(); i.hasNext();)
		{
			final Pair<String,String> p=i.next();
			addGroup(p.first,p.second);
		}
	}

	public Object parseSecurityFlag(String s)
	{
		SecGroup group=groups.get(s);
		if(group != null)
			return group;
		else
		if(s.startsWith("FS:"))
		{
			s=s.substring(3).trim();
			boolean isAreaOnly=false;
			if(s.startsWith("AREA "))
			{
				s=s.substring(5).trim();
				isAreaOnly=true;
			}
			while(s.startsWith("/")) s=s.substring(1).trim();
			return new SecPath(s,false,isAreaOnly);
			
		}
		else
		if(s.startsWith("VFS:"))
		{
			s=s.substring(4);
			boolean isAreaOnly=false;
			if(s.startsWith("AREA "))
			{
				s=s.substring(5).trim();
				isAreaOnly=true;
			}
			while(s.startsWith("/")) s=s.substring(1).trim();
			return new SecPath(s,true,isAreaOnly);
		}
		else
		if(journalFlags.contains(s))
			return s;
		else
		if(s.startsWith("KILL") && journalFlags.contains(s.substring(4)))
			return s.substring(4);
		else
		if(s.startsWith("KILL") && s.endsWith("S") && journalFlags.contains(s.substring(4,s.length()-1)))
			return s.substring(4,s.length()-1);
		else
		if(s.endsWith("S") && journalFlags.contains(s.substring(0,s.length()-1)))
			return s.substring(0,s.length()-1);
		else
		{
			s=s.replace(' ','_');
			SecFlag flag=(SecFlag)CMath.s_valueOf(SecFlag.class, s);
			if(flag==null) flag=(SecFlag)CMath.s_valueOf(SecFlag.class, s+"S");
			if((flag==null)&&(s.equals("POOF"))) flag=(SecFlag)CMath.s_valueOf(SecFlag.class, "GOTO");
			if((flag==null)&&(s.equals("AREA_POOF"))) flag=(SecFlag)CMath.s_valueOf(SecFlag.class, "AREA_GOTO");
			if(flag==null)
				return null;
			else
				return flag;
		}
	}

	public final SecGroup createGroup(String name, final List<String> set)
	{
		final Set<SecFlag> 	 newFlags=new HashSet<SecFlag>();
		final List<SecGroup> newGroups=new LinkedList<SecGroup>();
		final List<SecPath>  newPaths=new LinkedList<SecPath>();
		final Set<String> 	 newJFlags=new HashSet<String>();
		for(int v=0;v<set.size();v++)
		{
			String s=((String)set.get(v)).trim().toUpperCase();
			Object o=this.parseSecurityFlag(s);
			if(o==null)
				Log.errOut("CMSecurity","Unknown security flag: "+s+" in group "+name);
			else
			if(o instanceof SecGroup)
				newGroups.add((SecGroup)o);
			else
			if(o instanceof SecPath)
				newPaths.add((SecPath)o);
			else
			if(o instanceof SecFlag)
				newFlags.add((SecFlag)o);
			else
			if(o instanceof String)
				newJFlags.add((String)o);
			else
				Log.errOut("CMSecurity","Unparsed security flag: "+s+" in group "+name);
		}
		return new SecGroup(name,newFlags,newGroups,newPaths,newJFlags);
	}

	public static Enumeration<SecGroup> getSecurityGroups()
	{
		return new IteratorEnumeration<SecGroup>(instance().groups.values().iterator());
	}
	
	public static Enumeration<String> getJournalSecurityFlags()
	{
		return new IteratorEnumeration<String>(journalFlags.iterator());
	}
	
	private static final void addGroup(String name, final String set)
	{
		SecGroup newGroup=instance().createGroup(name,CMParms.parseCommas(set,true));
		SecGroup group=i().groups.get(name);
		if(group == null)
			i().groups.put(name,newGroup);
		else
			group.reset(newGroup.flags,newGroup.groups,newGroup.paths,newGroup.jFlags);
	}
	
	public static final boolean isASysOp(final MOB mob)
	{
		return CMLib.masking().maskCheck(i().compiledSysop,mob,true)
				||((mob!=null)
					&&(mob.soulMate()!=null)
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
		if((mob.playerStats().getSecurityFlags().size()==0)
		&&(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).size()==0))
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
		Iterator[] allGroups={mob.playerStats().getSecurityFlags().paths(),
				 mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()};
		for(Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			SecPath p=g.next();
			if((!p.isAreaOnly)||(subop))
				DIRSV.add((p.isVfs?"::":"//")+p.path);
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
		final Iterator[] allGroups={mob.playerStats().getSecurityFlags().paths(),
				 mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()};
		for(Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			SecPath p=g.next();
			if((!p.isAreaOnly)||(subop))
				return true;
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
		final String pathSlash=path+"/";
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		Iterator[] allGroups={mob.playerStats().getSecurityFlags().paths(),
				 mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()};
		for(Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			SecPath p=g.next();
			if((!p.isAreaOnly)||(subop))
			{
				if(p.path.startsWith(pathSlash)
				||path.startsWith(p.slashPath)
				||p.path.equals(path))
					return true;
			}
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
		Iterator[] allGroups={mob.playerStats().getSecurityFlags().paths(),
				 mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()};
		for(Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			SecPath p=g.next();
			if(((!p.isAreaOnly)||(subop))
			&&(!p.isVfs || isVFS))
			{
				if(path.startsWith(p.slashPath) || (path.equals(p.path)))
					return true;
			}
		}
		return false;
	}
	
	public static final Iterator<SecFlag> getSecurityCodes(final MOB mob, final Room room)
	{
		if((mob==null)||(mob.playerStats()==null)) return EMPTYSECFLAGS;
		final MultiIterator<SecFlag> it=new MultiIterator<SecFlag>();
		it.add(mob.playerStats().getSecurityFlags().flags());
		it.add(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).flags());
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		final List<SecFlag> flags=new ArrayList<SecFlag>();
		for(;it.hasNext();)
		{
			SecFlag flag=it.next();
			if((flag.areaAlias!=flag)||subop)
				flags.add(flag);
		}
		return flags.iterator();
	}
	
	
	public static boolean isJournalAccessAllowed(MOB mob, String journalFlagName)
	{
		journalFlagName=journalFlagName.trim().toUpperCase();
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		if(mob.playerStats().getSecurityFlags().contains(journalFlagName))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).contains(journalFlagName))
			return true;
		return false;
	}
	
	public static final boolean isAllowedContainsAny(final MOB mob, final Room room, final SecGroup secGroup)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		if(mob.playerStats().getSecurityFlags().containsAny(secGroup, subop))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).containsAny(secGroup, subop))
			return true;
		return false;
	}
	
	public static final boolean isAllowed(final MOB mob, final Room room, final SecFlag flag)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		if(mob.playerStats().getSecurityFlags().contains(flag, subop))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).contains(flag, subop))
			return true;
		return false;
	}
	
	public static final boolean isAllowedContainsAny(final MOB mob, final SecGroup secGroup)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		for(Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			final boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
			if(subop)
			{
				if(mob.playerStats().getSecurityFlags().containsAny(secGroup, subop))
					return true;
				if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).containsAny(secGroup, subop))
					return true;
				break;
			}
		}
		if(mob.playerStats().getSecurityFlags().containsAny(secGroup, false))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).containsAny(secGroup, false))
			return true;
		return false;
	}
	
	public static final boolean isAllowedEverywhere(final MOB mob, final SecFlag flag)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		if(mob.playerStats().getSecurityFlags().contains(flag, false))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).contains(flag, false))
			return true;
		return false;
	}
	
	public static final boolean isAllowedAnywhere(final MOB mob, final SecFlag flag)
	{
		if(mob==null) return false;
		if(isASysOp(mob)) return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!CMath.bset(mob.soulMate().getBitmap(),MOB.ATT_SYSOPMSGS)))) 
			return false;
		if(isAllowedEverywhere(mob,flag.getRegularAlias()))
			return true;
		if(flag.areaAlias!=flag)
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final boolean subop=((Area)e.nextElement()).amISubOp(mob.Name());
				if(subop)
				{
					if(mob.playerStats().getSecurityFlags().contains(flag, true))
						return true;
					if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).contains(flag, true))
						return true;
					break;
				}
			}
		return isAllowedEverywhere(mob,flag.getRegularAlias());
	}
	

	public static final boolean isSaveFlag(final String key)
	{ 
		return (saveFlags.size()>0) && saveFlags.contains(key);
	}
	
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
	
	public static Enumeration<DbgFlag> getDebugEnum() 
	{ 
		return new IteratorEnumeration<DbgFlag>(dbgVars.iterator());
	}
	
	public static final boolean isDebugging(final DbgFlag key)
	{ 
		return ((dbgVars.size()>0)&&dbgVars.contains(key))||debuggingEverything;
	}
	
	public static final boolean isDebuggingSearch(final String key)
	{ 
		final DbgFlag flag=(DbgFlag)CMath.s_valueOf(DbgFlag.values(),key.toUpperCase().trim());
		if(flag==null) return false;
		return isDebugging(flag);
	}
	
	public static final void setDebugVar(final DbgFlag var, final boolean delete)
	{
		if((var!=null)&&(delete)&&(dbgVars.size()>0))
			dbgVars.remove(var);
		else
		if((var!=null)&&(!delete))
			dbgVars.add(var);
	}
	
	public static final void setDebugVars(final String vars)
	{
		final List<String> V=CMParms.parseCommas(vars.toUpperCase(),true);
		dbgVars.clear();
		for(String var : V)
		{
			final DbgFlag flag=(DbgFlag)CMath.s_valueOf(DbgFlag.values(),var);
			if(flag==null)
				Log.errOut("CMSecurity","Unable DEBUG flag: "+var);
			else
				dbgVars.add(flag);
		}
		debuggingEverything = dbgVars.contains(DbgFlag.EVERYTHING);
	}
	
	public static Enumeration<DisFlag> getDisablesEnum() { return new IteratorEnumeration<DisFlag>(disVars.iterator());}
	
	public static final boolean isDisabled(final DisFlag ID)
	{ 
		return disVars.contains(ID); 
	}
	
	public static final boolean isCommandDisabled(final String ID)
	{ 
		return cmdDisVars.contains(ID); 
	}
	
	public static final boolean isAbilityDisabled(final String ID)
	{ 
		return ablDisVars.contains(ID); 
	}
	
	public static final boolean isExpertiseDisabled(final String ID)
	{ 
		return expDisVars.contains(ID); 
	}
	
	public static final boolean isDisabledSearch(final String anyFlag)
	{
		final Set<String> set;
		String flag = anyFlag.toUpperCase().trim();
		if(flag.startsWith("ABILITY_")||flag.startsWith("EXPERTISE_")||flag.startsWith("COMMAND_"))
			flag=flag.substring(flag.indexOf('_')+1);
		else
		{
			DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null) return isDisabled(disFlag);
		}
		if(CMClass.getCommand(flag)!=null)
			set=cmdDisVars;
		else
		if(CMClass.getAbility(flag)!=null)
			set=ablDisVars;
		else
		if(CMLib.expertises().getDefinition(flag)!=null)
			set=expDisVars;
		else
			return false;
		return set.contains(flag);
	}
	
	public static final void setDisableVars(final String vars)
	{
		final List<String> V=CMParms.parseCommas(vars.toUpperCase(),true);
		disVars.clear();
		for(String var : V)
		{
			if(var.startsWith("COMMAND_"))
				cmdDisVars.add(var.substring(8));
			else
			if(var.startsWith("ABILITY_"))
				ablDisVars.add(var.substring(8));
			else
			if(var.startsWith("EXPERTISE_"))
				expDisVars.add(var.substring(10));
			else
			{
				final DisFlag flag=(DisFlag)CMath.s_valueOf(DisFlag.values(), var);
				if(flag==null)
					Log.errOut("CMSecurity","Unknown disable flag: "+var);
				else
					disVars.add(flag);
			}
		}
	}
	
	public static final boolean setDisableVar(final String anyFlag, final boolean delete)
	{
		final Set<String> set;
		String flag = anyFlag.toUpperCase().trim();
		if(flag.startsWith("ABILITY_")||flag.startsWith("EXPERTISE_")||flag.startsWith("COMMAND_"))
			flag=flag.substring(flag.indexOf('_')+1);
		else
		{
			DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null)
			{
				setDisableVar(disFlag,delete);
				return true;
			}
		}
		if(CMClass.getCommand(flag)!=null)
			set=cmdDisVars;
		else
		if(CMClass.getAbility(flag)!=null)
			set=ablDisVars;
		else
		if(CMLib.expertises().getDefinition(flag)!=null)
			set=expDisVars;
		else
			return false;
		if((flag!=null)&&(delete)&&(set.size()>0))
			set.remove(flag);
		else
		if((flag!=null)&&(!delete))
			set.add(flag);
		return true;
	}
	
	public static final void setDisableVar(final DisFlag flag, final boolean delete)
	{
		if((flag!=null)&&(delete)&&(disVars.size()>0))
			disVars.remove(flag);
		else
		if((flag!=null)&&(!delete))
			disVars.add(flag);
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
	
	public static final long getStartTime()
	{
		return i().startTime;
	}
	
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
	
	public static enum SecFlag
	{
		ABILITIES, ABOVELAW, AFTER, AHELP, ALLSKILLS, ANNOUNCE, AS, AT, BAN, BEACON, BOOT, CARRYALL, CATALOG, CHARGEN, CLOAK, CMD, CMDABILITIES, 
		CMDAREAS, CMDCLANS, CMDCLASSES, CMDEXITS, CMDFACTIONS, CMDITEMS, CMDMOBS, CMDPLAYERS, CMDQUESTS, CMDRACES, CMDRECIPES, CMDROOMS, 
		CMDSOCIALS, COMPONENTS, COPY, COPYITEMS, COPYMOBS, COPYROOMS, DUMPFILE, EXPERTISE, EXPERTISES, EXPORT, EXPORTFILE, EXPORTPLAYERS, 
		GMODIFY, GOTO, I3, IDLEOK, IMC2, IMMORT, IMPORT, IMPORTITEMS, IMPORTMOBS, IMPORTPLAYERS, IMPORTROOMS, JOURNALS, JSCRIPTS, KILL, KILLDEAD, 
		LISTADMIN, LOADUNLOAD, MERGE, MXPTAGS, NEWS, NOEXPIRE, NOPURGE, ORDER, PAUSE, PKILL, POLLS, POSSESS, PURGE, RESET, RESETUTILS, RESTRING, 
		SESSIONS, SHUTDOWN, SNOOP, STAT, SUPERSKILL, SYSMSGS, TICKTOCK, TITLES, TRAILTO, TRANSFER, WHERE, WIZEMOTE, WIZINV, MISC, 
		
		AREA_ABILITIES, AREA_ABOVELAW, AREA_AFTER, AREA_AHELP, AREA_ALLSKILLS, AREA_ANNOUNCE, AREA_AS, AREA_AT, AREA_BAN, AREA_BEACON, AREA_BOOT, 
		AREA_CARRYALL, AREA_CATALOG, AREA_CHARGEN, AREA_CLOAK, AREA_CMD, AREA_CMDABILITIES, AREA_CMDAREAS, AREA_CMDCLANS, AREA_CMDCLASSES, 
		AREA_CMDEXITS, AREA_CMDFACTIONS, AREA_CMDITEMS, AREA_CMDMOBS, AREA_CMDPLAYERS, AREA_CMDQUESTS, AREA_CMDRACES, AREA_CMDRECIPES, 
		AREA_CMDROOMS, AREA_CMDSOCIALS, AREA_COMPONENTS, AREA_COPY, AREA_COPYITEMS, AREA_COPYMOBS, AREA_COPYROOMS, AREA_DUMPFILE, AREA_EXPERTISE, 
		AREA_EXPERTISES, AREA_EXPORT, AREA_EXPORTFILE, AREA_EXPORTPLAYERS, AREA_GMODIFY, AREA_GOTO, AREA_I3, AREA_IDLEOK, AREA_IMC2, AREA_IMMORT, 
		AREA_IMPORT, AREA_IMPORTITEMS, AREA_IMPORTMOBS, AREA_IMPORTPLAYERS, AREA_IMPORTROOMS, AREA_JOURNALS, AREA_JSCRIPTS, AREA_KILL, 
		AREA_KILLDEAD, AREA_LISTADMIN, AREA_LOADUNLOAD, AREA_MERGE, AREA_MXPTAGS, AREA_NEWS, AREA_NOEXPIRE, AREA_NOPURGE, AREA_ORDER, 
		AREA_PAUSE, AREA_PKILL, AREA_POLLS, AREA_POSSESS, AREA_PURGE, AREA_RESET, AREA_RESETUTILS, AREA_RESTRING, AREA_SESSIONS, 
		AREA_SHUTDOWN, AREA_SNOOP, AREA_STAT, AREA_SUPERSKILL, AREA_SYSMSGS, AREA_TICKTOCK, AREA_TITLES, AREA_TRAILTO, AREA_TRANSFER, AREA_WHERE, 
		AREA_WIZEMOTE, AREA_WIZINV, AREA_MISC
		;
		private SecFlag regularAlias=null;
		private SecFlag areaAlias=null;
		private SecFlag()
		{
		}
		private void fixAliases()
		{
			if(regularAlias==null)
			{
				if(this.name().startsWith("AREA_"))
				{
					regularAlias=(SecFlag)CMath.s_valueOf(SecFlag.class, this.name().substring(5));
					areaAlias=this;
				}
				else
				{
					areaAlias=(SecFlag)CMath.s_valueOf(SecFlag.class, "AREA_"+this.name());
					regularAlias=this;
				}
			}
		}
		public SecFlag getRegularAlias()
		{
			fixAliases();
			return regularAlias;
		}
		public SecFlag getAreaAlias()
		{
			fixAliases();
			return areaAlias;
		}
	}
	
	public static class SecPath
	{
		private String	path;
		private String  slashPath;
		private boolean	isVfs;
		private boolean	isAreaOnly;
		public SecPath(String path, boolean isVfs, boolean isAreaOnly)
		{
			this.path=path.trim();
			if(!this.path.endsWith("/"))
				slashPath=this.path+"/";
			else
				slashPath=this.path;
			this.isVfs=isVfs;
			this.isAreaOnly=isAreaOnly;
		}
		@Override
		public String toString()
		{
			return (isVfs?"VFS:":"FS:")+(isAreaOnly?"AREA ":"")+path;
		}
	}
	
	public static class SecGroup
	{
		private String				name;
		private Set<SecFlag> 		flags;
		private List<SecGroup>		groups;
		private List<SecPath>		paths;
		private Set<String> 		jFlags;
		private int					numAllFlags;
		public SecGroup(String name, Set<SecFlag> flags, List<SecGroup> groups, List<SecPath> paths, Set<String> jFlags)
		{
			this.name=name;
			reset(flags, groups, paths, jFlags);
		}
		public SecGroup(String name, Set<SecFlag> flags)
		{
			this.name=name;
			reset(flags,new ArrayList<SecGroup>(1),new ArrayList<SecPath>(1),new SHashSet<String>());
		}
		public SecGroup(SecFlag[] flags)
		{
			this.name="";
			reset(new SHashSet<SecFlag>(flags),new ArrayList<SecGroup>(1),new ArrayList<SecPath>(1),new SHashSet<String>());
		}
		public String getName()
		{
			return name;
		}
		public void reset(Set<SecFlag> flags, List<SecGroup> groups, List<SecPath> paths, Set<String> jFlags)
		{
			this.flags=flags;
			this.numAllFlags=flags.size();
			this.groups=groups;
			this.paths=paths;
			this.jFlags=jFlags;
			numAllFlags+=paths.size();
			for(SecGroup g : groups) this.numAllFlags+= g.size();
		}
		//public SecGroup copyOf() // NOT ALLOWED -- flags are ok, but groups MUST be unmutable for internal changes!!
		public boolean contains(String journalFlag)
		{
			if(jFlags.contains(journalFlag)) 
				return true;
			for(SecGroup group : groups)
				if(group.contains(journalFlag))
					return true;
			return false;
		}
		public boolean contains(SecFlag flag, boolean isSubOp)
		{
			if(flags.contains(flag.getRegularAlias())) 
				return true;
			if(isSubOp && flags.contains(flag.getAreaAlias())) 
				return true;
			for(SecGroup group : groups)
				if(group.contains(flag, isSubOp))
					return true;
			return false;
		}
		public boolean containsAny(SecGroup group, boolean isSubOp)
		{
			for(SecFlag flag : group.flags)
				if(contains(flag, isSubOp))
					return true;
			for(String jflag : group.jFlags)
				if(contains(jflag))
					return true;
			for(SecGroup g : group.groups)
				if(g.containsAny(group, isSubOp))
					return true;
			return false;
		}
		public int size()
		{
			return numAllFlags;
		}
		@Override
		public String toString()
		{
			return toString(';');
		}
		public SecGroup copyOf()
		{
			return new SecGroup(name,new SHashSet<SecFlag>(flags),new SVector<SecGroup>(groups),new SVector<SecPath>(paths),new SHashSet<String>(jFlags));
		}
		public String toString(char separatorChar)
		{
			StringBuilder str=new StringBuilder("");
			for(SecFlag flag : flags)
				str.append(flag.name()).append(separatorChar);
			for(SecGroup grp : groups)
				str.append(grp.name).append(separatorChar);
			for(SecPath path : paths)
				str.append(path.toString()).append(separatorChar);
			for(String flag : jFlags)
				str.append(flag).append(separatorChar);
			if(str.length()>0)
				return str.toString().substring(0,str.length()-1);
			return "";
		}
		public Iterator<SecPath> paths(){return new Iterator<SecPath>()
			{
				Iterator<SecPath>  p=null;
				Iterator<SecGroup> g=null;
				private boolean doNext()
				{
					if(p==null)
						p=paths.iterator();
					if(p.hasNext())
						return true;
					if(g==null)
						g=groups.iterator();
					while(!p.hasNext())
					{
						if(!g.hasNext())
							return false;
						p=g.next().paths();
					}
					return true;
				}
				@Override
				public boolean hasNext() 
				{
					if((p==null)||(!p.hasNext()))
						return doNext();
					return p.hasNext();
				}
				@Override
				public SecPath next() 
				{
					if(hasNext()) return p.next();
					throw new java.util.NoSuchElementException();
				}
				@Override
				public void remove() {}
			};
		}
		public Iterator<SecFlag> flags(){return new Iterator<SecFlag>()
			{
				Iterator<SecFlag>  p=null;
				Iterator<SecGroup> g=null;
				private boolean doNext()
				{
					if(p==null)
						p=flags.iterator();
					if(p.hasNext())
						return true;
					if(g==null)
						g=groups.iterator();
					while(!p.hasNext())
					{
						if(!g.hasNext())
							return false;
						p=g.next().flags();
					}
					return true;
				}
				@Override
				public boolean hasNext() 
				{
					if((p==null)||(!p.hasNext()))
						return doNext();
					return p.hasNext();
				}
				@Override
				public SecFlag next() 
				{
					if(hasNext()) return p.next();
					throw new java.util.NoSuchElementException();
				}
				@Override
				public void remove() {}
			};
		}
	}
	
	public static final SecGroup SECURITY_COPY_GROUP=new SecGroup(new SecFlag[]{ 
			SecFlag.COPY, SecFlag.COPYITEMS, SecFlag.COPYMOBS, SecFlag.COPYROOMS,
			
			SecFlag.AREA_COPY, SecFlag.AREA_COPYITEMS, SecFlag.AREA_COPYMOBS, SecFlag.AREA_COPYROOMS
	});
	
	public static final SecGroup SECURITY_GOTO_GROUP=new SecGroup(new SecFlag[]{ 
			SecFlag.GOTO, 
			
			SecFlag.AREA_GOTO
	});
	
	public static final SecGroup SECURITY_KILL_GROUP=new SecGroup(new SecFlag[]{ 
			SecFlag.KILL, SecFlag.KILLDEAD, 
			
			SecFlag.AREA_KILL, SecFlag.AREA_KILLDEAD, 
	});
	
	public static final SecGroup SECURITY_IMPORT_GROUP=new SecGroup(new SecFlag[]{ 
			SecFlag.IMPORT, SecFlag.IMPORTITEMS, SecFlag.IMPORTMOBS, SecFlag.IMPORTPLAYERS, 
			SecFlag.IMPORTROOMS, 
			
			SecFlag.AREA_IMPORT, SecFlag.AREA_IMPORTITEMS, SecFlag.AREA_IMPORTMOBS, 
			SecFlag.AREA_IMPORTPLAYERS,	SecFlag.AREA_IMPORTROOMS,
	});
	
	public static final SecGroup SECURITY_EXPORT_GROUP=new SecGroup(new SecFlag[]{ 
			SecFlag.EXPORT, SecFlag.EXPORTFILE, SecFlag.EXPORTPLAYERS,
			
			SecFlag.AREA_EXPORT, SecFlag.AREA_EXPORTFILE, SecFlag.AREA_EXPORTPLAYERS
	});
	
	public static final SecGroup SECURITY_CMD_GROUP=new SecGroup(new SecFlag[]{ 
			SecFlag.CMD, SecFlag.CMDABILITIES, SecFlag.CMDAREAS, SecFlag.CMDCLANS, SecFlag.CMDCLASSES, 
			SecFlag.CMDEXITS, SecFlag.CMDFACTIONS, SecFlag.CMDITEMS, SecFlag.CMDMOBS, SecFlag.CMDPLAYERS, 
			SecFlag.CMDQUESTS, SecFlag.CMDRACES, SecFlag.CMDRECIPES, SecFlag.CMDROOMS, SecFlag.CMDSOCIALS,
			
			SecFlag.AREA_CMD, SecFlag.AREA_CMDABILITIES, SecFlag.AREA_CMDAREAS, SecFlag.AREA_CMDCLANS, 
			SecFlag.AREA_CMDCLASSES, SecFlag.AREA_CMDEXITS, SecFlag.AREA_CMDFACTIONS, SecFlag.AREA_CMDITEMS, 
			SecFlag.AREA_CMDMOBS, SecFlag.AREA_CMDPLAYERS, SecFlag.AREA_CMDQUESTS, SecFlag.AREA_CMDRACES, 
			SecFlag.AREA_CMDRECIPES, SecFlag.AREA_CMDROOMS, SecFlag.AREA_CMDSOCIALS
	});
	
	public static enum DbgFlag
	{
		PROPERTY, ARREST, CONQUEST, MUDPERCOLATOR, GMODIFY, MERGE, BADSCRIPTS, TELNET, CLASSLOADER, DBROOMPOP, DBROOMS, CMROIT, CMROEX, CMROCH, CMAREA, 
		CMSTAT, HTTPMACROS, I3, HTTPACCESS, IMC2, SMTPSERVER, UTILITHREAD, MISSINGKIDS, FLAGWATCHING, CATALOGTHREAD, JOURNALTHREAD, 
		MAPTHREAD, VACUUM, AUTOPURGE, PLAYERTHREAD, OUTPUT, EXPORT, STATSTHREAD, GEAS, SMTPCLIENT, MESSAGES, EVERYTHING, CMROOM, HTTPREQ, CMJRNL, IMPORT,
		PLAYERSTATS, CLANS, BINOUT, BININ;
	}
	
	public static enum DisFlag
	{
		LEVELS("player leveling"), EXPERIENCE("player XP gains"), PROPERTYOWNERCHECKS("confirm property ownership"), AUTODISEASE("diseases from races, weather, age, etc.."), 
		DBERRORQUE("save SQL errors"), DBERRORQUESTART("retry SQL errors on boot"), CONNSPAMBLOCK("connection spam blocker"), FATAREAS("standard non-thin cached areas"), 
		PASSIVEAREAS("inactive area sleeping"), DARKWEATHER("weather causing room darkness"), DARKNIGHTS("time causing room darkness"), ARREST("legal system"), 
		EMOTERS("emoter behaviors"), CONQUEST("area clan conquest"), RANDOMITEMS("random item behavior"), MOBILITY("mobile behaviors"), MUDCHAT("MOB chat behavior"), 
		RANDOMMONSTERS("random monster behaviors"), RACES("player races"), CLASSES("player classes"), MXP("MXP system"), MSP("MSP system"), QUITREASON("early quitting prompt"), 
		CLASSTRAINING("class training"), ROOMVISITS("room visits"), THIRST("player thirst"), HUNGER("player hunger"), WEATHER("area weather"), WEATHERCHANGES("weather changes"), 
		WEATHERNOTIFIES("notification of weather changes"), QUESTS("quest system"), SCRIPTABLEDELAY("script event delay"), SCRIPTING("MOBPROG scripting"), 
		SCRIPTABLE("MOBProg scripting"), MCCP("MCCP compression"), LOGOUTS("player logouts"), THINAREAS("thin uncached areas"), UTILITHREAD("thread & session monitoring"), 
		THREADTHREAD("thread monitoring"), EQUIPSIZE("armor size fitting"), RETIREREASON("early char delete prompt"), MAXCONNSPERACCOUNT("connections per account limit"), 
		ALLERGIES("auto player allergies"), LOGINS("non-archin player logins"), NEWPLAYERS("new player creation"), MAXNEWPERIP("new character per ip limit"), 
		MAXCONNSPERIP("connections per ip limit"), CLANTICKS("clan ticks/automation"), CATALOGTHREAD("catalog house-cleaning"), 
		CATALOGCACHE("catalog instance caching"), SAVETHREAD("Player/Journal/Map/Table maintenance"), JOURNALTHREAD("journal house-cleaning"), MAPTHREAD("map house-cleaning"), 
		AUTOPURGE("player purging"), PURGEACCOUNTS("account purging"), PLAYERTHREAD("player maintenance/house cleaning"), MSSP("MSSP protocol support"), 
		STATS("statistics system"), STATSTHREAD("statistics auto-saving"), POLLCACHE("player poll caching"), SESSIONTHREAD("session monitoring"), SMTPCLIENT("email client"), 
		THINGRIDS("Thin uncached grids"), FATGRIDS("Standard cached grids"), STDRACES("Standard Player Races"), STDCLASSES("Standard Player Classes"),
		CHANNELAUCTION("Auction Channel"), ELECTRICTHREAD("Electric Threads"), MOBTEACHER("MOBTeacher");
		private final String desc;
		DisFlag(final String description){this.desc=description;}
		public String description() { return desc;}
	}
}
