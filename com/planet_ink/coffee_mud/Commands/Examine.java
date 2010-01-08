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
public class Examine extends StdCommand
{
    public Examine(){}

    private String[] access={"EXAMINE","EXAM","EXA","LONGLOOK","LLOOK","LL"};
    public String[] getAccessWords(){return access;}
    public boolean execute(MOB mob, Vector commands, int metaFlags)
        throws java.io.IOException
    {
        boolean quiet=false;
        if((commands!=null)&&(commands.size()>1)&&(((String)commands.lastElement()).equalsIgnoreCase("UNOBTRUSIVELY")))
        {
            commands.removeElementAt(commands.size()-1);
            quiet=true;
        }
        String textMsg="<S-NAME> examine(s) ";
        if(mob.location()==null) return false;
        if((commands!=null)&&(commands.size()>1))
        {
            Environmental thisThang=null;
            
            String ID=CMParms.combine(commands,1);
            if(ID.length()==0)
                thisThang=mob.location();
            else
            if((ID.toUpperCase().startsWith("EXIT")&&(commands.size()==2)))
            {
                CMLib.commands().lookAtExits(mob.location(),mob);
                return false;
            }
            if(ID.equalsIgnoreCase("SELF")||ID.equalsIgnoreCase("ME"))
                thisThang=mob;
            
            if(thisThang==null)
                thisThang=mob.location().fetchFromMOBRoomFavorsItems(mob,null,ID,Wearable.FILTER_ANY);
            int dirCode=-1;
            if(thisThang==null)
            {
                dirCode=Directions.getGoodDirectionCode(ID);
                if(dirCode>=0)
                {
                    Room room=mob.location().getRoomInDir(dirCode);
                    Exit exit=mob.location().getExitInDir(dirCode);
                    if((room!=null)&&(exit!=null))
                        thisThang=exit;
                    else
                    {
                        mob.tell("You don't see anything that way.");
                        return false;
                    }
                }
            }
            if(thisThang!=null)
            {
                String name="<T-NAMESELF>";
                if((thisThang instanceof Room)||(thisThang instanceof Exit))
                {
                    if(thisThang==mob.location())
                        name="around";
                    else
                    if(dirCode>=0)
                        name=Directions.getDirectionName(dirCode);
                }
                CMMsg msg=CMClass.getMsg(mob,thisThang,null,CMMsg.MSG_EXAMINE,textMsg+name+" closely.");
                if(mob.location().okMessage(mob,msg))
                    mob.location().send(mob,msg);
                if((thisThang instanceof Room)&&(CMath.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS)))
                    CMLib.commands().lookAtExits((Room)thisThang,mob);
            }
            else
                mob.tell("You don't see that here!");
        }
        else
        {
            CMMsg msg=CMClass.getMsg(mob,mob.location(),null,CMMsg.MSG_EXAMINE,(quiet?null:textMsg+"around carefully."),CMMsg.MSG_EXAMINE,(quiet?null:textMsg+"at you."),CMMsg.MSG_EXAMINE,(quiet?null:textMsg+"around carefully."));
            if(mob.location().okMessage(mob,msg))
                mob.location().send(mob,msg);
            if((CMath.bset(mob.getBitmap(),MOB.ATT_AUTOEXITS))
            &&(CMLib.flags().canBeSeenBy(mob.location(),mob)))
                CMLib.commands().lookAtExits(mob.location(),mob);
        }
        return false;
    }
    public double combatActionsCost(MOB mob, Vector cmds){return CMath.div(CMProps.getIntVar(CMProps.SYSTEMI_DEFCOMCMDTIME),100.0);}
    public double actionsCost(MOB mob, Vector cmds){return 1.0;}
    public boolean canBeOrdered(){return true;}
}
