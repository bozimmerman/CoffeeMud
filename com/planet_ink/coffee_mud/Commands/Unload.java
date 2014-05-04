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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Unload extends StdCommand
{
	public Unload(){}

	private final String[] access={"UNLOAD"};
	@Override public String[] getAccessWords(){return access;}
	final String[] ARCHON_LIST={"CLASS", "HELP", "USER", "AREA", "FACTION", "ALL", "FILE", "RESOURCE", "INIFILE", "[FILENAME]"};


	@Override
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob==null) return true;
		boolean tryArchon=CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LOADUNLOAD);
		if(commands.size()<2)
		{
			if(tryArchon)
				mob.tell("UNLOAD what? Try "+CMParms.toStringList(ARCHON_LIST));
			else
				mob.tell(_("Unload what?"));
			return false;
		}
		final String str=CMParms.combine(commands,1);
		if(tryArchon)
		{
			final Item I=mob.fetchWieldedItem();
			if((I instanceof AmmunitionWeapon)&&((AmmunitionWeapon)I).requiresAmmunition())
				tryArchon=false;
			for(final String aList : ARCHON_LIST)
				if(str.equalsIgnoreCase(aList))
					tryArchon=true;
		}
		if(!tryArchon)
		{
			commands.removeElementAt(0);
			final List<Item> baseItems=CMLib.english().fetchItemList(mob,mob,null,commands,Wearable.FILTER_ANY,false);
			final List<AmmunitionWeapon> items=new XVector<AmmunitionWeapon>();
			for (Item I : baseItems)
			{
				if((I instanceof AmmunitionWeapon)&&((AmmunitionWeapon)I).requiresAmmunition())
					items.add((AmmunitionWeapon)I);
			}
			if(baseItems.size()==0)
				mob.tell(_("You don't seem to have that."));
			else
			if(items.size()==0)
				mob.tell(_("You can't seem to unload that."));
			else
			for(final AmmunitionWeapon W : items)
			{
				final Item ammunition=CMLib.coffeeMaker().makeAmmunition(W.ammunitionType(),W.ammunitionRemaining());
				final CMMsg newMsg=CMClass.getMsg(mob,W,ammunition,CMMsg.MSG_UNLOAD,_("<S-NAME> unload(s) <O-NAME> from <T-NAME>."));
				if(mob.location().okMessage(mob,newMsg))
					mob.location().send(mob,newMsg);
			}
		}
		else
		{
			String what=(String)commands.elementAt(1);
			if((what.equalsIgnoreCase("CLASS")||(CMClass.findObjectType(what)!=null))
			&&(CMSecurity.isASysOp(mob)))
			{
				if(commands.size()<3)
				{
					mob.tell("Unload which "+what+"?");
					return false;
				}
				if(what.equalsIgnoreCase("CLASS"))
				{
					final Object O=CMClass.getObjectOrPrototype((String)commands.elementAt(2));
					if(O!=null)
					{
						final CMClass.CMObjectType x=CMClass.getObjectType(O);
						if(x!=null) what=x.toString();
					}
				}
				final CMObjectType whatType=CMClass.findObjectType(what);
				if(whatType==null)
					mob.tell("Don't know how to load a '"+what+"'.  Try one of the following: "+CMParms.toStringList(ARCHON_LIST));
				else
				{
					commands.removeElementAt(0);
					commands.removeElementAt(0);
					for(int i=0;i<commands.size();i++)
					{
						final String name=(String)commands.elementAt(0);
						final Object O=CMClass.getObjectOrPrototype(name);
						if(!(O instanceof CMObject))
							mob.tell("Class '"+name+"' was not found in the class loader.");
						else
						if(!CMClass.delClass(whatType,(CMObject)O))
							mob.tell("Failed to unload class '"+name+"' from the class loader.");
						else
							mob.tell("Class '"+name+"' was unloaded.");
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
					return false;
				}
				mob.tell(_("No access to help."));
			}
			else
			if(str.equalsIgnoreCase("inifile"))
			{
				CMProps.instance().resetSecurityVars();
				CMProps.instance().resetSystemVars();
				mob.tell(_("INI file entries have been unloaded."));
			}
			else
			if((str.equalsIgnoreCase("all"))&&(CMSecurity.isASysOp(mob)))
			{
				mob.tell(_("All soft resources unloaded."));
				CMLib.factions().removeFaction(null);
				Resources.clearResources();
				CMProps.instance().resetSecurityVars();
				CMProps.instance().resetSystemVars();
				CMLib.help().unloadHelpFile(mob);
				return false;
			}
			else
			// User Unloading
			if((((String)commands.elementAt(1)).equalsIgnoreCase("USER"))
			&&(mob.session()!=null)
			&&(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.CMDPLAYERS)))
			{
				final String which=CMParms.combine(commands,2);
				final Vector users=new Vector();
				if(which.equalsIgnoreCase("all"))
					for(final Enumeration e=CMLib.players().players();e.hasMoreElements();)
						users.addElement(e.nextElement());
				else
				{
					final MOB M=CMLib.players().getPlayer(which);
					if(M==null)
					{
						mob.tell("No such user as '"+which+"'!");
						return false;
					}
					users.addElement(M);
				}
				final boolean saveFirst=mob.session().confirm("Save first (Y/n)?","Y");
				for(int u=0;u<users.size();u++)
				{
					final MOB M=(MOB)users.elementAt(u);
					if(M.session()!=null)
					{
						if(M!=mob)
						{
							if(M.session()!=null) M.session().stopSession(false,false,false);
							while(M.session()!=null){try{Thread.sleep(100);}catch(final Exception e){}}
							if(M.session()!=null) M.session().stopSession(true,true,true);
						}
						else
							mob.tell(_("Can't unload yourself -- a destroy is involved, which would disrupt this process."));
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
					final MOB M=(MOB)users.elementAt(u);
					if(M!=mob)
					{
						done++;
						if(M.session()!=null) M.session().stopSession(true,true,true);
						CMLib.players().delPlayer(M);
						M.destroy();
					}
				}

				mob.tell(done+" user(s) unloaded.");
				return true;
			}
			else
			// Faction Unloading
			if((((String)commands.elementAt(1)).equalsIgnoreCase("FACTION"))
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
						mob.tell("Faction '"+which+"' unloaded.");
						return false;
					}
					mob.tell("Unknown Faction '"+which+"'.  Use LIST FACTIONS.");
					return false;
				}
			}
			else
			// Area Unloading
			if((((String)commands.elementAt(1)).equalsIgnoreCase("AREA"))
			&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDAREAS)))
			{
				final String which=CMParms.combine(commands,2);
				Area A=null;
				if(which.length()>0)
					A=CMLib.map().getArea(which);
				if(A==null)
					mob.tell("Unknown Area '"+which+"'.  Use AREAS.");
				else
				{
					return false;
				}
			}
			else
			if(("EXPERTISE".startsWith(((String)commands.elementAt(1)).toUpperCase()))
			&&(CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.EXPERTISE)))
			{
				Resources.removeResource("skills/expertises.txt");
				CMLib.expertises().recompileExpertises();
				mob.tell(_("Expertise list unloaded and reloaded."));
				return false;
			}
			else
			if(((String)commands.elementAt(1)).equalsIgnoreCase("RESOURCE"))
			{
				final String which=CMParms.combine(commands,2);
				final Iterator<String> k=Resources.findResourceKeys(which);
				if(!k.hasNext())
				{
					mob.tell("Unknown resource '"+which+"'.  Use LIST RESOURCES.");
					return false;
				}
				for(;k.hasNext();)
				{
					final String key=k.next();
					Resources.removeResource(key);
					mob.tell("Resource '"+key+"' unloaded.");
				}
			}
			else
			if(((String)commands.elementAt(1)).equalsIgnoreCase("FILE"))
			{
				final String which=CMParms.combine(commands,2);
				CMFile F1=new CMFile(which,mob,CMFile.FLAG_FORCEALLOW);
				if(!F1.exists())
				{
					int x=which.indexOf(':');
					if(x<0) x=which.lastIndexOf(' ');
					if(x>=0) F1=new CMFile(which.substring(x+1).trim(),mob,CMFile.FLAG_FORCEALLOW);
				}
				if(!F1.exists())
				{
					F1=new CMFile(Resources.buildResourcePath(which),mob,CMFile.FLAG_FORCEALLOW);
					if(!F1.exists())
					{
						int x=which.indexOf(':');
						if(x<0) x=which.lastIndexOf(' ');
						if(x>=0) F1=new CMFile(Resources.buildResourcePath(which.substring(x+1).trim()),mob,CMFile.FLAG_FORCEALLOW);
					}
				}
				if(F1.exists())
				{
					final CMFile F2=new CMFile(F1.getVFSPathAndName(),mob,CMFile.FLAG_LOGERRORS);
					if((!F2.exists())||(!F2.canRead()))
					{
						mob.tell("Inaccessible file resource: '"+which+"'");
						return false;
					}
					final Iterator<String> k=Resources.findResourceKeys(which);
					if(!k.hasNext())
					{
						mob.tell("Unknown resource '"+which+"'.  Use LIST RESOURCES.");
						return false;
					}
					for(;k.hasNext();)
					{
						final String key=k.next();
						Resources.removeResource(key);
						mob.tell("Resource '"+key+"' unloaded.");
					}
				}
				else
					mob.tell("Unknown file resource: '"+which+"'");
			}
			else
			{
				CMFile F1=new CMFile(str,mob,CMFile.FLAG_FORCEALLOW);
				if(!F1.exists())
				{
					int x=str.indexOf(':');
					if(x<0) x=str.lastIndexOf(' ');
					if(x>=0) F1=new CMFile(str.substring(x+1).trim(),mob,CMFile.FLAG_FORCEALLOW);
				}
				if(!F1.exists())
				{
					F1=new CMFile(Resources.buildResourcePath(str),mob,CMFile.FLAG_FORCEALLOW);
					if(!F1.exists())
					{
						int x=str.indexOf(':');
						if(x<0) x=str.lastIndexOf(' ');
						if(x>=0) F1=new CMFile(Resources.buildResourcePath(str.substring(x+1).trim()),mob,CMFile.FLAG_FORCEALLOW);
					}
				}
				if(F1.exists())
				{
					final CMFile F2=new CMFile(F1.getVFSPathAndName(),mob,CMFile.FLAG_LOGERRORS);
					if((!F2.exists())||(!F2.canRead()))
					{
						mob.tell("Inaccessible file resource: '"+str+"'");
						return false;
					}
					final Iterator<String> k=Resources.findResourceKeys(str);
					if(!k.hasNext())
					{
						mob.tell("Unknown resource '"+str+"'.  Use LIST RESOURCES.");
						return false;
					}
					for(;k.hasNext();)
					{
						final String key=k.next();
						Resources.removeResource(key);
						mob.tell("Resource '"+key+"' unloaded.");
					}
				}
				else
					mob.tell("Unknown resource type '"+((String)commands.elementAt(1))+". Try "+CMParms.toStringList(ARCHON_LIST)+".");
			}
		}
		return false;
	}

	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return super.securityCheck(mob);}
	@Override public double combatActionsCost(final MOB mob, final List<String> cmds){return CMProps.getCombatActionCost(ID());}
	@Override public double actionsCost(final MOB mob, final List<String> cmds){return CMProps.getActionCost(ID());}
}
