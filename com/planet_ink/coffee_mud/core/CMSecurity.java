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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Libraries.interfaces.PlayerLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.File; // does some cmfile type stuff
import java.net.*;
import java.util.*;

/*
   Copyright 2004-2018 Bo Zimmerman

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
 * CMSecurity is the singleton that handles all security checks for all manner
 * of resource access by users.  It also handles certain flag systems, such
 * as debug flags, resource re-save flags, and system disabling flags.
 * 
 * Supported: (see SecFlag)
 * ORDER includes TAKE, GIVE, DRESS, mob passivity, all follow
 * ABOVELAW (also law books),
 * WIZINV (includes see WIZINV),
 * CMDMOBS (also prevents walkaways)
 * SUPERSKILL (never fails skills),
 * IMMORT (never dies),
 * KILL* for deleting journal entries
 * FS:relative path from /coffeemud/ -- read/write access to regular file sys
 * VFS:relative path from /coffeemud/ -- read/write access to virtual file sys
 * LIST: (affected by killx, cmdplayers, loadunload, cmdclans, ban, nopurge,
 *   cmditems, cmdmobs, cmdrooms, sessions, cmdareas, listadmin, stat)
 * 
 * Like many similar systems, this class is thread-group-sensitive on some
 * or all resources, allowing different security views to be presented to 
 * different "muds" according to the thread group the calling thread belongs to.
 * @author Bo Zimmerman
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CMSecurity
{
	public static final int JSCRIPT__NO_APPROVAL = 0;
	public static final int JSCRIPT_REQ_APPROVAL = 1;
	public static final int JSCRIPT_ALL_APPROVAL = 2;
	
	protected static final String[] emptyStrArray= new String[0];
	
	protected final Set<DisFlag>	disVars		 = new HashSet<DisFlag>();
	protected final Set<String>		cmdDisVars	 = new HashSet<String>();
	protected final Set<String>		racDisVars	 = new HashSet<String>();
	protected final Set<String>		clsDisVars	 = new HashSet<String>();
	protected final Set<String>		facDisVars	 = new HashSet<String>();
	protected final Set<String>		ablDisVars	 = new HashSet<String>();
	protected final Set<String>		expDisVars	 = new HashSet<String>();
	protected final Set<DbgFlag>	dbgVars		 = new HashSet<DbgFlag>();
	protected final Set<SaveFlag>	saveFlags 	 = new HashSet<SaveFlag>();
	protected final Set<String>		journalFlags = new HashSet<String>(); // global, because of cross-library issues
	
	protected final Map<String,String[]>	racEnaVars	 = new Hashtable<String,String[]>();
	protected final Map<String,String[]>	clsEnaVars	 = new Hashtable<String,String[]>();

	protected final long					startTime	 = System.currentTimeMillis();
	protected CompiledZMask					compiledSysop= null;
	protected final Map<String,SecGroup> 	groups  	 = new Hashtable<String,SecGroup>();

	protected static boolean				debuggingEverything=false;

	private final static CMSecurity[]		secs		 = new CMSecurity[256];
	private final static Iterator<SecFlag>	EMPTYSECFLAGS= new EnumerationIterator<SecFlag>(new EmptyEnumeration<SecFlag>());
	
	private final static String				XABLE_PREFIX_ABILITY	= "ABILITY_";
	private final static String				XABLE_PREFIX_EXPERTISE	= "EXPERTISE_";
	private final static String				XABLE_PREFIX_COMMAND	= "COMMAND_";
	private final static String				XABLE_PREFIX_FACTION	= "FACTION_";
	private final static String				XABLE_PREFIX_RACE		= "RACE_";
	private final static String				XABLE_PREFIX_CHARCLASS	= "CHARCLASS_";

	/**
	 * Creates a new thread-group sensitive CMSecurity reference object.
	 */
	public CMSecurity()
	{
		super();
		final char c=Thread.currentThread().getThreadGroup().getName().charAt(0);
		if(secs[c]==null)
			secs[c]=this;
	}

	/**
	 * Returns the CMSecurity instance tied to this particular thread group, or creates a new one.
	 * @return the CMSecurity instance tied to this particular thread group, or creates a new one.
	 */
	public static final CMSecurity instance()
	{
		final CMSecurity p=i();
		if(p==null)
			return new CMSecurity();
		return p;
	}

	/**
	 * Returns the CMSecurity instance tied to this particular thread group, or null if not yet created.
	 * @param c the thread group to check
	 * @return the CMSecurity instance tied to this particular thread group, or null if not yet created.
	 */
	public static final CMSecurity instance(char c)
	{ 
		return secs[c];
	}

	/**
	 * Returns the CMSecurity instance tied to this particular thread group, or null if not yet created.
	 * @return the CMSecurity instance tied to this particular thread group, or null if not yet created.
	 */
	private static final CMSecurity i()
	{
		return secs[Thread.currentThread().getThreadGroup().getName().charAt(0)];
	}

	/**
	 * Designates that the thread which called this method should instead use the security system
	 * of the MAIN_HOST, sharing it.
	 */
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

	/**
	 * Redefines the ZapperMask that defines what an All Powerful Super Admin of the
	 * entire mud player looks like.  God help anyone that calls this method and doesn't
	 * know what they are doing.
	 * @param zapCheck the zapper mask identifying an "archon" (a SysOp Admin)
	 */
	public static final void setSysOp(String zapCheck)
	{
		if((zapCheck==null)||(zapCheck.trim().length()==0))
			zapCheck="-ANYCLASS +Archon";
		instance().compiledSysop=CMLib.masking().maskCompile(zapCheck);
	}

	/**
	 * Registers a new journal security flag, which is typically just its name
	 * @param journalName the journal security flag, or just the name/ID of the journal
	 */
	public static final void registerJournal(String journalName)
	{
		journalName=journalName.toUpperCase().trim();
		instance().journalFlags.add(journalName);
	}

	/**
	 * Removes all registered security groups.
	 */
	public static final void clearGroups()
	{
		instance().groups.clear();
	}

	/**
	 * Iterates through all the properties on the given property page, finding
	 * any security group definitions and, when found, registering them with 
	 * the security system.
	 * @param page the properties page to go through.
	 */
	public static final void parseGroups(final Properties page)
	{
		clearGroups();
		if(page==null)
			return;
		final List<Pair<String,String>> allGroups=new LinkedList<Pair<String,String>>();
		for(final Enumeration<Object> e=page.keys();e.hasMoreElements();)
		{
			final String key=(String)e.nextElement();
			if(key.startsWith("GROUP_"))
			{
				final String groupName=key.substring(6).trim().toUpperCase();
				allGroups.add(new Pair<String,String>(groupName,(String)page.get(key)));
				addGroup(groupName,"");
			}
		}
		for (final Pair<String, String> p : allGroups)
		{
			addGroup(p.first,p.second);
		}
	}

	/**
	 * Accepts a string representing a security flag, or another group name, or a filesystem path,
	 * or some other security designation, and returns the appropriate object, such as
	 * a SecGroup, SecPath, SecFlag, or String, that it represents.
	 * @param s the security thing to parse and identify
	 * @return the internal security object that the string represents
	 */
	public Object parseSecurityFlag(String s)
	{
		final SecGroup group=groups.get(s);
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
			while(s.startsWith("/"))
				s=s.substring(1).trim();
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
			while(s.startsWith("/"))
				s=s.substring(1).trim();
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
			if(flag==null)
				flag=(SecFlag)CMath.s_valueOf(SecFlag.class, s+"S");
			if((flag==null)&&(s.equals("POOF")))
				flag=(SecFlag)CMath.s_valueOf(SecFlag.class, "GOTO");
			if((flag==null)&&(s.equals("AREA_POOF")))
				flag=(SecFlag)CMath.s_valueOf(SecFlag.class, "AREA_GOTO");
			if(flag==null)
				return null;
			else
				return flag;
		}
	}

	/**
	 * Create a new security group object with the given name, and the given
	 * set of security flags/group names as a string list.  
	 * @param name the new security group name
	 * @param set the string list of flags/group names
	 * @return the new security group object
	 */
	public final SecGroup createGroup(String name, final List<String> set)
	{
		final Set<SecFlag> 	 newFlags=new HashSet<SecFlag>();
		final List<SecGroup> newGroups=new LinkedList<SecGroup>();
		final List<SecPath>  newPaths=new LinkedList<SecPath>();
		final Set<String> 	 newJFlags=new HashSet<String>();
		for(int v=0;v<set.size();v++)
		{
			final String s=set.get(v).trim().toUpperCase();
			final Object o=this.parseSecurityFlag(s);
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

	/**
	 * Returns an enumeration of all existing Security Groups
	 * @return an enumeration of all existing Security Groups
	 */
	public static Enumeration<SecGroup> getSecurityGroups()
	{
		return new IteratorEnumeration<SecGroup>(instance().groups.values().iterator());
	}

	/**
	 * Returns an enumeration of all journal security flags, which are usually just
	 * a bunch of journal id names.
	 * @return  an enumeration of all journal security flags
	 */
	public static Enumeration<String> getJournalSecurityFlags()
	{
		return new IteratorEnumeration<String>(instance().journalFlags.iterator());
	}

	/**
	 * Internal method to create a new security group of the given name, with the given
	 * set of security flags/group names as a comma-delimited list.  If the group
	 * with the given name already exists, it will be modified with the given flags.
	 * @param name the new security group name
	 * @param set the comma-delimited list of flags/group names
	 */
	private static final void addGroup(String name, final String set)
	{
		final SecGroup newGroup=instance().createGroup(name,CMParms.parseCommas(set,true));
		final SecGroup group=i().groups.get(name);
		if(group == null)
			i().groups.put(name,newGroup);
		else
			group.reset(newGroup.flags,newGroup.groups,newGroup.paths,newGroup.jFlags);
	}

	/**
	 * Checks whether the given user/player MOB is a full-on Archon, an
	 * admin of the entire mud, having all the power the system offers.
	 * @param mob the user/player to check
	 * @return true if the MOB represents a SysOp (Archon), false otherwise.
	 */
	public static final boolean isASysOp(final MOB mob)
	{
		return CMLib.masking().maskCheck(i().compiledSysop,mob,true)
				||((mob!=null)
					&&(mob.soulMate()!=null)
					&&(mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))
					&&(isASysOp(mob.soulMate())));
	}

	/**
	 * Checks whether the given user/player ThinPlayer object is a full-on
	 * admin of the entire mud, having all the power the system offers.
	 * @param mob the user/player to check, as a ThinPlayer object
	 * @return true if the ThinPlayer represents a SysOp (Archon), false otherwise.
	 */
	public static final boolean isASysOp(final PlayerLibrary.ThinPlayer mob)
	{
		return CMLib.masking().maskCheck(i().compiledSysop,mob);
	}

	/**
	 * Checks to see if the given user/player mob has any special security flags at all.
	 * This would encompass subops (area) as well as regular admins.
	 * @param mob the user/player to check
	 * @return true if the user/player has at least one security flag, false otherwise
	 */
	public static final boolean isStaff(final MOB mob)
	{
		if(isASysOp(mob))
			return true;
		if(mob==null)
			return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		if((mob.playerStats().getSecurityFlags().size()==0)
		&&(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).size()==0))
			return false;
		return true;
	}

	/**
	 * Returns a comprehensive list of all filesystem directories which the given user/player, in the given room
	 * location, may access
	 * @param mob the user/player to check
	 * @param room the room location of the above user/player
	 * @return the list of all directories this user/player can access
	 */
	public static final List<String> getAccessibleDirs(final MOB mob, final Room room)
	{
		final List<String> DIRSV=new Vector<String>();
		if(isASysOp(mob))
		{
			DIRSV.add("/");
			return DIRSV;
		}
		if(mob==null)
			return DIRSV;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return DIRSV;
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		final Iterator[] allGroups={mob.playerStats().getSecurityFlags().paths(),
				 mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()};
		for(final Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			final SecPath p=g.next();
			if((!p.isAreaOnly)||(subop))
				DIRSV.add((p.isVfs?"::":"//")+p.path);
		}
		String dir=null;
		for(int d=0;d<DIRSV.size();d++)
		{
			dir=DIRSV.get(d);
			if(dir.startsWith("//"))
			{
				dir=dir.substring(2);
				String path="";
				String subPath=null;
				while(dir.startsWith("/"))
					dir=dir.substring(1);
				while(dir.length()>0)
				{
					while(dir.startsWith("/"))
						dir=dir.substring(1);
					final int x=dir.indexOf('/');
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
					final CMFile F=new CMFile(path,null,CMFile.FLAG_LOGERRORS);
					if((F.exists())&&(F.canRead())&&(F.isDirectory()))
					{
						final String[] files=F.list();
						for (final String file : files)
						{
							if(file.equalsIgnoreCase(subPath))
							{
								if(path.length()>0)
									path+="/";
								path+=file;
								break;
							}
						}
					}
				}
				DIRSV.set(d,"//"+path);
			}
		}
		return DIRSV;
	}

	/**
	 * Checks whether the given user/player mob, in the given room location, can see ANY filesystem files.
	 * The room location allows subop (area) permissions to kick in.
	 * @param mob the user/player to check
	 * @param room the room location of the above user/player
	 * @return true if the user/player has permission to use any files in the filesystem, and false otherwise
	 */
	public static final boolean hasAccessibleDir(final MOB mob, final Room room)
	{
		if(isASysOp(mob))
			return true;
		if(mob==null)
			return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		final Iterator[] allGroups={mob.playerStats().getSecurityFlags().paths(),
				 mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()};
		for(final Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			final SecPath p=g.next();
			if((!p.isAreaOnly)||(subop))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether the given user/player mob, in the given room location, can see a directory at the given
	 * path. The room location allows subop (area) permissions to kick in.
	 * @param mob the user/player to check
	 * @param room the room location of the above user/player
	 * @param path the path of the file to check permissions on
	 * @return true if the user/player has permission to see and CD into the given directory, false otherwise.
	 */
	public static final boolean canTraverseDir(MOB mob, Room room, String path)
	{
		if(isASysOp(mob))
			return true;
		if(mob==null)
			return false;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
		if(path.equals("/")||path.equals("."))
			path="";
		final String pathSlash=path+"/";
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		final Iterator[] allGroups={mob.playerStats().getSecurityFlags().paths(),
				 mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()};
		for(final Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			final SecPath p=g.next();
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

	/**
	 * Checks whether the given user/player mob, in the given room location, can access a file at the given
	 * path, with the given VFS (database) status.  The room location allows subop (area) permissions to 
	 * kick in.
	 * @param mob the user/player to check
	 * @param room the room location of the above user/player
	 * @param path the path of the file to check permissions on
	 * @param isVFS whether the file denoted by the above path is in the database (VFS) or not
	 * @return true if the user/player has permission to access the given file, false otherwise.
	 */
	public static final boolean canAccessFile(final MOB mob, final Room room, String path, final boolean isVFS)
	{
		if(mob==null)
			return false;
		if(isASysOp(mob))
			return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		path=CMFile.vfsifyFilename(path.trim()).toUpperCase();
		if(path.equals("/")||path.equals("."))
			path="";
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		final Iterator[] allGroups=
		{
			mob.playerStats().getSecurityFlags().paths(),
			mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).paths()
		};
		for(final Iterator<SecPath> g=new MultiIterator<SecPath>(allGroups);g.hasNext();)
		{
			final SecPath p=g.next();
			if(((!p.isAreaOnly)||(subop))
			&&(!p.isVfs || isVFS))
			{
				if(path.startsWith(p.slashPath) || (path.equals(p.path)))
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns an iterator of all security flags that apply to the given user/player mob
	 * for their given room location, which allows subop (area) flags to also be included,
	 * if the mob is in their subop area.
	 * @param mob the user/player to check
	 * @param room the room location of the above user/player
	 * @return an iterator of all applicable security flags
	 */
	public static final Iterator<SecFlag> getSecurityCodes(final MOB mob, final Room room)
	{
		if((mob==null)||(mob.playerStats()==null))
			return EMPTYSECFLAGS;
		final MultiIterator<SecFlag> it=new MultiIterator<SecFlag>();
		it.add(mob.playerStats().getSecurityFlags().flags());
		it.add(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).flags());
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		final List<SecFlag> flags=new ArrayList<SecFlag>();
		for(;it.hasNext();)
		{
			final SecFlag flag=it.next();
			if((flag.areaAlias!=flag)||subop)
				flags.add(flag);
		}
		return flags.iterator();
	}

	/**
	 * Checks whether the given user/player mob has admin privileges on the given journal
	 * @param mob the user/player to check
	 * @param journalFlagName the journal flag, almost always the journal ID
	 * @return true if the mob is an admin, and false otherwise
	 */
	public static boolean isJournalAccessAllowed(MOB mob, String journalFlagName)
	{
		journalFlagName=journalFlagName.trim().toUpperCase();
		if(mob==null)
			return false;
		if(isASysOp(mob))
			return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		if(mob.playerStats().getSecurityFlags().containsJournal(journalFlagName))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).containsJournal(journalFlagName))
			return true;
		return false;
	}

	/**
	 * Checks whether the given mob, currently in the given room, has any of the security flags 
	 * denoted by the given security group.
	 * @param mob the user/player to check security settings on
	 * @param room the user/players current room location, for subop (area) permission checks
	 * @param secGroup the security group whose flags are cross-referenced against the players security settings
	 * @return true if any of the security group flags are permitted by this user, false otherwise
	 */
	public static final boolean isAllowedContainsAny(final MOB mob, final Room room, final SecGroup secGroup)
	{
		if(mob==null)
			return false;
		if(isASysOp(mob))
			return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		if(mob.playerStats().getSecurityFlags().containsAny(secGroup, subop))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).containsAny(secGroup, subop))
			return true;
		return false;
	}

	/**
	 * A Security System check method.  Checks whether the given user/player mob, who is presently
	 * in the given room, is permitted to perform the function denoted by the given security flag.
	 * The room check allows subop/area based security to come into play.
	 * This is the most commonly used security method
	 * @param mob the user/player to check security permissions on
	 * @param room the current room location of the above user/player
	 * @param flag the security flag to check for
	 * @return true if the user/player is permitted, and false otherwise
	 */
	public static final boolean isAllowed(final MOB mob, final Room room, final SecFlag flag)
	{
		if(mob==null)
			return false;
		if(isASysOp(mob))
			return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		final boolean subop=((room!=null)&&(room.getArea()!=null)&&(room.getArea().amISubOp(mob.Name())));
		if(mob.playerStats().getSecurityFlags().contains(flag, subop))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).contains(flag, subop))
			return true;
		return false;
	}

	/**
	 * Checks whether the given mob has any of the security flags denoted by the given security group.
	 * This method checks all locations, allowing subops to be triggered based on their area permissions,
	 * even if they are not presently in that area.
	 * @param mob the user/player to check security settings on
	 * @param secGroup the security group whose flags are cross-referenced against the players security settings
	 * @return true if any of the security group flags are permitted by this user, false otherwise
	 */
	public static final boolean isAllowedAnywhereContainsAny(final MOB mob, final SecGroup secGroup)
	{
		if(mob==null)
			return false;
		if(isASysOp(mob))
			return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
		{
			final boolean subop=e.nextElement().amISubOp(mob.Name());
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

	/**
	 * A Security system check method.  Returns whether the given player mob object is
	 * permitted to perform the function denoted by the given flag everywhere in the mud.
	 * This tends to discount subop/area based permissions.
	 * @param mob the player to check
	 * @param flag the flag to look for
	 * @return true if the player is permitted, and false otherwise
	 */
	public static final boolean isAllowedEverywhere(final MOB mob, final SecFlag flag)
	{
		if(mob==null)
			return false;
		if(isASysOp(mob))
			return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		if(mob.playerStats().getSecurityFlags().contains(flag, false))
			return true;
		if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).contains(flag, false))
			return true;
		return false;
	}

	/**
	 * A Security system check method.  Returns whether the given player mob object is
	 * permitted to perform the function denoted by the given flag anywhere in the mud.
	 * This tends to favor subop/area based permissions, since it checks for subop
	 * security even when the player is not currently in their area.
	 * @param mob the player to check
	 * @param flag the flag to look for
	 * @return true if the player is permitted, and false otherwise
	 */
	public static final boolean isAllowedAnywhere(final MOB mob, final SecFlag flag)
	{
		if(mob==null)
			return false;
		if(isASysOp(mob))
			return true;
		if((mob.playerStats()==null)
		||((mob.soulMate()!=null)&&(!mob.soulMate().isAttributeSet(MOB.Attrib.SYSOPMSGS))))
			return false;
		if(isAllowedEverywhere(mob,flag.getRegularAlias()))
			return true;
		if(flag.areaAlias!=flag)
		{
			for(final Enumeration<Area> e=CMLib.map().areas();e.hasMoreElements();)
			{
				final boolean subop=e.nextElement().amISubOp(mob.Name());
				if(subop)
				{
					if(mob.playerStats().getSecurityFlags().contains(flag, true))
						return true;
					if(mob.baseCharStats().getCurrentClass().getSecurityFlags(mob.baseCharStats().getCurrentClassLevel()).contains(flag, true))
						return true;
					break;
				}
			}
		}
		return isAllowedEverywhere(mob,flag.getRegularAlias());
	}

	/**
	 * Returns whether the given save flag is set, denoting something
	 * that should not be saved but normally is, or something that
	 * should be saved, but normally isn't
	 * @param key the SaveFlag enum object to check for
	 * @return true if the SaveFlag was found, false otherwise.
	 */
	public static final boolean isSaveFlag(final SaveFlag key)
	{
		final CMSecurity inst = instance();
		return (inst.saveFlags.size()>0) && inst.saveFlags.contains(key);
	}

	/**
	 * For muds using the JavaScript approval system, as specified in the coffeemud.ini
	 * file, this method approves a JavaScript as being permitted to run, and saves
	 * the record of the approval.
	 * @param approver the name of the player approving the script
	 * @param hashCode the hash value of the script being approved
	 */
	public static final void approveJScript(final String approver, final long hashCode)
	{
		if(CMProps.getIntVar(CMProps.Int.JSCRIPTS)!=JSCRIPT_REQ_APPROVAL)
			return;
		final Map<Long,String> approved=CMSecurity.getApprovedJScriptTable();
		if(approved.containsKey(Long.valueOf(hashCode)))
			approved.remove(Long.valueOf(hashCode));
		approved.put(Long.valueOf(hashCode),approver);
		final StringBuffer newApproved=new StringBuffer("");
		for(final Long L : approved.keySet())
		{
			final Object O=approved.get(L);
			if(CMLib.players().playerExists(O.toString()))
				newApproved.append(L.toString()+"="+((String)O)+"\n");
		}
		Resources.saveFileResource("::jscripts.ini",null,newApproved);
	}

	/**
	 * For muds using the JavaScript approval system, this method returns 
	 * the list of approval script approver names and their script hash value keys.
	 * @return the list of approved scripts and their approvers, keyed by their script hash values
	 */
	public static final Map<Long,String> getApprovedJScriptTable()
	{
		Map<Long,String> approved=(Map<Long, String>)Resources.getResource("APPROVEDJSCRIPTS");
		if(approved==null)
		{
			approved=new Hashtable<Long,String>();
			Resources.submitResource("APPROVEDJSCRIPTS",approved);
			final List<String> jscripts=Resources.getFileLineVector(Resources.getFileResource("jscripts.ini",false));
			if((jscripts!=null)&&(jscripts.size()>0))
			{
				for(int i=0;i<jscripts.size();i++)
				{
					final String s=jscripts.get(i);
					final int x=s.indexOf('=');
					if(x>0)
						approved.put(Long.valueOf(CMath.s_long(s.substring(0,x))),s.substring(x+1));
				}
			}
		}
		return approved;
	}

	/**
	 * Checks whether the given specific javascript is allowed to run, given the javascript
	 * security settings.
	 * @param script the script to check
	 * @return true if it can run right now, false otherwise.
	 */
	public static final boolean isApprovedJScript(final StringBuffer script)
	{
		if(CMProps.getIntVar(CMProps.Int.JSCRIPTS)==CMSecurity.JSCRIPT_ALL_APPROVAL)
			return true;
		if(CMProps.getIntVar(CMProps.Int.JSCRIPTS)==CMSecurity.JSCRIPT__NO_APPROVAL)
			return false;
		final Map<Long,String> approved=CMSecurity.getApprovedJScriptTable();
		final Long hashCode=Long.valueOf(script.toString().hashCode());
		final String approver=approved.get(hashCode);
		if(approver==null)
		{
			approved.put(hashCode,script.toString());
			return false;
		}
		return true;
	}

	/**
	 * An enumeration of all system currently being debugged.
	 * @return enumeration of all system currently being debugged.
	 */
	public static Enumeration<DbgFlag> getDebugEnum()
	{
		return new IteratorEnumeration<DbgFlag>(instance().dbgVars.iterator());
	}

	/**
	 * Checks if the given key system is being debugged.
	 * @param key the DbgFlag debug system to check
	 * @return true if the given system is being debugged, false otherwise
	 */
	public static final boolean isDebugging(final DbgFlag key)
	{
		final CMSecurity inst = instance();
		return ((inst.dbgVars.size()>0)&&inst.dbgVars.contains(key))||debuggingEverything;
	}

	/**
	 * Checks if the given key system is being debugged by finding the DbgFlag object
	 * that corresponds to the key name, and seeing if it is on the list of debugged
	 * systems.
	 * @param key the name of the debug flag to look for.
	 * @return true if the key was found and is being debugged, false otherwise
	 */
	public static final boolean isDebuggingSearch(final String key)
	{
		final DbgFlag flag=(DbgFlag)CMath.s_valueOf(DbgFlag.values(),key.toUpperCase().trim());
		if(flag==null)
			return false;
		return isDebugging(flag);
	}

	/**
	 * Sets the given debug flag by adding it to the list of debug flags.
	 * @param var the debug flag
	 * @return true if the flag was not already set, false otherwise
	 */
	public static final boolean setDebugVar(final DbgFlag var)
	{
		final CMSecurity inst = instance();
		if((var!=null)&&(!inst.dbgVars.contains(var)))
		{
			inst.dbgVars.add(var);
			return true;
		}
		return false;
	}

	/**
	 * Removes the given debug flag by removing it from the list of debug flags.
	 * @param var the debug flag
	 * @return true if the flag needed removing, false otherwise
	 */
	public static final boolean removeDebugVar(final DbgFlag var)
	{
		final CMSecurity inst = instance();
		if((var!=null)&&(inst.dbgVars.size()>0))
			return inst.dbgVars.remove(var);
		return false;
	}

	/**
	 * Sets the given debug flag by finding the flag that corresponds to the given
	 * string and adding it to the list of debug flags.
	 * @param anyFlag the name of the debug flag
	 * @return true if the flag was found but not already set, false otherwise
	 */
	public static final boolean setDebugVar(final String anyFlag)
	{
		final String flag = anyFlag.toUpperCase().trim();
		final DbgFlag dbgFlag = (DbgFlag)CMath.s_valueOf(CMSecurity.DbgFlag.values(), flag);
		if(dbgFlag!=null)
			return setDebugVar(dbgFlag);
		return false;
	}

	/**
	 * Removes the given debug flag by finding the flag that corresponds to the given
	 * string and removing it from the list of debug flags.
	 * @param anyFlag the name of the debug flag
	 * @return true if the flag was found and needed removing, false otherwise
	 */
	public static final boolean removeDebugVar(final String anyFlag)
	{
		final String flag = anyFlag.toUpperCase().trim();
		final DbgFlag dbgFlag = (DbgFlag)CMath.s_valueOf(CMSecurity.DbgFlag.values(), flag);
		if(dbgFlag!=null)
			return removeDebugVar(dbgFlag);
		return false;
	}

	/**
	 * Sets all DbgFlag debug flags given a comma-delimited list of debug
	 * flag names.  
	 * @param varList the comma-delimited list of debug flags to set.
	 */
	public static final void setDebugVars(final String varList)
	{
		final List<String> V=CMParms.parseCommas(varList.toUpperCase(),true);
		final CMSecurity inst = instance();
		inst.dbgVars.clear();
		for(final String var : V)
		{
			final DbgFlag flag=(DbgFlag)CMath.s_valueOf(DbgFlag.values(),var);
			if(flag==null)
				Log.errOut("CMSecurity","Unable DEBUG flag: "+var);
			else
				inst.dbgVars.add(flag);
		}
		debuggingEverything = inst.dbgVars.contains(DbgFlag.EVERYTHING);
	}

	/**
	 * Check to see if the given Race is enabled.
	 * @param ID the official Race ID
	 * @return true if it is enabled, false otherwise
	 */
	public static final boolean isRaceEnabled(final String ID)
	{
		return (ID==null) ? false : instance().racEnaVars.containsKey(ID.toUpperCase());
	}

	/**
	 * Check to see if the given Character Class is enabled.
	 * @param ID the official Class ID
	 * @return true if it is enabled, false otherwise
	 */
	public static final boolean isCharClassEnabled(final String ID)
	{
		return (ID==null) ? false : instance().clsEnaVars.containsKey(ID.toUpperCase());
	}

	/**
	 * Since there are several different kinds of enable flags, this method
	 * will check the prefix of the flag to determine which kind it is, 
	 * returning the string set that corresponds to one of the special
	 * ones, such as for abilities, expertises, commands, or factions.
	 * A return of null means it's probably a random enable flag.
	 * @param anyFlag the flag for the thing to enable
	 * @return the correct set that this flag will end up belonging in
	 */
	private static final Map<String,String[]> getSpecialEnableMap(final String anyFlag)
	{
		final String flag = anyFlag.toUpperCase().trim();
		if(flag.startsWith(XABLE_PREFIX_ABILITY))
		{
		}
		else
		if(flag.startsWith(XABLE_PREFIX_EXPERTISE))
		{
		}
		else
		if(flag.startsWith(XABLE_PREFIX_COMMAND))
		{
		}
		else
		if(flag.startsWith(XABLE_PREFIX_RACE))
		{
			return instance().racEnaVars;
		}
		else
		if(flag.startsWith(XABLE_PREFIX_CHARCLASS))
		{
			return instance().clsEnaVars;
		}
		else
		if(flag.startsWith(XABLE_PREFIX_FACTION))
		{
		}
		return null;
	}

	/**
	 * Returns an enumeration of all basic enables flags that are currently set,
	 * meaning all the things returned are enabled, but normally disabled presently.
	 * @return an enumeration of all enable flags that are currently set
	 */
	public static Enumeration<Object> getEnablesEnum() 
	{ 
		MultiEnumeration m = new MultiEnumeration(getEnabledSpecialsEnum(true));
		return m;
	}
	
	/**
	 * Returns an enumeration of the enabled race IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the enabled race IDs
	 */
	public static final Enumeration<String> getEnabledRacesEnum(final boolean addINIPrefix)
	{
		return getSafeEnumerableEnableParmSet(instance().racEnaVars, addINIPrefix?XABLE_PREFIX_RACE:"");
	}

	/**
	 * Returns an enumeration of the enabled IDs, with parms, and 
	 * complete with flag prefix, if requested.
	 * @param map the enabled set to use
	 * @param prefix the prefix to add, if any
	 * @return an enumeration of the enabled IDs
	 */
	protected static Enumeration<String> getSafeEnumerableEnableParmSet(final Map<String,String[]> map, final String prefix)
	{
		final TreeSet<String> newSet = new TreeSet<String>();
		for(String key : map.keySet())
		{
			if(key != null)
				newSet.add(prefix + key + ((map.get(key).length==0)?"":(" "+CMParms.combineWSpaces(map.get(key)))));
		}
		return new IteratorEnumeration<String>(newSet.iterator());
	}
	
	/**
	 * Returns an enumeration of the enabled character class IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the enabled character class IDs
	 */
	public static final Enumeration<String> getEnabledCharClassEnum(final boolean addINIPrefix)
	{
		return getSafeEnumerableEnableParmSet(instance().clsEnaVars, addINIPrefix?XABLE_PREFIX_CHARCLASS:"");
	}

	/**
	 * Since there are several different kinds of enable flags, this method
	 * allows of the different kinds to be removed/un-set simply by sending the string.
	 * @param anyFlag the thing to re-enable
	 * @return true if anyFlag was a valid thing to re-enable, and false otherwise
	 */
	public static final boolean removeAnyEnableVar(final String anyFlag)
	{
		final Map<String,String[]> set = getSpecialEnableMap(anyFlag);
		if(set == null)
		{
			/*
			String flag = anyFlag.toUpperCase().trim();
			final DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null)
			{
				removeDisableVar(disFlag);
				return true;
			}
			*/
		}
		else
		if(set.size()>0)
		{
			final String flag = getFinalSpecialXableFlagName(anyFlag);
			set.remove(flag);
		}
		return true;
	}

	/**
	 * Since there are several different kinds of enable flags, this method
	 * allows of the different kinds to be set simply by sending the string.
	 * @param anyFlag the thing to enable
	 * @return true if anyFlag was a valid thing to enable, and false otherwise
	 */
	public static final boolean setAnyEnableVar(final String anyFlag)
	{
		final Map<String,String[]> set = getSpecialEnableMap(anyFlag);
		if(set == null)
		{
			/*
			String flag = anyFlag.toUpperCase().trim();
			final DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null)
			{
				return setDisableVar(disFlag);
			}
			*/
		}
		else
		{
			List<String> flagList = CMParms.parse(anyFlag.toUpperCase().trim());
			if(flagList.size()>0)
			{
				final String flag = getFinalSpecialXableFlagName(flagList.get(0));
				if(!set.containsKey(flag))
				{
					flagList.remove(0);
					set.put(flag,flagList.toArray(new String[0]));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether the feature described by the given anyFlag is enabled.
	 * Since there are several different kinds of enable flags, this method
	 * will check the prefix of each flag to determine which kind it is.
	 * @param anyFlag the flag to check for
	 * @return true if it's already enabled, and false otherwise
	 */
	public static final boolean isAnyFlagEnabled(final String anyFlag)
	{
		final Map<String,String[]> set = getSpecialEnableMap(anyFlag);
		if(set == null)
		{
			/*
			String flag = anyFlag.toUpperCase().trim();
			final DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null)
			{
				return isDisabled(disFlag);
			}
			*/
		}
		else
		if(set.size()>0)
		{
			final String flag = anyFlag.toUpperCase().trim();
			final String flagName = getFinalSpecialXableFlagName(flag);
			return set.containsKey(flagName);
		}
		return false;
	}

	/**
	 * If the feature described by the given anyFlag is enabled, this will
	 * return any parameters defined along with it.  Normally this is an
	 * empty string array, but character classes may have race qualification
	 * flags added after them, which this supports.
	 * @param anyFlag the flag to check for
	 * @return an empty array, or an array of parameters after the flag set
	 */
	public static final String[] getAnyFlagEnabledParms(final String anyFlag)
	{
		final Map<String,String[]> set = getSpecialEnableMap(anyFlag);
		if((set != null)&&(set.size()>0))
		{
			final String flag = anyFlag.toUpperCase().trim();
			final String flagName = getFinalSpecialXableFlagName(flag);
			if(set.containsKey(flagName))
				return set.get(flagName);
		}
		return emptyStrArray;
	}
	
	/**
	 * Sets all enable flags of all types given a list of comma-delimited
	 * flag names in a string.
	 * Since there are several different kinds of enable flags, this method
	 * will check the prefix of each flag to determine which kind it is. It
	 * will log an error if any flag is unrecognized. 
	 * @param commaDelimFlagList the list of flags, comma delimited
	 */
	public static final void setAnyEnableVars(final String commaDelimFlagList)
	{
		final List<String> V=CMParms.parseCommas(commaDelimFlagList.toUpperCase(),true);
		for(final String var : V)
		{
			if(!setAnyEnableVar(var))
			{
				Log.errOut("CMSecurity","Unknown or duplicate enable flag: "+var);
			}
		}
	}

	/**
	 * Returns an enumeration of the enabled ability IDs, command IDs, charclass IDs, and race IDs 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the enabled ability, command, charclass, and race IDs
	 */
	public static final Enumeration<String> getEnabledSpecialsEnum(final boolean addINIPrefix)
	{
		MultiEnumeration<String> menums = new MultiEnumeration<String>(getEnabledCharClassEnum(addINIPrefix));
		menums.addEnumeration(getEnabledRacesEnum(true));
		return menums;
	}

	/**
	 * Returns an enumeration of all basic and special DisFlags that are currently set,
	 * meaning all the standard basic systems returned are disabled presently.
	 * @return an enumeration of all basic DisFlags that are currently set
	 */
	public static Enumeration<DisFlag> getBasicDisablesEnum() 
	{ 
		return new IteratorEnumeration<DisFlag>(instance().disVars.iterator());
	}

	/**
	 * Returns an enumeration of all disable flags that are currently set,
	 * meaning all the things returned are disabled presently.
	 * @return an enumeration of all flags that are currently set
	 */
	public static Enumeration<Object> getDisablesEnum() 
	{ 
		Enumeration<DisFlag> e=new IteratorEnumeration<DisFlag>(instance().disVars.iterator());
		MultiEnumeration m = new MultiEnumeration(e);
		return m.addEnumeration(getDisabledSpecialsEnum(true));
	}

	/**
	 * Checks to see if the given feature denoted by the given DisFlag
	 * is disabled.
	 * @param flag the DisFlag to check for
	 * @return true if it is disabled, and false otherwise
	 */
	public static final boolean isDisabled(final DisFlag flag)
	{
		return instance().disVars.contains(flag);
	}

	/**
	 * Check to see if the given Command is disabled.
	 * @param ID the official Command ID
	 * @return true if it is disabled, false otherwise
	 */
	public static final boolean isCommandDisabled(final String ID)
	{
		return (ID==null) ? false : instance().cmdDisVars.contains(ID.toUpperCase());
	}

	/**
	 * Check to see if the given Race is disabled.
	 * @param ID the official Race ID
	 * @return true if it is disabled, false otherwise
	 */
	public static final boolean isRaceDisabled(final String ID)
	{
		return (ID==null) ? false : instance().racDisVars.contains(ID.toUpperCase());
	}

	/**
	 * Check to see if the given Character Class is disabled.
	 * @param ID the official Class ID
	 * @return true if it is disabled, false otherwise
	 */
	public static final boolean isCharClassDisabled(final String ID)
	{
		return (ID==null) ? false : instance().clsDisVars.contains(ID.toUpperCase());
	}

	/**
	 * Check to see if the given Ability is disabled.
	 * @param ID the official Ability ID
	 * @return true if it is disabled, false otherwise
	 */
	public static final boolean isAbilityDisabled(final String ID)
	{
		return (ID==null) ? false : instance().ablDisVars.contains(ID.toUpperCase());
	}

	/**
	 * Check to see if the given Faction is disabled.
	 * @param ID the official Faction ID
	 * @return true if it is disabled, false otherwise
	 */
	public static final boolean isFactionDisabled(final String ID)
	{
		return (ID==null) ? false : instance().facDisVars.contains(ID.toUpperCase());
	}

	/**
	 * Check to see if the given expertise is disabled.
	 * @param ID the official expertise ID
	 * @return true if it is disabled, false otherwise
	 */
	public static final boolean isExpertiseDisabled(final String ID)
	{
		return (ID==null) ? false : instance().expDisVars.contains(ID.toUpperCase());
	}

	/**
	 * Returns whether the feature described by the given anyFlag is disabled.
	 * Since there are several different kinds of disable flags, this method
	 * will check the prefix of each flag to determine which kind it is.
	 * @param anyFlag the flag to check for
	 * @return true if it's already disabled, and false otherwise
	 */
	public static final boolean isAnyFlagDisabled(final String anyFlag)
	{
		final Set<String> set = getSpecialDisableSet(anyFlag);
		if(set == null)
		{
			String flag = anyFlag.toUpperCase().trim();
			final DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null)
			{
				return isDisabled(disFlag);
			}
		}
		else
		if(set.size()>0)
		{
			final String flag = anyFlag.toUpperCase().trim();
			final String flagName = getFinalSpecialXableFlagName(flag);
			return set.contains(flagName);
		}
		return false;
	}

	/**
	 * Sets all disable flags of all types given a list of comma-delimited
	 * flag names in a string.
	 * Since there are several different kinds of disable flags, this method
	 * will check the prefix of each flag to determine which kind it is. It
	 * will log an error if any flag is unrecognized. 
	 * @param commaDelimFlagList the list of flags, comma delimited
	 */
	public static final void setAnyDisableVars(final String commaDelimFlagList)
	{
		final List<String> V=CMParms.parseCommas(commaDelimFlagList.toUpperCase(),true);
		final CMSecurity inst=instance();
		inst.disVars.clear();
		for(final String var : V)
		{
			if(!setAnyDisableVar(var))
			{
				Log.errOut("CMSecurity","Unknown or duplicate disable flag: "+var);
			}
		}
	}

	/**
	 * Since there are several different kinds of dis/enable flags, this method
	 * will check the prefix of the flag to determine which kind it is, 
	 * and return the remaining portion, which is the important definition
	 * of the flag.
	 * @param anyFlag the full undetermined flag name
	 * @return null if its not a special flag, and the sub-portion otherwise 
	 */
	private static final String getFinalSpecialXableFlagName(final String anyFlag)
	{
		final String flag = anyFlag.toUpperCase().trim();
		if(flag.startsWith(XABLE_PREFIX_ABILITY) 
		|| flag.startsWith(XABLE_PREFIX_EXPERTISE) 
		|| flag.startsWith(XABLE_PREFIX_COMMAND) 
		|| flag.startsWith(XABLE_PREFIX_RACE) 
		|| flag.startsWith(XABLE_PREFIX_CHARCLASS) 
		|| flag.startsWith(XABLE_PREFIX_FACTION))
		{
			final int underIndex=flag.indexOf('_')+1;
			return flag.substring(underIndex);
		}
		return null;
	}
	
	/**
	 * Since there are several different kinds of disable flags, this method
	 * will check the prefix of the flag to determine which kind it is, 
	 * returning the string set that corresponds to one of the special
	 * ones, such as for abilities, expertises, commands, or factions.
	 * A return of null means it's probably a normal disable flag.
	 * @param anyFlag the flag for the thing to disable or re-enable
	 * @return the correct set that this flag will end up belonging in
	 */
	private final static Set<String> getSpecialDisableSet(final String anyFlag)
	{
		final String flag = anyFlag.toUpperCase().trim();
		if(flag.startsWith(XABLE_PREFIX_ABILITY))
		{
			return instance().ablDisVars;
		}
		else
		if(flag.startsWith(XABLE_PREFIX_EXPERTISE))
		{
			return instance().expDisVars;
		}
		else
		if(flag.startsWith(XABLE_PREFIX_COMMAND))
		{
			return instance().cmdDisVars;
		}
		else
		if(flag.startsWith(XABLE_PREFIX_RACE))
		{
			return instance().racDisVars;
		}
		else
		if(flag.startsWith(XABLE_PREFIX_CHARCLASS))
		{
			return instance().clsDisVars;
		}
		else
		if(flag.startsWith(XABLE_PREFIX_FACTION))
		{
			return instance().facDisVars;
		}
		return null;
	}

	/**
	 * Since there are several different kinds of disable flags, this method
	 * allows of the different kinds to be removed/un-set simply by sending the string.
	 * The DisFlag objects are covered by this, but so are command disablings,
	 * abilities, expertises, etc.. 
	 * @param anyFlag the thing to re-enable
	 * @return true if anyFlag was a valid thing to re-enable, and false otherwise
	 */
	public static final boolean removeAnyDisableVar(final String anyFlag)
	{
		final Set<String> set = getSpecialDisableSet(anyFlag);
		if(set == null)
		{
			String flag = anyFlag.toUpperCase().trim();
			final DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null)
			{
				removeDisableVar(disFlag);
				return true;
			}
		}
		else
		if(set.size()>0)
		{
			final String flag = getFinalSpecialXableFlagName(anyFlag);
			set.remove(flag);
		}
		return true;
	}

	/**
	 * Since there are several different kinds of disable flags, this method
	 * allows of the different kinds to be set simply by sending the string.
	 * The DisFlag objects are covered by this, but so are command disablings,
	 * abilities, expertises, etc.. 
	 * @param anyFlag the thing to disable
	 * @return true if anyFlag was a valid thing to disable, and false otherwise
	 */
	public final static boolean setAnyDisableVar(final String anyFlag)
	{
		final Set<String> set = getSpecialDisableSet(anyFlag);
		if(set == null)
		{
			String flag = anyFlag.toUpperCase().trim();
			final DisFlag disFlag = (DisFlag)CMath.s_valueOf(CMSecurity.DisFlag.values(), flag);
			if(disFlag!=null)
			{
				return setDisableVar(disFlag);
			}
		}
		else
		{
			final String flag = getFinalSpecialXableFlagName(anyFlag);
			if(!set.contains(flag))
			{
				set.add(flag);
				return true;
			}
		}
		return false;
	}

	/**
	 * Internal method for returning an enumeration of a given iterator, where the strings
	 * in the iterator have a prefix re-added to them before returning.
	 * @param iter the iterator to enumerate on
	 * @param prefix the prefix to add to all iterator strings
	 * @return the enumeration of the iterator
	 */
	private static final Enumeration<String> getSpecialXabledEnum(final Iterator<String> iter, final String prefix)
	{
		return new Enumeration<String>()
		{
			@Override
			public boolean hasMoreElements() 
			{
				return iter.hasNext();
			}

			@Override
			public String nextElement() 
			{
				final String ID = iter.next();
				if(ID != null)
					return prefix + ID;
				return null;
			}
		};
	}
	
	/**
	 * Returns an enumeration of the disabled ability IDs, command IDs, expertise IDs, and faction IDs 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the disabled ability, command, expertise, and faction IDs
	 */
	public static final Enumeration<String> getDisabledSpecialsEnum(final boolean addINIPrefix)
	{
		MultiEnumeration<String> menums = new MultiEnumeration<String>(getDisabledAbilitiesEnum(addINIPrefix));
		menums.addEnumeration(getDisabledExpertisesEnum(true));
		menums.addEnumeration(getDisabledCommandsEnum(true));
		menums.addEnumeration(getDisabledFactionsEnum(true));
		menums.addEnumeration(getDisabledCharClassEnum(true));
		menums.addEnumeration(getDisabledRacesEnum(true));
		return menums;
	}
	
	/**
	 * Returns an enumeration of the disabled ability IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the disabled ability IDs
	 */
	public static final Enumeration<String> getDisabledAbilitiesEnum(final boolean addINIPrefix)
	{
		return getSpecialXabledEnum(instance().ablDisVars.iterator(), addINIPrefix?XABLE_PREFIX_ABILITY:"");
	}

	/**
	 * Returns an enumeration of the disabled expertise IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the disabled expertise IDs
	 */
	public static final Enumeration<String> getDisabledExpertisesEnum(final boolean addINIPrefix)
	{
		return getSpecialXabledEnum(instance().expDisVars.iterator(), addINIPrefix?XABLE_PREFIX_EXPERTISE:"");
	}

	/**
	 * Returns an enumeration of the disabled command IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the disabled command IDs
	 */
	public static final Enumeration<String> getDisabledCommandsEnum(final boolean addINIPrefix)
	{
		return getSpecialXabledEnum(instance().cmdDisVars.iterator(), addINIPrefix?XABLE_PREFIX_COMMAND:"");
	}

	/**
	 * Returns an enumeration of the disabled race IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the disabled race IDs
	 */
	public static final Enumeration<String> getDisabledRacesEnum(final boolean addINIPrefix)
	{
		return getSpecialXabledEnum(instance().racDisVars.iterator(), addINIPrefix?XABLE_PREFIX_RACE:"");
	}

	/**
	 * Returns an enumeration of the disabled character class IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the disabled character class IDs
	 */
	public static final Enumeration<String> getDisabledCharClassEnum(final boolean addINIPrefix)
	{
		return getSpecialXabledEnum(instance().clsDisVars.iterator(), addINIPrefix?XABLE_PREFIX_CHARCLASS:"");
	}

	/**
	 * Returns an enumeration of the disabled faction IDs, 
	 * complete with flag prefix, if requested.
	 * @param addINIPrefix true to add the prefix required in the ini file, false for a plain ID
	 * @return an enumeration of the disabled faction IDs
	 */
	public static final Enumeration<String> getDisabledFactionsEnum(final boolean addINIPrefix)
	{
		return getSpecialXabledEnum(instance().facDisVars.iterator(), addINIPrefix?XABLE_PREFIX_FACTION:"");
	}

	/**
	 * Adds the given disable flag, activating it
	 * @param flag the DisFlag to add.
	 * @return true if the flag was changed, false if it was already set
	 */
	public static final boolean setDisableVar(final DisFlag flag)
	{
		final CMSecurity inst=instance();
		if((flag!=null)&&(!inst.disVars.contains(flag)))
		{
			inst.disVars.add(flag);
			return true;
		}
		return false;
	}

	/**
	 * Removes the given disable flag, deactivating it
	 * @param flag the DisFlag to remove.
	 * @return true if the flag was removed, false if it was not set
	 */
	public static final boolean removeDisableVar(final DisFlag flag)
	{
		final CMSecurity inst = instance();
		if((flag!=null)&&(inst.disVars.size()>0))
		{
			return inst.disVars.remove(flag);
		}
		return false;
	}

	/**
	 * Sets all the SaveFlags from a comma-delimited list, activating them.  
	 * @param flagsListStr a comma-delimited list of SaveFlag names
	 */
	public static final void setSaveFlags(final String flagsListStr)
	{
		final List<String> flagsList=CMParms.parseCommas(flagsListStr.toUpperCase(),true);
		final CMSecurity inst = instance();
		inst.saveFlags.clear();
		for(final String flag : flagsList)
		{
			SaveFlag flagObj = (SaveFlag)CMath.s_valueOf(CMSecurity.SaveFlag.class, flag);
			if(flagObj != null)
			{
				inst.saveFlags.add(flagObj);
			}
		}
	}

	/**
	 * Adds the given save flag, activating it
	 * @param flag the SaveFlag to add.
	 */
	public static final void setSaveFlag(final SaveFlag flag)
	{
		if(flag != null)
		{
			final CMSecurity inst=instance();
			if(!inst.saveFlags.contains(flag))
			{
				inst.saveFlags.add(flag);
			}
		}
	}

	/**
	 * Removes the given save flag, deactivating it
	 * @param flag the SaveFlag to remove.
	 */
	public static final void removeSaveFlag(final SaveFlag flag)
	{
		if(flag != null)
		{
			final CMSecurity inst=instance();
			if(inst.saveFlags.size()>0)
			{
				inst.saveFlags.remove(flag);
			}
		}
	}

	/**
	 * How long the system has bee running, in milliseconds, I guess.
	 * @return how long the system has bee running, in milliseconds, I guess.
	 */
	public static final long getStartTime()
	{
		return i().startTime;
	}

	/**
	 * Converts the given InetAddress into a long int
	 * @param addr an InetAddress representing the address to convert
	 * @return the ip address of the given as a long int
	 */
	private static long makeIPNumFromInetAddress(InetAddress addr)
	{
		return ((long)(addr.getAddress()[0] & 0xFF) << 24) 
			| ((long)(addr.getAddress()[1] & 0xFF) << 16) 
			| ((long)(addr.getAddress()[2] & 0xFF) << 8) 
			| (addr.getAddress()[3] & 0xFF);
	}
	
	/**
	 * Converts the given string ipaddress or host name into a long int
	 * @param s an ipaddress or host name
	 * @return the ip address of the given as a long int
	 */
	private static long makeIPNumFromInetAddress(String s)
	{
		try 
		{
			return makeIPNumFromInetAddress(InetAddress.getByName(s.trim()));
		} 
		catch (UnknownHostException e) 
		{
			return 0;
		}
	}
	
	/**
	 * Returns true if the given IP4 address is blocked, and false otherwise
	 * @param ipAddress the IP4 address to look for
	 * @return true if the given IP4 address is blocked, and false otherwise
	 */
	public static boolean isIPBlocked(String ipAddress)
	{
		final LongSet group = CMSecurity.getIPBlocks();
		final boolean chk = ((group != null) && (group.contains(makeIPNumFromInetAddress(ipAddress))));
		/*
		 if(chk && isDebugging(DbgFlag.TEMPMISC))
		{
			Log.debugOut("Blocking "+ipAddress+": ("+makeIPNumFromInetAddress(ipAddress)+")");
			try
			{
				InetAddress addr = InetAddress.getByName(ipAddress.trim());
				Log.debugOut("Blocking "+ipAddress+": ("+addr.toString()+")");
				Log.debugOut("Blocking "+ipAddress+": ("+CMParms.toStringList(addr.getAddress())+")");
			}
			catch (UnknownHostException e)
			{
				Log.errOut(e);
			}
		}
		*/
		return chk;
	}
	
	/**
	 * Returns all blocked IP4 addresses
	 * @return all blocked IP4 addresses
	 */
	private static LongSet getIPBlocks()
	{
		LongSet group = (LongSet)Resources.getResource("SYSTEM_IP_BLOCKS");
		if(group == null)
		{
			group = new LongSet();
			final String filename = CMProps.getVar(CMProps.Str.BLACKLISTFILE);
			final List<String> ipList = Resources.getFileLineVector(Resources.getFileResource(filename, false));
			for(String ip : ipList)
			{
				if(ip.trim().startsWith("#")||(ip.trim().length()==0))
				{
					continue;
				}
				final int x=ip.indexOf('-');
				if(x<0)
				{
					final long num = makeIPNumFromInetAddress(ip.trim());
					if(num > 0)
					{
						group.add(num);
					}
				}
				else
				{
					final long ipFrom = makeIPNumFromInetAddress(ip.substring(0,x).trim());
					final long ipTo = makeIPNumFromInetAddress(ip.substring(x+1).trim());
					if((ipFrom > 0) && (ipTo >= ipFrom))
					{
						group.add(ipFrom,ipTo);
					}
				}
			}
			Resources.submitResource("SYSTEM_IP_BLOCKS", group);
			Resources.removeResource(filename);
		}
		return group;
	}
	
	/**
	 * Returns true if the given name or ip address or whatever is found in the official ::/resources/banned.ini
	 * file, which is cached in a list for quick access.  The search is case insensitive, and any entry may
	 * start with *, end with *, or both.
	 * @param login the string to look for
	 * @return true if there was a match, and false otherwise.
	 */
	public static final boolean isBanned(final String login)
	{
		if((login==null)||(login.length()<=0))
			return false;
		final String uplogin=login.toUpperCase();
		final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		{
			for(int b=0;b<banned.size();b++)
			{
				final String str=banned.get(b);
				if(str.length()>0)
				{
					if(str.equals("*")||((str.indexOf('*')<0))&&(str.equals(uplogin)))
						return true;
					else
					if(str.startsWith("*")&&str.endsWith("*")&&(uplogin.indexOf(str.substring(1,str.length()-1))>=0))
						return true;
					else
					if(str.startsWith("*")&&(uplogin.endsWith(str.substring(1))))
						return true;
					else
					if(str.endsWith("*")&&(uplogin.startsWith(str.substring(0,str.length()-1))))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes the given player name, account name, or ip address  from the official ::/resources/banned.ini file. 
	 * It also removes it from the cached ban list.
	 * @param unBanMe the player name, account name, or ip address to remove
	 */
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
				final String B=banned.get(b);
				if((!B.equals(unBanMe))&&(B.trim().length()>0))
					newBanned.append(B+"\n");
			}
			Resources.updateFileResource("::banned.ini",newBanned);
		}
	}

	/**
	 * Removes the given player name, account name, or ip address that can be found at the given index
	 * from the official ::/resources/banned.ini file. It also removes it from the cached ban list.
	 * @param unBanMe the player name, account name, or ip address index in the banned.ini file to remove
	 * @return The name that was banned
	 */
	public static final String unban(final int unBanMe)
	{
		final StringBuffer newBanned=new StringBuffer("");
		final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		String nameWas="";
		if((banned!=null)&&(banned.size()>0))
		{
			for(int b=0;b<banned.size();b++)
			{
				final String B=banned.get(b);
				if(B.trim().length()>0)
				{
					if((b+1)==unBanMe)
						nameWas=B;
					else
						newBanned.append(B+"\n");
				}
			}
			Resources.updateFileResource("::banned.ini",newBanned);
		}
		return nameWas;
	}

	/**
	 * Adds the given player name, account name, or ip address to the official ::/resources/banned.ini file
	 * It also adds it to the cached ban list.
	 * @param banMe the player name, account name, or ip address to add
	 * @return -1 if the new entry was added, and an index &gt;=0 if it was already on the list beforehand
	 */
	public static final int ban(final String banMe)
	{
		if((banMe==null)||(banMe.length()<=0))
			return -1;
		final List<String> banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
		if((banned!=null)&&(banned.size()>0))
		for(int b=0;b<banned.size();b++)
		{
			final String B=banned.get(b);
			if(B.equals(banMe))
				return b;
		}
		final StringBuffer str=Resources.getFileResource("banned.ini",false);
		if(banMe.trim().length()>0)
			str.append(banMe+"\n");
		Resources.updateFileResource("::banned.ini",str);
		return -1;
	}

	/**
	 * This enum represents all of the base security flags in the system.  Each flag
	 * represents a command, feature, or subsystem that normal players may not normally
	 * have access to, which is why these flags exist to grant it.  Many flags also
	 * include a subop (area) version, which means the user must be in the area that
	 * they are a subop of in order to have the security flag.
	 * @author Bo Zimmerman
	 *
	 */
	public static enum SecFlag
	{
		ABILITIES, ABOVELAW, AFTER, AHELP, ALLSKILLS, ANNOUNCE, AS, AT, BAN, 
		BEACON, BOOT, CARRYALL, CATALOG, CHARGEN, CLOAK, CMD, CMDABILITIES,
		CMDAREAS, CMDCLANS, CMDCLASSES, CMDEXITS, CMDFACTIONS, CMDITEMS, 
		CMDMOBS, CMDPLAYERS, CMDQUESTS, CMDRACES, CMDRECIPES, CMDROOMS,
		CMDSOCIALS, COMPONENTS, COPY, COPYITEMS, COPYMOBS, COPYROOMS, 
		DUMPFILE, EXPERTISE, EXPERTISES, EXPORT, EXPORTFILE, EXPORTPLAYERS,
		GMODIFY, GOTO, I3, IDLEOK, IMC2, IMMORT, IMPORT, IMPORTITEMS, IMPORTMOBS, 
		IMPORTPLAYERS, IMPORTROOMS, JOURNALS, JSCRIPTS, KILL, KILLDEAD,
		LISTADMIN, LOADUNLOAD, MERGE, MXPTAGS, NEWS, NOEXPIRE, NOPURGE, ORDER, 
		PAUSE, PKILL, POLLS, POSSESS, PURGE, RESET, RESETUTILS, RESTRING,
		SESSIONS, SHUTDOWN, SNOOP, STAT, SUPERSKILL, SYSMSGS, TICKTOCK, TITLES, 
		TRAILTO, TRANSFER, WHERE, WIZEMOTE, WIZINV, MISC, CMDDATABASE, EVERY, 
		ACHIEVEMENTS,

		AREA_ABILITIES, AREA_ABOVELAW, AREA_AFTER, AREA_AHELP, AREA_ALLSKILLS, 
		AREA_ANNOUNCE, AREA_AS, AREA_AT, AREA_BAN, AREA_BEACON, AREA_BOOT,
		AREA_CARRYALL, AREA_CATALOG, AREA_CHARGEN, AREA_CLOAK, AREA_CMD, 
		AREA_CMDABILITIES, AREA_CMDAREAS, AREA_CMDCLANS, AREA_CMDCLASSES,
		AREA_CMDEXITS, AREA_CMDFACTIONS, AREA_CMDITEMS, AREA_CMDMOBS, 
		AREA_CMDPLAYERS, AREA_CMDQUESTS, AREA_CMDRACES, AREA_CMDRECIPES,
		AREA_CMDROOMS, AREA_CMDSOCIALS, AREA_COMPONENTS, AREA_COPY, AREA_COPYITEMS, 
		AREA_COPYMOBS, AREA_COPYROOMS, AREA_DUMPFILE, AREA_EXPERTISE,
		AREA_EXPERTISES, AREA_EXPORT, AREA_EXPORTFILE, AREA_EXPORTPLAYERS, 
		AREA_GMODIFY, AREA_GOTO, AREA_I3, AREA_IDLEOK, AREA_IMC2, AREA_IMMORT,
		AREA_IMPORT, AREA_IMPORTITEMS, AREA_IMPORTMOBS, AREA_IMPORTPLAYERS, 
		AREA_IMPORTROOMS, AREA_JOURNALS, AREA_JSCRIPTS, AREA_KILL,
		AREA_KILLDEAD, AREA_LISTADMIN, AREA_LOADUNLOAD, AREA_MERGE, 
		AREA_MXPTAGS, AREA_NEWS, AREA_NOEXPIRE, AREA_NOPURGE, AREA_ORDER,
		AREA_PAUSE, AREA_PKILL, AREA_POLLS, AREA_POSSESS, AREA_PURGE, 
		AREA_RESET, AREA_RESETUTILS, AREA_RESTRING, AREA_SESSIONS,
		AREA_SHUTDOWN, AREA_SNOOP, AREA_STAT, AREA_SUPERSKILL, AREA_SYSMSGS, 
		AREA_TICKTOCK, AREA_TITLES, AREA_TRAILTO, AREA_TRANSFER, AREA_WHERE,
		AREA_WIZEMOTE, AREA_WIZINV, AREA_MISC, AREA_CMDDATABASE, AREA_EVERY
		;
		private SecFlag regularAlias=null;
		private SecFlag areaAlias=null;
		
		private SecFlag()
		{
		}
		
		/**
		 * Fixes all the flags to make sure there is a regular and area (subop) version
		 */
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
		
		/**
		 * Returns the regular (non-subop) (non-area) version of this security flag
		 * @return the regular (non-subop) (non-area) version of this security flag
		 */
		public SecFlag getRegularAlias()
		{
			fixAliases();
			return regularAlias;
		}
		
		/**
		 * Returns the subop (area) version of this security flag, if there is one
		 * @return the subop (area) version of this security flag, if there is one
		 */
		public SecFlag getAreaAlias()
		{
			fixAliases();
			return areaAlias;
		}
	}

	/**
	 * A class representing a file path, either local or virtual, that
	 * a user can be given special access to.  The path can also be
	 * flagged for subop (area) access only, though the usefulness of
	 * that has never been determined.
	 * @author Bo Zimmerman
	 */
	public static class SecPath
	{
		private final String	path;
		private final String	slashPath;
		private final boolean	isVfs;
		private final boolean	isAreaOnly;
		
		/**
		 * Constructs a path object
		 * @param path the unix-like file path to give acesss to
		 * @param isVfs true if the path is virtual (db) only
		 * @param isAreaOnly true if the access is for subops only
		 */
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
		
		/**
		 * Converts this SecPath object back into the string path flag
		 * @return  this SecPath object back into the string path flag
		 */
		@Override
		public String toString()
		{
			return (isVfs?"VFS:":"FS:")+(isAreaOnly?"AREA ":"")+path;
		}
	}

	/**
	 * Internal security class for a group of security flags, and which
	 * may also containing other groups.
	 * @author Bo Zimmerman
	 */
	public static class SecGroup
	{
		private final String		name;
		private Set<SecFlag> 		flags;
		private List<SecGroup>		groups;
		private List<SecPath>		paths;
		private Set<String> 		jFlags;
		private int					numAllFlags;

		/**
		 * Constructor from existing security flag data.
		 * @param name the name of the group
		 * @param flags the basic security flags in this group
		 * @param groups other groups allowed by this group
		 * @param paths filesystem paths that this group grants access to
		 * @param jFlags names of journal that administrative rights are given to
		 */
		public SecGroup(String name, Set<SecFlag> flags, List<SecGroup> groups, List<SecPath> paths, Set<String> jFlags)
		{
			this.name=name;
			reset(flags, groups, paths, jFlags);
		}
		
		/**
		 * Constructor from existing security flag data.
		 * @param name the name of the group
		 * @param flags the basic security flags in this group
		 */
		public SecGroup(String name, Set<SecFlag> flags)
		{
			this.name=name;
			reset(flags,new ArrayList<SecGroup>(1),new ArrayList<SecPath>(1),new SHashSet<String>());
		}
		
		/**
		 * Constructor from existing security flag data.
		 * @param flags the basic security flags in this group
		 */
		public SecGroup(SecFlag[] flags)
		{
			this.name="";
			reset(new SHashSet<SecFlag>(flags),new ArrayList<SecGroup>(1),new ArrayList<SecPath>(1),new SHashSet<String>());
		}
		
		/**
		 * Returns the name of this security group.
		 * @return the name of this security group.
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Re-populates this group from new security flag data.
		 * @param flags the basic security flags in this group
		 * @param groups other groups allowed by this group
		 * @param paths filesystem paths that this group grants access to
		 * @param jFlags names of journal that administrative rights are given to
		 */
		public void reset(Set<SecFlag> flags, List<SecGroup> groups, List<SecPath> paths, Set<String> jFlags)
		{
			this.flags=flags;
			this.numAllFlags=flags.size();
			this.groups=groups;
			this.paths=paths;
			this.jFlags=jFlags;
			numAllFlags+=paths.size();
			for(final SecGroup g : groups) 
				this.numAllFlags+= g.size();
		}
		
		//public SecGroup copyOf() // NOT ALLOWED -- flags are ok, but groups MUST be unmutable for internal changes!!
		
		/**
		 * Checks the given group and all subgroups for access to a journal of the given name
		 * @param journalFlag the name of the journal
		 * @return true if this group grants access, and false otherwise.
		 */
		public boolean containsJournal(String journalFlag)
		{
			if(jFlags.contains(journalFlag))
				return true;
			for(final SecGroup group : groups)
			{
				if(group.containsJournal(journalFlag))
					return true;
			}
			return false;
		}
		
		/**
		 * Checks this group and all subgroups for access to the given security flag, also checking
		 * for subop (area) access if the isSubOp flag is sent.
		 * @param flag the security flag to look for
		 * @param isSubOp true if this should also check for equivalent subop (area) flags
		 * @return true if this group contains the security, false otherwise
		 */
		public boolean contains(SecFlag flag, boolean isSubOp)
		{
			if(flags.contains(flag.getRegularAlias()))
				return true;
			if(isSubOp && flags.contains(flag.getAreaAlias()))
				return true;
			for(final SecGroup group : groups)
			{
				if(group.contains(flag, isSubOp))
					return true;
			}
			return false;
		}
		
		/**
		 * Checks this group and all subgroups for access to any of the security flags or journal flags
		 * in the given group.  It also checks for subop (area) access if the isSubOp flag is set.
		 * @param group the group containing flags to look for in this class
		 * @param isSubOp true if this should also check for equivalent subop (area) flags
		 * @return true if this group contains any of the security, false otherwise
		 */
		public boolean containsAny(SecGroup group, boolean isSubOp)
		{
			for(final SecFlag flag : group.flags)
			{
				if(contains(flag, isSubOp))
					return true;
			}
			for(final String jflag : group.jFlags)
			{
				if(containsJournal(jflag))
					return true;
			}
			for(final SecGroup g : group.groups)
			{
				if(g.containsAny(group, isSubOp))
					return true;
			}
			return false;
		}

		/**
		 * The number of all flags that this group was created with
		 * @return number of all flags that this group was created with
		 */
		public int size()
		{
			return numAllFlags;
		}
		
		/**
		 * Converts this object to a ; delimited string
		 * @return a ; delimited representation of this object.
		 */
		@Override
		public String toString()
		{
			return toString(';');
		}
		
		/**
		 * Returns an exact copy of this object.
		 * @return an exact copy of this object.
		 */
		public SecGroup copyOf()
		{
			return new SecGroup(name,new SHashSet<SecFlag>(flags),new SVector<SecGroup>(groups),new SVector<SecPath>(paths),new SHashSet<String>(jFlags));
		}
		
		/**
		 * Converts this group object to a delimited string
		 * @param separatorChar the delimiter
		 * @return  this group object to a delimited string
		 */
		public String toString(char separatorChar)
		{
			final StringBuilder str=new StringBuilder("");
			for(final SecFlag flag : flags)
				str.append(flag.name()).append(separatorChar);
			for(final SecGroup grp : groups)
				str.append(grp.name).append(separatorChar);
			for(final SecPath path : paths)
				str.append(path.toString()).append(separatorChar);
			for(final String flag : jFlags)
				str.append(flag).append(separatorChar);
			if(str.length()>0)
				return str.toString().substring(0,str.length()-1);
			return "";
		}
		
		/**
		 * Returns an iterator of all file paths that this group
		 * has special access to, including those of subgroups.
		 * @return an iterator of all file paths
		 */
		public Iterator<SecPath> paths()
		{
			return new Iterator<SecPath>()
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
					if(hasNext())
						return p.next();
					throw new java.util.NoSuchElementException();
				}

				@Override
				public void remove()
				{
				}
			};
		}
		
		/**
		 * Returns an iterator through all the security flags that this group
		 * has access to, including all subgroups.
		 * @return an iterator through all the security flags in this group
		 */
		public Iterator<SecFlag> flags()
		{
			return new Iterator<SecFlag>()
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
					if(hasNext())
						return p.next();
					throw new java.util.NoSuchElementException();
				}

				@Override
				public void remove()
				{
				}
			};
		}
	}

	/**
	 * Predefined security group set containing security flags related to the ability
	 * to copy existing mobs, items, and rooms around when building rooms and areas.
	 */
	public static final SecGroup SECURITY_COPY_GROUP=new SecGroup(new SecFlag[]{
		SecFlag.COPY, SecFlag.COPYITEMS, SecFlag.COPYMOBS, SecFlag.COPYROOMS,

		SecFlag.AREA_COPY, SecFlag.AREA_COPYITEMS, SecFlag.AREA_COPYMOBS, SecFlag.AREA_COPYROOMS
	});

	/**
	 * Predefined security group set containing security flags related to the ability to jump
	 * around on the map freely.
	 */
	public static final SecGroup SECURITY_GOTO_GROUP=new SecGroup(new SecFlag[]{
		SecFlag.GOTO,

		SecFlag.AREA_GOTO
	});

	/**
	 * Predefined security group set containing security flags related to the ability to
	 * instantly kill other mobs and players.
	 */
	public static final SecGroup SECURITY_KILL_GROUP=new SecGroup(new SecFlag[]{
		SecFlag.KILL, SecFlag.KILLDEAD,

		SecFlag.AREA_KILL, SecFlag.AREA_KILLDEAD,
	});

	/**
	 * Predefined security group set containing security flags related to the ability to import
	 * mobs, items, players, and rooms from local files into the map.
	 */
	public static final SecGroup SECURITY_IMPORT_GROUP=new SecGroup(new SecFlag[]{
		SecFlag.IMPORT, SecFlag.IMPORTITEMS, SecFlag.IMPORTMOBS, SecFlag.IMPORTPLAYERS,
		SecFlag.IMPORTROOMS,

		SecFlag.AREA_IMPORT, SecFlag.AREA_IMPORTITEMS, SecFlag.AREA_IMPORTMOBS,
		SecFlag.AREA_IMPORTPLAYERS,	SecFlag.AREA_IMPORTROOMS,
	});

	/**
	 * Predefined security group set containing security flags related to the ability to export 
	 * mobs, items, players, and rooms to a local file.
	 */
	public static final SecGroup SECURITY_EXPORT_GROUP=new SecGroup(new SecFlag[]{
		SecFlag.EXPORT, SecFlag.EXPORTFILE, SecFlag.EXPORTPLAYERS,

		SecFlag.AREA_EXPORT, SecFlag.AREA_EXPORTFILE, SecFlag.AREA_EXPORTPLAYERS
	});

	/**
	 * Predefined security group set containing security flags related to the creation, 
	 * modification, and destruction of most subsystems, including basic map editing abilities.
	 */
	public static final SecGroup SECURITY_CMD_GROUP=new SecGroup(new SecFlag[]{
		SecFlag.CMD, SecFlag.CMDABILITIES, SecFlag.CMDAREAS, SecFlag.CMDCLANS, SecFlag.CMDCLASSES,
		SecFlag.CMDEXITS, SecFlag.CMDFACTIONS, SecFlag.CMDITEMS, SecFlag.CMDMOBS, SecFlag.CMDPLAYERS,
		SecFlag.CMDQUESTS, SecFlag.CMDRACES, SecFlag.CMDRECIPES, SecFlag.CMDROOMS, SecFlag.CMDSOCIALS,

		SecFlag.AREA_CMD, SecFlag.AREA_CMDABILITIES, SecFlag.AREA_CMDAREAS, SecFlag.AREA_CMDCLANS,
		SecFlag.AREA_CMDCLASSES, SecFlag.AREA_CMDEXITS, SecFlag.AREA_CMDFACTIONS, SecFlag.AREA_CMDITEMS,
		SecFlag.AREA_CMDMOBS, SecFlag.AREA_CMDPLAYERS, SecFlag.AREA_CMDQUESTS, SecFlag.AREA_CMDRACES,
		SecFlag.AREA_CMDRECIPES, SecFlag.AREA_CMDROOMS, SecFlag.AREA_CMDSOCIALS
	});
	
	/**
	 * Save flags enum, for either turning off things that are normally saved to the database periodically, or
	 * turning on things that are not normally saved so that they do so.
	 * @author Bo Zimmerman
	 *
	 */
	public static enum SaveFlag
	{
		NOPLAYERS,
		NOPROPERTYMOBS,
		NOPROPERTYITEMS,
		ROOMMOBS,
		ROOMSHOPS,
		ROOMITEMS
	}

	/**
	 * This enum represents all permitted DEBUG flags.  Each flag represents a feature or 
	 * subsystem which can generate extra logging, typically on the DEBUG logging channel.
	 * @author Bo Zimmerman
	 */
	public static enum DbgFlag
	{
		PROPERTY("room ownership"), 
		ARREST("law enforcement"), 
		CONQUEST("area conquering"), 
		MUDPERCOLATOR("random area generation"), 
		GMODIFY("global modify command"),
		MERGE("global object merging"), 
		BADSCRIPTS("bad mobprog practices"), 
		TELNET("telnet code negotiation"), 
		CLASSLOADER("java class loading"), 
		DBROOMPOP("room loading"),
		DBROOMS("room db activity"), 
		CMROIT("room item db activity"), 
		CMROEX("room exit db activity"), 
		CMROCH("room mob db activity"), 
		CMAREA("area db activity"),
		CMSTAT("stats db activity"), 
		HTTPMACROS("web macro scripts"), 
		I3("intermud3 communication"), 
		HTTPACCESS("web access logging"), 
		IMC2("intermud chat2 communication"),
		SMTPSERVER("email reception"), 
		UTILITHREAD("session and tech maint"), 
		MISSINGKIDS("babies"), 
		FLAGWATCHING("clan flags"), 
		CATALOGTHREAD("global obj catalog"),
		JOURNALTHREAD("journal msg maint"), 
		MAPTHREAD("room maint"), 
		VACUUM("room/obj expiration"), 
		AUTOPURGE("player/account purging"), 
		PLAYERTHREAD("player maint"),
		OUTPUT("all raw session output"), 
		EXPORT("area exporting"), 
		STATSTHREAD("stats maint"), 
		GEAS("geas/slavery parsing"), 
		SMTPCLIENT("email sending"),
		MESSAGES("internal core msgs"), 
		EVERYTHING("everything"), 
		CMROOM("room db creation"), 
		HTTPREQ("web requests"), 
		CMJRNL("journal db activity"), 
		IMPORT("area importing"),
		PLAYERSTATS("player stat loading"), 
		CLANS("clan maint"), 
		BINOUT("binary telnet input"), 
		BININ("binary telnet output"),
		BOOTSTRAPPER("Bootstrapper"),
		CLANMEMBERS("Clan Membership"),
		INPUT("All user input"),
		SHUTDOWN("System Shutdown"),
		SPACESHIP("Spaceships"),
		SQLERRORS("SQL error traces"),
		GMCP("GMCP Protocol"),
		ELECTRICTHREAD("Electric currents")
		;
		private final String desc;

		DbgFlag(final String description)
		{
			this.desc = description;
		}

		public String description()
		{
			return desc;
		}
	}

	/**
	 * The enum that represents all the defined DISABLE flags.  These typically represents 
	 * features or subsystems that are allowed to be "turned off" in configuration. 
	 * @author Bo Zimmerman
	 *
	 */
	public static enum DisFlag
	{
		LEVELS("player leveling"), 
		EXPERIENCE("player XP gains"), 
		PROPERTYOWNERCHECKS("confirm property ownership"), 
		AUTODISEASE("diseases from races, weather, age, etc.."),
		DBERRORQUE("save SQL errors"), 
		DBERRORQUESTART("retry SQL errors on boot"), 
		CONNSPAMBLOCK("connection spam blocker"), 
		FATAREAS("standard non-thin cached areas"),
		PASSIVEAREAS("inactive area sleeping"), 
		DARKWEATHER("weather causing room darkness"), 
		DARKNIGHTS("time causing room darkness"), 
		ARREST("legal system"),
		EMOTERS("emoter behaviors"), 
		CONQUEST("area clan conquest"), 
		RANDOMITEMS("random item behavior"), 
		MOBILITY("mobile behaviors"), 
		MUDCHAT("MOB chat behavior"),
		RANDOMMONSTERS("random monster behaviors"), 
		RACES("player races"), 
		CLASSES("player classes"), 
		MXP("MXP system"), 
		MSP("MSP system"), 
		QUITREASON("early quitting prompt"),
		CLASSTRAINING("class training"), 
		ROOMVISITS("room visits"), 
		THIRST("player thirst"), 
		HUNGER("player hunger"), 
		WEATHER("area weather"), 
		WEATHERCHANGES("weather changes"),
		WEATHERNOTIFIES("notification of weather changes"), 
		QUESTS("quest system"), 
		SCRIPTABLEDELAY("script event delay"), 
		SCRIPTING("MOBPROG scripting"),
		SCRIPTABLE("MOBProg scripting"), 
		MCCP("MCCP compression"), 
		LOGOUTS("player logouts"), 
		THINAREAS("thin uncached areas"), 
		UTILITHREAD("thread & session monitoring"),
		THREADTHREAD("thread monitoring"), 
		EQUIPSIZE("armor size fitting"), 
		RETIREREASON("early char delete prompt"), 
		MAXCONNSPERACCOUNT("connections per account limit"),
		ALLERGIES("auto player allergies"), 
		LOGINS("non-archin player logins"), 
		NEWPLAYERS("new player creation"), 
		MAXNEWPERIP("new character per ip limit"),
		MAXCONNSPERIP("connections per ip limit"), 
		CLANTICKS("clan ticks/automation"), 
		CATALOGTHREAD("catalog house-cleaning"), 
		NEWCHARACTERS("new character creation"),
		CATALOGCACHE("catalog instance caching"), 
		SAVETHREAD("Player/Journal/Map/Table maintenance"), 
		JOURNALTHREAD("journal house-cleaning"), 
		MAPTHREAD("map house-cleaning"),
		AUTOPURGE("player purging"), 
		PURGEACCOUNTS("account purging"), 
		PLAYERTHREAD("player maintenance/house cleaning"), 
		MSSP("MSSP protocol support"),
		STATS("statistics system"), 
		STATSTHREAD("statistics auto-saving"), 
		POLLCACHE("player poll caching"), 
		SESSIONTHREAD("session monitoring"), 
		SMTPCLIENT("email client"),
		THINGRIDS("thin uncached grids"), 
		FATGRIDS("standard cached grids"), 
		STDRACES("standard player races"), 
		STDCLASSES("standard player classes"),
		CHANNELAUCTION("auction channel"), 
		ELECTRICTHREAD("electric threads"), 
		SPECOMBATTHREAD("special combat threads"), 
		MOBTEACHER("mobteacher"),
		MSDP("msdp variables"),
		GMCP("gmcp variables"), 
		ATTRIBS("char stats"),
		TECHLEVEL("techleveling"),
		AUTOLANGUAGE("auto language switching"), 
		I3("intermud3"),
		IMC2("intermud2"),
		SLOW_AGEING("real ageing"),
		ALL_AGEING("age system"),
		CHANNELBACKLOGS("channel backlog system"),
		MCP("mcp protocol"),
		HYGIENE("hygiene system"),
		ANSIPROMPT("ANSI Y/N Prompt"),
		FOODROT("food/milk rot")
		;
		private final String desc;
		DisFlag(final String description){this.desc=description;}
		public String description() { return desc;}
	}
}
