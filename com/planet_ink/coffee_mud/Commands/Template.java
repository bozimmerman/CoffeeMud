package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Session.InputCallback;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CataData;
import com.planet_ink.coffee_mud.Libraries.interfaces.CatalogLibrary.CatalogKind;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2019-2020 Bo Zimmerman

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
public class Template extends StdCommand
{
	public Template()
	{
	}

	private final String[]	access	= I(new String[] { "TEMPLATE" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public CMClass.CMObjectType getObjectType(final List<String> commands)
	{
		CMClass.CMObjectType whatKind=null;
		if((commands.size()>0)&&("MOBS".startsWith(commands.get(0).toUpperCase().trim())))
		{
			commands.remove(0);
			whatKind = CMClass.CMObjectType.MOB;
		}
		if((commands.size()>0)&&("ITEMS".startsWith(commands.get(0).toUpperCase().trim())))
		{
			commands.remove(0);
			whatKind = CMClass.CMObjectType.ITEM;
		}
		if((commands.size()>0)&&("ROOMS".startsWith(commands.get(0).toUpperCase().trim())))
		{
			commands.remove(0);
			whatKind = CMClass.CMObjectType.LOCALE;
		}
		if((commands.size()>0)&&("LOCALES".startsWith(commands.get(0).toUpperCase().trim())))
		{
			commands.remove(0);
			whatKind = CMClass.CMObjectType.LOCALE;
		}
		if((commands.size()>0)&&("EXITS".startsWith(commands.get(0).toUpperCase().trim())))
		{
			commands.remove(0);
			whatKind = CMClass.CMObjectType.EXIT;
		}
		return whatKind;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		Room R=mob.location();
		if(R==null)
			return false;
		if((commands!=null)&&(commands.size()>1))
		{
			commands.remove(0);
			if(commands.get(0).equalsIgnoreCase("LIST"))
			{
				commands.remove(0);
				final CMClass.CMObjectType whatKind=getObjectType(commands);
				final String mask=CMParms.combine(commands,0);
				final StringBuffer list=new StringBuffer("");
				final List<Triad<String,String,String>> dats = CMLib.catalog().getBuilderTemplateList(mob.Name());
				int tot =  CMLib.lister().fixColWidth(78, mob.session());
				int numCols = (int)Math.round(CMath.floor(CMath.div(tot, 78)));
				if(numCols == 0)
					numCols=1;
				int col=0;
				final int col1 = CMLib.lister().fixColWidth(15, mob.session());
				final int col2 = CMLib.lister().fixColWidth(9, mob.session());
				final int col3 = CMLib.lister().fixColWidth(50, mob.session());
				for(int ttyp = 0; ttyp < 3; ttyp++)
				{
					final Filterer<Triad<String,String,String>> filter;
					switch(ttyp)
					{
					case 0:
						filter = new Filterer<Triad<String,String,String>>()
						{
							@Override
							public boolean passesFilter(Triad<String,String,String> obj)
							{
								return obj.second.startsWith(" ")
										&&((whatKind==null)||(whatKind.name().equalsIgnoreCase(obj.second.substring(1))))
										&&((mask==null)||(mask.length()==0)||(obj.first.indexOf(mask.toUpperCase())>=0));
							}
						};
						break;
					case 1:
						filter = new Filterer<Triad<String,String,String>>()
						{
							@Override
							public boolean passesFilter(Triad<String,String,String> obj)
							{
								return obj.second.startsWith("+")
									&&((whatKind==null)||(whatKind.name().equalsIgnoreCase(obj.second.substring(1))))
									&&((mask==null)||(mask.length()==0)||(obj.first.indexOf(mask.toUpperCase())>=0));
							}
						};
						break;
					case 2:
						filter = new Filterer<Triad<String,String,String>>()
						{
							@Override
							public boolean passesFilter(Triad<String,String,String> obj)
							{
								return obj.second.startsWith("*")
									&&((whatKind==null)||(whatKind.name().equalsIgnoreCase(obj.second.substring(1))))
									&&((mask==null)||(mask.length()==0)||(obj.first.indexOf(mask.toUpperCase())>=0));
							}
						};
						break;
					default:
						mob.tell(L("Something impossible happened."));
						return false;
					}
					boolean yep=false;
					for(final Triad<String, String, String> dat : dats)
					{
						if(filter.passesFilter(dat))
						{
							yep=true;
							break;
						}
					}
					if(yep)
					{
						switch(ttyp)
						{
						case 0:
							list.append(L("\n\r^HPersonal and Private template objects^N\n\r"));
							break;
						case 1:
							list.append(L("\n\r^HPersonal Shared template objects^N\n\r"));
							break;
						case 2:
							list.append(L("\n\r^HOther folks shared template objects^N\n\r"));
							break;
						}
						for(int i=0;i<numCols;i++)
						{
							list.append(CMStrings.padRight(L("ID"),col1)+" ");
							list.append(CMStrings.padRight("Type",col2)+" ");
							list.append(CMStrings.padRight(L("Name"),col3)+" ");
						}
						list.append("\n\r");
						list.append(CMStrings.repeat('-', tot)).append("\n\r");
						for(final Triad<String, String, String> dat : dats)
						{
							if(filter.passesFilter(dat))
							{
								list.append(CMStrings.padRight(dat.first, col1)+" ");
								list.append(CMStrings.padRight(dat.second.substring(1),col2)+" ");
								list.append(CMStrings.padRight(dat.third,col3));
								if(++col>numCols)
								{
									col=0;
									list.append("\n\r");
								}
								else
									list.append(" ");
							}
						}
					}
					if((list.length()>0)&&(list.charAt(list.length()-1)!='\r'))
						list.append("\n\r");
				}
				list.append("\n\r");
				if(mob.session()!=null)
					mob.session().wraplessPrintln(list.toString());
			}
			else
			if(commands.get(0).equalsIgnoreCase("DELETE"))
			{
				commands.remove(0);
				if(commands.size()==0)
				{
					mob.tell(L("Delete which template entry?"));
					return false;
				}
				final String ID=CMParms.combine(commands,0);
				final List<Triad<String,String,String>> dats = CMLib.catalog().getBuilderTemplateList(mob.Name());
				for(final Triad<String,String,String> dat : dats)
				{
					if(dat.first.equalsIgnoreCase(ID))
					{
						if(!CMLib.catalog().deleteBuilderTemplateObject(mob.Name(), ID))
						{
							mob.tell(L("The delete failed for some reason!"));
							return false;
						}
						mob.tell(L("Template object '@x1' has been deleted!",ID));
						return false;
					}
				}
				mob.tell(L("Template object '@x1' was not found.  Delete failed.",ID));
				return false;
			}
			else
			if(commands.get(0).equalsIgnoreCase("TOGGLE"))
			{
				commands.remove(0);
				if(commands.size()==0)
				{
					mob.tell(L("Toggle the share access status of which template entry?"));
					return false;
				}
				final String ID=CMParms.combine(commands,0);
				final List<Triad<String,String,String>> dats = CMLib.catalog().getBuilderTemplateList(mob.Name());
				for(final Triad<String,String,String> dat : dats)
				{
					if(dat.first.equalsIgnoreCase(ID))
					{
						if(!CMLib.catalog().toggleBuilderTemplateObject(mob.Name(), dat.first))
						{
							mob.tell(L("The toggle failed for some reason!"));
							return false;
						}
						if(dat.second.startsWith("+")||dat.second.startsWith("*"))
							mob.tell(L("Template object '@x1' is no longer shared!",ID));
						else
							mob.tell(L("Template object '@x1' is now shared!",ID));
						return false;
					}
				}
				mob.tell(L("Template object '@x1' was not found.  Delete failed.",ID));
				return false;
			}
			else
			if(commands.get(0).equalsIgnoreCase("ADD"))
			{
				commands.remove(0);
				if(commands.size()==0)
				{
					mob.tell(L("Add which id of what object?"));
					return false;
				}
				String possID=commands.get(0);
				commands.remove(0);
				final String ID = CMLib.catalog().makeValidNewBuilderTemplateID(possID);
				if(ID==null)
				{
					mob.tell(L("I'm afraid '@x1' is not a valid available ID.",possID));
					return false;
				}
				if(commands.size()==0)
				{
					mob.tell(L("Add what object?"));
					return false;
				}
				final List<Triad<String,String,String>> dats = CMLib.catalog().getBuilderTemplateList(mob.Name());
				for(final Triad<String,String,String> dat : dats)
				{
					if(dat.first.equalsIgnoreCase(ID))
					{
						mob.tell(L("That ID is already in use!"));
						return false;
					}
				}
				final String what=CMParms.combine(commands,0);
				final Environmental E;
				if((what.equalsIgnoreCase("here"))
				||(what.equals("room")))
					E=mob.location();
				else
				if(CMLib.map().getRoom(what)!=null)
					E=CMLib.map().getRoom(what);
				else
					E=mob.location().fetchFromMOBRoomFavorsItems(mob, null, what, Filterer.ANYTHING);
				if(E==null)
				{
					mob.tell(L("You don't see '@x1' here.",what));
					return false;
				}
				if(!CMLib.catalog().addNewBuilderTemplateObject(mob.Name(), ID.toUpperCase().trim(), E))
				{
					mob.tell(L("Unable to add @x1 (@x2) to your templates.  Is it a valid type?.",E.Name(), E.ID()));
					return false;
				}
				mob.tell(L("Added @x1 (@x2) to your templates.",E.Name(), E.ID()));
			}
			else
			{
				mob.tell(L("Template huh? Try "
						+ "TEMPLATE LIST (MOBS/ITEMS/EXITS/ROOMS) (MASK), "
						+ "TEMPLATE ADD [NEW ID] [mob/item name/exit direction/here or room id], "
						+ "TEMPLATE DELETE [ID], "
						+ "TEMPLATE TOGGLE [ID]"));
			}
		}
		else
		{
			mob.tell(L("Template huh? Try "
					+ "TEMPLATE LIST (MOBS/ITEMS/EXITS/ROOMS) (MASK), "
					+ "TEMPLATE ADD [NEW ID] [mob/item name/exit direction/here or room id], "
					+ "TEMPLATE DELETE [ID], "
					+ "TEMPLATE TOGGLE [ID]"));
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return false;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDITEMS)
			||CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDROOMS)
			||CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDEXITS)
			||CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDMOBS);
	}
}
