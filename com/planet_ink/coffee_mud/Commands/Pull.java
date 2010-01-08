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
public class Pull extends Go
{
	public Pull(){}

	private String[] access={"PULL"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		
		Environmental openThis=null;
		String dir="";
		int dirCode=-1;
		Environmental E=null;
		if(commands.size()>1)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
			{
			    if((mob.location().getRoomInDir(dirCode)==null)
			    ||(mob.location().getExitInDir(dirCode)==null)
			    ||(!mob.location().getExitInDir(dirCode).isOpen()))
			    {
			        mob.tell("You can't pull anything that way.");
			        return false;
			    }
			    E=mob.location().getRoomInDir(dirCode);
			    dir=" "+Directions.getDirectionName(dirCode);
			    commands.removeElementAt(commands.size()-1);
			}
		}
		if(dir.length()==0)
		{
			dirCode=Directions.getGoodDirectionCode((String)commands.lastElement());
			if(dirCode>=0)
				openThis=mob.location().getExitInDir(dirCode);
		}
		String whatToOpen=CMParms.combine(commands,1);
		if(openThis==null)
			openThis=mob.location().fetchFromMOBRoomFavorsItems(mob,null,whatToOpen,Wearable.FILTER_ANY);
		if((openThis==null)||(!CMLib.flags().canBeSeenBy(openThis,mob)))
		{
			mob.tell("You don't see '"+whatToOpen+"' here.");
			return false;
		}
		CMMsg msg=CMClass.getMsg(mob,openThis,E,CMMsg.MSG_PULL,"<S-NAME> pull(s) <T-NAME>"+dir+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
		    if((dir.length()>0)&&(msg.tool() instanceof Room))
		    {
		        Room R=(Room)msg.tool();
		        dirCode=CMLib.tracking().findRoomDir(mob,R);
		        if((dirCode>=0)&&(super.move(mob,dirCode,false,false,false,false)))
		        {
			        if(openThis instanceof Item)
			            R.bringItemHere((Item)openThis,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP),true);
			        else
			        if(openThis instanceof MOB)
			            move((MOB)openThis,dirCode,((MOB)openThis).isInCombat(),false,true,true);
		        }
		    }
		}
		return false;
	}
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCMDTIME),100.0);}
	public boolean canBeOrdered(){return true;}

	
}
