package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

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
@SuppressWarnings("unchecked")
public class Knock extends StdCommand
{
	public Knock(){}

	private String[] access={"KNOCK"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(commands.size()<=1)
		{
			mob.tell("Knock on what?");
			return false;
		}
		String knockWhat=CMParms.combine(commands,1).toUpperCase();
		int dir=CMLib.tracking().findExitDir(mob,mob.location(),knockWhat);
		if(dir<0)
		{
			Environmental getThis=mob.location().fetchFromMOBRoomItemExit(mob,null,knockWhat,Wearable.FILTER_UNWORNONLY);
			if(getThis==null)
			{
				mob.tell("You don't see '"+knockWhat.toLowerCase()+"' here.");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,getThis,null,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,"<S-NAME> knock(s) on <T-NAMESELF>."+CMProps.msp("knock.wav",50));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);

		}
		else
		{
			Exit E=mob.location().getExitInDir(dir);
			if(E==null)
			{
				mob.tell("Knock on what?");
				return false;
			}
			if(!E.hasADoor())
			{
				mob.tell("You can't knock on "+E.name()+"!");
				return false;
			}
			CMMsg msg=CMClass.getMsg(mob,E,null,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,CMMsg.MSG_KNOCK,"<S-NAME> knock(s) on <T-NAMESELF>."+CMProps.msp("knock.wav",50));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				E=mob.location().getPairedExit(dir);
				Room R=mob.location().getRoomInDir(dir);
				if((R!=null)&&(E!=null)&&(E.hasADoor())
				&&(R.showOthers(mob,E,null,CMMsg.MSG_KNOCK,"You hear a knock on <T-NAMESELF>."+CMProps.msp("knock.wav",50)))
				&&((R.domainType()&Room.INDOORS)==Room.INDOORS))
				{
					Vector V=new Vector();
					V.addElement(mob.location());
					TrackingLibrary.TrackingFlags flags;
					flags = new TrackingLibrary.TrackingFlags()
							.add(TrackingLibrary.TrackingFlag.OPENONLY);
					CMLib.tracking().getRadiantRooms(R,V,flags,null,5,null);
					V.removeElement(mob.location());
					for(int v=0;v<V.size();v++)
					{
						Room R2=(Room)V.elementAt(v);
						int dir2=CMLib.tracking().radiatesFromDir(R2,V);
						if((dir2>=0)&&((R2.domainType()&Room.INDOORS)==Room.INDOORS))
						{
							Room R3=R2.getRoomInDir(dir2);
							if(((R3!=null)&&(R3.domainType()&Room.INDOORS)==Room.INDOORS))
								R2.showHappens(CMMsg.MASK_SOUND|CMMsg.TYP_KNOCK,"You hear a knock "+Directions.getInDirectionName(dir2)+"."+CMProps.msp("knock.wav",50));
						}
					}
				}
			}
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
