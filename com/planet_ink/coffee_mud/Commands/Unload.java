package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.CMClass.CMObjectType;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2025 Bo Zimmerman

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
public class Unload extends StdCommand
{
	public Unload()
	{
	}

	private final String[]	access	= I(new String[] { "UNLOAD" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	final String[]	ARCHON_LIST	= {
		"CLASS", "HELP", "USER", "AREA", "FACTION", "ALL", "FILE",
		"RESOURCE", "INIFILE", "ACHIEVEMENTS", "[FILENAME]", "VFS",
		"INI", "SETTINGS", "AWARDS"
	};

	@SuppressWarnings("rawtypes")
	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		if(mob==null)
			return true;
		boolean tryArchon=CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LOADUNLOAD);
		if(commands.size()<2)
		{
			if(tryArchon)
				mob.tell(L("UNLOAD what? Try @x1",CMParms.toListString(ARCHON_LIST)));
			else
				mob.tell(L("Unload what?"));
			return false;
		}
		final String str=CMParms.combine(commands,1);
		String what=commands.get(1);
		if(tryArchon)
		{
			Item I=mob.fetchWieldedItem();
			if((I instanceof AmmunitionWeapon)
			&&((AmmunitionWeapon)I).requiresAmmunition())
				tryArchon=false;
			else
			{
				I=mob.findItem(null, str);
				if((I instanceof AmmunitionWeapon)
				&&(((AmmunitionWeapon)I).requiresAmmunition()))
					tryArchon=false;
				else
				{
					I=mob.location().findItem(null, str);
					if((I instanceof AmmunitionWeapon)
					&&(((AmmunitionWeapon)I).requiresAmmunition())
					&&((AmmunitionWeapon)I).isFreeStanding())
						tryArchon=false;
				}
			}
		}
		for(final String aList : ARCHON_LIST)
		{
			if(what.equalsIgnoreCase(aList))
				tryArchon=true;
		}
		if(!tryArchon)
		{
			commands.remove(0);
			final List<Item> baseItems=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_ANY,false);
			baseItems.addAll(mob.location().findItems(null,CMParms.combine(commands,0)));
			final List<AmmunitionWeapon> items=new XVector<AmmunitionWeapon>();
			for (final Item I : baseItems)
			{
				if((I instanceof AmmunitionWeapon)&&((AmmunitionWeapon)I).requiresAmmunition())
				{
					if(mob.isMine(I))
						items.add((AmmunitionWeapon)I);
					else
					if(((AmmunitionWeapon)I).isFreeStanding())
						items.add((AmmunitionWeapon)I);
				}
			}
			if(baseItems.size()==0)
				mob.tell(L("You don't seem to have that."));
			else
			if(items.size()==0)
				mob.tell(L("You can't seem to unload that."));
			else
			for(final AmmunitionWeapon W : items)
			{
				final Item ammunition=CMLib.coffeeMaker().makeAmmunition(W.ammunitionType(),W.ammunitionRemaining());
				final CMMsg newMsg=CMClass.getMsg(mob,W,ammunition,CMMsg.MSG_UNLOAD,L("<S-NAME> unload(s) <O-NAME> from <T-NAME>."));
				if(mob.location().okMessage(mob,newMsg))
					mob.location().send(mob,newMsg);
			}
		}
		else
		{
			// Area Unloading
			if((commands.get(1).equalsIgnoreCase("AREA"))
			&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDAREAS))
			&&(CMLib.map().getArea(CMParms.combine(commands,2))!=null))
			{
				final String which=CMParms.combine(commands,2);
				if(mob.session().confirm(L("Are you sure you want to unload area '@x1' (y/N)?",which), "N"))
				{
					Area A=null;
					if(which.length()>0)
						A=CMLib.map().getArea(which);
					if(A==null)
						mob.tell(L("Unknown Area '@x1'.  Use AREAS.",which));
					else
					{
						final LinkedList<Room> rooms=new LinkedList<Room>();
						for(final Enumeration<Room> r=A.getProperMap();r.hasMoreElements();)
						{
							try
							{
								final Room R=r.nextElement();
								if(R!=null)
								{
									rooms.add(R);
									CMLib.map().emptyRoom(R, null, true);
								}
							}
							catch(final Exception e)
							{
							}
						}
						for(final Iterator<Room> r=rooms.iterator();r.hasNext();)
						{
							try
							{
								final Room R=r.next();
								A.delProperRoom(R);
								if(R instanceof GridLocale)
									((GridLocale)R).clearGrid(R);
								R.destroy();
								if(R instanceof GridLocale)
									((GridLocale)R).clearGrid(null);
							}
							catch(final Exception e)
							{
							}
						}
						rooms.clear();
						A.destroy();
						CMLib.map().delArea(A);
						mob.tell(L("Area '@x1' has been unloaded.",A.Name()));
					}
				}
				return false;
			}
			else
			if((what.equalsIgnoreCase("CLASS")||(CMClass.findObjectType(what)!=null))
			&&(CMSecurity.isASysOp(mob)))
			{
				if(commands.size()<3)
				{
					mob.tell(L("Unload which @x1?",what));
					return false;
				}
				if(what.equalsIgnoreCase("CLASS"))
				{
					final Object O=CMClass.getObjectOrPrototype(commands.get(2));
					if(O!=null)
					{
						final CMClass.CMObjectType x=CMClass.getObjectType(O);
						if(x!=null)
							what=x.toString();
					}
				}
				final CMObjectType whatType=CMClass.findObjectType(what);
				if(whatType==null)
					mob.tell(L("Don't know how to load a '@x1'.  Try one of the following: @x2",what,CMParms.toListString(ARCHON_LIST)));
				else
				{
					commands.remove(0);
					commands.remove(0);
					for(int i=0;i<commands.size();i++)
					{
						final String name=commands.get(0);
						final Object O=CMClass.getObjectOrPrototype(name);
						if(!(O instanceof CMObject))
							mob.tell(L("Class '@x1' was not found in the class loader.",name));
						else
						if(!CMClass.delClass(whatType,(CMObject)O))
							mob.tell(L("Failed to unload class '@x1' from the class loader.",name));
						else
							mob.tell(L("Class '@x1' was unloaded.",name));
					}
				}
				return false;
			}
			else
			if(str.equalsIgnoreCase("help"))
			{
				final CMFile F=new CMFile("//resources/help",mob);
				if((F.exists())&&(F.canRead())&&(F.canWrite())&&(F.isDirectory()))
				{
					CMLib.help().unloadHelpFile(mob);
					for(final ClanGovernment G : CMLib.clans().getStockGovernments())
						G.setLongDesc(G.getLongDesc());
					return false;
				}
				mob.tell(L("No access to help."));
			}
			else
			if(str.equalsIgnoreCase("vfs"))
			{
				CMFile.unloadVFS();
				mob.tell(L("VFS Cache unloaded"));
			}
			else
			if(str.equalsIgnoreCase("ini")||str.equalsIgnoreCase("settings")||str.equalsIgnoreCase("inifile"))
			{
				final CMProps ipage=CMProps.instance();
				ipage.clear();
				ipage.load(CMProps.getVar(CMProps.Str.INIPATH));
				if((ipage!=null)&&(ipage.isLoaded()))
				{
					ipage.resetSystemVars();
					ipage.resetSecurityVars();
					final String normalChannels=ipage.getStr("CHANNELS");
					final String i3Channels=ipage.getBoolean("RUNI3SERVER") ? ipage.getStr("ICHANNELS") : "";
					final String imc2Channels=ipage.getBoolean("RUNIMC2CLIENT") ? ipage.getStr("IMC2CHANNELS") : "";
					CMLib.channels().loadChannels(normalChannels,i3Channels,imc2Channels);
					CMLib.journals().loadCommandJournals(ipage.getStr("COMMANDJOURNALS"));
					CMLib.journals().loadForumJournals(ipage.getStr("FORUMJOURNALS"));
					mob.tell(L("INI Settings unloaded and reset"));
				}
				else
					mob.tell(L("INI Settings not unloaded or reset"));
			}
			else
			if(str.equalsIgnoreCase("achievements"))
			{
				CMLib.achievements().shutdown();
				mob.tell(L("Achievements unloaded."));
			}
			else
			if(str.equalsIgnoreCase("awards")||str.equalsIgnoreCase("autoawards"))
			{
				CMLib.awards().shutdown();
				mob.tell(L("Auto-Awards unloaded."));
			}
			else
			if((str.equalsIgnoreCase("all"))&&(CMSecurity.isASysOp(mob)))
			{
				mob.tell(L("All soft resources unloaded."));
				CMLib.factions().removeFaction(null);
				Resources.clearResources();
				CMProps.instance().resetSecurityVars();
				CMProps.instance().resetSystemVars();
				CMLib.help().unloadHelpFile(mob);
				return false;
			}
			else
			// User Unloading
			if((commands.get(1).equalsIgnoreCase("USER")||commands.get(1).equalsIgnoreCase("PLAYER"))
			&&(mob.session()!=null))
			{
				final String which=CMParms.combine(commands,2);
				final List<MOB> users=new ArrayList<MOB>();
				if((which.equalsIgnoreCase("all"))
				&&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)))
				{
					for(final Enumeration<MOB> e=CMLib.players().players();e.hasMoreElements();)
						users.add(e.nextElement());
				}
				else
				{
					final MOB M=CMLib.players().getPlayer(which); // local, for security
					if(M==null)
					{
						mob.tell(L("No such user as '@x1'!",which));
						return false;
					}
					users.add(M);
				}
				final boolean saveFirst=mob.session().confirm(L("Save first (Y/n)?"),"Y");
				for(int u=0;u<users.size();u++)
				{
					final MOB M=users.get(u);
					if(M.session()!=null)
					{
						if(M!=mob)
						{
							if (M.session() != null)
								M.session().stopSession(true, false, false, false);
							int attempts=100;
							while ((M.session() != null)&&(--attempts>0))
							{
								CMLib.s_sleep(100);
							}
							if (M.session() != null)
								M.session().stopSession(true, true, true, false);
							attempts=100;
							while ((M.session() != null)&&(--attempts>0))
							{
								CMLib.s_sleep(100);
							}
							if (M.session() != null)
								M.session().stopSession(true, true, true, true);
							attempts=100;
							while ((M.session() != null)&&(--attempts>0))
							{
								CMLib.s_sleep(100);
							}
							if (M.session() != null)
								mob.tell(L("Session kill failed."));
						}
						else
							mob.tell(L("Can't unload yourself -- a destroy is involved, which would disrupt this process."));
					}
					if(saveFirst)
					{
						// important! shutdown their affects!
						M.delAllEffects(true);
						CMLib.database().DBUpdatePlayer(M);
						CMLib.database().DBUpdateFollowers(M);
					}
				}
				int done=0;
				for(int u=0;u<users.size();u++)
				{
					final MOB M=users.get(u);
					if(M!=mob)
					{
						done++;
						if(M.session()!=null)
							M.session().stopSession(true,true,true, true);
						final PlayerStats pStats = M.playerStats();
						if(pStats != null)
							pStats.getExtItems().delAllItems(true);
						CMLib.players().delPlayer(M);
						M.destroy();
					}
				}

				mob.tell(L("@x1 user(s) unloaded.",""+done));
				return true;
			}
			else
			// Faction Unloading
			if((commands.get(1).equalsIgnoreCase("FACTION"))
			&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDFACTIONS)))
			{
				final String which=CMParms.combine(commands,2);
				if(which.length()==0)
				{
					// No factions specified.  That's fine, they must mean ALL FACTIONS!!! hahahahaha
					CMLib.factions().removeFaction(null);
				}
				else
				{
					if(CMLib.factions().removeFaction(which))
					{
						mob.tell(L("Faction '@x1' unloaded.",which));
						return false;
					}
					mob.tell(L("Unknown Faction '@x1'.  Use LIST FACTIONS.",which));
					return false;
				}
			}
			else
			if(("EXPERTISE".startsWith(commands.get(1).toUpperCase()))
			&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.EXPERTISE)))
			{
				CMLib.expertises().recompileExpertises();
				mob.tell(L("Expertise list unloaded and reloaded."));
				return false;
			}
			else
			if(commands.get(1).equalsIgnoreCase("RESOURCE"))
			{
				String which=CMParms.combine(commands,2);
				if(which.trim().length()==0)
				{
					mob.tell(L("You need to specify a resource mask to unload, or ALL to unload all."));
					return false;
				}
				if(which.trim().equalsIgnoreCase("all"))
					which="";
				Iterator<String> k=Resources.findResourceKeys(which);
				final List<String> subKeys = new ArrayList<String>();
				if(!k.hasNext())
				{
					if(which.toLowerCase().startsWith("title"))
					{
						CMLib.awards().reloadAutoTitles();
						return false;
					}
					else
					{
						int x=which.indexOf('@');
						while(x>0)
						{
							final String w=which.substring(0,x);
							subKeys.add(w);
							which=which.substring(x+1);
							x=which.indexOf('@');
						}
						k=Resources.findResourceKeys(which);
						if(!k.hasNext())
						{
							mob.tell(L("Unknown resource '@x1'.  Use LIST RESOURCES.",which));
							return false;
						}
					}
				}
				for(;k.hasNext();)
				{
					final String key=k.next();
					boolean success=true;
					if(subKeys.size()==0)
						Resources.removeResource(key);
					else
					{
						Object o = Resources.getResource(key);
						for(int i=subKeys.size()-1;i>=0;i--)
						{
							if(i==0)
							{
								if(o instanceof Map)
									((Map)o).remove(subKeys.get(i));
								else
								if(o instanceof List)
									((List)o).remove(subKeys.get(i));
								else
								if(o instanceof Set)
									((Set)o).remove(subKeys.get(i));
								else
								if(o instanceof Resources)
									((Resources)o)._removeResource(subKeys.get(i));
								else
								{
									mob.tell(L("Can't remove "+subKeys.get(i)+" from "+o.toString()));
									success=false;
								}
							}
							else
							if(o instanceof Map)
								o=((Map)o).get(subKeys.get(i));
							else
							if(o instanceof Resources)
								o=((Resources)o)._getResource(subKeys.get(i));
							else
							{
								mob.tell(L("Can't remove "+subKeys.get(i)+" from "+o.toString()));
								success=false;
							}
						}
					}
					if(success)
					{
						if(subKeys.size()>0)
							mob.tell(L("Resource '@x1' unloaded.",CMParms.toListString(subKeys)+"@"+key));
						else
							mob.tell(L("Resource '@x1' unloaded.",key));
					}
				}
				if(which.toLowerCase().startsWith("title"))
					CMLib.awards().reloadAutoTitles();
			}
			else
			if(commands.get(1).equalsIgnoreCase("FILE"))
			{
				final String which=CMParms.combine(commands,2);
				CMFile F1=new CMFile(which,mob,CMFile.FLAG_FORCEALLOW);
				if(!F1.exists())
				{
					int x=which.indexOf(':');
					if(x<0)
						x=which.lastIndexOf(' ');
					if(x>=0)
						F1=new CMFile(which.substring(x+1).trim(),mob,CMFile.FLAG_FORCEALLOW);
				}
				if(!F1.exists())
				{
					F1=new CMFile(Resources.buildResourcePath(which),mob,CMFile.FLAG_FORCEALLOW);
					if(!F1.exists())
					{
						int x=which.indexOf(':');
						if(x<0)
							x=which.lastIndexOf(' ');
						if(x>=0)
							F1=new CMFile(Resources.buildResourcePath(which.substring(x+1).trim()),mob,CMFile.FLAG_FORCEALLOW);
					}
				}
				if(F1.exists())
				{
					final CMFile F2=new CMFile(F1.getVFSPathAndName(),mob,CMFile.FLAG_LOGERRORS);
					if((!F2.exists())||(!F2.canRead()))
					{
						mob.tell(L("Inaccessible file resource: '@x1'",which));
						return false;
					}
					final Iterator<String> k=Resources.findResourceKeys(which);
					if(!k.hasNext())
					{
						mob.tell(L("Unknown resource '@x1'.  Use LIST RESOURCES.",which));
						return false;
					}
					for(;k.hasNext();)
					{
						final String key=k.next();
						Resources.removeResource(key);
						mob.tell(L("Resource '@x1' unloaded.",key));
					}
				}
				else
					mob.tell(L("Unknown file resource: '@x1'",which));
			}
			else
			{
				CMFile F1=new CMFile(str,mob,CMFile.FLAG_FORCEALLOW);
				if(!F1.exists())
				{
					int x=str.indexOf(':');
					if(x<0)
						x=str.lastIndexOf(' ');
					if(x>=0)
						F1=new CMFile(str.substring(x+1).trim(),mob,CMFile.FLAG_FORCEALLOW);
				}
				if(!F1.exists())
				{
					F1=new CMFile(Resources.buildResourcePath(str),mob,CMFile.FLAG_FORCEALLOW);
					if(!F1.exists())
					{
						int x=str.indexOf(':');
						if(x<0)
							x=str.lastIndexOf(' ');
						if(x>=0)
							F1=new CMFile(Resources.buildResourcePath(str.substring(x+1).trim()),mob,CMFile.FLAG_FORCEALLOW);
					}
				}
				if(F1.exists())
				{
					final CMFile F2=new CMFile(F1.getVFSPathAndName(),mob,CMFile.FLAG_LOGERRORS);
					if((!F2.exists())||(!F2.canRead()))
					{
						mob.tell(L("Inaccessible file resource: '@x1'",str));
						return false;
					}
					final Iterator<String> k=Resources.findResourceKeys(str);
					if(!k.hasNext())
					{
						mob.tell(L("Unknown resource '@x1'.  Use LIST RESOURCES.",str));
						return false;
					}
					for(;k.hasNext();)
					{
						final String key=k.next();
						Resources.removeResource(key);
						mob.tell(L("Resource '@x1' unloaded.",key));
					}
				}
				else
					mob.tell(L("Unknown resource type '@x1. Try @x2.",(commands.get(1)),CMParms.toListString(ARCHON_LIST)));
			}
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return super.securityCheck(mob);
	}

	@Override
	public double combatActionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandCombatActionCost(ID());
	}

	@Override
	public double actionsCost(final MOB mob, final List<String> cmds)
	{
		return CMProps.getCommandActionCost(ID());
	}
}
