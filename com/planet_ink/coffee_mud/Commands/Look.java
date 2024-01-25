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
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2004-2024 Bo Zimmerman

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
public class Look extends StdCommand
{
	public Look()
	{
	}

	private final String[]	access	= I(new String[] { "LOOK", "LOO", "LO", "L" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	public boolean listItems(final MOB mob, final List<String> args)
	{
		return true;
	}

	@Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		final Vector<String> origCmds=new XVector<String>(commands);
		final Room R=mob.location();
		boolean quiet=false;
		if((commands!=null)
		&&(commands.size()>1)
		&&(commands.get(commands.size()-1).equalsIgnoreCase("UNOBTRUSIVELY")))
		{
			commands.remove(commands.size()-1);
			quiet=true;
		}
		final String textMsg="<S-NAME> look(s) ";
		if(R==null)
			return false;
		boolean listItems=false;
		boolean listAlls=false;
		if((commands!=null)&&(commands.size()>1))
		{
			int dirCode=-1;
			Environmental thisThang=null;
			Environmental lookingTool=null;

			if((commands.size()>1)&&(commands.get(1).equalsIgnoreCase("around")))
			{
				commands.remove(1);
				thisThang=mob.location();
			}
			else
			if((commands.size()>2)&&(commands.get(1).equalsIgnoreCase("for")))
			{
				commands.remove(1);
				if((commands.size()>1)&&(commands.get(1).equalsIgnoreCase("all")))
				{
					commands.remove(1);
					listAlls=true;
				}
				listItems=true;
			}
			else
			if((commands.size()>2)&&(commands.get(1).equalsIgnoreCase("to")))
				commands.remove(1);

			final String ID=CMParms.combine(commands,1);
			if((ID.toUpperCase().startsWith("EXIT"))
			&&(commands.size()==2)
			&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
			{
				final CMMsg exitMsg=CMClass.getMsg(mob,R,null,CMMsg.MSG_LOOK_EXITS,null);
				if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=CMProps.Int.EXVIEW_MIXED)!=mob.isAttributeSet(MOB.Attrib.BRIEF))
					exitMsg.setValue(CMMsg.MASK_OPTIMIZE);
				if(R.okMessage(mob, exitMsg))
					R.send(mob, exitMsg);
				return false;
			}
			if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
				thisThang=mob;

			dirCode=CMLib.directions().getStrictDirectionCode(ID);
			if(dirCode>=0)
			{
				final Room room=R.getRoomInDir(dirCode);
				final Exit exit=R.getExitInDir(dirCode);
				if((room!=null)&&(exit!=null))
				{
					thisThang=exit;
					lookingTool=room;
				}
			}
			if((thisThang==null)
			&&(commands.size()>3))
			{

				int fromDex=-1;
				for(int i=commands.size()-2;i>=1;i--)
				{
					if(commands.get(i).equalsIgnoreCase("from")
					|| commands.get(i).equalsIgnoreCase("in")
					|| commands.get(i).equalsIgnoreCase("on"))
					{
						fromDex=i;
						break;
					}
				}
				if(( fromDex > 1) && (fromDex < commands.size()-1))
				{
					final List<String> tempCmds=new XVector<String>(commands);
					final Item containerC=CMLib.english().parsePossibleContainer(mob,tempCmds,true,Wearable.FILTER_ANY);
					if(containerC!=null)
					{
						final String tempID=CMParms.combine(tempCmds,1);
						thisThang=R.fetchFromMOBRoomFavorsItems(mob, containerC, tempID, noCoinFilter);
						if(thisThang==null)
							thisThang=R.fetchFromMOBRoomFavorsItems(mob, containerC, tempID, Wearable.FILTER_ANY);
					}
				}
			}

			if(thisThang==null)
				thisThang=R.fetchFromMOBRoomFavorsItems(mob, null, ID, noCoinFilter);
			if(thisThang==null)
				thisThang=R.fetchFromMOBRoomFavorsItems(mob, null, ID, Wearable.FILTER_ANY);
			if((thisThang==null)
			&&(commands.size() > 2)
			&&(commands.get(1).equalsIgnoreCase("in")))
			{
				commands.remove(1);
				final String ID2=CMParms.combine(commands,1);
				thisThang=R.fetchFromMOBRoomFavorsItems(mob,null,ID2,Wearable.FILTER_ANY);
				if((thisThang!=null)
				&&((!(thisThang instanceof Container))||(((Container)thisThang).capacity()==0)))
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("That's not a container."));
					return false;
				}
			}
			final CMFlagLibrary flagLib=CMLib.flags();
			if(thisThang == null)
			{
				for(int i=0;i<R.numItems();i++)
				{
					final Item I=R.getItem(i);
					if(flagLib.isOpenAccessibleContainer(I))
					{
						thisThang=R.fetchFromRoomFavorItems(I, ID);
						if(thisThang != null)
							break;
					}
				}
			}
			if(thisThang==null)
			{
				dirCode=CMLib.directions().getGoodDirectionCode(ID);
				if(dirCode>=0)
				{
					final Room room=R.getRoomInDir(dirCode);
					final Exit exit=R.getExitInDir(dirCode);
					if((room!=null)&&(exit!=null))
					{
						thisThang=exit;
						lookingTool=room;
					}
					else
					{
						CMLib.commands().postCommandFail(mob,origCmds,L("You don't see anything that way."));
						return false;
					}
				}
			}
			if((thisThang!=null)&&(listItems))
			{
				final List<String> things=new ArrayList<String>();
				if(listAlls)
				{
					for(final Enumeration<Item> i=R.items();i.hasMoreElements();)
					{
						final Item I=i.nextElement();
						if((I!=null)
						&&((I.container()==null)
							||((flagLib.isOpenAccessibleContainer(I.container())
								&&(I.container().container()==null)
								&&(flagLib.canBeSeenBy(I.container(), mob)))))
						&&(CMLib.english().containsString(I.name(mob), ID)
							||CMLib.english().containsString(I.displayText(mob), ID))
						&&(flagLib.canBeSeenBy(I, mob)))
						{
							String name = R.getContextName(I);
							if(I.container() != null)
								name += " in " + R.getContextName(I.container());
							things.add("^I"+name+"^?");
						}
					}
					for(final Enumeration<MOB> i=R.inhabitants();i.hasMoreElements();)
					{
						final MOB M = i.nextElement();
						if((M!=null)
						&&(M!=mob)
						&&(CMLib.english().containsString(M.name(mob), ID)
							||CMLib.english().containsString(M.displayText(mob), ID))
						&&(flagLib.canBeSeenBy(M, mob)))
						{
							final String name = R.getContextName(M);
							things.add("^M"+name+"^?");
						}
					}
					for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
					{
						if(R.getRoomInDir(d)!=null)
						{
							final Exit E = R.getExitInDir(d);
							if((E!=null)
							&&(CMLib.english().containsString(E.name(mob), ID)
								||CMLib.english().containsString(E.displayText(mob), ID))
							&&(flagLib.canBeSeenBy(E, mob)))
							{
								final String name = R.getContextName(E);
								things.add("^D"+name+"^?");
							}
						}
					}
				}
				else
				{
					String name = R.getContextName(thisThang);
					if((thisThang != null) && (((Item)thisThang).container() != null))
						name += " in " + R.getContextName(((Item)thisThang).container());
					things.add(name);
				}
				if(things.size()==0)
					mob.tell(L("Nothing like that catches your eye."));
				else
				{
					final String list = CMLib.english().toEnglishStringList(things);
					final CMMsg msg=CMClass.getMsg(mob,null,lookingTool,CMMsg.MSG_GLANCE,L("You spot '@x1'.",list));
					if(R.okMessage(mob, msg))
						R.send(mob, msg);
				}
			}
			else
			if(thisThang!=null)
			{
				String name="at <T-NAMESELF>";
 				if((thisThang instanceof Room)||(thisThang instanceof Exit))
				{
					if(thisThang==R)
						name="around";
					else
					if(dirCode>=0)
						name=CMLib.directions().getDirectionName(dirCode, CMLib.flags().getDirType(R));
				}
				final CMMsg msg=CMClass.getMsg(mob,thisThang,lookingTool,CMMsg.MSG_LOOK,textMsg+name+".");
				if((thisThang instanceof Room)
				&&(mob.isAttributeSet(MOB.Attrib.AUTOEXITS))
				&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
				{
					final CMMsg exitMsg=CMClass.getMsg(mob,thisThang,lookingTool,CMMsg.MSG_LOOK_EXITS,null);
					if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=CMProps.Int.EXVIEW_MIXED)!=mob.isAttributeSet(MOB.Attrib.BRIEF))
						exitMsg.setValue(CMMsg.MASK_OPTIMIZE);
					msg.addTrailerMsg(exitMsg);
				}
				if(R.okMessage(mob,msg))
					R.send(mob,msg);
				else
					CMLib.commands().postCommandRejection(msg.source(),msg.target(),msg.tool(),origCmds);
			}
			else
				CMLib.commands().postCommandFail(mob,origCmds,L("You don't see that here!"));
		}
		else
		{
			if((commands!=null)&&(commands.size()>0))
			{
				if(commands.get(0).toUpperCase().startsWith("E"))
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("Examine what?"));
					return false;
				}
			}

			final CMMsg msg=CMClass.getMsg(mob,R,null,CMMsg.MSG_LOOK,(quiet?null:textMsg+"around."),CMMsg.MSG_LOOK,(quiet?null:textMsg+"at you."),CMMsg.MSG_LOOK,(quiet?null:textMsg+"around."));
			if((mob.isAttributeSet(MOB.Attrib.AUTOEXITS))
			&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH)
			&&(CMLib.flags().canBeSeenBy(R,mob)))
			{
				final CMMsg exitMsg=CMClass.getMsg(mob,R,null,CMMsg.MSG_LOOK_EXITS,null);
				if((CMProps.getIntVar(CMProps.Int.EXVIEW)>=CMProps.Int.EXVIEW_MIXED)!=mob.isAttributeSet(MOB.Attrib.BRIEF))
					exitMsg.setValue(CMMsg.MASK_OPTIMIZE);
				msg.addTrailerMsg(exitMsg);
				if(R.okMessage(mob,msg))
				{
					if(msg.target() instanceof Room)
					{
						exitMsg.setTarget(msg.target());
						((Room)msg.target()).send(mob,msg);
					}
					else
						R.send(mob,msg);
				}
				else
					CMLib.commands().postCommandRejection(msg.source(),msg.target(),msg.tool(),origCmds);
			}
			else
			if(R.okMessage(mob,msg))
				((Room)msg.target()).send(mob,msg);
			else
				CMLib.commands().postCommandRejection(msg.source(),msg.target(),msg.tool(),origCmds);
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
