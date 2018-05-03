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

public class Look extends StdCommand
{
	public Look(){}

	private final String[]	access	= I(new String[] { "LOOK", "LOO", "LO", "L" });

	@Override
	public String[] getAccessWords()
	{
		return access;
	}

	@Override
	public boolean execute(MOB mob, List<String> commands, int metaFlags)
		throws java.io.IOException
	{
		Vector<String> origCmds=new XVector<String>(commands);
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
		if((commands!=null)&&(commands.size()>1))
		{
			int dirCode=-1;
			Environmental thisThang=null;
			Environmental lookingTool=null;

			if((commands.size()>2)&&(commands.get(1).equalsIgnoreCase("at")))
				commands.remove(1);
			else
			if((commands.size()>2)&&(commands.get(1).equalsIgnoreCase("to")))
				commands.remove(1);
			final String ID=CMParms.combine(commands,1);

			if((ID.toUpperCase().startsWith("EXIT"))&&(commands.size()==2)&&(CMProps.getIntVar(CMProps.Int.EXVIEW)!=CMProps.Int.EXVIEW_PARAGRAPH))
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
			
			if(thisThang==null)
				thisThang=R.fetchFromMOBRoomFavorsItems(mob,null,ID, noCoinFilter);
			if(thisThang==null)
				thisThang=R.fetchFromMOBRoomFavorsItems(mob,null,ID,Wearable.FILTER_ANY);
			if((thisThang==null)
			&&(commands.size()>2)
			&&(commands.get(1).equalsIgnoreCase("in")))
			{
				commands.remove(1);
				final String ID2=CMParms.combine(commands,1);
				thisThang=R.fetchFromMOBRoomFavorsItems(mob,null,ID2,Wearable.FILTER_ANY);
				if((thisThang!=null)&&((!(thisThang instanceof Container))||(((Container)thisThang).capacity()==0)))
				{
					CMLib.commands().postCommandFail(mob,origCmds,L("That's not a container."));
					return false;
				}
			}
			if(thisThang == null)
			{
				final CMFlagLibrary flagLib=CMLib.flags();
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
			if(thisThang!=null)
			{
				String name="at <T-NAMESELF>";
 				if((thisThang instanceof Room)||(thisThang instanceof Exit))
				{
					if(thisThang==R)
						name="around";
					else
					if(dirCode>=0)
						name=((R instanceof BoardableShip)||(R.getArea() instanceof BoardableShip))?
								CMLib.directions().getShipDirectionName(dirCode):CMLib.directions().getDirectionName(dirCode);
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
			}
			if(R.okMessage(mob,msg))
				R.send(mob,msg);
		}
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}
}
